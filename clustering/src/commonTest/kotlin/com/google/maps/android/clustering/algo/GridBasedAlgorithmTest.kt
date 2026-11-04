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

import com.google.maps.android.clustering.ClusterItem
import com.google.maps.android.model.LatLng
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Unit tests for [GridBasedAlgorithm].
 *
 * Verifies grid-cell based aggregation of nearby items, item mutation, removal, and custom grid sizes.
 */
class GridBasedAlgorithmTest {

    private class TestItem(lat: Double, lng: Double) : ClusterItem {
        override val position: LatLng = LatLng(lat, lng)
        override val title: String? = null
        override val snippet: String? = null
        override val zIndex: Float? = null
    }

    private lateinit var algorithm: GridBasedAlgorithm<TestItem>

    @BeforeTest
    fun setUp() {
        algorithm = GridBasedAlgorithm()
    }

    @Test
    fun testAddAndGetClusters() {
        val item1 = TestItem(0.0, 0.0)
        val item2 = TestItem(0.0001, 0.0001) // Very close -> same cell
        val farItem = TestItem(50.0, 50.0) // Far away -> separate cell

        algorithm.addItems(listOf(item1, item2, farItem))
        assertEquals(3, algorithm.items.size)

        // Low zoom: item1 and item2 cluster together into 1 cell, farItem into another
        val clusters = algorithm.getClusters(3f)
        assertEquals(2, clusters.size)
    }

    @Test
    fun testUpdateAndRemoveItems() {
        val item = TestItem(10.0, 10.0)
        algorithm.addItem(item)
        assertEquals(1, algorithm.items.size)

        assertTrue(algorithm.updateItem(item))
        assertEquals(1, algorithm.items.size)

        val nonExistent = TestItem(20.0, 20.0)
        assertFalse(algorithm.updateItem(nonExistent))

        assertTrue(algorithm.removeItem(item))
        assertTrue(algorithm.items.isEmpty())

        algorithm.addItems(listOf(item, nonExistent))
        assertTrue(algorithm.removeItems(listOf(item, nonExistent)))
        assertTrue(algorithm.items.isEmpty())

        algorithm.addItem(item)
        algorithm.clearItems()
        assertTrue(algorithm.items.isEmpty())
    }

    @Test
    fun testMaxDistanceProperty() {
        algorithm.maxDistanceBetweenClusteredItems = 150
        assertEquals(150, algorithm.maxDistanceBetweenClusteredItems)
    }
}
