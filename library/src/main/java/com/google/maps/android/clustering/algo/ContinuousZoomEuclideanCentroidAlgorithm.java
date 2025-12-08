/*
 * Copyright 2025 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.maps.android.clustering.algo;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterItem;
import com.google.maps.android.geometry.Bounds;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * A variant of {@link CentroidNonHierarchicalDistanceBasedAlgorithm} that uses
 * continuous zoom scaling and Euclidean distance for clustering.
 *
 * <p>This class overrides {@link #getClusters(float)} to compute
 * clusters with a zoom-dependent radius, while keeping the centroid-based cluster positions.</p>
 *
 * @param <T> the type of cluster item
 */
public class ContinuousZoomEuclideanCentroidAlgorithm<T extends ClusterItem>
        extends CentroidNonHierarchicalDistanceBasedAlgorithm<T> {

    @Override
    public Set<? extends Cluster<T>> getClusters(float zoom) {
        // Continuous zoom — no casting to int
        final double zoomSpecificSpan = getMaxDistanceBetweenClusteredItems() / Math.pow(2, zoom) / 256;

        final Set<QuadItem<T>> visitedCandidates = new HashSet<>();
        final Set<Cluster<T>> results = new HashSet<>();
        final Map<QuadItem<T>, Double> distanceToCluster = new HashMap<>();
        final Map<QuadItem<T>, StaticCluster<T>> itemToCluster = new HashMap<>();

        synchronized (mQuadTree) {
            for (QuadItem<T> candidate : getClusteringItems(mQuadTree, zoom)) {
                if (visitedCandidates.contains(candidate)) {
                    // Candidate is already part of another cluster.
                    continue;
                }

                Bounds searchBounds = createBoundsFromSpan(candidate.getPoint(), zoomSpecificSpan);
                Collection<QuadItem<T>> clusterItems = new ArrayList<>();
                for (QuadItem<T> clusterItem : mQuadTree.search(searchBounds)) {
                    double distance = distanceSquared(clusterItem.getPoint(), candidate.getPoint());
                    double radiusSquared = Math.pow(zoomSpecificSpan / 2, 2);
                    if (distance < radiusSquared) {
                        clusterItems.add(clusterItem);
                    }
                }

                if (clusterItems.size() == 1) {
                    // Only the current marker is in range. Just add the single item to the results.
                    results.add(candidate);
                    visitedCandidates.add(candidate);
                    distanceToCluster.put(candidate, 0d);
                    continue;
                }
                StaticCluster<T> cluster = new StaticCluster<>(candidate.mClusterItem.getPosition());
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

        // Now, apply the centroid logic from CentroidNonHierarchicalDistanceBasedAlgorithm
        Set<StaticCluster<T>> newClusters = new HashSet<>();
        for (Cluster<T> cluster : results) {
            LatLng centroid = computeCentroid(cluster.getItems());
            StaticCluster<T> newCluster = new StaticCluster<>(centroid);
            for (T item : cluster.getItems()) {
                newCluster.add(item);
            }
            newClusters.add(newCluster);
        }

        return newClusters;
    }
}