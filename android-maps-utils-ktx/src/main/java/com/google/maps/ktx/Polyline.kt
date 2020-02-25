package com.google.maps.ktx

import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Polyline
import com.google.maps.android.PolyUtil
import com.google.maps.android.SphericalUtil

/**
 * Computes where the given [latLng] is contained on or near this Polyline within a specified
 * tolerance in meters.
 */
inline fun Polyline.contains(latLng: LatLng, tolerance: Double = PolyUtil.DEFAULT_TOLERANCE): Boolean =
    PolyUtil.isLocationOnPath(latLng, this.points, this.isGeodesic, tolerance)

/**
 * The spherical length of this Polyline on Earth as measured in meters.
 */
inline val Polyline.sphericalPathLength: Double
    get() = SphericalUtil.computeLength(this.points)
