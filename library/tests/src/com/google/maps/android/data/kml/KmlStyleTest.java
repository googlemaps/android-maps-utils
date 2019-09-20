package com.google.maps.android.data.kml;

import android.graphics.Color;

import org.junit.Test;
import org.junit.Assert;

public class KmlStyleTest {

    @Test
    public void testStyleId() throws Exception {
        KmlStyle kmlStyle = new KmlStyle();
        kmlStyle.setStyleId("BlueLine");
        Assert.assertEquals("BlueLine", kmlStyle.getStyleId());
    }

    @Test
    public void testFill() throws Exception {
        KmlStyle kmlStyle = new KmlStyle();
        kmlStyle.setFill(true);
        Assert.assertTrue(kmlStyle.hasFill());
        kmlStyle.setFill(false);
        Assert.assertFalse(kmlStyle.hasFill());
    }

    @Test
    public void testFillColor() throws Exception {
        KmlStyle kmlStyle = new KmlStyle();
        Assert.assertNotNull(kmlStyle);
        Assert.assertNotNull(kmlStyle.getPolygonOptions());
        kmlStyle.setFillColor("000000");
        int fillColor = Color.parseColor("#000000");
        Assert.assertEquals(fillColor, kmlStyle.getPolygonOptions().getFillColor());
    }

    @Test
    public void testColorFormatting() throws Exception {
        KmlStyle kmlStyle = new KmlStyle();
        // AABBGGRR -> AARRGGBB.
        kmlStyle.setFillColor("ff579D00");
        Assert.assertEquals(Color.parseColor("#009D57"), kmlStyle.getPolygonOptions().getFillColor());
        // Alpha w/ missing 0.
        kmlStyle.setFillColor(" D579D00");
        Assert.assertEquals(Color.parseColor("#0D009D57"), kmlStyle.getPolygonOptions().getFillColor());
    }

    @Test
    public void testHeading() throws Exception {
        KmlStyle kmlStyle = new KmlStyle();
        Assert.assertNotNull(kmlStyle);
        Assert.assertNotNull(kmlStyle.getMarkerOptions());
        Assert.assertEquals(kmlStyle.getMarkerOptions().getRotation(), 0.0f);
        kmlStyle.setHeading(3);
        Assert.assertEquals(kmlStyle.getMarkerOptions().getRotation(), 3.0f);
    }

    @Test
    public void testWidth() throws Exception {
        KmlStyle kmlStyle = new KmlStyle();
        Assert.assertNotNull(kmlStyle);
        Assert.assertNotNull(kmlStyle.getPolygonOptions());
        Assert.assertNotNull(kmlStyle.getPolylineOptions());
        Assert.assertEquals(kmlStyle.getPolylineOptions().getWidth(), 10.0f, 0);
        Assert.assertEquals(kmlStyle.getPolygonOptions().getStrokeWidth(), 10.0f, 0);
        kmlStyle.setWidth(11.0f);
        Assert.assertEquals(kmlStyle.getPolylineOptions().getWidth(), 11.0f);
        Assert.assertEquals(kmlStyle.getPolygonOptions().getStrokeWidth(), 11.0f);
    }

    @Test
    public void testLineColor() throws Exception {
        KmlStyle kmlStyle = new KmlStyle();
        Assert.assertNotNull(kmlStyle);
        Assert.assertNotNull(kmlStyle.getPolygonOptions());
        Assert.assertNotNull(kmlStyle.getPolylineOptions());
        Assert.assertEquals(Color.BLACK, kmlStyle.getPolylineOptions().getColor());
        Assert.assertEquals(Color.BLACK, kmlStyle.getPolygonOptions().getStrokeColor());
        kmlStyle.setOutlineColor("FFFFFF");
        Assert.assertEquals(Color.WHITE, kmlStyle.getPolylineOptions().getColor());
        Assert.assertEquals(Color.WHITE, kmlStyle.getPolygonOptions().getStrokeColor());
    }

    @Test
    public void testMarkerColor() {
        KmlStyle kmlStyle = new KmlStyle();
        Assert.assertNotNull(kmlStyle);
        Assert.assertNotNull(kmlStyle.getMarkerOptions());
    }

}
