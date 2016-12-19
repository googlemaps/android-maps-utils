package com.google.maps.android.geojsonkmlabs;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.geojsonkmlabs.Geometry;

public class Point implements Geometry {

    public final static String GEOMETRY_TYPE = "Point";

    public final LatLng mCoordinates;

    /**
     * Creates a new Point object
     *
     * @param coordinates coordinates of Point to store
     */
    public Point(LatLng coordinates) {
        if (coordinates == null) {
            throw new IllegalArgumentException("Coordinates cannot be null");
        }
        mCoordinates = coordinates;
    }

    /** {@inheritDoc} */
    public String getGeometryType() {
        return GEOMETRY_TYPE;
    }

    /**
     * Gets the coordinates of the Point
     *
     * @return coordinates of the Point
     */
    public LatLng getGeometryObject() {
        return mCoordinates;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(GEOMETRY_TYPE).append("{");
        sb.append("\n coordinates=").append(mCoordinates);
        sb.append("\n}\n");
        return sb.toString();
    }
}