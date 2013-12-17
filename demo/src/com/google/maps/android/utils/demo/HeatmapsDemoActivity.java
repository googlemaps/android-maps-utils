package com.google.maps.android.utils.demo;

import android.view.View;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.heatmaps.HeatmapConstants;
import com.google.maps.android.heatmaps.HeatmapHandler;
import com.google.maps.android.heatmaps.LatLngWrapper;

import org.json.JSONException;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Scanner;

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
        getMap().moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(37, -120), 5));

        mList = new ArrayList<LatLngWrapper>();

        try {
            readItems();
        } catch (JSONException e) {
            Toast.makeText(this, "Problem reading list of markers.", Toast.LENGTH_LONG).show();
        }

        // Make the handler deal with the map
        mHeatmapHandler = new HeatmapHandler(mList, true, getMap());
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

    // https://explore.data.gov/Geography-and-Environment/EPA-FRS-Facilities-Combined-File-CSV-Download-for-/y38d-q6kk
    // 130k points
    private void readItems() throws JSONException {
        double latitude, longitude;
        InputStream inputStream = getResources().openRawResource(R.raw.lat_long_micro);
        Scanner s = new Scanner(inputStream);

        while (s.hasNextDouble()) {
            latitude = s.nextDouble();
            longitude = s.nextDouble();
            mList.add(new LatLngWrapper(new LatLng(latitude, longitude)));
        }
        s.close();
    }

}
