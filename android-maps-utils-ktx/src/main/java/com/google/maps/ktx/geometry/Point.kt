package com.google.maps.ktx.geometry

import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.geometry.Point
import com.google.maps.android.projection.SphericalMercatorProjection

/**
 * Returns the x value of this Point.
 *
 * e.g.
 *
 * ```
 * val (x, _) = point
 * ```
 */
inline operator fun Point.component1() = this.x

/**
 * Returns the y value of this Point.
 *
 * e.g.
 *
 * ```
 * val (_, y) = point
 */
inline operator fun Point.component2() = this.y

/**
 * Converts this Point to a [LatLng] given the [SphericalMercatorProjection] [projection].
 *
 * @param projection the [SphericalMercatorProjection]
 * @return this LatLng represented as a Point in the provided [projection]
 */
inline fun Point.toLatLng(projection: SphericalMercatorProjection): LatLng =
    projection.toLatLng(this)