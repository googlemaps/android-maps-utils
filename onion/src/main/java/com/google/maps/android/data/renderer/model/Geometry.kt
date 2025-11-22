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

/**
 * A sealed interface representing a geometric object.
 *
 * This is a platform-agnostic representation of a shape that can be rendered on a map.
 */
sealed interface Geometry {
    val type: String
}

/**
 * A Geometry that contains a single Point.
 *
 * @property point The Point object representing the location.
 */
data class PointGeometry(val point: Point) : Geometry {
    override val type: String = "Point"
}

/**
 * A Geometry that contains a list of Points, representing a line.
 *
 * @property points The list of Points that make up the line.
 */
data class LineString(val points: List<Point>) : Geometry {
    override val type: String = "LineString"
}

/**
 * A Geometry that contains an outer boundary and a list of inner boundaries (holes).
 *
 * @property outerBoundary The list of Points that define the outer boundary of the polygon.
 * @property innerBoundaries The list of lists of Points that define the inner boundaries (holes) of the polygon. Defaults to an empty list.
 */
data class Polygon(val outerBoundary: List<Point>, val innerBoundaries: List<List<Point>> = emptyList()) : Geometry {
    override val type: String = "Polygon"
}

/**
 * A Geometry that is a collection of other Geometry objects.
 *
 * @property geometries The list of Geometry objects.
 */
data class MultiGeometry(val geometries: List<Geometry>) : Geometry {
    override val type: String = "MultiGeometry"
}
