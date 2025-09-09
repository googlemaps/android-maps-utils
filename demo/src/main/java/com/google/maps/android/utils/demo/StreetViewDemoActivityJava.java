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

package com.google.maps.android.utils.demo;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.Status;
import com.google.maps.android.utils.demo.BuildConfig;
import com.google.maps.android.utils.demo.R;

/**
 * This activity demonstrates how to use the Street View utility in Java.
 */
public class StreetViewDemoActivityJava extends Activity {

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.street_view_demo);

        if (!ApiKeyValidator.hasMapsApiKey(this)) {
            Toast.makeText(this, R.string.bad_maps_api_key, Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        // Fetches Street View data for the first location.
        StreetViewHelper.fetchStreetViewData(
            new LatLng(48.1425918, 11.5386121),
            BuildConfig.MAPS_API_KEY,
            new StreetViewHelper.StreetViewCallback() {
                @Override
                public void onStreetViewResult(Status status) {
                    // Updates the UI with the result.
                    runOnUiThread(() -> {
                        ((TextView) findViewById(R.id.textViewFirstLocation)).setText("Location 1 is supported in StreetView: " + status);
                    });
                }

                @Override
                public void onStreetViewError(Exception e) {
                    // Handles the error.
                    e.printStackTrace();
                    Toast.makeText(StreetViewDemoActivityJava.this, "Error fetching Street View data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        );

        // Fetches Street View data for the second location.
        StreetViewHelper.fetchStreetViewData(
            new LatLng(8.1425918, 11.5386121),
            BuildConfig.MAPS_API_KEY,
            new StreetViewHelper.StreetViewCallback() {
                @Override
                public void onStreetViewResult(Status status) {
                    // Updates the UI with the result.
                    runOnUiThread(() -> {
                        ((TextView) findViewById(R.id.textViewSecondLocation)).setText("Location 2 is  supported in StreetView: " + status);
                    });
                }

                @Override
                public void onStreetViewError(Exception e) {
                    // Handles the error.
                    e.printStackTrace();
                    Toast.makeText(StreetViewDemoActivityJava.this, "Error fetching Street View data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        );
    }
}