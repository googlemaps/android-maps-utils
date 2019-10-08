package com.google.maps.android.data.geojson;

import com.google.android.gms.maps.model.LatLng;

import org.junit.Test;

import static org.junit.Assert.*;

public class GeoJsonPointTest {
    @Test
    public void testGetType() {
        GeoJsonPoint p = new GeoJsonPoint(new LatLng(0, 0));
        assertEquals("Point", p.getType());
    }

    @Test
    public void testGetCoordinates() {
        GeoJsonPoint p = new GeoJsonPoint(new LatLng(0, 0));
        assertEquals(new LatLng(0, 0), p.getCoordinates());
        try {
            new GeoJsonPoint(null);
            fail();
        } catch (IllegalArgumentException e) {
            assertEquals("Coordinates cannot be null", e.getMessage());
        }
    }

    @Test
    public void testGetAltitude() {
        GeoJsonPoint p = new GeoJsonPoint(new LatLng(0, 0), 100d);
        assertEquals(new Double(100), p.getAltitude());
    }
}
