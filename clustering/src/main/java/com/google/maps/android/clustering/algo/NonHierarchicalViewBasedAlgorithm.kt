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

import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.clustering.ClusterItem
import com.google.maps.android.geometry.Bounds
import com.google.maps.android.geometry.Point
import com.google.maps.android.projection.SphericalMercatorProjection
import com.google.maps.android.quadtree.PointQuadTree
import kotlin.math.pow

/**
 * Algorithm that can be used for managing large numbers of items (>1000 markers). This algorithm works the same way as [NonHierarchicalDistanceBasedAlgorithm]
 * but works, only in visible area. It requires [shouldReclusterOnMapMovement] to be true in order to re-render clustering
 * when camera movement changes the visible area.
 *
 * @param T The [ClusterItem] type
 */
public class NonHierarchicalViewBasedAlgorithm<T : ClusterItem>(
    /**
     * map width in dp
     */
    private var viewWidth: Int,
    /**
     * map height in dp
     */
    private var viewHeight: Int
) : NonHierarchicalDistanceBasedAlgorithm<T>(), ScreenBasedAlgorithm<T> {

    private var mapCenter: LatLng? = null

    override fun onCameraChange(cameraPosition: CameraPosition) {
        mapCenter = cameraPosition.target
    }

    override fun getClusteringItems(quadTree: PointQuadTree<QuadItem<T>>, zoom: Float): Collection<QuadItem<T>> {
        var visibleBounds = getVisibleBounds(zoom)
        val items = mutableListOf<QuadItem<T>>()

        // Handle wrapping around international date line
        if (visibleBounds.minX < 0) {
            val wrappedBounds = Bounds(visibleBounds.minX + 1, 1.0, visibleBounds.minY, visibleBounds.maxY)
            items.addAll(quadTree.search(wrappedBounds))
            visibleBounds = Bounds(0.0, visibleBounds.maxX, visibleBounds.minY, visibleBounds.maxY)
        }
        if (visibleBounds.maxX > 1) {
            val wrappedBounds = Bounds(0.0, visibleBounds.maxX - 1, visibleBounds.minY, visibleBounds.maxY)
            items.addAll(quadTree.search(wrappedBounds))
            visibleBounds = Bounds(visibleBounds.minX, 1.0, visibleBounds.minY, visibleBounds.maxY)
        }
        items.addAll(quadTree.search(visibleBounds))

        return items
    }

    override fun shouldReclusterOnMapMovement(): Boolean = true

    /**
     * Update view width and height in case map size was changed.
     * You need to recluster all the clusters, to update view state after view size changes.
     *
     * @param width  map width in dp
     * @param height map height in dp
     */
    public fun updateViewSize(width: Int, height: Int) {
        viewWidth = width
        viewHeight = height
    }

    override val items: Collection<T>
        get() = super.items

    private fun getVisibleBounds(zoom: Float): Bounds {
        val latLng = mapCenter ?: return Bounds(0.0, 0.0, 0.0, 0.0)

        val p = PROJECTION.toPoint(latLng)

        val halfWidthSpan = viewWidth / 2.0.pow(zoom.toDouble()) / 256 / 2
        val halfHeightSpan = viewHeight / 2.0.pow(zoom.toDouble()) / 256 / 2

        return Bounds(
            p.x - halfWidthSpan, p.x + halfWidthSpan,
            p.y - halfHeightSpan, p.y + halfHeightSpan
        )
    }

    private companion object {
        private val PROJECTION = SphericalMercatorProjection(1.0)
    }
}
