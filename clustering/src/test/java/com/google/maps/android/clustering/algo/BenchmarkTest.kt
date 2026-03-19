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
import com.google.maps.android.clustering.Cluster
import com.google.maps.android.clustering.ClusterItem
import org.junit.Test
import java.util.Random

class BenchmarkTest {

    private class MyItem(lat: Double, lng: Double) : ClusterItem {
        override val position: LatLng = LatLng(lat, lng)
        override val title: String? = null
        override val snippet: String? = null
        override val zIndex: Float? = null
    }

    private fun generateItems(count: Int): List<MyItem> {
        val random = Random(12345) // Seed for consistency
        return List(count) {
            val lat = (random.nextDouble() - 0.5) * 170 // -85 to 85
            val lng = (random.nextDouble() - 0.5) * 360 // -180 to 180
            MyItem(lat, lng)
        }
    }

    @Test
    fun benchmarkNonHierarchicalDistanceBasedAlgorithm() {
        runBenchmark(NonHierarchicalDistanceBasedAlgorithm(), "NonHierarchicalDistanceBasedAlgorithm")
    }

    @Test
    fun benchmarkGridBasedAlgorithm() {
        runBenchmark(GridBasedAlgorithm(), "GridBasedAlgorithm")
    }

    private fun runBenchmark(algorithm: Algorithm<MyItem>, name: String) {
        println("--- Benchmarking $name ---")
        val count = 50000
        val items = generateItems(count)

        // Warmup
        algorithm.addItems(items.take(1000))
        algorithm.getClusters(10f)
        algorithm.clearItems()

        System.gc()

        // 1. Benchmark Adding Items
        val startAdd = System.nanoTime()
        algorithm.addItems(items)
        val endAdd = System.nanoTime()
        System.out.printf("addItems(%,d) took %.2f ms%n", count, (endAdd - startAdd) / 1000000.0)

        // 2. Benchmark getClusters at various zoom levels
        val zoomLevels = floatArrayOf(4f, 8f, 12f, 16f)
        for (zoom in zoomLevels) {
            System.gc()
            val startCluster = System.nanoTime()
            val clusters = algorithm.getClusters(zoom)
            val endCluster = System.nanoTime()
            System.out.printf("getClusters(zoom=%.1f) created %,d clusters in %.2f ms%n",
                zoom, clusters.size, (endCluster - startCluster) / 1000000.0)
        }
    }
}
