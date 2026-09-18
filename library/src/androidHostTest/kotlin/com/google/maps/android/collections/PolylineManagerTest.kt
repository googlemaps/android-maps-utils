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
import com.google.android.gms.maps.model.Polyline
import com.google.android.gms.maps.model.PolylineOptions
import com.google.common.truth.Truth.assertThat
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/**
 * Unit tests for [PolylineManager].
 */
@RunWith(RobolectricTestRunner::class)
class PolylineManagerTest {

    private lateinit var map: GoogleMap
    private lateinit var manager: PolylineManager

    @Before
    fun setUp() {
        map = mockk(relaxed = true)
        manager = PolylineManager(map)
    }

    @Test
    fun testAddAndRemovePolyline() {
        val mockPolyline = mockk<Polyline>(relaxed = true)
        every { map.addPolyline(any()) } returns mockPolyline

        val collection = manager.newCollection()
        val line = collection.addPolyline(PolylineOptions())

        assertThat(line).isEqualTo(mockPolyline)
        assertThat(collection.getPolylines()).containsExactly(mockPolyline)

        collection.remove(line)
        verify { mockPolyline.remove() }
        assertThat(collection.getPolylines()).isEmpty()
    }

    @Test
    fun testAddAllAndVisibility() {
        val l1 = mockk<Polyline>(relaxed = true)
        val l2 = mockk<Polyline>(relaxed = true)
        every { map.addPolyline(any()) } returnsMany listOf(l1, l2)

        val collection = manager.newCollection()
        collection.addAll(listOf(PolylineOptions(), PolylineOptions()), defaultVisible = false)

        assertThat(collection.getPolylines()).hasSize(2)
        verify { l1.isVisible = false }
        verify { l2.isVisible = false }

        collection.showAll()
        verify { l1.isVisible = true }
        verify { l2.isVisible = true }

        collection.hideAll()
        verify(atLeast = 2) { l1.isVisible = false }
        verify(atLeast = 2) { l2.isVisible = false }
    }

    @Test
    fun testPolylineClickDelegation() {
        val line = mockk<Polyline>(relaxed = true)
        every { map.addPolyline(any()) } returns line

        val collection = manager.newCollection()
        collection.addPolyline(PolylineOptions())

        var clicked = false
        collection.setOnPolylineClickListener { clicked = true }

        manager.onPolylineClick(line)
        assertThat(clicked).isTrue()
    }
}
