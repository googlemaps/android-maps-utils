/*
 * Copyright 2025 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.maps.android.isochrone;

import android.annotation.SuppressLint;
import android.util.Log;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolygonOptions;

import org.json.JSONObject;

import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Provides functionality to compute and display isochrones on a Google Map.
 * <p>
 * An isochrone is a polygon representing the geographic area reachable from
 * an origin point within a specified travel time using a chosen transport mode.
 */
public class IsochroneMapProvider {

    /**
     * Listener interface to notify when isochrone loading starts and finishes.
     */
    public interface LoadingListener {
        /**
         * Called when the isochrone loading process starts.
         */
        void onLoadingStarted();

        /**
         * Called when the isochrone loading process finishes.
         */
        void onLoadingFinished();
    }

    /**
     * Predefined color schemes to visualize isochrones.
     * The colors typically represent increasing travel times from inner (green) to outer (red).
     */
    public enum ColorSchema {
        GREEN_RED(new int[]{
                0xFF00FF00, // Green (innermost)
                0xFFFFFF00, // Yellow
                0xFFFFA500, // Orange
                0xFFFF0000  // Red (outermost)
        });

        private final int[] colors;

        ColorSchema(int[] colors) {
            this.colors = colors;
        }

        /**
         * Returns the array of colors used in this color schema.
         *
         * @return array of ARGB color integers
         */
        public int[] getColors() {
            return colors;
        }
    }

    /**
     * Supported transport modes for travel time calculation.
     */
    public enum TransportMode {
        BICYCLING("bicycling"),
        DRIVING("driving"),
        WALKING("walking"),
        TRANSIT("transit");

        private final String modeName;

        TransportMode(String modeName) {
            this.modeName = modeName;
        }

        /**
         * Returns the transport mode name used in the Google Directions API request.
         *
         * @return transport mode string
         */
        public String getModeName() {
            return modeName;
        }
    }

    /**
     * Internal data class representing a single isochrone polygon with duration and color.
     */
    private static class IsochronePolygon {
        int duration;
        List<LatLng> points;
        int baseColor;

        IsochronePolygon(int duration, List<LatLng> points, int baseColor) {
            this.duration = duration;
            this.points = points;
            this.baseColor = baseColor;
        }
    }

    private static final String TAG = "IsochroneMapProvider";

    /** Number of radial slices used to approximate the isochrone polygon */
    private static final int SLICES = 36;

    /** Maximum number of binary search cycles per slice */
    private static final int MAX_CYCLES = 10;

    /** Minimum precision for radius binary search */
    private static final double EPSILON = 1e-5;

    /** Approximate meters per one degree of latitude/longitude */
    private static final double METERS_PER_DEGREE = 111000.0;

    /** Average meters covered per minute for rough maximum radius estimate */
    private static final double METERS_PER_MINUTE = 250;

    private final GoogleMap map;
    private final String apiKey;
    private final LoadingListener loadingListener;
    private TransportMode transportMode;

    /**
     * Constructs an IsochroneMapProvider.
     *
     * @param map             the GoogleMap instance where isochrones will be drawn
     * @param apiKey          Google Directions API key
     * @param loadingListener listener to notify loading status (can be null)
     * @param transportMode   transport mode for travel time calculations
     */
    public IsochroneMapProvider(GoogleMap map, String apiKey, LoadingListener loadingListener, TransportMode transportMode) {
        this.map = map;
        this.apiKey = apiKey;
        this.loadingListener = loadingListener;
        this.transportMode = transportMode;
    }

    /**
     * Sets the transport mode for travel time calculations.
     *
     * @param transportMode new transport mode
     */
    public void setTransportMode(TransportMode transportMode) {
        this.transportMode = transportMode;
    }

