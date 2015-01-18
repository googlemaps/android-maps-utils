package com.google.maps.android.kml;

/**
 * Created by lavenderch on 12/29/14.
 */

public interface KmlGeometry<T> {

    public String getKmlGeometryType();

    public T getKmlGeometryCoordinates();

}