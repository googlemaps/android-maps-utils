package com.google.maps.android.data;

import com.google.android.gms.maps.model.LatLng;

import org.junit.Test;

import static org.junit.Assert.*;

public class PointTest {
    @Test
    public void testGetGeometryType() {
        Point p = new Point(new LatLng(0, 50));
        assertEquals("Point", p.getGeometryType());
    }

    @Test
    public void testGetGeometryObject() {
        Point p = new Point(new LatLng(0, 50));
        assertEquals(new LatLng(0, 50), p.getGeometryObject());
        try {
            new Point(null);
            fail();
        } catch (IllegalArgumentException e) {
            assertEquals("Coordinates cannot be null", e.getMessage());
        }
    }
}
