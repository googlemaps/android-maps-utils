package com.google.maps.android.geoJsonLayer;

/**
 * Created by juliawong on 12/30/14.
 */

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

/**
 * Parse a JSONObject and return an array of Feature objects
 */
public class GeoJsonParser {
    private ArrayList<Feature> mFeatures;

    private final static String FEATURE_COLLECTION = "FeatureCollection";

    private final static String FEATURE = "Feature";

    private final JSONObject mGeoJsonFile;

    public GeoJsonParser(JSONObject geoJsonFile) throws JSONException {
        mGeoJsonFile = geoJsonFile;
        String type = mGeoJsonFile.getString("type");

        // Decide how to parse the object

    }

    private void parseFeatureCollection(JSONObject geoJsonFile) throws JSONException {
        // FC is an array of features
        JSONArray featureCollectionArray = geoJsonFile.getJSONArray("features");
        for (int i = 0; i < featureCollectionArray.length(); i++) {
            JSONObject feature = featureCollectionArray.getJSONObject(i);

            if (feature.getString("type").equals(FEATURE)) {
                parseFeature(feature);
            }
        }
    }

    private void parseFeature(JSONObject geoJsonFile) throws JSONException {
        // TODO if the geometry is null don't add it to the map
        // Id is optional for a feature
        String id = null;
        if (geoJsonFile.has("id")) {
            id = geoJsonFile.getString("id");
        }
        String geometryType = geoJsonFile.getJSONObject("geometry").getString("type");
        JSONObject properties = geoJsonFile.getJSONObject("properties");

        if (!geometryType.equals(FEATURE_COLLECTION)) {
            JSONArray coordinates = geoJsonFile.getJSONArray("coordinates");
            createFeature(id, geometryType, properties, coordinates);
        }
    }

    private void createFeature(String id, String geometryType, JSONObject properties,
            JSONArray coordinates)
            throws JSONException {
        HashMap<String, String> propertiesMap = parseProperties(properties);
        Geometry geometry = null;
        if (geometryType.equals("Point")) {
            geometry = createPoint(coordinates);
        }
        else if (geometryType.equals("MultiPoint")) {
            geometry = createMultiPoint(coordinates);
        }
        else if (geometryType.equals("LineString")) {
            geometry = createLineString(coordinates);
        }
        else if (geometryType.equals("MultiLineString")) {
            geometry = createMultiLineString(coordinates);
        }
        else if (geometryType.equals("Polygon")) {
            geometry = createPolygon(coordinates);
        }
        else if (geometryType.equals("MultiPolygon")) {
            geometry = createMultiPolygon(coordinates);
        }

        if (geometry != null) {
            mFeatures.add(new Feature(id, geometry, propertiesMap));
        }
    }

    private Point createPoint(JSONArray coordinates) throws JSONException {
        return new Point(parseCoordinate(coordinates));
    }

    private MultiPoint createMultiPoint(JSONArray coordinates) throws JSONException {
        ArrayList<Point> points = new ArrayList<Point>();
        for (int i = 0; i < coordinates.length(); i++) {
            points.add(createPoint(coordinates.getJSONArray(i)));
        }
        return new MultiPoint(points);
    }

    private LineString createLineString(JSONArray coordinates) throws JSONException {
        return new LineString(parseCoordinatesArray(coordinates));
    }

    private MultiLineString createMultiLineString(JSONArray coordinates) throws JSONException {
        ArrayList<LineString> lineStrings = new ArrayList<LineString>();
        for (int i = 0; i < coordinates.length(); i++) {
            lineStrings.add(createLineString(coordinates.getJSONArray(i)));
        }
        return new MultiLineString(lineStrings);
    }

    private Polygon createPolygon(JSONArray coordinates) throws JSONException {
        return new Polygon(parseCoordinatesArrays(coordinates));
    }

    private MultiPolygon createMultiPolygon(JSONArray coordinates) throws JSONException {
        ArrayList<Polygon> polygons = new ArrayList<Polygon>();
        for (int i = 0; i < coordinates.length(); i++) {
            polygons.add(createPolygon(coordinates.getJSONArray(i)));
        }
        return new MultiPolygon(polygons);
    }

    private LatLng parseCoordinate(JSONArray coordinates) throws JSONException {
        // GeoJSON stores coordinates as Lng, Lat so we need to reverse
        return new LatLng(coordinates.getDouble(1), coordinates.getDouble(0));
    }

    private ArrayList<LatLng> parseCoordinatesArray(JSONArray coordinates) throws JSONException {
        ArrayList<LatLng> coordinatesArray = new ArrayList<LatLng>();

        for (int i = 0; i < coordinates.length(); i++) {
            coordinatesArray.add(parseCoordinate(coordinates.getJSONArray(i)));
        }
        return coordinatesArray;
    }

    private ArrayList<ArrayList<LatLng>> parseCoordinatesArrays(JSONArray coordinates) throws JSONException {
        ArrayList<ArrayList<LatLng>> coordinatesArray = new ArrayList<ArrayList<LatLng>>();

        for (int i = 0; i < coordinates.length(); i++) {
            coordinatesArray.add(parseCoordinatesArray(coordinates.getJSONArray(i)));
        }
        return coordinatesArray;
    }

    private HashMap<String, String> parseProperties(JSONObject properties) throws JSONException {
        HashMap<String, String> propertiesMap = new HashMap<String, String>();
        Iterator propertyKeys = properties.keys();
        while (propertyKeys.hasNext()) {
            String key = (String) propertyKeys.next();
            propertiesMap.put(key, properties.getString(key));
        }
        return propertiesMap;
    }

    public ArrayList<Feature> getFeatures() {
        return mFeatures;
    }
}
