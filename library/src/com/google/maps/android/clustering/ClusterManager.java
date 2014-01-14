package com.google.maps.android.clustering;

import android.content.Context;
import android.os.AsyncTask;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.Marker;
import com.google.maps.android.clustering.algo.Algorithm;
import com.google.maps.android.clustering.algo.NonHierarchicalDistanceBasedAlgorithm;
import com.google.maps.android.clustering.algo.PreCachingAlgorithmDecorator;
import com.google.maps.android.clustering.view.ClusterRenderer;
import com.google.maps.android.clustering.view.DefaultClusterRenderer;

import java.util.Collection;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Groups many items on a map based on zoom level.
 * <p/>
 * ClusterManager should be added to the map as an: <ul> <li>{@link com.google.android.gms.maps.GoogleMap.OnCameraChangeListener}</li>
 * <li>{@link com.google.android.gms.maps.GoogleMap.OnMarkerClickListener}</li> </ul>
 */
public class ClusterManager<T extends ClusterItem> implements GoogleMap.OnCameraChangeListener {
    private static final String TAG = ClusterManager.class.getName();

    private final MarkerManager<T> mMarkerManager;

    private Algorithm<T> mAlgorithm;
    private final ReadWriteLock mAlgorithmLock = new ReentrantReadWriteLock();
    private ClusterRenderer<T> mRenderer;

    private GoogleMap mMap;
    private CameraPosition mPreviousCameraPosition;
    private ClusterTask mClusterTask;
    private final ReadWriteLock mClusterTaskLock = new ReentrantReadWriteLock();

    private OnClusterClickListener<T> mOnClusterClickListener;
    private OnClusterItemDragListener<T> mOnClusterItemDragListener;
    private OnClusterItemClickListener<T> mOnClusterItemClickListener;
    private InfoWindowAdapter<T> mInfoWindowAdapter;
    private OnInfoWindowClickListener<T> mOnInfoWindowClickListener;
    private final MapFeedbackController mMapFeedbackController;
    private final Collection<GoogleMap.OnCameraChangeListener> mCameraChangeListeners;


    public ClusterManager(Context context, GoogleMap map) {
        mMap = map;
        mMarkerManager = new MarkerManager(map);
        mRenderer = new DefaultClusterRenderer<T>(context, map, this);
        mAlgorithm = new PreCachingAlgorithmDecorator<T>(new NonHierarchicalDistanceBasedAlgorithm<T>());
        mClusterTask = new ClusterTask();
        mMapFeedbackController = new MapFeedbackController();
        mCameraChangeListeners = new CopyOnWriteArraySet<GoogleMap.OnCameraChangeListener>();
        mMap.setOnCameraChangeListener(this);
//        mMap.setOnMarkerClickListener(mMapFeedbackController);
    }

    public MarkerManager getMarkerManager() {
        return mMarkerManager;
    }

    public void setRenderer(ClusterRenderer<T> view) {
        mMarkerManager.clear();
        mRenderer = view;

        cluster();
    }


