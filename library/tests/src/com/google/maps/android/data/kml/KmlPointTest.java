package com.google.maps.android.data.kml;

import com.google.android.gms.maps.model.LatLng;

import junit.framework.TestCase;

public class KmlPointTest extends TestCase {
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

    public void testGetType() throws Exception {
        kmlPoint = createSimplePoint();
        assertNotNull(kmlPoint);
        assertNotNull(kmlPoint.getGeometryType());
        assertEquals("Point", kmlPoint.getGeometryType());
    }

    public void testGetKmlGeometryObject() throws Exception {
        kmlPoint = createSimplePoint();
        assertNotNull(kmlPoint);
        assertNotNull(kmlPoint.getGeometryObject());
        assertEquals(kmlPoint.getGeometryObject().latitude, 0.0);
        assertEquals(kmlPoint.getGeometryObject().longitude, 50.0);
    }

    public void testPointAltitude() throws Exception {
        //test point without altitude
        kmlPoint = createSimplePoint();
        assertNotNull(kmlPoint);
        assertNull(kmlPoint.getAltitude());

        //test point with altitude
        kmlPoint = createSimplePointWithAltitudes();
        assertNotNull(kmlPoint);
        assertNotNull(kmlPoint.getAltitude());
        assertEquals(kmlPoint.getAltitude(), 100.0);
    }
}
