/*
 * Copyright 2025 Google LLC
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

package com.google.maps.android.heatmaps

import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.geometry.Point
import com.google.maps.android.projection.SphericalMercatorProjection
import com.google.maps.android.quadtree.PointQuadTree

data class WeightedLatLng(
    val latLng: LatLng,
    override val point: Point,
    val intensity: Double
) : PointQuadTree.Item {

    /**
     * Constructor that uses default value for intensity
     *
     * @param latLng LatLng to add to wrapper
     */
    @JvmOverloads
    constructor(latLng: LatLng, intensity: Double = DEFAULT_INTENSITY) : this(
        latLng,
        sProjection.toPoint(latLng),
        if (intensity >= 0) intensity else DEFAULT_INTENSITY
    )

    companion object {
        const val DEFAULT_INTENSITY = 1.0
        private val sProjection = SphericalMercatorProjection(HeatmapTileProvider.WORLD_WIDTH)
    }
}
