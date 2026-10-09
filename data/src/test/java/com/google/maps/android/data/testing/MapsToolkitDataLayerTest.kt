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
package com.google.maps.android.data.testing

import android.app.Activity
import android.graphics.Bitmap
import android.graphics.Color
import android.os.Bundle
import android.view.View
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.maps.robolectric.annotation.EnableMapsShadows
import com.google.android.maps.robolectric.shadows.ShadowGoogleMap
import com.google.android.maps.robolectric.shadows.ShadowMarker
import com.google.android.maps.robolectric.truth.LatLngSubject
import com.google.android.maps.robolectric.truth.isWithin
import com.google.android.maps.robolectric.truth.meters
import com.google.android.maps.testing.golden.GoldenImageDiff
import com.google.android.maps.testing.golden.GoldenTileStrategy
import com.google.android.maps.testing.golden.captureSnapshot
import com.google.android.maps.testing.golden.enableGoldenTesting
import com.google.common.truth.Truth.assertThat
import com.google.maps.android.collections.GroundOverlayManager
import com.google.maps.android.collections.MarkerManager
import com.google.maps.android.collections.PolygonManager
import com.google.maps.android.collections.PolylineManager
import com.google.maps.android.data.Feature
import com.google.maps.android.data.geojson.GeoJsonFeature
import com.google.maps.android.data.geojson.GeoJsonLayer
import com.google.maps.android.data.geojson.GeoJsonLineString
import com.google.maps.android.data.geojson.GeoJsonLineStringStyle
import com.google.maps.android.data.geojson.GeoJsonMultiLineString
import com.google.maps.android.data.geojson.GeoJsonPoint
import com.google.maps.android.data.geojson.GeoJsonPointStyle
import com.google.maps.android.data.geojson.GeoJsonPolygonStyle
import com.google.maps.android.data.geojson.geoJsonLayer
import com.google.maps.android.data.kml.KmlLayer
import com.google.maps.android.data.kml.kmlLayer
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.nio.charset.StandardCharsets
import java.util.concurrent.atomic.AtomicReference
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.json.JSONObject
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import org.robolectric.shadow.api.Shadow
import org.robolectric.shadows.ShadowLooper

/**
 * End-to-end JVM integration and adversarial bug-hunting suite for the `:data` module
 * ([GeoJsonLayer], [KmlLayer], KMZ archives, and `MapViewRenderer`) powered by the
 * Android Maps Testing Toolkit (`v1.1.0-rc01`).
 *
 * ## Architectural Scope
 * By testing [GeoJsonLayer] and [KmlLayer] against stateful [ShadowGoogleMap] overlays,
 * [LatLngSubject] geospatial assertions, and [GoldenImageDiff] snapshot comparisons rather than
 * lenient mocks, this suite validates end-to-end rendering AND uncovers **5 distinct architectural
 * defects (Bugs #1–#5)** in `:data`:
 * - **Bug #1**: `KmlLayer.removeLayerFromMap()` leaks every `KmlGroundOverlay` on `GoogleMap`
 *   because `KmlLayer.toModelFeature(KmlGroundOverlay)` allocates a new `Feature` instance on
 *   every call while `MapViewRenderer` keys `renderedFeatures` in an `IdentityHashMap`.
 * - **Bug #2**: `GeoJsonLayer` and `KmlLayer` `Polygon` and `Polyline` features are **never clickable**
 *   on `GoogleMap` because `MapViewRenderer.createPolygonOptions` and `createPolylineOptions`
 *   never call `.clickable(true)` (leaving SDK default `isClickable == false`).
 * - **Bug #3**: `GeoJsonLayer` and `KmlLayer` completely ignore their constructor `MarkerManager`,
 *   `PolygonManager`, `PolylineManager`, and `GroundOverlayManager` parameters and clobber global
 *   `GoogleMap` click listeners when `setOnFeatureClickListener` is invoked.
 * - **Bug #4**: `KmlLayer.hasPlacemarks()`, `getPlacemarks()`, `features`, and `getGroundOverlays()`
 *   return empty for standard KML documents containing a root `<Document>` tag, and
 *   `<MultiGeometry>` / `GeoJsonMultiLineString` / `GeoJsonMultiPoint` features lose their styles.
 * - **Bug #5**: `GeoJsonLayer.toModelFeature` silently drops `GeoJsonPointStyle` `title`, `snippet`,
 *   `isDraggable`, `isFlat`, `isVisible`, and custom `icon` (`BitmapDescriptor`).
 */
