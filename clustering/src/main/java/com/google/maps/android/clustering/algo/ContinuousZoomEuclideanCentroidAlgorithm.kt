/*
 * Copyright 2025 Google LLC
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
import kotlin.math.pow

/**
 * A variant of [CentroidNonHierarchicalDistanceBasedAlgorithm] that uses
 * continuous zoom scaling and Euclidean distance for clustering.
 *
 * This class overrides [getClusters] to compute
 * clusters with a zoom-dependent radius, while keeping the centroid-based cluster positions.
 *
 * @param T the type of cluster item
 */
public class ContinuousZoomEuclideanCentroidAlgorithm<T : ClusterItem> :
    CentroidNonHierarchicalDistanceBasedAlgorithm<T>() {

    override fun getClusters(zoom: Float): Set<Cluster<T>> {
        // Continuous zoom — no casting to int
        val zoomSpecificSpan = maxDistanceBetweenClusteredItems / 2.0.pow(zoom.toDouble()) / 256

        val visitedCandidates = hashSetOf<QuadItem<T>>()
        val results = hashSetOf<Cluster<T>>()
        val distanceToCluster = hashMapOf<QuadItem<T>, Double>()
        val itemToCluster = hashMapOf<QuadItem<T>, StaticCluster<T>>()

        synchronized(quadTree) {
            for (candidate in getClusteringItems(quadTree, zoom)) {
                if (visitedCandidates.contains(candidate)) {
                    // Candidate is already part of another cluster.
                    continue
                }

                val searchBounds = createBoundsFromSpan(candidate.point, zoomSpecificSpan)
                val clusterItems = quadTree.search(searchBounds)
                    .filter {
                        val distance = distanceSquared(it.point, candidate.point)
                        val radiusSquared = (zoomSpecificSpan / 2).pow(2)
                        distance < radiusSquared
                    }

                if (clusterItems.size == 1) {
                    // Only the current marker is in range. Just add the single item to the results.
                    results.add(candidate)
                    visitedCandidates.add(candidate)
                    distanceToCluster[candidate] = 0.0
                    continue
                }
                val cluster = StaticCluster<T>(candidate.clusterItem.position)
                results.add(cluster)

                for (clusterItem in clusterItems) {
                    val existingDistance = distanceToCluster[clusterItem]
                    val distance = distanceSquared(clusterItem.point, candidate.point)
                    if (existingDistance != null) {
                        // Item already belongs to another cluster. Check if it's closer to this cluster.
                        if (existingDistance < distance) {
                            continue
                        }
                        // Move item to the closer cluster.
                        itemToCluster[clusterItem]?.remove(clusterItem.clusterItem)
                    }
                    distanceToCluster[clusterItem] = distance
                    cluster.add(clusterItem.clusterItem)
                    itemToCluster[clusterItem] = cluster
                }
                visitedCandidates.addAll(clusterItems)
            }
        }

        // Now, apply the centroid logic from CentroidNonHierarchicalDistanceBasedAlgorithm
        return results.map { cluster ->
            val centroid = computeCentroid(cluster.items)
            val newCluster = StaticCluster<T>(centroid)
            cluster.items.forEach { newCluster.add(it) }
            newCluster
        }.toSet()
    }
}
