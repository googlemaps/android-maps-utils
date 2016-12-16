package com.google.maps.android.geojsonkmlabs.geojson;

import com.google.android.gms.maps.model.LatLng;

import junit.framework.TestCase;

import java.util.ArrayList;
import java.util.Arrays;

public class GeoJsonMultiPolygonTest extends TestCase {

    GeoJsonMultiPolygon mp;

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
        assertEquals("MultiPolygon", mp.getType());
    }

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
        assertEquals(polygons, mp.getPolygons());

        polygons = new ArrayList<GeoJsonPolygon>();
        mp = new GeoJsonMultiPolygon(polygons);
        assertEquals(new ArrayList<GeoJsonPolygon>(), mp.getPolygons());

        try {
            mp = new GeoJsonMultiPolygon(null);
            fail();
        } catch (IllegalArgumentException e) {
            assertEquals("GeoJsonPolygons cannot be null", e.getMessage());
        }
    }
}