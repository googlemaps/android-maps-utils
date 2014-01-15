package com.google.maps.android.clustering;

import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.Set;

/**
 * Renders clusters.
 */
public interface ClusterRenderer<T extends ClusterItem> {

    /**
     * Called when the view needs to be updated because new clusters need to be displayed.
     * @param clusters the clusters to be displayed.
     */
    void onClustersChanged(Set<? extends Cluster<T>> clusters);

    void onBeforeClusterItemRendered(T item, MarkerOptions markerOptions);

    void onBeforeClusterRendered(Cluster<T> cluster, MarkerOptions markerOptions);

    void onClusterRendered(Cluster<T> cluster, Marker marker);

    void onClusterItemRendered(T clusterItem, Marker marker);

    boolean shouldRenderAsCluster(Cluster cluster);
}