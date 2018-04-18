/*
 * Copyright 2013 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.maps.android.clustering.algo;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterItem;
import com.google.maps.android.geometry.Bounds;
import com.google.maps.android.geometry.Point;
import com.google.maps.android.projection.SphericalMercatorProjection;
import com.google.maps.android.quadtree.PointQuadTree;

/**
 * A simple clustering algorithm with O(nlog n) performance. Resulting clusters are not
 * hierarchical.
 * <p/>
 * High level algorithm:<br>
 * 1. Iterate over items in the order they were added (candidate clusters).<br>
 * 2. Create a cluster with the center of the item. <br>
 * 3. Add all items that are within a certain distance to the cluster. <br>
 * 4. Move any items out of an existing cluster if they are closer to another cluster. <br>
 * 5. Remove those items from the list of candidate clusters.
 * <p/>
 * Clusters have the center of the first element (not the centroid of the items within it).
 */
public class NonHierarchicalDistanceBasedAlgorithm<T extends ClusterItem> implements Algorithm<T> {
    private static final int DEFAULT_MAX_DISTANCE_AT_ZOOM = 100; // essentially 100 dp.

    private int mMaxDistance = DEFAULT_MAX_DISTANCE_AT_ZOOM;

    /**
     * Any modifications should be synchronized on mQuadTree.
     */
    private final Collection<QuadItem<T>> mItems = new HashSet<>();

    /**
     * Any modifications should be synchronized on mQuadTree.
     */
    private final PointQuadTree<QuadItem<T>> mQuadTree = new PointQuadTree<QuadItem<T>>(0, 1, 0, 1);

    private static final SphericalMercatorProjection PROJECTION = new SphericalMercatorProjection(1);

    @Override
    public void addItem(T item) {
        final QuadItem<T> quadItem = new QuadItem<T>(item);
        synchronized (mQuadTree) {
            mItems.add(quadItem);
            mQuadTree.add(quadItem);
        }
    }

    @Override
    public void addItems(Collection<T> items) {
        for (T item : items) {
            addItem(item);
        }
    }

    @Override
    public void clearItems() {
        synchronized (mQuadTree) {
            mItems.clear();
            mQuadTree.clear();
        }
    }

    @Override
    public void removeItem(T item) {
        // QuadItem delegates hashcode() and equals() to its item so,
        //   removing any QuadItem to that item will remove the item
        final QuadItem<T> quadItem = new QuadItem<T>(item);
        synchronized (mQuadTree) {
            mItems.remove(quadItem);
            mQuadTree.remove(quadItem);
        }
    }

    @Override
    public Set<? extends Cluster<T>> getClusters(double zoom) {
        final int discreteZoom = (int) zoom;

        final double zoomSpecificSpan = mMaxDistance / Math.pow(2, discreteZoom) / 256;

        final Set<QuadItem<T>> visitedCandidates = new HashSet<QuadItem<T>>();
        final Set<Cluster<T>> results = new HashSet<Cluster<T>>();
        final Map<QuadItem<T>, Double> distanceToCluster = new HashMap<QuadItem<T>, Double>();
        final Map<QuadItem<T>, StaticCluster<T>> itemToCluster = new HashMap<QuadItem<T>, StaticCluster<T>>();

        synchronized (mQuadTree) {
            for (QuadItem<T> candidate : getClusteringItems(mQuadTree, discreteZoom)) {
                if (visitedCandidates.contains(candidate)) {
                    // Candidate is already part of another cluster.
                    continue;
                }

                Bounds searchBounds = createBoundsFromSpan(candidate.getPoint(), zoomSpecificSpan);
                Collection<QuadItem<T>> clusterItems;
                clusterItems = mQuadTree.search(searchBounds);
                if (clusterItems.size() == 1) {
                    // Only the current marker is in range. Just add the single item to the results.
                    results.add(candidate);
                    visitedCandidates.add(candidate);
                    distanceToCluster.put(candidate, 0d);
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
                        // Move item to the closer cluster.
                        itemToCluster.get(clusterItem).remove(clusterItem.mClusterItem);
                    }
                    distanceToCluster.put(clusterItem, distance);
                    cluster.add(clusterItem.mClusterItem);
                    itemToCluster.put(clusterItem, cluster);
                }
                visitedCandidates.addAll(clusterItems);
            }
        }
        return results;
    }

    protected Collection<QuadItem<T>> getClusteringItems(PointQuadTree<QuadItem<T>> quadTree, int discreteZoom) {
        return mItems;
    }

    @Override
    public Collection<T> getItems() {
        final List<T> items = new ArrayList<T>();
        synchronized (mQuadTree) {
            for (QuadItem<T> quadItem : mItems) {
                items.add(quadItem.mClusterItem);
            }
        }
        return items;
    }

    @Override
    public void setMaxDistanceBetweenClusteredItems(int maxDistance) {
        mMaxDistance = maxDistance;
    }

    @Override
    public int getMaxDistanceBetweenClusteredItems() {
        return mMaxDistance;
    }

    private double distanceSquared(Point a, Point b) {
        return (a.x - b.x) * (a.x - b.x) + (a.y - b.y) * (a.y - b.y);
    }

    private Bounds createBoundsFromSpan(Point p, double span) {
        // TODO: Use a span that takes into account the visual size of the marker, not just its
        // LatLng.
        double halfSpan = span / 2;
        return new Bounds(
                p.x - halfSpan, p.x + halfSpan,
                p.y - halfSpan, p.y + halfSpan);
    }

    static class QuadItem<T extends ClusterItem> implements PointQuadTree.Item, Cluster<T> {
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

        @Override
        public int hashCode() {
            return mClusterItem.hashCode();
        }

        @Override
        public boolean equals(Object other) {
            if (!(other instanceof QuadItem<?>)) {
                return false;
            }

            return ((QuadItem<?>) other).mClusterItem.equals(mClusterItem);
        }
    }
}
