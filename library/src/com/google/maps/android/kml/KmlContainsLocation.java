package com.google.maps.android.kml;


import com.google.android.gms.maps.model.LatLng;

public interface KmlContainsLocation {
    /**
     * Checks if the given location lies inside.
     *
     */
    public boolean containsLocation(LatLng point, boolean geodesic);
}
