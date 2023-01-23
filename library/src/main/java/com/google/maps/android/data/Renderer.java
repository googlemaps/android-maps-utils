/*
 * Copyright 2023 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.maps.android.data;

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
import com.google.maps.android.R;
import com.google.maps.android.collections.GroundOverlayManager;
import com.google.maps.android.collections.MarkerManager;
import com.google.maps.android.collections.PolygonManager;
import com.google.maps.android.collections.PolylineManager;
import com.google.maps.android.data.geojson.BiMultiMap;
import com.google.maps.android.data.geojson.GeoJsonFeature;
import com.google.maps.android.data.geojson.GeoJsonGeometryCollection;
import com.google.maps.android.data.geojson.GeoJsonLineString;
import com.google.maps.android.data.geojson.GeoJsonLineStringStyle;
import com.google.maps.android.data.geojson.GeoJsonMultiLineString;
import com.google.maps.android.data.geojson.GeoJsonMultiPoint;
import com.google.maps.android.data.geojson.GeoJsonMultiPolygon;
import com.google.maps.android.data.geojson.GeoJsonPoint;
import com.google.maps.android.data.geojson.GeoJsonPointStyle;
import com.google.maps.android.data.geojson.GeoJsonPolygon;
import com.google.maps.android.data.geojson.GeoJsonPolygonStyle;
import com.google.maps.android.data.kml.KmlContainer;
import com.google.maps.android.data.kml.KmlGroundOverlay;
import com.google.maps.android.data.kml.KmlMultiGeometry;
import com.google.maps.android.data.kml.KmlPlacemark;
import com.google.maps.android.data.kml.KmlPoint;
import com.google.maps.android.data.kml.KmlStyle;
import com.google.maps.android.data.kml.KmlUtil;

import android.content.Context;
import android.graphics.Bitmap;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * An abstraction that shares the common properties of
 * {@link com.google.maps.android.data.kml.KmlRenderer KmlRenderer} and
 * {@link com.google.maps.android.data.geojson.GeoJsonRenderer GeoJsonRenderer}
 */
public class Renderer {

    private static final int MARKER_ICON_SIZE = 32;

    private static final Object FEATURE_NOT_ON_MAP = null;

    private static final DecimalFormat sScaleFormat = new DecimalFormat("#.####");

    private GoogleMap mMap;

    private final BiMultiMap<Feature> mFeatures = new BiMultiMap<>();

    private HashMap<String, KmlStyle> mStyles;

    private HashMap<String, KmlStyle> mStylesRenderer;

    private HashMap<String, String> mStyleMaps;

    private final BiMultiMap<Feature> mContainerFeatures;

    private HashMap<KmlGroundOverlay, GroundOverlay> mGroundOverlayMap;

    private final Set<String> mMarkerIconUrls;

    private ImagesCache mImagesCache;

    private int mNumActiveDownloads = 0;

    private boolean mLayerOnMap;

    private Context mContext;

    private ArrayList<KmlContainer> mContainers;

    private final GeoJsonPointStyle mDefaultPointStyle;

    private final GeoJsonLineStringStyle mDefaultLineStringStyle;

    private final GeoJsonPolygonStyle mDefaultPolygonStyle;

    private final MarkerManager.Collection mMarkers;
    private final PolygonManager.Collection mPolygons;
    private final PolylineManager.Collection mPolylines;
    private final GroundOverlayManager.Collection mGroundOverlays;

    /**
     * Creates a new Renderer object for KML features
     *
     * @param map     map to place objects on
     * @param context the Context
     * @param markerManager marker manager to create marker collection from
     * @param polygonManager polygon manager to create polygon collection from
     * @param polylineManager polyline manager to create polyline collection from
     * @param groundOverlayManager ground overlay manager to create ground overlay collection from
     * @param imagesCache an optional ImagesCache to be used for caching images fetched
     */
    public Renderer(GoogleMap map,
                    Context context,
                    MarkerManager markerManager,
                    PolygonManager polygonManager,
                    PolylineManager polylineManager,
                    GroundOverlayManager groundOverlayManager,
                    @Nullable ImagesCache imagesCache) {
        this(map, new HashSet<String>(), null, null, null, new BiMultiMap<Feature>(), markerManager, polygonManager, polylineManager, groundOverlayManager);
        mContext = context;
        mStylesRenderer = new HashMap<>();
        mImagesCache = (imagesCache == null) ? new ImagesCache() : imagesCache;
    }

    /**
     * Creates a new Renderer object for GeoJSON features
     *
     * @param map      map to place objects on
     * @param features contains a hashmap of features and objects that will go on the map
     * @param markerManager marker manager to create marker collection from
     * @param polygonManager polygon manager to create polygon collection from
     * @param polylineManager polyline manager to create polyline collection from
     * @param groundOverlayManager ground overlay manager to create ground overlay collection from
     */
    public Renderer(GoogleMap map, HashMap<? extends Feature, Object> features, MarkerManager markerManager, PolygonManager polygonManager, PolylineManager polylineManager, GroundOverlayManager groundOverlayManager) {
        this(map, null, new GeoJsonPointStyle(), new GeoJsonLineStringStyle(), new GeoJsonPolygonStyle(), null, markerManager, polygonManager, polylineManager, groundOverlayManager);
        mFeatures.putAll(features);
        mImagesCache = null;
    }

