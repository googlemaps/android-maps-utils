package com.google.maps.android.data.kml;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.data.Geometry;

import org.junit.Before;
import org.junit.Test;
import org.junit.Assert;

import java.util.ArrayList;

public class KmlMultiGeometryTest {
    KmlMultiGeometry kmlMultiGeometry;

    @Before
    public void setUp() throws Exception {

    }

    public KmlMultiGeometry createMultiGeometry() {
        ArrayList<Geometry> kmlGeometries = new ArrayList<Geometry>();
        ArrayList<LatLng> coordinates = new ArrayList<LatLng>();
        coordinates.add(new LatLng(0, 0));
        coordinates.add(new LatLng(50, 50));
        coordinates.add(new LatLng(100, 100));
        Geometry kmlGeometry = new KmlLineString(coordinates);
        kmlGeometries.add(kmlGeometry);
        return new KmlMultiGeometry(kmlGeometries);
    }

    @Test
    public void testGetKmlGeometryType() throws Exception {
        kmlMultiGeometry = createMultiGeometry();
        Assert.assertNotNull(kmlMultiGeometry);
        Assert.assertNotNull(kmlMultiGeometry.getGeometryType());
        Assert.assertEquals("MultiGeometry", kmlMultiGeometry.getGeometryType());
    }

    @Test
    public void testGetGeometry() throws Exception {
        kmlMultiGeometry = createMultiGeometry();
        Assert.assertNotNull(kmlMultiGeometry);
        Assert.assertEquals(kmlMultiGeometry.getGeometryObject().size(), 1);
        KmlLineString lineString = ((KmlLineString) kmlMultiGeometry.getGeometryObject().get(0));
        Assert.assertNotNull(lineString);
    }

    @Test
    public void testNullGeometry() {
        try {
            kmlMultiGeometry = new KmlMultiGeometry(null);
            Assert.fail();
        } catch (IllegalArgumentException e) {
            Assert.assertEquals("Geometries cannot be null", e.getMessage());
        }
    }
}