/*
 * Copyright 2013 Google LLC
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
import java.util.ArrayList
import java.util.Collections
import java.util.HashMap
import java.util.HashSet
import java.util.LinkedHashSet

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
open class NonHierarchicalDistanceBasedAlgorithm<T : ClusterItem> : AbstractAlgorithm<T>() {

    /**
     * Any modifications should be synchronized on mQuadTree.
     */
    @JvmField
    protected val mItems: MutableCollection<QuadItem<T>> = LinkedHashSet()

    /**
     * Any modifications should be synchronized on mQuadTree.
     */
    @JvmField
    protected val mQuadTree: PointQuadTree<QuadItem<T>> = PointQuadTree(0.0, 1.0, 0.0, 1.0)

    override var maxDistanceBetweenClusteredItems: Int = DEFAULT_MAX_DISTANCE_AT_ZOOM

    override fun addItem(item: T): Boolean {
        val quadItem = QuadItem(item)
        synchronized(mQuadTree) {
            val result = mItems.add(quadItem)
            if (result) {
                mQuadTree.add(quadItem)
            }
            return result
        }
    }

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
        synchronized(mQuadTree) {
            mItems.clear()
            mQuadTree.clear()
        }
    }

    override fun removeItem(item: T): Boolean {
        // QuadItem delegates hashcode() and equals() to its item so,
        //   removing any QuadItem to that item will remove the item
        val quadItem = QuadItem(item)
        synchronized(mQuadTree) {
            val result = mItems.remove(quadItem)
            if (result) {
                mQuadTree.remove(quadItem)
            }
            return result
        }
    }

    override fun removeItems(items: Collection<T>): Boolean {
        var result = false
        synchronized(mQuadTree) {
            for (item in items) {
                // QuadItem delegates hashcode() and equals() to its item so,
                //   removing any QuadItem to that item will remove the item
                val quadItem = QuadItem(item)
                val individualResult = mItems.remove(quadItem)
                if (individualResult) {
                    mQuadTree.remove(quadItem)
                    result = true
                }
            }
        }
        return result
    }

    override fun updateItem(item: T): Boolean {
        // TODO - Can this be optimized to update the item in-place if the location hasn't changed?
        synchronized(mQuadTree) {
            var result = removeItem(item)
            if (result) {
                // Only add the item if it was removed (to help prevent accidental duplicates on map)
                result = addItem(item)
            }
            return result
        }
    }

    override fun getClusters(zoom: Float): Set<Cluster<T>> {
        val discreteZoom = zoom.toInt()

        val zoomSpecificSpan = maxDistanceBetweenClusteredItems.toDouble() / Math.pow(2.0, discreteZoom.toDouble()) / 256.0

        val visitedCandidates = HashSet<QuadItem<T>>()
        val results = HashSet<Cluster<T>>()
        val distanceToCluster = HashMap<QuadItem<T>, Double>()
        val itemToCluster = HashMap<QuadItem<T>, StaticCluster<T>>()

        synchronized(mQuadTree) {
            for (candidate in getClusteringItems(mQuadTree, zoom)) {
                if (visitedCandidates.contains(candidate)) {
                    // Candidate is already part of another cluster.
                    continue
                }

                val searchBounds = createBoundsFromSpan(candidate.point, zoomSpecificSpan)
                val mClusterItems = mQuadTree.search(searchBounds)
                if (mClusterItems.size == 1) {
                    // Only the current marker is in range. Just add the single item to the results.
                    results.add(candidate)
                    visitedCandidates.add(candidate)
                    distanceToCluster[candidate] = 0.0
                    continue
                }
                val cluster = StaticCluster<T>(candidate.mClusterItem.position)
                results.add(cluster)

                for (mClusterItem in mClusterItems) {
                    val existingDistance = distanceToCluster[mClusterItem]
                    val distance = distanceSquared(mClusterItem.point, candidate.point)
                    if (existingDistance != null) {
                        // Item already belongs to another cluster. Check if it's closer to this cluster.
                        if (existingDistance < distance) {
                            continue
                        }
                        // Move item to the closer cluster.
                        itemToCluster[mClusterItem]?.remove(mClusterItem.mClusterItem)
                    }
                    distanceToCluster[mClusterItem] = distance
                    cluster.add(mClusterItem.mClusterItem)
                    itemToCluster[mClusterItem] = cluster
                }
                visitedCandidates.addAll(mClusterItems)
            }
        }
        return results
    }

    protected open fun getClusteringItems(quadTree: PointQuadTree<QuadItem<T>>, zoom: Float): Collection<QuadItem<T>> {
        return mItems
    }

    override val items: Collection<T>
        get() {
            val items = LinkedHashSet<T>()
            synchronized(mQuadTree) {
                for (quadItem in mItems) {
                    items.add(quadItem.mClusterItem)
                }
            }
            return items
        }

    /**
     * Calculates the squared Euclidean distance between two points.
     *
     * @param a the first point
     * @param b the second point
     * @return the squared Euclidean distance between [a] and [b]
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

    protected class QuadItem<T : ClusterItem>(@JvmField val mClusterItem: T) : PointQuadTree.Item, Cluster<T> {
        private val mPoint: Point = PROJECTION.toPoint(mClusterItem.position)
        private val mPosition: LatLng = mClusterItem.position
        private val singletonSet: Set<T> = Collections.singleton(mClusterItem)

        override val point: Point
            get() = mPoint

        override val position: LatLng
            get() = mPosition

        override val items: Collection<T>
            get() = singletonSet

        override val size: Int
            get() = 1

        override fun hashCode(): Int {
            return mClusterItem.hashCode()
        }

        override fun equals(other: Any?): Boolean {
            if (other !is QuadItem<*>) {
                return false
            }

            return other.mClusterItem == mClusterItem
        }
    }

    companion object {
        private const val DEFAULT_MAX_DISTANCE_AT_ZOOM = 100 // essentially 100 dp.

        private val PROJECTION = SphericalMercatorProjection(1.0)
    }
}