    private Renderer(GoogleMap map,
                     Set<String> markerIconUrls,
                     GeoJsonPointStyle defaultPointStyle,
                     GeoJsonLineStringStyle defaultLineStringStyle,
                     GeoJsonPolygonStyle defaultPolygonStyle,
                     BiMultiMap<Feature> containerFeatures,
                     MarkerManager markerManager,
                     PolygonManager polygonManager,
                     PolylineManager polylineManager,
                     GroundOverlayManager groundOverlayManager) {

        mMap = map;
        mLayerOnMap = false;
        mMarkerIconUrls = markerIconUrls;
        mDefaultPointStyle = defaultPointStyle;
        mDefaultLineStringStyle = defaultLineStringStyle;
        mDefaultPolygonStyle = defaultPolygonStyle;
        mContainerFeatures = containerFeatures;
        if (map != null) {
            if (markerManager == null) {
                markerManager = new MarkerManager(map);
            }
            mMarkers = markerManager.newCollection();
            if (polygonManager == null) {
                polygonManager = new PolygonManager(map);
            }
            mPolygons = polygonManager.newCollection();
            if (polylineManager == null) {
                polylineManager = new PolylineManager(map);
            }
            mPolylines = polylineManager.newCollection();
            if (groundOverlayManager == null) {
                groundOverlayManager = new GroundOverlayManager(map);
            }
            mGroundOverlays = groundOverlayManager.newCollection();
        } else {
            mMarkers = null;
            mPolygons = null;
            mPolylines = null;
            mGroundOverlays = null;
        }
    }

    public static final class ImagesCache {

        /**
         * Map of image URL to map of scale factor to BitmapDescriptors for point marker icons
         *
         * BitmapDescriptors are cached to avoid creating new BitmapDescriptors for each individual
         * usage of a Bitmap. Each BitmapDescriptor copies the Bitmap it's created from.
         */
        final Map<String, Map<String, BitmapDescriptor>> markerImagesCache = new HashMap<>();

        /**
         * Map of image URL to BitmapDescriptors for non-scaled ground overlay images
         */
        final Map<String, BitmapDescriptor> groundOverlayImagesCache = new HashMap<>();

        /**
         * Map of image URL to Bitmap
         *
         * Holds initial references to bitmaps so they can be scaled and BitmapDescriptors cached.
         * This cache is cleared once all icon URLs are loaded, scaled, and cached as BitmapDescriptors.
         */
        final Map<String, Bitmap> bitmapCache = new HashMap<>();
    }

    /**
     * Checks if layer has been added to map
     *
     * @return true if layer is on map, false otherwise
     */
    public boolean isLayerOnMap() {
        return mLayerOnMap;
    }

    /**
     * Sets the visibility of the layer
     *
     * @param layerOnMap contains true if the layer should be set to visible and false otherwise
     */
    protected void setLayerVisibility(boolean layerOnMap) {
        mLayerOnMap = layerOnMap;
    }

    /**
     * Gets the GoogleMap that Feature objects are being placed on
     *
     * @return GoogleMap
     */
    public GoogleMap getMap() {
        return mMap;
    }

    /**
     * Sets the map that objects are being placed on
     *
     * @param map map to place all objects on
     */
    public void setMap(GoogleMap map) {
        mMap = map;
    }

    protected void putContainerFeature(Object mapObject, Feature placemark) {
        mContainerFeatures.put(placemark, mapObject);
    }

    /**
     * Gets a set containing Features
     *
     * @return set containing Features
     */
    public Set<Feature> getFeatures() {
        return mFeatures.keySet();
    }

    /**
     * Gets a Feature for the given map object, which is a Marker, Polyline or Polygon.
     *
     * @param mapObject Marker, Polyline or Polygon
     * @return Feature for the given map object
     */
    Feature getFeature(Object mapObject) {
        return mFeatures.getKey(mapObject);
    }

    Feature getContainerFeature(Object mapObject) {
        if (mContainerFeatures != null) {
            return mContainerFeatures.getKey(mapObject);
        }
        return null;
    }

    /**
     * getValues is called to retrieve the values stored in the mFeatures
     * hashmap.
     *
     * @return mFeatures.values()   collection of values stored in mFeatures
     */
    public Collection<Object> getValues() {
        return mFeatures.values();
    }

    /**
     * Gets a hashmap of all the features and objects that are on this layer
     *
     * @return mFeatures hashmap
     */
    protected HashMap<? extends Feature, Object> getAllFeatures() {
        return mFeatures;
    }

    /**
     * Gets the URLs stored for the Marker icons
     *
     * @return mMarkerIconUrls Set of URLs
     */
    protected Set<String> getMarkerIconUrls() {
        return mMarkerIconUrls;
    }

    /**
     * Gets the styles for KML placemarks
     *
     * @return mStylesRenderer hashmap containing styles for KML placemarks (String, KmlStyle)
     */
    protected HashMap<String, KmlStyle> getStylesRenderer() {
        return mStylesRenderer;
    }

    /**
     * Gets the styles for KML placemarks
     *
     * @return mStyleMaps hashmap containing styles for KML placemarks (String, String)
     */
    protected HashMap<String, String> getStyleMaps() {
        return mStyleMaps;
    }

    /**
     * Gets a cached image at the specified scale which is needed for Marker icon images.
     * If a BitmapDescriptor doesn't exist in the cache, the Bitmap for the URL from the
     * bitmap cache is scaled and cached as a BitmapDescriptor.
     *
     * @param url URL to get cached image for
     * @param scale scale to get image at
     * @return scaled BitmapDescriptor
     */
    protected BitmapDescriptor getCachedMarkerImage(String url, double scale) {
        String scaleString = sScaleFormat.format(scale);
        Map<String, BitmapDescriptor> bitmaps = mImagesCache.markerImagesCache.get(url);
        BitmapDescriptor bitmapDescriptor = null;
        if (bitmaps != null) {
            bitmapDescriptor = bitmaps.get(scaleString);
        }
        if (bitmapDescriptor == null) {
            Bitmap bitmap = mImagesCache.bitmapCache.get(url);
            if (bitmap != null) {
                bitmapDescriptor = scaleIcon(bitmap, scale);
                putMarkerImagesCache(url, scaleString, bitmapDescriptor);
            }
        }
        return bitmapDescriptor;
    }

