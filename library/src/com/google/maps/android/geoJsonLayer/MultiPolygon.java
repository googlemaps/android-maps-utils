package com.google.maps.android.geoJsonLayer;

import java.util.ArrayList;

/**
 * Created by juliawong on 12/29/14.
 */
public class MultiPolygon extends Geometry {

    private final static String GEOMETRY_TYPE = "MultiPolygon";

    private ArrayList<Polygon> mPolygons;

    public MultiPolygon(ArrayList<Polygon> polygons) {
        mPolygons = polygons;
    }

    @Override
    public String getType() {
        return GEOMETRY_TYPE;
    }

    public ArrayList<Polygon> getPolygons() {
        return mPolygons;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(GEOMETRY_TYPE).append("{");
        sb.append("\n Polygons=").append(mPolygons);
        sb.append("\n}\n");
        return sb.toString();
    }
}
