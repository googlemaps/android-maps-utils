/*
 * Copyright 2013 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may not use this file except in compliance with the License.
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

package com.google.maps.android;

import com.google.android.gms.maps.model.LatLng;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static com.google.common.truth.Truth.assertThat;

/**
 * This class defines a series of tests for the {@link PolyUtil} class.
 * Each test is designed to verify the correctness of a specific geometric utility function
 * provided by {@link PolyUtil}, such as checking if a point is contained within a polygon,
 * if it lies on an edge, or simplifying a polyline.
 * <p>
 * The tests are structured to cover a wide range of scenarios, including edge cases like
 * empty polygons, polygons that cross the international date line, and polygons near the poles.
 * This comprehensive testing ensures that the geometric calculations are robust and reliable.
 */
public class PolyUtilTest {
    private static final String TEST_LINE =
        "_cqeFf~cjVf@p@fA}AtAoB`ArAx@hA`GbIvDiFv@gAh@t@X\\|@z@`@Z\\Xf@Vf@VpA\\tATJ@NBBkC";

    /**
     * A helper method to construct a {@link List} of {@link LatLng} objects from a series of
     * latitude and longitude coordinates. This simplifies the creation of test polygons and polylines.
     *
     * @param coords A varargs array of doubles, representing latitude and longitude pairs.
     * @return A {@link List} of {@link LatLng} objects.
     */
    private static List<LatLng> makeList(double... coords) {
        int size = coords.length / 2;
        List<LatLng> list = new ArrayList<>(size);
        for (int i = 0; i < size; ++i) {
            list.add(new LatLng(coords[i + i], coords[i + i + 1]));
        }
        return list;
    }

    /**
     * A helper method to test the {@link PolyUtil#containsLocation(LatLng, List, boolean)} method.
     * It asserts that all points in the {@code yes} list are contained within the polygon,
     * and all points in the {@code no} list are not. This is tested for both geodesic and rhumb line paths.
     *
     * @param poly The polygon to test against.
     * @param yes A list of points that are expected to be inside the polygon.
     * @param no A list of points that are expected to be outside the polygon.
     */
    private static void containsCase(List<LatLng> poly, List<LatLng> yes, List<LatLng> no) {
        for (LatLng point : yes) {
            assertThat(PolyUtil.containsLocation(point, poly, true)).isTrue();
            assertThat(PolyUtil.containsLocation(point, poly, false)).isTrue();
        }
        for (LatLng point : no) {
            assertThat(PolyUtil.containsLocation(point, poly, true)).isFalse();
            assertThat(PolyUtil.containsLocation(point, poly, false)).isFalse();
        }
    }

    /**
     * A helper method to test the {@link PolyUtil#isLocationOnEdge(LatLng, List, boolean)} and
     * {@link PolyUtil#isLocationOnPath(LatLng, List, boolean)} methods.
     * It asserts that all points in the {@code yes} list are on the edge of the polygon,
     * and all points in the {@code no} list are not.
     *
     * @param geodesic Whether to use geodesic or rhumb line paths.
     * @param poly The polygon or polyline to test against.
     * @param yes A list of points that are expected to be on the edge.
     * @param no A list of points that are expected to not be on the edge.
     */
    private static void onEdgeCase(
        boolean geodesic, List<LatLng> poly, List<LatLng> yes, List<LatLng> no) {
        for (LatLng point : yes) {
            assertThat(PolyUtil.isLocationOnEdge(point, poly, geodesic)).isTrue();
            assertThat(PolyUtil.isLocationOnPath(point, poly, geodesic)).isTrue();
        }
        for (LatLng point : no) {
            assertThat(PolyUtil.isLocationOnEdge(point, poly, geodesic)).isFalse();
            assertThat(PolyUtil.isLocationOnPath(point, poly, geodesic)).isFalse();
        }
    }

    /**
     * Overloaded helper method for {@link #onEdgeCase(boolean, List, List, List)} that tests for both
     * geodesic and rhumb line paths.
     */
    private static void onEdgeCase(List<LatLng> poly, List<LatLng> yes, List<LatLng> no) {
        onEdgeCase(true, poly, yes, no);
        onEdgeCase(false, poly, yes, no);
    }