    /**
     * Scales a bitmap by a specified float, taking into account the display density such
     * that the bitmap is scaled for a standard sized KML point marker.
     *
     * @param unscaledBitmap Unscaled bitmap image to scale.
     * @param scale          Scale value. A "1.0" scale value corresponds to the original size of the Bitmap
     * @return A scaled bitmap image
     */
    private BitmapDescriptor scaleIcon(Bitmap unscaledBitmap, double scale) {
        float density = mContext.getResources().getDisplayMetrics().density;
        int minSize = (int) (MARKER_ICON_SIZE * density * scale);

        int unscaledWidth = unscaledBitmap.getWidth();
        int unscaledHeight = unscaledBitmap.getHeight();

        int width;
        int height;
        if (unscaledWidth < unscaledHeight) {
            width = minSize;
            height = (int) ((float) (minSize * unscaledHeight) / (float) unscaledWidth);
        } else if (unscaledWidth > unscaledHeight) {
            width = (int) ((float) (minSize * unscaledWidth) / (float) unscaledHeight);
            height = minSize;
        } else {
            width = minSize;
            height = minSize;
        }

        Bitmap scaledBitmap = Bitmap.createScaledBitmap(unscaledBitmap, width, height, false);
        return BitmapDescriptorFactory.fromBitmap(scaledBitmap);
    }

    /**
     * Gets a cached image needed for GroundOverlays images
     *
     * @param url URL to get cached image for
     * @return BitmapDescriptor
     */
    protected BitmapDescriptor getCachedGroundOverlayImage(String url) {
        BitmapDescriptor bitmapDescriptor = mImagesCache.groundOverlayImagesCache.get(url);
        if (bitmapDescriptor == null) {
            Bitmap bitmap = mImagesCache.bitmapCache.get(url);
            if (bitmap != null) {
                bitmapDescriptor = BitmapDescriptorFactory.fromBitmap(bitmap);
                mImagesCache.groundOverlayImagesCache.put(url, bitmapDescriptor);
            }
        }
        return bitmapDescriptor;
    }

    /**
     * Gets the ground overlays on the current layer
     *
     * @return mGroundOverlayMap hashmap contains the ground overlays
     */
    public HashMap<KmlGroundOverlay, GroundOverlay> getGroundOverlayMap() {
        return mGroundOverlayMap;
    }

    /**
     * Gets the list of KmlContainers that are on the current layer
     *
     * @return mContainers list of KmlContainers
     */
    protected ArrayList<KmlContainer> getContainerList() {
        return mContainers;
    }

    /**
     * Obtains the styleUrl from a placemark and finds the corresponding style in a list
     *
     * @param styleId StyleUrl from a placemark
     * @return Style which corresponds to an ID
     */
    protected KmlStyle getPlacemarkStyle(String styleId) {
        KmlStyle style = mStylesRenderer.get(null);
        if (mStylesRenderer.get(styleId) != null) {
            style = mStylesRenderer.get(styleId);
        }
        return style;
    }

    /**
     * Gets the default style used to render GeoJsonPoints
     *
     * @return default style used to render GeoJsonPoints
     */
    GeoJsonPointStyle getDefaultPointStyle() {
        return mDefaultPointStyle;
    }

    /**
     * Gets the default style used to render GeoJsonLineStrings
     *
     * @return default style used to render GeoJsonLineStrings
     */
    GeoJsonLineStringStyle getDefaultLineStringStyle() {
        return mDefaultLineStringStyle;
    }

    /**
     * Gets the default style used to render GeoJsonPolygons
     *
     * @return default style used to render GeoJsonPolygons
     */
    GeoJsonPolygonStyle getDefaultPolygonStyle() {
        return mDefaultPolygonStyle;
    }

    /**
     * Adds a new mapping to the mFeatures hashmap
     *
     * @param feature Feature to be added onto the map
     * @param object  Corresponding map object to this feature
     */
    protected void putFeatures(Feature feature, Object object) {
        mFeatures.put(feature, object);
    }

    /**
     * Adds mStyles to the mStylesRenderer
     */
    protected void putStyles() {
        mStylesRenderer.putAll(mStyles);
    }

    /**
     * Stores new mappings into the mStylesRenderer hashmap
     *
     * @param styles hashmap of strings and KmlStyles to be added to mStylesRenderer
     */
    protected void putStyles(HashMap<String, KmlStyle> styles) {
        mStylesRenderer.putAll(styles);
    }

    /**
     * Cache the scaled BitmapDescriptor for the URL
     *
     * @param url              URL image was loaded from
     * @param scale            scale the image was scaled to as a formatted string for the cache
     * @param bitmapDescriptor BitmapDescriptor to cache for reuse
     */
    private void putMarkerImagesCache(String url, String scale, BitmapDescriptor bitmapDescriptor) {
        Map<String, BitmapDescriptor> bitmaps = mImagesCache.markerImagesCache.get(url);
        if (bitmaps == null) {
            bitmaps = new HashMap<>();
            mImagesCache.markerImagesCache.put(url, bitmaps);
        }
        bitmaps.put(scale, bitmapDescriptor);
    }

