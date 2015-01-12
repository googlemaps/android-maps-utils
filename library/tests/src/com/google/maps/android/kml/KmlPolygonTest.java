package com.google.maps.android.kml;

import com.google.android.gms.maps.model.LatLng;

import junit.framework.TestCase;

import java.util.ArrayList;
import java.util.Arrays;

public class KmlPolygonTest extends TestCase {
    KmlPolygon p;

    public void setUp() throws Exception {
        super.setUp();

    }

    public void tearDown() throws Exception {

    }

    public void testGetType() throws Exception {
        ArrayList<LatLng> outerCoordinates = new ArrayList<LatLng>(
                Arrays.asList(new LatLng(0, 0), new LatLng(20, 20), new LatLng(60, 60),
                        new LatLng(0, 0)));
        ArrayList<ArrayList<LatLng>> innerCoordinates = new ArrayList<ArrayList<LatLng>>();
        innerCoordinates.add(new ArrayList<LatLng>(Arrays.asList(new LatLng(0,0), new LatLng(20, 20), new LatLng(60, 60), new LatLng(0, 0))));
        p = new KmlPolygon(outerCoordinates, innerCoordinates);
        assertEquals("Polygon", p.getType());
    }

    public void testGetOuterBoundaryCoordinates() throws Exception {

    }

    public void testGetInnerBoundaryCoordinates() throws Exception {

    }

    public void testGetGeometry() throws Exception {

    }
}