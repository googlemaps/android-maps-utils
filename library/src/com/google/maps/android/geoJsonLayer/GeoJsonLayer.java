package com.google.maps.android.geoJsonLayer;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;


/**
 * A class that allows the developer import GeoJSON data, style it and interact with the imported
 * data.
 */
public class GeoJsonLayer {

    private final HashMap<GeoJsonFeature, Object> mFeatures;

    private final GeoJsonRenderer mRenderer;

    private final JSONObject mGeoJsonFile;

    private final GeoJsonParser mParser;

    private GeoJsonPointStyle mDefaultPointStyle;

    private GeoJsonLineStringStyle mDefaultLineStringStyle;

    private GeoJsonPolygonStyle mDefaultPolygonStyle;

    // Flag indicating whether the GeoJSON data has been added to the map
    private boolean mGeoJsonDataOnMap;

    private ArrayList<LatLng> mBoundingBox;

    /**
     * Creates a new GeoJsonLayer object
     *
     * @param map         GoogleMap object
     * @param geoJsonFile JSONObject to parse GeoJSON data from
     * @throws JSONException if the JSON file has invalid syntax and cannot be parsed successfully
     */
    public GeoJsonLayer(GoogleMap map, JSONObject geoJsonFile) {
        mFeatures = new HashMap<GeoJsonFeature, Object>();
        mDefaultPointStyle = new GeoJsonPointStyle();
        mDefaultLineStringStyle = new GeoJsonLineStringStyle();
        mDefaultPolygonStyle = new GeoJsonPolygonStyle();
        mGeoJsonDataOnMap = false;
        mGeoJsonFile = geoJsonFile;
        mBoundingBox = null;
        mRenderer = new GeoJsonRenderer(mFeatures, map);
        mParser = new GeoJsonParser(geoJsonFile);
    }

    /**
     * Creates a new GeoJsonLayer object
     *
     * @param map        GoogleMap object
     * @param resourceId Raw resource GeoJSON file
     * @param context    Context object
     * @throws IOException   if the file cannot be open for read
     * @throws JSONException if the JSON file has invalid syntax and cannot be parsed successfully
     */
    public GeoJsonLayer(GoogleMap map, int resourceId, Context context)
            throws IOException, JSONException {
        this(map, createJsonFileObject(context.getResources().openRawResource(resourceId)));
    }

