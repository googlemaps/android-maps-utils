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
import com.google.maps.android.clustering.ClusterItem
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for [PreCachingAlgorithmDecorator].
 *
 * Proves that [PreCachingAlgorithmDecorator] correctly delegates clustering operations to the
 * underlying algorithm, invalidates its internal LRU cache whenever items or clustering properties
 * are modified, and returns cached cluster sets on subsequent reads for identical zoom levels.
 */
class PreCachingAlgorithmDecoratorTest {

    private class TestItem(lat: Double, lng: Double) : ClusterItem {
        override val position: LatLng = LatLng(lat, lng)
        override val title: String? = null
        override val snippet: String? = null
        override val zIndex: Float? = null
    }

    private lateinit var baseAlgorithm: NonHierarchicalDistanceBasedAlgorithm<TestItem>
    private lateinit var decorator: PreCachingAlgorithmDecorator<TestItem>

    @Before
    fun setUp() {
        baseAlgorithm = NonHierarchicalDistanceBasedAlgorithm()
        decorator = PreCachingAlgorithmDecorator(baseAlgorithm)
    }

    /**
     * Proves that [PreCachingAlgorithmDecorator.addItem] delegates item insertion to the underlying
     * algorithm and reflects the newly added item in [PreCachingAlgorithmDecorator.items].
     */
    @Test
    fun testAddItemAndItems() {
        val item = TestItem(10.0, 10.0)
        assertTrue("addItem should return true when adding a new item", decorator.addItem(item))
        assertEquals("Items collection size should be 1 after adding 1 item", 1, decorator.items.size)
        assertTrue("Items collection should contain the inserted item", decorator.items.contains(item))
    }

    /**
     * Proves that [PreCachingAlgorithmDecorator.addItems] adds multiple items to the underlying algorithm
     * and that [PreCachingAlgorithmDecorator.clearItems] flushes all items and invalidates the cache.
     */
    @Test
    fun testAddItemsAndClearItems() {
        val items = listOf(TestItem(10.0, 10.0), TestItem(20.0, 20.0))
        assertTrue("addItems should return true when adding multiple items", decorator.addItems(items))
        assertEquals("Items collection size should be 2", 2, decorator.items.size)

        decorator.clearItems()
        assertEquals("Items collection size should be 0 after clearItems()", 0, decorator.items.size)
    }

    /**
     * Proves that [PreCachingAlgorithmDecorator.removeItem] and [PreCachingAlgorithmDecorator.removeItems]
     * correctly remove single and batch items from the underlying algorithm and update the items collection.
     */
    @Test
    fun testRemoveItemAndRemoveItems() {
        val item1 = TestItem(10.0, 10.0)
        val item2 = TestItem(20.0, 20.0)
        decorator.addItems(listOf(item1, item2))

        assertTrue("removeItem should return true when removing an existing item", decorator.removeItem(item1))
        assertEquals("Items collection size should be 1 after removing 1 item", 1, decorator.items.size)

        assertTrue("removeItems should return true when removing remaining items", decorator.removeItems(listOf(item2)))
        assertEquals("Items collection size should be 0 after removing all items", 0, decorator.items.size)
    }

    /**
     * Proves that [PreCachingAlgorithmDecorator.updateItem] delegates item updates to the base algorithm
     * and invalidates the cache when an existing item is updated.
     */
    @Test
    fun testUpdateItem() {
        val item = TestItem(10.0, 10.0)
        decorator.addItem(item)
        assertTrue("updateItem should return true when updating an existing item", decorator.updateItem(item))
    }

    /**
     * Proves that setting [PreCachingAlgorithmDecorator.maxDistanceBetweenClusteredItems] updates
     * the property on the underlying algorithm and clears the cache to ensure future cluster
     * computations reflect the new distance threshold.
     */
    @Test
    fun testMaxDistanceBetweenClusteredItems() {
        decorator.maxDistanceBetweenClusteredItems = 100
        assertEquals("maxDistanceBetweenClusteredItems should reflect the newly assigned value", 100, decorator.maxDistanceBetweenClusteredItems)
    }

    /**
     * Proves that repeated calls to [PreCachingAlgorithmDecorator.getClusters] with the same zoom level
     * hit the thread-safe LRU cache and return identical cluster results without re-computing clusters.
     */
    @Test
    fun testGetClustersCaching() {
        val item1 = TestItem(10.0, 10.0)
        val item2 = TestItem(10.0001, 10.0001)
        decorator.addItems(listOf(item1, item2))

        val clustersFirstCall = decorator.getClusters(10.0f)
        assertFalse("Clusters should not be empty for nearby items at zoom level 10", clustersFirstCall.isEmpty())

        val clustersSecondCall = decorator.getClusters(10.0f)
        assertEquals("Second call to getClusters for identical zoom level should return cached cluster result", clustersFirstCall, clustersSecondCall)
    }
}
