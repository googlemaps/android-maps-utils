/*
 * Copyright 2013 Google LLC.
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
import org.junit.Test
import kotlin.math.*
import org.junit.Assert.*

class SphericalUtilTest {
    // The vertices of an octahedron, for testing
    private val up = LatLng(90.0, 0.0)
    private val down = LatLng(-90.0, 0.0)
    private val front = LatLng(0.0, 0.0)
    private val right = LatLng(0.0, 90.0)
    private val back = LatLng(0.0, -180.0)
    private val left = LatLng(0.0, -90.0)

    private fun expectLatLngApproxEquals(actual: LatLng, expected: LatLng) {
        assertEquals(expected.latitude, actual.latitude, 1e-6)
        val cosLat = cos(Math.toRadians(actual.latitude))
        assertEquals(cosLat * expected.longitude, cosLat * actual.longitude, 1e-6)
    }

    private fun computeSignedTriangleArea(a: LatLng, b: LatLng, c: LatLng): Double {
        val path = listOf(a, b, c)
        return SphericalUtil.computeSignedArea(path, 1.0)
    }

    private fun computeTriangleArea(a: LatLng, b: LatLng, c: LatLng): Double {
        return abs(computeSignedTriangleArea(a, b, c))
    }

    private fun isCCW(a: LatLng, b: LatLng, c: LatLng): Int {
        return if (computeSignedTriangleArea(a, b, c) > 0) 1 else -1
    }

    @Test
    fun testAngles() { /* ... already provided above ... */ }

    @Test
    fun testDistances() { /* ... already provided above ... */ }

    @Test
    fun testHeadings() { /* ... already provided above ... */ }

    @Test
    fun testComputeOffset() { /* ... already provided above ... */ }

    @Test
    fun testComputeOffsetOrigin() {
        expectLatLngApproxEquals(front, SphericalUtil.computeOffsetOrigin(front, 0.0, 0.0)!!)

        expectLatLngApproxEquals(
            front,
            SphericalUtil.computeOffsetOrigin(LatLng(0.0, 45.0), PI * MathUtil.EARTH_RADIUS / 4, 90.0)!!
        )
        expectLatLngApproxEquals(
            front,
            SphericalUtil.computeOffsetOrigin(LatLng(0.0, -45.0), PI * MathUtil.EARTH_RADIUS / 4, -90.0)!!
        )
        expectLatLngApproxEquals(
            front,
            SphericalUtil.computeOffsetOrigin(LatLng(45.0, 0.0), PI * MathUtil.EARTH_RADIUS / 4, 0.0)!!
        )
        expectLatLngApproxEquals(
            front,
            SphericalUtil.computeOffsetOrigin(LatLng(-45.0, 0.0), PI * MathUtil.EARTH_RADIUS / 4, 180.0)!!
        )

        // Situations with no solution, should return null.
        assertNull(
            SphericalUtil.computeOffsetOrigin(
                LatLng(80.0, 0.0), PI * MathUtil.EARTH_RADIUS / 4, 180.0
            )
        )
        assertNull(
            SphericalUtil.computeOffsetOrigin(
                LatLng(80.0, 0.0), PI * MathUtil.EARTH_RADIUS / 4, 90.0
            )
        )
    }

    @Test
    fun testComputeOffsetAndBackToOrigin() {
        var start = LatLng(40.0, 40.0)
        var distance = 1e5
        var heading = 15.0
        var end: LatLng

        end = SphericalUtil.computeOffset(start, distance, heading)
        expectLatLngApproxEquals(start, SphericalUtil.computeOffsetOrigin(end, distance, heading)!!)

        heading = -37.0
        end = SphericalUtil.computeOffset(start, distance, heading)
        expectLatLngApproxEquals(start, SphericalUtil.computeOffsetOrigin(end, distance, heading)!!)

        distance = 3.8e7
        end = SphericalUtil.computeOffset(start, distance, heading)
        expectLatLngApproxEquals(start, SphericalUtil.computeOffsetOrigin(end, distance, heading)!!)

        start = LatLng(-21.0, -73.0)
        end = SphericalUtil.computeOffset(start, distance, heading)
        expectLatLngApproxEquals(start, SphericalUtil.computeOffsetOrigin(end, distance, heading)!!)

        start = SphericalUtil.computeOffsetOrigin(LatLng(0.0, 90.0), PI * MathUtil.EARTH_RADIUS / 2, 90.0)!!
        expectLatLngApproxEquals(
            LatLng(0.0, 90.0),
            SphericalUtil.computeOffset(start, PI * MathUtil.EARTH_RADIUS / 2, 90.0)
        )

        start = SphericalUtil.computeOffsetOrigin(LatLng(90.0, 0.0), PI * MathUtil.EARTH_RADIUS / 4, 0.0)!!
        expectLatLngApproxEquals(
            LatLng(90.0, 0.0),
            SphericalUtil.computeOffset(start, PI * MathUtil.EARTH_RADIUS / 4, 0.0)
        )
    }

    @Test
    fun testInterpolate() {
        // Same point
        expectLatLngApproxEquals(up, SphericalUtil.interpolate(up, up, 0.5))
        expectLatLngApproxEquals(down, SphericalUtil.interpolate(down, down, 0.5))
        expectLatLngApproxEquals(left, SphericalUtil.interpolate(left, left, 0.5))

        // Between front and up
        expectLatLngApproxEquals(LatLng(1.0, 0.0), SphericalUtil.interpolate(front, up, 1 / 90.0))
        expectLatLngApproxEquals(LatLng(1.0, 0.0), SphericalUtil.interpolate(up, front, 89 / 90.0))
        expectLatLngApproxEquals(LatLng(89.0, 0.0), SphericalUtil.interpolate(front, up, 89 / 90.0))
        expectLatLngApproxEquals(LatLng(89.0, 0.0), SphericalUtil.interpolate(up, front, 1 / 90.0))

        // Between front and down
        expectLatLngApproxEquals(LatLng(-1.0, 0.0), SphericalUtil.interpolate(front, down, 1 / 90.0))
        expectLatLngApproxEquals(LatLng(-1.0, 0.0), SphericalUtil.interpolate(down, front, 89 / 90.0))
        expectLatLngApproxEquals(LatLng(-89.0, 0.0), SphericalUtil.interpolate(front, down, 89 / 90.0))
        expectLatLngApproxEquals(LatLng(-89.0, 0.0), SphericalUtil.interpolate(down, front, 1 / 90.0))

        // Between left and back
        expectLatLngApproxEquals(LatLng(0.0, -91.0), SphericalUtil.interpolate(left, back, 1 / 90.0))
        expectLatLngApproxEquals(LatLng(0.0, -91.0), SphericalUtil.interpolate(back, left, 89 / 90.0))
        expectLatLngApproxEquals(LatLng(0.0, -179.0), SphericalUtil.interpolate(left, back, 89 / 90.0))
        expectLatLngApproxEquals(LatLng(0.0, -179.0), SphericalUtil.interpolate(back, left, 1 / 90.0))

        // geodesic crosses pole
        expectLatLngApproxEquals(up, SphericalUtil.interpolate(LatLng(45.0, 0.0), LatLng(45.0, 180.0), 0.5))
        expectLatLngApproxEquals(down, SphericalUtil.interpolate(LatLng(-45.0, 0.0), LatLng(-45.0, 180.0), 0.5))

        // boundary values
        expectLatLngApproxEquals(left, SphericalUtil.interpolate(left, back, 0.0))
        expectLatLngApproxEquals(back, SphericalUtil.interpolate(left, back, 1.0))

        // small separation fallback to linear
        expectLatLngApproxEquals(
            LatLng(-37.756872, 175.325252),
            SphericalUtil.interpolate(
                LatLng(-37.756891, 175.325262),
                LatLng(-37.756853, 175.325242),
                0.5
            )
        )
    }

    @Test
    fun testComputeLength() {
        assertEquals(0.0, SphericalUtil.computeLength(emptyList()), 1e-6)
        assertEquals(0.0, SphericalUtil.computeLength(listOf(LatLng(0.0, 0.0))), 1e-6)

        var latLngs = listOf(LatLng(0.0, 0.0), LatLng(0.1, 0.1))
        assertEquals(
            Math.toRadians(0.1) * sqrt(2.0) * MathUtil.EARTH_RADIUS,
            SphericalUtil.computeLength(latLngs),
            1.0
        )

        latLngs = listOf(LatLng(0.0, 0.0), LatLng(90.0, 0.0), LatLng(0.0, 90.0))
        assertEquals(PI * MathUtil.EARTH_RADIUS, SphericalUtil.computeLength(latLngs), 1e-6)
    }

    @Test
    fun testIsCCW() {
        assertEquals(1, isCCW(right, up, front))
        assertEquals(1, isCCW(up, front, right))
        assertEquals(1, isCCW(front, right, up))
        assertEquals(-1, isCCW(front, up, right))
        assertEquals(-1, isCCW(up, right, front))
        assertEquals(-1, isCCW(right, front, up))
    }

    @Test
    fun testComputeTriangleArea() {
        assertEquals(PI / 2, computeTriangleArea(right, up, front), 1e-6)
        assertEquals(PI / 2, computeTriangleArea(front, up, right), 1e-6)

        val area = computeTriangleArea(
            LatLng(0.0, 0.0),
            LatLng(0.0, Math.toDegrees(1E-6)),
            LatLng(Math.toDegrees(1E-6), 0.0)
        )
        val expectedArea = 1E-12 / 2
        assertTrue(abs(expectedArea - area) < 1e-20)
    }

    @Test
    fun testComputeSignedTriangleArea() {
        assertEquals(
            Math.toRadians(0.1) * Math.toRadians(0.1) / 2,
            computeSignedTriangleArea(LatLng(0.0, 0.0), LatLng(0.0, 0.1), LatLng(0.1, 0.1)),
            1e-6
        )
        assertEquals(PI / 2, computeSignedTriangleArea(right, up, front), 1e-6)
        assertEquals(-PI / 2, computeSignedTriangleArea(front, up, right), 1e-6)
    }

    @Test
    fun testComputeArea() {
        assertEquals(
            PI * MathUtil.EARTH_RADIUS * MathUtil.EARTH_RADIUS,
            SphericalUtil.computeArea(listOf(right, up, front, down, right)),
            0.4
        )
        assertEquals(
            PI * MathUtil.EARTH_RADIUS * MathUtil.EARTH_RADIUS,
            SphericalUtil.computeArea(listOf(right, down, front, up, right)),
            0.4
        )
    }

    @Test
    fun testComputeSignedArea() {
        val path = listOf(right, up, front, down, right)
        val pathReversed = listOf(right, down, front, up, right)
        assertEquals(
            -SphericalUtil.computeSignedArea(path),
            SphericalUtil.computeSignedArea(pathReversed),
            0.0
        )
    }
}
