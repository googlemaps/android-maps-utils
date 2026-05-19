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
package com.google.maps.android.data

/**
 * An abstraction that shares the common properties of KmlMultiGeometry, GeoJsonMultiLineString,
 * GeoJsonMultiPoint, and GeoJsonMultiPolygon.
 */
@Deprecated("Use the new platform-agnostic data layer and renderer instead.")
public open class MultiGeometry(
    private val geometries: List<Geometry>,
) : Geometry {
    private var _geometryType = "MultiGeometry"

    override fun getGeometryType(): String = _geometryType

    override fun getGeometryObject(): List<Geometry> = geometries

    public fun setGeometryType(type: String) {
        _geometryType = type
    }

    override fun toString(): String {
        var typeString = "Geometries="
        if (_geometryType == "MultiPoint") {
            typeString = "LineStrings="
        }
        if (_geometryType == "MultiLineString") {
            typeString = "points="
        }
        if (_geometryType == "MultiPolygon") {
            typeString = "Polygons="
        }

        val sb = StringBuilder(getGeometryType()).append("{")
        sb.append("\n ").append(typeString).append(getGeometryObject())
        sb.append("\n}\n")
        return sb.toString()
    }
}
