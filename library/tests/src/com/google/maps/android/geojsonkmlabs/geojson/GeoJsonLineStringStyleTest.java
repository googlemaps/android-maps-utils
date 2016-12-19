package com.google.maps.android.geojsonkmlabs.geojson;

import junit.framework.TestCase;

import android.graphics.Color;

import java.util.Arrays;

public class GeoJsonLineStringStyleTest extends TestCase {

    GeoJsonLineStringStyle lineStringStyle;

    public void setUp() throws Exception {
        super.setUp();
        lineStringStyle = new GeoJsonLineStringStyle();
    }

    public void testGetGeometryType() throws Exception {
        assertTrue(Arrays.asList(lineStringStyle.getGeometryType()).contains("LineString"));
        assertTrue(Arrays.asList(lineStringStyle.getGeometryType()).contains("MultiLineString"));
        assertTrue(Arrays.asList(lineStringStyle.getGeometryType()).contains("GeometryCollection"));
        assertEquals(3, lineStringStyle.getGeometryType().length);
    }

    public void testColor() throws Exception {
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

    public void testGeodesic() throws Exception {
        lineStringStyle.setGeodesic(true);
        assertTrue(lineStringStyle.isGeodesic());
        assertTrue(lineStringStyle.toPolylineOptions().isGeodesic());
    }

    public void testVisible() throws Exception {
        lineStringStyle.setVisible(false);
        assertFalse(lineStringStyle.isVisible());
        assertFalse(lineStringStyle.toPolylineOptions().isVisible());
    }

    public void testWidth() throws Exception {
        lineStringStyle.setWidth(20.2f);
        assertEquals(20.2f, lineStringStyle.getWidth());
        assertEquals(20.2f, lineStringStyle.toPolylineOptions().getWidth());
    }

    public void testZIndex() throws Exception {
        lineStringStyle.setZIndex(50.78f);
        assertEquals(50.78f, lineStringStyle.getZIndex());
        assertEquals(50.78f, lineStringStyle.toPolylineOptions().getZIndex());
    }

    public void testDefaultLineStringStyle() {
        assertEquals(Color.BLACK, lineStringStyle.getColor());
        assertFalse(lineStringStyle.isGeodesic());
        assertTrue(lineStringStyle.isVisible());
        assertEquals(10.0f, lineStringStyle.getWidth());
        assertEquals(0.0f, lineStringStyle.getZIndex());
    }

    public void testDefaultGetPolylineOptions() throws Exception {
        assertEquals(Color.BLACK, lineStringStyle.toPolylineOptions().getColor());
        assertFalse(lineStringStyle.toPolylineOptions().isGeodesic());
        assertTrue(lineStringStyle.toPolylineOptions().isVisible());
        assertEquals(10.0f, lineStringStyle.toPolylineOptions().getWidth());
        assertEquals(0.0f, lineStringStyle.toPolylineOptions().getZIndex());
    }
}