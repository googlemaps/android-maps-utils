package com.google.maps.android.data.kml;

import com.google.android.gms.maps.model.LatLng;

import org.junit.Before;
import org.junit.Test;
import org.junit.Assert;

import java.util.ArrayList;
import java.util.List;

public class KmlPolygonTest {

    KmlPolygon kmlPolygon;

    @Before
    public void setUp() throws Exception {

    }

    public KmlPolygon createRegularPolygon() {
        ArrayList<LatLng> outerCoordinates = new ArrayList<LatLng>();
        outerCoordinates.add(new LatLng(10, 10));
        outerCoordinates.add(new LatLng(20, 20));
        outerCoordinates.add(new LatLng(30, 30));
        outerCoordinates.add(new LatLng(10, 10));
        ArrayList<List<LatLng>> innerCoordinates = new ArrayList<List<LatLng>>();
        ArrayList<LatLng> innerHole = new ArrayList<LatLng>();
        innerHole.add(new LatLng(20, 20));
        innerHole.add(new LatLng(10, 10));
        innerHole.add(new LatLng(20, 20));
        innerCoordinates.add(innerHole);
        return new KmlPolygon(outerCoordinates, innerCoordinates);
    }

    public KmlPolygon createOuterPolygon() {
        ArrayList<LatLng> outerCoordinates = new ArrayList<LatLng>();
        outerCoordinates.add(new LatLng(10, 10));
        outerCoordinates.add(new LatLng(20, 20));
        outerCoordinates.add(new LatLng(30, 30));
        outerCoordinates.add(new LatLng(10, 10));
        return new KmlPolygon(outerCoordinates, null);
    }

    @Test
    public void testGetType() throws Exception {
        kmlPolygon = createRegularPolygon();
        Assert.assertNotNull(kmlPolygon);
        Assert.assertNotNull(kmlPolygon.getGeometryType());
        Assert.assertEquals("Polygon", kmlPolygon.getGeometryType());
    }

    @Test
    public void testGetOuterBoundaryCoordinates() throws Exception {
        kmlPolygon = createRegularPolygon();
        Assert.assertNotNull(kmlPolygon);
        Assert.assertNotNull(kmlPolygon.getOuterBoundaryCoordinates());
        kmlPolygon = createOuterPolygon();
        Assert.assertNotNull(kmlPolygon);
        Assert.assertNotNull(kmlPolygon.getOuterBoundaryCoordinates());
    }

    @Test
    public void testGetInnerBoundaryCoordinates() throws Exception {
        kmlPolygon = createRegularPolygon();
        Assert.assertNotNull(kmlPolygon);
        Assert.assertNotNull(kmlPolygon.getInnerBoundaryCoordinates());
        kmlPolygon = createOuterPolygon();
        Assert.assertNotNull(kmlPolygon);
        Assert.assertNull(kmlPolygon.getInnerBoundaryCoordinates());
    }

    @Test
    public void testGetKmlGeometryObject() throws Exception {
        kmlPolygon = createRegularPolygon();
        Assert.assertNotNull(kmlPolygon);
        Assert.assertNotNull(kmlPolygon.getGeometryObject());
        Assert.assertEquals(kmlPolygon.getGeometryObject().size(), 2);
        kmlPolygon = createOuterPolygon();
        Assert.assertNotNull(kmlPolygon);
        Assert.assertNotNull(kmlPolygon.getGeometryObject());
        Assert.assertEquals(kmlPolygon.getGeometryObject().size(), 1);
    }
}