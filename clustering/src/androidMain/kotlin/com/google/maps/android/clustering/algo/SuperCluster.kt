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
import com.google.maps.android.geometry.Point
import com.google.maps.android.projection.SphericalMercatorProjection
import java.util.Collections

/**
 * Represents a cluster or an unclustered single point within [SuperClusterAlgorithm].
 *
 * Implements [Cluster] so that instances can be passed directly to
 * [com.google.maps.android.clustering.view.ClusterRenderer] for visualization on a GoogleMap.
 *
 * ### Hierarchical Memory Model
 * Traditional clustering models copy and duplicate sets of items across every zoom level. At scales
 * of 100,000–1,000,000 items, eagerly populating item collections per cluster causes massive heap
 * bloat.
 *
 * [SuperCluster] solves this with lazy hierarchical resolution:
 * - Leaf nodes store the direct reference to their underlying [ClusterItem].
 * - Composite clusters store their weighted centroid coordinates, aggregate [size], and child
 *   node references from the zoom level below.
 * - [size] is an $O(1)$ primitive field access, allowing map renderers to display cluster counts
 *   instantaneously.
 * - [position] is computed lazily from normalized Mercator coordinates on first access.
 * - [items] traverses the child hierarchy on demand only when inspected.
 *
 * @param <T> The [ClusterItem] type.
 */
public class SuperCluster<T : ClusterItem> internal constructor(
    public val clusterId: Int,
    internal val mercatorX: Double,
    internal val mercatorY: Double,
    public override val size: Int,
    private val leafItem: T?,
    private val children: List<SuperCluster<T>>?,
    public val zoom: Int,
    private val itemsProvider: (() -> Collection<T>)? = null,
) : Cluster<T> {

    @Volatile
    private var cachedPosition: LatLng? = null

    public override val position: LatLng
        get() {
            if (leafItem != null) {
                return leafItem.position
            }
            var pos = cachedPosition
            if (pos == null) {
                pos = PROJECTION.toLatLng(Point(mercatorX, mercatorY))
                cachedPosition = pos
            }
            return pos
        }

    /**
     * True if this represents a single unclustered [ClusterItem], false if it is a cluster composed
     * of multiple items.
     */
    public val isLeaf: Boolean
        get() = leafItem != null

    /**
     * The underlying [ClusterItem] if this is a leaf node, or `null` if this is a composite cluster.
     */
    public val item: T?
        get() = leafItem

    /**
     * Child clusters or points from the next zoom level down that form this cluster.
     * Returns an empty list if this node is a leaf or child clusters are lazily resolved.
     */
    public val childClusters: List<SuperCluster<T>>
        get() = children ?: emptyList()

    /**
     * Lazily collects all leaf [ClusterItem] objects represented by this cluster.
     */
    public override val items: Collection<T>
        get() {
            if (leafItem != null) {
                return Collections.singleton(leafItem)
            }
            if (itemsProvider != null) {
                return itemsProvider.invoke()
            }
            val result = ArrayList<T>(size)
            collectLeafItems(result)
            return result
        }

    private fun collectLeafItems(out: MutableList<T>) {
        if (leafItem != null) {
            out.add(leafItem)
        } else if (children != null) {
            for (child in children) {
                child.collectLeafItems(out)
            }
        }
    }

    public override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is SuperCluster<*>) return false
        return clusterId == other.clusterId &&
            size == other.size &&
            mercatorX == other.mercatorX &&
            mercatorY == other.mercatorY
    }

    public override fun hashCode(): Int {
        var result = clusterId
        result = 31 * result + mercatorX.hashCode()
        result = 31 * result + size
        result = 31 * result + mercatorY.hashCode()
        return result
    }

    public override fun toString(): String =
        "SuperCluster(id=$clusterId, size=$size, position=$position, isLeaf=$isLeaf, zoom=$zoom)"

    internal companion object {
        private val PROJECTION = SphericalMercatorProjection(1.0)
    }
}
