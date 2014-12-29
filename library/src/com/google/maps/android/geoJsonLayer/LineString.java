package com.google.maps.android.geoJsonLayer;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;

/**
 * Created by juliawong on 12/29/14.
 */
public class LineString extends Geometry {
    private final static String mType = "LineString";

    private ArrayList<LatLng> mCoordinates;

    @Override
    public String getType() {
        return mType;
    }

    public ArrayList<LatLng> getCoordinates() {
        return mCoordinates;
    }
}