@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [34])
@EnableMapsShadows
class MapsToolkitDataLayerTest {

    private lateinit var activity: Activity
    private lateinit var mapView: MapView
    private lateinit var googleMap: GoogleMap
    private lateinit var shadowMap: ShadowGoogleMap

    @Before
    fun setUp() {
        activity = Robolectric.buildActivity(Activity::class.java).create().get()
        mapView = MapView(activity).apply {
            onCreate(Bundle())
            onStart()
            onResume()
            measure(
                View.MeasureSpec.makeMeasureSpec(400, View.MeasureSpec.EXACTLY),
                View.MeasureSpec.makeMeasureSpec(400, View.MeasureSpec.EXACTLY)
            )
            layout(0, 0, 400, 400)
        }

        val mapRef = AtomicReference<GoogleMap>()
        mapView.getMapAsync { mapRef.set(it) }
        ShadowLooper.idleMainLooper()

        googleMap = checkNotNull(mapRef.get())
        shadowMap = Shadow.extract(googleMap) as ShadowGoogleMap
    }

    // ============================================================================================
    // Section 1: End-to-End GeoJSON & KMZ Rendering + Golden Snapshot Verification
    // ============================================================================================

    /**
     * Verifies that [GeoJsonLayer] parses a `FeatureCollection` containing `Point`, `LineString`,
     * and `Polygon` (with an interior hole), computes accurate [GeoJsonLayer.boundingBox],
     * renders all overlays onto [ShadowGoogleMap], produces a verifiable visual diff via
     * [GoldenImageDiff], dispatches point feature click callbacks, and cleanly removes all
     * rendered GeoJSON overlays when `removeLayerFromMap()` is called.
     */
    @Test
    fun geoJsonLayer_rendersPointsLinesAndPolygonsAndMatchesBaselineAfterRemoval() = runTest {
        val dispatcher = UnconfinedTestDispatcher(testScheduler)
        val center = LatLng(37.7749, -122.4194)
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(center, 13f))
        googleMap.enableGoldenTesting(strategy = GoldenTileStrategy.Checkerboard())

        val baselineJob = async(dispatcher) { googleMap.captureSnapshot() }
        ShadowLooper.idleMainLooper()
        val baselineSnapshot = baselineJob.await()

        val geoJson = JSONObject(
            """
            {
              "type": "FeatureCollection",
              "features": [
                {
                  "type": "Feature",
                  "id": "pt_1",
                  "properties": {
                    "title": "Ferry Building",
                    "snippet": "Embarcadero"
                  },
                  "geometry": {
                    "type": "Point",
                    "coordinates": [-122.3937, 37.7955]
                  }
                },
                {
                  "type": "Feature",
                  "id": "line_1",
                  "properties": { "name": "Market Street" },
                  "geometry": {
                    "type": "LineString",
                    "coordinates": [
                      [-122.4194, 37.7749],
                      [-122.3937, 37.7955]
                    ]
                  }
                },
                {
                  "type": "Feature",
                  "id": "poly_1",
                  "properties": { "name": "Civic Center Zone" },
                  "geometry": {
                    "type": "Polygon",
                    "coordinates": [
                      [
                        [-122.4250, 37.7700],
                        [-122.4100, 37.7700],
                        [-122.4100, 37.7800],
                        [-122.4250, 37.7800],
                        [-122.4250, 37.7700]
                      ],
                      [
                        [-122.4200, 37.7730],
                        [-122.4150, 37.7730],
                        [-122.4150, 37.7770],
                        [-122.4200, 37.7770],
                        [-122.4200, 37.7730]
                      ]
                    ]
                  }
                }
              ]
            }
            """.trimIndent()
        )

