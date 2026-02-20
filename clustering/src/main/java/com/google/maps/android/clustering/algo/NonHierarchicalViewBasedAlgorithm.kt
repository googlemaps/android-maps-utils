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
import com.google.maps.android.projection.SphericalMercatorProjection
import com.google.maps.android.quadtree.PointQuadTree
import java.util.ArrayList
import kotlin.math.pow

/**
 * Algorithm that can be used for managing large numbers of items (>1000 markers). This algorithm works the same way as [NonHierarchicalDistanceBasedAlgorithm]
 * but works, only in visible area. It requires [ ][.shouldReclusterOnMapMovement] to be true in order to re-render clustering
 * when camera movement changes the visible area.
 *
 * @param <T> The [ClusterItem] type
</T> */
class NonHierarchicalViewBasedAlgorithm<T : ClusterItem>
/**
 * @param screenWidth  map width in dp
 * @param screenHeight map height in dp
 */
    (private var mViewWidth: Int, private var mViewHeight: Int) : NonHierarchicalDistanceBasedAlgorithm<T>(), ScreenBasedAlgorithm<T> {

    private var mMapCenter: LatLng? = null

    override fun onCameraChange(position: CameraPosition) {
        mMapCenter = position.target
    }

    override fun getClusteringItems(quadTree: PointQuadTree<QuadItem<T>>, zoom: Float): Collection<QuadItem<T>> {
        var visibleBounds = getVisibleBounds(zoom)
        val items = ArrayList<QuadItem<T>>()

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

    override fun shouldReclusterOnMapMovement(): Boolean {
        return true
    }

    /**
     * Update view width and height in case map size was changed.
     * You need to recluster all the clusters, to update view state after view size changes.
     *
     * @param width  map width in dp
     * @param height map height in dp
     */
    fun updateViewSize(width: Int, height: Int) {
        mViewWidth = width
        mViewHeight = height
    }

    private fun getVisibleBounds(zoom: Float): Bounds {
        if (mMapCenter == null) {
            return Bounds(0.0, 0.0, 0.0, 0.0)
        }

        val p = PROJECTION.toPoint(mMapCenter!!)

        val halfWidthSpan = mViewWidth.toDouble() / 2.0.pow(zoom.toDouble()) / 256.0 / 2.0
        val halfHeightSpan = mViewHeight.toDouble() / 2.0.pow(zoom.toDouble()) / 256.0 / 2.0

        return Bounds(
            p.x - halfWidthSpan, p.x + halfWidthSpan,
            p.y - halfHeightSpan, p.y + halfHeightSpan
        )
    }

    companion object {
        private val PROJECTION = SphericalMercatorProjection(1.0)
    }
}
