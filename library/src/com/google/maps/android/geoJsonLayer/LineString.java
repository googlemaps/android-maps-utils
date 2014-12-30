package com.google.maps.android.geoJsonLayer;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;

/**
 * Created by juliawong on 12/29/14.
 */
public class LineString extends Geometry {

    private final static String GEOMETRY_TYPE = "LineString";

    private ArrayList<LatLng> mCoordinates;

    /**
     * Creates a new LineString object
     *
     * @param coordinates array of coordinates of LineString to store
     */
    public LineString(ArrayList<LatLng> coordinates) {
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
     * Gets the coordinates of the LineString
     *
     * @return coordinates of the LineString
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
