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

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.os.Build;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class Animation {
    /**
     * Constructs and returns a ValueAnimator that animates between LatLng values.
     * @param latLngs A set of locations that the animation will animate between over time.
     */
    public static ValueAnimator ofLatLng(LatLng... latLngs) {
        return ValueAnimator.ofObject(new LatLngTypeEvaluator(), latLngs);
    }

    /**
     * Constructs and returns an ObjectAnimator that animates between LatLng values.
     *
     * @param target The object whose property is to be animated.
     * @param propertyName The property being animated.
     * @param latLngs A set of locations that the animation will animate between over time.
     */
    public static ObjectAnimator ofLatLng(Object target, String propertyName, LatLng... latLngs) {
        return ObjectAnimator.ofObject(target, propertyName, new LatLngTypeEvaluator(), latLngs);
    }

    /**
     * Constructs and returns an ObjectAnimator that animates a Marker's position between LatLng values.
     *
     * @param target  The marker whose position is to be animated.
     * @param latLngs A set of locations that the marker will animate between over time.
     * @return An ObjectAnimator object that is set up to animate between the given values.
     */
    public static ObjectAnimator ofMarkerPosition(Marker target, LatLng... latLngs) {
        return ofLatLng(target, "position", latLngs);
    }
}