package com.google.maps.android.geoJsonImport;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.Polyline;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by juliawong on 12/2/14.
 * Imports the given GeoJSON file and adds all GeoJSON objects to the map
 */

/*         _
          {_}
          / \
         /   \
        /_____\
      {`_______`}
       // . . \\
      (/(__7__)\)
      |'-' = `-'|
      |         |
      /\       /\
     /  '.   .'  \
    /_/   `"`   \_\
   {__}###[_]###{__}
   (_/\_________/\_)
       |___|___|
        |--|--|
       (__)`(__)
 */


public class GeoJsonImport {

    // defined geometry objects except for GeometryCollection
    private static final String GEOJSON_GEOMETRY_OBJECTS_REGEX
            = "Point|MultiPoint|LineString|MultiLineString|Polygon|MultiPolygon";

    // all geometries (except for GeometryCollection) and features must have an array of coordinates
    private final static String COORDINATE_ARRAY = "coordinates";

    private final static String FEATURE = "Feature";

    private final static String FEATURE_COLLECTION = "FeatureCollection";

    // all feature objects must have a member named geometry
    private final static String FEATURE_GEOMETRY = "geometry";

    // all feature objects must have a member named properties
    private final static String FEATURE_PROPERTIES = "properties";

    // name of the member in feature collection that is an array
    private final static String FEATURE_COLLECTION_ARRAY = "features";

    private final static String GEOMETRY_COLLECTION = "GeometryCollection";

    // name of the member in geometry collection that is an array
    private final static String GEOMETRY_COLLECTION_ARRAY = "geometries";

    private final ArrayList<Object> mGeoJsonMapPropertyObjects = new ArrayList<Object>();

    private final ArrayList<Object> mGeoJsonMapObjects = new ArrayList<Object>();

    private final GoogleMap mMap;

    private JSONObject mGeoJsonObject;

    /**
     * Active layer means that the GeoJSON data has been added to the map
     */
    private boolean mActiveLayer = false;

    private boolean mVisibility = false;

    // TODO: implement fetching files by URL

    /**
     * Creates a new GeoJsonImport object
     *
     * @param map            map object
     * @param geoJsonFileUrl URL of GeoJSON file
     */
    public GeoJsonImport(GoogleMap map, String geoJsonFileUrl) {
        mMap = map;

        // Currently a bad implementation
        // Waits for the file to be loaded
        // new parseUrlToJson().execute(geoJsonFileUrl).get();

        // parseGeoJsonFile(mGeoJsonObject);
    }

    /**
     * Creates a new GeoJsonImport object
     *
     * @param map        Map object
     * @param resourceId Raw resource GeoJSON file
     * @param context    Context object
     * @throws JSONException if the JSON file has invalid syntax and cannot be parsed successfully
     * @throws IOException   if the file cannot be opened for read
     */
    public GeoJsonImport(GoogleMap map, int resourceId, Context context)
            throws JSONException, IOException {
        mMap = map;
        InputStream stream = context.getResources().openRawResource(resourceId);
        mGeoJsonObject = createJsonFileObject(stream);
        parseGeoJsonFile(mGeoJsonObject);
    }

    /**
     * Takes an object and adds it to mapObjectList. In the case of an ArrayList, it adds all the
     * elements to mapObjectList
     *
     * @param mapObjectList list to append all map objects to
     * @param mapObject     object to add to mGeoJsonMapPropertyObjects, either
     *                      MarkerProperties,PolylineProperties, PolygonProperties or an ArrayList
     */
    private static void storeMapObjects(List<Object> mapObjectList, Object mapObject) {
        if (mapObject instanceof List) {
            mapObjectList.addAll((ArrayList) mapObject);
        } else {
            mapObjectList.add(mapObject);
        }
    }

