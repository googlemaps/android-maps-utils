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
        JSONObject validGeoJsonObject = new JSONObject("{ \"type\": \"MultiLineString\",\n"
                + "    \"coordinates\": [\n"
                + "        [ [100.0, 0.0], [101.0, 1.0] ],\n"
                + "        [ [102.0, 2.0], [103.0, 3.0] ]\n"
                + "      ]\n"
                + "    }");

        GeoJsonParser parser = new GeoJsonParser(validGeoJsonObject);
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

    // TODO: Testing for nested geometry collections
    public void testParseGeometryCollection() throws Exception {
        JSONObject geometryCollectionObject = validGeometryCollection();
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

    // TODO: parse each feature type
    public void testParseMultiPolygon() throws Exception {
        JSONObject multiPolygon = validMultiPolygon();
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
        GeoJsonParser parser = new GeoJsonParser(emptyFile());
        assertNull(parser.getBoundingBox());
        assertEquals(0, parser.getFeatures().size());
    }

    public void testInvalidFeature() throws Exception {
        GeoJsonParser parser;

        // Feature exists without geometry
        parser = new GeoJsonParser(invalidFeatureNoGeometry());
        assertNull(parser.getBoundingBox());
        assertEquals(1, parser.getFeatures().size());
        assertNull(parser.getFeatures().get(0).getGeometry());
        assertEquals("Dinagat Islands", parser.getFeatures().get(0).getProperties().get("name"));

        // Feature exists without properties
        parser = new GeoJsonParser(invalidFeatureNoProperties());
        assertNull(parser.getBoundingBox());
        assertEquals(1, parser.getFeatures().size());
        assertEquals(0, parser.getFeatures().get(0).getProperties().size());

        // No features exist due to no type
        parser = new GeoJsonParser(invalidFeatureMissingType());
        assertNull(parser.getBoundingBox());
        assertEquals(0, parser.getFeatures().size());

        // 1 geometry in geometry collection, other geometry was invalid
        parser = new GeoJsonParser(invalidFeatureGeometryCollectionInvalidGeometry());
        assertNull(parser.getBoundingBox());
        assertEquals(1, parser.getFeatures().size());
        assertEquals(1, ((GeoJsonGeometryCollection) parser.getFeatures().get(0).getGeometry()).getGeometries().size());

        // No geometry due to no geometries array member
        parser = new GeoJsonParser(invalidFeatureGeometryCollectionNoGeometries());
        assertNull(parser.getBoundingBox());
        assertEquals(1, parser.getFeatures().size());
        assertNull(parser.getFeatures().get(0).getGeometry());
    }

    public void testInvalidFeatureCollection() throws Exception {
        GeoJsonParser parser;

        // 1 feature without geometry
        parser = new GeoJsonParser(invalidFeatureCollectionNoCoordinatesInGeometryInFeature());
        assertNull(parser.getBoundingBox());
        assertEquals(3, parser.getFeatures().size());
        assertNotNull(parser.getFeatures().get(0).getGeometry());
        assertNull(parser.getFeatures().get(1).getGeometry());
        assertNotNull(parser.getFeatures().get(2).getGeometry());

        // 1 feature without geometry
        parser = new GeoJsonParser(invalidFeatureCollectionNoTypeInGeometryInFeature());
        assertNull(parser.getBoundingBox());
        assertEquals(3, parser.getFeatures().size());
        assertNotNull(parser.getFeatures().get(0).getGeometry());
        assertNotNull(parser.getFeatures().get(1).getGeometry());
        assertNull(parser.getFeatures().get(2).getGeometry());

        // No features due to no features array
        parser = new GeoJsonParser(invalidFeatureCollectionNoFeaturesArray());
        assertNull(parser.getBoundingBox());
        assertEquals(0, parser.getFeatures().size());

        // 1 feature not parsed due to no type indicating it is a feature
        parser = new GeoJsonParser(invalidFeatureCollectionNoGeometryTypeInFeature());
        assertNull(parser.getBoundingBox());
        assertEquals(2, parser.getFeatures().size());
        assertTrue(!parser.getFeatures().get(0).getGeometry().getType().equals("Polygon") && !parser
                .getFeatures().get(1).getGeometry().getType().equals("Polygon"));

        // Contains 1 feature element with no geometry as it was missing a coordinates member
        parser = new GeoJsonParser(invalidFeatureNoCoordinatesInGeometry());
        assertNull(parser.getBoundingBox());
        assertEquals(1, parser.getFeatures().size());
        assertNull(parser.getFeatures().get(0).getGeometry());
    }

    public void testInvalidGeometry() throws Exception {
        GeoJsonParser parser;

        // No geometry due to no type member
        parser = new GeoJsonParser(invalidGeometryNoType());
        assertNull(parser.getBoundingBox());
        assertEquals(0, parser.getFeatures().size());

        // No geometry due to no coordinates member
        parser = new GeoJsonParser(invalidGeometryNoCoordinates());
        assertNull(parser.getBoundingBox());
        assertEquals(0, parser.getFeatures().size());

        // Geometry collection has 1 valid and 1 invalid geometry
        parser = new GeoJsonParser(invalidGeometryCollectionInvalidGeometry());
        assertNull(parser.getBoundingBox());
        assertEquals(1, parser.getFeatures().size());
        assertEquals(1, ((GeoJsonGeometryCollection) parser.getFeatures().get(0).getGeometry()).getGeometries().size());

        // No geometry due to invalid geometry collection
        parser = new GeoJsonParser(invalidGeometryCollectionInvalidGeometries());
        assertNull(parser.getBoundingBox());
        assertEquals(0, parser.getFeatures().size());

        // No geometry due to only lng provided
        parser = new GeoJsonParser(invalidGeometryInvalidCoordinatesPair());
        assertEquals(0, parser.getFeatures().size());

        // No geometry due to coordinates being strings instead of doubles
        parser = new GeoJsonParser(invalidGeometryInvalidCoordinatesString());
        assertEquals(0, parser.getFeatures().size());
    }

    public JSONObject validGeometryCollection() throws Exception {
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

    public JSONObject validMultiPolygon() throws Exception {
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

    public JSONObject validEmptyFeatureCollection() throws Exception {
        return new JSONObject(
                "{ \"type\": \"FeatureCollection\",\n"
                        + "  \"features\": [\n"
                        + "     ]\n"
                        + "}");
    }

    public JSONObject emptyFile() {
        return new JSONObject();
    }

    public JSONObject invalidGeometryNoType() throws Exception {
        return new JSONObject(
                "{\n"
                        + "    \"coordinates\": [100.0, 0.0] \n"
                        + "}"
        );
    }

    public JSONObject invalidGeometryInvalidCoordinatesPair() throws Exception {
        return new JSONObject("{ \"type\": \"Point\", \"coordinates\": [100.0] }");
    }

    public JSONObject invalidGeometryInvalidCoordinatesString() throws Exception {
        return new JSONObject("{ \"type\": \"Point\", \"coordinates\": [\"BANANA\"] }");
    }

    /**
     * Feature missing its geometry member, should be created with null geometry
     *
     * @return feature missing its geometry
     */
    public JSONObject invalidFeatureNoGeometry() throws Exception {
        return new JSONObject(
                "{\n"
                        + "  \"type\": \"Feature\",\n"
                        + "  \"properties\": {\n"
                        + "    \"name\": \"Dinagat Islands\"\n"
                        + "  }\n"
                        + "}"
        );
    }

    public JSONObject invalidGeometryCollectionInvalidGeometry() throws Exception {
        return new JSONObject(
                "{ \"type\": \"GeometryCollection\",\n"
                + "  \"geometries\": [\n"
                + "    { \"type\": \"Point\",\n"
                + "      \"shark\": [100.0, 0.0]\n"
                + "      },\n"
                + "    { \"type\": \"LineString\",\n"
                + "      \"coordinates\": [ [101.0, 0.0], [102.0, 1.0] ]\n"
                + "      }\n"
                + "  ]\n"
                + "}"
        );
    }

    public JSONObject invalidGeometryCollectionInvalidGeometries() throws Exception {
        return new JSONObject(
                "{ \"type\": \"GeometryCollection\",\n"
                        + "  \"doge\": [\n"
                        + "    { \"type\": \"Point\",\n"
                        + "      \"coordinates\": [100.0, 0.0]\n"
                        + "      },\n"
                        + "    { \"type\": \"LineString\",\n"
                        + "      \"coordinates\": [ [101.0, 0.0], [102.0, 1.0] ]\n"
                        + "      }\n"
                        + "  ]\n"
                        + "}"
        );
    }

    public JSONObject invalidFeatureGeometryCollectionInvalidGeometry() throws Exception {
        return new JSONObject(
                "{\n"
                + "  \"type\":\"FeatureCollection\",\n"
                + "  \"features\":[\n"
                + "    {\n"
                + "      \"type\":\"Feature\",\n"
                + "      \"geometry\":{\n"
                + "        \"type\":\"GeometryCollection\",\n"
                + "        \"geometries\":[\n"
                + "          {\n"
                + "            \"type\":\"Point\",\n"
                + "            \"shark\":[\n"
                + "              100.0,\n"
                + "              0.0\n"
                + "            ]\n"
                + "          },\n"
                + "          {\n"
                + "            \"type\":\"LineString\",\n"
                + "            \"coordinates\":[\n"
                + "              [\n"
                + "                101.0,\n"
                + "                0.0\n"
                + "              ],\n"
                + "              [\n"
                + "                102.0,\n"
                + "                1.0\n"
                + "              ]\n"
                + "            ]\n"
                + "          }\n"
                + "        ]\n"
                + "      },\n"
                + "      \"properties\":{\n"
                + "        \"prop0\":\"value0\"\n"
                + "      }\n"
                + "    }\n"
                + "  ]\n"
                + "}"
        );
    }

    public JSONObject invalidFeatureGeometryCollectionNoGeometries() throws Exception {
        return new JSONObject(
                "{\n"
                        + "  \"type\":\"FeatureCollection\",\n"
                        + "  \"features\":[\n"
                        + "    {\n"
                        + "      \"type\":\"Feature\",\n"
                        + "      \"geometry\":{\n"
                        + "        \"type\":\"GeometryCollection\",\n"
                        + "        \"llamas\":[\n"
                        + "          {\n"
                        + "            \"type\":\"Point\",\n"
                        + "            \"coordinates\":[\n"
                        + "              100.0,\n"
                        + "              0.0\n"
                        + "            ]\n"
                        + "          },\n"
                        + "          {\n"
                        + "            \"type\":\"LineString\",\n"
                        + "            \"coordinates\":[\n"
                        + "              [\n"
                        + "                101.0,\n"
                        + "                0.0\n"
                        + "              ],\n"
                        + "              [\n"
                        + "                102.0,\n"
                        + "                1.0\n"
                        + "              ]\n"
                        + "            ]\n"
                        + "          }\n"
                        + "        ]\n"
                        + "      },\n"
                        + "      \"properties\":{\n"
                        + "        \"prop0\":\"value0\"\n"
                        + "      }\n"
                        + "    }\n"
                        + "  ]\n"
                        + "}"
        );
    }

    /**
     * Feature missing its properties member, should be created with empty feature hashmap
     *
     * @return feature missing its properties
     */
    public JSONObject invalidFeatureNoProperties() throws Exception {
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

    /**
     * 2 valid features with 1 invalid feature missing its type member indicating that it is a
     * feature
     *
     * @return 2 valid features with 1 invalid feature
     */
    public JSONObject invalidFeatureCollectionNoGeometryTypeInFeature() throws Exception {
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

    /**
     * FeatureCollection missing its feature array member
     *
     * @return FeatureCollection missing its feature array
     */
    public JSONObject invalidFeatureCollectionNoFeaturesArray() throws JSONException {
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

    /**
     * Geometry missing its coordinates member
     */
    public JSONObject invalidGeometryNoCoordinates() throws JSONException {
        return new JSONObject(
                "{ \"type\": \"LineString\",\n"
                        + "    \"banana\": [ [100.0, 0.0], [101.0, 1.0] ]\n"
                        + "    }"
        );
    }

    /**
     * Feature with geometry missing its coordinates member
     *
     * @return Feature with geometry missing its coordinates
     */
    public JSONObject invalidFeatureNoCoordinatesInGeometry() throws JSONException {
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

    /**
     * Feature missing its type member
     *
     * @return Feature missing its type
     */
    public JSONObject invalidFeatureMissingType() throws JSONException {
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

    /**
     * Feature collection with a feature that contains a geometry missing its coordinates member
     *
     * @return Feature collection with feature missing its coordinates
     */
    public JSONObject invalidFeatureCollectionNoCoordinatesInGeometryInFeature() throws Exception {
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

    /**
     * Feature collection with a feature that has a geometry missing its type member
     *
     * @return Feature collection with a feature missing its type
     */
    public JSONObject invalidFeatureCollectionNoTypeInGeometryInFeature() throws Exception {
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
}
