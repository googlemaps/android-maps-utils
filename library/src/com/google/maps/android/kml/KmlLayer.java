package com.google.maps.android.kml;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Document class allows for users to input their KML data and output it onto the map
 */
public class KmlLayer {

    private static final int INNER_BOUNDARY = 0;

    private static final int OUTER_BOUNDARY = 1;

    private static final int GEOMETRY_TYPE = 0;

    private static final int MULTIGEOMETRY_TYPE = 1;

    private final HashMap<KmlPlacemark, Object> mPlacemarks;

    /**
     * A Google Map
     */
    private final GoogleMap mMap;

    /**
     * Hashmap of Style classes. The key is a string value which represents the id of the Style in
     * the KML document.
     */
    private final HashMap<String, KmlStyle> mStyles;

    /**
     * XML Pull Parser, which reads in a KML Document
     */
    private XmlPullParser mParser;

    /*TODO:
        Implement IconStyle (IconStyle is the style class for Point classes) - in progress
        Implement StyleMap.
        Implement BalloonStyle (Equivalent in GoogleMaps is IconWindow)
        Implement LabelStyle (Equivalent is IconGenerator Utility Library)
        Test Multigeometry - can be recursive
    */

    /**
     * Creates a new KmlLayer object
     *
     * @param map        GoogleMap object
     * @param resourceId Raw resource KML file
     * @param context    Context object
     * @throws XmlPullParserException if file cannot be parsed
     */
    public KmlLayer(GoogleMap map, int resourceId, Context context)
            throws XmlPullParserException {
        this.mMap = map;
        this.mStyles = new HashMap<String, KmlStyle>();
        // Add a default style
        mStyles.put(null, new KmlStyle());
        mPlacemarks = new HashMap<KmlPlacemark, Object>();
        InputStream stream = context.getResources().openRawResource(resourceId);
        this.mParser = createXmlParser(stream);
    }

    /**
     * Creates a new KmlLayer object
     *
     * @param map    GoogleMap object
     * @param stream InputStream containing KML file
     * @throws XmlPullParserException if file cannot be parsed
     */
    public KmlLayer(GoogleMap map, InputStream stream)
            throws XmlPullParserException {
        this.mMap = map;
        this.mStyles = new HashMap<String, KmlStyle>();
        // Add a default style
        mStyles.put(null, new KmlStyle());
        mPlacemarks = new HashMap<KmlPlacemark, Object>();
        this.mParser = createXmlParser(stream);
    }

    /**
     * Adds the KML data to the map
     *
     * @throws XmlPullParserException if KML file cannot be parsed
     * @throws IOException            if KML file cannot be opened
     */
    public void setKmlData() {
        // TODO: replace with KmlParser
        // importKML();
//        KmlParser parser = new KmlParser(mParser);
//        parser.parseKml();
//        mStyles = parser.getStyles();
//        for (Placemark placemark : parser.getPlacemarks()) {
//            mPlacemarks.put(placemark, null);
//        }
        addToMap();
    }

    /**
     * Creates a new XmlPullParser to allow for the KML file to be parsed
     *
     * @param stream InputStream containing KML file
     * @return XmlPullParser containing the KML file
     * @throws XmlPullParserException if KML file cannot be parsed
     */
    public XmlPullParser createXmlParser(InputStream stream) throws XmlPullParserException {
        XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
        factory.setNamespaceAware(true);
        XmlPullParser parser = factory.newPullParser();
        parser.setInput(stream, null);
        return parser;
    }

    /**
     * Receives an ArrayList of Placemark objects and retrieves its geometry object; then creates a
     * corresponding GoogleMaps Options objects; and assigns styles and coordinates to this option
     * to store in an ArrayList of GoogleMapsOptions.
     */
    private void addToMap() {
        Log.i("Add", "START");
        for (KmlPlacemark placemark : mPlacemarks.keySet()) {

            KmlStyle style = mStyles.get(placemark.getStyle());
            // Check if the style is stored
            if (style != null) {
                String geometryType = placemark.getGeometry().getType();
                KmlGeometry geometry = placemark.getGeometry();
                if (geometryType.equals("Point")) {
                    mPlacemarks.put(placemark, addPointToMap((KmlPoint) geometry, style));
                } else if (geometryType.equals("KmlLineString")) {
                    mPlacemarks.put(placemark, addLineStringToMap((KmlLineString) geometry, style));
                } else if (geometryType.equals("Polygon")) {
                    mPlacemarks.put(placemark, addPolygonToMap((KmlPolygon) geometry, style));
                } else if (geometryType.equals("MultiGeometry")) {
                    addMultiGeometryToMap(placemark);
                }
            } else {
                Log.i("Style not found", placemark.getStyle());
            }
        }
        Log.i("Add", "END");
    }

