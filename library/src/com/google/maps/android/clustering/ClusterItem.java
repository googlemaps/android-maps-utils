package com.google.maps.android.clustering;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

/**
 * ClusterItem represents a marker on the map.
 */
public interface ClusterItem {

    /**
     * The position of this marker. This must always return the same value.
     */
    LatLng getPosition();
}