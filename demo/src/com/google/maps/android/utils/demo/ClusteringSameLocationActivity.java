package com.google.maps.android.utils.demo;

import android.content.Context;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.MarkerManager;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterItem;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.utils.demo.model.MyItem;

import org.json.JSONException;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class ClusteringSameLocationActivity extends BaseDemoActivity {

    private static final double DEFAULT_RADIUS = 0.00003;

    private static final String DEFAULT_DELETE_LIST = "itemsDeleted";

    private static final String DEFAULT_ADDED_LIST = "itemsAdded";

    private CustomClusterManager<MyItem> mClusterManager;

    private Map<String, List<MyItem>> mItemsCache;

    public ClusteringSameLocationActivity() {
        mItemsCache = new HashMap<>();
        mItemsCache.put(DEFAULT_ADDED_LIST, new ArrayList<MyItem>());
        mItemsCache.put(DEFAULT_DELETE_LIST, new ArrayList<MyItem>());
    }

    @Override
    protected void startDemo() {
        getMap().moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(51.503186, -0.126446), 10));

        mClusterManager = new CustomClusterManager<>(this, getMap());

        getMap().setOnMarkerClickListener(mClusterManager);
        getMap().setOnCameraMoveListener(new GoogleMap.OnCameraMoveListener() {

            @Override
            public void onCameraMove() {

                // get markesr back to the original position if they were relocated
                if (getMap().getCameraPosition().zoom < getMap().getMaxZoomLevel()) {
                    mClusterManager.removeItems(mItemsCache.get(DEFAULT_ADDED_LIST));
                    mClusterManager.addItems(mItemsCache.get(DEFAULT_DELETE_LIST));
                    mClusterManager.cluster();

                    mItemsCache.get(DEFAULT_ADDED_LIST).clear();
                    mItemsCache.get(DEFAULT_DELETE_LIST).clear();
                }
            }
        });

        mClusterManager.setOnClusterClickListener(new ClusterManager.OnClusterClickListener<MyItem>() {

            @Override
            public boolean onClusterClick(Cluster<MyItem> cluster) {
                float maxZoomLevel = getMap().getMaxZoomLevel();
                float currentZoomLevel = getMap().getCameraPosition().zoom;

                // only show markers if users is in the max zoom level
                if (currentZoomLevel != maxZoomLevel) {
                    return false;
                }

                if (!mClusterManager.itemsInSameLocation(cluster)) {
                    return false;
                }

                // relocate the markers around the current markers position
                int counter = 0;
                float rotateFactor = (360 / cluster.getItems().size());
                for (MyItem item : cluster.getItems()) {
                    double lat = item.getPosition().latitude + (DEFAULT_RADIUS * Math.cos(++counter * rotateFactor));
                    double lng = item.getPosition().longitude + (DEFAULT_RADIUS * Math.sin(counter * rotateFactor));
                    MyItem copy = new MyItem(lat, lng, item.getTitle(), item.getSnippet());

                    mClusterManager.removeItem(item);
                    mClusterManager.addItem(copy);
                    mClusterManager.cluster();

                    mItemsCache.get(DEFAULT_ADDED_LIST).add(copy);
                    mItemsCache.get(DEFAULT_DELETE_LIST).add(item);
                }

                return true;
            }
        });

        try {
            readItems();

            mClusterManager.cluster();
        } catch (JSONException e) {
            Toast.makeText(this, "Problem reading list of markers.", Toast.LENGTH_LONG).show();
        }
    }

    private void readItems() throws JSONException {
        InputStream inputStream = getResources().openRawResource(R.raw.markers_same_location);
        List<MyItem> items = new MyItemReader().read(inputStream);

        mClusterManager.addItems(items);
    }

    private class CustomClusterManager<T extends ClusterItem> extends ClusterManager<T> {

        CustomClusterManager(Context context, GoogleMap map) {
            super(context, map);
        }

        boolean itemsInSameLocation(Cluster<T> cluster) {
            LinkedList<T> items = new LinkedList<>(cluster.getItems());
            T item = items.remove(0);

            double longitude = item.getPosition().longitude;
            double latitude = item.getPosition().latitude;

            for (T t : items) {
                if (longitude != t.getPosition().longitude && latitude != t.getPosition().latitude) {
                    return false;
                }
            }

            return true;
        }

        void removeItems(List<T> items) {

            for (T item : items) {
                removeItem(item);
            }
        }
    }
}
