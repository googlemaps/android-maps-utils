package com.google.maps.android.importGeoJson;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolygonOptions;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by juliawong on 12/9/14.
 *
 * Parses a given JSONObject containing properties for the Polygon
 * Allows the user to retrieve the PolygonOptions
 */
public class PolygonProperties {

    private ArrayList<ArrayList<LatLng>> mCoordinates;

    private ArrayList<ArrayList<LatLng>> hCoordinates;

    PolygonOptions options;

    /**
     * Takes in a JSONObject containing properties for a polygon and saves relevant properties
     *
     * @param geoJsonPolygonProperties contains properties of a polygon
     * @param coordinates              contains a list of a list of coordinates representing the
     *                                 polygon and its holes
     */
    public PolygonProperties(JSONObject geoJsonPolygonProperties,
            ArrayList<ArrayList<LatLng>> coordinates) throws JSONException {

        this.options = new PolygonOptions();
        this.mCoordinates = coordinates;
        setHoles();
        options.addAll(this.mCoordinates.get(0));


        if (geoJsonPolygonProperties != null) {
            if (geoJsonPolygonProperties.has("id")) {
                //TODO: What do I do with the id??
            }
            if (geoJsonPolygonProperties.has("stroke width")) {
                this.options.strokeWidth((float) geoJsonPolygonProperties.getDouble("stroke width"));
            }
            if (geoJsonPolygonProperties.has("stroke color")) {
                this.options.strokeColor(geoJsonPolygonProperties.getInt("stroke color"));
            }
            if (geoJsonPolygonProperties.has("fill color")) {
                this.options.fillColor(geoJsonPolygonProperties.getInt("fill color"));
            }
            if (geoJsonPolygonProperties.has("z index")) {
               this.options.zIndex((float) geoJsonPolygonProperties.getDouble("z index"));
            }
            if (geoJsonPolygonProperties.has("visible")) {
               this.options.visible(geoJsonPolygonProperties.getBoolean("stroke visible"));
            }
            if (geoJsonPolygonProperties.has("geodesic")) {
                this.options.geodesic(geoJsonPolygonProperties.getBoolean("geodesic"));
            }
        }
    }

    private ArrayList<ArrayList<LatLng>> setHoles() {
        hCoordinates = new ArrayList<ArrayList<LatLng>>();
        if(mCoordinates.size() > 1) {
            for (int i = 1; i < mCoordinates.size(); i++) {
                hCoordinates.add(mCoordinates.get(i));
                options.addHole(mCoordinates.get(i));
            }
        }
        return hCoordinates;
    }


    /**
     * Creates a PolygonOptions object with all of the properties from the properties object passed
     * in
     *
     * @return PolygonOptions object with defined options
     */
    public PolygonOptions getPolygonOptions() {
        return this.options;
    }
}
