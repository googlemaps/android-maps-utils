package com.google.maps.android.kml;

import java.util.ArrayList;

/**
 * Created by juliawong on 1/7/15.
 */
public class MultiGeometry implements Geometry {

    private static final String GEOMETRY_TYPE = "MultiGeometry";

    private ArrayList<Geometry> mGeometries = new ArrayList<Geometry>();

    /**
     * Gets the type of geometry
     *
     * @return type of geometry
     */
    @Override
    public String getType() {
        return GEOMETRY_TYPE;
    }

    @Override
    public void createCoordinates(String text) {

    }

    /**
     * Returns an ArrayList of Geometry objects
     *
     * @return geometry objects
     */
    @Override
    public Object getGeometry() {
        return mGeometries;
    }

    /**
     * Adds an array of geometries
     *
     * @param geometry geometries to add
     */
    @Override
    public void setGeometry(Object geometry) {
        mGeometries = (ArrayList<Geometry>) geometry;
    }
}
