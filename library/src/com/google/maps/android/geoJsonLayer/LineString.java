package com.google.maps.android.geoJsonLayer;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;

/**
 * Created by juliawong on 12/29/14.
 */
public class LineString extends Geometry {
    private final static String mType = "LineString";

    private ArrayList<LatLng> mCoordinates;

    public LineString(ArrayList<LatLng> coordinates) {
        mCoordinates = coordinates;
    }

    @Override
    public String getType() {
        return mType;
    }

    public ArrayList<LatLng> getCoordinates() {
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
