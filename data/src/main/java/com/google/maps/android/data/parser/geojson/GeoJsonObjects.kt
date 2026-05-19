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
package com.google.maps.android.data.parser.geojson

/**
 * A data class representing a single geographical coordinate.
 *
 * This class holds the latitude, longitude, and an optional altitude value.
 * The order of properties is (latitude, longitude) to align with common mapping SDKs,
 * even though GeoJSON specifies (longitude, latitude).
 *
 * @property lat The latitude of the coordinate.
 * @property lng The longitude of the coordinate.
 * @property alt The altitude of the coordinate, in meters. Optional.
 */
data class Coordinates(val lat: Double, val lng: Double, val alt: Double? = null)

// Using a sealed interface for all GeoJSON objects
sealed interface GeoJsonObject {
    val type: String
}

// Sealed interface for Geometry objects
sealed interface GeoJsonGeometry : GeoJsonObject

data class GeoJsonPoint(
    val coordinates: Coordinates
) : GeoJsonGeometry {
    override val type: String = "Point"
}

data class GeoJsonMultiPoint(
    val coordinates: List<Coordinates>
) : GeoJsonGeometry {
    override val type: String = "MultiPoint"
}

data class GeoJsonLineString(
    val coordinates: List<Coordinates>
) : GeoJsonGeometry {
    override val type: String = "LineString"
}

data class GeoJsonMultiLineString(
    val coordinates: List<List<Coordinates>>
) : GeoJsonGeometry {
    override val type: String = "MultiLineString"
}

data class GeoJsonPolygon(
    val coordinates: List<List<Coordinates>>
) : GeoJsonGeometry {
    override val type: String = "Polygon"
}

data class GeoJsonMultiPolygon(
    val coordinates: List<List<List<Coordinates>>>
) : GeoJsonGeometry {
    override val type: String = "MultiPolygon"
}

data class GeoJsonGeometryCollection(
    val geometries: List<GeoJsonGeometry>
) : GeoJsonGeometry {
    override val type: String = "GeometryCollection"
}

data class GeoJsonFeature(
    val geometry: GeoJsonGeometry?,
    val properties: Map<String, String?>?,
    val id: String? = null // id is optional and can be string or number
) : GeoJsonObject {
    override val type: String = "Feature"
}

data class GeoJsonFeatureCollection(
    val features: List<GeoJsonFeature>
) : GeoJsonObject {
    override val type: String = "FeatureCollection"
}