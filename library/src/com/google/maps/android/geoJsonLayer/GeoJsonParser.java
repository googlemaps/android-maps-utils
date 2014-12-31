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

    private final static String FEATURE = "Feature";

    private final static String FEATURE_COLLECTION = "FeatureCollection";

    private final JSONObject mGeoJsonFile;

    private ArrayList<Feature> mFeatures;

    /**
     * Creates a new GeoJsonParser
     *
     * @param geoJsonFile GeoJSON file to parse
     */
    public GeoJsonParser(JSONObject geoJsonFile) {
        mGeoJsonFile = geoJsonFile;
        mFeatures = new ArrayList<Feature>();
    }

    /**
     * Parses the GeoJSON file and adds the generated Feature objects to the mFeatures array
     *
     * @throws JSONException if the GeoJSON file could not be parsed
     */
    public void parseGeoJson() throws JSONException {
        // TODO: add default styles
        String type = mGeoJsonFile.getString("type");
        if (type.equals(FEATURE)) {
            mFeatures.add(parseFeature(mGeoJsonFile));
        } else if (type.equals(FEATURE_COLLECTION)) {
            mFeatures.addAll(parseFeatureCollection(mGeoJsonFile.getJSONArray("features")));
        }
        // TODO: figure out GeometryCollection and its styles
    }

    /**
     * Parses a GeoJSON feature collection which contains an array of features
     *
     * @param geoJsonFeatures array of features from the GeoJSON feature collection
     * @return array of Feature objects parsed from the given array
     * @throws JSONException if the feature collection could not be parsed
     */
    private ArrayList<Feature> parseFeatureCollection(JSONArray geoJsonFeatures)
            throws JSONException {
        // FC is an array of features
        ArrayList<Feature> features = new ArrayList<Feature>();
        for (int i = 0; i < geoJsonFeatures.length(); i++) {
            JSONObject feature = geoJsonFeatures.getJSONObject(i);

            if (feature.getString("type").equals(FEATURE)) {
                features.add(parseFeature(feature));
            }
        }
        return features;
    }

    /**
     * Parses a single GeoJSON feature which contains a geometry and properties member both of which
     * can be null and optionally an id. If the geometry member has a null value, we do not add the
     * geometry to the array.
     *
     * @param geoJsonFeature GeoJSON feature to parse
     * @return Feature object parsed from the given GeoJSON feature
     * @throws JSONException if the feature does not have members geometry and properties or could
     *                       not be parsed for some other reason
     */
    private Feature parseFeature(JSONObject geoJsonFeature) throws JSONException {
        // TODO: if the geometry is null don't add it to the map
        String id = null;
        Geometry geometry;
        Feature feature;
        geometry = parseGeometry(geoJsonFeature.getJSONObject("geometry"));
        // Id is optional for a feature
        if (geoJsonFeature.has("id")) {
            id = geoJsonFeature.getString("id");
        }
        JSONObject properties = geoJsonFeature.getJSONObject("properties");
        feature = new Feature(geometry, id, parseProperties(properties));
        return feature;
    }

    /**
     * Parses a single GeoJSON geometry object containing a coordinates or geometries array if it
     * has type GeometryCollection
     *
     * @param geoJsonGeometry GeoJSON geometry object to parse
     * @return Geometry object parsed from the given GeoJSON geometry object
     * @throws JSONException if the geometry does not have a coordinates or geometries array or
     *                       could not be parsed for some other reason
     */
    private Geometry parseGeometry(JSONObject geoJsonGeometry) throws JSONException {
        String geometryType = geoJsonGeometry.getString("type");
        JSONArray coordinates = geoJsonGeometry.getJSONArray("coordinates");
        return createGeometry(geometryType, coordinates);
    }

    /**
     * Converts a geometry object to a feature object. A geometry object has no ID or properties so
     * it is set to null.
     *
     * @param geometry Geometry object to convert into a Feature object
     * @return new Feature object
     */
    private Feature geometrytoFeature(Geometry geometry) {
        return new Feature(geometry, null, null);
    }

    /**
     * Parses the properties of a GeoJSON feature into a hashmap
     *
     * @param properties GeoJSON properties member
     * @return hashmap containing property values
     * @throws JSONException if the properties could not be parsed
     */
    private HashMap<String, String> parseProperties(JSONObject properties) throws JSONException {
        HashMap<String, String> propertiesMap = new HashMap<String, String>();
        Iterator propertyKeys = properties.keys();
        while (propertyKeys.hasNext()) {
            String key = (String) propertyKeys.next();
            propertiesMap.put(key, properties.getString(key));
        }
        return propertiesMap;
    }

    /**
     * Creates a Geometry object from the given type of geometry and its coordinates
     *
     * @param geometryType type of geometry
     * @param coordinates  coordinates of the geometry to parse and add to the Geometry object
     * @return Geometry object of type geometryType and containing the given coordinates
     * @throws JSONException if the coordinates could be parsed
     */
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

    /**
     * Creates a new Point object
     *
     * @param coordinates array containing the GeoJSON coordinates
     * @return Point object
     * @throws JSONException if coordinates cannot be parsed
     */
    private Point createPoint(JSONArray coordinates) throws JSONException {
        return new Point(parseCoordinate(coordinates));
    }

    /**
     * Creates a new MultiPoint object containing an array of Point objects
     *
     * @param coordinates array containing the GeoJSON coordinates
     * @return MultiPoint object
     * @throws JSONException if coordinates cannot be parsed
     */
    private MultiPoint createMultiPoint(JSONArray coordinates) throws JSONException {
        ArrayList<Point> points = new ArrayList<Point>();
        for (int i = 0; i < coordinates.length(); i++) {
            points.add(createPoint(coordinates.getJSONArray(i)));
        }
        return new MultiPoint(points);
    }

    /**
     * Creates a new LineString object
     *
     * @param coordinates array containing the GeoJSON coordinates
     * @return LineString object
     * @throws JSONException if coordinates cannot be parsed
     */
    private LineString createLineString(JSONArray coordinates) throws JSONException {
        return new LineString(parseCoordinatesArray(coordinates));
    }

    /**
     * Creates a new MultiLineString object containing an array of LineString objects
     *
     * @param coordinates array containing the GeoJSON coordinates
     * @return MultiLineString object
     * @throws JSONException if coordinates cannot be parsed
     */
    private MultiLineString createMultiLineString(JSONArray coordinates) throws JSONException {
        ArrayList<LineString> lineStrings = new ArrayList<LineString>();
        for (int i = 0; i < coordinates.length(); i++) {
            lineStrings.add(createLineString(coordinates.getJSONArray(i)));
        }
        return new MultiLineString(lineStrings);
    }

    /**
     * Creates a new Polygon object
     *
     * @param coordinates array containing the GeoJSON coordinates
     * @return Polygon object
     * @throws JSONException if coordinates cannot be parsed
     */
    private Polygon createPolygon(JSONArray coordinates) throws JSONException {
        return new Polygon(parseCoordinatesArrays(coordinates));
    }

    /**
     * Creates a new MultiPolygon object containing an array of Polygon objects
     *
     * @param coordinates array containing the GeoJSON coordinates
     * @return MultiPolygon object
     * @throws JSONException if coordinates cannot be parsed
     */
    private MultiPolygon createMultiPolygon(JSONArray coordinates) throws JSONException {
        ArrayList<Polygon> polygons = new ArrayList<Polygon>();
        for (int i = 0; i < coordinates.length(); i++) {
            polygons.add(createPolygon(coordinates.getJSONArray(i)));
        }
        return new MultiPolygon(polygons);
    }

    /**
     * Parses an array containing a coordinate into a LatLng object
     *
     * @param coordinates array containing the GeoJSON coordinate
     * @return LatLng object
     * @throws JSONException if coordinates cannot be parsed
     */
    private LatLng parseCoordinate(JSONArray coordinates) throws JSONException {
        // GeoJSON stores coordinates as Lng, Lat so we need to reverse
        return new LatLng(coordinates.getDouble(1), coordinates.getDouble(0));
    }

    /**
     * Parses an array containing coordinates into an ArrayList of LatLng objects
     *
     * @param coordinates array containing the GeoJSON coordinates
     * @return ArrayList of LatLng objects
     * @throws JSONException if coordinates cannot be parsed
     */
    private ArrayList<LatLng> parseCoordinatesArray(JSONArray coordinates) throws JSONException {
        ArrayList<LatLng> coordinatesArray = new ArrayList<LatLng>();

        for (int i = 0; i < coordinates.length(); i++) {
            coordinatesArray.add(parseCoordinate(coordinates.getJSONArray(i)));
        }
        return coordinatesArray;
    }

    /**
     * Parses an array of arrays containing coordinates into an ArrayList of an ArrayList of LatLng
     * objects
     *
     * @param coordinates array of an array containing the GeoJSON coordinates
     * @return ArrayList of an ArrayList of LatLng objects
     * @throws JSONException if coordinates cannot be parsed
     */
    private ArrayList<ArrayList<LatLng>> parseCoordinatesArrays(JSONArray coordinates)
            throws JSONException {
        ArrayList<ArrayList<LatLng>> coordinatesArray = new ArrayList<ArrayList<LatLng>>();

        for (int i = 0; i < coordinates.length(); i++) {
            coordinatesArray.add(parseCoordinatesArray(coordinates.getJSONArray(i)));
        }
        return coordinatesArray;
    }

    /**
     * Gets the array of Feature objects
     *
     * @return array of Features
     */
    public ArrayList<Feature> getFeatures() {
        return mFeatures;
    }
}
