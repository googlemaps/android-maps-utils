package com.google.maps.android.kml;

import com.google.android.gms.maps.model.LatLng;

import junit.framework.TestCase;

public class KmlPointTest extends TestCase {
    KmlPoint p;

    public void setUp() throws Exception {
        super.setUp();

    }

    public void tearDown() throws Exception {

    }

    public void testGetType() throws Exception {
        p = new KmlPoint(new LatLng(0, 0));
        assertEquals("Point", p.getType());
    }

    public void testGetGeometry() throws Exception {

    }
}