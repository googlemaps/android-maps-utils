package com.google.maps.android.geoJsonLayer;

import com.google.android.gms.maps.model.LatLng;

import junit.framework.TestCase;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;

public class GeoJsonParserTest extends TestCase {

    public void setUp() throws Exception {
        super.setUp();

    }

    public void tearDown() throws Exception {

    }

    public void testParseGeoJson() throws Exception {
        JSONObject geoJsonObject = new JSONObject("{ \"type\": \"MultiLineString\",\n"
                + "    \"coordinates\": [\n"
                + "        [ [100.0, 0.0], [101.0, 1.0] ],\n"
                + "        [ [102.0, 2.0], [103.0, 3.0] ]\n"
                + "      ]\n"
                + "    }");

        GeoJsonParser parser = new GeoJsonParser(geoJsonObject);
        parser.parseGeoJson();
        LineString ls1 = new LineString(new ArrayList<LatLng>(Arrays.asList(new LatLng(0, 100), new LatLng(1, 101))));
        LineString ls2 = new LineString(new ArrayList<LatLng>(Arrays.asList(new LatLng(2, 102), new LatLng(3, 103))));
        MultiLineString multiLineString = new MultiLineString(new ArrayList<LineString>(Arrays.asList(ls1, ls2)));
        Feature feature = new Feature(multiLineString, null, null);
        ArrayList<Feature> features = new ArrayList<Feature>(Arrays.asList(feature));
        assertEquals(features.get(0).getId(), parser.getFeatures().get(0).getId());
    }

    public void testGetFeatures() throws Exception {

    }
}