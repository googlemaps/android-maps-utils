package com.google.maps.android.geoJsonLayer;

import com.google.android.gms.maps.GoogleMap;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

/**
 * Created by juliawong on 12/29/14.
 */
public class Collection {

    private ArrayList<Feature> mFeatures;

    private GoogleMap mMap;

    private PointStyle mDefaultPointStyle;

    private LineStringStyle mDefaultLineStringStyle;

    private PolygonStyle mDefaultPolygonStyle;

    public Collection(GoogleMap map, JSONObject geoJsonObject)
            throws IOException, JSONException {
        mMap = map;
        mFeatures = new ArrayList<Feature>();
    }

    public Collection(GoogleMap map, int resourceId, Context context)
            throws IOException, JSONException {
        mMap = map;
        mFeatures = new ArrayList<Feature>();
        InputStream stream = context.getResources().openRawResource(resourceId);
        JSONObject geoJsonObject = createJsonFileObject(stream);
    }

    /**
     * Takes a character input stream and converts it into a JSONObject
     *
     * @param stream Character input stream representing  the GeoJSON file
     * @return JSONObject representing the GeoJSON file
     * @throws java.io.IOException   if the file cannot be opened for read
     * @throws org.json.JSONException if the JSON file has poor structure
     */
    private JSONObject createJsonFileObject(InputStream stream) throws IOException, JSONException {
        String line;
        StringBuilder result = new StringBuilder();
        // Reads from stream
        BufferedReader reader = new BufferedReader(new InputStreamReader(stream));

        // Read each line of the GeoJSON file into a string
        while ((line = reader.readLine()) != null) {
            result.append(line);
        }

        // Converts the result string into a JSONObject
        return new JSONObject(result.toString());
    }

    // TODO: implement an iterator thing or just return mFeatures

    public Feature getFeatureById(String id) {
        for (Feature feature : mFeatures) {
            if (feature.getId().equals(id)) {
                return feature;
            }
        }
        return null;
    }

    public GoogleMap getMap() {
        return mMap;
    }

    public void setMap(GoogleMap map) {
        mMap = map;
    }

    public void removeFeature(Feature feature) {
        mFeatures.remove(feature);
    }

    public PointStyle getDefaultPointStyle() {
        return mDefaultPointStyle;
    }

    public void setDefaultPointStyle(PointStyle pointStyle) {
        mDefaultPointStyle = pointStyle;
    }

    public LineStringStyle getDefaultLineStringStyle() {
        return mDefaultLineStringStyle;
    }

    public void setDefaultLineStringStyle(LineStringStyle lineStringStyle) {
        mDefaultLineStringStyle = lineStringStyle;
    }

    public PolygonStyle getDefaultPolygonStyle() {
        return mDefaultPolygonStyle;
    }

    public void setDefaultPolygonStyle(PolygonStyle polygonStyle) {
        mDefaultPolygonStyle = polygonStyle;
    }

    public void setPreserveViewPort(boolean preserveViewPort) {

    }

    public void setZIndex(float zIndex) {
        mDefaultLineStringStyle.setZIndex(zIndex);
        mDefaultPolygonStyle.setZIndex(zIndex);
        // TODO: redraw objects
    }

}
