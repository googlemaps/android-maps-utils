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
        val fromLat = Math.toRadians(from.latitude)
        val fromLng = Math.toRadians(from.longitude)
        val toLat = Math.toRadians(to.latitude)
        val toLng = Math.toRadians(to.longitude)
        val dLng = toLng - fromLng
        val heading = atan2(
            sin(dLng) * cos(toLat),
            cos(fromLat) * sin(toLat) - sin(fromLat) * cos(toLat) * cos(dLng)
        )
        return wrap(Math.toDegrees(heading), -180.0, 180.0)
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
        var distance = distance
        var heading = heading
        distance /= EARTH_RADIUS
        heading = Math.toRadians(heading)
        // http://williams.best.vwh.net/avform.htm#LL
        val fromLat = Math.toRadians(from.latitude)
        val fromLng = Math.toRadians(from.longitude)
        val cosDistance = cos(distance)
        val sinDistance = sin(distance)
        val sinFromLat = sin(fromLat)
        val cosFromLat = cos(fromLat)
        val sinLat = cosDistance * sinFromLat + sinDistance * cosFromLat * cos(heading)
        val dLng = atan2(
            sinDistance * cosFromLat * sin(heading),
            cosDistance - sinFromLat * sinLat
        )
        return LatLng(Math.toDegrees(asin(sinLat)), Math.toDegrees(fromLng + dLng))
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
        var distance = distance
        var heading = heading
        heading = Math.toRadians(heading)
        distance /= EARTH_RADIUS
        // http://lists.maptools.org/pipermail/proj/2008-October/003939.html
        val n1 = cos(distance)
        val n2 = sin(distance) * cos(heading)
        val n3 = sin(distance) * sin(heading)
        val n4 = sin(Math.toRadians(to.latitude))
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
        val fromLngRadians = Math.toRadians(to.longitude) -
                atan2(n3, n1 * cos(fromLatRadians) - n2 * sin(fromLatRadians))
        return LatLng(Math.toDegrees(fromLatRadians), Math.toDegrees(fromLngRadians))
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
        val fromLat = Math.toRadians(from.latitude)
        val fromLng = Math.toRadians(from.longitude)
        val toLat = Math.toRadians(to.latitude)
        val toLng = Math.toRadians(to.longitude)
        val cosFromLat = cos(fromLat)
        val cosToLat = cos(toLat)

        // Computes Spherical interpolation coefficients.
        val angle = computeAngleBetween(from, to)
        val sinAngle = sin(angle)
        if (sinAngle < 1E-6) {
            return LatLng(
                from.latitude + fraction * (to.latitude - from.latitude),
                from.longitude + fraction * (to.longitude - from.longitude)
            )
        }
        val a = sin((1 - fraction) * angle) / sinAngle
        val b = sin(fraction * angle) / sinAngle

        // Converts from polar to vector and interpolate.
        val x = a * cosFromLat * cos(fromLng) + b * cosToLat * cos(toLng)
        val y = a * cosFromLat * sin(fromLng) + b * cosToLat * sin(toLng)
        val z = a * sin(fromLat) + b * sin(toLat)

        // Converts interpolated vector back to polar.
        val lat = atan2(z, sqrt(x * x + y * y))
        val lng = atan2(y, x)
        return LatLng(Math.toDegrees(lat), Math.toDegrees(lng))
    }

    /**
     * Returns distance on the unit sphere; the arguments are in radians.
     */
    private fun distanceRadians(lat1: Double, lng1: Double, lat2: Double, lng2: Double): Double {
        return arcHav(havDistance(lat1, lat2, lng1 - lng2))
    }

    /**
     * Returns the angle between two LatLngs, in radians. This is the same as the distance
     * on the unit sphere.
     */
    @JvmStatic
    fun computeAngleBetween(from: LatLng, to: LatLng): Double {
        return distanceRadians(
            Math.toRadians(from.latitude), Math.toRadians(from.longitude),
            Math.toRadians(to.latitude), Math.toRadians(to.longitude)
        )
    }

    /**
     * Returns the distance between two LatLngs, in meters.
     */
    @JvmStatic
    fun computeDistanceBetween(from: LatLng, to: LatLng): Double {
        return computeAngleBetween(from, to) * EARTH_RADIUS
    }

    /**
     * Returns the length of the given path, in meters, on Earth.
     */
    @JvmStatic
    fun computeLength(path: List<LatLng>): Double {
        if (path.size < 2) {
            return 0.0
        }
        var length = 0.0
        var prev: LatLng? = null
        for (point in path) {
            if (prev != null) {
                val prevLat = Math.toRadians(prev.latitude)
                val prevLng = Math.toRadians(prev.longitude)
                val lat = Math.toRadians(point.latitude)
                val lng = Math.toRadians(point.longitude)
                length += distanceRadians(prevLat, prevLng, lat, lng)
            }
            prev = point
        }
        return length * EARTH_RADIUS
    }

    /**
     * Returns the area of a closed path on Earth.
     *
     * @param path A closed path.
     * @return The path's area in square meters.
     */
    @JvmStatic
    fun computeArea(path: List<LatLng>): Double {
        return abs(computeSignedArea(path))
    }

    /**
     * Returns the signed area of a closed path on Earth. The sign of the area may be used to
     * determine the orientation of the path.
     * "inside" is the surface that does not contain the South Pole.
     *
     * @param path A closed path.
     * @return The loop's area in square meters.
     */
    @JvmStatic
    fun computeSignedArea(path: List<LatLng>): Double {
        return computeSignedArea(path, EARTH_RADIUS)
    }

    /**
     * Returns the signed area of a closed path on a sphere of given radius.
     * The computed area uses the same units as the radius squared.
     * Used by SphericalUtilTest.
     */
    @JvmStatic
    fun computeSignedArea(path: List<LatLng>, radius: Double): Double {
        val size = path.size
        if (size < 3) {
            return 0.0
        }
        var total = 0.0
        val prev = path[size - 1]
        var prevTanLat = tan((PI / 2 - Math.toRadians(prev.latitude)) / 2)
        var prevLng = Math.toRadians(prev.longitude)
        // For each edge, accumulate the signed area of the triangle formed by the North Pole
        // and that edge ("polar triangle").
        for (point in path) {
            val tanLat = tan((PI / 2 - Math.toRadians(point.latitude)) / 2)
            val lng = Math.toRadians(point.longitude)
            total += polarTriangleArea(tanLat, lng, prevTanLat, prevLng)
            prevTanLat = tanLat
            prevLng = lng
        }
        return total * (radius * radius)
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