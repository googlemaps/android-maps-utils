package com.google.maps.android.geojsonkmlabs.geojson;

import com.google.android.gms.maps.model.LatLng;

import junit.framework.TestCase;

import java.util.ArrayList;

public class GeoJsonLineStringTest extends TestCase {

    GeoJsonLineString ls;

    public void testGetType() throws Exception {
        ArrayList<LatLng> coordinates = new ArrayList<LatLng>();
        coordinates.add(new LatLng(0, 0));
        coordinates.add(new LatLng(50, 50));
        coordinates.add(new LatLng(100, 100));
        ls = new GeoJsonLineString(coordinates);
        assertEquals("LineString", ls.getType());
    }

    public void testGetCoordinates() throws Exception {
        ArrayList<LatLng> coordinates = new ArrayList<LatLng>();
        coordinates.add(new LatLng(0, 0));
        coordinates.add(new LatLng(50, 50));
        coordinates.add(new LatLng(100, 100));
        ls = new GeoJsonLineString(coordinates);
        assertEquals(coordinates, ls.getCoordinates());

        try {
            ls = new GeoJsonLineString(null);
            fail();
        } catch (IllegalArgumentException e) {
            assertEquals("Coordinates cannot be null", e.getMessage());
        }
    }
}