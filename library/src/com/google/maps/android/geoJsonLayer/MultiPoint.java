package com.google.maps.android.geoJsonLayer;

import java.util.ArrayList;

/**
 * Created by juliawong on 12/29/14.
 */
public class MultiPoint extends Geometry {
    private final static String mType = "MultiPoint";

    private ArrayList<Point> mPoints;

    @Override
    public String getType() {
        return mType;
    }

    public ArrayList<Point> getPoints() {
        return mPoints;
    }
}
