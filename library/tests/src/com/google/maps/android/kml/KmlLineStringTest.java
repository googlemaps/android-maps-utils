package com.google.maps.android.kml;

import com.google.android.gms.maps.model.LatLng;

import junit.framework.TestCase;

import java.util.ArrayList;

public class KmlLineStringTest extends TestCase {
    KmlLineString ls;
    public void setUp() throws Exception {
        super.setUp();

    }

    public void tearDown() throws Exception {

    }

    public void testGetType() throws Exception {
        ArrayList<LatLng> coordinates = new ArrayList<LatLng>();
        coordinates.add(new LatLng(0, 0));
        coordinates.add(new LatLng(50, 50));
        coordinates.add(new LatLng(100, 100));
        ls = new KmlLineString(coordinates);
        assertEquals("LineString", ls.getKmlGeometryType());
    }

    public void testGetGeometry() throws Exception {

    }
}