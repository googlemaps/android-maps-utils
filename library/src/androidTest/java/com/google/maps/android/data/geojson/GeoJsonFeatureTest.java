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
import com.google.android.gms.maps.model.LatLngBounds;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import static org.junit.Assert.*;

public class GeoJsonFeatureTest {
    @Test
    public void testGetId() {
        GeoJsonFeature feature = new GeoJsonFeature(null, "Pirate", null, null);
        assertNotNull(feature.getId());
        assertEquals("Pirate", feature.getId());
        feature = new GeoJsonFeature(null, null, null, null);
        assertNull(feature.getId());
    }

    @Test
    public void testProperty() {
        HashMap<String, String> properties = new HashMap<>();
        properties.put("Color", "Yellow");
        properties.put("Width", "5");
        GeoJsonFeature feature = new GeoJsonFeature(null, null, properties, null);
        assertFalse(feature.hasProperty("llama"));
        assertTrue(feature.hasProperty("Color"));
        assertEquals("Yellow", feature.getProperty("Color"));
        assertTrue(feature.hasProperty("Width"));
        assertEquals("5", feature.getProperty("Width"));
        assertNull(feature.removeProperty("banana"));
        assertEquals("5", feature.removeProperty("Width"));
        assertNull(feature.setProperty("Width", "10"));
        assertEquals("10", feature.setProperty("Width", "500"));
    }

    @Test
    public void testNullProperty() {
        GeoJsonLayer layer = new GeoJsonLayer(null, createFeatureCollection());
        GeoJsonFeature feature = layer.getFeatures().iterator().next();
        assertTrue(feature.hasProperty("prop0"));
        assertNull(feature.getProperty("prop0"));
        assertFalse(feature.hasProperty("prop1"));
        assertNull(feature.getProperty("prop1"));
    }

    @Test
    public void testPointStyle() {
        GeoJsonFeature feature = new GeoJsonFeature(null, null, null, null);
        GeoJsonPointStyle pointStyle = new GeoJsonPointStyle();
        feature.setPointStyle(pointStyle);
        assertEquals(pointStyle, feature.getPointStyle());

        try {
            feature.setPointStyle(null);
            fail();
        } catch (IllegalArgumentException e) {
            assertEquals("Point style cannot be null", e.getMessage());
        }
    }

    @Test
    public void testLineStringStyle() {
        GeoJsonFeature feature = new GeoJsonFeature(null, null, null, null);
        GeoJsonLineStringStyle lineStringStyle = new GeoJsonLineStringStyle();
        feature.setLineStringStyle(lineStringStyle);
        assertEquals(lineStringStyle, feature.getLineStringStyle());

        try {
            feature.setLineStringStyle(null);
            fail();
        } catch (IllegalArgumentException e) {
            assertEquals("Line string style cannot be null", e.getMessage());
        }
    }

    @Test
    public void testPolygonStyle() {
        GeoJsonFeature feature = new GeoJsonFeature(null, null, null, null);
        GeoJsonPolygonStyle polygonStyle = new GeoJsonPolygonStyle();
        feature.setPolygonStyle(polygonStyle);
        assertEquals(polygonStyle, feature.getPolygonStyle());

        try {
            feature.setPolygonStyle(null);
            fail();
        } catch (IllegalArgumentException e) {
            assertEquals("Polygon style cannot be null", e.getMessage());
        }
    }

    @Test
    public void testGeometry() {
        GeoJsonFeature feature = new GeoJsonFeature(null, null, null, null);
        assertNull(feature.getGeometry());
        GeoJsonPoint point = new GeoJsonPoint(new LatLng(0, 0));
        feature.setGeometry(point);
        assertEquals(point, feature.getGeometry());
        feature.setGeometry(null);
        assertNull(feature.getGeometry());

        GeoJsonLineString lineString =
                new GeoJsonLineString(
                        new ArrayList<>(Arrays.asList(new LatLng(0, 0), new LatLng(50, 50))));
        feature = new GeoJsonFeature(lineString, null, null, null);
        assertEquals(lineString, feature.getGeometry());
        feature.setGeometry(point);
        assertEquals(point, feature.getGeometry());
        feature.setGeometry(null);
        assertNull(feature.getGeometry());
        feature.setGeometry(lineString);
        assertEquals(lineString, feature.getGeometry());
    }

    @Test
    public void testGetBoundingBox() {
        GeoJsonFeature feature = new GeoJsonFeature(null, null, null, null);
        assertNull(feature.getBoundingBox());

        LatLngBounds boundingBox = new LatLngBounds(new LatLng(-20, -20), new LatLng(50, 50));
        feature = new GeoJsonFeature(null, null, null, boundingBox);
        assertEquals(boundingBox, feature.getBoundingBox());
    }

    private JSONObject createFeatureCollection() {
        try {
            return new JSONObject(
                    "{ \"type\": \"FeatureCollection\",\n"
                            + "\"bbox\": [-150.0, -80.0, 150.0, 80.0],    \"features\": [\n"
                            + "      { \"type\": \"Feature\",\n"
                            + "        \"id\": \"point\", \n"
                            + "        \"geometry\": {\"type\": \"Point\", \"coordinates\": [102.0,"
                            + " 0.5]},\n"
                            + "        \"properties\": {\"prop0\": null}\n"
                            + "        }\n"
                            + "       ]\n"
                            + "     }");
        } catch (JSONException e) {
            fail();
        }
        return null;
    }
}
