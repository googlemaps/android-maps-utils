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
package com.google.maps.android.data.renderer

import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.AdvancedMarkerOptions
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolygonOptions
import com.google.maps.android.data.renderer.mapview.MapViewRenderer
import com.google.maps.android.data.renderer.model.Feature
<<<<<<< HEAD
import com.google.maps.android.data.renderer.model.LineString
=======
>>>>>>> origin/main
import com.google.maps.android.data.renderer.model.MultiGeometry
import com.google.maps.android.data.renderer.model.Point
import com.google.maps.android.data.renderer.model.PointGeometry
import com.google.maps.android.data.renderer.model.Polygon
<<<<<<< HEAD
=======
import com.google.maps.android.data.renderer.model.PolygonStyle
>>>>>>> origin/main
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.junit.Assert.assertEquals
import org.junit.Test

import com.google.android.gms.maps.model.Polyline
import com.google.android.gms.maps.model.PolylineOptions
import com.google.maps.android.data.renderer.model.LineString
import com.google.maps.android.data.renderer.model.LineStyle

/**
 * Unit tests for [MapViewRenderer] verifying correct translation of platform-agnostic
 * feature models to Google Maps SDK marker options.
 */
class MapViewRendererTest {
    @Test
    fun testAddFeaturePoint_setsTitleAndSnippetFromProperties() {
        // Given
        val mockMap = mockk<GoogleMap>(relaxed = true)
        val mockMarker = mockk<Marker>(relaxed = true)
        val mockIconProvider = mockk<IconProvider>(relaxed = true)

        val optionsSlot = slot<MarkerOptions>()
        every { mockMap.addMarker(capture(optionsSlot)) } returns mockMarker

        val renderer = MapViewRenderer(mockMap, mockIconProvider)
        renderer.useAdvancedMarkers = false

        val properties =
            mapOf(
                "title" to "Critical Right Turn",
                "description" to "Be careful here!",
            )
        val feature =
            Feature(
                geometry = PointGeometry(Point(41.942, -111.620)),
                properties = properties,
            )

        // When
        renderer.addFeature(feature)

        // Then
        verify(exactly = 1) { mockMap.addMarker(any<MarkerOptions>()) }

        val capturedOptions = optionsSlot.captured
        assertEquals(LatLng(41.942, -111.620), capturedOptions.position)
        assertEquals("Critical Right Turn", capturedOptions.title)
        assertEquals("Be careful here!", capturedOptions.snippet)
    }

    @Test
    fun testAddFeaturePoint_setsTitleAndSnippetFromAlternativeProperties() {
        // Given
        val mockMap = mockk<GoogleMap>(relaxed = true)
        val mockMarker = mockk<Marker>(relaxed = true)
        val mockIconProvider = mockk<IconProvider>(relaxed = true)

        val optionsSlot = slot<MarkerOptions>()
        every { mockMap.addMarker(capture(optionsSlot)) } returns mockMarker

        val renderer = MapViewRenderer(mockMap, mockIconProvider)
        renderer.useAdvancedMarkers = false

        val properties =
            mapOf(
                "name" to "Water Source",
                "snippet" to "Spring water available.",
            )
        val feature =
            Feature(
                geometry = PointGeometry(Point(41.798, -111.560)),
                properties = properties,
            )

        // When
        renderer.addFeature(feature)

        // Then
        verify(exactly = 1) { mockMap.addMarker(any<MarkerOptions>()) }

        val capturedOptions = optionsSlot.captured
        assertEquals(LatLng(41.798, -111.560), capturedOptions.position)
        assertEquals("Water Source", capturedOptions.title)
        assertEquals("Spring water available.", capturedOptions.snippet)
    }

    @Test
    fun testAddFeaturePoint_advancedMarkers_setsTitleAndSnippetFromProperties() {
        // Given
        val mockMap = mockk<GoogleMap>(relaxed = true)
        val mockMarker = mockk<Marker>(relaxed = true)
        val mockIconProvider = mockk<IconProvider>(relaxed = true)

        val optionsSlot = slot<AdvancedMarkerOptions>()
        every { mockMap.addMarker(capture(optionsSlot)) } returns mockMarker

        val renderer = MapViewRenderer(mockMap, mockIconProvider)
        renderer.useAdvancedMarkers = true

        val properties =
            mapOf(
                "title" to "Critical Right Turn",
                "description" to "Be careful here!",
            )
        val feature =
            Feature(
                geometry = PointGeometry(Point(41.942, -111.620)),
                properties = properties,
            )

        // When
        renderer.addFeature(feature)

        // Then
        verify(exactly = 1) { mockMap.addMarker(any<AdvancedMarkerOptions>()) }

        val capturedOptions = optionsSlot.captured
        assertEquals(LatLng(41.942, -111.620), capturedOptions.position)
        assertEquals("Critical Right Turn", capturedOptions.title)
        assertEquals("Be careful here!", capturedOptions.snippet)
    }

    @Test
fun testRemoveFeature_multiGeometry_removesAllRenderedObjects() {
        // Given
        val mockMap = mockk<GoogleMap>(relaxed = true)
        val mockIconProvider = mockk<IconProvider>(relaxed = true)

        val addedPolygons = mutableListOf<com.google.android.gms.maps.model.Polygon>()
        every { mockMap.addPolygon(any()) } answers {
            mockk<com.google.android.gms.maps.model.Polygon>(relaxed = true).also { addedPolygons.add(it) }
        }

        val renderer = MapViewRenderer(mockMap, mockIconProvider)
        val feature =
            Feature(
                geometry =
                    MultiGeometry(
                        listOf(
                            Polygon(listOf(Point(0.0, 0.0), Point(0.0, 1.0), Point(1.0, 1.0))),
                            Polygon(listOf(Point(2.0, 2.0), Point(2.0, 3.0), Point(3.0, 3.0))),
                        ),
                    ),
            )

        // When
        renderer.addFeature(feature)
        renderer.removeFeature(feature)

        // Then: both rendered polygons are removed via the original feature reference.
        assertEquals(2, addedPolygons.size)
        addedPolygons.forEach { polygon -> verify(exactly = 1) { polygon.remove() } }
    }

