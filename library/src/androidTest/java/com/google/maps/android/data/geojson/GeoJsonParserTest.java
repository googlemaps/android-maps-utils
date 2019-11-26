package com.google.maps.android.data.geojson;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.data.Geometry;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

public class GeoJsonParserTest {
    @Test
    public void testParseGeoJson() throws Exception {
        JSONObject validGeoJsonObject =
                new JSONObject(
                        "{ \"type\": \"MultiLineString\",\n"
                                + "    \"coordinates\": [\n"
                                + "        [ [100.0, 0.0], [101.0, 1.0] ],\n"
                                + "        [ [102.0, 2.0], [103.0, 3.0] ]\n"
                                + "      ]\n"
                                + "    }");

        GeoJsonParser parser = new GeoJsonParser(validGeoJsonObject);
        GeoJsonLineString ls1 =
                new GeoJsonLineString(
                        new ArrayList<>(Arrays.asList(new LatLng(0, 100), new LatLng(1, 101))));
        GeoJsonLineString ls2 =
                new GeoJsonLineString(
                        new ArrayList<>(Arrays.asList(new LatLng(2, 102), new LatLng(3, 103))));
        GeoJsonMultiLineString geoJsonMultiLineString =
                new GeoJsonMultiLineString(new ArrayList<>(Arrays.asList(ls1, ls2)));
        GeoJsonFeature geoJsonFeature =
                new GeoJsonFeature(geoJsonMultiLineString, null, null, null);
        List<GeoJsonFeature> geoJsonFeatures = new ArrayList<>(Arrays.asList(geoJsonFeature));
        assertEquals(geoJsonFeatures.get(0).getId(), parser.getFeatures().get(0).getId());
    }

    @Test
    public void testParseGeometryCollection() throws Exception {
        GeoJsonParser parser = new GeoJsonParser(validGeometryCollection());
        assertEquals(1, parser.getFeatures().size());
        for (GeoJsonFeature feature : parser.getFeatures()) {
            assertEquals("GeometryCollection", feature.getGeometry().getGeometryType());
            int size = 0;
            for (String property : feature.getPropertyKeys()) {
                size++;
            }
            assertEquals(2, size);
            assertEquals("Popsicles", feature.getId());
            GeoJsonGeometryCollection geometry =
                    ((GeoJsonGeometryCollection) feature.getGeometry());
            assertEquals(1, geometry.getGeometries().size());
            for (Geometry geoJsonGeometry : geometry.getGeometries()) {
                assertEquals("GeometryCollection", geoJsonGeometry.getGeometryType());
            }
        }
    }

    @Test
    public void testParsePoint() throws Exception {
        GeoJsonParser parser = new GeoJsonParser(validPoint());
        assertEquals(1, parser.getFeatures().size());
        assertNull(parser.getFeatures().get(0).getBoundingBox());
        assertNull(parser.getFeatures().get(0).getId());
        int size = 0;
        for (String property : parser.getFeatures().get(0).getPropertyKeys()) {
            size++;
        }
        assertEquals(0, size);
        assertTrue(parser.getFeatures().get(0).getGeometry() instanceof GeoJsonPoint);
        GeoJsonPoint point = (GeoJsonPoint) parser.getFeatures().get(0).getGeometry();
        assertEquals(new LatLng(0.0, 100.0), point.getCoordinates());
    }

    @Test
    public void testParseLineString() throws Exception {
        GeoJsonParser parser = new GeoJsonParser(validLineString());
        assertEquals(1, parser.getFeatures().size());
        assertNull(parser.getFeatures().get(0).getBoundingBox());
        assertNull(parser.getFeatures().get(0).getId());
        int size = 0;
        for (String property : parser.getFeatures().get(0).getPropertyKeys()) {
            size++;
        }
        assertEquals(0, size);
        assertTrue(parser.getFeatures().get(0).getGeometry() instanceof GeoJsonLineString);
        GeoJsonLineString lineString =
                (GeoJsonLineString) parser.getFeatures().get(0).getGeometry();
        assertEquals(2, lineString.getCoordinates().size());
        List<LatLng> ls = new ArrayList<>(Arrays.asList(new LatLng(0, 100), new LatLng(1, 101)));
        assertEquals(ls, lineString.getCoordinates());
    }

