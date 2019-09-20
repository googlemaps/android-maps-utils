package com.google.maps.android.data.geojson;

import android.support.test.InstrumentationRegistry;

import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;

import org.junit.Before;
import org.junit.Test;
import org.junit.Assert;

import java.util.Arrays;

public class GeoJsonPointStyleTest {

    GeoJsonPointStyle pointStyle;

    @Before
    public void setUp() throws Exception {
        MapsInitializer.initialize(InstrumentationRegistry.getTargetContext());
        pointStyle = new GeoJsonPointStyle();
    }

    @Test
    public void testGetGeometryType() throws Exception {
        Assert.assertTrue(Arrays.asList(pointStyle.getGeometryType()).contains("Point"));
        Assert.assertTrue(Arrays.asList(pointStyle.getGeometryType()).contains("MultiPoint"));
        Assert.assertTrue(Arrays.asList(pointStyle.getGeometryType()).contains("GeometryCollection"));
        Assert.assertEquals(3, pointStyle.getGeometryType().length);
    }

    @Test
    public void testAlpha() throws Exception {
        pointStyle.setAlpha(0.1234f);
        Assert.assertEquals(0.1234f, pointStyle.getAlpha(), 0);
        Assert.assertEquals(0.1234f, pointStyle.toMarkerOptions().getAlpha(), 0);
    }

    @Test
    public void testAnchor() throws Exception {
        pointStyle.setAnchor(0.23f, 0.87f);
        Assert.assertEquals(0.23f, pointStyle.getAnchorU(), 0);
        Assert.assertEquals(0.87f, pointStyle.getAnchorV(), 0);
        Assert.assertEquals(0.23f, pointStyle.toMarkerOptions().getAnchorU(), 0);
        Assert.assertEquals(0.87f, pointStyle.toMarkerOptions().getAnchorV(), 0);
    }

    @Test
    public void testDraggable() throws Exception {
        pointStyle.setDraggable(true);
        Assert.assertTrue(pointStyle.isDraggable());
        Assert.assertTrue(pointStyle.toMarkerOptions().isDraggable());
    }

    @Test
    public void testFlat() throws Exception {
        pointStyle.setFlat(true);
        Assert.assertTrue(pointStyle.isFlat());
        Assert.assertTrue(pointStyle.toMarkerOptions().isFlat());
    }

    @Test
    public void testIcon() throws Exception {
        BitmapDescriptor icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN);
        pointStyle
                .setIcon(icon);
        Assert.assertEquals(icon,
                pointStyle.getIcon());
        Assert.assertEquals(icon,
                pointStyle.toMarkerOptions().getIcon());
    }

    @Test
    public void testInfoWindowAnchor() throws Exception {
        pointStyle.setInfoWindowAnchor(0.12f, 0.98f);
        Assert.assertEquals(0.12f, pointStyle.getInfoWindowAnchorU(), 0);
        Assert.assertEquals(0.98f, pointStyle.getInfoWindowAnchorV(), 0);
        Assert.assertEquals(0.12f, pointStyle.toMarkerOptions().getInfoWindowAnchorU(), 0);
        Assert.assertEquals(0.98f, pointStyle.toMarkerOptions().getInfoWindowAnchorV(), 0);
    }

    @Test
    public void testRotation() throws Exception {
        pointStyle.setRotation(156.24f);
        Assert.assertEquals(156.24f, pointStyle.getRotation(), 0);
        Assert.assertEquals(156.24f, pointStyle.toMarkerOptions().getRotation(), 0);
    }

    @Test
    public void testSnippet() throws Exception {
        pointStyle.setSnippet("The peaches are in a jar");
        Assert.assertEquals("The peaches are in a jar", pointStyle.getSnippet());
        Assert.assertEquals("The peaches are in a jar", pointStyle.toMarkerOptions().getSnippet());
    }

    @Test
    public void testTitle() throws Exception {
        pointStyle.setTitle("Peaches");
        Assert.assertEquals("Peaches", pointStyle.getTitle());
        Assert.assertEquals("Peaches", pointStyle.toMarkerOptions().getTitle());
    }

    @Test
    public void testVisible() throws Exception {
        pointStyle.setVisible(false);
        Assert.assertFalse(pointStyle.isVisible());
        Assert.assertFalse(pointStyle.toMarkerOptions().isVisible());

    }

    @Test
    public void testDefaultPointStyle() throws Exception {
        Assert.assertEquals(1.0f, pointStyle.getAlpha(), 0);
        Assert.assertEquals(0.5f, pointStyle.getAnchorU(), 0);
        Assert.assertEquals(1.0f, pointStyle.getAnchorV(), 0);
        Assert.assertFalse(pointStyle.isDraggable());
        Assert.assertFalse(pointStyle.isFlat());
        Assert.assertNull(pointStyle.getIcon());
        Assert.assertEquals(0.5f, pointStyle.getInfoWindowAnchorU(), 0);
        Assert.assertEquals(0.0f, pointStyle.getInfoWindowAnchorV(), 0);
        Assert.assertEquals(0.0f, pointStyle.getRotation(), 0);
        Assert.assertNull(pointStyle.getSnippet());
        Assert.assertNull(pointStyle.getTitle());
        Assert.assertTrue(pointStyle.isVisible());
    }

    @Test
    public void testDefaultGetMarkerOptions() throws Exception {
        Assert.assertEquals(1.0f, pointStyle.toMarkerOptions().getAlpha(), 0);
        Assert.assertEquals(0.5f, pointStyle.toMarkerOptions().getAnchorU(), 0);
        Assert.assertEquals(1.0f, pointStyle.toMarkerOptions().getAnchorV(), 0);
        Assert.assertFalse(pointStyle.toMarkerOptions().isDraggable());
        Assert.assertFalse(pointStyle.toMarkerOptions().isFlat());
        Assert.assertNull(pointStyle.toMarkerOptions().getIcon());
        Assert.assertEquals(0.5f, pointStyle.toMarkerOptions().getInfoWindowAnchorU(), 0);
        Assert.assertEquals(0.0f, pointStyle.toMarkerOptions().getInfoWindowAnchorV(), 0);
        Assert.assertEquals(0.0f, pointStyle.toMarkerOptions().getRotation(), 0);
        Assert.assertNull(pointStyle.toMarkerOptions().getSnippet());
        Assert.assertNull(pointStyle.toMarkerOptions().getTitle());
        Assert.assertTrue(pointStyle.toMarkerOptions().isVisible());
    }

}