    /**
     * Cache loaded bitmap images
     *
     * @param url    image URL
     * @param bitmap image bitmap
     */
    protected void cacheBitmap(String url, Bitmap bitmap) {
        mImagesCache.bitmapCache.put(url, bitmap);
    }

    /**
     * Increment active download count
     */
    protected void downloadStarted() {
        mNumActiveDownloads++;
    }

    /**
     * Decrement active download count and check if bitmap cache should be cleared
     */
    protected void downloadFinished() {
        mNumActiveDownloads--;
        checkClearBitmapCache();
    }

    /**
     * Clear bitmap cache if no active image downloads remain. All images
     * should be loaded, scaled, and cached as BitmapDescriptors at this point.
     */
    protected void checkClearBitmapCache() {
        if (mNumActiveDownloads == 0 && mImagesCache != null && !mImagesCache.bitmapCache.isEmpty()) {
            mImagesCache.bitmapCache.clear();
        }
    }

    /**
     * Checks if the layer contains placemarks
     *
     * @return true if there are placemarks, false otherwise
     */
    protected boolean hasFeatures() {
        return mFeatures.size() > 0;
    }

    /**
     * Removes all given Features from the map and clears all stored features.
     *
     * @param features features to remove
     */
    protected void removeFeatures(HashMap<? extends Feature, Object> features) {
        removeFeatures(features.values());
    }

    /**
     * Removes all given Features from the map and clears all stored features.
     *
     * @param features features to remove
     */
    private void removeFeatures(Collection features) {
        // Remove map object from the map
        for (Object mapObject : features) {
            if (mapObject instanceof Collection) {
                removeFeatures((Collection) mapObject);
            } else if (mapObject instanceof Marker) {
                mMarkers.remove((Marker) mapObject);
            } else if (mapObject instanceof Polyline) {
                mPolylines.remove((Polyline) mapObject);
            } else if (mapObject instanceof Polygon) {
                mPolygons.remove((Polygon) mapObject);
            }
        }
    }

    /**
     * Removes all ground overlays in the given hashmap
     *
     * @param groundOverlays hashmap of ground overlays to remove
     */
    protected void removeGroundOverlays(HashMap<KmlGroundOverlay, GroundOverlay> groundOverlays) {
        for (GroundOverlay groundOverlay : groundOverlays.values()) {
            // Ground overlay values may be null if their image was not yet downloaded
            if (groundOverlay != null) {
                mGroundOverlays.remove(groundOverlay);
            }
        }
    }

    /**
     * Removes a Feature from the map if its geometry property is not null
     *
     * @param feature feature to remove from map
     */
    protected void removeFeature(Feature feature) {
        // Check if given feature is stored
        if (mFeatures.containsKey(feature)) {
            removeFromMap(mFeatures.remove(feature));
        }
    }

    /**
     * Checks for each style in the feature and adds a default style if none is applied
     *
     * @param feature feature to apply default styles to
     */
    private void setFeatureDefaultStyles(GeoJsonFeature feature) {
        if (feature.getPointStyle() == null) {
            feature.setPointStyle(mDefaultPointStyle);
        }
        if (feature.getLineStringStyle() == null) {
            feature.setLineStringStyle(mDefaultLineStringStyle);
        }
        if (feature.getPolygonStyle() == null) {
            feature.setPolygonStyle(mDefaultPolygonStyle);
        }
    }

    /**
     * Removes all the mappings from the mStylesRenderer hashmap
     */
    protected void clearStylesRenderer() {
        mStylesRenderer.clear();
    }

    /**
     * Stores all given data
     *
     * @param styles         hashmap of styles
     * @param styleMaps      hashmap of style maps
     * @param features       hashmap of features
     * @param folders        array of containers
     * @param groundOverlays hashmap of ground overlays
     */
    protected void storeData(HashMap<String, KmlStyle> styles,
                             HashMap<String, String> styleMaps,
                             HashMap<KmlPlacemark, Object> features,
                             ArrayList<KmlContainer> folders,
                             HashMap<KmlGroundOverlay, GroundOverlay> groundOverlays) {
        mStyles = styles;
        mStyleMaps = styleMaps;
        mFeatures.putAll(features);
        mContainers = folders;
        mGroundOverlayMap = groundOverlays;
    }

    /**
     * Adds a new Feature to the map if its geometry property is not null.
     *
     * @param feature feature to add to the map
     */
    protected void addFeature(Feature feature) {
        Object mapObject = FEATURE_NOT_ON_MAP;
        if (feature instanceof GeoJsonFeature) {
            setFeatureDefaultStyles((GeoJsonFeature) feature);
        }
        if (mLayerOnMap) {
            if (mFeatures.containsKey(feature)) {
                // Remove current map objects before adding new ones
                removeFromMap(mFeatures.get(feature));
            }
            if (feature.hasGeometry()) {
                // Create new map object
                if (feature instanceof KmlPlacemark) {
                    boolean isPlacemarkVisible = getPlacemarkVisibility(feature);
                    String placemarkId = feature.getId();
                    Geometry geometry = feature.getGeometry();
                    KmlStyle style = getPlacemarkStyle(placemarkId);
                    KmlStyle inlineStyle = ((KmlPlacemark) feature).getInlineStyle();
                    mapObject = addKmlPlacemarkToMap((KmlPlacemark) feature, geometry, style, inlineStyle, isPlacemarkVisible);
                } else {
                    mapObject = addGeoJsonFeatureToMap(feature, feature.getGeometry());
                }
            }
        }
        mFeatures.put(feature, mapObject);
    }

