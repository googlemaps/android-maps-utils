package com.google.maps.android.kml;

import com.google.android.gms.maps.model.LatLng;

import junit.framework.TestCase;

import java.util.ArrayList;

public class KmlMultiGeometryTest extends TestCase {
    KmlMultiGeometry kmlMultiGeometry;

    public void setUp() throws Exception {
        super.setUp();
    }

    public KmlMultiGeometry createMultiGeometry() {
        ArrayList<KmlGeometry> kmlGeometries = new ArrayList<KmlGeometry>();
        ArrayList<LatLng> coordinates = new ArrayList<LatLng>();
        coordinates.add(new LatLng(0, 0));
        coordinates.add(new LatLng(50, 50));
        coordinates.add(new LatLng(100, 100));
        KmlGeometry kmlGeometry = new KmlLineString(coordinates);
        kmlGeometries.add(kmlGeometry);
        return new KmlMultiGeometry(kmlGeometries);
    }

    public void testGetKmlGeometryType() throws Exception {
        kmlMultiGeometry = createMultiGeometry();
        assertNotNull(kmlMultiGeometry);
        assertNotNull(kmlMultiGeometry.getGeometryType());
        assertEquals("MultiGeometry", kmlMultiGeometry.getGeometryType());
    }

    public void testGetGeometry() throws Exception {
        kmlMultiGeometry = createMultiGeometry();
        assertNotNull(kmlMultiGeometry);
        assertEquals(kmlMultiGeometry.getGeometryObject().size(), 1);
        KmlLineString lineString = ((KmlLineString) kmlMultiGeometry.getGeometryObject().get(0));
        assertNotNull(lineString);
    }
}