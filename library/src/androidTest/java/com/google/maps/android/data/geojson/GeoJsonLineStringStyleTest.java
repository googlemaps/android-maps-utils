package com.google.maps.android.data.geojson;

import android.graphics.Color;

import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.*;

public class GeoJsonLineStringStyleTest {
    private GeoJsonLineStringStyle lineStringStyle;

    @Before
    public void setUp() {
        lineStringStyle = new GeoJsonLineStringStyle();
    }

    @Test
    public void testGetGeometryType() {
        assertTrue(Arrays.asList(lineStringStyle.getGeometryType()).contains("LineString"));
        assertTrue(Arrays.asList(lineStringStyle.getGeometryType()).contains("MultiLineString"));
        assertTrue(Arrays.asList(lineStringStyle.getGeometryType()).contains("GeometryCollection"));
        assertEquals(3, lineStringStyle.getGeometryType().length);
    }

    @Test
    public void testColor() {
        lineStringStyle.setColor(Color.YELLOW);
        assertEquals(Color.YELLOW, lineStringStyle.getColor());
        assertEquals(Color.YELLOW, lineStringStyle.toPolylineOptions().getColor());

        lineStringStyle.setColor(0x76543210);
        assertEquals(0x76543210, lineStringStyle.getColor());
        assertEquals(0x76543210, lineStringStyle.toPolylineOptions().getColor());

        lineStringStyle.setColor(Color.parseColor("#000000"));
        assertEquals(Color.parseColor("#000000"), lineStringStyle.getColor());
        assertEquals(Color.parseColor("#000000"), lineStringStyle.toPolylineOptions().getColor());
    }

    @Test
    public void testGeodesic() {
        lineStringStyle.setGeodesic(true);
        assertTrue(lineStringStyle.isGeodesic());
        assertTrue(lineStringStyle.toPolylineOptions().isGeodesic());
    }

    @Test
    public void testVisible() {
        lineStringStyle.setVisible(false);
        assertFalse(lineStringStyle.isVisible());
        assertFalse(lineStringStyle.toPolylineOptions().isVisible());
    }

    @Test
    public void testWidth() {
        lineStringStyle.setWidth(20.2f);
        assertEquals(20.2f, lineStringStyle.getWidth(), 0);
        assertEquals(20.2f, lineStringStyle.toPolylineOptions().getWidth(), 0);
    }

    @Test
    public void testZIndex() {
        lineStringStyle.setZIndex(50.78f);
        assertEquals(50.78f, lineStringStyle.getZIndex(), 0);
        assertEquals(50.78f, lineStringStyle.toPolylineOptions().getZIndex(), 0);
    }

    @Test
    public void testDefaultLineStringStyle() {
        assertEquals(Color.BLACK, lineStringStyle.getColor());
        assertFalse(lineStringStyle.isGeodesic());
        assertTrue(lineStringStyle.isVisible());
        assertEquals(10.0f, lineStringStyle.getWidth(), 0);
        assertEquals(0.0f, lineStringStyle.getZIndex(), 0);
    }

    @Test
    public void testDefaultGetPolylineOptions() {
        assertEquals(Color.BLACK, lineStringStyle.toPolylineOptions().getColor());
        assertFalse(lineStringStyle.toPolylineOptions().isGeodesic());
        assertTrue(lineStringStyle.toPolylineOptions().isVisible());
        assertEquals(10.0f, lineStringStyle.toPolylineOptions().getWidth(), 0);
        assertEquals(0.0f, lineStringStyle.toPolylineOptions().getZIndex(), 0);
    }
}
