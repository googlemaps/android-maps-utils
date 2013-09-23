package com.google.maps.android.clustering;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.maps.android.clustering.algo.Algorithm;
import com.google.maps.android.clustering.algo.PreCachingDecorator;
import com.google.maps.android.clustering.algo.SimpleDistanceBased;
import com.google.maps.android.clustering.view.ClusterView;
import com.google.maps.android.clustering.view.DefaultView;

import java.util.Set;

@Deprecated // Experimental. Do not use.
public class ClusterManager<T extends ClusterItem> implements GoogleMap.OnCameraChangeListener {
    private static final String TAG = ClusterManager.class.getName();
    private static final boolean ASYNC = true;

    private Algorithm<T> mAlgorithm;
    private ClusterView<T> mView;

    private GoogleMap mMap;
    private CameraPosition mPreviousCameraPosition;
    private boolean mShouldCluster = true;
    private ClusterTask mClusterTask;

    public ClusterManager(Context context,
                          GoogleMap map) {
        mMap = map;
        mView = new DefaultView<T>(context, map);
        setAlgorithm(new SimpleDistanceBased<T>());
        mClusterTask = new ClusterTask();
    }

    public void setView(ClusterView<T> view) {
        mView = view;
    }

    public void setAlgorithm(Algorithm<T> algorithm) {
        mAlgorithm = new PreCachingDecorator<T>(algorithm);
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
            long start = System.currentTimeMillis();
            Set<? extends Cluster<T>> clusters = mAlgorithm.getClusters(zoom[0]);
            Log.d(TAG, "clustering took " + (System.currentTimeMillis() - start));
            return clusters;
        }

        @Override
        protected void onPostExecute(Set<? extends Cluster<T>> clusters) {
            mView.onClustersChanged(clusters);
        }
    }
}