    /**
     * Given a Marker, Polyline, Polygon or an array of these and removes it from the map
     *
     * @param mapObject map object or array of map objects to remove from the map
     */
    protected void removeFromMap(Object mapObject) {
        if (mapObject instanceof Marker) {
            mMarkers.remove((Marker) mapObject);
        } else if (mapObject instanceof Polyline) {
            mPolylines.remove((Polyline) mapObject);
        } else if (mapObject instanceof Polygon) {
            mPolygons.remove((Polygon) mapObject);
        } else if (mapObject instanceof GroundOverlay) {
            mGroundOverlays.remove((GroundOverlay) mapObject);
        } else if (mapObject instanceof ArrayList) {
            for (Object mapObjectElement : (ArrayList) mapObject) {
                removeFromMap(mapObjectElement);
            }
        }
    }

    /**
     * Adds a new object onto the map using the Geometry for the coordinates and the
     * Feature for the styles. (used for GeoJson)
     *
     * @param feature  feature to get geometry style
     * @param geometry geometry to add to the map
     */
    protected Object addGeoJsonFeatureToMap(Feature feature, Geometry geometry) {
        String geometryType = geometry.getGeometryType();
        switch (geometryType) {
            case "Point":
                MarkerOptions markerOptions = null;
                if (feature instanceof GeoJsonFeature) {
                    markerOptions = ((GeoJsonFeature) feature).getMarkerOptions();
                } else if (feature instanceof KmlPlacemark) {
                    markerOptions = ((KmlPlacemark) feature).getMarkerOptions();
                }
                return addPointToMap(markerOptions, (GeoJsonPoint) geometry);
            case "LineString":
                PolylineOptions polylineOptions = null;
                if (feature instanceof GeoJsonFeature) {
                    polylineOptions = ((GeoJsonFeature) feature).getPolylineOptions();
                } else if (feature instanceof KmlPlacemark) {
                    polylineOptions = ((KmlPlacemark) feature).getPolylineOptions();
                }
                return addLineStringToMap(polylineOptions, (GeoJsonLineString) geometry);
            case "Polygon":
                PolygonOptions polygonOptions = null;
                if (feature instanceof GeoJsonFeature) {
                    polygonOptions = ((GeoJsonFeature) feature).getPolygonOptions();
                } else if (feature instanceof KmlPlacemark) {
                    polygonOptions = ((KmlPlacemark) feature).getPolygonOptions();
                }
                return addPolygonToMap(polygonOptions, (DataPolygon) geometry);
            case "MultiPoint":
                return addMultiPointToMap(((GeoJsonFeature) feature).getPointStyle(),
                        (GeoJsonMultiPoint) geometry);
            case "MultiLineString":
                return addMultiLineStringToMap(((GeoJsonFeature) feature).getLineStringStyle(),
                        ((GeoJsonMultiLineString) geometry));
            case "MultiPolygon":
                return addMultiPolygonToMap(((GeoJsonFeature) feature).getPolygonStyle(),
                        ((GeoJsonMultiPolygon) geometry));
            case "GeometryCollection":
                return addGeometryCollectionToMap(((GeoJsonFeature) feature),
                        ((GeoJsonGeometryCollection) geometry).getGeometries());
        }
        return null;
    }

    /**
     * Adds a single geometry object to the map with its specified style (used for KML)
     *
     * @param geometry defines the type of object to add to the map
     * @param style    defines styling properties to add to the object when added to the map
     * @return the object that was added to the map, this is a Marker, Polyline, Polygon or an array
     * of either objects
     */
    protected Object addKmlPlacemarkToMap(KmlPlacemark placemark, Geometry geometry, KmlStyle style,
                                          KmlStyle inlineStyle, boolean isVisible) {
        String geometryType = geometry.getGeometryType();
        boolean hasDrawOrder = placemark.hasProperty("drawOrder");
        float drawOrder = 0;

        if (hasDrawOrder) {
            try {
                drawOrder = Float.parseFloat(placemark.getProperty("drawOrder"));
            } catch (NumberFormatException e) {
                hasDrawOrder = false;
            }
        }
        switch (geometryType) {
            case "Point":
                MarkerOptions markerOptions = style.getMarkerOptions();
                if (inlineStyle != null) {
                    setInlinePointStyle(markerOptions, inlineStyle, style);
                } else if (style.getIconUrl() != null) {
                    // Use shared style
                    addMarkerIcons(style.getIconUrl(), style.getIconScale(), markerOptions);
                }
                Marker marker = addPointToMap(markerOptions, (KmlPoint) geometry);
                marker.setVisible(isVisible);
                setMarkerInfoWindow(style, marker, placemark);
                if (hasDrawOrder) {
                    marker.setZIndex(drawOrder);
                }
                return marker;
            case "LineString":
                PolylineOptions polylineOptions = style.getPolylineOptions();
                if (inlineStyle != null) {
                    setInlineLineStringStyle(polylineOptions, inlineStyle);
                } else if (style.isLineRandomColorMode()) {
                    polylineOptions.color(KmlStyle.computeRandomColor(polylineOptions.getColor()));
                }
                Polyline polyline = addLineStringToMap(polylineOptions, (LineString) geometry);
                polyline.setVisible(isVisible);
                if (hasDrawOrder) {
                    polyline.setZIndex(drawOrder);
                }
                return polyline;
            case "Polygon":
                PolygonOptions polygonOptions = style.getPolygonOptions();
                if (inlineStyle != null) {
                    setInlinePolygonStyle(polygonOptions, inlineStyle);
                } else if (style.isPolyRandomColorMode()) {
                    polygonOptions.fillColor(KmlStyle.computeRandomColor(polygonOptions.getFillColor()));
                }
                Polygon polygon = addPolygonToMap(polygonOptions, (DataPolygon) geometry);
                polygon.setVisible(isVisible);
                if (hasDrawOrder) {
                    polygon.setZIndex(drawOrder);
                }
                return polygon;
            case "MultiGeometry":
                return addMultiGeometryToMap(placemark, (KmlMultiGeometry) geometry, style, inlineStyle,
                        isVisible);
        }
        return null;
    }