    /**
     * Parses a JSONArray of coordinates into an array of
     * {@link com.google.android.gms.maps.model.LatLng} objects
     *
     * @param geoJsonCoordinates JSONArray of coordinates from the GeoJSON file
     * @return array of {@link com.google.android.gms.maps.model.LatLng} objects representing the
     * coordinates
     * @throws JSONException if array doesn't contain an array of coordinates
     */
    private static ArrayList<LatLng> coordinatesToLatLngArray(JSONArray geoJsonCoordinates)
            throws JSONException {
        JSONArray jsonCoordinate;
        ArrayList<LatLng> coordinatesArray = new ArrayList<LatLng>();
        // Iterate over the array of coordinates
        for (int i = 0; i < geoJsonCoordinates.length(); i++) {
            jsonCoordinate = geoJsonCoordinates.getJSONArray(i);
            // GeoJSON stores coordinates as lng, lat so need to reverse
            coordinatesArray
                    .add(new LatLng(jsonCoordinate.getDouble(1), jsonCoordinate.getDouble(0)));
        }
        return coordinatesArray;
    }

    /**
     * Parses the JSONObject of a single geometry for type, properties and coordinates
     * Geometry objects have only a member named coordinates which is an array
     *
     * @param geoJsonFeature geometry feature to parse
     * @return {@link com.google.android.gms.maps.model.MarkerOptions}, {@link
     * com.google.android.gms.maps.model.PolylineOptions} or {@link
     * com.google.android.gms.maps.model.PolygonOptions} object or an array of either Options
     * objects
     * @throws JSONException if there is no type string or coordinates array for the geometry
     */
    private static Object parseGeoJsonGeometry(JSONObject geoJsonFeature) throws JSONException {
        String geometryType;
        JSONArray featureCoordinatesArray;
        geometryType = geoJsonFeature.getString("type");
        featureCoordinatesArray = geoJsonFeature.getJSONArray(COORDINATE_ARRAY);
        return parseGeoJsonGeometryObject(geometryType, featureCoordinatesArray, null);
    }

    /**
     * Parses a single feature from the GeoJSON file into an object for the map has a coordinates
     * array and properties array
     * In the case of a geometry collection, ir will have an array of geometries instead of an
     * array
     * of coordinates
     *
     * @param geoJsonFeature JSONObject containing one feature to parse
     * @return {@link com.google.android.gms.maps.model.MarkerOptions}, {@link
     * com.google.android.gms.maps.model.PolylineOptions} or {@link
     * com.google.android.gms.maps.model.PolygonOptions} object or an array of either Options
     * objects
     * @throws JSONException if there is no type string or geometries array or coordinates array
     *                       for
     *                       the feature
     */
    private static Object parseGeoJsonFeature(JSONObject geoJsonFeature) throws JSONException {
        String geometryType;
        // Contains an array of geometries if the feature is a GeometryCollection and coordinates
        // otherwise
        JSONArray featureArray;
        JSONObject featureProperties;
        // Store the type, coordinates and properties of the GeoJSON feature
        geometryType = geoJsonFeature.getJSONObject(FEATURE_GEOMETRY).getString("type");

        if (geometryType.equals(GEOMETRY_COLLECTION)) {
            featureArray = geoJsonFeature.getJSONObject(FEATURE_GEOMETRY)
                    .getJSONArray(GEOMETRY_COLLECTION_ARRAY);
        } else {
            featureArray = geoJsonFeature.getJSONObject(FEATURE_GEOMETRY)
                    .getJSONArray(COORDINATE_ARRAY);
        }
        featureProperties = geoJsonFeature.getJSONObject(FEATURE_PROPERTIES);
        return parseGeoJsonGeometryObject(geometryType, featureArray,
                featureProperties);
    }

