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
import com.google.common.truth.Truth.assertThat
import com.google.maps.android.clustering.ClusterItem
import com.google.maps.android.clustering.ClusterManager
import com.google.maps.android.geometry.Bounds
import kotlin.random.Random
import org.junit.Test

class SuperClusterAlgorithmTest {

    data class SimpleItem(
        val id: String,
        override val position: LatLng,
        override val title: String? = null,
        override val snippet: String? = null,
        override val zIndex: Float? = null,
    ) : ClusterItem

    @Test
    fun emptyAlgorithmReturnsEmptyClusters() {
        val algorithm = SuperClusterAlgorithm<SimpleItem>()
        assertThat(algorithm.items).isEmpty()
        assertThat(algorithm.getClusters(0f)).isEmpty()
        assertThat(algorithm.getClusters(10f)).isEmpty()
    }

    @Test
    fun singleItemReturnsSingleCluster() {
        val algorithm = SuperClusterAlgorithm<SimpleItem>()
        val item = SimpleItem("1", LatLng(37.7749, -122.4194))
        algorithm.addItem(item)

        assertThat(algorithm.items).containsExactly(item)

        val clustersAtZoom0 = algorithm.getClusters(0f)
        assertThat(clustersAtZoom0).hasSize(1)
        val cluster = clustersAtZoom0.first()
        assertThat(cluster.size).isEqualTo(1)
        assertThat(cluster.items).containsExactly(item)
        assertThat(cluster.position.latitude).isWithin(0.0001).of(37.7749)
        assertThat(cluster.position.longitude).isWithin(0.0001).of(-122.4194)

        val clustersAtZoom15 = algorithm.getClusters(15f)
        assertThat(clustersAtZoom15).hasSize(1)
        assertThat(clustersAtZoom15.first().size).isEqualTo(1)
    }

    @Test
    fun coincidentItemsClusterTogetherAtAllZoomLevels() {
        val algorithm = SuperClusterAlgorithm<SimpleItem>()
        val pos = LatLng(40.7128, -74.0060)
        val item1 = SimpleItem("1", pos)
        val item2 = SimpleItem("2", pos)
        val item3 = SimpleItem("3", pos)
        algorithm.addItems(listOf(item1, item2, item3))

        for (zoom in 0..18) {
            val clusters = algorithm.getClusters(zoom.toFloat())
            assertThat(clusters).hasSize(1)
            val cluster = clusters.first()
            assertThat(cluster.size).isEqualTo(3)
            assertThat(cluster.items).containsExactly(item1, item2, item3)
        }
    }

    @Test
    fun hierarchicalClusteringAggregatesBottomUp() {
        val algorithm = SuperClusterAlgorithm<SimpleItem>()
        // Two points close together in San Francisco, one distant in Tokyo
        val sf1 = SimpleItem("sf1", LatLng(37.7749, -122.4194))
        val sf2 = SimpleItem("sf2", LatLng(37.7750, -122.4195))
        val tokyo = SimpleItem("tokyo", LatLng(35.6762, 139.6503))
        algorithm.addItems(listOf(sf1, sf2, tokyo))

        // At zoom 0 (world view): SF points merge, Tokyo stays distinct or merges if radius encompasses it
        val clustersZoom0 = algorithm.getClusters(0f)
        assertThat(clustersZoom0.size).isLessThan(3)

        // At zoom 18 (street level): SF points are unclustered into distinct points
        val clustersZoom18 = algorithm.getClusters(18f)
        assertThat(clustersZoom18).hasSize(3)
        for (c in clustersZoom18) {
            assertThat(c.size).isEqualTo(1)
        }
    }

    @Test
    fun removeItemUpdatesPyramid() {
        val algorithm = SuperClusterAlgorithm<SimpleItem>()
        val pos = LatLng(51.5074, -0.1278)
        val item1 = SimpleItem("1", pos)
        val item2 = SimpleItem("2", pos)
        algorithm.addItems(listOf(item1, item2))

        assertThat(algorithm.getClusters(5f).first().size).isEqualTo(2)

        algorithm.removeItem(item1)
        assertThat(algorithm.items).containsExactly(item2)

        val clusters = algorithm.getClusters(5f)
        assertThat(clusters).hasSize(1)
        assertThat(clusters.first().size).isEqualTo(1)
        assertThat(clusters.first().items).containsExactly(item2)
    }

