package com.google.maps.android.kml;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;

/**
 * Represents a series of coordinates in a placemark
 */
public class KmlLineString implements KmlGeometry {

    public static final String GEOMETRY_TYPE = "LineString";

    final ArrayList<LatLng> mCoordinates;

    /**
     * Creates a new KmlLineString object
     *
     * @param coordinates array of coordinates
     */
    public KmlLineString(ArrayList<LatLng> coordinates) {
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
    public String getKmlGeometryType() {
        return GEOMETRY_TYPE;
    }

    /**
     * Gets the coordinates
     *
     * @return ArrayList of LatLng
     */
    public Object getKmlGeometryCoordinates() {
        return mCoordinates;
    }


}
