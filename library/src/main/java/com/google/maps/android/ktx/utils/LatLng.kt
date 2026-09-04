@file:Suppress("NOTHING_TO_INLINE")
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
 */
package com.google.maps.android.ktx.utils

import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.component1 as canonicalComponent1
import com.google.maps.android.component2 as canonicalComponent2
import com.google.maps.android.isLocationOnPath as canonicalIsLocationOnPath
import com.google.maps.android.isOnEdge as canonicalIsOnEdge
import com.google.maps.android.containsLocation as canonicalContainsLocation
import com.google.maps.android.simplify as canonicalSimplify
import com.google.maps.android.toLatLngList as canonicalToLatLngList
import com.google.maps.android.latLngListEncode as canonicalLatLngListEncode
import com.google.maps.android.isClosedPolygon as canonicalIsClosedPolygon
import com.google.maps.android.sphericalPathLength as canonicalSphericalPathLength
import com.google.maps.android.sphericalPolygonArea as canonicalSphericalPolygonArea
import com.google.maps.android.sphericalPolygonSignedArea as canonicalSphericalPolygonSignedArea
import com.google.maps.android.sphericalHeading as canonicalSphericalHeading
import com.google.maps.android.withSphericalOffset as canonicalWithSphericalOffset
import com.google.maps.android.computeSphericalOffsetOrigin as canonicalComputeSphericalOffsetOrigin
import com.google.maps.android.withSphericalLinearInterpolation as canonicalWithSphericalLinearInterpolation
import com.google.maps.android.sphericalDistance as canonicalSphericalDistance

@Deprecated("Moved to com.google.maps.android.component1", ReplaceWith("component1()", "com.google.maps.android.component1"))
public inline operator fun LatLng.component1(): Double = this.canonicalComponent1()

@Deprecated("Moved to com.google.maps.android.component2", ReplaceWith("component2()", "com.google.maps.android.component2"))
public inline operator fun LatLng.component2(): Double = this.canonicalComponent2()

@Deprecated("Moved to com.google.maps.android.isLocationOnPath", ReplaceWith("isLocationOnPath(latLng, geodesic, tolerance)", "com.google.maps.android.isLocationOnPath"))
public inline fun List<LatLng>.isLocationOnPath(latLng: LatLng, geodesic: Boolean, tolerance: Double = 0.1): Boolean = this.canonicalIsLocationOnPath(latLng, geodesic, tolerance)

@Deprecated("Moved to com.google.maps.android.isOnEdge", ReplaceWith("isOnEdge(latLng, geodesic, tolerance)", "com.google.maps.android.isOnEdge"))
public inline fun List<LatLng>.isOnEdge(latLng: LatLng, geodesic: Boolean, tolerance: Double = 0.1): Boolean = this.canonicalIsOnEdge(latLng, geodesic, tolerance)

@Deprecated("Moved to com.google.maps.android.containsLocation", ReplaceWith("containsLocation(latLng, geodesic)", "com.google.maps.android.containsLocation"))
public inline fun List<LatLng>.containsLocation(latLng: LatLng, geodesic: Boolean): Boolean = this.canonicalContainsLocation(latLng, geodesic)

@Deprecated("Moved to com.google.maps.android.simplify", ReplaceWith("simplify(tolerance)", "com.google.maps.android.simplify"))
public inline fun List<LatLng>.simplify(tolerance: Double): List<LatLng> = this.canonicalSimplify(tolerance)

@Deprecated("Moved to com.google.maps.android.toLatLngList", ReplaceWith("toLatLngList()", "com.google.maps.android.toLatLngList"))
public inline fun String.toLatLngList(): List<LatLng> = this.canonicalToLatLngList()

@Deprecated("Moved to com.google.maps.android.latLngListEncode", ReplaceWith("latLngListEncode()", "com.google.maps.android.latLngListEncode"))
public inline fun List<LatLng>.latLngListEncode(): String = this.canonicalLatLngListEncode()

@Deprecated("Moved to com.google.maps.android.isClosedPolygon", ReplaceWith("isClosedPolygon()", "com.google.maps.android.isClosedPolygon"))
public inline fun List<LatLng>.isClosedPolygon(): Boolean = this.canonicalIsClosedPolygon()

@Deprecated("Moved to com.google.maps.android.sphericalPathLength", ReplaceWith("sphericalPathLength()", "com.google.maps.android.sphericalPathLength"))
public inline fun List<LatLng>.sphericalPathLength(): Double = this.canonicalSphericalPathLength()

@Deprecated("Moved to com.google.maps.android.sphericalPolygonArea", ReplaceWith("sphericalPolygonArea()", "com.google.maps.android.sphericalPolygonArea"))
public inline fun List<LatLng>.sphericalPolygonArea(): Double = this.canonicalSphericalPolygonArea()

@Deprecated("Moved to com.google.maps.android.sphericalPolygonSignedArea", ReplaceWith("sphericalPolygonSignedArea()", "com.google.maps.android.sphericalPolygonSignedArea"))
public inline fun List<LatLng>.sphericalPolygonSignedArea(): Double = this.canonicalSphericalPolygonSignedArea()

@Deprecated("Moved to com.google.maps.android.sphericalHeading", ReplaceWith("sphericalHeading(toLatLng)", "com.google.maps.android.sphericalHeading"))
public inline fun LatLng.sphericalHeading(toLatLng: LatLng): Double = this.canonicalSphericalHeading(toLatLng)

@Deprecated("Moved to com.google.maps.android.withSphericalOffset", ReplaceWith("withSphericalOffset(distance, heading)", "com.google.maps.android.withSphericalOffset"))
public inline fun LatLng.withSphericalOffset(distance: Double, heading: Double): LatLng = this.canonicalWithSphericalOffset(distance, heading)

@Deprecated("Moved to com.google.maps.android.computeSphericalOffsetOrigin", ReplaceWith("computeSphericalOffsetOrigin(distance, heading)", "com.google.maps.android.computeSphericalOffsetOrigin"))
public inline fun LatLng.computeSphericalOffsetOrigin(distance: Double, heading: Double): LatLng? = this.canonicalComputeSphericalOffsetOrigin(distance, heading)

@Deprecated("Moved to com.google.maps.android.withSphericalLinearInterpolation", ReplaceWith("withSphericalLinearInterpolation(to, fraction)", "com.google.maps.android.withSphericalLinearInterpolation"))
public inline fun LatLng.withSphericalLinearInterpolation(to: LatLng, fraction: Double): LatLng = this.canonicalWithSphericalLinearInterpolation(to, fraction)

@Deprecated("Moved to com.google.maps.android.sphericalDistance", ReplaceWith("sphericalDistance(to)", "com.google.maps.android.sphericalDistance"))
public inline fun LatLng.sphericalDistance(to: LatLng): Double = this.canonicalSphericalDistance(to)
