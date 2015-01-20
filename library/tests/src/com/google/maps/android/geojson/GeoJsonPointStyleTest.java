package com.google.maps.android.geojson;

import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;

import android.test.AndroidTestCase;

public class GeoJsonPointStyleTest extends AndroidTestCase {

    GeoJsonPointStyle pointStyle;

    public void setUp() throws Exception {
        super.setUp();
        MapsInitializer.initialize(getContext());
        pointStyle = new GeoJsonPointStyle();
    }

    public void testGetGeometryType() throws Exception {
        assertTrue("Point".matches(pointStyle.getGeometryType()));
        assertTrue("MultiPoint".matches(pointStyle.getGeometryType()));
        assertTrue("GeometryCollection".matches(pointStyle.getGeometryType()));
        assertEquals("Point|MultiPoint|GeometryCollection", pointStyle.getGeometryType());
    }

    public void testAlpha() throws Exception {
        pointStyle.setAlpha(0.1234f);
        assertEquals(0.1234f, pointStyle.getAlpha());
        assertEquals(0.1234f, pointStyle.getMarkerOptions().getAlpha());
    }

    public void testAnchor() throws Exception {
        pointStyle.setAnchor(0.23f, 0.87f);
        assertEquals(0.23f, pointStyle.getAnchorU());
        assertEquals(0.87f, pointStyle.getAnchorV());
        assertEquals(0.23f, pointStyle.getMarkerOptions().getAnchorU());
        assertEquals(0.87f, pointStyle.getMarkerOptions().getAnchorV());
    }

    public void testDraggable() throws Exception {
        pointStyle.setDraggable(true);
        assertTrue(pointStyle.isDraggable());
        assertTrue(pointStyle.getMarkerOptions().isDraggable());
    }

    public void testFlat() throws Exception {
        pointStyle.setFlat(true);
        assertTrue(pointStyle.isFlat());
        assertTrue(pointStyle.getMarkerOptions().isFlat());
    }

    public void testIcon() throws Exception {
        BitmapDescriptor icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN);
        pointStyle
                .setIcon(icon);
        assertEquals(icon,
                pointStyle.getIcon());
        assertEquals(icon,
                pointStyle.getMarkerOptions().getIcon());
    }

    public void testInfoWindowAnchor() throws Exception {
        pointStyle.setInfoWindowAnchor(0.12f, 0.98f);
        assertEquals(0.12f, pointStyle.getInfoWindowAnchorU());
        assertEquals(0.98f, pointStyle.getInfoWindowAnchorV());
        assertEquals(0.12f, pointStyle.getMarkerOptions().getInfoWindowAnchorU());
        assertEquals(0.98f, pointStyle.getMarkerOptions().getInfoWindowAnchorV());
    }

    public void testRotation() throws Exception {
        pointStyle.setRotation(156.24f);
        assertEquals(156.24f, pointStyle.getRotation());
        assertEquals(156.24f, pointStyle.getMarkerOptions().getRotation());
    }

    public void testSnippet() throws Exception {
        pointStyle.setSnippet("The peaches are in a jar");
        assertEquals("The peaches are in a jar", pointStyle.getSnippet());
        assertEquals("The peaches are in a jar", pointStyle.getMarkerOptions().getSnippet());
    }

    public void testTitle() throws Exception {
        pointStyle.setTitle("Peaches");
        assertEquals("Peaches", pointStyle.getTitle());
        assertEquals("Peaches", pointStyle.getMarkerOptions().getTitle());
    }

    public void testVisible() throws Exception {
        pointStyle.setVisible(false);
        assertFalse(pointStyle.isVisible());
        assertFalse(pointStyle.getMarkerOptions().isVisible());
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
        assertEquals(1.0f, pointStyle.getMarkerOptions().getAlpha());
        assertEquals(0.5f, pointStyle.getMarkerOptions().getAnchorU());
        assertEquals(1.0f, pointStyle.getMarkerOptions().getAnchorV());
        assertFalse(pointStyle.getMarkerOptions().isDraggable());
        assertFalse(pointStyle.getMarkerOptions().isFlat());
        assertNull(pointStyle.getMarkerOptions().getIcon());
        assertEquals(0.5f, pointStyle.getMarkerOptions().getInfoWindowAnchorU());
        assertEquals(0.0f, pointStyle.getMarkerOptions().getInfoWindowAnchorV());
        assertEquals(0.0f, pointStyle.getMarkerOptions().getRotation());
        assertNull(pointStyle.getMarkerOptions().getSnippet());
        assertNull(pointStyle.getMarkerOptions().getTitle());
        assertTrue(pointStyle.getMarkerOptions().isVisible());
    }

}