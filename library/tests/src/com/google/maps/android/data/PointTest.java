package com.google.maps.android.data;


import com.google.android.gms.maps.model.LatLng;

import org.junit.Test;
import org.junit.Assert;

public class PointTest {

    Point p;

    @Test
    public void testGetGeometryType() throws Exception {
        p = new Point(new LatLng(0, 50));
        Assert.assertEquals("Point", p.getGeometryType());
    }

    @Test
    public void testGetGeometryObject() throws Exception {
        p = new Point(new LatLng(0, 50));
        Assert.assertEquals(new LatLng(0, 50), p.getGeometryObject());
        try {
            p = new Point(null);
            Assert.fail();
        } catch (IllegalArgumentException e) {
            Assert.assertEquals("Coordinates cannot be null", e.getMessage());
        }
    }

}
