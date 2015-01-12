package com.google.maps.android.geoJsonLayer;

import java.util.ArrayList;

/**
 * Created by juliawong on 12/29/14.
 *
 * A MultiPoint geometry contains a number of {@link GeoJsonPoint}s.
 */
public class GeoJsonMultiPoint implements GeoJsonGeometry {

    private final static String GEOMETRY_TYPE = "MultiPoint";

    private ArrayList<GeoJsonPoint> mGeoJsonPoints;

    /**
     * Creates a MultiPoint object
     *
     * @param geoJsonPoints array of Points to add to the MultiPoint
     */
    public GeoJsonMultiPoint(ArrayList<GeoJsonPoint> geoJsonPoints) {
        if (geoJsonPoints == null) {
            throw new IllegalArgumentException("Points cannot be null");
        }
        mGeoJsonPoints = geoJsonPoints;
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
    public ArrayList<GeoJsonPoint> getPoints() {
        return mGeoJsonPoints;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(GEOMETRY_TYPE).append("{");
        sb.append("\n points=").append(mGeoJsonPoints);
        sb.append("\n}\n");
        return sb.toString();
    }
}
