package com.google.maps.android.utils.demo;

import android.view.View;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.heatmaps.HeatmapConstants;
import com.google.maps.android.heatmaps.HeatmapHandler;
import com.google.maps.android.heatmaps.LatLngWrapper;
import com.google.maps.android.utils.demo.model.MyItem;

import org.json.JSONException;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class HeatmapsDemoActivity extends BaseDemoActivity {

    private HeatmapHandler mHeatmapHandler;

    private boolean defaultGradient = true;
    private boolean defaultRadius = true;
    private boolean defaultOpacity = true;

    /**
     * List of LatLngWrappers
     */
    private ArrayList<LatLngWrapper> mList;

    @Override
    protected int getLayoutId() {
        return R.layout.heatmaps_demo;
    }

    @Override
    protected void startDemo() {
        getMap().moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(51.503186, -0.126446), 10));

        mList = new ArrayList<LatLngWrapper>();

        try {
            readItems();
        } catch (JSONException e) {
            Toast.makeText(this, "Problem reading list of markers.", Toast.LENGTH_LONG).show();
        }

        // Make the handler deal with the map
        mHeatmapHandler = new HeatmapHandler(mList, this, getMap());
    }

    public void changeRadius(View view) {
        if (defaultRadius) {
            mHeatmapHandler.setRadius(HeatmapConstants.ALT_HEATMAP_RADIUS);
            defaultRadius = false;
        } else {
            mHeatmapHandler.setRadius(HeatmapConstants.DEFAULT_HEATMAP_RADIUS);
            defaultRadius = true;
        }
    }

    public void changeGradient(View view) {
        if (defaultGradient) {
            mHeatmapHandler.setGradient(HeatmapConstants.ALT_HEATMAP_GRADIENT);
            defaultGradient = false;
        } else {
            mHeatmapHandler.setGradient(HeatmapConstants.DEFAULT_HEATMAP_GRADIENT);
            defaultGradient = true;
        }
    }

    public void changeOpacity(View view) {
        if (defaultOpacity) {
            mHeatmapHandler.setOpacity(HeatmapConstants.ALT_HEATMAP_OPACITY);
            defaultOpacity = false;
        } else {
            mHeatmapHandler.setOpacity(HeatmapConstants.DEFAULT_HEATMAP_OPACITY);
            defaultOpacity = true;
        }
    }

    // Copied from ClusteringDemoActivity
    private void readItems() throws JSONException {
        InputStream inputStream = getResources().openRawResource(R.raw.radar_search);
        List<MyItem> items = new MyItemReader().read(inputStream);

        int i;
        for (i = 0; i < items.size(); i++) {
            MyItem temp = items.get(i);
            mList.add(new LatLngWrapper(temp.getPosition()));
        }

        for (i = 0; i < 10; i++) {
            double offset = i / 60d;
            for (MyItem item : items) {
                LatLng position = item.getPosition();
                double lat = position.latitude + offset;
                double lng = position.longitude + offset;
                mList.add(new LatLngWrapper(new LatLng(lat, lng)));
                mList.add(new LatLngWrapper(new LatLng(lat, lng - 2 * offset)));
                mList.add(new LatLngWrapper(new LatLng(lat - 2 * offset, lng)));
                mList.add(new LatLngWrapper(new LatLng(lat - 2 * offset, lng - 2 * offset)));
            }
        }

    }

}
