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

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * A variant of {@link NonHierarchicalDistanceBasedAlgorithm} that clusters items
 * based on distance but assigns cluster positions at the centroid of their items,
 * instead of using the position of a single item as the cluster position.
 *
 * <p>This algorithm overrides {@link #getClusters(float)} to compute a geographic centroid
 * for each cluster and creates {@link StaticCluster} instances positioned at these centroids.
 * This can provide a more accurate visual representation of the cluster location.</p>
 *
 * @param <T> the type of cluster item
 */
public class CentroidNonHierarchicalDistanceBasedAlgorithm<T extends ClusterItem>
        extends NonHierarchicalDistanceBasedAlgorithm<T> {

    /**
     * Computes the centroid (average latitude and longitude) of a collection of cluster items.
     *
     * @param items the collection of cluster items to compute the centroid for
     * @return the centroid {@link LatLng} of the items
     */
    protected LatLng computeCentroid(Collection<T> items) {
        double latSum = 0;
        double lngSum = 0;
        int count = 0;
        for (T item : items) {
            latSum += item.getPosition().latitude;
            lngSum += item.getPosition().longitude;
            count++;
        }
        return new LatLng(latSum / count, lngSum / count);
    }

    /**
     * Returns clusters of items for the given zoom level, with cluster positions
     * set to the centroid of their constituent items rather than the position of
     * any single item.
     *
     * @param zoom the current zoom level
     * @return a set of clusters with centroid positions
     */
    @Override
    public Set<? extends Cluster<T>> getClusters(float zoom) {
        Set<? extends Cluster<T>> originalClusters = super.getClusters(zoom);
        Set<StaticCluster<T>> newClusters = new HashSet<>();

        for (Cluster<T> cluster : originalClusters) {
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
