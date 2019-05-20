package com.google.maps.android.data.kml;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.data.Geometry;

import junit.framework.TestCase;

import java.util.ArrayList;
import java.util.HashMap;

public class KmlMultiTrackTest extends TestCase {
    KmlMultiTrack kmlMultiTrack;

    public void setUp() throws Exception {
        super.setUp();
    }

    public KmlMultiTrack createMultiTrack() {
        ArrayList<KmlTrack> kmlTracks = new ArrayList<KmlTrack>();
        ArrayList<LatLng> coordinates = new ArrayList<LatLng>();
        ArrayList<Double> altitudes = new ArrayList<Double>();
        ArrayList <Long> timestamps = new ArrayList<Long>();
        HashMap<String, String> properties = new HashMap<String, String>();
        coordinates.add(new LatLng(0, 0));
        coordinates.add(new LatLng(50, 50));
        coordinates.add(new LatLng(90, 90));
        altitudes.add(new Double(100));
        altitudes.add(new Double(200));
        altitudes.add(new Double(300));
        timestamps.add(new Long(1000));
        timestamps.add(new Long(2000));
        timestamps.add(new Long(3000));
        properties.put("key", "value");
        KmlTrack kmlTrack = new KmlTrack(coordinates, altitudes, timestamps, properties);
        kmlTracks.add(kmlTrack);
        return new KmlMultiTrack(kmlTracks);
    }

    public void testGetKmlGeometryType() throws Exception {
        kmlMultiTrack = createMultiTrack();
        assertNotNull(kmlMultiTrack);
        assertNotNull(kmlMultiTrack.getGeometryType());
        assertEquals("MultiGeometry", kmlMultiTrack.getGeometryType());
    }

    public void testGetGeometry() throws Exception {
        kmlMultiTrack = createMultiTrack();
        assertNotNull(kmlMultiTrack);
        assertEquals(kmlMultiTrack.getGeometryObject().size(), 1);
        KmlTrack lineString = ((KmlTrack) kmlMultiTrack.getGeometryObject().get(0));
        assertNotNull(lineString);
    }

    public void testNullGeometry() {
        try {
            kmlMultiTrack = new KmlMultiTrack(null);
            fail();
        } catch (IllegalArgumentException e) {
            assertEquals("Tracks cannot be null", e.getMessage());
        }
    }
}