    /**
     * Converts a single geometry object into its relevant Google Map object
     *
     * @param geometryType      type of geometry object
     * @param featureArray      array of coordinates or geometries, the later used for geometry
     *                          collections
     * @param featureProperties properties of the geometry object to use when creating the options
     *                          object
     * @return {@link com.google.android.gms.maps.model.MarkerOptions}, {@link
     * com.google.android.gms.maps.model.PolylineOptions} or {@link
     * com.google.android.gms.maps.model.PolygonOptions} object or an array of either Options
     * objects
     */
    private static Object parseGeoJsonGeometryObject(String geometryType,
            JSONArray featureArray, JSONObject featureProperties) throws JSONException {
        if (geometryType.equals("Point")) {
            return toMarker(featureArray, featureProperties);
        } else if (geometryType.equals("MultiPoint")) {
            return toMarkers(featureArray, featureProperties);
        } else if (geometryType.equals("LineString")) {
            return toPolyline(featureArray, featureProperties);
        } else if (geometryType.equals("MultiLineString")) {
            return toPolylines(featureArray, featureProperties);
        } else if (geometryType.equals("Polygon")) {
            return toPolygon(featureArray, featureProperties);
        } else if (geometryType.equals("MultiPolygon")) {
            return toPolygons(featureArray, featureProperties);
        } else if (geometryType.equals(GEOMETRY_COLLECTION)) {
            return toGeometryCollection(featureArray, featureProperties);
        }
        return null;
    }

    /**
     * Creates a new MarkerProperties object based on the given coordinates and properties
     *
     * @param geoJsonPointCoordinatesArray JSONArray containing coordinates of the GeoJSON Point
     *                                     object
     * @param geoJsonPointProperties       JSONObject containing the GeoJSON Point object
     *                                     properties
     * @return new MarkerProperties object
     * @throws JSONException if coordinates or marker properties cannot be successfully parsed
     */
    private static MarkerProperties toMarker(JSONArray geoJsonPointCoordinatesArray,
            JSONObject geoJsonPointProperties) throws JSONException {
        MarkerProperties properties;
        LatLng coordinates = new LatLng(geoJsonPointCoordinatesArray.getDouble(1),
                geoJsonPointCoordinatesArray.getDouble(0));
        properties = new MarkerProperties(geoJsonPointProperties, coordinates);

        return properties;
    }

    /**
     * Creates an array of new MarkerProperties objects based on the given coordinates and
     * properties
     *
     * @param geoJsonMultiPointCoordinatesArray JSONArray containing coordinates of the GeoJSON
     *                                          MultiPoint object
     * @param geoJsonMultiPointProperties       JSONObject containing the MultiPoint GeoJSON object
     *                                          properties
     * @return array of new MarkerProperties objects
     * @throws JSONException if all coordinates or all marker properties cannot be successfully
     *                       parsed
     */
    private static ArrayList<MarkerProperties> toMarkers(
            JSONArray geoJsonMultiPointCoordinatesArray,
            JSONObject geoJsonMultiPointProperties) throws JSONException {
        ArrayList<MarkerProperties> markers = new ArrayList<MarkerProperties>();
        for (int i = 0; i < geoJsonMultiPointCoordinatesArray.length(); i++) {
            // Add each marker to the list
            markers.add(toMarker(geoJsonMultiPointCoordinatesArray.getJSONArray(i),
                    geoJsonMultiPointProperties));
        }
        return markers;
    }

    /**
     * Creates a new PolylineProperties object based on the given coordinates and properties
     *
     * @param geoJsonLineStringCoordinatesArray JSONArray containing coordinates of the GeoJSON
     *                                          LineString object
     * @param geoJsonLineStringProperties       JSONObject containing the LineString GeoJSON object
     *                                          properties
     * @return new PolylineProperties object
     * @throws JSONException if coordinates or polyline properties could not be successfully parsed
     */
    private static PolylineProperties toPolyline(JSONArray geoJsonLineStringCoordinatesArray,
            JSONObject geoJsonLineStringProperties) throws JSONException {
        ArrayList<LatLng> coordinates = coordinatesToLatLngArray(geoJsonLineStringCoordinatesArray);
        PolylineProperties properties;
        properties = new PolylineProperties(geoJsonLineStringProperties, coordinates);
        return properties;
    }

