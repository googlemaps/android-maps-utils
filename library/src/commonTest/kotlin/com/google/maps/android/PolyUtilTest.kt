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

import com.google.maps.android.model.LatLng
import com.google.maps.android.model.latitude
import com.google.maps.android.model.longitude
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertSame
import kotlin.test.assertTrue

/**
 * This class defines a series of tests for the [PolyUtil] class. Each test is designed to
 * verify the correctness of a specific geometric utility function provided by [PolyUtil],
 * such as checking if a point is contained within a polygon, if it lies on an edge, or simplifying
 * a polyline.
 *
 * The tests are structured to cover a wide range of scenarios, including edge cases like empty
 * polygons, polygons that cross the international date line, and polygons near the poles. This
 * comprehensive testing ensures that the geometric calculations are robust and reliable.
 */
class PolyUtilTest {
    /**
     * This test verifies the behavior of the `isLocationOnEdge` and `isLocationOnPath` methods. It
     * covers a variety of scenarios, including empty polylines, endpoints, and segments on the
     * equator, meridians, and slanted lines. It also tests cases near the poles and with long arcs.
     * The test uses a small tolerance to check for points that are very close to the edge, and a
     * larger tolerance to check for points that are further away.
     */
    @Test
    fun testOnEdge() {
        // Empty
        onEdgeCase(makeList(), makeList(), makeList(0.0, 0.0))

        val small = 5e-7 // About 5cm on equator, half the default tolerance.
        val big = 2e-6 // About 10cm on equator, double the default tolerance.

        // Endpoints
        onEdgeCase(makeList(1.0, 2.0), makeList(1.0, 2.0), makeList(3.0, 5.0))
        onEdgeCase(makeList(1.0, 2.0, 3.0, 5.0), makeList(1.0, 2.0, 3.0, 5.0), makeList(0.0, 0.0))

        // On equator.
        onEdgeCase(
            makeList(0.0, 90.0, 0.0, 180.0),
            makeList(0.0, 90 - small, 0.0, 90 + small, 0 - small, 90.0, 0.0, 135.0, small, 135.0),
            makeList(0.0, 90 - big, 0.0, 0.0, 0.0, -90.0, big, 135.0),
        )

        // Ends on same latitude.
        onEdgeCase(
            makeList(-45.0, -180.0, -45.0, -small),
            makeList(-45.0, 180 + small, -45.0, 180 - small, -45 - small, 180 - small, -45.0, 0.0),
            makeList(-45.0, big, -45.0, 180 - big, -45 + big, -90.0, -45.0, 90.0),
        )

        // Meridian.
        onEdgeCase(
            makeList(-10.0, 30.0, 45.0, 30.0),
            makeList(10.0, 30 - small, 20.0, 30 + small, -10 - small, 30 + small),
            makeList(-10 - big, 30.0, 10.0, -150.0, 0.0, 30 - big),
        )

        // Slanted close to meridian, close to North pole.
        onEdgeCase(
            makeList(0.0, 0.0, 90 - small, 0 + big),
            makeList(1.0, 0 + small, 2.0, 0 - small, 90 - small, -90.0, 90 - small, 10.0),
            makeList(-big, 0.0, 90 - big, 180.0, 10.0, big),
        )

        // Arc > 120 deg.
        onEdgeCase(
            makeList(0.0, 0.0, 0.0, 179.999),
            makeList(0.0, 90.0, 0.0, small, 0.0, 179.0, small, 90.0),
            makeList(0.0, -90.0, small, -100.0, 0.0, 180.0, 0.0, -big, 90.0, 0.0, -90.0, 180.0),
        )

        onEdgeCase(
            makeList(10.0, 5.0, 30.0, 15.0),
            makeList(10 + 2 * big, 5 + big, 10 + big, 5 + big / 2, 30 - 2 * big, 15 - big),
            makeList(
                20.0, 10.0, 10 - big, 5 - big / 2, 30 + 2 * big, 15 + big, 10 + 2 * big, 5.0, 10.0, 5 + big,
            ),
        )

        onEdgeCase(
            makeList(90 - small, 0.0, 0.0, 180 - small / 2),
            makeList(big, -180 + small / 2, big, 180 - small / 4, big, 180 - small),
            makeList(-big, -180 + small / 2, -big, 180.0, -big, 180 - small),
        )

        // Reaching close to North pole.
        onEdgeCase(
            true,
            makeList(80.0, 0.0, 80.0, 180 - small),
            makeList(90 - small, -90.0, 90.0, -135.0, 80 - small, 0.0, 80 + small, 0.0),
            makeList(80.0, 90.0, 79.0, big),
        )

        onEdgeCase(
            false,
            makeList(80.0, 0.0, 80.0, 180 - small),
            makeList(80 - small, 0.0, 80 + small, 0.0, 80.0, 90.0),
            makeList(79.0, big, 90 - small, -90.0, 90.0, -135.0),
        )
    }

