package com.google.maps.ktx.core

import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Polygon
import com.google.maps.android.PolyUtil

/**
 * Computes whether or not [latLng] is contained within this Polygon.
 */
fun Polygon.contains(latLng: LatLng): Boolean =
    PolyUtil.containsLocation(latLng, this.points, this.isGeodesic)

/**
 * Checks whether or not [latLng] lies on or is near the edge of this Polygon within a tolerate
 * (in meters) of [tolerance]. The default value
 */
fun Polygon.isOnEdge(latLng: LatLng, tolerance: Double = PolyUtil.DEFAULT_TOLERANCE) =
    PolyUtil.isLocationOnEdge(latLng, this.points, this.isGeodesic, tolerance)
