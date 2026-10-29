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
package com.google.maps.android.clustering.algo

import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FlatKdTreeTest {
    @Test
    fun testEmptyTreeFindsNothing() {
        val tree = FlatKdTree(DoubleArray(0), IntArray(0))
        val found = mutableListOf<Int>()

        tree.queryRange(0.0, 0.0, 1.0, 1.0) { found.add(it) }
        tree.queryRadius(0.5, 0.5, 1.0) { found.add(it) }

        assertEquals(0, tree.size)
        assertTrue(found.isEmpty())
    }

    @Test
    fun testConstructionKeepsEveryId() {
        val coords = randomCoords(1_000)
        val tree = FlatKdTree(coords, IntArray(1_000) { it })

        assertEquals(1_000, tree.size)
        assertEquals((0 until 1_000).toSet(), tree.ids.toSet())
    }

    @Test
    fun testQueryRangeMatchesBruteForce() {
        val count = 2_000
        val coords = randomCoords(count)
        // A small node size forces a deep tree, so the partitioning is exercised.
        val tree = FlatKdTree(coords, IntArray(count) { it }, nodeSize = 4)

        val boxes =
            listOf(
                doubleArrayOf(0.0, 0.0, 1.0, 1.0),
                doubleArrayOf(0.1, 0.2, 0.4, 0.5),
                doubleArrayOf(0.5, 0.5, 0.5, 0.5),
                doubleArrayOf(0.9, 0.0, 1.0, 0.05),
                doubleArrayOf(2.0, 2.0, 3.0, 3.0),
            )
        for ((minX, minY, maxX, maxY) in boxes) {
            val expected =
                (0 until count).filter { id ->
                    val x = coords[2 * id]
                    val y = coords[2 * id + 1]
                    x in minX..maxX && y in minY..maxY
                }
            val found = mutableListOf<Int>()
            tree.queryRange(minX, minY, maxX, maxY) { found.add(it) }

            assertEquals(expected.sorted(), found.sorted())
        }
    }

    @Test
    fun testQueryRadiusMatchesBruteForce() {
        val count = 2_000
        val coords = randomCoords(count)
        val tree = FlatKdTree(coords, IntArray(count) { it }, nodeSize = 4)

        val queries =
            listOf(
                doubleArrayOf(0.5, 0.5, 0.1),
                doubleArrayOf(0.0, 0.0, 0.25),
                doubleArrayOf(0.9, 0.1, 0.01),
                doubleArrayOf(0.3, 0.7, 0.0),
            )
        for ((qx, qy, radius) in queries) {
            val expected =
                (0 until count).filter { id ->
                    val dx = coords[2 * id] - qx
                    val dy = coords[2 * id + 1] - qy
                    dx * dx + dy * dy <= radius * radius
                }
            val found = mutableListOf<Int>()
            tree.queryRadius(qx, qy, radius) { found.add(it) }

            assertEquals(expected.sorted(), found.sorted())
        }
    }

    private fun randomCoords(count: Int): DoubleArray {
        val random = Random(1234)
        return DoubleArray(2 * count) { random.nextDouble() }
    }
}
