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

import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.LatLng

/**
 * A data model representing a marker on the map.
 */
data class Marker(
    var position: LatLng,
    var title: String? = null,
    var snippet: String? = null,
    var icon: BitmapDescriptor? = null,
    var alpha: Float = 1.0f,
    var anchorU: Float = 0.5f,
    var anchorV: Float = 1.0f,
    var rotation: Float = 0.0f,
    var flat: Boolean = false,
    var draggable: Boolean = false,
    override var isVisible: Boolean = true,
    override var zIndex: Float = 0.0f
) : MapObject {
    override val type: MapObject.Type
        get() = MapObject.Type.MARKER
}
