package com.google.maps.android.data.geojson;

import com.google.android.gms.maps.model.LatLng;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class GeoJsonMultiPointTest {
    private GeoJsonMultiPoint mp;

    @Test
    public void testGetType() {
        List<GeoJsonPoint> points = new ArrayList<>();
        points.add(new GeoJsonPoint(new LatLng(0, 0)));
        points.add(new GeoJsonPoint(new LatLng(5, 5)));
        points.add(new GeoJsonPoint(new LatLng(10, 10)));
        mp = new GeoJsonMultiPoint(points);
        assertEquals("MultiPoint", mp.getType());
    }

    @Test
    public void testGetPoints() {
        List<GeoJsonPoint> points = new ArrayList<>();
        points.add(new GeoJsonPoint(new LatLng(0, 0)));
        points.add(new GeoJsonPoint(new LatLng(5, 5)));
        points.add(new GeoJsonPoint(new LatLng(10, 10)));
        mp = new GeoJsonMultiPoint(points);
        assertEquals(points, mp.getPoints());

        points = new ArrayList<>();
        mp = new GeoJsonMultiPoint(points);
        assertEquals(new ArrayList<GeoJsonPoint>(), mp.getPoints());

        try {
            mp = new GeoJsonMultiPoint(null);
            fail();
        } catch (IllegalArgumentException e) {
            assertEquals("Geometries cannot be null", e.getMessage());
        }
    }
}
