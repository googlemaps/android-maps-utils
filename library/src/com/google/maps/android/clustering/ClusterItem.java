package com.google.maps.android.clustering;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

/**
 * ClusterItem essentially represents a marker on the map.
 */
public interface ClusterItem {

    /**
     * The position of this marker. This must always return the same value.
     */
    LatLng getPosition();

    /**
     * Marker options used to render this marker, when it's not in a cluster.
     */
    MarkerOptions getMarkerOptions();
}