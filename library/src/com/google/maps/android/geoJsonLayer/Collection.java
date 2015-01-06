package com.google.maps.android.geoJsonLayer;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import com.google.maps.android.geoJsonLayer.Geometry;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

/**
 * Created by juliawong on 12/29/14.
 *
 * Adds a new layer of GeoJSON data with methods to allow the developer to interact with it.
 */
public class Collection {

    private final HashMap<Feature, Object> mFeatures;

    private GoogleMap mMap;

    private PointStyle mDefaultPointStyle;

    private LineStringStyle mDefaultLineStringStyle;

    private PolygonStyle mDefaultPolygonStyle;

    private JSONObject mGeoJsonFile;

    private float mZIndex;

    // TODO close streams

    /**
     * Creates a new Collection object
     *
     * @param map           GoogleMap object
     * @param geoJsonObject JSONObject to parse GeoJSON data from
     */
    public Collection(GoogleMap map, JSONObject geoJsonObject) {
        mMap = map;
        mFeatures = new HashMap<Feature, Object>();
        mDefaultPointStyle = new PointStyle();
        mDefaultLineStringStyle = new LineStringStyle();
        mDefaultPolygonStyle = new PolygonStyle();
        mGeoJsonFile = geoJsonObject;
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
        mFeatures = new HashMap<Feature, Object>();
        mDefaultPointStyle = new PointStyle();
        mDefaultLineStringStyle = new LineStringStyle();
        mDefaultPolygonStyle = new PolygonStyle();
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


    /**
     * Gets an iterator of all feature elements.
     *
     * @return iterator of feature elements
     */
    public  Set<Feature> getFeatures() {
        return mFeatures.keySet();
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
     * Gets the default style for the Point objects
     *
     * @return PointStyle containing default styles for Point
     */
    public PointStyle getDefaultPointStyle() {
        return mDefaultPointStyle;
    }

    /**
     * Sets the default style for the Point objects. Overrides all current styles on Point objects.
     *
     * @param pointStyle to set as default style, this is applied to Points as they are imported
     *                   from the GeoJSON file
     */
    public void setDefaultPointStyle(PointStyle pointStyle) {
        mDefaultPointStyle = pointStyle;
        for (Feature feature : mFeatures.keySet()) {
            feature.setPointStyle(pointStyle);
        }
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
     * Sets the default style for the LineString objects. Overrides all current styles on
     * LineString objects.
     *
     * @param lineStringStyle to set as default style, this is applied to LineStrings as they are
     *                        imported from the GeoJSON file
     */
    public void setDefaultLineStringStyle(LineStringStyle lineStringStyle) {
        mDefaultLineStringStyle = lineStringStyle;
        for (Feature feature : mFeatures.keySet()) {
            feature.setLineStringStyle(lineStringStyle);
        }
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
     * Sets the default style for the Polygon objects. Overrides all current styles on Polygon
     * objects.
     *
     * @param polygonStyle to set as default style, this is applied to Polygons as they are
     *                     imported from the GeoJSON file
     */
    public void setDefaultPolygonStyle(PolygonStyle polygonStyle) {
        mDefaultPolygonStyle = polygonStyle;
        for (Feature feature : mFeatures.keySet()) {
            feature.setPolygonStyle(polygonStyle);
        }
    }

    /**
     * Converts a GeoJson file to a parser and puts found features in set
     * @throws JSONException
     */
    public void parseGeoJson() throws JSONException {
        GeoJsonParser parser = new GeoJsonParser(mGeoJsonFile);
        parser.parseGeoJson();

        for (Feature feature : parser.getFeatures()) {
            mFeatures.put(feature, null);
        }
    }

    /**
     * Adds all features which are available in this collection to the map
     */
    public void addCollectionToMap() {
        for (Feature feature : mFeatures.keySet()) {
            //geometry: "null", is valid - need to check beforehand before we add
            if (feature.hasGeometry()) {
                addToMap(feature, feature.getGeometry());
            }
        }
    }

    /**
     * Adds a Point to the map as a Marker
     *
     * @param pointStyle contains relevant styling properties for the Marker
     * @param point      contains coordinates for the Marker
     * @return Marker object created from the given point feature
     */
    private Marker addPointToMap(PointStyle pointStyle, Point point) {
        MarkerOptions markerOptions = pointStyle.getMarkerOptions();
        markerOptions.position(point.getCoordinates());
        return mMap.addMarker(markerOptions);
    }

    /**
     * Adds all objects currently stored in the mFeature array, onto the map
     *
     * @param feature feature to get geometry style
     * @param geometry Geometry to add to the map
     */
    private void addToMap(Feature feature, Geometry geometry) {
        String geometryType = geometry.getType();
        if (geometryType.equals("Point")) {
            mFeatures.put(feature,
                    addPointToMap(feature.getPointStyle(), (Point) geometry));
        } else if (geometryType.equals("LineString")) {
            mFeatures.put(feature, addLineStringToMap(feature.getLineStringStyle(),
                    (LineString) geometry));
        } else if (geometryType.equals("Polygon")) {
            mFeatures.put(feature, addPolygonToMap(feature.getPolygonStyle(),
                    (Polygon) geometry));
        } else if (geometryType.equals("MultiPoint")) {
            mFeatures.put(feature, addMultiPointToMap(feature));
        } else if (geometryType.equals("MultiLineString")) {
            mFeatures.put(feature, addMultiLineStringToMap(feature));
        } else if (geometryType.equals("MultiPolygon")) {
            mFeatures.put(feature, addMultiPolygonToMap(feature));
        } else if(geometryType.equals("GeometryCollection")) {
            GeometryCollection geometryCollection = ((GeometryCollection) feature.getGeometry());
            for (Geometry geometryObject: geometryCollection.getGeometries()) {
                addToMap(feature, geometryObject);
            }
        }
    }

    /**
     * Adds all Points in MultiPoint to the map as multiple Markers
     *
     * @param feature contains MultiPoint and relevant style properties
     * @return array of Markers that have been added to the map
     */
    private ArrayList<Marker> addMultiPointToMap(Feature feature) {
        ArrayList<Marker> markers = new ArrayList<Marker>();
        for (Point point : ((MultiPoint) feature.getGeometry()).getPoints()) {
            markers.add(addPointToMap(feature.getPointStyle(), point));
        }
        return markers;
    }

    /**
     * Adds a LineString to the map as a Polyline
     *
     * @param lineStringStyle contains relevant styling properties for the Polyline
     * @param lineString      contains coordinates for the Polyline
     * @return Polyline object created from given feature
     */
    private Polyline addLineStringToMap(LineStringStyle lineStringStyle,
            LineString lineString) {
        PolylineOptions polylineOptions = lineStringStyle.getPolylineOptions();
        // Add coordinates
        polylineOptions.addAll(lineString.getCoordinates());
        return mMap.addPolyline(polylineOptions);
    }

    /**
     * Adds a GeoJSON Polygon to the map as a Polygon
     *
     * @param polygonStyle contains relevant styling properties for the Polygon
     * @param polygon      contains coordinates for the Polygon
     * @return Polygon object created from given feature
     */
    private com.google.android.gms.maps.model.Polygon addPolygonToMap(PolygonStyle polygonStyle,
            Polygon polygon) {
        PolygonOptions polygonOptions = polygonStyle.getPolygonOptions();
        // First array of coordinates are the outline
        polygonOptions.addAll(polygon.getCoordinates().get(0));
        // Following arrays are holes
        for (int i = 1; i < polygon.getCoordinates().size(); i++) {
            polygonOptions.addHole(polygon.getCoordinates().get(i));
        }
        return mMap.addPolygon(polygonOptions);
    }



    /**
     * Adds all LineStrings in the MultiLineString to the map as multiple Polylines
     *
     * @param feature contains MultiLineString and relevant style properties
     * @return array of Polylines that have been added to the map
     */
    private ArrayList<Polyline> addMultiLineStringToMap(Feature feature) {
        ArrayList<Polyline> polylines = new ArrayList<Polyline>();
        for (LineString lineString : ((MultiLineString) feature.getGeometry()).getLineStrings()) {
            polylines.add(addLineStringToMap(feature.getLineStringStyle(),
                    lineString));
        }
        return polylines;
    }

    /**
     * Adds all GeoJSON Polygons in the MultiPolygon to the map as multiple Polygons
     *
     * @param feature contains MultiPolygon and relevant style properties
     * @return array of Polygons that have been added to the map
     */
    private ArrayList<com.google.android.gms.maps.model.Polygon> addMultiPolygonToMap(
            Feature feature) {
        ArrayList<com.google.android.gms.maps.model.Polygon> polygons
                = new ArrayList<com.google.android.gms.maps.model.Polygon>();
        for (Polygon polygon : ((MultiPolygon) feature.getGeometry()).getPolygons()) {
            polygons.add(addPolygonToMap(feature.getPolygonStyle(), polygon));
        }
        return polygons;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("Collection{");
        sb.append("\n features=").append(mFeatures);
        sb.append(",\n Point style=").append(mDefaultPointStyle);
        sb.append(",\n LineString style=").append(mDefaultLineStringStyle);
        sb.append(",\n Polygon style=").append(mDefaultPolygonStyle);
        sb.append(",\n z index=").append(mZIndex);
        sb.append("\n}\n");
        return sb.toString();
    }
}
