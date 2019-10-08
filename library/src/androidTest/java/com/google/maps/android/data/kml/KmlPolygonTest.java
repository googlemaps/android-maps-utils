package com.google.maps.android.data.kml;

import com.google.android.gms.maps.model.LatLng;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class KmlPolygonTest {
    private KmlPolygon createRegularPolygon() {
        List<LatLng> outerCoordinates = new ArrayList<>();
        outerCoordinates.add(new LatLng(10, 10));
        outerCoordinates.add(new LatLng(20, 20));
        outerCoordinates.add(new LatLng(30, 30));
        outerCoordinates.add(new LatLng(10, 10));
        List<List<LatLng>> innerCoordinates = new ArrayList<>();
        List<LatLng> innerHole = new ArrayList<>();
        innerHole.add(new LatLng(20, 20));
        innerHole.add(new LatLng(10, 10));
        innerHole.add(new LatLng(20, 20));
        innerCoordinates.add(innerHole);
        return new KmlPolygon(outerCoordinates, innerCoordinates);
    }

    private KmlPolygon createOuterPolygon() {
        ArrayList<LatLng> outerCoordinates = new ArrayList<>();
        outerCoordinates.add(new LatLng(10, 10));
        outerCoordinates.add(new LatLng(20, 20));
        outerCoordinates.add(new LatLng(30, 30));
        outerCoordinates.add(new LatLng(10, 10));
        return new KmlPolygon(outerCoordinates, null);
    }

    @Test
    public void testGetType() {
        KmlPolygon kmlPolygon = createRegularPolygon();
        assertNotNull(kmlPolygon);
        assertNotNull(kmlPolygon.getGeometryType());
        assertEquals("Polygon", kmlPolygon.getGeometryType());
    }

    @Test
    public void testGetOuterBoundaryCoordinates() {
        KmlPolygon kmlPolygon = createRegularPolygon();
        assertNotNull(kmlPolygon);
        assertNotNull(kmlPolygon.getOuterBoundaryCoordinates());
        kmlPolygon = createOuterPolygon();
        assertNotNull(kmlPolygon);
        assertNotNull(kmlPolygon.getOuterBoundaryCoordinates());
    }

    @Test
    public void testGetInnerBoundaryCoordinates() {
        KmlPolygon kmlPolygon = createRegularPolygon();
        assertNotNull(kmlPolygon);
        assertNotNull(kmlPolygon.getInnerBoundaryCoordinates());
        kmlPolygon = createOuterPolygon();
        assertNotNull(kmlPolygon);
        assertNull(kmlPolygon.getInnerBoundaryCoordinates());
    }

    @Test
    public void testGetKmlGeometryObject() {
        KmlPolygon kmlPolygon = createRegularPolygon();
        assertNotNull(kmlPolygon);
        assertNotNull(kmlPolygon.getGeometryObject());
        assertEquals(2, kmlPolygon.getGeometryObject().size());
        kmlPolygon = createOuterPolygon();
        assertNotNull(kmlPolygon);
        assertNotNull(kmlPolygon.getGeometryObject());
        assertEquals(1, kmlPolygon.getGeometryObject().size());
    }
}
