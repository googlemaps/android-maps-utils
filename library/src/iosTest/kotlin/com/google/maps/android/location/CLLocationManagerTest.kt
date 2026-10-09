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

@file:OptIn(ExperimentalForeignApi::class, ExperimentalCoroutinesApi::class)

package com.google.maps.android.location

import com.google.maps.android.model.LatLng
import com.google.maps.android.model.latitude
import com.google.maps.android.model.longitude
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import platform.CoreLocation.CLLocation
import platform.CoreLocation.CLLocationCoordinate2DMake
import platform.CoreLocation.CLLocationManager
import platform.CoreLocation.kCLErrorDenied
import platform.CoreLocation.kCLErrorDomain
import platform.CoreLocation.kCLLocationAccuracyBest
import platform.CoreLocation.kCLLocationAccuracyHundredMeters
import platform.Foundation.NSDate
import platform.Foundation.NSError
import platform.Foundation.dateWithTimeIntervalSince1970

class CLLocationManagerTest {

    @Test
    fun locationPropertiesMapFromCLLocation() {
        val timestampSeconds = 1_728_500_000.0
        val clLocation = CLLocation(
            coordinate = CLLocationCoordinate2DMake(51.5074, -0.1278),
            altitude = 35.5,
            horizontalAccuracy = 4.5,
            verticalAccuracy = 8.0,
            course = 180.0,
            speed = 12.25,
            timestamp = NSDate.dateWithTimeIntervalSince1970(timestampSeconds)
        )

        val location: Location = clLocation

        assertEquals(51.5074, location.latitude, 1e-6)
        assertEquals(-0.1278, location.longitude, 1e-6)
        assertEquals(LatLng(51.5074, -0.1278), location.latLng)
        assertEquals(4.5f, location.accuracyMeters, 1e-4f)
        assertEquals(35.5, location.altitudeMeters, 1e-6)
        assertEquals(180.0f, location.bearingDegrees, 1e-4f)
        assertEquals(12.25f, location.speedMetersPerSecond, 1e-4f)
        assertEquals(1_728_500_000_000L, location.timestampMillis)
    }

    @Test
    fun fineLocationEventsEmitsUpdatesAndCleansUpDelegate() = runTest(UnconfinedTestDispatcher()) {
        val manager = CLLocationManager()
        val expectedFix = CLLocation(latitude = 37.4220, longitude = -122.0841)

        val deferred = async {
            manager.fineLocationEvents(minDistanceM = 5f).first()
        }

        val delegate = manager.delegate
        assertNotNull(delegate, "Collecting fineLocationEvents should attach a CLLocationManagerDelegate")
        assertEquals(kCLLocationAccuracyBest, manager.desiredAccuracy)
        assertEquals(5.0, manager.distanceFilter)

        delegate.locationManager(manager, didUpdateLocations = listOf(expectedFix))

        val emitted: Location = deferred.await()
        assertEquals(37.4220, emitted.latLng.latitude, 1e-6)
        assertEquals(-122.0841, emitted.latLng.longitude, 1e-6)
        assertNull(manager.delegate, "Cancelling the flow collector should detach the delegate")
    }

    @Test
    fun coarseLocationEventsCompletesOnPermissionDeniedError() = runTest(UnconfinedTestDispatcher()) {
        val manager = CLLocationManager()
        val firstFix = CLLocation(latitude = 40.7128, longitude = -74.0060)

        val deferred = async {
            manager.coarseLocationEvents(minDistanceM = 10f).toList()
        }

        val delegate = manager.delegate
        assertNotNull(delegate)
        assertEquals(kCLLocationAccuracyHundredMeters, manager.desiredAccuracy)
        assertEquals(10.0, manager.distanceFilter)

        delegate.locationManager(manager, didUpdateLocations = listOf(firstFix))
        val deniedError = NSError.errorWithDomain(
            domain = kCLErrorDomain,
            code = kCLErrorDenied,
            userInfo = null
        )
        delegate.locationManager(manager, didFailWithError = deniedError)

        val results = deferred.await()
        assertEquals(1, results.size)
        assertEquals(LatLng(40.7128, -74.0060), results.single().latLng)
        assertNull(manager.delegate)
    }

    @Test
    fun multiplatformLocationSourceStreamsThroughCommonAbstraction() = runTest(UnconfinedTestDispatcher()) {
        val manager = CLLocationManager()
        val source: LocationSource = manager.asLocationSource()
        val expectedFix = CLLocation(latitude = 48.8566, longitude = 2.3522)

        val deferred = async {
            source.locationEvents(
                intervalMs = 1_000L,
                minUpdateDistanceM = 2f,
                priority = LocationPriority.HIGH_ACCURACY
            ).first()
        }

        val delegate = assertNotNull(manager.delegate)
        delegate.locationManager(manager, didUpdateLocations = listOf(expectedFix))

        val emitted = deferred.await()
        assertEquals(LatLng(48.8566, 2.3522), emitted.latLng)
    }
}
