package com.google.maps.android.geojson;

import com.google.android.gms.maps.model.LatLng;

import junit.framework.TestCase;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;

public class GeoJsonParserTest extends TestCase {

    public void setUp() throws Exception {
        super.setUp();

    }

    public void testParseGeoJson() throws Exception {
        JSONObject geoJsonObject = new JSONObject("{ \"type\": \"MultiLineString\",\n"
                + "    \"coordinates\": [\n"
                + "        [ [100.0, 0.0], [101.0, 1.0] ],\n"
                + "        [ [102.0, 2.0], [103.0, 3.0] ]\n"
                + "      ]\n"
                + "    }");

        GeoJsonParser parser = new GeoJsonParser(geoJsonObject);
        GeoJsonLineString ls1 = new GeoJsonLineString(
                new ArrayList<LatLng>(Arrays.asList(new LatLng(0, 100), new LatLng(1, 101))));
        GeoJsonLineString ls2 = new GeoJsonLineString(
                new ArrayList<LatLng>(Arrays.asList(new LatLng(2, 102), new LatLng(3, 103))));
        GeoJsonMultiLineString geoJsonMultiLineString = new GeoJsonMultiLineString(
                new ArrayList<GeoJsonLineString>(Arrays.asList(ls1, ls2)));
        GeoJsonFeature geoJsonFeature = new GeoJsonFeature(geoJsonMultiLineString, null, null,
                null);
        ArrayList<GeoJsonFeature> geoJsonFeatures = new ArrayList<GeoJsonFeature>(
                Arrays.asList(geoJsonFeature));
        assertEquals(geoJsonFeatures.get(0).getId(), parser.getFeatures().get(0).getId());
    }

