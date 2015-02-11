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
 * A class that allows the developer to import GeoJSON data, style it and interact with the
 * imported data.
 *
 * To create a new GeoJsonLayer from a resource stored locally
 * {@code GeoJsonLayer layer = new GeoJsonLayer(getMap(), R.raw.resource,
 * getApplicationContext());}
 *
 * To render the imported GeoJSON data onto the layer
 * {@code layer.addLayerToMap();}
 *
 * To remove the rendered data from the layer
 * {@code layer.removeLayerFromMap();}
 */
public class GeoJsonLayer {

    private final GeoJsonRenderer mRenderer;

    private LatLngBounds mBoundingBox;

    /**
     * Creates a new GeoJsonLayer object
     *
     * @param map         map where the layer is to be rendered
     * @param geoJsonFile GeoJSON data to add to the layer
     */
    public GeoJsonLayer(GoogleMap map, JSONObject geoJsonFile) {
        if (geoJsonFile == null) {
            throw new IllegalArgumentException("GeoJSON file cannot be null");
        }

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
     * @param map        map where the layer is to be rendered
     * @param resourceId GeoJSON file to add to the layer
     * @param context    context of the application, required to open the GeoJSON file
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
     * @param stream character input stream representing the GeoJSON file
     * @return JSONObject with the GeoJSON data
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
     * Gets an iterable of all GeoJsonFeature elements that have been added to the layer
     *
     * @return iterable of GeoJsonFeature elements
     */
    public Iterable<GeoJsonFeature> getFeatures() {
        return mRenderer.getFeatures();
    }

    /**
     * Adds all the GeoJsonFeature objects parsed from the given GeoJSON data onto the map. Default
     * styles are applied if the features haven't been previously added to the layer or if the
     * layer has been cleared.
     */
    public void addLayerToMap() {
        mRenderer.addLayerToMap();
    }

    /**
     * Adds a GeoJsonFeature to the layer. If the point, linestring or polygon style is set to
     * null, the relevant default styles are applied.
     *
     * @param feature GeoJsonFeature to add to the layer
     */
    public void addFeature(GeoJsonFeature feature) {
        if (feature == null) {
            throw new IllegalArgumentException("Feature cannot be null");
        }
        mRenderer.addFeature(feature);
    }

    /**
     * Removes the given GeoJsonFeature from the layer
     *
     * @param feature feature to remove
     */
    public void removeFeature(GeoJsonFeature feature) {
        if (feature == null) {
            throw new IllegalArgumentException("Feature cannot be null");
        }
        mRenderer.removeFeature(feature);
    }

    /**
     * Gets the map on which the layer is rendered
     *
     * @return map on which the layer is rendered
     */
    public GoogleMap getMap() {
        return mRenderer.getMap();
    }

    /**
     * Renders the layer on the given map. The layer on the current map is removed and
     * added to the given map.
     *
     * @param map to render the layer on, if null the layer is cleared from the current map
     */
    public void setMap(GoogleMap map) {
        mRenderer.setMap(map);
    }

    /**
     * Clears all of the GeoJsonFeatures from the layer. All styles are removed from the
     * GeoJsonFeatures.
     */
    public void removeLayerFromMap() {
        mRenderer.removeLayerFromMap();
    }

    /**
     * Get whether the layer is on the map
     *
     * @return true if the layer is on the map, false otherwise
     */
    public boolean isLayerOnMap() {
        return mRenderer.isLayerOnMap();
    }

    /**
     * Gets the default style used to render GeoJsonPoints
     *
     * @return default style used to render GeoJsonPoints
     */
    public GeoJsonPointStyle getDefaultPointStyle() {
        return mRenderer.getDefaultPointStyle();
    }

    /**
     * Sets the default style to use when rendering GeoJsonPoints. This style is only applied to
     * GeoJsonPoints that are added to the layer after this method is called.
     *
     * @param pointStyle to set as the default style for GeoJsonPoints
     */
    public void setDefaultPointStyle(GeoJsonPointStyle pointStyle) {
        if (pointStyle == null) {
            throw new IllegalArgumentException("Default style cannot be null");
        }
        mRenderer.setDefaultPointStyle(pointStyle);
    }

    /**
     * Gets the default style used to render GeoJsonLineStrings
     *
     * @return default style used to render GeoJsonLineStrings
     */
    public GeoJsonLineStringStyle getDefaultLineStringStyle() {
        return mRenderer.getDefaultLineStringStyle();
    }

    /**
     * Sets the default style to use when rendering GeoJsonLineStrings. This style is only applied
     * to GeoJsonLineStrings that are added to the layer after this method is called.
     *
     * @param lineStringStyle to set as the default style for GeoJsonLineStrings
     */
    public void setDefaultLineStringStyle(GeoJsonLineStringStyle lineStringStyle) {
        if (lineStringStyle == null) {
            throw new IllegalArgumentException("Default style cannot be null");
        }
        mRenderer.setDefaultLineStringStyle(lineStringStyle);
    }

    /**
     * Gets the default style used to render GeoJsonPolygons
     *
     * @return default style used to render GeoJsonPolygons
     */
    public GeoJsonPolygonStyle getDefaultPolygonStyle() {
        return mRenderer.getDefaultPolygonStyle();
    }

    /**
     * Sets the default style to use when rendering GeoJsonPolygons. This style is only applied to
     * GeoJsonPolygons that are added to the layer after this method is called.
     *
     * @param polygonStyle to set as the default style for GeoJsonPolygons
     */
    public void setDefaultPolygonStyle(GeoJsonPolygonStyle polygonStyle) {
        if (polygonStyle == null) {
            throw new IllegalArgumentException("Default style cannot be null");
        }
        mRenderer.setDefaultPolygonStyle(polygonStyle);
    }

    /**
     * Gets the LatLngBounds containing the coordinates of the bounding box for the
     * FeatureCollection. If the FeatureCollection did not have a bounding box or if the GeoJSON
     * file did not contain a FeatureCollection then null will be returned.
     *
     * @return LatLngBounds containing bounding box of FeatureCollection, null if no bounding box
     */
    public LatLngBounds getBoundingBox() {
        return mBoundingBox;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("Collection{");
        sb.append("\n Bounding box=").append(mBoundingBox);
        sb.append("\n}\n");
        return sb.toString();
    }
}
