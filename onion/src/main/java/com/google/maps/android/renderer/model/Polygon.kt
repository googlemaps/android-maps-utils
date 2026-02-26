/*
 * Copyright 2026 Google LLC
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

package com.google.maps.android.renderer.model

import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.PatternItem

/**
 * A data model representing a polygon on the map.
 */
data class Polygon(
    var points: List<LatLng>,
    var holes: MutableList<List<LatLng>> = mutableListOf(),
    var strokeWidth: Float = 10.0f,
    var strokeColor: Int = -0x1000000, // Black
    var fillColor: Int = 0x00000000, // Transparent
    var isClickable: Boolean = false,
    var isGeodesic: Boolean = false,
    override var isVisible: Boolean = true,
    override var zIndex: Float = 0.0f,
    var strokeJointType: Int = 0, // JointType.DEFAULT
    var strokePattern: List<PatternItem>? = null
) : MapObject {
    override val type: MapObject.Type
        get() = MapObject.Type.POLYGON

    fun addHole(hole: List<LatLng>) {
        holes.add(hole)
    }
}
