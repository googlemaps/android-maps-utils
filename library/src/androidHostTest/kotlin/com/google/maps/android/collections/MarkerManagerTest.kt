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

package com.google.maps.android.collections

import android.view.View
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.AdvancedMarkerOptions
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.common.truth.Truth.assertThat
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/**
 * Unit tests for [MarkerManager].
 */
@RunWith(RobolectricTestRunner::class)
class MarkerManagerTest {

    private lateinit var map: GoogleMap
    private lateinit var markerManager: MarkerManager

    @Before
    fun setUp() {
        map = mockk(relaxed = true)
        markerManager = MarkerManager(map)
    }

    @Test
    fun testAddMarkerWithOptions() {
        val mockMarker = mockk<Marker>(relaxed = true)
        every { map.addMarker(any<MarkerOptions>()) } returns mockMarker

        val collection = markerManager.newCollection()
        val opts = MarkerOptions()
        val added = collection.addMarker(opts)

        assertThat(added).isEqualTo(mockMarker)
        assertThat(collection.getMarkers()).containsExactly(mockMarker)

        collection.remove(added)
        verify { mockMarker.remove() }
        assertThat(collection.getMarkers()).isEmpty()
    }

    @Test
    fun testAddAdvancedMarker() {
        val mockMarker = mockk<Marker>(relaxed = true)
        every { map.addMarker(any<AdvancedMarkerOptions>()) } returns mockMarker

        val collection = markerManager.newCollection()
        val opts = AdvancedMarkerOptions()
        val added = collection.addMarker(opts)

        assertThat(added).isEqualTo(mockMarker)
        assertThat(collection.getMarkers()).containsExactly(mockMarker)
    }

    @Test
    fun testAddAllAndVisibility() {
        val marker1 = mockk<Marker>(relaxed = true)
        val marker2 = mockk<Marker>(relaxed = true)
        every { map.addMarker(any<MarkerOptions>()) } returnsMany listOf(marker1, marker2)

        val collection = markerManager.newCollection()
        collection.addAll(listOf(MarkerOptions(), MarkerOptions()), defaultVisible = false)

        assertThat(collection.getMarkers()).hasSize(2)
        verify { marker1.isVisible = false }
        verify { marker2.isVisible = false }

        collection.showAll()
        verify { marker1.isVisible = true }
        verify { marker2.isVisible = true }

        collection.hideAll()
        verify(atLeast = 2) { marker1.isVisible = false }
        verify(atLeast = 2) { marker2.isVisible = false }
    }

    @Test
    fun testMarkerEventDelegation() {
        val marker = mockk<Marker>(relaxed = true)
        every { map.addMarker(any<MarkerOptions>()) } returns marker

        val collection = markerManager.newCollection()
        collection.addMarker(MarkerOptions())

        var clicked = false
        collection.setOnMarkerClickListener {
            clicked = true
            true
        }

        var infoClicked = false
        collection.setOnInfoWindowClickListener { infoClicked = true }

        var infoLongClicked = false
        collection.setOnInfoWindowLongClickListener { infoLongClicked = true }

        var dragStarted = false
        var dragging = false
        var dragEnded = false
        collection.setOnMarkerDragListener(object : GoogleMap.OnMarkerDragListener {
            override fun onMarkerDragStart(m: Marker) { dragStarted = true }
            override fun onMarkerDrag(m: Marker) { dragging = true }
            override fun onMarkerDragEnd(m: Marker) { dragEnded = true }
        })

        val mockView = mockk<View>()
        collection.setInfoWindowAdapter(object : GoogleMap.InfoWindowAdapter {
            override fun getInfoWindow(m: Marker): View = mockView
            override fun getInfoContents(m: Marker): View? = null
        })

        assertThat(markerManager.onMarkerClick(marker)).isTrue()
        assertThat(clicked).isTrue()

        markerManager.onInfoWindowClick(marker)
        assertThat(infoClicked).isTrue()

        markerManager.onInfoWindowLongClick(marker)
        assertThat(infoLongClicked).isTrue()

        markerManager.onMarkerDragStart(marker)
        assertThat(dragStarted).isTrue()

        markerManager.onMarkerDrag(marker)
        assertThat(dragging).isTrue()

        markerManager.onMarkerDragEnd(marker)
        assertThat(dragEnded).isTrue()

        assertThat(markerManager.getInfoWindow(marker)).isEqualTo(mockView)
        assertThat(markerManager.getInfoContents(marker)).isNull()
    }
}
