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

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.data.Geometry;

import org.json.JSONObject;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.google.maps.android.data.geojson.GeoJsonTestUtil.emptyFile;
import static com.google.maps.android.data.geojson.GeoJsonTestUtil.invalidFeatureCollectionNoCoordinatesInGeometryInFeature;
import static com.google.maps.android.data.geojson.GeoJsonTestUtil.invalidFeatureCollectionNoFeaturesArray;
import static com.google.maps.android.data.geojson.GeoJsonTestUtil.invalidFeatureCollectionNoGeometryTypeInFeature;
import static com.google.maps.android.data.geojson.GeoJsonTestUtil.invalidFeatureCollectionNoTypeInGeometryInFeature;
import static com.google.maps.android.data.geojson.GeoJsonTestUtil.invalidFeatureGeometryCollectionInvalidGeometry;
import static com.google.maps.android.data.geojson.GeoJsonTestUtil.invalidFeatureGeometryCollectionNoGeometries;
import static com.google.maps.android.data.geojson.GeoJsonTestUtil.invalidFeatureMissingType;
import static com.google.maps.android.data.geojson.GeoJsonTestUtil.invalidFeatureNoCoordinatesInGeometry;
import static com.google.maps.android.data.geojson.GeoJsonTestUtil.invalidFeatureNoGeometry;
import static com.google.maps.android.data.geojson.GeoJsonTestUtil.invalidFeatureNoProperties;
import static com.google.maps.android.data.geojson.GeoJsonTestUtil.invalidGeometryCollectionInvalidGeometries;
import static com.google.maps.android.data.geojson.GeoJsonTestUtil.invalidGeometryCollectionInvalidGeometry;
import static com.google.maps.android.data.geojson.GeoJsonTestUtil.invalidGeometryInvalidCoordinatesPair;
import static com.google.maps.android.data.geojson.GeoJsonTestUtil.invalidGeometryInvalidCoordinatesString;
import static com.google.maps.android.data.geojson.GeoJsonTestUtil.invalidGeometryNoCoordinates;
import static com.google.maps.android.data.geojson.GeoJsonTestUtil.invalidGeometryNoType;
import static com.google.maps.android.data.geojson.GeoJsonTestUtil.validGeometryCollection;
import static com.google.maps.android.data.geojson.GeoJsonTestUtil.validLineString;
import static com.google.maps.android.data.geojson.GeoJsonTestUtil.validMultiLineString;
import static com.google.maps.android.data.geojson.GeoJsonTestUtil.validMultiPoint;
import static com.google.maps.android.data.geojson.GeoJsonTestUtil.validMultiPolygon;
import static com.google.maps.android.data.geojson.GeoJsonTestUtil.validPoint;
import static com.google.maps.android.data.geojson.GeoJsonTestUtil.validPolygon;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

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
}
