/*
 * Copyright 2024 Google Inc.
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

import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterItem;
import com.google.maps.android.geometry.Point;
import com.google.maps.android.projection.SphericalMercatorProjection;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import androidx.collection.LongSparseArray;

/**
 * Groups markers into a grid for clustering. This algorithm organizes items into a two-dimensional grid,
 * facilitating the formation of clusters based on proximity within each grid cell. The grid size determines
 * the spatial granularity of clustering, and clusters are created by aggregating items within the same grid cell.
 * <p>
 * The effectiveness of clustering is influenced by the specified grid size, which determines the spatial resolution of the grid.
 * Smaller grid sizes result in more localized clusters, whereas larger grid sizes lead to broader clusters covering larger areas.
 * <p>
 *
 * @param <T> The type of {@link ClusterItem} to be clustered.
 */
public class GridBasedAlgorithm<T extends ClusterItem> extends AbstractAlgorithm<T> {
    private static final int DEFAULT_GRID_SIZE = 100;

    private int mGridSize = DEFAULT_GRID_SIZE;

    private final Set<T> mItems = Collections.synchronizedSet(new HashSet<T>());

    /**
     * Adds an item to the algorithm
     * @param item the item to be added
     * @return true if the algorithm contents changed as a result of the call
     */
    @Override
    public boolean addItem(T item) {
        return mItems.add(item);
    }

    /**
     * Adds a collection of items to the algorithm
     * @param items the items to be added
     * @return true if the algorithm contents changed as a result of the call
     */
    @Override
    public boolean addItems(Collection<T> items) {
        return mItems.addAll(items);
    }

    @Override
    public void clearItems() {
        mItems.clear();
    }

    /**
     * Removes an item from the algorithm
     * @param item the item to be removed
     * @return true if this algorithm contained the specified element (or equivalently, if this
     * algorithm changed as a result of the call).
     */
    @Override
    public boolean removeItem(T item) {
        return mItems.remove(item);
    }

    /**
     * Removes a collection of items from the algorithm
     * @param items the items to be removed
     * @return true if this algorithm contents changed as a result of the call
     */
    @Override
    public boolean removeItems(Collection<T> items) {
        return mItems.removeAll(items);
    }

    /**
     * Updates the provided item in the algorithm
     * @param item the item to be updated
     * @return true if the item existed in the algorithm and was updated, or false if the item did
     * not exist in the algorithm and the algorithm contents remain unchanged.
     */
    @Override
    public boolean updateItem(T item) {
        boolean result;
        synchronized (mItems) {
            result = removeItem(item);
            if (result) {
                // Only add the item if it was removed (to help prevent accidental duplicates on map)
                result = addItem(item);
            }
        }
        return result;
    }

    @Override
    public void setMaxDistanceBetweenClusteredItems(int maxDistance) {
        mGridSize = maxDistance;
    }

    @Override
    public int getMaxDistanceBetweenClusteredItems() {
        return mGridSize;
    }

    @Override
    public Set<? extends Cluster<T>> getClusters(float zoom) {
        long numCells = (long) Math.ceil(256 * Math.pow(2, zoom) / mGridSize);
        SphericalMercatorProjection proj = new SphericalMercatorProjection(numCells);

        HashSet<Cluster<T>> clusters = new HashSet<Cluster<T>>();
        LongSparseArray<StaticCluster<T>> sparseArray = new LongSparseArray<StaticCluster<T>>();

        synchronized (mItems) {
            for (T item : mItems) {
                Point p = proj.toPoint(item.getPosition());

                long coord = getCoord(numCells, p.x, p.y);

                StaticCluster<T> cluster = sparseArray.get(coord);
                if (cluster == null) {
                    cluster = new StaticCluster<T>(proj.toLatLng(new Point(Math.floor(p.x) + .5, Math.floor(p.y) + .5)));
                    sparseArray.put(coord, cluster);
                    clusters.add(cluster);
                }
                cluster.add(item);
            }
        }

        return clusters;
    }

    @Override
    public Collection<T> getItems() {
        return mItems;
    }

    private static long getCoord(long numCells, double x, double y) {
        return (long) (numCells * Math.floor(x) + Math.floor(y));
    }
}
