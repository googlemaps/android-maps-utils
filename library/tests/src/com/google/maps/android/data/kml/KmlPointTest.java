package com.google.maps.android.data.kml;

import com.google.android.gms.maps.model.LatLng;

import org.junit.Test;
import org.junit.Assert;

public class KmlPointTest {
    KmlPoint kmlPoint;

    public KmlPoint createSimplePoint() {
        LatLng coordinates = new LatLng(0, 50);
        return new KmlPoint(coordinates);
    }

    public KmlPoint createSimplePointWithAltitudes() {
        LatLng coordinates = new LatLng(0, 50);
        Double altitude = new Double(100);
        return new KmlPoint(coordinates, altitude);
    }

    @Test
    public void testGetType() throws Exception {
        kmlPoint = createSimplePoint();
        Assert.assertNotNull(kmlPoint);
        Assert.assertNotNull(kmlPoint.getGeometryType());
        Assert.assertEquals("Point", kmlPoint.getGeometryType());
    }

    @Test
    public void testGetKmlGeometryObject() throws Exception {
        kmlPoint = createSimplePoint();
        Assert.assertNotNull(kmlPoint);
        Assert.assertNotNull(kmlPoint.getGeometryObject());
        Assert.assertEquals(kmlPoint.getGeometryObject().latitude, 0.0);
        Assert.assertEquals(kmlPoint.getGeometryObject().longitude, 50.0);
    }

    @Test
    public void testPointAltitude() throws Exception {
        //test point without altitude
        kmlPoint = createSimplePoint();
        Assert.assertNotNull(kmlPoint);
        Assert.assertNull(kmlPoint.getAltitude());

        //test point with altitude
        kmlPoint = createSimplePointWithAltitudes();
        Assert.assertNotNull(kmlPoint);
        Assert.assertNotNull(kmlPoint.getAltitude());
        Assert.assertEquals(kmlPoint.getAltitude(), 100.0, 0);
    }
}
