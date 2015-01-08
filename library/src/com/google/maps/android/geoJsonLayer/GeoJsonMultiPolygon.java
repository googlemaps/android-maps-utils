package com.google.maps.android.geoJsonLayer;

import java.util.ArrayList;

/**
 * Created by juliawong on 12/29/14.
 *
 * A MultiPolygon geometry contains a number of {@link GeoJsonPolygon}s.
 */
public class GeoJsonMultiPolygon extends GeoJsonGeometry {

    private final static String GEOMETRY_TYPE = "MultiPolygon";

    private ArrayList<GeoJsonPolygon> mGeoJsonPolygons;

    /**
     * Creates a new MultiPolygon
     *
     * @param geoJsonPolygons array of Polygons to add to the MultiPolygon
     */
    public GeoJsonMultiPolygon(ArrayList<GeoJsonPolygon> geoJsonPolygons) {
        if (geoJsonPolygons == null) {
            throw new IllegalArgumentException("Polygons cannot be null");
        }
        mGeoJsonPolygons = geoJsonPolygons;
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
    public ArrayList<GeoJsonPolygon> getPolygons() {
        return mGeoJsonPolygons;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(GEOMETRY_TYPE).append("{");
        sb.append("\n Polygons=").append(mGeoJsonPolygons);
        sb.append("\n}\n");
        return sb.toString();
    }
}
