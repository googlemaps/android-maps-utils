package com.google.maps.ktx

import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Polyline
import com.google.maps.android.PolyUtil

/**
 * Computes where the given [latLng] is contained on or near this Polyline within a specified
 * tolerance in meters.
 */
fun Polyline.contains(latLng: LatLng, tolerance: Double = PolyUtil.DEFAULT_TOLERANCE): Boolean =
    PolyUtil.isLocationOnPath(latLng, this.points, this.isGeodesic, tolerance)
