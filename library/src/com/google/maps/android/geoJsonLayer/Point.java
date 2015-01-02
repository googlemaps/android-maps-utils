package com.google.maps.android.geoJsonLayer;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by juliawong on 12/29/14.
 *
 * A Point geometry contains a single {@link com.google.android.gms.maps.model.LatLng}.
 */
public class Point extends Geometry {

    private final static String GEOMETRY_TYPE = "Point";

    private LatLng mCoordinates;

    /**
     * Creates a new Point
     *
     * @param coordinates coordinates of Point to store
     */
    public Point(LatLng coordinates) {
        if (coordinates == null) {
            throw new IllegalArgumentException("Coordinates cannot be null");
        }
        mCoordinates = coordinates;
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
     * Gets the coordinates of the Point
     *
     * @return coordinates of the Point
     */
    public LatLng getCoordinates() {
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
