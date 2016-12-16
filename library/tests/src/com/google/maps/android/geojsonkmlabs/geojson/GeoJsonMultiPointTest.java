package com.google.maps.android.geojsonkmlabs.geojson;

import com.google.android.gms.maps.model.LatLng;

import junit.framework.TestCase;

import java.util.ArrayList;

public class GeoJsonMultiPointTest extends TestCase {

    GeoJsonMultiPoint mp;

    public void testGetType() throws Exception {
        ArrayList<GeoJsonPoint> points = new ArrayList<GeoJsonPoint>();
        points.add(new GeoJsonPoint(new LatLng(0, 0)));
        points.add(new GeoJsonPoint(new LatLng(5, 5)));
        points.add(new GeoJsonPoint(new LatLng(10, 10)));
        mp = new GeoJsonMultiPoint(points);
        assertEquals("MultiPoint", mp.getType());
    }

    public void testGetPoints() throws Exception {
        ArrayList<GeoJsonPoint> points = new ArrayList<GeoJsonPoint>();
        points.add(new GeoJsonPoint(new LatLng(0, 0)));
        points.add(new GeoJsonPoint(new LatLng(5, 5)));
        points.add(new GeoJsonPoint(new LatLng(10, 10)));
        mp = new GeoJsonMultiPoint(points);
        assertEquals(points, mp.getPoints());

        points = new ArrayList<GeoJsonPoint>();
        mp = new GeoJsonMultiPoint(points);
        assertEquals(new ArrayList<GeoJsonPoint>(), mp.getPoints());

        try {
            mp = new GeoJsonMultiPoint(null);
            fail();
        } catch (IllegalArgumentException e) {
            assertEquals("GeoJsonPoints cannot be null", e.getMessage());
        }
    }
}