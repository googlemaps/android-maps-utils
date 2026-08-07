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
import com.google.android.gms.maps.model.Polygon
import com.google.maps.android.PolyUtil
import com.google.maps.android.SphericalUtil

/**
 * Computes whether or not [latLng] is contained within this Polygon.
 *
 * @param latLng the LatLng to inspect
 * @return true if [latLng] is contained within this Polygon, otherwise, false
 *
 * @see PolyUtil.containsLocation
 */
public inline fun Polygon.contains(latLng: LatLng): Boolean =
    PolyUtil.containsLocation(latLng, this.points, this.isGeodesic)

/**
 * Checks whether or not [latLng] lies on or is near the edge of this Polygon within a tolerance
 * (in meters) of [tolerance]. The default value is [PolyUtil.DEFAULT_TOLERANCE].
 *
 * @param latLng the LatLng to inspect
 * @param tolerance the tolerance in meters
 * @return true if [latLng] lies on or is near the edge of this Polygon, otherwise, false
 *
 * @see PolyUtil.isLocationOnEdge
 */
public inline fun Polygon.isOnEdge(latLng: LatLng, tolerance: Double = 0.1): Boolean =
    PolyUtil.isLocationOnEdge(latLng, this.points, this.isGeodesic, tolerance)

/**
 * The area of this Polygon on Earth in square meters.
 */
public inline val Polygon.area: Double
    get() = SphericalUtil.computeArea(this.points)

/**
 * Computes the signed area under a closed path on Earth. The sign of the area may be used to
 * determine the orientation of the path.
 */
public inline val Polygon.signedArea: Double
    get() = SphericalUtil.computeSignedArea(this.points)
