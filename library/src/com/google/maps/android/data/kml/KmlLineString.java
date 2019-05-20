package com.google.maps.android.data.kml;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.data.LineString;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a KML LineString. Contains a single array of coordinates.
 */
public class KmlLineString extends LineString {
    private final ArrayList<Double> mAltitudes;

    /**
     * Creates a new KmlLineString object
     *
     * @param coordinates array of coordinates
     */
    public KmlLineString(ArrayList<LatLng> coordinates) {
        this(coordinates, null);
    }

    /**
     * Creates a new KmlLineString object
     *
     * @param coordinates array of coordinates
     * @param altitudes array of altitudes
     */
    public KmlLineString(ArrayList<LatLng> coordinates, ArrayList<Double> altitudes) {
        super(coordinates);

        this.mAltitudes = altitudes;
    }

    /**
     * Gets the altitudes
     *
     * @return ArrayList of Double
     */
    public ArrayList <Double> getAltitudes() {
        return mAltitudes;
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
