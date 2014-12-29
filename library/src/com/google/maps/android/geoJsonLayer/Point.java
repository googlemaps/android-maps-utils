package com.google.maps.android.geoJsonLayer;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by juliawong on 12/29/14.
 */
public class Point extends Geometry {

    private final static String mType = "Point";

    private LatLng mCoordinates;

    public Point(LatLng coordinates) {
        mCoordinates = coordinates;
    }

    @Override
    public String getType() {
        return mType;
    }

    public LatLng getCoordinates() {
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
