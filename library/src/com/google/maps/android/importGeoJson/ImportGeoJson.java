package com.google.maps.android.importGeoJson;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

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

    private final ArrayList<Object> mGeoJsonMapOptionObjects = new ArrayList<Object>();

    private final ArrayList<Object> mGeoJsonMapObjects = new ArrayList<Object>();

    private final GoogleMap mMap;

    private JSONObject mGeoJsonFile;

    private boolean mIsVisible = true;

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
     * Parses all GeoJSON objects in the GeoJSON file
     */
    /**
     * Parses all GeoJSON objects in the GeoJSON file and adds them to mGeoJsonMapOptionObjects
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
            storeMapObjects(mGeoJsonMapOptionObjects, parseGeoJsonFeatureCollection(geoJsonFile));
        } else if (isGeometryCollection) {
            geometriesArray = geoJsonFile.getJSONArray("geometries");
            storeMapObjects(mGeoJsonMapOptionObjects, toGeometryCollection(geometriesArray, null));
        }
        // Single feature in the JSONObject
        else if (isFeature) {
            storeMapObjects(mGeoJsonMapOptionObjects, parseGeoJsonFeature(geoJsonFile));
        } else if (isGeometry) {
            storeMapObjects(mGeoJsonMapOptionObjects, parseGeoJsonGeometry(geoJsonFile));
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
     * @param mapObject     object to add to mGeoJsonMapOptionObjects, either
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
     * Adds all objects in mGeoJsonMapOptionObjects to the mMap
     */
    public void addGeoJsonData() {
        for (Object mapObject : mGeoJsonMapOptionObjects) {
            if (mapObject instanceof MarkerOptions) {
                mGeoJsonMapObjects.add(mMap.addMarker((MarkerOptions) mapObject));
            } else if (mapObject instanceof PolylineOptions) {
                mGeoJsonMapObjects.add(mMap.addPolyline((PolylineOptions) mapObject));
            } else if (mapObject instanceof PolygonOptions) {
                mGeoJsonMapObjects.add(mMap.addPolygon((PolygonOptions) mapObject));
            }
        }
        mIsVisible = true;
    }

    /**
     * Inverts the visibility of all features on the map
     */
    public void toggleGeoJsonData() {
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
     * Removes all objects in mGeoJsonMapOptionObjects from the mMap
     */
    public void removeGeoJsonData() {
        mIsVisible = false;
        for (Object mapObject : mGeoJsonMapObjects) {
            if (mapObject instanceof Marker) {
                ((Marker) mapObject).remove();
            } else if (mapObject instanceof Polyline) {
                ((Polyline) mapObject).remove();
            } else if (mapObject instanceof Polygon) {
                ((Polygon) mapObject).remove();
            }
        }
    }

    /**
     * Parses a JSONArray of coordinates into an array of
     * {@link com.google.android.gms.maps.model.LatLng} objects
     *
     * @param geoJsonCoordinates JSONArray of coordinates from the GeoJSON file
     * @return array of {@link com.google.android.gms.maps.model.LatLng} objects representing the
     * coordinates
     */
    private ArrayList<LatLng> coordinatesToLatLngArray(JSONArray geoJsonCoordinates) {
        JSONArray jsonCoordinate;
        ArrayList<LatLng> coordinatesArray = new ArrayList<LatLng>();
        // Iterate over the array of coordinates
        for (int i = 0; i < geoJsonCoordinates.length(); i++) {
            try {
                jsonCoordinate = geoJsonCoordinates.getJSONArray(i);
                // GeoJSON stores coordinates as lng, lat so need to reverse
                coordinatesArray.add(new LatLng(jsonCoordinate.getDouble(1),
                        jsonCoordinate.getDouble(0)));
            } catch (JSONException e) {
                Log.e("JSONException", e.toString());
            }
        }
        return coordinatesArray;
    }

    /**
     * Converts a single geometry object into its relevant Google Map object
     *
     * @param geometryType      type of geometry object
     * @param featureArray      array of coordinates or geometries
     * @param featureProperties properties of the geometry object
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
            //TODO: why aren't styles being applied?
            return toGeometryCollection(featureArray, featureProperties);
        }
        return null;
    }

    /**
     * Parses the JSONObject of a single geometry for type, properties and coordinates
     * Geometry objects have member coordinates but not properties like feature objects do
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
     * Parses a single feature from the GeoJSON file into an object for the map
     *
     * @param geoJsonFeature JSONObject containing one feature
     * @return {@link com.google.android.gms.maps.model.MarkerOptions}, {@link
     * com.google.android.gms.maps.model.PolylineOptions} or {@link
     * com.google.android.gms.maps.model.PolygonOptions} object or an array of either Options
     * objects
     */
    private Object parseGeoJsonFeature(JSONObject geoJsonFeature) {
        String geometryType;
        // Contains an array of geometries if the feature is a GeometryCollection and coordinates
        // otherwise
        JSONArray featureArray;
        JSONObject featureProperties;
        try {
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
        } catch (JSONException e) {
            Log.e("JSONException", e.toString());
        }

        return null;
    }

    /**
     * Creates a new {@link com.google.android.gms.maps.model.MarkerOptions} object based on
     * the given coordinates and properties
     *
     * @param geoJsonPointCoordinatesArray JSONArray containing coordinates of the GeoJSON Point
     *                                     object
     * @param geoJsonPointProperties       JSONObject containing the GeoJSON Point object
     *                                     properties
     * @return new map {@link com.google.android.gms.maps.model.MarkerOptions} object
     */
    private MarkerOptions toMarker(JSONArray geoJsonPointCoordinatesArray,
            JSONObject geoJsonPointProperties) {
        MarkerOptions properties = null;
        try {
            LatLng coordinates = new LatLng(geoJsonPointCoordinatesArray.getDouble(1),
                    geoJsonPointCoordinatesArray.getDouble(0));
            properties = new MarkerProperties(geoJsonPointProperties, coordinates)
                    .getMarkerOptions();
        } catch (JSONException e) {
            Log.e("JSONException", e.toString());
        }
        return properties;
    }

    /**
     * Creates new {@link com.google.android.gms.maps.model.MarkerOptions} objects based on the
     * given coordinates and properties
     *
     * @param geoJsonMultiPointCoordinatesArray JSONArray containing coordinates of the GeoJSON
     *                                          MultiPoint object
     * @param geoJsonMultiPointProperties       JSONObject containing the MultiPoint GeoJSON object
     *                                          properties
     * @return array of new map {@link com.google.android.gms.maps.model.MarkerOptions} objects
     */
    private ArrayList<MarkerOptions> toMarkers(JSONArray geoJsonMultiPointCoordinatesArray,
            JSONObject geoJsonMultiPointProperties) {
        ArrayList<MarkerOptions> markers = new ArrayList<MarkerOptions>();
        for (int i = 0; i < geoJsonMultiPointCoordinatesArray.length(); i++) {
            try {
                // Add each marker to the list
                markers.add(toMarker(geoJsonMultiPointCoordinatesArray.getJSONArray(i),
                        geoJsonMultiPointProperties));
            } catch (JSONException e) {
                Log.e("JSONException", e.toString());
            }
        }
        return markers;
    }

    /**
     * Creates a new {@link com.google.android.gms.maps.model.PolylineOptions} object based on the
     * existing coordinates and properties
     *
     * @param geoJsonLineStringCoordinatesArray JSONArray containing coordinates of the GeoJSON
     *                                          LineString object
     * @param geoJsonLineStringProperties       JSONObject containing the LineString GeoJSON object
     *                                          properties
     * @return new {@link com.google.android.gms.maps.model.PolylineOptions} object
     */
    private PolylineOptions toPolyline(JSONArray geoJsonLineStringCoordinatesArray,
            JSONObject geoJsonLineStringProperties) {
        ArrayList<LatLng> coordinates = coordinatesToLatLngArray(geoJsonLineStringCoordinatesArray);
        PolylineOptions properties = null;
        // Get polyline properties
        try {
            properties = new PolylineProperties(geoJsonLineStringProperties, coordinates)
                    .getPolylineOptions();
        } catch (JSONException e) {
            Log.e("JSONException", e.toString());
        }

        return properties;
    }

    /**
     * Creates new {@link com.google.android.gms.maps.model.PolylineOptions} objects based on the
     * existing coordinates and properties
     *
     * @param geoJsonMultiLineStringCoordinatesArray JSONArray containing coordinates of the
     *                                               GeoJSON
     *                                               MultiLineString object
     * @param geoJsonMultiLineStringProperties       JSONObject containing the MultiLineString
     *                                               GeoJSON object properties
     * @return array of new {@link com.google.android.gms.maps.model.PolylineOptions} objects
     */
    private ArrayList<PolylineOptions> toPolylines(JSONArray geoJsonMultiLineStringCoordinatesArray,
            JSONObject geoJsonMultiLineStringProperties) {
        ArrayList<PolylineOptions> polylines = new ArrayList<PolylineOptions>();
        // Iterate over the list of polylines
        for (int i = 0; i < geoJsonMultiLineStringCoordinatesArray.length(); i++) {
            try {
                // Add each polyline to the list
                polylines.add(toPolyline(geoJsonMultiLineStringCoordinatesArray.getJSONArray(i),
                        geoJsonMultiLineStringProperties));
            } catch (JSONException e) {
                Log.e("JSONException", e.toString());
            }
        }
        return polylines;
    }

    /**
     * Creates a new {@link com.google.android.gms.maps.model.PolygonOptions} object based on the
     * existing coordinates and properties
     *
     * @param geoJsonPolygonCoordinatesArray JSONArray containing coordinates of the GeoJSON
     *                                       Polygon object
     * @param geoJsonPolygonProperties       JSONObject containing the Polygon GeoJSON object
     *                                       properties
     * @return new {@link com.google.android.gms.maps.model.PolygonOptions} object
     */
    private PolygonOptions toPolygon(JSONArray geoJsonPolygonCoordinatesArray,
            JSONObject geoJsonPolygonProperties) {
        // All elements except the first are holes
        ArrayList<ArrayList<LatLng>> coordinates = new ArrayList<ArrayList<LatLng>>();
        // Iterate over the list of coordinates for the polygon
        for (int i = 0; i < geoJsonPolygonCoordinatesArray.length(); i++) {
            try {
                // Add each group of coordinates to the list
                coordinates.add(
                        coordinatesToLatLngArray(geoJsonPolygonCoordinatesArray.getJSONArray(i)));
            } catch (JSONException e) {
                Log.e("JSONException", e.toString());
            }
        }

        PolygonOptions properties = null;
        // Get the polygon properties
        try {
            properties = new PolygonProperties(geoJsonPolygonProperties, coordinates)
                    .getPolygonOptions();
        } catch (JSONException e) {
            Log.e("JSONException", e.toString());
        }

        return properties;
    }

    /**
     * Creates new {@link com.google.android.gms.maps.model.PolygonOptions} objects based on the
     * existing
     * coordinates and properties
     *
     * @param geoJsonMultiPolygonCoordinatesArray JSONArray containing coordinates of the GeoJSON
     *                                            MultiPolygon object
     * @param geoJsonMultiPolygonProperties       JSONObject containing the MultiPolygon GeoJSON
     *                                            object properties
     * @return array of new {@link com.google.android.gms.maps.model.PolygonOptions} objects
     */
    private ArrayList<PolygonOptions> toPolygons(JSONArray geoJsonMultiPolygonCoordinatesArray,
            JSONObject geoJsonMultiPolygonProperties) {
        ArrayList<PolygonOptions> polygons = new ArrayList<PolygonOptions>();
        // Iterate over the list of polygons
        for (int i = 0; i < geoJsonMultiPolygonCoordinatesArray.length(); i++) {
            try {
                // Add each polygon to the list
                polygons.add(toPolygon(geoJsonMultiPolygonCoordinatesArray.getJSONArray(i),
                        geoJsonMultiPolygonProperties));
            } catch (JSONException e) {
                Log.e("JSONException", e.toString());
            }
        }
        return polygons;
    }

    /**
     * Creates a new array of various MarkerOptions, PolylineOptions, PolygonOptions based on
     * existing coordinates and properties
     *
     * @param geoJsonGeometryCollectionArray JSONArray containing the geometries of the
     *                                       GeometryCollection
     * @param geoJsonCollectionProperties    JSONObject containing the GeometryCollection
     *                                       properties
     * @return array of various MarkerOptions, PolylineOptions, PolygonOptions
     */
    private ArrayList<Object> toGeometryCollection(JSONArray geoJsonGeometryCollectionArray,
            JSONObject geoJsonCollectionProperties) throws JSONException {
        ArrayList<Object> geometryCollectionObjects = new ArrayList<Object>();
        boolean isGeometry;
        boolean isGeometryCollection;
        JSONObject geometryCollectionElement;
        String geometryCollectionType;
        JSONArray geometriesArray;
        JSONArray coordinatesArray;

        // Iterate over all the elements in the geometry collection
        for (int i = 0; i < geoJsonGeometryCollectionArray.length(); i++) {
            geometryCollectionElement = geoJsonGeometryCollectionArray.getJSONObject(i);
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