    /**
     * Starts computing and drawing isochrones for the specified origin and durations.
     * Isochrones are drawn in descending order of duration using the provided color schema.
     *
     * @param origin            center point of the isochrones
     * @param durationsInMinutes array of durations (in minutes) to compute isochrones for
     * @param schema            color schema to visualize different durations
     */
    public void drawIsochrones(LatLng origin, int[] durationsInMinutes, ColorSchema schema) {
        int[] sortedDurations = durationsInMinutes.clone();
        java.util.Arrays.sort(sortedDurations);
        reverseArray(sortedDurations); // Draw outermost first for layering

        if (loadingListener != null) {
            loadingListener.onLoadingStarted();
        }

        int[] colors = schema.getColors();
        List<IsochronePolygon> polygons = Collections.synchronizedList(new ArrayList<>());

        ExecutorService executor = Executors.newFixedThreadPool(4);
        for (int i = 0; i < sortedDurations.length; i++) {
            final int minutes = sortedDurations[i];
            final int durationIndex = sortedDurations.length - 1 - i;
            final int baseColor = (durationIndex < colors.length) ? colors[durationIndex] : colors[colors.length - 1];

            executor.execute(() -> {
                List<LatLng> polygon = computeIsochrone(origin, minutes);
                if (!polygon.isEmpty()) {
                    polygons.add(new IsochronePolygon(minutes, polygon, baseColor));
                }
            });
        }

        executor.shutdown();

        // Wait for all computations to finish and draw on UI thread
        new Thread(() -> {
            try {
                while (!executor.isTerminated()) {
                    Thread.sleep(100);
                }
                Collections.sort(polygons, (a, b) -> Integer.compare(b.duration, a.duration));

                runOnUiThread(() -> {
                    List<LatLng> previous = null;
                    for (IsochronePolygon poly : polygons) {
                        drawPolygon(poly.points, poly.baseColor, previous);
                        previous = poly.points;
                    }
                    if (loadingListener != null) {
                        loadingListener.onLoadingFinished();
                    }
                });
            } catch (InterruptedException e) {
                Log.e(TAG, "Interrupted while waiting for executor", e);
            }
        }).start();
    }

    /**
     * Computes a single isochrone polygon for a given origin and maximum travel time.
     * Uses a radial binary search approach to find reachable points along each slice.
     *
     * @param origin center point
     * @param minutes maximum travel time in minutes
     * @return list of LatLng points forming a closed polygon (smoothed)
     */
    private List<LatLng> computeIsochrone(LatLng origin, int minutes) {
        final List<LatLng> points = Collections.synchronizedList(new ArrayList<>());
        final int maxTravelTimeSec = minutes * 60;

        ExecutorService executor = Executors.newFixedThreadPool(4);
        for (int slice = 0; slice < SLICES; slice++) {
            final double angleRad = 2 * Math.PI * slice / SLICES;
            executor.execute(() -> {
                try {
                    double minRadius = 0.0;
                    double maxRadius = (METERS_PER_MINUTE * minutes) / METERS_PER_DEGREE;
                    double bestRadius = minRadius;

                    // Binary search to find max reachable radius within travel time limit
                    for (int cycle = 0; cycle < MAX_CYCLES; cycle++) {
                        double midRadius = (minRadius + maxRadius) / 2;
                        double latOffset = midRadius * Math.cos(angleRad);
                        double lngOffset = midRadius * Math.sin(angleRad) / Math.cos(Math.toRadians(origin.latitude));
                        LatLng dest = new LatLng(origin.latitude + latOffset, origin.longitude + lngOffset);
                        int travelTime = getTravelTime(origin, dest);
                        if (travelTime < 0) break; // error fetching travel time
                        if (travelTime <= maxTravelTimeSec) {
                            bestRadius = midRadius;
                            minRadius = midRadius;
                        } else {
                            maxRadius = midRadius;
                        }
                        if ((maxRadius - minRadius) < EPSILON) break;
                    }

                    double finalLatOffset = bestRadius * Math.cos(angleRad);
                    double finalLngOffset = bestRadius * Math.sin(angleRad) / Math.cos(Math.toRadians(origin.latitude));
                    LatLng finalPoint = new LatLng(origin.latitude + finalLatOffset, origin.longitude + finalLngOffset);
                    points.add(finalPoint);
                } catch (Exception e) {
                    Log.e(TAG, "Error computing slice", e);
                }
            });
        }

        executor.shutdown();
        try {
            while (!executor.isTerminated()) {
                Thread.sleep(50);
            }
        } catch (InterruptedException e) {
            Log.e(TAG, "Interrupted while computing isochrone", e);
        }

        if (!points.isEmpty()) {
            // Close polygon by adding first point at end
            points.add(points.get(0));
            // This could work too
            //   points.sort(Comparator.comparingDouble(p ->
            //          Math.atan2(p.latitude - origin.latitude, p.longitude - origin.longitude)));
            // Smooth polygon with Chaikin's algorithm
            return chaikinSmoothing(points, 2);
        }
        return new ArrayList<>();
    }

