package com.google.maps.ktx

import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Polygon
import com.google.maps.android.PolyUtil

/**
 * Computes whether or not [latLng] is contained within this Polygon.
 *
 * @param latLng the LatLng to inspect
 * @return true if [latLng] is contained within this Polygon, otherwise, false
 *
 * @see PolyUtil.containsLocation
 */
fun Polygon.contains(latLng: LatLng): Boolean =
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
fun Polygon.isOnEdge(latLng: LatLng, tolerance: Double = PolyUtil.DEFAULT_TOLERANCE): Boolean =
    PolyUtil.isLocationOnEdge(latLng, this.points, this.isGeodesic, tolerance)
