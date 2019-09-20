package com.google.maps.android.data.geojson;

import com.google.android.gms.maps.model.LatLng;

import org.junit.Test;
import org.junit.Assert;

import java.util.ArrayList;

public class GeoJsonMultiPointTest {

    GeoJsonMultiPoint mp;

    @Test
    public void testGetType() throws Exception {
        ArrayList<GeoJsonPoint> points = new ArrayList<GeoJsonPoint>();
        points.add(new GeoJsonPoint(new LatLng(0, 0)));
        points.add(new GeoJsonPoint(new LatLng(5, 5)));
        points.add(new GeoJsonPoint(new LatLng(10, 10)));
        mp = new GeoJsonMultiPoint(points);
        Assert.assertEquals("MultiPoint", mp.getType());
    }

    @Test
    public void testGetPoints() throws Exception {
        ArrayList<GeoJsonPoint> points = new ArrayList<GeoJsonPoint>();
        points.add(new GeoJsonPoint(new LatLng(0, 0)));
        points.add(new GeoJsonPoint(new LatLng(5, 5)));
        points.add(new GeoJsonPoint(new LatLng(10, 10)));
        mp = new GeoJsonMultiPoint(points);
        Assert.assertEquals(points, mp.getPoints());

        points = new ArrayList<>();
        mp = new GeoJsonMultiPoint(points);
        Assert.assertEquals(new ArrayList<GeoJsonPoint>(), mp.getPoints());

        try {
            mp = new GeoJsonMultiPoint(null);
            Assert.fail();
        } catch (IllegalArgumentException e) {
            Assert.assertEquals("Geometries cannot be null", e.getMessage());
        }
    }
}