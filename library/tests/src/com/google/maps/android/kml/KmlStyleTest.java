package com.google.maps.android.kml;

import android.graphics.Color;

import junit.framework.TestCase;

public class KmlStyleTest extends TestCase {

    public void testStyleId() throws Exception {
        KmlStyle kmlStyle = new KmlStyle();
        kmlStyle.setStyleId("BlueLine");
        assertEquals("BlueLine", kmlStyle.getStyleId());
    }

    public void testFill() throws Exception {
        KmlStyle kmlStyle = new KmlStyle();
        kmlStyle.setFill(true);
        assertTrue(kmlStyle.hasFill());
        kmlStyle.setFill(false);
        assertFalse(kmlStyle.hasFill());
    }

    public void testFillColor() throws Exception {
        KmlStyle kmlStyle = new KmlStyle();
        assertNotNull(kmlStyle);
        assertNotNull(kmlStyle.getPolygonOptions());
        kmlStyle.setFillColor("000000");
        int fillColor = Color.parseColor("#000000");
        assertEquals(fillColor, kmlStyle.getPolygonOptions().getFillColor());
    }

    public void testHeading() throws Exception {
        KmlStyle kmlStyle = new KmlStyle();
        assertNotNull(kmlStyle);
        assertNotNull(kmlStyle.getMarkerOptions());
        assertEquals(kmlStyle.getMarkerOptions().getRotation(), 0.0f);
        kmlStyle.setHeading(3);
        assertEquals(kmlStyle.getMarkerOptions().getRotation(), 3.0f);
    }

    public void testWidth() throws Exception {
        KmlStyle kmlStyle = new KmlStyle();
        assertNotNull(kmlStyle);
        assertNotNull(kmlStyle.getPolygonOptions());
        assertNotNull(kmlStyle.getPolylineOptions());
        assertEquals(kmlStyle.getPolylineOptions().getWidth(), 10.0f);
        assertEquals(kmlStyle.getPolygonOptions().getStrokeWidth(), 10.0f);
        kmlStyle.setWidth(11.0f);
        assertEquals(kmlStyle.getPolylineOptions().getWidth(), 11.0f);
        assertEquals(kmlStyle.getPolygonOptions().getStrokeWidth(), 11.0f);
    }

    public void testLineColor() throws Exception {
        KmlStyle kmlStyle = new KmlStyle();
        assertNotNull(kmlStyle);
        assertNotNull(kmlStyle.getPolygonOptions());
        assertNotNull(kmlStyle.getPolylineOptions());
        assertEquals(Color.BLACK, kmlStyle.getPolylineOptions().getColor());
        assertEquals(Color.BLACK, kmlStyle.getPolygonOptions().getStrokeColor());
        kmlStyle.setOutlineColor("FFFFFF");
        assertEquals(Color.WHITE, kmlStyle.getPolylineOptions().getColor());
        assertEquals(Color.WHITE, kmlStyle.getPolygonOptions().getStrokeColor());
    }

    public void testMarkerColor() {
        KmlStyle kmlStyle = new KmlStyle();
        assertNotNull(kmlStyle);
        assertNotNull(kmlStyle.getMarkerOptions());
    }








}
