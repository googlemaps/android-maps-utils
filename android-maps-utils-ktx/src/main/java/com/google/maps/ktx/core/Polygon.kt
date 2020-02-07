package com.google.maps.ktx.core

import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Polygon
import com.google.maps.android.PolyUtil

/**
 * Computes whether or not [latLng] is contained within this Polygon.
 */
fun Polygon.contains(latLng: LatLng, isGeodesic: Boolean): Boolean =
    PolyUtil.containsLocation(latLng, this.points, isGeodesic)