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

    private PointStyle mDefaultPointStyle;

    private LineStringStyle mDefaultLineStringStyle;

    private PolygonStyle mDefaultPolygonStyle;

    public Collection(GoogleMap map, int resourceId, Context context) {
        mMap = map;
    }

    // TODO: implement an iterator thing or just return mFeatures

    public Feature getFeatureById(String id) {
        for (Feature feature : mFeatures) {
            if (feature.getId().equals(id)) {
                return feature;
            }
        }
        return null;
    }

    public GoogleMap getMap() {
        return mMap;
    }

    public void setMap(GoogleMap map) {
        mMap = map;
    }

    public void removeFeature(Feature feature) {
        mFeatures.remove(feature);
    }

    public PointStyle getDefaultPointStyle() {
        return mDefaultPointStyle;
    }

    public void setDefaultPointStyle(PointStyle pointStyle) {
        mDefaultPointStyle = pointStyle;
    }

    public LineStringStyle getDefaultLineStringStyle() {
        return mDefaultLineStringStyle;
    }

    public void setDefaultLineStringStyle(LineStringStyle lineStringStyle) {
        mDefaultLineStringStyle = lineStringStyle;
    }

    public PolygonStyle getDefaultPolygonStyle() {
        return mDefaultPolygonStyle;
    }

    public void setDefaultPolygonStyle(PolygonStyle polygonStyle) {
        mDefaultPolygonStyle = polygonStyle;
    }

    public void setPreserveViewPort(boolean preserveViewPort) {

    }

    public void setZIndex(float zIndex) {
        mDefaultLineStringStyle.setZIndex(zIndex);
        mDefaultPolygonStyle.setZIndex(zIndex);
        // TODO: redraw objects
    }

}
