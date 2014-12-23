package com.google.maps.android.geoJsonImport;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;

import junit.framework.TestCase;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by juliawong on 12/9/14.
 */
public class PolylinePropertiesTest extends TestCase {

    public void testDefaultProperties() {
        PolylineProperties pp = null;
        PolylineOptions po = null;
        ArrayList<LatLng> coords;
        JSONObject properties;

        try {
            properties = new JSONObject("{}");
            coords = new ArrayList<LatLng>(
                    Arrays.asList(new LatLng(0, 0), new LatLng(10, 10), new LatLng(50, 50)));
            pp = new PolylineProperties(properties, coords);
            po = pp.getPolylineOptions();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        assertNotNull(po);
        assertEquals(Arrays.asList(new LatLng(0, 0), new LatLng(10, 10), new LatLng(50, 50)),
                po.getPoints());
        assertEquals((float) 10, po.getWidth());
        assertEquals(0xff000000, po.getColor());
        assertEquals((float) 0, po.getZIndex());
        assertEquals(true, po.isVisible());
        assertEquals(false, po.isGeodesic());


    }
}
