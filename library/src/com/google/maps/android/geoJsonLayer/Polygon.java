package com.google.maps.android.geoJsonLayer;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;

/**
 * Created by juliawong on 12/29/14.
 */
public class Polygon extends Geometry {
    private final static String mType = "Polygon";

    private ArrayList<ArrayList<LatLng>> mCoordinates;

    @Override
    public String getType() {
        return mType;
    }

    public ArrayList<ArrayList<LatLng>> getCoordinates() {
        return mCoordinates;
    }

}
