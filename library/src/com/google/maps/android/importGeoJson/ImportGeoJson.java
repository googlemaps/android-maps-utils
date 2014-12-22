package com.google.maps.android.importGeoJson;

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
import java.util.concurrent.ExecutionException;

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


public class ImportGeoJson {

    private static final String GEOJSON_GEOMETRY_OBJECTS
            = "Point|MultiPoint|LineString|MultiLineString|Polygon|MultiPolygon";

    private final ArrayList<Object> mGeoJsonMapPropertyObjects = new ArrayList<Object>();

    private final ArrayList<Object> mGeoJsonMapObjects = new ArrayList<Object>();

    private final GoogleMap mMap;


    private JSONObject mGeoJsonFile;

    /**
     * Visibility status of the GeoJSON data
     */
    private boolean mIsVisible = true;

    /**
     * Active layer means that the GeoJSON data has been added to the map
     */
    private boolean mActiveLayer = false;

    // TODO: implement fetching files by URL later

    /**
     * Creates a new ImportGeoJson object
     *
     * @param map            map object
     * @param geoJsonFileUrl URL of GeoJSON file
     */
    public ImportGeoJson(GoogleMap map, String geoJsonFileUrl) throws JSONException {
        mMap = map;

        // Currently a bad implementation
        try {
            // Waits for the file to be loaded
            new parseUrlToJson().execute(geoJsonFileUrl).get();
        } catch (InterruptedException e) {
            Log.e("InterruptedException", e.toString());
        } catch (ExecutionException e) {
            Log.e("ExecutionException", e.toString());
        }
        parseGeoJsonFile(mGeoJsonFile);
    }

    /**
     * Creates a new ImportGeoJson object
     *
     * @param map                Map object
     * @param resourceId         Raw resource GeoJSON file
     * @param applicationContext Application context object
     */
    public ImportGeoJson(GoogleMap map, int resourceId, Context applicationContext)
            throws JSONException, IOException {
        mMap = map;
        InputStream stream = applicationContext.getResources().openRawResource(resourceId);
        mGeoJsonFile = createJsonFileObject(stream);
        parseGeoJsonFile(mGeoJsonFile);
    }

    /**
     * Takes a character input stream and converts it into a JSONObject
     *
     * @param stream Character input stream representing  the GeoJSON file
     * @return JSONObject representing the GeoJSON file
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
     */
    private void parseGeoJsonFile(JSONObject geoJsonFile) throws JSONException {
        JSONArray geometriesArray;
        boolean isFeature = geoJsonFile.getString("type").trim().equals("Feature");
        boolean isGeometry = geoJsonFile.getString("type").trim().matches(GEOJSON_GEOMETRY_OBJECTS);
        boolean isFeatureCollection = geoJsonFile.getString("type").trim().equals(
                "FeatureCollection");
        boolean isGeometryCollection = geoJsonFile.getString("type").trim().equals(
                "GeometryCollection");
        if (isFeatureCollection) {
            storeMapObjects(mGeoJsonMapPropertyObjects, parseGeoJsonFeatureCollection(geoJsonFile));
        } else if (isGeometryCollection) {
            geometriesArray = geoJsonFile.getJSONArray("geometries");
            storeMapObjects(mGeoJsonMapPropertyObjects,
                    toGeometryCollection(geometriesArray, null));
        } else if (isFeature) {
            storeMapObjects(mGeoJsonMapPropertyObjects, parseGeoJsonFeature(geoJsonFile));
        } else if (isGeometry) {
            storeMapObjects(mGeoJsonMapPropertyObjects, parseGeoJsonGeometry(geoJsonFile));
        }
    }

    /**
     * Creates a list of properties objects from a given JSONObject containing a feature collection
     *
     * @param featureCollection Feature collection to parse into geometry property objects
     * @return ArrayList of MarkerProperties, PolylineProperties and PolygonProperties
     */
    private ArrayList<Object> parseGeoJsonFeatureCollection(JSONObject featureCollection)
            throws JSONException {
        ArrayList<Object> featureCollectionObjects = new ArrayList<Object>();
        // Gets the array containing the feature collection objects
        JSONArray featureCollectionArray = featureCollection.getJSONArray("features");
        boolean isFeature;
        for (int i = 0; i < featureCollectionArray.length(); i++) {
            isFeature = featureCollectionArray.getJSONObject(i).getString("type").trim()
                    .equals("Feature");
            // Add the single feature to featureCollectionObjects
            if (isFeature) {
                storeMapObjects(featureCollectionObjects,
                        parseGeoJsonFeature(featureCollectionArray.getJSONObject(i)));
            }
        }
        return featureCollectionObjects;
    }

