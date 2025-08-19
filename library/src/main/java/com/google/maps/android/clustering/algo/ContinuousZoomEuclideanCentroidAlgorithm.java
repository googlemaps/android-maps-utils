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

import com.google.maps.android.clustering.ClusterItem;
import com.google.maps.android.geometry.Bounds;
import com.google.maps.android.quadtree.PointQuadTree;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * A variant of {@link CentroidNonHierarchicalDistanceBasedAlgorithm} that uses
 * continuous zoom scaling and Euclidean distance for clustering.
 *
 * <p>This class overrides {@link #getClusteringItems(PointQuadTree, float)} to compute
 * clusters with a zoom-dependent radius, while keeping the centroid-based cluster positions.</p>
 *
 * @param <T> the type of cluster item
 */
public class ContinuousZoomEuclideanCentroidAlgorithm<T extends ClusterItem>
        extends CentroidNonHierarchicalDistanceBasedAlgorithm<T> {

    @Override
    protected Collection<QuadItem<T>> getClusteringItems(PointQuadTree<QuadItem<T>> quadTree, float zoom) {
        // Continuous zoom — no casting to int
        final double zoomSpecificSpan = getMaxDistanceBetweenClusteredItems() / Math.pow(2, zoom) / 256;

        final Set<QuadItem<T>> visitedCandidates = new HashSet<>();
        final Collection<QuadItem<T>> result = new ArrayList<>();
        synchronized (mQuadTree) {
            for (QuadItem<T> candidate : mItems) {
                if (visitedCandidates.contains(candidate)) continue;

                Bounds searchBounds = createBoundsFromSpan(candidate.getPoint(), zoomSpecificSpan);
                Collection<QuadItem<T>> clusterItems = new ArrayList<>();
                for (QuadItem<T> clusterItem : mQuadTree.search(searchBounds)) {
                    double distance = distanceSquared(clusterItem.getPoint(), candidate.getPoint());
                    double radiusSquared = Math.pow(zoomSpecificSpan / 2, 2);
                    if (distance < radiusSquared) {
                        clusterItems.add(clusterItem);
                    }
                }

                visitedCandidates.addAll(clusterItems);
                result.add(candidate);
            }
        }
        return result;
    }

}
