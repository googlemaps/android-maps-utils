/*
 * Copyright 2026 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.google.maps.android.data.geojson

import com.google.android.gms.maps.GoogleMap
import com.google.maps.android.data.Layer
import io.mockk.mockk
import org.json.JSONObject
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/** Regression test for https://github.com/googlemaps/android-maps-utils/issues/1746. */
@RunWith(RobolectricTestRunner::class)
class GeoJsonLayerOnMapTest {
    private val emptyFeatureCollection =
        JSONObject(
            """
            { "type": "FeatureCollection", "features": [] }
            """.trimIndent(),
        )

    @Test
    fun isLayerOnMap_isExposedThroughLayerBaseClass() {
        val layer: Layer = GeoJsonLayer(mockk<GoogleMap>(relaxed = true), emptyFeatureCollection)

        assertFalse(layer.isLayerOnMap())
    }

    @Test
    fun isLayerOnMap_reflectsAddAndRemove() {
        val layer = GeoJsonLayer(mockk<GoogleMap>(relaxed = true), emptyFeatureCollection)

        assertFalse(layer.isLayerOnMap())

        layer.addLayerToMap()
        assertTrue(layer.isLayerOnMap())

        layer.removeLayerFromMap()
        assertFalse(layer.isLayerOnMap())
    }

    @Test
    fun addLayerToMap_routesThroughObjectManagersAndAppliesFullStyles() {
        io.mockk.mockkStatic(com.google.android.gms.maps.model.BitmapDescriptorFactory::class)
        io.mockk.every { com.google.android.gms.maps.model.BitmapDescriptorFactory.defaultMarker(any()) } returns mockk()
        try {
            val mockMap = mockk<GoogleMap>(relaxed = true)
            val markerSlot = io.mockk.slot<com.google.android.gms.maps.model.MarkerOptions>()
            val polylineSlot = io.mockk.slot<com.google.android.gms.maps.model.PolylineOptions>()
            val polygonSlot = io.mockk.slot<com.google.android.gms.maps.model.PolygonOptions>()
            val mockMarker = mockk<com.google.android.gms.maps.model.Marker>(relaxed = true)
            io.mockk.every { mockMap.addMarker(capture(markerSlot)) } returns mockMarker
            io.mockk.every { mockMap.addPolyline(capture(polylineSlot)) } returns mockk(relaxed = true)
            io.mockk.every { mockMap.addPolygon(capture(polygonSlot)) } returns mockk(relaxed = true)

            val markerManager = com.google.maps.android.collections.MarkerManager(mockMap)
            val polygonManager = com.google.maps.android.collections.PolygonManager(mockMap)
            val polylineManager = com.google.maps.android.collections.PolylineManager(mockMap)

            val layer = GeoJsonLayer(mockMap, emptyFeatureCollection, markerManager, polygonManager, polylineManager, null)
            layer.addLayerToMap()

            val customIcon = mockk<com.google.android.gms.maps.model.BitmapDescriptor>()
            val pointFeature =
                GeoJsonFeature(
                    GeoJsonPoint(com.google.android.gms.maps.model.LatLng(37.7749, -122.4194)),
                    "pt_1",
                    HashMap(),
                    null,
                ).apply {
                    pointStyle =
                        GeoJsonPointStyle().apply {
                            setTitle("Styled Title")
                            setSnippet("Styled Snippet")
                            setDraggable(true)
                            setFlat(true)
                            setVisible(false)
                            setIcon(customIcon)
                        }
                }
            layer.addFeature(pointFeature)

            org.junit.Assert.assertEquals("Styled Title", markerSlot.captured.title)
            org.junit.Assert.assertEquals("Styled Snippet", markerSlot.captured.snippet)
            assertTrue(markerSlot.captured.isDraggable)
            assertTrue(markerSlot.captured.isFlat)
            assertFalse(markerSlot.captured.isVisible)
            org.junit.Assert.assertSame(customIcon, markerSlot.captured.icon)

            val multiLine =
                GeoJsonMultiLineString(
                    listOf(
                        GeoJsonLineString(
                            listOf(
                                com.google.android.gms.maps.model.LatLng(37.0, -122.0),
                                com.google.android.gms.maps.model.LatLng(37.1, -122.1),
                            ),
                        ),
                    ),
                )
            val multiLineFeature =
                GeoJsonFeature(multiLine, "ml_1", HashMap(), null).apply {
                    lineStringStyle =
                        GeoJsonLineStringStyle().apply {
                            color = android.graphics.Color.RED
                            setWidth(15f)
                        }
                }
            layer.addFeature(multiLineFeature)

            assertTrue(polylineSlot.captured.isClickable)
            org.junit.Assert.assertEquals(15f, polylineSlot.captured.width, 0.001f)
            org.junit.Assert.assertEquals(android.graphics.Color.RED, polylineSlot.captured.color)
            assertTrue(markerManager.remove(mockMarker))
        } finally {
            io.mockk.unmockkStatic(com.google.android.gms.maps.model.BitmapDescriptorFactory::class)
        }
    }
}
