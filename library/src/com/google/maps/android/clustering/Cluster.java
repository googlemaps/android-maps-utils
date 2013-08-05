package com.google.maps.android.clustering;

import com.google.android.gms.maps.model.LatLng;

import java.util.Set;

public interface Cluster<T extends ClusterItem> {
    public LatLng getPosition();

    Set<T> getItems();

    int getSize();
}