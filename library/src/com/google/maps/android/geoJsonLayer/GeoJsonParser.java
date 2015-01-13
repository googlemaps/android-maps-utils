package com.google.maps.android.geoJsonLayer;


import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

/**
 * Created by juliawong on 12/30/14.
 *
 * Parses a JSONObject and places data into their appropriate Feature objects. Returns an array of
 * Feature objects parsed from the GeoJSON file.
 */
public class GeoJsonParser {

    // Feature object type
    private static final String FEATURE = "Feature";

    // Feature object geometry member
    private static final String FEATURE_GEOMETRY = "geometry";

    // Feature object id member
    private static final String FEATURE_ID = "id";

    // FeatureCollection type
    private static final String FEATURE_COLLECTION = "FeatureCollection";

    // FeatureCollection features array member
    private static final String FEATURE_COLLECTION_ARRAY = "features";

    // Geometry coordinates member
    private static final String GEOMETRY_COORDINATES_ARRAY = "coordinates";

    // GeometryCollection type
    private static final String GEOMETRY_COLLECTION = "GeometryCollection";

    // GeometryCollection geometries array member
    private static final String GEOMETRY_COLLECTION_ARRAY = "geometries";

    // Coordinates for bbox
    private static final String BOUNDING_BOX = "bbox";

    private static final String PROPERTIES = "properties";

    // Geometry objects except for GeometryCollection
    private static final String GEOJSON_GEOMETRY_OBJECTS_REGEX
            = "Point|MultiPoint|LineString|MultiLineString|Polygon|MultiPolygon";

    private final JSONObject mGeoJsonFile;

    private final ArrayList<GeoJsonFeature> mGeoJsonFeatures;

    private ArrayList<LatLng> mBoundingBox;


    /**
     * Creates a new GeoJsonParser
     *
     * @param geoJsonFile GeoJSON file to parse
     */
    public GeoJsonParser(JSONObject geoJsonFile) {
        mGeoJsonFile = geoJsonFile;
        mGeoJsonFeatures = new ArrayList<GeoJsonFeature>();
        mBoundingBox = null;
    }

    /**
     * Parses the GeoJSON file and adds the generated Feature objects to the mFeatures array
     *
     * @throws JSONException if the GeoJSON file could not be parsed
     */
    public void parseGeoJson() throws JSONException {
        String type = mGeoJsonFile.getString("type");
        if (type.equals(FEATURE)) {
            mGeoJsonFeatures.add(parseFeature(mGeoJsonFile));
        } else if (type.equals(FEATURE_COLLECTION)) {
            mGeoJsonFeatures.addAll(parseFeatureCollection(mGeoJsonFile));
        } else if (type.matches(GEOJSON_GEOMETRY_OBJECTS_REGEX) || type
                .equals(GEOMETRY_COLLECTION)) {
            mGeoJsonFeatures.add(geometryToFeature(parseGeometry(mGeoJsonFile)));
        }
    }

    /**
     * Gets the array of Feature objects
     *
     * @return array of Features
     */
    public ArrayList<GeoJsonFeature> getFeatures() {
        return mGeoJsonFeatures;
    }

    /**
     * Gets the array containing the coordinates of the bounding box for the FeatureCollection. If
     * the FeatureCollection did not have a bounding box or if the GeoJSON file did not contain a
     * FeatureCollection then null will be returned.
     *
     * @return array containing bounding box of FeatureCollection, null if no bounding box
     */
    public ArrayList<LatLng> getBoundingBox() {
        return mBoundingBox;
    }

    /**
     * Parses a GeoJSON feature collection which contains an array of features
     *
     * @return array of Feature objects parsed from the given array
     * @throws JSONException if the feature collection could not be parsed
     */
    private ArrayList<GeoJsonFeature> parseFeatureCollection(JSONObject geoJsonFeatureCollection)
            throws JSONException {
        JSONArray geoJsonFeatures = geoJsonFeatureCollection.getJSONArray(FEATURE_COLLECTION_ARRAY);
        if (geoJsonFeatureCollection.has(BOUNDING_BOX)) {
            mBoundingBox = parseBoundingBox(geoJsonFeatureCollection.getJSONArray(BOUNDING_BOX));
        }

        ArrayList<GeoJsonFeature> features = new ArrayList<GeoJsonFeature>();
        for (int i = 0; i < geoJsonFeatures.length(); i++) {
            JSONObject feature = geoJsonFeatures.getJSONObject(i);
            if (feature.getString("type").equals(FEATURE)) {
                features.add(parseFeature(feature));
            }
        }
        return features;
    }

