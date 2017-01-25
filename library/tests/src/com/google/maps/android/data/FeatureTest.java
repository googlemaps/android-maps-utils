package com.google.maps.android.data;

import com.google.android.gms.maps.model.LatLng;

import junit.framework.TestCase;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class FeatureTest extends TestCase {
    Feature feature;

    public void testGetId() throws Exception {
        feature = new Feature(null, "Pirate", null);
        assertNotNull(feature.getId());
        assertTrue(feature.getId().equals("Pirate"));
        feature = new Feature(null, null, null);
        assertNull(feature.getId());
    }

    public void testProperty() throws Exception {
        HashMap<String, String> properties = new HashMap<>();
        properties.put("Color", "Red");
        properties.put("Width", "3");
        feature = new Feature(null, null, properties);
        assertFalse(feature.hasProperty("llama"));
        assertTrue(feature.hasProperty("Color"));
        assertEquals("Red", feature.getProperty("Color"));
        assertTrue(feature.hasProperty("Width"));
        assertEquals("3", feature.getProperty("Width"));
        assertNull(feature.removeProperty("banana"));
        assertEquals("3", feature.removeProperty("Width"));
        assertNull(feature.setProperty("Width", "10"));
        assertEquals("10", feature.setProperty("Width", "500"));
    }

    public void testGeometry() {
        feature = new Feature(null, null, null);
        assertNull(feature.getGeometry());
        Point point = new Point(new LatLng(0, 0));
        feature.setGeometry(point);
        assertEquals(point, feature.getGeometry());
        feature.setGeometry(null);
        assertNull(feature.getGeometry());

        LineString lineString = new LineString(new ArrayList<>(Arrays.asList(new LatLng(0, 0), new LatLng(50, 50))));
        feature = new Feature(lineString, null, null);
        assertEquals(lineString, feature.getGeometry());
        feature.setGeometry(point);
        assertEquals(point, feature.getGeometry());
        feature.setGeometry(null);
        assertNull(feature.getGeometry());
        feature.setGeometry(lineString);
        assertEquals(lineString, feature.getGeometry());
    }



}
