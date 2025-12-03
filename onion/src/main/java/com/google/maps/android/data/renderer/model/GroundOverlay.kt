/*
 * Copyright 2025 Google LLC
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
package com.google.maps.android.data.renderer.model

import com.google.android.gms.maps.model.LatLngBounds

/**
 * Represents a GroundOverlay geometry.
 *
 * @property latLonBox The bounding box of the overlay.
 * @property rotation The rotation of the overlay in degrees clockwise from north.
 */
data class GroundOverlay(
    val north: Double,
    val south: Double,
    val east: Double,
    val west: Double,
    val rotation: Float = 0f
) : Geometry {
    val latLngBounds: LatLngBounds
        get() = LatLngBounds(
            com.google.android.gms.maps.model.LatLng(south, west),
            com.google.android.gms.maps.model.LatLng(north, east)
        )

    override val type: String = "GroundOverlay"
}
