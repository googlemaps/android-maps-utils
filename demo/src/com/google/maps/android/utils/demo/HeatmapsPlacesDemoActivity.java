/*
 * Copyright 2014 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.maps.android.utils.demo;

import android.content.Context;
import android.graphics.Color;
import android.os.AsyncTask;
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
import com.google.android.gms.maps.model.TileOverlay;
import com.google.android.gms.maps.model.TileOverlayOptions;
import com.google.maps.android.SphericalUtil;
import com.google.maps.android.heatmaps.Gradient;
import com.google.maps.android.heatmaps.HeatmapTileProvider;

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

/**
 * A demo of the heatmaps library incorporating radar search from the Google Places API.
 * This demonstrates the usefulness of heatmaps for displaying the distribution of points,
 * as well as demonstrating the various color options and dealing with multiple heatmaps.
 */
public class HeatmapsPlacesDemoActivity extends BaseDemoActivity {

    private GoogleMap mMap = null;

    private final LatLng SYDNEY = new LatLng(-33.873651, 151.2058896);

    /**
     * The base URL for the radar search request.
     */
    private static final String PLACES_API_BASE = "https://maps.googleapis.com/maps/api/place";

    /**
     * The options required for the radar search.
     */
    private static final String TYPE_RADAR_SEARCH = "/radarsearch";
    private static final String OUT_JSON = "/json";

    /**
     * Places API server key.
     */
    private static final String API_KEY = "YOUR_KEY_HERE"; // TODO place your own here!

    /**
     * The colors to be used for the different heatmap layers.
     */
    private static final int[] HEATMAP_COLORS = {
        HeatmapColors.RED.color,
        HeatmapColors.BLUE.color,
        HeatmapColors.GREEN.color,
        HeatmapColors.PINK.color,
        HeatmapColors.GREY.color
    };

    public enum HeatmapColors {
        RED (Color.rgb(238, 44, 44)),
        BLUE (Color.rgb(60, 80, 255)),
        GREEN (Color.rgb(20, 170, 50)),
        PINK (Color.rgb(255, 80, 255)),
        GREY (Color.rgb(100, 100, 100));

        private final int color;
        HeatmapColors(int color) {
            this.color = color;
        }
    }

    private static final int MAX_CHECKBOXES = 5;

    /**
     * The search radius which roughly corresponds to the radius of the results
     * from the radar search in meters.
     */
    public static final int SEARCH_RADIUS = 8000;

    /**
     * Stores the TileOverlay corresponding to each of the keywords that have been searched for.
     */
    private Hashtable<String, TileOverlay> mOverlays = new Hashtable<String, TileOverlay>();

    /**
     * A layout containing checkboxes for each of the heatmaps rendered.
     */
    private LinearLayout mCheckboxLayout;

    /**
     * The number of overlays rendered so far.
     */
    private int mOverlaysRendered = 0;

    /**
     * The number of overlays that have been inputted so far.
     */
    private int mOverlaysInput = 0;

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

        mCheckboxLayout = (LinearLayout) findViewById(R.id.checkboxes);
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
     * Takes the input from the user and generates the required heatmap.
     * Called when a search query is submitted
     */
    public void submit() {
        if ("YOUR_KEY_HERE".equals(API_KEY)) {
            Toast.makeText(this, "Please sign up for a Places API key and add it to HeatmapsPlacesDemoActivity.API_KEY",
                Toast.LENGTH_LONG).show();
            return;
        }
        EditText editText = (EditText) findViewById(R.id.input_text);
        String keyword = editText.getText().toString();
        if (mOverlays.contains(keyword)) {
            Toast.makeText(this, "This keyword has already been inputted :(", Toast.LENGTH_SHORT).show();
        } else if (mOverlaysRendered == MAX_CHECKBOXES) {
            Toast.makeText(this, "You can only input " + MAX_CHECKBOXES + " keywords. :(", Toast.LENGTH_SHORT).show();
        } else if (keyword.length() != 0) {
            mOverlaysInput++;
            ProgressBar progressBar = (ProgressBar) findViewById(R.id.progress_bar);
            progressBar.setVisibility(View.VISIBLE);
            new MakeOverlayTask().execute(keyword);
            editText.setText("");

            InputMethodManager imm = (InputMethodManager) getSystemService(
                    Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);
        }
    }

