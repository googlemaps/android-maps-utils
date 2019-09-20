package com.google.maps.android.data.geojson;

import org.junit.Before;
import org.junit.Test;
import org.junit.Assert;

import android.graphics.Color;

import java.util.Arrays;

public class GeoJsonLineStringStyleTest {

    GeoJsonLineStringStyle lineStringStyle;

    @Before
    public void setUp() throws Exception {
        lineStringStyle = new GeoJsonLineStringStyle();
    }

    @Test
    public void testGetGeometryType() throws Exception {
        Assert.assertTrue(Arrays.asList(lineStringStyle.getGeometryType()).contains("LineString"));
        Assert.assertTrue(Arrays.asList(lineStringStyle.getGeometryType()).contains("MultiLineString"));
        Assert.assertTrue(Arrays.asList(lineStringStyle.getGeometryType()).contains("GeometryCollection"));
        Assert.assertEquals(3, lineStringStyle.getGeometryType().length);
    }

    @Test
    public void testColor() throws Exception {
        lineStringStyle.setColor(Color.YELLOW);
        Assert.assertEquals(Color.YELLOW, lineStringStyle.getColor());
        Assert.assertEquals(Color.YELLOW, lineStringStyle.toPolylineOptions().getColor());

        lineStringStyle.setColor(0x76543210);
        Assert.assertEquals(0x76543210, lineStringStyle.getColor());
        Assert.assertEquals(0x76543210, lineStringStyle.toPolylineOptions().getColor());

        lineStringStyle.setColor(Color.parseColor("#000000"));
        Assert.assertEquals(Color.parseColor("#000000"), lineStringStyle.getColor());
        Assert.assertEquals(Color.parseColor("#000000"), lineStringStyle.toPolylineOptions().getColor());
    }

    @Test
    public void testGeodesic() throws Exception {
        lineStringStyle.setGeodesic(true);
        Assert.assertTrue(lineStringStyle.isGeodesic());
        Assert.assertTrue(lineStringStyle.toPolylineOptions().isGeodesic());
    }

    @Test
    public void testVisible() throws Exception {
        lineStringStyle.setVisible(false);
        Assert.assertFalse(lineStringStyle.isVisible());
        Assert.assertFalse(lineStringStyle.toPolylineOptions().isVisible());
    }

    @Test
    public void testWidth() throws Exception {
        lineStringStyle.setWidth(20.2f);
        Assert.assertEquals(20.2f, lineStringStyle.getWidth());
        Assert.assertEquals(20.2f, lineStringStyle.toPolylineOptions().getWidth());
    }

    @Test
    public void testZIndex() throws Exception {
        lineStringStyle.setZIndex(50.78f);
        Assert.assertEquals(50.78f, lineStringStyle.getZIndex());
        Assert.assertEquals(50.78f, lineStringStyle.toPolylineOptions().getZIndex());
    }

    @Test
    public void testDefaultLineStringStyle() {
        Assert.assertEquals(Color.BLACK, lineStringStyle.getColor());
        Assert.assertFalse(lineStringStyle.isGeodesic());
        Assert.assertTrue(lineStringStyle.isVisible());
        Assert.assertEquals(10.0f, lineStringStyle.getWidth());
        Assert.assertEquals(0.0f, lineStringStyle.getZIndex());
    }

    @Test
    public void testDefaultGetPolylineOptions() throws Exception {
        Assert.assertEquals(Color.BLACK, lineStringStyle.toPolylineOptions().getColor());
        Assert.assertFalse(lineStringStyle.toPolylineOptions().isGeodesic());
        Assert.assertTrue(lineStringStyle.toPolylineOptions().isVisible());
        Assert.assertEquals(10.0f, lineStringStyle.toPolylineOptions().getWidth());
        Assert.assertEquals(0.0f, lineStringStyle.toPolylineOptions().getZIndex());
    }
}