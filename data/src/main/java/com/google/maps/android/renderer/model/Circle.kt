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

import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.PatternItem

/**
 * A data model representing a circle on the map.
 */
public data class Circle(
    public var center: LatLng,
    public var radius: Double,
    public var strokeWidth: Float = 10.0f,
    public var strokeColor: Int = -0x1000000, // Black
    public var fillColor: Int = 0x00000000, // Transparent
    public var isClickable: Boolean = false,
    public override var isVisible: Boolean = true,
    public override var zIndex: Float = 0.0f,
    public var strokePattern: List<PatternItem>? = null,
) : MapObject {
    public override val type: MapObject.Type
        get() = MapObject.Type.CIRCLE
}
