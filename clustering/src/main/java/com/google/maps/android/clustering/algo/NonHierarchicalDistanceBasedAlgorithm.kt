/*
 * Copyright 2023 Google Inc.
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

import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.clustering.Cluster
import com.google.maps.android.clustering.ClusterItem
import com.google.maps.android.geometry.Bounds
import com.google.maps.android.geometry.Point
import com.google.maps.android.projection.SphericalMercatorProjection
import com.google.maps.android.quadtree.PointQuadTree
import java.util.Collections

private const val DEFAULT_MAX_DISTANCE_AT_ZOOM = 100 // essentially 100 dp.

/**
 * A simple clustering algorithm with O(nlog n) performance. Resulting clusters are not
 * hierarchical.
 *
 * High level algorithm:
 * 1. Iterate over items in the order they were added (candidate clusters).
 * 2. Create a cluster with the center of the item.
 * 3. Add all items that are within a certain distance to the cluster.
 * 4. Move any items out of an existing cluster if they are closer to another cluster.
 * 5. Remove those items from the list of candidate clusters.
 *
 * Clusters have the center of the first element (not the centroid of the items within it).
 */
public open class NonHierarchicalDistanceBasedAlgorithm<T : ClusterItem> : AbstractAlgorithm<T>() {
    /**
     * Any modifications should be synchronized on quadTree.
     */
    protected val _quadItems: MutableCollection<QuadItem<T>> = LinkedHashSet()

    /**
     * Any modifications should be synchronized on quadTree.
     */
    protected val quadTree: PointQuadTree<QuadItem<T>> = PointQuadTree(0.0, 1.0, 0.0, 1.0)

    override var maxDistanceBetweenClusteredItems: Int = DEFAULT_MAX_DISTANCE_AT_ZOOM

    /**
     * Adds an item to the algorithm
     * @param item the item to be added
     * @return true if the algorithm contents changed as a result of the call
     */
    override fun addItem(item: T): Boolean {
        val quadItem = QuadItem(item)
        return synchronized(quadTree) {
            if (_quadItems.add(quadItem)) {
                quadTree.add(quadItem)
                true
            } else {
                false
            }
        }
    }

    /**
     * Adds a collection of items to the algorithm
     * @param items the items to be added
     * @return true if the algorithm contents changed as a result of the call
     */
    override fun addItems(items: Collection<T>): Boolean {
        var result = false
        for (item in items) {
            val individualResult = addItem(item)
            if (individualResult) {
                result = true
            }
        }
        return result
    }

    override fun clearItems() {
        synchronized(quadTree) {
            _quadItems.clear()
            quadTree.clear()
        }
    }

    /**
     * Removes an item from the algorithm
     * @param item the item to be removed
     * @return true if this algorithm contained the specified element (or equivalently, if this
     * algorithm changed as a result of the call).
     */
    override fun removeItem(item: T): Boolean {
        // QuadItem delegates hashcode() and equals() to its item so,
        //   removing any QuadItem to that item will remove the item
        val quadItem = QuadItem(item)
        return synchronized(quadTree) {
            if (_quadItems.remove(quadItem)) {
                quadTree.remove(quadItem)
                true
            } else {
                false
            }
        }
    }

    /**
     * Removes a collection of items from the algorithm
     * @param items the items to be removed
     * @return true if this algorithm contents changed as a result of the call
     */
    override fun removeItems(items: Collection<T>): Boolean {
        var result = false
        synchronized(quadTree) {
            for (item in items) {
                // QuadItem delegates hashcode() and equals() to its item so,
                //   removing any QuadItem to that item will remove the item
                val quadItem = QuadItem(item)
                if (this._quadItems.remove(quadItem)) {
                    quadTree.remove(quadItem)
                    result = true
                }
            }
        }
        return result
    }

    /**
     * Updates the provided item in the algorithm
     * @param item the item to be updated
     * @return true if the item existed in the algorithm and was updated, or false if the item did
     * not exist in the algorithm and the algorithm contents remain unchanged.
     */
    override fun updateItem(item: T): Boolean {
        // TODO - Can this be optimized to update the item in-place if the location hasn't changed?
        return synchronized(quadTree) {
            if (removeItem(item)) {
                // Only add the item if it was removed (to help prevent accidental duplicates on map)
                addItem(item)
            } else {
                false
            }
        }
    }

    override fun getClusters(zoom: Float): Set<Cluster<T>> {
        val discreteZoom = zoom.toInt()

        val zoomSpecificSpan = maxDistanceBetweenClusteredItems / Math.pow(2.0, discreteZoom.toDouble()) / 256.0

        val visitedCandidates = HashSet<QuadItem<T>>()
        val results = HashSet<Cluster<T>>()
        val distanceToCluster = HashMap<QuadItem<T>, Double>()
        val itemToCluster = HashMap<QuadItem<T>, StaticCluster<T>>()

        synchronized(quadTree) {
            for (candidate in getClusteringItems(quadTree, zoom)) {
                if (visitedCandidates.contains(candidate)) {
                    // Candidate is already part of another cluster.
                    continue
                }

                val searchBounds = createBoundsFromSpan(candidate.point, zoomSpecificSpan)
                val clusterItems = quadTree.search(searchBounds)
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
        return results
    }

    protected open fun getClusteringItems(quadTree: PointQuadTree<QuadItem<T>>, zoom: Float): Collection<QuadItem<T>> {
        return _quadItems
    }

    override val items: Collection<T>
        get() {
            val items = LinkedHashSet<T>()
            synchronized(quadTree) {
                for (quadItem in this._quadItems) {
                    items.add(quadItem.clusterItem)
                }
            }
            return items
        }

    /**
     * Calculates the squared Euclidean distance between two points.
     *
     * @param a the first point
     * @param b the second point
     * @return the squared Euclidean distance between `a` and `b`
     */
    protected fun distanceSquared(a: Point, b: Point): Double {
        return (a.x - b.x) * (a.x - b.x) + (a.y - b.y) * (a.y - b.y)
    }

    /**
     * Creates a square bounding box centered at a point with the specified span.
     *
     * @param p the center point
     * @param span the total width/height of the bounding box
     * @return the [Bounds] object representing the search area
     */
    protected fun createBoundsFromSpan(p: Point, span: Double): Bounds {
        // TODO: Use a span that takes into account the visual size of the marker, not just its
        // LatLng.
        val halfSpan = span / 2
        return Bounds(
            p.x - halfSpan, p.x + halfSpan,
            p.y - halfSpan, p.y + halfSpan
        )
    }

    protected class QuadItem<T : ClusterItem>(
        val clusterItem: T
    ) : PointQuadTree.Item, Cluster<T> {
        override val point: Point
        override val position: LatLng
        private val singletonSet: Set<T>

        init {
            position = clusterItem.position
            point = PROJECTION.toPoint(position)
            singletonSet = Collections.singleton(clusterItem)
        }

        override val items: Set<T>
            get() = singletonSet

        override val size: Int
            get() = 1

        override fun hashCode(): Int {
            return clusterItem.hashCode()
        }

        override fun equals(other: Any?): Boolean {
            if (other !is QuadItem<*>) {
                return false
            }
            return other.clusterItem == clusterItem
        }
    }

    private companion object {
        private val PROJECTION = SphericalMercatorProjection(1.0)
    }
}