    /**
     * A helper method to test the {@link PolyUtil#locationIndexOnPath(LatLng, List, boolean)}.
     * It asserts that the returned index for a given point on a polyline is as expected.
     *
     * @param geodesic Whether to use geodesic or rhumb line paths.
     * @param poly The polyline to test against.
     * @param point The point to find the index for.
     * @param idx The expected index.
     */
    private static void locationIndexCase(
        boolean geodesic, List<LatLng> poly, LatLng point, int idx) {
        assertThat(PolyUtil.locationIndexOnPath(point, poly, geodesic)).isEqualTo(idx);
    }

    /**
     * Overloaded helper method for {@link #locationIndexCase(boolean, List, LatLng, int)} that tests for both
     * geodesic and rhumb line paths.
     */
    private static void locationIndexCase(List<LatLng> poly, LatLng point, int idx) {
        locationIndexCase(true, poly, point, idx);
        locationIndexCase(false, poly, point, idx);
    }

    /**
     * A helper method to test {@link PolyUtil#locationIndexOnPath(LatLng, List, boolean, double)}
     * with a specific tolerance.
     *
     * @param geodesic Whether to use geodesic or rhumb line paths.
     * @param poly The polyline to test against.
     * @param point The point to find the index for.
     * @param idx The expected index.
     */
    private static void locationIndexToleranceCase(
        boolean geodesic, List<LatLng> poly, LatLng point, int idx) {
        assertThat(PolyUtil.locationIndexOnPath(point, poly, geodesic, 0.1)).isEqualTo(idx);
    }

    /**
     * Overloaded helper method for {@link #locationIndexToleranceCase(boolean, List, LatLng, int)} that tests for both
     * geodesic and rhumb line paths.
     */
    private static void locationIndexToleranceCase(List<LatLng> poly, LatLng point, int idx) {
        locationIndexToleranceCase(true, poly, point, idx);
        locationIndexToleranceCase(false, poly, point, idx);
    }

    /**
     * This test verifies the behavior of the `isLocationOnEdge` and `isLocationOnPath` methods.
     * It covers a variety of scenarios, including empty polylines, endpoints, and segments on the equator,
     * meridians, and slanted lines. It also tests cases near the poles and with long arcs.
     * The test uses a small tolerance to check for points that are very close to the edge, and a larger
     * tolerance to check for points that are further away.
     */
    @Test
    public void testOnEdge() {
        // Empty
        onEdgeCase(makeList(), makeList(), makeList(0, 0));

        final double small = 5e-7; // About 5cm on equator, half the default tolerance.
        final double big = 2e-6; // About 10cm on equator, double the default tolerance.

        // Endpoints
        onEdgeCase(makeList(1, 2), makeList(1, 2), makeList(3, 5));
        onEdgeCase(makeList(1, 2, 3, 5), makeList(1, 2, 3, 5), makeList(0, 0));

        // On equator.
        onEdgeCase(
            makeList(0, 90, 0, 180),
            makeList(0, 90 - small, 0, 90 + small, 0 - small, 90, 0, 135, small, 135),
            makeList(0, 90 - big, 0, 0, 0, -90, big, 135));

        // Ends on same latitude.
        onEdgeCase(
            makeList(-45, -180, -45, -small),
            makeList(-45, 180 + small, -45, 180 - small, -45 - small, 180 - small, -45, 0),
            makeList(-45, big, -45, 180 - big, -45 + big, -90, -45, 90));

        // Meridian.
        onEdgeCase(
            makeList(-10, 30, 45, 30),
            makeList(10, 30 - small, 20, 30 + small, -10 - small, 30 + small),
            makeList(-10 - big, 30, 10, -150, 0, 30 - big));

        // Slanted close to meridian, close to North pole.
        onEdgeCase(
            makeList(0, 0, 90 - small, 0 + big),
            makeList(1, 0 + small, 2, 0 - small, 90 - small, -90, 90 - small, 10),
            makeList(-big, 0, 90 - big, 180, 10, big));

        // Arc > 120 deg.
        onEdgeCase(
            makeList(0, 0, 0, 179.999),
            makeList(0, 90, 0, small, 0, 179, small, 90),
            makeList(0, -90, small, -100, 0, 180, 0, -big, 90, 0, -90, 180));

        onEdgeCase(
            makeList(10, 5, 30, 15),
            makeList(10 + 2 * big, 5 + big, 10 + big, 5 + big / 2, 30 - 2 * big, 15 - big),
            makeList(
                20,
                10,
                10 - big,
                5 - big / 2,
                30 + 2 * big,
                15 + big,
                10 + 2 * big,
                5,
                10,
                5 + big));

        onEdgeCase(
            makeList(90 - small, 0, 0, 180 - small / 2),
            makeList(big, -180 + small / 2, big, 180 - small / 4, big, 180 - small),
            makeList(-big, -180 + small / 2, -big, 180, -big, 180 - small));

        // Reaching close to North pole.
        onEdgeCase(
            true,
            makeList(80, 0, 80, 180 - small),
            makeList(90 - small, -90, 90, -135, 80 - small, 0, 80 + small, 0),
            makeList(80, 90, 79, big));

        onEdgeCase(
            false,
            makeList(80, 0, 80, 180 - small),
            makeList(80 - small, 0, 80 + small, 0, 80, 90),
            makeList(79, big, 90 - small, -90, 90, -135));
    }