    /**
     * Might re-cluster.
     *
     * @param cameraPosition
     */
    @Override
    public void onCameraChange(CameraPosition cameraPosition) {

        for (GoogleMap.OnCameraChangeListener subChangeListener : mCameraChangeListeners) {
            subChangeListener.onCameraChange(cameraPosition);
        }

        // Don't re-compute clusters if the map has just been panned/tilted/rotated.
        if (mPreviousCameraPosition != null && mPreviousCameraPosition.zoom == cameraPosition.zoom) {
            mPreviousCameraPosition = cameraPosition;
            return;
        }
        mPreviousCameraPosition = cameraPosition;

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

    public void clearItems() {
        mAlgorithmLock.writeLock().lock();
        try {
            mAlgorithm.clearItems();
        } finally {
            mAlgorithmLock.writeLock().unlock();
        }
    }

    public void addItems(Collection<T> items) {
        mAlgorithmLock.writeLock().lock();
        try {
            mAlgorithm.addItems(items);
        } finally {
            mAlgorithmLock.writeLock().unlock();
        }

    }

    public void addItem(T myItem) {
        mAlgorithmLock.writeLock().lock();
        try {
            mAlgorithm.addItem(myItem);
        } finally {
            mAlgorithmLock.writeLock().unlock();
        }
    }

    public void removeItem(T item) {
        mAlgorithmLock.writeLock().lock();
        try {
            mAlgorithm.removeItem(item);
        } finally {
            mAlgorithmLock.writeLock().unlock();
        }
    }

    /**
     * Force a re-cluster. You may want to call this after adding new item(s).
     */
    public void cluster() {
        mClusterTaskLock.writeLock().lock();
        try {
            // Attempt to cancel the in-flight request.
            mClusterTask.cancel(true);
            mClusterTask = new ClusterTask();
            mClusterTask.execute(mMap.getCameraPosition().zoom);
        } finally {
            mClusterTaskLock.writeLock().unlock();
        }
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

    public boolean addCameraChangeListeners(GoogleMap.OnCameraChangeListener listener){
        return mCameraChangeListeners.add(listener);
    }

    public boolean removeCameraChangeListeners(GoogleMap.OnCameraChangeListener listener){
        return mCameraChangeListeners.remove(listener);
    }

    public void setOnItemMarkerDragListener(OnClusterItemDragListener<T> listener) {
        this.mOnClusterItemDragListener = listener;
        if(mOnClusterItemDragListener == null){
            mMap.setOnMarkerDragListener(null);
        } else {
            mMap.setOnMarkerDragListener(mMapFeedbackController);
        }
    }

    /**
     * Sets a callback that's invoked when a Cluster is tapped. Note: For this listener to function,
     * the ClusterManager must be added as a click listener to the map.
     */
    public void setOnClusterClickListener(OnClusterClickListener<T> listener) {
        mOnClusterClickListener = listener;
        if(mOnClusterClickListener == null){
            mMap.setOnMarkerClickListener(null);
        } else {
            mMap.setOnMarkerClickListener(mMapFeedbackController);
        }
    }

    /**
     * Sets a callback that's invoked when an individual ClusterItem is tapped. Note: For this
     * listener to function, the ClusterManager must be added as a click listener to the map.
     */
    public void setOnClusterItemClickListener(OnClusterItemClickListener<T> listener) {
        mOnClusterItemClickListener = listener;
        if(mOnClusterItemClickListener == null){
            mMap.setOnMarkerClickListener(null);
        } else {
            mMap.setOnMarkerClickListener(mMapFeedbackController);
        }
    }

    public void setOnInfoWindowClickListener(ClusterManager.OnInfoWindowClickListener<T> listener) {
        this.mOnInfoWindowClickListener = listener;
        if(mOnInfoWindowClickListener == null){
            mMap.setOnInfoWindowClickListener(null);
        } else {
            mMap.setOnInfoWindowClickListener(mMapFeedbackController);
        }
    }

    public void setInfoWindowAdapter(InfoWindowAdapter infoWindowAdapter) {
        this.mInfoWindowAdapter = infoWindowAdapter;
        if(mInfoWindowAdapter == null){
            mMap.setInfoWindowAdapter(null);
        } else {
            mMap.setInfoWindowAdapter(mMapFeedbackController);
        }
    }


    private class MapFeedbackController implements GoogleMap.OnMarkerClickListener, GoogleMap.OnMarkerDragListener, GoogleMap.InfoWindowAdapter, GoogleMap.OnInfoWindowClickListener  {

        // OnMarkerClickListener
        @Override
        public boolean onMarkerClick(Marker marker) {
            T item = mMarkerManager.getItemFor(marker);

            if(mOnClusterItemClickListener != null && item != null){
                item.setPosition(marker.getPosition());
                return mOnClusterItemClickListener.onClusterItemClick(item);
            }

            Cluster<T> cluster = mMarkerManager.getClusterFor(marker);
            if(mOnClusterClickListener != null && cluster != null){
                return mOnClusterClickListener.onClusterClick(cluster);
            }

            return false;
        }

        // OnMarkerDragListener
        @Override
        public void onMarkerDragStart(Marker marker) {
            T item = mMarkerManager.getItemFor(marker);
            item.setPosition(marker.getPosition());
            if(mOnClusterItemDragListener != null && item != null){
                mOnClusterItemDragListener.onClusterItemDragStart(item, marker);
            }
        }

        // OnMarkerDragListener
        @Override
        public void onMarkerDrag(Marker marker) {
            T item = mMarkerManager.getItemFor(marker);
            item.setPosition(marker.getPosition());
            if(mOnClusterItemDragListener != null && item != null){
                mOnClusterItemDragListener.onClusterItemDrag(item, marker);
            }
        }

        // OnMarkerDragListener
        @Override
        public void onMarkerDragEnd(Marker marker) {
            T item = mMarkerManager.getItemFor(marker);
            item.setPosition(marker.getPosition());
            if(mOnClusterItemDragListener != null && item != null){
                mOnClusterItemDragListener.onClusterItemDragEnd(item, marker);
            }
        }

        // InfoWindowAdapter
        public android.view.View getInfoWindow(Marker marker){
            if(mInfoWindowAdapter != null){

                T item = mMarkerManager.getItemFor(marker);
                Cluster<T> cluster = mMarkerManager.getClusterFor(marker);

                if(item !=null){
                    return mInfoWindowAdapter.getInfoWindow(item, marker);
                } else if(cluster !=null){
                    return mInfoWindowAdapter.getInfoWindow(cluster, marker);
                }
            }
            return null;
        }

        // InfoWindowAdapter
        public android.view.View getInfoContents(Marker marker){
            T item = mMarkerManager.getItemFor(marker);
            Cluster<T> cluster = mMarkerManager.getClusterFor(marker);

            if(mInfoWindowAdapter != null){
                if(item !=null){
                    return mInfoWindowAdapter.getInfoContents(item, marker);
                } else if(cluster !=null){
                    return mInfoWindowAdapter.getInfoContents(cluster, marker);
                }
            }
            return null;
        }

        // OnInfoWindowClickListener
        @Override
        public void onInfoWindowClick(Marker marker) {

            if(mOnInfoWindowClickListener != null){
                T item = mMarkerManager.getItemFor(marker);
                Cluster<T> cluster = mMarkerManager.getClusterFor(marker);

                if(item !=null){
                    mOnInfoWindowClickListener.onInfoWindowClick(item, marker);
                } else if(cluster  != null){
                    mOnInfoWindowClickListener.onInfoWindowClick(cluster, marker);
                }
            }
        }
    }

    /**
     * Called when a Cluster is clicked.
     */
    public interface OnClusterClickListener<T extends ClusterItem> {
        public boolean onClusterClick(Cluster<T> cluster);
    }

    /**
     * Called when an individual ClusterItem is clicked.
     */
    public interface OnClusterItemClickListener<T extends ClusterItem> {
        public boolean onClusterItemClick(T item);
    }

    public interface OnClusterItemDragListener<T extends ClusterItem> {
        void onClusterItemDragStart(T item, Marker marker);

        void onClusterItemDrag(T item, Marker marker);

        void onClusterItemDragEnd(T item, Marker marker);
    }

    /**
     * Used to show a custom view when an Cluster or ClusterIten is clicked.
     * If null is returned the the default window will be shown
     * @param <T> the Implementation of ClusterItem
     */
    public interface InfoWindowAdapter<T  extends ClusterItem> {
        android.view.View getInfoWindow(T item, Marker marker);

        android.view.View getInfoWindow(Cluster<T> cluster, Marker marker);

        android.view.View getInfoContents(T item, Marker marker);

        android.view.View getInfoContents(Cluster<T> cluster, Marker marker);

    }

    /**
     * Called when the markers info window is clicked
     * @param <T> the Implementation of ClusterItem
     */
    public interface OnInfoWindowClickListener<T  extends ClusterItem> {
        void onInfoWindowClick(T item, Marker marker);
        void onInfoWindowClick(Cluster<T> cluster, Marker marker);
    }

    public static interface MarkerStorage<T  extends ClusterItem> {
        void putItem(T item, Marker marker);
        void putCluster(Cluster<T> cluster, Marker marker);

        Marker getMarkerFor(T item);
        Marker getMarkerFor(Cluster<T> cluster);

        T getItemFor(Marker marker);
        Cluster<T> getClusterFor(Marker marker);

        void remove(Marker marker);
    }
}