package com.google.maps.android.data;

import com.google.android.gms.maps.model.LatLng;

import org.junit.Test;
import org.junit.Assert;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class FeatureTest {
    Feature feature;

    @Test
    public void testGetId() throws Exception {
        feature = new Feature(null, "Pirate", null);
        Assert.assertNotNull(feature.getId());
        Assert.assertTrue(feature.getId().equals("Pirate"));
        feature = new Feature(null, null, null);
        Assert.assertNull(feature.getId());
    }

    @Test
    public void testProperty() throws Exception {
        HashMap<String, String> properties = new HashMap<>();
        properties.put("Color", "Red");
        properties.put("Width", "3");
        feature = new Feature(null, null, properties);
        Assert.assertFalse(feature.hasProperty("llama"));
        Assert.assertTrue(feature.hasProperty("Color"));
        Assert.assertEquals("Red", feature.getProperty("Color"));
        Assert.assertTrue(feature.hasProperty("Width"));
        Assert.assertEquals("3", feature.getProperty("Width"));
        Assert.assertNull(feature.removeProperty("banana"));
        Assert.assertEquals("3", feature.removeProperty("Width"));
        Assert.assertNull(feature.setProperty("Width", "10"));
        Assert.assertEquals("10", feature.setProperty("Width", "500"));
    }

    @Test
    public void testGeometry() {
        feature = new Feature(null, null, null);
        Assert.assertNull(feature.getGeometry());
        Point point = new Point(new LatLng(0, 0));
        feature.setGeometry(point);
        Assert.assertEquals(point, feature.getGeometry());
        feature.setGeometry(null);
        Assert.assertNull(feature.getGeometry());

        LineString lineString = new LineString(new ArrayList<>(Arrays.asList(new LatLng(0, 0), new LatLng(50, 50))));
        feature = new Feature(lineString, null, null);
        Assert.assertEquals(lineString, feature.getGeometry());
        feature.setGeometry(point);
        Assert.assertEquals(point, feature.getGeometry());
        feature.setGeometry(null);
        Assert.assertNull(feature.getGeometry());
        feature.setGeometry(lineString);
        Assert.assertEquals(lineString, feature.getGeometry());
    }


}
