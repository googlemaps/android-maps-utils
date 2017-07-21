package com.google.maps.android.utils.demo;

import android.content.Context;
import android.util.Log;
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
import java.util.LinkedList;
import java.util.List;

public class ClusteringSameLocationActivity extends BaseDemoActivity {

    private CustomClusterManager<MyItem> mClusterManager;

    @Override
    protected void startDemo() {
        getMap().moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(51.503186, -0.126446), 10));

        mClusterManager = new CustomClusterManager<>(this, getMap());

        getMap().setOnMarkerClickListener(mClusterManager);

        mClusterManager.setOnClusterClickListener(new ClusterManager.OnClusterClickListener<MyItem>() {

            @Override
            public boolean onClusterClick(Cluster<MyItem> cluster) {
                float maxZoomLevel = getMap().getMaxZoomLevel();
                float currentZoomLevel = getMap().getCameraPosition().zoom;

                // only show markers if users is in the max zoom level
                if (currentZoomLevel != maxZoomLevel) {
                    return false;
                }

                if (!mClusterManager.hasMarkersSameLocation(cluster)) {
                    return false;
                }

                // TODO: show markers included in cluster with the same location

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

    class CustomClusterManager<T extends ClusterItem> extends ClusterManager<T> {

        public CustomClusterManager(Context context, GoogleMap map) {
            super(context, map);
        }

        public CustomClusterManager(Context context, GoogleMap map, MarkerManager markerManager) {
            super(context, map, markerManager);
        }

        public boolean hasMarkersSameLocation(Cluster<T> cluster) {
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
    }
}
