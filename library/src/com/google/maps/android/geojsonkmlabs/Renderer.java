package com.google.maps.android.geojsonkmlabs;

import android.content.Context;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.GroundOverlay;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.Polyline;
import com.google.maps.android.geojsonkmlabs.Feature;
import com.google.maps.android.geojsonkmlabs.kml.KmlContainer;
import com.google.maps.android.geojsonkmlabs.kml.KmlGroundOverlay;
import com.google.maps.android.geojsonkmlabs.kml.KmlPlacemark;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by suvercha on 12/15/16.
 */

public class Renderer {
    private GoogleMap mMap;

    private HashMap<Feature, Object> mFeatures;

    private HashMap<String, String> mStyleMaps;

    private HashMap<KmlGroundOverlay, GroundOverlay> mGroundOverlays;

    private boolean mLayerOnMap;

    private Context mContext;

    public Renderer(GoogleMap map) {
        mMap = map;
        mLayerOnMap = false;
    }

    public Renderer(GoogleMap map, HashMap<Feature, Object> features ) {
        mMap = map;
        mFeatures = features;
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
    private static void removeFeatures(HashMap<Feature, Object> features) {
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




}
