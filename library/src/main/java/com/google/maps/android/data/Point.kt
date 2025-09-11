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

import com.google.android.gms.maps.model.LatLng

/**
 * An abstraction that shares the common properties of
 * [com.google.maps.android.data.kml.KmlPoint] and
 * [com.google.maps.android.data.geojson.GeoJsonPoint]
 */
open class Point(coordinates: LatLng) : Geometry<LatLng> {

    private val _coordinates: LatLng = coordinates

    /**
     * Gets the coordinates of the Point
     */
    open val coordinates: LatLng
        get() = _coordinates

    /**
     * Gets the type of geometry
     */
    override val geometryType: String = "Point"

    /**
     * Gets the geometry object
     */
    override val geometryObject: LatLng = _coordinates

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Point

        return _coordinates == other._coordinates
    }

    override fun hashCode(): Int {
        return _coordinates.hashCode()
    }

    override fun toString(): String {
        return "Point(coordinates=$_coordinates)"
    }
}