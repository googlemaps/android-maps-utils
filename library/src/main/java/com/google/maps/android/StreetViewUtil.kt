/*
 * Copyright 2023 Google Inc.
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
package com.google.maps.android

import kotlinx.coroutines.withContext
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.Dispatchers
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader


/**
 * Utility functions for StreetView
 */
class StreetViewUtils {
    companion object {

        /**
         * This function will check whether a location is available on StreetView or not.
         *
         * @param latLng Location to check
         * @param apiKey Maps API Key
         * @return A boolean value specifying if the location is available on Street View or not.
         */
        suspend fun fetchStreetViewData(latLng: LatLng, apiKey: String): Boolean {
            val urlString =
                "https://maps.googleapis.com/maps/api/streetview/metadata?location=${latLng.latitude},${latLng.longitude}&key=$apiKey"

            return withContext(Dispatchers.IO) {
                try {
                    val url = URL(urlString)
                    val connection = url.openConnection() as HttpURLConnection
                    connection.requestMethod = "GET"

                    val responseCode = connection.responseCode
                    if (responseCode == HttpURLConnection.HTTP_OK) {
                        val inputStream = connection.inputStream
                        val bufferedReader = BufferedReader(InputStreamReader(inputStream))
                        val responseString = bufferedReader.use { it.readText() }
                        bufferedReader.close()
                        inputStream.close()

                        val response: ResponseStreetView = deserializeResponse(responseString)
                        response.status == Status.OK
                    } else {
                        throw IOException("HTTP Error: $responseCode")
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    throw IOException("Network error: ${e.message}")
                }
            }
        }

        private fun deserializeResponse(responseString: String): ResponseStreetView {
            val jsonObject = JSONObject(responseString)
            val statusString = jsonObject.optString("status")
            val status = Status.valueOf(statusString)

            return ResponseStreetView(status)
        }
    }
}

data class ResponseStreetView(val status: Status)

enum class Status {
    OK,
    ZERO_RESULTS,
    NOT_FOUND
}