    public JSONObject createGeometryCollection() throws Exception {
        return new JSONObject(
                "{\n" +
                        "   \"type\": \"Feature\",\n" +
                        "   \"id\": \"Popsicles\",\n" +
                        "   \"geometry\": {\n" +
                        "      \"type\": \"GeometryCollection\",\n" +
                        "      \"geometries\": [\n" +
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

    public JSONObject createMultiPolygon() throws Exception {
        return new JSONObject(
                "  { \"type\": \"MultiPolygon\",\n" +
                        "    \"coordinates\": [\n" +
                        "      [[[102.0, 2.0], [103.0, 2.0], [103.0, 3.0], [102.0, 3.0], [102.0, 2.0]]],\n"
                        +
                        "      [[[100.0, 0.0], [101.0, 0.0], [101.0, 1.0], [100.0, 1.0], [100.0, 0.0]],\n"
                        +
                        "       [[100.2, 0.2], [100.8, 0.2], [100.8, 0.8], [100.2, 0.8], [100.2, 0.2]]]\n"
                        +
                        "      ]\n" +
                        "    }"
        );

    }

    public JSONObject createEmptyFile() {
        return new JSONObject();
    }

    public JSONObject createNoType() throws Exception {
        return new JSONObject(
                "{\n"
                        + "    \"coordinates\": [100.0, 0.0] \n"
                        + "}"
        );
    }

    public JSONObject createFeatureNoGeometry() throws Exception {
        return new JSONObject(
                "{\n"
                        + "  \"type\": \"Feature\",\n"
                        + "  \"properties\": {\n"
                        + "    \"name\": \"Dinagat Islands\"\n"
                        + "  }\n"
                        + "}"
        );
    }

    public JSONObject createFeatureNoProperties() throws Exception {
        return new JSONObject(
                "{\n"
                        + "  \"type\": \"Feature\",\n"
                        + "  \"geometry\": {\n"
                        + "    \"type\": \"Point\",\n"
                        + "    \"coordinates\": [125.6, 10.1]\n"
                        + "  }\n"
                        + "}"
        );
    }

    public JSONObject createInvalidFeatureInFeatureCollection() throws Exception {
        return new JSONObject(
                "{ \"type\": \"FeatureCollection\",\n"
                        + "    \"features\": [\n"
                        + "      { \"type\": \"Feature\",\n"
                        + "        \"geometry\": {\"type\": \"Point\", \"coordinates\": [102.0, 0.5]},\n"
                        + "        \"properties\": {\"prop0\": \"value0\"}\n"
                        + "        },\n"
                        + "      { \"type\": \"Feature\",\n"
                        + "        \"geometry\": {\n"
                        + "          \"type\": \"LineString\",\n"
                        + "          \"coordinates\": [\n"
                        + "            [102.0, 0.0], [103.0, 1.0], [104.0, 0.0], [105.0, 1.0]\n"
                        + "            ]\n"
                        + "          },\n"
                        + "        \"properties\": {\n"
                        + "          \"prop0\": \"value0\",\n"
                        + "          \"prop1\": 0.0\n"
                        + "          }\n"
                        + "        },\n"
                        + "      {\n"
                        + "         \"geometry\": {\n"
                        + "           \"type\": \"Polygon\",\n"
                        + "           \"coordinates\": [\n"
                        + "             [ [100.0, 0.0], [101.0, 0.0], [101.0, 1.0],\n"
                        + "               [100.0, 1.0], [100.0, 0.0] ]\n"
                        + "             ]\n"
                        + "         },\n"
                        + "         \"properties\": {\n"
                        + "           \"prop0\": \"value0\",\n"
                        + "           \"prop1\": {\"this\": \"that\"}\n"
                        + "           }\n"
                        + "         }\n"
                        + "       ]\n"
                        + "     }"

        );
    }

    public JSONObject createInvalidFeaturesArrayFeatureCollection() throws JSONException {
        return new JSONObject(
                "{ \"type\": \"FeatureCollection\",\n"
                        + "    \"INVALID\": [\n"
                        + "      { \"type\": \"Feature\",\n"
                        + "        \"geometry\": {\"type\": \"Point\", \"coordinates\": [102.0, 0.5]},\n"
                        + "        \"properties\": {\"prop0\": \"value0\"}\n"
                        + "        },\n"
                        + "      { \"type\": \"Feature\",\n"
                        + "        \"geometry\": {\n"
                        + "          \"type\": \"LineString\",\n"
                        + "          \"coordinates\": [\n"
                        + "            [102.0, 0.0], [103.0, 1.0], [104.0, 0.0], [105.0, 1.0]\n"
                        + "            ]\n"
                        + "          },\n"
                        + "        \"properties\": {\n"
                        + "          \"prop0\": \"value0\",\n"
                        + "          \"prop1\": 0.0\n"
                        + "          }\n"
                        + "        },\n"
                        + "      { \"type\": \"Feature\",\n"
                        + "         \"geometry\": {\n"
                        + "           \"type\": \"Polygon\",\n"
                        + "           \"coordinates\": [\n"
                        + "             [ [100.0, 0.0], [101.0, 0.0], [101.0, 1.0],\n"
                        + "               [100.0, 1.0], [100.0, 0.0] ]\n"
                        + "             ]\n"
                        + "         },\n"
                        + "         \"properties\": {\n"
                        + "           \"prop0\": \"value0\",\n"
                        + "           \"prop1\": {\"this\": \"that\"}\n"
                        + "           }\n"
                        + "         }\n"
                        + "       ]\n"
                        + "     }"
        );
    }

    public JSONObject createInvalidGeometry() throws JSONException {
        return new JSONObject(
                "{ \"type\": \"LineString\",\n"
                        + "    \"banana\": [ [100.0, 0.0], [101.0, 1.0] ]\n"
                        + "    }"
        );
    }

    public JSONObject createInvalidGeometryCoordinatesInFeature() throws JSONException {
        return new JSONObject(
                "{\n"
                        + "  \"type\": \"Feature\",\n"
                        + "  \"geometry\": {\n"
                        + "    \"type\": \"Point\",\n"
                        + "    \"mango\": [125.6, 10.1]\n"
                        + "  },\n"
                        + "  \"properties\": {\n"
                        + "    \"name\": \"Dinagat Islands\"\n"
                        + "  }\n"
                        + "}"
        );
    }

    public JSONObject createInvalidGeometryTypeInFeature() throws JSONException {
        return new JSONObject(
                "{\n"
                        + "  \"cow\": \"Feature\",\n"
                        + "  \"geometry\": {\n"
                        + "    \"type\": \"Point\",\n"
                        + "    \"coordinates\": [125.6, 10.1]\n"
                        + "  },\n"
                        + "  \"properties\": {\n"
                        + "    \"name\": \"Dinagat Islands\"\n"
                        + "  }\n"
                        + "}"
        );
    }

    public JSONObject createInvalidGeometryCoordinatesInFeatureCollection() throws Exception {
        return new JSONObject(
                "{ \"type\": \"FeatureCollection\",\n"
                        + "    \"features\": [\n"
                        + "      { \"type\": \"Feature\",\n"
                        + "        \"geometry\": {\"type\": \"Point\", \"coordinates\": [102.0, 0.5]},\n"
                        + "        \"properties\": {\"prop0\": \"value0\"}\n"
                        + "        },\n"
                        + "      { \"type\": \"Feature\",\n"
                        + "        \"geometry\": {\n"
                        + "          \"type\": \"LineString\",\n"
                        + "          \"aardvark\": [\n"
                        + "            [102.0, 0.0], [103.0, 1.0], [104.0, 0.0], [105.0, 1.0]\n"
                        + "            ]\n"
                        + "          },\n"
                        + "        \"properties\": {\n"
                        + "          \"prop0\": \"value0\",\n"
                        + "          \"prop1\": 0.0\n"
                        + "          }\n"
                        + "        },\n"
                        + "      { \"type\": \"Feature\",\n"
                        + "         \"geometry\": {\n"
                        + "           \"type\": \"Polygon\",\n"
                        + "           \"coordinates\": [\n"
                        + "             [ [100.0, 0.0], [101.0, 0.0], [101.0, 1.0],\n"
                        + "               [100.0, 1.0], [100.0, 0.0] ]\n"
                        + "             ]\n"
                        + "         },\n"
                        + "         \"properties\": {\n"
                        + "           \"prop0\": \"value0\",\n"
                        + "           \"prop1\": {\"this\": \"that\"}\n"
                        + "           }\n"
                        + "         }\n"
                        + "       ]\n"
                        + "     }"
        );
    }

    public JSONObject createInvalidGeometryTypeInFeatureCollection() throws Exception {
        return new JSONObject(
                "{ \"type\": \"FeatureCollection\",\n"
                        + "    \"features\": [\n"
                        + "      { \"type\": \"Feature\",\n"
                        + "        \"geometry\": {\"type\": \"Point\", \"coordinates\": [102.0, 0.5]},\n"
                        + "        \"properties\": {\"prop0\": \"value0\"}\n"
                        + "        },\n"
                        + "      { \"type\": \"Feature\",\n"
                        + "        \"geometry\": {\n"
                        + "          \"type\": \"LineString\",\n"
                        + "          \"coordinates\": [\n"
                        + "            [102.0, 0.0], [103.0, 1.0], [104.0, 0.0], [105.0, 1.0]\n"
                        + "            ]\n"
                        + "          },\n"
                        + "        \"properties\": {\n"
                        + "          \"prop0\": \"value0\",\n"
                        + "          \"prop1\": 0.0\n"
                        + "          }\n"
                        + "        },\n"
                        + "      { \"type\": \"Feature\",\n"
                        + "         \"geometry\": {\n"
                        + "           \"crocodile\": \"Polygon\",\n"
                        + "           \"coordinates\": [\n"
                        + "             [ [100.0, 0.0], [101.0, 0.0], [101.0, 1.0],\n"
                        + "               [100.0, 1.0], [100.0, 0.0] ]\n"
                        + "             ]\n"
                        + "         },\n"
                        + "         \"properties\": {\n"
                        + "           \"prop0\": \"value0\",\n"
                        + "           \"prop1\": {\"this\": \"that\"}\n"
                        + "           }\n"
                        + "         }\n"
                        + "       ]\n"
                        + "     }"
        );
    }

    //Testing for nested geometry collections
    public void testParseGeometryCollection() throws Exception {
        JSONObject geometryCollectionObject = createGeometryCollection();
        GeoJsonParser parser = new GeoJsonParser(geometryCollectionObject);
        assertTrue(parser.getFeatures().size() == 1);
        for (GeoJsonFeature feature : parser.getFeatures()) {
            assertTrue(feature.getGeometry().getType().equals("GeometryCollection"));
            assertTrue(feature.getProperties().size() == 2);
            assertTrue(feature.getId().equals("Popsicles"));
            GeoJsonGeometryCollection geometry = ((GeoJsonGeometryCollection) feature
                    .getGeometry());
            assertTrue(geometry.getGeometries().size() == 1);
            for (GeoJsonGeometry geoJsonGeometry : geometry.getGeometries()) {
                assertTrue(geoJsonGeometry.getType().equals("GeometryCollection"));
            }
        }
    }

    public void testParseMultiPolygon() throws Exception {
        JSONObject multiPolygon = createMultiPolygon();
        GeoJsonParser parser = new GeoJsonParser(multiPolygon);
        assertTrue(parser.getFeatures().size() == 1);
        GeoJsonFeature feature = parser.getFeatures().get(0);
        GeoJsonMultiPolygon polygon = ((GeoJsonMultiPolygon) feature.getGeometry());
        assertTrue(polygon.getPolygons().size() == 2);
        assertTrue(polygon.getPolygons().get(0).getType().equals("Polygon"));
        assertTrue(polygon.getPolygons().get(0).getCoordinates().size() == 1);
        assertTrue(polygon.getPolygons().get(1).getType().equals("Polygon"));
        assertTrue(polygon.getPolygons().get(1).getCoordinates().size() == 2);
    }

    public void testEmptyFile() throws Exception {
        GeoJsonParser parser = new GeoJsonParser(createEmptyFile());
        assertNull(parser.getBoundingBox());
        assertEquals(0, parser.getFeatures().size());
    }

    public void testInvalidGeoJson() throws Exception {
        GeoJsonParser parser = new GeoJsonParser(createNoType());
        assertNull(parser.getBoundingBox());
        assertEquals(0, parser.getFeatures().size());

        parser = new GeoJsonParser(createFeatureNoGeometry());
        assertNull(parser.getBoundingBox());
        assertEquals(1, parser.getFeatures().size());

        parser = new GeoJsonParser(createFeatureNoProperties());
        assertNull(parser.getBoundingBox());
        assertEquals(1, parser.getFeatures().size());

        parser = new GeoJsonParser(createInvalidFeatureInFeatureCollection());
        assertNull(parser.getBoundingBox());
        assertEquals(2, parser.getFeatures().size());

        parser = new GeoJsonParser(createInvalidFeaturesArrayFeatureCollection());
        assertNull(parser.getBoundingBox());
        assertEquals(0, parser.getFeatures().size());

        parser = new GeoJsonParser(createInvalidGeometry());
        assertNull(parser.getBoundingBox());
        assertEquals(0, parser.getFeatures().size());

        parser = new GeoJsonParser(createInvalidGeometryCoordinatesInFeature());
        assertNull(parser.getBoundingBox());
        assertEquals(1, parser.getFeatures().size());

        parser = new GeoJsonParser(createInvalidGeometryTypeInFeature());
        assertNull(parser.getBoundingBox());
        assertEquals(0, parser.getFeatures().size());

        parser = new GeoJsonParser(createInvalidGeometryCoordinatesInFeatureCollection());
        assertNull(parser.getBoundingBox());
        assertEquals(3, parser.getFeatures().size());

        parser = new GeoJsonParser(createInvalidGeometryTypeInFeatureCollection());
        assertNull(parser.getBoundingBox());
        assertEquals(3, parser.getFeatures().size());
    }
}
