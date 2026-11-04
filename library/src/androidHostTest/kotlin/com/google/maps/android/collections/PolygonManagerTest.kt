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
import com.google.android.gms.maps.model.Polygon
import com.google.android.gms.maps.model.PolygonOptions
import com.google.common.truth.Truth.assertThat
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/**
 * Unit tests for [PolygonManager].
 */
@RunWith(RobolectricTestRunner::class)
class PolygonManagerTest {

    private lateinit var map: GoogleMap
    private lateinit var manager: PolygonManager

    @Before
    fun setUp() {
        map = mockk(relaxed = true)
        manager = PolygonManager(map)
    }

    @Test
    fun testAddAndRemovePolygon() {
        val mockPolygon = mockk<Polygon>(relaxed = true)
        every { map.addPolygon(any()) } returns mockPolygon

        val collection = manager.newCollection()
        val poly = collection.addPolygon(PolygonOptions())

        assertThat(poly).isEqualTo(mockPolygon)
        assertThat(collection.getPolygons()).containsExactly(mockPolygon)

        collection.remove(poly)
        verify { mockPolygon.remove() }
        assertThat(collection.getPolygons()).isEmpty()
    }

    @Test
    fun testAddAllAndVisibility() {
        val p1 = mockk<Polygon>(relaxed = true)
        val p2 = mockk<Polygon>(relaxed = true)
        every { map.addPolygon(any()) } returnsMany listOf(p1, p2)

        val collection = manager.newCollection()
        collection.addAll(listOf(PolygonOptions(), PolygonOptions()), defaultVisible = false)

        assertThat(collection.getPolygons()).hasSize(2)
        verify { p1.isVisible = false }
        verify { p2.isVisible = false }

        collection.showAll()
        verify { p1.isVisible = true }
        verify { p2.isVisible = true }

        collection.hideAll()
        verify(atLeast = 2) { p1.isVisible = false }
        verify(atLeast = 2) { p2.isVisible = false }
    }

    @Test
    fun testPolygonClickDelegation() {
        val poly = mockk<Polygon>(relaxed = true)
        every { map.addPolygon(any()) } returns poly

        val collection = manager.newCollection()
        collection.addPolygon(PolygonOptions())

        var clicked = false
        collection.setOnPolygonClickListener { clicked = true }

        manager.onPolygonClick(poly)
        assertThat(clicked).isTrue()
    }
}
