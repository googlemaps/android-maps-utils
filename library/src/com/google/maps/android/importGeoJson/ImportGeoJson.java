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
import android.graphics.Color;
import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

/**
 * Created by juliawong on 12/2/14.
 * Imports the given GeoJSON file and adds all GeoJSON objects to the map
 */

/*          _
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


    private JSONObject mGeoJsonFile;
    private ArrayList<Object> mGeoJsonMapObjects = new ArrayList<Object>();
    private  GoogleMap mMap;


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
    public ImportGeoJson(GoogleMap map, int resourceId, Context applicationContext) throws JSONException {
        mMap = map;
        InputStream stream = applicationContext.getResources().openRawResource(resourceId);
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
    private void parseGeoJsonFile() throws JSONException {

        JSONArray jsonFeaturesArray;

        // Case sensitive, so we do not need toLowerCase() here:
        // mGeoJsonFile.getString("type").toLowerCase().trim().equals("feature");
        boolean isFeature = mGeoJsonFile.getString("type").trim().equals("Feature");
        boolean isGeometry = mGeoJsonFile.getString("type").trim().matches("Point|LineString|Polygon|MultiPoint|LineString|MultiLineString|MultiPolygon");
        boolean isFeatureCollection = mGeoJsonFile.getString("type").trim().equals("FeatureCollection");
        boolean isGeometryCollection = mGeoJsonFile.getString("type").trim().equals("GeometryCollection");

        if (isFeatureCollection) {
            try {
                // Store the list of GeoJSON feature object
                jsonFeaturesArray = mGeoJsonFile.getJSONArray("features");
                for (int i = 0; i < jsonFeaturesArray.length(); i++) {
                    isFeature = jsonFeaturesArray.getJSONObject(i).getString("type").trim().equals("Feature");
                    if (isFeature) {
                        mGeoJsonMapObjects.add(parseGeoJsonFeature(jsonFeaturesArray.getJSONObject(i)));
                    }
                }
            } catch (JSONException e) {
                Log.e("JSONException", e.toString());
            }

        } else if (isFeature) {
            mGeoJsonMapObjects.add(parseGeoJsonFeature(mGeoJsonFile));
        } else if (isGeometryCollection) {
            JSONArray geometriesObjectArray;
            geometriesObjectArray = mGeoJsonFile.getJSONArray("geometries");
            for (int i = 0; i < geometriesObjectArray.length(); i++) {
                mGeoJsonMapObjects.add(parseGeoJsonGeometry(geometriesObjectArray.getJSONObject(i)));
            }
        } else if (isGeometry) {
            //TODO: If MultiPoint, then this returns an ArrayList of markers. We need to find a way to add markers individually.
            mGeoJsonMapObjects.add(parseGeoJsonGeometry(mGeoJsonFile));
        }
    }

    /**
     * Adds map options to the map.
     */

    public void addGeoJsonData() {

        for (Object mapObject: mGeoJsonMapObjects) {
            if (mapObject instanceof  PolygonOptions) {
                mMap.addPolygon((PolygonOptions) mapObject);
            } else if (mapObject instanceof MarkerOptions) {
                mMap.addMarker((MarkerOptions) mapObject);
            }else if (mapObject instanceof PolylineOptions) {
                mMap.addPolyline((PolylineOptions) mapObject);
            } else if (mapObject instanceof ArrayList) {
                //TODO: See multipoint TODO above.
                for (Object element : (ArrayList) mapObject) {
                    if (element instanceof MarkerOptions) {
                        mMap.addMarker((MarkerOptions)element);
                    } else if (element instanceof PolylineOptions) {
                        mMap.addPolyline((PolylineOptions) element);
                    } else if (element instanceof PolygonOptions) {
                        mMap.addPolygon((PolygonOptions) element);
                    }
                }
            }
        }
    }

    private boolean isVisible = true;

    public void toggleGeoJsonData() {

    }







    /**
     * Removes all map options from the map
     * TODO:
     * Implementation removes everything from the map. We should probably create a function where the user
     * can toggle the GeoJSON layer on and off.
     */

    public void removeGeoJsonData() {
        mMap.clear();
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


    private Object parseGeoJsonGeometryObject(JSONObject geoJsonFeature, String geometryType,
    JSONArray featureCoordinatesArray, JSONObject featureProperties) {
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
        return null;

    }

    private Object parseGeoJsonGeometry (JSONObject geoJsonFeature) {
        String geometryType = null;
        JSONArray featureCoordinatesArray;
        JSONObject featureProperties = null;
        try {
            geometryType = geoJsonFeature.getString("type").toLowerCase();
            featureCoordinatesArray = geoJsonFeature.getJSONArray("coordinates");

            if (geoJsonFeature.has("properties")) {
                featureProperties = geoJsonFeature.getJSONObject("properties");
            }


            return parseGeoJsonGeometryObject(geoJsonFeature, geometryType, featureCoordinatesArray, featureProperties);
        } catch (JSONException e) {
            Log.e("JSONException", e.toString());
        }
        return null;
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
            return parseGeoJsonGeometryObject(geoJsonFeature, geometryType, featureCoordinatesArray, featureProperties);
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
     * Creates new map {@link com.google.android.gms.maps.model.Marker} objects based on the
     * existing coordinates and properties
     *
     * @param geoJsonMultiPointProperties JSONObject containing the MultiPoint GeoJSON object
     * @return an array of new map {@link com.google.android.gms.maps.model.Marker} model objects
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
     * Creates a new {@link com.google.android.gms.maps.model.Polyline} object based on the
     * existing coordinates and properties
     *
     * @param geoJsonLineStringProperties JSONObject containing the LineString GeoJSON object
     * @return new {@link com.google.android.gms.maps.model.Polyline} model object
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
     * Creates new {@link com.google.android.gms.maps.model.Polyline} objects based on the existing
     * coordinates and properties
     *
     * @param geoJsonMultiLineStringProperties JSONObject containing the MultiLineString GeoJSON
     *                                         object
     * @return an array of new {@link com.google.android.gms.maps.model.Polyline} model objects
     */
    private ArrayList<PolylineOptions> toPolylines(JSONArray geoJsonMultiLineStringCoordinatesArray,
            JSONObject geoJsonMultiLineStringProperties) {
        ArrayList<PolylineOptions> polylines = new ArrayList<PolylineOptions>();
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


        //create a new property ??? extra coordinates ???????


        return properties;
    }

    /**
     * Creates new {@link com.google.android.gms.maps.model.Polygon} objects based on the existing
     * coordinates and properties
     *
     * @param geoJsonMultiPolygonProperties JSONObject containing the Polygon GeoJSON object
     * @return an array of new {@link com.google.android.gms.maps.model.Polygon} model objects
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

}
