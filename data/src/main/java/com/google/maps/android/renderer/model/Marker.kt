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
package com.google.maps.android.renderer.model

import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.LatLng

/**
 * A data model representing a marker on the map.
 */
public data class Marker(
    public var position: LatLng,
    public var title: String? = null,
    public var snippet: String? = null,
    public var icon: BitmapDescriptor? = null,
    public var alpha: Float = 1.0f,
    public var anchorU: Float = 0.5f,
    public var anchorV: Float = 1.0f,
    public var rotation: Float = 0.0f,
    public var flat: Boolean = false,
    public var draggable: Boolean = false,
    public override var isVisible: Boolean = true,
    public override var zIndex: Float = 0.0f,
) : MapObject {
    public override val type: MapObject.Type
        get() = MapObject.Type.MARKER
}
