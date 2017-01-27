package com.google.maps.android.data.kml;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.data.LineString;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a KML LineString. Contains a single array of coordinates.
 */
public class KmlLineString extends LineString {

    /**
     * Creates a new KmlLineString object
     *
     * @param coordinates array of coordinates
     */
    public KmlLineString(ArrayList<LatLng> coordinates) {
        super(coordinates);
    }

    /**
     * Gets the coordinates
     *
     * @return ArrayList of LatLng
     */
    public ArrayList<LatLng> getGeometryObject() {
        List<LatLng> coordinatesList = super.getGeometryObject();
        return new ArrayList<>(coordinatesList);
    }
}
