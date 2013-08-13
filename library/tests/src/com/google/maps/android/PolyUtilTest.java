package com.google.maps.android;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.PolyUtil;

import junit.framework.Assert;
import junit.framework.TestCase;

import java.lang.String;
import java.util.List;

public class PolyUtilTest extends TestCase {
    private static final String TEST_LINE = "_cqeFf~cjVf@p@fA}AtAoB`ArAx@hA`GbIvDiFv@gAh@t@X\\|@z@`@Z\\Xf@Vf@VpA\\tATJ@NBBkC";

    private static void expectNearNumber(double expected, double actual, double epsilon) {
        Assert.assertTrue(String.format("Expected %f to be near %f", actual, expected),
                Math.abs(expected - actual) <= epsilon);
    }

    public void testDecodePath() {
        List<LatLng> latLngs = PolyUtil.decode(TEST_LINE);

        int expectedLength = 21;
        Assert.assertEquals("Wrong length.", expectedLength, latLngs.size());

        LatLng lastPoint = latLngs.get(expectedLength - 1);
        expectNearNumber(37.76953, lastPoint.latitude, 1e-6);
        expectNearNumber(-122.41488, lastPoint.longitude, 1e-6);
    }

    public void testEncodePath() {
        List<LatLng> path = PolyUtil.decode(TEST_LINE);
        String encoded = PolyUtil.encode(path);
        Assert.assertEquals(TEST_LINE, encoded);
    }
}
