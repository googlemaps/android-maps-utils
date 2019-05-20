/*
 * Copyright 2017 Google Inc.
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

import android.content.Context;
import android.graphics.Bitmap;
import android.support.v4.util.LruCache;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap;
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

/**
 * An abstraction that shares the common properties of
 * {@link com.google.maps.android.data.kml.KmlRenderer KmlRenderer} and
 * {@link com.google.maps.android.data.geojson.GeoJsonRenderer GeoJsonRenderer}
 */
public class Renderer {

    private final static Object FEATURE_NOT_ON_MAP = null;

    private static final int LRU_CACHE_SIZE = 50;

    private GoogleMap mMap;

    private final BiMultiMap<Feature> mFeatures = new BiMultiMap<>();

    private HashMap<String, KmlStyle> mStyles;

    private HashMap<String, KmlStyle> mStylesRenderer;

    private HashMap<String, String> mStyleMaps;

    private BiMultiMap<Feature> mContainerFeatures;

    private HashMap<KmlGroundOverlay, GroundOverlay> mGroundOverlays;

    private final ArrayList<String> mMarkerIconUrls;

    private final LruCache<String, Bitmap> mImagesCache;

    private boolean mLayerOnMap;

    private Context mContext;

    private ArrayList<KmlContainer> mContainers;

    private final GeoJsonPointStyle mDefaultPointStyle;

    private final GeoJsonLineStringStyle mDefaultLineStringStyle;

    private final GeoJsonPolygonStyle mDefaultPolygonStyle;

    /**
     * Creates a new Renderer object
     *
     * @param map map to place objects on
     * @param context context needed to add info windows
     */
    public Renderer(GoogleMap map, Context context) {
        mMap = map;
        mContext = context;
        mLayerOnMap = false;
        mImagesCache = new LruCache<>(LRU_CACHE_SIZE);
        mMarkerIconUrls = new ArrayList<>();
        mStylesRenderer = new HashMap<>();
        mDefaultPointStyle = null;
        mDefaultLineStringStyle = null;
        mDefaultPolygonStyle = null;
        mContainerFeatures = new BiMultiMap<>();
    }