    /**
     * Takes an object and adds it to mapObjectList. In the case of an ArrayList, it adds all the
     * elements to mapObjectList
     *
     * @param mapObjectList list to append all map objects to
     * @param mapObject     object to add to mGeoJsonMapPropertyObjects, either
     *                      MarkerProperties,PolylineProperties, PolygonProperties or an ArrayList
     */
    private void storeMapObjects(List<Object> mapObjectList, Object mapObject) {
        if (mapObject instanceof List) {
            mapObjectList.addAll((ArrayList) mapObject);
        } else {
            mapObjectList.add(mapObject);
        }
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
        mIsVisible = true;
        mActiveLayer = true;
    }

    /**
     * Sets all geometry objects to visible
     * Affects geometry objects with original imported visibility as false
     */
    public void showAllGeoJsonData() {
        for (Object mapObject : mGeoJsonMapObjects) {
            if (mapObject instanceof Marker) {
                ((Marker) mapObject).setVisible(true);
            } else if (mapObject instanceof Polyline) {
                ((Polyline) mapObject).setVisible(true);
            } else if (mapObject instanceof Polygon) {
                ((Polygon) mapObject).setVisible(true);
            }
        }
        mIsVisible = true;
    }

    /**
     * Sets all geometry objects to invisible
     * Affects geometry objects with original imported visibility as true
     */
    public void hideAllGeoJsonData() {
        for (Object mapObject : mGeoJsonMapObjects) {
            if (mapObject instanceof Marker) {
                ((Marker) mapObject).setVisible(false);
            } else if (mapObject instanceof Polyline) {
                ((Polyline) mapObject).setVisible(false);
            } else if (mapObject instanceof Polygon) {
                ((Polygon) mapObject).setVisible(false);
            }
        }
        mIsVisible = false;
    }

    /**
     * Inverts the current visibility of all geometry objects on the map
     * Affects all geometry objects regardless of it being imported with a true or false visibility
     */
    public void invertVisibility() {
        for (Object mapObject : mGeoJsonMapObjects) {
            if (mapObject instanceof Marker) {
                ((Marker) mapObject).setVisible(!((Marker) mapObject).isVisible());
            } else if (mapObject instanceof Polyline) {
                ((Polyline) mapObject).setVisible(!((Polyline) mapObject).isVisible());
            } else if (mapObject instanceof Polygon) {
                ((Polygon) mapObject).setVisible(!((Polygon) mapObject).isVisible());
            }
        }
    }

    /**
     * Toggles the overlay on and off
     * Only affects geometry objects that were imported with visibility set to true
     */
    public void toggleVisibility() {
        mIsVisible = !mIsVisible;
        Object mapObject;
        // Needed to check for the visibility
        Object mapProperty;
        // Check if each object on the map was imported with visibility set to true and toggle
        for (int i = 0; i < mGeoJsonMapObjects.size(); i++) {
            mapObject = mGeoJsonMapObjects.get(i);
            mapProperty = mGeoJsonMapPropertyObjects.get(i);
            if (mapObject instanceof Marker && ((MarkerProperties) mapProperty).getVisibility()) {
                ((Marker) mapObject).setVisible(mIsVisible);
            } else if (mapObject instanceof Polyline && ((PolylineProperties) mapProperty)
                    .getVisibility()) {
                ((Polyline) mapObject).setVisible(mIsVisible);
            } else if (mapObject instanceof Polygon && ((PolygonProperties) mapProperty)
                    .getVisibility()) {
                ((Polygon) mapObject).setVisible(mIsVisible);
            }
        }
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
        mIsVisible = false;
        mActiveLayer = false;

        // Remove all stored map objects
        mGeoJsonMapObjects.clear();
    }

