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

import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.clustering.ClusterItem
import java.util.Random
import org.junit.Test

/**
 * Comparative performance benchmark evaluating [SuperClusterAlgorithm] against traditional
 * clustering algorithms: [NonHierarchicalDistanceBasedAlgorithm], [NonHierarchicalViewBasedAlgorithm],
 * and [GridBasedAlgorithm].
 */
class ClusteringPerformanceComparisonTest {

    private class BenchItem(
        lat: Double,
        lng: Double,
        val id: Int,
    ) : ClusterItem {
        override val position: LatLng = LatLng(lat, lng)
        override val title: String? = null
        override val snippet: String? = null
        override val zIndex: Float? = null
    }

    private fun generateUniformItems(count: Int): List<BenchItem> {
        val random = Random(42)
        return List(count) { i ->
            val lat = (random.nextDouble() - 0.5) * 140.0 // -70 to 70
            val lng = (random.nextDouble() - 0.5) * 360.0 // -180 to 180
            BenchItem(lat, lng, i)
        }
    }

    data class BenchmarkResult(
        val algorithmName: String,
        val itemCount: Int,
        val addItemsMs: Double,
        val indexBuildMs: Double,
        val queryZoom4Ms: Double,
        val queryZoom8Ms: Double,
        val queryZoom12Ms: Double,
        val queryZoom16Ms: Double,
        val totalQueryMs: Double,
        val memoryUsedMb: Double,
    )

    // [START maps_android_utils_benchmark_comparison_runner]
    private fun benchmarkAlgorithm(
        name: String,
        algorithm: Algorithm<BenchItem>,
        items: List<BenchItem>,
        viewWidth: Int = 1080,
        viewHeight: Int = 1920,
    ): BenchmarkResult {
        // Setup screen bounds for ScreenBasedAlgorithms
        if (algorithm is ScreenBasedAlgorithm<BenchItem>) {
            algorithm.onCameraChange(
                CameraPosition.Builder()
                    .target(LatLng(0.0, 0.0))
                    .zoom(8f)
                    .build(),
            )
        }

        System.gc()
        Thread.sleep(50)
        val runtime = Runtime.getRuntime()
        val memBefore = (runtime.totalMemory() - runtime.freeMemory()) / (1024.0 * 1024.0)

        // 1. Measure Ingestion
        val startAdd = System.nanoTime()
        algorithm.addItems(items)
        val addMs = (System.nanoTime() - startAdd) / 1_000_000.0

        // 2. Measure Build / Index (if SuperClusterAlgorithm)
        val startBuild = System.nanoTime()
        if (algorithm is SuperClusterAlgorithm<BenchItem>) {
            algorithm.buildIndexIfNeeded()
        }
        val buildMs = (System.nanoTime() - startBuild) / 1_000_000.0

        val memAfter = (runtime.totalMemory() - runtime.freeMemory()) / (1024.0 * 1024.0)
        val memUsed = (memAfter - memBefore).coerceAtLeast(0.0)

        // 3. Measure Queries across zoom levels (averaged over 3 runs)
        fun measureZoom(zoom: Float): Double {
            var total = 0.0
            val runs = 3
            for (r in 0 until runs) {
                val start = System.nanoTime()
                val clusters = algorithm.getClusters(zoom)
                val duration = (System.nanoTime() - start) / 1_000_000.0
                total += duration
                // Record or consume to prevent JIT dead code elimination
                if (clusters.size < 0) println("Unreachable")
            }
            return total / runs
        }

        val q4 = measureZoom(4f)
        val q8 = measureZoom(8f)
        val q12 = measureZoom(12f)
        val q16 = measureZoom(16f)
        val totalQuery = q4 + q8 + q12 + q16

        return BenchmarkResult(
            algorithmName = name,
            itemCount = items.size,
            addItemsMs = addMs,
            indexBuildMs = buildMs,
            queryZoom4Ms = q4,
            queryZoom8Ms = q8,
            queryZoom12Ms = q12,
            queryZoom16Ms = q16,
            totalQueryMs = totalQuery,
            memoryUsedMb = memUsed,
        )
    }
    // [END maps_android_utils_benchmark_comparison_runner]

    @Test
    fun runComprehensiveComparisonBenchmark() {
        println("\n=========================================================================================")
        println("         CLUSTERING BENCHMARK: SuperClusterAlgorithm vs Traditional Algorithms           ")
        println("=========================================================================================")

        val scales = listOf(10_000, 50_000, 100_000)

        for (count in scales) {
            println("\n--- DATASET SIZE: %,d points ---".format(count))
            val items = generateUniformItems(count)

            val results = ArrayList<BenchmarkResult>()

            // 1. SuperClusterAlgorithm (Unbounded full-world)
            val scUnbounded = SuperClusterAlgorithm<BenchItem>()
            results.add(benchmarkAlgorithm("SuperCluster (Unbounded)", scUnbounded, items))

            // 2. SuperClusterAlgorithm (Viewport culling 1080x1920)
            val scViewport = SuperClusterAlgorithm<BenchItem>(viewWidth = 1080, viewHeight = 1920)
            results.add(benchmarkAlgorithm("SuperCluster (Viewport)", scViewport, items))

            // 3. NonHierarchicalDistanceBasedAlgorithm (Default)
            val distanceBased = NonHierarchicalDistanceBasedAlgorithm<BenchItem>()
            results.add(benchmarkAlgorithm("NonHierarchicalDistance", distanceBased, items))

            // 4. NonHierarchicalViewBasedAlgorithm (20k demo algo)
            val viewBased = NonHierarchicalViewBasedAlgorithm<BenchItem>(1080, 1920)
            results.add(benchmarkAlgorithm("NonHierarchicalView", viewBased, items))

            // 5. GridBasedAlgorithm
            val gridBased = GridBasedAlgorithm<BenchItem>()
            results.add(benchmarkAlgorithm("GridBasedAlgorithm", gridBased, items))

            // Print Markdown Table for this scale
            println("| Algorithm | addItems | Index Build | Zoom 4.0 | Zoom 8.0 | Zoom 12.0 | Zoom 16.0 | Total Query | Memory |")
            println("| :--- | :---: | :---: | :---: | :---: | :---: | :---: | :---: | :---: |")
            for (res in results) {
                println(
                    "| %-24s | %8.2f ms | %8.2f ms | %8.2f ms | %8.2f ms | %8.2f ms | %8.2f ms | %8.2f ms | %6.1f MB |".format(
                        res.algorithmName,
                        res.addItemsMs,
                        res.indexBuildMs,
                        res.queryZoom4Ms,
                        res.queryZoom8Ms,
                        res.queryZoom12Ms,
                        res.queryZoom16Ms,
                        res.totalQueryMs,
                        res.memoryUsedMb,
                    ),
                )
            }
        }
        println("\n=========================================================================================\n")
    }
}
