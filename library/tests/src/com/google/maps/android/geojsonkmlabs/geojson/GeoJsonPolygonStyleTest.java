package com.google.maps.android.geojsonkmlabs.geojson;

import junit.framework.TestCase;

import android.graphics.Color;

import java.util.Arrays;

public class GeoJsonPolygonStyleTest extends TestCase {

    GeoJsonPolygonStyle polygonStyle;

    public void setUp() throws Exception {
        super.setUp();
        polygonStyle = new GeoJsonPolygonStyle();
    }

    public void testGetGeometryType() throws Exception {
        assertTrue(Arrays.asList(polygonStyle.getGeometryType()).contains("Polygon"));
        assertTrue(Arrays.asList(polygonStyle.getGeometryType()).contains("MultiPolygon"));
        assertTrue(Arrays.asList(polygonStyle.getGeometryType()).contains("GeometryCollection"));
        assertEquals(3, polygonStyle.getGeometryType().length);
    }

    public void testFillColor() throws Exception {
        polygonStyle.setFillColor(Color.BLACK);
        assertEquals(Color.BLACK, polygonStyle.getFillColor());
        assertEquals(Color.BLACK, polygonStyle.toPolygonOptions().getFillColor());

        polygonStyle.setFillColor(0xFFFFFF00);
        assertEquals(0xFFFFFF00, polygonStyle.getFillColor());
        assertEquals(0xFFFFFF00, polygonStyle.toPolygonOptions().getFillColor());

        polygonStyle.setFillColor(Color.parseColor("#FFFFFF"));
        assertEquals(Color.parseColor("#FFFFFF"), polygonStyle.getFillColor());
        assertEquals(Color.parseColor("#FFFFFF"), polygonStyle.toPolygonOptions().getFillColor());
    }

    public void testGeodesic() throws Exception {
        polygonStyle.setGeodesic(true);
        assertTrue(polygonStyle.isGeodesic());
        assertTrue(polygonStyle.toPolygonOptions().isGeodesic());
    }

    public void testStrokeColor() throws Exception {
        polygonStyle.setStrokeColor(Color.RED);
        assertEquals(Color.RED, polygonStyle.getStrokeColor());
        assertEquals(Color.RED, polygonStyle.toPolygonOptions().getStrokeColor());

        polygonStyle.setStrokeColor(0x01234567);
        assertEquals(0x01234567, polygonStyle.getStrokeColor());
        assertEquals(0x01234567, polygonStyle.toPolygonOptions().getStrokeColor());

        polygonStyle.setStrokeColor(Color.parseColor("#000000"));
        assertEquals(Color.parseColor("#000000"), polygonStyle.getStrokeColor());
        assertEquals(Color.parseColor("#000000"),
                polygonStyle.toPolygonOptions().getStrokeColor());
    }

    public void testStrokeWidth() throws Exception {
        polygonStyle.setStrokeWidth(20.0f);
        assertEquals(20.0f, polygonStyle.getStrokeWidth());
        assertEquals(20.0f, polygonStyle.toPolygonOptions().getStrokeWidth());
    }

    public void testVisible() throws Exception {
        polygonStyle.setVisible(false);
        assertFalse(polygonStyle.isVisible());
        assertFalse(polygonStyle.toPolygonOptions().isVisible());
    }

    public void testZIndex() throws Exception {
        polygonStyle.setZIndex(3.4f);
        assertEquals(3.4f, polygonStyle.getZIndex());
        assertEquals(3.4f, polygonStyle.toPolygonOptions().getZIndex());
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
        assertEquals(Color.TRANSPARENT, polygonStyle.toPolygonOptions().getFillColor());
        assertFalse(polygonStyle.toPolygonOptions().isGeodesic());
        assertEquals(Color.BLACK, polygonStyle.toPolygonOptions().getStrokeColor());
        assertEquals(10.0f, polygonStyle.toPolygonOptions().getStrokeWidth());
        assertTrue(polygonStyle.isVisible());
        assertEquals(0.0f, polygonStyle.toPolygonOptions().getZIndex());
    }
}