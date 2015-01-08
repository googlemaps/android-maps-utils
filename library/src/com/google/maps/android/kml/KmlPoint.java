package com.google.maps.android.kml;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by lavenderch on 12/22/14.
 */
public class KmlPoint implements KmlGeometry {

    public static final String GEOMETRY_TYPE = "Point";

    private final LatLng mCoordinate;

    /**
     * Creates a new KmlPoint
     *
     * @param coordinate coordinate of the KmlPoint
     */
    public KmlPoint(LatLng coordinate) {
        mCoordinate = coordinate;
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
     * Gets the coordinates
     *
     * @return LatLng with the coordinate of the KmlPoint
     */
    public Object getGeometry() {
        return mCoordinate;
    }
}
