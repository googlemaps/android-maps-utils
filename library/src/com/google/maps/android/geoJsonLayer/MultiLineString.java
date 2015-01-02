package com.google.maps.android.geoJsonLayer;

import java.util.ArrayList;

/**
 * Created by juliawong on 12/29/14.
 *
 * A MultiLineString geometry contains a number of {@link com.google.maps.android.geoJsonLayer.LineString}s.
 */
public class MultiLineString extends Geometry {

    private final static String GEOMETRY_TYPE = "MultiLineString";

    private ArrayList<LineString> mLineStrings;

    /**
     * Creates a new MultiLineString object
     *
     * @param lineStrings array of LineStrings to add to the MultiLineString
     */
    public MultiLineString(
            ArrayList<LineString> lineStrings) {
        mLineStrings = lineStrings;
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
    public ArrayList<LineString> getLineStrings() {
        return mLineStrings;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(GEOMETRY_TYPE).append("{");
        sb.append("\n LineStrings=").append(mLineStrings);
        sb.append("\n}\n");
        return sb.toString();
    }
}
