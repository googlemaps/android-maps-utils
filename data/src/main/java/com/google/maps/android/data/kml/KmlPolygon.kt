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

import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.data.DataPolygon

/**
 * Represents a KML Polygon. Contains a single array of outer boundary coordinates and an array of
 * arrays for the inner boundary coordinates.
 */
@Deprecated("Use the new platform-agnostic data layer and renderer instead.")
public class KmlPolygon(
    private val outerBoundaryCoordinates: List<LatLng>,
    private val innerBoundaryCoordinates: List<List<LatLng>>?,
) : DataPolygon<List<List<LatLng>>> {
    init {
        requireNotNull(outerBoundaryCoordinates) { "Outer boundary coordinates cannot be null" }
    }

    override fun getGeometryType(): String = GEOMETRY_TYPE

    override fun getGeometryObject(): List<List<LatLng>> {
        val coordinates = ArrayList<List<LatLng>>()
        coordinates.add(outerBoundaryCoordinates)
        if (innerBoundaryCoordinates != null) {
            coordinates.addAll(innerBoundaryCoordinates)
        }
        return coordinates
    }

    override fun getOuterBoundaryCoordinates(): List<LatLng> = outerBoundaryCoordinates

    override fun getInnerBoundaryCoordinates(): List<List<LatLng>> = innerBoundaryCoordinates ?: emptyList()

    override fun toString(): String =
        StringBuilder(GEOMETRY_TYPE)
            .apply {
                append("{")
                append("\n outer coordinates=").append(outerBoundaryCoordinates)
                append(",\n inner coordinates=").append(innerBoundaryCoordinates)
                append("\n}\n")
            }.toString()

    companion object {
        public const val GEOMETRY_TYPE = "Polygon"
    }
}
