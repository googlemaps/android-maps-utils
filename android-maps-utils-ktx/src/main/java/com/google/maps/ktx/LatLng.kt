package com.google.maps.ktx

import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.PolyUtil

/**
 * Simplifies this list of LatLng using the Douglas-Peucker decimation. Increasing the value of
 * [tolerance] will result in fewer points.
 *
 * @param tolerance the tolerance in meters.
 *
 * @see PolyUtil.simplify
 */
fun List<LatLng>.simplify(tolerance: Double): List<LatLng> = PolyUtil.simplify(this, tolerance)

