package com.google.maps.android.geojsonkmlabs.geojson;

import com.google.android.gms.maps.model.LatLng;

import junit.framework.TestCase;

public class GeoJsonPointTest extends TestCase {

    GeoJsonPoint p;

    public void testGetType() throws Exception {
        p = new GeoJsonPoint(new LatLng(0, 0));
        assertEquals("Point", p.getType());
    }

    public void testGetCoordinates() throws Exception {
        p = new GeoJsonPoint(new LatLng(0, 0));
        assertEquals(new LatLng(0, 0), p.getCoordinates());
        try {
            p = new GeoJsonPoint(null);
            fail();
        } catch (IllegalArgumentException e) {
            assertEquals("Coordinate cannot be null", e.getMessage());
        }
    }
}