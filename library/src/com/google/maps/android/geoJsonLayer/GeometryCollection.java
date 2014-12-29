package com.google.maps.android.geoJsonLayer;

import java.util.ArrayList;

/**
 * Created by juliawong on 12/29/14.
 */
public class GeometryCollection extends Geometry {

    private final static String mType = "GeometryCollection";

    private ArrayList<Geometry> mGeometries;

    public GeometryCollection(
            ArrayList<Geometry> geometries) {
        mGeometries = geometries;
    }

    @Override
    public String getType() {
        return mType;
    }

    public ArrayList<Geometry> getGeometries() {
        return mGeometries;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(mType).append("{");
        sb.append("\n Geometries=").append(mGeometries);
        sb.append("\n}\n");
        return sb.toString();
    }
}
