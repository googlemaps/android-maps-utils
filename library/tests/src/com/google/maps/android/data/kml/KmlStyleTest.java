package com.google.maps.android.data.kml;

import android.graphics.Color;

import org.junit.Test;

import static org.junit.Assert.*;

public class KmlStyleTest {
    @Test
    public void testStyleId() {
        KmlStyle kmlStyle = new KmlStyle();
        kmlStyle.setStyleId("BlueLine");
        assertEquals("BlueLine", kmlStyle.getStyleId());
    }

    @Test
    public void testFill() {
        KmlStyle kmlStyle = new KmlStyle();
        kmlStyle.setFill(true);
        assertTrue(kmlStyle.hasFill());
        kmlStyle.setFill(false);
        assertFalse(kmlStyle.hasFill());
    }

    @Test
    public void testFillColor() {
        KmlStyle kmlStyle = new KmlStyle();
        assertNotNull(kmlStyle);
        assertNotNull(kmlStyle.getPolygonOptions());
        kmlStyle.setFillColor("000000");
        int fillColor = Color.parseColor("#000000");
        assertEquals(fillColor, kmlStyle.getPolygonOptions().getFillColor());
    }

    @Test
    public void testColorFormatting() {
        KmlStyle kmlStyle = new KmlStyle();
        // AABBGGRR -> AARRGGBB.
        kmlStyle.setFillColor("ff579D00");
        assertEquals(Color.parseColor("#009D57"), kmlStyle.getPolygonOptions().getFillColor());
        // Alpha w/ missing 0.
        kmlStyle.setFillColor(" D579D00");
        assertEquals(Color.parseColor("#0D009D57"), kmlStyle.getPolygonOptions().getFillColor());
    }

    @Test
    public void testHeading() {
        KmlStyle kmlStyle = new KmlStyle();
        assertNotNull(kmlStyle);
        assertNotNull(kmlStyle.getMarkerOptions());
        assertEquals(0.0f, kmlStyle.getMarkerOptions().getRotation(), 0);
        kmlStyle.setHeading(3);
        assertEquals(3.0f, kmlStyle.getMarkerOptions().getRotation(), 0);
    }

    @Test
    public void testWidth() {
        KmlStyle kmlStyle = new KmlStyle();
        assertNotNull(kmlStyle);
        assertNotNull(kmlStyle.getPolygonOptions());
        assertNotNull(kmlStyle.getPolylineOptions());
        assertEquals(10.0f, kmlStyle.getPolylineOptions().getWidth(), 0);
        assertEquals(10.0f, kmlStyle.getPolygonOptions().getStrokeWidth(), 0);
        kmlStyle.setWidth(11.0f);
        assertEquals(11.0f, kmlStyle.getPolylineOptions().getWidth(), 0);
        assertEquals(11.0f, kmlStyle.getPolygonOptions().getStrokeWidth(), 0);
    }

    @Test
    public void testLineColor() {
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

    @Test
    public void testMarkerColor() {
        KmlStyle kmlStyle = new KmlStyle();
        assertNotNull(kmlStyle);
        assertNotNull(kmlStyle.getMarkerOptions());
    }
}
