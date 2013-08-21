/*
 * Copyright 2013 Google Inc.
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

package com.google.maps.android.animation;

import android.animation.TypeEvaluator;
import android.annotation.TargetApi;
import android.os.Build;

import com.google.android.gms.maps.model.LatLng;

@TargetApi(Build.VERSION_CODES.HONEYCOMB)
class LatLngTypeEvaluator implements TypeEvaluator<LatLng> {
    @Override
    public LatLng evaluate(float v, LatLng a, LatLng b) {
        double lat = interp(v, a.latitude, b.latitude);

        // Take the shortest path across the 180th meridian.
        double lngDelta = a.longitude - b.longitude;
        if (Math.abs(lngDelta) < 180) {
            return new LatLng(lat, interp(v, a.longitude, b.longitude));
        }
        return new LatLng(lat, interp(v, a.longitude, b.longitude + Math.signum(lngDelta) * 360));
    }

    private double interp(float v, double a, double b) {
        return a + (b - a) * v;
    }
}