    @Test
    fun testAddFeature_multiGeometry_addedTwice_doesNotLeakOnRemove() {
        // Given
        val mockMap = mockk<GoogleMap>(relaxed = true)
        val mockIconProvider = mockk<IconProvider>(relaxed = true)

        val addedPolylines = mutableListOf<com.google.android.gms.maps.model.Polyline>()
        every { mockMap.addPolyline(any()) } answers {
            mockk<com.google.android.gms.maps.model.Polyline>(relaxed = true).also { addedPolylines.add(it) }
        }

        val renderer = MapViewRenderer(mockMap, mockIconProvider)
        val feature =
            Feature(
                geometry =
                    MultiGeometry(
                        listOf(
                            LineString(listOf(Point(0.0, 0.0), Point(1.0, 1.0))),
                        ),
                    ),
            )

        // When: a hide/show cycle (remove + re-add) followed by a final remove.
        renderer.addFeature(feature)
        renderer.removeFeature(feature)
        renderer.addFeature(feature)
        renderer.removeFeature(feature)

        // Then: every polyline ever added has been removed — nothing is left on the map.
        assertEquals(2, addedPolylines.size)
        addedPolylines.forEach { polyline -> verify(exactly = 1) { polyline.remove() } }
    }

    @Test
    fun testAddFeatureMultiPolygon_appliesPolygonStyleToChildPolygons() {
        // Given
        val mockMap = mockk<GoogleMap>(relaxed = true)
        val mockPolygon = mockk<com.google.android.gms.maps.model.Polygon>(relaxed = true)
        val mockIconProvider = mockk<IconProvider>(relaxed = true)

        val optionsSlot = slot<PolygonOptions>()
        every { mockMap.addPolygon(capture(optionsSlot)) } returns mockPolygon

        val renderer = MapViewRenderer(mockMap, mockIconProvider)

        val childPolygon = Polygon(
            outerBoundary = listOf(Point(0.0, 0.0), Point(1.0, 0.0), Point(1.0, 1.0), Point(0.0, 0.0)),
            innerBoundaries = emptyList()
        )
        val multiGeometry = MultiGeometry(geometries = listOf(childPolygon))
        val style = PolygonStyle(
            fillColor = 0x3F00FF00,
            strokeColor = 0xFFFF0000.toInt(),
            strokeWidth = 2.0f
        )
        val feature = Feature(geometry = multiGeometry, style = style)

        // When
        renderer.addFeature(feature)

        // Then
        verify(exactly = 1) { mockMap.addPolygon(any()) }
        val capturedOptions = optionsSlot.captured
        assertEquals(0x3F00FF00, capturedOptions.fillColor)
        assertEquals(0xFFFF0000.toInt(), capturedOptions.strokeColor)
        assertEquals(2.0f, capturedOptions.strokeWidth, 0.001f)
    }

    @Test
    fun testAddFeatureMultiLineString_appliesLineStyleToChildLines() {
        // Given
        val mockMap = mockk<GoogleMap>(relaxed = true)
        val mockPolyline = mockk<Polyline>(relaxed = true)
        val mockIconProvider = mockk<IconProvider>(relaxed = true)

        val optionsSlot = slot<PolylineOptions>()
        every { mockMap.addPolyline(capture(optionsSlot)) } returns mockPolyline

        val renderer = MapViewRenderer(mockMap, mockIconProvider)

        val childLine = LineString(points = listOf(Point(0.0, 0.0), Point(1.0, 1.0)))
        val multiGeometry = MultiGeometry(geometries = listOf(childLine))
        val style = LineStyle(color = 0xFF0000FF.toInt(), width = 3.0f)
        val feature = Feature(geometry = multiGeometry, style = style)

        // When
        renderer.addFeature(feature)

        // Then
        verify(exactly = 1) { mockMap.addPolyline(any()) }
        val capturedOptions = optionsSlot.captured
        assertEquals(0xFF0000FF.toInt(), capturedOptions.color)
        assertEquals(3.0f, capturedOptions.width, 0.001f)
    }

    @Test
    fun testRemoveFeature_removesRenderedObjects() {
        // Given
        val mockMap = mockk<GoogleMap>(relaxed = true)
        val mockPolygon = mockk<com.google.android.gms.maps.model.Polygon>(relaxed = true)
        val mockIconProvider = mockk<IconProvider>(relaxed = true)

        every { mockMap.addPolygon(any()) } returns mockPolygon

        val renderer = MapViewRenderer(mockMap, mockIconProvider)

        val childPolygon = Polygon(
            outerBoundary = listOf(Point(0.0, 0.0), Point(1.0, 0.0), Point(1.0, 1.0), Point(0.0, 0.0)),
            innerBoundaries = emptyList()
        )
        val multiGeometry = MultiGeometry(geometries = listOf(childPolygon))
        val feature = Feature(geometry = multiGeometry, style = PolygonStyle(fillColor = 0xFF00FF00.toInt()))

        renderer.addFeature(feature)

        // When
        renderer.removeFeature(feature)

        // Then
        verify(exactly = 1) { mockPolygon.remove() }
    }
}
