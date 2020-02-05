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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static com.google.maps.android.MathUtil.EARTH_RADIUS;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class SphericalUtilTest {
    // The vertices of an octahedron, for testing
    private final LatLng up = new LatLng(90, 0);
    private final LatLng down = new LatLng(-90, 0);
    private final LatLng front = new LatLng(0, 0);
    private final LatLng right = new LatLng(0, 90);
    private final LatLng back = new LatLng(0, -180);
    private final LatLng left = new LatLng(0, -90);

    /**
     * Tests for approximate equality.
     */
    private static void expectLatLngApproxEquals(LatLng actual, LatLng expected) {
        assertEquals(actual.latitude, expected.latitude, 1e-6);
        // Account for the convergence of longitude lines at the poles
        double cosLat = Math.cos(Math.toRadians(actual.latitude));
        assertEquals(cosLat * actual.longitude, cosLat * expected.longitude, 1e-6);
    }

    private static double computeSignedTriangleArea(LatLng a, LatLng b, LatLng c) {
        List<LatLng> path = Arrays.asList(a, b, c);
        return SphericalUtil.computeSignedArea(path, 1);
    }

    private static double computeTriangleArea(LatLng a, LatLng b, LatLng c) {
        return Math.abs(computeSignedTriangleArea(a, b, c));
    }

    private static int isCCW(LatLng a, LatLng b, LatLng c) {
        return computeSignedTriangleArea(a, b, c) > 0 ? 1 : -1;
    }

    @Test
    public void testAngles() {
        // Same vertex
        assertEquals(SphericalUtil.computeAngleBetween(up, up), 0, 1e-6);
        assertEquals(SphericalUtil.computeAngleBetween(down, down), 0, 1e-6);
        assertEquals(SphericalUtil.computeAngleBetween(left, left), 0, 1e-6);
        assertEquals(SphericalUtil.computeAngleBetween(right, right), 0, 1e-6);
        assertEquals(SphericalUtil.computeAngleBetween(front, front), 0, 1e-6);
        assertEquals(SphericalUtil.computeAngleBetween(back, back), 0, 1e-6);

        // Adjacent vertices
        assertEquals(SphericalUtil.computeAngleBetween(up, front), Math.PI / 2, 1e-6);
        assertEquals(SphericalUtil.computeAngleBetween(up, right), Math.PI / 2, 1e-6);
        assertEquals(SphericalUtil.computeAngleBetween(up, back), Math.PI / 2, 1e-6);
        assertEquals(SphericalUtil.computeAngleBetween(up, left), Math.PI / 2, 1e-6);

        assertEquals(SphericalUtil.computeAngleBetween(down, front), Math.PI / 2, 1e-6);
        assertEquals(SphericalUtil.computeAngleBetween(down, right), Math.PI / 2, 1e-6);
        assertEquals(SphericalUtil.computeAngleBetween(down, back), Math.PI / 2, 1e-6);
        assertEquals(SphericalUtil.computeAngleBetween(down, left), Math.PI / 2, 1e-6);

        assertEquals(SphericalUtil.computeAngleBetween(back, up), Math.PI / 2, 1e-6);
        assertEquals(SphericalUtil.computeAngleBetween(back, right), Math.PI / 2, 1e-6);
        assertEquals(SphericalUtil.computeAngleBetween(back, down), Math.PI / 2, 1e-6);
        assertEquals(SphericalUtil.computeAngleBetween(back, left), Math.PI / 2, 1e-6);

        // Opposite vertices
        assertEquals(SphericalUtil.computeAngleBetween(up, down), Math.PI, 1e-6);
        assertEquals(SphericalUtil.computeAngleBetween(front, back), Math.PI, 1e-6);
        assertEquals(SphericalUtil.computeAngleBetween(left, right), Math.PI, 1e-6);
    }

    @Test
    public void testDistances() {
        assertEquals(SphericalUtil.computeDistanceBetween(up, down), Math.PI * EARTH_RADIUS, 1e-6);
    }

    @Test
    public void testHeadings() {
        // Opposing vertices for which there is a result
        assertEquals(SphericalUtil.computeHeading(up, down), -180, 1e-6);
        assertEquals(SphericalUtil.computeHeading(down, up), 0, 1e-6);

        // Adjacent vertices for which there is a result
        assertEquals(SphericalUtil.computeHeading(front, up), 0, 1e-6);
        assertEquals(SphericalUtil.computeHeading(right, up), 0, 1e-6);
        assertEquals(SphericalUtil.computeHeading(back, up), 0, 1e-6);
        assertEquals(SphericalUtil.computeHeading(down, up), 0, 1e-6);

        assertEquals(SphericalUtil.computeHeading(front, down), -180, 1e-6);
        assertEquals(SphericalUtil.computeHeading(right, down), -180, 1e-6);
        assertEquals(SphericalUtil.computeHeading(back, down), -180, 1e-6);
        assertEquals(SphericalUtil.computeHeading(left, down), -180, 1e-6);

        assertEquals(SphericalUtil.computeHeading(right, front), -90, 1e-6);
        assertEquals(SphericalUtil.computeHeading(left, front), 90, 1e-6);

        assertEquals(SphericalUtil.computeHeading(front, right), 90, 1e-6);
        assertEquals(SphericalUtil.computeHeading(back, right), -90, 1e-6);
    }

    @Test
    public void testComputeOffset() {
        // From front
        expectLatLngApproxEquals(front, SphericalUtil.computeOffset(front, 0, 0));
        expectLatLngApproxEquals(
                up, SphericalUtil.computeOffset(front, Math.PI * EARTH_RADIUS / 2, 0));
        expectLatLngApproxEquals(
                down, SphericalUtil.computeOffset(front, Math.PI * EARTH_RADIUS / 2, 180));
        expectLatLngApproxEquals(
                left, SphericalUtil.computeOffset(front, Math.PI * EARTH_RADIUS / 2, -90));
        expectLatLngApproxEquals(
                right, SphericalUtil.computeOffset(front, Math.PI * EARTH_RADIUS / 2, 90));
        expectLatLngApproxEquals(
                back, SphericalUtil.computeOffset(front, Math.PI * EARTH_RADIUS, 0));
        expectLatLngApproxEquals(
                back, SphericalUtil.computeOffset(front, Math.PI * EARTH_RADIUS, 90));

        // From left
        expectLatLngApproxEquals(left, SphericalUtil.computeOffset(left, 0, 0));
        expectLatLngApproxEquals(
                up, SphericalUtil.computeOffset(left, Math.PI * EARTH_RADIUS / 2, 0));
        expectLatLngApproxEquals(
                down, SphericalUtil.computeOffset(left, Math.PI * EARTH_RADIUS / 2, 180));
        expectLatLngApproxEquals(
                front, SphericalUtil.computeOffset(left, Math.PI * EARTH_RADIUS / 2, 90));
        expectLatLngApproxEquals(
                back, SphericalUtil.computeOffset(left, Math.PI * EARTH_RADIUS / 2, -90));
        expectLatLngApproxEquals(
                right, SphericalUtil.computeOffset(left, Math.PI * EARTH_RADIUS, 0));
        expectLatLngApproxEquals(
                right, SphericalUtil.computeOffset(left, Math.PI * EARTH_RADIUS, 90));

        // NOTE(appleton): Heading is undefined at the poles, so we do not test
        // from up/down.
    }

    @Test
    public void testComputeOffsetOrigin() {
        expectLatLngApproxEquals(front, SphericalUtil.computeOffsetOrigin(front, 0, 0));

        expectLatLngApproxEquals(
                front,
                SphericalUtil.computeOffsetOrigin(
                        new LatLng(0, 45), Math.PI * EARTH_RADIUS / 4, 90));
        expectLatLngApproxEquals(
                front,
                SphericalUtil.computeOffsetOrigin(
                        new LatLng(0, -45), Math.PI * EARTH_RADIUS / 4, -90));
        expectLatLngApproxEquals(
                front,
                SphericalUtil.computeOffsetOrigin(
                        new LatLng(45, 0), Math.PI * EARTH_RADIUS / 4, 0));
        expectLatLngApproxEquals(
                front,
                SphericalUtil.computeOffsetOrigin(
                        new LatLng(-45, 0), Math.PI * EARTH_RADIUS / 4, 180));
        /*expectLatLngApproxEquals(
        front, SphericalUtil.computeOffsetOrigin(new LatLng(-45, 0),
        Math.PI / 4, 180, 1)); */
        // Situations with no solution, should return null.
        //
        // First 'over' the pole.
        assertNull(
                SphericalUtil.computeOffsetOrigin(
                        new LatLng(80, 0), Math.PI * EARTH_RADIUS / 4, 180));
        // Second a distance that doesn't fit on the earth.
        assertNull(
                SphericalUtil.computeOffsetOrigin(
                        new LatLng(80, 0), Math.PI * EARTH_RADIUS / 4, 90));
    }

    @Test
    public void testComputeOffsetAndBackToOrigin() {
        LatLng start = new LatLng(40, 40);
        double distance = 1e5;
        double heading = 15;
        LatLng end;

        // Some semi-random values to demonstrate going forward and backward yields
        // the same location.
        end = SphericalUtil.computeOffset(start, distance, heading);
        expectLatLngApproxEquals(start, SphericalUtil.computeOffsetOrigin(end, distance, heading));

        heading = -37;
        end = SphericalUtil.computeOffset(start, distance, heading);
        expectLatLngApproxEquals(start, SphericalUtil.computeOffsetOrigin(end, distance, heading));

        distance = 3.8e+7;
        end = SphericalUtil.computeOffset(start, distance, heading);
        expectLatLngApproxEquals(start, SphericalUtil.computeOffsetOrigin(end, distance, heading));

        start = new LatLng(-21, -73);
        end = SphericalUtil.computeOffset(start, distance, heading);
        expectLatLngApproxEquals(start, SphericalUtil.computeOffsetOrigin(end, distance, heading));

        // computeOffsetOrigin with multiple solutions, all we care about is that
        // going from there yields the requested result.
        //
        // First, for this particular situation the latitude is completely arbitrary.
        start =
                SphericalUtil.computeOffsetOrigin(
                        new LatLng(0, 90), Math.PI * EARTH_RADIUS / 2, 90);
        expectLatLngApproxEquals(
                new LatLng(0, 90),
                SphericalUtil.computeOffset(start, Math.PI * EARTH_RADIUS / 2, 90));

        // Second, for this particular situation the longitude is completely
        // arbitrary.
        start = SphericalUtil.computeOffsetOrigin(new LatLng(90, 0), Math.PI * EARTH_RADIUS / 4, 0);
        expectLatLngApproxEquals(
                new LatLng(90, 0),
                SphericalUtil.computeOffset(start, Math.PI * EARTH_RADIUS / 4, 0));
    }

    @Test
    public void testInterpolate() {
        // Same point
        expectLatLngApproxEquals(up, SphericalUtil.interpolate(up, up, 1 / 2.0));
        expectLatLngApproxEquals(down, SphericalUtil.interpolate(down, down, 1 / 2.0));
        expectLatLngApproxEquals(left, SphericalUtil.interpolate(left, left, 1 / 2.0));

        // Between front and up
        expectLatLngApproxEquals(new LatLng(1, 0), SphericalUtil.interpolate(front, up, 1 / 90.0));
        expectLatLngApproxEquals(new LatLng(1, 0), SphericalUtil.interpolate(up, front, 89 / 90.0));
        expectLatLngApproxEquals(
                new LatLng(89, 0), SphericalUtil.interpolate(front, up, 89 / 90.0));
        expectLatLngApproxEquals(new LatLng(89, 0), SphericalUtil.interpolate(up, front, 1 / 90.0));

        // Between front and down
        expectLatLngApproxEquals(
                new LatLng(-1, 0), SphericalUtil.interpolate(front, down, 1 / 90.0));
        expectLatLngApproxEquals(
                new LatLng(-1, 0), SphericalUtil.interpolate(down, front, 89 / 90.0));
        expectLatLngApproxEquals(
                new LatLng(-89, 0), SphericalUtil.interpolate(front, down, 89 / 90.0));
        expectLatLngApproxEquals(
                new LatLng(-89, 0), SphericalUtil.interpolate(down, front, 1 / 90.0));

        // Between left and back
        expectLatLngApproxEquals(
                new LatLng(0, -91), SphericalUtil.interpolate(left, back, 1 / 90.0));
        expectLatLngApproxEquals(
                new LatLng(0, -91), SphericalUtil.interpolate(back, left, 89 / 90.0));
        expectLatLngApproxEquals(
                new LatLng(0, -179), SphericalUtil.interpolate(left, back, 89 / 90.0));
        expectLatLngApproxEquals(
                new LatLng(0, -179), SphericalUtil.interpolate(back, left, 1 / 90.0));

        // geodesic crosses pole
        expectLatLngApproxEquals(
                up, SphericalUtil.interpolate(new LatLng(45, 0), new LatLng(45, 180), 1 / 2.0));
        expectLatLngApproxEquals(
                down, SphericalUtil.interpolate(new LatLng(-45, 0), new LatLng(-45, 180), 1 / 2.0));

        // boundary values for fraction, between left and back
        expectLatLngApproxEquals(left, SphericalUtil.interpolate(left, back, 0));
        expectLatLngApproxEquals(back, SphericalUtil.interpolate(left, back, 1.0));

        // two nearby points, separated by ~4m, for which the Slerp algorithm is not stable and we
        // have to fall back to linear interpolation.
        expectLatLngApproxEquals(
                new LatLng(-37.756872, 175.325252),
                SphericalUtil.interpolate(
                        new LatLng(-37.756891, 175.325262),
                        new LatLng(-37.756853, 175.325242),
                        0.5));
    }

    @Test
    public void testComputeLength() {
        List<LatLng> latLngs;

        assertEquals(SphericalUtil.computeLength(Collections.<LatLng>emptyList()), 0, 1e-6);
        assertEquals(SphericalUtil.computeLength(Arrays.asList(new LatLng(0, 0))), 0, 1e-6);

        latLngs = Arrays.asList(new LatLng(0, 0), new LatLng(0.1, 0.1));
        assertEquals(
                SphericalUtil.computeLength(latLngs),
                Math.toRadians(0.1) * Math.sqrt(2) * EARTH_RADIUS,
                1);

        latLngs = Arrays.asList(new LatLng(0, 0), new LatLng(90, 0), new LatLng(0, 90));
        assertEquals(SphericalUtil.computeLength(latLngs), Math.PI * EARTH_RADIUS, 1e-6);
    }

    @Test
    public void testIsCCW() {
        // One face of the octahedron
        assertEquals(1, isCCW(right, up, front));
        assertEquals(1, isCCW(up, front, right));
        assertEquals(1, isCCW(front, right, up));
        assertEquals(-1, isCCW(front, up, right));
        assertEquals(-1, isCCW(up, right, front));
        assertEquals(-1, isCCW(right, front, up));
    }

    @Test
    public void testComputeTriangleArea() {
        assertEquals(computeTriangleArea(right, up, front), Math.PI / 2, 1e-6);
        assertEquals(computeTriangleArea(front, up, right), Math.PI / 2, 1e-6);

        // computeArea returns area of zero on small polys
        double area =
                computeTriangleArea(
                        new LatLng(0, 0),
                        new LatLng(0, Math.toDegrees(1E-6)),
                        new LatLng(Math.toDegrees(1E-6), 0));
        double expectedArea = 1E-12 / 2;

        assertTrue(Math.abs(expectedArea - area) < 1e-20);
    }

    @Test
    public void testComputeSignedTriangleArea() {
        assertEquals(
                computeSignedTriangleArea(
                        new LatLng(0, 0), new LatLng(0, 0.1), new LatLng(0.1, 0.1)),
                Math.toRadians(0.1) * Math.toRadians(0.1) / 2,
                1e-6);

        assertEquals(computeSignedTriangleArea(right, up, front), Math.PI / 2, 1e-6);

        assertEquals(computeSignedTriangleArea(front, up, right), -Math.PI / 2, 1e-6);
    }

    @Test
    public void testComputeArea() {
        assertEquals(
                SphericalUtil.computeArea(Arrays.asList(right, up, front, down, right)),
                Math.PI * EARTH_RADIUS * EARTH_RADIUS,
                .4);

        assertEquals(
                SphericalUtil.computeArea(Arrays.asList(right, down, front, up, right)),
                Math.PI * EARTH_RADIUS * EARTH_RADIUS,
                .4);
    }

    @Test
    public void testComputeSignedArea() {
        List<LatLng> path = Arrays.asList(right, up, front, down, right);
        List<LatLng> pathReversed = Arrays.asList(right, down, front, up, right);
        assertEquals(
                -SphericalUtil.computeSignedArea(path),
                SphericalUtil.computeSignedArea(pathReversed),
                0);
    }
}
