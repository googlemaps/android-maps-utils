package com.google.maps.android.kml;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by lavenderch on 12/22/14.
 */
public class Point implements Geometry {

    private LatLng pointCoordinate;

    /**
     * Receives text representing a tuple coordinates separated by a comma
     * (longitude, latitude, altitude) This method converts this tuples into LatLng points,
     * and ignores the altitude component
     * @param text A string representation of a tuple coordinate, with values separated by commas
     */
    @Override
    public void createCoordinates(String text) {
        String[] coordinate = text.split(",");
        LatLng pointCoordinate = Placemark.convertToLatLng(coordinate);
        setGeometry(pointCoordinate);
    }

    /**
     * Creates a LatLng point
     *
     * @param geometry  An object which represents a LatLng point
     */
    public void setGeometry(Object geometry) {
        pointCoordinate = ((LatLng) geometry);
    }

    /**
     * Returns a LatLng point representing point of a marker
     */
    public Object getGeometry() {
       return pointCoordinate;
    }
}
