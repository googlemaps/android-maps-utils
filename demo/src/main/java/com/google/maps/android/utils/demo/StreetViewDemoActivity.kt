/*
 * Copyright 2026 Google LLC
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

package com.google.maps.android.utils.demo

import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.StreetViewUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * An activity that demonstrates how to use the [StreetViewUtils] to check for Street View
 * availability at different locations.
 *
 * This activity performs the following actions:
 * 1. Sets up the layout to fit system windows.
 * 2. Checks if a valid Google Maps API key is present.
 * 3. Launches a coroutine to fetch Street View data for two predefined locations.
 * 4. Displays the results of the Street View data fetch on the screen.
 */
class StreetViewDemoActivity : AppCompatActivity() {

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Make the activity content fit behind the system bars.
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContentView(R.layout.street_view_demo)

        // Apply window insets to the main view to avoid content overlapping with system bars.
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Check for a valid Maps API key before proceeding.
        if (!hasMapsApiKey(this)) {
            Toast.makeText(this, R.string.bad_maps_api_key, Toast.LENGTH_LONG).show()
            finish()
            return // Return early to prevent further execution
        }

        // Launch a coroutine in the Main dispatcher to fetch Street View data and update the UI.
        lifecycleScope.launch(Dispatchers.Main) {
            // Fetch Street View data for the first location (which is expected to be supported).
            val response1 =
                StreetViewUtils.fetchStreetViewData(LatLng(48.1425918, 11.5386121), BuildConfig.MAPS_API_KEY)
            // Fetch Street View data for the second location (which is expected to be unsupported).
            val response2 = StreetViewUtils.fetchStreetViewData(LatLng(8.1425918, 11.5386121), BuildConfig.MAPS_API_KEY)

            // Update the UI with the results.
            findViewById<TextView>(R.id.textViewFirstLocation).text = "Location 1 is supported in StreetView: $response1"
            findViewById<TextView>(R.id.textViewSecondLocation).text = "Location 2 is  supported in StreetView: $response2"
        }
    }
}
