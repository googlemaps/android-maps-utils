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
import org.junit.Assert.assertTrue
import org.junit.Test

class ContinuousZoomEuclideanCentroidAlgorithmTest {
    class TestClusterItem(lat: Double, lng: Double) : ClusterItem {
        override val position: LatLng = LatLng(lat, lng)

        override val title: String? = null

        override val snippet: String? = null

        override val zIndex: Float = 0f
    }

    @Test
    fun testContinuousZoomMergesClosePairAtLowZoomAndSeparatesAtHighZoom() {
        val algo = ContinuousZoomEuclideanCentroidAlgorithm<TestClusterItem>()

        val items =
            listOf(
                TestClusterItem(10.0, 10.0),
                // very close to the first
                TestClusterItem(10.0001, 10.0001),
                // far away
                TestClusterItem(20.0, 20.0),
            )

        algo.addItems(items)

        // At a high zoom, the close pair should be separate (small radius)
        val highZoom = algo.getClusters(20.0f)
        assertEquals(3, highZoom.size)

        // At a lower zoom, the close pair should merge (larger radius)
        val lowZoom = algo.getClusters(5.0f)
        assertTrue(lowZoom.size < 3)

        // Specifically, we expect one cluster of size 2 and one singleton
        assertTrue(lowZoom.any { it.items.size == 2 })
        assertTrue(lowZoom.any { it.items.size == 1 })
    }

    @Test
    fun testClusterPositionsAreCentroids() {
        val algo = ContinuousZoomEuclideanCentroidAlgorithm<TestClusterItem>()

        val items =
            listOf(
                TestClusterItem(0.0, 0.0),
                TestClusterItem(0.0, 2.0),
                TestClusterItem(2.0, 0.0),
            )

        algo.addItems(items)

        val clusters = algo.getClusters(1.0f)

        // Expect all items clustered into one
        assertEquals(1, clusters.size)

        val cluster = clusters.iterator().next()

        // The centroid should be approximately (0.6667, 0.6667)
        val centroid = cluster.position
        assertEquals(0.6667, centroid.latitude, 0.0001)
        assertEquals(0.6667, centroid.longitude, 0.0001)
    }
}
