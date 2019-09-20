package com.google.maps.android.data.geojson;

import com.google.android.gms.maps.model.LatLng;

import org.junit.Test;
import org.junit.Assert;

import java.util.ArrayList;
import java.util.Arrays;

public class GeoJsonPolygonTest {

    GeoJsonPolygon p;

    @Test
    public void testGetType() throws Exception {
        ArrayList<ArrayList<LatLng>> coordinates = new ArrayList<ArrayList<LatLng>>();
        coordinates.add(new ArrayList<LatLng>(
                Arrays.asList(new LatLng(0, 0), new LatLng(20, 20), new LatLng(60, 60),
                        new LatLng(0, 0))));
        p = new GeoJsonPolygon(coordinates);
        Assert.assertEquals("Polygon", p.getType());
    }

    @Test
    public void testGetCoordinates() throws Exception {
        // No holes
        ArrayList<ArrayList<LatLng>> coordinates = new ArrayList<ArrayList<LatLng>>();
        coordinates.add(new ArrayList<LatLng>(
                Arrays.asList(new LatLng(0, 0), new LatLng(20, 20), new LatLng(60, 60),
                        new LatLng(0, 0))));
        p = new GeoJsonPolygon(coordinates);
        Assert.assertEquals(coordinates, p.getCoordinates());

        // Holes
        coordinates.add(new ArrayList<LatLng>(
                Arrays.asList(new LatLng(0, 0), new LatLng(20, 20), new LatLng(60, 60),
                        new LatLng(0, 0))));
        p = new GeoJsonPolygon(coordinates);


        try {
            p = new GeoJsonPolygon(null);
            Assert.fail();
        } catch (IllegalArgumentException e) {
            Assert.assertEquals("Coordinates cannot be null", e.getMessage());
        }

    }
}