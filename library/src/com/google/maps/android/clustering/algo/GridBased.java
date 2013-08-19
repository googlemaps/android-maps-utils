package com.google.maps.android.clustering.algo;

import android.util.Log;
import android.util.SparseArray;

import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterItem;
import com.google.maps.android.geometry.Point;
import com.google.maps.android.projection.SphericalMercatorProjection;

import java.util.HashSet;
import java.util.Set;

public class GridBased<T extends ClusterItem> implements Algorithm<T> {
    private static final String TAG = GridBased.class.getName();
    Set<T> mItems = new HashSet<T>();

    @Override
    public void addItem(T item) {
        mItems.add(item);
    }

    @Override
    public void removeItem(T item) {
        mItems.remove(item);
    }

    @Override
    public Set<? extends Cluster<T>> getClusters(double zoom) {
        int numCells = (int) Math.ceil(256 * Math.pow(2, zoom) / 100);
        Log.d(TAG, "zoom: " + zoom + " | numCells: " + numCells);
        SphericalMercatorProjection proj = new SphericalMercatorProjection(numCells);

        HashSet<Cluster<T>> clusters = new HashSet<Cluster<T>>();
        SparseArray<StaticCluster<T>> sparseArray = new SparseArray<StaticCluster<T>>();

        for (T item : mItems) {
            Point p = proj.toPoint(item.getPosition());

            int coord = getCoord(numCells, p.x, p.y);

            StaticCluster<T> cluster = sparseArray.get(coord);
            Log.d(TAG, String.format("coord: %d, cluster: %s", coord, cluster));
            if (cluster == null) {
                cluster = new StaticCluster<T>(proj.toLatLng(new Point(Math.floor(p.x) + .5, Math.floor(p.y) + .5)));
                sparseArray.put(coord, cluster);
                clusters.add(cluster);
            }
            cluster.add(item);
        }

        return clusters;
    }

    private static int getCoord(long numCells, double x, double y) {
        return (int) (numCells * Math.floor(x) + Math.floor(y));
    }
}