package com.google.maps.android.utils.demo;

import android.util.JsonReader;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.clustering.ClusterItem;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.algo.SimpleDistanceBased;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class BigClusteringDemoActivity extends BaseDemoActivity {
    private ClusterManager<MyItem> mClusterManager;

    @Override
    protected void startDemo() {
        getMap().moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(51.503186, -0.126446), 10));

        mClusterManager = new ClusterManager<MyItem>(this, getMap());
        mClusterManager.setAlgorithm(new SimpleDistanceBased<MyItem>());

        getMap().setOnCameraChangeListener(mClusterManager);
        for (int i = 0; i < 10; i++) {
            try {
                readItems(i / 60d);
            } catch (IOException e) {
                Toast.makeText(this, "Problem reading list of markers.", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void readItems(double offset) throws IOException {
        InputStream inputStream = getResources().openRawResource(R.raw.radar_search);
        JsonReader reader = new JsonReader(new InputStreamReader(inputStream));

        reader.beginArray();

        while (reader.hasNext()) {
            reader.beginObject();
            double lat = 0, lng = 0;
            while (reader.hasNext()) {
                String name = reader.nextName();
                if ("lat".equals(name)) {
                    lat = reader.nextDouble() + offset;
                } else if ("lng".equals(name)) {
                    lng = reader.nextDouble() + offset;
                }
            }
            mClusterManager.addItem(new MyItem(lat, lng));
            reader.endObject();
        }
        reader.endArray();
    }

    private class MyItem implements ClusterItem {
        private final LatLng mPosition;

        public MyItem(double lat, double lng) {
            mPosition = new LatLng(lat, lng);
        }

        @Override
        public LatLng getPosition() {
            return mPosition;
        }

        @Override
        public MarkerOptions getMarkerOptions() {
            return new MarkerOptions().position(mPosition);
        }
    }
}