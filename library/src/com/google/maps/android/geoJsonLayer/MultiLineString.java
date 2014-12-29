package com.google.maps.android.geoJsonLayer;

import java.util.ArrayList;

/**
 * Created by juliawong on 12/29/14.
 */
public class MultiLineString extends Geometry {

    private final static String GEOMETRY_TYPE = "MultiLineString";

    private ArrayList<LineString> mLineStrings;

    public MultiLineString(
            ArrayList<LineString> lineStrings) {
        mLineStrings = lineStrings;
    }

    @Override
    public String getType() {
        return GEOMETRY_TYPE;
    }

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
