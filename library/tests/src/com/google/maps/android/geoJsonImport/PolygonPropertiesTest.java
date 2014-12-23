package com.google.maps.android.geoJsonImport;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolygonOptions;

import junit.framework.TestCase;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by juliawong on 12/9/14.
 */
public class PolygonPropertiesTest extends TestCase {

    public void testDefaultProperties() {
        PolygonProperties pp = null;
        PolygonOptions po = null;
        ArrayList<ArrayList<LatLng>> coords;
        JSONObject properties;

        try {
            properties = new JSONObject("{}");
            coords = new ArrayList<ArrayList<LatLng>>();
            coords.add(new ArrayList<LatLng>(
                    Arrays.asList(new LatLng(0, 0), new LatLng(20, 20), new LatLng(30, 50),
                            new LatLng(0, 0))));
            pp = new PolygonProperties(properties, coords);
            po = pp.getPolygonOptions();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        assertNotNull(po);
        assertTrue(pp.getVisibility());
        assertEquals(Arrays.asList(new LatLng(0, 0), new LatLng(20, 20), new LatLng(30, 50),
                new LatLng(0, 0)), po.getPoints());
        assertEquals(Arrays.asList(), po.getHoles());
        assertEquals((float) 10, po.getStrokeWidth());
        assertEquals(0xff000000, po.getStrokeColor());
        assertEquals(0x00000000, po.getFillColor());
        assertEquals((float) 0, po.getZIndex());
        assertEquals(true, po.isVisible());
        assertEquals(false, po.isGeodesic());
    }
}
