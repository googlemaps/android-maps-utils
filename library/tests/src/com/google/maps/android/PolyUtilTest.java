package com.google.maps.android;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.PolyUtil;

import junit.framework.Assert;
import junit.framework.TestCase;

import java.lang.String;
import java.util.List;
import java.util.Arrays;
import java.util.ArrayList;

public class PolyUtilTest extends TestCase {
    private static final String TEST_LINE = "_cqeFf~cjVf@p@fA}AtAoB`ArAx@hA`GbIvDiFv@gAh@t@X\\|@z@`@Z\\Xf@Vf@VpA\\tATJ@NBBkC";

    private static void expectNearNumber(double expected, double actual, double epsilon) {
        Assert.assertTrue(String.format("Expected %f to be near %f", actual, expected),
                Math.abs(expected - actual) <= epsilon);
    }

    private static List<LatLng> makeList(double... coords) {
        int size = coords.length / 2;
        ArrayList<LatLng> list = new ArrayList<LatLng>(size);
        for (int i = 0; i < size; ++i) {
            list.add(new LatLng(coords[i + i], coords[i + i + 1]));
        }
        return list;
    }
    
    private static void containsCase(List<LatLng> poly, List<LatLng> yes, List<LatLng> no) {
        for (LatLng point : yes) {
            Assert.assertTrue(PolyUtil.containsLocation(point, poly, true));
            Assert.assertTrue(PolyUtil.containsLocation(point, poly, false));
        }
        for (LatLng point : no) {
            Assert.assertFalse(PolyUtil.containsLocation(point, poly, true));
            Assert.assertFalse(PolyUtil.containsLocation(point, poly, false));
        }
    }

    public void testContainsLocation() {
        // Empty.
        containsCase(makeList(),
                     makeList(),
                     makeList(0, 0));

        // One point.
        containsCase(makeList(1, 2),
                     makeList(1, 2),
                     makeList(0, 0));

        // Two points.
        containsCase(makeList(1, 2, 3, 5),
                     makeList(1, 2, 3, 5),
                     makeList(0, 0, 40, 4));

        // Some arbitrary triangle.
        containsCase(makeList(0., 0., 10., 12., 20., 5.),
                     makeList(10., 12., 10, 11, 19, 5),
                     makeList(0, 1, 11, 12, 30, 5, 0, -180, 0, 90));

        // Around North Pole.
        containsCase(makeList(89, 0, 89, 120, 89, -120),
                     makeList(90, 0, 90, 180, 90, -90),
                     makeList(-90, 0, 0, 0));

        // Around South Pole.
        containsCase(makeList(-89, 0, -89, 120, -89, -120),
                    makeList(90, 0, 90, 180, 90, -90, 0, 0),
                    makeList(-90, 0, -90, 90));

        // Over/under segment on meridian and equator.
        containsCase(makeList(5, 10, 10, 10, 0, 20, 0, -10),
                     makeList(2.5, 10, 1, 0),
                     makeList(15, 10, 0, -15, 0, 25, -1, 0));
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
