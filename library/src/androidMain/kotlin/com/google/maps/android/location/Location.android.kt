/*
 * Copyright 2026 Google LLC
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

package com.google.maps.android.location

import android.Manifest
import android.location.LocationManager
import android.os.Looper
import androidx.annotation.RequiresPermission

/**
 * On Android, [Location] is a direct `typealias` for [android.location.Location].
 *
 * This preserves binary and source compatibility with existing Android callers of
 * [coarseLocationEvents], [fineLocationEvents], and [fusedLocationEvents].
 */
public actual typealias Location = android.location.Location

/**
 * Binds to [android.location.Location.getLatitude]. Inside the getter body, Java member/bean
 * property resolution takes precedence over this extension property, avoiding recursion.
 */
public actual val Location.latitude: Double
    get() = this.latitude

/**
 * Binds to [android.location.Location.getLongitude].
 */
public actual val Location.longitude: Double
    get() = this.longitude

/**
 * Returns [android.location.Location.getAccuracy] in meters when present, or `-1f` when
 * [android.location.Location.hasAccuracy] is `false` (matching iOS `CLLocation.horizontalAccuracy`
 * semantics for invalid/unavailable accuracy).
 */
public actual val Location.accuracyMeters: Float
    get() = if (hasAccuracy()) accuracy else -1f

/**
 * Returns [android.location.Location.getAltitude] in meters when present, or `0.0` otherwise.
 */
public actual val Location.altitudeMeters: Double
    get() = if (hasAltitude()) altitude else 0.0

/**
 * Returns [android.location.Location.getBearing] in degrees when present, or `-1f` when
 * [android.location.Location.hasBearing] is `false` (matching iOS `CLLocation.course` semantics).
 */
public actual val Location.bearingDegrees: Float
    get() = if (hasBearing()) bearing else -1f

/**
 * Returns [android.location.Location.getSpeed] in meters/second when present, or `-1f` when
 * [android.location.Location.hasSpeed] is `false` (matching iOS `CLLocation.speed` semantics).
 */
public actual val Location.speedMetersPerSecond: Float
    get() = if (hasSpeed()) speed else -1f

/**
 * Returns the UTC fix timestamp in milliseconds via [android.location.Location.getTime].
 */
public actual val Location.timestampMillis: Long
    get() = time

/**
 * Adapts an Android platform [LocationManager] into a multiplatform [LocationSource] that can be
 * consumed from shared `commonMain` code.
 *
 * [LocationPriority.HIGH_ACCURACY] delegates to [fineLocationEvents] (`GPS_PROVIDER`), while
 * [LocationPriority.BALANCED_POWER_ACCURACY] and [LocationPriority.LOW_POWER] delegate to
 * [coarseLocationEvents] (`NETWORK_PROVIDER`).
 */
@RequiresPermission(anyOf = [Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION])
public fun LocationManager.asLocationSource(
    looper: Looper = Looper.getMainLooper()
): LocationSource = LocationSource { intervalMs, minUpdateDistanceM, priority ->
    when (priority) {
        LocationPriority.HIGH_ACCURACY -> fineLocationEvents(
            minTimeMs = intervalMs,
            minDistanceM = minUpdateDistanceM,
            looper = looper
        )
        LocationPriority.BALANCED_POWER_ACCURACY,
        LocationPriority.LOW_POWER -> coarseLocationEvents(
            minTimeMs = intervalMs,
            minDistanceM = minUpdateDistanceM,
            looper = looper
        )
    }
}
