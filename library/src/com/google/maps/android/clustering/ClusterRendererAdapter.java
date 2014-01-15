package com.google.maps.android.clustering;

import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.Set;

/**
 * A convinces class to extend this class if only some methods it to be used
 */
public abstract class ClusterRendererAdapter<T extends ClusterItem> implements ClusterRenderer<T> {
    @Override
    public void onClustersChanged(Set<? extends Cluster<T>> clusters) {

    }

    @Override
    public void onBeforeClusterItemRendered(T item, MarkerOptions markerOptions) {

    }

    @Override
    public void onBeforeClusterRendered(Cluster<T> cluster, MarkerOptions markerOptions) {

    }

    @Override
    public void onClusterRendered(Cluster<T> cluster, Marker marker) {

    }

    @Override
    public void onClusterItemRendered(T clusterItem, Marker marker) {

    }

    @Override
    public boolean shouldRenderAsCluster(Cluster cluster) {
        return cluster.getSize() >ClusterRendereEngine.DEFAULT_MIN_CLUSTER_SIZE;
    }
}
