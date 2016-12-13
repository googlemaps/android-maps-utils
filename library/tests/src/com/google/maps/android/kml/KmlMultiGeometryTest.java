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

    public KmlMultiGeometry createBiggerMultiGeometry() {
        ArrayList<KmlGeometry> kmlGeometries = new ArrayList<KmlGeometry>();
        ArrayList<LatLng> coordinates = new ArrayList<LatLng>();
        coordinates.add(new LatLng(0, 0));
        coordinates.add(new LatLng(50, 50));
        coordinates.add(new LatLng(100, 100));
        KmlGeometry kmlGeometry = new KmlLineString(coordinates);
        kmlGeometries.add(kmlGeometry);

        ArrayList<LatLng> outerCoordinates = new ArrayList<LatLng>();
        outerCoordinates.add(new LatLng(10, 10));
        outerCoordinates.add(new LatLng(10, 20));
        outerCoordinates.add(new LatLng(20, 20));
        outerCoordinates.add(new LatLng(20, 10));
        outerCoordinates.add(new LatLng(10, 10));
        kmlGeometry = new KmlPolygon(outerCoordinates, null);
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

    public void testContainsLocation() throws Exception {
        LatLng insideLine, outsideLine, insidePolygon, outsidePolygon;
        kmlMultiGeometry = createBiggerMultiGeometry();
        insideLine = new LatLng(45, 40);
        outsideLine = new LatLng(21, 23);
        insidePolygon = new LatLng(15, 15);
        outsidePolygon = new LatLng(21, 17);
        assertEquals(kmlMultiGeometry.containsLocation(insideLine, true), true);
        assertEquals(kmlMultiGeometry.containsLocation(outsideLine, true), false);
        assertEquals(kmlMultiGeometry.containsLocation(insidePolygon, true), true);
        assertEquals(kmlMultiGeometry.containsLocation(outsidePolygon, true), false);
    }
}