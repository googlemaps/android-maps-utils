package com.google.maps.android.data.geojson;

import com.google.android.gms.maps.model.LatLng;

import org.junit.Test;
import org.junit.Assert;

public class GeoJsonPointTest {

    GeoJsonPoint p;

    @Test
    public void testGetType() throws Exception {
        p = new GeoJsonPoint(new LatLng(0, 0));
        Assert.assertEquals("Point", p.getType());
    }

    @Test
    public void testGetCoordinates() throws Exception {
        p = new GeoJsonPoint(new LatLng(0, 0));
        Assert.assertEquals(new LatLng(0, 0), p.getCoordinates());
        try {
            p = new GeoJsonPoint(null);
            Assert.fail();
        } catch (IllegalArgumentException e) {
            Assert.assertEquals("Coordinates cannot be null", e.getMessage());
        }
    }

    @Test
    public void testGetAltitude() throws Exception {
        p = new GeoJsonPoint(new LatLng(0, 0), new Double(100));
        Assert.assertEquals(new Double(100), p.getAltitude());
    }
}