package com.google.maps.android.geojson;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;

import junit.framework.TestCase;

import org.json.JSONObject;

import android.graphics.Color;

public class GeoJsonLayerTest extends TestCase {
    GoogleMap map;
    GeoJsonLayer mLayer;
    GoogleMap map2;
    public void setUp() throws Exception {
        super.setUp();
        mLayer = new GeoJsonLayer(map, createFeatureCollection());
    }

    public void testGetFeatures() throws Exception {
        int featureCount = 0;
        for (GeoJsonFeature feature : mLayer.getFeatures()) {
            featureCount++;
        }
        assertEquals(3, featureCount);
    }

    public void testAddFeature() throws Exception {
        int featureCount = 0;
        mLayer.addFeature(new GeoJsonFeature(null, null, null, null));
        for (GeoJsonFeature feature : mLayer.getFeatures()) {
            featureCount++;
        }
        assertEquals(4, featureCount);
    }

    public void testRemoveFeature() throws Exception {
        int featureCount = 0;
        for (GeoJsonFeature feature : mLayer.getFeatures()) {
            featureCount++;
        }
        assertEquals(3, featureCount);
    }

    public void testMap() throws Exception {
        assertEquals(map, mLayer.getMap());
        mLayer.setMap(map2);
        assertEquals(map2, mLayer.getMap());
        mLayer.setMap(null);
        assertEquals(null, mLayer.getMap());
    }

    public void testDefaultPointStyle() throws Exception {
        mLayer.getDefaultPointStyle().setTitle("Dolphin");
        assertEquals("Dolphin", mLayer.getDefaultPointStyle().getTitle());
    }

    public void testDefaultLineStringStyle() throws Exception {
        mLayer.getDefaultLineStringStyle().setColor(Color.BLUE);
        assertEquals(Color.BLUE, mLayer.getDefaultLineStringStyle().getColor());
    }

    public void testDefaultPolygonStyle() throws Exception {
        mLayer.getDefaultPolygonStyle().setGeodesic(true);
        assertEquals(true, mLayer.getDefaultPolygonStyle().isGeodesic());
    }

    public void testGetBoundingBox() throws Exception {
        assertEquals(new LatLngBounds(new LatLng(-80, -150), new LatLng(80, 150)), mLayer.getBoundingBox());
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