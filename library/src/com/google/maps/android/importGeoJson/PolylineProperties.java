package com.google.maps.android.importGeoJson;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONException;
import org.json.JSONObject;

import android.graphics.Color;

import java.util.ArrayList;

/**
 * Created by juliawong on 12/9/14.
 *
 * Parses a given JSONObject containing properties for the Polyline and creates a {@link
 * com.google.android.gms.maps.model.PolylineOptions} object
 */
public class PolylineProperties {

    private final PolylineOptions mOptions;

    /**
     * Takes in a JSONObject containing properties for a Polyline and parses the relevant
     * properties
     * for mOptions
     *
     * @param geoJsonPointProperties contains style properties of a polyline
     * @param coordinates            array of {@link com.google.android.gms.maps.model.LatLng}
     *                               representing the coordinates
     */
    public PolylineProperties(JSONObject geoJsonPointProperties, ArrayList<LatLng> coordinates)
            throws JSONException {

        this.mOptions = new PolylineOptions();

        this.mOptions.addAll(coordinates);

        // Parse style properties relevant to a Polyline
        if (geoJsonPointProperties != null) {

            if (geoJsonPointProperties.has("id")) {
                //TODO: To consider if returning the elements added to the map
            }
            if (geoJsonPointProperties.has("width")) {
                this.mOptions.width((float) geoJsonPointProperties.getDouble("width"));
            }
            if (geoJsonPointProperties.has("color")) {
                this.mOptions.color(Color.parseColor(geoJsonPointProperties.getString("color")));
            }
            if (geoJsonPointProperties.has("z index")) {
                this.mOptions.zIndex((float) geoJsonPointProperties.getDouble("z index"));
            }
            if (geoJsonPointProperties.has("visible")) {
                this.mOptions.visible(geoJsonPointProperties.getBoolean("visible"));
            }
            if (geoJsonPointProperties.has("geodesic")) {
                this.mOptions.geodesic(geoJsonPointProperties.getBoolean("geodesic"));

            }
        }
    }

    /**
     * Creates a PolylineOptions object with all of the properties from the properties object
     * passed
     * in
     *
     * @return PolylineOptions object with defined mOptions
     */
    public PolylineOptions getPolylineOptions() {
        return this.mOptions;
    }

    /**
     * Creates a string containing properties for the polyline
     * @return string containing properties for polyline
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("PolylineProperties{");
        sb.append("\n points=").append(mOptions.getPoints());
        sb.append(",\n width=").append(mOptions.getWidth());
        sb.append(",\n color=").append(mOptions.getColor());
        sb.append(",\n z index=").append(mOptions.getZIndex());
        sb.append(",\n visible=").append(mOptions.isVisible());
        sb.append(",\n geodesic=").append(mOptions.isGeodesic());
        sb.append("\n}");
        return sb.toString();
    }

}
