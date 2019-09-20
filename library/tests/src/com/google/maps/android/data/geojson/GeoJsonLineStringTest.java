package com.google.maps.android.data.geojson;

import com.google.android.gms.maps.model.LatLng;

import org.junit.Test;
import org.junit.Assert;

import java.util.ArrayList;

public class GeoJsonLineStringTest {

    GeoJsonLineString ls;

    @Test
    public void testGetType() throws Exception {
        ArrayList<LatLng> coordinates = new ArrayList<LatLng>();
        coordinates.add(new LatLng(0, 0));
        coordinates.add(new LatLng(50, 50));
        coordinates.add(new LatLng(100, 100));
        ls = new GeoJsonLineString(coordinates);
        Assert.assertEquals("LineString", ls.getType());
    }

    @Test
    public void testGetCoordinates() throws Exception {
        ArrayList<LatLng> coordinates = new ArrayList<LatLng>();
        coordinates.add(new LatLng(0, 0));
        coordinates.add(new LatLng(50, 50));
        coordinates.add(new LatLng(100, 100));
        ls = new GeoJsonLineString(coordinates);
        Assert.assertEquals(coordinates, ls.getCoordinates());

        try {
            ls = new GeoJsonLineString(null);
            Assert.fail();
        } catch (IllegalArgumentException e) {
            Assert.assertEquals("Coordinates cannot be null", e.getMessage());
        }
    }

    @Test
    public void testGetAltitudes() throws Exception {
        ArrayList<LatLng> coordinates = new ArrayList<LatLng>();
        coordinates.add(new LatLng(0, 0));
        coordinates.add(new LatLng(50, 50));
        coordinates.add(new LatLng(100, 100));
        ArrayList<Double> altitudes = new ArrayList<Double>();
        altitudes.add(new Double(100));
        altitudes.add(new Double(200));
        altitudes.add(new Double(300));
        ls = new GeoJsonLineString(coordinates, altitudes);
        Assert.assertEquals(altitudes, ls.getAltitudes());
        Assert.assertEquals(ls.getAltitudes().get(0), 100.0, 0);
        Assert.assertEquals(ls.getAltitudes().get(1), 200.0, 0);
        Assert.assertEquals(ls.getAltitudes().get(2), 300.0, 0);
    }
}