    /**
     * This test verifies the `locationIndexOnPath` method, which determines the index of the segment
     * a point lies on. It tests empty polylines, single-point polylines, and multi-segment polylines,
     * ensuring that the correct segment index is returned for points on and off the path.
     */
    @Test
    fun testLocationIndex() {
        // Empty.
        locationIndexCase(makeList(), LatLng(0.0, 0.0), -1)

        // One point.
        locationIndexCase(makeList(1.0, 2.0), LatLng(1.0, 2.0), 0)
        locationIndexCase(makeList(1.0, 2.0), LatLng(3.0, 5.0), -1)

        // Two points.
        locationIndexCase(makeList(1.0, 2.0, 3.0, 5.0), LatLng(1.0, 2.0), 0)
        locationIndexCase(makeList(1.0, 2.0, 3.0, 5.0), LatLng(3.0, 5.0), 0)
        locationIndexCase(makeList(1.0, 2.0, 3.0, 5.0), LatLng(4.0, 6.0), -1)

        // Three points.
        locationIndexCase(makeList(0.0, 80.0, 0.0, 90.0, 0.0, 100.0), LatLng(0.0, 80.0), 0)
        locationIndexCase(makeList(0.0, 80.0, 0.0, 90.0, 0.0, 100.0), LatLng(0.0, 85.0), 0)
        locationIndexCase(makeList(0.0, 80.0, 0.0, 90.0, 0.0, 100.0), LatLng(0.0, 90.0), 0)
        locationIndexCase(makeList(0.0, 80.0, 0.0, 90.0, 0.0, 100.0), LatLng(0.0, 95.0), 1)
        locationIndexCase(makeList(0.0, 80.0, 0.0, 90.0, 0.0, 100.0), LatLng(0.0, 100.0), 1)
        locationIndexCase(makeList(0.0, 80.0, 0.0, 90.0, 0.0, 100.0), LatLng(0.0, 110.0), -1)
    }

    /**
     * This test specifically focuses on the tolerance parameter of the `locationIndexOnPath` method.
     * It verifies that the method correctly identifies points as being on a path segment within a
     * given tolerance, and correctly identifies points as being off the path if they are outside the
     * tolerance.
     */
    @Test
    fun testLocationIndexTolerance() {
        val small = 5e-7 // About 5cm on equator, half the default tolerance.
        val big = 2e-6 // About 10cm on equator, double the default tolerance.

        // Test tolerance.
        locationIndexToleranceCase(makeList(0.0, 90 - small, 0.0, 90.0, 0.0, 90 + small), LatLng(0.0, 90.0), 0)
        locationIndexToleranceCase(
            makeList(0.0, 90 - small, 0.0, 90.0, 0.0, 90 + small),
            LatLng(0.0, 90 + small),
            0,
        )
        locationIndexToleranceCase(
            makeList(0.0, 90 - small, 0.0, 90.0, 0.0, 90 + small),
            LatLng(0.0, 90 + 2 * small),
            1,
        )
        locationIndexToleranceCase(
            makeList(0.0, 90 - small, 0.0, 90.0, 0.0, 90 + small),
            LatLng(0.0, 90 + 3 * small),
            -1,
        )
        locationIndexToleranceCase(makeList(0.0, 90 - big, 0.0, 90.0, 0.0, 90 + big), LatLng(0.0, 90.0), 0)
        locationIndexToleranceCase(
            makeList(0.0, 90 - big, 0.0, 90.0, 0.0, 90 + big),
            LatLng(0.0, 90 + big),
            1,
        )
        locationIndexToleranceCase(
            makeList(0.0, 90 - big, 0.0, 90.0, 0.0, 90 + big),
            LatLng(0.0, 90 + 2 * big),
            -1,
        )
    }

