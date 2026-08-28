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

import com.google.android.gms.maps.model.LatLng
import com.google.common.truth.Truth.assertThat
import com.google.maps.android.clustering.ClusterItem
import org.junit.Before
import org.junit.Test

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

    @Before
    fun setUp() {
        algorithm = GridBasedAlgorithm()
    }

    @Test
    fun testAddAndGetClusters() {
        val item1 = TestItem(0.0, 0.0)
        val item2 = TestItem(0.0001, 0.0001) // Very close -> same cell
        val farItem = TestItem(50.0, 50.0) // Far away -> separate cell

        algorithm.addItems(listOf(item1, item2, farItem))
        assertThat(algorithm.items).hasSize(3)

        // Low zoom: item1 and item2 cluster together into 1 cell, farItem into another
        val clusters = algorithm.getClusters(3f)
        assertThat(clusters).hasSize(2)
    }

    @Test
    fun testUpdateAndRemoveItems() {
        val item = TestItem(10.0, 10.0)
        algorithm.addItem(item)
        assertThat(algorithm.items).hasSize(1)

        assertThat(algorithm.updateItem(item)).isTrue()
        assertThat(algorithm.items).hasSize(1)

        val nonExistent = TestItem(20.0, 20.0)
        assertThat(algorithm.updateItem(nonExistent)).isFalse()

        assertThat(algorithm.removeItem(item)).isTrue()
        assertThat(algorithm.items).isEmpty()

        algorithm.addItems(listOf(item, nonExistent))
        assertThat(algorithm.removeItems(listOf(item, nonExistent))).isTrue()
        assertThat(algorithm.items).isEmpty()

        algorithm.addItem(item)
        algorithm.clearItems()
        assertThat(algorithm.items).isEmpty()
    }

    @Test
    fun testMaxDistanceProperty() {
        algorithm.maxDistanceBetweenClusteredItems = 150
        assertThat(algorithm.maxDistanceBetweenClusteredItems).isEqualTo(150)
    }
}