    @Test
    public void testParsePolygon() throws Exception {
        GeoJsonParser parser = new GeoJsonParser(validPolygon());
        assertEquals(1, parser.getFeatures().size());
        assertNull(parser.getFeatures().get(0).getBoundingBox());
        assertNull(parser.getFeatures().get(0).getId());
        int size = 0;
        for (String property : parser.getFeatures().get(0).getPropertyKeys()) {
            size++;
        }
        assertEquals(0, size);
        assertTrue(parser.getFeatures().get(0).getGeometry() instanceof GeoJsonPolygon);
        GeoJsonPolygon polygon = (GeoJsonPolygon) parser.getFeatures().get(0).getGeometry();
        assertEquals(2, polygon.getCoordinates().size());
        assertEquals(5, polygon.getCoordinates().get(0).size());
        assertEquals(5, polygon.getCoordinates().get(1).size());
        List<ArrayList<LatLng>> p = new ArrayList<>();
        p.add(
                new ArrayList<>(
                        Arrays.asList(
                                new LatLng(0, 100),
                                new LatLng(0, 101),
                                new LatLng(1, 101),
                                new LatLng(1, 100),
                                new LatLng(0, 100))));
        p.add(
                new ArrayList<>(
                        Arrays.asList(
                                new LatLng(0.2, 100.2),
                                new LatLng(0.2, 100.8),
                                new LatLng(0.8, 100.8),
                                new LatLng(0.8, 100.2),
                                new LatLng(0.2, 100.2))));
        assertEquals(p, polygon.getCoordinates());
    }

    @Test
    public void testParseMultiPoint() throws Exception {
        GeoJsonParser parser = new GeoJsonParser(validMultiPoint());
        assertEquals(1, parser.getFeatures().size());
        assertNull(parser.getFeatures().get(0).getBoundingBox());
        assertNull(parser.getFeatures().get(0).getId());
        int size = 0;
        for (String property : parser.getFeatures().get(0).getPropertyKeys()) {
            size++;
        }
        assertEquals(0, size);
        assertTrue(parser.getFeatures().get(0).getGeometry() instanceof GeoJsonMultiPoint);
        GeoJsonMultiPoint multiPoint =
                (GeoJsonMultiPoint) parser.getFeatures().get(0).getGeometry();
        assertEquals(2, multiPoint.getPoints().size());
        assertEquals(new LatLng(0, 100), multiPoint.getPoints().get(0).getCoordinates());
        assertEquals(new LatLng(1, 101), multiPoint.getPoints().get(1).getCoordinates());
    }

    @Test
    public void testParseMultiLineString() throws Exception {
        GeoJsonParser parser = new GeoJsonParser(validMultiLineString());
        assertEquals(1, parser.getFeatures().size());
        assertNull(parser.getFeatures().get(0).getBoundingBox());
        assertNull(parser.getFeatures().get(0).getId());
        int size = 0;
        for (String property : parser.getFeatures().get(0).getPropertyKeys()) {
            size++;
        }
        assertEquals(0, size);
        assertTrue(parser.getFeatures().get(0).getGeometry() instanceof GeoJsonMultiLineString);
        GeoJsonMultiLineString multiLineString =
                (GeoJsonMultiLineString) parser.getFeatures().get(0).getGeometry();
        assertEquals(2, multiLineString.getLineStrings().size());
        List<LatLng> ls = new ArrayList<>(Arrays.asList(new LatLng(0, 100), new LatLng(1, 101)));
        assertEquals(ls, multiLineString.getLineStrings().get(0).getCoordinates());
        ls = new ArrayList<>(Arrays.asList(new LatLng(2, 102), new LatLng(3, 103)));
        assertEquals(ls, multiLineString.getLineStrings().get(1).getCoordinates());
    }