    /**
     * This test verifies the `containsLocation` method, which checks if a point is inside a polygon.
     * It includes tests for empty polygons, single-point polygons, and various shapes of polygons.
     * Special attention is given to polygons that are near the North and South poles, as these can be
     * tricky edge cases for geometric calculations.
     */
    @Test
    fun testContainsLocation() {
        // Empty.
        containsCase(makeList(), makeList(), makeList(0.0, 0.0))

        // One point.
        containsCase(makeList(1.0, 2.0), makeList(1.0, 2.0), makeList(0.0, 0.0))

        // Two points.
        containsCase(makeList(1.0, 2.0, 3.0, 5.0), makeList(1.0, 2.0, 3.0, 5.0), makeList(0.0, 0.0, 40.0, 4.0))

        // Some arbitrary triangle.
        containsCase(
            makeList(0.0, 0.0, 10.0, 12.0, 20.0, 5.0),
            makeList(10.0, 12.0, 10.0, 11.0, 19.0, 5.0),
            makeList(0.0, 1.0, 11.0, 12.0, 30.0, 5.0, 0.0, -180.0, 0.0, 90.0),
        )

        // Around North Pole.
        containsCase(
            makeList(89.0, 0.0, 89.0, 120.0, 89.0, -120.0),
            makeList(90.0, 0.0, 90.0, 180.0, 90.0, -90.0),
            makeList(-90.0, 0.0, 0.0, 0.0),
        )

        // Around South Pole.
        containsCase(
            makeList(-89.0, 0.0, -89.0, 120.0, -89.0, -120.0),
            makeList(90.0, 0.0, 90.0, 180.0, 90.0, -90.0, 0.0, 0.0),
            makeList(-90.0, 0.0, -90.0, 90.0),
        )

        // Over/under segment on meridian and equator.
        containsCase(
            makeList(5.0, 10.0, 10.0, 10.0, 0.0, 20.0, 0.0, -10.0),
            makeList(2.5, 10.0, 1.0, 0.0),
            makeList(15.0, 10.0, 0.0, -15.0, 0.0, 25.0, -1.0, 0.0),
        )
    }

