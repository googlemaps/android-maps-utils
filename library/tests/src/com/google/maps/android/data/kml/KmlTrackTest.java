package com.google.maps.android.data.kml;

import com.google.android.gms.maps.model.LatLng;

import org.junit.Test;
import org.junit.Assert;

import java.util.ArrayList;
import java.util.HashMap;

public class KmlTrackTest {
    KmlTrack kmlTrack;

    public KmlTrack createSimpleTrack() {
        ArrayList<LatLng> coordinates = new ArrayList<LatLng>();
        ArrayList<Double> altitudes = new ArrayList<Double>();
        ArrayList<Long> timestamps = new ArrayList<Long>();
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
        return new KmlTrack(coordinates, altitudes, timestamps, properties);
    }

    @Test
    public void testGetType() throws Exception {
        kmlTrack = createSimpleTrack();
        Assert.assertNotNull(kmlTrack);
        Assert.assertNotNull(kmlTrack.getGeometryType());
        Assert.assertEquals("LineString", kmlTrack.getGeometryType());
    }

    @Test
    public void testGetKmlGeometryObject() throws Exception {
        kmlTrack = createSimpleTrack();
        Assert.assertNotNull(kmlTrack);
        Assert.assertNotNull(kmlTrack.getGeometryObject());
        Assert.assertEquals(kmlTrack.getGeometryObject().size(), 3);
        Assert.assertEquals(kmlTrack.getGeometryObject().get(0).latitude, 0.0, 0);
        Assert.assertEquals(kmlTrack.getGeometryObject().get(1).latitude, 50.0, 0);
        Assert.assertEquals(kmlTrack.getGeometryObject().get(2).latitude, 90.0, 0);
    }

    @Test
    public void testAltitudes() throws Exception {
        kmlTrack = createSimpleTrack();
        Assert.assertNotNull(kmlTrack);
        Assert.assertNotNull(kmlTrack.getAltitudes());
        Assert.assertEquals(kmlTrack.getAltitudes().size(), 3);
        Assert.assertEquals(kmlTrack.getAltitudes().get(0), 100.0, 0);
        Assert.assertEquals(kmlTrack.getAltitudes().get(1), 200.0, 0);
        Assert.assertEquals(kmlTrack.getAltitudes().get(2), 300.0, 0);
    }

    @Test
    public void testTimestamps() throws Exception {
        kmlTrack = createSimpleTrack();
        Assert.assertNotNull(kmlTrack);
        Assert.assertNotNull(kmlTrack.getTimestamps());
        Assert.assertEquals(kmlTrack.getTimestamps().size(), 3);
        Assert.assertEquals(kmlTrack.getTimestamps().get(0), Long.valueOf(1000L));
        Assert.assertEquals(kmlTrack.getTimestamps().get(1), Long.valueOf(2000L));
        Assert.assertEquals(kmlTrack.getTimestamps().get(2), Long.valueOf(3000L));
    }

    @Test
    public void testProperties() throws Exception {
        kmlTrack = createSimpleTrack();
        Assert.assertNotNull(kmlTrack);
        Assert.assertNotNull(kmlTrack.getProperties());
        Assert.assertEquals(kmlTrack.getProperties().size(), 1);
        Assert.assertEquals(kmlTrack.getProperties().get("key"), "value");
        Assert.assertNull(kmlTrack.getProperties().get("missingKey"));
    }
}