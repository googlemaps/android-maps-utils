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
import com.google.maps.android.model.CameraPosition
import com.google.maps.android.model.LatLng
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Unit tests for [ScreenBasedAlgorithmAdapter].
 *
 * Proves that [ScreenBasedAlgorithmAdapter] adapts standard [Algorithm] instances to the
 * [ScreenBasedAlgorithm] contract by delegating item lifecycle, clustering, and distance settings.
 */
class ScreenBasedAlgorithmAdapterTest {

    private class TestItem(lat: Double, lng: Double) : ClusterItem {
        override val position: LatLng = LatLng(lat, lng)
        override val title: String? = null
        override val snippet: String? = null
        override val zIndex: Float? = null
    }

    private lateinit var baseAlgorithm: NonHierarchicalDistanceBasedAlgorithm<TestItem>
    private lateinit var adapter: ScreenBasedAlgorithmAdapter<TestItem>

    @BeforeTest
    fun setUp() {
        baseAlgorithm = NonHierarchicalDistanceBasedAlgorithm()
        adapter = ScreenBasedAlgorithmAdapter(baseAlgorithm)
    }

    @Test
    fun testDelegationLifecycle() {
        val item1 = TestItem(10.0, 10.0)
        val item2 = TestItem(20.0, 20.0)

        // Add
        assertTrue(adapter.addItem(item1))
        assertTrue(adapter.addItems(listOf(item2)))
        assertEquals(2, adapter.items.size)

        // Update
        assertTrue(adapter.updateItem(item1))

        // Get clusters
        val clusters = adapter.getClusters(10f)
        assertEquals(2, clusters.size)

        // Max distance
        adapter.maxDistanceBetweenClusteredItems = 50
        assertEquals(50, adapter.maxDistanceBetweenClusteredItems)

        // Remove
        assertTrue(adapter.removeItem(item1))
        assertEquals(1, adapter.items.size)
        assertTrue(adapter.removeItems(listOf(item2)))
        assertTrue(adapter.items.isEmpty())

        // Clear
        adapter.addItem(item1)
        adapter.clearItems()
        assertTrue(adapter.items.isEmpty())

        // Camera change stub
        adapter.onCameraChange(CameraPosition(LatLng(0.0, 0.0), 5f, 0f, 0f))
        assertFalse(adapter.shouldReclusterOnMapMovement())
    }
}
