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

/**
 * Decodes this encoded string into a [LatLng] list.
 *
 * @return the decoded [LatLng] list
 *
 * @see [Polyline Algorithm Format](https://developers.google.com/maps/documentation/utilities/polylinealgorithm)
 */
fun String.toLatLngList(): List<LatLng> = PolyUtil.decode(this)

/**
 * Encodes this [LatLng] list in a String using the <a href=""><M
 *
 * @return the encoded String
 *
 * @see [Polyline Algorithm Format](https://developers.google.com/maps/documentation/utilities/polylinealgorithm)
 */
fun List<LatLng>.latLngListEncode(): String = PolyUtil.encode(this)
