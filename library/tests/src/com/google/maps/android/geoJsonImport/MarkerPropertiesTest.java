package com.google.maps.android.geoJsonImport;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import junit.framework.TestCase;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by juliawong on 12/9/14.
 */
public class MarkerPropertiesTest extends TestCase {

    public void testDefaultProperties() throws JSONException {
        MarkerProperties mp = null;
        MarkerOptions mo = null;
        LatLng coord;
        JSONObject properties;

            properties = new JSONObject("{}");
            coord = new LatLng(0.0, 0.0);
            mp = new MarkerProperties(properties, coord);
            mo = mp.getMarkerOptions();


        assertNotNull(mp);
        assertEquals(new LatLng(0.0, 0.0), mo.getPosition());
        assertEquals(null, mo.getTitle());
        assertEquals(null, mo.getSnippet());
        assertEquals((float) 1.0, mo.getAlpha());
        assertEquals((float) 0.5, mo.getAnchorU());
        assertEquals((float) 1.0, mo.getAnchorV());
        assertEquals(false, mo.isDraggable());
        assertEquals((float) 0, mo.getRotation());
        assertEquals(true, mo.isVisible());
        assertEquals(false, mo.isFlat());
        assertEquals((float) 0.5, mo.getInfoWindowAnchorU());
        assertEquals((float) 0, mo.getInfoWindowAnchorV());
    }

    public void testAllChangedProperties() throws JSONException {
        MarkerProperties mp = null;
        MarkerOptions mo = null;
        LatLng coord;
        JSONObject properties;

        properties = new JSONObject("{\n"
                + "\"title\": \"test title\",\n"
                + "\"snippet\": \"this is a description\",\n"
                + "\"alpha\": 0.2,\n"
                + "\"anchorU\": 0.8,\n"
                + "\"anchorV\": 0.2,\n"
                + "\"draggable\": true,\n"
                + "\"rotation\": 3.5,\n"
                + "\"visible\": false,\n"
                + "\"flat\": false,\n"
                + "\"infoWindowAnchorU\": 0.1,\n"
                + "\"infoWindowAnchorV\": 0.9\n"
                + "}");
        coord = new LatLng(20.0, -30.5);
        mp = new MarkerProperties(properties, coord);
        mo = mp.getMarkerOptions();

        assertNotNull(mp);
        assertEquals(new LatLng(20.0, -30.5), mo.getPosition());
        assertEquals("test title", mo.getTitle());
        assertEquals("this is a description", mo.getSnippet());
        assertEquals((float) 0.2, mo.getAlpha());
        assertEquals((float) 0.8, mo.getAnchorU());
        assertEquals((float) 0.2, mo.getAnchorV());
        assertEquals(true, mo.isDraggable());
        assertEquals((float) 3.5, mo.getRotation());
        assertEquals(false, mo.isVisible());
        assertEquals(false, mo.isFlat());
        assertEquals((float) 0.1, mo.getInfoWindowAnchorU());
        assertEquals((float) 0.9, mo.getInfoWindowAnchorV());
    }

    public void testCoordinateProperty() throws JSONException {
        MarkerProperties mp = null;
        MarkerOptions mo = null;
        LatLng coord;
        JSONObject properties = new JSONObject("{}");
        // Valid coordinates
        coord = new LatLng(30.0, 30.0);
        mp = new MarkerProperties(properties, coord);
        mo = mp.getMarkerOptions();
        assertEquals(new LatLng(30.0, 30.0), mo.getPosition());

        // Negative coordinates
        coord = new LatLng(-25.1, -50.64);
        mp = new MarkerProperties(properties, coord);
        mo = mp.getMarkerOptions();
        assertEquals(new LatLng(-25.1, -50.64), mo.getPosition());

        // Negative latitude value out of range
        coord = new LatLng(-155.6, 0);
        mp = new MarkerProperties(properties, coord);
        mo = mp.getMarkerOptions();
        assertEquals(new LatLng(-90, 0), mo.getPosition());

        // Positive latitude value out of range
        coord = new LatLng(654.6, 0);
        mp = new MarkerProperties(properties, coord);
        mo = mp.getMarkerOptions();
        assertEquals(new LatLng(90, 0), mo.getPosition());

        // Negative longitude value out of range
        coord = new LatLng(0, -560.1);
        mp = new MarkerProperties(properties, coord);
        mo = mp.getMarkerOptions();
        assertEquals(new LatLng(0, modLongitudeValue(-560.1)), mo.getPosition());

        // Positive longitude value out of range
        coord = new LatLng(0, 252.34);
        mp = new MarkerProperties(properties, coord);
        mo = mp.getMarkerOptions();
        // TODO: Less than this
        assertEquals(new LatLng(0, modLongitudeValue(252.34)), mo.getPosition());

        // Latitude and Longitude values out of range
        coord = new LatLng(-564.12, -879.2);
        mp = new MarkerProperties(properties, coord);
        mo = mp.getMarkerOptions();
        assertEquals(new LatLng(-90.0, modLongitudeValue(-879.2)), mo.getPosition());

        coord = new LatLng(555.5, 555.5);
        mp = new MarkerProperties(properties, coord);
        mo = mp.getMarkerOptions();
        assertEquals(new LatLng(90, modLongitudeValue(555.5)), mo.getPosition());
    }

    /**
     * Helper function to calculate longitude values outside of the upper and lower bounds
     * @param longitude value to mod
     * @return modded longitude
     */
    private double modLongitudeValue(double longitude) {
        if (-180.0 <= longitude && longitude < 180.0) {
            return longitude;
        } else {
            return ((longitude - 180.0) % 360.0 + 360.0) % 360.0 - 180.0;
        }
    }
}
