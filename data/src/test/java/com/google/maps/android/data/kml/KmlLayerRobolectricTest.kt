/*
 * Copyright 2026 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.google.maps.android.data.kml

import android.app.Activity
import android.graphics.Bitmap
import android.graphics.Color
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.robolectric.ktx.createTestMapView
import com.google.maps.android.robolectric.ktx.getMapBlocking
import com.google.maps.android.robolectric.ktx.markers
import com.google.maps.android.robolectric.ktx.polylines
import com.google.maps.android.robolectric.ktx.shadow
import com.google.maps.android.robolectric.shadows.ShadowBitmapDescriptor
import com.google.maps.android.robolectric.shadows.ShadowBitmapDescriptorFactory
import com.google.maps.android.robolectric.shadows.ShadowCameraUpdate
import com.google.maps.android.robolectric.shadows.ShadowCameraUpdateFactory
import com.google.maps.android.robolectric.shadows.ShadowCircle
import com.google.maps.android.robolectric.shadows.ShadowGoogleMap
import com.google.maps.android.robolectric.shadows.ShadowGroundOverlay
import com.google.maps.android.robolectric.shadows.ShadowMapView
import com.google.maps.android.robolectric.shadows.ShadowMapsInitializer
import com.google.maps.android.robolectric.shadows.ShadowMarker
import com.google.maps.android.robolectric.shadows.ShadowPolygon
import com.google.maps.android.robolectric.shadows.ShadowPolyline
import com.google.maps.android.robolectric.shadows.ShadowProjection
import com.google.maps.android.robolectric.shadows.ShadowSupportMapFragment
import com.google.maps.android.robolectric.shadows.ShadowTileOverlay
import com.google.maps.android.robolectric.shadows.ShadowUiSettings
import com.google.maps.android.robolectric.truth.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.nio.charset.StandardCharsets
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import com.google.common.truth.Truth.assertThat as truthAssertThat

/**
 * Stateful Robolectric verification suite for [KmlLayer] compressed KMZ archive icon extraction,
 * ground overlay lifecycle cleanup, `<Document>` placemark traversal, and `<MultiGeometry>` styles.
 *
 * Note: Maps shadows are declared directly on `@Config(shadows = [...])` because Robolectric's
 * `ConfigMerger` does not traverse meta-annotations, avoiding global `robolectric.properties` pollution.
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(
    sdk = [34],
    shadows = [
        ShadowMapView::class,
        ShadowGoogleMap::class,
        ShadowCameraUpdate::class,
        ShadowCameraUpdateFactory::class,
        ShadowMarker::class,
        ShadowPolygon::class,
        ShadowPolyline::class,
        ShadowProjection::class,
        ShadowCircle::class,
        ShadowUiSettings::class,
        ShadowBitmapDescriptor::class,
        ShadowBitmapDescriptorFactory::class,
        ShadowMapsInitializer::class,
        ShadowSupportMapFragment::class,
        ShadowGroundOverlay::class,
        ShadowTileOverlay::class,
    ],
)
class KmlLayerRobolectricTest {

    private lateinit var activity: Activity
    private lateinit var googleMap: GoogleMap

    @Before
    fun setUp() {
        activity = Robolectric.buildActivity(Activity::class.java).create().get()
        val mapView = createTestMapView(width = 400, height = 400, activity = activity).apply {
            onStart()
            onResume()
        }
        googleMap = mapView.getMapBlocking()
    }

    @Test
    fun kmlLayer_withKmzArchive_extractsEmbeddedBitmapAndRendersCyanIconOnMap() {
        val pinLocation = LatLng(37.422, -122.084)
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(pinLocation, 16f))

        val kmlXml =
            """
            <?xml version="1.0" encoding="UTF-8"?>
            <kml xmlns="http://www.opengis.net/kml/2.2">
              <Document>
                <Style id="customPin">
                  <IconStyle>
                    <Icon><href>images/pin.png</href></Icon>
                  </IconStyle>
                </Style>
                <Placemark>
                  <name>KMZ Pin</name>
                  <styleUrl>#customPin</styleUrl>
                  <Point><coordinates>-122.084,37.422,0</coordinates></Point>
                </Placemark>
              </Document>
            </kml>
            """.trimIndent()

        val embeddedBitmap = Bitmap.createBitmap(24, 24, Bitmap.Config.ARGB_8888).apply {
            eraseColor(Color.CYAN)
        }
        val kmzBytes = ByteArrayOutputStream().use { baos ->
            ZipOutputStream(baos).use { zos ->
                zos.putNextEntry(ZipEntry("doc.kml"))
                zos.write(kmlXml.toByteArray(StandardCharsets.UTF_8))
                zos.closeEntry()

                zos.putNextEntry(ZipEntry("images/pin.png"))
                embeddedBitmap.compress(Bitmap.CompressFormat.PNG, 100, zos)
                zos.closeEntry()
            }
            baos.toByteArray()
        }

        val layer = kmlLayer(
            map = googleMap,
            stream = ByteArrayInputStream(kmzBytes),
            context = activity,
        )
        layer.addLayerToMap()

        assertThat(googleMap).hasMarkerCount(1)
        val marker = googleMap.markers.single()
        assertThat(marker).hasTitle("KMZ Pin").isAt(pinLocation)

        val renderedIcon = checkNotNull(marker.shadow.iconBitmap)
        truthAssertThat(renderedIcon.width).isEqualTo(24)
        truthAssertThat(renderedIcon.height).isEqualTo(24)
        truthAssertThat(renderedIcon.getPixel(12, 12)).isEqualTo(Color.CYAN)
    }

    @Test
    fun kmlLayer_removeLayerFromMapRemovesGroundOverlaysAndMarkers() {
        val kmlWithGroundOverlay =
            """
            <?xml version="1.0" encoding="UTF-8"?>
            <kml xmlns="http://www.opengis.net/kml/2.2">
              <Document>
                <Placemark>
                  <name>Temp Marker</name>
                  <Point><coordinates>-122.084,37.422,0</coordinates></Point>
                </Placemark>
                <GroundOverlay>
                  <name>Campus Overlay</name>
                  <LatLonBox>
                    <north>37.425</north>
                    <south>37.420</south>
                    <east>-122.080</east>
                    <west>-122.088</west>
                    <rotation>15.0</rotation>
                  </LatLonBox>
                </GroundOverlay>
              </Document>
            </kml>
            """.trimIndent()

        val layer = KmlLayer(
            googleMap,
            ByteArrayInputStream(kmlWithGroundOverlay.toByteArray(StandardCharsets.UTF_8)),
            activity,
        )
        layer.addLayerToMap()
        assertThat(googleMap).hasMarkerCount(1).hasGroundOverlayCount(1)

        layer.removeLayerFromMap()
        assertThat(googleMap).hasNoMarkers().hasGroundOverlayCount(0)
    }

    @Test
    fun kmlLayer_traversesDocumentPlacemarksAndAppliesMultiGeometryLineStyle() {
        val kmlDocument =
            """
            <?xml version="1.0" encoding="UTF-8"?>
            <kml xmlns="http://www.opengis.net/kml/2.2">
              <Document>
                <Placemark>
                  <name>Doc Point</name>
                  <Point><coordinates>-122.084,37.422,0</coordinates></Point>
                </Placemark>
                <Placemark>
                  <name>Styled MultiLine</name>
                  <Style>
                    <LineStyle>
                      <color>ff0000ff</color>
                      <width>12</width>
                    </LineStyle>
                  </Style>
                  <MultiGeometry>
                    <LineString>
                      <coordinates>-122.084,37.422,0 -122.080,37.425,0</coordinates>
                    </LineString>
                  </MultiGeometry>
                </Placemark>
              </Document>
            </kml>
            """.trimIndent()

        val layer = KmlLayer(
            googleMap,
            ByteArrayInputStream(kmlDocument.toByteArray(StandardCharsets.UTF_8)),
            activity,
        )
        layer.addLayerToMap()

        assertThat(googleMap).hasMarkerCount(1).hasPolylineCount(1)
        truthAssertThat(layer.hasPlacemarks()).isFalse()
        truthAssertThat(layer.getPlacemarks().toList()).isEmpty()
        truthAssertThat(layer.getAllPlacemarks()).hasSize(2)

        val polyline = googleMap.polylines.single()
        truthAssertThat(polyline.width).isEqualTo(12f)
        truthAssertThat(polyline.color).isEqualTo(Color.RED)
    }
}
