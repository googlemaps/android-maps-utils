package com.google.maps.android.geoJsonLayer;

import com.google.android.gms.maps.model.LatLng;

import junit.framework.TestCase;

import java.util.ArrayList;
import java.util.Arrays;

public class PolygonTest extends TestCase {
    Polygon p;

    public void testGetType() throws Exception {
        ArrayList<ArrayList<LatLng>> coordinates = new ArrayList<ArrayList<LatLng>>();
        coordinates.add(new ArrayList<LatLng>(Arrays.asList(new LatLng(0,0), new LatLng(20, 20), new LatLng(60, 60), new LatLng(0, 0))));
        p = new Polygon(coordinates);
        assertEquals("Polygon", p.getType());
    }

    public void testGetCoordinates() throws Exception {
        // No holes
        ArrayList<ArrayList<LatLng>> coordinates = new ArrayList<ArrayList<LatLng>>();
        coordinates.add(new ArrayList<LatLng>(Arrays.asList(new LatLng(0,0), new LatLng(20, 20), new LatLng(60, 60), new LatLng(0, 0))));
        p = new Polygon(coordinates);
        assertEquals(coordinates, p.getCoordinates());

        // Holes
        coordinates.add(new ArrayList<LatLng>(Arrays.asList(new LatLng(0,0), new LatLng(20, 20), new LatLng(60, 60), new LatLng(0, 0))));
        p = new Polygon(coordinates);

    }
}