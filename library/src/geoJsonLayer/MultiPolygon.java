package geoJsonLayer;

import java.util.ArrayList;

/**
 * Created by juliawong on 12/29/14.
 */
public class MultiPolygon extends Geometry {

    private final static String mType = "MultiPolygon";

    private ArrayList<Polygon> mPolygons;

    @Override
    public String getType() {
        return mType;
    }

    public ArrayList<Polygon> getPolygons() {
        return mPolygons;
    }


}
