package com.google.maps.android.data.kml;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.GroundOverlay;
import com.google.android.gms.maps.model.GroundOverlayOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.maps.android.data.Feature;
import com.google.maps.android.data.Renderer;
import com.google.maps.android.data.Geometry;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

/**
 * Renders all visible KmlPlacemark and KmlGroundOverlay objects onto the GoogleMap as Marker,
 * Polyline, Polygon, GroundOverlay objects. Also removes objects from the map.
 */
public class KmlRenderer  extends Renderer {

    private static final String LOG_TAG = "KmlRenderer";

    private final ArrayList<String> mGroundOverlayUrls;

    private boolean mMarkerIconsDownloaded;

    private boolean mGroundOverlayImagesDownloaded;

    private HashMap<KmlGroundOverlay, GroundOverlay> mGroundOverlays;

    private ArrayList<KmlContainer> mContainers;

    /* package */ KmlRenderer(GoogleMap map, Context context) {
        super(map, context);
        mGroundOverlayUrls = new ArrayList<>();
        mMarkerIconsDownloaded = false;
        mGroundOverlayImagesDownloaded = false;
    }

    /**
     * Scales a Bitmap to a specified float.
     *
     * @param unscaledBitmap Unscaled bitmap image to scale.
     * @param scale Scale value. A "1.0" scale value corresponds to the original size of the Bitmap
     * @return A scaled bitmap image
     */
    private static BitmapDescriptor scaleIcon(Bitmap unscaledBitmap, Double scale) {
        int width = (int) (unscaledBitmap.getWidth() * scale);
        int height = (int) (unscaledBitmap.getHeight() * scale);
        Bitmap scaledBitmap = Bitmap.createScaledBitmap(unscaledBitmap, width, height, false);
        return BitmapDescriptorFactory.fromBitmap(scaledBitmap);
    }

    /**
     * Removes all given KML placemarks from the map and clears all stored placemarks.
     *
     * @param placemarks placemarks to remove
     */
    private void removePlacemarks(HashMap<? extends Feature, Object> placemarks) {
        // Remove map object from the map
        removeFeatures((HashMap<Feature, Object>) placemarks);
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
     * Removes all ground overlays in the given hashmap
     *
     * @param groundOverlays hashmap of ground overlays to remove
     */
    private void removeGroundOverlays(HashMap<KmlGroundOverlay, GroundOverlay> groundOverlays) {
        for (GroundOverlay groundOverlay : groundOverlays.values()) {
            groundOverlay.remove();
        }
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
        mGroundOverlays = getGroundOverlayMap();
        mContainers = getContainerList();
        putStyles();
        assignStyleMap(getStyleMaps(), getStylesRenderer());
        addGroundOverlays(mGroundOverlays, mContainers);
        addContainerGroupToMap(mContainers, true);
        addPlacemarksToMap(getAllFeatures());
        if (!mGroundOverlayImagesDownloaded) {
            downloadGroundOverlays();
        }
        if (!mMarkerIconsDownloaded) {
            downloadMarkerIcons();
        }
    }

    /*package*/ void storeKmlData(HashMap<String, KmlStyle> styles,
                             HashMap<String, String> styleMaps,
                             HashMap<KmlPlacemark, Object> features, ArrayList<KmlContainer> folders,
                             HashMap<KmlGroundOverlay, GroundOverlay> groundOverlays) {
        storeData(styles, styleMaps, features, folders, groundOverlays);
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
        return (getFeatures()) ;
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
        return mGroundOverlays.keySet();
    }

    /**
     * Removes all the KML data from the map and clears all the stored placemarks
     */
    public void removeLayerFromMap() {
        removePlacemarks(getAllFeatures());
        removeGroundOverlays(mGroundOverlays);
        if (hasNestedContainers()) {
            removeContainers(getNestedContainers());
        }
        setLayerVisibility(false);
        clearStylesRenderer();
    }

    /**
     * Iterates over the placemarks, gets its style or assigns a default one and adds it to the map
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
     *
     * @param iconUrl icon url of icon to add to markers
     * @param placemarks
     */
    private void addIconToMarkers(String iconUrl, HashMap<KmlPlacemark, Object> placemarks) {
        for (Feature placemark : placemarks.keySet()) {
            KmlStyle urlStyle = getStylesRenderer().get(placemark.getId());
            KmlStyle inlineStyle = ((KmlPlacemark) placemark).getInlineStyle();
            if ("Point".equals(placemark.getGeometry().getGeometryType())) {
                boolean isInlineStyleIcon = inlineStyle != null && iconUrl
                        .equals(inlineStyle.getIconUrl());
                boolean isPlacemarkStyleIcon = urlStyle != null && iconUrl
                        .equals(urlStyle.getIconUrl());
                if (isInlineStyleIcon) {
                    scaleBitmap(inlineStyle, placemarks, (KmlPlacemark) placemark);
                } else if (isPlacemarkStyleIcon) {
                    scaleBitmap(urlStyle, placemarks, (KmlPlacemark) placemark);
                }
            }
        }
    }

    /**
     * Enlarges or shrinks a bitmap image based on the scale provided
     * @param style     Style to retrieve iconUrl and scale from
     * @param placemarks
     * @param placemark Placemark object to set the image to
     */
    private void scaleBitmap(KmlStyle style, HashMap<KmlPlacemark, Object> placemarks,
                             KmlPlacemark placemark) {
        double bitmapScale = style.getIconScale();
        String bitmapUrl = style.getIconUrl();
        Bitmap bitmapImage = getImagesCache().get(bitmapUrl);
        BitmapDescriptor scaledBitmap = scaleIcon(bitmapImage, bitmapScale);
        ((Marker) placemarks.get(placemark)).setIcon(scaledBitmap);
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
                if (getImagesCache().get(groundOverlayUrl) != null) {
                    addGroundOverlayToMap(groundOverlayUrl, mGroundOverlays, true);
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
            HashMap<KmlGroundOverlay, GroundOverlay> groundOverlays, boolean containerVisibility) {
        BitmapDescriptor groundOverlayBitmap = BitmapDescriptorFactory
                .fromBitmap(getImagesCache().get(groundOverlayUrl));
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
                putImagesCache(mIconUrl, bitmap);
                if (isLayerOnMap()) {
                    addIconToMarkers(mIconUrl, (HashMap<KmlPlacemark, Object>) getAllFeatures());
                    addContainerGroupIconsToMarkers(mIconUrl, mContainers);
                }
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
                putImagesCache(mGroundOverlayUrl, bitmap);
                if (isLayerOnMap()) {
                    addGroundOverlayToMap(mGroundOverlayUrl, mGroundOverlays, true);
                    addGroundOverlayInContainerGroups(mGroundOverlayUrl, mContainers, true);
                }
            }
        }
    }

    /**
     * @param url internet address of the image.
     * @return the bitmap of that image, scaled according to screen density.
     */
    private Bitmap getBitmapFromUrl(String url) throws MalformedURLException, IOException {
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