    /**
     * Parses a JSONArray of coordinates into an array of
     * {@link com.google.android.gms.maps.model.LatLng} objects
     *
     * @param geoJsonCoordinates JSONArray of coordinates from the GeoJSON file
     * @return array of {@link com.google.android.gms.maps.model.LatLng} objects representing the
     * coordinates
     */
    private ArrayList<LatLng> coordinatesToLatLngArray(JSONArray geoJsonCoordinates)
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
     */
    private Object parseGeoJsonGeometry(JSONObject geoJsonFeature) throws JSONException {
        String geometryType;
        JSONArray featureCoordinatesArray;
        geometryType = geoJsonFeature.getString("type");
        featureCoordinatesArray = geoJsonFeature.getJSONArray("coordinates");
        return parseGeoJsonGeometryObject(geometryType, featureCoordinatesArray, null);
    }

    /**
     * Parses a single feature from the GeoJSON file into an object for the map has a coordinates
     * array and properties array
     * In the case of a geometry collection, ir will have an array of geometries instead of an array
     * of coordinates
     *
     * @param geoJsonFeature JSONObject containing one feature to parse
     * @return {@link com.google.android.gms.maps.model.MarkerOptions}, {@link
     * com.google.android.gms.maps.model.PolylineOptions} or {@link
     * com.google.android.gms.maps.model.PolygonOptions} object or an array of either Options
     * objects
     */
    private Object parseGeoJsonFeature(JSONObject geoJsonFeature) throws JSONException {
        String geometryType;
        // Contains an array of geometries if the feature is a GeometryCollection and coordinates
        // otherwise
        JSONArray featureArray;
        JSONObject featureProperties;
        // Store the type, coordinates and properties of the GeoJSON feature
        geometryType = geoJsonFeature.getJSONObject("geometry").getString("type");

        if (geometryType.equals("GeometryCollection")) {
            featureArray = geoJsonFeature.getJSONObject("geometry").getJSONArray("geometries");
        } else {
            featureArray = geoJsonFeature.getJSONObject("geometry").getJSONArray("coordinates");
        }
        featureProperties = geoJsonFeature.getJSONObject("properties");
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
    private Object parseGeoJsonGeometryObject(String geometryType,
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
        } else if (geometryType.equals("GeometryCollection")) {
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
     */
    private MarkerProperties toMarker(JSONArray geoJsonPointCoordinatesArray,
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
     */
    private ArrayList<MarkerProperties> toMarkers(JSONArray geoJsonMultiPointCoordinatesArray,
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
     */
    private PolylineProperties toPolyline(JSONArray geoJsonLineStringCoordinatesArray,
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
     */
    private ArrayList<PolylineProperties> toPolylines(
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
     */
    private PolygonProperties toPolygon(JSONArray geoJsonPolygonCoordinatesArray,
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
     */
    private ArrayList<PolygonProperties> toPolygons(JSONArray geoJsonMultiPolygonCoordinatesArray,
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
     */
    private ArrayList<Object> toGeometryCollection(JSONArray geoJsonGeometryCollectionArray,
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
            isGeometry = geometryCollectionType.matches(GEOJSON_GEOMETRY_OBJECTS);
            isGeometryCollection = geometryCollectionType.equals("GeometryCollection");

            // Add new geometry object to geometryCollectionObjects
            if (isGeometry) {
                coordinatesArray = geometryCollectionElement.getJSONArray("coordinates");
                // Pass in type, coordinates and properties of the current geometry
                storeMapObjects(geometryCollectionObjects,
                        parseGeoJsonGeometryObject(geometryCollectionType, coordinatesArray,
                                geoJsonCollectionProperties));
            }
            // Recursively parse all elements and add to geometryCollectionObjects array
            else if (isGeometryCollection) {
                geometriesArray = geometryCollectionElement.getJSONArray("geometries");
                storeMapObjects(geometryCollectionObjects,
                        toGeometryCollection(geometriesArray, geoJsonCollectionProperties));
            }
        }
        return geometryCollectionObjects;
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
                mGeoJsonFile = createJsonFileObject(stream);

            } catch (IOException e) {
                Log.e("IOException", e.toString());
            } catch (JSONException e) {
                Log.e("JSONException", e.toString());
            }
            return null;
        }
    }
}