    /**
     * Parses a single GeoJSON feature which contains a geometry and properties member both of
     * which can be null and optionally an id. If the geometry member has a null value, we do not
     * add the geometry to the array.
     *
     * @param geoJsonFeature GeoJSON feature to parse
     * @return Feature object parsed from the given GeoJSON feature
     * @throws JSONException if the feature does not have members geometry and properties or could
     *                       not be parsed for some other reason
     */
    private GeoJsonFeature parseFeature(JSONObject geoJsonFeature) throws JSONException {
        String id = null;
        ArrayList<LatLng> boundingBox = null;
        GeoJsonGeometry geometry = null;
        JSONObject properties = null;

        // TODO: if any of these are missing, log an error
        if (geoJsonFeature.has(FEATURE_ID)) {
            id = geoJsonFeature.getString(FEATURE_ID);
        }
        if (geoJsonFeature.has(PROPERTIES)) {
            properties = geoJsonFeature.getJSONObject("properties");
        }
        if (geoJsonFeature.has(FEATURE_GEOMETRY)) {
            if (!geoJsonFeature.isNull(FEATURE_GEOMETRY)) {
                // Parse geometry if it isn't null
                geometry = parseGeometry(geoJsonFeature.getJSONObject(FEATURE_GEOMETRY));
            }
        }
        if (geoJsonFeature.has(BOUNDING_BOX)) {
            boundingBox = parseBoundingBox(geoJsonFeature.getJSONArray(BOUNDING_BOX));
        }

        return new GeoJsonFeature(geometry, id, parseProperties(properties), boundingBox);
    }


