package com.google.maps.android.geojsonkmlabs.kml;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.geojsonkmlabs.Point;

/**
 * Represents a KML Point. Contains a single coordinate.
 */
public class KmlPoint extends Point {

    /**
     * Creates a new KmlPoint
     *
     * @param coordinates coordinates of the KmlPoint
     */
    public KmlPoint(LatLng coordinates) {
        super(coordinates);
    }

}