package com.google.maps.android.data.geojson;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;

import org.junit.Test;
import org.junit.Assert;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class GeoJsonFeatureTest {

    GeoJsonFeature feature;

    @Test
    public void testGetId() throws Exception {
        feature = new GeoJsonFeature(null, "Pirate", null, null);
        Assert.assertNotNull(feature.getId());
        Assert.assertTrue(feature.getId().equals("Pirate"));
        feature = new GeoJsonFeature(null, null, null, null);
        Assert.assertNull(feature.getId());
    }

    @Test
    public void testProperty() throws Exception {
        HashMap<String, String> properties = new HashMap<>();
        properties.put("Color", "Yellow");
        properties.put("Width", "5");
        feature = new GeoJsonFeature(null, null, properties, null);
        Assert.assertFalse(feature.hasProperty("llama"));
        Assert.assertTrue(feature.hasProperty("Color"));
        Assert.assertEquals("Yellow", feature.getProperty("Color"));
        Assert.assertTrue(feature.hasProperty("Width"));
        Assert.assertEquals("5", feature.getProperty("Width"));
        Assert.assertNull(feature.removeProperty("banana"));
        Assert.assertEquals("5", feature.removeProperty("Width"));
        Assert.assertNull(feature.setProperty("Width", "10"));
        Assert.assertEquals("10", feature.setProperty("Width", "500"));
    }

    @Test
    public void testNullProperty() throws Exception {
        GeoJsonLayer layer = new GeoJsonLayer(null, createFeatureCollection());
        GeoJsonFeature feature = layer.getFeatures().iterator().next();
        Assert.assertTrue(feature.hasProperty("prop0"));
        Assert.assertNull(feature.getProperty("prop0"));
        Assert.assertFalse(feature.hasProperty("prop1"));
        Assert.assertNull(feature.getProperty("prop1"));
    }

    @Test
    public void testPointStyle() {
        feature = new GeoJsonFeature(null, null, null, null);
        GeoJsonPointStyle pointStyle = new GeoJsonPointStyle();
        feature.setPointStyle(pointStyle);
        Assert.assertEquals(pointStyle, feature.getPointStyle());

        try {
            feature.setPointStyle(null);
            Assert.fail();
        } catch (IllegalArgumentException e) {
            Assert.assertEquals("Point style cannot be null", e.getMessage());
        }
    }

    @Test
    public void testLineStringStyle() {
        feature = new GeoJsonFeature(null, null, null, null);
        GeoJsonLineStringStyle lineStringStyle = new GeoJsonLineStringStyle();
        feature.setLineStringStyle(lineStringStyle);
        Assert.assertEquals(lineStringStyle, feature.getLineStringStyle());

        try {
            feature.setLineStringStyle(null);
            Assert.fail();
        } catch (IllegalArgumentException e) {
            Assert.assertEquals("Line string style cannot be null", e.getMessage());
        }
    }

    public void testPolygonStyle() {
        feature = new GeoJsonFeature(null, null, null, null);
        GeoJsonPolygonStyle polygonStyle = new GeoJsonPolygonStyle();
        feature.setPolygonStyle(polygonStyle);
        Assert.assertEquals(polygonStyle, feature.getPolygonStyle());

        try {
            feature.setPolygonStyle(null);
            Assert.fail();
        } catch (IllegalArgumentException e) {
            Assert.assertEquals("Polygon style cannot be null", e.getMessage());
        }
    }

    public void testGeometry() {
        feature = new GeoJsonFeature(null, null, null, null);
        Assert.assertNull(feature.getGeometry());
        GeoJsonPoint point = new GeoJsonPoint(new LatLng(0, 0));
        feature.setGeometry(point);
        Assert.assertEquals(point, feature.getGeometry());
        feature.setGeometry(null);
        Assert.assertNull(feature.getGeometry());

        GeoJsonLineString lineString = new GeoJsonLineString(new ArrayList<LatLng>(Arrays.asList(new LatLng(0, 0), new LatLng(50, 50))));
        feature = new GeoJsonFeature(lineString, null, null, null);
        Assert.assertEquals(lineString, feature.getGeometry());
        feature.setGeometry(point);
        Assert.assertEquals(point, feature.getGeometry());
        feature.setGeometry(null);
        Assert.assertNull(feature.getGeometry());
        feature.setGeometry(lineString);
        Assert.assertEquals(lineString, feature.getGeometry());
    }

    public void testGetBoundingBox() {
        feature = new GeoJsonFeature(null, null, null, null);
        Assert.assertNull(feature.getBoundingBox());

        LatLngBounds boundingBox = new LatLngBounds(new LatLng(-20, -20), new LatLng(50, 50));
        feature = new GeoJsonFeature(null, null, null, boundingBox);
        Assert.assertEquals(boundingBox, feature.getBoundingBox());
    }

    private JSONObject createFeatureCollection() throws Exception {
        return new JSONObject(
                "{ \"type\": \"FeatureCollection\",\n"
                        + "\"bbox\": [-150.0, -80.0, 150.0, 80.0],"
                        + "    \"features\": [\n"
                        + "      { \"type\": \"Feature\",\n"
                        + "        \"id\": \"point\", \n"
                        + "        \"geometry\": {\"type\": \"Point\", \"coordinates\": [102.0, 0.5]},\n"
                        + "        \"properties\": {\"prop0\": null}\n"
                        + "        }\n"
                        + "       ]\n"
                        + "     }"
        );
    }

}