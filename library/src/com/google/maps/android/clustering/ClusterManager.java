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

import android.content.Context;
import android.os.AsyncTask;
import android.os.Build;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.Marker;
import com.google.maps.android.MarkerManager;
import com.google.maps.android.clustering.algo.Algorithm;
import com.google.maps.android.clustering.algo.NonHierarchicalDistanceBasedAlgorithm;
import com.google.maps.android.clustering.algo.PreCachingAlgorithmDecorator;
import com.google.maps.android.clustering.view.ClusterRenderer;
import com.google.maps.android.clustering.view.DefaultClusterRenderer;

import java.util.Collection;
import java.util.Set;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Groups many items on a map based on zoom level.
 *
 * <p>ClusterManager should be added to the map as an: <ul> <li>{@link GoogleMap.OnCameraChangeListener}</li> </ul> </ p>
 */
public class ClusterManager<T extends ClusterItem> implements GoogleMap.OnCameraChangeListener {

    private static final String TAG = "ClusterManager";

    private final ReadWriteLock mAlgorithmLock = new ReentrantReadWriteLock();
    private final ReadWriteLock mClusterTaskLock = new ReentrantReadWriteLock();
    private final MarkerManager mMarkerManager;
    private final MarkerManager.MarkerItemCollection<T> mMarkers;
    private final MarkerManager.MarkerCollection mClusterMarkers;

    private Algorithm<T> mAlgorithm;
    private ClusterRenderer<T> mRenderer;
    private GoogleMap mMap;
    private CameraPosition mPreviousCameraPosition;
    private ClusterTask mClusterTask;

    private OnClusterClickListener<T> mOnClusterClickListener;
    private OnClusterInfoWindowClickListener<T> mOnClusterInfoWindowClickListener;

    private final GoogleMap.OnMarkerClickListener mClusterMarkerClickListener = new GoogleMap.OnMarkerClickListener() {
        @Override
        public boolean onMarkerClick(Marker marker) {
            if (mOnClusterClickListener != null && mRenderer != null) {
                return mOnClusterClickListener.onClusterClick(mRenderer.getCluster(marker));
            }

            return false;
        }
    };

    private final GoogleMap.OnInfoWindowClickListener mClusterInfoWindowClickListener = new GoogleMap.OnInfoWindowClickListener() {
        @Override
        public void onInfoWindowClick(Marker marker) {
            if (mOnClusterInfoWindowClickListener != null && mRenderer != null) {
                mOnClusterInfoWindowClickListener.onClusterInfoWindowClick(mRenderer.getCluster(marker));
            }
        }
    };

    private final MarkerManager.MarkerItemCollectionObserver<T> mItemCollectionObserver = new MarkerManager.MarkerItemCollectionObserver<T>() {

        @Override
        public void onItemAdded(MarkerManager.MarkerItemCollection<T> collection, T item) {
            addItem(item);
            cluster();
        }

        @Override
        public void onItemRemoved(MarkerManager.MarkerItemCollection<T> collection, T item) {
            removeItem(item);
            cluster();
        }

        @Override
        public void onCollectionChanged(MarkerManager.MarkerItemCollection<T> collection) {
            setItems(collection.getItems());
            cluster();
        }

        @Override
        public void onCollectionInvalidated(MarkerManager.MarkerItemCollection<T> collection) {
            setItems(null);
            cluster();
        }
    };

    public ClusterManager(Context context, GoogleMap map) {
        this(context, map, new MarkerManager(map));

        map.setOnMarkerClickListener(mMarkerManager);
        map.setOnInfoWindowClickListener(mMarkerManager);
        map.setOnMarkerDragListener(mMarkerManager);
    }

    public ClusterManager(Context context, GoogleMap map, MarkerManager markerManager) {
        this(context, map, markerManager, markerManager.<T>newItemCollection());
    }

    public ClusterManager(Context context, GoogleMap map, MarkerManager markerManager, MarkerManager.MarkerItemCollection<T> markerItemCollection) {
        mMap = map;
        mMarkerManager = markerManager;
        mClusterMarkers = markerManager.newCollection();
        mMarkers = markerItemCollection;
        mAlgorithm = new PreCachingAlgorithmDecorator<>(new NonHierarchicalDistanceBasedAlgorithm<T>());

        mClusterMarkers.setOnMarkerClickListener(mClusterMarkerClickListener);
        mClusterMarkers.setOnInfoWindowClickListener(mClusterInfoWindowClickListener);

        setRenderer(new DefaultClusterRenderer<>(context, map, this));
    }

    public MarkerManager.MarkerItemCollection<T> getMarkerCollection() {
        return mMarkers;
    }

    public MarkerManager.MarkerCollection getClusterMarkerCollection() {
        return mClusterMarkers;
    }

