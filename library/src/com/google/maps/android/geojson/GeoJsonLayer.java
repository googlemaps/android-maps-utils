package com.google.maps.android.geojson;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLngBounds;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;


/**
 * A class that allows the developer import GeoJSON data, style it and interact with the imported
 * data.
 */
public class GeoJsonLayer {

    private final GeoJsonRenderer mRenderer;

    private GeoJsonPointStyle mDefaultPointStyle;

    private GeoJsonLineStringStyle mDefaultLineStringStyle;

    private GeoJsonPolygonStyle mDefaultPolygonStyle;

    private LatLngBounds mBoundingBox;

    /**
     * Creates a new GeoJsonLayer object
     *
     * @param map         GoogleMap object
     * @param geoJsonFile JSONObject to parse GeoJSON data from
     */
    public GeoJsonLayer(GoogleMap map, JSONObject geoJsonFile) {
        mDefaultPointStyle = new GeoJsonPointStyle();
        mDefaultLineStringStyle = new GeoJsonLineStringStyle();
        mDefaultPolygonStyle = new GeoJsonPolygonStyle();
        mBoundingBox = null;
        GeoJsonParser parser = new GeoJsonParser(geoJsonFile);
        // Assign GeoJSON bounding box for FeatureCollection
        mBoundingBox = parser.getBoundingBox();
        HashMap<GeoJsonFeature, Object> geoJsonFeatures = new HashMap<GeoJsonFeature, Object>();
        for (GeoJsonFeature feature : parser.getFeatures()) {
            geoJsonFeatures.put(feature, null);
        }
        mRenderer = new GeoJsonRenderer(map, geoJsonFeatures);
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
     * @throws IOException   if the file cannot be opened for read
     * @throws JSONException if the JSON file has poor structure
     */
    private static JSONObject createJsonFileObject(InputStream stream)
            throws IOException, JSONException {
        String line;
        StringBuilder result = new StringBuilder();
        // Reads from stream
        BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
        try {
            // Read each line of the GeoJSON file into a string
            while ((line = reader.readLine()) != null) {
                result.append(line);
            }
        } finally {
            reader.close();
        }
        // Converts the result string into a JSONObject
        return new JSONObject(result.toString());
    }

    /**
     * Gets an iterable of all GeoJsonFeature elements that appear on the map
     *
     * @return iterable of GeoJsonFeature elements
     */
    public Iterable<GeoJsonFeature> getFeatures() {
        return mRenderer.getFeatures();
    }

    /**
     * Gets the GeoJsonFeature with the given ID if it exists
     *
     * @param id of GeoJsonFeature object to find
     * @return GeoJsonFeature object with matching ID or otherwise null
     */
    public GeoJsonFeature getFeatureById(String id) {
        for (GeoJsonFeature feature : getFeatures()) {
            if (feature.getId() != null && feature.getId().equals(id)) {
                return feature;
            }
        }
        return null;
    }

    /**
     * Adds all the GeoJsonFeature objects parsed from the GeoJSON document onto the map. Default
     * styles are applied to all features each time this method is called.
     */
    public void addDataToLayer() {
        for (GeoJsonFeature feature : mRenderer.getFeatures()) {
            addFeature(feature);
        }
    }

    /**
     * Adds a GeoJsonFeature to the GeoJsonLayer
     *
     * @param feature GeoJsonFeature to add
     */
    public void addFeature(GeoJsonFeature feature) {
        // Set default styles
        feature.setPointStyle(mDefaultPointStyle);
        feature.setLineStringStyle(mDefaultLineStringStyle);
        feature.setPolygonStyle(mDefaultPolygonStyle);
        mRenderer.addFeature(feature);
    }

    /**
     * Removes a GeoJsonFeature from the GeoJsonLayer
     *
     * @param feature GeoJsonFeature to remove
     */
    public void removeFeature(GeoJsonFeature feature) {
        mRenderer.removeFeature(feature);
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
     * Removes all of the stored GeoJsonFeature objects from the map and clears the mFeatures
     * hashmap
     */
    public void removeLayer() {
        mRenderer.removeLayerFromMap();
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
     * Sets the default style for the GeoJsonPoint objects. This style is only applied to
     * GeoJsonFeature objects that are added after this method is called.
     *
     * @param geoJsonPointStyle to set as default style
     */
    public void setDefaultPointStyle(GeoJsonPointStyle geoJsonPointStyle) {
        mDefaultPointStyle = geoJsonPointStyle;
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
     * Sets the default style for the GeoJsonLineString objects. This style is only applied to
     * GeoJsonFeature objects that are added after this method is called.
     *
     * @param geoJsonLineStringStyle to set as default style
     */
    public void setDefaultLineStringStyle(GeoJsonLineStringStyle geoJsonLineStringStyle) {
        mDefaultLineStringStyle = geoJsonLineStringStyle;
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
     * Sets the default style for the GeoJsonPolygon objects. This style is only applied to
     * GeoJsonFeature objects that are added after this method is called.
     *
     * @param geoJsonPolygonStyle to set as default style
     */
    public void setDefaultPolygonStyle(GeoJsonPolygonStyle geoJsonPolygonStyle) {
        mDefaultPolygonStyle = geoJsonPolygonStyle;
    }

    /**
     * Gets the array containing the coordinates of the bounding box for the FeatureCollection. If
     * the FeatureCollection did not have a bounding box or if the GeoJSON file did not contain a
     * FeatureCollection then null will be returned.
     *
     * @return LatLngBounds containing bounding box of FeatureCollection, null if no bounding box
     */
    public LatLngBounds getBoundingBox() {
        return mBoundingBox;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("Collection{");
        sb.append(",\n Point style=").append(mDefaultPointStyle);
        sb.append(",\n LineString style=").append(mDefaultLineStringStyle);
        sb.append(",\n Polygon style=").append(mDefaultPolygonStyle);
        sb.append("\n}\n");
        return sb.toString();
    }
}
