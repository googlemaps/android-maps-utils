package com.google.maps.android.geojson;


import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

/**
 * Parses a JSONObject and places data into their appropriate GeoJsonFeature objects. Returns an
 * array of
 * GeoJsonFeature objects parsed from the GeoJSON file.
 */
/* package */ class GeoJsonParser {

    private static final String LOG_TAG = "GeoJsonParser";

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

    private static final String POINT = "Point";

    private static final String MULTIPOINT = "MultiPoint";

    private static final String LINESTRING = "LineString";

    private static final String MULTILINESTRING = "MultiLineString";

    private static final String POLYGON = "Polygon";

    private static final String MULTIPOLYGON = "MultiPolygon";

    private final JSONObject mGeoJsonFile;

    private final ArrayList<GeoJsonFeature> mGeoJsonFeatures;

    private LatLngBounds mBoundingBox;


    /**
     * Creates a new GeoJsonParser
     *
     * @param geoJsonFile GeoJSON file to parse
     */
    /* package */ GeoJsonParser(JSONObject geoJsonFile) {
        mGeoJsonFile = geoJsonFile;
        mGeoJsonFeatures = new ArrayList<GeoJsonFeature>();
        mBoundingBox = null;
        parseGeoJson();
    }

    private static boolean isGeometry(String type) {
        return type.matches(POINT + "|" + MULTIPOINT + "|" + LINESTRING + "|" + MULTILINESTRING +
                "|" + POLYGON + "|" + MULTIPOLYGON + "|" + GEOMETRY_COLLECTION);
    }

    /**
     * Parses a single GeoJSON feature which contains a geometry and properties member both of
     * which can be null. Also parses the bounding box and id members of the feature if they exist.
     *
     * @param geoJsonFeature feature to parse
     * @return GeoJsonFeature object
     */
    private static GeoJsonFeature parseFeature(JSONObject geoJsonFeature) {
        String id = null;
        LatLngBounds boundingBox = null;
        GeoJsonGeometry geometry = null;
        HashMap<String, String> properties = new HashMap<String, String>();

        try {
            if (geoJsonFeature.has(FEATURE_ID)) {
                id = geoJsonFeature.getString(FEATURE_ID);
            }
            if (geoJsonFeature.has(BOUNDING_BOX)) {
                boundingBox = parseBoundingBox(geoJsonFeature.getJSONArray(BOUNDING_BOX));
            }
            if (geoJsonFeature.has(FEATURE_GEOMETRY) && !geoJsonFeature.isNull(FEATURE_GEOMETRY)) {
                geometry = parseGeometry(geoJsonFeature.getJSONObject(FEATURE_GEOMETRY));
            }
            if (geoJsonFeature.has(PROPERTIES) && !geoJsonFeature.isNull(PROPERTIES)) {
                properties = parseProperties(geoJsonFeature.getJSONObject("properties"));
            }
        } catch (JSONException e) {
            Log.w(LOG_TAG, "Feature could not be successfully parsed " + geoJsonFeature.toString());
            return null;
        }
        return new GeoJsonFeature(geometry, id, properties, boundingBox);
    }

    /**
     * Parses a bounding box given as a JSONArray of 4 elements in the order of lowest values for
     * all axes followed by highest values. Axes order of a bounding box follows the axes order of
     * geometries.
     *
     * @param coordinates array of 4 coordinates
     * @return LatLngBounds containing the coordinates of the bounding box
     * @throws JSONException if the bounding box could not be parsed
     */
    private static LatLngBounds parseBoundingBox(JSONArray coordinates) throws JSONException {
        // Lowest values for all axes
        LatLng southWestCorner = new LatLng(coordinates.getDouble(1), coordinates.getDouble(0));
        // Highest value for all axes
        LatLng northEastCorner = new LatLng(coordinates.getDouble(3), coordinates.getDouble(2));
        return new LatLngBounds(southWestCorner, northEastCorner);
    }

    /**
     * Parses a single GeoJSON geometry object containing a coordinates array or a geometries array
     * if it has type GeometryCollection
     *
     * @param geoJsonGeometry geometry object to parse
     * @return GeoJsonGeometry object
     */
    private static GeoJsonGeometry parseGeometry(JSONObject geoJsonGeometry) {
        try {

            String geometryType = geoJsonGeometry.getString("type");

            JSONArray geometryArray;
            if (geometryType.equals(GEOMETRY_COLLECTION)) {
                // GeometryCollection
                geometryArray = geoJsonGeometry.getJSONArray(GEOMETRY_COLLECTION_ARRAY);
            } else if (isGeometry(geometryType)) {
                geometryArray = geoJsonGeometry.getJSONArray(GEOMETRY_COORDINATES_ARRAY);
            } else {
                // No geometries or coordinates array
                return null;
            }
            return createGeometry(geometryType, geometryArray);
        } catch (JSONException e) {
            return null;
        }
    }

    /**
     * Converts a GeoJsonGeometry object into a GeoJsonFeature object. A geometry object has no ID,
     * properties or bounding box so it is set to null.
     *
     * @param geoJsonGeometry Geometry object to convert into a Feature object
     * @return new Feature object
     */
    private static GeoJsonFeature parseGeometryToFeature(JSONObject geoJsonGeometry) {
        GeoJsonGeometry geometry = parseGeometry(geoJsonGeometry);
        if (geometry != null) {
            return new GeoJsonFeature(geometry, null, new HashMap<String, String>(), null);
        }
        Log.w(LOG_TAG, "Geometry could not be parsed");
        return null;

    }

    /**
     * Parses the properties of a GeoJSON feature into a hashmap
     *
     * @param properties GeoJSON properties member
     * @return hashmap containing property values
     * @throws JSONException if the properties could not be parsed
     */
    private static HashMap<String, String> parseProperties(JSONObject properties)
            throws JSONException {
        HashMap<String, String> propertiesMap = new HashMap<String, String>();
        Iterator propertyKeys = properties.keys();
        while (propertyKeys.hasNext()) {
            String key = (String) propertyKeys.next();
            propertiesMap.put(key, properties.getString(key));
        }
        return propertiesMap;
    }

    /**
     * Creates a GeoJsonGeometry object from the given type of geometry and its coordinates or
     * geometries array
     *
     * @param geometryType  type of geometry
     * @param geometryArray coordinates or geometries of the geometry
     * @return GeoJsonGeometry object
     * @throws JSONException if the coordinates or geometries could be parsed
     */
    private static GeoJsonGeometry createGeometry(String geometryType, JSONArray geometryArray)
            throws JSONException {
        if (geometryType.equals(POINT)) {
            return createPoint(geometryArray);
        } else if (geometryType.equals(MULTIPOINT)) {
            return createMultiPoint(geometryArray);
        } else if (geometryType.equals(LINESTRING)) {
            return createLineString(geometryArray);
        } else if (geometryType.equals(MULTILINESTRING)) {
            return createMultiLineString(geometryArray);
        } else if (geometryType.equals(POLYGON)) {
            return createPolygon(geometryArray);
        } else if (geometryType.equals(MULTIPOLYGON)) {
            return createMultiPolygon(geometryArray);
        } else if (geometryType.equals(GEOMETRY_COLLECTION)) {
            return createGeometryCollection(geometryArray);
        }
        return null;
    }

    /**
     * Creates a new GeoJsonPoint object
     *
     * @param coordinates array containing the coordinates for the GeoJsonPoint
     * @return GeoJsonPoint object
     * @throws JSONException if coordinates cannot be parsed
     */
    private static GeoJsonPoint createPoint(JSONArray coordinates) throws JSONException {
        return new GeoJsonPoint(parseCoordinate(coordinates));
    }

    /**
     * Creates a new GeoJsonMultiPoint object containing an array of GeoJsonPoint objects
     *
     * @param coordinates array containing the coordinates for the GeoJsonMultiPoint
     * @return GeoJsonMultiPoint object
     * @throws JSONException if coordinates cannot be parsed
     */
    private static GeoJsonMultiPoint createMultiPoint(JSONArray coordinates) throws JSONException {
        ArrayList<GeoJsonPoint> geoJsonPoints = new ArrayList<GeoJsonPoint>();
        for (int i = 0; i < coordinates.length(); i++) {
            geoJsonPoints.add(createPoint(coordinates.getJSONArray(i)));
        }
        return new GeoJsonMultiPoint(geoJsonPoints);
    }

    /**
     * Creates a new GeoJsonLineString object
     *
     * @param coordinates array containing the coordinates for the GeoJsonLineString
     * @return GeoJsonLineString object
     * @throws JSONException if coordinates cannot be parsed
     */
    private static GeoJsonLineString createLineString(JSONArray coordinates) throws JSONException {
        return new GeoJsonLineString(parseCoordinatesArray(coordinates));
    }

    /**
     * Creates a new GeoJsonMultiLineString object containing an array of GeoJsonLineString objects
     *
     * @param coordinates array containing the coordinates for the GeoJsonMultiLineString
     * @return GeoJsonMultiLineString object
     * @throws JSONException if coordinates cannot be parsed
     */
    private static GeoJsonMultiLineString createMultiLineString(JSONArray coordinates)
            throws JSONException {
        ArrayList<GeoJsonLineString> geoJsonLineStrings = new ArrayList<GeoJsonLineString>();
        for (int i = 0; i < coordinates.length(); i++) {
            geoJsonLineStrings.add(createLineString(coordinates.getJSONArray(i)));
        }
        return new GeoJsonMultiLineString(geoJsonLineStrings);
    }

    /**
     * Creates a new GeoJsonPolygon object
     *
     * @param coordinates array containing the coordinates for the GeoJsonPolygon
     * @return GeoJsonPolygon object
     * @throws JSONException if coordinates cannot be parsed
     */
    private static GeoJsonPolygon createPolygon(JSONArray coordinates) throws JSONException {
        return new GeoJsonPolygon(parseCoordinatesArrays(coordinates));
    }

    /**
     * Creates a new GeoJsonMultiPolygon object containing an array of GeoJsonPolygon objects
     *
     * @param coordinates array containing the coordinates for the GeoJsonMultiPolygon
     * @return GeoJsonPolygon object
     * @throws JSONException if coordinates cannot be parsed
     */
    private static GeoJsonMultiPolygon createMultiPolygon(JSONArray coordinates)
            throws JSONException {
        ArrayList<GeoJsonPolygon> geoJsonPolygons = new ArrayList<GeoJsonPolygon>();
        for (int i = 0; i < coordinates.length(); i++) {
            geoJsonPolygons.add(createPolygon(coordinates.getJSONArray(i)));
        }
        return new GeoJsonMultiPolygon(geoJsonPolygons);
    }

    /**
     * Creates a new GeoJsonGeometryCollection object containing an array of GeoJsonGeometry
     * objects
     *
     * @param geometries array containing the geometries for the GeoJsonGeometryCollection
     * @return GeoJsonGeometryCollection object
     * @throws JSONException if geometries cannot be parsed
     */
    private static GeoJsonGeometryCollection createGeometryCollection(JSONArray geometries)
            throws JSONException {
        ArrayList<GeoJsonGeometry> geometryCollectionElements
                = new ArrayList<GeoJsonGeometry>();

        for (int i = 0; i < geometries.length(); i++) {
            JSONObject geometryElement = geometries.getJSONObject(i);
            GeoJsonGeometry geometry = parseGeometry(geometryElement);
            if (geometry != null) {
                // Do not add geometries that could not be parsed
                geometryCollectionElements.add(geometry);
            }
        }
        return new GeoJsonGeometryCollection(geometryCollectionElements);
    }

    /**
     * Parses an array containing a coordinate into a LatLng object
     *
     * @param coordinates array containing the GeoJSON coordinate
     * @return LatLng object
     * @throws JSONException if coordinate cannot be parsed
     */
    private static LatLng parseCoordinate(JSONArray coordinates) throws JSONException {
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
    private static ArrayList<LatLng> parseCoordinatesArray(JSONArray coordinates)
            throws JSONException {
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
    private static ArrayList<ArrayList<LatLng>> parseCoordinatesArrays(JSONArray coordinates)
            throws JSONException {
        ArrayList<ArrayList<LatLng>> coordinatesArray = new ArrayList<ArrayList<LatLng>>();

        for (int i = 0; i < coordinates.length(); i++) {
            coordinatesArray.add(parseCoordinatesArray(coordinates.getJSONArray(i)));
        }
        return coordinatesArray;
    }

    /**
     * Parses the GeoJSON file by type and adds the generated GeoJsonFeature objects to the
     * mFeatures array. Supported GeoJSON types include feature, feature collection and geometry.
     */
    private void parseGeoJson() {
        try {
            GeoJsonFeature feature;
            String type = mGeoJsonFile.getString("type");

            if (type.equals(FEATURE)) {
                feature = parseFeature(mGeoJsonFile);
                if (feature != null) {
                    mGeoJsonFeatures.add(feature);
                }
            } else if (type.equals(FEATURE_COLLECTION)) {
                mGeoJsonFeatures.addAll(parseFeatureCollection(mGeoJsonFile));
            } else if (isGeometry(type)) {
                feature = parseGeometryToFeature(mGeoJsonFile);
                if (feature != null) {
                    // Don't add null features
                    mGeoJsonFeatures.add(feature);
                }
            } else {
                Log.w(LOG_TAG, "GeoJSON file could not be parsed.");
            }
        } catch (JSONException e) {
            Log.w(LOG_TAG, "GeoJSON file could not be parsed.");
        }
    }

    /**
     * Parses the array of GeoJSON features in a given GeoJSON feature collection. Also parses the
     * bounding box member of the feature collection if it exists.
     *
     * @param geoJsonFeatureCollection feature collection to parse
     * @return array of GeoJsonFeature objects
     */
    private ArrayList<GeoJsonFeature> parseFeatureCollection(JSONObject geoJsonFeatureCollection) {
        JSONArray geoJsonFeatures;
        ArrayList<GeoJsonFeature> features = new ArrayList<GeoJsonFeature>();
        try {
            geoJsonFeatures = geoJsonFeatureCollection.getJSONArray(FEATURE_COLLECTION_ARRAY);
            if (geoJsonFeatureCollection.has(BOUNDING_BOX)) {
                mBoundingBox = parseBoundingBox(
                        geoJsonFeatureCollection.getJSONArray(BOUNDING_BOX));
            }
        } catch (JSONException e) {
            Log.w(LOG_TAG, "Feature Collection could not be created.");
            return features;
        }

        for (int i = 0; i < geoJsonFeatures.length(); i++) {
            try {
                JSONObject feature = geoJsonFeatures.getJSONObject(i);
                if (feature.getString("type").equals(FEATURE)) {
                    GeoJsonFeature parsedFeature = parseFeature(feature);
                    if (parsedFeature != null) {
                        // Don't add null features
                        features.add(parsedFeature);
                    } else {
                        Log.w(LOG_TAG,
                                "Index of Feature in Feature Collection that could not be created: "
                                        + i);
                    }
                }
            } catch (JSONException e) {
                Log.w(LOG_TAG,
                        "Index of Feature in Feature Collection that could not be created: " + i);
            }
        }
        return features;
    }

    /**
     * Gets the array of GeoJsonFeature objects
     *
     * @return array of GeoJsonFeatures
     */
    /* package */ ArrayList<GeoJsonFeature> getFeatures() {
        return mGeoJsonFeatures;
    }

    /**
     * Gets the array containing the coordinates of the bounding box for the FeatureCollection. If
     * the FeatureCollection did not have a bounding box or if the GeoJSON file did not contain a
     * FeatureCollection then null will be returned.
     *
     * @return LatLngBounds object containing bounding box of FeatureCollection, null if no bounding
     * box
     */
    /* package */ LatLngBounds getBoundingBox() {
        return mBoundingBox;
    }

}
