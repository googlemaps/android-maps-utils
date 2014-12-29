package com.google.maps.android.geoJsonLayer;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;

/**
 * Created by juliawong on 12/29/14.
 */

public class Polygon extends Geometry {
    private final static String mType = "Polygon";

    private ArrayList<ArrayList<LatLng>> mCoordinates;

    public Polygon(
            ArrayList<ArrayList<LatLng>> coordinates) {
        mCoordinates = coordinates;
    }

    @Override
    public String getType() {
        return mType;
    }

    public ArrayList<ArrayList<LatLng>> getCoordinates() {
        return mCoordinates;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(mType).append("{");
        sb.append("\n coordinates=").append(mCoordinates);
        sb.append("\n}\n");
        return sb.toString();
    }

}
