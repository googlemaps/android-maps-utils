package com.google.maps.android.geoJsonLayer;

import java.util.ArrayList;

/**
 * Created by juliawong on 12/29/14.
 */
public class GeometryCollection extends Geometry {

    private final static String mType = "GeometryCollection";

    private ArrayList<Geometry> mGeometries;

    @Override
    public String getType() {
        return mType;
    }

    public ArrayList<Geometry> getGeometries() {
        return mGeometries;
    }

}
