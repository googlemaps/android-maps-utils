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
import com.google.maps.android.data.renderer.mapview.MapViewRenderer
import com.google.maps.android.data.renderer.model.Feature
import com.google.maps.android.data.renderer.model.Point
import com.google.maps.android.data.renderer.model.PointGeometry
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.junit.Assert.assertEquals
import org.junit.Test

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
}
