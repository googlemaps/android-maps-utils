package com.google.maps.android.utils.demo;

import android.app.Activity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.heatmaps.HeatmapConstants;
import com.google.maps.android.heatmaps.HeatmapHelper;
import com.google.maps.android.heatmaps.LatLngWrapper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

/**
 * A demo of the Heatmaps library. Demonstates how the HeatmapHandler can be used to create
 * a coloured map overlay that visualises many points of weighted importance/intensity, with
 * different colours representing areas of high and low concentration/combined intensity of points.
 */
public class HeatmapsDemoActivity extends BaseDemoActivity {

    private HeatmapHelper mHeatmapHelper;

    private boolean defaultGradient = true;
    private boolean defaultRadius = true;
    private boolean defaultOpacity = true;
    private boolean origData = true;


    /**
     * Maps name of data set to data (list of LatLngWrappers)
     * Each LatLngWrapper contains a LatLng as well as corresponding intensity value (which
     * represents "importance" of this LatLng) - see the class for more detail
     */
    private HashMap<String, ArrayList<LatLngWrapper>> mLists =
            new HashMap<String, ArrayList<LatLngWrapper>>();

    @Override
    protected int getLayoutId() {
        return R.layout.heatmaps_demo;
    }

    @Override
    protected void startDemo() {
        // getMap().moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(-37.8140000, 144.9633200), 5));
        getMap().moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(-25, 135), 3 ));

        // Set up the spinner/dropdown list
        Spinner spinner = (Spinner) findViewById(R.id.spinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.heatmaps_datasets_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(new SpinnerActivity());

        try {
            mLists.put(getString(R.string.police_stations), readItems(R.raw.policeall));
            mLists.put(getString(R.string.red_lights), readItems(R.raw.redlights));
        } catch (JSONException e) {
            Toast.makeText(this, "Problem reading list of markers.", Toast.LENGTH_LONG).show();
        }

        // Make the handler deal with the map
        // Input: list of LatLngWrappers, minimum and maximum zoom levels to calculate custom
        // intensity from, and the map to draw the heatmap on
        // radius, gradient and opacity not specified, so default are used
        try {
            mHeatmapHelper = new HeatmapHelper.Builder(
                    mLists.get(getString(R.string.police_stations)), getMap()).build();
        } catch(IllegalArgumentException e) {
            Log.e("IllegalArgumentException in Builder", e.getMessage());
        }
    }

    public void changeRadius(View view) {
        if (defaultRadius) {
            mHeatmapHelper.setRadius(HeatmapConstants.ALT_HEATMAP_RADIUS);
        }
        else {
            mHeatmapHelper.setRadius(HeatmapConstants.DEFAULT_HEATMAP_RADIUS);
        }
        defaultRadius =!defaultRadius;
    }

    public void changeGradient(View view) {
        if (defaultGradient) {
            mHeatmapHelper.setGradient(HeatmapConstants.ALT_HEATMAP_GRADIENT);
        }
        else {
            mHeatmapHelper.setGradient(HeatmapConstants.DEFAULT_HEATMAP_GRADIENT);
        }
        defaultGradient = !defaultGradient;
    }

    public void changeOpacity(View view) {
        if (defaultOpacity) {
            mHeatmapHelper.setOpacity(HeatmapConstants.ALT_HEATMAP_OPACITY);
        }
        else {
            mHeatmapHelper.setOpacity(HeatmapConstants.DEFAULT_HEATMAP_OPACITY);
        }
        defaultOpacity = !defaultOpacity;
    }

    // Dealing with spinner choices
    public class SpinnerActivity extends Activity implements AdapterView.OnItemSelectedListener {
        public void onItemSelected(AdapterView<?> parent, View view,
                                   int pos, long id) {
            String dataset = parent.getItemAtPosition(pos).toString();
            mHeatmapHelper.setData(mLists.get(dataset));
        }

        public void onNothingSelected(AdapterView<?> parent) {
            // Another interface callback
        }
    }

    // Datasets:
    // Police Stations: all police stations across Australia from http://poidb.com
    private ArrayList<LatLngWrapper> readItems(int resource) throws JSONException {
        ArrayList<LatLngWrapper> list = new ArrayList<LatLngWrapper>();
        long start = getTime();
        InputStream inputStream = getResources().openRawResource(resource);
        String json = new Scanner(inputStream).useDelimiter("\\A").next();
        JSONArray array = new JSONArray(json);
        for (int i = 0; i < array.length(); i++) {
            JSONObject object = array.getJSONObject(i);
            double lat = object.getDouble("lat");
            double lng = object.getDouble("lng");
            list.add(new LatLngWrapper(new LatLng(lat, lng)));
        }

        long end = getTime();
        Log.e("Time readItems", (end-start)+"ms");
        return list;
    }


    private long getTime() {
        return System.currentTimeMillis();
    }

}
