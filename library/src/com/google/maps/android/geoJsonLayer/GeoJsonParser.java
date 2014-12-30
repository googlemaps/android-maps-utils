package com.google.maps.android.geoJsonLayer;

/**
 * Created by juliawong on 12/30/14.
 */

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * Parse a JSONObject and return an array of Feature objects
 */
public class GeoJsonParser {

    private final static String FEATURE_COLLECTION = "FeatureCollection";

    private final static String FEATURE = "Feature";

    private final JSONObject mGeoJsonFile;

    private ArrayList<Feature> mFeatures;

    public GeoJsonParser(JSONObject geoJsonFile) throws JSONException {
        mGeoJsonFile = geoJsonFile;
        String type = mGeoJsonFile.getString("type");

        // Decide how to parse the object
        // If parsing geom, you need to create the feature

    }

    private ArrayList<Feature> parseFeatureCollection(JSONObject geoJsonFile) throws JSONException {
        // FC is an array of features
        ArrayList<Feature> features = new ArrayList<Feature>();
        JSONArray featureCollectionArray = geoJsonFile.getJSONArray("features");
        for (int i = 0; i < featureCollectionArray.length(); i++) {
            JSONObject feature = featureCollectionArray.getJSONObject(i);

            if (feature.getString("type").equals(FEATURE)) {
                features.add(parseFeature(feature));
            }
        }
        return features;
    }

    private Feature parseFeature(JSONObject geoJsonFile) throws JSONException {
        // TODO if the geometry is null don't add it to the map
        String id = null;
        Feature feature;
        feature = parseGeometry(geoJsonFile.getJSONObject("geometry"));
        // Id is optional for a feature
        if (geoJsonFile.has("id")) {
            feature.setId(geoJsonFile.getString("id"));
        }
        JSONObject properties = geoJsonFile.getJSONObject("properties");
        parseProperties(feature, properties);
        return feature;
    }

    private Feature parseGeometry(JSONObject geoJsonFile) throws JSONException {
        String geometryType = geoJsonFile.getString("type");
        JSONArray coordinates = geoJsonFile.getJSONArray("coordinates");
        Geometry geometry = createGeometry(geometryType, coordinates);
        return new Feature(geometry);
    }

    private void parseProperties(Feature feature, JSONObject properties) throws JSONException {
        Iterator propertyKeys = properties.keys();
        while (propertyKeys.hasNext()) {
            String key = (String) propertyKeys.next();
            feature.setProperty(key, properties.getString(key));
        }
    }

    private Geometry createGeometry(String geometryType, JSONArray coordinates)
            throws JSONException {
        if (geometryType.equals("Point")) {
            return createPoint(coordinates);
        } else if (geometryType.equals("MultiPoint")) {
            return createMultiPoint(coordinates);
        } else if (geometryType.equals("LineString")) {
            return createLineString(coordinates);
        } else if (geometryType.equals("MultiLineString")) {
            return createMultiLineString(coordinates);
        } else if (geometryType.equals("Polygon")) {
            return createPolygon(coordinates);
        } else if (geometryType.equals("MultiPolygon")) {
            return createMultiPolygon(coordinates);
        }

        return null;
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

    private ArrayList<ArrayList<LatLng>> parseCoordinatesArrays(JSONArray coordinates)
            throws JSONException {
        ArrayList<ArrayList<LatLng>> coordinatesArray = new ArrayList<ArrayList<LatLng>>();

        for (int i = 0; i < coordinates.length(); i++) {
            coordinatesArray.add(parseCoordinatesArray(coordinates.getJSONArray(i)));
        }
        return coordinatesArray;
    }

    public ArrayList<Feature> getFeatures() {
        return mFeatures;
    }
}
