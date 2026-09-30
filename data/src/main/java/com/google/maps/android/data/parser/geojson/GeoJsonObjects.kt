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
public data class Coordinates(public val lat: Double, public val lng: Double, public val alt: Double? = null) {
    init {
        require(lat.isFinite() && lng.isFinite() && (alt == null || alt.isFinite())) {
            "GeoJSON coordinate contains a non-finite value"
        }
    }
}

// Using a sealed interface for all GeoJSON objects
public sealed interface GeoJsonObject {
    public val type: String
}

// Sealed interface for Geometry objects
public sealed interface GeoJsonGeometry : GeoJsonObject

public data class GeoJsonPoint(
    public val coordinates: Coordinates
) : GeoJsonGeometry {
    public override val type: String = "Point"
}

public data class GeoJsonMultiPoint(
    public val coordinates: List<Coordinates>
) : GeoJsonGeometry {
    public override val type: String = "MultiPoint"
}

public data class GeoJsonLineString(
    public val coordinates: List<Coordinates>
) : GeoJsonGeometry {
    public override val type: String = "LineString"
}

public data class GeoJsonMultiLineString(
    public val coordinates: List<List<Coordinates>>
) : GeoJsonGeometry {
    public override val type: String = "MultiLineString"
}

public data class GeoJsonPolygon(
    public val coordinates: List<List<Coordinates>>
) : GeoJsonGeometry {
    public override val type: String = "Polygon"
}

public data class GeoJsonMultiPolygon(
    public val coordinates: List<List<List<Coordinates>>>
) : GeoJsonGeometry {
    public override val type: String = "MultiPolygon"
}

public data class GeoJsonGeometryCollection(
    public val geometries: List<GeoJsonGeometry>
) : GeoJsonGeometry {
    public override val type: String = "GeometryCollection"
}

public data class GeoJsonFeature(
    public val geometry: GeoJsonGeometry?,
    public val properties: Map<String, String?>?,
    public val id: String? = null // id is optional and can be string or number
) : GeoJsonObject {
    public override val type: String = "Feature"
}

public data class GeoJsonFeatureCollection(
    public val features: List<GeoJsonFeature>
) : GeoJsonObject {
    public override val type: String = "FeatureCollection"
}