        val layer = geoJsonLayer(map = googleMap, geoJsonFile = geoJson)
        layer.getDefaultPolygonStyle().apply {
            fillColor = Color.argb(160, 255, 0, 0)
            setStrokeColor(Color.BLUE)
            setStrokeWidth(6f)
        }
        layer.getDefaultLineStringStyle().apply {
            color = Color.GREEN
            setWidth(8f)
        }

        layer.addLayerToMap()
        assertThat(layer.isLayerOnMap()).isTrue()

        // Verify ShadowGoogleMap state
        assertThat(shadowMap.markers).hasSize(1)
        assertThat(shadowMap.polylines).hasSize(1)
        assertThat(shadowMap.polygons).hasSize(1)

        val marker = shadowMap.markers.single()
        assertThat(marker.title).isEqualTo("Ferry Building")
        assertThat(marker.snippet).isEqualTo("Embarcadero")
        LatLngSubject.assertThat(marker.position)
            .isWithin(1.meters)
            .of(LatLng(37.7955, -122.3937))

        val polygon = shadowMap.polygons.single()
        assertThat(polygon.holes).hasSize(1)
        assertThat(polygon.fillColor).isEqualTo(Color.argb(160, 255, 0, 0))

        val bounds = checkNotNull(layer.getBoundingBox())
        assertThat(bounds.contains(LatLng(37.7955, -122.3937))).isTrue()
        assertThat(bounds.contains(LatLng(37.7700, -122.4250))).isTrue()

        // Verify point feature click callback
        val clickedFeatures = mutableListOf<Feature>()
        layer.setOnFeatureClickListener { feature -> clickedFeatures.add(feature) }
        shadowMap.clickMarker(marker)
        assertThat(clickedFeatures).hasSize(1)
        assertThat(clickedFeatures.single().getId()).isEqualTo("pt_1")

        // Verify GoldenImageDiff detects the rendered GeoJSON features on the MapView canvas
        val renderedJob = async(dispatcher) { googleMap.captureSnapshot() }
        ShadowLooper.idleMainLooper()
        val renderedSnapshot = renderedJob.await()
        val diff = GoldenImageDiff.compare(expected = baselineSnapshot, actual = renderedSnapshot)
        assertThat(diff.matches).isFalse()
        assertThat(diff.diffPixelCount).isGreaterThan(200)

        // Removing the GeoJSON layer removes all markers, polylines, and polygons and restores the baseline image
        layer.removeLayerFromMap()
        assertThat(layer.isLayerOnMap()).isFalse()
        assertThat(shadowMap.markers).isEmpty()
        assertThat(shadowMap.polylines).isEmpty()
        assertThat(shadowMap.polygons).isEmpty()

