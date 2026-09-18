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
import com.google.android.gms.maps.model.Circle
import com.google.android.gms.maps.model.CircleOptions
import com.google.common.truth.Truth.assertThat
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/**
 * Unit tests for [CircleManager].
 */
@RunWith(RobolectricTestRunner::class)
class CircleManagerTest {

    private lateinit var map: GoogleMap
    private lateinit var circleManager: CircleManager

    @Before
    fun setUp() {
        map = mockk(relaxed = true)
        circleManager = CircleManager(map)
    }

    @Test
    fun testAddAndRemoveCircle() {
        val mockCircle = mockk<Circle>(relaxed = true)
        every { map.addCircle(any()) } returns mockCircle

        val collection = circleManager.newCollection()
        val circle = collection.addCircle(CircleOptions())

        assertThat(circle).isEqualTo(mockCircle)
        assertThat(collection.getCircles()).containsExactly(mockCircle)

        collection.remove(circle)
        verify { mockCircle.remove() }
        assertThat(collection.getCircles()).isEmpty()
    }

    @Test
    fun testAddAllAndVisibility() {
        val circle1 = mockk<Circle>(relaxed = true)
        val circle2 = mockk<Circle>(relaxed = true)
        every { map.addCircle(any()) } returnsMany listOf(circle1, circle2)

        val collection = circleManager.newCollection()
        collection.addAll(listOf(CircleOptions(), CircleOptions()), defaultVisible = false)

        assertThat(collection.getCircles()).hasSize(2)
        verify { circle1.isVisible = false }
        verify { circle2.isVisible = false }

        collection.showAll()
        verify { circle1.isVisible = true }
        verify { circle2.isVisible = true }

        collection.hideAll()
        verify(atLeast = 2) { circle1.isVisible = false }
        verify(atLeast = 2) { circle2.isVisible = false }
    }

    @Test
    fun testCircleClickDelegation() {
        val circle = mockk<Circle>(relaxed = true)
        every { map.addCircle(any()) } returns circle

        val collection = circleManager.newCollection()
        collection.addCircle(CircleOptions())

        var clicked = false
        collection.setOnCircleClickListener { clicked = true }

        circleManager.onCircleClick(circle)
        assertThat(clicked).isTrue()
    }
}
