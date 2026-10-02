package com.google.maps.android.clustering

import com.google.android.gms.maps.model.LatLngBounds

/**
 * Functional interface for supplying clusters and individual markers on-demand based on the
 * visible geographic bounding box and camera zoom level.
 *
 * This allows massive datasets (e.g. millions of points) to be queried efficiently from spatial
 * databases, memory-mapped files, or precomputed pyramids without requiring all items to reside
 * in heap memory as [ClusterItem] instances.
 */
public fun interface AreaClusterProvider<T : ClusterItem> {
    /**
     * Returns a collection of [Cluster] instances for the requested geographic area and zoom level.
     *
     * @param bounds Geographic area currently visible (typically includes viewport margin).
     * @param zoom Current map zoom level.
     * @return A collection of [Cluster] items to display. Items with [Cluster.getSize] > 1 are rendered
     *         as cluster badges, while items with [Cluster.getSize] == 1 are rendered as individual markers.
     */
    public fun getClustersForArea(bounds: LatLngBounds, zoom: Float): Collection<Cluster<T>>
}
