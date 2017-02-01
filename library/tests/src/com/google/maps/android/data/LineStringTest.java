package com.google.maps.android.data;

import com.google.android.gms.maps.model.LatLng;

import junit.framework.TestCase;

import java.util.ArrayList;

public class LineStringTest extends TestCase {
    LineString lineString;

    public LineString createSimpleLineString() {
        ArrayList<LatLng> coordinates = new ArrayList<LatLng>();
        coordinates.add(new LatLng(95, 60));
        coordinates.add(new LatLng(93, 57));
        coordinates.add(new LatLng(95, 55));
        coordinates.add(new LatLng(95, 53));
        coordinates.add(new LatLng(91, 54));
        coordinates.add(new LatLng(86, 56));
        return new LineString(coordinates);
    }

    public LineString createLoopedLineString() {
        ArrayList<LatLng> coordinates = new ArrayList<LatLng>();
        coordinates.add(new LatLng(92, 66));
        coordinates.add(new LatLng(89, 64));
        coordinates.add(new LatLng(94, 62));
        coordinates.add(new LatLng(92, 66));
        return new LineString(coordinates);
    }


    public void testGetType() throws Exception {
        lineString = createSimpleLineString();
        assertNotNull(lineString);
        assertNotNull(lineString.getGeometryType());
        assertEquals("LineString", lineString.getGeometryType());
        lineString = createLoopedLineString();
        assertNotNull(lineString);
        assertNotNull(lineString.getGeometryType());
        assertEquals("LineString", lineString.getGeometryType());
    }

    public void testGetGeometryObject() throws Exception {
        lineString = createSimpleLineString();
        assertNotNull(lineString);
        assertNotNull(lineString.getGeometryObject());
        assertEquals(lineString.getGeometryObject().size(), 6);
        assertEquals(lineString.getGeometryObject().get(0).latitude, 90.0);
        assertEquals(lineString.getGeometryObject().get(1).latitude, 90.0);
        assertEquals(lineString.getGeometryObject().get(2).latitude, 90.0);
        assertEquals(lineString.getGeometryObject().get(3).longitude, 53.0);
        assertEquals(lineString.getGeometryObject().get(4).longitude, 54.0);
        lineString = createLoopedLineString();
        assertNotNull(lineString);
        assertNotNull(lineString.getGeometryObject());
        assertEquals(lineString.getGeometryObject().size(), 4);
        assertEquals(lineString.getGeometryObject().get(0).latitude, 90.0);
        assertEquals(lineString.getGeometryObject().get(1).latitude, 89.0);
        assertEquals(lineString.getGeometryObject().get(2).longitude, 62.0);
        assertEquals(lineString.getGeometryObject().get(3).longitude, 66.0);

    }

}
