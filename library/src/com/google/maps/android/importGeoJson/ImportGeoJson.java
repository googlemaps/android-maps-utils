package com.google.maps.android.importGeoJson;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.Polyline;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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
public class ImportGeoJson{
// TODO return newly added map objects
    private JSONObject geojson_file;
    private ArrayList<Object> newGeoJsonObjects = new ArrayList<Object>();

    /**
     * Downloads the GeoJSON file from the given URL
     */
    private class parseUrlToJson extends AsyncTask<String, Void, Void> {
        /**
         * Downloads the file and store the GeoJSON object
         * @param params First parameter is the URL of the GeoJSON file to download
         */
        @Override
        protected Void doInBackground(String... params) {
            try {
                // Creates the character input stream
                InputStream stream = new URL(params[0]).openConnection().getInputStream();
                // Convert stream to JSONObject
                geojson_file = createJsonFileObject(stream);

            } catch (IOException e) {
                Log.e("IOException", e.toString());
            }

            return null;
        }
    }

    /**
     * Creates a new ImportGeoJson object
     * @param map map object
     * @param geojson_file_url URL of GeoJSON file
     */
    public ImportGeoJson(GoogleMap map, String geojson_file_url)  {
        try {
            // Waits for the file to be loaded
            new parseUrlToJson().execute(geojson_file_url).get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
    }

    /**
     * Creates a new ImportGeoJson object
     * @param map Map object
     * @param resource_id Raw resource GeoJSON file
     * @param applicationContext Application context object
     */
    public ImportGeoJson(GoogleMap map, int resource_id, Context applicationContext) {
        // Creates the character input stream
        InputStream stream = applicationContext.getResources().openRawResource(resource_id);
        // Convert stream to JSONObject
        geojson_file = createJsonFileObject(stream);
    }

    /**
     * Takes a character input stream and converts it into a JSONObject
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
        if (checkIfFeatureCollection()) {
            // parse JSONArray
            // parseGeoJsonFeature(...)
            // add new object to newGeoJsonObjects
        }
        // TODO check if feature
        else {
            parseGeoJsonFeature(geojson_file);
        }
    }

    /**
     * Checks if GeoJSON file contains a feature collection
     * @return true if file contains a feature collection, otherwise false
     */
    private boolean checkIfFeatureCollection() {
        try {
            return geojson_file.getString("type").toLowerCase().equals("featurecollection");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Parses a JSONArray containing a coordinate into a single
     * {@link com.google.android.gms.maps.model.LatLng} object
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
     * @param geoJsonCoordinates JSONArray of coordinates from the GeoJSON file
     * @return array of {@link com.google.android.gms.maps.model.LatLng}
     *                                objects representing the coordinates
     */
    private ArrayList<LatLng> coordinatesToLatLngArray(JSONArray geoJsonCoordinates) {
        JSONArray json_coords;
        JSONArray json_coord;
        ArrayList<LatLng> coordinatesArray = new ArrayList<LatLng>();
        // Iterate over the array of arrays of coordinates
        for(int i = 0; i < geoJsonCoordinates.length(); i++) {
            try {
                json_coords = geoJsonCoordinates.getJSONArray(i);
                // Iterate over the array of coordinates
                for (int j = 0; j < json_coords.length(); j++) {
                    json_coord = json_coords.getJSONArray(j);
                    // GeoJSON stores coordinates as lng, lat so need to reverse
                    coordinatesArray.add(new LatLng(json_coord.getDouble(1), json_coord.getDouble(0)));
                }
            } catch (JSONException e) {
                Log.e("JSONException", e.toString());
            }
        }
        return coordinatesArray;
    }

    /**
     * Parses a single feature from the GeoJSON file into an object for the map
     * @param geoJsonFeature JSONObject containing one feature
     * @return Map model object created or the array of map model objects created
     */
    private Object parseGeoJsonFeature(JSONObject geoJsonFeature) {
        // TODO convert coords and properties
        // Extract properties
        // Extract GeoJSON object into an array of LatLng
        // swap x, y coords
        // Create & return GeoJSON object

//        try {
//            switch (lowercaseType) {
//                case 'point':
//                    return toMarker(geoJsonFeature);
//
//                case 'multipoint':
//                    return toMarkers(geoJsonFeature);
//
//                case 'linestring':
//                    return toPolyline(geoJsonFeature);
//
//                case 'multilinestring':
//                    return toPolylines(geoJsonFeature);
//
//                case 'polygon':
//                    return toPolygon(geoJsonFeature);
//
//                case 'multipolygon':
//                    return toPolygons(geoJsonFeature);
//            }
//        } catch (error) {
//            // throw errors here
//        }
        return null;
    }

    /**
     * Creates a new map {@link com.google.android.gms.maps.model.Marker} object based on the
     * existing coordinates and properties
     * @param geoJsonPoint JSONObject containing the Point GeoJSON object
     * @return new map {@link com.google.android.gms.maps.model.Marker} model object
     */
    private Marker toMarker(JSONObject geoJsonPoint) {
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
     * @param geoJsonMultiPoint JSONObject containing the MultiPoint GeoJSON object
     * @return an array of new map {@link com.google.android.gms.maps.model.Marker} model objects
     */
    private ArrayList<Marker> toMarkers(JSONObject geoJsonMultiPoint) {
        // parse GeoJson multipoint into multiple Maps Markers
        // Return an array of these new Markers
        // Call toMarker(...)
        return null;
    }

    /**
     * Creates a new {@link com.google.android.gms.maps.model.Polyline} object based on the existing
     * coordinates and properties
     * @param geoJsonLineString JSONObject containing the LineString GeoJSON object
     * @return new {@link com.google.android.gms.maps.model.Polyline} model object
     */
    private Polyline toPolyline(JSONObject geoJsonLineString) {
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
     * @param geoJsonMultiLineString JSONObject containing the MultiLineString GeoJSON object
     * @return an array of new {@link com.google.android.gms.maps.model.Polyline} model objects
     */
    private ArrayList<Polyline> toPolylines(JSONObject geoJsonMultiLineString) {
        // parse GeoJson linestring into multiple Maps Polylines
        // Return an array of these new Polylines
        // Call toPolyline(...)
        return null;
    }

    /**
     * Creates a new {@link com.google.android.gms.maps.model.Polygon} object based on the existing
     * coordinates and properties
     * @param geoJsonPolygon JSONObject containing the Polygon GeoJSON object
     * @return new {@link com.google.android.gms.maps.model.Polygon} model object
     */
    private Polygon toPolygon(JSONObject geoJsonPolygon) {
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
     * @param geoJsonMultiPolygon JSONObject containing the Polygon GeoJSON object
     * @return an array of new {@link com.google.android.gms.maps.model.Polygon} model objects
     */
    private ArrayList<Polygon> toPolygons(JSONObject geoJsonMultiPolygon) {
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

