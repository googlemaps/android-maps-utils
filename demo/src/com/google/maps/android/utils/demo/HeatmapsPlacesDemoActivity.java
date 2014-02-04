package com.google.maps.android.utils.demo;

import android.content.Context;
import android.graphics.Color;
import android.os.AsyncTask;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Hashtable;

import com.google.android.gms.maps.model.TileOverlay;
import com.google.android.gms.maps.model.TileOverlayOptions;
import com.google.maps.android.SphericalUtil;
import com.google.maps.android.heatmaps.Gradient;
import com.google.maps.android.heatmaps.HeatmapTileProvider;

public class HeatmapsPlacesDemoActivity extends BaseDemoActivity {

    private GoogleMap mMap = null;

    private final LatLng SYDNEY = new LatLng(-33.873651, 151.2058896);

    private static final String LOG_TAG = "HeatmapsDemoApp";

    private static final String PLACES_API_BASE = "https://maps.googleapis.com/maps/api/place";
    private static final String TYPE_RADAR_SEARCH = "/radarsearch";
    private static final String OUT_JSON = "/json";
    // server API key
    // TODO : remove
    private static final String API_KEY = "AIzaSyDzEnwjg6wwrkrLLBUr0TEWfS8O5Dt1NEA";

    //Red, Blue, Green, Purple, Orange
    private static final int[] COLORS = {
            Color.rgb(238, 44, 44),    // red
            Color.rgb(60, 80, 255),    // blue
            Color.rgb(20, 170, 50),    // green
            Color.rgb(255, 80, 255),   // pink
            Color.rgb(100, 100, 100)}; // grey
    private static final int MAX_CHECKBOXES = COLORS.length;

    public static final int SEARCH_RADIUS = 8000;

    private Hashtable<String, TileOverlay> mOverlays = new Hashtable<String, TileOverlay>();

    private ArrayList<String> mKeywords;

    private LinearLayout mCheckboxLayout;

    private Context mContext;

    private int mOverlayCount = 0;

    @Override
    protected int getLayoutId() {
        return R.layout.places_demo;
    }

