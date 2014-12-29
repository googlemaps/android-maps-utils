package com.google.maps.android.geoJsonLayer;

import java.util.ArrayList;

/**
 * Created by juliawong on 12/29/14.
 */
public class MultiLineString extends Geometry {
    private final static String mType = "MultiLineString";

    private ArrayList<LineString> mLineStrings;

    public MultiLineString(
            ArrayList<LineString> lineStrings) {
        mLineStrings = lineStrings;
    }

    @Override
    public String getType() {
        return mType;
    }

    public ArrayList<LineString> getLineStrings() {
        return mLineStrings;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(mType).append("{");
        sb.append("\n LineStrings=").append(mLineStrings);
        sb.append("\n}\n");
        return sb.toString();
    }
}
