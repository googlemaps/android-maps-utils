package com.google.maps.android.data.geojson;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.maps.android.data.Feature;

import org.junit.Before;
import org.junit.Test;
import org.junit.Assert;

import org.json.JSONObject;

import android.graphics.Color;

public class GeoJsonLayerTest {
    GoogleMap map;
    GeoJsonLayer mLayer;
    GoogleMap map2;

    @Before
    public void setUp() throws Exception {
        mLayer = new GeoJsonLayer(map, createFeatureCollection());
    }

    @Test
    public void testGetFeatures() throws Exception {
        int featureCount = 0;
        for (Feature ignored : mLayer.getFeatures()) {
            featureCount++;
        }
        Assert.assertEquals(3, featureCount);
    }

    @Test
    public void testAddFeature() throws Exception {
        int featureCount = 0;
        mLayer.addFeature(new GeoJsonFeature(null, null, null, null));
        for (Feature ignored : mLayer.getFeatures()) {
            featureCount++;
        }
        Assert.assertEquals(4, featureCount);
    }

    @Test
    public void testRemoveFeature() throws Exception {
        int featureCount = 0;
        for (Feature ignored : mLayer.getFeatures()) {
            featureCount++;
        }
        Assert.assertEquals(3, featureCount);
    }

    @Test
    public void testMap() throws Exception {
        Assert.assertEquals(map, mLayer.getMap());
        mLayer.setMap(map2);
        Assert.assertEquals(map2, mLayer.getMap());
        mLayer.setMap(null);
        Assert.assertEquals(null, mLayer.getMap());
    }

    @Test
    public void testDefaultPointStyle() throws Exception {
        mLayer.getDefaultPointStyle().setTitle("Dolphin");
        Assert.assertEquals("Dolphin", mLayer.getDefaultPointStyle().getTitle());
    }

    @Test
    public void testDefaultLineStringStyle() throws Exception {
        mLayer.getDefaultLineStringStyle().setColor(Color.BLUE);
        Assert.assertEquals(Color.BLUE, mLayer.getDefaultLineStringStyle().getColor());
    }

    @Test
    public void testDefaultPolygonStyle() throws Exception {
        mLayer.getDefaultPolygonStyle().setGeodesic(true);
        Assert.assertEquals(true, mLayer.getDefaultPolygonStyle().isGeodesic());
    }

    @Test
    public void testGetBoundingBox() throws Exception {
        Assert.assertEquals(new LatLngBounds(new LatLng(-80, -150), new LatLng(80, 150)), mLayer.getBoundingBox());
    }

    private JSONObject createFeatureCollection() throws Exception {
        return new JSONObject(
                "{ \"type\": \"FeatureCollection\",\n"
                        + "\"bbox\": [-150.0, -80.0, 150.0, 80.0],"
                        + "    \"features\": [\n"
                        + "      { \"type\": \"Feature\",\n"
                        + "        \"id\": \"point\", \n"
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
}