    /**
     * Parses a bounding box given as a JSONArray of 4 elements in the order of lowest values for
     * all axes followed by highest values. Axes order of a bounding box follows the axes order of
     * geometries.
     *
     * @param coordinates array of 4 coordinates
     * @return array containing 2 LatLngs where the first element is the lowest values and the
     * second element is the highest values
     * @throws JSONException if the bounding box could not be parsed
     */
    private ArrayList<LatLng> parseBoundingBox(JSONArray coordinates) throws JSONException {
        ArrayList<LatLng> boundingBox = new ArrayList<LatLng>();
        // Lowest values for all axes
        boundingBox.add((new LatLng(coordinates.getDouble(1), coordinates.getDouble(0))));
        // Highest value for all axes
        boundingBox.add((new LatLng(coordinates.getDouble(3), coordinates.getDouble(2))));
        return boundingBox;
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
    private GeoJsonGeometry parseGeometry(JSONObject geoJsonGeometry) throws JSONException {
        String geometryType = geoJsonGeometry.getString("type");
        JSONArray geometryArray;
        if (geometryType.matches(GEOJSON_GEOMETRY_OBJECTS_REGEX)) {
            geometryArray = geoJsonGeometry.getJSONArray(GEOMETRY_COORDINATES_ARRAY);
        } else {
            // GeometryCollection
            geometryArray = geoJsonGeometry.getJSONArray(GEOMETRY_COLLECTION_ARRAY);
        }

        return createGeometry(geometryType, geometryArray);
    }

    /**
     * Converts a geometry object to a feature object. A geometry object has no ID or properties so
     * it is set to null.
     *
     * @param GeoJsonGeometry Geometry object to convert into a Feature object
     * @return new Feature object
     */
    private GeoJsonFeature geometryToFeature(GeoJsonGeometry GeoJsonGeometry) {
        return new GeoJsonFeature(GeoJsonGeometry, null, null, null);
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
     * Creates a Geometry object from the given type of geometry and its coordinates or geometries
     * array
     *
     * @param geometryType  type of geometry
     * @param geometryArray coordinates or geometries of the geometry to parse and add to the
     *                      Geometry object
     * @return Geometry object of type geometryType and containing the given coordinates or
     * geometries
     * @throws JSONException if the coordinates could be parsed
     */
    private GeoJsonGeometry createGeometry(String geometryType, JSONArray geometryArray)
            throws JSONException {
        if (geometryType.equals("Point")) {
            return createPoint(geometryArray);
        } else if (geometryType.equals("MultiPoint")) {
            return createMultiPoint(geometryArray);
        } else if (geometryType.equals("LineString")) {
            return createLineString(geometryArray);
        } else if (geometryType.equals("MultiLineString")) {
            return createMultiLineString(geometryArray);
        } else if (geometryType.equals("Polygon")) {
            return createPolygon(geometryArray);
        } else if (geometryType.equals("MultiPolygon")) {
            return createMultiPolygon(geometryArray);
        } else if (geometryType.equals(GEOMETRY_COLLECTION)) {
            return createGeometryCollection(geometryArray);
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
    private GeoJsonPoint createPoint(JSONArray coordinates) throws JSONException {
        return new GeoJsonPoint(parseCoordinate(coordinates));
    }

    /**
     * Creates a new MultiPoint object containing an array of Point objects
     *
     * @param coordinates array containing the GeoJSON coordinates
     * @return MultiPoint object
     * @throws JSONException if coordinates cannot be parsed
     */
    private GeoJsonMultiPoint createMultiPoint(JSONArray coordinates) throws JSONException {
        ArrayList<GeoJsonPoint> geoJsonPoints = new ArrayList<GeoJsonPoint>();
        for (int i = 0; i < coordinates.length(); i++) {
            geoJsonPoints.add(createPoint(coordinates.getJSONArray(i)));
        }
        return new GeoJsonMultiPoint(geoJsonPoints);
    }

    /**
     * Creates a new LineString object
     *
     * @param coordinates array containing the GeoJSON coordinates
     * @return LineString object
     * @throws JSONException if coordinates cannot be parsed
     */
    private GeoJsonLineString createLineString(JSONArray coordinates) throws JSONException {
        return new GeoJsonLineString(parseCoordinatesArray(coordinates));
    }

    /**
     * Creates a new MultiLineString object containing an array of LineString objects
     *
     * @param coordinates array containing the GeoJSON coordinates
     * @return MultiLineString object
     * @throws JSONException if coordinates cannot be parsed
     */
    private GeoJsonMultiLineString createMultiLineString(JSONArray coordinates)
            throws JSONException {
        ArrayList<GeoJsonLineString> geoJsonLineStrings = new ArrayList<GeoJsonLineString>();
        for (int i = 0; i < coordinates.length(); i++) {
            geoJsonLineStrings.add(createLineString(coordinates.getJSONArray(i)));
        }
        return new GeoJsonMultiLineString(geoJsonLineStrings);
    }

    /**
     * Creates a new Polygon object
     *
     * @param coordinates array containing the GeoJSON coordinates
     * @return Polygon object
     * @throws JSONException if coordinates cannot be parsed
     */
    private GeoJsonPolygon createPolygon(JSONArray coordinates) throws JSONException {
        return new GeoJsonPolygon(parseCoordinatesArrays(coordinates));
    }

    /**
     * Creates a new MultiPolygon object containing an array of Polygon objects
     *
     * @param coordinates array containing the GeoJSON coordinates
     * @return MultiPolygon object
     * @throws JSONException if coordinates cannot be parsed
     */
    private GeoJsonMultiPolygon createMultiPolygon(JSONArray coordinates) throws JSONException {
        ArrayList<GeoJsonPolygon> geoJsonPolygons = new ArrayList<GeoJsonPolygon>();
        for (int i = 0; i < coordinates.length(); i++) {
            geoJsonPolygons.add(createPolygon(coordinates.getJSONArray(i)));
        }
        return new GeoJsonMultiPolygon(geoJsonPolygons);
    }

    /**
     * Creates a new GeometryCollection object containing an array of Geometry objects
     *
     * @param geometries array containing the elements of the GeoJSON GeometryCollection
     * @return GeometryCollection object
     * @throws JSONException if geometries cannot be parsed
     */
    private GeoJsonGeometryCollection createGeometryCollection(JSONArray geometries)
            throws JSONException {
        ArrayList<GeoJsonGeometry> geoJsonGeometryCollectionElements
                = new ArrayList<GeoJsonGeometry>();

        for (int i = 0; i < geometries.length(); i++) {
            JSONObject geometryElement = geometries.getJSONObject(i);
            geoJsonGeometryCollectionElements.add(parseGeometry(geometryElement));
        }
        return new GeoJsonGeometryCollection(geoJsonGeometryCollectionElements);
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

}
