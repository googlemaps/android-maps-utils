/*
 * Copyright 2020 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.google.maps.android.data.kml;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;

import androidx.annotation.Nullable;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.GroundOverlay;
import com.google.android.gms.maps.model.GroundOverlayOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.maps.android.collections.GroundOverlayManager;
import com.google.maps.android.collections.MarkerManager;
import com.google.maps.android.collections.PolygonManager;
import com.google.maps.android.collections.PolylineManager;
import com.google.maps.android.data.Feature;
import com.google.maps.android.data.Geometry;
import com.google.maps.android.data.MultiGeometry;
import com.google.maps.android.data.Renderer;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Renders all visible KmlPlacemark and KmlGroundOverlay objects onto the GoogleMap as Marker,
 * Polyline, Polygon, GroundOverlay objects. Also removes objects from the map.
 */
public class KmlRenderer extends Renderer {

    private static final String LOG_TAG = "KmlRenderer";

    private final Set<String> mGroundOverlayUrls;

    private boolean mMarkerIconsDownloaded;

    private boolean mGroundOverlayImagesDownloaded;

    private ArrayList<KmlContainer> mContainers;

    /* package */ KmlRenderer(GoogleMap map,
                              Context context,
                              MarkerManager markerManager,
                              PolygonManager polygonManager,
                              PolylineManager polylineManager,
                              GroundOverlayManager groundOverlayManager,
                              @Nullable ImagesCache imagesCache) {
        super(map, context, markerManager, polygonManager, polylineManager, groundOverlayManager, imagesCache);
        mGroundOverlayUrls = new HashSet<>();
        mMarkerIconsDownloaded = false;
        mGroundOverlayImagesDownloaded = false;
    }

    /**
     * Removes all given KML placemarks from the map and clears all stored placemarks.
     *
     * @param placemarks placemarks to remove
     */
    private void removePlacemarks(HashMap<? extends Feature, Object> placemarks) {
        // Remove map object from the map
        removeFeatures(placemarks);
    }

    /**
     * Gets the visibility of the container
     *
     * @param kmlContainer             container to check visibility of
     * @param isParentContainerVisible true if the parent container is visible, false otherwise
     * @return true if this container is visible, false otherwise
     */
    static boolean getContainerVisibility(KmlContainer kmlContainer, boolean
            isParentContainerVisible) {
        boolean isChildContainerVisible = true;
        if (kmlContainer.hasProperty("visibility")) {
            String placemarkVisibility = kmlContainer.getProperty("visibility");
            if (Integer.parseInt(placemarkVisibility) == 0) {
                isChildContainerVisible = false;
            }
        }
        return (isParentContainerVisible && isChildContainerVisible);
    }

    /**
     * Removes all the KML data from the map and clears all the stored placemarks of those which
     * are in a container.
     */
    private void removeContainers(Iterable<KmlContainer> containers) {
        for (KmlContainer container : containers) {
            removePlacemarks(container.getPlacemarksHashMap());
            removeGroundOverlays(container.getGroundOverlayHashMap());
            removeContainers(container.getContainers());
        }
    }

    public void addLayerToMap() {
        setLayerVisibility(true);
        mContainers = getContainerList();
        putStyles();
        assignStyleMap(getStyleMaps(), getStylesRenderer());
        addGroundOverlays(getGroundOverlayMap(), mContainers);
        addContainerGroupToMap(mContainers, true);
        addPlacemarksToMap(getAllFeatures());
        if (!mGroundOverlayImagesDownloaded) {
            downloadGroundOverlays();
        }
        if (!mMarkerIconsDownloaded) {
            downloadMarkerIcons();
        }
        // in case KMZ has no downloaded images
        checkClearBitmapCache();
    }

    /**
     * Stores all given data from KML file
     *
     * @param styles         hashmap of styles
     * @param styleMaps      hashmap of style maps
     * @param features       hashmap of features
     * @param folders        array of containers
     * @param groundOverlays hashmap of ground overlays
     */
    /*package*/ void storeKmlData(HashMap<String, KmlStyle> styles,
                                  HashMap<String, String> styleMaps,
                                  HashMap<KmlPlacemark, Object> features,
                                  ArrayList<KmlContainer> folders,
                                  HashMap<KmlGroundOverlay, GroundOverlay> groundOverlays) {

        storeData(styles, styleMaps, features, folders, groundOverlays);
    }

