/*
 * Copyright 2025 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.maps.android.utils.demo;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.Status;

/**
 * An activity that demonstrates how to use the Street View utility in Java to check for Street View
 * availability at different locations.
 *
 * This activity performs the following actions:
 * 1. Sets up the layout to fit system windows.
 * 2. Checks if a valid Google Maps API key is present.
 * 3. Fetches Street View data for two predefined locations using asynchronous callbacks.
 * 4. Displays the results of the Street View data fetch on the screen.
 */
public class StreetViewDemoActivityJava extends AppCompatActivity {

    /**
     * Called when the activity is first created.
     *
     * @param savedInstanceState If the activity is being re-initialized after
     *     previously being shut down then this Bundle contains the data it most
     *     recently supplied in {@link #onSaveInstanceState}.  Otherwise it is null.
     */
    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Make the activity content fit behind the system bars.
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        setContentView(R.layout.street_view_demo);

        // Apply window insets to the main view to avoid content overlapping with system bars.
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insetsCompat) -> {
            Insets insets = insetsCompat.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(insets.left, insets.top, insets.right, insets.bottom);
            return insetsCompat;
        });

        // Check for a valid Maps API key before proceeding.
        if (!ApiKeyValidator.hasMapsApiKey(this)) {
            Toast.makeText(this, R.string.bad_maps_api_key, Toast.LENGTH_LONG).show();
            finish();
            return; // Return early to prevent further execution
        }

        // Fetches Street View data for the first location (expected to be supported).
        StreetViewHelper.fetchStreetViewData(
                new LatLng(48.1425918, 11.5386121),
                BuildConfig.MAPS_API_KEY, new StreetViewHelper.StreetViewCallback() {
                    @Override
                    public void onStreetViewResult(@NonNull Status status) {
                        // Updates the UI with the result on the UI thread.
                        runOnUiThread(() -> {
                            ((TextView) findViewById(R.id.textViewFirstLocation)).setText("Location 1 is supported in StreetView: " + status);
                        });
                    }

                    @Override
                    public void onStreetViewError(@NonNull Exception e) {
                        // Handles the error by printing stack trace and showing a toast.
                        e.printStackTrace();
                        Toast.makeText(StreetViewDemoActivityJava.this, "Error fetching Street View data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

        // Fetches Street View data for the second location (expected to be unsupported).
        StreetViewHelper.fetchStreetViewData(new LatLng(8.1425918, 11.5386121), BuildConfig.MAPS_API_KEY, new StreetViewHelper.StreetViewCallback() {
            @Override
            public void onStreetViewResult(@NonNull Status status) {
                // Updates the UI with the result on the UI thread.
                runOnUiThread(() -> {
                    ((TextView) findViewById(R.id.textViewSecondLocation)).setText("Location 2 is  supported in StreetView: " + status);
                });
            }

            @Override
            public void onStreetViewError(@NonNull Exception e) {
                // Handles the error by printing stack trace and showing a toast.
                e.printStackTrace();
                Toast.makeText(StreetViewDemoActivityJava.this, "Error fetching Street View data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
