package com.google.maps.android.data.kml;

import com.google.android.gms.maps.model.LatLng;

import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;

import static org.junit.Assert.*;

public class KmlMultiTrackTest {
    private KmlMultiTrack createMultiTrack() {
        ArrayList<KmlTrack> kmlTracks = new ArrayList<>();
        ArrayList<LatLng> coordinates = new ArrayList<>();
        ArrayList<Double> altitudes = new ArrayList<>();
        ArrayList<Long> timestamps = new ArrayList<>();
        HashMap<String, String> properties = new HashMap<>();
        coordinates.add(new LatLng(0, 0));
        coordinates.add(new LatLng(50, 50));
        coordinates.add(new LatLng(90, 90));
        altitudes.add(100d);
        altitudes.add(200d);
        altitudes.add(300d);
        timestamps.add(1000L);
        timestamps.add(2000L);
        timestamps.add(3000L);
        properties.put("key", "value");
        KmlTrack kmlTrack = new KmlTrack(coordinates, altitudes, timestamps, properties);
        kmlTracks.add(kmlTrack);
        return new KmlMultiTrack(kmlTracks);
    }

    @Test
    public void testGetKmlGeometryType() {
        KmlMultiTrack kmlMultiTrack = createMultiTrack();
        assertNotNull(kmlMultiTrack);
        assertNotNull(kmlMultiTrack.getGeometryType());
        assertEquals("MultiGeometry", kmlMultiTrack.getGeometryType());
    }

    @Test
    public void testGetGeometry() {
        KmlMultiTrack kmlMultiTrack = createMultiTrack();
        assertNotNull(kmlMultiTrack);
        assertEquals(1, kmlMultiTrack.getGeometryObject().size());
        KmlTrack lineString = ((KmlTrack) kmlMultiTrack.getGeometryObject().get(0));
        assertNotNull(lineString);
    }

    @Test
    public void testNullGeometry() {
        try {
            new KmlMultiTrack(null);
            fail();
        } catch (IllegalArgumentException e) {
            assertEquals("Tracks cannot be null", e.getMessage());
        }
    }
}
