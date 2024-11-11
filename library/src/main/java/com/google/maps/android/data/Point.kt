/*
 * Copyright 2024 Google Inc.
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
 * [KmlPoint][com.google.maps.android.data.kml.KmlPoint] and
 * [GeoJsonPoint][com.google.maps.android.data.geojson.GeoJsonPoint]
 */
open class Point(private val coordinates: LatLng) : Geometry<LatLng> {
    /**
     * Gets the type of geometry
     *
     * @return type of geometry
     */
    override fun getGeometryType(): String {
        return GEOMETRY_TYPE
    }

    /**
     * Gets the coordinates of the Point
     *
     * @return coordinates of the Point
     */
    override fun getGeometryObject(): LatLng {
        return coordinates
    }

    override fun toString(): String {
        val sb = StringBuilder(GEOMETRY_TYPE).append("{")
        sb.append("\n coordinates=").append(coordinates)
        sb.append("\n}\n")
        return sb.toString()
    }

    companion object {
        private const val GEOMETRY_TYPE = "Point"
    }
}
