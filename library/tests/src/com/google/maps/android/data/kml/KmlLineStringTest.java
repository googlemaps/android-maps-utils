package com.google.maps.android.data.kml;

import com.google.android.gms.maps.model.LatLng;

import org.junit.Test;
import org.junit.Assert;

import java.util.ArrayList;

public class KmlLineStringTest {
    KmlLineString kmlLineString;

    public KmlLineString createSimpleLineString() {
        ArrayList<LatLng> coordinates = new ArrayList<LatLng>();
        coordinates.add(new LatLng(0, 0));
        coordinates.add(new LatLng(50, 50));
        coordinates.add(new LatLng(100, 100));
        return new KmlLineString(coordinates);
    }

    public KmlLineString createSimpleLineStringWithAltitudes() {
        ArrayList<LatLng> coordinates = new ArrayList<LatLng>();
        ArrayList<Double> altitudes = new ArrayList<Double>();
        coordinates.add(new LatLng(0, 0));
        coordinates.add(new LatLng(50, 50));
        coordinates.add(new LatLng(100, 100));
        altitudes.add(new Double(100));
        altitudes.add(new Double(200));
        altitudes.add(new Double(300));
        return new KmlLineString(coordinates, altitudes);
    }

    public KmlLineString createLoopedLineString() {
        ArrayList<LatLng> coordinates = new ArrayList<LatLng>();
        coordinates.add(new LatLng(0, 0));
        coordinates.add(new LatLng(50, 50));
        coordinates.add(new LatLng(0, 0));
        return new KmlLineString(coordinates);
    }

    @Test
    public void testGetType() throws Exception {
        kmlLineString = createSimpleLineString();
        Assert.assertNotNull(kmlLineString);
        Assert.assertNotNull(kmlLineString.getGeometryType());
        Assert.assertEquals("LineString", kmlLineString.getGeometryType());
        kmlLineString = createLoopedLineString();
        Assert.assertNotNull(kmlLineString);
        Assert.assertNotNull(kmlLineString.getGeometryType());
        Assert.assertEquals("LineString", kmlLineString.getGeometryType());
    }

    @Test
    public void testGetKmlGeometryObject() throws Exception {
        kmlLineString = createSimpleLineString();
        Assert.assertNotNull(kmlLineString);
        Assert.assertNotNull(kmlLineString.getGeometryObject());
        Assert.assertEquals(kmlLineString.getGeometryObject().size(), 3);
        Assert.assertEquals(kmlLineString.getGeometryObject().get(0).latitude, 0.0);
        Assert.assertEquals(kmlLineString.getGeometryObject().get(1).latitude, 50.0);
        Assert.assertEquals(kmlLineString.getGeometryObject().get(2).latitude, 90.0);
        kmlLineString = createLoopedLineString();
        Assert.assertNotNull(kmlLineString);
        Assert.assertNotNull(kmlLineString.getGeometryObject());
        Assert.assertEquals(kmlLineString.getGeometryObject().size(), 3);
        Assert.assertEquals(kmlLineString.getGeometryObject().get(0).latitude, 0.0);
        Assert.assertEquals(kmlLineString.getGeometryObject().get(1).latitude, 50.0);
        Assert.assertEquals(kmlLineString.getGeometryObject().get(2).latitude, 0.0);

    }

    @Test
    public void testLineStringAltitudes() throws Exception {
        //test linestring without altitudes
        kmlLineString = createSimpleLineString();
        Assert.assertNotNull(kmlLineString);
        Assert.assertNull(kmlLineString.getAltitudes());

        //test linestring with altitudes
        kmlLineString = createSimpleLineStringWithAltitudes();
        Assert.assertNotNull(kmlLineString);
        Assert.assertNotNull(kmlLineString.getAltitudes());
        Assert.assertEquals(kmlLineString.getAltitudes().get(0), 100.0, 0);
        Assert.assertEquals(kmlLineString.getAltitudes().get(1), 200.0, 0);
        Assert.assertEquals(kmlLineString.getAltitudes().get(2), 300.0, 0);
    }
}