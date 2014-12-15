package com.google.maps.android.importGeoJson;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by juliawong on 12/9/14.
 *
 * Parses a given JSONObject containing properties for the Marker
 * Allows the user to retrieve the MarkerOptions
 */
public class MarkerProperties {


    MarkerOptions options;

    /**
     * Takes in a JSONObject containing properties for a marker and saves relevant properties
     *
     * @param geoJsonPointProperties contains properties of a marker
     * @param coordinates LatLng object representing coordinates of the marker
     */
    public MarkerProperties(JSONObject geoJsonPointProperties, LatLng coordinates) throws JSONException {

        this.options = new MarkerOptions();

        this.options.position(coordinates);


        if (geoJsonPointProperties != null) {

            if (geoJsonPointProperties.has("id")) {
                //TODO: What do I do with the ID?
            }
            if (geoJsonPointProperties.has("title")) {
                this.options.title(geoJsonPointProperties.getString("title"));
            }
            if (geoJsonPointProperties.has("snippet")) {
                this.options.snippet(geoJsonPointProperties.getString("snippet"));
            }
            if (geoJsonPointProperties.has("alpha")) {
                this.options.alpha((float) geoJsonPointProperties.getDouble("alpha"));
            }
            if (geoJsonPointProperties.has("anchorU") && geoJsonPointProperties.has("anchorV")) {
                this.options.anchor((float) geoJsonPointProperties.getDouble("anchorU"),
                        (float) geoJsonPointProperties.getDouble("anchorV"));
            }
            if (geoJsonPointProperties.has("draggable")) {
                this.options.draggable(geoJsonPointProperties.getBoolean("draggable"));
            }
            if (geoJsonPointProperties.has("rotation")) {
                this.options.rotation((float) geoJsonPointProperties.getDouble("rotation"));
            }
            if (geoJsonPointProperties.has("visible")) {
                this.options.visible(geoJsonPointProperties.getBoolean("visible"));
            }
        }
    }

    public MarkerOptions getMarkerOptions() {
        return this.options;
    }
}
