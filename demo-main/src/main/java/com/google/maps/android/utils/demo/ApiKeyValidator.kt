/*
 * Copyright 2024 Google LLC
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

package com.google.maps.android.utils.demo

import android.content.Context
import android.content.pm.PackageManager
import java.util.regex.Pattern

/**
 * Checks if the provided context has a valid Google Maps API key in its metadata.
 *
 *
 * This method retrieves the API key from the application's metadata and returns whether or
 * not it has a valid format.
 *
 * @param context The context to check for the API key.
 * @return `true` if the context has a valid API key, `false` otherwise.
 */
fun hasMapsApiKey(context: Context): Boolean {
    val mapsApiKey = getMapsApiKey(context)

    return mapsApiKey != null && keyHasValidFormat(mapsApiKey)
}

/**
 * Checks if the provided API key has a valid format.
 *
 *
 * The valid format is defined by the regular expression "^AIza[0-9A-Za-z\\-_]{35}$".
 *
 * @param apiKey The API key to validate.
 * @return `true` if the API key has a valid format, `false` otherwise.
 */
internal fun keyHasValidFormat(apiKey: String): Boolean {
    val regex = "^AIza[0-9A-Za-z\\-_]{35}$"
    val pattern = Pattern.compile(regex)
    val matcher = pattern.matcher(apiKey)
    return matcher.matches()
}

/**
 * Retrieves the Google Maps API key from the application metadata.
 *
 * @param context The context to retrieve the API key from.
 * @return The API key if found, `null` otherwise.
 */
private fun getMapsApiKey(context: Context): String? {
    try {
        val bundle = context.packageManager
            .getApplicationInfo(context.packageName, PackageManager.GET_META_DATA)
            .metaData
        return bundle.getString("com.google.android.geo.API_KEY")
    } catch (e: PackageManager.NameNotFoundException) {
        e.printStackTrace()
        return null
    }
}