    /**
     * Creates a new Renderer object
     *
     * @param map map to place objects on
     * @param features contains a hashmap of features and objects that will go on the map
     */
    public Renderer(GoogleMap map, HashMap<? extends Feature, Object> features ) {
        mMap = map;
        mFeatures.putAll(features);
        mLayerOnMap = false;
        mMarkerIconUrls = null;
        mDefaultPointStyle = new GeoJsonPointStyle();
        mDefaultLineStringStyle = new GeoJsonLineStringStyle();
        mDefaultPolygonStyle = new GeoJsonPolygonStyle();
        mImagesCache = null;
        mContainerFeatures = null;
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
    public Feature getFeature(Object mapObject) {
        return mFeatures.getKey(mapObject);
    }

    public Feature getContainerFeature(Object mapObject) {
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
     * @return mMarkerIconUrls ArrayList of URLs
     */
    public ArrayList<String> getMarkerIconUrls()  { return mMarkerIconUrls; }

    /**
     * Gets the styles for KML placemarks
     *
     * @return mStylesRenderer hashmap containing styles for KML placemarks (String, KmlStyle)
     */
    public HashMap<String, KmlStyle> getStylesRenderer() { return mStylesRenderer; }

    /**
     * Gets the styles for KML placemarks
     *
     * @return mStyleMaps hashmap containing styles for KML placemarks (String, String)
     */
    public HashMap<String, String> getStyleMaps() { return mStyleMaps; }

    /**
     * Gets the images cache which is needed to download GroundOverlays and Marker Icon images
     *
     * @return mImagesCache
     */
    public LruCache<String, Bitmap> getImagesCache() { return mImagesCache; }

    /**
     * Gets the ground overlays on the current layer
     *
     * @return mGroundOverlays hashmap contains the ground overlays
     */
    public HashMap<KmlGroundOverlay, GroundOverlay> getGroundOverlayMap() { return mGroundOverlays; }

    /**
     * Gets the list of KmlContainers that are on the current layer
     *
     * @return mContainers list of KmlContainers
     */
    public ArrayList<KmlContainer> getContainerList() { return mContainers; }

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
    public GeoJsonPointStyle getDefaultPointStyle() {
        return mDefaultPointStyle;
    }

    /**
     * Gets the default style used to render GeoJsonLineStrings
     *
     * @return default style used to render GeoJsonLineStrings
     */
    public GeoJsonLineStringStyle getDefaultLineStringStyle() {
        return mDefaultLineStringStyle;
    }

    /**
     * Gets the default style used to render GeoJsonPolygons
     *
     * @return default style used to render GeoJsonPolygons
     */
    public GeoJsonPolygonStyle getDefaultPolygonStyle() {
        return mDefaultPolygonStyle;
    }

    /**
     * Adds a new mapping to the mFeatures hashmap
     * @param feature Feature to be added onto the map
     * @param object Corresponding map object to this feature
     */
    public void putFeatures(Feature feature, Object object) {
        mFeatures.put(feature, object);
    }

    /**
     * Adds mStyles to the mStylesRenderer
     */
    public void putStyles() {
        mStylesRenderer.putAll(mStyles);
    }

    /**
     * Stores new mappings into the mStylesRenderer hashmap
     * @param styles hashmap of strings and KmlStyles to be added to mStylesRenderer
     */
    public void putStyles(HashMap<String, KmlStyle> styles) {
        mStylesRenderer.putAll(styles);
    }

    public void putImagesCache(String groundOverlayUrl, Bitmap bitmap) {
        mImagesCache.put(groundOverlayUrl, bitmap);
    }

    /**
     * Checks if the layer contains placemarks
     *
     * @return true if there are placemarks, false otherwise
     */
    public boolean hasFeatures() {
        return mFeatures.size() > 0;
    }

    /**
     * Removes all given Features from the map and clears all stored features.
     *
     * @param features features to remove
     */
    protected static void removeFeatures(HashMap<Feature, Object> features) {
        // Remove map object from the map
        for (Object mapObject : features.values()) {
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
    public void clearStylesRenderer() {
        mStylesRenderer.clear();
    }

    /**
     * Stores all given data and adds it onto the map
     *
     * @param styles         hashmap of styles
     * @param styleMaps      hashmap of style maps
     * @param features       hashmap of features
     * @param folders        array of containers
     * @param groundOverlays hashmap of ground overlays
     */
    protected void storeData(HashMap<String, KmlStyle> styles,
                             HashMap<String, String> styleMaps,
                             HashMap<KmlPlacemark, Object> features, ArrayList<KmlContainer> folders,
                             HashMap<KmlGroundOverlay, GroundOverlay> groundOverlays) {
        mStyles = styles;
        mStyleMaps = styleMaps;
        mFeatures.putAll(features);
        mContainers = folders;
        mGroundOverlays = groundOverlays;
    }

    /**
     * Adds a new Feature to the map if its geometry property is not null.
     *
     * @param feature feature to add to the map
     */
    public void addFeature(Feature feature) {
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
    public static void removeFromMap(Object mapObject) {
        if (mapObject instanceof Marker) {
            ((Marker) mapObject).remove();
        } else if (mapObject instanceof Polyline) {
            ((Polyline) mapObject).remove();
        } else if (mapObject instanceof Polygon) {
            ((Polygon) mapObject).remove();
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
                    setInlinePointStyle(markerOptions, inlineStyle, style.getIconUrl());
                } else if (style.getIconUrl() != null) {
                    // Use shared style
                    addMarkerIcons(style.getIconUrl(), markerOptions);
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
     * @param point      contains coordinates for the Marker
     * @return Marker object created from the given Point
     */
    protected Marker addPointToMap(MarkerOptions markerOptions, Point point) {
        markerOptions.position(point.getGeometryObject());
        return mMap.addMarker(markerOptions);
    }

    /**
     * Sets the inline point style by copying over the styles that have been set
     *
     * @param markerOptions    marker options object to add inline styles to
     * @param inlineStyle      inline styles to apply
     * @param markerUrlIconUrl default marker icon URL from shared style
     */
    private void setInlinePointStyle(MarkerOptions markerOptions, KmlStyle inlineStyle,
                                     String markerUrlIconUrl) {
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
        if (inlineStyle.isStyleSet("iconUrl")) {
            addMarkerIcons(inlineStyle.getIconUrl(), markerOptions);
        } else if (markerUrlIconUrl != null) {
            // Inline style with no icon defined
            addMarkerIcons(markerUrlIconUrl, markerOptions);
        }
    }

    /**
     * Adds a LineString to the map as a Polyline
     *
     * @param polylineOptions contains relevant styling properties for the Polyline
     * @param lineString      contains coordinates for the Polyline
     * @return Polyline object created from given LineString
     */
    protected Polyline addLineStringToMap(PolylineOptions polylineOptions,
                                          LineString lineString) {
        // Add coordinates
        polylineOptions.addAll(lineString.getGeometryObject());
        Polyline addedPolyline = mMap.addPolyline(polylineOptions);
        addedPolyline.setClickable(true);
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
     * @param polygon      contains coordinates for the Polygon
     * @return Polygon object created from given DataPolygon
     */
    protected Polygon addPolygonToMap(PolygonOptions polygonOptions, DataPolygon polygon) {
        // First array of coordinates are the outline
        polygonOptions.addAll(polygon.getOuterBoundaryCoordinates());
        // Following arrays are holes
        List<List<LatLng>> innerBoundaries = polygon.getInnerBoundaryCoordinates();
        for (List<LatLng> innerBoundary : innerBoundaries) {
            polygonOptions.addHole(innerBoundary);
        }
        Polygon addedPolygon = mMap.addPolygon(polygonOptions);
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
     * @param urlStyle         contains relevant styling properties for the MultiGeometry
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
     * Adds a ground overlay to the map
     *
     * @param groundOverlayOptions GroundOverlay style options to be added to the map
     * @return new GroundOverlay object created from the given GroundOverlayOptions
     */
    public GroundOverlay attachGroundOverlay(GroundOverlayOptions groundOverlayOptions) {
        return mMap.addGroundOverlay(groundOverlayOptions);
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
            marker.setTitle(style.getBalloonOptions().get("text"));
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
        mMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {

            public View getInfoWindow(Marker arg0) {
                return null;
            }

            public View getInfoContents(Marker arg0) {
                View view =  LayoutInflater.from(mContext).inflate(R.layout.amu_info_window, null);
                TextView infoWindowText = (TextView) view.findViewById(R.id.window);
                if (arg0.getSnippet() != null) {
                    infoWindowText.setText(Html.fromHtml(arg0.getTitle() + "<br>" + arg0.getSnippet()));
                } else {
                    infoWindowText.setText(Html.fromHtml(arg0.getTitle()));
                }
                return view;
            }
        });
    }

}
