package com.google.maps.android.geojsonkmlabs.geojson;

import com.google.android.gms.maps.model.LatLng;

import junit.framework.TestCase;

import java.util.ArrayList;
import java.util.Arrays;

public class GeoJsonMultiLineStringTest extends TestCase {

    GeoJsonMultiLineString mls;

    public void testGetType() throws Exception {
        ArrayList<GeoJsonLineString> lineStrings = new ArrayList<GeoJsonLineString>();
        lineStrings.add(new GeoJsonLineString(
                new ArrayList<LatLng>(Arrays.asList(new LatLng(0, 0), new LatLng(50, 50)))));
        lineStrings.add(new GeoJsonLineString(
                new ArrayList<LatLng>(Arrays.asList(new LatLng(80, 10), new LatLng(-54, 12.7)))));
        mls = new GeoJsonMultiLineString(lineStrings);
        assertEquals("MultiLineString", mls.getType());
    }

    public void testGetLineStrings() throws Exception {
        ArrayList<GeoJsonLineString> lineStrings = new ArrayList<GeoJsonLineString>();
        lineStrings.add(new GeoJsonLineString(
                new ArrayList<LatLng>(Arrays.asList(new LatLng(0, 0), new LatLng(50, 50)))));
        lineStrings.add(new GeoJsonLineString(
                new ArrayList<LatLng>(Arrays.asList(new LatLng(80, 10), new LatLng(-54, 12.7)))));
        mls = new GeoJsonMultiLineString(lineStrings);
        assertEquals(lineStrings, mls.getLineStrings());

        lineStrings = new ArrayList<GeoJsonLineString>();
        mls = new GeoJsonMultiLineString(lineStrings);
        assertEquals(new ArrayList<GeoJsonLineString>(), mls.getLineStrings());

        try {
            mls = new GeoJsonMultiLineString(null);
            fail();
        } catch (IllegalArgumentException e) {
            assertEquals("GeoJsonLineStrings cannot be null", e.getMessage());
        }
    }
}