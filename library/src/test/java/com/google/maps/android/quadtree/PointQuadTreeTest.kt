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

package com.google.maps.android.quadtree

import com.google.maps.android.geometry.Bounds
import com.google.maps.android.geometry.Point
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.util.Random

class PointQuadTreeTest {

    private lateinit var mTree: PointQuadTree<Item>

    @Before
    fun setUp() {
        mTree = PointQuadTree(0.0, 1.0, 0.0, 1.0)
    }

    @Test
    fun testAddOnePoint() {
        val item = Item(0.0, 0.0)
        mTree.add(item)
        val items = searchAll()
        assertEquals(1, items.size)
        mTree.clear()
    }

    @Test
    fun testEmpty() {
        val items = searchAll()
        assertEquals(0, items.size)
    }

    @Test
    fun testMultiplePoints() {
        var response: Boolean
        val item1 = Item(0.0, 0.0)

        // Remove item that isn't yet in the QuadTree
        response = mTree.remove(item1)
        assertFalse(response)

        mTree.add(item1)
        val item2 = Item(.1, .1)
        mTree.add(item2)
        val item3 = Item(.2, .2)
        mTree.add(item3)

        val items = searchAll()
        assertEquals(3, items.size)

        assertTrue(items.contains(item1))
        assertTrue(items.contains(item2))
        assertTrue(items.contains(item3))

        response = mTree.remove(item1)
        assertTrue(response)
        response = mTree.remove(item2)
        assertTrue(response)
        response = mTree.remove(item3)
        assertTrue(response)

        assertEquals(0, searchAll().size)

        // Remove item that is no longer in the QuadTree
        response = mTree.remove(item1)
        assertFalse(response)
        mTree.clear()
    }

    @Test
    fun testSameLocationDifferentPoint() {
        mTree.add(Item(0.0, 0.0))
        mTree.add(Item(0.0, 0.0))

        assertEquals(2, searchAll().size)
        mTree.clear()
    }

    @Test
    fun testClear() {
        mTree.add(Item(.1, .1))
        mTree.add(Item(.2, .2))
        mTree.add(Item(.3, .3))

        mTree.clear()
        assertEquals(0, searchAll().size)
    }

    @Test
    fun testSearch() {
        System.gc()
        for (i in 0..9999) {
            mTree.add(Item(i / 20000.0, i / 20000.0))
        }

        assertEquals(10000, searchAll().size)
        assertEquals(
            1, mTree.search(Bounds(0.0, 0.00001, 0.0, 0.00001)).size
        )
        assertEquals(0, mTree.search(Bounds(.7, .8, .7, .8)).size)
        mTree.clear()
        System.gc()
    }

    @Test
    fun testFourPoints() {
        mTree.add(Item(0.2, 0.2))
        mTree.add(Item(0.7, 0.2))
        mTree.add(Item(0.2, 0.7))
        mTree.add(Item(0.7, 0.7))

        assertEquals(2, mTree.search(Bounds(0.0, 0.5, 0.0, 1.0)).size)
        mTree.clear()
    }

    /**
     * Tests 30,000 items at the same point. Timing results are averaged.
     */
    @Test
    fun testVeryDeepTree() {
        System.gc()
        repeat(30000) {
            mTree.add(Item(0.0, 0.0))
        }

        assertEquals(30000, searchAll().size)
        assertEquals(30000, mTree.search(Bounds(0.0, .1, 0.0, .1)).size)
        assertEquals(0, mTree.search(Bounds(.1, 1.0, .1, 1.0)).size)

        mTree.clear()
        System.gc()
    }

    /**
     * Tests 400,000 points relatively uniformly distributed across the space. Timing results are
     * averaged.
     */
    @Test
    fun testManyPoints() {
        System.gc()
        for (i in 0..199) {
            for (j in 0..1999) {
                mTree.add(Item(i / 200.0, j / 2000.0))
            }
        }

        // searching bounds that are exact subtrees of the main quadTree
        assertEquals(400000, searchAll().size)
        assertEquals(100000, mTree.search(Bounds(0.0, .5, 0.0, .5)).size)
        assertEquals(100000, mTree.search(Bounds(.5, 1.0, 0.0, .5)).size)
        assertEquals(25000, mTree.search(Bounds(0.0, .25, 0.0, .25)).size)
        assertEquals(25000, mTree.search(Bounds(.75, 1.0, .75, 1.0)).size)

        // searching bounds that do not line up with main quadTree
        assertEquals(399800, mTree.search(Bounds(0.0, 0.999, 0.0, 0.999)).size)
        assertEquals(4221, mTree.search(Bounds(0.8, 0.9, 0.8, 0.9)).size)
        assertEquals(4200, mTree.search(Bounds(0.0, 1.0, 0.0, 0.01)).size)
        assertEquals(16441, mTree.search(Bounds(0.4, 0.6, 0.4, 0.6)).size)

        // searching bounds that are small / have very exact end points
        assertEquals(1, mTree.search(Bounds(0.0, .001, 0.0, .0001)).size)
        assertEquals(26617, mTree.search(Bounds(0.356, 0.574, 0.678, 0.987)).size)
        assertEquals(44689, mTree.search(Bounds(0.123, 0.456, 0.456, 0.789)).size)
        assertEquals(4906, mTree.search(Bounds(0.111, 0.222, 0.333, 0.444)).size)

        mTree.clear()
        assertEquals(0, searchAll().size)
        System.gc()
    }

    @Test
    fun testAddOutsideBounds() {
        mTree.add(Item(-1.0, -1.0))
        assertEquals(0, searchAll().size)
    }

    @Test
    fun testReAdd() {
        val item = Item(0.0, 0.0)
        mTree.add(item)
        mTree.add(item)
        assertEquals(1, searchAll().size)
    }

    @Test
    fun testSearchOutsideBounds() {
        mTree.add(Item(0.0, 0.0))
        assertEquals(0, mTree.search(Bounds(10.0, 20.0, 10.0, 20.0)).size)
    }

    /**
     * Runs a test with 100,000 points. Timing results are averaged.
     */
    @Test
    fun testRandomPoints() {
        System.gc()
        Random().apply {
            repeat(100000) {
                mTree.add(Item(nextDouble(), nextDouble()))
            }
        }
        searchAll()

        mTree.search(Bounds(0.0, 0.5, 0.0, 0.5))
        mTree.search(Bounds(0.0, 0.25, 0.0, 0.25))
        mTree.search(Bounds(0.0, 0.125, 0.0, 0.125))
        mTree.search(Bounds(0.0, 0.999, 0.0, 0.999))
        mTree.search(Bounds(0.0, 1.0, 0.0, 0.01))
        mTree.search(Bounds(0.4, 0.6, 0.4, 0.6))
        mTree.search(Bounds(0.356, 0.574, 0.678, 0.987))
        mTree.search(Bounds(0.123, 0.456, 0.456, 0.789))
        mTree.search(Bounds(0.111, 0.222, 0.333, 0.444))

        mTree.clear()
        System.gc()
    }

    private fun searchAll(): Collection<Item> {
        return mTree.search(Bounds(0.0, 1.0, 0.0, 1.0))
    }

    private class Item(x: Double, y: Double) : PointQuadTree.Item {
        override val point: Point = Point(x, y)
    }
}