package com.google.maps.android.kml;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.AsyncTask;
import android.support.v4.util.LruCache;

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

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;

/**
 * Document class allows for users to input their KML data and output it onto the map
 */
public class KmlLayer {

    private HashMap<KmlPlacemark, Object> mPlacemarks;

    private final GoogleMap mMap;

    private final LruCache<String, Bitmap> mMarkerIconCache;

    private final ArrayList<String> mMarkerIconUrls;

    private HashMap<String, String> mStyleMaps;

    private ArrayList<KmlContainerInterface> mContainers;

    private HashMap<String, KmlStyle> mStyles;

    private XmlPullParser mParser;

    private static int RANDOM_COLOR_MODE = 1;

    private static int LRU_CACHE_SIZE = 100;

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
        mMarkerIconCache = new LruCache<String, Bitmap>(LRU_CACHE_SIZE);
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
    public KmlLayer(GoogleMap map, InputStream stream, Context context)
            throws XmlPullParserException {
        mMap = map;
        mStyles = new HashMap<String, KmlStyle>();
        mPlacemarks = null;
        mStyleMaps = new HashMap<String, String>();
        mMarkerIconCache = new LruCache<String, Bitmap>(LRU_CACHE_SIZE);
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
     *  Iterates a list of styles and assigns a style
     */
    private void assignStyleMapStyles(HashMap<String, String> styleMap,
            HashMap<String, KmlStyle> styles) {
        for (String styleId : styleMap.keySet()) {
            if (styles.containsKey(styleMap.get(styleId))) {
                styles.put(styleId, styles.get(styleMap.get(styleId)));
            }
        }
    }

    /**
     * Iterates over the placemarks, gets its style or assigns a default one and adds it to the map
     */
    private void addPlacemarkGroupToMap(HashMap<KmlPlacemark, Object> placemarks) {
        for (KmlPlacemark placemark : placemarks.keySet()) {
            Boolean isVisible = getPlacemarkVisibility(placemark);
            placemarks.put(placemark, addPlacemarkObjectToMap(placemark, isVisible));
        }
    }

    private Object addPlacemarkObjectToMap (KmlPlacemark placemark, Boolean isVisible) {
        String placemarkId = placemark.getStyleID();
        KmlGeometry kmlGeometry = placemark.getGeometry();
        KmlStyle kmlStyle = getPlacemarkStyle(placemarkId);
        return addToMap(kmlGeometry, kmlStyle, isVisible);
    }

    private boolean getPlacemarkVisibility (KmlPlacemark placemark) {
        Boolean isPlacemarkVisible = true;
        if (placemark.hasProperty("visibility")) {
            String placemarkVisibility = placemark.getProperty("visibility");
            if (Integer.parseInt(placemarkVisibility) == 0) {
                isPlacemarkVisible = false;
            }
        }
        return isPlacemarkVisible;
    }

    /**
     * Adds placemarks with their corresponding styles onto the map
     * @param kmlContainers   An arraylist of folders
     */
    private void addContainerGroupToMap(Iterator<KmlContainerInterface> kmlContainers,
            boolean parentVisibility) {
        while (kmlContainers.hasNext()) {
            KmlContainer kmlContainer = (KmlContainer) kmlContainers.next();
            Boolean isContainerVisible = getContainerVisibility(kmlContainer, parentVisibility);
            if (kmlContainer.getStyles() != null) {
                mStyles.putAll(kmlContainer.getStyles());
                assignStyleMapStyles(mStyleMaps, mStyles);
                assignStyleMapStyles(kmlContainer.getStyleMap(), mStyles);
            }
            addContainerObjectToMap(kmlContainer, isContainerVisible);
            if (kmlContainer.hasNestedKmlContainers()) {
                addContainerGroupToMap(kmlContainer.getNestedKmlContainers(), isContainerVisible);
            }
        }
    }

    /**
     * Goes through the every placemark, style and properties object within a <Folder> tag
     * @param kmlContainer    Folder to obtain placemark and styles from
     */
    private void addContainerObjectToMap(KmlContainer kmlContainer, boolean containerVisibility) {
        for (KmlPlacemark placemark : kmlContainer.getPlacemarks().keySet()) {
            if (containerVisibility && getPlacemarkVisibility(placemark)) {
                kmlContainer.setPlacemark(placemark, addPlacemarkObjectToMap(placemark,true));
            } else {
                kmlContainer.setPlacemark(placemark, addPlacemarkObjectToMap(placemark,false));
            }
        }
    }

    private Boolean getContainerVisibility (KmlContainerInterface kmlContainer, Boolean
            isParentContainerVisible) {
        Boolean isChildContainerVisible = true;
        if (kmlContainer.hasKmlProperty("visibility")) {
            String placemarkVisibility = kmlContainer.getKmlProperty("visibility");
            if (Integer.parseInt(placemarkVisibility) == 0) {
                isChildContainerVisible = false;
            }
        }
        return (isParentContainerVisible && isChildContainerVisible);
    }

    /**
     * Gets the visibility of the placemark if it is defined. The default visibility is true
     * @param placemark Placemark to obtain visibility from
     * @return true if visibility is set to 1 or visibility undefined, false if visibility is 0
     */
    private static boolean getVisibility (KmlPlacemark placemark) {
        Boolean visibility = true;
        if (placemark.getProperty("visibility") != null) {
            if (placemark.getProperty("visibility").equals("0")) {
                visibility = false;
            }
        }
        return visibility;
    }

    /**
     * Determines if there are any icons to add to markers
     * @param iconUrls String value represent path to obtain icon from
     */
    private void addIconsToMarkers (ArrayList<String> iconUrls) {
        if (!iconUrls.isEmpty()) {
            for (String markerIconUrl : mMarkerIconUrls) {
                new IconImageDownload(markerIconUrl).execute();
                mMarkerIconUrls.remove(markerIconUrl);
            }
        }
    }

    /**
     * Obtains the styleUrl from a placemark and finds the corresponding style in a list
     * @param styleId   StyleUrl from a placemark
     * @return  Style which corresponds to an ID
     */
    private KmlStyle getPlacemarkStyle(String styleId) {
        KmlStyle style = mStyles.get(null);
        if (mStyles.get(styleId) != null) style = mStyles.get(styleId);
        return style;
    }


    /**
     * Adds the marker icon stored in mMarkerIconCache, to the {@link com.google.android.gms.maps.model.Marker}
     *
     * @param iconUrl icon url of icon to add to markers
     */
    private void addIconToMarkers(String iconUrl, HashMap<KmlPlacemark, Object> mPlacemarks) {
        for (KmlPlacemark placemark : mPlacemarks.keySet()) {
            KmlStyle placemarkStyle = mStyles.get(placemark.getStyleID());
            // Check if the style URL is the same and the type of geometry is a point
            if (placemarkStyle != null && placemarkStyle.getIconUrl().equals(iconUrl)
                    && placemark.getGeometry().getKmlGeometryType().equals("Point")) {
                Bitmap iconBitmap = mMarkerIconCache.get(iconUrl);
                Double scale = mStyles.get(placemark.getStyleID()).getIconScale();
                ((Marker) mPlacemarks.get(placemark)).setIcon(scaleIconToMarkers(iconBitmap, scale));
            }
        }
    }

    /**
     * Assigns icons to markers with a url if put in a placemark tag that is nested in a folder.
     * @param iconUrl   Iconurl to obtain marker image
     * @param kmlContainers kml container which contains the marker i
     */

    private void addContainerGroupIconsToMarkers(String iconUrl,
        Iterator<KmlContainerInterface> kmlContainers) {
        while (kmlContainers.hasNext()) {
            KmlContainer kmlContainer = ((KmlContainer) kmlContainers.next());
            addIconToMarkers(iconUrl, kmlContainer.getPlacemarks());
            if (kmlContainer.hasNestedKmlContainers()) {
                addContainerGroupIconsToMarkers(iconUrl, kmlContainer.getNestedKmlContainers());
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
        Integer width = (int) (unscaledIconBitmap.getWidth() * scale);
        Integer height = (int) (unscaledIconBitmap.getHeight() * scale);
        Bitmap scaledIconBitmap = Bitmap.createScaledBitmap(unscaledIconBitmap,
                width, height, false);
        return BitmapDescriptorFactory.fromBitmap(scaledIconBitmap);
    }

    /**
     * Adds a single geometry object to the map with its specified style
     *
     * @param geometry defines the type of object to add to the map
     * @param style    defines styling properties to add to the object when added to the map
     * @return the object that was added to the map, this is a Marker, Polyline, Polygon or an array
     * of either objects
     */
    private Object addToMap(KmlGeometry geometry, KmlStyle style, Boolean isVisible) {
        String geometryType = geometry.getKmlGeometryType();
        if (geometryType.equals("Point")) {
            Marker marker = addPointToMap((KmlPoint) geometry, style);
            marker.setVisible(isVisible);
            return marker;
        } else if (geometryType.equals("LineString")) {
            Polyline polyline = addLineStringToMap((KmlLineString) geometry, style);
            polyline.setVisible(isVisible);
            return polyline;
        } else if (geometryType.equals("Polygon")) {
            Polygon polygon = addPolygonToMap((KmlPolygon) geometry, style);
            polygon.setVisible(isVisible);
            return polygon;
        } else if (geometryType.equals("MultiGeometry")) {
            return addMultiGeometryToMap((KmlMultiGeometry) geometry, style, isVisible);
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
        markerOptions.position(point.getKmlGeometryObject());
        Marker marker = mMap.addMarker(markerOptions);
        if (style.getBalloonOptions().size() > 0) {
            setMarkerInfoWindow(style, marker, mPlacemarks.entrySet().iterator());
            setContainerMarkerInfoWindow(style, marker, mContainers.iterator());
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
    private void setMarkerInfoWindow(KmlStyle style, Marker marker,
                                     Iterator<Map.Entry<KmlPlacemark, Object>> mPlacemarks) {
        while (mPlacemarks.hasNext()) {
            KmlPlacemark placemark = mPlacemarks.next().getKey();
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

    private void setContainerMarkerInfoWindow(KmlStyle style, Marker marker,
        Iterator<KmlContainerInterface> containers) {
        while (containers.hasNext()) {
            KmlContainerInterface container = containers.next();
            if (container.hasKmlPlacemarks()) {
                setMarkerInfoWindow(style, marker, container.getKmlPlacemarks());
            }
            if (hasNestedKmlContainers()) {
                setContainerMarkerInfoWindow(style, marker, container.getNestedKmlContainers());
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
        polylineOptions.addAll(lineString.getKmlGeometryObject());
        if (style.hasColorMode("LineString") && style.getColorMode("LineString")
                == RANDOM_COLOR_MODE) {
            polylineOptions.color(computeRandomColor(polylineOptions.getColor()));
        }
        return mMap.addPolyline(polylineOptions);
    }

    /**
     * Adds a KML Polygon to the map as a Polygon by combining the styling and coordinates
     *
     * @param polygon contains coordinates for the Polygon
     * @param style   contains relevant styling properties for the Polygon
     * @return Polygon object
     */
    private Polygon addPolygonToMap(KmlPolygon polygon, KmlStyle style) {
        PolygonOptions polygonOptions = style.getPolygonOptions();
        polygonOptions.addAll(polygon.getOuterBoundaryCoordinates());
        for (ArrayList<LatLng> innerBoundary : polygon.getInnerBoundaryCoordinates()) {
            polygonOptions.addHole(innerBoundary);
        }
        Polygon mapPolygon = mMap.addPolygon(polygonOptions);
        if (style.getColorMode("Polygon") == RANDOM_COLOR_MODE) {
            mapPolygon.setFillColor(computeRandomColor(mapPolygon.getFillColor()));
        }
        return mapPolygon;
    }

    /**
     * Computes a random color given an integer.
     * @param color Integer value representing a color
     * @return  Integer representing a random color
     */
    private static int computeRandomColor (int color) {
        Random random = new Random();
        int red = Color.red(color);
        int green = Color.green(color);
        int blue = Color.blue(color);
        //Random number can only be computed in range [0, n)
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
     * @param multiGeometry contains array of geometries for the MultiGeometry
     * @param style    contains relevant styling properties for the MultiGeometry
     * @return array of Marker, Polyline and Polygon objects
     */
    private ArrayList<Object> addMultiGeometryToMap(KmlMultiGeometry multiGeometry, KmlStyle style, Boolean isVisible) {
        ArrayList<Object> geometries = new ArrayList<Object>();
        ArrayList<KmlGeometry> geometry = multiGeometry.getKmlGeometryObject();
        for (KmlGeometry kmlGeometry : geometry) {
            geometries.add(addToMap(kmlGeometry, style, isVisible));
        }
        return geometries;
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
        mPlacemarks = parser.getPlacemarks();
        mContainers = parser.getFolders();
        assignStyleMapStyles(mStyleMaps, mStyles);
        addContainerGroupToMap(mContainers.iterator(), true);
        addPlacemarkGroupToMap(mPlacemarks);
        addIconsToMarkers(mMarkerIconUrls);
    }

    /**
     * Removes all the KML data from the map and clears all the stored placemarks
     */
    public void removeKmlData() {
        removeKmlPlacemarks(mPlacemarks);
        //TODO: Remove kml placemarks in folders
        //removeKmlFolders();
    }


    private void removeKmlPlacemarks(HashMap<KmlPlacemark, Object> placemarks) {
        // Remove map object from the map
        for (Object mapObject : placemarks.values()) {
            if (mapObject instanceof Marker) {
                ((Marker) mapObject).remove();
            } else if (mapObject instanceof Polyline) {
                ((Polyline) mapObject).remove();
            } else if (mapObject instanceof Polygon) {
                ((Polygon) mapObject).remove();
            }
        }
        // Remove the KmlPlacemark and map object from the mPlacemarks hashmap
        placemarks.clear();
    }


    /**
     * @return true if there exists placemarks
     */

    public Boolean hasKmlPlacemarks() {
        return mPlacemarks.size() > 0;
    }

    /**
     * @return An Iterator of an ArrayList of Placemarks, null if no placemarks outside of a container
     * tag exist
     */
    public Iterator<KmlPlacemark> getKmlPlacemarks() {
        return mPlacemarks.keySet().iterator();
    }

    /**
     * @return true if there exists a container (document or folder) within the kml document,
     * false otherwise
     */
    public boolean hasNestedKmlContainers() {
        return mContainers.size() > 0;
    }

    /**
     * @return An iterator of a KmlContainerInterface Array, null if it does not exist
     */
    public Iterator<KmlContainerInterface> getNestedKmlContainers() {
        return mContainers.iterator();
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
            addIconToMarkers(mIconUrl, mPlacemarks);
            addContainerGroupIconsToMarkers(mIconUrl, mContainers.iterator());
        }
    }
}