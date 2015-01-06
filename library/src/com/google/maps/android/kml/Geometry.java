package com.google.maps.android.kml;

/**
 * Created by lavenderch on 12/29/14.
 */
public interface Geometry {

    public String getType();

    public void createCoordinates(String text);

    public Object getGeometry();

    public void setGeometry(Object geometry);
}