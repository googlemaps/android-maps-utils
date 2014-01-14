package com.google.maps.android.utils.demo;

import android.app.Activity;
import android.graphics.Color;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.TileOverlay;
import com.google.android.gms.maps.model.TileOverlayOptions;
import com.google.maps.android.heatmaps.HeatmapConstants;
import com.google.maps.android.heatmaps.HeatmapTileProvider;
import com.google.maps.android.heatmaps.LatLngWrapper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

/**
 * A demo of the Heatmaps library. Demonstates how the HeatmapTileProvider can be used to create
 * a colored map overlay that visualises many points of weighted importance/intensity, with
 * different colors representing areas of high and low concentration/combined intensity of points.
 */
public class HeatmapsDemoActivity extends BaseDemoActivity {

    /**
     * Alternative radius for convolution
     */
    private static final int ALT_HEATMAP_RADIUS = 10;

    /**
     * Alternative opacity of heatmap overlay
     */
    private static final double ALT_HEATMAP_OPACITY = 0.4;

    /**
     * Alternative heatmap gradient (blue -> red)
     * Copied from Javascript version
     */
    private static final int[] ALT_HEATMAP_GRADIENT = {
            Color.argb(0, 0, 255, 255),// transparent
            Color.argb(255 / 3 * 2, 0, 255, 255),
            Color.rgb(0, 191, 255),
            Color.rgb(0, 127, 255),
            Color.rgb(0, 63, 255),
            Color.rgb(0, 0, 255),
            Color.rgb(0, 0, 223),
            Color.rgb(0, 0, 191),
            Color.rgb(0, 0, 159),
            Color.rgb(0, 0, 127),
            Color.rgb(63, 0, 91),
            Color.rgb(127, 0, 63),
            Color.rgb(191, 0, 31),
            Color.rgb(255, 0, 0)
    };

    private static final String TAG = HeatmapsDemoActivity.class.getName();

    private HeatmapTileProvider mProvider;
    private TileOverlay mOverlay;

    private boolean defaultGradient = true;
    private boolean defaultRadius = true;
    private boolean defaultOpacity = true;


    /**
     * Maps name of data set to data (list of LatLngWrappers)
     * Each LatLngWrapper contains a LatLng as well as corresponding intensity value (which
     * represents "importance" of this LatLng) - see the class for more details
     */
    private HashMap<String, ArrayList<LatLngWrapper>> mLists =
            new HashMap<String, ArrayList<LatLngWrapper>>();

    @Override
    protected int getLayoutId() {
        return R.layout.heatmaps_demo;
    }

    @Override
    protected void startDemo() {
        getMap().moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(-25, 135), 3));

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
            mProvider = new HeatmapTileProvider.Builder(
                    mLists.get(getString(R.string.police_stations))).build();
            mOverlay = getMap().addTileOverlay(new TileOverlayOptions().tileProvider(mProvider));
        } catch (IllegalArgumentException e) {
            Log.e("IllegalArgumentException in Builder", e.getMessage());
        }
    }

    public void changeRadius(View view) {
        if (defaultRadius) {
            mProvider.setRadius(ALT_HEATMAP_RADIUS);
        } else {
            mProvider.setRadius(HeatmapConstants.DEFAULT_HEATMAP_RADIUS);
        }
        mOverlay.clearTileCache();
        defaultRadius = !defaultRadius;
    }

    public void changeGradient(View view) {
        if (defaultGradient) {
            mProvider.setGradient(ALT_HEATMAP_GRADIENT);
        } else {
            mProvider.setGradient(HeatmapConstants.DEFAULT_HEATMAP_GRADIENT);
        }
        mOverlay.clearTileCache();
        defaultGradient = !defaultGradient;
    }

    public void changeOpacity(View view) {
        if (defaultOpacity) {
            mProvider.setOpacity(ALT_HEATMAP_OPACITY);
        } else {
            mProvider.setOpacity(HeatmapConstants.DEFAULT_HEATMAP_OPACITY);
        }
        mOverlay.clearTileCache();
        defaultOpacity = !defaultOpacity;
    }

    // Dealing with spinner choices
    public class SpinnerActivity implements AdapterView.OnItemSelectedListener {
        public void onItemSelected(AdapterView<?> parent, View view,
                                   int pos, long id) {
            String dataset = parent.getItemAtPosition(pos).toString();
            mProvider.setData(mLists.get(dataset));
            mOverlay.clearTileCache();
        }

        public void onNothingSelected(AdapterView<?> parent) {
            // Another interface callback
        }
    }

    // Datasets:
    // Police Stations: all police stations across Australia from http://poidb.com
    // Red Lights: all red lights across Australia from http://poidb.com
    private ArrayList<LatLngWrapper> readItems(int resource) throws JSONException {
        ArrayList<LatLngWrapper> list = new ArrayList<LatLngWrapper>();
        long start = System.currentTimeMillis();
        InputStream inputStream = getResources().openRawResource(resource);
        String json = new Scanner(inputStream).useDelimiter("\\A").next();
        JSONArray array = new JSONArray(json);
        for (int i = 0; i < array.length(); i++) {
            JSONObject object = array.getJSONObject(i);
            double lat = object.getDouble("lat");
            double lng = object.getDouble("lng");
            list.add(new LatLngWrapper(new LatLng(lat, lng)));
        }

        long end = System.currentTimeMillis();
        Log.d(TAG, "readItems: " + (end - start) + "ms");
        return list;
    }
}
