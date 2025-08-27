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
import org.junit.Assert.assertNotEquals
import org.junit.Test

class PointTest {

    @Test
    fun testInitialization() {
        val point = Point(1.0, 2.0)
        assertEquals(1.0, point.x, 0.0)
        assertEquals(2.0, point.y, 0.0)
    }

    @Test
    fun testToString() {
        val point = Point(1.0, 2.0)
        assertEquals("Point(x=1.0, y=2.0)", point.toString())
    }

    @Test
    fun testEqualsAndHashCode() {
        val point1 = Point(1.0, 2.0)
        val point2 = Point(1.0, 2.0)
        val point3 = Point(2.0, 1.0)

        assertEquals(point1, point2)
        assertEquals(point1.hashCode(), point2.hashCode())

        assertNotEquals(point1, point3)
        assertNotEquals(point1.hashCode(), point3.hashCode())
    }

    @Test
    fun testCopy() {
        val original = Point(1.0, 2.0)
        val copy = original.copy()
        val modifiedCopy = original.copy(y = 3.0)

        assertEquals(original, copy)
        assertNotEquals(original, modifiedCopy)
        assertEquals(1.0, modifiedCopy.x, 0.0)
        assertEquals(3.0, modifiedCopy.y, 0.0)
    }
}