    /**
     * Adds a Point to the map as a Marker
     *
     * @param markerOptions contains relevant styling properties for the Marker
     * @param point         contains coordinates for the Marker
     * @return Marker object created from the given Point
     */
    private Marker addPointToMap(MarkerOptions markerOptions, Point point) {
        markerOptions.position(point.getGeometryObject());
        return mMarkers.addMarker(markerOptions);
    }

    /**
     * Sets the inline point style by copying over the styles that have been set
     *
     * @param markerOptions marker options object to add inline styles to
     * @param inlineStyle   inline styles to apply
     * @param defaultStyle  default shared style
     */
    private void setInlinePointStyle(MarkerOptions markerOptions, KmlStyle inlineStyle,
                                     KmlStyle defaultStyle) {
        MarkerOptions inlineMarkerOptions = inlineStyle.getMarkerOptions();
        if (inlineStyle.isStyleSet("heading")) {
            markerOptions.rotation(inlineMarkerOptions.getRotation());
        }
        if (inlineStyle.isStyleSet("hotSpot")) {
            markerOptions
                    .anchor(inlineMarkerOptions.getAnchorU(), inlineMarkerOptions.getAnchorV());
        }
        if (inlineStyle.isStyleSet("markerColor")) {
            markerOptions.icon(inlineMarkerOptions.getIcon());
        }
        double scale;
        if (inlineStyle.isStyleSet("iconScale")) {
            scale = inlineStyle.getIconScale();
        } else if (defaultStyle.isStyleSet("iconScale")) {
            scale = defaultStyle.getIconScale();
        } else {
            scale = 1.0;
        }
        if (inlineStyle.isStyleSet("iconUrl")) {
            addMarkerIcons(inlineStyle.getIconUrl(), scale, markerOptions);
        } else if (defaultStyle.getIconUrl() != null) {
            // Inline style with no icon defined
            addMarkerIcons(defaultStyle.getIconUrl(), scale, markerOptions);
        }
    }

    /**
     * Adds a LineString to the map as a Polyline
     *
     * @param polylineOptions contains relevant styling properties for the Polyline
     * @param lineString      contains coordinates for the Polyline
     * @return Polyline object created from given LineString
     */
    private Polyline addLineStringToMap(PolylineOptions polylineOptions,
                                        LineString lineString) {
        // Add coordinates
        polylineOptions.addAll(lineString.getGeometryObject());
        Polyline addedPolyline = mPolylines.addPolyline(polylineOptions);
        addedPolyline.setClickable(polylineOptions.isClickable());
        return addedPolyline;
    }

    /**
     * Sets the inline linestring style by copying over the styles that have been set
     *
     * @param polylineOptions polygon options object to add inline styles to
     * @param inlineStyle     inline styles to apply
     */
    private void setInlineLineStringStyle(PolylineOptions polylineOptions, KmlStyle inlineStyle) {
        PolylineOptions inlinePolylineOptions = inlineStyle.getPolylineOptions();
        if (inlineStyle.isStyleSet("outlineColor")) {
            polylineOptions.color(inlinePolylineOptions.getColor());
        }
        if (inlineStyle.isStyleSet("width")) {
            polylineOptions.width(inlinePolylineOptions.getWidth());
        }
        if (inlineStyle.isLineRandomColorMode()) {
            polylineOptions.color(KmlStyle.computeRandomColor(inlinePolylineOptions.getColor()));
        }
    }

    /**
     * Adds a DataPolygon to the map as a Polygon
     *
     * @param polygonOptions
     * @param polygon        contains coordinates for the Polygon
     * @return Polygon object created from given DataPolygon
     */
    private Polygon addPolygonToMap(PolygonOptions polygonOptions, DataPolygon polygon) {
        // First array of coordinates are the outline
        polygonOptions.addAll(polygon.getOuterBoundaryCoordinates());
        // Following arrays are holes
        List<List<LatLng>> innerBoundaries = polygon.getInnerBoundaryCoordinates();
        for (List<LatLng> innerBoundary : innerBoundaries) {
            polygonOptions.addHole(innerBoundary);
        }
        Polygon addedPolygon = mPolygons.addPolygon(polygonOptions);
        addedPolygon.setClickable(polygonOptions.isClickable());
        return addedPolygon;
    }

    /**
     * Sets the inline polygon style by copying over the styles that have been set
     *
     * @param polygonOptions polygon options object to add inline styles to
     * @param inlineStyle    inline styles to apply
     */
    private void setInlinePolygonStyle(PolygonOptions polygonOptions, KmlStyle inlineStyle) {
        PolygonOptions inlinePolygonOptions = inlineStyle.getPolygonOptions();
        if (inlineStyle.hasFill() && inlineStyle.isStyleSet("fillColor")) {
            polygonOptions.fillColor(inlinePolygonOptions.getFillColor());
        }
        if (inlineStyle.hasOutline()) {
            if (inlineStyle.isStyleSet("outlineColor")) {
                polygonOptions.strokeColor(inlinePolygonOptions.getStrokeColor());
            }
            if (inlineStyle.isStyleSet("width")) {
                polygonOptions.strokeWidth(inlinePolygonOptions.getStrokeWidth());
            }
        }
        if (inlineStyle.isPolyRandomColorMode()) {
            polygonOptions.fillColor(KmlStyle.computeRandomColor(inlinePolygonOptions.getFillColor()));
        }
    }

