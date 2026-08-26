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

package com.google.maps.android.collections.overlay

import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.common.truth.Truth.assertThat
import com.google.maps.android.collections.MarkerManager
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Assert.assertThrows
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/**
 * Unit tests for [OverlayManager].
 */
@RunWith(RobolectricTestRunner::class)
class OverlayManagerTest {

    private lateinit var map: GoogleMap
    private lateinit var sharedMarkerManager: MarkerManager
    private lateinit var overlayManager: OverlayManager

    @Before
    fun setUp() {
        map = mockk(relaxed = true)
        sharedMarkerManager = MarkerManager(map)
        // Initialize OverlayManager sharing the same MarkerManager (Coexistence mode)
        overlayManager = OverlayManager(map = map, markerManager = sharedMarkerManager)
    }

    @Test
    fun testCollectionManagement() {
        val col1 = overlayManager.newCollection("routes")
        assertThat(overlayManager.getCollection("routes")).isSameInstanceAs(col1)

        // Unique ID constraint
        assertThrows(IllegalArgumentException::class.java) {
            overlayManager.newCollection("routes")
        }

        // Anonymous collection
        val anon = overlayManager.newCollection()
        assertThat(anon.id).isNull()

        // Remove collection
        assertThat(overlayManager.removeCollection("routes")).isTrue()
        assertThat(overlayManager.getCollection("routes")).isNull()
        assertThat(overlayManager.removeCollection("non_existent")).isFalse()
    }

    @Test
    fun testCoexistenceWithLegacyManager() {
        val mockLegacyMarker = mockk<Marker>(relaxed = true)
        val mockOverlayMarker = mockk<Marker>(relaxed = true)

        every { map.addMarker(any<MarkerOptions>()) } returnsMany listOf(mockLegacyMarker, mockOverlayMarker)

        // Legacy marker created via shared MarkerManager
        val legacyCollection = sharedMarkerManager.newCollection()
        val legacyMarker = legacyCollection.addMarker(MarkerOptions())

        // Unified overlay created via OverlayManager
        val overlayCollection = overlayManager.newCollection("unified")
        val overlayMarker = overlayCollection.add(MarkerOptions())

        var legacyClicked = false
        legacyCollection.setOnMarkerClickListener {
            legacyClicked = true
            true
        }

        var overlayClicked = false
        overlayMarker.onClick {
            overlayClicked = true
            true
        }

        // Clicking legacy marker invokes legacy listener
        assertThat(sharedMarkerManager.onMarkerClick(legacyMarker)).isTrue()
        assertThat(legacyClicked).isTrue()
        assertThat(overlayClicked).isFalse()

        // Clicking overlay marker invokes overlay listener
        assertThat(sharedMarkerManager.onMarkerClick(mockOverlayMarker)).isTrue()
        assertThat(overlayClicked).isTrue()

        // Test drag delegation through shared manager
        var dragStarted = false
        var dragging = false
        var dragEnded = false
        overlayMarker.onDrag(
            onStart = { dragStarted = true },
            onDrag = { dragging = true },
            onEnd = { dragEnded = true }
        )

        sharedMarkerManager.onMarkerDragStart(mockOverlayMarker)
        assertThat(dragStarted).isTrue()

        sharedMarkerManager.onMarkerDrag(mockOverlayMarker)
        assertThat(dragging).isTrue()

        sharedMarkerManager.onMarkerDragEnd(mockOverlayMarker)
        assertThat(dragEnded).isTrue()

        // Test info window delegation
        val mockView = mockk<android.view.View>()
        overlayMarker.setCustomInfoWindow(infoWindow = { mockView })
        assertThat(sharedMarkerManager.getInfoWindow(mockOverlayMarker)).isEqualTo(mockView)
        assertThat(sharedMarkerManager.getInfoContents(mockOverlayMarker)).isNull()
    }

    @Test
    fun testClearAll() {
        val mockMarker = mockk<Marker>(relaxed = true)
        every { map.addMarker(any<MarkerOptions>()) } returns mockMarker

        val col1 = overlayManager.newCollection("col1")
        col1.add(MarkerOptions())
        val anon = overlayManager.newCollection()
        anon.add(MarkerOptions())

        overlayManager.clearAll()
        assertThat(col1.isEmpty).isTrue()
        assertThat(anon.isEmpty).isTrue()
        assertThat(overlayManager.getCollection("col1")).isNull()
    }
}