    /**
     * Creates an array of new PolylineProperties objects based on the given coordinates and
     * properties
     *
     * @param geoJsonMultiLineStringCoordinatesArray JSONArray containing coordinates of the
     *                                               GeoJSON MultiLineString object
     * @param geoJsonMultiLineStringProperties       JSONObject containing the MultiLineString
     *                                               GeoJSON object properties
     * @return array of new PolylineProperties objects
     * @throws JSONException if all coordinates or all polyline properties cannot be successfully
     *                       parsed
     */
    private static ArrayList<PolylineProperties> toPolylines(
            JSONArray geoJsonMultiLineStringCoordinatesArray,
            JSONObject geoJsonMultiLineStringProperties) throws JSONException {
        ArrayList<PolylineProperties> polylines = new ArrayList<PolylineProperties>();
        // Iterate over the list of polylines
        for (int i = 0; i < geoJsonMultiLineStringCoordinatesArray.length(); i++) {
            // Add each polyline to the list
            polylines.add(toPolyline(geoJsonMultiLineStringCoordinatesArray.getJSONArray(i),
                    geoJsonMultiLineStringProperties));

        }
        return polylines;
    }

    /**
     * Creates a new PolygonProperties object based on the given coordinates and properties
     *
     * @param geoJsonPolygonCoordinatesArray JSONArray containing coordinates of the GeoJSON
     *                                       Polygon object
     * @param geoJsonPolygonProperties       JSONObject containing the Polygon GeoJSON object
     *                                       properties
     * @return new PolygonProperties object
     * @throws JSONException if coordinates or polygon properties could not be successfully parsed
     */
    private static PolygonProperties toPolygon(JSONArray geoJsonPolygonCoordinatesArray,
            JSONObject geoJsonPolygonProperties) throws JSONException {
        PolygonProperties properties;
        ArrayList<ArrayList<LatLng>> coordinates = new ArrayList<ArrayList<LatLng>>();
        // Iterate over the list of coordinates for the polygon
        for (int i = 0; i < geoJsonPolygonCoordinatesArray.length(); i++) {
            // Add each group of coordinates to the list
            coordinates.add(
                    coordinatesToLatLngArray(geoJsonPolygonCoordinatesArray.getJSONArray(i)));
        }

        // Get the polygon properties
        properties = new PolygonProperties(geoJsonPolygonProperties, coordinates);

        return properties;
    }

    /**
     * Creates an array of new PolygonProperties objects based on the given coordinates and
     * properties
     *
     * @param geoJsonMultiPolygonCoordinatesArray JSONArray containing coordinates of the GeoJSON
     *                                            MultiPolygon object
     * @param geoJsonMultiPolygonProperties       JSONObject containing the MultiPolygon GeoJSON
     *                                            object properties
     * @return array of new PolygonProperties objects
     * @throws JSONException if all coordinates or all polygon properties could not be successfully
     *                       parsed
     */
    private static ArrayList<PolygonProperties> toPolygons(
            JSONArray geoJsonMultiPolygonCoordinatesArray,
            JSONObject geoJsonMultiPolygonProperties) throws JSONException {
        ArrayList<PolygonProperties> polygons = new ArrayList<PolygonProperties>();
        // Iterate over the list of polygons
        for (int i = 0; i < geoJsonMultiPolygonCoordinatesArray.length(); i++) {
            // Add each polygon to the list
            polygons.add(toPolygon(geoJsonMultiPolygonCoordinatesArray.getJSONArray(i),
                    geoJsonMultiPolygonProperties));
        }
        return polygons;
    }

