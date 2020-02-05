package com.google.maps.android.data;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.data.kml.KmlPlacemark;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.*;

public class RendererTest {
    private GoogleMap mMap1;
    private Renderer mRenderer;
    private Set<Feature> featureSet;

    @Before
    public void setUp() {
        HashMap<Feature, Object> features = new HashMap<>();

        LineString lineString =
                new LineString(
                        new ArrayList<>(Arrays.asList(new LatLng(0, 0), new LatLng(50, 50))));
        Feature feature1 = new Feature(lineString, null, null);
        Point point = new Point(new LatLng(0, 0));
        Feature feature2 = new Feature(point, null, null);
        features.put(feature1, null);
        features.put(feature2, null);
        featureSet = features.keySet();
        mRenderer = new Renderer(mMap1, features, null, null, null, null);
    }

    @Test
    public void testGetMap() {
        assertEquals(mMap1, mRenderer.getMap());
    }

    @Test
    public void testGetFeatures() {
        assertEquals(featureSet, mRenderer.getFeatures());
    }

    @Test
    public void testAddFeature() {
        Point p = new Point(new LatLng(30, 50));
        Feature feature1 = new Feature(p, null, null);
        mRenderer.addFeature(feature1);
        assertTrue(mRenderer.getFeatures().contains(feature1));
    }

    @Test
    public void testRemoveFeature() {
        Point p = new Point(new LatLng(40, 50));
        Feature feature1 = new Feature(p, null, null);
        mRenderer.addFeature(feature1);
        mRenderer.removeFeature(feature1);
        assertFalse(mRenderer.getFeatures().contains(feature1));
    }

    @Test
    public void testSubstituteProperties() {
        Map<String, String> properties = new HashMap<>();
        properties.put("name", "Bruce Wayne");
        properties.put("description", "Batman");
        properties.put("Snippet", "I am the night");
        KmlPlacemark placemark = new KmlPlacemark(null, null, null, properties);

        String result1 = Renderer.substituteProperties("$[name] is my name", placemark);
        assertEquals("Bruce Wayne is my name", result1);

        String result2 = Renderer.substituteProperties("Also known as $[description]", placemark);
        assertEquals("Also known as Batman", result2);

        String result3 = Renderer.substituteProperties("I say \"$[Snippet]\" often", placemark);
        assertEquals("I say \"I am the night\" often", result3);

        String result4 = Renderer.substituteProperties("My address is $[address]", placemark);
        assertEquals("When property doesn't exist, placeholder is left in place", "My address is $[address]", result4);
    }
}
