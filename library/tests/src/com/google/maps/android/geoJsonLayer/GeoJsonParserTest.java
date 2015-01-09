package com.google.maps.android.geoJsonLayer;

import com.google.android.gms.maps.model.LatLng;

import junit.framework.TestCase;

import org.json.JSONArray;
import org.json.JSONException;
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
        GeoJsonLineString ls1 = new GeoJsonLineString(new ArrayList<LatLng>(Arrays.asList(new LatLng(0, 100), new LatLng(1, 101))));
        GeoJsonLineString ls2 = new GeoJsonLineString(new ArrayList<LatLng>(Arrays.asList(new LatLng(2, 102), new LatLng(3, 103))));
        GeoJsonMultiLineString geoJsonMultiLineString = new GeoJsonMultiLineString(new ArrayList<GeoJsonLineString>(Arrays.asList(ls1, ls2)));
        GeoJsonFeature geoJsonFeature = new GeoJsonFeature(geoJsonMultiLineString, null, null, null);
        ArrayList<GeoJsonFeature> geoJsonFeatures = new ArrayList<GeoJsonFeature>(Arrays.asList(geoJsonFeature));
        assertEquals(geoJsonFeatures.get(0).getId(), parser.getFeatures().get(0).getId());
    }

    public JSONObject createGeometryCollection () throws Exception {
        return new JSONObject (
            "{\n" +
            "   \"type\": \"Feature\",\n" +
            "   \"id\": \"Popsicles\",\n" +
            "   \"geometry\": {\n" +
            "      \"type\": \"GeometryCollection\",\n" +
            "      \"geometries\": [\n" +
            "          {\n" +
            "            \"type\": \"Point\",\n" +
            "            \"coordinates\": [100.0, 0.0]\n" +
            "          },\n" +
            "          { \"type\": \"GeometryCollection\",\n" +
            "            \"geometries\": [\n" +
            "              { \"type\": \"Point\",\n" +
            "                \"coordinates\": [103.0, 0.0]\n" +
            "                }\n" +
            "            ]\n" +
            "          }\n" +
            "      ]\n" +
            "   },\n" +
            "   \"properties\": {\n" +
            "       \"prop0\": \"value0\",\n" +
            "       \"prop1\": \"value1\"\n" +
            "   }\n" +
            "}");
    }

    //Testing for nested geometry collections
    public void testParseGeometryCollection () throws Exception {
        JSONObject geometryCollectionObject = createGeometryCollection();
        GeoJsonParser parser = new GeoJsonParser(geometryCollectionObject);
        parser.parseGeoJson();
        assertTrue(parser.getFeatures().size() == 1);
        for (GeoJsonFeature feature : parser.getFeatures()) {
            assertTrue(feature.getGeometry().equals("GeometryCollection"));
            assertTrue(feature.getProperties().size() == 2);
            assertTrue(feature.getId().equals("Popsicles"));
            GeoJsonGeometryCollection geometry = ((GeoJsonGeometryCollection) feature.getGeometry());
            assertTrue (geometry.getGeometries().size() == 2);
            for (GeoJsonGeometry geoJsonGeometry : geometry.getGeometries()) {
                assertTrue (geoJsonGeometry.getType().equals("Point"));
            }
        }
    }

    public void testGetFeatures() throws Exception {

    }
}