package com.google.maps.android.kml;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptor;
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
import android.support.v4.util.LruCache;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

/**
 * Document class allows for users to input their KML data and output it onto the map
 */
public class KmlLayer {

    private final HashMap<KmlPlacemark, Object> mPlacemarks;

    private final GoogleMap mMap;

    private final LruCache<String, Bitmap> mMarkerIconCache;

    private final ArrayList<String> mMarkerIconUrls;

    private HashMap<String, String> mStyleMaps;


    /**
     * Hashmap of Style classes. The key is a string value which represents the id of the Style in
     * the KML document.
     */
    private HashMap<String, KmlStyle> mStyles;

    /**
     * XML Pull Parser, which reads in a KML Document
     */
    private XmlPullParser mParser;

    /*TODO(lavenderch): IconStyle, BallonStyle, ExtendedData, Folder
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
        mMap = map;
        mStyles = new HashMap<String, KmlStyle>();
        mPlacemarks = new HashMap<KmlPlacemark, Object>();
        mMarkerIconCache = new LruCache<String, Bitmap>(100);
        mStyleMaps = new HashMap<String, String>();
        mMarkerIconUrls = new ArrayList<String>();
        InputStream stream = context.getResources().openRawResource(resourceId);
        mParser = createXmlParser(stream);

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
        mMap = map;
        mStyles = new HashMap<String, KmlStyle>();
        mPlacemarks = new HashMap<KmlPlacemark, Object>();
        mStyleMaps = new HashMap<String, String>();
        mMarkerIconCache = new LruCache<String, Bitmap>(100);
        mMarkerIconUrls = new ArrayList<String>();
        mParser = createXmlParser(stream);
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
        mStyleMaps = parser.getStyleMaps();
        // Store parsed placemarks
        for (KmlPlacemark placemark : parser.getPlacemarks()) {
            mPlacemarks.put(placemark, null);
        }
        assignStyleMapStyles();
        addKmlLayer();
    }

    /**
     * Iterates through the the stylemap hashmap and assigns the relevant style objects to them if
     * they exist
     */
    private void assignStyleMapStyles() {
        for (String styleId : mStyleMaps.keySet()) {
            if (mStyles.containsKey(mStyleMaps.get(styleId))) {
                mStyles.put(styleId, mStyles.get(mStyleMaps.get(styleId)));
            }
        }
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
    public Iterator<KmlPlacemark> getPlacemarks() {
        return mPlacemarks.keySet().iterator();
    }

    /**
     * Iterates over the placemarks, gets its style or assigns a default one and adds it to the map
     */
    private void addKmlLayer() {
        for (KmlPlacemark placemark : mPlacemarks.keySet()) {
            KmlStyle style = null;
            if (mStyles.get(placemark.getStyle()) != null) {
                // Assign style if found, else remains null
                style = mStyles.get(placemark.getStyle());
            }
            mPlacemarks.put(placemark, addToMap(placemark.getGeometry(), style));
        }
        if (!mMarkerIconUrls.isEmpty()) {
            // If there are marker icon URLs stored, download and assign to markers
            downloadMarkerIcons();
        }
    }

    /**
     * Downloads the marker icon from the stored URLs in mUrlMapMarkerHashmap and assigns them to
     * the relevant icons
     */
    private void downloadMarkerIcons() {
        // Iterate over the URLs to download
        for (String markerIconUrl : mMarkerIconUrls) {
            new IconImageDownload(markerIconUrl).execute();
            mMarkerIconUrls.remove(markerIconUrl);
        }
    }

    /**
     * Adds the marker icon stored in mMarkerIconCache, to the {@link com.google.android.gms.maps.model.Marker}
     *
     * @param iconUrl icon url of icon to add to markers
     */
    private void addIconToMarkers(String iconUrl) {
        BitmapDescriptor markerIcon = BitmapDescriptorFactory
                .fromBitmap(mMarkerIconCache.get(iconUrl));
        for (KmlPlacemark placemark : mPlacemarks.keySet()) {
            // Check if the style URL is the same and the type of geometry is a point
            if (mStyles.get(placemark.getStyle()) != null && mStyles.get(placemark.getStyle())
                    .getIconUrl().equals(iconUrl) && placemark.getGeometry().getType()
                    .equals("Point")) {
                ((Marker) mPlacemarks.get(placemark)).setIcon(markerIcon);
            }
        }
    }

    /**
     * Adds a single geometry object to the map with its specified style
     *
     * @param geometry defines the type of object to add to the map
     * @param style    defines styling properties to add to the object when added to the map
     * @return the object that was added to the map, this is a Marker, Polyline, Polygon or an array
     * of either objects
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
        markerOptions.position((LatLng) point.getCoordinates());
        Marker marker = mMap.addMarker(markerOptions);
        // Check if the marker icon needs to be downloaded
        if (style.getIconUrl() != null) {
            if (mMarkerIconCache.get(style.getIconUrl()) != null) {
                // Bitmap stored in cache
                Bitmap bitmap = mMarkerIconCache.get(style.getIconUrl());
                marker.setIcon(BitmapDescriptorFactory.fromBitmap(bitmap));
            } else if (!mMarkerIconUrls.contains(style.getIconUrl())) {
                mMarkerIconUrls.add(style.getIconUrl());
            }
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
        polylineOptions.addAll((Iterable<LatLng>) lineString.getCoordinates());
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

    /**
     * Adds all the geometries within a KML MultiGeometry to the map. Supports recursive
     * MultiGeometry. Combines styling of the placemark with the coordinates of each geometry.
     *
     * @param geometry contains array of geometries for the MultiGeometry
     * @param style    contains relevant styling properties for the MultiGeometry
     * @return array of Marker, Polyline and Polygon objects
     */
    private ArrayList<Object> addMultiGeometryToMap(KmlMultiGeometry geometry, KmlStyle style) {
        ArrayList<Object> geometries = new ArrayList<Object>();
        for (KmlGeometry kmlGeometry : (ArrayList<KmlGeometry>) geometry.getCoordinates()) {
            geometries.add(addToMap(kmlGeometry, style));
        }
        return geometries;
    }

    /**
     * Downloads images for use as marker icons
     */
    private class IconImageDownload extends AsyncTask<String, Void, Bitmap> {

        private final String mIconUrl;

        /**
         * Creates a new IconImageDownload object
         *
         * @param iconUrl URL of the marker icon to download
         */
        public IconImageDownload(String iconUrl) {
            mIconUrl = iconUrl;
        }

        /**
         * Downloads the marker icon in another thread
         *
         * @param params String varargs not used
         * @return Bitmap object downloaded
         */
        @Override
        protected Bitmap doInBackground(String... params) {
            try {
                return BitmapFactory.decodeStream((InputStream) new URL(mIconUrl).getContent());
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        /**
         * Adds the bitmap to the cache and adds the bitmap to the markers
         *
         * @param bitmap bitmap downloaded
         */
        @Override
        protected void onPostExecute(Bitmap bitmap) {
            mMarkerIconCache.put(mIconUrl, bitmap);
            addIconToMarkers(mIconUrl);

        }
    }
}
