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

package com.google.maps.android.renderer.model

import com.google.android.gms.maps.model.Cap
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.PatternItem
import com.google.android.gms.maps.model.RoundCap

/**
 * A data model representing a polyline on the map.
 */
data class Polyline(
    var points: List<LatLng>,
    var width: Float = 10.0f,
    var color: Int = -0x1000000, // Black
    var isClickable: Boolean = false,
    var isGeodesic: Boolean = false,
    override var isVisible: Boolean = true,
    override var zIndex: Float = 0.0f,
    var startCap: Cap = RoundCap(),
    var endCap: Cap = RoundCap(),
    var jointType: Int = 0, // JointType.DEFAULT
    var pattern: List<PatternItem>? = null
) : MapObject {
    override val type: MapObject.Type
        get() = MapObject.Type.POLYLINE
}
