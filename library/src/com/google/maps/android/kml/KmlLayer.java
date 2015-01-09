package com.google.maps.android.kml;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polygon;
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

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Document class allows for users to input their KML data and output it onto the map
 */
public class KmlLayer {

    private final HashMap<KmlPlacemark, Object> mPlacemarks;

    /**
     * A Google Map
     */
    private final GoogleMap mMap;

    /**
     * Hashmap of Style classes. The key is a string value which represents the id of the Style in
     * the KML document.
     */
    private HashMap<String, KmlStyle> mStyles;

    /**
     * XML Pull Parser, which reads in a KML Document
     */
    private XmlPullParser mParser;

    /*TODO:
        Implement IconStyle (IconStyle is the style class for Point classes) - in progress
        Implement StyleMap.
        Implement BalloonStyle (Equivalent in GoogleMaps is IconWindow)
        Implement LabelStyle (Equivalent is IconGenerator Utility Library)
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
        this.mPlacemarks = new HashMap<KmlPlacemark, Object>();
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
        this.mPlacemarks = new HashMap<KmlPlacemark, Object>();
        this.mParser = createXmlParser(stream);
    }

    /**
     * Creates a new XmlPullParser to allow for the KML file to be parsed
     *
     * @param stream InputStream containing KML file
     * @return XmlPullParser containing the KML file
     * @throws XmlPullParserException if KML file cannot be parsed
     */
    private XmlPullParser createXmlParser(InputStream stream) throws XmlPullParserException {
        XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
        factory.setNamespaceAware(true);
        XmlPullParser parser = factory.newPullParser();
        parser.setInput(stream, null);
        return parser;
    }

    /**
     * Adds the KML data to the map
     *
     * @throws XmlPullParserException if KML file cannot be parsed
     * @throws IOException            if KML file cannot be opened
     */
    public void addKmlData() throws IOException, XmlPullParserException {
        KmlParser parser = new KmlParser(mParser);
        parser.parseKml();
        mStyles = parser.getStyles();
        // Store parsed placemarks
        for (KmlPlacemark placemark : parser.getPlacemarks()) {
            mPlacemarks.put(placemark, null);
        }
        addKmlLayer();
    }

    /**
     * Removes all the KML data from the map and clears all the stored placemarks
     */
    public void removeKmlData() {
        // Remove map object from the map
        for (Object mapObject : mPlacemarks.values()) {
            if (mapObject instanceof Marker) {
                ((Marker) mapObject).remove();
            } else if (mapObject instanceof Polyline) {
                ((Polyline) mapObject).remove();
            } else if (mapObject instanceof Polygon) {
                ((Polygon) mapObject).remove();
            }
        }
        // Remove the KmlPlacemark and map object from the mPlacemarks hashmap
        mPlacemarks.clear();
    }

    /**
     * Gets an iterator of KmlPlacemark objects
     *
     * @return iterator of KmlPlacemark objects
     */
    public java.util.Iterator<KmlPlacemark> getPlacemarks() {
        return mPlacemarks.keySet().iterator();
    }

    /**
     * Sets the z index of the KML layer. This affects Polyline and Polygon objects. Markers are set to appear above other data.
     */
    public void setZIndex(float zIndex) {
        for (Object mapObject : mPlacemarks.values()) {
            if (mapObject instanceof Polyline) {
                ((Polyline) mapObject).setZIndex(zIndex););
            } else if (mapObject instanceof Polygon) {
                ((Polygon) mapObject).setZIndex(zIndex);
            }
        }
    }

    /**
     * Iterates over the placemarks, gets its style or assigns a default one and adds it to the map
     */
    private void addKmlLayer() {
        for (KmlPlacemark placemark : mPlacemarks.keySet()) {
            KmlStyle style;
            if (mStyles.get(placemark.getStyle()) == null) {
                // Assign default style if style cannot be found
                style = mStyles.get(null);
            } else {
                style = mStyles.get(placemark.getStyle());
            }
            mPlacemarks.put(placemark, addToMap(placemark.getGeometry(), style));
        }
    }

    /**
     * Adds a single geometry object to the map with its specified style
     *
     * @param geometry defines the type of object to add to the map
     * @param style defines styling properties to add to the object when added to the map
     * @return the object that was added to the map, this is a Marker, Polyline, Polygon or an array of either objects
     */
    private Object addToMap(KmlGeometry geometry, KmlStyle style) {
        String geometryType = geometry.getType();
        if (geometryType.equals("Point")) {
            return addPointToMap((KmlPoint) geometry, style);
        } else if (geometryType.equals("LineString")) {
            return addLineStringToMap((KmlLineString) geometry, style);
        } else if (geometryType.equals("Polygon")) {
            return addPolygonToMap((KmlPolygon) geometry, style);
        } else if (geometryType.equals("MultiGeometry")) {
             return addMultiGeometryToMap((KmlMultiGeometry) geometry, style);
        }
        return null;
    }

    /**
     * Adds a KML Point to the map as a Marker by combining the styling and coordinates
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
    private Polygon addPolygonToMap(KmlPolygon polygon,
            KmlStyle style) {
        PolygonOptions polygonOptions = style.getPolygonOptions();
        polygonOptions.addAll(polygon.getOuterBoundaryCoordinates());
        for (ArrayList<LatLng> innerBoundary : polygon.getInnerBoundaryCoordinates()) {
            polygonOptions.addHole(innerBoundary);
        }
        return mMap.addPolygon(polygonOptions);
    }

    private ArrayList<Object> addMultiGeometryToMap(KmlMultiGeometry geometry, KmlStyle style) {
        ArrayList<Object> geometries = new ArrayList<Object>();
        for (KmlGeometry kmlGeometry : (ArrayList<KmlGeometry>) geometry.getGeometry()) {
            geometries.add(addToMap(kmlGeometry, style));
        }
        return geometries;
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
