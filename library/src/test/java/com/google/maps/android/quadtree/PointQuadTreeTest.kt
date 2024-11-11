/*
 * Copyright 2014 Google Inc.
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

import com.google.common.truth.Truth.assertThat
import com.google.maps.android.geometry.Bounds
import com.google.maps.android.geometry.Point
import org.junit.Test

class PointQuadTreeTest {
    @Test
    fun testAddOnePoint() {
        val tree = PointQuadTree<Item>(0.0, 1.0, 0.0, 1.0)

        tree.add(Item(0.0, 0.0))

        with(searchAll(tree)) {
            assertThat(this).hasSize(1)
            assertThat(this).containsExactly(Item(0.0, 0.0))
        }
    }

    @Test
    fun testEmpty() {
        val tree = PointQuadTree<Item>(0.0, 1.0, 0.0, 1.0)

        with(searchAll(tree)) {
            assertThat(this).isEmpty()
        }
    }

    @Test
    fun testMultiplePoints() {
        val tree = PointQuadTree<Item>(0.0, 1.0, 0.0, 1.0)

        val items = buildList {
            add(Item(0.0, 0.0))
            add(Item(0.1, 0.1))
            add(Item(0.2, 0.2))
        }

        // Remove item that isn't yet in the QuadTree
        assertThat(tree.remove(items.first())).isFalse()

        for (item in items) {
            tree.add(item)
        }

        with(searchAll(tree)) {
            assertThat(this).hasSize(3)
            assertThat(this).containsExactlyElementsIn(items)
        }

        items.forEach {
            assertThat(tree.remove(it)).isTrue()
        }

        assertThat(searchAll(tree)).isEmpty()

        // Remove item that is no longer in the QuadTree
        assertThat(tree.remove(items.first())).isFalse()
    }

    @Test
    fun testSameLocationDifferentPoint() {
        val tree = PointQuadTree<Item>(0.0, 1.0, 0.0, 1.0)

        tree.add(Item(0.0, 0.0, 0))
        tree.add(Item(0.0, 0.0, 1))

        assertThat(searchAll(tree)).hasSize(2)
    }

    @Test
    fun testClear() {
        val tree = PointQuadTree<Item>(0.0, 1.0, 0.0, 1.0)

        tree.add(Item(.1, .1))
        tree.add(Item(.2, .2))
        tree.add(Item(.3, .3))

        tree.clear()
        assertThat(searchAll(tree)).isEmpty()
    }

    @Test
    fun testSearch() {
        val tree = PointQuadTree<Item>(0.0, 1.0, 0.0, 1.0)

        for (i in 0..9999) {
            tree.add(Item(i / 20_000.0, i / 20_000.0))
        }

        assertThat(
            tree.search(Bounds(0.0, 0.00001, 0.0, 0.00001))
        ).hasSize(1)

        assertThat(
            tree.search(Bounds(.7, .8, .7, .8))
        ).isEmpty()
    }

    @Test
    fun testFourPoints() {
        val tree = PointQuadTree<Item>(0.0, 1.0, 0.0, 1.0)

        tree.add(Item(0.2, 0.2))
        tree.add(Item(0.7, 0.2))
        tree.add(Item(0.2, 0.7))
        tree.add(Item(0.7, 0.7))

        assertThat(tree.search(Bounds(0.0, 0.5, 0.0, 1.0))).hasSize(2)
    }

    /**
     * Tests 30,000 items at the same point. Timing results are averaged.
     */
    @Test
    fun testVeryDeepTree() {
        val tree = PointQuadTree<Item>(0.0, 1.0, 0.0, 1.0)

        for (i in 0..29999) {
            tree.add(Item(0.0, 0.0, i))
        }

        assertThat(searchAll(tree)).hasSize(30000)
        assertThat(tree.search(Bounds(0.0, .1, 0.0, .1))).hasSize(30000)
        assertThat(tree.search(Bounds(0.1, 1.0, 0.1, 1.0))).isEmpty()
    }

    /**
     * Tests 400,000 points relatively uniformly distributed across the space. Timing results are
     * averaged.
     */

    @Test
    fun testManyPoints() {
        val tree = PointQuadTree<Item>(0.0, 1.0, 0.0, 1.0)

        for (i in 0..< 200) {
            for (j in 0..< 2000) {
                tree.add(Item(i / 200.0, j / 2000.0))
            }
        }

        // Searching bounds that are exact subtrees of the main quadTree
        assertThat(searchAll(tree)).hasSize(400000)          // Using Truth
        assertThat(tree.search(Bounds(0.0, .5, 0.0, .5))).hasSize(100000)
        assertThat(tree.search(Bounds(.5, 1.0, 0.0, .5))).hasSize(100000)
        assertThat(tree.search(Bounds(0.0, .25, 0.0, .25))).hasSize(25000)
        assertThat(tree.search(Bounds(.75, 1.0, .75, 1.0))).hasSize(25000)

        // Searching bounds that do not line up with main quadTree
        assertThat(tree.search(Bounds(0.0, 0.999, 0.0, 0.999))).hasSize(399800)
        assertThat(tree.search(Bounds(0.8, 0.9, 0.8, 0.9))).hasSize(4221)
        assertThat(tree.search(Bounds(0.0, 1.0, 0.0, 0.01))).hasSize(4200)
        assertThat(tree.search(Bounds(0.4, 0.6, 0.4, 0.6))).hasSize(16441)

        // Searching bounds that are small / have very exact end points
        assertThat(tree.search(Bounds(0.0, .001, 0.0, .0001))).hasSize(1)
        assertThat(tree.search(Bounds(0.356, 0.574, 0.678, 0.987))).hasSize(26617)
        assertThat(tree.search(Bounds(0.123, 0.456, 0.456, 0.789))).hasSize(44689)
        assertThat(tree.search(Bounds(0.111, 0.222, 0.333, 0.444))).hasSize(4906)

        tree.clear()
        assertThat(searchAll(tree)).isEmpty()
    }
    
    private fun searchAll(tree: PointQuadTree<Item>): Collection<Item> {
        return tree.search(Bounds(0.0, 1.0, 0.0, 1.0))
    }

    private data class Item(val x: Double, val y: Double, val tag: Any? = null) : PointQuadTree.Item {
        override val point: Point = Point(x, y)
    }
}
