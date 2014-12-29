package com.google.maps.android.geoJsonLayer;

import android.util.Log;

import java.util.Map;

/**
 * Created by juliawong on 12/29/14.
 */
public class Feature {

    private Geometry mGeometry;

    private String mId;

    private Style mStyle;

    private Map<String, String> mProperties;

    // TODO: implement an iterator thing or just return mProperties

    public Geometry getGeometry() {
        return mGeometry;
    }

    public String getId() {
        return mId;
    }

    public String setProperty(String property, String value) {
        return mProperties.put(property, value);
    }

    public String getProperty(String property) {
        return mProperties.get(property);
    }

    public String removeProperty(String property) {
        return mProperties.remove(property);
    }

    public Feature() {
        mGeometry = new LineString();
        Log.i("E", Boolean.toString(mGeometry instanceof Geometry));
    }

}
