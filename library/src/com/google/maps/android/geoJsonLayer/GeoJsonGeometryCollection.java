package com.google.maps.android.geoJsonLayer;

import java.util.ArrayList;

/**
 * A GeoJsonGeometryCollection geometry contains a number of GeoJsonGeometry objects.
 */
public class GeoJsonGeometryCollection implements GeoJsonGeometry {

    private final static String GEOMETRY_TYPE = "GeometryCollection";

    private ArrayList<GeoJsonGeometry> mGeometries;

    /**
     * Creates a new GeoJsonGeometryCollection object
     *
     * @param geometries array of GeoJsonGeometry objects to add to the GeoJsonGeometryCollection
     */
    public GeoJsonGeometryCollection(
            ArrayList<GeoJsonGeometry> geometries) {
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
    public String getType() {
        return GEOMETRY_TYPE;
    }

    /**
     * Gets the stored GeoJsonGeometry objects
     *
     * @return stored GeoJsonGeometry objects
     */
    public ArrayList<GeoJsonGeometry> getGeometries() {
        return mGeometries;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(GEOMETRY_TYPE).append("{");
        sb.append("\n Geometries=").append(mGeometries);
        sb.append("\n}\n");
        return sb.toString();
    }
}