    /**
     * This test verifies the `simplify` method, which uses the Douglas-Peucker algorithm to reduce
     * the number of points in a polyline or polygon. The test checks the simplification at various
     * tolerance levels, from small to large, and asserts that the simplified line has the expected
     * number of points. It also verifies that the endpoints of the simplified line are the same as
     * the original, that the simplified points are a subset of the original points, and that the
     * length of the simplified line is less than or equal to the original.
     */
    @Test
    fun testSimplify() {
        /*
         * Polyline
         */
        val encodedLine =
            "elfjD~a}uNOnFN~Em@fJv@tEMhGDjDe@hG^nF??@lA?n@IvAC`Ay@A{@DwCA{CF_EC{CEi@PBTFDJBJ?V?n@?D@?A@?@?F?F?LAf@?n@@`@@T@~@FpA?fA?p@?r@?vAH`@OR@^ETFJCLD?JA^?J?P?fAC`B@d@?b@A\\@`@Ad@@\\?`@?f@?V?H?DD@DDBBDBD?D?B?B@B@@@B@B@B@D?D?JAF@H@FCLADBDBDCFAN?b@Af@@x@@"
        val line = PolyUtil.decode(encodedLine)
        assertEquals(95, line.size)

        var simplifiedLine: List<LatLng>
        var copy: List<LatLng>

        var tolerance = 5.0 // meters
        copy = ArrayList(line)
        simplifiedLine = PolyUtil.simplify(line, tolerance)
        assertEquals(20, simplifiedLine.size)
        assertEndPoints(line, simplifiedLine)
        assertSimplifiedPointsFromLine(line, simplifiedLine)
        assertLineLength(line, simplifiedLine)
        assertInputUnchanged(line, copy)

        tolerance = 10.0 // meters
        copy = ArrayList(line)
        simplifiedLine = PolyUtil.simplify(line, tolerance)
        assertEquals(14, simplifiedLine.size)
        assertEndPoints(line, simplifiedLine)
        assertSimplifiedPointsFromLine(line, simplifiedLine)
        assertLineLength(line, simplifiedLine)
        assertInputUnchanged(line, copy)

        tolerance = 15.0 // meters
        copy = ArrayList(line)
        simplifiedLine = PolyUtil.simplify(line, tolerance)
        assertEquals(10, simplifiedLine.size)
        assertEndPoints(line, simplifiedLine)
        assertSimplifiedPointsFromLine(line, simplifiedLine)
        assertLineLength(line, simplifiedLine)
        assertInputUnchanged(line, copy)

        tolerance = 20.0 // meters
        copy = ArrayList(line)
        simplifiedLine = PolyUtil.simplify(line, tolerance)
        assertEquals(8, simplifiedLine.size)
        assertEndPoints(line, simplifiedLine)
        assertSimplifiedPointsFromLine(line, simplifiedLine)
        assertLineLength(line, simplifiedLine)
        assertInputUnchanged(line, copy)

        tolerance = 50.0 // meters
        copy = ArrayList(line)
        simplifiedLine = PolyUtil.simplify(line, tolerance)
        assertEquals(6, simplifiedLine.size)
        assertEndPoints(line, simplifiedLine)
        assertSimplifiedPointsFromLine(line, simplifiedLine)
        assertLineLength(line, simplifiedLine)
        assertInputUnchanged(line, copy)

        tolerance = 500.0 // meters
        copy = ArrayList(line)
        simplifiedLine = PolyUtil.simplify(line, tolerance)
        assertEquals(3, simplifiedLine.size)
        assertEndPoints(line, simplifiedLine)
        assertSimplifiedPointsFromLine(line, simplifiedLine)
        assertLineLength(line, simplifiedLine)
        assertInputUnchanged(line, copy)

        tolerance = 1000.0 // meters
        copy = ArrayList(line)
        simplifiedLine = PolyUtil.simplify(line, tolerance)
        assertEquals(2, simplifiedLine.size)
        assertEndPoints(line, simplifiedLine)
        assertSimplifiedPointsFromLine(line, simplifiedLine)
        assertLineLength(line, simplifiedLine)
        assertInputUnchanged(line, copy)

        /*
         * Polygons
         */
        // Open triangle
        val triangle = ArrayList<LatLng>()
        triangle.add(LatLng(28.06025, -82.41030))
        triangle.add(LatLng(28.06129, -82.40945))
        triangle.add(LatLng(28.06206, -82.40917))
        triangle.add(LatLng(28.06125, -82.40850))
        triangle.add(LatLng(28.06035, -82.40834))
        triangle.add(LatLng(28.06038, -82.40924))
        assertFalse(PolyUtil.isClosedPolygon(triangle))

        copy = ArrayList(triangle)
        tolerance = 88.0 // meters
        var simplifiedTriangle = PolyUtil.simplify(triangle, tolerance)
        assertEquals(4, simplifiedTriangle.size)
        assertEndPoints(triangle, simplifiedTriangle)
        assertSimplifiedPointsFromLine(triangle, simplifiedTriangle)
        assertLineLength(triangle, simplifiedTriangle)
        assertInputUnchanged(triangle, copy)

        // Close the triangle
        var p = triangle[0]
        var closePoint = LatLng(p.latitude, p.longitude)
        triangle.add(closePoint)
        assertTrue(PolyUtil.isClosedPolygon(triangle))

        copy = ArrayList(triangle)
        tolerance = 88.0 // meters
        simplifiedTriangle = PolyUtil.simplify(triangle, tolerance)
        assertEquals(4, simplifiedTriangle.size)
        assertEndPoints(triangle, simplifiedTriangle)
        assertSimplifiedPointsFromLine(triangle, simplifiedTriangle)
        assertLineLength(triangle, simplifiedTriangle)
        assertInputUnchanged(triangle, copy)

        // Open oval
        val encodedOvalPolygon =
            "}wgjDxw_vNuAd@}AN{A]w@_Au@kAUaA?{@Ke@@_@C]D[FULWFOLSNMTOVOXO\\I\\CX?VJXJTDTNXTVVLVJ`@FXA\\AVLZBTATBZ@ZAT?\\?VFT@XGZ"
        val oval = PolyUtil.decode(encodedOvalPolygon).toMutableList()
        assertFalse(PolyUtil.isClosedPolygon(oval))

        copy = ArrayList(oval)
        tolerance = 10.0 // meters
        var simplifiedOval = PolyUtil.simplify(oval, tolerance)
        assertEquals(13, simplifiedOval.size)
        assertEndPoints(oval, simplifiedOval)
        assertSimplifiedPointsFromLine(oval, simplifiedOval)
        assertLineLength(oval, simplifiedOval)
        assertInputUnchanged(oval, copy)

        // Close the oval
        p = oval[0]
        closePoint = LatLng(p.latitude, p.longitude)
        oval.add(closePoint)
        assertTrue(PolyUtil.isClosedPolygon(oval))

        copy = ArrayList(oval)
        tolerance = 10.0 // meters
        simplifiedOval = PolyUtil.simplify(oval, tolerance)
        assertEquals(13, simplifiedOval.size)
        assertEndPoints(oval, simplifiedOval)
        assertSimplifiedPointsFromLine(oval, simplifiedOval)
        assertLineLength(oval, simplifiedOval)
        assertInputUnchanged(oval, copy)
    }

