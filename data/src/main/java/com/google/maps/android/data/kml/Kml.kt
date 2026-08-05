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
package com.google.maps.android.data.kml

import android.content.Context
import androidx.annotation.RawRes
import com.google.android.gms.maps.GoogleMap
import com.google.maps.android.collections.GroundOverlayManager
import com.google.maps.android.collections.MarkerManager
import com.google.maps.android.collections.PolygonManager
import com.google.maps.android.collections.PolylineManager
import com.google.maps.android.data.Renderer
import com.google.maps.android.data.kml.KmlLayer
import java.io.InputStream

/**
 * Alias for the [KmlLayer] constructor that provides Kotlin named parameters and default values.
 */
public inline fun kmlLayer(
    map: GoogleMap,
    @RawRes resourceId: Int,
    context: Context,
    markerManager: MarkerManager = MarkerManager(map),
    polygonManager: PolygonManager = PolygonManager(map),
    polylineManager: PolylineManager = PolylineManager(map),
    groundOverlayManager: GroundOverlayManager = GroundOverlayManager(map),
    imagesCache: Renderer.ImagesCache? = null
): KmlLayer = KmlLayer(
    map,
    resourceId,
    context,
    markerManager,
    polygonManager,
    polylineManager,
    groundOverlayManager,
    imagesCache
)

/**
 * Alias for the [KmlLayer] constructor that provides Kotlin named parameters and default values.
 */
public inline fun kmlLayer(
    map: GoogleMap,
    stream: InputStream,
    context: Context,
    markerManager: MarkerManager = MarkerManager(map),
    polygonManager: PolygonManager = PolygonManager(map),
    polylineManager: PolylineManager = PolylineManager(map),
    groundOverlayManager: GroundOverlayManager = GroundOverlayManager(map),
    imagesCache: Renderer.ImagesCache? = null
): KmlLayer = KmlLayer(
    map,
    stream,
    context,
    markerManager,
    polygonManager,
    polylineManager,
    groundOverlayManager,
    imagesCache
)
