package com.google.maps.android.heatmaps;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.geometry.Point;

import junit.framework.Assert;
import junit.framework.TestCase;


/**
 * Tests for the WeightedLatLng class
 */
public class WeightedLatLngTest extends TestCase {
    // Points for testing
    private final WeightedLatLng a = new WeightedLatLng(new LatLng(0, 0));
    private final WeightedLatLng b = new WeightedLatLng(new LatLng(90, 0));
    private final WeightedLatLng c = new WeightedLatLng(new LatLng(-90, 0));

    // Copied from SphericalUtilTest
    private static void expectNearNumber(double actual, double expected, double epsilon) {
        Assert.assertTrue(String.format("Expected %g to be near %g", actual, expected),
                Math.abs(expected - actual) <= epsilon);
    }

    private static void assertEquals(WeightedLatLng expected, WeightedLatLng actual) {
        assertEquals(expected.getIntensity(), actual.getIntensity());
        assertEquals(expected.getPoint(), actual.getPoint());
    }

    private static void assertEquals(Point expected, Point actual) {
        expectNearNumber(expected.x, actual.x, 1e-6);
        expectNearNumber(expected.x, actual.x, 1e-6);
    }

    public void testGetPoint() {
        // TODO: how/is this required? just trust cbro's code?
    }

    // Tests the special wraparound constructor
    public void testWraparound() {
        // adding nothing
        assertEquals(a, new WeightedLatLng(a, 0));
        assertEquals(b, new WeightedLatLng(b, 0));
        assertEquals(c, new WeightedLatLng(c, 0));
        //adding
        assertEquals(a.getPoint().x + 10, (new WeightedLatLng(a, 10)).getPoint().x);
        assertEquals(b.getPoint().x + 10, (new WeightedLatLng(b, 10)).getPoint().x);
        assertEquals(c.getPoint().x + 10, (new WeightedLatLng(c, 10)).getPoint().x);
        //subtracting
        assertEquals(a.getPoint().x - 10, (new WeightedLatLng(a, -10)).getPoint().x);
        assertEquals(b.getPoint().x - 10, (new WeightedLatLng(b, -10)).getPoint().x);
        assertEquals(c.getPoint().x - 10, (new WeightedLatLng(c, -10)).getPoint().x);
    }

}