    /**
     * Takes a character input stream and converts it into a JSONObject
     *
     * @param stream Character input stream representing the GeoJSON file
     * @return JSONObject representing the GeoJSON file
     * @throws java.io.IOException    if the file cannot be opened for read
     * @throws org.json.JSONException if the JSON file has poor structure
     */
    private static JSONObject createJsonFileObject(InputStream stream)
            throws IOException, JSONException {
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

    /**
     * Gets an iterator of all GeoJsonFeature elements stored
     *
     * @return iterator of GeoJsonFeature elements
     */
    public java.util.Iterator<GeoJsonFeature> getFeatures() {
        return mFeatures.keySet().iterator();
    }

    /**
     * Gets the GeoJsonFeature with the given ID if it exists
     *
     * @param id of GeoJsonFeature object to find
     * @return GeoJsonFeature object with matching ID or otherwise null
     */
    public GeoJsonFeature getFeatureById(String id) {
        for (GeoJsonFeature feature : mFeatures.keySet()) {
            if (feature.getId() != null && feature.getId().equals(id)) {
                return feature;
            }
        }
        return null;
    }

    /**
     * Adds a GeoJsonFeature to the GeoJsonLayer
     *
     * @param feature GeoJsonFeature to add
     */
    public void addFeature(GeoJsonFeature feature) {
        mFeatures.put(feature, null);
        feature.addObserver(mRenderer);
        mRenderer.addFeature(feature);
    }

    /**
     * Removes a GeoJsonFeature from the GeoJsonLayer
     *
     * @param feature GeoJsonFeature to remove
     */
    public void removeFeature(GeoJsonFeature feature) {
        mRenderer.removeFeature(feature);
        mFeatures.remove(feature);
        feature.deleteObserver(mRenderer);
    }

    /**
     * Gets the GoogleMap on which the GeoJsonFeature are displayed
     *
     * @return map on which the GeoJsonFeature are displayed
     */
    public GoogleMap getMap() {
        return mRenderer.getMap();
    }

    /**
     * Renders all stored GeoJsonFeature on the specified GoogleMap
     *
     * @param map to render GeoJsonFeature on, if null all GeoJsonFeature objects are cleared from
     *            the map
     */
    public void setMap(GoogleMap map) {
        mRenderer.setMap(map);
    }

    /**
     * Adds all the GeoJsonFeature objects parsed from the GeoJSON document onto the map
     */
    public void addGeoJsonData() throws JSONException {
        if (!mGeoJsonDataOnMap) {
            parseGeoJsonFile();
            for (GeoJsonFeature geoJsonFeature : mFeatures.keySet()) {
                geoJsonFeature.addObserver(mRenderer);
            }

            mRenderer.addLayerToMap();
            mGeoJsonDataOnMap = true;
        }
    }

    /**
     * Removes all of the stored GeoJsonFeature objects from the map and mFeatures
     */
    public void removeGeoJsonData() {
        if (mGeoJsonDataOnMap) {
            for (GeoJsonFeature feature : mFeatures.keySet()) {
                feature.deleteObserver(mRenderer);
            }
            mRenderer.removeLayerFromMap();
            mFeatures.clear();
            // TODO: cater for when the developer uses addFeature()
            //
            mGeoJsonDataOnMap = false;
        }
    }

    /**
     * Parses the stored GeoJSON file into GeoJsonFeature objects
     *
     * @throws JSONException if GeoJSON file cannot be parsed
     */
    private void parseGeoJsonFile() throws JSONException {
        mParser.parseGeoJson();
        for (GeoJsonFeature feature : mParser.getFeatures()) {
            mFeatures.put(feature, null);
        }
        // Assign GeoJSON bounding box for FeatureCollection
        mBoundingBox = mParser.getBoundingBox();
    }

    /**
     * Gets the default style for the GeoJsonPoint objects
     *
     * @return GeoJsonPointStyle containing default styles for GeoJsonPoint
     */
    public GeoJsonPointStyle getDefaultPointStyle() {
        return mDefaultPointStyle;
    }

    /**
     * Sets the default style for the GeoJsonPoint objects. Overrides all current styles on
     * GeoJsonPoint objects.
     *
     * @param geoJsonPointStyle to set as default style, this is applied to all GeoJsonPoints that
     *                          are stored
     */
    public void setDefaultPointStyle(GeoJsonPointStyle geoJsonPointStyle) {
        mDefaultPointStyle = geoJsonPointStyle;
        for (GeoJsonFeature geoJsonFeature : mFeatures.keySet()) {
            geoJsonFeature.setPointStyle(geoJsonPointStyle);
        }
    }

    /**
     * Gets the default style for the GeoJsonLineString objects
     *
     * @return GeoJsonLineStringStyle containing default styles for GeoJsonLineString
     */
    public GeoJsonLineStringStyle getDefaultLineStringStyle() {
        return mDefaultLineStringStyle;
    }

    /**
     * Sets the default style for the GeoJsonLineString objects. Overrides all current styles on
     * GeoJsonLineString objects.
     *
     * @param geoJsonLineStringStyle to set as default style, this is applied to all
     *                               GeoJsonLineString that are stored
     */
    public void setDefaultLineStringStyle(GeoJsonLineStringStyle geoJsonLineStringStyle) {
        mDefaultLineStringStyle = geoJsonLineStringStyle;
        for (GeoJsonFeature geoJsonFeature : mFeatures.keySet()) {
            geoJsonFeature.setLineStringStyle(geoJsonLineStringStyle);
        }
    }

    /**
     * Gets the default style for the GeoJsonPolygon objects
     *
     * @return GeoJsonPolygonStyle containing default styles for GeoJsonPolygon
     */
    public GeoJsonPolygonStyle getDefaultPolygonStyle() {
        return mDefaultPolygonStyle;
    }

    /**
     * Sets the default style for the GeoJsonPolygon objects. Overrides all current styles on
     * GeoJsonPolygon objects.
     *
     * @param geoJsonPolygonStyle to set as default style, this is applied to all GeoJsonPolygon
     *                            that are stored
     */
    public void setDefaultPolygonStyle(GeoJsonPolygonStyle geoJsonPolygonStyle) {
        mDefaultPolygonStyle = geoJsonPolygonStyle;
        for (GeoJsonFeature geoJsonFeature : mFeatures.keySet()) {
            geoJsonFeature.setPolygonStyle(geoJsonPolygonStyle);
        }
    }

    /**
     * Gets the array containing the coordinates of the bounding box for the FeatureCollection. If
     * the FeatureCollection did not have a bounding box or if the GeoJSON file did not contain a
     * FeatureCollection then null will be returned.
     *
     * @return array of 2 LatLng containing bounding box of FeatureCollection, null if no bounding
     * box
     */
    public ArrayList<LatLng> getBoundingBox() {
        return mBoundingBox;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("Collection{");
        sb.append("\n features=").append(mFeatures);
        sb.append(",\n Point style=").append(mDefaultPointStyle);
        sb.append(",\n LineString style=").append(mDefaultLineStringStyle);
        sb.append(",\n Polygon style=").append(mDefaultPolygonStyle);
        sb.append("\n}\n");
        return sb.toString();
    }
}
