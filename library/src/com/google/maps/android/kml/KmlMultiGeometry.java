package com.google.maps.android.kml;

import java.util.ArrayList;

/**
 * Created by juliawong on 1/7/15.
 */
public class KmlMultiGeometry implements KmlGeometry<ArrayList<KmlGeometry>> {

    private static final String GEOMETRY_TYPE = "MultiGeometry";

    private ArrayList<KmlGeometry> mGeometries = new ArrayList<KmlGeometry>();

    /**
     * Creates a new KmlMultiGeometry object
     *
     * @param geometries array of KmlGeometry objects contained in the MultiGeometry
     */
    public KmlMultiGeometry(ArrayList<KmlGeometry> geometries) {
        if (geometries == null) {
            throw new IllegalArgumentException("Geometries cannot be null");
        }
        mGeometries = geometries;
    }

    /**
     * Gets the type of geometry
     *
     * @return type of geometry
     */
    @Override
    public String getKmlGeometryType() {
        return GEOMETRY_TYPE;
    }

    /**
     * Gets an ArrayList of KmlGeometry objects
     *
     * @return Arraylist of KmlGeometry objects
     */

    public ArrayList<KmlGeometry> getKmlGeometryObject() {
        return mGeometries;
    }
}