    /**
     * Makes four radar search requests for the given keyword, then parses the
     * json output and returns the search results as a collection of LatLng objects.
     *
     * @param keyword A string to use as a search term for the radar search
     * @return Returns the search results from radar search as a collection
     * of LatLng objects.
     */
    private Collection<LatLng> getPoints(String keyword) {
        HashMap<String, LatLng> results = new HashMap<String, LatLng>();

        // Calculate four equidistant points around Sydney to use as search centers
        //   so that four searches can be done.
        ArrayList<LatLng> searchCenters = new ArrayList<LatLng>(4);
        for (int heading = 45; heading < 360; heading += 90) {
            searchCenters.add(SphericalUtil.computeOffset(SYDNEY, SEARCH_RADIUS / 2, heading));
        }

        for (int j = 0; j < 4; j++) {
            String jsonResults = getJsonPlaces(keyword, searchCenters.get(j));
            try {
                // Create a JSON object hierarchy from the results
                JSONObject jsonObj = new JSONObject(jsonResults);
                JSONArray pointsJsonArray = jsonObj.getJSONArray("results");

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
                Toast.makeText(this, "Cannot process JSON results", Toast.LENGTH_SHORT).show();
            }
        }
        return results.values();
    }

    /**
     * Makes a radar search request and returns the results in a json format.
     *
     * @param keyword  The keyword to be searched for.
     * @param location The location the radar search should be based around.
     * @return The results from the radar search request as a json
     */
    private String getJsonPlaces(String keyword, LatLng location) {
        HttpURLConnection conn = null;
        StringBuilder jsonResults = new StringBuilder();
        try {
            URL url = new URL(
                    PLACES_API_BASE + TYPE_RADAR_SEARCH + OUT_JSON
                    + "?location=" + location.latitude + "," + location.longitude
                    + "&radius=" + (SEARCH_RADIUS / 2)
                    + "&sensor=false"
                    + "&key=" + API_KEY
                    + "&keyword=" + keyword.replace(" ", "%20")
            );
            conn = (HttpURLConnection) url.openConnection();
            InputStreamReader in = new InputStreamReader(conn.getInputStream());

            // Load the results into a StringBuilder
            int read;
            char[] buff = new char[1024];
            while ((read = in.read(buff)) != -1) {
                jsonResults.append(buff, 0, read);
            }
        } catch (MalformedURLException e) {
            Toast.makeText(this, "Error processing Places API URL", Toast.LENGTH_SHORT).show();
            return null;
        } catch (IOException e) {
            Toast.makeText(this, "Error connecting to Places API", Toast.LENGTH_SHORT).show();
            return null;
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
        return jsonResults.toString();
    }

    /**
     * Creates check box for a given search term
     *
     * @param keyword the search terms associated with the check box
     */
    private void makeCheckBox(final String keyword) {
        mCheckboxLayout.setVisibility(View.VISIBLE);

        // Make new checkbox
        CheckBox checkBox = new CheckBox(this);
        checkBox.setText(keyword);
        checkBox.setTextColor(HEATMAP_COLORS[mOverlaysRendered]);
        checkBox.setChecked(true);
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
    private class MakeOverlayTask extends AsyncTask<String, Integer, PointsKeywords> {
        protected PointsKeywords doInBackground(String... keyword) {
            return new PointsKeywords(getPoints(keyword[0]), keyword[0]);
        }

        protected void onPostExecute(PointsKeywords pointsKeywords) {
            Collection<LatLng> points = pointsKeywords.points;
            String keyword = pointsKeywords.keyword;

            // Check that it wasn't an empty query.
            if (!points.isEmpty()) {
                if (mOverlays.size() < MAX_CHECKBOXES) {
                    makeCheckBox(keyword);
                    HeatmapTileProvider provider = new HeatmapTileProvider.Builder()
                            .data(new ArrayList<LatLng>(points))
                            .gradient(makeGradient(HEATMAP_COLORS[mOverlaysRendered]))
                            .build();
                    TileOverlay overlay = getMap().addTileOverlay(new TileOverlayOptions().tileProvider(provider));
                    mOverlays.put(keyword, overlay);
                }
                mOverlaysRendered++;
                if (mOverlaysRendered == mOverlaysInput) {
                    ProgressBar progressBar = (ProgressBar) findViewById(R.id.progress_bar);
                    progressBar.setVisibility(View.GONE);
                }
            } else {
                ProgressBar progressBar = (ProgressBar) findViewById(R.id.progress_bar);
                progressBar.setVisibility(View.GONE);
                Toast.makeText(HeatmapsPlacesDemoActivity.this, "No results for this query :(", Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * Class to store both the points and the keywords, for use in the MakeOverlayTask class.
     */
    private class PointsKeywords {
        public Collection<LatLng> points;
        public String keyword;

        public PointsKeywords(Collection<LatLng> points, String keyword) {
            this.points = points;
            this.keyword = keyword;
        }
    }

    /**
     * Creates a one colored gradient which varies in opacity.
     *
     * @param color The opaque color the gradient should be.
     * @return A gradient made purely of the given color with different alpha values.
     */
    private Gradient makeGradient(int color) {
        int[] colors = {color};
        float[] startPoints = {1.0f};
        return new Gradient(colors, startPoints);
    }
}
