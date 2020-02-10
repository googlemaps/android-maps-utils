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

import android.graphics.Color;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.maps.android.data.Feature;

import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class GeoJsonLayerTest {
    private GoogleMap map;
    private GeoJsonLayer mLayer;
    private GoogleMap map2;

    @Before
    public void setUp() throws Exception {
        mLayer = new GeoJsonLayer(map, createFeatureCollection());
    }

    @Test
    public void testGetFeatures() {
        int featureCount = 0;
        for (Feature ignored : mLayer.getFeatures()) {
            featureCount++;
        }
        assertEquals(3, featureCount);
    }

    @Test
    public void testAddFeature() {
        int featureCount = 0;
        mLayer.addFeature(new GeoJsonFeature(null, null, null, null));
        for (Feature ignored : mLayer.getFeatures()) {
            featureCount++;
        }
        assertEquals(4, featureCount);
    }

    @Test
    public void testRemoveFeature() {
        int featureCount = 0;
        for (Feature ignored : mLayer.getFeatures()) {
            featureCount++;
        }
        assertEquals(3, featureCount);
    }

    @Test
    public void testMap() {
        assertEquals(map, mLayer.getMap());
        mLayer.setMap(map2);
        assertEquals(map2, mLayer.getMap());
        mLayer.setMap(null);
        assertNull(mLayer.getMap());
    }

    @Test
    public void testDefaultPointStyle() {
        mLayer.getDefaultPointStyle().setTitle("Dolphin");
        assertEquals("Dolphin", mLayer.getDefaultPointStyle().getTitle());
    }

    @Test
    public void testDefaultLineStringStyle() {
        mLayer.getDefaultLineStringStyle().setColor(Color.BLUE);
        assertEquals(Color.BLUE, mLayer.getDefaultLineStringStyle().getColor());
    }

    @Test
    public void testDefaultPolygonStyle() {
        mLayer.getDefaultPolygonStyle().setGeodesic(true);
        assertTrue(mLayer.getDefaultPolygonStyle().isGeodesic());
    }

    @Test
    public void testGetBoundingBox() {
        assertEquals(
                new LatLngBounds(new LatLng(-80, -150), new LatLng(80, 150)),
                mLayer.getBoundingBox());
    }

    private JSONObject createFeatureCollection() throws Exception {
        return new JSONObject(
                "{ \"type\": \"FeatureCollection\",\n"
                        + "\"bbox\": [-150.0, -80.0, 150.0, 80.0],    \"features\": [\n"
                        + "      { \"type\": \"Feature\",\n"
                        + "        \"id\": \"point\", \n"
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
}
