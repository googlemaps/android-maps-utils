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

@file:OptIn(ExperimentalForeignApi::class)

package com.google.maps.android.location

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.useContents
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import platform.CoreLocation.CLLocation
import platform.CoreLocation.CLLocationAccuracy
import platform.CoreLocation.CLLocationManager
import platform.CoreLocation.CLLocationManagerDelegateProtocol
import platform.CoreLocation.kCLAuthorizationStatusDenied
import platform.CoreLocation.kCLAuthorizationStatusRestricted
import platform.CoreLocation.kCLErrorDenied
import platform.CoreLocation.kCLLocationAccuracyBest
import platform.CoreLocation.kCLLocationAccuracyHundredMeters
import platform.CoreLocation.kCLLocationAccuracyNearestTenMeters
import platform.Foundation.NSError
import platform.Foundation.timeIntervalSince1970
import platform.darwin.NSObject

/**
 * On iOS, [Location] is a direct `typealias` for Apple's [CLLocation].
 *
 * This avoids wrapper allocations on every GPS fix while allowing shared `commonMain` code to
 * read coordinates and metadata via the multiplatform extension properties below.
 */
public actual typealias Location = CLLocation

/**
 * Extracts the latitude in degrees from the underlying `CLLocationCoordinate2D` C struct.
 */
public actual val Location.latitude: Double
    get() = coordinate.useContents { latitude }

/**
 * Extracts the longitude in degrees from the underlying `CLLocationCoordinate2D` C struct.
 */
public actual val Location.longitude: Double
    get() = coordinate.useContents { longitude }

/**
 * Returns `CLLocation.horizontalAccuracy` in meters (negative when horizontal accuracy is invalid).
 */
public actual val Location.accuracyMeters: Float
    get() = horizontalAccuracy.toFloat()

/**
 * Returns `CLLocation.altitude` in meters above the WGS 84 reference ellipsoid.
 */
public actual val Location.altitudeMeters: Double
    get() = altitude

/**
 * Returns `CLLocation.course` in degrees East of true North (negative when course is invalid).
 */
public actual val Location.bearingDegrees: Float
    get() = course.toFloat()

/**
 * Returns `CLLocation.speed` in meters per second (negative when speed is invalid).
 */
public actual val Location.speedMetersPerSecond: Float
    get() = speed.toFloat()

/**
 * Converts `CLLocation.timestamp` (`NSDate`) into UTC milliseconds since the Unix epoch.
 */
public actual val Location.timestampMillis: Long
    get() = (timestamp.timeIntervalSince1970 * 1_000.0).toLong()

/**
 * Returns a cold [Flow] that emits device [Location] updates from this [CLLocationManager].
 *
 * Updates begin streaming only when the flow is collected, and `stopUpdatingLocation()` is
 * invoked automatically as soon as the collector cancels or completes.
 *
 * **Why `delegate` is captured inside `awaitClose`:**
 * Objective-C declares `CLLocationManager.delegate` as an `assign` / `weak` reference. Capturing
 * the Kotlin `delegate` instance inside `awaitClose` ensures Kotlin/Native's garbage collector
 * keeps the delegate alive for the entire lifetime of the active flow subscription.
 *
 * @param minDistanceM Minimum horizontal displacement in meters before a new fix is emitted.
 * @param desiredAccuracy Desired CoreLocation accuracy constant (for example,
 *   [kCLLocationAccuracyBest] or [kCLLocationAccuracyHundredMeters]).
 */
public fun CLLocationManager.locationEvents(
    minDistanceM: Float = 1f,
    desiredAccuracy: CLLocationAccuracy = kCLLocationAccuracyBest
): Flow<Location> = callbackFlow {
    val currentStatus = authorizationStatus
    if (currentStatus == kCLAuthorizationStatusDenied ||
        currentStatus == kCLAuthorizationStatusRestricted
    ) {
        close()
        return@callbackFlow
    }

    val delegate = object : NSObject(), CLLocationManagerDelegateProtocol {
        override fun locationManager(manager: CLLocationManager, didUpdateLocations: List<*>) {
            for (item in didUpdateLocations) {
                val location = item as? CLLocation ?: continue
                trySend(location)
            }
        }

        override fun locationManager(manager: CLLocationManager, didFailWithError: NSError) {
            if (didFailWithError.code == kCLErrorDenied) {
                close()
            }
        }

        override fun locationManagerDidChangeAuthorization(manager: CLLocationManager) {
            val status = manager.authorizationStatus
            if (status == kCLAuthorizationStatusDenied ||
                status == kCLAuthorizationStatusRestricted
            ) {
                close()
            }
        }
    }

    this@locationEvents.desiredAccuracy = desiredAccuracy
    this@locationEvents.distanceFilter = minDistanceM.toDouble()
    this@locationEvents.delegate = delegate
    startUpdatingLocation()

    awaitClose {
        stopUpdatingLocation()
        if (this@locationEvents.delegate === delegate) {
            this@locationEvents.delegate = null
        }
    }
}

/**
 * Returns a cold [Flow] that emits coarse location updates on iOS using
 * [kCLLocationAccuracyHundredMeters], mirroring Android's `LocationManager.coarseLocationEvents`.
 */
public fun CLLocationManager.coarseLocationEvents(
    minDistanceM: Float = 1f
): Flow<Location> = locationEvents(
    minDistanceM = minDistanceM,
    desiredAccuracy = kCLLocationAccuracyHundredMeters
)

/**
 * Returns a cold [Flow] that emits fine (GPS-grade) location updates on iOS using
 * [kCLLocationAccuracyBest], mirroring Android's `LocationManager.fineLocationEvents`.
 */
public fun CLLocationManager.fineLocationEvents(
    minDistanceM: Float = 1f
): Flow<Location> = locationEvents(
    minDistanceM = minDistanceM,
    desiredAccuracy = kCLLocationAccuracyBest
)

/**
 * Adapts an iOS [CLLocationManager] into a multiplatform [LocationSource] that can be consumed
 * from shared `commonMain` code alongside Android's `LocationManager` and
 * `FusedLocationProviderClient`.
 */
public fun CLLocationManager.asLocationSource(): LocationSource =
    LocationSource { _, minUpdateDistanceM, priority ->
        val accuracy = when (priority) {
            LocationPriority.HIGH_ACCURACY -> kCLLocationAccuracyBest
            LocationPriority.BALANCED_POWER_ACCURACY -> kCLLocationAccuracyNearestTenMeters
            LocationPriority.LOW_POWER -> kCLLocationAccuracyHundredMeters
        }
        locationEvents(
            minDistanceM = minUpdateDistanceM,
            desiredAccuracy = accuracy
        )
    }
