/*
 * Copyright 2023 Google Inc.
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

import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static com.google.common.truth.Truth.assertThat;
import static com.google.maps.android.MathUtil.EARTH_RADIUS;

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
        assertThat(actual.latitude).isWithin(1e-6).of(expected.latitude);
        // Account for the convergence of longitude lines at the poles
        double cosLat = Math.cos(Math.toRadians(actual.latitude));
        assertThat(cosLat * actual.longitude).isWithin(1e-6).of(cosLat * expected.longitude);
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
        assertThat(SphericalUtil.computeAngleBetween(up, up)).isWithin(1e-6).of(0);
        assertThat(SphericalUtil.computeAngleBetween(down, down)).isWithin(1e-6).of(0);
        assertThat(SphericalUtil.computeAngleBetween(left, left)).isWithin(1e-6).of(0);
        assertThat(SphericalUtil.computeAngleBetween(right, right)).isWithin(1e-6).of(0);
        assertThat(SphericalUtil.computeAngleBetween(front, front)).isWithin(1e-6).of(0);
        assertThat(SphericalUtil.computeAngleBetween(back, back)).isWithin(1e-6).of(0);

        // Adjacent vertices
        assertThat(SphericalUtil.computeAngleBetween(up, front)).isWithin(1e-6).of(Math.PI / 2);
        assertThat(SphericalUtil.computeAngleBetween(up, right)).isWithin(1e-6).of(Math.PI / 2);
        assertThat(SphericalUtil.computeAngleBetween(up, back)).isWithin(1e-6).of(Math.PI / 2);
        assertThat(SphericalUtil.computeAngleBetween(up, left)).isWithin(1e-6).of(Math.PI / 2);

        assertThat(SphericalUtil.computeAngleBetween(down, front)).isWithin(1e-6).of(Math.PI / 2);
        assertThat(SphericalUtil.computeAngleBetween(down, right)).isWithin(1e-6).of(Math.PI / 2);
        assertThat(SphericalUtil.computeAngleBetween(down, back)).isWithin(1e-6).of(Math.PI / 2);
        assertThat(SphericalUtil.computeAngleBetween(down, left)).isWithin(1e-6).of(Math.PI / 2);

        assertThat(SphericalUtil.computeAngleBetween(back, up)).isWithin(1e-6).of(Math.PI / 2);
        assertThat(SphericalUtil.computeAngleBetween(back, right)).isWithin(1e-6).of(Math.PI / 2);
        assertThat(SphericalUtil.computeAngleBetween(back, down)).isWithin(1e-6).of(Math.PI / 2);
        assertThat(SphericalUtil.computeAngleBetween(back, left)).isWithin(1e-6).of(Math.PI / 2);

        // Opposite vertices
        assertThat(SphericalUtil.computeAngleBetween(up, down)).isWithin(1e-6).of(Math.PI);
        assertThat(SphericalUtil.computeAngleBetween(front, back)).isWithin(1e-6).of(Math.PI);
        assertThat(SphericalUtil.computeAngleBetween(left, right)).isWithin(1e-6).of(Math.PI);
    }

    @Test
    public void testDistances() {
        assertThat(SphericalUtil.computeDistanceBetween(up, down)).isWithin(1e-6).of(Math.PI * EARTH_RADIUS);
    }

    @Test
    public void testHeadings() {
        // Opposing vertices for which there is a result
        assertThat(SphericalUtil.computeHeading(up, down)).isWithin(1e-6).of(-180);
        assertThat(SphericalUtil.computeHeading(down, up)).isWithin(1e-6).of(0);

        // Adjacent vertices for which there is a result
        assertThat(SphericalUtil.computeHeading(front, up)).isWithin(1e-6).of(0);
        assertThat(SphericalUtil.computeHeading(right, up)).isWithin(1e-6).of(0);
        assertThat(SphericalUtil.computeHeading(back, up)).isWithin(1e-6).of(0);
        assertThat(SphericalUtil.computeHeading(down, up)).isWithin(1e-6).of(0);

        assertThat(SphericalUtil.computeHeading(front, down)).isWithin(1e-6).of(-180);
        assertThat(SphericalUtil.computeHeading(right, down)).isWithin(1e-6).of(-180);
        assertThat(SphericalUtil.computeHeading(back, down)).isWithin(1e-6).of(-180);
        assertThat(SphericalUtil.computeHeading(left, down)).isWithin(1e-6).of(-180);

        assertThat(SphericalUtil.computeHeading(right, front)).isWithin(1e-6).of(-90);
        assertThat(SphericalUtil.computeHeading(left, front)).isWithin(1e-6).of(90);

        assertThat(SphericalUtil.computeHeading(front, right)).isWithin(1e-6).of(90);
        assertThat(SphericalUtil.computeHeading(back, right)).isWithin(1e-6).of(-90);
    }

    @Test
    public void testComputeOffset() {
        // From front
        expectLatLngApproxEquals(front, SphericalUtil.computeOffset(front, 0, 0));
        expectLatLngApproxEquals(up, SphericalUtil.computeOffset(front, Math.PI * EARTH_RADIUS / 2, 0));
        expectLatLngApproxEquals(down, SphericalUtil.computeOffset(front, Math.PI * EARTH_RADIUS / 2, 180));
        expectLatLngApproxEquals(left, SphericalUtil.computeOffset(front, Math.PI * EARTH_RADIUS / 2, -90));
        expectLatLngApproxEquals(right, SphericalUtil.computeOffset(front, Math.PI * EARTH_RADIUS / 2, 90));
        expectLatLngApproxEquals(back, SphericalUtil.computeOffset(front, Math.PI * EARTH_RADIUS, 0));
        expectLatLngApproxEquals(back, SphericalUtil.computeOffset(front, Math.PI * EARTH_RADIUS, 90));

        // From left
        expectLatLngApproxEquals(left, SphericalUtil.computeOffset(left, 0, 0));
        expectLatLngApproxEquals(up, SphericalUtil.computeOffset(left, Math.PI * EARTH_RADIUS / 2, 0));
        expectLatLngApproxEquals(down, SphericalUtil.computeOffset(left, Math.PI * EARTH_RADIUS / 2, 180));
        expectLatLngApproxEquals(front, SphericalUtil.computeOffset(left, Math.PI * EARTH_RADIUS / 2, 90));
        expectLatLngApproxEquals(back, SphericalUtil.computeOffset(left, Math.PI * EARTH_RADIUS / 2, -90));
        expectLatLngApproxEquals(right, SphericalUtil.computeOffset(left, Math.PI * EARTH_RADIUS, 0));
        expectLatLngApproxEquals(right, SphericalUtil.computeOffset(left, Math.PI * EARTH_RADIUS, 90));

        // NOTE(appleton): Heading is undefined at the poles, so we do not test
        // from up/down.
    }

    @Test
    public void testComputeOffsetOrigin() {
        expectLatLngApproxEquals(front, Objects.requireNonNull(SphericalUtil.computeOffsetOrigin(front, 0, 0)));

        expectLatLngApproxEquals(front, Objects.requireNonNull(SphericalUtil.computeOffsetOrigin(new LatLng(0, 45), Math.PI * EARTH_RADIUS / 4, 90)));
        expectLatLngApproxEquals(front, Objects.requireNonNull(SphericalUtil.computeOffsetOrigin(new LatLng(0, -45), Math.PI * EARTH_RADIUS / 4, -90)));
        expectLatLngApproxEquals(front, Objects.requireNonNull(SphericalUtil.computeOffsetOrigin(new LatLng(45, 0), Math.PI * EARTH_RADIUS / 4, 0)));
        expectLatLngApproxEquals(front, Objects.requireNonNull(SphericalUtil.computeOffsetOrigin(new LatLng(-45, 0), Math.PI * EARTH_RADIUS / 4, 180)));

        // Situations with no solution, should return null.
        //
        // First 'over' the pole.
        assertThat(SphericalUtil.computeOffsetOrigin(new LatLng(80, 0), Math.PI * EARTH_RADIUS / 4, 180)).isNull();
        // Second a distance that doesn't fit on the earth.
        assertThat(SphericalUtil.computeOffsetOrigin(new LatLng(80, 0), Math.PI * EARTH_RADIUS / 4, 90)).isNull();
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
        expectLatLngApproxEquals(start, Objects.requireNonNull(SphericalUtil.computeOffsetOrigin(end, distance, heading)));

        heading = -37;
        end = SphericalUtil.computeOffset(start, distance, heading);
        expectLatLngApproxEquals(start, Objects.requireNonNull(SphericalUtil.computeOffsetOrigin(end, distance, heading)));

        distance = 3.8e+7;
        end = SphericalUtil.computeOffset(start, distance, heading);
        expectLatLngApproxEquals(start, Objects.requireNonNull(SphericalUtil.computeOffsetOrigin(end, distance, heading)));

        start = new LatLng(-21, -73);
        end = SphericalUtil.computeOffset(start, distance, heading);
        expectLatLngApproxEquals(start, Objects.requireNonNull(SphericalUtil.computeOffsetOrigin(end, distance, heading)));

        // computeOffsetOrigin with multiple solutions, all we care about is that
        // going from there yields the requested result.
        //
        // First, for this particular situation the latitude is completely arbitrary.
        start = SphericalUtil.computeOffsetOrigin(new LatLng(0, 90), Math.PI * EARTH_RADIUS / 2, 90);
        Assert.assertNotNull(start);
        expectLatLngApproxEquals(new LatLng(0, 90), SphericalUtil.computeOffset(start, Math.PI * EARTH_RADIUS / 2, 90));

        // Second, for this particular situation the longitude is completely
        // arbitrary.
        start = SphericalUtil.computeOffsetOrigin(new LatLng(90, 0), Math.PI * EARTH_RADIUS / 4, 0);
        Assert.assertNotNull(start);
        expectLatLngApproxEquals(new LatLng(90, 0), SphericalUtil.computeOffset(start, Math.PI * EARTH_RADIUS / 4, 0));
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
        expectLatLngApproxEquals(new LatLng(89, 0), SphericalUtil.interpolate(front, up, 89 / 90.0));
        expectLatLngApproxEquals(new LatLng(89, 0), SphericalUtil.interpolate(up, front, 1 / 90.0));

        // Between front and down
        expectLatLngApproxEquals(new LatLng(-1, 0), SphericalUtil.interpolate(front, down, 1 / 90.0));
        expectLatLngApproxEquals(new LatLng(-1, 0), SphericalUtil.interpolate(down, front, 89 / 90.0));
        expectLatLngApproxEquals(new LatLng(-89, 0), SphericalUtil.interpolate(front, down, 89 / 90.0));
        expectLatLngApproxEquals(new LatLng(-89, 0), SphericalUtil.interpolate(down, front, 1 / 90.0));

        // Between left and back
        expectLatLngApproxEquals(new LatLng(0, -91), SphericalUtil.interpolate(left, back, 1 / 90.0));
        expectLatLngApproxEquals(new LatLng(0, -91), SphericalUtil.interpolate(back, left, 89 / 90.0));
        expectLatLngApproxEquals(new LatLng(0, -179), SphericalUtil.interpolate(left, back, 89 / 90.0));
        expectLatLngApproxEquals(new LatLng(0, -179), SphericalUtil.interpolate(back, left, 1 / 90.0));

        // geodesic crosses pole
        expectLatLngApproxEquals(up, SphericalUtil.interpolate(new LatLng(45, 0), new LatLng(45, 180), 1 / 2.0));
        expectLatLngApproxEquals(down, SphericalUtil.interpolate(new LatLng(-45, 0), new LatLng(-45, 180), 1 / 2.0));

        // boundary values for fraction, between left and back
        expectLatLngApproxEquals(left, SphericalUtil.interpolate(left, back, 0));
        expectLatLngApproxEquals(back, SphericalUtil.interpolate(left, back, 1.0));

        // two nearby points, separated by ~4m, for which the Slerp algorithm is not stable and we
        // have to fall back to linear interpolation.
        expectLatLngApproxEquals(new LatLng(-37.756872, 175.325252), SphericalUtil.interpolate(new LatLng(-37.756891, 175.325262), new LatLng(-37.756853, 175.325242), 0.5));
    }

    @Test
    public void testComputeLength() {
        List<LatLng> latLngs;

        assertThat(SphericalUtil.computeLength(Collections.emptyList())).isWithin(1e-6).of(0);
        assertThat(SphericalUtil.computeLength(List.of(new LatLng(0, 0)))).isWithin(1e-6).of(0);

        latLngs = Arrays.asList(new LatLng(0, 0), new LatLng(0.1, 0.1));
        assertThat(SphericalUtil.computeLength(latLngs)).isWithin(1).of(Math.toRadians(0.1) * Math.sqrt(2) * EARTH_RADIUS);

        latLngs = Arrays.asList(new LatLng(0, 0), new LatLng(90, 0), new LatLng(0, 90));
        assertThat(SphericalUtil.computeLength(latLngs)).isWithin(1e-6).of(Math.PI * EARTH_RADIUS);
    }

    @Test
    public void testIsCCW() {
        // One face of the octahedron
        assertThat(isCCW(right, up, front)).isEqualTo(1);
        assertThat(isCCW(up, front, right)).isEqualTo(1);
        assertThat(isCCW(front, right, up)).isEqualTo(1);
        assertThat(isCCW(front, up, right)).isEqualTo(-1);
        assertThat(isCCW(up, right, front)).isEqualTo(-1);
        assertThat(isCCW(right, front, up)).isEqualTo(-1);
    }

    @Test
    public void testComputeTriangleArea() {
        assertThat(computeTriangleArea(right, up, front)).isWithin(1e-6).of(Math.PI / 2);
        assertThat(computeTriangleArea(front, up, right)).isWithin(1e-6).of(Math.PI / 2);

        // computeArea returns area of zero on small polys
        double area = computeTriangleArea(new LatLng(0, 0), new LatLng(0, Math.toDegrees(1E-6)), new LatLng(Math.toDegrees(1E-6), 0));
        double expectedArea = 1E-12 / 2;

        assertThat(Math.abs(expectedArea - area)).isLessThan(1e-20);
    }

    @Test
    public void testComputeSignedTriangleArea() {
        assertThat(computeSignedTriangleArea(new LatLng(0, 0), new LatLng(0, 0.1), new LatLng(0.1, 0.1))).isWithin(1e-6).of(Math.toRadians(0.1) * Math.toRadians(0.1) / 2);

        assertThat(computeSignedTriangleArea(right, up, front)).isWithin(1e-6).of(Math.PI / 2);

        assertThat(computeSignedTriangleArea(front, up, right)).isWithin(1e-6).of(-Math.PI / 2);
    }

    @Test
    public void testComputeArea() {
        assertThat(SphericalUtil.computeArea(Arrays.asList(right, up, front, down, right))).isWithin(.4).of(Math.PI * EARTH_RADIUS * EARTH_RADIUS);

        assertThat(SphericalUtil.computeArea(Arrays.asList(right, down, front, up, right))).isWithin(.4).of(Math.PI * EARTH_RADIUS * EARTH_RADIUS);
    }

    @Test
    public void testComputeSignedArea() {
        List<LatLng> path = Arrays.asList(right, up, front, down, right);
        List<LatLng> pathReversed = Arrays.asList(right, down, front, up, right);
        assertThat(-SphericalUtil.computeSignedArea(path)).isWithin(0).of(SphericalUtil.computeSignedArea(pathReversed));
    }
}