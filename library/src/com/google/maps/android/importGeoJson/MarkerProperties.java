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

    private String mId = null;

    private String mTitle = null;

    private String mSnippet = null;

    private float mAlpha = (float) 1.0;

    // Think about this as it needs to download, perhaps a task to be done with URL importing
    //private String mIcon;
    private float mAnchorU = (float) 0.5;

    private float mAnchorV = (float) 0.5;

    private boolean mIsDraggable = false;

    private float mRotation = 0;

    private boolean mIsVisible = true;

    private LatLng mCoodinates;

    /**
     * Takes in a JSONObject containing properties for a marker and saves relevant properties
     *
     * @param geoJsonPointProperties contains properties of a marker
     * @param coordinates LatLng object representing coordinates of the marker
     */
    public MarkerProperties(JSONObject geoJsonPointProperties, LatLng coordinates) throws JSONException {
        this.mCoodinates = coordinates;
        if (geoJsonPointProperties.has("id")) {
            mId = geoJsonPointProperties.getString("id");
        }
        if (geoJsonPointProperties.has("title")) {
            mTitle = geoJsonPointProperties.getString("title");
        }
        if (geoJsonPointProperties.has("snippet")) {
            mSnippet = geoJsonPointProperties.getString("snippet");
        }
        if (geoJsonPointProperties.has("alpha")) {
            mAlpha = (float) geoJsonPointProperties.getDouble("alpha");
        }
        if (geoJsonPointProperties.has("anchorU")) {
            mAnchorU = (float) geoJsonPointProperties.getDouble("anchorU");
        }
        if (geoJsonPointProperties.has("anchorV")) {
            mAnchorV = (float) geoJsonPointProperties.getDouble("anchorV");
        }
        if (geoJsonPointProperties.has("draggable")) {
            mIsDraggable = geoJsonPointProperties.getBoolean("draggable");
        }
        if (geoJsonPointProperties.has("rotation")) {
            mRotation = (float) geoJsonPointProperties.getDouble("rotation");
        }
        if (geoJsonPointProperties.has("visible")) {
            mIsVisible = geoJsonPointProperties.getBoolean("visible");
        }

    }

    /**
     * Gets the coordinates of the marker
     * @return coordinates of the marker
     */
    public LatLng getCoodinates() {
        return mCoodinates;
    }

    /**
     * Get the ID of the marker
     *
     * @return ID of marker
     */
    private String getId() {
        return mId;
    }

    /**
     * Get the title of the marker
     *
     * @return title of the marker
     */
    private String getTitle() {
        return mTitle;
    }

    /**
     * Get the snippet of the marker
     *
     * @return snippet of the marker
     */
    private String getSnippet() {
        return mSnippet;
    }

    /**
     * Get the alpha of the marker
     *
     * @return alpha of the marker
     */
    private float getAlpha() {
        return mAlpha;
    }

    /**
     * Get the u-coordinate from the anchor of the marker
     *
     * @return u-coordinate of the marker
     */
    private float getAnchorU() {
        return mAnchorU;
    }

    /**
     * Get the v-coordinate from the anchor of the marker
     *
     * @return v-coordinate of the marker
     */
    private float getAnchorV() {
        return mAnchorV;
    }

    /**
     * Gets whether the marker is draggable
     *
     * @return true if draggable; false otherwise
     */
    private boolean isDraggable() {
        return mIsDraggable;
    }

    /**
     * Gets the rotation of the marker
     *
     * @return rotation of the marker
     */
    private float getRotation() {
        return mRotation;
    }

    /**
     * Gets the visibility of the marker
     *
     * @return true if visible; false otherwise
     */
    private boolean isVisible() {
        return mIsVisible;
    }

    /**
     * Creates a MarkerOptions object with all of the properties from the properties object passed
     * in
     *
     * @return MarkerOptions object with defined options
     */
    public MarkerOptions getMarkerOptions() {
        MarkerOptions options = new MarkerOptions();
        options.position(getCoodinates()).title(getTitle()).snippet(getSnippet()).alpha(getAlpha())
                .anchor(getAnchorU(), getAnchorV()).draggable(isDraggable()).rotation(getRotation())
                .visible(isVisible());
        return options;
    }
}
