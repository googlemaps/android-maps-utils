package com.google.maps.android.geojson;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;

/**
 * A GeoJsonLineString geometry contains a number of {@link com.google.android.gms.maps.model.LatLng}s.
 */
public class GeoJsonLineString implements GeoJsonGeometry {

    private final static String GEOMETRY_TYPE = "LineString";

    private ArrayList<LatLng> mCoordinates;

    /**
     * Creates a new GeoJsonLineString object
     *
     * @param coordinates array of coordinates of GeoJsonLineString to store
     */
    public GeoJsonLineString(ArrayList<LatLng> coordinates) {
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
     * Gets the coordinates of the GeoJsonLineString
     *
     * @return coordinates of the GeoJsonLineString
     */
    public ArrayList<LatLng> getCoordinates() {
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