    public MarkerManager getMarkerManager() {
        return mMarkerManager;
    }

    public void setRenderer(ClusterRenderer<T> view) {
        if (mRenderer != null) {
            mMarkers.unregisterObserver(mItemCollectionObserver);
            mClusterMarkers.clear();
            mMarkers.clear();
            mRenderer.onRemove();
        }

        mRenderer = view;

        if (mRenderer == null) {
            throw new NullPointerException(ClusterRenderer.class + " must not be null");
        }

        mRenderer.onAdd();
        mMarkers.registerObserver(mItemCollectionObserver);
        cluster();
    }

    public void setAlgorithm(Algorithm<T> algorithm) {
        mAlgorithmLock.writeLock().lock();
        try {
            if (mAlgorithm != null) {
                algorithm.addItems(mAlgorithm.getItems());
            }
            mAlgorithm = new PreCachingAlgorithmDecorator<T>(algorithm);
        } finally {
            mAlgorithmLock.writeLock().unlock();
        }
        cluster();
    }

    private void addItem(T item) {
        mAlgorithmLock.writeLock().lock();
        try {
            mAlgorithm.addItem(item);
        } finally {
            mAlgorithmLock.writeLock().unlock();
        }

    }

    private void removeItem(T item) {
        mAlgorithmLock.writeLock().lock();
        try {
            mAlgorithm.removeItem(item);
        } finally {
            mAlgorithmLock.writeLock().unlock();
        }

    }

    private void setItems(Collection<T> items) {
        mAlgorithmLock.writeLock().lock();
        try {
            mAlgorithm.clearItems();

            if (items != null) {
                mAlgorithm.addItems(items);
            }
        } finally {
            mAlgorithmLock.writeLock().unlock();
        }

    }

    /**
     * Force a re-cluster.
     */
    public void cluster() {
        mClusterTaskLock.writeLock().lock();
        try {
            // Attempt to cancel the in-flight request.
            if (mClusterTask != null) {
                mClusterTask.cancel(true);
                mClusterTask = null;
            }

            mClusterTask = new ClusterTask();
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
                mClusterTask.execute(mMap.getCameraPosition().zoom);
            } else {
                mClusterTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, mMap.getCameraPosition().zoom);
            }
        } finally {
            mClusterTaskLock.writeLock().unlock();
        }
    }

    /**
     * Might re-cluster.
     *
     * @param cameraPosition
     */
    @Override
    public void onCameraChange(CameraPosition cameraPosition) {
        if (mRenderer instanceof GoogleMap.OnCameraChangeListener) {
            ((GoogleMap.OnCameraChangeListener) mRenderer).onCameraChange(cameraPosition);
        }

        // Don't re-compute clusters if the map has just been panned/tilted/rotated.
        CameraPosition position = mMap.getCameraPosition();
        if (mPreviousCameraPosition != null && mPreviousCameraPosition.zoom == position.zoom) {
            return;
        }
        mPreviousCameraPosition = mMap.getCameraPosition();

        cluster();
    }

    /**
     * Runs the clustering algorithm in a background thread, then re-paints when results come back.
     */
    private class ClusterTask extends AsyncTask<Float, Void, Set<? extends Cluster<T>>> {
        @Override
        protected Set<? extends Cluster<T>> doInBackground(Float... zoom) {
            mAlgorithmLock.readLock().lock();
            try {
                return mAlgorithm.getClusters(zoom[0]);
            } finally {
                mAlgorithmLock.readLock().unlock();
            }
        }

        @Override
        protected void onPostExecute(Set<? extends Cluster<T>> clusters) {
            mRenderer.onClustersChanged(clusters);
        }
    }

    /**
     * Sets a callback that's invoked when a Cluster is tapped. Note: For this listener to function, the ClusterManager must be added as a
     * click listener to the map.
     */
    public void setOnClusterClickListener(OnClusterClickListener<T> listener) {
        mOnClusterClickListener = listener;
    }

    /**
     * Sets a callback that's invoked when a Cluster is tapped. Note: For this listener to function, the ClusterManager must be added as a
     * info window click listener to the map.
     */
    public void setOnClusterInfoWindowClickListener(OnClusterInfoWindowClickListener<T> listener) {
        mOnClusterInfoWindowClickListener = listener;
    }

    /**
     * Called when a Cluster is clicked.
     */
    public interface OnClusterClickListener<T extends ClusterItem> {
        public boolean onClusterClick(Cluster<T> cluster);
    }

    /**
     * Called when a Cluster's Info Window is clicked.
     */
    public interface OnClusterInfoWindowClickListener<T extends ClusterItem> {
        public void onClusterInfoWindowClick(Cluster<T> cluster);
    }
}
