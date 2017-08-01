package com.google.maps.android.clustering.view;

import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterItem;

/**
 * It distributes the items in a cluster.
 */
public interface ClusterItemsDistributor<T extends ClusterItem> {

    /**
     * Proceed with the distribution of the items in a cluster.
     */
    void distribute(Cluster<T> cluster);

    /**
     * Proceed to collect the items back to their previous state.
     */
    void collect();
}
