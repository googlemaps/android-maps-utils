package com.google.maps.android.utils.demo;

import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.heatmaps.HeatmapConstants;
import com.google.maps.android.heatmaps.HeatmapHandler;
import com.google.maps.android.heatmaps.LatLngWrapper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Scanner;

/**
 * A demo of the Heatmaps library. Demonstates how the HeatmapHandler can be used to create
 * a coloured map overlay that visualises many points of weighted importance/intensity, with
 * different colours representing areas of high and low concentration/combined intensity of points.
 */
public class HeatmapsDemoActivity extends BaseDemoActivity {

    private HeatmapHandler mHeatmapHandler;

    private boolean defaultGradient = true;
    private boolean defaultRadius = true;
    private boolean defaultOpacity = true;
    private boolean origData = true;

    /**
     * List of LatLngWrappers
     * Each LatLngWrapper contains a LatLng as well as corresponding intensity value (which
     * represents "importance" of this LatLng) - see the class for more detail
     */
    private ArrayList<LatLngWrapper> mList;

    /**
     * Alternative data set
     */
    private ArrayList<LatLngWrapper> mListThinned;

    @Override
    protected int getLayoutId() {
        return R.layout.heatmaps_demo;
    }

    @Override
    protected void startDemo() {
        getMap().moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(34.0522300, -118.2436800), 7));

        mList = new ArrayList<LatLngWrapper>();
        mListThinned = new ArrayList<LatLngWrapper>();

        try {
            readItems();
        } catch (JSONException e) {
            Toast.makeText(this, "Problem reading list of markers.", Toast.LENGTH_LONG).show();
        }

        /* Testing wraparound
        mList.add(new LatLngWrapper(new LatLng(0, -179.99)));
        mList.add(new LatLngWrapper(new LatLng(2, -180)));
        mList.add(new LatLngWrapper(new LatLng(0, 179)));
        mList.add(new LatLngWrapper(new LatLng(1, 179.99))); */

        // Make the handler deal with the map
        // Input: list of LatLngWrappers, minimum and maximum zoom levels to calculate custom
        // intensity from, and the map to draw the heatmap on
        // radius, gradient and opacity not specified, so default are used
        try {
            mHeatmapHandler = new HeatmapHandler.Builder(mList, getMap()).zoom(5, 8).build();
        } catch(IllegalArgumentException e) {
            Log.e("IllegalArgumentException in Builder", e.getMessage());
        }
    }

    public void changeRadius(View view) {
        if (defaultRadius) {
            mHeatmapHandler.setRadius(HeatmapConstants.ALT_HEATMAP_RADIUS);
        }
        else {
            mHeatmapHandler.setRadius(HeatmapConstants.DEFAULT_HEATMAP_RADIUS);
        }
        defaultRadius =!defaultRadius;
    }

    public void changeGradient(View view) {
        if (defaultGradient) {
            mHeatmapHandler.setGradient(HeatmapConstants.ALT_HEATMAP_GRADIENT);
        }
        else {
            mHeatmapHandler.setGradient(HeatmapConstants.DEFAULT_HEATMAP_GRADIENT);
        }
        defaultGradient = !defaultGradient;
    }

    public void changeOpacity(View view) {
        if (defaultOpacity) {
            mHeatmapHandler.setOpacity(HeatmapConstants.ALT_HEATMAP_OPACITY);
        }
        else {
            mHeatmapHandler.setOpacity(HeatmapConstants.DEFAULT_HEATMAP_OPACITY);
        }
        defaultOpacity = !defaultOpacity;
    }

    public void changeData(View view) {
        if (origData) {
            mHeatmapHandler.setData(mListThinned);
        }
        else {
            mHeatmapHandler.setData(mList);
        }
        origData = !origData;

    }

    // https://explore.data.gov/Geography-and-Environment/EPA-FRS-Facilities-Combined-File-CSV-Download-for-/y38d-q6kk
    // 130k points
    private void readItems() throws JSONException {
        long start = getTime();
        InputStream inputStream = getResources().openRawResource(R.raw.latlong_500);
        String json = new Scanner(inputStream).useDelimiter("\\A").next();
        JSONArray array = new JSONArray(json);
        for (int i = 0; i < array.length(); i++) {
            JSONObject object = array.getJSONObject(i);
            double lat = object.getDouble("lat");
            double lng = object.getDouble("lng");
            mList.add(new LatLngWrapper(new LatLng(lat, lng)));
            if (i % 10 == 0) {
                mListThinned.add(new LatLngWrapper(new LatLng(lat, lng)));
            }
        }

        long end = getTime();
        Log.e("Time readItems", (end-start)+"ms");
    }


    private long getTime() {
        return System.currentTimeMillis();
    }

}
