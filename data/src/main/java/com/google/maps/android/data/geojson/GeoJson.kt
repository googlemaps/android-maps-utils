/*
 * Copyright 2026 Google LLC
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
 *
 */

@file:Suppress("NOTHING_TO_INLINE")
package com.google.maps.android.data.geojson

import android.content.Context
import androidx.annotation.IntegerRes
import androidx.annotation.RawRes
import com.google.android.gms.maps.GoogleMap
import com.google.maps.android.collections.GroundOverlayManager
import com.google.maps.android.collections.MarkerManager
import com.google.maps.android.collections.PolygonManager
import com.google.maps.android.collections.PolylineManager
import com.google.maps.android.data.geojson.GeoJsonLayer
import org.json.JSONObject

/**
 * Alias for the [GeoJsonLayer] constructor that provides Kotlin named parameters and default
 * values.
 */
public inline fun geoJsonLayer(
    map: GoogleMap,
    geoJsonFile: JSONObject,
    markerManager: MarkerManager? = null,
    polygonManager: PolygonManager? = null,
    polylineManager: PolylineManager? = null,
    groundOverlayManager: GroundOverlayManager? = null
): GeoJsonLayer = GeoJsonLayer(
    map,
    geoJsonFile,
    markerManager,
    polygonManager,
    polylineManager,
    groundOverlayManager
)

/**
 * Alias for the [GeoJsonLayer] constructor that provides Kotlin named parameters and default
 * values.
 */
public inline fun geoJsonLayer(
    map: GoogleMap,
    @RawRes resourceId: Int,
    context: Context,
    markerManager: MarkerManager? = null,
    polygonManager: PolygonManager? = null,
    polylineManager: PolylineManager? = null,
    groundOverlayManager: GroundOverlayManager? = null
): GeoJsonLayer = GeoJsonLayer(
    map,
    resourceId,
    context,
    markerManager,
    polygonManager,
    polylineManager,
    groundOverlayManager
)