    /**
     * Stores all given data from KMZ file
     *
     * @param styles         hashmap of styles
     * @param styleMaps      hashmap of style maps
     * @param features       hashmap of features
     * @param folders        array of containers
     * @param groundOverlays hashmap of ground overlays
     * @param images         hashmap of images
     */
    /*package*/ void storeKmzData(HashMap<String, KmlStyle> styles,
                                  HashMap<String, String> styleMaps,
                                  HashMap<KmlPlacemark, Object> features,
                                  ArrayList<KmlContainer> folders,
                                  HashMap<KmlGroundOverlay, GroundOverlay> groundOverlays,
                                  HashMap<String, Bitmap> images) {

        storeData(styles, styleMaps, features, folders, groundOverlays);
        for (Map.Entry<String, Bitmap> entry : images.entrySet()) {
            cacheBitmap(entry.getKey(), entry.getValue());
        }
    }

    /**
     * Sets the map that objects are being placed on
     *
     * @param map map to place placemark, container, style and ground overlays on
     */
    public void setMap(GoogleMap map) {
        removeLayerFromMap();
        super.setMap(map);
        addLayerToMap();
    }

    /**
     * Checks if the layer contains placemarks
     *
     * @return true if there are placemarks, false otherwise
     */
    /* package */ boolean hasKmlPlacemarks() {
        return hasFeatures();
    }

    /**
     * Gets an iterable of KmlPlacemark objects
     *
     * @return iterable of KmlPlacemark objects
     */
    /* package */ Iterable<? extends Feature> getKmlPlacemarks() {
        return (getFeatures());
    }

    /**
     * Checks if the layer contains any KmlContainers
     *
     * @return true if there is at least 1 container within the KmlLayer, false otherwise
     */
    public boolean hasNestedContainers() {
        return mContainers.size() > 0;
    }

    /**
     * Gets an iterable of KmlContainerInterface objects
     *
     * @return iterable of KmlContainerInterface objects
     */
    public Iterable<KmlContainer> getNestedContainers() {
        return mContainers;
    }

    /**
     * Gets an iterable of KmlGroundOverlay objects
     *
     * @return iterable of KmlGroundOverlay objects
     */
    public Iterable<KmlGroundOverlay> getGroundOverlays() {
        return getGroundOverlayMap().keySet();
    }

    /**
     * Removes all the KML data from the map and clears all the stored placemarks
     */
    public void removeLayerFromMap() {
        removePlacemarks(getAllFeatures());
        removeGroundOverlays(getGroundOverlayMap());
        if (hasNestedContainers()) {
            removeContainers(getNestedContainers());
        }
        setLayerVisibility(false);
        clearStylesRenderer();
    }

    /**
     * Iterates over the placemarks, gets its style or assigns a default one and adds it to the map
     *
     * @param placemarks
     */
    private void addPlacemarksToMap(HashMap<? extends Feature, Object> placemarks) {
        for (Feature kmlPlacemark : placemarks.keySet()) {
            addFeature(kmlPlacemark);
        }
    }

    /**
     * Adds placemarks with their corresponding styles onto the map
     *
     * @param kmlContainers An arraylist of folders
     */
    private void addContainerGroupToMap(Iterable<KmlContainer> kmlContainers,
                                        boolean containerVisibility) {
        for (KmlContainer container : kmlContainers) {
            boolean isContainerVisible = getContainerVisibility(container, containerVisibility);
            if (container.getStyles() != null) {
                // Stores all found styles from the container
                putStyles(container.getStyles());
            }
            if (container.getStyleMap() != null) {
                // Stores all found style maps from the container
                super.assignStyleMap(container.getStyleMap(), getStylesRenderer());
            }
            addContainerObjectToMap(container, isContainerVisible);
            if (container.hasContainers()) {
                addContainerGroupToMap(container.getContainers(), isContainerVisible);
            }
        }
    }

    /**
     * Goes through the every placemark, style and properties object within a <Folder> tag
     *
     * @param kmlContainer Folder to obtain placemark and styles from
     */
    private void addContainerObjectToMap(KmlContainer kmlContainer, boolean isContainerVisible) {

        for (Feature placemark : kmlContainer.getPlacemarks()) {
            boolean isPlacemarkVisible = getPlacemarkVisibility(placemark);
            boolean isObjectVisible = isContainerVisible && isPlacemarkVisible;
            if (placemark.getGeometry() != null) {
                String placemarkId = placemark.getId();
                Geometry geometry = placemark.getGeometry();
                KmlStyle style = getPlacemarkStyle(placemarkId);
                KmlStyle inlineStyle = ((KmlPlacemark) placemark).getInlineStyle();
                Object mapObject = addKmlPlacemarkToMap((KmlPlacemark) placemark, geometry,
                        style, inlineStyle, isObjectVisible);
                kmlContainer.setPlacemark((KmlPlacemark) placemark, mapObject);
                putContainerFeature(mapObject, placemark);
            }
        }
    }

    /**
     * Determines if there are any icons to add to markers
     */
    private void downloadMarkerIcons() {
        mMarkerIconsDownloaded = true;
        for (Iterator<String> iterator = getMarkerIconUrls().iterator(); iterator.hasNext(); ) {
            String markerIconUrl = iterator.next();
            new MarkerIconImageDownload(markerIconUrl).execute();
            iterator.remove();
        }
    }