    /**
     * This test verifies the `isClosedPolygon` method. It checks that the method correctly identifies
     * a polygon as closed only when its first and last points are identical.
     */
    @Test
    fun testIsClosedPolygon() {
        val poly = ArrayList<LatLng>()
        poly.add(LatLng(28.06025, -82.41030))
        poly.add(LatLng(28.06129, -82.40945))
        poly.add(LatLng(28.06206, -82.40917))
        poly.add(LatLng(28.06125, -82.40850))
        poly.add(LatLng(28.06035, -82.40834))

        assertFalse(PolyUtil.isClosedPolygon(poly))

        // Add the closing point that's same as the first
        poly.add(LatLng(28.06025, -82.41030))
        assertTrue(PolyUtil.isClosedPolygon(poly))
    }

    /**
     * The following method checks whether [PolyUtil.distanceToLine] is determining the distance
     * between a point and a segment accurately.
     *
     * Currently there are tests for different orders of magnitude (i.e., 1X, 10X, 100X, 1000X), as
     * well as a test where the segment and the point lie in different hemispheres.
     *
     * If further tests need to be added here, make sure that the distance has been verified with
     * [QGIS](https://www.qgis.org/).
     */
    @Test
    fun testDistanceToLine() {
        var startLine = LatLng(28.05359, -82.41632)
        var endLine = LatLng(28.05310, -82.41634)
        var p = LatLng(28.05342, -82.41594)

        var distance = PolyUtil.distanceToLine(p, startLine, endLine)
        assertEquals(37.94596795917082, distance, 1e-6)

        startLine = LatLng(49.321045, 12.097749)
        endLine = LatLng(49.321016, 12.097795)
        p = LatLng(49.3210674, 12.0978238)

        distance = PolyUtil.distanceToLine(p, startLine, endLine)
        assertEquals(5.559443879999753, distance, 1e-6)

        startLine = LatLng(48.125961, 11.548998)
        endLine = LatLng(48.125918, 11.549005)
        p = LatLng(48.125941, 11.549028)

        distance = PolyUtil.distanceToLine(p, startLine, endLine)
        assertEquals(1.9733966358947437, distance, 1e-6)

        startLine = LatLng(78.924669, 11.925521)
        endLine = LatLng(78.924707, 11.929060)
        p = LatLng(78.923164, 11.924029)

        distance = PolyUtil.distanceToLine(p, startLine, endLine)
        assertEquals(170.35662670453187, distance, 1e-6)

        startLine = LatLng(69.664036, 18.957124)
        endLine = LatLng(69.664029, 18.957109)
        p = LatLng(69.672901, 18.967911)

        distance = PolyUtil.distanceToLine(p, startLine, endLine)
        assertEquals(1070.222749990837, distance, 1e-6)

        startLine = LatLng(-0.018200, 109.343282)
        endLine = LatLng(-0.017877, 109.343537)
        p = LatLng(0.058299, 109.408054)

        distance = PolyUtil.distanceToLine(p, startLine, endLine)
        assertEquals(11100.157563150981, distance, 1e-6)
    }

