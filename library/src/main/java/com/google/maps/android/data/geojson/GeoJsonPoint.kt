/*
 * Copyright 2020 Google Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.google.maps.android.data.geojson

import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.data.Point

/**
 * A GeoJsonPoint geometry contains a single [com.google.android.gms.maps.model.LatLng].
 */
class GeoJsonPoint
@JvmOverloads
/**
 * Creates a new GeoJsonPoint
 *
 * @param coordinates coordinates of the KmlPoint
 * @param altitude    altitude of the KmlPoint
 */
constructor(
    val coordinates: LatLng,
    /**
     * Gets the altitude of the GeoJsonPoint
     *
     * @return altitude of the GeoJsonPoint
     */
    val altitude: Double? = null
) : Point(coordinates) {

    val type: String
        /**
         * Gets the type of geometry. The type of geometry conforms to the GeoJSON 'type'
         * specification.
         *
         * @return type of geometry
         */
        get() = getGeometryType()
}