    /**
     * This test verifies the `locationIndexOnPath` method, which determines the index of the segment
     * a point lies on. It tests empty polylines, single-point polylines, and multi-segment polylines,
     * ensuring that the correct segment index is returned for points on and off the path.
     */
    @Test
    public void testLocationIndex() {
        // Empty.
        locationIndexCase(makeList(), new LatLng(0, 0), -1);

        // One point.
        locationIndexCase(makeList(1, 2), new LatLng(1, 2), 0);
        locationIndexCase(makeList(1, 2), new LatLng(3, 5), -1);

        // Two points.
        locationIndexCase(makeList(1, 2, 3, 5), new LatLng(1, 2), 0);
        locationIndexCase(makeList(1, 2, 3, 5), new LatLng(3, 5), 0);
        locationIndexCase(makeList(1, 2, 3, 5), new LatLng(4, 6), -1);

        // Three points.
        locationIndexCase(makeList(0, 80, 0, 90, 0, 100), new LatLng(0, 80), 0);
        locationIndexCase(makeList(0, 80, 0, 90, 0, 100), new LatLng(0, 85), 0);
        locationIndexCase(makeList(0, 80, 0, 90, 0, 100), new LatLng(0, 90), 0);
        locationIndexCase(makeList(0, 80, 0, 90, 0, 100), new LatLng(0, 95), 1);
        locationIndexCase(makeList(0, 80, 0, 90, 0, 100), new LatLng(0, 100), 1);
        locationIndexCase(makeList(0, 80, 0, 90, 0, 100), new LatLng(0, 110), -1);
    }

    /**
     * This test specifically focuses on the tolerance parameter of the `locationIndexOnPath` method.
     * It verifies that the method correctly identifies points as being on a path segment within a given
     * tolerance, and correctly identifies points as being off the path if they are outside the tolerance.
     */
    @Test
    public void testLocationIndexTolerance() {
        final double small = 5e-7; // About 5cm on equator, half the default tolerance.
        final double big = 2e-6; // About 10cm on equator, double the default tolerance.

        // Test tolerance.
        locationIndexToleranceCase(
            makeList(0, 90 - small, 0, 90, 0, 90 + small), new LatLng(0, 90), 0);
        locationIndexToleranceCase(
            makeList(0, 90 - small, 0, 90, 0, 90 + small), new LatLng(0, 90 + small), 0);
        locationIndexToleranceCase(
            makeList(0, 90 - small, 0, 90, 0, 90 + small), new LatLng(0, 90 + 2 * small), 1);
        locationIndexToleranceCase(
            makeList(0, 90 - small, 0, 90, 0, 90 + small), new LatLng(0, 90 + 3 * small), -1);
        locationIndexToleranceCase(makeList(0, 90 - big, 0, 90, 0, 90 + big), new LatLng(0, 90), 0);
        locationIndexToleranceCase(
            makeList(0, 90 - big, 0, 90, 0, 90 + big), new LatLng(0, 90 + big), 1);
        locationIndexToleranceCase(
            makeList(0, 90 - big, 0, 90, 0, 90 + big), new LatLng(0, 90 + 2 * big), -1);
    }

