/*
 * Copyright 2020 Google Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.google.maps.android.data.geojson;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Shared utilities for GeoJSON tests
 */
public class GeoJsonTestUtil {
    static JSONObject validGeometryCollection() throws Exception {
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

    static JSONObject validPoint() throws JSONException {
        return new JSONObject("{ \"type\": \"Point\", \"coordinates\": [100.0, 0.0] }");
    }

    static JSONObject validLineString() throws JSONException {
        return new JSONObject(
                "{ \"type\": \"LineString\",\n"
                        + "    \"coordinates\": [ [100.0, 0.0], [101.0, 1.0] ]\n"
                        + "    }");
    }

    static JSONObject validPolygon() throws JSONException {
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

    static JSONObject validMultiPoint() throws JSONException {
        return new JSONObject(
                "{ \"type\": \"MultiPoint\",\n"
                        + "    \"coordinates\": [ [100.0, 0.0], [101.0, 1.0] ]\n"
                        + "    }");
    }

    static JSONObject validMultiLineString() throws JSONException {
        return new JSONObject(
                "{ \"type\": \"MultiLineString\",\n"
                        + "    \"coordinates\": [\n"
                        + "        [ [100.0, 0.0], [101.0, 1.0] ],\n"
                        + "        [ [102.0, 2.0], [103.0, 3.0] ]\n"
                        + "      ]\n"
                        + "    }");
    }

    static JSONObject validMultiPolygon() throws Exception {
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

    static JSONObject validEmptyFeatureCollection() throws Exception {
        return new JSONObject(
                "{ \"type\": \"FeatureCollection\",\n" + "  \"features\": [\n" + "     ]\n" + "}");
    }

    static JSONObject emptyFile() {
        return new JSONObject();
    }

    static JSONObject invalidGeometryNoType() throws Exception {
        return new JSONObject("{\n" + "    \"coordinates\": [100.0, 0.0] \n" + "}");
    }

    /**
     * Geometry has coordinates member which is a single double instead of a pair of doubles
     *
     * @return geometry with invalid coordinates member
     */
    static JSONObject invalidGeometryInvalidCoordinatesPair() throws Exception {
        return new JSONObject("{ \"type\": \"Point\", \"coordinates\": [100.0] }");
    }

    /**
     * Geometry has coordinates member which is a pair of strings instead of a pair of doubles
     *
     * @return geometry with invalid coordinates member
     */
    static JSONObject invalidGeometryInvalidCoordinatesString() throws Exception {
        return new JSONObject("{ \"type\": \"Point\", \"coordinates\": [\"BANANA\", \"BOAT\"] }");
    }

    /**
     * Feature missing its geometry member, should be created with null geometry
     *
     * @return feature missing its geometry
     */
    static JSONObject invalidFeatureNoGeometry() throws Exception {
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
    static JSONObject invalidGeometryCollectionInvalidGeometry() throws Exception {
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
    static JSONObject invalidGeometryCollectionInvalidGeometries() throws Exception {
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
    static JSONObject invalidFeatureGeometryCollectionInvalidGeometry() throws Exception {
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
    static JSONObject invalidFeatureGeometryCollectionNoGeometries() throws Exception {
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
    static JSONObject invalidFeatureNoProperties() throws Exception {
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
    static JSONObject invalidFeatureCollectionNoGeometryTypeInFeature() throws Exception {
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
    static JSONObject invalidFeatureCollectionNoFeaturesArray() throws JSONException {
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
    static JSONObject invalidGeometryNoCoordinates() throws JSONException {
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
    static JSONObject invalidFeatureNoCoordinatesInGeometry() throws JSONException {
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
    static JSONObject invalidFeatureMissingType() throws JSONException {
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
    static JSONObject invalidFeatureCollectionNoCoordinatesInGeometryInFeature() throws Exception {
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
    static JSONObject invalidFeatureCollectionNoTypeInGeometryInFeature() throws Exception {
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

    /**
     * Creates a valid GeoJSON feature collection
     * @return a valid GeoJSON feature collection
     * @throws Exception
     */
    static JSONObject createFeatureCollection() throws Exception {
        return new JSONObject(
                "{ \"type\": \"FeatureCollection\",\n"
                        + "    \"features\": [\n"
                        + "      { \"type\": \"Feature\",\n"
                        + "        \"geometry\": {\"type\": \"MultiPoint\", \"coordinates\": [[102.0,"
                        + " 0.5], [100, 0.5]]},\n"
                        + "        \"properties\": {\"title\": \"Test MultiPoint\"}\n"
                        + "        },\n"
                        + "      { \"type\": \"Feature\",\n"
                        + "        \"geometry\": {\n"
                        + "          \"type\": \"MultiLineString\",\n"
                        + "          \"coordinates\": [\n"
                        + "            [[100, 0],[101, 1]], [[102, 2], [103, 3]]\n"
                        + "            ]\n"
                        + "          },\n"
                        + "        \"properties\": {\n"
                        + "          \"title\": \"Test MultiLineString\"\n"
                        + "          }\n"
                        + "        },\n"
                        + "      { \"type\": \"Feature\",\n"
                        + "         \"geometry\": {\n"
                        + "           \"type\": \"MultiPolygon\",\n"
                        + "           \"coordinates\": [\n"
                        + "             [[[102.0, 2.0], [103.0, 2.0], [103.0, 3.0], [102.0, 3.0],"
                        + " [102.0, 2.0]]],\n"
                        + "      [[[100.0, 0.0], [101.0, 0.0], [101.0, 1.0], [100.0, 1.0], [100.0,"
                        + " 0.0]],\n"
                        + "       [[100.2, 0.2], [100.8, 0.2], [100.8, 0.8], [100.2, 0.8], [100.2,"
                        + " 0.2]]],\n"
                        + "             ]\n"
                        + "         },\n"
                        + "         \"properties\": {\n"
                        + "           \"title\": \"Test MultiPolygon\"}\n"
                        + "         }\n"
                        + "       ]\n"
                        + "     }");
    }
}
