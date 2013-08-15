package com.google.maps.android.utils.demo;

import java.io.InputStream;
import java.util.Scanner;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.algo.SimpleDistanceBased;
import com.google.maps.android.utils.demo.model.MyItem;

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
            } catch (JSONException e) {
                Toast.makeText(this, "Problem reading list of markers.", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void readItems(double offset) throws JSONException {
        InputStream inputStream = getResources().openRawResource(R.raw.radar_search);
        String json = new Scanner(inputStream).useDelimiter("\\A").next();

        JSONArray array = new JSONArray(json);

        for (int i = 0; i < array.length(); i++) {
            JSONObject object = array.getJSONObject(i);
            double lat = object.getDouble("lat") + offset;
            double lng = object.getDouble("lng") + offset;

            mClusterManager.addItem(new MyItem(lat, lng));
        }
    }
}