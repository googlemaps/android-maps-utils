package com.google.maps.android.data.geojson;

import com.google.android.gms.maps.model.LatLng;

import org.junit.Test;
import org.junit.Assert;

import java.util.ArrayList;
import java.util.Arrays;

public class GeoJsonMultiPolygonTest {

    GeoJsonMultiPolygon mp;

    @Test
    public void testGetType() throws Exception {
        ArrayList<GeoJsonPolygon> polygons = new ArrayList<GeoJsonPolygon>();
        ArrayList<ArrayList<LatLng>> polygon = new ArrayList<ArrayList<LatLng>>();
        polygon.add(new ArrayList<LatLng>(
                Arrays.asList(new LatLng(0, 0), new LatLng(20, 20), new LatLng(60, 60),
                        new LatLng(0, 0))));
        polygons.add(new GeoJsonPolygon(polygon));
        polygon = new ArrayList<ArrayList<LatLng>>();
        polygon.add(new ArrayList<LatLng>(
                Arrays.asList(new LatLng(0, 0), new LatLng(50, 80), new LatLng(10, 15),
                        new LatLng(0, 0))));
        polygon.add(new ArrayList<LatLng>(
                Arrays.asList(new LatLng(0, 0), new LatLng(20, 20), new LatLng(60, 60),
                        new LatLng(0, 0))));
        polygons.add(new GeoJsonPolygon(polygon));
        mp = new GeoJsonMultiPolygon(polygons);
        Assert.assertEquals("MultiPolygon", mp.getType());
    }

    @Test
    public void testGetPolygons() throws Exception {
        ArrayList<GeoJsonPolygon> polygons = new ArrayList<GeoJsonPolygon>();
        ArrayList<ArrayList<LatLng>> polygon = new ArrayList<ArrayList<LatLng>>();
        polygon.add(new ArrayList<LatLng>(
                Arrays.asList(new LatLng(0, 0), new LatLng(20, 20), new LatLng(60, 60),
                        new LatLng(0, 0))));
        polygons.add(new GeoJsonPolygon(polygon));
        polygon = new ArrayList<ArrayList<LatLng>>();
        polygon.add(new ArrayList<LatLng>(
                Arrays.asList(new LatLng(0, 0), new LatLng(50, 80), new LatLng(10, 15),
                        new LatLng(0, 0))));
        polygon.add(new ArrayList<LatLng>(
                Arrays.asList(new LatLng(0, 0), new LatLng(20, 20), new LatLng(60, 60),
                        new LatLng(0, 0))));
        polygons.add(new GeoJsonPolygon(polygon));
        mp = new GeoJsonMultiPolygon(polygons);
        Assert.assertEquals(polygons, mp.getPolygons());

        polygons = new ArrayList<GeoJsonPolygon>();
        mp = new GeoJsonMultiPolygon(polygons);
        Assert.assertEquals(new ArrayList<GeoJsonPolygon>(), mp.getPolygons());

        try {
            mp = new GeoJsonMultiPolygon(null);
            Assert.fail();
        } catch (IllegalArgumentException e) {
            Assert.assertEquals("Geometries cannot be null", e.getMessage());
        }
    }
}