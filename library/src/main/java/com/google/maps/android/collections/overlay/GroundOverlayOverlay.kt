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

import com.google.android.gms.maps.model.GroundOverlay
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds

/**
 * High-level, decoupled wrapper around Google Maps [GroundOverlay].
 */
class GroundOverlayOverlay(
    override val native: GroundOverlay,
    private val onRemoveCallback: (GroundOverlayOverlay) -> Boolean,
) : PointOverlay<GroundOverlay> {

    override var position: LatLng
        get() = native.position
        set(value) { native.position = value }

    var bounds: LatLngBounds?
        get() = native.bounds
        set(value) {
            if (value != null) {
                native.setPositionFromBounds(value)
            }
        }

    var width: Float
        get() = native.width
        set(value) { native.setDimensions(value) }

    var height: Float
        get() = native.height
        set(value) { native.setDimensions(native.width, value) }

    var bearing: Float
        get() = native.bearing
        set(value) { native.bearing = value }

    var transparency: Float
        get() = native.transparency
        set(value) { native.transparency = value }

    var isClickable: Boolean
        get() = native.isClickable
        set(value) { native.isClickable = value }

    override var isVisible: Boolean
        get() = native.isVisible
        set(value) { native.isVisible = value }

    override var zIndex: Float
        get() = native.zIndex
        set(value) { native.zIndex = value }

    override var tag: Any?
        get() = native.tag
        set(value) { native.tag = value }

    override fun remove(): Boolean = onRemoveCallback(this)

    internal var clickListener: ((GroundOverlayOverlay) -> Unit)? = null

    /**
     * Registers a click callback for this ground overlay.
     */
    fun onClick(listener: (GroundOverlayOverlay) -> Unit) {
        native.isClickable = true
        clickListener = listener
    }
}
