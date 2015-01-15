package com.google.maps.android.geoJsonLayer;

import junit.framework.TestCase;

import android.graphics.Color;

public class GeoJsonPolygonStyleTest extends TestCase {

    GeoJsonPolygonStyle polygonStyle;

    public void setUp() throws Exception {
        super.setUp();
        polygonStyle = new GeoJsonPolygonStyle();
    }

    public void testGetGeometryType() throws Exception {
        assertTrue("Polygon".matches(polygonStyle.getGeometryType()));
        assertTrue("MultiPolygon".matches(polygonStyle.getGeometryType()));
        assertTrue("GeometryCollection".matches(polygonStyle.getGeometryType()));
        assertEquals("Polygon|MultiPolygon|GeometryCollection", polygonStyle.getGeometryType());
    }

    public void testFillColor() throws Exception {
        polygonStyle.setFillColor(Color.BLACK);
        assertEquals(Color.BLACK, polygonStyle.getFillColor());
        assertEquals(Color.BLACK, polygonStyle.getPolygonOptions().getFillColor());

        polygonStyle.setFillColor(0xFFFFFF00);
        assertEquals(0xFFFFFF00, polygonStyle.getFillColor());
        assertEquals(0xFFFFFF00, polygonStyle.getPolygonOptions().getFillColor());

        polygonStyle.setFillColor(Color.parseColor("#FFFFFF"));
        assertEquals(Color.parseColor("#FFFFFF"), polygonStyle.getFillColor());
        assertEquals(Color.parseColor("#FFFFFF"), polygonStyle.getPolygonOptions().getFillColor());
    }

    public void testGeodesic() throws Exception {
        polygonStyle.setGeodesic(true);
        assertTrue(polygonStyle.isGeodesic());
        assertTrue(polygonStyle.getPolygonOptions().isGeodesic());
    }

    public void testStrokeColor() throws Exception {
        polygonStyle.setStrokeColor(Color.RED);
        assertEquals(Color.RED, polygonStyle.getStrokeColor());
        assertEquals(Color.RED, polygonStyle.getPolygonOptions().getStrokeColor());

        polygonStyle.setStrokeColor(0x01234567);
        assertEquals(0x01234567, polygonStyle.getStrokeColor());
        assertEquals(0x01234567, polygonStyle.getPolygonOptions().getStrokeColor());

        polygonStyle.setStrokeColor(Color.parseColor("#000000"));
        assertEquals(Color.parseColor("#000000"), polygonStyle.getStrokeColor());
        assertEquals(Color.parseColor("#000000"), polygonStyle.getPolygonOptions().getStrokeColor());
    }

    public void testStrokeWidth() throws Exception {
        polygonStyle.setStrokeWidth(20.0f);
        assertEquals(20.0f, polygonStyle.getStrokeWidth());
        assertEquals(20.0f, polygonStyle.getPolygonOptions().getStrokeWidth());
    }

    public void testVisible() throws Exception {
        polygonStyle.setVisible(false);
        assertFalse(polygonStyle.isVisible());
        assertFalse(polygonStyle.getPolygonOptions().isVisible());
    }

    public void testZIndex() throws Exception {
        polygonStyle.setZIndex(3.4f);
        assertEquals(3.4f, polygonStyle.getZIndex());
        assertEquals(3.4f, polygonStyle.getPolygonOptions().getZIndex());
    }

    public void testDefaultPolygonStyle() throws Exception {
        assertEquals(Color.TRANSPARENT, polygonStyle.getFillColor());
        assertFalse(polygonStyle.isGeodesic());
        assertEquals(Color.BLACK, polygonStyle.getStrokeColor());
        assertEquals(10.0f, polygonStyle.getStrokeWidth());
        assertTrue(polygonStyle.isVisible());
        assertEquals(0.0f, polygonStyle.getZIndex());
    }

    public void testDefaultGetPolygonOptions() throws Exception {
        assertEquals(Color.TRANSPARENT, polygonStyle.getPolygonOptions().getFillColor());
        assertFalse(polygonStyle.getPolygonOptions().isGeodesic());
        assertEquals(Color.BLACK, polygonStyle.getPolygonOptions().getStrokeColor());
        assertEquals(10.0f, polygonStyle.getPolygonOptions().getStrokeWidth());
        assertTrue(polygonStyle.isVisible());
        assertEquals(0.0f, polygonStyle.getPolygonOptions().getZIndex());
    }
}