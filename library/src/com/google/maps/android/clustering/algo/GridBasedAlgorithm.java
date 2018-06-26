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

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import android.support.v4.util.LongSparseArray;

import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterItem;
import com.google.maps.android.geometry.Point;
import com.google.maps.android.projection.SphericalMercatorProjection;

/**
 * Groups markers into a grid.
 */
public class GridBasedAlgorithm<T extends ClusterItem> implements Algorithm<T> {
    private static final int DEFAULT_GRID_SIZE = 100;

    private int mGridSize = DEFAULT_GRID_SIZE;

    private final Set<T> mItems = Collections.synchronizedSet(new HashSet<T>());

    @Override
    public void addItem(T item) {
        mItems.add(item);
    }

    @Override
    public void addItems(Collection<T> items) {
        mItems.addAll(items);
    }

    @Override
    public void clearItems() {
        mItems.clear();
    }

    @Override
    public void removeItem(T item) {
        mItems.remove(item);
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
    public Set<? extends Cluster<T>> getClusters(double zoom) {
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