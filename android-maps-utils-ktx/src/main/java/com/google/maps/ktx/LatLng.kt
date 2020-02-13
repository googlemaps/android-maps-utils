package com.google.maps.ktx

import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.PolyUtil
import com.google.maps.android.SphericalUtil

/**
 * Simplifies this list of LatLng using the Douglas-Peucker decimation. Increasing the value of
 * [tolerance] will result in fewer points.
 *
 * @param tolerance the tolerance in meters
 * @return the simplified list of [LatLng]
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
 * Encodes this [LatLng] list in a String using the
 * [Polyline Algorithm Format](https://developers.google.com/maps/documentation/utilities/polylinealgorithm).
 *
 * @return the encoded String
 *
 * @see [Polyline Algorithm Format](https://developers.google.com/maps/documentation/utilities/polylinealgorithm)
 *
 */
fun List<LatLng>.latLngListEncode(): String = PolyUtil.encode(this)

/**
 * Checks whether or not this [LatLng] list is a closed Polygon.
 *
 * @return true if this list is a closed Polygon, otherwise, false
 *
 * @see PolyUtil.isClosedPolygon
 */
fun List<LatLng>.isClosedPolygon(): Boolean = PolyUtil.isClosedPolygon(this)

/**
 * Computes the heading from this LatLng to [toLatLng].
 *
 * @param toLatLng the other LatLng to compute the heading to
 * @return the heading expressed in degrees clockwise from North within the range [-180, 180]
 *
 * @see SphericalUtil.computeHeading
 */
fun LatLng.sphericalHeading(toLatLng: LatLng): Double =
    SphericalUtil.computeHeading(this, toLatLng)

/**
 * Offsets this LatLng from the provided [distance] and [heading] and returns the result.
 *
 * @param distance the distance to offset by in meters
 * @param heading the heading to offset by in degrees clockwise from north
 * @return the resulting LatLng
 *
 * @see SphericalUtil.computeOffset
 */
fun LatLng.withSphericalOffset(distance: Double, heading: Double): LatLng =
    SphericalUtil.computeOffset(this, distance, heading)

/**
 * Attempts to compute the origin [LatLng] from this LatLng where [distance] meters have been
 * traveled with heading value [heading].
 *
 * @param distance the distance traveled from origin in meters
 * @param heading the heading from origin to this LatLng (measured in degrees clockwise from North)
 * @return the computed origin if a solution is available, otherwise, null
 *
 * @see SphericalUtil.computeOffsetOrigin
 */
fun LatLng.computeSphericalOffsetOrigin(distance: Double, heading: Double): LatLng? =
    SphericalUtil.computeOffsetOrigin(this, distance, heading)

/**
 * Returns an interpolated [LatLng] between this LatLng and [to] by the provided fractional value
 * [fraction].
 *
 * @param to the destination LatLng
 * @param fraction the fraction to interpolate by where the range is [0.0, 1.0]
 * @return the interpolated [LatLng]
 *
 * @see [Slerp](http://en.wikipedia.org/wiki/Slerp)
 */
fun LatLng.withSphericalLinearInterpolation(to: LatLng, fraction: Double): LatLng =
    SphericalUtil.interpolate(this, to, fraction)

/**
 * Computes the spherical distance between this LatLng and [to].
 *
 * @param to the LatLng to compute the distance to
 * @return the distance between this and [to] in meters
 */
fun LatLng.sphericalDistance(to: LatLng): Double =
    SphericalUtil.computeDistanceBetween(this, to)
