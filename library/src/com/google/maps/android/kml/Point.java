package com.google.maps.android.kml;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by lavenderch on 12/22/14.
 */
public class Point implements Geometry {

    private LatLng pointCoordinate;

    @Override
    public void createCoordinates(String text) {
        String[] coordinate = text.split(",");
        LatLng pointCoordinate = Placemark.convertToLatLng(coordinate);
        setGeometry(pointCoordinate);
    }

    public void setGeometry(Object geometry) {
        pointCoordinate = ((LatLng) geometry);
    }

    @Override
    public Object getGeometry() {
       return pointCoordinate;
    }
}
