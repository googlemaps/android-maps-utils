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

    private String mId = null;

    private float mWidth = 10;

    private int mColor = 0xff000000;

    private float mZIndex = 0;

    private boolean mIsVisible = true;

    private boolean mIsGeodesic = false;

    private ArrayList<LatLng> mCoordinates;

    /**
     * Takes in a JSONObject containing properties for a polyline and saves relevant properties
     *
     * @param geoJsonPointProperties contains properties of a polyline
     * @param coordinates            Array of LatLngs representing the coordinates
     */
    public PolylineProperties(JSONObject geoJsonPointProperties, ArrayList<LatLng> coordinates)
            throws JSONException {
        this.mCoordinates = coordinates;
        if (geoJsonPointProperties.has("id")) {
            mId = geoJsonPointProperties.getString("id");
        }
        if (geoJsonPointProperties.has("width")) {
            mWidth = (float) geoJsonPointProperties.getDouble("width");
        }
        if (geoJsonPointProperties.has("color")) {
            mColor = geoJsonPointProperties.getInt("color");
        }
        if (geoJsonPointProperties.has("z index")) {
            mZIndex = (float) geoJsonPointProperties.getDouble("z index");
        }
        if (geoJsonPointProperties.has("visible")) {
            mIsVisible = geoJsonPointProperties.getBoolean("visible");
        }
        if (geoJsonPointProperties.has("geodesic")) {
            mIsGeodesic = geoJsonPointProperties.getBoolean("geodesic");

        }
    }

    /**
     * Gets coordinates of the polyline
     *
     * @return list with coordinates of the polyline
     */
    public ArrayList<LatLng> getCoordinates() {
        return mCoordinates;
    }

    /**
     * Gets the ID of the polyline
     *
     * @return ID of the polyline
     */
    private String getId() {
        return mId;
    }

    /**
     * Gets the width of the polyline
     *
     * @return width of the polyline
     */
    private float getWidth() {
        return mWidth;
    }

    /**
     * Gets the color of the polyline
     *
     * @return color of the polyline
     */
    private int getColor() {
        return mColor;
    }

    /**
     * Gets the z index of the polyline
     *
     * @return z index of the polyline
     */
    private float getZIndex() {
        return mZIndex;
    }

    /**
     * Gets the visibility of the polyline
     *
     * @return true if visible; false otherwise
     */
    private boolean isVisible() {
        return mIsVisible;
    }

    /**
     * Gets whether each segment of the line is drawn as a geodesic or not
     *
     * @return true if each segment is drawn as a geodesic; false otherwise
     */
    private boolean isGeodesic() {
        return mIsGeodesic;
    }

    /**
     * Creates a PolylineOptions object with all of the properties from the properties object
     * passed
     * in
     *
     * @return PolylineOptions object with defined options
     */
    public PolylineOptions getPolylineOptions() {
        PolylineOptions options = new PolylineOptions();
        options.addAll(getCoordinates()).width(getWidth()).color(getColor()).zIndex(getZIndex())
                .visible(isVisible()).geodesic(isGeodesic());
        return options;
    }
}