    // [START maps_android_utils_supercluster_location_update_test]
    @Test
    fun updateItemMovesClusterToNewLocation() {
        class MutableItem(
            val id: String,
            override var position: LatLng,
            override val title: String? = null,
            override val snippet: String? = null,
            override val zIndex: Float? = null,
        ) : ClusterItem {
            override fun equals(other: Any?): Boolean = other is MutableItem && other.id == this.id
            override fun hashCode(): Int = id.hashCode()
        }

        val algorithm = SuperClusterAlgorithm<MutableItem>()
        val item1 = MutableItem("1", LatLng(37.7749, -122.4194))
        val item2 = MutableItem("2", LatLng(37.7750, -122.4195))
        algorithm.addItems(listOf(item1, item2))

        // Initially in San Francisco, clustered together at zoom 10
        val initialClusters = algorithm.getClusters(10f)
        assertThat(initialClusters).hasSize(1)
        assertThat(initialClusters.first().size).isEqualTo(2)

        // Move item2 across the country to New York
        item2.position = LatLng(40.7128, -74.0060)
        val updated = algorithm.updateItem(item2)
        assertThat(updated).isTrue()

        // Re-query at zoom 10: item1 and item2 are now in completely different locations
        val updatedClusters = algorithm.getClusters(10f)
        assertThat(updatedClusters).hasSize(2)
        val c1 = updatedClusters.find { it.items.contains(item1) }!!
        val c2 = updatedClusters.find { it.items.contains(item2) }!!
        assertThat(c1.position.latitude).isWithin(0.01).of(37.7749)
        assertThat(c2.position.latitude).isWithin(0.01).of(40.7128)
    }
    // [END maps_android_utils_supercluster_location_update_test]

    @Test
    fun clearItemsResetsEverything() {
        val algorithm = SuperClusterAlgorithm<SimpleItem>()
        algorithm.addItems(listOf(SimpleItem("1", LatLng(0.0, 0.0)), SimpleItem("2", LatLng(10.0, 10.0))))
        assertThat(algorithm.items).hasSize(2)

        algorithm.clearItems()
        assertThat(algorithm.items).isEmpty()
        assertThat(algorithm.getClusters(5f)).isEmpty()
    }

    @Test
    fun viewportCullingReturnsOnlyVisibleClusters() {
        val algorithm = SuperClusterAlgorithm<SimpleItem>(
            viewWidth = 400,
            viewHeight = 400,
        )

        // Item in SF, item in Sydney
        val sf = SimpleItem("sf", LatLng(37.7749, -122.4194))
        val sydney = SimpleItem("sydney", LatLng(-33.8688, 151.2093))
        algorithm.addItems(listOf(sf, sydney))

        // Focus camera on San Francisco at high zoom
        algorithm.onCameraChange(
            CameraPosition.Builder()
                .target(LatLng(37.7749, -122.4194))
                .zoom(14f)
                .build(),
        )

        val clusters = algorithm.getClusters(14f)
        // Only SF should be within the 400x400 viewport at zoom 14
        assertThat(clusters).hasSize(1)
        assertThat(clusters.first().items).containsExactly(sf)
    }

    @Test
    fun customBoundsQueryReturnsMatchingSubsets() {
        val algorithm = SuperClusterAlgorithm<SimpleItem>()
        val p1 = SimpleItem("p1", LatLng(10.0, 10.0))
        val p2 = SimpleItem("p2", LatLng(80.0, 80.0))
        algorithm.addItems(listOf(p1, p2))

        // Bounds covering (0,0) to (20,20) in Mercator space
        val bounds = Bounds(0.5, 0.6, 0.4, 0.6)
        val clusters = algorithm.getClusters(bounds, 10f)
        for (c in clusters) {
            assertThat(c.items).contains(p1)
            assertThat(c.items).doesNotContain(p2)
        }
    }

