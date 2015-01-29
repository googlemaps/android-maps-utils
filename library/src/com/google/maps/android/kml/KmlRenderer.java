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
import java.util.Set;

/**
 * Renders KmlPlacemark, KmlContainer and KmlGroundOverlay objects onto the GoogleMap as Marker,
 * Polyline, Polygon, GroundOverlay objects. Also removes objects from the map.
 */
/* package */ class KmlRenderer {

    private static final int LRU_CACHE_SIZE = 50;

    private final LruCache<String, Bitmap> mImagesCache;

    private final ArrayList<String> mMarkerIconUrls;

    private final ArrayList<String> mGroundOverlayUrls;

    private GoogleMap mMap;

    private HashMap<KmlPlacemark, Object> mPlacemarks;

    private HashMap<String, String> mStyleMaps;

    private ArrayList<KmlContainer> mContainers;

    private HashMap<String, KmlStyle> mStyles;

    private HashMap<KmlGroundOverlay, GroundOverlay> mGroundOverlays;

    private HashMap<String, KmlStyle> mOriginalStyles;

    private boolean mLayerVisible;

    private boolean mMarkerIconsDownloaded;

    private boolean mGroundOverlayImagesDownloaded;

    /**
     * Creates a new KmlRenderer object
     *
     * @param map map to place placemark, container, style and ground overlays on
     */
    /* package */ KmlRenderer(GoogleMap map) {
        mMap = map;
        mImagesCache = new LruCache<String, Bitmap>(LRU_CACHE_SIZE);
        mMarkerIconUrls = new ArrayList<String>();
        mGroundOverlayUrls = new ArrayList<String>();
        mLayerVisible = false;
        mMarkerIconsDownloaded = false;
        mGroundOverlayImagesDownloaded = false;
    }

    /**
     * Computes a random color given an integer. Algorithm to compute the random color can be
     * found in https://developers.google.com/kml/documentation/kmlreference#colormode
     *
     * @param color Integer value representing a color
     * @return Integer representing a random color
     */
    /* package */
    static int computeRandomColor(int color) {
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
    private static void removePlacemarks(HashMap<KmlPlacemark, Object> placemarks) {
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
    }

    /**
     * Determines whether the container is visible, based on the visibility of the parent container
     * and its own specified visibility
     *
     * @param kmlContainer             The kml container to retrieve visibility
     * @param isParentContainerVisible Boolean value representing that parents visibility
     * @return Visibility of the container
     */

    /*package*/
    static Boolean getContainerVisibility(KmlContainer kmlContainer, Boolean
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

    private void removeGroundOverlays(HashMap<KmlGroundOverlay, GroundOverlay> groundOverlays) {
        for (GroundOverlay groundOverlay : groundOverlays.values()) {
            if (groundOverlay != null) {
                groundOverlay.remove();
            }
        }
    }

    /**
     * Removes all the KML data from the map and clears all the stored placemarks of those which
     * are in a container.
     */
    private void removeContainers(Iterable<KmlContainer> containers) {
        for (KmlContainer container : containers) {
            removePlacemarks(container.getPlacemarks());
            removeGroundOverlays(container.getGroundOverlayHashMap());
            removeContainers(container.getNestedKmlContainers());
        }
    }

    /**
     * Iterates a list of styles and assigns a style
     */
    /*package*/ void assignStyleMap(HashMap<String, String> styleMap,
            HashMap<String, KmlStyle> styles) {
        for (String styleMapKey : styleMap.keySet()) {
            String styleMapValue = styleMap.get(styleMapKey);
            if (styles.containsKey(styleMapValue)) {
                styles.put(styleMapKey, styles.get(styleMapValue));
            }
        }
    }

    /**
     * Stores all given data and adds it onto the map
     *
     * @param styles         hashmap of styles
     * @param styleMaps      hashmap of style maps
     * @param placemarks     hashmap of placemarks
     * @param folders        array of containers
     * @param groundOverlays hashmap of ground overlays
     */
    /* package */ void storeKmlData(HashMap<String, KmlStyle> styles,
            HashMap<String, String> styleMaps,
            HashMap<KmlPlacemark, Object> placemarks, ArrayList<KmlContainer> folders,
            HashMap<KmlGroundOverlay, GroundOverlay> groundOverlays) {
        mStyles = styles;
        mStyleMaps = styleMaps;
        mPlacemarks = placemarks;
        mContainers = folders;
        mGroundOverlays = groundOverlays;
        // Need to keep an original copy of the styles in case we override
        mOriginalStyles = styles;
    }

    /* package */ void addKmlData() {
        mStyles = mOriginalStyles;
        assignStyleMap(mStyleMaps, mStyles);
        addGroundOverlays(mGroundOverlays, mContainers);
        addContainerGroupToMap(mContainers, true);
        addPlacemarksToMap(mPlacemarks);
        if (!mGroundOverlayImagesDownloaded) {
            downloadGroundOverlays();
        }
        if (!mMarkerIconsDownloaded) {
            downloadMarkerIcons();
        }
        mLayerVisible = true;
    }

    /**
     * Gets the map that objects are being placed on
     *
     * @return map
     */
    /* package */ GoogleMap getMap() {
        return mMap;
    }

    /**
     * Sets the map that objects are being placed on
     *
     * @param map map to place placemark, container, style and ground overlays on
     */
    /* package */ void setMap(GoogleMap map) {
        removeKmlData();
        mMap = map;
        mStyles = mOriginalStyles;
        addKmlData();
    }

    /**
     * Checks if the layer contains placemarks
     *
     * @return true if there are placemarks, false otherwise
     */
    /* package */ Boolean hasKmlPlacemarks() {
        return mPlacemarks.size() > 0;
    }

    /**
     * Gets an iterable of KmlPlacemark objects
     *
     * @return iterable of KmlPlacemark objects
     */
    /* package */ Iterable<KmlPlacemark> getKmlPlacemarks() {
        return mPlacemarks.keySet();
    }

    /**
     * Checks if the layer contains any KmlContainers
     *
     * @return true if there is at least 1 container within the KmlLayer, false otherwise
     */
    /* package */ boolean hasNestedContainers() {
        return mContainers.size() > 0;
    }

    /**
     * Gets an iterable of KmlContainerInterface objects
     *
     * @return iterable of KmlContainerInterface objects
     */
    /* package */ Iterable<KmlContainer> getNestedContainers() {
        return mContainers;
    }

    /**
     * Gets an iterable of KmlGroundOverlay objects
     *
     * @return iterable of KmlGroundOverlay objects
     */
    /* package */ Iterable<KmlGroundOverlay> getGroundOverlays() {
        return mGroundOverlays.keySet();
    }

    /**
     * Removes all the KML data from the map and clears all the stored placemarks
     */
    /* package */ void removeKmlData() {
        removePlacemarks(mPlacemarks);
        removeGroundOverlays(mGroundOverlays);
        if (hasNestedContainers()) {
            removeContainers(getNestedContainers());
        }
        mLayerVisible = false;
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
        KmlGeometry geometry = placemark.getGeometry();
        KmlStyle style = getPlacemarkStyle(placemarkId);
        KmlStyle inlineStyle = placemark.getInlineStyle();
        return addToMap(geometry, style, inlineStyle, placemarkVisibility);
    }

    /**
     * Adds placemarks with their corresponding styles onto the map
     *
     * @param kmlContainers An arraylist of folders
     */
    private void addContainerGroupToMap(Iterable<KmlContainer> kmlContainers,
            boolean containerVisibility) {
        for (KmlContainer container : kmlContainers) {
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
     * @param styleUrl      The style which we retrieve the icon url from
     * @param markerOptions The marker which is displaying the icon
     */
    private void addMarkerIcons(String styleUrl, MarkerOptions markerOptions) {
        if (mImagesCache.get(styleUrl) != null) {
            // Bitmap stored in cache
            Bitmap bitmap = mImagesCache.get(styleUrl);
            markerOptions.icon(BitmapDescriptorFactory.fromBitmap(bitmap));
        } else if (!mMarkerIconUrls.contains(styleUrl)) {
            mMarkerIconUrls.add(styleUrl);
        }
    }

    /**
     * Determines if there are any icons to add to markers
     */
    private void downloadMarkerIcons() {
        mMarkerIconsDownloaded = true;
        for (Iterator<String> iterator = mMarkerIconUrls.iterator(); iterator.hasNext(); ) {
            String markerIconUrl = iterator.next();
            new MarkerIconImageDownload(markerIconUrl).execute();
            iterator.remove();
        }
    }

    /**
     * Adds the marker icon stored in mMarkerIconCache, to the {@link com.google.android.gms.maps.model.Marker}
     *
     * @param iconUrl icon url of icon to add to markers
     */
    private void addIconToMarkers(String iconUrl, HashMap<KmlPlacemark, Object> mPlacemarks) {
        for (KmlPlacemark placemark : mPlacemarks.keySet()) {
            KmlStyle placemarkStyle = mStyles.get(placemark.getStyleID());
            if ("Point".equals(placemark.getGeometry().getKmlGeometryType())) {
                boolean isInlineStyleIcon = placemark.getInlineStyle() != null && iconUrl
                        .equals(placemark.getInlineStyle().getIconUrl());
                boolean isPlacemarkStyleIcon = placemarkStyle != null && iconUrl
                        .equals(placemarkStyle.getIconUrl());
                if (isInlineStyleIcon) {
                    Bitmap iconBitmap = mImagesCache.get(iconUrl);
                    Double scale = placemark.getInlineStyle().getIconScale();
                    ((Marker) mPlacemarks.get(placemark)).setIcon(
                            scaleIconToMarkers(iconBitmap, scale));
                } else if (isPlacemarkStyleIcon) {
                    Bitmap iconBitmap = mImagesCache.get(iconUrl);
                    Double scale = placemarkStyle.getIconScale();
                    ((Marker) mPlacemarks.get(placemark)).setIcon(
                            scaleIconToMarkers(iconBitmap, scale));
                }
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
            Iterable<KmlContainer> kmlContainers) {
        for (KmlContainer container : kmlContainers) {
            addIconToMarkers(iconUrl, container.getPlacemarks());
            if (container.hasNestedKmlContainers()) {
                addContainerGroupIconsToMarkers(iconUrl, container.getNestedKmlContainers());
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
    private Object addToMap(KmlGeometry geometry, KmlStyle style, KmlStyle inlineStyle,
            Boolean isVisible) {
        String geometryType = geometry.getKmlGeometryType();
        if (geometryType.equals("Point")) {
            Marker marker = addPointToMap((KmlPoint) geometry, style, inlineStyle);
            marker.setVisible(isVisible);
            return marker;
        } else if (geometryType.equals("LineString")) {
            Polyline polyline = addLineStringToMap((KmlLineString) geometry, style, inlineStyle);
            polyline.setVisible(isVisible);
            return polyline;
        } else if (geometryType.equals("Polygon")) {
            Polygon polygon = addPolygonToMap((KmlPolygon) geometry, style, inlineStyle);
            polygon.setVisible(isVisible);
            return polygon;
        } else if (geometryType.equals("MultiGeometry")) {
            return addMultiGeometryToMap((KmlMultiGeometry) geometry, style, inlineStyle,
                    isVisible);
        }
        return null;
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
            Iterable<KmlContainer> containers) {
        for (KmlContainer container : containers) {
            if (container.hasKmlPlacemarks()) {
                setMarkerInfoWindow(style, marker, container.getKmlPlacemarks());
            }
            if (hasNestedContainers()) {
                setContainerMarkerInfoWindow(style, marker, container.getNestedKmlContainers());
            }
        }
    }

    /**
     * Adds a KML Point to the map as a Marker by combining the styling and coordinates
     *
     * @param point contains coordinates for the Marker
     * @param style contains relevant styling properties for the Marker
     * @return Marker object
     */
    private Marker addPointToMap(KmlPoint point, KmlStyle style, KmlStyle markerInlineStyle) {
        MarkerOptions markerUrlStyle = style.getMarkerOptions();
        markerUrlStyle.position(point.getKmlGeometryObject());
        if (markerInlineStyle != null) {
            setInlinePointStyle(markerUrlStyle, markerInlineStyle, style.getIconUrl());
        } else if (style.getIconUrl() != null) {
            // Use shared style
            addMarkerIcons(style.getIconUrl(), markerUrlStyle);
        }
        Marker marker = mMap.addMarker(markerUrlStyle);
        // If there exists style options for a balloonStyle
        if (style.getBalloonOptions().size() > 0) {
            // Set info window if balloonStyle is set
            setMarkerInfoWindow(style, marker, mPlacemarks.keySet());
            setContainerMarkerInfoWindow(style, marker, mContainers);
        }

        return marker;
    }

    private void setInlinePointStyle(MarkerOptions markerUrlStyle, KmlStyle inlineStyle,
            String markerUrlIconUrl) {
        MarkerOptions inlineMarkerOptions = inlineStyle.getMarkerOptions();
        if (inlineStyle.isStyleSet("heading")) {
            markerUrlStyle.rotation(inlineMarkerOptions.getRotation());
        }
        if (inlineStyle.isStyleSet("hotSpot")) {
            markerUrlStyle
                    .anchor(inlineMarkerOptions.getAnchorU(), inlineMarkerOptions.getAnchorV());
        }
        if (inlineStyle.isStyleSet("markerColor")) {
            markerUrlStyle.icon(inlineMarkerOptions.getIcon());
        }
        if (inlineStyle.isStyleSet("iconUrl")) {
            addMarkerIcons(inlineStyle.getIconUrl(), markerUrlStyle);
        } else if (markerUrlIconUrl != null) {
            // Inline style with no icon defined
            addMarkerIcons(markerUrlIconUrl, markerUrlStyle);
        }
    }

    /**
     * Adds a KML LineString to the map as a Polyline by combining the styling and coordinates
     *
     * @param lineString contains coordinates for the Polyline
     * @param style      contains relevant styling properties for the Polyline
     * @return Polyline object
     */
    private Polyline addLineStringToMap(KmlLineString lineString, KmlStyle style,
            KmlStyle inlineStyle) {
        PolylineOptions polylineOptions = style.getPolylineOptions();
        polylineOptions.addAll(lineString.getKmlGeometryObject());
        if (inlineStyle != null) {
            setInlineLineStringStyle(polylineOptions, inlineStyle);
        } else if (style.isLineRandomColorMode()) {
            polylineOptions.color(computeRandomColor(polylineOptions.getColor()));
        }
        return mMap.addPolyline(polylineOptions);
    }

    private void setInlineLineStringStyle(PolylineOptions polylineOptions, KmlStyle inlineStyle) {
        PolylineOptions inlinePolylineOptions = inlineStyle.getPolylineOptions();
        if (inlineStyle.isStyleSet("outlineColor")) {
            polylineOptions.color(inlinePolylineOptions.getColor());
        }
        if (inlineStyle.isStyleSet("width")) {
            polylineOptions.width(inlinePolylineOptions.getWidth());
        }
        if (inlineStyle.isLineRandomColorMode()) {
            polylineOptions.color(computeRandomColor(inlinePolylineOptions.getColor()));
        }
    }

    /**
     * Adds a KML Polygon to the map as a Polygon by combining the styling and coordinates
     *
     * @param polygon contains coordinates for the Polygon
     * @param style   contains relevant styling properties for the Polygon
     * @return Polygon object
     */
    private Polygon addPolygonToMap(KmlPolygon polygon, KmlStyle style, KmlStyle inlineStyle) {
        PolygonOptions polygonOptions = style.getPolygonOptions();
        polygonOptions.addAll(polygon.getOuterBoundaryCoordinates());
        for (ArrayList<LatLng> innerBoundary : polygon.getInnerBoundaryCoordinates()) {
            polygonOptions.addHole(innerBoundary);
        }
        if (inlineStyle != null) {
            setInlinePolygonStyle(polygonOptions, inlineStyle);
        } else if (style.isPolyRandomColorMode()) {
            polygonOptions.fillColor(computeRandomColor(polygonOptions.getFillColor()));
        }
        return mMap.addPolygon(polygonOptions);
    }

    private void setInlinePolygonStyle(PolygonOptions polygonUrlStyle, KmlStyle inlineStyle) {
        PolygonOptions inlinePolygonOptions = inlineStyle.getPolygonOptions();
        if (inlineStyle.hasFill() && inlineStyle.isStyleSet("fillColor")) {
            polygonUrlStyle.fillColor(inlinePolygonOptions.getFillColor());
        }
        if (inlineStyle.hasOutline()) {
            if (inlineStyle.isStyleSet("outlineColor")) {
                polygonUrlStyle.strokeColor(inlinePolygonOptions.getStrokeColor());
            }
            if (inlineStyle.isStyleSet("width")) {
                polygonUrlStyle.strokeWidth(inlinePolygonOptions.getStrokeWidth());
            }
        }
        if (inlineStyle.isPolyRandomColorMode()) {
            polygonUrlStyle.fillColor(computeRandomColor(inlinePolygonOptions.getFillColor()));
        }
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
            KmlStyle inlineStyle, Boolean isVisible) {
        ArrayList<Object> geometries = new ArrayList<Object>();
        ArrayList<KmlGeometry> geometry = multiGeometry.getKmlGeometryObject();
        for (KmlGeometry kmlGeometry : geometry) {
            geometries.add(addToMap(kmlGeometry, style, inlineStyle, isVisible));
        }
        return geometries;
    }

    /**
     * Adds a ground overlay adds all the ground overlays onto the map and recursively adds all
     * ground overlays stored in the given containers
     *
     * @param groundOverlays ground overlays to add to the map
     * @param kmlContainers  containers to check for ground overlays
     */
    private void addGroundOverlays(HashMap<KmlGroundOverlay, GroundOverlay> groundOverlays,
            Iterable<KmlContainer> kmlContainers) {
        addGroundOverlays(groundOverlays);
        for (KmlContainer container : kmlContainers) {
            addGroundOverlays(container.getGroundOverlayHashMap(),
                    container.getNestedKmlContainers());
        }
    }

    /**
     * Adds all given ground overlays to the map
     *
     * @param groundOverlays hashmap of ground overlays to add to the map
     */
    private void addGroundOverlays(HashMap<KmlGroundOverlay, GroundOverlay> groundOverlays) {
        for (KmlGroundOverlay groundOverlay : groundOverlays.keySet()) {
            String groundOverlayUrl = groundOverlay.getImageUrl();
            if (groundOverlayUrl != null && groundOverlay.getLatLngBox() != null) {
                // Can't draw overlay if url and coordinates are missing
                if (mImagesCache.get(groundOverlayUrl) != null) {
                    addGroundOverlayToMap(groundOverlayUrl, mGroundOverlays);
                } else if (!mGroundOverlayUrls.contains(groundOverlayUrl)) {
                    mGroundOverlayUrls.add(groundOverlayUrl);
                }
            }
        }
    }

    /**
     * Downloads images of all ground overlays
     */
    private void downloadGroundOverlays() {
        mGroundOverlayImagesDownloaded = true;
        for (Iterator<String> iterator = mGroundOverlayUrls.iterator(); iterator.hasNext(); ) {
            String groundOverlayUrl = iterator.next();
            new GroundOverlayImageDownload(groundOverlayUrl).execute();
            iterator.remove();
        }
    }

    /**
     * Adds ground overlays from a given URL onto the map
     *
     * @param groundOverlayUrl url of ground overlay
     * @param groundOverlays   hashmap of ground overlays to add to the map
     */
    private void addGroundOverlayToMap(String groundOverlayUrl,
            HashMap<KmlGroundOverlay, GroundOverlay> groundOverlays) {
        BitmapDescriptor groundOverlayBitmap = BitmapDescriptorFactory
                .fromBitmap(mImagesCache.get(groundOverlayUrl));
        for (KmlGroundOverlay groundOverlay : groundOverlays.keySet()) {
            if (groundOverlay.getImageUrl().equals(groundOverlayUrl)) {
                GroundOverlayOptions groundOverlayOptions = groundOverlay.getGroundOverlayOptions()
                        .image(groundOverlayBitmap);
                groundOverlays.put(groundOverlay, mMap.addGroundOverlay(groundOverlayOptions));
            }
        }
    }

    /**
     * Adds ground overlays in containers from a given URL onto the map
     *
     * @param groundOverlayUrl url of ground overlay
     * @param kmlContainers    containers containing ground overlays to add to the map
     */
    private void addGroundOverlayInContainerGroups(String groundOverlayUrl,
            Iterable<KmlContainer> kmlContainers) {
        for (KmlContainer container : kmlContainers) {
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
    private class MarkerIconImageDownload extends AsyncTask<String, Void, Bitmap> {

        private final String mIconUrl;

        /**
         * Creates a new IconImageDownload object
         *
         * @param iconUrl URL of the marker icon to download
         */
        public MarkerIconImageDownload(String iconUrl) {
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
            mImagesCache.put(mIconUrl, bitmap);
            if (mLayerVisible) {
                addIconToMarkers(mIconUrl, mPlacemarks);
                addContainerGroupIconsToMarkers(mIconUrl, mContainers);
            }
        }
    }

    /**
     * Downloads images for use as ground overlays
     */
    private class GroundOverlayImageDownload extends AsyncTask<String, Void, Bitmap> {

        private final String mGroundOverlayUrl;

        public GroundOverlayImageDownload(String groundOverlayUrl) {
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
            if (mLayerVisible) {
                mImagesCache.put(mGroundOverlayUrl, bitmap);
                addGroundOverlayToMap(mGroundOverlayUrl, mGroundOverlays);
                addGroundOverlayInContainerGroups(mGroundOverlayUrl, mContainers);
            }
        }
    }
}
