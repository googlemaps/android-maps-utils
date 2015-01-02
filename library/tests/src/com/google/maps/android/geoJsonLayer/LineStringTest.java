package com.google.maps.android.geoJsonLayer;

import com.google.android.gms.maps.model.LatLng;

import junit.framework.TestCase;

import java.util.ArrayList;

public class LineStringTest extends TestCase {
    LineString ls;

    public void testGetType() throws Exception {
        ArrayList<LatLng> coordinates = new ArrayList<LatLng>();
        coordinates.add(new LatLng(0, 0));
        coordinates.add(new LatLng(50, 50));
        coordinates.add(new LatLng(100, 100));
        ls = new LineString(coordinates);
        assertEquals("LineString", ls.getType());
    }

    public void testGetCoordinates() throws Exception {
        ArrayList<LatLng> coordinates = new ArrayList<LatLng>();
        coordinates.add(new LatLng(0, 0));
        coordinates.add(new LatLng(50, 50));
        coordinates.add(new LatLng(100, 100));
        ls = new LineString(coordinates);
        assertEquals(coordinates, ls.getCoordinates());
    }
}