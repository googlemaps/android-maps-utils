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

package com.google.maps.android.ui

import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.Interpolator
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import kotlin.math.abs
import kotlin.math.sign

/**
 * Animation utilities for markers with Maps API.
 */
object AnimationUtil {
    /**
     * Animates a marker from it's current position to the provided finalPosition
     *
     * @param marker        marker to animate
     * @param finalPosition the final position of the marker after the animation
     */
    @JvmStatic
    fun animateMarkerTo(marker: Marker, finalPosition: LatLng) {
        animateMarkerTo(marker, finalPosition, 2000) // delegate to new version
    }

    /**
     * Animates a marker from its current position to the provided finalPosition.
     *
     * @param marker        marker to animate
     * @param finalPosition the final position of the marker after the animation
     * @param durationInMs  the duration of the animation in milliseconds
     */
    @JvmStatic
    fun animateMarkerTo(
        marker: Marker,
        finalPosition: LatLng,
        durationInMs: Long
    ) {
        val latLngInterpolator: LatLngInterpolator = LatLngInterpolator.Linear()
        val startPosition = marker.position
        val handler = Handler(Looper.getMainLooper())
        val start = SystemClock.uptimeMillis()
        val interpolator: Interpolator = AccelerateDecelerateInterpolator()
        handler.post(object : Runnable {
            var elapsed: Long = 0
            var t = 0f
            var v = 0f
            override fun run() {
                // Calculate progress using interpolator
                elapsed = SystemClock.uptimeMillis() - start
                t = elapsed / durationInMs.toFloat()
                v = interpolator.getInterpolation(t)
                marker.position = latLngInterpolator.interpolate(v, startPosition, finalPosition)

                // Repeat till progress is complete.
                if (t < 1) {
                    // Post again 16ms later.
                    handler.postDelayed(this, 16)
                }
            }
        })
    }

    /**
     * For other LatLngInterpolator interpolators, see [this link](https://gist.github.com/broady/6314689)
     */
    fun interface LatLngInterpolator {
        fun interpolate(fraction: Float, a: LatLng, b: LatLng): LatLng
        class Linear : LatLngInterpolator {
            override fun interpolate(fraction: Float, a: LatLng, b: LatLng): LatLng {
                val lat = (b.latitude - a.latitude) * fraction + a.latitude
                var lngDelta = b.longitude - a.longitude

                // Take the shortest path across the 180th meridian.
                if (abs(lngDelta) > 180) {
                    lngDelta -= sign(lngDelta) * 360
                }
                val lng = lngDelta * fraction + a.longitude
                return LatLng(lat, lng)
            }
        }
    }
}
