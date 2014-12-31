package com.google.maps.android.geoJsonLayer;

import java.util.ArrayList;

/**
 * Created by juliawong on 12/29/14.
 */
public class MultiPoint extends Geometry {

    private final static String GEOMETRY_TYPE = "MultiPoint";

    private ArrayList<Point> mPoints;

    /**
     * Creates a MultiPoint object
     *
     * @param points array of Points to add to the MultiPoint
     */
    public MultiPoint(ArrayList<Point> points) {
        mPoints = points;
    }

    /**
     * Gets the type of geometry
     *
     * @return type of geometry
     */
    @Override
    public String getType() {
        return GEOMETRY_TYPE;
    }

    /**
     * Gets the array of Points
     *
     * @return array of Points
     */
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
