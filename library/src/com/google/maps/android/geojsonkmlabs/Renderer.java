package com.google.maps.android.geojsonkmlabs;

import android.content.Context;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.GroundOverlay;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.Polyline;
import com.google.maps.android.geojsonkmlabs.Feature;
import com.google.maps.android.geojsonkmlabs.geojson.BiMultiMap;
import com.google.maps.android.geojsonkmlabs.geojson.GeoJsonFeature;
import com.google.maps.android.geojsonkmlabs.geojson.GeoJsonGeometry;
import com.google.maps.android.geojsonkmlabs.geojson.GeoJsonGeometryCollection;
import com.google.maps.android.geojsonkmlabs.geojson.GeoJsonLineString;
import com.google.maps.android.geojsonkmlabs.geojson.GeoJsonMultiLineString;
import com.google.maps.android.geojsonkmlabs.geojson.GeoJsonMultiPoint;
import com.google.maps.android.geojsonkmlabs.geojson.GeoJsonMultiPolygon;
import com.google.maps.android.geojsonkmlabs.geojson.GeoJsonPoint;
import com.google.maps.android.geojsonkmlabs.geojson.GeoJsonPolygon;
import com.google.maps.android.geojsonkmlabs.kml.KmlContainer;
import com.google.maps.android.geojsonkmlabs.kml.KmlGroundOverlay;
import com.google.maps.android.geojsonkmlabs.kml.KmlPlacemark;
import com.google.maps.android.geojsonkmlabs.kml.KmlStyle;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

/**
 * Created by suvercha on 12/15/16.
 */

public class Renderer {
    private GoogleMap mMap;

    private final BiMultiMap<Feature> mFeatures = new BiMultiMap<>();

    private HashMap<String, KmlStyle> mStyles;

    private HashMap<String, String> mStyleMaps;

    private final static Object FEATURE_NOT_ON_MAP = null;

    private HashMap<KmlGroundOverlay, GroundOverlay> mGroundOverlays;

    private boolean mLayerOnMap;

    private Context mContext;

    private ArrayList<KmlContainer> mContainers;

    public Renderer(GoogleMap map) {
        mMap = map;
        mLayerOnMap = false;
    }

    public Renderer(GoogleMap map, HashMap<Feature, Object> features ) {
        mMap = map;
        mFeatures.putAll(features);
        mLayerOnMap = false;
    }

    /* package */ boolean isLayerOnMap() {
        return mLayerOnMap;
    }

    /**
     * Gets the GoogleMap that GeoJsonFeature objects are being placed on
     *
     * @return GoogleMap
     */
    /* package */ GoogleMap getMap() {
        return mMap;
    }

    /**
     * Removes all given Features/Placemarks from the map and clears all stored features and placemarks.
     *
     * @param features placemarks to remove
     */
    public static void removeFeatures(HashMap<Feature, Object> features) {
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
     * Stores all given data and adds it onto the map
     *
     * @param styles         hashmap of styles
     * @param styleMaps      hashmap of style maps
     * @param features       hashmap of features
     * @param folders        array of containers
     * @param groundOverlays hashmap of ground overlays
     */
    /* package */ void storeKmlData(HashMap<String, KmlStyle> styles,
                                    HashMap<String, String> styleMaps,
                                    HashMap<Feature, Object> features, ArrayList<KmlContainer> folders,
                                    HashMap<KmlGroundOverlay, GroundOverlay> groundOverlays) {
        mStyles = styles;
        mStyleMaps = styleMaps;
        mFeatures = features;
        mContainers = folders;
        mGroundOverlays = groundOverlays;
    }


    /**
     * Adds all of the stored features in the layer onto the map if the layer is not already on the
     * map.
     */
    /* package */ void addLayerToMap() {
        if (!mLayerOnMap) {
            mLayerOnMap = true;
            for (Feature feature : getFeatures()) {
                addFeature(feature);
            }
        }
    }

    /**
     * Gets a set containing GeoJsonFeatures
     *
     * @return set containing GeoJsonFeatures
     */
     public Set<Feature> getFeatures() {
        return mFeatures.keySet();
    }

    /**
     * Adds a new GeoJsonFeature to the map if its geometry property is not null.
     *
     * @param feature feature to add to the map
     */
     public void addFeature(Feature feature) {
        Object mapObject = FEATURE_NOT_ON_MAP;
        if (mLayerOnMap) {
            if (mFeatures.containsKey(feature)) {
                // Remove current map objects before adding new ones
                removeFromMap(mFeatures.get(feature));
            }

            if (feature.hasGeometry()) {
                // Create new map object
                mapObject = addFeatureToMap(feature, feature.getGeometry());
            }
        }
        mFeatures.put(feature, mapObject);
    }

    /**
     * Adds a new object onto the map using the GeoJsonGeometry for the coordinates and the
     * GeoJsonFeature for the styles.
     *
     * @param feature  feature to get geometry style
     * @param geometry geometry to add to the map
     */
    private Object addFeatureToMap(Feature feature, Geometry geometry) {
        String geometryType = geometry.getGeometryType();
        if (geometryType.equals("Point")) {
            return addPointToMap(feature.getPointStyle(), (GeoJsonPoint) geometry);
        } else if (geometryType.equals("LineString")) {
            return addLineStringToMap(feature.getLineStringStyle(),
                    (GeoJsonLineString) geometry);
        } else if (geometryType.equals("Polygon")) {
            return addPolygonToMap(feature.getPolygonStyle(),
                    (GeoJsonPolygon) geometry);
        } else if (geometryType.equals("MultiPoint")) {
            return addMultiPointToMap(feature.getPointStyle(),
                    (GeoJsonMultiPoint) geometry);
        } else if (geometryType.equals("MultiLineString")) {
            return addMultiLineStringToMap(feature.getLineStringStyle(),
                    ((GeoJsonMultiLineString) geometry));
        } else if (geometryType.equals("MultiPolygon")) {
            return addMultiPolygonToMap(feature.getPolygonStyle(),
                    ((GeoJsonMultiPolygon) geometry));
        } else if (geometryType.equals("GeometryCollection")) {
            return addGeometryCollectionToMap(feature,
                    ((GeoJsonGeometryCollection) geometry).getGeometries());
        }
        return null;
    }







}
