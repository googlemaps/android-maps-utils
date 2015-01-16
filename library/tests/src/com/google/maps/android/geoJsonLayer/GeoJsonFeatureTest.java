package com.google.maps.android.geoJsonLayer;

import junit.framework.TestCase;

import java.util.HashMap;

public class GeoJsonFeatureTest extends TestCase {

    public void setUp() throws Exception {
        super.setUp();

    }

    public void testGetId() throws Exception {
        GeoJsonFeature feature = new GeoJsonFeature(null, "Pirate", null, null);
        assertNotNull(feature.getId());
        assertTrue(feature.getId().equals("Pirate"));
        feature = new GeoJsonFeature(null, null, null, null);
        assertNull(feature.getId());
    }

    public void testGetProperty() throws Exception {
        HashMap<String, String> properties = new HashMap<String, String>();
        properties.put("Color", "Yellow");
        properties.put("Width", "5");
        GeoJsonFeature feature = new GeoJsonFeature(null, null, properties, null);
        assertNotNull(feature.getProperties());
        for (String string : feature.getProperties().keySet()) {
            assertTrue(string.equals("Color") || string.equals("Width"));
        }
        for (String string : feature.getProperties().values()) {
            assertTrue(string.equals("Yellow") || string.equals("5"));
        }
    }

    public void testPointStyle() {
        GeoJsonFeature feature = new GeoJsonFeature(null, null, null, null);
        GeoJsonPointStyle pointStyle = new GeoJsonPointStyle();
        feature.setPointStyle(pointStyle);
        assertTrue(pointStyle.equals(feature.getPointStyle()));
    }


}