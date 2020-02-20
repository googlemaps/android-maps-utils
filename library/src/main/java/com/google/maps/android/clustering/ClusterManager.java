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

package com.google.maps.android.clustering;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.Marker;
import com.google.maps.android.clustering.algo.Algorithm;
import com.google.maps.android.clustering.algo.NonHierarchicalDistanceBasedAlgorithm;
import com.google.maps.android.clustering.algo.PreCachingAlgorithmDecorator;
import com.google.maps.android.clustering.algo.ScreenBasedAlgorithm;
import com.google.maps.android.clustering.algo.ScreenBasedAlgorithmAdapter;
import com.google.maps.android.clustering.view.ClusterRenderer;
import com.google.maps.android.clustering.view.DefaultClusterRenderer;
import com.google.maps.android.collections.MarkerManager;

import android.content.Context;
import android.os.AsyncTask;

import java.util.Collection;
import java.util.Set;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Groups many items on a map based on zoom level.
 * <p/>
 * ClusterManager should be added to the map as an: <ul> <li>{@link com.google.android.gms.maps.GoogleMap.OnCameraIdleListener}</li>
 * <li>{@link com.google.android.gms.maps.GoogleMap.OnMarkerClickListener}</li> </ul>
 */
public class ClusterManager<T extends ClusterItem> implements
        GoogleMap.OnCameraIdleListener,
        GoogleMap.OnMarkerClickListener,
        GoogleMap.OnInfoWindowClickListener {

    private final MarkerManager mMarkerManager;
    private final MarkerManager.Collection mMarkers;
    private final MarkerManager.Collection mClusterMarkers;

    private ScreenBasedAlgorithm<T> mAlgorithm;
    private ClusterRenderer<T> mRenderer;

    private GoogleMap mMap;
    private CameraPosition mPreviousCameraPosition;
    private ClusterTask mClusterTask;
    private final ReadWriteLock mClusterTaskLock = new ReentrantReadWriteLock();

    private OnClusterItemClickListener<T> mOnClusterItemClickListener;
    private OnClusterInfoWindowClickListener<T> mOnClusterInfoWindowClickListener;
    private OnClusterItemInfoWindowClickListener<T> mOnClusterItemInfoWindowClickListener;
    private OnClusterClickListener<T> mOnClusterClickListener;

    public ClusterManager(Context context, GoogleMap map) {
        this(context, map, new MarkerManager(map));
    }

    public ClusterManager(Context context, GoogleMap map, MarkerManager markerManager) {
        mMap = map;
        mMarkerManager = markerManager;
        mClusterMarkers = markerManager.newCollection();
        mMarkers = markerManager.newCollection();
        mRenderer = new DefaultClusterRenderer<>(context, map, this);
        mAlgorithm = new ScreenBasedAlgorithmAdapter<>(new PreCachingAlgorithmDecorator<>(
                new NonHierarchicalDistanceBasedAlgorithm<T>()));

        mClusterTask = new ClusterTask();
        mRenderer.onAdd();
    }

    public MarkerManager.Collection getMarkerCollection() {
        return mMarkers;
    }

    public MarkerManager.Collection getClusterMarkerCollection() {
        return mClusterMarkers;
    }

    public MarkerManager getMarkerManager() {
        return mMarkerManager;
    }

    public void setRenderer(ClusterRenderer<T> renderer) {
        mRenderer.setOnClusterClickListener(null);
        mRenderer.setOnClusterItemClickListener(null);
        mClusterMarkers.clear();
        mMarkers.clear();
        mRenderer.onRemove();
        mRenderer = renderer;
        mRenderer.onAdd();
        mRenderer.setOnClusterClickListener(mOnClusterClickListener);
        mRenderer.setOnClusterInfoWindowClickListener(mOnClusterInfoWindowClickListener);
        mRenderer.setOnClusterItemClickListener(mOnClusterItemClickListener);
        mRenderer.setOnClusterItemInfoWindowClickListener(mOnClusterItemInfoWindowClickListener);
        cluster();
    }

    public void setAlgorithm(Algorithm<T> algorithm) {
        if (algorithm instanceof ScreenBasedAlgorithm) {
            setAlgorithm((ScreenBasedAlgorithm<T>) algorithm);
        } else {
            setAlgorithm(new ScreenBasedAlgorithmAdapter<>(algorithm));
        }
    }

    public void setAlgorithm(ScreenBasedAlgorithm<T> algorithm) {
        algorithm.lock();
        try {
            if (mAlgorithm != null) {
                algorithm.addItems(mAlgorithm.getItems());
            }

            mAlgorithm = algorithm;
        } finally {
            algorithm.unlock();
        }

        if (mAlgorithm.shouldReclusterOnMapMovement()) {
            mAlgorithm.onCameraChange(mMap.getCameraPosition());
        }

        cluster();
    }

    public void setAnimation(boolean animate) {
        mRenderer.setAnimation(animate);
    }

    public ClusterRenderer<T> getRenderer() {
        return mRenderer;
    }

    public Algorithm<T> getAlgorithm() {
        return mAlgorithm;
    }

    /**
     * Removes all items from the cluster manager. After calling this method you must invoke
     * {@link #cluster()} for the map to be cleared.
     */
    public void clearItems() {
        mAlgorithm.lock();
        try {
            mAlgorithm.clearItems();
        } finally {
            mAlgorithm.unlock();
        }
    }

    /**
     * Adds items to clusters. After calling this method you must invoke {@link #cluster()} for the
     * state of the clusters to be updated on the map.
     * @param items items to add to clusters
     * @return true if the cluster manager contents changed as a result of the call
     */
    public boolean addItems(Collection<T> items) {
        mAlgorithm.lock();
        try {
            return mAlgorithm.addItems(items);
        } finally {
            mAlgorithm.unlock();
        }
    }

    /**
     * Adds an item to a cluster. After calling this method you must invoke {@link #cluster()} for
     * the state of the clusters to be updated on the map.
     * @param myItem item to add to clusters
     * @return true if the cluster manager contents changed as a result of the call
     */
    public boolean addItem(T myItem) {
        mAlgorithm.lock();
        try {
            return mAlgorithm.addItem(myItem);
        } finally {
            mAlgorithm.unlock();
        }
    }

    /**
     * Removes items from clusters. After calling this method you must invoke {@link #cluster()} for
     * the state of the clusters to be updated on the map.
     * @param items items to remove from clusters
     * @return true if the cluster manager contents changed as a result of the call
     */
    public boolean removeItems(Collection<T> items) {
        mAlgorithm.lock();
        try {
            return mAlgorithm.removeItems(items);
        } finally {
            mAlgorithm.unlock();
        }
    }

    /**
     * Removes an item from clusters. After calling this method you must invoke {@link #cluster()}
     * for the state of the clusters to be updated on the map.
     * @param item item to remove from clusters
     * @return true if the item was removed from the cluster manager as a result of this call
     */
    public boolean removeItem(T item) {
        mAlgorithm.lock();
        try {
            return mAlgorithm.removeItem(item);
        } finally {
            mAlgorithm.unlock();
        }
    }

    /**
     * Updates an item in clusters. After calling this method you must invoke {@link #cluster()} for
     * the state of the clusters to be updated on the map.
     * @param item item to update in clusters
     * @return true if the item was updated in the cluster manager, false if the item is not
     * contained within the cluster manager and the cluster manager contents are unchanged
     */
    public boolean updateItem(T item) {
        mAlgorithm.lock();
        try {
            return mAlgorithm.updateItem(item);
        } finally {
            mAlgorithm.unlock();
        }
    }

    /**
     * Force a re-cluster on the map. You should call this after adding, removing, updating,
     * or clearing item(s).
     */
    public void cluster() {
        mClusterTaskLock.writeLock().lock();
        try {
            // Attempt to cancel the in-flight request.
            mClusterTask.cancel(true);
            mClusterTask = new ClusterTask();
            mClusterTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, mMap.getCameraPosition().zoom);
        } finally {
            mClusterTaskLock.writeLock().unlock();
        }
    }

    /**
     * Might re-cluster.
     */
    @Override
    public void onCameraIdle() {
        if (mRenderer instanceof GoogleMap.OnCameraIdleListener) {
            ((GoogleMap.OnCameraIdleListener) mRenderer).onCameraIdle();
        }

        mAlgorithm.onCameraChange(mMap.getCameraPosition());

        // delegate clustering to the algorithm
        if (mAlgorithm.shouldReclusterOnMapMovement()) {
            cluster();

            // Don't re-compute clusters if the map has just been panned/tilted/rotated.
        } else if (mPreviousCameraPosition == null || mPreviousCameraPosition.zoom != mMap.getCameraPosition().zoom) {
            mPreviousCameraPosition = mMap.getCameraPosition();
            cluster();
        }
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        return getMarkerManager().onMarkerClick(marker);
    }

    @Override
    public void onInfoWindowClick(Marker marker) {
        getMarkerManager().onInfoWindowClick(marker);
    }

    /**
     * Runs the clustering algorithm in a background thread, then re-paints when results come back.
     */
    private class ClusterTask extends AsyncTask<Float, Void, Set<? extends Cluster<T>>> {
        @Override
        protected Set<? extends Cluster<T>> doInBackground(Float... zoom) {
            mAlgorithm.lock();
            try {
                return mAlgorithm.getClusters(zoom[0]);
            } finally {
                mAlgorithm.unlock();
            }
        }

        @Override
        protected void onPostExecute(Set<? extends Cluster<T>> clusters) {
            mRenderer.onClustersChanged(clusters);
        }
    }

    /**
     * Sets a callback that's invoked when a Cluster is tapped. Note: For this listener to function,
     * the ClusterManager must be added as a click listener to the map.
     */
    public void setOnClusterClickListener(OnClusterClickListener<T> listener) {
        mOnClusterClickListener = listener;
        mRenderer.setOnClusterClickListener(listener);
    }

    /**
     * Sets a callback that's invoked when a Cluster is tapped. Note: For this listener to function,
     * the ClusterManager must be added as a info window click listener to the map.
     */
    public void setOnClusterInfoWindowClickListener(OnClusterInfoWindowClickListener<T> listener) {
        mOnClusterInfoWindowClickListener = listener;
        mRenderer.setOnClusterInfoWindowClickListener(listener);
    }

    /**
     * Sets a callback that's invoked when an individual ClusterItem is tapped. Note: For this
     * listener to function, the ClusterManager must be added as a click listener to the map.
     */
    public void setOnClusterItemClickListener(OnClusterItemClickListener<T> listener) {
        mOnClusterItemClickListener = listener;
        mRenderer.setOnClusterItemClickListener(listener);
    }

    /**
     * Sets a callback that's invoked when an individual ClusterItem's Info Window is tapped. Note: For this
     * listener to function, the ClusterManager must be added as a info window click listener to the map.
     */
    public void setOnClusterItemInfoWindowClickListener(OnClusterItemInfoWindowClickListener<T> listener) {
        mOnClusterItemInfoWindowClickListener = listener;
        mRenderer.setOnClusterItemInfoWindowClickListener(listener);
    }

    /**
     * Called when a Cluster is clicked.
     */
    public interface OnClusterClickListener<T extends ClusterItem> {
        /**
         * Called when cluster is clicked.
         * Return true if click has been handled
         * Return false and the click will dispatched to the next listener
         */
        boolean onClusterClick(Cluster<T> cluster);
    }

    /**
     * Called when a Cluster's Info Window is clicked.
     */
    public interface OnClusterInfoWindowClickListener<T extends ClusterItem> {
        void onClusterInfoWindowClick(Cluster<T> cluster);
    }

    /**
     * Called when an individual ClusterItem is clicked.
     */
    public interface OnClusterItemClickListener<T extends ClusterItem> {
        boolean onClusterItemClick(T item);
    }

    /**
     * Called when an individual ClusterItem's Info Window is clicked.
     */
    public interface OnClusterItemInfoWindowClickListener<T extends ClusterItem> {
        void onClusterItemInfoWindowClick(T item);
    }
}
