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
package com.google.maps.android

import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Polyline
import com.google.maps.android.PolyUtil
import com.google.maps.android.SphericalUtil

/**
 * Computes where the given [latLng] is contained on or near this Polyline within a specified
 * tolerance in meters.
 */
public inline fun Polyline.contains(latLng: LatLng, tolerance: Double = 0.1): Boolean =
    PolyUtil.isLocationOnPath(latLng, this.points, this.isGeodesic, tolerance)

/**
 * The spherical length of this Polyline on Earth as measured in meters.
 */
public inline val Polyline.sphericalPathLength: Double
    get() = SphericalUtil.computeLength(this.points)
