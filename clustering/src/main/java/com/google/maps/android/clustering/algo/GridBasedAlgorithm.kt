/*
 * Copyright 2024 Google Inc.
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

private const val DEFAULT_GRID_SIZE = 100

/**
 * Groups markers into a grid for clustering. This algorithm organizes items into a two-dimensional grid,
 * facilitating the formation of clusters based on proximity within each grid cell. The grid size determines
 * the spatial granularity of clustering, and clusters are created by aggregating items within the same grid cell.
 *
 * The effectiveness of clustering is influenced by the specified grid size, which determines the spatial resolution of the grid.
 * Smaller grid sizes result in more localized clusters, whereas larger grid sizes lead to broader clusters covering larger areas.
 *
 * @param <T> The type of [ClusterItem] to be clustered.
 */
public class GridBasedAlgorithm<T : ClusterItem> : AbstractAlgorithm<T>() {
    private val _items: MutableSet<T> = Collections.synchronizedSet(HashSet())
    override val items: Collection<T>
        get() = _items

    override var maxDistanceBetweenClusteredItems: Int = DEFAULT_GRID_SIZE

    /**
     * Adds an item to the algorithm
     * @param item the item to be added
     * @return true if the algorithm contents changed as a result of the call
     */
    override fun addItem(item: T): Boolean = _items.add(item)

    /**
     * Adds a collection of items to the algorithm
     * @param items the items to be added
     * @return true if the algorithm contents changed as a result of the call
     */
    override fun addItems(items: Collection<T>): Boolean = _items.addAll(items)

    override fun clearItems() = _items.clear()

    /**
     * Removes an item from the algorithm
     * @param item the item to be removed
     * @return true if this algorithm contained the specified element (or equivalently, if this
     * algorithm changed as a result of the call).
     */
    override fun removeItem(item: T): Boolean = _items.remove(item)

    /**
     * Removes a collection of items from the algorithm
     * @param items the items to be removed
     * @return true if this algorithm contents changed as a result of the call
     */
    override fun removeItems(items: Collection<T>): Boolean = _items.removeAll(items)

    /**
     * Updates the provided item in the algorithm
     * @param item the item to be updated
     * @return true if the item existed in the algorithm and was updated, or false if the item did
     * not exist in the algorithm and the algorithm contents remain unchanged.
     */
    override fun updateItem(item: T): Boolean {
        return synchronized(_items) {
            if (removeItem(item)) {
                // Only add the item if it was removed (to help prevent accidental duplicates on map)
                addItem(item)
            } else {
                false
            }
        }
    }

    override fun getClusters(zoom: Float): Set<Cluster<T>> {
        val numCells = ceil(256 * 2.0.pow(zoom.toDouble()) / maxDistanceBetweenClusteredItems).toLong()
        val proj = SphericalMercatorProjection(numCells.toDouble())

        val clusters = HashSet<Cluster<T>>()
        val sparseArray = LongSparseArray<StaticCluster<T>>()

        synchronized(_items) {
            for (item in _items) {
                val p = proj.toPoint(item.position)

                val coord = getCoord(numCells, p.x, p.y)

                var cluster = sparseArray.get(coord)
                if (cluster == null) {
                    cluster = StaticCluster(proj.toLatLng(Point(floor(p.x) + .5, floor(p.y) + .5)))
                    sparseArray.put(coord, cluster)
                    clusters.add(cluster)
                }
                cluster.add(item)
            }
        }

        return clusters
    }

    private companion object {
        private fun getCoord(numCells: Long, x: Double, y: Double): Long {
            return (numCells * floor(x) + floor(y)).toLong()
        }
    }
}
