package com.google.maps.android.geoJsonLayer;

import com.google.android.gms.maps.GoogleMap;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;

/**
 * Created by juliawong on 12/29/14.
 *
 * Adds a new layer of GeoJSON data with methods to allow the developer to interact with it.
 */
public class GeoJsonLayer {

    private final HashMap<GeoJsonFeature, Object> mFeatures;

    private GeoJsonPointStyle mDefaultPointStyle;

    private GeoJsonLineStringStyle mDefaultLineStringStyle;

    private GeoJsonPolygonStyle mDefaultPolygonStyle;

    private boolean mGeoJsonDataOnMap;

    private JSONObject mGeoJsonFile;

    private GeoJsonRenderer mRenderer;

    /**
     * Creates a new Collection object
     *
     * @param map           GoogleMap object
     * @param geoJsonObject JSONObject to parse GeoJSON data from
     */
    public GeoJsonLayer(GoogleMap map, JSONObject geoJsonObject) {
        mFeatures = new HashMap<GeoJsonFeature, Object>();
        mDefaultPointStyle = new GeoJsonPointStyle();
        mDefaultLineStringStyle = new GeoJsonLineStringStyle();
        mDefaultPolygonStyle = new GeoJsonPolygonStyle();
        mGeoJsonDataOnMap = false;
        mGeoJsonFile = geoJsonObject;
        mRenderer = new GeoJsonRenderer(mFeatures, map);
    }

    /**
     * Creates a new Collection object
     *
     * @param map        GoogleMap object
     * @param resourceId Raw resource GeoJSON file
     * @param context    Context object
     * @throws IOException   if the file cannot be open for read
     * @throws JSONException if the JSON file has invalid syntax and cannot be parsed successfully
     */
    public GeoJsonLayer(GoogleMap map, int resourceId, Context context)
            throws IOException, JSONException {
        mFeatures = new HashMap<GeoJsonFeature, Object>();
        mDefaultPointStyle = new GeoJsonPointStyle();
        mDefaultLineStringStyle = new GeoJsonLineStringStyle();
        mDefaultPolygonStyle = new GeoJsonPolygonStyle();
        mGeoJsonDataOnMap = false;
        InputStream stream = context.getResources().openRawResource(resourceId);
        mGeoJsonFile = createJsonFileObject(stream);
        mRenderer = new GeoJsonRenderer(mFeatures, map);
    }

    /**
     * Takes a character input stream and converts it into a JSONObject
     *
     * @param stream Character input stream representing  the GeoJSON file
     * @return JSONObject representing the GeoJSON file
     * @throws java.io.IOException    if the file cannot be opened for read
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

    /**
     * Gets an iterator of all feature elements.
     *
     * @return iterator of feature elements
     */
    public java.util.Iterator<GeoJsonFeature> getFeatures() {
        return mFeatures.keySet().iterator();
    }

    /**
     * Returns the features with the given ID if it exists
     *
     * @param id of Feature object to find
     * @return Feature object with matching ID or otherwise null
     */
    public GeoJsonFeature getFeatureById(String id) {
        for (GeoJsonFeature feature : mFeatures.keySet()) {
            if (feature.getId().equals(id)) {
                return feature;
            }
        }
        return null;
    }

    /**
     * Removes a feature from the collection
     *
     * @param feature to remove
     */
    public void removeFeature(GeoJsonFeature feature) {
        mFeatures.remove(feature);
    }

    /**
     * Returns the map on which the features are displayed
     *
     * @return map on which the features are displayed
     */
    public GoogleMap getMap() {
        return mRenderer.getMap();
    }

    /**
     * Renders features on the specified map
     *
     * @param map to render features on, if null all features are cleared from the map
     */
    public void setMap(GoogleMap map) {
        mRenderer.setMap(map);
    }


    public void addGeoJsonData() throws JSONException {
        if (!mGeoJsonDataOnMap) {
            GeoJsonParser parser = new GeoJsonParser(mGeoJsonFile);
            parser.parseGeoJson();

            for (GeoJsonFeature geoJsonFeature : parser.getFeatures()) {
                geoJsonFeature.addObserver(mRenderer);
                mFeatures.put(geoJsonFeature, null);
            }

            mRenderer.setFeatures(mFeatures);
            mRenderer.addLayerToMap();
            mGeoJsonDataOnMap = true;
        }
    }

    public void removeGeoJsonData() {
        mRenderer.removeLayerFromMap();
        mGeoJsonDataOnMap = false;
    }

    /**
     * Gets the default style for the Point objects
     *
     * @return PointStyle containing default styles for Point
     */
    public GeoJsonPointStyle getDefaultPointStyle() {
        return mDefaultPointStyle;
    }

    /**
     * Sets the default style for the Point objects. Overrides all current styles on Point objects.
     *
     * @param geoJsonPointStyle to set as default style, this is applied to Points as they are
     *                          imported
     *                          from the GeoJSON file
     */
    public void setDefaultPointStyle(GeoJsonPointStyle geoJsonPointStyle) {
        mDefaultPointStyle = geoJsonPointStyle;
        for (GeoJsonFeature geoJsonFeature : mFeatures.keySet()) {
            geoJsonFeature.setPointStyle(geoJsonPointStyle);
        }
        mRenderer.setFeatures(mFeatures);
    }

    /**
     * Gets the default style for the LineString objects
     *
     * @return LineStringStyle containing default styles for LineString
     */
    public GeoJsonLineStringStyle getDefaultLineStringStyle() {
        return mDefaultLineStringStyle;
    }

    /**
     * Sets the default style for the LineString objects. Overrides all current styles on
     * LineString objects.
     *
     * @param geoJsonLineStringStyle to set as default style, this is applied to LineStrings as they
     *                               are
     *                               imported from the GeoJSON file
     */
    public void setDefaultLineStringStyle(GeoJsonLineStringStyle geoJsonLineStringStyle) {
        mDefaultLineStringStyle = geoJsonLineStringStyle;
        for (GeoJsonFeature geoJsonFeature : mFeatures.keySet()) {
            geoJsonFeature.setLineStringStyle(geoJsonLineStringStyle);
        }
        mRenderer.setFeatures(mFeatures);
    }

    /**
     * Gets the default style for the Polygon objects
     *
     * @return PolygonStyle containing default styles for Polygon
     */
    public GeoJsonPolygonStyle getDefaultPolygonStyle() {
        return mDefaultPolygonStyle;
    }

    /**
     * Sets the default style for the Polygon objects. Overrides all current styles on Polygon
     * objects.
     *
     * @param geoJsonPolygonStyle to set as default style, this is applied to Polygons as they are
     *                            imported from the GeoJSON file
     */
    public void setDefaultPolygonStyle(GeoJsonPolygonStyle geoJsonPolygonStyle) {
        mDefaultPolygonStyle = geoJsonPolygonStyle;
        for (GeoJsonFeature geoJsonFeature : mFeatures.keySet()) {
            geoJsonFeature.setPolygonStyle(geoJsonPolygonStyle);
        }
        mRenderer.setFeatures(mFeatures);
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
