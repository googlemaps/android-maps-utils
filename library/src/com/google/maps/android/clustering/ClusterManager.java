package com.google.maps.android.clustering;

import android.content.Context;
import android.os.AsyncTask;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.maps.android.MarkerManager;
import com.google.maps.android.clustering.algo.Algorithm;
import com.google.maps.android.clustering.algo.PreCachingDecorator;
import com.google.maps.android.clustering.algo.SimpleDistanceBased;
import com.google.maps.android.clustering.view.ClusterView;
import com.google.maps.android.clustering.view.DefaultClusterView;

import java.util.Collection;
import java.util.Set;

/**
 * Groups many items on a map based on zoom level.
 */
public class ClusterManager<T extends ClusterItem> implements GoogleMap.OnCameraChangeListener {
    private static final String TAG = ClusterManager.class.getName();
    private static final boolean ASYNC = true;

    private final MarkerManager mMarkerManager;
    private final MarkerManager.Collection mMarkers;
    private final MarkerManager.Collection mClusterMarkers;

    private Algorithm<T> mAlgorithm;
    private ClusterView<T> mView;

    private GoogleMap mMap;
    private CameraPosition mPreviousCameraPosition;
    private boolean mShouldCluster = true;
    private ClusterTask mClusterTask;
    private OnClusterItemClickListener<T> mOnClusterItemClickListener;
    private OnClusterClickListener<T> mOnClusterClickListener;

    public ClusterManager(Context context, GoogleMap map) {
        this(context, map, new MarkerManager(map));
    }

    public ClusterManager(Context context, GoogleMap map, MarkerManager markerManager) {
        mMap = map;
        mMarkerManager = markerManager;
        mClusterMarkers = markerManager.newCollection();
        mMarkers = markerManager.newCollection();
        mView = new DefaultClusterView<T>(context, map, this);
        setAlgorithm(new SimpleDistanceBased<T>());
        mClusterTask = new ClusterTask();
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

    public void setView(ClusterView<T> view) {
        view.setOnClusterClickListener(null);
        view.setOnClusterItemClickListener(null);
        mClusterMarkers.clear();
        mMarkers.clear();
        mView = view;
        mView.setOnClusterClickListener(mOnClusterClickListener);
        mView.setOnClusterItemClickListener(mOnClusterItemClickListener);
    }

    public void setAlgorithm(Algorithm<T> algorithm) {
        Collection<T> items = null;
        if (mAlgorithm != null) {
            items = mAlgorithm.getItems();
            mAlgorithm.clearItems();
        }

        mAlgorithm = new PreCachingDecorator<T>(algorithm);
        if (items != null) {
            mAlgorithm.addAllItems(items);
        }
        mShouldCluster = true;
    }

    public void clearItems() {
        mAlgorithm.clearItems();
        mShouldCluster = true;
    }

    public void addAllItems(Collection<T> items) {
        mAlgorithm.addAllItems(items);
        mShouldCluster = true;
    }

    public void addItem(T myItem) {
        mAlgorithm.addItem(myItem);
        mShouldCluster = true;
    }

    public void removeItem(T item) {
        mAlgorithm.removeItem(item);
        mShouldCluster = true;
    }

    /**
     * Force a re-cluster.
     */
    public void cluster() {
        mClusterTask.cancel(true);
        mClusterTask = new ClusterTask();
        if (ASYNC) {
            mClusterTask.execute(mMap.getCameraPosition().zoom);
        } else {
            Set<? extends Cluster<T>> clusters = mClusterTask.doInBackground(mMap.getCameraPosition().zoom);
            mClusterTask.onPostExecute(clusters);
        }
    }

    /**
     * Might re-cluster.
     *
     * @param cameraPosition
     */
    @Override
    public void onCameraChange(CameraPosition cameraPosition) {
        // Don't re-compute clusters if the map has just been panned.
        CameraPosition position = mMap.getCameraPosition();
        if (!mShouldCluster && mPreviousCameraPosition != null &&
                mPreviousCameraPosition.zoom == position.zoom && mPreviousCameraPosition.tilt == position.tilt) {
            return;
        }
        mPreviousCameraPosition = mMap.getCameraPosition();

        cluster();
    }

    /**
     * Runs the clustering algorithm in a background thread, then re-paints when results come
     * back..
     */
    private class ClusterTask extends AsyncTask<Float, Void, Set<? extends Cluster<T>>> {
        @Override
        protected Set<? extends Cluster<T>> doInBackground(Float... zoom) {
            mShouldCluster = false;
            return mAlgorithm.getClusters(zoom[0]);
        }

        @Override
        protected void onPostExecute(Set<? extends Cluster<T>> clusters) {
            mView.onClustersChanged(clusters);
        }
    }

    public void setOnClusterClickListener(OnClusterClickListener<T> listener) {
        this.mOnClusterClickListener = listener;
        mView.setOnClusterClickListener(listener);
    }

    public void setOnClusterItemClickListener(OnClusterItemClickListener<T> listener) {
        this.mOnClusterItemClickListener = listener;
        mView.setOnClusterItemClickListener(listener);
    }

    public interface OnClusterClickListener<T extends ClusterItem> {
        public boolean onClusterClick(Cluster<T> cluster);
    }

    public interface OnClusterItemClickListener<T extends ClusterItem> {
        public boolean onClusterItemClick(T item);
    }
}