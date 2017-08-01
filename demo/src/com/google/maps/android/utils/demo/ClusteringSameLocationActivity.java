package com.google.maps.android.utils.demo;

import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.utils.demo.model.MyItem;

import org.json.JSONException;

import java.io.InputStream;
import java.util.List;

public class ClusteringSameLocationActivity extends BaseDemoActivity {

    private ClusterManager<MyItem> mClusterManager;

    @Override
    protected void startDemo() {
        getMap().moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(51.503186, -0.126446), 10));

        mClusterManager = new ClusterManager<>(this, getMap());

        getMap().setOnMarkerClickListener(mClusterManager);
        getMap().setOnCameraMoveListener(mClusterManager);

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
}
