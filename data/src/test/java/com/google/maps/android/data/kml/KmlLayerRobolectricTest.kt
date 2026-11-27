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
import com.google.maps.android.robolectric.MapsRobolectricTestRunner
import com.google.maps.android.robolectric.ktx.createTestMapView
import com.google.maps.android.robolectric.ktx.getMapBlocking
import com.google.maps.android.robolectric.ktx.groundOverlays
import com.google.maps.android.robolectric.ktx.markers
import com.google.maps.android.robolectric.ktx.polylines
import com.google.maps.android.robolectric.ktx.shadow
import com.google.maps.android.robolectric.powerassert.assertGeo
import com.google.maps.android.robolectric.powerassert.assertMap
import com.google.maps.android.robolectric.powerassert.assertMarker
import com.google.maps.android.robolectric.powerassert.distanceTo
import com.google.maps.android.robolectric.powerassert.meters
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.nio.charset.StandardCharsets
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

/**
 * Stateful Robolectric verification suite for [KmlLayer] compressed KMZ archive icon extraction,
 * ground overlay lifecycle cleanup, `<Document>` placemark traversal, and `<MultiGeometry>` styles.
 */
@RunWith(MapsRobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [34])
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

        googleMap.assertMap(googleMap.markers.size == 1)
        val marker = googleMap.markers.single()
        marker.assertMarker(
            marker.title == "KMZ Pin" &&
                (marker.position distanceTo pinLocation) <= 1.meters,
        )

        val renderedIcon = checkNotNull(marker.shadow.iconBitmap)
        assertGeo(
            renderedIcon.width == 24 &&
                renderedIcon.height == 24 &&
                renderedIcon.getPixel(12, 12) == Color.CYAN,
        )
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
        googleMap.assertMap(googleMap.markers.size == 1 && googleMap.groundOverlays.size == 1)

        layer.removeLayerFromMap()
        googleMap.assertMap(googleMap.markers.isEmpty() && googleMap.groundOverlays.isEmpty())
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

        googleMap.assertMap(googleMap.markers.size == 1 && googleMap.polylines.size == 1)
        assertGeo(
            !layer.hasPlacemarks() &&
                layer.getPlacemarks().toList().isEmpty() &&
                layer.getAllPlacemarks().size == 2,
        )

        val polyline = googleMap.polylines.single()
        assertGeo(polyline.width == 12f && polyline.color == Color.RED)
    }
}
