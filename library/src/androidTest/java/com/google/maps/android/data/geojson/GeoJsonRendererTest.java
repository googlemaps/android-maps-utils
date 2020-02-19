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

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.data.Feature;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Set;

import static com.google.maps.android.data.geojson.GeoJsonTestUtil.createFeatureCollection;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class GeoJsonRendererTest {
    private GoogleMap mMap1;
    private Set<GeoJsonFeature> geoJsonFeaturesSet;
    private GeoJsonRenderer mRenderer;
    private GeoJsonLayer mLayer;
    private GeoJsonFeature mGeoJsonFeature;
    private Collection<Object> mValues;

    @Before
    public void setUp() throws Exception {
        GeoJsonParser parser = new GeoJsonParser(createFeatureCollection());
        HashMap<GeoJsonFeature, Object> geoJsonFeatures = new HashMap<>();
        for (GeoJsonFeature feature : parser.getFeatures()) {
            // Set default styles
            feature.setPointStyle(new GeoJsonPointStyle());
            feature.setLineStringStyle(new GeoJsonLineStringStyle());
            feature.setPolygonStyle(new GeoJsonPolygonStyle());
            geoJsonFeatures.put(feature, null);
        }
        geoJsonFeaturesSet = geoJsonFeatures.keySet();
        mRenderer = new GeoJsonRenderer(mMap1, geoJsonFeatures, null, null, null, null);
        mLayer = new GeoJsonLayer(mMap1, createFeatureCollection());
        GeoJsonLineString geoJsonLineString =
                new GeoJsonLineString(
                        new ArrayList<>(Arrays.asList(new LatLng(0, 100), new LatLng(1, 101))));
        mGeoJsonFeature = new GeoJsonFeature(geoJsonLineString, null, null, null);
        mValues = geoJsonFeatures.values();
    }

    @Test
    public void testGetMap() {
        assertEquals(mMap1, mRenderer.getMap());
    }

    @Test
    public void testSetMap() throws Exception {
        mLayer = new GeoJsonLayer(mMap1, createFeatureCollection());
        mMap1 = mLayer.getMap();
        mRenderer.setMap(mMap1);
        assertEquals(mMap1, mRenderer.getMap());
        mRenderer.setMap(null);
        assertNull(mRenderer.getMap());
    }

    @Test
    public void testGetFeatures() {
        assertEquals(geoJsonFeaturesSet, mRenderer.getFeatures());
    }

    @Test
    public void testAddFeature() {
        mRenderer.addFeature(mGeoJsonFeature);
        assertTrue(mRenderer.getFeatures().contains(mGeoJsonFeature));
    }

    @Test
    public void testGetValues() {
        assertEquals(mValues.size(), mRenderer.getValues().size());
    }

    @Test
    public void testRemoveLayerFromMap() throws Exception {
        mLayer = new GeoJsonLayer(mMap1, createFeatureCollection());
        mRenderer.removeLayerFromMap();
        assertEquals(mMap1, mRenderer.getMap());
    }

    @Test
    public void testRemoveFeature() {
        mRenderer.addFeature(mGeoJsonFeature);
        mRenderer.removeFeature(mGeoJsonFeature);
        assertFalse(mRenderer.getFeatures().contains(mGeoJsonFeature));
    }

    @Test
    public void testDefaultStyleClickable() {
        // TODO - we should call mRenderer.addLayerToMap() here for a complete end-to-end test, but
        // that requires an instantiated GoogleMap be passed into GeoJsonRenderer()
        for (Feature f : mRenderer.getFeatures()) {
            assertTrue(((GeoJsonFeature)f).getPolylineOptions().isClickable());
            assertTrue(((GeoJsonFeature)f).getPolygonOptions().isClickable());
        }
    }

    @Test
    public void testCustomStyleNotClickable() throws Exception {
        GeoJsonParser parser = new GeoJsonParser(createFeatureCollection());
        HashMap<GeoJsonFeature, Object> geoJsonFeatures = new HashMap<>();
        for (GeoJsonFeature feature : parser.getFeatures()) {
            // Set default style for points
            feature.setPointStyle(new GeoJsonPointStyle());
            // Set custom style of not clickable for lines and polygons
            GeoJsonLineStringStyle ls = new GeoJsonLineStringStyle();
            ls.setClickable(false);
            feature.setLineStringStyle(ls);
            GeoJsonPolygonStyle ps = new GeoJsonPolygonStyle();
            ps.setClickable(false);
            feature.setPolygonStyle(ps);
            geoJsonFeatures.put(feature, null);
        }

        GeoJsonRenderer renderer = new GeoJsonRenderer(mMap1, geoJsonFeatures, null, null, null, null);
        // TODO - we should call renderer.addLayerToMap() here for a complete end-to-end test, but
        // that requires an instantiated GoogleMap be passed into GeoJsonRenderer()
        for (Feature f : renderer.getFeatures()) {
            assertFalse(((GeoJsonFeature)f).getPolylineOptions().isClickable());
            assertFalse(((GeoJsonFeature)f).getPolygonOptions().isClickable());
        }
    }
}