    /**
     * This test ensures that the distance from a point to a line segment is always less than or equal
     * to the distance from the point to either of the segment's endpoints. This is a fundamental
     * property of Euclidean geometry that should also hold true for spherical geometry for short
     * distances.
     */
    @Test
    fun testDistanceToLineLessThanDistanceToExtremes() {
        val startLine = LatLng(28.05359, -82.41632)
        val endLine = LatLng(28.05310, -82.41634)
        val p = LatLng(28.05342, -82.41594)

        val distance = PolyUtil.distanceToLine(p, startLine, endLine)
        val distanceToStart = SphericalUtil.computeDistanceBetween(p, startLine)
        val distanceToEnd = SphericalUtil.computeDistanceBetween(p, endLine)

        assertTrue(distance <= distanceToStart)
        assertTrue(distance <= distanceToEnd)
    }

    /**
     * This test verifies the `decode` method, which decodes an encoded polyline string into a list of
     * `LatLng` points. It checks that the decoded path has the correct number of points and that the
     * last point has the expected latitude and longitude.
     */
    @Test
    fun testDecodePath() {
        val latLngs = PolyUtil.decode(TEST_LINE)

        val expectedLength = 21
        assertEquals(expectedLength, latLngs.size)

        val lastPoint = latLngs[expectedLength - 1]
        assertEquals(37.76953, lastPoint.latitude, 1e-6)
        assertEquals(-122.41488, lastPoint.longitude, 1e-6)
    }

    /**
     * This test verifies the `encode` method, which encodes a list of `LatLng` points into a polyline
     * string. It first decodes a test string, then re-encodes the resulting list of points, and
     * finally asserts that the re-encoded string is identical to the original. This ensures the
     * encode and decode methods are inverse operations.
     */
    @Test
    fun testEncodePath() {
        val path = PolyUtil.decode(TEST_LINE)
        val encoded = PolyUtil.encode(path)
        assertEquals(TEST_LINE, encoded)
    }

