package com.google.maps.android.data.geojson;

import com.google.android.gms.maps.model.LatLng;

import org.junit.Test;
import org.junit.Assert;

import java.util.ArrayList;
import java.util.Arrays;

public class GeoJsonMultiLineStringTest {

    GeoJsonMultiLineString mls;

    @Test
    public void testGetType() throws Exception {
        ArrayList<GeoJsonLineString> lineStrings = new ArrayList<GeoJsonLineString>();
        lineStrings.add(new GeoJsonLineString(
                new ArrayList<LatLng>(Arrays.asList(new LatLng(0, 0), new LatLng(50, 50)))));
        lineStrings.add(new GeoJsonLineString(
                new ArrayList<LatLng>(Arrays.asList(new LatLng(80, 10), new LatLng(-54, 12.7)))));
        mls = new GeoJsonMultiLineString(lineStrings);
        Assert.assertEquals("MultiLineString", mls.getType());
    }

    @Test
    public void testGetLineStrings() throws Exception {
        ArrayList<GeoJsonLineString> lineStrings = new ArrayList<GeoJsonLineString>();
        lineStrings.add(new GeoJsonLineString(
                new ArrayList<LatLng>(Arrays.asList(new LatLng(0, 0), new LatLng(50, 50)))));
        lineStrings.add(new GeoJsonLineString(
                new ArrayList<LatLng>(Arrays.asList(new LatLng(80, 10), new LatLng(-54, 12.7)))));
        mls = new GeoJsonMultiLineString(lineStrings);
        Assert.assertEquals(lineStrings, mls.getLineStrings());

        lineStrings = new ArrayList<GeoJsonLineString>();
        mls = new GeoJsonMultiLineString(lineStrings);
        Assert.assertEquals(new ArrayList<GeoJsonLineString>(), mls.getLineStrings());

        try {
            mls = new GeoJsonMultiLineString(null);
            Assert.fail();
        } catch (IllegalArgumentException e) {
            Assert.assertEquals("Geometries cannot be null", e.getMessage());
        }
    }
}