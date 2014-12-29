package geoJsonLayer;

import java.util.ArrayList;

/**
 * Created by juliawong on 12/29/14.
 */
public class MultiLineString extends Geometry {
    private final static String mType = "MultiLineString";

    private ArrayList<LineString> mLineStrings;

    @Override
    public String getType() {
        return mType;
    }

    public ArrayList<LineString> getLineStrings() {
        return mLineStrings;
    }

}
