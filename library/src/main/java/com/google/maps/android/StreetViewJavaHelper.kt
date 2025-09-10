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
package com.google.maps.android

import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * A helper object to call the suspend function `fetchStreetViewData` from Java.
 */
object StreetViewJavaHelper {
    /**
     * A callback interface to receive the result of the Street View data fetch.
     */
    interface StreetViewCallback {
        /**
         * Called when the Street View data is fetched successfully.
         *
         * @param status The status of the Street View data.
         */
        fun onStreetViewResult(status: Status)

        /**
         * Called when there is an error fetching the Street View data.
         *
         * @param e The exception that occurred.
         */
        fun onStreetViewError(e: Exception)
    }

    /**
     * Fetches Street View data for the given location and returns the result via a callback.
     *
     * @param latLng The location to fetch Street View data for.
     * @param apiKey The API key to use for the request.
     * @param callback The callback to receive the result.
     */
    @JvmStatic
    fun fetchStreetViewData(latLng: LatLng, apiKey: String, callback: StreetViewCallback) {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                val status = StreetViewUtils.fetchStreetViewData(latLng, apiKey)
                callback.onStreetViewResult(status)
            } catch (e: Exception) {
                callback.onStreetViewError(e)
            }
        }
    }
}
