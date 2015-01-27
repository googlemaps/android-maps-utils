package com.google.maps.android.kml;

import com.google.android.gms.maps.model.LatLng;

import junit.framework.TestCase;

public class KmlPointTest extends TestCase {
    KmlPoint kmlPoint;

    public void setUp() throws Exception {
        super.setUp();

    }

    public void tearDown() throws Exception {

    }

    public KmlPoint createPoint() {
        return new KmlPoint(new LatLng(0, 0));
    }

    public void testGetType() throws Exception {

    }

    public void testGetGeometry() throws Exception {

    }
}