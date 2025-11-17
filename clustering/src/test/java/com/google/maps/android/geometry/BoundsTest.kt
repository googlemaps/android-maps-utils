/*
 * Copyright 2025 Google LLC.
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

package com.google.maps.android.geometry

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class BoundsTest {

    @Test
    fun testInitialization() {
        val bounds = Bounds(0.0, 10.0, 0.0, 20.0)
        assertEquals(0.0, bounds.minX, 0.0)
        assertEquals(10.0, bounds.maxX, 0.0)
        assertEquals(0.0, bounds.minY, 0.0)
        assertEquals(20.0, bounds.maxY, 0.0)
        assertEquals(5.0, bounds.midX, 0.0)
        assertEquals(10.0, bounds.midY, 0.0)
    }

    @Test
    fun testContainsPoint() {
        val bounds = Bounds(0.0, 10.0, 0.0, 10.0)
        assertTrue(bounds.contains(Point(5.0, 5.0)))
        assertTrue(bounds.contains(Point(0.0, 0.0)))
        assertTrue(bounds.contains(Point(10.0, 10.0)))
        assertFalse(bounds.contains(Point(11.0, 5.0)))
        assertFalse(bounds.contains(Point(5.0, 11.0)))
    }

    @Test
    fun testContainsCoordinates() {
        val bounds = Bounds(0.0, 10.0, 0.0, 10.0)
        assertTrue(bounds.contains(5.0, 5.0))
        assertTrue(bounds.contains(0.0, 0.0))
        assertTrue(bounds.contains(10.0, 10.0))
        assertFalse(bounds.contains(11.0, 5.0))
        assertFalse(bounds.contains(5.0, 11.0))
    }

    @Test
    fun testIntersectsBounds() {
        val bounds1 = Bounds(0.0, 10.0, 0.0, 10.0)
        val bounds2 = Bounds(5.0, 15.0, 5.0, 15.0)
        val bounds3 = Bounds(11.0, 20.0, 11.0, 20.0)
        val bounds4 = Bounds(0.0, 10.0, 11.0, 20.0)

        assertTrue(bounds1.intersects(bounds2))
        assertFalse(bounds1.intersects(bounds3))
        assertFalse(bounds1.intersects(bounds4))
    }

    @Test
    fun testIntersectsCoordinates() {
        val bounds = Bounds(0.0, 10.0, 0.0, 10.0)
        assertTrue(bounds.intersects(5.0, 15.0, 5.0, 15.0))
        assertFalse(bounds.intersects(11.0, 20.0, 11.0, 20.0))
    }

    @Test
    fun testContainsBounds() {
        val bounds1 = Bounds(0.0, 10.0, 0.0, 10.0)
        val bounds2 = Bounds(2.0, 8.0, 2.0, 8.0)
        val bounds3 = Bounds(5.0, 15.0, 5.0, 15.0)

        assertTrue(bounds1.contains(bounds2))
        assertFalse(bounds1.contains(bounds3))
    }
}