    /**
     * Adds the marker icon stored in mMarkerIconCache, to the {@link com.google.android.gms.maps.model.Marker}
     * recursing over multi-geometry placemarks to add icons to all point geometries
     *
     * @param iconUrl    icon url of icon to add to markers
     * @param placemarks map of placemark to features
     */
    private void addIconToMarkers(String iconUrl, HashMap<KmlPlacemark, Object> placemarks) {
        for (Feature placemark : placemarks.keySet()) {
            KmlStyle urlStyle = getStylesRenderer().get(placemark.getId());
            KmlStyle inlineStyle = ((KmlPlacemark) placemark).getInlineStyle();
            Geometry geometry = placemark.getGeometry();
            Object object = placemarks.get(placemark);
            addIconToGeometry(iconUrl, urlStyle, inlineStyle, geometry, object);
        }
    }

    /**
     * Checks for point geometry and adds icon to marker or
     * recurses over multi-geometries to add icon to point sub-geometries
     *
     * @param iconUrl     icon url of icon to add to markers
     * @param urlStyle    url style for placemark
     * @param inlineStyle inline style for placemark
     * @param geometry    geometry to check
     * @param object      object related to geometry, marker if point
     *                    or list of sub-objects for multi-geometries
     */
    private void addIconToGeometry(String iconUrl, KmlStyle urlStyle, KmlStyle inlineStyle, Geometry geometry, Object object) {
        if (geometry == null) return;
        if ("Point".equals(geometry.getGeometryType())) {
            addIconToMarker(iconUrl, urlStyle, inlineStyle, (Marker) object);
        } else if ("MultiGeometry".equals(geometry.getGeometryType())) {
            addIconToMultiGeometry(iconUrl, urlStyle, inlineStyle, (MultiGeometry) geometry, (List<Object>) object);
        }
    }

    /**
     * Adds icon to point sub-geometries of multi-geometry placemarks
     *
     * @param iconUrl       icon url of icon to add to markers
     * @param urlStyle      url style for placemark
     * @param inlineStyle   inline style for placemark
     * @param multiGeometry multi-geometry to iterator over sub-geometries of
     * @param objects       list of sub-objects for sub-geometries
     */
    private void addIconToMultiGeometry(String iconUrl, KmlStyle urlStyle, KmlStyle inlineStyle, MultiGeometry multiGeometry, List<Object> objects) {
        Iterator<Geometry> geometries = multiGeometry.getGeometryObject().iterator();
        Iterator<Object> objItr = objects.iterator();
        while (geometries.hasNext() && objItr.hasNext()) {
            Geometry geometry = geometries.next();
            Object object = objItr.next();
            addIconToGeometry(iconUrl, urlStyle, inlineStyle, geometry, object);
        }
    }

    /**
     * Add icon to marker for point geometry placemarks
     *
     * @param iconUrl     icon url of icon to add to markers
     * @param urlStyle    url style for placemark
     * @param inlineStyle inline style for placemark
     * @param marker      marker to add icon to
     */
    private void addIconToMarker(String iconUrl, KmlStyle urlStyle, KmlStyle inlineStyle, Marker marker) {
        boolean isInlineStyleIcon = inlineStyle != null && iconUrl
                .equals(inlineStyle.getIconUrl());
        boolean isPlacemarkStyleIcon = urlStyle != null && iconUrl
                .equals(urlStyle.getIconUrl());
        if (isInlineStyleIcon) {
            scaleBitmap(inlineStyle, marker);
        } else if (isPlacemarkStyleIcon) {
            scaleBitmap(urlStyle, marker);
        }
    }