    /**
     * Creates a new array of various MarkerProperties, PolylineProperties, PolygonProperties based
     * on the given coordinates and properties
     *
     * @param geoJsonGeometryCollectionArray JSONArray containing the geometry elements of the
     *                                       geometry collection
     * @param geoJsonCollectionProperties    JSONObject containing the GeometryCollection
     *                                       properties to be applied to the entire geometry
     *                                       collection
     * @return array of various MarkerProperties, PolylineProperties, PolygonProperties
     * @throws JSONException if elements in the geometry collection could not be successfully
     *                       parsed
     */
    private static ArrayList<Object> toGeometryCollection(JSONArray geoJsonGeometryCollectionArray,
            JSONObject geoJsonCollectionProperties) throws JSONException {
        ArrayList<Object> geometryCollectionObjects = new ArrayList<Object>();

        // Variables containing the status of the current element in the geometry collection
        boolean isGeometry;
        boolean isGeometryCollection;
        String geometryCollectionType;

        // Variables storing the current element and its geometries or coordinates
        JSONObject geometryCollectionElement;
        JSONArray geometriesArray;
        JSONArray coordinatesArray;

        // Iterate over all the elements in the geometry collection
        for (int i = 0; i < geoJsonGeometryCollectionArray.length(); i++) {
            // Set current element
            geometryCollectionElement = geoJsonGeometryCollectionArray.getJSONObject(i);
            // Update status of current element
            geometryCollectionType = geometryCollectionElement.getString("type");
            isGeometry = geometryCollectionType.matches(GEOJSON_GEOMETRY_OBJECTS_REGEX);
            isGeometryCollection = geometryCollectionType.equals(GEOMETRY_COLLECTION);

            if (isGeometry) {
                // Add new geometry object to geometryCollectionObjects
                coordinatesArray = geometryCollectionElement.getJSONArray(COORDINATE_ARRAY);
                // Pass in type, coordinates and properties of the current geometry
                storeMapObjects(geometryCollectionObjects,
                        parseGeoJsonGeometryObject(geometryCollectionType, coordinatesArray,
                                geoJsonCollectionProperties));
            } else if (isGeometryCollection) {
                // Recursively parse all elements and add to geometryCollectionObjects array
                geometriesArray = geometryCollectionElement.getJSONArray(GEOMETRY_COLLECTION_ARRAY);
                storeMapObjects(geometryCollectionObjects,
                        toGeometryCollection(geometriesArray, geoJsonCollectionProperties));
            }
        }
        return geometryCollectionObjects;
    }

    /**
     * Takes a character input stream and converts it into a JSONObject
     *
     * @param stream Character input stream representing  the GeoJSON file
     * @return JSONObject representing the GeoJSON file
     * @throws IOException   if the file cannot be opened for read
     * @throws JSONException if the JSON file has poor structure
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
     * Parses all GeoJSON objects in the GeoJSON file and adds them to mGeoJsonMapPropertyObjects
     *
     * @param geoJsonFile GeoJSON file to parse
     * @throws JSONException if the GeoJSON file cannot be successfully parsed
     */

    private void parseGeoJsonFile(JSONObject geoJsonFile) throws JSONException {
        JSONArray geometriesArray;
        String type = geoJsonFile.getString("type").trim();
        if (FEATURE_COLLECTION.equals(type)) {
            storeMapObjects(mGeoJsonMapPropertyObjects, parseGeoJsonFeatureCollection(geoJsonFile));
        } else if (GEOMETRY_COLLECTION.equals(type)) {
            geometriesArray = geoJsonFile.getJSONArray(GEOMETRY_COLLECTION_ARRAY);
            storeMapObjects(mGeoJsonMapPropertyObjects,
                    toGeometryCollection(geometriesArray, null));
        } else if (FEATURE.equals(type)) {
            storeMapObjects(mGeoJsonMapPropertyObjects, parseGeoJsonFeature(geoJsonFile));
        } else if (type != null && type.matches(GEOJSON_GEOMETRY_OBJECTS_REGEX)) {
            storeMapObjects(mGeoJsonMapPropertyObjects, parseGeoJsonGeometry(geoJsonFile));
        }
    }

