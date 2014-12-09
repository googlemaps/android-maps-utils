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
import java.util.concurrent.ExecutionException;

/**
 * Created by juliawong on 12/2/14.
 * Imports the given GeoJSON file and adds all GeoJSON objects to the map
 */
public class ImportGeoJson {

    // TODO: cater for GeometryCollection
    // TODO: return newly added map objects
    private JSONObject mGeoJsonFile;

    private final GoogleMap mMap;

    private ArrayList<Object> mGeoJsonMapObjects = new ArrayList<Object>();

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
            }

            return null;
        }
    }

    // TODO: implement fetching files by URL later

    /**
     * Creates a new ImportGeoJson object
     *
     * @param map            map object
     * @param geoJsonFileUrl URL of GeoJSON file
     */
    public ImportGeoJson(GoogleMap map, String geoJsonFileUrl) {
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
    }

    /**
     * Creates a new ImportGeoJson object
     *
     * @param map                Map object
     * @param resourceId         Raw resource GeoJSON file
     * @param applicationContext Application context object
     */
    public ImportGeoJson(GoogleMap map, int resourceId, Context applicationContext) {
        mMap = map;
        // Creates the character input stream
        InputStream stream = applicationContext.getResources().openRawResource(resourceId);
        // Convert stream to JSONObject
        mGeoJsonFile = createJsonFileObject(stream);
        parseGeoJsonFile();
    }

    /**
     * Takes a character input stream and converts it into a JSONObject
     *
     * @param stream Character input stream representing  the GeoJSON file
     * @return JSONObject representing the GeoJSON file
     */
    private JSONObject createJsonFileObject(InputStream stream) {
        String line;
        StringBuilder result = new StringBuilder();
        // Reads from stream
        BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
        // Read each line of the GeoJSON file into a string
        try {
            while ((line = reader.readLine()) != null) {
                result.append(line);
            }
        } catch (IOException e) {
            Log.e("IOException", e.toString());
        }

        // Converts the result string into a JSONObject
        try {
            return new JSONObject(result.toString());
        } catch (JSONException e) {
            Log.e("JSONException", e.toString());
        }
        return null;
    }

    /**
     * Parses all GeoJSON objects in the GeoJSON file
     */
    private void parseGeoJsonFile() {
        JSONArray jsonFeaturesArray;
        if (checkIfFeatureCollection(mGeoJsonFile)) {
            try {
                // Store the list of GeoJSON feature object
                jsonFeaturesArray = mGeoJsonFile.getJSONArray("features");
                for (int i = 0; i < jsonFeaturesArray.length(); i++) {
                    // Check if each element in the array has type feature
                    if (checkIfFeature(jsonFeaturesArray.getJSONObject(i))) {
                        // Parse element from array into a Google Maps object
                        parseGeoJsonFeature(jsonFeaturesArray.getJSONObject(i));
                    }
                }
            } catch (JSONException e) {
                Log.e("JSONException", e.toString());
            }

        }
        // Check if object has type feature
        else if (checkIfFeature(mGeoJsonFile)) {
            // Parse object into a Google Maps object
            parseGeoJsonFeature(mGeoJsonFile);
        }
    }

    /**
     * Checks if the given JSONObject contains a feature collection
     *
     * @param geoJsonObj JSONObject to check
     * @return true if file contains a feature collection, otherwise false
     */
    private boolean checkIfFeatureCollection(JSONObject geoJsonObj) {
        try {
            // Ignore case and strip whitespace from the value which has key field "type"
            return geoJsonObj.getString("type").toLowerCase().trim().equals("featurecollection");
        } catch (JSONException e) {
            Log.e("JSONException", e.toString());
        }
        return false;
    }

    /**
     * Checks if the given JSONObject contains a feature
     *
     * @param geoJsonObj JSONObject to check
     * @return true if file contains a feature, otherwise false
     */
    private boolean checkIfFeature(JSONObject geoJsonObj) {
        try {
            // Ignore case and strip whitespace from the value which has key field "type"
            return geoJsonObj.getString("type").toLowerCase().trim().equals("feature");
        } catch (JSONException e) {
            Log.e("JSONException", e.toString());
        }
        return false;
    }

    /**
     * Parses a JSONArray containing a coordinate into a single
     * {@link com.google.android.gms.maps.model.LatLng} object
     *
     * @param geoJsonCoordinates JSONArray containing a coordinate from the GeoJSON file
     * @return {@link com.google.android.gms.maps.model.LatLng} object representing the coordinate
     */
    private LatLng coordinateToLatLngArray(JSONArray geoJsonCoordinates) {
        try {
            // GeoJSON stores coordinates as lng, lat so need to reverse
            return new LatLng(geoJsonCoordinates.getDouble(1),
                    geoJsonCoordinates.getDouble(0));

        } catch (JSONException e) {
            Log.e("JSONException", e.toString());
        }
        return null;
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
     * Parses a single feature from the GeoJSON file into an object for the map
     *
     * @param geoJsonFeature JSONObject containing one feature
     * @return Map model object created or the array of map model objects created
     */
    private Object parseGeoJsonFeature(JSONObject geoJsonFeature) {
        String geometryType = null;
        JSONArray featureCoordinatesArray;
        JSONObject featureProperties;
        try {
            // Store the type, coordinates and properties of the GeoJSON feature
            geometryType = geoJsonFeature.getJSONObject("geometry").getString("type").toLowerCase();
            featureCoordinatesArray = geoJsonFeature.getJSONObject("geometry")
                    .getJSONArray("coordinates");
            featureProperties = geoJsonFeature.getJSONObject("properties");

            // Pass the coordinates and properties to the correct type of geometry
            if (geometryType.equals("point")) {
                return toMarker(featureCoordinatesArray, featureProperties);
            } else if (geometryType.equals("multipoint")) {
                return toMarkers(featureCoordinatesArray, featureProperties);
            } else if (geometryType.equals("linestring")) {
                return toPolyline(featureCoordinatesArray, featureProperties);
            } else if (geometryType.equals("multilinestring")) {
                return toPolylines(featureCoordinatesArray, featureProperties);
            } else if (geometryType.equals("polygon")) {
                return toPolygon(featureCoordinatesArray, featureProperties);
            } else if (geometryType.equals("multipolygon")) {
                return toPolygons(featureCoordinatesArray, featureProperties);
            }
        } catch (JSONException e) {
            Log.e("JSONException", e.toString());
        }

        // Type didn't match any existing objects
        return null;
    }

    /**
     * Creates a new map {@link com.google.android.gms.maps.model.Marker} object based on the
     * existing coordinates and properties
     *
     * @param geoJsonPointProperties JSONObject containing the Point GeoJSON object
     * @return new map {@link com.google.android.gms.maps.model.Marker} model object
     */
    private Marker toMarker(JSONArray geoJsonPointCoordinatesArray,
            JSONObject geoJsonPointProperties) {
        LatLng coordinates = coordinateToLatLngArray(geoJsonPointCoordinatesArray);
        MarkerProperties properties = null;
        // Get the marker properties
        try {
            properties = new MarkerProperties(geoJsonPointProperties);
        } catch (JSONException e) {
            Log.e("JSONException", e.toString());
        }
        // TODO: return marker + add to map
        return null;
    }

    /**
     * Creates new map {@link com.google.android.gms.maps.model.Marker} objects based on the
     * existing coordinates and properties
     *
     * @param geoJsonMultiPointProperties JSONObject containing the MultiPoint GeoJSON object
     * @return an array of new map {@link com.google.android.gms.maps.model.Marker} model objects
     */
    private ArrayList<Marker> toMarkers(JSONArray geoJsonMultiPointCoordinatesArray,
            JSONObject geoJsonMultiPointProperties) {
        ArrayList<Marker> markers = new ArrayList<Marker>();
        // Iterate over the list of points
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
     * Creates a new {@link com.google.android.gms.maps.model.Polyline} object based on the
     * existing coordinates and properties
     *
     * @param geoJsonLineStringProperties JSONObject containing the LineString GeoJSON object
     * @return new {@link com.google.android.gms.maps.model.Polyline} model object
     */
    private Polyline toPolyline(JSONArray geoJsonLineStringCoordinatesArray,
            JSONObject geoJsonLineStringProperties) {
        ArrayList<LatLng> coordinates = coordinatesToLatLngArray(geoJsonLineStringCoordinatesArray);
        PolylineProperties properties = null;
        // Get polyline properties
        try {
            properties = new PolylineProperties(geoJsonLineStringProperties);
        } catch (JSONException e) {
            Log.e("JSONException", e.toString());
        }

        // TODO: return polyline + add to map
        return null;
    }

    /**
     * Creates new {@link com.google.android.gms.maps.model.Polyline} objects based on the existing
     * coordinates and properties
     *
     * @param geoJsonMultiLineStringProperties JSONObject containing the MultiLineString GeoJSON
     *                                         object
     * @return an array of new {@link com.google.android.gms.maps.model.Polyline} model objects
     */
    private ArrayList<Polyline> toPolylines(JSONArray geoJsonMultiLineStringCoordinatesArray,
            JSONObject geoJsonMultiLineStringProperties) {
        ArrayList<Polyline> polylines = new ArrayList<Polyline>();
        // Iterate over the list of multilines
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
     * Creates a new {@link com.google.android.gms.maps.model.Polygon} object based on the existing
     * coordinates and properties
     *
     * @param geoJsonPolygonProperties JSONObject containing the Polygon GeoJSON object
     * @return new {@link com.google.android.gms.maps.model.Polygon} model object
     */
    private Polygon toPolygon(JSONArray geoJsonPolygonCoordinatesArray,
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

        PolygonProperties properties = null;
        // Get the polygon properties
        try {
            properties = new PolygonProperties(geoJsonPolygonProperties);
        } catch (JSONException e) {
            Log.e("JSONException", e.toString());
        }
        // TODO: return polygon + add to map
        return null;
    }

    /**
     * Creates new {@link com.google.android.gms.maps.model.Polygon} objects based on the existing
     * coordinates and properties
     *
     * @param geoJsonMultiPolygonProperties JSONObject containing the Polygon GeoJSON object
     * @return an array of new {@link com.google.android.gms.maps.model.Polygon} model objects
     */
    private ArrayList<Polygon> toPolygons(JSONArray geoJsonMultiPolygonCoordinatesArray,
            JSONObject geoJsonMultiPolygonProperties) {
        ArrayList<Polygon> polygons = new ArrayList<Polygon>();
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

}
