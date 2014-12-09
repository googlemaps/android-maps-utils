package com.google.maps.android.importGeoJson;

import com.google.android.gms.maps.model.PolygonOptions;

import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

/**
 * Created by juliawong on 12/9/14.
 */

/**
 * Parses a given JSONObject containing properties for the Polygon
 * Allows the user to retrieve the PolygonOptions
 */
public class PolygonProperties {

    private String mId = null;

    private float mStrokeWidth = 10;

    private int mStrokeColor = 0xff000000;

    private int mFillColor = 0x00000000;

    private float mZIndex = 0;

    private boolean mIsVisible = true;

    private boolean mIsGeodesic = false;

    /**
     * Takes in a JSONObject containing properties for a polygon and saves relevant properties
     *
     * @param geoJsonPolygonProperties contains properties of a polygon
     */
    public PolygonProperties(JSONObject geoJsonPolygonProperties) throws JSONException {
        Log.i("TEST", geoJsonPolygonProperties.toString());
        if (geoJsonPolygonProperties.has("id")) {
            mId = geoJsonPolygonProperties.getString("id");
        }
        if (geoJsonPolygonProperties.has("stroke width")) {
            mStrokeWidth = (float) geoJsonPolygonProperties.getDouble("stroke width");
        }
        if (geoJsonPolygonProperties.has("stroke color")) {
            mStrokeColor = geoJsonPolygonProperties.getInt("stroke color");
        }
        if (geoJsonPolygonProperties.has("fill color")) {
            mFillColor = geoJsonPolygonProperties.getInt("fill color");
        }
        if (geoJsonPolygonProperties.has("z index")) {
            mZIndex = (float) geoJsonPolygonProperties.getDouble("z index");
        }
        if (geoJsonPolygonProperties.has("visible")) {
            mIsVisible = geoJsonPolygonProperties.getBoolean("stroke visible");
        }
        if (geoJsonPolygonProperties.has("geodesic")) {
            mIsGeodesic = geoJsonPolygonProperties.getBoolean("geodesic");
        }
    }

    /**
     * Gets the ID of the polygon
     *
     * @return ID of polygon
     */
    private String getId() {
        return mId;
    }

    /**
     * Gets the stroke width of the polygon
     *
     * @return stroke width of polygon
     */
    private float getStrokeWidth() {
        return mStrokeWidth;
    }

    /**
     * Gets the stroke color of the polygon
     *
     * @return stroke color of polygon
     */
    private int getStrokeColor() {
        return mStrokeColor;
    }

    /**
     * Gets the fill color of the polygon
     *
     * @return fill color of polygon
     */
    private int getFillColor() {
        return mFillColor;
    }

    /**
     * Gets the z index of the polygon
     *
     * @return z index of polygon
     */
    private float getZIndex() {
        return mZIndex;
    }

    /**
     * Gets the visibility of the polygon
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
     * Creates a PolygonOptions object with all of the properties from the properties object passed
     * in
     *
     * @return PolygonOptions object with defined options
     */
    public PolygonOptions getPolygonOptions() {
        PolygonOptions options = new PolygonOptions();
        options.strokeWidth(getStrokeWidth()).strokeColor(getStrokeColor())
                .fillColor(getFillColor()).zIndex(getZIndex()).visible(isVisible())
                .geodesic(mIsGeodesic);
        return options;
    }
}
