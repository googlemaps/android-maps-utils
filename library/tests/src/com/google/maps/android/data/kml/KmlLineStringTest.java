package com.google.maps.android.data.kml;

import com.google.android.gms.maps.model.LatLng;

import junit.framework.TestCase;

import java.util.ArrayList;

public class KmlLineStringTest extends TestCase {
    KmlLineString kmlLineString;

    public KmlLineString createSimpleLineString() {
        ArrayList<LatLng> coordinates = new ArrayList<LatLng>();
        coordinates.add(new LatLng(0, 0));
        coordinates.add(new LatLng(50, 50));
        coordinates.add(new LatLng(100, 100));
        return new KmlLineString(coordinates);
    }

    public KmlLineString createSimpleLineStringWithAltitudes() {
        ArrayList<LatLng> coordinates = new ArrayList<LatLng>();
        ArrayList<Double> altitudes = new ArrayList<Double>();
        coordinates.add(new LatLng(0, 0));
        coordinates.add(new LatLng(50, 50));
        coordinates.add(new LatLng(100, 100));
        altitudes.add(new Double(100));
        altitudes.add(new Double(200));
        altitudes.add(new Double(300));
        return new KmlLineString(coordinates, altitudes);
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
        assertNotNull(kmlLineString.getGeometryType());
        assertEquals("LineString", kmlLineString.getGeometryType());
        kmlLineString = createLoopedLineString();
        assertNotNull(kmlLineString);
        assertNotNull(kmlLineString.getGeometryType());
        assertEquals("LineString", kmlLineString.getGeometryType());
    }

    public void testGetKmlGeometryObject() throws Exception {
        kmlLineString = createSimpleLineString();
        assertNotNull(kmlLineString);
        assertNotNull(kmlLineString.getGeometryObject());
        assertEquals(kmlLineString.getGeometryObject().size(), 3);
        assertEquals(kmlLineString.getGeometryObject().get(0).latitude, 0.0);
        assertEquals(kmlLineString.getGeometryObject().get(1).latitude, 50.0);
        assertEquals(kmlLineString.getGeometryObject().get(2).latitude, 90.0);
        kmlLineString = createLoopedLineString();
        assertNotNull(kmlLineString);
        assertNotNull(kmlLineString.getGeometryObject());
        assertEquals(kmlLineString.getGeometryObject().size(), 3);
        assertEquals(kmlLineString.getGeometryObject().get(0).latitude, 0.0);
        assertEquals(kmlLineString.getGeometryObject().get(1).latitude, 50.0);
        assertEquals(kmlLineString.getGeometryObject().get(2).latitude, 0.0);

    }

    public void testLineStringAltitudes() throws Exception {
        //test linestring without altitudes
        kmlLineString = createSimpleLineString();
        assertNotNull(kmlLineString);
        assertNull(kmlLineString.getAltitudes());

        //test linestring with altitudes
        kmlLineString = createSimpleLineStringWithAltitudes();
        assertNotNull(kmlLineString);
        assertNotNull(kmlLineString.getAltitudes());
        assertEquals(kmlLineString.getAltitudes().get(0), 100.0);
        assertEquals(kmlLineString.getAltitudes().get(1), 200.0);
        assertEquals(kmlLineString.getAltitudes().get(2), 300.0);
    }
}