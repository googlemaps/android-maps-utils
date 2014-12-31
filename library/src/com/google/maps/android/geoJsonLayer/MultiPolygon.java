package com.google.maps.android.geoJsonLayer;

import java.util.ArrayList;

/**
 * Created by juliawong on 12/29/14.
 */
public class MultiPolygon extends Geometry {

    private final static String GEOMETRY_TYPE = "MultiPolygon";

    private ArrayList<Polygon> mPolygons;

    /**
     * Creates a new MultiPolygon
     *
     * @param polygons array of Polygons to add to the MultiPolygon
     */
    public MultiPolygon(ArrayList<Polygon> polygons) {
        mPolygons = polygons;
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
     * Gets the array of Polygons
     *
     * @return array of Polygons
     */
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