    @Test
    public void testParseMultiPolygon() throws Exception {
        GeoJsonParser parser = new GeoJsonParser(validMultiPolygon());
        assertEquals(1, parser.getFeatures().size());
        GeoJsonFeature feature = parser.getFeatures().get(0);
        GeoJsonMultiPolygon polygon = ((GeoJsonMultiPolygon) feature.getGeometry());
        assertEquals(2, polygon.getPolygons().size());
        assertEquals(1, polygon.getPolygons().get(0).getCoordinates().size());
        List<List<LatLng>> p1 = new ArrayList<>();
        p1.add(
                new ArrayList<>(
                        Arrays.asList(
                                new LatLng(2, 102),
                                new LatLng(2, 103),
                                new LatLng(3, 103),
                                new LatLng(3, 102),
                                new LatLng(2, 102))));
        assertEquals(p1, polygon.getPolygons().get(0).getCoordinates());
        assertEquals(2, polygon.getPolygons().get(1).getCoordinates().size());
        List<List<LatLng>> p2 = new ArrayList<>();
        p2.add(
                new ArrayList<>(
                        Arrays.asList(
                                new LatLng(0, 100),
                                new LatLng(0, 101),
                                new LatLng(1, 101),
                                new LatLng(1, 100),
                                new LatLng(0, 100))));
        p2.add(
                new ArrayList<>(
                        Arrays.asList(
                                new LatLng(0.2, 100.2),
                                new LatLng(0.2, 100.8),
                                new LatLng(0.8, 100.8),
                                new LatLng(0.8, 100.2),
                                new LatLng(0.2, 100.2))));
        assertEquals(p2, polygon.getPolygons().get(1).getCoordinates());
    }

    @Test
    public void testEmptyFile() {
        GeoJsonParser parser = new GeoJsonParser(emptyFile());
        assertNull(parser.getBoundingBox());
        assertEquals(0, parser.getFeatures().size());
    }

    @Test
    public void testInvalidFeature() throws Exception {
        GeoJsonParser parser;

        // Feature exists without geometry
        parser = new GeoJsonParser(invalidFeatureNoGeometry());
        assertNull(parser.getBoundingBox());
        assertEquals(1, parser.getFeatures().size());
        assertNull(parser.getFeatures().get(0).getGeometry());
        assertEquals("Dinagat Islands", parser.getFeatures().get(0).getProperty("name"));

        // Feature exists without properties
        parser = new GeoJsonParser(invalidFeatureNoProperties());
        assertNull(parser.getBoundingBox());
        assertEquals(1, parser.getFeatures().size());
        int size = 0;
        for (String property : parser.getFeatures().get(0).getPropertyKeys()) {
            size++;
        }
        assertEquals(0, size);

        // No features exist due to no type
        parser = new GeoJsonParser(invalidFeatureMissingType());
        assertNull(parser.getBoundingBox());
        assertEquals(0, parser.getFeatures().size());

        // 1 geometry in geometry collection, other geometry was invalid
        parser = new GeoJsonParser(invalidFeatureGeometryCollectionInvalidGeometry());
        assertNull(parser.getBoundingBox());
        assertEquals(1, parser.getFeatures().size());
        assertEquals(
                1,
                ((GeoJsonGeometryCollection) parser.getFeatures().get(0).getGeometry())
                        .getGeometries()
                        .size());

        // No geometry due to no geometries array member
        parser = new GeoJsonParser(invalidFeatureGeometryCollectionNoGeometries());
        assertNull(parser.getBoundingBox());
        assertEquals(1, parser.getFeatures().size());
        assertNull(parser.getFeatures().get(0).getGeometry());
    }

    @Test
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
        assertTrue(
                !parser.getFeatures().get(0).getGeometry().getGeometryType().equals("Polygon")
                        && !parser.getFeatures()
                        .get(1)
                        .getGeometry()
                        .getGeometryType()
                        .equals("Polygon"));

