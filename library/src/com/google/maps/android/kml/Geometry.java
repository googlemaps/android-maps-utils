package com.google.maps.android.kml;

/**
 * Created by lavenderch on 12/29/14.
 */
public interface Geometry {

    public void createCoordinates(String text);

    public void setGeometry(Object geometry);

    public Object getGeometry();
}