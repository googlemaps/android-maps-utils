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

package com.google.maps.android.collections.overlay

import com.google.android.gms.maps.model.LatLng

/**
 * Common abstraction representing an overlay object displayed on a Google Map.
 *
 * This interface decouples map operations from concrete platform types (such as [com.google.android.gms.maps.model.Marker],
 * [com.google.android.gms.maps.model.Polyline], or [com.google.android.gms.maps.model.Polygon]) by providing a unified
 * contract for visibility, z-ordering, tags, event listeners, and lifecycle removal.
 *
 * @param T The underlying native platform object type.
 */
interface MapOverlay<T : Any> {

    /**
     * The underlying native platform object instance.
     */
    val native: T

    /**
     * Controls the visibility of this overlay on the map.
     */
    var isVisible: Boolean

    /**
     * The z-index order of this overlay relative to other overlays on the map.
     */
    var zIndex: Float

    /**
     * An optional application metadata tag associated with this overlay.
     */
    var tag: Any?

    /**
     * Removes this overlay from the map and its containing collection.
     *
     * @return `true` if this overlay was successfully removed, or `false` if it was already removed.
     */
    fun remove(): Boolean
}

/**
 * An overlay positioned at a discrete geographic coordinate.
 */
interface PointOverlay<T : Any> : MapOverlay<T> {
    /**
     * The geographic position of this overlay.
     */
    var position: LatLng
}

/**
 * An overlay defined by a continuous sequence of geographic coordinates.
 */
interface PathOverlay<T : Any> : MapOverlay<T> {
    /**
     * The sequence of points defining the path.
     */
    var points: List<LatLng>
}
