package com.google.maps.android.clustering.view;

import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterItem;
import com.google.maps.android.clustering.ClusterManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The default distributor of items included in a cluster. It distributes the items around the original lat/lng in a given radius.
 *
 * @param <T> Cluster item type.
 */
public class DefaultClusterItemsDistributor<T extends ClusterItem> implements ClusterItemsDistributor<T> {

    private static final double DEFAULT_RADIUS = 0.00003;

    private static final String DEFAULT_DELETE_LIST = "itemsDeleted";

    private static final String DEFAULT_ADDED_LIST = "itemsAdded";

    private ClusterManager<T> mClusterManager;

    private Map<String, List<T>> mItemsCache;

    public DefaultClusterItemsDistributor(ClusterManager<T> clusterManager) {
        mClusterManager = clusterManager;
        mItemsCache = new HashMap<>();
        mItemsCache.put(DEFAULT_ADDED_LIST, new ArrayList<T>());
        mItemsCache.put(DEFAULT_DELETE_LIST, new ArrayList<T>());
    }

    @Override
    public void distribute(Cluster<T> cluster) {
        // relocate the markers around the original markers position
        int counter = 0;
        float rotateFactor = (360 / cluster.getItems().size());

        for (T item : cluster.getItems()) {
            double lat = item.getPosition().latitude + (DEFAULT_RADIUS * Math.cos(++counter * rotateFactor));
            double lng = item.getPosition().longitude + (DEFAULT_RADIUS * Math.sin(counter * rotateFactor));
            T copy = (T) item.copy(lat, lng);

            mClusterManager.removeItem(item);
            mClusterManager.addItem(copy);
            mClusterManager.cluster();

            mItemsCache.get(DEFAULT_ADDED_LIST).add(copy);
            mItemsCache.get(DEFAULT_DELETE_LIST).add(item);
        }
    }

    public void collect() {
        // collect the items
        mClusterManager.removeItems(mItemsCache.get(DEFAULT_ADDED_LIST));
        mClusterManager.addItems(mItemsCache.get(DEFAULT_DELETE_LIST));
        mClusterManager.cluster();

        mItemsCache.get(DEFAULT_ADDED_LIST).clear();
        mItemsCache.get(DEFAULT_DELETE_LIST).clear();
    }
}
