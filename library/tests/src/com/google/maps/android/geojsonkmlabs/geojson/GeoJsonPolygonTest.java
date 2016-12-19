package com.google.maps.android.geojsonkmlabs.geojson;

import com.google.android.gms.maps.model.LatLng;

import junit.framework.TestCase;

import java.util.ArrayList;
import java.util.Arrays;

public class GeoJsonPolygonTest extends TestCase {

    GeoJsonPolygon p;

    public void testGetType() throws Exception {
        ArrayList<ArrayList<LatLng>> coordinates = new ArrayList<ArrayList<LatLng>>();
        coordinates.add(new ArrayList<LatLng>(
                Arrays.asList(new LatLng(0, 0), new LatLng(20, 20), new LatLng(60, 60),
                        new LatLng(0, 0))));
        p = new GeoJsonPolygon(coordinates);
        assertEquals("Polygon", p.getType());
    }

    public void testGetCoordinates() throws Exception {
        // No holes
        ArrayList<ArrayList<LatLng>> coordinates = new ArrayList<ArrayList<LatLng>>();
        coordinates.add(new ArrayList<LatLng>(
                Arrays.asList(new LatLng(0, 0), new LatLng(20, 20), new LatLng(60, 60),
                        new LatLng(0, 0))));
        p = new GeoJsonPolygon(coordinates);
        assertEquals(coordinates, p.getCoordinates());

        // Holes
        coordinates.add(new ArrayList<LatLng>(
                Arrays.asList(new LatLng(0, 0), new LatLng(20, 20), new LatLng(60, 60),
                        new LatLng(0, 0))));
        p = new GeoJsonPolygon(coordinates);


        try {
            p = new GeoJsonPolygon(null);
            fail();
        } catch (IllegalArgumentException e) {
            assertEquals("Coordinates cannot be null", e.getMessage());
        }

    }
}