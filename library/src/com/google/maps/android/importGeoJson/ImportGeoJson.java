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
        try {
            // TODO: Decide whether to allow for JSONObject
            // Waits for the file to be loaded
            new parseUrlToJson().execute(geoJsonFileUrl).get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
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
            e.printStackTrace();
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
            e.printStackTrace();
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
            e.printStackTrace();
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
        JSONArray jsonCoordinates;
        JSONArray jsonCoordinate;
        ArrayList<LatLng> coordinatesArray = new ArrayList<LatLng>();
        // Iterate over the array of arrays of coordinates
        for (int i = 0; i < geoJsonCoordinates.length(); i++) {
            try {
                jsonCoordinates = geoJsonCoordinates.getJSONArray(i);
                // Iterate over the array of coordinates
                for (int j = 0; j < jsonCoordinates.length(); j++) {
                    jsonCoordinate = jsonCoordinates.getJSONArray(j);
                    // GeoJSON stores coordinates as lng, lat so need to reverse
                    coordinatesArray.add(
                            new LatLng(jsonCoordinate.getDouble(1), jsonCoordinate.getDouble(0)));
                }
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
     * @param geoJsonPoint JSONObject containing the Point GeoJSON object
     * @return new map {@link com.google.android.gms.maps.model.Marker} model object
     */
    private Marker toMarker(JSONArray featureCoordinatesArray, JSONObject geoJsonPoint) {
        // parse GeoJson point obj into Maps Marker
        // Call coordinateToLatLngArray(...)
        // Properties:
        /*
        id
        alpha
        icon
        anchor
        draggable
        rotation
        visible */

        return null;
    }

    /**
     * Creates new map {@link com.google.android.gms.maps.model.Marker} objects based on the
     * existing coordinates and properties
     *
     * @param geoJsonMultiPoint JSONObject containing the MultiPoint GeoJSON object
     * @return an array of new map {@link com.google.android.gms.maps.model.Marker} model objects
     */
    private ArrayList<Marker> toMarkers(JSONArray featureCoordinatesArray,
            JSONObject geoJsonMultiPoint) {
        // parse GeoJson multipoint into multiple Maps Markers
        // Return an array of these new Markers
        // Call toMarker(...)
        return null;
    }

    /**
     * Creates a new {@link com.google.android.gms.maps.model.Polyline} object based on the
     * existing
     * coordinates and properties
     *
     * @param geoJsonLineString JSONObject containing the LineString GeoJSON object
     * @return new {@link com.google.android.gms.maps.model.Polyline} model object
     */
    private Polyline toPolyline(JSONArray featureCoordinatesArray, JSONObject geoJsonLineString) {
        // parse GeoJson linestring obj into Maps Polyline
        // Call coordinatesToLatLngArray(...)
        // All elements except for first are holes

        // Properties:
        /*
        id
        width
        color
        z-index
        visible
        geodesic */

        return null;
    }

    /**
     * Creates new {@link com.google.android.gms.maps.model.Polyline} objects based on the existing
     * coordinates and properties
     *
     * @param geoJsonMultiLineString JSONObject containing the MultiLineString GeoJSON object
     * @return an array of new {@link com.google.android.gms.maps.model.Polyline} model objects
     */
    private ArrayList<Polyline> toPolylines(JSONArray featureCoordinatesArray,
            JSONObject geoJsonMultiLineString) {
        // parse GeoJson linestring into multiple Maps Polylines
        // Return an array of these new Polylines
        // Call toPolyline(...)
        return null;
    }

    /**
     * Creates a new {@link com.google.android.gms.maps.model.Polygon} object based on the existing
     * coordinates and properties
     *
     * @param geoJsonPolygon JSONObject containing the Polygon GeoJSON object
     * @return new {@link com.google.android.gms.maps.model.Polygon} model object
     */
    private Polygon toPolygon(JSONArray featureCoordinatesArray, JSONObject geoJsonPolygon) {
        // parse GeoJson polygon obj into Maps Polygon
        // Call coordinatesToLatLngArray(...)

        // Properties:
        /*
        id
        stroke width
        stroke color
        fill color
        z-index
        visible
        geodesic */

        return null;
    }

    /**
     * Creates new {@link com.google.android.gms.maps.model.Polygon} objects based on the existing
     * coordinates and properties
     *
     * @param geoJsonMultiPolygon JSONObject containing the Polygon GeoJSON object
     * @return an array of new {@link com.google.android.gms.maps.model.Polygon} model objects
     */
    private ArrayList<Polygon> toPolygons(JSONArray featureCoordinatesArray,
            JSONObject geoJsonMultiPolygon) {
        // parse GeoJson polygon into multiple Maps Polygon
        // Return an array of these new Polygons
        // Call toPolygons(...)
        return null;
    }

//    @Override
//    protected JSONObject doInBackground(String... params) {
//        try {
//            InputStream stream = new URL(params[0]).openConnection().getInputStream();
//            BufferedReader reader = new BufferedReader(new InputStreamReader(stream, "UTF-8"));
//            StringBuilder result = new StringBuilder();
//            String line;
//            while ((line = reader.readLine()) != null) {
//                result.append(line);
//            }
//            Log.i("JSON FILE", result.toString());
//        } catch (MalformedURLException e) {
//            e.printStackTrace();
//        } catch (UnsupportedEncodingException e) {
//            e.printStackTrace();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        Log.i("JSON FILE", "END");
//        return null;
//    }
}

