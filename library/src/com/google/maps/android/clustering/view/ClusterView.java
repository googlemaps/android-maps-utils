package com.google.maps.android.clustering.view;

import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterItem;

import java.util.Set;

/**
 * Renders clusters.
 */
public interface ClusterView<T extends ClusterItem> {

    /**
     * Called when the view needs to be updated because new clusters need to be displayed.
     * @param clusters the clusters to be displayed.
     */
    void onClustersChanged(Set<? extends Cluster<T>> clusters);
}