    /**
     * Creates a list of properties objects from a given JSONObject containing a feature collection
     *
     * @param featureCollection Feature collection to parse into geometry property objects
     * @return ArrayList of MarkerProperties, PolylineProperties and PolygonProperties
     * @throws JSONException if feature collection does not contain an array of features
     */
    private ArrayList<Object> parseGeoJsonFeatureCollection(JSONObject featureCollection)
            throws JSONException {
        ArrayList<Object> featureCollectionObjects = new ArrayList<Object>();
        // Gets the array containing the feature collection objects
        JSONArray featureCollectionArray = featureCollection.getJSONArray(FEATURE_COLLECTION_ARRAY);
        for (int i = 0; i < featureCollectionArray.length(); i++) {
            boolean isFeature = featureCollectionArray.getJSONObject(i).getString("type").trim()
                    .equals(FEATURE);
            // Add the single feature to featureCollectionObjects
            if (isFeature) {
                storeMapObjects(featureCollectionObjects,
                        parseGeoJsonFeature(featureCollectionArray.getJSONObject(i)));
            }
        }
        return featureCollectionObjects;
    }

    /**
     * Adds all objects in mGeoJsonMapPropertyObjects to the mMap
     */
    public void addGeoJsonData() {
        // Prevents duplicate layers of the file from being added
        if (!mActiveLayer) {
            for (Object mapObject : mGeoJsonMapPropertyObjects) {
                if (mapObject instanceof MarkerProperties) {
                    mGeoJsonMapObjects
                            .add(mMap.addMarker(((MarkerProperties) mapObject).getMarkerOptions()));
                } else if (mapObject instanceof PolylineProperties) {
                    mGeoJsonMapObjects.add(mMap
                            .addPolyline(((PolylineProperties) mapObject).getPolylineOptions()));
                } else if (mapObject instanceof PolygonProperties) {
                    mGeoJsonMapObjects
                            .add(mMap.addPolygon(
                                    ((PolygonProperties) mapObject).getPolygonOptions()));
                }
            }
        }
        mActiveLayer = true;
        mVisibility = true;
    }

    /**
     * Sets all objects on the map to have the given visibility
     *
     * @param visibility true if all objects on the map are to be shown, false if no objects on the
     *                   map are to be shown
     */
    public void geoJsonDataVisibility(boolean visibility) {
        for (Object mapObject : mGeoJsonMapObjects) {
            if (mapObject instanceof Marker) {
                ((Marker) mapObject).setVisible(visibility);
            } else if (mapObject instanceof Polyline) {
                ((Polyline) mapObject).setVisible(visibility);
            } else if (mapObject instanceof Polygon) {
                ((Polygon) mapObject).setVisible(visibility);
            }
        }
        mVisibility = visibility;
    }

    /**
     * Removes all objects in mGeoJsonMapObjects from the mMap
     */
    public void removeGeoJsonData() {
        for (Object mapObject : mGeoJsonMapObjects) {
            if (mapObject instanceof Marker) {
                ((Marker) mapObject).remove();
            } else if (mapObject instanceof Polyline) {
                ((Polyline) mapObject).remove();
            } else if (mapObject instanceof Polygon) {
                ((Polygon) mapObject).remove();
            }
        }
        mActiveLayer = false;
        mVisibility = false;

        // Remove all stored map objects
        mGeoJsonMapObjects.clear();
    }

    public boolean getVisibility() {
        return mVisibility;
    }

    /**
     * Downloads the GeoJSON file from the given URL
     */
    private class parseUrlToJson extends AsyncTask<String, Void, Void> {

        /**
         * Downloads the file and store the GeoJSON object
         *
         * @param params First parameter is the URL of the GeoJSON file to download
         */
        @Override
        protected Void doInBackground(String... params) {
            try {
                // Creates the character input stream
                InputStream stream = new URL(params[0]).openConnection().getInputStream();
                // Convert stream to JSONObject
                mGeoJsonObject = createJsonFileObject(stream);

            } catch (IOException e) {
                Log.e("IOException", e.toString());
            } catch (JSONException e) {
                Log.e("JSONException", e.toString());
            }
            return null;
        }
    }
}
