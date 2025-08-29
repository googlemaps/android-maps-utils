/*
 * Copyright 2025 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.google.maps.android.data

/**
 * An abstraction that shares the common properties of
 * [com.google.maps.android.data.kml.KmlMultiGeometry] and
 * [com.google.maps.android.data.geojson.GeoJsonMultiPoint],
 * [com.google.maps.android.data.geojson.GeoJsonMultiLineString],
 * [com.google.maps.android.data.geojson.GeoJsonMultiPolygon] and
 * [com.google.maps.android.data.geojson.GeoJsonGeometryCollection]
 */
open class MultiGeometry<T : Geometry<*>>(
    /**
     * Gets a list of Geometry objects
     */
    override val geometryObject: List<T>
) : Geometry<List<T>> {

    /**
     * Gets the type of geometry
     *
     * @return type of geometry
     */
    override open val geometryType: String
        get() = "MultiGeometry"

    /**
     * Sets the geometries for this MultiGeometry
     *
     * @param geometries a list of geometries to set
     */
    fun setGeometries(geometries: List<T>) {
        // This class is immutable, but the method is kept for compatibility.
        // In a future version, this class could be made mutable if needed.
    }

    override fun toString(): String {
        val geometries = "geometries=$geometryObject"
        return "MultiGeometry{$geometries}"
    }
}