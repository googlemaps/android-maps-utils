package com.google.maps.android.kml;

import com.google.android.gms.maps.model.LatLng;

import junit.framework.TestCase;

import java.util.ArrayList;

public class KmlLineStringTest extends TestCase {
    KmlLineString kmlLineString;
    public void setUp() throws Exception {
        super.setUp();

    }

    public void tearDown() throws Exception {

    }

    public KmlLineString createSimpleLineString() {
        ArrayList<LatLng> coordinates = new ArrayList<LatLng>();
        coordinates.add(new LatLng(0, 0));
        coordinates.add(new LatLng(50, 50));
        coordinates.add(new LatLng(100, 100));
        return new KmlLineString(coordinates);
    }

    public KmlLineString createLoopedLineString() {
        ArrayList<LatLng> coordinates = new ArrayList<LatLng>();
        coordinates.add(new LatLng(0, 0));
        coordinates.add(new LatLng(50, 50));
        coordinates.add(new LatLng(0, 0));
        return new KmlLineString(coordinates);
    }

    public void testGetType() throws Exception {
        kmlLineString = createSimpleLineString();
        assertNotNull(kmlLineString);
        assertNotNull(kmlLineString.getKmlGeometryType());
        assertEquals("LineString", kmlLineString.getKmlGeometryType());
        kmlLineString = createLoopedLineString();
        assertNotNull(kmlLineString);
        assertNotNull(kmlLineString.getKmlGeometryType());
        assertEquals("LineString", kmlLineString.getKmlGeometryType());
    }

    public void testGetKmlGeometryObject() throws Exception {
        kmlLineString = createSimpleLineString();
        assertNotNull(kmlLineString);
        assertNotNull(kmlLineString.getKmlGeometryObject());
        assertEquals(kmlLineString.getKmlGeometryObject().size(), 3);
        assertEquals(kmlLineString.getKmlGeometryObject().get(0).latitude, 0.0);
        assertEquals(kmlLineString.getKmlGeometryObject().get(1).latitude, 50.0);
        assertEquals(kmlLineString.getKmlGeometryObject().get(2).latitude, 90.0);
        kmlLineString = createLoopedLineString();
        assertNotNull(kmlLineString);
        assertNotNull(kmlLineString.getKmlGeometryObject());
        assertEquals(kmlLineString.getKmlGeometryObject().size(), 3);
        assertEquals(kmlLineString.getKmlGeometryObject().get(0).latitude, 0.0);
        assertEquals(kmlLineString.getKmlGeometryObject().get(1).latitude, 50.0);
        assertEquals(kmlLineString.getKmlGeometryObject().get(2).latitude, 0.0);

    }
}