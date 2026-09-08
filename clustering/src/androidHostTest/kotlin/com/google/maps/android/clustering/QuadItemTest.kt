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
package com.google.maps.android.clustering

import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.clustering.algo.NonHierarchicalDistanceBasedAlgorithm
import com.google.maps.android.geometry.Bounds
import com.google.maps.android.projection.SphericalMercatorProjection
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class QuadItemTest {
    @Test
    fun testAddRemoveUpdateClear() {
        val item15: ClusterItem = TestingItem("title1", 0.1, 0.5)
        val item23 = TestingItem("title2", 0.2, 0.3)

        val algo = NonHierarchicalDistanceBasedAlgorithm<ClusterItem>()
        assertTrue(algo.addItem(item15))
        assertTrue(algo.addItem(item23))

        assertEquals(2, algo.items.size)

        assertTrue(algo.removeItem(item15))

        assertEquals(1, algo.items.size)

        assertFalse(algo.items.contains(item15))
        assertTrue(algo.items.contains(item23))

        // Update the item still in the algorithm
        item23.title = "newTitle"
        assertTrue(algo.updateItem(item23))

        // Try to remove the item that was already removed
        assertFalse(algo.removeItem(item15))

        // Try to update the item that was already removed
        assertFalse(algo.updateItem(item15))

        algo.clearItems()
        assertEquals(0, algo.items.size)

        // Test bulk operations
        val items = listOf(item15, item23)
        assertTrue(algo.addItems(items))

        // Try to bulk add items that were already added
        assertFalse(algo.addItems(items))

        assertTrue(algo.removeItems(items))

        // Try to bulk remove items that were already removed
        assertFalse(algo.removeItems(items))
    }

    /**
     * Test if insertion order into the algorithm is the same as returned item order. This matters
     * because we want repeatable clustering behavior when updating model values and re-clustering.
     */
    @Test
    fun testInsertionOrder() {
        val algo = NonHierarchicalDistanceBasedAlgorithm<ClusterItem>()
        for (i in 0 until 100) {
            algo.addItem(TestingItem(i.toString(), 0.0, 0.0))
        }

        assertEquals(100, algo.items.size)

        var counter = 0
        for (item in algo.items) {
            assertEquals(counter.toString(), item.title)
            counter++
        }
    }

    @Test
    fun testUpdateItemAfterPositionChange() {
        val algo = NonHierarchicalDistanceBasedAlgorithm<TestingItem>()
        val item = TestingItem("title1", 0.0, 0.0)
        algo.addItem(item)
        assertEquals(1, algo.items.size)

        // Update the position of the mutable item
        item.setPosition(10.0, 10.0)

        // Call updateItem
        assertTrue("updateItem should return true after position change", algo.updateItem(item))
        assertEquals(1, algo.items.size)

        // Verify that the old QuadItem at (0, 0) was removed from the tree
        // and only the new position (10, 10) is indexed
        val clusters = algo.getClusters(4.0f)
        assertEquals(1, clusters.size)
        val cluster = clusters.iterator().next()
        assertEquals(10.0, cluster.position.latitude, 0.001)
        assertEquals(10.0, cluster.position.longitude, 0.001)
    }

    @Test
    fun testRemoveItemAfterPositionChange() {
        val algo = NonHierarchicalDistanceBasedAlgorithm<TestingItem>()
        val item = TestingItem("title1", 0.0, 0.0)
        algo.addItem(item)
        assertEquals(1, algo.items.size)

        // Update the position of the mutable item
        item.setPosition(10.0, 10.0)

        // Removing the item should succeed and remove it from the tree
        assertTrue("removeItem should return true after position change", algo.removeItem(item))
        assertEquals(0, algo.items.size)
        assertEquals(0, algo.getClusters(4.0f).size)
    }

    @Test
    fun testUpdateItemPreventsStaleQuadTreeEntries() {
        val algo = TestAlgorithm<TestingItem>()

        // Add 60 filler items to force PointQuadTree to split (MAX_ELEMENTS = 50)
        for (i in 0 until 60) {
            algo.addItem(TestingItem("filler$i", 10.0 + i * 0.001, 10.0 + i * 0.001))
        }

        // Add item1 in top-left quadrant
        val item1 = TestingItem("item1", 1.0, 1.0)
        algo.addItem(item1)

        assertEquals(
            "QuadTree should contain item1 at (1, 1)",
            1,
            algo.getQuadTreeItemCount(1.0, 1.0, 0.001),
        )

        // Move item1 far across quadrant boundary to (50.0, 50.0) and update
        item1.setPosition(50.0, 50.0)
        algo.updateItem(item1)

        // Without fix, old QuadItem remains at (1.0, 1.0) in mQuadTree because remove traversed the new coordinates
        assertEquals(
            "QuadTree should NOT contain stale entry at (1, 1) after update",
            0,
            algo.getQuadTreeItemCount(1.0, 1.0, 0.001),
        )
        assertEquals(
            "QuadTree should contain item1 at (50, 50)",
            1,
            algo.getQuadTreeItemCount(50.0, 50.0, 0.001),
        )
    }

    @Test
    fun testRemoveItemsAfterPositionChange() {
        val algo = NonHierarchicalDistanceBasedAlgorithm<TestingItem>()
        val item1 = TestingItem("title1", 0.0, 0.0)
        val item2 = TestingItem("title2", 1.0, 1.0)
        algo.addItems(listOf(item1, item2))
        assertEquals(2, algo.items.size)

        // Update the position of both items
        item1.setPosition(10.0, 10.0)
        item2.setPosition(20.0, 20.0)

        assertTrue(
            "removeItems should return true after position change",
            algo.removeItems(listOf(item1, item2)),
        )
        assertEquals(0, algo.items.size)
        assertEquals(0, algo.getClusters(4.0f).size)
    }

    @Test
    fun testClearItemsAfterPositionChange() {
        val algo = NonHierarchicalDistanceBasedAlgorithm<TestingItem>()
        val item1 = TestingItem("title1", 0.0, 0.0)
        algo.addItem(item1)
        item1.setPosition(10.0, 10.0)

        algo.clearItems()
        assertEquals(0, algo.items.size)
        assertEquals(0, algo.getClusters(4.0f).size)
    }

    private class TestAlgorithm<T : ClusterItem> : NonHierarchicalDistanceBasedAlgorithm<T>() {
        fun getQuadTreeItemCount(lat: Double, lng: Double, span: Double): Int {
            val p = PROJ.toPoint(LatLng(lat, lng))
            val bounds = Bounds(p.x - span, p.x + span, p.y - span, p.y + span)
            return mQuadTree.search(bounds).size
        }

        companion object {
            private val PROJ = SphericalMercatorProjection(1.0)
        }
    }

    private class TestingItem(
        override var title: String,
        lat: Double,
        lng: Double,
    ) : ClusterItem {
        override var position: LatLng = LatLng(lat, lng)
            private set

        fun setPosition(lat: Double, lng: Double) {
            position = LatLng(lat, lng)
        }

        override val snippet: String? = null

        override val zIndex: Float? = null
    }
}