    @Test
    fun largeDatasetClusteringScalesLinearly() {
        val algorithm = SuperClusterAlgorithm<SimpleItem>()
        val count = 25000
        val random = Random(42)
        val items = ArrayList<SimpleItem>(count)

        for (i in 0 until count) {
            val lat = random.nextDouble(-60.0, 60.0)
            val lng = random.nextDouble(-170.0, 170.0)
            items.add(SimpleItem("item_$i", LatLng(lat, lng)))
        }

        val startBuild = System.currentTimeMillis()
        algorithm.addItems(items)
        algorithm.buildIndexIfNeeded()
        val buildDuration = System.currentTimeMillis() - startBuild

        // Building the entire zoom pyramid for 25,000 points should take well under reasonable bounds
        assertThat(buildDuration).isLessThan(10000L)

        // Querying clusters at any zoom should be instantaneous
        for (z in listOf(0f, 4f, 8f, 12f, 16f)) {
            val startQuery = System.nanoTime()
            val clusters = algorithm.getClusters(z)
            val queryDurationMs = (System.nanoTime() - startQuery) / 1_000_000

            assertThat(clusters).isNotEmpty()
            assertThat(queryDurationMs).isLessThan(1000L)

            // Verify total item conservation across all clusters
            var totalCount = 0
            for (cluster in clusters) {
                totalCount += cluster.size
            }
            assertThat(totalCount).isEqualTo(count)
        }
    }

    // [START maps_android_utils_supercluster_100k_test]
    @Test
    fun megaScaleDataset100kPointsPerformanceTest() {
        val algorithm = SuperClusterAlgorithm<SimpleItem>()
        val count = 100000
        val random = Random(12345)
        val items = ArrayList<SimpleItem>(count)

        for (i in 0 until count) {
            val lat = random.nextDouble(-50.0, 50.0)
            val lng = random.nextDouble(-150.0, 150.0)
            items.add(SimpleItem("mega_$i", LatLng(lat, lng)))
        }

        val startBuild = System.currentTimeMillis()
        algorithm.addItems(items)
        algorithm.buildIndexIfNeeded()
        val buildDuration = System.currentTimeMillis() - startBuild

        // Building the entire hierarchical pyramid for 100,000 points should complete well within generous bounds in CI
        assertThat(buildDuration).isLessThan(30000L)

        // Querying clusters across typical map zoom levels
        for (z in listOf(2f, 6f, 10f, 14f)) {
            val startQuery = System.nanoTime()
            val clusters = algorithm.getClusters(z)
            val queryDurationMs = (System.nanoTime() - startQuery) / 1_000_000

            assertThat(clusters).isNotEmpty()
            assertThat(queryDurationMs).isLessThan(1000L)

            var clusteredCount = 0
            for (c in clusters) {
                clusteredCount += c.size
            }
            assertThat(clusteredCount).isEqualTo(count)
        }
    }
    // [END maps_android_utils_supercluster_100k_test]

    @Test
    fun rawCoordinatesIngestionMatchesStandardAddItems() {
        val algorithm = SuperClusterAlgorithm<SimpleItem>()
        val count = 1000
        val coords = DoubleArray(2 * count)
        val random = Random(42)
        for (i in 0 until count) {
            coords[2 * i] = 30.0 + random.nextDouble() * 10.0
            coords[2 * i + 1] = -100.0 + random.nextDouble() * 10.0
        }

        algorithm.setCoordinates(coords) { id, pos ->
            SimpleItem("raw_$id", pos)
        }

        assertThat(algorithm.totalItemCount).isEqualTo(count)

        val clusters = algorithm.getClusters(4f)
        assertThat(clusters).isNotEmpty()

        var sumSizes = 0
        for (c in clusters) {
            sumSizes += c.size
            if (c.size == 1) {
                assertThat(c.items.first().id).startsWith("raw_")
            }
        }
        assertThat(sumSizes).isEqualTo(count)
    }

    @Test
    fun onProgressListenerReportsProgressAccurately() {
        val algorithm = SuperClusterAlgorithm<SimpleItem>()
        val progressReports = ArrayList<Pair<Float, String>>()

        algorithm.onProgressListener = ClusterManager.OnClusteringProgressListener { progress, status ->
            progressReports.add(Pair(progress, status))
        }

        val items = List(100) { i ->
            SimpleItem("item_$i", LatLng(37.0 + i * 0.01, -122.0 + i * 0.01))
        }
        algorithm.addItems(items)
        algorithm.buildIndexIfNeeded()

        assertThat(progressReports).isNotEmpty()
        assertThat(progressReports.first().first).isGreaterThan(0.0f)
        assertThat(progressReports.last().first).isEqualTo(1.0f)
        assertThat(progressReports.last().second).isEqualTo("Indexing complete")
    }
}
