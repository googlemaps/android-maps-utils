package com.google.maps.android;

import com.google.android.gms.maps.model.LatLng;
import com.google.io.accelmap.directions.Route;
import junit.framework.Assert;
import org.junit.Test;

import java.util.List;

public class PolyTest {
    private static final String TEST_LINE = "_cqeFf~cjVf@p@fA}AtAoB`ArAx@hA`GbIvDiFv@gAh@t@X\\|@z@`@Z\\Xf@Vf@VpA\\tATJ@NBBkC";

    @Test
    public void test_decodePath() {
        Route r = new Route();
        r.overviewPolyline.points = TEST_LINE;
        List<LatLng> latLngs = r.overviewPolyline.asList();

        int expectedLength = 21;
        Assert.assertEquals("Wrong length.", expectedLength, latLngs.size());

        LatLng lastPoint = latLngs.get(expectedLength - 1);
        Assert.assertEquals(37.76953, lastPoint.latitude);
        Assert.assertEquals(-122.41488, lastPoint.longitude);
    }
}
