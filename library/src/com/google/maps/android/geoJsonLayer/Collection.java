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

    private JSONObject mGeoJsonFile;

    // TODO close streams

    /**
     * Creates a new Collection object
     *
     * @param map           GoogleMap object
     * @param geoJsonObject JSONObject to parse GeoJSON data from
     * @throws IOException   if the file cannot be opened for read
     * @throws JSONException if the JSON file has invalid syntax and cannot be parsed successfully
     */
    public Collection(GoogleMap map, JSONObject geoJsonObject) {
        mMap = map;
        mFeatures = new ArrayList<Feature>();
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
    public Collection(GoogleMap map, int resourceId, Context context)
            throws IOException, JSONException {
        mMap = map;
        mFeatures = new ArrayList<Feature>();
        InputStream stream = context.getResources().openRawResource(resourceId);
        mGeoJsonFile = createJsonFileObject(stream);
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

    public void parseGeoJson() throws JSONException {
        GeoJsonParser parser = new GeoJsonParser(mGeoJsonFile);
        parser.parseGeoJson();
        mFeatures = parser.getFeatures();
    }

    // TODO: implement an iterator thing or just return mFeatures

    /**
     * Returns the features with the given ID if it exists
     *
     * @param id of Feature object to find
     * @return Feature object with matching ID or otherwise null
     */
    public Feature getFeatureById(String id) {
        for (Feature feature : mFeatures) {
            if (feature.getId().equals(id)) {
                return feature;
            }
        }
        return null;
    }

    /**
     * Returns the map on which the features are displayed
     *
     * @return map on which the features are displayed
     */
    public GoogleMap getMap() {
        return mMap;
    }

    /**
     * Renders features on the specified map
     *
     * @param map to render features on, if null all features are cleared from the map
     */
    public void setMap(GoogleMap map) {
        mMap = map;
    }

    /**
     * Removes a feature from the collection
     *
     * @param feature to remove
     */
    public void removeFeature(Feature feature) {
        mFeatures.remove(feature);
    }

    /**
     * Gets the default style for the Point objects
     *
     * @return PointStyle containing default styles for Point
     */
    public PointStyle getDefaultPointStyle() {
        return mDefaultPointStyle;
    }

    /**
     * Sets the default style for the Point objects
     *
     * @param pointStyle to set as default style, this is applied to Points as they are imported
     *                   from the GeoJSON file
     */
    public void setDefaultPointStyle(PointStyle pointStyle) {
        mDefaultPointStyle = pointStyle;
    }

    /**
     * Gets the default style for the LineString objects
     *
     * @return LineStringStyle containing default styles for LineString
     */
    public LineStringStyle getDefaultLineStringStyle() {
        return mDefaultLineStringStyle;
    }

    /**
     * Sets the default style for the LineString objects
     *
     * @param lineStringStyle to set as default style, this is applied to LineStrings as they are
     *                        imported from the GeoJSON file
     */
    public void setDefaultLineStringStyle(LineStringStyle lineStringStyle) {
        mDefaultLineStringStyle = lineStringStyle;
    }

    /**
     * Gets the default style for the Polygon objects
     *
     * @return PolygonStyle containing default styles for Polygon
     */
    public PolygonStyle getDefaultPolygonStyle() {
        return mDefaultPolygonStyle;
    }

    /**
     * Sets the default style for the Polygon objects
     *
     * @param polygonStyle to set as default style, this is applied to Polygons as they are
     *                     imported
     *                     from the GeoJSON file
     */
    public void setDefaultPolygonStyle(PolygonStyle polygonStyle) {
        mDefaultPolygonStyle = polygonStyle;
    }

    /**
     * Set the input map to be centred and zoomed to the bounding box of the set of data
     *
     * @param preserveViewPort if true, map's centre and zoom are changed, if false viewport is
     *                         left
     *                         unchanged
     */
    public void setPreserveViewPort(boolean preserveViewPort) {

    }

    /**
     * Sets the z index of the imported layer, only applies to LineStrings and Polygons, Points are
     * on top of all objects
     *
     * @param zIndex stack order for data on this layer, excludes Points
     */
    public void setZIndex(float zIndex) {
        mDefaultLineStringStyle.setZIndex(zIndex);
        mDefaultPolygonStyle.setZIndex(zIndex);
        // TODO: redraw objects
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("Collection{");
        sb.append("\n features=").append(mFeatures);
        sb.append(",\n Point style=").append(mDefaultPointStyle);
        sb.append(",\n LineString style=").append(mDefaultLineStringStyle);
        sb.append(",\n Polygon style=").append(mDefaultPolygonStyle);
        sb.append(",\n GeoJson file=").append(mGeoJsonFile);
        sb.append("\n}\n");
        return sb.toString();
    }
}
