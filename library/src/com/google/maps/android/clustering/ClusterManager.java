package com.google.maps.android.clustering;

import android.content.Context;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.maps.android.clustering.algo.Algorithm;
import com.google.maps.android.clustering.algo.GridBased;
import com.google.maps.android.clustering.view.ClusterView;
import com.google.maps.android.clustering.view.DefaultView;

import java.util.Set;

@Deprecated // Experimental. Do not use.
public class ClusterManager<T extends ClusterItem> implements GoogleMap.OnCameraChangeListener {
    private static final String TAG = ClusterManager.class.getName();

    private Algorithm<T> mAlgorithm;
    private ClusterView<T> mView;

    private GoogleMap mMap;
    private CameraPosition mPreviousCameraPosition;
    private boolean mShouldCluster = true;

    public ClusterManager(Context context,
                          GoogleMap map) {
        mMap = map;
        mView = new DefaultView<T>(context, map);
        mAlgorithm = new GridBased<T>();
    }

    public void setView(ClusterView<T> view) {
        mView = view;
    }

    public void setAlgorithm(Algorithm<T> algorithm) {
        mAlgorithm = algorithm;
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
        Set<? extends Cluster<T>> clusters = mAlgorithm.getClusters(mMap.getCameraPosition().zoom);
        mView.onClustersChanged(clusters);
        mShouldCluster = false;
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
}