    companion object {
        private const val TEST_LINE =
            "_cqeFf~cjVf@p@fA}AtAoB`ArAx@hA`GbIvDiFv@gAh@t@X\\|@z@`@Z\\Xf@Vf@VpA\\tATJ@NBBkC"

        /**
         * A helper method to construct a [List] of [LatLng] objects from a series of latitude
         * and longitude coordinates. This simplifies the creation of test polygons and polylines.
         */
        private fun makeList(vararg coords: Double): List<LatLng> {
            val size = coords.size / 2
            val list = ArrayList<LatLng>(size)
            for (i in 0 until size) {
                list.add(LatLng(coords[i + i], coords[i + i + 1]))
            }
            return list
        }

        /**
         * A helper method to test [PolyUtil.containsLocation]. It asserts that all points in the
         * [yes] list are contained within the polygon, and all points in the [no] list are not.
         * This is tested for both geodesic and rhumb line paths.
         */
        private fun containsCase(poly: List<LatLng>, yes: List<LatLng>, no: List<LatLng>) {
            for (point in yes) {
                assertTrue(PolyUtil.containsLocation(point, poly, true))
                assertTrue(PolyUtil.containsLocation(point, poly, false))
            }
            for (point in no) {
                assertFalse(PolyUtil.containsLocation(point, poly, true))
                assertFalse(PolyUtil.containsLocation(point, poly, false))
            }
        }

        /**
         * A helper method to test [PolyUtil.isLocationOnEdge] and [PolyUtil.isLocationOnPath].
         * It asserts that all points in the [yes] list are on the edge of the polygon, and all
         * points in the [no] list are not.
         */
        private fun onEdgeCase(geodesic: Boolean, poly: List<LatLng>, yes: List<LatLng>, no: List<LatLng>) {
            for (point in yes) {
                assertTrue(PolyUtil.isLocationOnEdge(point, poly, geodesic))
                assertTrue(PolyUtil.isLocationOnPath(point, poly, geodesic))
            }
            for (point in no) {
                assertFalse(PolyUtil.isLocationOnEdge(point, poly, geodesic))
                assertFalse(PolyUtil.isLocationOnPath(point, poly, geodesic))
            }
        }

        /** Overload of [onEdgeCase] that tests for both geodesic and rhumb line paths. */
        private fun onEdgeCase(poly: List<LatLng>, yes: List<LatLng>, no: List<LatLng>) {
            onEdgeCase(true, poly, yes, no)
            onEdgeCase(false, poly, yes, no)
        }

        /**
         * A helper method to test [PolyUtil.locationIndexOnPath]. It asserts that the returned
         * index for a given point on a polyline is as expected.
         */
        private fun locationIndexCase(geodesic: Boolean, poly: List<LatLng>, point: LatLng, idx: Int) {
            assertEquals(idx, PolyUtil.locationIndexOnPath(point, poly, geodesic))
        }

        /** Overload of [locationIndexCase] that tests for both geodesic and rhumb line paths. */
        private fun locationIndexCase(poly: List<LatLng>, point: LatLng, idx: Int) {
            locationIndexCase(true, poly, point, idx)
            locationIndexCase(false, poly, point, idx)
        }

        /** A helper method to test [PolyUtil.locationIndexOnPath] with a specific tolerance. */
        private fun locationIndexToleranceCase(geodesic: Boolean, poly: List<LatLng>, point: LatLng, idx: Int) {
            assertEquals(idx, PolyUtil.locationIndexOnPath(point, poly, geodesic, 0.1))
        }

        /** Overload of [locationIndexToleranceCase] that tests both geodesic and rhumb line paths. */
        private fun locationIndexToleranceCase(poly: List<LatLng>, point: LatLng, idx: Int) {
            locationIndexToleranceCase(true, poly, point, idx)
            locationIndexToleranceCase(false, poly, point, idx)
        }

        /**
         * Asserts that the beginning and end points of the original line match those of the
         * simplified line.
         */
        private fun assertEndPoints(line: List<LatLng>, simplifiedLine: List<LatLng>) {
            assertEquals(line[0], simplifiedLine[0])
            assertEquals(line[line.size - 1], simplifiedLine[simplifiedLine.size - 1])
        }

        /** Asserts that the simplified line is composed of points from the original line. */
        private fun assertSimplifiedPointsFromLine(line: List<LatLng>, simplifiedLine: List<LatLng>) {
            for (l in simplifiedLine) {
                assertTrue(line.contains(l))
            }
        }

        /**
         * Asserts that the length of the simplified line is always equal to or less than the length
         * of the original line, if simplification has eliminated any points from the original line.
         */
        private fun assertLineLength(line: List<LatLng>, simplifiedLine: List<LatLng>) {
            if (line.size == simplifiedLine.size) {
                // If no points were eliminated, then the length of both lines should be the same
                assertEquals(SphericalUtil.computeLength(line), SphericalUtil.computeLength(simplifiedLine), 0.0)
            } else {
                assertTrue(simplifiedLine.size < line.size)
                // If points were eliminated, then the simplified line should always be shorter
                assertTrue(SphericalUtil.computeLength(simplifiedLine) < SphericalUtil.computeLength(line))
            }
        }

        /**
         * Asserts that the contents of the original List passed into the PolyUtil.simplify() method
         * doesn't change after the method is executed. We test for this because the poly is modified
         * (a small offset is added to the last point) to allow for polygon simplification.
         */
        private fun assertInputUnchanged(afterInput: List<LatLng>, beforeInput: List<LatLng>) {
            // Check values
            assertEquals(beforeInput, afterInput)

            // Check references
            for (i in beforeInput.indices) {
                assertSame(beforeInput[i], afterInput[i])
            }
        }
    }
}
