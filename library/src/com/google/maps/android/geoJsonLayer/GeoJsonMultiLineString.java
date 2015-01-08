package com.google.maps.android.geoJsonLayer;

import java.util.ArrayList;

/**
 * Created by juliawong on 12/29/14.
 *
 * A MultiLineString geometry contains a number of {@link GeoJsonLineString}s.
 */
public class GeoJsonMultiLineString extends GeoJsonGeometry {

    private final static String GEOMETRY_TYPE = "MultiLineString";

    private ArrayList<GeoJsonLineString> mGeoJsonLineStrings;

    /**
     * Creates a new MultiLineString object
     *
     * @param geoJsonLineStrings array of LineStrings to add to the MultiLineString
     */
    public GeoJsonMultiLineString(
            ArrayList<GeoJsonLineString> geoJsonLineStrings) {
        if (geoJsonLineStrings == null) {
            throw new IllegalArgumentException("LineStrings cannot be null");
        }
        mGeoJsonLineStrings = geoJsonLineStrings;
    }

    /**
     * Get the type of geometry
     *
     * @return type of geometry
     */
    @Override
    public String getType() {
        return GEOMETRY_TYPE;
    }

    /**
     * Gets the array of LineStrings
     *
     * @return array of LineStrings
     */
    public ArrayList<GeoJsonLineString> getLineStrings() {
        return mGeoJsonLineStrings;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(GEOMETRY_TYPE).append("{");
        sb.append("\n LineStrings=").append(mGeoJsonLineStrings);
        sb.append("\n}\n");
        return sb.toString();
    }
}
