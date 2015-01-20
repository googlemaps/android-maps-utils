package com.google.maps.android.geojson;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;

import junit.framework.TestCase;

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

    public void testGetProperty() throws Exception {
        HashMap<String, String> properties = new HashMap<String, String>();
        properties.put("Color", "Yellow");
        properties.put("Width", "5");
        feature = new GeoJsonFeature(null, null, properties, null);
        assertNotNull(feature.getProperties());
        for (String string : feature.getProperties().keySet()) {
            assertTrue(string.equals("Color") || string.equals("Width"));
        }
        for (String string : feature.getProperties().values()) {
            assertTrue(string.equals("Yellow") || string.equals("5"));
        }
    }

    public void testPointStyle() {
        feature = new GeoJsonFeature(null, null, null, null);
        GeoJsonPointStyle pointStyle = new GeoJsonPointStyle();
        feature.setPointStyle(pointStyle);
        assertEquals(pointStyle, feature.getPointStyle());
    }

    public void testLineStringStyle() {
        feature = new GeoJsonFeature(null, null, null, null);
        GeoJsonLineStringStyle lineStringStyle = new GeoJsonLineStringStyle();
        feature.setLineStringStyle(lineStringStyle);
        assertEquals(lineStringStyle, feature.getLineStringStyle());
    }

    public void testPolygonStyle() {
        feature = new GeoJsonFeature(null, null, null, null);
        GeoJsonPolygonStyle polygonStyle = new GeoJsonPolygonStyle();
        feature.setPolygonStyle(polygonStyle);
        assertEquals(polygonStyle, feature.getPolygonStyle());
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


}