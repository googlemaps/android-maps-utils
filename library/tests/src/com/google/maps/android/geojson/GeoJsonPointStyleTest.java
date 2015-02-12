package com.google.maps.android.geojson;

import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;

import android.test.AndroidTestCase;

import java.util.Arrays;

public class GeoJsonPointStyleTest extends AndroidTestCase {

    GeoJsonPointStyle pointStyle;

    public void setUp() throws Exception {
        super.setUp();
        MapsInitializer.initialize(getContext());
        pointStyle = new GeoJsonPointStyle();
    }

    public void testGetGeometryType() throws Exception {
        assertTrue(Arrays.asList(pointStyle.getGeometryType()).contains("Point"));
        assertTrue(Arrays.asList(pointStyle.getGeometryType()).contains("MultiPoint"));
        assertTrue(Arrays.asList(pointStyle.getGeometryType()).contains("GeometryCollection"));
        assertEquals(3, pointStyle.getGeometryType().length);
    }

    public void testAlpha() throws Exception {
        pointStyle.setAlpha(0.1234f);
        assertEquals(0.1234f, pointStyle.getAlpha());
        assertEquals(0.1234f, pointStyle.toMarkerOptions().getAlpha());
    }

    public void testAnchor() throws Exception {
        pointStyle.setAnchor(0.23f, 0.87f);
        assertEquals(0.23f, pointStyle.getAnchorU());
        assertEquals(0.87f, pointStyle.getAnchorV());
        assertEquals(0.23f, pointStyle.toMarkerOptions().getAnchorU());
        assertEquals(0.87f, pointStyle.toMarkerOptions().getAnchorV());
    }

    public void testDraggable() throws Exception {
        pointStyle.setDraggable(true);
        assertTrue(pointStyle.isDraggable());
        assertTrue(pointStyle.toMarkerOptions().isDraggable());
    }

    public void testFlat() throws Exception {
        pointStyle.setFlat(true);
        assertTrue(pointStyle.isFlat());
        assertTrue(pointStyle.toMarkerOptions().isFlat());
    }

    public void testIcon() throws Exception {
        BitmapDescriptor icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN);
        pointStyle
                .setIcon(icon);
        assertEquals(icon,
                pointStyle.getIcon());
        assertEquals(icon,
                pointStyle.toMarkerOptions().getIcon());
    }

    public void testInfoWindowAnchor() throws Exception {
        pointStyle.setInfoWindowAnchor(0.12f, 0.98f);
        assertEquals(0.12f, pointStyle.getInfoWindowAnchorU());
        assertEquals(0.98f, pointStyle.getInfoWindowAnchorV());
        assertEquals(0.12f, pointStyle.toMarkerOptions().getInfoWindowAnchorU());
        assertEquals(0.98f, pointStyle.toMarkerOptions().getInfoWindowAnchorV());
    }

    public void testRotation() throws Exception {
        pointStyle.setRotation(156.24f);
        assertEquals(156.24f, pointStyle.getRotation());
        assertEquals(156.24f, pointStyle.toMarkerOptions().getRotation());
    }

    public void testSnippet() throws Exception {
        pointStyle.setSnippet("The peaches are in a jar");
        assertEquals("The peaches are in a jar", pointStyle.getSnippet());
        assertEquals("The peaches are in a jar", pointStyle.toMarkerOptions().getSnippet());
    }

    public void testTitle() throws Exception {
        pointStyle.setTitle("Peaches");
        assertEquals("Peaches", pointStyle.getTitle());
        assertEquals("Peaches", pointStyle.toMarkerOptions().getTitle());
    }

    public void testVisible() throws Exception {
        pointStyle.setVisible(false);
        assertFalse(pointStyle.isVisible());
        assertFalse(pointStyle.toMarkerOptions().isVisible());

    }

    public void testDefaultPointStyle() throws Exception {
        assertEquals(1.0f, pointStyle.getAlpha());
        assertEquals(0.5f, pointStyle.getAnchorU());
        assertEquals(1.0f, pointStyle.getAnchorV());
        assertFalse(pointStyle.isDraggable());
        assertFalse(pointStyle.isFlat());
        assertNull(pointStyle.getIcon());
        assertEquals(0.5f, pointStyle.getInfoWindowAnchorU());
        assertEquals(0.0f, pointStyle.getInfoWindowAnchorV());
        assertEquals(0.0f, pointStyle.getRotation());
        assertNull(pointStyle.getSnippet());
        assertNull(pointStyle.getTitle());
        assertTrue(pointStyle.isVisible());
    }

    public void testDefaultGetMarkerOptions() throws Exception {
        assertEquals(1.0f, pointStyle.toMarkerOptions().getAlpha());
        assertEquals(0.5f, pointStyle.toMarkerOptions().getAnchorU());
        assertEquals(1.0f, pointStyle.toMarkerOptions().getAnchorV());
        assertFalse(pointStyle.toMarkerOptions().isDraggable());
        assertFalse(pointStyle.toMarkerOptions().isFlat());
        assertNull(pointStyle.toMarkerOptions().getIcon());
        assertEquals(0.5f, pointStyle.toMarkerOptions().getInfoWindowAnchorU());
        assertEquals(0.0f, pointStyle.toMarkerOptions().getInfoWindowAnchorV());
        assertEquals(0.0f, pointStyle.toMarkerOptions().getRotation());
        assertNull(pointStyle.toMarkerOptions().getSnippet());
        assertNull(pointStyle.toMarkerOptions().getTitle());
        assertTrue(pointStyle.toMarkerOptions().isVisible());
    }

}