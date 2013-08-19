package com.google.maps.android.clustering;

import com.google.android.gms.maps.model.LatLng;

import java.util.Collection;

public interface Cluster<T extends ClusterItem> {
    public LatLng getPosition();

    Collection<T> getItems();

    int getSize();
}