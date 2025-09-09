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
 * WITHOUT WARRANTIES, OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.maps.android.utils.demo;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;

import java.util.regex.Pattern;

/**
 * A utility class to validate the Maps API key.
 */
class ApiKeyValidator {
    private static final String REGEX = "^AIza[0-9A-Za-z-_]{35}$";
    private static final Pattern PATTERN = Pattern.compile(REGEX);

    /**
     * Checks if the provided context has a valid Google Maps API key in its metadata.
     *
     * @param context The context to check for the API key.
     * @return `true` if the context has a valid API key, `false` otherwise.
     */
    static boolean hasMapsApiKey(Context context) {
        String mapsApiKey = getMapsApiKey(context);
        return mapsApiKey != null && PATTERN.matcher(mapsApiKey).matches();
    }

    /**
     * Retrieves the Google Maps API key from the application metadata.
     *
     * @param context The context to retrieve the API key from.
     * @return The API key if found, `null` otherwise.
     */
    private static String getMapsApiKey(Context context) {
        try {
            Bundle bundle = context.getPackageManager()
                .getApplicationInfo(context.getPackageName(), PackageManager.GET_META_DATA)
                .metaData;
            return bundle.getString("com.google.android.geo.API_KEY");
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }
}
