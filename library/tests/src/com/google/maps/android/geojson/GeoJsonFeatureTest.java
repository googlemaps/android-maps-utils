package com.google.maps.android.geojson;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;

import junit.framework.TestCase;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class GeoJsonFeatureTest extends TestCase {

    GeoJsonFeature feature;

    public void testGetId() throws Exception {
        feature = new GeoJsonFeature(null, "Pirate", null, null);
        assertNotNull(feature.getId());
        assertTrue(feature.getId().equals("Pirate"));
        feature = new GeoJsonFeature(null, null, null, null);
        assertNull(feature.getId());
    }

    public void testProperty() throws Exception {
        HashMap<String, String> properties = new HashMap<String, String>();
        properties.put("Color", "Yellow");
        properties.put("Width", "5");
        feature = new GeoJsonFeature(null, null, properties, null);
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

    public void testNullProperty() throws Exception {
        GeoJsonLayer layer = new GeoJsonLayer(null, createFeatureCollection());
        GeoJsonFeature feature = layer.getFeatures().iterator().next();
        assertTrue(feature.hasProperty("prop0"));
        assertNull(feature.getProperty("prop0"));
        assertFalse(feature.hasProperty("prop1"));
        assertNull(feature.getProperty("prop1"));
    }

    public void testPointStyle() {
        feature = new GeoJsonFeature(null, null, null, null);
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

    public void testLineStringStyle() {
        feature = new GeoJsonFeature(null, null, null, null);
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

    public void testPolygonStyle() {
        feature = new GeoJsonFeature(null, null, null, null);
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

    public void testGeometry() {
        feature = new GeoJsonFeature(null, null, null, null);
        assertNull(feature.getGeometry());
        GeoJsonPoint point = new GeoJsonPoint(new LatLng(0, 0));
        feature.setGeometry(point);
        assertEquals(point, feature.getGeometry());
        feature.setGeometry(null);
        assertNull(feature.getGeometry());

        GeoJsonLineString lineString = new GeoJsonLineString(new ArrayList<LatLng>(Arrays.asList(new LatLng(0, 0), new LatLng(50, 50))));
        feature = new GeoJsonFeature(lineString, null, null, null);
        assertEquals(lineString, feature.getGeometry());
        feature.setGeometry(point);
        assertEquals(point, feature.getGeometry());
        feature.setGeometry(null);
        assertNull(feature.getGeometry());
        feature.setGeometry(lineString);
        assertEquals(lineString, feature.getGeometry());
    }

    public void testGetBoundingBox() {
        feature = new GeoJsonFeature(null, null, null, null);
        assertNull(feature.getBoundingBox());

        LatLngBounds boundingBox = new LatLngBounds(new LatLng(-20, -20), new LatLng(50, 50));
        feature = new GeoJsonFeature(null, null, null, boundingBox);
        assertEquals(boundingBox, feature.getBoundingBox());
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