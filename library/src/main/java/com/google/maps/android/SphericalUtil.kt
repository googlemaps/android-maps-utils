/*
 * Copyright 2025 Google LLC
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

package com.google.maps.android

import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.data.Polygon
import com.google.maps.android.data.Polyline
import com.google.maps.android.MathUtil.EARTH_RADIUS
import com.google.maps.android.MathUtil.arcHav
import com.google.maps.android.MathUtil.havDistance
import com.google.maps.android.MathUtil.wrap
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.asin
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.math.tan

object SphericalUtil {
    /**
     * Returns the heading from one LatLng to another LatLng. Headings are
     * expressed in degrees clockwise from North within the range [-180,180).
     *
     * @return The heading in degrees clockwise from north.
     */
    @JvmStatic
    fun computeHeading(from: LatLng, to: LatLng): Double {
        // http://williams.best.vwh.net/avform.htm#Crs
        val fromLatRad = from.latitude.toRadians()
        val toLatRad = to.latitude.toRadians()
        val deltaLngRad = (to.longitude - from.longitude).toRadians()

        // Breaking the formula down into Y and X components for atan2().
        val y = sin(deltaLngRad) * cos(toLatRad)
        val x = cos(fromLatRad) * sin(toLatRad) -
                sin(fromLatRad) * cos(toLatRad) * cos(deltaLngRad)

        val headingRad = atan2(y, x)

        return wrap(headingRad.toDegrees(), -180.0, 180.0)
    }

    /**
     * Returns the LatLng resulting from moving a distance from an origin
     * in the specified heading (expressed in degrees clockwise from north).
     *
     * @param from     The LatLng from which to start.
     * @param distance The distance to travel.
     * @param heading  The heading in degrees clockwise from north.
     */
    @JvmStatic
    fun computeOffset(from: LatLng, distance: Double, heading: Double): LatLng {
        val distanceRad = distance / EARTH_RADIUS
        val headingRad = heading.toRadians()

        val (fromLatRad, fromLngRad) = from.toRadians()

        val cosDistance = cos(distanceRad)
        val sinDistance = sin(distanceRad)
        val sinFromLat = sin(fromLatRad)
        val cosFromLat = cos(fromLatRad)

        val sinToLat = cosDistance * sinFromLat + sinDistance * cosFromLat * cos(headingRad)
        val toLatRad = asin(sinToLat)

        val y = sin(headingRad) * sinDistance * cosFromLat
        val x = cosDistance - sinFromLat * sinToLat
        val dLngRad = atan2(y, x)

        val toLngRad = fromLngRad + dLngRad

        return LatLng(toLatRad.toDegrees(), toLngRad.toDegrees())
    }

    /**
     * Returns the location of origin when provided with a LatLng destination,
     * meters travelled and original heading. Headings are expressed in degrees
     * clockwise from North. This function returns null when no solution is
     * available.
     *
     * @param to       The destination LatLng.
     * @param distance The distance travelled, in meters.
     * @param heading  The heading in degrees clockwise from north.
     */
    @JvmStatic
    fun computeOffsetOrigin(to: LatLng, distance: Double, heading: Double): LatLng? {
        val headingRad = heading.toRadians()
        val distanceRad = distance / EARTH_RADIUS
        // http://lists.maptools.org/pipermail/proj/2008-October/003939.html
        val n1 = cos(distanceRad)
        val n2 = sin(distanceRad) * cos(headingRad)
        val n3 = sin(distanceRad) * sin(headingRad)
        val n4 = sin(to.latitude.toRadians())
        // There are two solutions for b. b = n2 * n4 +/- sqrt(), one solution results
        // in the latitude outside the [-90, 90] range. We first try one solution and
        // back off to the other if we are outside that range.
        val n12 = n1 * n1
        val discriminant = n2 * n2 * n12 + n12 * n12 - n12 * n4 * n4
        if (discriminant < 0) {
            // No real solution which would make sense in LatLng-space.
            return null
        }
        var b = n2 * n4 + sqrt(discriminant)
        b /= n1 * n1 + n2 * n2
        val a = (n4 - n2 * b) / n1
        var fromLatRadians = atan2(a, b)
        if (fromLatRadians < -PI / 2 || fromLatRadians > PI / 2) {
            b = n2 * n4 - sqrt(discriminant)
            b /= n1 * n1 + n2 * n2
            fromLatRadians = atan2(a, b)
        }
        if (fromLatRadians < -PI / 2 || fromLatRadians > PI / 2) {
            // No solution which would make sense in LatLng-space.
            return null
        }
        val fromLngRadians = to.longitude.toRadians() -
                atan2(n3, n1 * cos(fromLatRadians) - n2 * sin(fromLatRadians))
        return LatLng(fromLatRadians.toDegrees(), fromLngRadians.toDegrees())
    }

    /**
     * Returns the LatLng which lies the given fraction of the way between the
     * origin LatLng and the destination LatLng.
     *
     * @param from     The LatLng from which to start.
     * @param to       The LatLng toward which to travel.
     * @param fraction A fraction of the distance to travel.
     * @return The interpolated LatLng.
     */
    @JvmStatic
    fun interpolate(from: LatLng, to: LatLng, fraction: Double): LatLng {
        // http://en.wikipedia.org/wiki/Slerp
        val (fromLatRad, fromLngRad) = from.toRadians()
        val (toLatRad, toLngRad) = to.toRadians()

        val cosFromLat = cos(fromLatRad)
        val cosToLat = cos(toLatRad)

        // Computes Spherical interpolation coefficients.
        val angle = computeAngleBetween(from, to)
        val sinAngle = sin(angle)
        if (sinAngle < 1E-6) {
            // Fall back to linear interpolation for very small angles.
            return LatLng(
                from.latitude + fraction * (to.latitude - from.latitude),
                from.longitude + fraction * (to.longitude - from.longitude)
            )
        }
        val a = sin((1 - fraction) * angle) / sinAngle
        val b = sin(fraction * angle) / sinAngle

        // Converts from polar to vector and interpolate.
        val x = a * cosFromLat * cos(fromLngRad) + b * cosToLat * cos(toLngRad)
        val y = a * cosFromLat * sin(fromLngRad) + b * cosToLat * sin(toLngRad)
        val z = a * sin(fromLatRad) + b * sin(toLatRad)

        // Converts interpolated vector back to polar.
        val latRad = atan2(z, sqrt(x * x + y * y))
        val lngRad = atan2(y, x)

        return LatLng(latRad.toDegrees(), lngRad.toDegrees())
    }

    /**
     * Returns distance on the unit sphere; the arguments are in radians.
     */
    private fun distanceRadians(lat1: Double, lng1: Double, lat2: Double, lng2: Double) =
        arcHav(havDistance(lat1, lat2, lng1 - lng2))

    /**
     * Returns the angle between two [LatLng]s, in radians. This is the same as the distance
     * on the unit sphere.
     */
    @JvmStatic
    fun computeAngleBetween(from: LatLng, to: LatLng) = distanceRadians(
        from.latitude.toRadians(), from.longitude.toRadians(),
        to.latitude.toRadians(), to.longitude.toRadians()
    )

    /**
     * Returns the distance between two [LatLng]s, in meters.
     */
    @JvmStatic
    fun computeDistanceBetween(from: LatLng, to: LatLng) =
        computeAngleBetween(from, to) * EARTH_RADIUS

    /**
     * Returns the length of the given path, in meters, on Earth.
     */
    @JvmStatic
    fun computeLength(path: Polyline): Double {
        if (path.size < 2) {
            return 0.0
        }

        // Using zipWithNext() is a more functional and idiomatic way to handle
        // adjacent pairs in a collection. We then sum the distances between each pair.
        val totalDistance = path.zipWithNext().sumOf { (prev, point) ->
            val (prevLatRad, prevLngRad) = prev.toRadians()
            val (latRad, lngRad) = point.toRadians()
            distanceRadians(prevLatRad, prevLngRad, latRad, lngRad)
        }

        return totalDistance * EARTH_RADIUS
    }

    /**
     * Returns the area of a closed path on Earth.
     *
     * @param path A closed path.
     * @return The path's area in square meters.
     */
    @JvmStatic
    fun computeArea(path: Polygon) = abs(computeSignedArea(path))

    /**
     * Returns the signed area of a closed path on Earth. The sign of the area may be used to
     * determine the orientation of the path.
     * "inside" is the surface that does not contain the South Pole.
     *
     * @param path A closed path.
     * @return The loop's area in square meters.
     */
    @JvmStatic
    fun computeSignedArea(path: Polygon) = computeSignedArea(path, EARTH_RADIUS)


    /**
     * Returns the signed area of a closed path on a sphere of given radius.
     * The computed area uses the same units as the radius squared.
     * Used by SphericalUtilTest.
     */
    @JvmStatic
    fun computeSignedArea(path: List<LatLng>, radius: Double): Double {
        if (path.size < 3) {
            return 0.0
        }

        // This local function to keep the logic clean
        fun polarArea(p1: LatLng, p2: LatLng): Double {
            val tanLat1 = tan((PI / 2 - p1.latitude.toRadians()) / 2)
            val tanLat2 = tan((PI / 2 - p2.latitude.toRadians()) / 2)
            val lng1 = p1.longitude.toRadians()
            val lng2 = p2.longitude.toRadians()
            return polarTriangleArea(tanLat1, lng2, tanLat2, lng1)
        }

        // Use a sequence to avoid creating intermediate lists
        val totalArea = (path.asSequence().zipWithNext() + (path.last() to path.first()))
            .sumOf { (p1, p2) -> polarArea(p1, p2) }

        return totalArea * (radius * radius)
    }

    /**
     * Returns the signed area of a triangle which has North Pole as a vertex.
     * Formula derived from "Area of a spherical triangle given two edges and the included angle"
     * as per "Spherical Trigonometry" by Todhunter, page 71, section 103, point 2.
     * See http://books.google.com/books?id=3uBHAAAAIAAJ&pg=PA71
     * The arguments named "tan" are tan((pi/2 - latitude)/2).
     */
    private fun polarTriangleArea(tan1: Double, lng1: Double, tan2: Double, lng2: Double): Double {
        val deltaLng = lng1 - lng2
        val t = tan1 * tan2
        return 2 * atan2(t * sin(deltaLng), 1 + t * cos(deltaLng))
    }
}

/**
 * Helper extension function to convert a LatLng to a pair of radians.
 */
private fun LatLng.toRadians() = Pair(latitude.toRadians(), longitude.toRadians())

private fun Double.toRadians() = this * (PI / 180.0)
private fun Double.toDegrees() = this * (180.0 / PI)