    /**
     * Draws a polygon on the map with specified fill and stroke colors.
     * Optionally, a hole polygon can be added to create layered isochrones.
     *
     * @param points    list of polygon vertices
     * @param baseColor ARGB base color for the polygon
     * @param hole      polygon points representing a hole (inner polygon), or null if none
     */
    private void drawPolygon(List<LatLng> points, int baseColor, List<LatLng> hole) {
        int fillColor = (baseColor & 0x00FFFFFF) | (0x33 << 24);
        int strokeColor = baseColor | 0xFF000000;
        PolygonOptions options = new PolygonOptions()
                .addAll(points)
                .strokeColor(strokeColor)
                .fillColor(fillColor);
        if (hole != null) {
            options.addHole(hole);
        }
        map.addPolygon(options);
    }

    /**
     * Applies Chaikin's corner-cutting algorithm to smooth a polygon.
     * This algorithm refines the polygon by inserting new points that cut corners.
     *
     * @param input      list of LatLng points forming a closed polygon
     * @param iterations number of smoothing iterations to apply
     * @return new list of LatLng points representing the smoothed polygon
     */
    private List<LatLng> chaikinSmoothing(List<LatLng> input, int iterations) {
        List<LatLng> output = new ArrayList<>(input);
        for (int iter = 0; iter < iterations; iter++) {
            List<LatLng> newPoints = new ArrayList<>();
            for (int i = 0; i < output.size() - 1; i++) {
                LatLng p0 = output.get(i);
                LatLng p1 = output.get(i + 1);
                // Q is 75% from p0 to p1
                LatLng Q = new LatLng(0.75 * p0.latitude + 0.25 * p1.latitude, 0.75 * p0.longitude + 0.25 * p1.longitude);
                // R is 25% from p0 to p1
                LatLng R = new LatLng(0.25 * p0.latitude + 0.75 * p1.latitude, 0.25 * p0.longitude + 0.75 * p1.longitude);
                newPoints.add(Q);
                newPoints.add(R);
            }
            // Close polygon by adding first new point at the end
            newPoints.add(newPoints.get(0));
            output = newPoints;
        }
        return output;
    }

    /**
     * Fetches travel time in seconds between origin and destination points using Google Directions API.
     *
     * @param origin start LatLng
     * @param dest   destination LatLng
     * @return travel time in seconds, or -1 if error occurred
     */
    private int getTravelTime(LatLng origin, LatLng dest) {
        @SuppressLint("DefaultLocale") String urlString = String.format(
                "https://maps.googleapis.com/maps/api/directions/json?origin=%f,%f&destination=%f,%f&mode=%s&key=%s",
                origin.latitude, origin.longitude, dest.latitude, dest.longitude, transportMode.getModeName(), apiKey);
        HttpURLConnection connection = null;
        try {
            URL url = new URL(urlString);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);
            InputStreamReader reader = new InputStreamReader(connection.getInputStream());
            StringBuilder responseBuilder = new StringBuilder();
            int c;
            while ((c = reader.read()) != -1) {
                responseBuilder.append((char) c);
            }
            reader.close();
            JSONObject json = new JSONObject(responseBuilder.toString());
            return json.getJSONArray("routes")
                    .getJSONObject(0)
                    .getJSONArray("legs")
                    .getJSONObject(0)
                    .getJSONObject("duration")
                    .getInt("value");
        } catch (Exception e) {
            Log.e(TAG, "Error getting travel time", e);
            return -1;
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    /**
     * Executor interface to run code on the UI thread.
     * Must be set before calling drawing methods.
     */
    public interface UiThreadExecutor {
        /**
         * Execute a Runnable on the UI thread.
         *
         * @param runnable code to run on UI thread
         */
        void execute(Runnable runnable);
    }

    private UiThreadExecutor uiThreadExecutor;

    /**
     * Sets the executor that runs tasks on the UI thread.
     *
     * @param executor UI thread executor
     */
    public void setUiThreadExecutor(UiThreadExecutor executor) {
        this.uiThreadExecutor = executor;
    }

    /**
     * Runs the given Runnable on the UI thread using the configured executor.
     *
     * @param runnable code to run on UI thread
     */
    private void runOnUiThread(Runnable runnable) {
        if (uiThreadExecutor != null) {
            uiThreadExecutor.execute(runnable);
        } else {
            Log.e(TAG, "UI thread executor not set!");
        }
    }

    /**
     * Helper method to reverse an integer array in place.
     *
     * @param array the array to reverse
     */
    private void reverseArray(int[] array) {
        int left = 0, right = array.length - 1;
        while (left < right) {
            int temp = array[left];
            array[left] = array[right];
            array[right] = temp;
            left++;
            right--;
        }
    }
}
