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
import com.google.android.gms.maps.model.PatternItem
import com.google.android.gms.maps.model.Polygon

/**
 * High-level, decoupled wrapper around Google Maps [Polygon].
 */
public class PolygonOverlay(override val native: Polygon, private val onRemoveCallback: (PolygonOverlay) -> Boolean) :
    PathOverlay<Polygon> {

    override var points: List<LatLng>
        get() = native.points
        set(value) {
            native.points = value
        }

    public var holes: List<List<LatLng>>
        get() = native.holes
        set(value) {
            native.holes = value
        }

    public var fillColor: Int
        get() = native.fillColor
        set(value) {
            native.fillColor = value
        }

    public var strokeColor: Int
        get() = native.strokeColor
        set(value) {
            native.strokeColor = value
        }

    public var strokeWidth: Float
        get() = native.strokeWidth
        set(value) {
            native.strokeWidth = value
        }

    public var strokeJointType: Int
        get() = native.strokeJointType
        set(value) {
            native.strokeJointType = value
        }

    public var strokePattern: List<PatternItem>?
        get() = native.strokePattern
        set(value) {
            native.strokePattern = value
        }

    public var isGeodesic: Boolean
        get() = native.isGeodesic
        set(value) {
            native.isGeodesic = value
        }

    public var isClickable: Boolean
        get() = native.isClickable
        set(value) {
            native.isClickable = value
        }

    override var isVisible: Boolean
        get() = native.isVisible
        set(value) {
            native.isVisible = value
        }

    override var zIndex: Float
        get() = native.zIndex
        set(value) {
            native.zIndex = value
        }

    override var tag: Any?
        get() = native.tag
        set(value) {
            native.tag = value
        }

    override fun remove(): Boolean = onRemoveCallback(this)

    internal var clickListener: ((PolygonOverlay) -> Unit)? = null

    /**
     * Registers a click callback for this polygon overlay.
     */
    public fun onClick(listener: (PolygonOverlay) -> Unit) {
        native.isClickable = true
        clickListener = listener
    }
}
