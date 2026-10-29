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

import com.google.maps.android.clustering.Cluster
import com.google.maps.android.clustering.ClusterItem
import com.google.maps.android.model.LatLng
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Clustering behaviour of [NonHierarchicalDistanceBasedAlgorithm], shared by every platform.
 * The JVM-only locking contract of the same class is covered by
 * NonHierarchicalDistanceBasedAlgorithmTest in the Android host tests.
 */
class DistanceBasedClusteringTest {
    private class TestItem(lat: Double, lng: Double) : ClusterItem {
        override val position: LatLng = LatLng(lat, lng)
        override val title: String? = null
        override val snippet: String? = null
        override val zIndex: Float? = null
    }

    // Half a degree apart: together at zoom 4, apart at zoom 12.
    private val near1 = TestItem(0.0, 0.0)
    private val near2 = TestItem(0.0, 0.5)

    // A quarter of the world away from the others: never in the same cluster.
    private val far = TestItem(0.0, 90.0)

    @Test
    fun testCloseItemsClusterAtLowZoom() {
        val algo = NonHierarchicalDistanceBasedAlgorithm<TestItem>()
        algo.addItems(listOf(near1, near2, far))

        val clusters = algo.getClusters(4f)

        assertEquals(2, clusters.size)
        assertEquals(setOf(near1, near2), clusterContaining(clusters, near1).items.toSet())
        assertEquals(setOf(far), clusterContaining(clusters, far).items.toSet())
        assertEachItemInExactlyOneCluster(clusters, listOf(near1, near2, far))
    }

    @Test
    fun testCloseItemsSeparateAtHighZoom() {
        val algo = NonHierarchicalDistanceBasedAlgorithm<TestItem>()
        algo.addItems(listOf(near1, near2, far))

        val clusters = algo.getClusters(12f)

        assertEquals(3, clusters.size)
        assertTrue(clusters.all { it.size == 1 })
        assertEachItemInExactlyOneCluster(clusters, listOf(near1, near2, far))
    }

    @Test
    fun testSingleItemClusterKeepsItemPosition() {
        val algo = NonHierarchicalDistanceBasedAlgorithm<TestItem>()
        algo.addItem(far)

        val cluster = algo.getClusters(4f).single()

        assertEquals(far.position, cluster.position)
        assertEquals(listOf(far), cluster.items.toList())
    }

    @Test
    fun testItemsAtSamePositionClusterAtMaxZoom() {
        val algo = NonHierarchicalDistanceBasedAlgorithm<TestItem>()
        val first = TestItem(10.0, 10.0)
        val second = TestItem(10.0, 10.0)
        algo.addItems(listOf(first, second))

        val cluster = algo.getClusters(21f).single()

        assertEquals(setOf(first, second), cluster.items.toSet())
    }

    @Test
    fun testMaxDistanceControlsClusterRadius() {
        val algo = NonHierarchicalDistanceBasedAlgorithm<TestItem>()
        algo.addItems(listOf(near1, near2, far))

        algo.maxDistanceBetweenClusteredItems = 0
        assertEquals(3, algo.getClusters(4f).size)

        algo.maxDistanceBetweenClusteredItems = 100
        assertEquals(2, algo.getClusters(4f).size)
    }

    @Test
    fun testClustersReflectRemoveAndClear() {
        val algo = NonHierarchicalDistanceBasedAlgorithm<TestItem>()
        algo.addItems(listOf(near1, near2, far))

        algo.removeItem(near2)
        val clusters = algo.getClusters(4f)
        assertEquals(2, clusters.size)
        assertTrue(clusters.all { it.size == 1 })

        algo.clearItems()
        assertTrue(algo.getClusters(4f).isEmpty())
    }

    @Test
    fun testCentroidAlgorithmPlacesClusterAtCentroid() {
        val algo = CentroidNonHierarchicalDistanceBasedAlgorithm<TestItem>()
        algo.addItems(listOf(near1, near2, far))

        val cluster = clusterContaining(algo.getClusters(4f), near1)

        assertEquals(2, cluster.size)
        assertEquals(LatLng(0.0, 0.25), cluster.position)
    }

    private fun clusterContaining(clusters: Set<Cluster<TestItem>>, item: TestItem): Cluster<TestItem> =
        clusters.single { item in it.items }

    private fun assertEachItemInExactlyOneCluster(clusters: Set<Cluster<TestItem>>, items: List<TestItem>) {
        val clustered = clusters.flatMap { it.items }
        assertEquals(items.size, clustered.size)
        assertEquals(items.toSet(), clustered.toSet())
    }
}