    /**
     * Adds all Geometry objects stored in the GeoJsonGeometryCollection onto the map.
     * Supports recursive GeometryCollections.
     *
     * @param feature           contains relevant styling properties for the Geometry inside
     *                          the GeoJsonGeometryCollection
     * @param geoJsonGeometries contains an array of Geometry objects
     * @return array of Marker, Polyline, Polygons that have been added to the map
     */
    private ArrayList<Object> addGeometryCollectionToMap(GeoJsonFeature feature,
                                                         List<Geometry> geoJsonGeometries) {
        ArrayList<Object> geometries = new ArrayList<>();
        for (Geometry geometry : geoJsonGeometries) {
            geometries.add(addGeoJsonFeatureToMap(feature, geometry));
        }
        return geometries;
    }

    /**
     * Gets the visibility of the placemark if it is specified. A visibility value of "1"
     * corresponds as "true", a visibility value of "0" corresponds as false. If the
     * visibility is not set, the method returns "true".
     *
     * @param feature Feature to obtain visibility from.
     * @return False if a Feature has a visibility value of "1", true otherwise.
     */
    protected static boolean getPlacemarkVisibility(Feature feature) {
        boolean isFeatureVisible = true;
        if (feature.hasProperty("visibility")) {
            String placemarkVisibility = feature.getProperty("visibility");
            if (Integer.parseInt(placemarkVisibility) == 0) {
                isFeatureVisible = false;
            }
        }
        return isFeatureVisible;
    }

    /**
     * Iterates through a list of styles and assigns a style
     *
     * @param styleMap
     * @param styles
     */
    public void assignStyleMap(HashMap<String, String> styleMap,
                               HashMap<String, KmlStyle> styles) {
        for (String styleMapKey : styleMap.keySet()) {
            String styleMapValue = styleMap.get(styleMapKey);
            if (styles.containsKey(styleMapValue)) {
                styles.put(styleMapKey, styles.get(styleMapValue));
            }
        }
    }

    /**
     * Adds all the geometries within a KML MultiGeometry to the map. Supports recursive
     * MultiGeometry. Combines styling of the placemark with the coordinates of each geometry.
     *
     * @param multiGeometry contains array of geometries for the MultiGeometry
     * @param urlStyle      contains relevant styling properties for the MultiGeometry
     * @return array of Marker, Polyline and Polygon objects
     */
    private ArrayList<Object> addMultiGeometryToMap(KmlPlacemark placemark,
                                                    KmlMultiGeometry multiGeometry, KmlStyle urlStyle, KmlStyle inlineStyle,
                                                    boolean isContainerVisible) {
        ArrayList<Object> mapObjects = new ArrayList<>();
        ArrayList<Geometry> kmlObjects = multiGeometry.getGeometryObject();
        for (Geometry kmlGeometry : kmlObjects) {
            mapObjects.add(addKmlPlacemarkToMap(placemark, kmlGeometry, urlStyle, inlineStyle,
                    isContainerVisible));
        }
        return mapObjects;
    }

    /**
     * Adds all GeoJsonPoint objects in GeoJsonMultiPoint to the map as multiple Markers
     *
     * @param pointStyle contains relevant styling properties for the Markers
     * @param multiPoint contains an array of GeoJsonPoints
     * @return array of Markers that have been added to the map
     */
    private ArrayList<Marker> addMultiPointToMap(GeoJsonPointStyle pointStyle,
                                                 GeoJsonMultiPoint multiPoint) {
        ArrayList<Marker> markers = new ArrayList<>();
        for (GeoJsonPoint geoJsonPoint : multiPoint.getPoints()) {
            markers.add(addPointToMap(pointStyle.toMarkerOptions(), geoJsonPoint));
        }
        return markers;
    }

    /**
     * Adds all GeoJsonLineString objects in the GeoJsonMultiLineString to the map as multiple
     * Polylines
     *
     * @param lineStringStyle contains relevant styling properties for the Polylines
     * @param multiLineString contains an array of GeoJsonLineStrings
     * @return array of Polylines that have been added to the map
     */
    private ArrayList<Polyline> addMultiLineStringToMap(GeoJsonLineStringStyle lineStringStyle,
                                                        GeoJsonMultiLineString multiLineString) {
        ArrayList<Polyline> polylines = new ArrayList<>();
        for (GeoJsonLineString geoJsonLineString : multiLineString.getLineStrings()) {
            polylines.add(addLineStringToMap(lineStringStyle.toPolylineOptions(), geoJsonLineString));
        }
        return polylines;
    }

    /**
     * Adds all GeoJsonPolygon in the GeoJsonMultiPolygon to the map as multiple Polygons
     *
     * @param polygonStyle contains relevant styling properties for the Polygons
     * @param multiPolygon contains an array of GeoJsonPolygons
     * @return array of Polygons that have been added to the map
     */
    private ArrayList<Polygon> addMultiPolygonToMap(GeoJsonPolygonStyle polygonStyle,
                                                    GeoJsonMultiPolygon multiPolygon) {
        ArrayList<Polygon> polygons = new ArrayList<>();
        for (GeoJsonPolygon geoJsonPolygon : multiPolygon.getPolygons()) {
            polygons.add(addPolygonToMap(polygonStyle.toPolygonOptions(), geoJsonPolygon));
        }
        return polygons;
    }

