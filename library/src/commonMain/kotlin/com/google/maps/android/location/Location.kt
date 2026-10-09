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

import com.google.maps.android.model.LatLng
import kotlinx.coroutines.flow.Flow

/**
 * A platform-native geographic location fix emitted by device location providers.
 *
 * Why an `expect class` with extension properties instead of a wrapper data class?
 * Existing Android code in this library ([coarseLocationEvents], [fineLocationEvents], and
 * [fusedLocationEvents]) already returns `Flow<android.location.Location>`. By actualizing
 * [Location] as a `typealias` to `android.location.Location` on Android and
 * `platform.CoreLocation.CLLocation` on iOS:
 * 1. Existing Android callers experience zero breaking changes and zero wrapper allocations.
 * 2. Platform-specific code can still access every native method on `android.location.Location`
 *    or `CLLocation` directly.
 * 3. Shared `commonMain` code can read coordinates and metadata uniformly through the
 *    extension properties below ([latLng], [latitude], [longitude], [accuracyMeters], etc.).
 */
public expect class Location

/**
 * The latitude of this location fix, in degrees.
 */
public expect val Location.latitude: Double

/**
 * The longitude of this location fix, in degrees.
 */
public expect val Location.longitude: Double

/**
 * The horizontal accuracy radius of this location fix in meters, or a negative value if
 * horizontal accuracy is unavailable.
 */
public expect val Location.accuracyMeters: Float

/**
 * The altitude of this location fix in meters above the WGS 84 reference ellipsoid.
 */
public expect val Location.altitudeMeters: Double

/**
 * The direction of travel in degrees East of true North (`[0.0, 360.0)`), or a negative value
 * if bearing/course is unavailable.
 */
public expect val Location.bearingDegrees: Float

/**
 * The instantaneous speed of the device in meters per second, or a negative value if speed
 * is unavailable.
 */
public expect val Location.speedMetersPerSecond: Float

/**
 * The UTC timestamp of this location fix, in milliseconds since the Unix epoch
 * (January 1, 1970 00:00:00 UTC).
 */
public expect val Location.timestampMillis: Long

/**
 * Converts this platform [Location] fix into a multiplatform [LatLng] coordinate for direct use
 * with camera animations, markers, clustering, and spatial geometry utilities.
 */
public val Location.latLng: LatLng
    get() = LatLng(latitude, longitude)

/**
 * Cross-platform quality-of-service priority for location update requests.
 *
 * Each platform maps these priorities to its native location engine:
 * - On Android `FusedLocationProviderClient`, maps to Play Services `Priority` constants.
 * - On Android `LocationManager`, selects between `GPS_PROVIDER` ([HIGH_ACCURACY]) and
 *   `NETWORK_PROVIDER` ([BALANCED_POWER_ACCURACY], [LOW_POWER]).
 * - On iOS `CLLocationManager`, maps to `kCLLocationAccuracyBest`,
 *   `kCLLocationAccuracyNearestTenMeters`, and `kCLLocationAccuracyHundredMeters`.
 */
public enum class LocationPriority {
    /** Requests the finest location accuracy available (typically GPS). */
    HIGH_ACCURACY,

    /** Requests block-level accuracy (~10-100m) balancing battery and precision. */
    BALANCED_POWER_ACCURACY,

    /** Requests coarse, city/neighborhood-level accuracy with minimal battery impact. */
    LOW_POWER
}

/**
 * A multiplatform source of reactive device [Location] updates exposed as a cold Kotlin [Flow].
 *
 * Shared UI and presentation layers in `commonMain` can depend on [LocationSource] without
 * coupling to Android's `FusedLocationProviderClient` / `LocationManager` or iOS's
 * `CLLocationManager`.
 */
public fun interface LocationSource {
    /**
     * Returns a cold [Flow] that starts streaming [Location] updates when collected and
     * automatically unregisters the underlying platform listener when the collector cancels.
     *
     * @param intervalMs Desired interval between updates in milliseconds (used by Android providers;
     *   iOS `CLLocationManager` filters primarily by [minUpdateDistanceM] and [priority]).
     * @param minUpdateDistanceM Minimum displacement in meters before a new update is emitted.
     * @param priority Desired accuracy and power trade-off.
     */
    public fun locationEvents(
        intervalMs: Long,
        minUpdateDistanceM: Float,
        priority: LocationPriority
    ): Flow<Location>
}

/**
 * Convenience overload of [LocationSource.locationEvents] with sensible multiplatform defaults
 * (`2,000 ms` interval, `1 meter` minimum distance, and [LocationPriority.HIGH_ACCURACY]).
 */
public fun LocationSource.locationEvents(
    intervalMs: Long = 2_000L,
    minUpdateDistanceM: Float = 1f,
    priority: LocationPriority = LocationPriority.HIGH_ACCURACY
): Flow<Location> = locationEvents(
    intervalMs = intervalMs,
    minUpdateDistanceM = minUpdateDistanceM,
    priority = priority
)
