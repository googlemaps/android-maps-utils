package com.google.maps.android.data.geojson;

import com.google.android.gms.maps.model.LatLng;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

public class GeoJsonMultiLineStringTest {
    private GeoJsonMultiLineString mls;

    @Test
    public void testGetType() {
        List<GeoJsonLineString> lineStrings = new ArrayList<>();
        lineStrings.add(
                new GeoJsonLineString(
                        new ArrayList<>(Arrays.asList(new LatLng(0, 0), new LatLng(50, 50)))));
        lineStrings.add(
                new GeoJsonLineString(
                        new ArrayList<>(Arrays.asList(new LatLng(80, 10), new LatLng(-54, 12.7)))));
        mls = new GeoJsonMultiLineString(lineStrings);
        assertEquals("MultiLineString", mls.getType());
    }

    @Test
    public void testGetLineStrings() {
        List<GeoJsonLineString> lineStrings = new ArrayList<>();
        lineStrings.add(
                new GeoJsonLineString(
                        new ArrayList<>(Arrays.asList(new LatLng(0, 0), new LatLng(50, 50)))));
        lineStrings.add(
                new GeoJsonLineString(
                        new ArrayList<>(Arrays.asList(new LatLng(80, 10), new LatLng(-54, 12.7)))));
        mls = new GeoJsonMultiLineString(lineStrings);
        assertEquals(lineStrings, mls.getLineStrings());

        lineStrings = new ArrayList<>();
        mls = new GeoJsonMultiLineString(lineStrings);
        assertEquals(new ArrayList<GeoJsonLineString>(), mls.getLineStrings());

        try {
            mls = new GeoJsonMultiLineString(null);
            fail();
        } catch (IllegalArgumentException e) {
            assertEquals("Geometries cannot be null", e.getMessage());
        }
    }
}