    /**
     * Enlarges or shrinks a bitmap image based on the scale provided
     *
     * @param style  Style to retrieve iconUrl and scale from
     * @param marker Marker to set the image to
     */
    private void scaleBitmap(KmlStyle style, Marker marker) {
        double bitmapScale = style.getIconScale();
        String bitmapUrl = style.getIconUrl();
        BitmapDescriptor scaledBitmap = getCachedMarkerImage(bitmapUrl, bitmapScale);
        marker.setIcon(scaledBitmap);
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
            addIconToMarkers(iconUrl, container.getPlacemarksHashMap());
            if (container.hasContainers()) {
                addContainerGroupIconsToMarkers(iconUrl, container.getContainers());
            }
        }
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
                    container.getContainers());
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
                if (getCachedGroundOverlayImage(groundOverlayUrl) != null) {
                    addGroundOverlayToMap(groundOverlayUrl, groundOverlays, true);
                } else {
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
                                       HashMap<KmlGroundOverlay, GroundOverlay> groundOverlays, boolean containerVisibility) {
        BitmapDescriptor groundOverlayBitmap = getCachedGroundOverlayImage(groundOverlayUrl);
        for (KmlGroundOverlay kmlGroundOverlay : groundOverlays.keySet()) {
            if (kmlGroundOverlay.getImageUrl().equals(groundOverlayUrl)) {
                GroundOverlayOptions groundOverlayOptions = kmlGroundOverlay.getGroundOverlayOptions()
                        .image(groundOverlayBitmap);
                GroundOverlay mapGroundOverlay = attachGroundOverlay(groundOverlayOptions);
                if (!containerVisibility) {
                    mapGroundOverlay.setVisible(false);
                }
                groundOverlays.put(kmlGroundOverlay, mapGroundOverlay);
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
                                                   Iterable<KmlContainer> kmlContainers, boolean containerVisibility) {
        for (KmlContainer container : kmlContainers) {
            boolean isContainerVisible = getContainerVisibility(container, containerVisibility);
            addGroundOverlayToMap(groundOverlayUrl, container.getGroundOverlayHashMap(), isContainerVisible);
            if (container.hasContainers()) {
                addGroundOverlayInContainerGroups(groundOverlayUrl,
                        container.getContainers(), isContainerVisible);
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
            downloadStarted();
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
                return getBitmapFromUrl(mIconUrl);
            } catch (MalformedURLException e) {
                return BitmapFactory.decodeFile(mIconUrl);
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
                Log.e(LOG_TAG, "Image at this URL could not be found " + mIconUrl);
            } else {
                cacheBitmap(mIconUrl, bitmap);
                if (isLayerOnMap()) {
                    addIconToMarkers(mIconUrl, (HashMap<KmlPlacemark, Object>) getAllFeatures());
                    addContainerGroupIconsToMarkers(mIconUrl, mContainers);
                }
            }
            downloadFinished();
        }
    }

    /**
     * Downloads images for use as ground overlays
     */
    private class GroundOverlayImageDownload extends AsyncTask<String, Void, Bitmap> {

        private final String mGroundOverlayUrl;

        public GroundOverlayImageDownload(String groundOverlayUrl) {
            mGroundOverlayUrl = groundOverlayUrl;
            downloadStarted();
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
                return getBitmapFromUrl(mGroundOverlayUrl);
            } catch (MalformedURLException e) {
                return BitmapFactory.decodeFile(mGroundOverlayUrl);
            } catch (IOException e) {
                Log.e(LOG_TAG, "Image [" + mGroundOverlayUrl + "] download issue", e);
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
                Log.e(LOG_TAG, "Image at this URL could not be found " + mGroundOverlayUrl);
            } else {
                cacheBitmap(mGroundOverlayUrl, bitmap);
                if (isLayerOnMap()) {
                    addGroundOverlayToMap(mGroundOverlayUrl, getGroundOverlayMap(), true);
                    addGroundOverlayInContainerGroups(mGroundOverlayUrl, mContainers, true);
                }
            }
            downloadFinished();
        }
    }

    /**
     * @param url internet address of the image.
     * @return the bitmap of that image, scaled according to screen density.
     */
    private Bitmap getBitmapFromUrl(String url) throws IOException {
        return BitmapFactory.decodeStream(openConnectionCheckRedirects(new URL(url).openConnection()));
    }

    /**
     * opens a stream allowing redirects.
     */
    private InputStream openConnectionCheckRedirects(URLConnection c) throws IOException {
        boolean redir;
        int redirects = 0;
        InputStream in;
        do {
            if (c instanceof HttpURLConnection) {
                ((HttpURLConnection) c).setInstanceFollowRedirects(false);
            }
            // We want to open the input stream before getting headers
            // because getHeaderField() et al swallow IOExceptions.
            in = c.getInputStream();
            redir = false;
            if (c instanceof HttpURLConnection) {
                HttpURLConnection http = (HttpURLConnection) c;
                int stat = http.getResponseCode();
                if (stat >= 300 && stat <= 307 && stat != 306 &&
                        stat != HttpURLConnection.HTTP_NOT_MODIFIED) {
                    URL base = http.getURL();
                    String loc = http.getHeaderField("Location");
                    URL target = null;
                    if (loc != null) {
                        target = new URL(base, loc);
                    }
                    http.disconnect();
                    // Redirection should be allowed only for HTTP and HTTPS
                    // and should be limited to 5 redirections at most.
                    if (target == null || !(target.getProtocol().equals("http")
                            || target.getProtocol().equals("https"))
                            || redirects >= 5) {
                        throw new SecurityException("illegal URL redirect");
                    }
                    redir = true;
                    c = target.openConnection();
                    redirects++;
                }
            }
        } while (redir);
        return in;
    }
}
