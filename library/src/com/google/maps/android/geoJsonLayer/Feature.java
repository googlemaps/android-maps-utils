package com.google.maps.android.geoJsonLayer;

import com.google.android.gms.maps.model.LatLng;

import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;

/**
 * Created by juliawong on 12/29/14.
 */
public class Feature {

    private Geometry mGeometry;

    private Style mStyle;

    private String mId;

    private Map<String, String> mProperties;

    public Feature(Geometry geometry, Style style, String id, Map<String, String> properties) {
        this.mGeometry = geometry;
        this.mStyle = style;
        this.mId = id;
        this.mProperties = properties;


        createDemoGeometry();
    }

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

    private void createDemoGeometry() {
        Point p1 = new Point(new LatLng(100.0, 0.0));
        Point p2 = new Point(new LatLng(0.0, 0.0));
        LineString l1 = new LineString(new ArrayList<LatLng>(
                Arrays.asList(new LatLng(101.0, 0.0), new LatLng(102.0, 1.0))));
        LineString l2 = new LineString(new ArrayList<LatLng>(Arrays.asList(new LatLng(101.0, 0.0), new LatLng(102.0, 1.0))));
        GeometryCollection gc1 = new GeometryCollection(new ArrayList<Geometry>(Arrays.asList(l2, p2)));
        GeometryCollection gc2 = new GeometryCollection(new ArrayList<Geometry>(Arrays.asList(gc1)));
        mGeometry = new GeometryCollection(new ArrayList<Geometry>(Arrays.asList(p1, l1, gc2)));
        Log.i("Geometry", mGeometry.toString());
    }

}
