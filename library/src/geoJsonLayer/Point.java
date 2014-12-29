package geoJsonLayer;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by juliawong on 12/29/14.
 */
public class Point extends Geometry {

    private final static String mType = "Point";

    private LatLng mCoordinates;

    @Override
    public String getType() {
        return mType;
    }

    public LatLng getCoordinates() {
        return mCoordinates;
    }
}
