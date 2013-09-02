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
import com.google.maps.android.SphericalUtil;

@TargetApi(Build.VERSION_CODES.HONEYCOMB)
class SphericalLatLngTypeEvaluator implements TypeEvaluator<LatLng> {
    @Override
    public LatLng evaluate(float v, LatLng a, LatLng b) {
        return SphericalUtil.interpolate(a, b, v);
    }
}
