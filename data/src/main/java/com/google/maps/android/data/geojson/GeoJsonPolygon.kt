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
package com.google.maps.android.data.geojson

import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.data.DataPolygon

/**
 * A GeoJsonPolygon geometry contains an array of arrays of LatLngs.
 * The first array is the polygon exterior boundary. Subsequent arrays are holes.
 */
@Deprecated("Use the new platform-agnostic data layer and renderer instead.")
public class GeoJsonPolygon(
    private val coordinates: List<List<LatLng>>,
) : DataPolygon<LatLng> {
    init {
        requireNotNull(coordinates) { "Coordinates cannot be null" }
    }

    public fun getType(): String = GEOMETRY_TYPE

    public fun getCoordinates(): List<List<LatLng>> = coordinates

    override fun getGeometryObject(): List<List<LatLng>> = getCoordinates()

    override fun getGeometryType(): String = getType()

    override fun getOuterBoundaryCoordinates(): List<LatLng> = coordinates[POLYGON_OUTER_COORDINATE_INDEX]

    override fun getInnerBoundaryCoordinates(): List<List<LatLng>> {
        val innerBoundary = ArrayList<List<LatLng>>()
        for (i in POLYGON_INNER_COORDINATE_INDEX until coordinates.size) {
            innerBoundary.add(coordinates[i])
        }
        return innerBoundary
    }

    override fun toString(): String {
        val sb = StringBuilder(GEOMETRY_TYPE).append("{")
        sb.append("\n coordinates=").append(coordinates)
        sb.append("\n}\n")
        return sb.toString()
    }

    companion object {
        private const val GEOMETRY_TYPE = "Polygon"
        private const val POLYGON_OUTER_COORDINATE_INDEX = 0
        private const val POLYGON_INNER_COORDINATE_INDEX = 1
    }
}
