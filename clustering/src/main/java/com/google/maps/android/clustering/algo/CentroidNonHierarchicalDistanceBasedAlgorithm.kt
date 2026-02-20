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

import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.clustering.Cluster
import com.google.maps.android.clustering.ClusterItem
import java.util.HashSet

/**
 * A variant of [NonHierarchicalDistanceBasedAlgorithm] that clusters items
 * based on distance but assigns cluster positions at the centroid of their items,
 * instead of using the position of a single item as the cluster position.
 *
 * This algorithm overrides [.getClusters] to compute a geographic centroid
 * for each cluster and creates [StaticCluster] instances positioned at these centroids.
 * This can provide a more accurate visual representation of the cluster location.
 *
 * @param <T> the type of cluster item
</T> */
open class CentroidNonHierarchicalDistanceBasedAlgorithm<T : ClusterItem> : NonHierarchicalDistanceBasedAlgorithm<T>() {

    /**
     * Computes the centroid (average latitude and longitude) of a collection of cluster items.
     *
     * @param items the collection of cluster items to compute the centroid for
     * @return the centroid [LatLng] of the items
     */
    protected fun computeCentroid(items: Collection<T>): LatLng {
        var latSum = 0.0
        var lngSum = 0.0
        var count = 0
        for (item in items) {
            latSum += item.position.latitude
            lngSum += item.position.longitude
            count++
        }
        return LatLng(latSum / count, lngSum / count)
    }

    /**
     * Returns clusters of items for the given zoom level, with cluster positions
     * set to the centroid of their constituent items rather than the position of
     * any single item.
     *
     * @param zoom the current zoom level
     * @return a set of clusters with centroid positions
     */
    override fun getClusters(zoom: Float): Set<Cluster<T>> {
        val originalClusters = super.getClusters(zoom)
        val newClusters = HashSet<Cluster<T>>()

        for (cluster in originalClusters) {
            val centroid = computeCentroid(cluster.items)
            val newCluster = StaticCluster<T>(centroid)
            for (item in cluster.items) {
                newCluster.add(item)
            }
            newClusters.add(newCluster)
        }
        return newClusters
    }
}
