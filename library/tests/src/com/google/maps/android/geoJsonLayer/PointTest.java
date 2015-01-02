package com.google.maps.android.geoJsonLayer;

import com.google.android.gms.maps.model.LatLng;

import junit.framework.TestCase;

public class PointTest extends TestCase {
    Point p;

    public void testGetType() throws Exception {
        p = new Point(new LatLng(0 ,0));
        assertEquals("Point", p.getType());
    }

    public void testGetCoordinates() throws Exception {
        p = new Point(new LatLng(0 ,0));
        assertEquals(new LatLng(0, 0), p.getCoordinates());
        try {
            p = new Point(null);
            fail();
        } catch (IllegalArgumentException e) {
            assertEquals("Coordinates cannot be null", e.getMessage());
        }
    }
}