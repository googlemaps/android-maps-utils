package com.google.maps.android.geoJsonLayer;

import java.util.ArrayList;

/**
 * Created by juliawong on 12/29/14.
 *
 * A GeometryCollection geometry contains a number of geometry objects.
 */
public class GeoJsonGeometryCollection extends GeoJsonGeometry {

    private final static String GEOMETRY_TYPE = "GeometryCollection";

    private ArrayList<GeoJsonGeometry> mGeometries;

    /**
     * Creates a new GeometryCollection object
     *
     * @param geometries array of Geometry objects to add to the GeometryCollection
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
     * Gets the stored geometries
     *
     * @return stored geometries
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
