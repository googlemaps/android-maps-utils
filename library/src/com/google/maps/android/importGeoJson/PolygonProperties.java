package com.google.maps.android.importGeoJson;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolygonOptions;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by juliawong on 12/9/14.
 *
 * Parses a given JSONObject containing properties for the Polygon and creates a {@link
 * com.google.android.gms.maps.model.PolygonOptions} object
 */
public class PolygonProperties {

    private final PolygonOptions mOptions;

    private final ArrayList<ArrayList<LatLng>> mCoordinates;

    /**
     * Takes in a JSONObject containing properties for a polygon and saves relevant properties
     *
     * @param geoJsonPolygonProperties contains properties of a polygon
     * @param coordinates              contains a list of a list of coordinates representing the
     *                                 polygon and its holes
     */
    public PolygonProperties(JSONObject geoJsonPolygonProperties,
            ArrayList<ArrayList<LatLng>> coordinates) throws JSONException {

        this.mOptions = new PolygonOptions();
        this.mCoordinates = coordinates;
        setHoles();
        // First element in mCoordinates defines the vertices of the Polygon
        mOptions.addAll(this.mCoordinates.get(0));

        if (geoJsonPolygonProperties != null) {
            if (geoJsonPolygonProperties.has("id")) {
                //TODO: What do I do with the id??
            }
            if (geoJsonPolygonProperties.has("stroke width")) {
                this.mOptions
                        .strokeWidth((float) geoJsonPolygonProperties.getDouble("stroke width"));
            }
            if (geoJsonPolygonProperties.has("stroke color")) {
                this.mOptions.strokeColor(geoJsonPolygonProperties.getInt("stroke color"));
            }
            if (geoJsonPolygonProperties.has("fill color")) {
                this.mOptions.fillColor(geoJsonPolygonProperties.getInt("fill color"));
            }
            if (geoJsonPolygonProperties.has("z index")) {
                this.mOptions.zIndex((float) geoJsonPolygonProperties.getDouble("z index"));
            }
            if (geoJsonPolygonProperties.has("visible")) {
                this.mOptions.visible(geoJsonPolygonProperties.getBoolean("stroke visible"));
            }
            if (geoJsonPolygonProperties.has("geodesic")) {
                this.mOptions.geodesic(geoJsonPolygonProperties.getBoolean("geodesic"));
            }
        }
    }

    /**
     * Adds the hole property to mOptions
     * All elements, except the first, in mCoordinates are considered to be holes
     */
    private void setHoles() {
        if (mCoordinates.size() > 1) {
            for (int i = 1; i < mCoordinates.size(); i++) {
                mOptions.addHole(mCoordinates.get(i));
            }
        }
    }

    /**
     * Creates a PolygonOptions object with all of the properties from the properties object passed
     * in
     *
     * @return PolygonOptions object with defined mOptions
     */
    public PolygonOptions getPolygonOptions() {
        return this.mOptions;
    }
}
