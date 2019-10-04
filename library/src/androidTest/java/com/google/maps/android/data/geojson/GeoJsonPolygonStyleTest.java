package com.google.maps.android.data.geojson;

import android.graphics.Color;

import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.*;

public class GeoJsonPolygonStyleTest {
    private GeoJsonPolygonStyle polygonStyle;

    @Before
    public void setUp() {
        polygonStyle = new GeoJsonPolygonStyle();
    }

    @Test
    public void testGetGeometryType() {
        assertTrue(Arrays.asList(polygonStyle.getGeometryType()).contains("Polygon"));
        assertTrue(Arrays.asList(polygonStyle.getGeometryType()).contains("MultiPolygon"));
        assertTrue(Arrays.asList(polygonStyle.getGeometryType()).contains("GeometryCollection"));
        assertEquals(3, polygonStyle.getGeometryType().length);
    }

    @Test
    public void testFillColor() {
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

    @Test
    public void testGeodesic() {
        polygonStyle.setGeodesic(true);
        assertTrue(polygonStyle.isGeodesic());
        assertTrue(polygonStyle.toPolygonOptions().isGeodesic());
    }

    @Test
    public void testStrokeColor() {
        polygonStyle.setStrokeColor(Color.RED);
        assertEquals(Color.RED, polygonStyle.getStrokeColor());
        assertEquals(Color.RED, polygonStyle.toPolygonOptions().getStrokeColor());

        polygonStyle.setStrokeColor(0x01234567);
        assertEquals(0x01234567, polygonStyle.getStrokeColor());
        assertEquals(0x01234567, polygonStyle.toPolygonOptions().getStrokeColor());

        polygonStyle.setStrokeColor(Color.parseColor("#000000"));
        assertEquals(Color.parseColor("#000000"), polygonStyle.getStrokeColor());
        assertEquals(Color.parseColor("#000000"), polygonStyle.toPolygonOptions().getStrokeColor());
    }

    @Test
    public void testStrokeWidth() {
        polygonStyle.setStrokeWidth(20.0f);
        assertEquals(20.0f, polygonStyle.getStrokeWidth(), 0);
        assertEquals(20.0f, polygonStyle.toPolygonOptions().getStrokeWidth(), 0);
    }

    @Test
    public void testVisible() {
        polygonStyle.setVisible(false);
        assertFalse(polygonStyle.isVisible());
        assertFalse(polygonStyle.toPolygonOptions().isVisible());
    }

    @Test
    public void testZIndex() {
        polygonStyle.setZIndex(3.4f);
        assertEquals(3.4f, polygonStyle.getZIndex(), 0);
        assertEquals(3.4f, polygonStyle.toPolygonOptions().getZIndex(), 0);
    }

    @Test
    public void testDefaultPolygonStyle() {
        assertEquals(Color.TRANSPARENT, polygonStyle.getFillColor());
        assertFalse(polygonStyle.isGeodesic());
        assertEquals(Color.BLACK, polygonStyle.getStrokeColor());
        assertEquals(10.0f, polygonStyle.getStrokeWidth(), 0);
        assertTrue(polygonStyle.isVisible());
        assertEquals(0.0f, polygonStyle.getZIndex(), 0);
    }

    @Test
    public void testDefaultGetPolygonOptions() {
        assertEquals(Color.TRANSPARENT, polygonStyle.toPolygonOptions().getFillColor());
        assertFalse(polygonStyle.toPolygonOptions().isGeodesic());
        assertEquals(Color.BLACK, polygonStyle.toPolygonOptions().getStrokeColor());
        assertEquals(10.0f, polygonStyle.toPolygonOptions().getStrokeWidth(), 0);
        assertTrue(polygonStyle.isVisible());
        assertEquals(0.0f, polygonStyle.toPolygonOptions().getZIndex(), 0);
    }
}
