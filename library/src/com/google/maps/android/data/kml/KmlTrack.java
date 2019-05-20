package com.google.maps.android.data.kml;

import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by thorin on 22/02/2017.
 */

public class KmlTrack extends KmlLineString {
    private final ArrayList<Long> mTimestamps;
    private final HashMap<String, String> mProperties;

    /**
     * Creates a new KmlTrack object
     *
     * @param coordinates array of coordinates
     * @param altitudes array of altitudes
     * @param timestamps array of timestamps
     * @param properties hashmap of properties
     */
    public KmlTrack(ArrayList<LatLng> coordinates, ArrayList<Double> altitudes, ArrayList <Long> timestamps, HashMap<String, String> properties) {
        super(coordinates, altitudes);

        this.mTimestamps = timestamps;
        this.mProperties = properties;
    }

    public ArrayList <Long> getTimestamps() {
        return mTimestamps;
    }

    public HashMap<String, String> getProperties() {
        return mProperties;
    }
}
