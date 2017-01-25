package com.google.maps.android.data;


import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;

import junit.framework.TestCase;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Set;

public class RendererTest extends TestCase {

    GoogleMap mMap1;
    Renderer mRenderer;
    Set<Feature> featureSet;

    public void setUp() throws Exception {
        super.setUp();
        HashMap<Feature, Object> features = new HashMap<>();

        LineString lineString = new LineString(new ArrayList<>(Arrays.asList(new LatLng(0, 0), new LatLng(50, 50))));
        Feature feature1 = new Feature(lineString, null, null);
        Point point = new Point(new LatLng(0, 0));
        Feature feature2 = new Feature(point, null, null);
        features.put(feature1, null);
        features.put(feature2, null);
        featureSet = features.keySet();
        mRenderer = new Renderer(mMap1, features);
    }

    public void testGetMap() throws Exception {
        assertEquals(mMap1, mRenderer.getMap());
    }

    public void testGetFeatures() throws Exception {
        assertEquals(featureSet, mRenderer.getFeatures());
    }

    public void testAddFeature() throws Exception {
        Point p = new Point(new LatLng(30, 50));
        Feature feature1 = new Feature(p, null, null);
        mRenderer.addFeature(feature1);
        assertTrue(mRenderer.getFeatures().contains(feature1));
    }

    public void testRemoveFeature() throws Exception {
        Point p = new Point(new LatLng(40, 50));
        Feature feature1 = new Feature(p, null, null);
        mRenderer.addFeature(feature1);
        mRenderer.removeFeature(feature1);
        assertFalse(mRenderer.getFeatures().contains(feature1));
    }






}
