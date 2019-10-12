package com.google.maps.android.data.geojson;

import androidx.test.platform.app.InstrumentationRegistry;

import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;

import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.*;

public class GeoJsonPointStyleTest {
    GeoJsonPointStyle pointStyle;

    @Before
    public void setUp() {
        MapsInitializer.initialize(InstrumentationRegistry.getInstrumentation().getTargetContext());
        pointStyle = new GeoJsonPointStyle();
    }

    @Test
    public void testGetGeometryType() {
        assertTrue(Arrays.asList(pointStyle.getGeometryType()).contains("Point"));
        assertTrue(Arrays.asList(pointStyle.getGeometryType()).contains("MultiPoint"));
        assertTrue(Arrays.asList(pointStyle.getGeometryType()).contains("GeometryCollection"));
        assertEquals(3, pointStyle.getGeometryType().length);
    }

    @Test
    public void testAlpha() {
        pointStyle.setAlpha(0.1234f);
        assertEquals(0.1234f, pointStyle.getAlpha(), 0);
        assertEquals(0.1234f, pointStyle.toMarkerOptions().getAlpha(), 0);
    }

    @Test
    public void testAnchor() {
        pointStyle.setAnchor(0.23f, 0.87f);
        assertEquals(0.23f, pointStyle.getAnchorU(), 0);
        assertEquals(0.87f, pointStyle.getAnchorV(), 0);
        assertEquals(0.23f, pointStyle.toMarkerOptions().getAnchorU(), 0);
        assertEquals(0.87f, pointStyle.toMarkerOptions().getAnchorV(), 0);
    }

    @Test
    public void testDraggable() {
        pointStyle.setDraggable(true);
        assertTrue(pointStyle.isDraggable());
        assertTrue(pointStyle.toMarkerOptions().isDraggable());
    }

    @Test
    public void testFlat() {
        pointStyle.setFlat(true);
        assertTrue(pointStyle.isFlat());
        assertTrue(pointStyle.toMarkerOptions().isFlat());
    }

    @Test
    public void testIcon() {
        BitmapDescriptor icon =
                BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN);
        pointStyle.setIcon(icon);
        assertEquals(icon, pointStyle.getIcon());
        assertEquals(icon, pointStyle.toMarkerOptions().getIcon());
    }

    @Test
    public void testInfoWindowAnchor() {
        pointStyle.setInfoWindowAnchor(0.12f, 0.98f);
        assertEquals(0.12f, pointStyle.getInfoWindowAnchorU(), 0);
        assertEquals(0.98f, pointStyle.getInfoWindowAnchorV(), 0);
        assertEquals(0.12f, pointStyle.toMarkerOptions().getInfoWindowAnchorU(), 0);
        assertEquals(0.98f, pointStyle.toMarkerOptions().getInfoWindowAnchorV(), 0);
    }

    @Test
    public void testRotation() {
        pointStyle.setRotation(156.24f);
        assertEquals(156.24f, pointStyle.getRotation(), 0);
        assertEquals(156.24f, pointStyle.toMarkerOptions().getRotation(), 0);
    }

    @Test
    public void testSnippet() {
        pointStyle.setSnippet("The peaches are in a jar");
        assertEquals("The peaches are in a jar", pointStyle.getSnippet());
        assertEquals("The peaches are in a jar", pointStyle.toMarkerOptions().getSnippet());
    }

    @Test
    public void testTitle() {
        pointStyle.setTitle("Peaches");
        assertEquals("Peaches", pointStyle.getTitle());
        assertEquals("Peaches", pointStyle.toMarkerOptions().getTitle());
    }

    @Test
    public void testVisible() {
        pointStyle.setVisible(false);
        assertFalse(pointStyle.isVisible());
        assertFalse(pointStyle.toMarkerOptions().isVisible());
    }

    @Test
    public void testDefaultPointStyle() {
        assertEquals(1.0f, pointStyle.getAlpha(), 0);
        assertEquals(0.5f, pointStyle.getAnchorU(), 0);
        assertEquals(1.0f, pointStyle.getAnchorV(), 0);
        assertFalse(pointStyle.isDraggable());
        assertFalse(pointStyle.isFlat());
        assertNull(pointStyle.getIcon());
        assertEquals(0.5f, pointStyle.getInfoWindowAnchorU(), 0);
        assertEquals(0.0f, pointStyle.getInfoWindowAnchorV(), 0);
        assertEquals(0.0f, pointStyle.getRotation(), 0);
        assertNull(pointStyle.getSnippet());
        assertNull(pointStyle.getTitle());
        assertTrue(pointStyle.isVisible());
    }

    @Test
    public void testDefaultGetMarkerOptions() {
        assertEquals(1.0f, pointStyle.toMarkerOptions().getAlpha(), 0);
        assertEquals(0.5f, pointStyle.toMarkerOptions().getAnchorU(), 0);
        assertEquals(1.0f, pointStyle.toMarkerOptions().getAnchorV(), 0);
        assertFalse(pointStyle.toMarkerOptions().isDraggable());
        assertFalse(pointStyle.toMarkerOptions().isFlat());
        assertNull(pointStyle.toMarkerOptions().getIcon());
        assertEquals(0.5f, pointStyle.toMarkerOptions().getInfoWindowAnchorU(), 0);
        assertEquals(0.0f, pointStyle.toMarkerOptions().getInfoWindowAnchorV(), 0);
        assertEquals(0.0f, pointStyle.toMarkerOptions().getRotation(), 0);
        assertNull(pointStyle.toMarkerOptions().getSnippet());
        assertNull(pointStyle.toMarkerOptions().getTitle());
        assertTrue(pointStyle.toMarkerOptions().isVisible());
    }
}
