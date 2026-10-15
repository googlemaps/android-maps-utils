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

import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.GroundOverlay
import com.google.android.gms.maps.model.GroundOverlayOptions
import com.google.common.truth.Truth.assertThat
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/**
 * Unit tests for [GroundOverlayManager].
 */
@RunWith(RobolectricTestRunner::class)
class GroundOverlayManagerTest {

    private lateinit var map: GoogleMap
    private lateinit var manager: GroundOverlayManager

    @Before
    fun setUp() {
        map = mockk(relaxed = true)
        manager = GroundOverlayManager(map)
    }

    @Test
    fun testAddAndRemoveGroundOverlay() {
        val mockOverlay = mockk<GroundOverlay>(relaxed = true)
        every { map.addGroundOverlay(any()) } returns mockOverlay

        val collection = manager.newCollection()
        val overlay = collection.addGroundOverlay(GroundOverlayOptions())

        assertThat(overlay).isEqualTo(mockOverlay)
        assertThat(collection.getGroundOverlays()).containsExactly(mockOverlay)

        collection.remove(overlay)
        verify { mockOverlay.remove() }
        assertThat(collection.getGroundOverlays()).isEmpty()
    }

    @Test
    fun testAddAllAndVisibility() {
        val o1 = mockk<GroundOverlay>(relaxed = true)
        val o2 = mockk<GroundOverlay>(relaxed = true)
        every { map.addGroundOverlay(any()) } returnsMany listOf(o1, o2)

        val collection = manager.newCollection()
        collection.addAll(listOf(GroundOverlayOptions(), GroundOverlayOptions()), defaultVisible = false)

        assertThat(collection.getGroundOverlays()).hasSize(2)
        verify { o1.isVisible = false }
        verify { o2.isVisible = false }

        collection.showAll()
        verify { o1.isVisible = true }
        verify { o2.isVisible = true }

        collection.hideAll()
        verify(atLeast = 2) { o1.isVisible = false }
        verify(atLeast = 2) { o2.isVisible = false }
    }

    @Test
    fun testGroundOverlayClickDelegation() {
        val overlay = mockk<GroundOverlay>(relaxed = true)
        every { map.addGroundOverlay(any()) } returns overlay

        val collection = manager.newCollection()
        collection.addGroundOverlay(GroundOverlayOptions())

        var clicked = false
        collection.setOnGroundOverlayClickListener { clicked = true }

        manager.onGroundOverlayClick(overlay)
        assertThat(clicked).isTrue()
    }
}
