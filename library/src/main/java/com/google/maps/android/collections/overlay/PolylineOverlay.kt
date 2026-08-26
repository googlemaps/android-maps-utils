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

import com.google.android.gms.maps.model.Cap
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.PatternItem
import com.google.android.gms.maps.model.Polyline

/**
 * High-level, decoupled wrapper around Google Maps [Polyline].
 */
class PolylineOverlay(
    override val native: Polyline,
    private val onRemoveCallback: (PolylineOverlay) -> Boolean,
) : PathOverlay<Polyline> {

    override var points: List<LatLng>
        get() = native.points
        set(value) { native.points = value }

    var color: Int
        get() = native.color
        set(value) { native.color = value }

    var width: Float
        get() = native.width
        set(value) { native.width = value }

    var jointType: Int
        get() = native.jointType
        set(value) { native.jointType = value }

    var startCap: Cap
        get() = native.startCap
        set(value) { native.startCap = value }

    var endCap: Cap
        get() = native.endCap
        set(value) { native.endCap = value }

    var pattern: List<PatternItem>?
        get() = native.pattern
        set(value) { native.pattern = value }

    var isGeodesic: Boolean
        get() = native.isGeodesic
        set(value) { native.isGeodesic = value }

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

    internal var clickListener: ((PolylineOverlay) -> Unit)? = null

    /**
     * Registers a click callback for this polyline overlay.
     */
    fun onClick(listener: (PolylineOverlay) -> Unit) {
        native.isClickable = true
        clickListener = listener
    }
}
