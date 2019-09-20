package com.google.maps.android.data.geojson;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;

import org.junit.Before;
import org.junit.Test;
import org.junit.Assert;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Set;

public class GeoJsonRendererTest {

    GoogleMap mMap1;
    Set<GeoJsonFeature> geoJsonFeaturesSet;
    GeoJsonRenderer mRenderer;
    GeoJsonLayer mLayer;
    GeoJsonFeature mGeoJsonFeature;
    Collection<Object> mValues;

    @Before
    public void setUp() throws Exception {
        GeoJsonParser parser = new GeoJsonParser(createFeatureCollection());
        HashMap<GeoJsonFeature, Object> geoJsonFeatures = new HashMap<GeoJsonFeature, Object>();
        for (GeoJsonFeature feature : parser.getFeatures()) {
            geoJsonFeatures.put(feature, null);
        }
        geoJsonFeaturesSet = geoJsonFeatures.keySet();
        mRenderer = new GeoJsonRenderer(mMap1, geoJsonFeatures);
        mLayer = new GeoJsonLayer(mMap1, createFeatureCollection());
        GeoJsonLineString geoJsonLineString = new GeoJsonLineString(
                new ArrayList<>(Arrays.asList(new LatLng(0, 100), new LatLng(1, 101))));
        mGeoJsonFeature = new GeoJsonFeature(geoJsonLineString, null, null, null);
        mValues = geoJsonFeatures.values();
    }

    public void tearDown() throws Exception {

    }

    @Test
    public void testGetMap() throws Exception {
        Assert.assertEquals(mMap1, mRenderer.getMap());
    }

    @Test
    public void testSetMap() throws Exception {
        mLayer = new GeoJsonLayer(mMap1, createFeatureCollection());
        mMap1 = mLayer.getMap();
        mRenderer.setMap(mMap1);
        Assert.assertEquals(mMap1, mRenderer.getMap());
        mRenderer.setMap(null);
        Assert.assertEquals(null, mRenderer.getMap());
    }

    @Test
    public void testGetFeatures() throws Exception {
        Assert.assertEquals(geoJsonFeaturesSet, mRenderer.getFeatures());
    }

    @Test
    public void testAddFeature() throws Exception {
        mRenderer.addFeature(mGeoJsonFeature);
        Assert.assertTrue(mRenderer.getFeatures().contains(mGeoJsonFeature));
    }

    @Test
    public void testGetValues() {
        Assert.assertEquals(mValues.size(), mRenderer.getValues().size());
    }

    @Test
    public void testRemoveLayerFromMap() throws Exception {
        mLayer = new GeoJsonLayer(mMap1, createFeatureCollection());
        mRenderer.removeLayerFromMap();
        Assert.assertEquals(mMap1, mRenderer.getMap());
    }

    @Test
    public void testRemoveFeature() throws Exception {
        mRenderer.addFeature(mGeoJsonFeature);
        mRenderer.removeFeature(mGeoJsonFeature);
        Assert.assertFalse(mRenderer.getFeatures().contains(mGeoJsonFeature));
    }

    private JSONObject createFeatureCollection() throws Exception {
        return new JSONObject(
                "{ \"type\": \"FeatureCollection\",\n"
                        + "    \"features\": [\n"
                        + "      { \"type\": \"Feature\",\n"
                        + "        \"geometry\": {\"type\": \"MultiPoint\", \"coordinates\": [[102.0, 0.5], [100, 0.5]]},\n"
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
                        + "             [[[102.0, 2.0], [103.0, 2.0], [103.0, 3.0], [102.0, 3.0], [102.0, 2.0]]],\n" +
                        "      [[[100.0, 0.0], [101.0, 0.0], [101.0, 1.0], [100.0, 1.0], [100.0, 0.0]],\n" +
                        "       [[100.2, 0.2], [100.8, 0.2], [100.8, 0.8], [100.2, 0.8], [100.2, 0.2]]],\n"
                        + "             ]\n"
                        + "         },\n"
                        + "         \"properties\": {\n"
                        + "           \"title\": \"Test MultiPolygon\"}\n"
                        + "         }\n"
                        + "       ]\n"
                        + "     }"
        );
    }
}