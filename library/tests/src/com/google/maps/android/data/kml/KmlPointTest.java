package com.google.maps.android.data.kml;

import com.google.android.gms.maps.model.LatLng;

import org.junit.Test;

import static org.junit.Assert.*;

public class KmlPointTest {
    private KmlPoint createSimplePoint() {
        LatLng coordinates = new LatLng(0, 50);
        return new KmlPoint(coordinates);
    }

    private KmlPoint createSimplePointWithAltitudes() {
        LatLng coordinates = new LatLng(0, 50);
        Double altitude = 100d;
        return new KmlPoint(coordinates, altitude);
    }

    @Test
    public void testGetType() {
        KmlPoint kmlPoint = createSimplePoint();
        assertNotNull(kmlPoint);
        assertNotNull(kmlPoint.getGeometryType());
        assertEquals("Point", kmlPoint.getGeometryType());
    }

    @Test
    public void testGetKmlGeometryObject() {
        KmlPoint kmlPoint = createSimplePoint();
        assertNotNull(kmlPoint);
        assertNotNull(kmlPoint.getGeometryObject());
        assertEquals(0.0, kmlPoint.getGeometryObject().latitude, 0);
        assertEquals(50.0, kmlPoint.getGeometryObject().longitude, 0);
    }

    @Test
    public void testPointAltitude() {
        // test point without altitude
        KmlPoint kmlPoint = createSimplePoint();
        assertNotNull(kmlPoint);
        assertNull(kmlPoint.getAltitude());

        // test point with altitude
        kmlPoint = createSimplePointWithAltitudes();
        assertNotNull(kmlPoint);
        assertNotNull(kmlPoint.getAltitude());
        assertEquals(100.0, kmlPoint.getAltitude(), 0);
    }
}
