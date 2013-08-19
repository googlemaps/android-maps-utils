package com.google.maps.android.clustering.algo;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterItem;
import com.google.maps.android.geometry.Bounds;
import com.google.maps.android.geometry.Point;
import com.google.maps.android.projection.SphericalMercatorProjection;
import com.google.maps.android.quadtree.PointQuadTree;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * A simple clustering algorithm.
 * <p>
 * High level algorithm:
 * 1. Iterate over items in the order they were added (candidate clusters).<br>
 * 2. Create a cluster with the center of the item. <br>
 * 3. Add all items that are within a certain distance to the cluster. <br>
 * 4. Remove those items from the list of candidate clusters.
 * <p/>
 * This means that items that were added first will tend to have more items in their cluster.
 * Clusters have the center of the first element (not the centroid of the items within it).
 */
public class SimpleDistanceBased<T extends ClusterItem> implements Algorithm<T> {
    public static final int MAX_DISTANCE_AT_ZOOM = 100;

    private final LinkedHashSet<QuadItem<T>> mItems = new LinkedHashSet<QuadItem<T>>();
    private final PointQuadTree<QuadItem<T>> mQuadTree = new PointQuadTree<QuadItem<T>>(0, 1, 0, 1);
    private static final SphericalMercatorProjection PROJECTION = new SphericalMercatorProjection(1);

    @Override
    public void addItem(T item) {
        QuadItem<T> quadItem = new QuadItem<T>(item);
        mItems.add(quadItem);
        mQuadTree.add(quadItem);
    }

    @Override
    public void removeItem(T item) {
        // TODO: delegate QuadItem#hashCode and QuadItem#equals to its item.
        throw new UnsupportedOperationException("SimpleDistanceBased.remove not implemented");
    }

    @Override
    public Set<? extends Cluster<T>> getClusters(double zoom) {
        int discreteZoom = (int) zoom;

        double zoomSpecificSpan = MAX_DISTANCE_AT_ZOOM / Math.pow(2, discreteZoom) / 256;

        Set<QuadItem<T>> visitedCandidates = new HashSet<QuadItem<T>>();
        HashSet<Cluster<T>> results = new HashSet<Cluster<T>>();
        Map<QuadItem<T>, Double> distanceToCluster = new HashMap<QuadItem<T>, Double>();
        Map<QuadItem<T>, StaticCluster<T>> itemToCluster = new HashMap<QuadItem<T>, StaticCluster<T>>();

        // TODO: Investigate use of ConcurrentSkipListSet.
        for (QuadItem<T> candidate : mItems) {
            if (visitedCandidates.contains(candidate)) continue;

            Bounds searchBounds = createBoundsFromSpan(candidate.getPoint(), zoomSpecificSpan);
            Collection<QuadItem<T>> clusterItems = mQuadTree.search(searchBounds);
            if (clusterItems.size() == 1) {
                // Only the current market is in range.
                results.add(candidate);
                visitedCandidates.add(candidate);
                continue;
            }
            StaticCluster<T> cluster = new StaticCluster<T>(candidate.mClusterItem.getPosition());
            results.add(cluster);

            for (QuadItem<T> clusterItem : clusterItems) {
                Double existingDistance = distanceToCluster.get(clusterItem);
                double distance = distanceSquared(clusterItem.getPoint(), candidate.getPoint());
                if (existingDistance != null) {
                    // Item already belongs to another cluster. Check if it's closer to this cluster.
                    if (existingDistance < distance) {
                        continue;
                    }
                    itemToCluster.get(clusterItem).remove(clusterItem.mClusterItem);
                }
                distanceToCluster.put(clusterItem, distance);
                cluster.add(clusterItem.mClusterItem);
                itemToCluster.put(clusterItem, cluster);
            }
            visitedCandidates.addAll(clusterItems);
        }
        return results;
    }

    private double distanceSquared(Point a, Point b) {
        return (a.x - b.x) * (a.x - b.x) + (a.y - b.y) * (a.y - b.y);
    }

    private Bounds createBoundsFromSpan(Point p, double span) {
        double halfSpan = span / 2;
        return new Bounds(
                p.x - halfSpan, p.x + halfSpan,
                p.y - halfSpan, p.y + halfSpan);
    }

    private static class QuadItem<T extends ClusterItem> implements PointQuadTree.Item, Cluster<T> {
        private final T mClusterItem;
        private final Point mPoint;
        private final LatLng mPosition;
        private Set<T> singletonSet;

        private QuadItem(T item) {
            mClusterItem = item;
            mPosition = item.getPosition();
            mPoint = PROJECTION.toPoint(mPosition);
            singletonSet = Collections.singleton(mClusterItem);
        }

        @Override
        public Point getPoint() {
            return mPoint;
        }

        @Override
        public LatLng getPosition() {
            return mPosition;
        }

        @Override
        public Set<T> getItems() {
            return singletonSet;
        }

        @Override
        public int getSize() {
            return 1;
        }
    }
}
