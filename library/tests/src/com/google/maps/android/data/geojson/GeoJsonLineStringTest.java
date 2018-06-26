package com.google.maps.android.data.geojson;

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

    public void testGetAltitudes() throws Exception {
        ArrayList<LatLng> coordinates = new ArrayList<LatLng>();
        coordinates.add(new LatLng(0, 0));
        coordinates.add(new LatLng(50, 50));
        coordinates.add(new LatLng(100, 100));
        ArrayList<Double> altitudes = new ArrayList<Double>();
        altitudes.add(new Double(100));
        altitudes.add(new Double(200));
        altitudes.add(new Double(300));
        ls = new GeoJsonLineString(coordinates,altitudes);
        assertEquals(altitudes, ls.getAltitudes());
        assertEquals(ls.getAltitudes().get(0), 100.0);
        assertEquals(ls.getAltitudes().get(1), 200.0);
        assertEquals(ls.getAltitudes().get(2), 300.0);
    }
}