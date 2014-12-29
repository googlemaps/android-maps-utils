package com.google.maps.android.kml;

import com.google.android.gms.maps.model.LatLng;

import junit.framework.TestCase;

import java.util.ArrayList;

/**
 * Created by lavenderch on 12/19/14.
 */
public class LineStringPropertiesTest extends TestCase {

    LineStringProperties lineStringProperties = new LineStringProperties();

    public void testIsValidLineString() {
        ArrayList<LatLng> lineStringPoints = new ArrayList<LatLng>();

        lineStringPoints.add(new LatLng(5, 5));
        lineStringPoints.add(new LatLng(15, 15));
        assertTrue(lineStringProperties.isValidLineString(lineStringPoints));

        lineStringPoints.clear();

        lineStringPoints.add(new LatLng(5, 5));
        lineStringPoints.add(new LatLng(15, 15));
        lineStringPoints.add(new LatLng(25, 25));
        assertTrue(lineStringProperties.isValidLineString(lineStringPoints));

        lineStringPoints.clear();

        lineStringPoints.add(new LatLng(10, 10));
        assertFalse(lineStringProperties.isValidLineString(lineStringPoints));

        lineStringPoints.clear();

        assertFalse(lineStringProperties.isValidLineString(lineStringPoints));
    }

    public void testConvertToLatLng () {

        String[] point = {"0", "0"};
        LatLng latLng = new LatLng(0, 0);
        assertNotNull(lineStringProperties.convertToLatLng(point));
        assertEquals(lineStringProperties.convertToLatLng(point).latitude, latLng.latitude);
        assertEquals(lineStringProperties.convertToLatLng(point).longitude, latLng.longitude);


        String[] point1 = {"0", "0"};






    }





}
