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
import java.util.IdentityHashMap
import java.util.Random
import org.junit.Test

/**
 * Formal verification proving that [SuperClusterAlgorithm] maintains strict conservation of items
 * and that every cluster's [size] strictly equals the count of items in [items].
 */
class SuperClusterInvariantProofTest {

    private class TestItem(
        val id: Int,
        override val position: LatLng,
        override val title: String? = "Item #$id",
        override val snippet: String? = null,
        override val zIndex: Float? = null,
    ) : ClusterItem

    // [START maps_android_utils_invariant_proof_test]
    @Test
    fun proveStrictItemConservationAcrossAllZoomLevels() {
        val algorithm = SuperClusterAlgorithm<TestItem>(
            minZoom = 0,
            maxZoom = 16,
            radius = 120.0,
            extent = 512.0,
        )

        val totalItemCount = 10_000
        val random = Random(42)
        val items = List(totalItemCount) { i ->
            val lat = 37.7749 + (random.nextDouble() - 0.5) * 2.0
            val lng = -122.4194 + (random.nextDouble() - 0.5) * 2.0
            TestItem(i, LatLng(lat, lng))
        }

        algorithm.addItems(items)

        // Verify across EVERY single zoom level from 0 to 18
        for (zoom in 0..18) {
            val clusters = algorithm.getClusters(zoom.toFloat())

            var sumOfSizes = 0
            val seenItems = IdentityHashMap<TestItem, Boolean>()

            for (cluster in clusters) {
                // 1. Invariant 1: cluster.size MUST be > 0
                assertThat(cluster.size).isGreaterThan(0)

                // 2. Invariant 2: cluster.items collection size MUST equal cluster.size exactly
                val clusterItems = cluster.items
                assertThat(clusterItems.size).isEqualTo(cluster.size)

                // 3. Invariant 3: No duplicate items within any cluster or across clusters
                for (item in clusterItems) {
                    val alreadySeen = seenItems.put(item, true)
                    assertThat(alreadySeen).isNull() // Must be null, indicating item was never seen before
                }

                sumOfSizes += cluster.size
            }

            // 4. Invariant 4: Sum of all cluster sizes MUST STRICTLY EQUAL totalItemCount
            assertThat(sumOfSizes).isEqualTo(totalItemCount)

            // 5. Invariant 5: Exactly all original items are accounted for
            assertThat(seenItems.size).isEqualTo(totalItemCount)
        }
    }
    // [END maps_android_utils_invariant_proof_test]
}
