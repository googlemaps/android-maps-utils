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
package com.google.maps.android.data.kml

import com.google.android.gms.maps.model.GroundOverlayOptions
import com.google.android.gms.maps.model.LatLngBounds

/**
 * Represents a KML Ground Overlay.
 */
@Deprecated("Use the new platform-agnostic data layer and renderer instead.")
public class KmlGroundOverlay(
    private val imageUrl: String,
    private val latLngBox: LatLngBounds,
    drawOrder: Float,
    visibility: Int,
    private val properties: Map<String, String>,
    rotation: Float,
) {
    private val groundOverlayOptions = GroundOverlayOptions()

    init {
        requireNotNull(latLngBox) { "No LatLonBox given" }
        groundOverlayOptions.positionFromBounds(latLngBox)
        groundOverlayOptions.bearing(rotation)
        groundOverlayOptions.zIndex(drawOrder)
        groundOverlayOptions.visible(visibility != 0)
    }

    public fun getImageUrl(): String = imageUrl

    public fun getLatLngBox(): LatLngBounds = latLngBox

    public fun getProperties(): Iterable<String> = properties.keys

    public fun getProperty(keyValue: String): String? = properties[keyValue]

    public fun hasProperty(keyValue: String): Boolean = properties.containsKey(keyValue)

    internal fun getGroundOverlayOptions(): GroundOverlayOptions = groundOverlayOptions

    override fun toString(): String =
        StringBuilder("GroundOverlay")
            .apply {
                append("{")
                append("\n properties=").append(properties)
                append(",\n image url=").append(imageUrl)
                append(",\n LatLngBox=").append(latLngBox)
                append("\n}\n")
            }.toString()
}
