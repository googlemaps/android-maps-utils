package com.google.maps.android.geoJsonLayer;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;

/**
 * Created by juliawong on 12/29/14.
 *
 * A Polygon geometry contains a number of arrays of {@link com.google.android.gms.maps.model.LatLng}s.
 * The first array is the polygon exterior boundary. Subsequent arrays are holes.
 */

public class Polygon extends Geometry {

    private final static String GEOMETRY_TYPE = "Polygon";

    private ArrayList<ArrayList<LatLng>> mCoordinates;

    /**
     * Creates a new Polygon object
     *
     * @param coordinates array of arrays of coordinates of Polygon to store
     */
    public Polygon(
            ArrayList<ArrayList<LatLng>> coordinates) {
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
     * Gets the coordinates of the Polygon
     *
     * @return coordinates of the Polygon
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
