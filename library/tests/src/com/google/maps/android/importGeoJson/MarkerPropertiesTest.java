package com.google.maps.android.importGeoJson;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import junit.framework.TestCase;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by juliawong on 12/9/14.
 */
public class MarkerPropertiesTest extends TestCase {

    public void testDefaultProperties() {
        MarkerProperties mp = null;
        MarkerOptions mo = null;
        LatLng coord;
        JSONObject properties;

        try {
            properties = new JSONObject("{}");
            coord = new LatLng(0.0, 0.0);
            mp = new MarkerProperties(properties, coord);
            mo = mp.getMarkerOptions();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        assertNotNull(mp);
        assertEquals(null, mo.getTitle());
        assertEquals(null, mo.getSnippet());
        assertEquals((float) 1.0, mo.getAlpha());
        assertEquals((float) 0.5, mo.getAnchorU());
        assertEquals((float) 0.5, mo.getAnchorV());
        assertEquals(false, mo.isDraggable());
        assertEquals((float) 0, mo.getRotation());
        assertEquals(true, mo.isVisible());
        assertEquals(new LatLng(0.0, 0.0), mo.getPosition());
    }

    // TODO: this test and more :(
    public void testAllChangedProperties() {
        MarkerProperties mp = null;
        MarkerOptions mo = null;
        LatLng coord;
        JSONObject properties;
    }
}
