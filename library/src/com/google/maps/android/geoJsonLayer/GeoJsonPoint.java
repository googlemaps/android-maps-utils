package com.google.maps.android.geoJsonLayer;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by juliawong on 12/29/14.
 *
 * A Point geometry contains a single {@link com.google.android.gms.maps.model.LatLng}.
 */
public class GeoJsonPoint implements GeoJsonGeometry {

    private final static String GEOMETRY_TYPE = "Point";

    private LatLng mCoordinates;

    /**
     * Creates a new Point
     *
     * @param coordinates coordinates of Point to store
     */
    public GeoJsonPoint(LatLng coordinates) {
        if (coordinates == null) {
            throw new IllegalArgumentException("Coordinates cannot be null");
        }
        mCoordinates = coordinates;
    }

    public String getType() {
        return GEOMETRY_TYPE;
    }

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
