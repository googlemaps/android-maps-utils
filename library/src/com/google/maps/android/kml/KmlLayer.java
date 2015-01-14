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
import android.graphics.Color;
import android.os.AsyncTask;
import android.support.v4.util.LruCache;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Random;

/**
 * Document class allows for users to input their KML data and output it onto the map
 */
public class KmlLayer {

    private HashMap<KMLFeature, Object> mPlacemarks;

    private final GoogleMap mMap;

    private final LruCache<String, Bitmap> mMarkerIconCache;

    private final ArrayList<String> mMarkerIconUrls;

    private HashMap<String, String> mStyleMaps;

    private Context mContext;


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
        mMarkerIconCache = new LruCache<String, Bitmap>(100);
        mStyleMaps = new HashMap<String, String>();
        mMarkerIconUrls = new ArrayList<String>();
        InputStream stream = context.getResources().openRawResource(resourceId);
        mParser = createXmlParser(stream);
        mContext = context;

    }

    /**
     * Creates a new KmlLayer object
     *
     * @param map    GoogleMap object
     * @param stream InputStream containing KML file
     * @throws XmlPullParserException if file cannot be parsed
     */
    public KmlLayer(GoogleMap map, InputStream stream, Context context)
            throws XmlPullParserException {
        mMap = map;
        mStyles = new HashMap<String, KmlStyle>();
        mPlacemarks = null;
        mStyleMaps = new HashMap<String, String>();
        mMarkerIconCache = new LruCache<String, Bitmap>(100);
        mMarkerIconUrls = new ArrayList<String>();
        mParser = createXmlParser(stream);
        mContext = context;
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
        initializeFeatureLayer(parser);
        assignStyleMapStyles();
        addContainerLayer(parser);
        addKmlLayer();
    }

    public void initializeFeatureLayer(KmlParser parser) {
        mStyles = parser.getStyles();
        mStyleMaps = parser.getStyleMaps();
        mPlacemarks = parser.getPlacemarks();
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
    public Iterator<KMLFeature> getPlacemarks() {
        return mPlacemarks.keySet().iterator();
    }

    /**
     * Iterates over the placemarks, gets its style or assigns a default one and adds it to the map
     */
    private void addKmlLayer() {
        for (KMLFeature placemark : mPlacemarks.keySet()) {
            KmlStyle style = null;
            if (mStyles.get(placemark.getStyleID()) != null) {
                // Assign style if found, else remains null
                style = mStyles.get(placemark.getStyleID());
            }
            mPlacemarks.put(placemark, addToMap(placemark.getGeometry(), style));
        }
        if (!mMarkerIconUrls.isEmpty()) {
            // If there are marker icon URLs stored, download and assign to markers
            downloadMarkerIcons();
        }
    }

    private void addContainerLayer(KmlParser parser) {
        KmlStyle style = null;
        for (KmlContainer container : parser.getContainers()) {
            HashMap<KMLFeature, Object> placemarks = container.getPlacemarks();
            for (KMLFeature placemark : placemarks.keySet()) {
                System.out.println(placemarks.keySet().size());
                style = container.getStyle(placemark.getStyleID());

                container.addPlacemarks(placemark, addToMap(placemark.getGeometry(), style));
            }
        }
        parser.getContainers();
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
        for (KMLFeature placemark : mPlacemarks.keySet()) {
            // Check if the style URL is the same and the type of geometry is a point
            if (mStyles.get(placemark.getStyleID()) != null && mStyles.get(placemark.getStyleID())
                    .getIconUrl().equals(iconUrl) && placemark.getGeometry().getType()
                    .equals("Point")) {
                Bitmap iconBitmap = mMarkerIconCache.get(iconUrl);
                Double scale = mStyles.get(placemark.getStyleID()).getIconScale();
                ((Marker) mPlacemarks.get(placemark)).setIcon(scaleIconToMarkers(iconBitmap, scale));
            }
        }
    }

    /**
     * Create a new bitmap which takes the size of the original bitmap and applies a scale as defined
     * in the style
     * @param unscaledIconBitmap Original bitmap image to convert to size
     * @param scale The scale we wish to apply to the original bitmap image
     * @return A BitMapDescriptor of the icon image
     */
    private BitmapDescriptor scaleIconToMarkers(Bitmap unscaledIconBitmap, Double scale) {
        Integer width =(int) (unscaledIconBitmap.getWidth() * scale);
        Integer height =(int) (unscaledIconBitmap.getHeight() * scale);
        Bitmap scaledIconBitmap = Bitmap.createScaledBitmap(unscaledIconBitmap,
                width, height, false);
        BitmapDescriptor markerIcon = BitmapDescriptorFactory
                .fromBitmap(scaledIconBitmap);
        return markerIcon;
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
        MarkerOptions markerOptions = new MarkerOptions();
        if (style != null) {
           markerOptions = style.getMarkerOptions();
        }
        markerOptions.position((LatLng) point.getCoordinates());
        Marker marker = mMap.addMarker(markerOptions);
        if (markerOptions.getTitle() == null) {
            setMarkerInfoWindow(style, marker);
        } if (style != null && style.getIconUrl() != null) {
            setMarkerIcon(style, marker);
        }
        return marker;
    }

    /**
     * Sets a marker info window if no <text> tag was found in the KML document. This method sets
     * the marker title as the text found in the <name> start tag and the snippet as <description>
     * @param style Style to apply
     * @param marker
     */
    private void setMarkerInfoWindow(KmlStyle style, Marker marker) {
        for (KMLFeature placemark : mPlacemarks.keySet()) {
            if (mStyles.get(placemark.getStyleID()).equals(style)) {
                Boolean hasName = placemark.getProperty("name") != null;
                Boolean hasDescription = placemark.getProperty("description") != null;
                if (style.getBalloonOptions().containsKey("text")) {
                    marker.setTitle(style.getBalloonOptions().get("text"));
                } else if (hasName && hasDescription) {
                    marker.setTitle(placemark.getProperty("name"));
                    marker.setSnippet(placemark.getProperty("description"));
                } else if (hasName && !hasDescription) {
                    marker.setTitle(placemark.getProperty("name"));
                } else if (hasDescription && !hasName) {
                    marker.setTitle(placemark.getProperty("description"));
                } else {
                    //TODO: Figure if we should throw an illegal argument exception?
                }
            }
        }
    }

    /**
     * Sets the marker icon if there was a url that was found
     * @param style The style which we retreieve the icon url from
     * @param marker The marker which is displaying the icon
     */
    private void setMarkerIcon (KmlStyle style, Marker marker) {
        if (mMarkerIconCache.get(style.getIconUrl()) != null) {
            // Bitmap stored in cache
            Bitmap bitmap = mMarkerIconCache.get(style.getIconUrl());
            marker.setIcon(BitmapDescriptorFactory.fromBitmap(bitmap));
        } else if (!mMarkerIconUrls.contains(style.getIconUrl())) {
            mMarkerIconUrls.add(style.getIconUrl());
        }
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
        Polygon mapPolygon = mMap.addPolygon(polygonOptions);

        if (style.getColorMode("Polygon") == 1) {
            int randomColor = computeRandomColor(mapPolygon.getFillColor());

            mapPolygon.setFillColor(computeRandomColor(mapPolygon.getFillColor()));
        }

        return mapPolygon;
    }

    private int computeRandomColor (int color) {
        Random random = new Random();
        int red = Color.red(color);
        int green = Color.green(color);
        int blue = Color.blue(color);
        if (red != 0) red = random.nextInt(red);
        if (blue != 0) blue = random.nextInt(blue);
        if (green != 0) green = random.nextInt(green);
        int randomColor = Color.rgb(red,green,blue);
        return randomColor;
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
