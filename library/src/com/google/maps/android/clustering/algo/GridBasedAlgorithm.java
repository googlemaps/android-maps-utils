package com.google.maps.android.clustering.algo;

import android.util.SparseArray;

import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterItem;
import com.google.maps.android.geometry.Point;
import com.google.maps.android.projection.SphericalMercatorProjection;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * Groups markers into a grid.
 */
public class GridBasedAlgorithm<T extends ClusterItem> implements Algorithm<T> {
    private static final String TAG = GridBasedAlgorithm.class.getName();
    private static final int GRID_SIZE = 100;
    private final Set<T> mItems = new HashSet<T>();

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
    public Set<? extends Cluster<T>> getClusters(double zoom) {
        int numCells = (int) Math.ceil(256 * Math.pow(2, zoom) / GRID_SIZE);
        SphericalMercatorProjection proj = new SphericalMercatorProjection(numCells);

        HashSet<Cluster<T>> clusters = new HashSet<Cluster<T>>();
        SparseArray<StaticCluster<T>> sparseArray = new SparseArray<StaticCluster<T>>();

        for (T item : mItems) {
            Point p = proj.toPoint(item.getPosition());

            int coord = getCoord(numCells, p.x, p.y);

            StaticCluster<T> cluster = sparseArray.get(coord);
            if (cluster == null) {
                cluster = new StaticCluster<T>(proj.toLatLng(new Point(Math.floor(p.x) + .5, Math.floor(p.y) + .5)));
                sparseArray.put(coord, cluster);
                clusters.add(cluster);
            }
            cluster.add(item);
        }

        return clusters;
    }

    @Override
    public Collection<T> getItems() {
        return mItems;
    }

    private static int getCoord(long numCells, double x, double y) {
        return (int) (numCells * Math.floor(x) + Math.floor(y));
    }
}