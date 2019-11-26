package com.google.maps.android.data.geojson;

import com.google.android.gms.maps.model.LatLng;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

public class GeoJsonPolygonTest {
    private GeoJsonPolygon p;

    @Test
    public void testGetType() {
        List<List<LatLng>> coordinates = new ArrayList<>();
        coordinates.add(
                new ArrayList<>(
                        Arrays.asList(
                                new LatLng(0, 0),
                                new LatLng(20, 20),
                                new LatLng(60, 60),
                                new LatLng(0, 0))));
        p = new GeoJsonPolygon(coordinates);
        assertEquals("Polygon", p.getType());
    }

    @Test
    public void testGetCoordinates() {
        // No holes
        List<List<LatLng>> coordinates = new ArrayList<>();
        coordinates.add(
                new ArrayList<>(
                        Arrays.asList(
                                new LatLng(0, 0),
                                new LatLng(20, 20),
                                new LatLng(60, 60),
                                new LatLng(0, 0))));
        p = new GeoJsonPolygon(coordinates);
        assertEquals(coordinates, p.getCoordinates());

        // Holes
        coordinates.add(
                new ArrayList<>(
                        Arrays.asList(
                                new LatLng(0, 0),
                                new LatLng(20, 20),
                                new LatLng(60, 60),
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