        val afterRemovalJob = async(dispatcher) { googleMap.captureSnapshot() }
        ShadowLooper.idleMainLooper()
        val afterRemovalSnapshot = afterRemovalJob.await()
        GoldenImageDiff.assertMatches(
            expected = baselineSnapshot,
            actual = afterRemovalSnapshot,
            maxDiffPercentage = 0.0
        )
    }

    /**
     * Verifies that [KmlLayer] transparently detects and parses a compressed KMZ archive (`PK` zip header)
     * containing `doc.kml` and an embedded PNG icon (`images/pin.png`), caches the decoded bitmap in
     * `MapViewRenderer`, and assigns the embedded bitmap icon to the rendered [com.google.android.gms.maps.model.Marker].
     */
    @Test
    fun kmlLayer_withKmzArchive_extractsEmbeddedBitmapAndAppliesToMarker() {
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

        // Build an in-memory KMZ (ZIP) archive with doc.kml and a 24x24 cyan PNG image
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
            context = activity
        )
        layer.addLayerToMap()

        assertThat(shadowMap.markers).hasSize(1)
        val marker = shadowMap.markers.single()
        assertThat(marker.title).isEqualTo("KMZ Pin")
        val shadowMarker = Shadow.extract(marker) as ShadowMarker
        val renderedIcon = checkNotNull(shadowMarker.iconBitmap)
        assertThat(renderedIcon.width).isEqualTo(24)
        assertThat(renderedIcon.height).isEqualTo(24)
        assertThat(renderedIcon.getPixel(12, 12)).isEqualTo(Color.CYAN)
    }

    // ============================================================================================
    // Section 2: Adversarial Bug Reproductions in :data (Bugs #1, #2, #3, #4, #5)
    // ============================================================================================

    /**
     * **BUG #1 REPRODUCTION (`KmlLayer.removeLayerFromMap()` Leaks All `KmlGroundOverlay`s on `GoogleMap`)**:
     *
     * - In `KmlLayer.kt:434-453`, `toModelFeature(groundOverlay: KmlGroundOverlay)` constructs and
     *   returns a **new** `com.google.maps.android.data.renderer.model.Feature` instance on every
     *   invocation without caching it (unlike `toModelFeature(placemark: KmlPlacemark)` at line 378
     *   which caches in `mPlacemarkMap`).
     * - In `MapViewRenderer.kt:91`, rendered map objects are stored in:
     *   `private val renderedFeatures = IdentityHashMap<Feature, MutableList<Any>>()`
     * - When `kmlLayer.removeLayerFromMap()` runs (`KmlLayer.kt:356` & `374`), calling
     *   `renderer.removeFeature(toModelFeature(g))` passes a freshly allocated `Feature` whose
     *   object identity (`===`) does not match the `Feature` key stored during `addLayerToMap()`.
     * - Result: `renderedFeatures[feature]` returns `null`, and **every KML GroundOverlay remains
     *   permanently leaked on the `GoogleMap` (`shadowMap.groundOverlays`)!**
     */
    @Test
    fun bug1_kmlLayer_removeLayerFromMapLeaksGroundOverlaysDueToUncachedModelFeatureIdentity() {
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
                  <name>Leaked Campus Overlay</name>
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
            activity
        )
        layer.addLayerToMap()

        // Both the Placemark Marker and the GroundOverlay are added to the GoogleMap
        assertThat(shadowMap.markers).hasSize(1)
        assertThat(shadowMap.groundOverlays).hasSize(1)

        // Now remove the KML layer from the map
        layer.removeLayerFromMap()

        // The Placemark Marker (cached in mPlacemarkMap) is removed:
        assertThat(shadowMap.markers).isEmpty()

        // BUG #1 VERIFIED FIXED: The GroundOverlay (cached in mGroundOverlayMap) is also removed!
        assertThat(shadowMap.groundOverlays).isEmpty()
    }

    /**
     * **BUG #2 REGRESSION VERIFICATION (`GeoJsonLayer` and `KmlLayer` Polygons and Polylines Are Clickable on `GoogleMap`)**:
     */
    @Test
    fun bug2_geoJsonAndKmlLayers_polygonsAndPolylinesAreClickableAndFireFeatureClicks() {
        val geoJson = JSONObject(
            """
            {
              "type": "FeatureCollection",
              "features": [
                {
                  "type": "Feature",
                  "id": "clickable_line",
                  "geometry": {
                    "type": "LineString",
                    "coordinates": [[-122.42, 37.77], [-122.41, 37.78]]
                  }
                },
                {
                  "type": "Feature",
                  "id": "clickable_poly",
                  "geometry": {
                    "type": "Polygon",
                    "coordinates": [[
                      [-122.42, 37.77],
                      [-122.41, 37.77],
                      [-122.41, 37.78],
                      [-122.42, 37.78],
                      [-122.42, 37.77]
                    ]]
                  }
                }
              ]
            }
            """.trimIndent()
        )

        val layer = GeoJsonLayer(googleMap, geoJson)
        assertThat(layer.getDefaultLineStringStyle().isClickable()).isTrue()
        assertThat(layer.getDefaultPolygonStyle().isClickable()).isTrue()

        layer.addLayerToMap()

        val clickedFeatures = mutableListOf<Feature>()
        layer.setOnFeatureClickListener { feature -> clickedFeatures.add(feature) }

        val renderedPolyline = shadowMap.polylines.single()
        val renderedPolygon = shadowMap.polygons.single()

        // BUG #2 VERIFIED FIXED: Both Polyline and Polygon have isClickable == true and route clicks!
        assertThat(renderedPolyline.isClickable).isTrue()
        assertThat(renderedPolygon.isClickable).isTrue()

        shadowMap.clickPolyline(renderedPolyline)
        shadowMap.clickPolygon(renderedPolygon)
        assertThat(clickedFeatures.map { it.getId() }).containsExactly("clickable_line", "clickable_poly")
    }

    /**
     * **BUG #3 REGRESSION VERIFICATION (`GeoJsonLayer` and `KmlLayer` Route Through Passed-In `*Manager`
     * Instances and Preserve Shared `MarkerManager` Listeners)**:
     */
    @Test
    fun bug3_geoJsonAndKmlLayers_useObjectManagersAndPreserveMarkerManagerListeners() {
        val markerManager = MarkerManager(googleMap)
        val polygonManager = PolygonManager(googleMap)
        val polylineManager = PolylineManager(googleMap)
        val groundOverlayManager = GroundOverlayManager(googleMap)
        ShadowLooper.idleMainLooper()

        // Create an application marker collection managed by MarkerManager
        val appCollection = markerManager.newCollection("app_markers")
        val appMarker = appCollection.addMarker(
            MarkerOptions().position(LatLng(37.7749, -122.4194)).title("App Marker")
        )
        val appMarkerClicks = mutableListOf<String>()
        appCollection.setOnMarkerClickListener {
            appMarkerClicks.add(it.title ?: "")
            true
        }

        // Pass the shared managers into GeoJsonLayer
        val geoJson = JSONObject(
            """
            {
              "type": "FeatureCollection",
              "features": [{
                "type": "Feature",
                "id": "geojson_pt",
                "geometry": { "type": "Point", "coordinates": [-122.40, 37.78] }
              }]
            }
            """.trimIndent()
        )
        val layer = GeoJsonLayer(
            googleMap,
            geoJson,
            markerManager,
            polygonManager,
            polylineManager,
            groundOverlayManager
        )
        layer.addLayerToMap()

        assertThat(shadowMap.markers).hasSize(2)
        val geoJsonMarker = shadowMap.markers.first { it != appMarker }

        // Clicking appMarker routes through MarkerManager before AND after layer.setOnFeatureClickListener!
        shadowMap.clickMarker(appMarker)
        assertThat(appMarkerClicks).containsExactly("App Marker")

        val layerClicks = mutableListOf<String>()
        layer.setOnFeatureClickListener { feature -> layerClicks.add(feature.getId() ?: "") }

        shadowMap.clickMarker(appMarker)
        assertThat(appMarkerClicks).hasSize(2)

        shadowMap.clickMarker(geoJsonMarker)
        assertThat(layerClicks).containsExactly("geojson_pt")

        // Toggling setMap(null) + setMap(googleMap) reuses the existing collection and preserves listeners:
        layer.setMap(null)
        layer.setMap(googleMap)
        layer.addLayerToMap()
        val readdedGeoJsonMarker = shadowMap.markers.first { it != appMarker }
        shadowMap.clickMarker(readdedGeoJsonMarker)
        assertThat(layerClicks).hasSize(2)

        // And markerManager owns readdedGeoJsonMarker and can remove it cleanly:
        assertThat(markerManager.remove(readdedGeoJsonMarker)).isTrue()
    }

    /**
     * **BUG #4 REGRESSION VERIFICATION (`KmlLayer.getAllPlacemarks()` Returns `<Document>` KML Items
     * While `getPlacemarks()` Preserves Top-Level Behavior, and `<MultiGeometry>` Preserves Styles)**:
     */
    @Test
    fun bug4_kmlDocumentPlacemarksAndMultiGeometriesPreserveStyles() {
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

        val kmlLayer = KmlLayer(
            googleMap,
            ByteArrayInputStream(kmlDocument.toByteArray(StandardCharsets.UTF_8)),
            activity
        )
        kmlLayer.addLayerToMap()

        assertThat(shadowMap.markers).hasSize(1)
        assertThat(shadowMap.polylines).hasSize(1)

        // Top-level accessors return only root placemarks outside <Document>/<Folder>, while
        // getAllPlacemarks() recursively traverses nested <Document> and <Folder> containers:
        assertThat(kmlLayer.hasPlacemarks()).isFalse()
        assertThat(kmlLayer.getPlacemarks().toList()).isEmpty()
        assertThat(kmlLayer.features.toList()).isEmpty()
        assertThat(kmlLayer.getAllPlacemarks()).hasSize(2)

        // BUG #4B VERIFIED FIXED: KML <MultiGeometry> <LineString> preserves its <width>12</width> and red color!
        val kmlMultiPolyline = shadowMap.polylines.single()
        assertThat(kmlMultiPolyline.width).isEqualTo(12f)
        assertThat(kmlMultiPolyline.color).isEqualTo(Color.RED)

        // BUG #4C VERIFIED FIXED: GeoJsonMultiLineString preserves its GeoJsonLineStringStyle!
        googleMap.clear()
        val geoJsonLayer = GeoJsonLayer(googleMap, JSONObject("""{"type":"FeatureCollection","features":[]}"""))
        geoJsonLayer.addLayerToMap()
        val multiLine = GeoJsonMultiLineString(
            listOf(GeoJsonLineString(listOf(LatLng(37.0, -122.0), LatLng(37.1, -122.1))))
        )
        val multiLineFeature = GeoJsonFeature(multiLine, "multi_line_1", HashMap(), null).apply {
            lineStringStyle = GeoJsonLineStringStyle().apply {
                color = Color.RED
                setWidth(15f)
            }
        }
        geoJsonLayer.addFeature(multiLineFeature)

        val renderedGeoJsonMultiLine = shadowMap.polylines.single()
        assertThat(renderedGeoJsonMultiLine.width).isEqualTo(15f)
        assertThat(renderedGeoJsonMultiLine.color).isEqualTo(Color.RED)
    }

    /**
     * **BUG #5 REGRESSION VERIFICATION (`GeoJsonLayer.toModelFeature` Preserves `GeoJsonPointStyle`
     * Title, Snippet, Draggable, Flat, Visible, and Custom Icon)**:
     */
    @Test
    fun bug5_geoJsonPointStyle_preservesTitleSnippetDraggableFlatVisibleAndCustomIcon() {
        val layer = GeoJsonLayer(googleMap, JSONObject("""{"type":"FeatureCollection","features":[]}"""))
        layer.addLayerToMap()

        val customBitmap = Bitmap.createBitmap(20, 20, Bitmap.Config.ARGB_8888).apply {
            eraseColor(Color.GREEN)
        }
        val customIcon = BitmapDescriptorFactory.fromBitmap(customBitmap)

        val pointFeature = GeoJsonFeature(
            GeoJsonPoint(LatLng(37.7749, -122.4194)),
            "styled_pt",
            HashMap(),
            null
        ).apply {
            pointStyle = GeoJsonPointStyle().apply {
                setTitle("Styled Title")
                setSnippet("Styled Snippet")
                setDraggable(true)
                setFlat(true)
                setVisible(false)
                setIcon(customIcon)
                setAlpha(0.5f)
                setRotation(90f)
                setZIndex(5f)
            }
        }

        layer.addFeature(pointFeature)
        assertThat(shadowMap.markers).hasSize(1)
        val marker = shadowMap.markers.single()
        val shadowMarker = Shadow.extract(marker) as ShadowMarker

        assertThat(marker.alpha).isWithin(0.02f).of(0.5f)
        assertThat(marker.rotation).isWithin(0.01f).of(90f)
        assertThat(marker.zIndex).isWithin(0.01f).of(5f)

        // BUG #5 VERIFIED FIXED: title, snippet, isDraggable, isFlat, isVisible, and custom icon are all preserved!
        assertThat(marker.title).isEqualTo("Styled Title")
        assertThat(marker.snippet).isEqualTo("Styled Snippet")
        assertThat(marker.isDraggable).isTrue()
        assertThat(marker.isFlat).isTrue()
        assertThat(marker.isVisible).isFalse()
        assertThat(shadowMarker.iconBitmap).isSameInstanceAs(customBitmap)
    }
}
