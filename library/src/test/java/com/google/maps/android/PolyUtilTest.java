/*
 * Copyright 2013 Google Inc.
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

package com.google.maps.android;

import com.google.android.gms.maps.model.LatLng;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class PolyUtilTest {
    private static final String TEST_LINE =
            "_cqeFf~cjVf@p@fA}AtAoB`ArAx@hA`GbIvDiFv@gAh@t@X\\|@z@`@Z\\Xf@Vf@VpA\\tATJ@NBBkC";

    private static List<LatLng> makeList(double... coords) {
        int size = coords.length / 2;
        List<LatLng> list = new ArrayList<>(size);
        for (int i = 0; i < size; ++i) {
            list.add(new LatLng(coords[i + i], coords[i + i + 1]));
        }
        return list;
    }

    private static void containsCase(List<LatLng> poly, List<LatLng> yes, List<LatLng> no) {
        for (LatLng point : yes) {
            assertTrue(PolyUtil.containsLocation(point, poly, true));
            assertTrue(PolyUtil.containsLocation(point, poly, false));
        }
        for (LatLng point : no) {
            assertFalse(PolyUtil.containsLocation(point, poly, true));
            assertFalse(PolyUtil.containsLocation(point, poly, false));
        }
    }

    private static void onEdgeCase(
            boolean geodesic, List<LatLng> poly, List<LatLng> yes, List<LatLng> no) {
        for (LatLng point : yes) {
            assertTrue(PolyUtil.isLocationOnEdge(point, poly, geodesic));
            assertTrue(PolyUtil.isLocationOnPath(point, poly, geodesic));
        }
        for (LatLng point : no) {
            assertFalse(PolyUtil.isLocationOnEdge(point, poly, geodesic));
            assertFalse(PolyUtil.isLocationOnPath(point, poly, geodesic));
        }
    }

    private static void onEdgeCase(List<LatLng> poly, List<LatLng> yes, List<LatLng> no) {
        onEdgeCase(true, poly, yes, no);
        onEdgeCase(false, poly, yes, no);
    }

    private static void locationIndexCase(
            boolean geodesic, List<LatLng> poly, LatLng point, int idx) {
        assertEquals(idx, PolyUtil.locationIndexOnPath(point, poly, geodesic));
    }

    private static void locationIndexCase(List<LatLng> poly, LatLng point, int idx) {
        locationIndexCase(true, poly, point, idx);
        locationIndexCase(false, poly, point, idx);
    }

    private static void locationIndexToleranceCase(
            boolean geodesic, List<LatLng> poly, LatLng point, int idx) {
        assertEquals(idx, PolyUtil.locationIndexOnPath(point, poly, geodesic, 0.1));
    }

    private static void locationIndexToleranceCase(List<LatLng> poly, LatLng point, int idx) {
        locationIndexToleranceCase(true, poly, point, idx);
        locationIndexToleranceCase(false, poly, point, idx);
    }

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

    @Test
    public void testSimplify() {
        /*
         * Polyline
         */
        final String LINE =
                "elfjD~a}uNOnFN~Em@fJv@tEMhGDjDe@hG^nF??@lA?n@IvAC`Ay@A{@DwCA{CF_EC{CEi@PBTFDJBJ?V?n@?D@?A@?@?F?F?LAf@?n@@`@@T@~@FpA?fA?p@?r@?vAH`@OR@^ETFJCLD?JA^?J?P?fAC`B@d@?b@A\\@`@Ad@@\\?`@?f@?V?H?DD@DDBBDBD?D?B?B@B@@@B@B@B@D?D?JAF@H@FCLADBDBDCFAN?b@Af@@x@@";
        List<LatLng> line = PolyUtil.decode(LINE);
        assertEquals(95, line.size());

        List<LatLng> simplifiedLine;
        List<LatLng> copy;

        double tolerance = 5; // meters
        copy = new ArrayList<>(line);
        simplifiedLine = PolyUtil.simplify(line, tolerance);
        assertEquals(20, simplifiedLine.size());
        assertEndPoints(line, simplifiedLine);
        assertSimplifiedPointsFromLine(line, simplifiedLine);
        assertLineLength(line, simplifiedLine);
        assertInputUnchanged(line, copy);

        tolerance = 10; // meters
        copy = new ArrayList<>(line);
        simplifiedLine = PolyUtil.simplify(line, tolerance);
        assertEquals(14, simplifiedLine.size());
        assertEndPoints(line, simplifiedLine);
        assertSimplifiedPointsFromLine(line, simplifiedLine);
        assertLineLength(line, simplifiedLine);
        assertInputUnchanged(line, copy);

        tolerance = 15; // meters
        copy = new ArrayList<>(line);
        simplifiedLine = PolyUtil.simplify(line, tolerance);
        assertEquals(10, simplifiedLine.size());
        assertEndPoints(line, simplifiedLine);
        assertSimplifiedPointsFromLine(line, simplifiedLine);
        assertLineLength(line, simplifiedLine);
        assertInputUnchanged(line, copy);

        tolerance = 20; // meters
        copy = new ArrayList<>(line);
        simplifiedLine = PolyUtil.simplify(line, tolerance);
        assertEquals(8, simplifiedLine.size());
        assertEndPoints(line, simplifiedLine);
        assertSimplifiedPointsFromLine(line, simplifiedLine);
        assertLineLength(line, simplifiedLine);
        assertInputUnchanged(line, copy);

        tolerance = 50; // meters
        copy = new ArrayList<>(line);
        simplifiedLine = PolyUtil.simplify(line, tolerance);
        assertEquals(6, simplifiedLine.size());
        assertEndPoints(line, simplifiedLine);
        assertSimplifiedPointsFromLine(line, simplifiedLine);
        assertLineLength(line, simplifiedLine);
        assertInputUnchanged(line, copy);

        tolerance = 500; // meters
        copy = new ArrayList<>(line);
        simplifiedLine = PolyUtil.simplify(line, tolerance);
        assertEquals(3, simplifiedLine.size());
        assertEndPoints(line, simplifiedLine);
        assertSimplifiedPointsFromLine(line, simplifiedLine);
        assertLineLength(line, simplifiedLine);
        assertInputUnchanged(line, copy);

        tolerance = 1000; // meters
        copy = new ArrayList<>(line);
        simplifiedLine = PolyUtil.simplify(line, tolerance);
        assertEquals(2, simplifiedLine.size());
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
        assertFalse(PolyUtil.isClosedPolygon(triangle));

        copy = new ArrayList<>(triangle);
        tolerance = 88; // meters
        List<LatLng> simplifiedTriangle = PolyUtil.simplify(triangle, tolerance);
        assertEquals(4, simplifiedTriangle.size());
        assertEndPoints(triangle, simplifiedTriangle);
        assertSimplifiedPointsFromLine(triangle, simplifiedTriangle);
        assertLineLength(triangle, simplifiedTriangle);
        assertInputUnchanged(triangle, copy);

        // Close the triangle
        LatLng p = triangle.get(0);
        LatLng closePoint = new LatLng(p.latitude, p.longitude);
        triangle.add(closePoint);
        assertTrue(PolyUtil.isClosedPolygon(triangle));

        copy = new ArrayList<>(triangle);
        tolerance = 88; // meters
        simplifiedTriangle = PolyUtil.simplify(triangle, tolerance);
        assertEquals(4, simplifiedTriangle.size());
        assertEndPoints(triangle, simplifiedTriangle);
        assertSimplifiedPointsFromLine(triangle, simplifiedTriangle);
        assertLineLength(triangle, simplifiedTriangle);
        assertInputUnchanged(triangle, copy);

        // Open oval
        final String OVAL_POLYGON =
                "}wgjDxw_vNuAd@}AN{A]w@_Au@kAUaA?{@Ke@@_@C]D[FULWFOLSNMTOVOXO\\I\\CX?VJXJTDTNXTVVLVJ`@FXA\\AVLZBTATBZ@ZAT?\\?VFT@XGZ";
        List<LatLng> oval = PolyUtil.decode(OVAL_POLYGON);
        assertFalse(PolyUtil.isClosedPolygon(oval));

        copy = new ArrayList<>(oval);
        tolerance = 10; // meters
        List<LatLng> simplifiedOval = PolyUtil.simplify(oval, tolerance);
        assertEquals(13, simplifiedOval.size());
        assertEndPoints(oval, simplifiedOval);
        assertSimplifiedPointsFromLine(oval, simplifiedOval);
        assertLineLength(oval, simplifiedOval);
        assertInputUnchanged(oval, copy);

        // Close the oval
        p = oval.get(0);
        closePoint = new LatLng(p.latitude, p.longitude);
        oval.add(closePoint);
        assertTrue(PolyUtil.isClosedPolygon(oval));

        copy = new ArrayList<>(oval);
        tolerance = 10; // meters
        simplifiedOval = PolyUtil.simplify(oval, tolerance);
        assertEquals(13, simplifiedOval.size());
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
        assertEquals(line.get(0), simplifiedLine.get(0));
        assertEquals(line.get(line.size() - 1), simplifiedLine.get(simplifiedLine.size() - 1));
    }

    /**
     * Asserts that the simplified line is composed of points from the original line.
     *
     * @param line original line
     * @param simplifiedLine simplified line
     */
    private void assertSimplifiedPointsFromLine(List<LatLng> line, List<LatLng> simplifiedLine) {
        for (LatLng l : simplifiedLine) {
            assertTrue(line.contains(l));
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
            assertEquals(
                    SphericalUtil.computeLength(simplifiedLine),
                    SphericalUtil.computeLength(line),
                    0.0);
        } else {
            assertTrue(simplifiedLine.size() < line.size());
            // If points were eliminated, then the simplified line should always be shorter
            assertTrue(
                    SphericalUtil.computeLength(simplifiedLine)
                            < SphericalUtil.computeLength(line));
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
        assertEquals(beforeInput, afterInput);

        // Check references
        for (int i = 0; i < beforeInput.size(); i++) {
            assertSame(afterInput.get(i), beforeInput.get(i));
        }
    }

    @Test
    public void testIsClosedPolygon() {
        ArrayList<LatLng> poly = new ArrayList<>();
        poly.add(new LatLng(28.06025, -82.41030));
        poly.add(new LatLng(28.06129, -82.40945));
        poly.add(new LatLng(28.06206, -82.40917));
        poly.add(new LatLng(28.06125, -82.40850));
        poly.add(new LatLng(28.06035, -82.40834));

        assertFalse(PolyUtil.isClosedPolygon(poly));

        // Add the closing point that's same as the first
        poly.add(new LatLng(28.06025, -82.41030));
        assertTrue(PolyUtil.isClosedPolygon(poly));
    }

    @Test
    public void testDistanceToLine() {
        LatLng startLine = new LatLng(28.05359, -82.41632);
        LatLng endLine = new LatLng(28.05310, -82.41634);
        LatLng p = new LatLng(28.05342, -82.41594);

        double distance = PolyUtil.distanceToLine(p, startLine, endLine);
        assertEquals(37.947946, distance, 1e-6);
    }

    @Test
    public void testDistanceToLineLessThanDistanceToExtrems() {
        LatLng startLine = new LatLng(28.05359, -82.41632);
        LatLng endLine = new LatLng(28.05310, -82.41634);
        LatLng p = new LatLng(28.05342, -82.41594);

        double distance = PolyUtil.distanceToLine(p, startLine, endLine);
        double distanceToStart = SphericalUtil.computeDistanceBetween(p, startLine);
        double distanceToEnd = SphericalUtil.computeDistanceBetween(p, endLine);

        assertTrue("Wrong distance.", distance <= distanceToStart && distance <= distanceToEnd);
    }

    @Test
    public void testDecodePath() {
        List<LatLng> latLngs = PolyUtil.decode(TEST_LINE);

        int expectedLength = 21;
        assertEquals("Wrong length.", expectedLength, latLngs.size());

        LatLng lastPoint = latLngs.get(expectedLength - 1);
        assertEquals(37.76953, lastPoint.latitude, 1e-6);
        assertEquals(-122.41488, lastPoint.longitude, 1e-6);
    }

    @Test
    public void testEncodePath() {
        List<LatLng> path = PolyUtil.decode(TEST_LINE);
        String encoded = PolyUtil.encode(path);
        assertEquals(TEST_LINE, encoded);
    }
}
