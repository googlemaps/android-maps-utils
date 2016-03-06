package com.google.maps.android.kml;

import com.google.android.gms.maps.model.LatLng;

/**
 * Represents a KML Point. Contains a single coordinate.
 */
public class KmlPoint implements KmlGeometry<LatLng> {

    public static final String GEOMETRY_TYPE = "Point";

    private final LatLng mCoordinate;

    /**
     * Creates a new KmlPoint
     *
     * @param coordinate coordinate of the KmlPoint
     */
    public KmlPoint(LatLng coordinate) {
        if (coordinate == null) {
            throw new IllegalArgumentException("Coordinates cannot be null");
        }
        mCoordinate = coordinate;
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
     * Gets the coordinates
     *
     * @return LatLng with the coordinate of the KmlPoint
     */
    public LatLng getGeometryObject() {
        return mCoordinate;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(GEOMETRY_TYPE).append("{");
        sb.append("\n coordinates=").append(mCoordinate);
        sb.append("\n}\n");
        return sb.toString();
    }
}
