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

package com.google.maps.android.data.parser

import com.google.maps.android.data.parser.kml.Style

/**
 * A generic container for geographic data parsed from any file.
 * It holds a collection of features, each representing a distinct entity on the map.
 */
data class GeoData(
    val features: List<Feature>
)

/**
 * Represents a single, distinct geographic entity, such as a placemark, a route, or a defined area.
 * It combines geometry (the 'what' and 'where') with properties (the 'metadata') and styling.
 */
data class Feature(
    val geometry: Geometry,
    val properties: Map<String, Any> = emptyMap(), // For metadata like name, description, etc.
    val style: Style? = null
)

/**
 * A sealed interface representing the geometric shape of a feature.
 */
sealed interface Geometry {
    data class Point(val lat: Double, val lon: Double, val alt: Double?) : Geometry
    data class LineString(val points: List<Point>) : Geometry
    data class Polygon(val shell: List<Point>, val holes: List<List<Point>> = emptyList()) : Geometry
    data class GeometryCollection(val geometries: List<Geometry>) : Geometry

}

/**
 * Represents styling information that can be applied to a feature.
 * Properties are nullable as not all formats or features will specify them.
 */
data class Style(
    val strokeColor: String?, // e.g., "#RRGGBB" or "#AARRGGBB"
    val strokeWidth: Float?,
    val fillColor: String?
)