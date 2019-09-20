package com.google.maps.android.data.geojson;

import org.junit.Before;
import org.junit.Test;
import org.junit.Assert;

import android.graphics.Color;

import java.util.Arrays;

public class GeoJsonPolygonStyleTest {

    GeoJsonPolygonStyle polygonStyle;

    @Before
    public void setUp() throws Exception {
        polygonStyle = new GeoJsonPolygonStyle();
    }

    @Test
    public void testGetGeometryType() throws Exception {
        Assert.assertTrue(Arrays.asList(polygonStyle.getGeometryType()).contains("Polygon"));
        Assert.assertTrue(Arrays.asList(polygonStyle.getGeometryType()).contains("MultiPolygon"));
        Assert.assertTrue(Arrays.asList(polygonStyle.getGeometryType()).contains("GeometryCollection"));
        Assert.assertEquals(3, polygonStyle.getGeometryType().length);
    }

    @Test
    public void testFillColor() throws Exception {
        polygonStyle.setFillColor(Color.BLACK);
        Assert.assertEquals(Color.BLACK, polygonStyle.getFillColor());
        Assert.assertEquals(Color.BLACK, polygonStyle.toPolygonOptions().getFillColor());

        polygonStyle.setFillColor(0xFFFFFF00);
        Assert.assertEquals(0xFFFFFF00, polygonStyle.getFillColor());
        Assert.assertEquals(0xFFFFFF00, polygonStyle.toPolygonOptions().getFillColor());

        polygonStyle.setFillColor(Color.parseColor("#FFFFFF"));
        Assert.assertEquals(Color.parseColor("#FFFFFF"), polygonStyle.getFillColor());
        Assert.assertEquals(Color.parseColor("#FFFFFF"), polygonStyle.toPolygonOptions().getFillColor());
    }

    @Test
    public void testGeodesic() throws Exception {
        polygonStyle.setGeodesic(true);
        Assert.assertTrue(polygonStyle.isGeodesic());
        Assert.assertTrue(polygonStyle.toPolygonOptions().isGeodesic());
    }

    @Test
    public void testStrokeColor() throws Exception {
        polygonStyle.setStrokeColor(Color.RED);
        Assert.assertEquals(Color.RED, polygonStyle.getStrokeColor());
        Assert.assertEquals(Color.RED, polygonStyle.toPolygonOptions().getStrokeColor());

        polygonStyle.setStrokeColor(0x01234567);
        Assert.assertEquals(0x01234567, polygonStyle.getStrokeColor());
        Assert.assertEquals(0x01234567, polygonStyle.toPolygonOptions().getStrokeColor());

        polygonStyle.setStrokeColor(Color.parseColor("#000000"));
        Assert.assertEquals(Color.parseColor("#000000"), polygonStyle.getStrokeColor());
        Assert.assertEquals(Color.parseColor("#000000"),
                polygonStyle.toPolygonOptions().getStrokeColor());
    }

    @Test
    public void testStrokeWidth() throws Exception {
        polygonStyle.setStrokeWidth(20.0f);
        Assert.assertEquals(20.0f, polygonStyle.getStrokeWidth());
        Assert.assertEquals(20.0f, polygonStyle.toPolygonOptions().getStrokeWidth());
    }

    @Test
    public void testVisible() throws Exception {
        polygonStyle.setVisible(false);
        Assert.assertFalse(polygonStyle.isVisible());
        Assert.assertFalse(polygonStyle.toPolygonOptions().isVisible());
    }

    @Test
    public void testZIndex() throws Exception {
        polygonStyle.setZIndex(3.4f);
        Assert.assertEquals(3.4f, polygonStyle.getZIndex());
        Assert.assertEquals(3.4f, polygonStyle.toPolygonOptions().getZIndex());
    }

    @Test
    public void testDefaultPolygonStyle() throws Exception {
        Assert.assertEquals(Color.TRANSPARENT, polygonStyle.getFillColor());
        Assert.assertFalse(polygonStyle.isGeodesic());
        Assert.assertEquals(Color.BLACK, polygonStyle.getStrokeColor());
        Assert.assertEquals(10.0f, polygonStyle.getStrokeWidth());
        Assert.assertTrue(polygonStyle.isVisible());
        Assert.assertEquals(0.0f, polygonStyle.getZIndex());
    }

    @Test
    public void testDefaultGetPolygonOptions() throws Exception {
        Assert.assertEquals(Color.TRANSPARENT, polygonStyle.toPolygonOptions().getFillColor());
        Assert.assertFalse(polygonStyle.toPolygonOptions().isGeodesic());
        Assert.assertEquals(Color.BLACK, polygonStyle.toPolygonOptions().getStrokeColor());
        Assert.assertEquals(10.0f, polygonStyle.toPolygonOptions().getStrokeWidth());
        Assert.assertTrue(polygonStyle.isVisible());
        Assert.assertEquals(0.0f, polygonStyle.toPolygonOptions().getZIndex());
    }
}