    /**
     * Sets the marker icon if there is a cached image for the URL,
     * otherwise adds the URL to set to download images
     *
     * @param styleUrl      the icon url from
     * @param scale         the icon scale
     * @param markerOptions marker options to set icon on
     */
    private void addMarkerIcons(String styleUrl, double scale, MarkerOptions markerOptions) {
        // BitmapDescriptor stored in cache
        BitmapDescriptor bitmap = getCachedMarkerImage(styleUrl, scale);
        if (bitmap != null) {
            markerOptions.icon(bitmap);
        } else {
            mMarkerIconUrls.add(styleUrl);
        }
    }

    /**
     * Adds a ground overlay to the map
     *
     * @param groundOverlayOptions GroundOverlay style options to be added to the map
     * @return new GroundOverlay object created from the given GroundOverlayOptions
     */
    protected GroundOverlay attachGroundOverlay(GroundOverlayOptions groundOverlayOptions) {
        return mGroundOverlays.addGroundOverlay(groundOverlayOptions);
    }

    /**
     * Sets a marker info window if no <text> tag was found in the KML document. This method sets
     * the marker title as the text found in the <name> start tag and the snippet as <description>
     *
     * @param style Style to apply
     */
    private void setMarkerInfoWindow(KmlStyle style, Marker marker,
                                     final KmlPlacemark placemark) {
        boolean hasName = placemark.hasProperty("name");
        boolean hasDescription = placemark.hasProperty("description");
        boolean hasBalloonOptions = style.hasBalloonStyle();
        boolean hasBalloonText = style.getBalloonOptions().containsKey("text");
        if (hasBalloonOptions && hasBalloonText) {
            marker.setTitle(KmlUtil.substituteProperties(style.getBalloonOptions().get("text"), placemark));
            createInfoWindow();
        } else if (hasBalloonOptions && hasName) {
            marker.setTitle(placemark.getProperty("name"));
            createInfoWindow();
        } else if (hasName && hasDescription) {
            marker.setTitle(placemark.getProperty("name"));
            marker.setSnippet(placemark.getProperty("description"));
            createInfoWindow();
        } else if (hasDescription) {
            marker.setTitle(placemark.getProperty("description"));
            createInfoWindow();
        } else if (hasName) {
            marker.setTitle(placemark.getProperty("name"));
            createInfoWindow();
        }
    }

    /**
     * Creates a new InfoWindowAdapter and sets text if marker snippet or title is set. This allows
     * the info window to have custom HTML.
     */
    private void createInfoWindow() {
        mMarkers.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {

            public View getInfoWindow(@NonNull Marker arg0) {
                return null;
            }

            public View getInfoContents(@NonNull Marker arg0) {
                View view = LayoutInflater.from(mContext).inflate(R.layout.amu_info_window, null);
                TextView infoWindowText = view.findViewById(R.id.window);
                if (arg0.getSnippet() != null) {
                    infoWindowText.setText(Html.fromHtml(arg0.getTitle() + "<br>" + arg0.getSnippet()));
                } else {
                    infoWindowText.setText(Html.fromHtml(arg0.getTitle()));
                }
                return view;
            }
        });
    }

    /**
     * Sets a single click listener for each of the map object collections, that will be called
     * with the corresponding Feature object when an object on the map (Polygon, Marker, Polyline)
     * from one of this Renderer's collections is clicked.
     *
     * If getFeature() returns null this means that either the object is inside a KMLContainer,
     * or the object is a MultiPolygon, MultiLineString or MultiPoint and must
     * be handled differently.
     *
     * @param listener Listener providing the onFeatureClick method to call.
     */
    void setOnFeatureClickListener(final Layer.OnFeatureClickListener listener) {

        mPolygons.setOnPolygonClickListener(polygon -> {
            if (getFeature(polygon) != null) {
                listener.onFeatureClick(getFeature(polygon));
            } else if (getContainerFeature(polygon) != null) {
                listener.onFeatureClick(getContainerFeature(polygon));
            } else {
                listener.onFeatureClick(getFeature(multiObjectHandler(polygon)));
            }
        });

        mMarkers.setOnMarkerClickListener(marker -> {
            if (getFeature(marker) != null) {
                listener.onFeatureClick(getFeature(marker));
            }  else if (getContainerFeature(marker) != null) {
                listener.onFeatureClick(getContainerFeature(marker));
            } else {
                listener.onFeatureClick(getFeature(multiObjectHandler(marker)));
            }
            return false;
        });

        mPolylines.setOnPolylineClickListener(polyline -> {
            if (getFeature(polyline) != null) {
                listener.onFeatureClick(getFeature(polyline));
            } else if (getContainerFeature(polyline) != null) {
                listener.onFeatureClick(getContainerFeature(polyline));
            }  else {
                listener.onFeatureClick(getFeature(multiObjectHandler(polyline)));
            }
        });
    }

    /**
     * Called if the map object is a MultiPolygon, MultiLineString or a MultiPoint and returns
     * the corresponding ArrayList containing the singular Polygons, LineStrings or Points
     * respectively.
     *
     * @param mapObject Object
     * @return an ArrayList of the individual
     */
    private ArrayList<?> multiObjectHandler(Object mapObject) {
        for (Object value : getValues()) {
            Class c = value.getClass();
            if (c.getSimpleName().equals("ArrayList")) {
                ArrayList<?> mapObjects = (ArrayList<?>) value;
                if (mapObjects.contains(mapObject)) {
                    return mapObjects;
                }
            }
        }
        return null;
    }
}
