package com.google.maps.android.geojson;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;

/**
 * A GeoJsonPolygon geometry contains an array of arrays of {@link com.google.android.gms.maps.model.LatLng}s.
 * The first array is the polygon exterior boundary. Subsequent arrays are holes.
 */

public class GeoJsonPolygon implements GeoJsonGeometry {

    private final static String GEOMETRY_TYPE = "Polygon";

    private ArrayList<ArrayList<LatLng>> mCoordinates;

    /**
     * Creates a new GeoJsonPolygon object
     *
     * @param coordinates array of arrays of coordinates of GeoJsonPolygon to store
     */
    public GeoJsonPolygon(
            ArrayList<ArrayList<LatLng>> coordinates) {
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
     * Gets the coordinates of the GeoJsonPolygon
     *
     * @return coordinates of the GeoJsonPolygon
     */
    public ArrayList<ArrayList<LatLng>> getCoordinates() {
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
