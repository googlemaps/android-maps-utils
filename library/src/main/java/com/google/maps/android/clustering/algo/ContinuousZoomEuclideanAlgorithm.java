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
 * A variant of {@link NonHierarchicalDistanceBasedAlgorithm} that supports:
 * <ul>
 *     <li><strong>Continuous zoom-based clustering</strong> — the clustering radius
 *     changes smoothly with the zoom level, rather than stepping at integer zoom levels.</li>
 *     <li><strong>Euclidean distance metric</strong> — items are clustered based on their
 *     true Euclidean distance in projected map coordinates, instead of relying solely on
 *     rectangular bounds overlap.</li>
 * </ul>
 *
 * <p>This algorithm overrides {@link #getClusters(float)} to calculate clusters using a
 * zoom-dependent span and a circular radius check, improving visual stability during zoom
 * animations and producing clusters that are spatially more uniform.</p>
 *
 * @param <T> the type of cluster item
 */
public class ContinuousZoomEuclideanAlgorithm<T extends ClusterItem>
        extends NonHierarchicalDistanceBasedAlgorithm<T> {

    /**
     * Returns clusters for the given zoom level using continuous zoom scaling and
     * a Euclidean distance threshold.
     *
     * <p>The algorithm works as follows:</p>
     * <ol>
     *     <li>Computes a {@code zoomSpecificSpan} in projected coordinates based on the
     *     current zoom level and the configured maximum clustering distance.</li>
     *     <li>Iterates over unvisited items in the quadtree.</li>
     *     <li>For each candidate item, searches nearby items within a bounding box
     *     derived from {@code zoomSpecificSpan}.</li>
     *     <li>Filters those items by actual Euclidean distance to ensure they fall
     *     within a circular clustering radius.</li>
     *     <li>Creates a {@link StaticCluster} if more than one item is within range,
     *     otherwise treats the item as its own singleton cluster.</li>
     * </ol>
     *
     * @param zoom the current map zoom level (fractional values supported)
     * @return a set of clusters computed with continuous zoom and Euclidean distance
     */
    @Override
    public Set<? extends Cluster<T>> getClusters(float zoom) {
        final double zoomSpecificSpan = getMaxDistanceBetweenClusteredItems()
                / Math.pow(2, zoom) / 256;

        final Set<QuadItem<T>> visitedCandidates = new HashSet<>();
        final Set<Cluster<T>> results = new HashSet<>();
        final Map<QuadItem<T>, Double> distanceToCluster = new HashMap<>();
        final Map<QuadItem<T>, StaticCluster<T>> itemToCluster = new HashMap<>();

        synchronized (mQuadTree) {
            for (QuadItem<T> candidate : getClusteringItems(mQuadTree, zoom)) {
                if (visitedCandidates.contains(candidate)) {
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
                    results.add(candidate);
                    visitedCandidates.add(candidate);
                    distanceToCluster.put(candidate, 0d);
                    continue;
                }

                StaticCluster<T> cluster = new StaticCluster<>(candidate.getPosition());
                results.add(cluster);

                for (QuadItem<T> clusterItem : clusterItems) {
                    Double existingDistance = distanceToCluster.get(clusterItem);
                    double distance = distanceSquared(clusterItem.getPoint(), candidate.getPoint());
                    if (existingDistance != null && existingDistance < distance) {
                        continue;
                    }
                    if (existingDistance != null) {
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
}
