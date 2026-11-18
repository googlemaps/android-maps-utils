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
package com.google.maps.android.data.geojson

import android.graphics.Bitmap
import android.graphics.Color
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.maps.android.collections.GroundOverlayManager
import com.google.maps.android.collections.MarkerManager
import com.google.maps.android.collections.PolygonManager
import com.google.maps.android.collections.PolylineManager
import com.google.maps.android.data.Feature
import com.google.maps.android.robolectric.ktx.click
import com.google.maps.android.robolectric.ktx.createTestMap
import com.google.maps.android.robolectric.ktx.markers
import com.google.maps.android.robolectric.ktx.polygons
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
import com.google.maps.android.robolectric.truth.meters
import com.google.maps.android.testing.golden.GoldenImageDiff
import com.google.maps.android.testing.golden.GoldenTileStrategy
import com.google.maps.android.testing.golden.captureSnapshot
import com.google.maps.android.testing.golden.enableGoldenTesting
import kotlinx.coroutines.test.runTest
import org.json.JSONObject
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import org.robolectric.shadows.ShadowLooper
import com.google.common.truth.Truth.assertThat as truthAssertThat

/**
 * Stateful Robolectric verification suite for [GeoJsonLayer] rendering, canvas pixel colors,
 * feature click routing, shared [MarkerManager] listener preservation, and style propagation.
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
class GeoJsonLayerRobolectricTest {

    private lateinit var googleMap: GoogleMap

    @Before
    fun setUp() {
        googleMap = createTestMap(width = 400, height = 400)
    }

    @Test
    fun geoJsonLayer_rendersOverlaysWithExpectedPixelColorAndRestoresBaselineOnRemoval() = runTest {
        val center = LatLng(37.7750, -122.4175)
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(center, 14f))
        googleMap.enableGoldenTesting(strategy = GoldenTileStrategy.Checkerboard())
        val baselineSnapshot = googleMap.captureSnapshot()

        val geoJson = JSONObject(
            """
            {
              "type": "FeatureCollection",
              "features": [
                {
                  "type": "Feature",
                  "id": "pt_1",
                  "properties": { "title": "Ferry Building", "snippet": "Embarcadero" },
                  "geometry": { "type": "Point", "coordinates": [-122.3937, 37.7955] }
                },
                {
                  "type": "Feature",
                  "id": "line_1",
                  "geometry": {
                    "type": "LineString",
                    "coordinates": [[-122.4194, 37.7749], [-122.3937, 37.7955]]
                  }
                },
                {
                  "type": "Feature",
                  "id": "poly_1",
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
            """.trimIndent(),
        )

        val layer = geoJsonLayer(map = googleMap, geoJsonFile = geoJson)
        layer.getDefaultPolygonStyle().apply {
            fillColor = Color.RED
            setStrokeColor(Color.BLUE)
            setStrokeWidth(4f)
        }
        layer.getDefaultLineStringStyle().apply {
            color = Color.GREEN
            setWidth(6f)
        }
        layer.addLayerToMap()
        truthAssertThat(layer.isLayerOnMap()).isTrue()

        assertThat(googleMap)
            .hasMarkerCount(1)
            .hasPolylineCount(1)
            .hasPolygonCount(1)
            .hasPixelColorAt(LatLng(37.7715, -122.4175), Color.RED)

        val marker = googleMap.markers.single()
        assertThat(marker)
            .hasTitle("Ferry Building")
            .hasSnippet("Embarcadero")
            .isAt(LatLng(37.7955, -122.3937), tolerance = 1.meters)

        assertThat(googleMap.polygons.single())
            .hasHoleCount(1)
            .hasFillColor(Color.RED)

        val bounds = checkNotNull(layer.getBoundingBox())
        assertThat(bounds).contains(LatLng(37.7955, -122.3937))
        assertThat(bounds).contains(LatLng(37.7700, -122.4250))

        val clickedIds = mutableListOf<String>()
        layer.setOnFeatureClickListener { feature -> clickedIds.add(feature.getId() ?: "") }
        marker.click()
        truthAssertThat(clickedIds).containsExactly("pt_1")

        layer.removeLayerFromMap()
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(center, 14f))
        truthAssertThat(layer.isLayerOnMap()).isFalse()
        assertThat(googleMap).hasNoMarkers().hasPolylineCount(0).hasPolygonCount(0)

        GoldenImageDiff.assertMatches(
            expected = baselineSnapshot,
            actual = googleMap.captureSnapshot(),
            maxDiffPercentage = 0.0,
        )
    }

    @Test
    fun geoJsonLayer_polygonsAndPolylinesAreClickableAndDispatchFeatureClicks() {
        val geoJson = JSONObject(
            """
            {
              "type": "FeatureCollection",
              "features": [
                {
                  "type": "Feature",
                  "id": "clickable_line",
                  "geometry": { "type": "LineString", "coordinates": [[-122.42, 37.77], [-122.41, 37.78]] }
                },
                {
                  "type": "Feature",
                  "id": "clickable_poly",
                  "geometry": {
                    "type": "Polygon",
                    "coordinates": [[
                      [-122.42, 37.77], [-122.41, 37.77], [-122.41, 37.78], [-122.42, 37.78], [-122.42, 37.77]
                    ]]
                  }
                }
              ]
            }
            """.trimIndent(),
        )

        val layer = GeoJsonLayer(googleMap, geoJson)
        layer.addLayerToMap()

        val clickedFeatures = mutableListOf<Feature>()
        layer.setOnFeatureClickListener { feature -> clickedFeatures.add(feature) }

        googleMap.polylines.single().click()
        googleMap.polygons.single().click()
        truthAssertThat(clickedFeatures.map { it.getId() }).containsExactly("clickable_line", "clickable_poly")
    }

    @Test
    fun geoJsonLayer_preservesSharedMarkerManagerListenersAcrossMapReattachment() {
        val markerManager = MarkerManager(googleMap)
        val polygonManager = PolygonManager(googleMap)
        val polylineManager = PolylineManager(googleMap)
        val groundOverlayManager = GroundOverlayManager(googleMap)
        ShadowLooper.idleMainLooper()

        val appCollection = markerManager.newCollection("app_markers")
        val appMarker = appCollection.addMarker(MarkerOptions().position(LatLng(37.7749, -122.4194)).title("App"))
        val appClicks = mutableListOf<String>()
        appCollection.setOnMarkerClickListener {
            appClicks.add(it.title ?: "")
            true
        }

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
            """.trimIndent(),
        )
        val layer =
            GeoJsonLayer(googleMap, geoJson, markerManager, polygonManager, polylineManager, groundOverlayManager)
        layer.addLayerToMap()

        val layerClicks = mutableListOf<String>()
        layer.setOnFeatureClickListener { feature -> layerClicks.add(feature.getId() ?: "") }

        appMarker.click()
        googleMap.markers.first { it != appMarker }.click()
        truthAssertThat(appClicks).containsExactly("App")
        truthAssertThat(layerClicks).containsExactly("geojson_pt")

        layer.setMap(null)
        layer.setMap(googleMap)
        layer.addLayerToMap()

        val readdedMarker = googleMap.markers.first { it != appMarker }
        readdedMarker.click()
        truthAssertThat(layerClicks).hasSize(2)
        truthAssertThat(markerManager.remove(readdedMarker)).isTrue()
    }

    @Test
    fun geoJsonLayer_preservesPointStyleAndMultiLineStringStyleProperties() {
        val layer = GeoJsonLayer(googleMap, JSONObject("""{"type":"FeatureCollection","features":[]}"""))
        layer.addLayerToMap()

        val customBitmap = Bitmap.createBitmap(20, 20, Bitmap.Config.ARGB_8888).apply { eraseColor(Color.GREEN) }
        val pointFeature = GeoJsonFeature(
            GeoJsonPoint(LatLng(37.7749, -122.4194)),
            "styled_pt",
            HashMap(),
            null,
        ).apply {
            pointStyle = GeoJsonPointStyle().apply {
                setTitle("Styled Title")
                setSnippet("Styled Snippet")
                setDraggable(true)
                setFlat(true)
                setVisible(false)
                setIcon(BitmapDescriptorFactory.fromBitmap(customBitmap))
                setAlpha(0.5f)
                setRotation(90f)
                setZIndex(5f)
            }
        }

        val multiLineFeature = GeoJsonFeature(
            GeoJsonMultiLineString(listOf(GeoJsonLineString(listOf(LatLng(37.0, -122.0), LatLng(37.1, -122.1))))),
            "multi_line_1",
            HashMap(),
            null,
        ).apply {
            lineStringStyle = GeoJsonLineStringStyle().apply {
                color = Color.RED
                setWidth(15f)
            }
        }

        layer.addFeature(pointFeature)
        layer.addFeature(multiLineFeature)

        val marker = googleMap.markers.single()
        assertThat(marker)
            .hasTitle("Styled Title")
            .hasSnippet("Styled Snippet")
            .isDraggable()
            .isFlat()
            .isNotVisible()
            .hasAlpha(0.5f, tolerance = 0.02f)
            .hasRotation(90f)
            .hasZIndex(5f)
        truthAssertThat(marker.shadow.iconBitmap).isSameInstanceAs(customBitmap)

        val polyline = googleMap.polylines.single()
        truthAssertThat(polyline.width).isEqualTo(15f)
        truthAssertThat(polyline.color).isEqualTo(Color.RED)
    }
}