    @Override
    protected void startDemo() {
        EditText editText = (EditText) findViewById(R.id.input_text);
        editText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
                boolean handled = false;
                if (actionId == EditorInfo.IME_NULL) {
                    submit();
                    handled = true;
                }
                return handled;
            }
        });

        mContext = this;

        mCheckboxLayout = (LinearLayout) findViewById(R.id.checkboxes);

        mKeywords = new ArrayList<String>();

        setUpMap();
    }

    private void setUpMap() {
        if (mMap == null) {
            mMap = getMap();
            if (mMap != null) {
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(SYDNEY, 11));
                // Add a circle around Sydney to roughly encompass the results
                mMap.addCircle(new CircleOptions()
                        .center(SYDNEY)
                        .radius(SEARCH_RADIUS * 1.2)
                        .strokeColor(Color.RED)
                        .strokeWidth(4));
            }
        }
    }

    /**
     * Called when a search query is submitted
     */
    public void submit() {
        EditText editText = (EditText) findViewById(R.id.input_text);
        String keyword = editText.getText().toString();
        if (mKeywords.contains(keyword)) {
            Toast.makeText(mContext, "This keyword has already been inputted :(", Toast.LENGTH_SHORT).show();
        } else if (mOverlayCount == MAX_CHECKBOXES) {
            Toast.makeText(mContext, "You can only input " + MAX_CHECKBOXES + " keywords. :(", Toast.LENGTH_SHORT).show();
        } else {
            mKeywords.add(keyword);
            ProgressBar progressBar = (ProgressBar) findViewById(R.id.progress_bar);
            progressBar.setVisibility(View.VISIBLE);
            new MakeOverlay().execute(keyword);
            editText.setText("");

            InputMethodManager imm = (InputMethodManager) getSystemService(
                    Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);
        }
    }

    private Collection<LatLng> getPoints(String keyword) {
        Log.d(LOG_TAG, "getting points for " + keyword);
        long start = System.currentTimeMillis();
        HashMap<String, LatLng> results = new HashMap<String, LatLng>();

        // create four points to centre points around - so that four separate requests will be done
        ArrayList<LatLng> searchCenters = new ArrayList<LatLng>(4);
        for (int heading = 45; heading < 360; heading += 90) {
            searchCenters.add(SphericalUtil.computeOffset(SYDNEY, SEARCH_RADIUS / 2, heading));
        }

        for (int j = 0; j < 4; j++) {
            StringBuilder jsonResults = getJsonPlaces(keyword, searchCenters.get(j));
            try {
                // Create a JSON object hierarchy from the results
                JSONObject jsonObj = new JSONObject(jsonResults.toString());
                JSONArray pointsJsonArray = jsonObj.getJSONArray("results");

                Log.e("json results for " + keyword, "" + pointsJsonArray.length());

                // Extract the Place descriptions from the results
                for (int i = 0; i < pointsJsonArray.length(); i++) {
                    if (!results.containsKey(pointsJsonArray.getJSONObject(i).getString("id"))) {
                        JSONObject location = pointsJsonArray.getJSONObject(i)
                                .getJSONObject("geometry").getJSONObject("location");
                        results.put(pointsJsonArray.getJSONObject(i).getString("id"),
                                new LatLng(location.getDouble("lat"),
                                        location.getDouble("lng")));
                    }
                }
            } catch (JSONException e) {
                Log.e(LOG_TAG, "Cannot process JSON results", e);
            }
        }
        Log.d(LOG_TAG, "getPoints time = " + (System.currentTimeMillis() - start));
        return results.values();
    }

    private StringBuilder getJsonPlaces(String keyword, LatLng location) {
        HttpURLConnection conn = null;
        StringBuilder jsonResults = new StringBuilder();
        try {
            StringBuilder sb = new StringBuilder(PLACES_API_BASE + TYPE_RADAR_SEARCH + OUT_JSON);
            sb.append("?location=" + location.latitude + "," + location.longitude);
            sb.append("&radius=5000");
            sb.append("&sensor=false");
            sb.append("&key=" + API_KEY);
            sb.append("&keyword=" + keyword.replace(" ", "%20"));

            URL url = new URL(sb.toString());
            conn = (HttpURLConnection) url.openConnection();
            InputStreamReader in = new InputStreamReader(conn.getInputStream());

            // Load the results into a StringBuilder
            int read;
            char[] buff = new char[1024];
            while ((read = in.read(buff)) != -1) {
                jsonResults.append(buff, 0, read);
            }
        } catch (MalformedURLException e) {
            Log.e(LOG_TAG, "Error processing Places API URL", e);
            return null;
        } catch (IOException e) {
            Log.e(LOG_TAG, "Error connecting to Places API", e);
            return null;
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
        return jsonResults;
    }

    /**
     * Creates check box for a given search term
     *
     * @param keyword the search terms associated with the check box
     */
    private void makeCheckBox(final String keyword) {
        boolean show = true;

        mCheckboxLayout.setVisibility(View.VISIBLE);

        // Make new checkbox
        CheckBox checkBox = new CheckBox(mContext);
        checkBox.setText(keyword);
        checkBox.setTextColor(COLORS[mOverlayCount]);
        checkBox.setChecked(show);
        checkBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CheckBox c = (CheckBox) view;
                // Text is the keyword
                TileOverlay overlay = mOverlays.get(keyword);
                if (overlay != null) {
                    overlay.setVisible(c.isChecked());
                }
            }
        });
        mCheckboxLayout.addView(checkBox);
    }

    /**
     * Async task, because finding the points cannot be done on the main thread, while adding
     * the overlay must be done on the main thread.
     */
    private class MakeOverlay extends AsyncTask<String, Integer, PointsKeywords> {
        protected PointsKeywords doInBackground(String... keyword) {
            return new PointsKeywords(getPoints(keyword[0]), keyword[0]);
        }

        protected void onPostExecute(PointsKeywords pointsKeywords) {
            long start = System.currentTimeMillis();
            Collection<LatLng> points = pointsKeywords.points;
            String keyword = pointsKeywords.keyword;
            Log.e("keyword", keyword);

            // Check that it wasn't an empty query.
            if (!points.isEmpty()) {
                if (mOverlays.size() < MAX_CHECKBOXES) {
                    makeCheckBox(keyword);
                    HeatmapTileProvider provider = new HeatmapTileProvider.Builder()
                            .data(new ArrayList<LatLng>(points))
                            .gradient(makeGradient(COLORS[mOverlayCount]))
                            .build();
                    Log.d(":)", "Using the " + mOverlayCount + "th colour");
                    TileOverlay overlay = getMap().addTileOverlay(new TileOverlayOptions().tileProvider(provider));
                    mOverlays.put(keyword, overlay);
                }
                mOverlayCount++;
                if (mOverlayCount == mKeywords.size()) {
                    ProgressBar progressBar = (ProgressBar) findViewById(R.id.progress_bar);
                    progressBar.setVisibility(View.GONE);
                }
                Log.d(LOG_TAG, "Make heatmap time = " + (System.currentTimeMillis() - start));
            } else {
                ProgressBar progressBar = (ProgressBar) findViewById(R.id.progress_bar);
                progressBar.setVisibility(View.GONE);
                Toast.makeText(mContext, "No results for this query :(", Toast.LENGTH_SHORT).show();
                mKeywords.remove(keyword);
            }
        }
    }

    // Because we want to keep both the points and the keywords.
    private class PointsKeywords {
        public Collection<LatLng> points;
        public String keyword;

        public PointsKeywords(Collection<LatLng> points, String keyword) {
            this.points = points;
            this.keyword = keyword;
        }
    }

    private Gradient makeGradient(int color) {
        int[] colors = {color};
        float[] startPoints = {1.0f};
        return new Gradient(colors, startPoints);
    }
}
