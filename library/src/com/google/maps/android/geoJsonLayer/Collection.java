package com.google.maps.android.geoJsonLayer;

import com.google.android.gms.maps.GoogleMap;

import android.content.Context;

import java.util.ArrayList;

/**
 * Created by juliawong on 12/29/14.
 */
public class Collection {

    private ArrayList<Feature> mFeatures = new ArrayList<Feature>();

    private GoogleMap mMap;

    public Collection(GoogleMap map, int resourceId, Context context) {
        mMap = map;
    }

    // TODO: implement an iterator thing or just return mFeatures

    public Feature getFeatureById(String id) {

        return null;
    }

    public GoogleMap getMap() {
        return mMap;
    }

    public void setMap(GoogleMap map) {
        mMap = map;
    }

    // TODO: Split styles or keep as one
    public Style getStyle() {
        return null;
    }

    public void setStyle(Style style) {

    }

    public void removeFeature(Feature feature) {

    }

    public void setDefaultStyle(Style defaultStyle) {

    }

    public void setPreserveViewPort(boolean preserveViewPort) {

    }

    public void setZIndex(int zIndex) {

    }

}
