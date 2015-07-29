package com.google.maps.android.kml;

import java.util.ArrayList;

/**
 * Represents a KML MultiGeometry. Contains an array of KmlGeometry objects.
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
    public String getGeometryType() {
        return GEOMETRY_TYPE;
    }

    /**
     * Gets an ArrayList of KmlGeometry objects
     *
     * @return ArrayList of KmlGeometry objects
     */

    public ArrayList<KmlGeometry> getGeometryObject() {
        return mGeometries;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(GEOMETRY_TYPE).append("{");
        sb.append("\n geometries=").append(mGeometries);
        sb.append("\n}\n");
        return sb.toString();
    }
}
