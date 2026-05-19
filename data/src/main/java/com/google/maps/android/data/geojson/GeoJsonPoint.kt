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
import com.google.maps.android.data.Point

/**
 * A GeoJsonPoint geometry contains a single LatLng.
 */
@Deprecated("Use the new platform-agnostic data layer and renderer instead.")
public class GeoJsonPoint
    @JvmOverloads
    constructor(
        coordinates: LatLng,
        private val altitude: Double? = null,
    ) : Point(coordinates) {
        public fun getType(): String = getGeometryType()

        public fun getCoordinates(): LatLng = getGeometryObject()

        public fun getAltitude(): Double? = altitude
    }