    /**
     * This test verifies the `containsLocation` method, which checks if a point is inside a polygon.
     * It includes tests for empty polygons, single-point polygons, and various shapes of polygons.
     * Special attention is given to polygons that are near the North and South poles, as these can be
     * tricky edge cases for geometric calculations.
     */
    @Test
    public void testContainsLocation() {
        // Empty.
        containsCase(makeList(), makeList(), makeList(0, 0));

        // One point.
        containsCase(makeList(1, 2), makeList(1, 2), makeList(0, 0));

        // Two points.
        containsCase(makeList(1, 2, 3, 5), makeList(1, 2, 3, 5), makeList(0, 0, 40, 4));

        // Some arbitrary triangle.
        containsCase(
            makeList(0., 0., 10., 12., 20., 5.),
            makeList(10., 12., 10, 11, 19, 5),
            makeList(0, 1, 11, 12, 30, 5, 0, -180, 0, 90));

        // Around North Pole.
        containsCase(
            makeList(89, 0, 89, 120, 89, -120),
            makeList(90, 0, 90, 180, 90, -90),
            makeList(-90, 0, 0, 0));

        // Around South Pole.
        containsCase(
            makeList(-89, 0, -89, 120, -89, -120),
            makeList(90, 0, 90, 180, 90, -90, 0, 0),
            makeList(-90, 0, -90, 90));

        // Over/under segment on meridian and equator.
        containsCase(
            makeList(5, 10, 10, 10, 0, 20, 0, -10),
            makeList(2.5, 10, 1, 0),
            makeList(15, 10, 0, -15, 0, 25, -1, 0));
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
    public void testSimplify() {
        /*
         * Polyline
         */
        final String LINE =
            "elfjD~a}uNOnFN~Em@fJv@tEMhGDjDe@hG^nF??@lA?n@IvAC`Ay@A{@DwCA{CF_EC{CEi@PBTFDJBJ?V?n@?D@?A@?@?F?F?LAf@?n@@`@@T@~@FpA?fA?p@?r@?vAH`@OR@^ETFJCLD?JA^?J?P?fAC`B@d@?b@A\\@`@Ad@@\\?`@?f@?V?H?DD@DDBBDBD?D?B?B@B@@@B@B@B@D?D?JAF@H@FCLADBDBDCFAN?b@Af@@x@@";
        List<LatLng> line = PolyUtil.decode(LINE);
        assertThat(line.size()).isEqualTo(95);

        List<LatLng> simplifiedLine;
        List<LatLng> copy;

        double tolerance = 5; // meters
        copy = new ArrayList<>(line);
        simplifiedLine = PolyUtil.simplify(line, tolerance);
        assertThat(simplifiedLine.size()).isEqualTo(20);
        assertEndPoints(line, simplifiedLine);
        assertSimplifiedPointsFromLine(line, simplifiedLine);
        assertLineLength(line, simplifiedLine);
        assertInputUnchanged(line, copy);

        tolerance = 10; // meters
        copy = new ArrayList<>(line);
        simplifiedLine = PolyUtil.simplify(line, tolerance);
        assertThat(simplifiedLine.size()).isEqualTo(14);
        assertEndPoints(line, simplifiedLine);
        assertSimplifiedPointsFromLine(line, simplifiedLine);
        assertLineLength(line, simplifiedLine);
        assertInputUnchanged(line, copy);

        tolerance = 15; // meters
        copy = new ArrayList<>(line);
        simplifiedLine = PolyUtil.simplify(line, tolerance);
        assertThat(simplifiedLine.size()).isEqualTo(10);
        assertEndPoints(line, simplifiedLine);
        assertSimplifiedPointsFromLine(line, simplifiedLine);
        assertLineLength(line, simplifiedLine);
        assertInputUnchanged(line, copy);

        tolerance = 20; // meters
        copy = new ArrayList<>(line);
        simplifiedLine = PolyUtil.simplify(line, tolerance);
        assertThat(simplifiedLine.size()).isEqualTo(8);
        assertEndPoints(line, simplifiedLine);
        assertSimplifiedPointsFromLine(line, simplifiedLine);
        assertLineLength(line, simplifiedLine);
        assertInputUnchanged(line, copy);

        tolerance = 50; // meters
        copy = new ArrayList<>(line);
        simplifiedLine = PolyUtil.simplify(line, tolerance);
        assertThat(simplifiedLine.size()).isEqualTo(6);
        assertEndPoints(line, simplifiedLine);
        assertSimplifiedPointsFromLine(line, simplifiedLine);
        assertLineLength(line, simplifiedLine);
        assertInputUnchanged(line, copy);

        tolerance = 500; // meters
        copy = new ArrayList<>(line);
        simplifiedLine = PolyUtil.simplify(line, tolerance);
        assertThat(simplifiedLine.size()).isEqualTo(3);
        assertEndPoints(line, simplifiedLine);
        assertSimplifiedPointsFromLine(line, simplifiedLine);
        assertLineLength(line, simplifiedLine);
        assertInputUnchanged(line, copy);

        tolerance = 1000; // meters
        copy = new ArrayList<>(line);
        simplifiedLine = PolyUtil.simplify(line, tolerance);
        assertThat(simplifiedLine.size()).isEqualTo(2);
        assertEndPoints(line, simplifiedLine);
        assertSimplifiedPointsFromLine(line, simplifiedLine);
        assertLineLength(line, simplifiedLine);
        assertInputUnchanged(line, copy);

        /*
         * Polygons
         */
        // Open triangle
        ArrayList<LatLng> triangle = new ArrayList<>();
        triangle.add(new LatLng(28.06025, -82.41030));
        triangle.add(new LatLng(28.06129, -82.40945));
        triangle.add(new LatLng(28.06206, -82.40917));
        triangle.add(new LatLng(28.06125, -82.40850));
        triangle.add(new LatLng(28.06035, -82.40834));
        triangle.add(new LatLng(28.06038, -82.40924));
        assertThat(PolyUtil.isClosedPolygon(triangle)).isFalse();

        copy = new ArrayList<>(triangle);
        tolerance = 88; // meters
        List<LatLng> simplifiedTriangle = PolyUtil.simplify(triangle, tolerance);
        assertThat(simplifiedTriangle.size()).isEqualTo(4);
        assertEndPoints(triangle, simplifiedTriangle);
        assertSimplifiedPointsFromLine(triangle, simplifiedTriangle);
        assertLineLength(triangle, simplifiedTriangle);
        assertInputUnchanged(triangle, copy);

        // Close the triangle
        LatLng p = triangle.get(0);
        LatLng closePoint = new LatLng(p.latitude, p.longitude);
        triangle.add(closePoint);
        assertThat(PolyUtil.isClosedPolygon(triangle)).isTrue();

        copy = new ArrayList<>(triangle);
        tolerance = 88; // meters
        simplifiedTriangle = PolyUtil.simplify(triangle, tolerance);
        assertThat(simplifiedTriangle.size()).isEqualTo(4);
        assertEndPoints(triangle, simplifiedTriangle);
        assertSimplifiedPointsFromLine(triangle, simplifiedTriangle);
        assertLineLength(triangle, simplifiedTriangle);
        assertInputUnchanged(triangle, copy);

        // Open oval
        final String OVAL_POLYGON =
            "}wgjDxw_vNuAd@}AN{A]w@_Au@kAUaA?{@Ke@@_@C]D[FULWFOLSNMTOVOXO\\I\\CX?VJXJTDTNXTVVLVJ`@FXA\\AVLZBTATBZ@ZAT?\\?VFT@XGZ";
        List<LatLng> oval = PolyUtil.decode(OVAL_POLYGON);
        assertThat(PolyUtil.isClosedPolygon(oval)).isFalse();

        copy = new ArrayList<>(oval);
        tolerance = 10; // meters
        List<LatLng> simplifiedOval = PolyUtil.simplify(oval, tolerance);
        assertThat(simplifiedOval.size()).isEqualTo(13);
        assertEndPoints(oval, simplifiedOval);
        assertSimplifiedPointsFromLine(oval, simplifiedOval);
        assertLineLength(oval, simplifiedOval);
        assertInputUnchanged(oval, copy);

        // Close the oval
        p = oval.get(0);
        closePoint = new LatLng(p.latitude, p.longitude);
        oval.add(closePoint);
        assertThat(PolyUtil.isClosedPolygon(oval)).isTrue();

        copy = new ArrayList<>(oval);
        tolerance = 10; // meters
        simplifiedOval = PolyUtil.simplify(oval, tolerance);
        assertThat(simplifiedOval.size()).isEqualTo(13);
        assertEndPoints(oval, simplifiedOval);
        assertSimplifiedPointsFromLine(oval, simplifiedOval);
        assertLineLength(oval, simplifiedOval);
        assertInputUnchanged(oval, copy);
    }

    /**
     * Asserts that the beginning point of the original line matches the beginning point of the
     * simplified line, and that the end point of the original line matches the end point of the
     * simplified line.
     *
     * @param line original line
     * @param simplifiedLine simplified line
     */
    private void assertEndPoints(List<LatLng> line, List<LatLng> simplifiedLine) {
        assertThat(simplifiedLine.get(0)).isEqualTo(line.get(0));
        assertThat(simplifiedLine.get(simplifiedLine.size() - 1)).isEqualTo(line.get(line.size() - 1));
    }

    /**
     * Asserts that the simplified line is composed of points from the original line.
     *
     * @param line original line
     * @param simplifiedLine simplified line
     */
    private void assertSimplifiedPointsFromLine(List<LatLng> line, List<LatLng> simplifiedLine) {
        for (LatLng l : simplifiedLine) {
            assertThat(line).contains(l);
        }
    }

    /**
     * Asserts that the length of the simplified line is always equal to or less than the length of
     * the original line, if simplification has eliminated any points from the original line
     *
     * @param line original line
     * @param simplifiedLine simplified line
     */
    private void assertLineLength(List<LatLng> line, List<LatLng> simplifiedLine) {
        if (line.size() == simplifiedLine.size()) {
            // If no points were eliminated, then the length of both lines should be the same
            assertThat(SphericalUtil.computeLength(simplifiedLine))
                .isWithin(0.0)
                .of(SphericalUtil.computeLength(line));
        } else {
            assertThat(simplifiedLine.size()).isLessThan(line.size());
            // If points were eliminated, then the simplified line should always be shorter
            assertThat(SphericalUtil.computeLength(simplifiedLine))
                .isLessThan(SphericalUtil.computeLength(line));
        }
    }

    /**
     * Asserts that the contents of the original List passed into the PolyUtil.simplify() method
     * doesn't change after the method is executed. We test for this because the poly is modified (a
     * small offset is added to the last point) to allow for polygon simplification.
     *
     * @param afterInput the list passed into PolyUtil.simplify(), after PolyUtil.simplify() has
     *         finished executing
     * @param beforeInput a copy of the list before it is passed into PolyUtil.simplify()
     */
    private void assertInputUnchanged(List<LatLng> afterInput, List<LatLng> beforeInput) {
        // Check values
        assertThat(afterInput).isEqualTo(beforeInput);

        // Check references
        for (int i = 0; i < beforeInput.size(); i++) {
            assertThat(afterInput.get(i)).isSameInstanceAs(beforeInput.get(i));
        }
    }

    /**
     * This test verifies the `isClosedPolygon` method. It checks that the method correctly
     * identifies a polygon as closed only when its first and last points are identical.
     */
    @Test
    public void testIsClosedPolygon() {
        ArrayList<LatLng> poly = new ArrayList<>();
        poly.add(new LatLng(28.06025, -82.41030));
        poly.add(new LatLng(28.06129, -82.40945));
        poly.add(new LatLng(28.06206, -82.40917));
        poly.add(new LatLng(28.06125, -82.40850));
        poly.add(new LatLng(28.06035, -82.40834));

        assertThat(PolyUtil.isClosedPolygon(poly)).isFalse();

        // Add the closing point that's same as the first
        poly.add(new LatLng(28.06025, -82.41030));
        assertThat(PolyUtil.isClosedPolygon(poly)).isTrue();
    }

    /**
     * The following method checks whether {@link PolyUtil#distanceToLine(LatLng, LatLng, LatLng)  distanceToLine()} }
     * is determining the distance between a point and a segment accurately.
     * <p>
     * Currently there are tests for different orders of magnitude (i.e., 1X, 10X, 100X, 1000X), as well as a test
     * where the segment and the point lie in different hemispheres.
     * <p>
     * If further tests need to be added here, make sure that the distance has been verified with <a href="https://www.qgis.org/">QGIS</a>.
     *
     * @see <a href="https://www.qgis.org/">QGIS</a>
     */
    @Test
    public void testDistanceToLine() {
        LatLng startLine = new LatLng(28.05359, -82.41632);
        LatLng endLine = new LatLng(28.05310, -82.41634);
        LatLng p = new LatLng(28.05342, -82.41594);

        double distance = PolyUtil.distanceToLine(p, startLine, endLine);
        assertThat(distance).isWithin(1e-6).of(37.94596795917082);

        startLine = new LatLng(49.321045, 12.097749);
        endLine = new LatLng(49.321016, 12.097795);
        p = new LatLng(49.3210674, 12.0978238);

        distance = PolyUtil.distanceToLine(p, startLine, endLine);
        assertThat(distance).isWithin(1e-6).of(5.559443879999753);

        startLine = new LatLng(48.125961, 11.548998);
        endLine = new LatLng(48.125918, 11.549005);
        p = new LatLng(48.125941, 11.549028);

        distance = PolyUtil.distanceToLine(p, startLine, endLine);
        assertThat(distance).isWithin(1e-6).of(1.9733966358947437);

        startLine = new LatLng(78.924669, 11.925521);
        endLine = new LatLng(78.924707, 11.929060);
        p = new LatLng(78.923164, 11.924029);

        distance = PolyUtil.distanceToLine(p, startLine, endLine);
        assertThat(distance).isWithin(1e-6).of(170.35662670453187);

        startLine = new LatLng(69.664036, 18.957124);
        endLine = new LatLng(69.664029, 18.957109);
        p = new LatLng(69.672901, 18.967911);

        distance = PolyUtil.distanceToLine(p, startLine, endLine);
        assertThat(distance).isWithin(1e-6).of(1070.222749990837);

        startLine = new LatLng(-0.018200, 109.343282);
        endLine = new LatLng(-0.017877, 109.343537);
        p = new LatLng(0.058299, 109.408054);

        distance = PolyUtil.distanceToLine(p, startLine, endLine);
        assertThat(distance).isWithin(1e-6).of(11100.157563150981);
    }

    /**
     * This test ensures that the distance from a point to a line segment is always less than or equal
     * to the distance from the point to either of the segment's endpoints. This is a fundamental
     * property of Euclidean geometry that should also hold true for spherical geometry for short distances.
     */
    @Test
    public void testDistanceToLineLessThanDistanceToExtremes() {
        LatLng startLine = new LatLng(28.05359, -82.41632);
        LatLng endLine = new LatLng(28.05310, -82.41634);
        LatLng p = new LatLng(28.05342, -82.41594);

        double distance = PolyUtil.distanceToLine(p, startLine, endLine);
        double distanceToStart = SphericalUtil.computeDistanceBetween(p, startLine);
        double distanceToEnd = SphericalUtil.computeDistanceBetween(p, endLine);

        assertThat(distance).isAtMost(distanceToStart);
        assertThat(distance).isAtMost(distanceToEnd);
    }

    /**
     * This test verifies the `decode` method, which decodes an encoded polyline string into a list
     * of `LatLng` points. It checks that the decoded path has the correct number of points and that
     * the last point has the expected latitude and longitude.
     */
    @Test
    public void testDecodePath() {
        List<LatLng> latLngs = PolyUtil.decode(TEST_LINE);

        int expectedLength = 21;
        assertThat(latLngs.size()).isEqualTo(expectedLength);

        LatLng lastPoint = latLngs.get(expectedLength - 1);
        assertThat(lastPoint.latitude).isWithin(1e-6).of(37.76953);
        assertThat(lastPoint.longitude).isWithin(1e-6).of(-122.41488);
    }

    /**
     * This test verifies the `encode` method, which encodes a list of `LatLng` points into a
     * polyline string. It first decodes a test string, then re-encodes the resulting list of points,
     * and finally asserts that the re-encoded string is identical to the original. This ensures the
     * encode and decode methods are inverse operations.
     */
    @Test
    public void testEncodePath() {
        List<LatLng> path = PolyUtil.decode(TEST_LINE);
        String encoded = PolyUtil.encode(path);
        assertThat(encoded).isEqualTo(TEST_LINE);
    }
}
