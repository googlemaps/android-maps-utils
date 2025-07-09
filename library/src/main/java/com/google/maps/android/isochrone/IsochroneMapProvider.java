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

public class IsochroneMapProvider {

    public interface LoadingListener {
        void onLoadingStarted();
        void onLoadingFinished();
    }

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

        public int[] getColors() {
            return colors;
        }
    }

    public enum TransportMode {
        BICYCLING("bicycling"),
        DRIVING("driving"),
        WALKING("walking"),
        TRANSIT("transit");

        private final String modeName;

        TransportMode(String modeName) {
            this.modeName = modeName;
        }

        public String getModeName() {
            return modeName;
        }
    }

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
    private static final int SLICES = 36;
    private static final int MAX_CYCLES = 10;
    private static final double EPSILON = 1e-5;
    private static final double METERS_PER_DEGREE = 111000.0;
    private static final double METERS_PER_MINUTE = 250;

    private final GoogleMap map;
    private final String apiKey;
    private final LoadingListener loadingListener;
    private TransportMode transportMode;
    private final TravelTimeFetcher travelTimeFetcher;

    public interface TravelTimeFetcher {
        int fetchTravelTime(LatLng origin, LatLng dest);
    }

    private final UiThreadExecutor uiThreadExecutor;

    public interface UiThreadExecutor {
        void execute(Runnable runnable);
    }

    private final TravelTimeFetcher DEFAULT_TRAVEL_TIME_FETCHER = new TravelTimeFetcher() {
        @Override
        public int fetchTravelTime(LatLng origin, LatLng dest) {
            try {
                @SuppressLint("DefaultLocale") String urlString = String.format(
                        "https://maps.googleapis.com/maps/api/directions/json?origin=%f,%f&destination=%f,%f&mode=%s&key=%s",
                        origin.latitude, origin.longitude,
                        dest.latitude, dest.longitude,
                        transportMode.getModeName(),
                        apiKey);

                HttpURLConnection connection = (HttpURLConnection) new URL(urlString).openConnection();
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
                Log.e(TAG, "Error fetching travel time", e);
                return -1;
            }
        }
    };

    public IsochroneMapProvider(GoogleMap map,
                                String apiKey,
                                LoadingListener loadingListener,
                                TransportMode transportMode) {
        this(map, apiKey, loadingListener, transportMode, null, null);
    }

    public IsochroneMapProvider(GoogleMap map,
                                String apiKey,
                                LoadingListener loadingListener,
                                TransportMode transportMode,
                                UiThreadExecutor uiThreadExecutor,
                                TravelTimeFetcher travelTimeFetcher) {
        this.map = map;
        this.apiKey = apiKey;
        this.loadingListener = loadingListener;
        this.transportMode = transportMode;
        this.uiThreadExecutor = uiThreadExecutor;
        this.travelTimeFetcher = travelTimeFetcher != null ? travelTimeFetcher : DEFAULT_TRAVEL_TIME_FETCHER;
    }

    public void setTransportMode(TransportMode transportMode) {
        this.transportMode = transportMode;
    }

    public void drawIsochrones(LatLng origin, int[] durationsInMinutes, ColorSchema schema) {
        int[] sortedDurations = durationsInMinutes.clone();
        java.util.Arrays.sort(sortedDurations);
        reverseArray(sortedDurations);

        if (loadingListener != null) {
            loadingListener.onLoadingStarted();
        }

        int[] colors = schema.getColors();
        List<IsochronePolygon> polygons = Collections.synchronizedList(new ArrayList<>());

        ExecutorService executor = Executors.newFixedThreadPool(4);
        for (int i = 0; i < sortedDurations.length; i++) {
            final int durationIndex = sortedDurations.length - 1 - i; // reverse index
            final int minutes = sortedDurations[i];
            final int baseColor = (durationIndex < colors.length) ? colors[durationIndex] : colors[colors.length - 1];

            executor.execute(() -> {
                List<LatLng> polygon = computeIsochrone(origin, minutes);
                if (!polygon.isEmpty()) {
                    polygons.add(new IsochronePolygon(minutes, polygon, baseColor));
                }
            });
        }

        executor.shutdown();

        new Thread(() -> {
            try {
                while (!executor.isTerminated()) {
                    Thread.sleep(100);
                }
                Collections.sort(polygons, (a, b) -> Integer.compare(b.duration, a.duration));

                runOnUiThread(() -> {
                    for (IsochronePolygon poly : polygons) {
                        drawPolygon(poly.points, poly.baseColor);
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

     List<LatLng> computeIsochrone(LatLng origin, int minutes) {
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

                    for (int cycle = 0; cycle < MAX_CYCLES; cycle++) {
                        double midRadius = (minRadius + maxRadius) / 2;
                        double latOffset = midRadius * Math.cos(angleRad);
                        double lngOffset = midRadius * Math.sin(angleRad) / Math.cos(Math.toRadians(origin.latitude));
                        LatLng dest = new LatLng(origin.latitude + latOffset, origin.longitude + lngOffset);
                        int travelTime = travelTimeFetcher.fetchTravelTime(origin, dest);
                        if (travelTime < 0) break;
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
            points.add(points.get(0));
            return chaikinSmoothing(points, 2);
        }
        return new ArrayList<>();
    }

    private void drawPolygon(List<LatLng> points, int baseColor) {
        int fillColor = (baseColor & 0x00FFFFFF) | (0x33 << 24);
        int strokeColor = baseColor | 0xFF000000;
        map.addPolygon(new PolygonOptions()
                .addAll(points)
                .strokeColor(strokeColor)
                .fillColor(fillColor));
    }

    private List<LatLng> chaikinSmoothing(List<LatLng> input, int iterations) {
        List<LatLng> output = new ArrayList<>(input);
        for (int iter = 0; iter < iterations; iter++) {
            List<LatLng> newPoints = new ArrayList<>();
            for (int i = 0; i < output.size() - 1; i++) {
                LatLng p0 = output.get(i);
                LatLng p1 = output.get(i + 1);
                LatLng Q = new LatLng(0.75 * p0.latitude + 0.25 * p1.latitude, 0.75 * p0.longitude + 0.25 * p1.longitude);
                LatLng R = new LatLng(0.25 * p0.latitude + 0.75 * p1.latitude, 0.25 * p0.longitude + 0.75 * p1.longitude);
                newPoints.add(Q);
                newPoints.add(R);
            }
            newPoints.add(newPoints.get(0));
            output = newPoints;
        }
        return output;
    }

    private void runOnUiThread(Runnable runnable) {
        if (uiThreadExecutor != null) {
            uiThreadExecutor.execute(runnable);
        } else {
            Log.e(TAG, "UI thread executor not set! Cannot run UI code.");
        }
    }

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