    /**
     * Addsa a KML Point to the map as a Marker by combining the styling and coordinates
     *
     * @param point contains coordinates for the Marker
     * @param style contains relevant styling properties for the Marker
     * @return Marker object
     */
    private Marker addPointToMap(KmlPoint point, KmlStyle style) {
        MarkerOptions markerOptions = style.getMarkerOptions();
        markerOptions.position((LatLng) point.getGeometry());
        Marker marker = mMap.addMarker(markerOptions);
        if (style.getIconUrl() != null) {
            new IconImageDownload(marker).execute(style.getIconUrl());
        }
        return marker;
    }

    /**
     * Adds a KML LineString to the map as a Polyline by combining the styling and coordinates
     *
     * @param lineString contains coordinates for the Polyline
     * @param style      contains relevant styling properties for the Polyline
     * @return Polyline object
     */
    private Polyline addLineStringToMap(KmlLineString lineString, KmlStyle style) {
        PolylineOptions polylineOptions = style.getPolylineOptions();
        polylineOptions.addAll((Iterable<LatLng>) lineString.getGeometry());
        return mMap.addPolyline(polylineOptions);
    }

    /**
     * Adds a KML Polygon to the map as a Polygon by combining the styling and coordinates
     *
     * @param polygon contains coordinates for the Polygon
     * @param style   contains relevant styling properties for the Polygon
     * @return Polygon object
     */
    private com.google.android.gms.maps.model.Polygon addPolygonToMap(KmlPolygon polygon,
            KmlStyle style) {
        PolygonOptions polygonOptions = style.getPolygonOptions();
        HashMap<ArrayList<LatLng>, Integer> poly = (HashMap<ArrayList<LatLng>, Integer>) polygon
                .getGeometry();
        for (Map.Entry<ArrayList<LatLng>, Integer> p : poly.entrySet()) {
            if (p.getValue() == OUTER_BOUNDARY) {
                polygonOptions.addAll(p.getKey());
            } else if (p.getValue() == INNER_BOUNDARY) {
                polygonOptions.addHole(p.getKey());
            }
        }
        return mMap.addPolygon(polygonOptions);
    }

    // TODO: combine into 1 function, if MG, store arraylist

    /**
     * Recieves an ArrayList of objects in a MultiGeometry. It then creates a corresponding
     * GoogleMapsOptions
     * object, assigns styles and coordinates to this option and stores it in an ArrayList of
     * GoogleMapsOptions
     */
    private void addMultiGeometryToMap(KmlPlacemark placemark) {
        KmlStyle style = mStyles.get(placemark.getStyle());
        ArrayList<KmlGeometry> geometries = (ArrayList<KmlGeometry>) placemark.getGeometry()
                .getGeometry();
        for (KmlGeometry geometry : geometries) {
            if (style != null) {
                String geometryType = geometry.getType();
                if (geometryType.equals("Point")) {
                    mPlacemarks.put(placemark, addPointToMap((KmlPoint) geometry, style));
                } else if (geometryType.equals("LineString")) {
                    mPlacemarks.put(placemark, addLineStringToMap((KmlLineString) geometry, style));
                } else if (geometryType.equals("Polygon")) {
                    mPlacemarks.put(placemark, addPolygonToMap((KmlPolygon) geometry, style));
                }
            } else {
            }
        }
    }

    /**
     * Downloads images for use as marker icons
     */
    private class IconImageDownload extends AsyncTask<String, Void, Bitmap> {

        final Marker mMarker;

        public IconImageDownload(Marker marker) {
            mMarker = marker;
        }

        @Override
        protected Bitmap doInBackground(String... params) {
            String imageUrl = params[0];
            try {
                return BitmapFactory.decodeStream((InputStream) new URL(imageUrl).getContent());
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            mMarker.setIcon(BitmapDescriptorFactory.fromBitmap(bitmap));
        }
    }
}
