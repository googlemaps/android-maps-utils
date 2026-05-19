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

import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolygonOptions
import com.google.android.gms.maps.model.PolylineOptions
import com.google.maps.android.data.Feature
import com.google.maps.android.data.Geometry

/**
 * Represents a placemark which is either a KmlPoint, KmlLineString, KmlPolygon or a
 * KmlMultiGeometry. Stores the properties and styles of the place.
 */
@Deprecated("Use the new platform-agnostic data layer and renderer instead.")
public class KmlPlacemark(
    geometry: Geometry?,
    private val style: String?,
    private val inlineStyle: KmlStyle?,
    properties: Map<String, String>?,
) : Feature(geometry, style, properties) {
    public fun getStyleId(): String? = getId()

    public fun getInlineStyle(): KmlStyle? = inlineStyle

    public fun getPolygonOptions(): PolygonOptions? = inlineStyle?.getPolygonOptions()

    public fun getMarkerOptions(): MarkerOptions? = inlineStyle?.getMarkerOptions()

    public fun getPolylineOptions(): PolylineOptions? = inlineStyle?.getPolylineOptions()

    override fun toString(): String =
        StringBuilder("Placemark")
            .apply {
                append("{")
                append("\n style id=").append(style)
                append(",\n inline style=").append(inlineStyle)
                append("\n}\n")
            }.toString()
}