        // Contains 1 feature element with no geometry as it was missing a coordinates member
        parser = new GeoJsonParser(invalidFeatureNoCoordinatesInGeometry());
        assertNull(parser.getBoundingBox());
        assertEquals(1, parser.getFeatures().size());
        assertNull(parser.getFeatures().get(0).getGeometry());
    }

    @Test
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
        assertEquals(
                1,
                ((GeoJsonGeometryCollection) parser.getFeatures().get(0).getGeometry())
                        .getGeometries()
                        .size());

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

    private JSONObject validGeometryCollection() throws Exception {
        return new JSONObject(
                "{\n"
                        + "   \"type\": \"Feature\",\n"
                        + "   \"id\": \"Popsicles\",\n"
                        + "   \"geometry\": {\n"
                        + "      \"type\": \"GeometryCollection\",\n"
                        + "      \"geometries\": [\n"
                        + "          { \"type\": \"GeometryCollection\",\n"
                        + "            \"geometries\": [\n"
                        + "              { \"type\": \"Point\",\n"
                        + "                \"coordinates\": [103.0, 0.0]\n"
                        + "                }\n"
                        + "            ]\n"
                        + "          }\n"
                        + "      ]\n"
                        + "   },\n"
                        + "   \"properties\": {\n"
                        + "       \"prop0\": \"value0\",\n"
                        + "       \"prop1\": \"value1\"\n"
                        + "   }\n"
                        + "}");
    }

    private JSONObject validPoint() throws JSONException {
        return new JSONObject("{ \"type\": \"Point\", \"coordinates\": [100.0, 0.0] }");
    }

    private JSONObject validLineString() throws JSONException {
        return new JSONObject(
                "{ \"type\": \"LineString\",\n"
                        + "    \"coordinates\": [ [100.0, 0.0], [101.0, 1.0] ]\n"
                        + "    }");
    }

    private JSONObject validPolygon() throws JSONException {
        return new JSONObject(
                "{ \"type\": \"Polygon\",\n"
                        + "    \"coordinates\": [\n"
                        + "      [ [100.0, 0.0], [101.0, 0.0], [101.0, 1.0], [100.0, 1.0], [100.0,"
                        + " 0.0] ],\n"
                        + "      [ [100.2, 0.2], [100.8, 0.2], [100.8, 0.8], [100.2, 0.8], [100.2,"
                        + " 0.2] ]\n"
                        + "      ]\n"
                        + "   }");
    }

    private JSONObject validMultiPoint() throws JSONException {
        return new JSONObject(
                "{ \"type\": \"MultiPoint\",\n"
                        + "    \"coordinates\": [ [100.0, 0.0], [101.0, 1.0] ]\n"
                        + "    }");
    }

    private JSONObject validMultiLineString() throws JSONException {
        return new JSONObject(
                "{ \"type\": \"MultiLineString\",\n"
                        + "    \"coordinates\": [\n"
                        + "        [ [100.0, 0.0], [101.0, 1.0] ],\n"
                        + "        [ [102.0, 2.0], [103.0, 3.0] ]\n"
                        + "      ]\n"
                        + "    }");
    }

    private JSONObject validMultiPolygon() throws Exception {
        return new JSONObject(
                "{ \"type\": \"MultiPolygon\",\n"
                        + "    \"coordinates\": [\n"
                        + "      [[[102.0, 2.0], [103.0, 2.0], [103.0, 3.0], [102.0, 3.0], [102.0,"
                        + " 2.0]]],\n"
                        + "      [[[100.0, 0.0], [101.0, 0.0], [101.0, 1.0], [100.0, 1.0], [100.0,"
                        + " 0.0]],\n"
                        + "       [[100.2, 0.2], [100.8, 0.2], [100.8, 0.8], [100.2, 0.8], [100.2,"
                        + " 0.2]]]\n"
                        + "      ]\n"
                        + "    }");
    }

    private JSONObject validEmptyFeatureCollection() throws Exception {
        return new JSONObject(
                "{ \"type\": \"FeatureCollection\",\n" + "  \"features\": [\n" + "     ]\n" + "}");
    }

    private JSONObject emptyFile() {
        return new JSONObject();
    }

    private JSONObject invalidGeometryNoType() throws Exception {
        return new JSONObject("{\n" + "    \"coordinates\": [100.0, 0.0] \n" + "}");
    }

    /**
     * Geometry has coordinates member which is a single double instead of a pair of doubles
     *
     * @return geometry with invalid coordinates member
     */
    private JSONObject invalidGeometryInvalidCoordinatesPair() throws Exception {
        return new JSONObject("{ \"type\": \"Point\", \"coordinates\": [100.0] }");
    }

    /**
     * Geometry has coordinates member which is a pair of strings instead of a pair of doubles
     *
     * @return geometry with invalid coordinates member
     */
    private JSONObject invalidGeometryInvalidCoordinatesString() throws Exception {
        return new JSONObject("{ \"type\": \"Point\", \"coordinates\": [\"BANANA\", \"BOAT\"] }");
    }

    /**
     * Feature missing its geometry member, should be created with null geometry
     *
     * @return feature missing its geometry
     */
    private JSONObject invalidFeatureNoGeometry() throws Exception {
        return new JSONObject(
                "{\n"
                        + "  \"type\": \"Feature\",\n"
                        + "  \"properties\": {\n"
                        + "    \"name\": \"Dinagat Islands\"\n"
                        + "  }\n"
                        + "}");
    }

    /**
     * Geometry collection with a geometry missing the coordinates array member
     *
     * @return Geometry collection with invalid geometry
     */
    private JSONObject invalidGeometryCollectionInvalidGeometry() throws Exception {
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
                        + "}");
    }

    /**
     * Geometry collection with a geometry that is missing the geometries array member
     *
     * @return Geometry collection with no geometries
     */
    private JSONObject invalidGeometryCollectionInvalidGeometries() throws Exception {
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
                        + "}");
    }

    /**
     * Feature containing a geometry collection with an geometry that is missing the coordinates
     * member
     *
     * @return Feature containing a geometry collection with an invalid geometry
     */
    private JSONObject invalidFeatureGeometryCollectionInvalidGeometry() throws Exception {
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
                        + "}");
    }

    /**
     * Feature collection with no geometries array member
     *
     * @return Feature collection with no geometries array
     */
    private JSONObject invalidFeatureGeometryCollectionNoGeometries() throws Exception {
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
                        + "}");
    }

    /**
     * Feature missing its properties member, should be created with empty feature hashmap
     *
     * @return feature missing its properties
     */
    private JSONObject invalidFeatureNoProperties() throws Exception {
        return new JSONObject(
                "{\n"
                        + "  \"type\": \"Feature\",\n"
                        + "  \"geometry\": {\n"
                        + "    \"type\": \"Point\",\n"
                        + "    \"coordinates\": [125.6, 10.1]\n"
                        + "  }\n"
                        + "}");
    }

    /**
     * 2 valid features with 1 invalid feature missing its type member indicating that it is a
     * feature
     *
     * @return 2 valid features with 1 invalid feature
     */
    private JSONObject invalidFeatureCollectionNoGeometryTypeInFeature() throws Exception {
        return new JSONObject(
                "{ \"type\": \"FeatureCollection\",\n"
                        + "    \"features\": [\n"
                        + "      { \"type\": \"Feature\",\n"
                        + "        \"geometry\": {\"type\": \"Point\", \"coordinates\": [102.0,"
                        + " 0.5]},\n"
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
                        + "     }");
    }

    /**
     * FeatureCollection missing its feature array member
     *
     * @return FeatureCollection missing its feature array
     */
    private JSONObject invalidFeatureCollectionNoFeaturesArray() throws JSONException {
        return new JSONObject(
                "{ \"type\": \"FeatureCollection\",\n"
                        + "    \"INVALID\": [\n"
                        + "      { \"type\": \"Feature\",\n"
                        + "        \"geometry\": {\"type\": \"Point\", \"coordinates\": [102.0,"
                        + " 0.5]},\n"
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
                        + "     }");
    }

    /**
     * Geometry missing its coordinates member
     */
    private JSONObject invalidGeometryNoCoordinates() throws JSONException {
        return new JSONObject(
                "{ \"type\": \"LineString\",\n"
                        + "    \"banana\": [ [100.0, 0.0], [101.0, 1.0] ]\n"
                        + "    }");
    }

    /**
     * Feature with geometry missing its coordinates member
     *
     * @return Feature with geometry missing its coordinates
     */
    private JSONObject invalidFeatureNoCoordinatesInGeometry() throws JSONException {
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
                        + "}");
    }

    /**
     * Feature missing its type member
     *
     * @return Feature missing its type
     */
    private JSONObject invalidFeatureMissingType() throws JSONException {
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
                        + "}");
    }

    /**
     * Feature collection with a feature that contains a geometry missing its coordinates member
     *
     * @return Feature collection with feature missing its coordinates
     */
    private JSONObject invalidFeatureCollectionNoCoordinatesInGeometryInFeature() throws Exception {
        return new JSONObject(
                "{ \"type\": \"FeatureCollection\",\n"
                        + "    \"features\": [\n"
                        + "      { \"type\": \"Feature\",\n"
                        + "        \"geometry\": {\"type\": \"Point\", \"coordinates\": [102.0,"
                        + " 0.5]},\n"
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
                        + "     }");
    }

    /**
     * Feature collection with a feature that has a geometry missing its type member
     *
     * @return Feature collection with a feature missing its type
     */
    private JSONObject invalidFeatureCollectionNoTypeInGeometryInFeature() throws Exception {
        return new JSONObject(
                "{ \"type\": \"FeatureCollection\",\n"
                        + "    \"features\": [\n"
                        + "      { \"type\": \"Feature\",\n"
                        + "        \"geometry\": {\"type\": \"Point\", \"coordinates\": [102.0,"
                        + " 0.5]},\n"
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
                        + "     }");
    }
}
