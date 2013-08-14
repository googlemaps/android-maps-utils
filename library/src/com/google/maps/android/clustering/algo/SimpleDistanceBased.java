package com.google.maps.android.clustering.algo;

import android.util.SparseArray;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterItem;
import com.google.maps.android.geometry.Bounds;
import com.google.maps.android.geometry.Point;
import com.google.maps.android.projection.SphericalMercatorProjection;
import com.google.maps.android.quadtree.PointQuadTree;

import java.lang.ref.SoftReference;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * A simple clustering algorithm. Simply:
 * 1. Iterate over items in the order they were added (candidate clusters).
 * 2. Create a cluster with the center of the item.
 * 3. Add all items that are within a certain distance to the cluster.
 * 4. Remove those items from the list of candidate clusters.
 *
 * The zoom level is quantized to a discrete zoom level (integer).
 * This means that items that were added first will tend to have more items in their cluster.
 * Clusters have the center of the first element (not the centroid of the items within it).
 */
public class SimpleDistanceBased<T extends ClusterItem> implements Algorithm<T> {
    public static final int MAX_DISTANCE_AT_ZOOM = 100;

    private final LinkedHashSet<QuadItem<T>> mItems = new LinkedHashSet<QuadItem<T>>();
    private final PointQuadTree<QuadItem<T>> mQuadTree = new PointQuadTree<QuadItem<T>>(0, 1, 0, 1);
    private static final SphericalMercatorProjection PROJECTION = new SphericalMercatorProjection(1);
    private SparseArray<SoftReference<Set>> cachedClusters = new SparseArray<SoftReference<Set>>(0); // TOOD: use LruCache

    @Override
    public void addItem(T item) {
        QuadItem<T> quadItem = new QuadItem<T>(item);
        mItems.add(quadItem);
        mQuadTree.add(quadItem);
    }

    @Override
    public void removeItem(T item) {
        QuadItem<T> quadItem = new QuadItem<T>(item);
        mItems.remove(quadItem);
        mQuadTree.remove(quadItem);
    }

    @Override
    public Set<? extends Cluster<T>> getClusters(double zoom) {
        int discreteZoom = (int) zoom;

        if (cachedClusters.get(discreteZoom) != null) {
            Set clusters = cachedClusters.get(discreteZoom).get();
            if (clusters != null) {
                return clusters;
            }
        }

        double zoomSpecificSpan = MAX_DISTANCE_AT_ZOOM / Math.pow(2, discreteZoom) / 256;

        Set<QuadItem<T>> visitedCandidates = new HashSet<QuadItem<T>>();
        HashSet<StaticCluster<T>> results = new HashSet<StaticCluster<T>>();

        // TODO: Investigate use of ConcurrentSkipListSet.
        for (QuadItem<T> candidate : mItems) {
            if (visitedCandidates.contains(candidate)) continue;

            Bounds searchBounds = createBoundsFromSpan(candidate.getPoint(), zoomSpecificSpan);
            Set<QuadItem<T>> clusterItems = mQuadTree.search(searchBounds);
            // Don't add points twice.
            // TODO: Move the point to the closest cluster.
            clusterItems.removeAll(visitedCandidates);
            visitedCandidates.addAll(clusterItems);
            results.add(createCluster(candidate.mClusterItem.getPosition(), clusterItems));
        }
        cachedClusters.put(discreteZoom, new SoftReference<Set>(results));
        return results;
    }

    private Bounds createBoundsFromSpan(Point p, double span) {
        double halfSpan = span / 2;
        return new Bounds(
                p.x - halfSpan, p.x + halfSpan,
                p.y - halfSpan, p.y + halfSpan);
    }

    private StaticCluster<T> createCluster(LatLng position, Set<QuadItem<T>> clusterItems) {
        StaticCluster<T> cluster = new StaticCluster<T>(position);
        for (QuadItem<T> item : clusterItems) {
            cluster.add(item.mClusterItem);
        }
        return cluster;
    }

    private static class QuadItem<T extends ClusterItem> implements PointQuadTree.Item {
        private final T mClusterItem;
        private final Point mPoint;

        private QuadItem(T item) {
            mClusterItem = item;
            mPoint = PROJECTION.toPoint(item.getPosition());
        }

        @Override
        public Point getPoint() {
            return mPoint;
        }
    }
}
