package com.google.maps.android.kml;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.GroundOverlay;
import com.google.android.gms.maps.model.GroundOverlayOptions;
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
import java.util.Random;
import java.util.Set;

/**
 * Document class allows for users to input their KML data and output it onto the map
 */
public class KmlLayer {

    private static final int RANDOM_COLOR_MODE = 1;

    private static final int LRU_CACHE_SIZE = 50;

    private final GoogleMap mMap;

    private final LruCache<String, Bitmap> mMarkerIconCache;

    private final LruCache<String, Bitmap> mGroundOverlayCache;

    private final ArrayList<String> mMarkerIconUrls;

    private final ArrayList<String> mGroundOverlayUrls;

    private HashMap<KmlPlacemark, Object> mPlacemarks;

    private HashMap<String, String> mStyleMaps;

    private ArrayList<KmlContainerInterface> mContainers;

    private HashMap<String, KmlStyle> mStyles;

    private HashMap<KmlGroundOverlay, GroundOverlay> mGroundOverlays;

    private XmlPullParser mParser;

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
        this(map, context.getResources().openRawResource(resourceId));
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
        mStyleMaps = new HashMap<String, String>();
        mMarkerIconCache = new LruCache<String, Bitmap>(LRU_CACHE_SIZE);
        mGroundOverlayCache = new LruCache<String, Bitmap>(LRU_CACHE_SIZE);
        mMarkerIconUrls = new ArrayList<String>();
        mGroundOverlayUrls = new ArrayList<String>();
        mPlacemarks = new HashMap<KmlPlacemark, Object>();
        mContainers = new ArrayList<KmlContainerInterface>();
        mGroundOverlays = new HashMap<KmlGroundOverlay, GroundOverlay>();
        mParser = createXmlParser(stream);
    }

    /**
     * Creates a new XmlPullParser to allow for the KML file to be parsed
     *
     * @param stream InputStream containing KML file
     * @return XmlPullParser containing the KML file
     * @throws XmlPullParserException if KML file cannot be parsed
     */
    private static XmlPullParser createXmlParser(InputStream stream) throws XmlPullParserException {
        XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
        factory.setNamespaceAware(true);
        XmlPullParser parser = factory.newPullParser();
        parser.setInput(stream, null);
        return parser;
    }

    /**
     * Computes a random color given an integer. Algorithm to compute the random color can be
     * found in https://developers.google.com/kml/documentation/kmlreference#colormode
     *
     * @param color Integer value representing a color
     * @return Integer representing a random color
     */
    private static int computeRandomColor(int color) {
        Random random = new Random();
        int red = Color.red(color);
        int green = Color.green(color);
        int blue = Color.blue(color);
        //Random number can only be computed in range [0, n)
        if (red != 0) {
            red = random.nextInt(red);
        }
        if (blue != 0) {
            blue = random.nextInt(blue);
        }
        if (green != 0) {
            green = random.nextInt(green);
        }
        return Color.rgb(red, green, blue);
    }

    /**
     * Iterates a list of styles and assigns a style
     */
    private static void assignStyleMap(HashMap<String, String> styleMap,
            HashMap<String, KmlStyle> styles) {
        for (String styleMapKey : styleMap.keySet()) {
            String styleMapValue = styleMap.get(styleMapKey);
            if (styles.containsKey(styleMapValue)) {
                styles.put(styleMapKey, styles.get(styleMapValue));
            }
        }
    }

    /**
     * Obtains the visibility of the placemark if it is specified, otherwise it returns true as a
     * default
     *
     * @param placemark Placemark to obtain visibility from
     * @return true if placemark visibility is set to the true or unspecified, false otherwise
     */
    private static boolean getPlacemarkVisibility(KmlPlacemark placemark) {
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
     * Create a new bitmap which takes the size of the original bitmap and applies a scale as
     * defined
     * in the style
     *
     * @param unscaledIconBitmap Original bitmap image to convert to size
     * @param scale              The scale we wish to apply to the original bitmap image
     * @return A BitMapDescriptor of the icon image
     */
    private static BitmapDescriptor scaleIconToMarkers(Bitmap unscaledIconBitmap, Double scale) {
        Integer width = (int) (unscaledIconBitmap.getWidth() * scale);
        Integer height = (int) (unscaledIconBitmap.getHeight() * scale);
        Bitmap scaledIconBitmap = Bitmap.createScaledBitmap(unscaledIconBitmap,
                width, height, false);
        return BitmapDescriptorFactory.fromBitmap(scaledIconBitmap);
    }

    /**
     * Removes all given KML placemarks from the map and clears all stored placemarks.
     *
     * @param placemarks placemarks to remove
     */
    private static void removeKmlPlacemarks(HashMap<KmlPlacemark, Object> placemarks) {
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
        mGroundOverlays = parser.getGroundOverlays();
        mContainers = parser.getFolders();
        assignStyleMap(mStyleMaps, mStyles);
        addGroundOverlays(mGroundOverlays, mContainers);
        downloadGroundOverlays();
        addContainerGroupToMap(mContainers, true);
        addPlacemarksToMap(mPlacemarks);
        downloadMarkerIcons();
    }

    /**
     * Iterates over the placemarks, gets its style or assigns a default one and adds it to the map
     */
    private void addPlacemarksToMap(HashMap<KmlPlacemark, Object> placemarks) {
        for (KmlPlacemark placemark : placemarks.keySet()) {
            Boolean isPlacemarkVisible = getPlacemarkVisibility(placemark);
            Object mapObject = addPlacemarkToMap(placemark, isPlacemarkVisible);
            // Placemark stores a KmlPlacemark as a key, and GoogleMap Object as its value
            placemarks.put(placemark, mapObject);
        }
    }

    /**
     * Combines style and visibility to apply to a placemark geometry object and adds it to the map
     *
     * @param placemark           Placemark to obtain geometry object to add to the map
     * @param placemarkVisibility Boolean value, where true indicates the placemark geometry is
     *                            shown initially on the map, false for not shown initially on the
     *                            map.
     * @return Google Map Object of the placemark geometry after it has been added to the map.
     */
    private Object addPlacemarkToMap(KmlPlacemark placemark, Boolean placemarkVisibility) {
        String placemarkId = placemark.getStyleID();
        KmlGeometry kmlGeometry = placemark.getGeometry();
        KmlStyle kmlStyle = getPlacemarkStyle(placemarkId);
        return addToMap(kmlGeometry, kmlStyle, placemarkVisibility);
    }

    /**
     * Adds placemarks with their corresponding styles onto the map
     *
     * @param kmlContainers An arraylist of folders
     */
    private void addContainerGroupToMap(Iterable<KmlContainerInterface> kmlContainers,
            boolean containerVisibility) {
        for (KmlContainerInterface containerInterface : kmlContainers) {
            KmlContainer container = (KmlContainer) containerInterface;
            Boolean isContainerVisible = getContainerVisibility(container, containerVisibility);
            if (container.getStyles() != null) {
                // Stores all found styles from the container
                mStyles.putAll(container.getStyles());
            }
            if (container.getStyleMap() != null) {
                // Stores all found style maps from the container
                mStyleMaps.putAll(container.getStyleMap());
            }
            assignStyleMap(mStyleMaps, mStyles);
            addContainerObjectToMap(container, isContainerVisible);
            if (container.hasNestedKmlContainers()) {
                addContainerGroupToMap(container.getNestedKmlContainers(), isContainerVisible);
            }
        }
    }

    /**
     * Goes through the every placemark, style and properties object within a <Folder> tag
     *
     * @param kmlContainer Folder to obtain placemark and styles from
     */
    private void addContainerObjectToMap(KmlContainer kmlContainer, boolean isContainerVisible) {
        Set<KmlPlacemark> containerPlacemarks = kmlContainer.getPlacemarks().keySet();
        for (KmlPlacemark placemark : containerPlacemarks) {
            Boolean isPlacemarkVisible = getPlacemarkVisibility(placemark);
            Boolean isObjectVisible = isContainerVisible && isPlacemarkVisible;
            Object mapObject = addPlacemarkToMap(placemark, isObjectVisible);
            kmlContainer.setPlacemark(placemark, mapObject);
        }
    }

    /**
     * Determines whether the container is visible, based on the visibility of the parent container
     * and its own specified visibility
     *
     * @param kmlContainer             The kml container to retrieve visibility
     * @param isParentContainerVisible Boolean value representing that parents visibility
     * @return Visibility of the container
     */

    private Boolean getContainerVisibility(KmlContainerInterface kmlContainer, Boolean
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
     * Obtains the styleUrl from a placemark and finds the corresponding style in a list
     *
     * @param styleId StyleUrl from a placemark
     * @return Style which corresponds to an ID
     */
    private KmlStyle getPlacemarkStyle(String styleId) {
        KmlStyle style = mStyles.get(null);
        if (mStyles.get(styleId) != null) {
            style = mStyles.get(styleId);
        }
        return style;
    }

    /**
     * Sets the marker icon if there was a url that was found
     *
     * @param style  The style which we retrieve the icon url from
     * @param marker The marker which is displaying the icon
     */
    private void addMarkerIcons(KmlStyle style, Marker marker) {
        if (mMarkerIconCache.get(style.getIconUrl()) != null) {
            // Bitmap stored in cache
            Bitmap bitmap = mMarkerIconCache.get(style.getIconUrl());
            marker.setIcon(BitmapDescriptorFactory.fromBitmap(bitmap));
        } else if (!mMarkerIconUrls.contains(style.getIconUrl())) {
            mMarkerIconUrls.add(style.getIconUrl());
        }
    }

    /**
     * Determines if there are any icons to add to markers
     */
    private void downloadMarkerIcons() {
        for (String markerIconUrl : mMarkerIconUrls) {
            new IconImageDownload(markerIconUrl).execute();
        }
        mMarkerIconUrls.clear();
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
                Double scale = placemarkStyle.getIconScale();
                ((Marker) mPlacemarks.get(placemark))
                        .setIcon(scaleIconToMarkers(iconBitmap, scale));
            }
        }
    }

    /**
     * Assigns icons to markers with a url if put in a placemark tag that is nested in a folder.
     *
     * @param iconUrl       url to obtain marker image
     * @param kmlContainers kml container which contains the marker
     */
    private void addContainerGroupIconsToMarkers(String iconUrl,
            Iterable<KmlContainerInterface> kmlContainers) {
        for (KmlContainerInterface container : kmlContainers) {
            KmlContainer containerObject = (KmlContainer) container;
            addIconToMarkers(iconUrl, containerObject.getPlacemarks());
            if (containerObject.hasNestedKmlContainers()) {
                addContainerGroupIconsToMarkers(iconUrl, containerObject.getNestedKmlContainers());
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
        // If there exists style options for a balloonStyle
        if (style.getBalloonOptions().size() > 0) {
            // Set info window if balloonStyle is set
            setMarkerInfoWindow(style, marker, mPlacemarks.keySet());
            setContainerMarkerInfoWindow(style, marker, mContainers);
        }
        if (style.getIconUrl() != null) {
            // Sets an icon image if there is a url for it
            addMarkerIcons(style, marker);
        }
        return marker;
    }

    /**
     * Sets a marker info window if no <text> tag was found in the KML document. This method sets
     * the marker title as the text found in the <name> start tag and the snippet as <description>
     *
     * @param style Style to apply
     */
    private void setMarkerInfoWindow(KmlStyle style, Marker marker,
            Iterable<KmlPlacemark> mPlacemarks) {
        for (KmlPlacemark placemark : mPlacemarks) {
            if (mStyles.get(placemark.getStyleID()).equals(style)) {
                Boolean hasName = placemark.getProperty("name") != null;
                Boolean hasDescription = placemark.getProperty("description") != null;
                if (style.getBalloonOptions().containsKey("text")) {
                    marker.setTitle(style.getBalloonOptions().get("text"));
                } else if (hasName && hasDescription) {
                    marker.setTitle(placemark.getProperty("name"));
                    marker.setSnippet(placemark.getProperty("description"));
                } else if (hasName) {
                    marker.setTitle(placemark.getProperty("name"));
                } else if (hasDescription) {
                    marker.setTitle(placemark.getProperty("description"));
                } else {
                    throw new IllegalArgumentException("Can't display BalloonStyle; no text found");
                }
            }
        }
    }

    /**
     * Determines if a marker inside a container needs to be set
     *
     * @param style      Style containting the style of the marker
     * @param marker     The marker to display the info window on
     * @param containers List of containers to find an info window of
     */
    private void setContainerMarkerInfoWindow(KmlStyle style, Marker marker,
            Iterable<KmlContainerInterface> containers) {
        for (KmlContainerInterface container : containers) {
            if (container.hasKmlPlacemarks()) {
                setMarkerInfoWindow(style, marker, container.getKmlPlacemarks());
            }
            if (hasNestedKmlContainers()) {
                setContainerMarkerInfoWindow(style, marker, container.getNestedKmlContainers());
            }
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
     * Adds all the geometries within a KML MultiGeometry to the map. Supports recursive
     * MultiGeometry. Combines styling of the placemark with the coordinates of each geometry.
     *
     * @param multiGeometry contains array of geometries for the MultiGeometry
     * @param style         contains relevant styling properties for the MultiGeometry
     * @return array of Marker, Polyline and Polygon objects
     */
    private ArrayList<Object> addMultiGeometryToMap(KmlMultiGeometry multiGeometry, KmlStyle style,
            Boolean isVisible) {
        ArrayList<Object> geometries = new ArrayList<Object>();
        ArrayList<KmlGeometry> geometry = multiGeometry.getKmlGeometryObject();
        for (KmlGeometry kmlGeometry : geometry) {
            geometries.add(addToMap(kmlGeometry, style, isVisible));
        }
        return geometries;
    }

    /**
     * Removes all the KML data from the map and clears all the stored placemarks
     */
    public void removeKmlData() {
        // TODO: remove all ground overlays
        removeKmlPlacemarks(mPlacemarks);
        if (hasNestedKmlContainers()) {
            removeKmlContainers(getNestedKmlContainers());
        }
    }

    /**
     * Removes all the KML data from the map and clears all the stored placemarks of those which
     * are in a container.
     */
    public void removeKmlContainers(Iterable<KmlContainerInterface> containers) {
        for (KmlContainerInterface container : containers) {
            KmlContainer containerObject = (KmlContainer) container;
            if (containerObject.hasKmlPlacemarks()) {
                removeKmlPlacemarks(containerObject.getPlacemarks());
            }
            if (containerObject.hasNestedKmlContainers()) {
                removeKmlContainers(containerObject.getNestedKmlContainers());
            }
        }
    }

    /**
     * Checks if the layer contains placemarks
     *
     * @return true if there are placemarks, false otherwise
     */

    public Boolean hasKmlPlacemarks() {
        return mPlacemarks.size() > 0;
    }

    /**
     * Gets an iterable of KmlPlacemark objects
     *
     * @return iterable of KmlPlacemark objects
     */
    public Iterable<KmlPlacemark> getKmlPlacemarks() {
        return mPlacemarks.keySet();
    }

    /**
     * Checks if the layer contains any KmlContainers
     *
     * @return true if there is at least 1 container within the KmlLayer, false otherwise
     */
    public boolean hasNestedKmlContainers() {
        return mContainers.size() > 0;
    }

    /**
     * Gets an iterable of KmlContainerInterface objects
     *
     * @return iterable of KmlContainerInterface objects
     */
    public Iterable<KmlContainerInterface> getNestedKmlContainers() {
        return mContainers;
    }

    /**
     * Gets an iterable of KmlGroundOverlay objects
     *
     * @return iterable of KmlGroundOverlay objects
     */
    public Iterable<KmlGroundOverlay> getGroundOverlays() {
        return mGroundOverlays.keySet();
    }

    private void addGroundOverlays(HashMap<KmlGroundOverlay, GroundOverlay> groundOverlays,
            Iterable<KmlContainerInterface> kmlContainers) {
        addGroundOverlays(groundOverlays);
        for (KmlContainerInterface kmlContainer : kmlContainers) {
            KmlContainer container = (KmlContainer) kmlContainer;
            addGroundOverlays(container.getGroundOverlayHashMap(),
                    kmlContainer.getNestedKmlContainers());
        }
    }

    private void addGroundOverlays(HashMap<KmlGroundOverlay, GroundOverlay> groundOverlays) {
        for (KmlGroundOverlay groundOverlay : groundOverlays.keySet()) {
            String groundOverlayUrl = groundOverlay.getImageUrl();
            if (groundOverlayUrl != null && groundOverlay.getLatLngBox() != null) {
                // Can't draw overlay if url and coordinates are missing
                if (mGroundOverlayCache.get(groundOverlayUrl) != null) {
                    addGroundOverlayToMap(groundOverlayUrl, mGroundOverlays);
                } else if (!mGroundOverlayUrls.contains(groundOverlayUrl)) {
                    mGroundOverlayUrls.add(groundOverlayUrl);
                }
            }
        }
    }

    private void downloadGroundOverlays() {
        for (String groundOverlayUrl : mGroundOverlayUrls) {
            new GroundOverlayImageDownload(groundOverlayUrl).execute();
        }
        mGroundOverlayUrls.clear();
    }

    private void addGroundOverlayToMap(String groundOverlayUrl,
            HashMap<KmlGroundOverlay, GroundOverlay> groundOverlays) {
        BitmapDescriptor groundOverlayBitmap = BitmapDescriptorFactory
                .fromBitmap(mGroundOverlayCache.get(groundOverlayUrl));
        for (KmlGroundOverlay groundOverlay : groundOverlays.keySet()) {
            if (groundOverlay.getImageUrl().equals(groundOverlayUrl)) {
                GroundOverlayOptions groundOverlayOptions = groundOverlay.getGroundOverlayOptions()
                        .image(groundOverlayBitmap);
                groundOverlays.put(groundOverlay, mMap.addGroundOverlay(groundOverlayOptions));
            }
        }
    }

    private void addGroundOverlayInContainerGroups(String groundOverlayUrl,
            Iterable<KmlContainerInterface> kmlContainers) {
        for (KmlContainerInterface kmlContainer : kmlContainers) {
            KmlContainer container = (KmlContainer) kmlContainer;
            addGroundOverlayToMap(groundOverlayUrl, container.getGroundOverlayHashMap());
            if (container.hasNestedKmlContainers()) {
                addGroundOverlayInContainerGroups(groundOverlayUrl,
                        container.getNestedKmlContainers());
            }
        }
    }

    /**
     * Downloads images for use as marker icons
     */

    class IconImageDownload extends AsyncTask<String, Void, Bitmap> {

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
            if (bitmap == null) {
                throw new NullPointerException("Image not found!");
            }
            mMarkerIconCache.put(mIconUrl, bitmap);
            addIconToMarkers(mIconUrl, mPlacemarks);
            addContainerGroupIconsToMarkers(mIconUrl, mContainers);
        }
    }

    /**
     * Downloads images for use as ground overlays
     */
    private class GroundOverlayImageDownload extends AsyncTask<String, Void, Bitmap> {

        private final String mGroundOverlayUrl;

        private GroundOverlayImageDownload(String groundOverlayUrl) {
            mGroundOverlayUrl = groundOverlayUrl;
        }

        /**
         * Downloads the ground overlay image in another thread
         *
         * @param params String varargs not used
         * @return Bitmap object downloaded
         */
        @Override
        protected Bitmap doInBackground(String... params) {
            try {
                return BitmapFactory
                        .decodeStream((InputStream) new URL(mGroundOverlayUrl).getContent());
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        /**
         * Adds the bitmap to the ground overlay and places it on a map
         *
         * @param bitmap bitmap downloaded
         */
        @Override
        protected void onPostExecute(Bitmap bitmap) {
            if (bitmap == null) {
                throw new NullPointerException("Image not found!");
            }
            mGroundOverlayCache.put(mGroundOverlayUrl, bitmap);
            addGroundOverlayToMap(mGroundOverlayUrl, mGroundOverlays);
            addGroundOverlayInContainerGroups(mGroundOverlayUrl, mContainers);
        }
    }
}
