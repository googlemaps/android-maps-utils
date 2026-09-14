/*
 * Copyright 2026 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.google.maps.android

import com.google.maps.android.MathUtil.EARTH_RADIUS
import com.google.maps.android.model.LatLng
import com.google.maps.android.model.latitude
import com.google.maps.android.model.longitude
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sqrt
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class SphericalUtilTest {
    // The vertices of an octahedron, for testing
    private val up = LatLng(90.0, 0.0)
    private val down = LatLng(-90.0, 0.0)
    private val front = LatLng(0.0, 0.0)
    private val right = LatLng(0.0, 90.0)
    private val back = LatLng(0.0, -180.0)
    private val left = LatLng(0.0, -90.0)

    @Test
    fun testAngles() {
        // Same vertex
        assertEquals(0.0, SphericalUtil.computeAngleBetween(up, up), 1e-6)
        assertEquals(0.0, SphericalUtil.computeAngleBetween(down, down), 1e-6)
        assertEquals(0.0, SphericalUtil.computeAngleBetween(left, left), 1e-6)
        assertEquals(0.0, SphericalUtil.computeAngleBetween(right, right), 1e-6)
        assertEquals(0.0, SphericalUtil.computeAngleBetween(front, front), 1e-6)
        assertEquals(0.0, SphericalUtil.computeAngleBetween(back, back), 1e-6)

        // Adjacent vertices
        assertEquals(PI / 2, SphericalUtil.computeAngleBetween(up, front), 1e-6)
        assertEquals(PI / 2, SphericalUtil.computeAngleBetween(up, right), 1e-6)
        assertEquals(PI / 2, SphericalUtil.computeAngleBetween(up, back), 1e-6)
        assertEquals(PI / 2, SphericalUtil.computeAngleBetween(up, left), 1e-6)

        assertEquals(PI / 2, SphericalUtil.computeAngleBetween(down, front), 1e-6)
        assertEquals(PI / 2, SphericalUtil.computeAngleBetween(down, right), 1e-6)
        assertEquals(PI / 2, SphericalUtil.computeAngleBetween(down, back), 1e-6)
        assertEquals(PI / 2, SphericalUtil.computeAngleBetween(down, left), 1e-6)

        assertEquals(PI / 2, SphericalUtil.computeAngleBetween(back, up), 1e-6)
        assertEquals(PI / 2, SphericalUtil.computeAngleBetween(back, right), 1e-6)
        assertEquals(PI / 2, SphericalUtil.computeAngleBetween(back, down), 1e-6)
        assertEquals(PI / 2, SphericalUtil.computeAngleBetween(back, left), 1e-6)

        // Opposite vertices
        assertEquals(PI, SphericalUtil.computeAngleBetween(up, down), 1e-6)
        assertEquals(PI, SphericalUtil.computeAngleBetween(front, back), 1e-6)
        assertEquals(PI, SphericalUtil.computeAngleBetween(left, right), 1e-6)
    }

    @Test
    fun testDistances() {
        assertEquals(PI * EARTH_RADIUS, SphericalUtil.computeDistanceBetween(up, down), 1e-6)
    }

    @Test
    fun testHeadings() {
        // Opposing vertices for which there is a result
        assertEquals(-180.0, SphericalUtil.computeHeading(up, down), 1e-6)
        assertEquals(0.0, SphericalUtil.computeHeading(down, up), 1e-6)

        // Adjacent vertices for which there is a result
        assertEquals(0.0, SphericalUtil.computeHeading(front, up), 1e-6)
        assertEquals(0.0, SphericalUtil.computeHeading(right, up), 1e-6)
        assertEquals(0.0, SphericalUtil.computeHeading(back, up), 1e-6)
        assertEquals(0.0, SphericalUtil.computeHeading(down, up), 1e-6)

        assertEquals(-180.0, SphericalUtil.computeHeading(front, down), 1e-6)
        assertEquals(-180.0, SphericalUtil.computeHeading(right, down), 1e-6)
        assertEquals(-180.0, SphericalUtil.computeHeading(back, down), 1e-6)
        assertEquals(-180.0, SphericalUtil.computeHeading(left, down), 1e-6)

        assertEquals(-90.0, SphericalUtil.computeHeading(right, front), 1e-6)
        assertEquals(90.0, SphericalUtil.computeHeading(left, front), 1e-6)

        assertEquals(90.0, SphericalUtil.computeHeading(front, right), 1e-6)
        assertEquals(-90.0, SphericalUtil.computeHeading(back, right), 1e-6)
    }

    @Test
    fun testComputeOffset() {
        // From front
        expectLatLngApproxEquals(front, SphericalUtil.computeOffset(front, 0.0, 0.0))
        expectLatLngApproxEquals(up, SphericalUtil.computeOffset(front, PI * EARTH_RADIUS / 2, 0.0))
        expectLatLngApproxEquals(down, SphericalUtil.computeOffset(front, PI * EARTH_RADIUS / 2, 180.0))
        expectLatLngApproxEquals(left, SphericalUtil.computeOffset(front, PI * EARTH_RADIUS / 2, -90.0))
        expectLatLngApproxEquals(right, SphericalUtil.computeOffset(front, PI * EARTH_RADIUS / 2, 90.0))
        expectLatLngApproxEquals(back, SphericalUtil.computeOffset(front, PI * EARTH_RADIUS, 0.0))
        expectLatLngApproxEquals(back, SphericalUtil.computeOffset(front, PI * EARTH_RADIUS, 90.0))

        // From left
        expectLatLngApproxEquals(left, SphericalUtil.computeOffset(left, 0.0, 0.0))
        expectLatLngApproxEquals(up, SphericalUtil.computeOffset(left, PI * EARTH_RADIUS / 2, 0.0))
        expectLatLngApproxEquals(down, SphericalUtil.computeOffset(left, PI * EARTH_RADIUS / 2, 180.0))
        expectLatLngApproxEquals(front, SphericalUtil.computeOffset(left, PI * EARTH_RADIUS / 2, 90.0))
        expectLatLngApproxEquals(back, SphericalUtil.computeOffset(left, PI * EARTH_RADIUS / 2, -90.0))
        expectLatLngApproxEquals(right, SphericalUtil.computeOffset(left, PI * EARTH_RADIUS, 0.0))
        expectLatLngApproxEquals(right, SphericalUtil.computeOffset(left, PI * EARTH_RADIUS, 90.0))

        // NOTE(appleton): Heading is undefined at the poles, so we do not test
        // from up/down.
    }

    @Test
    fun testComputeOffsetOrigin() {
        expectLatLngApproxEquals(front, assertNotNull(SphericalUtil.computeOffsetOrigin(front, 0.0, 0.0)))

        expectLatLngApproxEquals(
            front,
            assertNotNull(SphericalUtil.computeOffsetOrigin(LatLng(0.0, 45.0), PI * EARTH_RADIUS / 4, 90.0)),
        )
        expectLatLngApproxEquals(
            front,
            assertNotNull(SphericalUtil.computeOffsetOrigin(LatLng(0.0, -45.0), PI * EARTH_RADIUS / 4, -90.0)),
        )
        expectLatLngApproxEquals(
            front,
            assertNotNull(SphericalUtil.computeOffsetOrigin(LatLng(45.0, 0.0), PI * EARTH_RADIUS / 4, 0.0)),
        )
        expectLatLngApproxEquals(
            front,
            assertNotNull(SphericalUtil.computeOffsetOrigin(LatLng(-45.0, 0.0), PI * EARTH_RADIUS / 4, 180.0)),
        )

        // Situations with no solution, should return null.
        //
        // First 'over' the pole.
        assertNull(SphericalUtil.computeOffsetOrigin(LatLng(80.0, 0.0), PI * EARTH_RADIUS / 4, 180.0))
        // Second a distance that doesn't fit on the earth.
        assertNull(SphericalUtil.computeOffsetOrigin(LatLng(80.0, 0.0), PI * EARTH_RADIUS / 4, 90.0))
    }

    @Test
    fun testComputeOffsetAndBackToOrigin() {
        var start = LatLng(40.0, 40.0)
        var distance = 1e5
        var heading = 15.0
        var end: LatLng

        // Some semi-random values to demonstrate going forward and backward yields
        // the same location.
        end = SphericalUtil.computeOffset(start, distance, heading)
        expectLatLngApproxEquals(start, assertNotNull(SphericalUtil.computeOffsetOrigin(end, distance, heading)))

        heading = -37.0
        end = SphericalUtil.computeOffset(start, distance, heading)
        expectLatLngApproxEquals(start, assertNotNull(SphericalUtil.computeOffsetOrigin(end, distance, heading)))

        distance = 3.8e+7
        end = SphericalUtil.computeOffset(start, distance, heading)
        expectLatLngApproxEquals(start, assertNotNull(SphericalUtil.computeOffsetOrigin(end, distance, heading)))

        start = LatLng(-21.0, -73.0)
        end = SphericalUtil.computeOffset(start, distance, heading)
        expectLatLngApproxEquals(start, assertNotNull(SphericalUtil.computeOffsetOrigin(end, distance, heading)))

        // computeOffsetOrigin with multiple solutions, all we care about is that
        // going from there yields the requested result.
        //
        // First, for this particular situation the latitude is completely arbitrary.
        val start1 = SphericalUtil.computeOffsetOrigin(LatLng(0.0, 90.0), PI * EARTH_RADIUS / 2, 90.0)
        assertNotNull(start1)
        expectLatLngApproxEquals(LatLng(0.0, 90.0), SphericalUtil.computeOffset(start1, PI * EARTH_RADIUS / 2, 90.0))

        // Second, for this particular situation the longitude is completely
        // arbitrary.
        val start2 = SphericalUtil.computeOffsetOrigin(LatLng(90.0, 0.0), PI * EARTH_RADIUS / 4, 0.0)
        assertNotNull(start2)
        expectLatLngApproxEquals(LatLng(90.0, 0.0), SphericalUtil.computeOffset(start2, PI * EARTH_RADIUS / 4, 0.0))
    }

    @Test
    fun testInterpolate() {
        // Same point
        expectLatLngApproxEquals(up, SphericalUtil.interpolate(up, up, 1 / 2.0))
        expectLatLngApproxEquals(down, SphericalUtil.interpolate(down, down, 1 / 2.0))
        expectLatLngApproxEquals(left, SphericalUtil.interpolate(left, left, 1 / 2.0))

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
        expectLatLngApproxEquals(up, SphericalUtil.interpolate(LatLng(45.0, 0.0), LatLng(45.0, 180.0), 1 / 2.0))
        expectLatLngApproxEquals(down, SphericalUtil.interpolate(LatLng(-45.0, 0.0), LatLng(-45.0, 180.0), 1 / 2.0))

        // boundary values for fraction, between left and back
        expectLatLngApproxEquals(left, SphericalUtil.interpolate(left, back, 0.0))
        expectLatLngApproxEquals(back, SphericalUtil.interpolate(left, back, 1.0))

        // two nearby points, separated by ~4m, for which the Slerp algorithm is not stable and we
        // have to fall back to linear interpolation.
        expectLatLngApproxEquals(
            LatLng(-37.756872, 175.325252),
            SphericalUtil.interpolate(LatLng(-37.756891, 175.325262), LatLng(-37.756853, 175.325242), 0.5),
        )
    }

    @Test
    fun testComputeLength() {
        assertEquals(0.0, SphericalUtil.computeLength(emptyList()), 1e-6)
        assertEquals(0.0, SphericalUtil.computeLength(listOf(LatLng(0.0, 0.0))), 1e-6)

        var latLngs = listOf(LatLng(0.0, 0.0), LatLng(0.1, 0.1))
        assertEquals(toRadians(0.1) * sqrt(2.0) * EARTH_RADIUS, SphericalUtil.computeLength(latLngs), 1.0)

        latLngs = listOf(LatLng(0.0, 0.0), LatLng(90.0, 0.0), LatLng(0.0, 90.0))
        assertEquals(PI * EARTH_RADIUS, SphericalUtil.computeLength(latLngs), 1e-6)
    }

    @Test
    fun testIsCCW() {
        // One face of the octahedron
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

        // computeArea returns area of zero on small polys
        val area =
            computeTriangleArea(
                LatLng(0.0, 0.0),
                LatLng(0.0, toDegrees(1E-6)),
                LatLng(toDegrees(1E-6), 0.0),
            )
        val expectedArea = 1E-12 / 2

        assertTrue(abs(expectedArea - area) < 1e-20)
    }

    @Test
    fun testComputeSignedTriangleArea() {
        assertEquals(
            toRadians(0.1) * toRadians(0.1) / 2,
            computeSignedTriangleArea(LatLng(0.0, 0.0), LatLng(0.0, 0.1), LatLng(0.1, 0.1)),
            1e-6,
        )

        assertEquals(PI / 2, computeSignedTriangleArea(right, up, front), 1e-6)

        assertEquals(-PI / 2, computeSignedTriangleArea(front, up, right), 1e-6)
    }

    @Test
    fun testComputeArea() {
        assertEquals(
            PI * EARTH_RADIUS * EARTH_RADIUS,
            SphericalUtil.computeArea(listOf(right, up, front, down, right)),
            .4,
        )

        assertEquals(
            PI * EARTH_RADIUS * EARTH_RADIUS,
            SphericalUtil.computeArea(listOf(right, down, front, up, right)),
            .4,
        )
    }

    @Test
    fun testComputeSignedArea() {
        val path = listOf(right, up, front, down, right)
        val pathReversed = listOf(right, down, front, up, right)
        assertEquals(SphericalUtil.computeSignedArea(pathReversed), -SphericalUtil.computeSignedArea(path), 0.0)
    }

    @Test
    fun testGetPointOnPolyline() {
        val a = LatLng(0.0, 0.0)
        val b = LatLng(0.0, 10.0)
        val c = LatLng(10.0, 10.0)
        val d = LatLng(10.0, 0.0)
        val polyline = listOf(a, b, c, d)

        // Test for null cases
        assertNull(SphericalUtil.getPointOnPolyline(emptyList(), 0.5))
        assertNull(SphericalUtil.getPointOnPolyline(polyline, -0.1))
        assertNull(SphericalUtil.getPointOnPolyline(polyline, 1.1))

        // Test for start and end points
        expectLatLngApproxEquals(a, assertNotNull(SphericalUtil.getPointOnPolyline(polyline, 0.0)))
        expectLatLngApproxEquals(d, assertNotNull(SphericalUtil.getPointOnPolyline(polyline, 1.0)))

        // Test for a point in the middle of a segment
        val midAB = LatLng(0.0, 5.0)
        val totalLength = SphericalUtil.computeLength(polyline)
        val abLength = SphericalUtil.computeDistanceBetween(a, b)
        expectLatLngApproxEquals(
            midAB,
            assertNotNull(SphericalUtil.getPointOnPolyline(polyline, (abLength / 2) / totalLength)),
        )

        // Test for a point on a vertex
        val aToCLength = SphericalUtil.computeLength(listOf(a, b, c))
        expectLatLngApproxEquals(
            c,
            assertNotNull(SphericalUtil.getPointOnPolyline(polyline, aToCLength / totalLength)),
        )
    }

    @Test
    fun testGetPolylinePrefix() {
        val a = LatLng(0.0, 0.0)
        val b = LatLng(0.0, 10.0)
        val c = LatLng(10.0, 10.0)
        val d = LatLng(10.0, 0.0)
        val polyline = listOf(a, b, c, d)

        // Test for empty list cases
        assertTrue(SphericalUtil.getPolylinePrefix(emptyList(), 0.5).isEmpty())
        assertTrue(SphericalUtil.getPolylinePrefix(polyline, -0.1).isEmpty())
        assertTrue(SphericalUtil.getPolylinePrefix(polyline, 1.1).isEmpty())

        // Test for 0%
        val prefix0 = SphericalUtil.getPolylinePrefix(polyline, 0.0)
        assertEquals(1, prefix0.size)
        expectLatLngApproxEquals(a, prefix0[0])

        // Test for 100%
        val prefix100 = SphericalUtil.getPolylinePrefix(polyline, 1.0)
        assertEquals(polyline.size, prefix100.size)
        for (i in polyline.indices) {
            expectLatLngApproxEquals(polyline[i], prefix100[i])
        }

        // Test for a prefix that ends in the middle of a segment
        val midAB = LatLng(0.0, 5.0)
        val totalLength = SphericalUtil.computeLength(polyline)
        val abLength = SphericalUtil.computeDistanceBetween(a, b)
        val prefixMidAB = SphericalUtil.getPolylinePrefix(polyline, (abLength / 2) / totalLength)
        assertEquals(2, prefixMidAB.size)
        expectLatLngApproxEquals(a, prefixMidAB[0])
        expectLatLngApproxEquals(midAB, prefixMidAB[1])

        // Test for a prefix that ends on a vertex
        val aToCLength = SphericalUtil.computeLength(listOf(a, b, c))
        val prefixC = SphericalUtil.getPolylinePrefix(polyline, aToCLength / totalLength)
        assertEquals(3, prefixC.size)
        expectLatLngApproxEquals(a, prefixC[0])
        expectLatLngApproxEquals(b, prefixC[1])
        expectLatLngApproxEquals(c, prefixC[2])
    }

    companion object {
        /** Tests for approximate equality. */
        private fun expectLatLngApproxEquals(expected: LatLng, actual: LatLng) {
            assertEquals(expected.latitude, actual.latitude, 1e-6)
            // Account for the convergence of longitude lines at the poles
            val cosLat = cos(toRadians(actual.latitude))
            assertEquals(cosLat * expected.longitude, cosLat * actual.longitude, 1e-6)
        }

        private fun computeSignedTriangleArea(a: LatLng, b: LatLng, c: LatLng): Double =
            SphericalUtil.computeSignedArea(listOf(a, b, c), 1.0)

        private fun computeTriangleArea(a: LatLng, b: LatLng, c: LatLng): Double =
            abs(computeSignedTriangleArea(a, b, c))

        private fun isCCW(a: LatLng, b: LatLng, c: LatLng): Int =
            if (computeSignedTriangleArea(a, b, c) > 0) 1 else -1
    }
}
