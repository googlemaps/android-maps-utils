package com.google.maps.android.geoJsonLayer;

import java.util.ArrayList;

/**
 * Created by juliawong on 12/29/14.
 */
public class MultiPoint extends Geometry {

    private final static String GEOMETRY_TYPE = "MultiPoint";

    private ArrayList<Point> mPoints;

    public MultiPoint(ArrayList<Point> points) {
        mPoints = points;
    }

    @Override
    public String getType() {
        return GEOMETRY_TYPE;
    }

    public ArrayList<Point> getPoints() {
        return mPoints;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(GEOMETRY_TYPE).append("{");
        sb.append("\n points=").append(mPoints);
        sb.append("\n}\n");
        return sb.toString();
    }
}
