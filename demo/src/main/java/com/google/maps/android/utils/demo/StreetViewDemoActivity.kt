/*
 * Copyright 2024 Google LLC
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
import android.app.Activity
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.StreetViewUtils
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class StreetViewDemoActivity : Activity() {

    @SuppressLint("SetTextI18n")
    @OptIn(DelicateCoroutinesApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.street_view_demo)

        if (!hasMapsApiKey(this)) {
            Toast.makeText(this, R.string.bad_maps_api_key, Toast.LENGTH_LONG).show()
            finish()
        }

        GlobalScope.launch(Dispatchers.Main) {
            val response1 =
                StreetViewUtils.fetchStreetViewData(LatLng(48.1425918, 11.5386121), BuildConfig.MAPS_API_KEY)
            val response2 = StreetViewUtils.fetchStreetViewData(LatLng(8.1425918, 11.5386121), BuildConfig.MAPS_API_KEY)

            findViewById<TextView>(R.id.textViewFirstLocation).text = "Location 1 is supported in StreetView: $response1"
            findViewById<TextView>(R.id.textViewSecondLocation).text = "Location 2 is  supported in StreetView: $response2"
        }
    }
}
