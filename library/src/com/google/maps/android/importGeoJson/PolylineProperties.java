package com.google.maps.android.importGeoJson;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by juliawong on 12/9/14.
 *
 * Parses a given JSONObject containing properties for the Polyline
 * Allows the user to retrieve the MarkerOptions
 */

public class PolylineProperties {


    private ArrayList<LatLng> mCoordinates;

    private PolylineOptions options;

    /**
     * Takes in a JSONObject containing properties for a polyline and saves relevant properties
     *
     * @param geoJsonPointProperties contains properties of a polyline
     * @param coordinates            Array of LatLngs representing the coordinates
     */
    public PolylineProperties(JSONObject geoJsonPointProperties, ArrayList<LatLng> coordinates)
            throws JSONException {

        this.options = new PolylineOptions();

        this.mCoordinates = coordinates;

        this.options.addAll(mCoordinates);


        if (geoJsonPointProperties.has("id")) {
            //TODO: What do I do with the id?
        }
        if (geoJsonPointProperties.has("width")) {
            this.options.width((float) geoJsonPointProperties.getDouble("width"));
        }
        if (geoJsonPointProperties.has("color")) {
            this.options.color(geoJsonPointProperties.getInt("color"));
        }
        if (geoJsonPointProperties.has("z index")) {
            this.options.zIndex((float) geoJsonPointProperties.getDouble("z index"));
        }
        if (geoJsonPointProperties.has("visible")) {
            this.options.visible(geoJsonPointProperties.getBoolean("visible"));
        }
        if (geoJsonPointProperties.has("geodesic")) {
            this.options.geodesic(geoJsonPointProperties.getBoolean("geodesic"));

        }
    }

    public PolylineOptions getPolylineOptions() {
        return this.options;
    }
}
