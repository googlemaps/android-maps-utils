package com.google.maps.android.data.kml;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.data.Geometry;

import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;

public class KmlMultiGeometryTest {
    private KmlMultiGeometry createMultiGeometry() {
        ArrayList<Geometry> kmlGeometries = new ArrayList<>();
        ArrayList<LatLng> coordinates = new ArrayList<>();
        coordinates.add(new LatLng(0, 0));
        coordinates.add(new LatLng(50, 50));
        coordinates.add(new LatLng(100, 100));
        Geometry kmlGeometry = new KmlLineString(coordinates);
        kmlGeometries.add(kmlGeometry);
        return new KmlMultiGeometry(kmlGeometries);
    }

    @Test
    public void testGetKmlGeometryType() {
        KmlMultiGeometry kmlMultiGeometry = createMultiGeometry();
        Assert.assertNotNull(kmlMultiGeometry);
        Assert.assertNotNull(kmlMultiGeometry.getGeometryType());
        Assert.assertEquals("MultiGeometry", kmlMultiGeometry.getGeometryType());
    }

    @Test
    public void testGetGeometry() {
        KmlMultiGeometry kmlMultiGeometry = createMultiGeometry();
        Assert.assertNotNull(kmlMultiGeometry);
        Assert.assertEquals(1, kmlMultiGeometry.getGeometryObject().size());
        KmlLineString lineString = ((KmlLineString) kmlMultiGeometry.getGeometryObject().get(0));
        Assert.assertNotNull(lineString);
    }

    @Test
    public void testNullGeometry() {
        try {
            new KmlMultiGeometry(null);
            Assert.fail();
        } catch (IllegalArgumentException e) {
            Assert.assertEquals("Geometries cannot be null", e.getMessage());
        }
    }
}
