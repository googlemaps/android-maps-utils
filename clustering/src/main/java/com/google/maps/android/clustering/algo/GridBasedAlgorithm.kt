/*
 * Copyright 2026Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.maps.android.clustering.algo

import androidx.collection.LongSparseArray
import com.google.maps.android.clustering.Cluster
import com.google.maps.android.clustering.ClusterItem
import com.google.maps.android.geometry.Point
import com.google.maps.android.projection.SphericalMercatorProjection
import java.util.Collections
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.pow

/**
 * Groups markers into a grid for clustering. This algorithm organizes items into a two-dimensional grid,
 * facilitating the formation of clusters based on proximity within each grid cell. The grid size determines
 * the spatial granularity of clustering, and clusters are created by aggregating items within the same grid cell.
 *
 * The effectiveness of clustering is influenced by the specified grid size, which determines the spatial resolution of the grid.
 * Smaller grid sizes result in more localized clusters, whereas larger grid sizes lead to broader clusters covering larger areas.
 *
 * @param <T> The type of {@link ClusterItem} to be clustered.
 */
class GridBasedAlgorithm<T : ClusterItem> : AbstractAlgorithm<T>() {
    private var mGridSize = DEFAULT_GRID_SIZE
    private val mItems: MutableSet<T> = Collections.synchronizedSet(HashSet())

    override fun addItem(item: T): Boolean {
        return mItems.add(item)
    }

    override fun addItems(items: Collection<T>): Boolean {
        return mItems.addAll(items)
    }

    override fun clearItems() {
        mItems.clear()
    }

    override fun removeItem(item: T): Boolean {
        return mItems.remove(item)
    }

    override fun removeItems(items: Collection<T>): Boolean {
        return mItems.removeAll(items.toSet())
    }

    override fun updateItem(item: T): Boolean {
        var result: Boolean
        synchronized(mItems) {
            result = removeItem(item)
            if (result) {
                // Only add the item if it was removed (to help prevent accidental duplicates on map)
                result = addItem(item)
            }
        }
        return result
    }

    override var maxDistanceBetweenClusteredItems: Int
        get() = mGridSize
        set(maxDistance) {
            mGridSize = maxDistance
        }

    override fun getClusters(zoom: Float): Set<Cluster<T>> {
        val numCells = ceil(256 * 2.0.pow(zoom.toDouble()) / mGridSize).toLong()
        val proj = SphericalMercatorProjection(numCells.toDouble())

        val clusters = HashSet<Cluster<T>>()
        val sparseArray = LongSparseArray<StaticCluster<T>>()

        synchronized(mItems) {
            for (item in mItems) {
                val p = proj.toPoint(item.position)
                val coord = getCoord(numCells, p.x, p.y)

                var cluster = sparseArray.get(coord)
                if (cluster == null) {
                    cluster = StaticCluster(proj.toLatLng(com.google.maps.android.geometry.Point(floor(p.x) + .5, floor(p.y) + .5)))
                    sparseArray.put(coord, cluster)
                    clusters.add(cluster)
                }
                cluster.add(item)
            }
        }

        return clusters
    }

    override val items: Collection<T>
        get() = mItems

    companion object {
        private const val DEFAULT_GRID_SIZE = 100

        private fun getCoord(numCells: Long, x: Double, y: Double): Long {
            return (numCells * floor(x) + floor(y)).toLong()
        }
    }
}
