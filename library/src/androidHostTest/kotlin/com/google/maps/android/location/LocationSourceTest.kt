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

import android.annotation.SuppressLint
import android.location.LocationListener
import android.location.LocationManager
import android.os.Looper
import com.google.common.truth.Truth.assertThat
import com.google.maps.android.model.LatLng
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.eq
import org.mockito.Captor
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.Mockito.verify
import org.mockito.junit.MockitoJUnitRunner

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(MockitoJUnitRunner::class)
public class LocationSourceTest {

    @Mock
    private lateinit var locationManager: LocationManager

    @Mock
    private lateinit var location: Location

    @Mock
    private lateinit var looper: Looper

    @Captor
    private lateinit var listenerCaptor: ArgumentCaptor<LocationListener>

    @Test
    public fun locationExtensionPropertiesMapFromAndroidLocation() {
        `when`(location.latitude).thenReturn(51.5074)
        `when`(location.longitude).thenReturn(-0.1278)
        `when`(location.hasAccuracy()).thenReturn(true)
        `when`(location.accuracy).thenReturn(4.5f)
        `when`(location.hasAltitude()).thenReturn(true)
        `when`(location.altitude).thenReturn(35.5)
        `when`(location.hasBearing()).thenReturn(true)
        `when`(location.bearing).thenReturn(180.0f)
        `when`(location.hasSpeed()).thenReturn(true)
        `when`(location.speed).thenReturn(12.25f)
        `when`(location.time).thenReturn(1_728_500_000_000L)

        assertThat(location.latLng).isEqualTo(LatLng(51.5074, -0.1278))
        assertThat(location.accuracyMeters).isEqualTo(4.5f)
        assertThat(location.altitudeMeters).isEqualTo(35.5)
        assertThat(location.bearingDegrees).isEqualTo(180.0f)
        assertThat(location.speedMetersPerSecond).isEqualTo(12.25f)
        assertThat(location.timestampMillis).isEqualTo(1_728_500_000_000L)
    }

    @SuppressLint("MissingPermission")
    @Test
    public fun locationManagerAsLocationSourceStreamsThroughCommonAbstraction(): Unit = runTest {
        `when`(locationManager.allProviders).thenReturn(listOf(LocationManager.GPS_PROVIDER))
        `when`(location.latitude).thenReturn(37.4220)
        `when`(location.longitude).thenReturn(-122.0841)

        val source: LocationSource = locationManager.asLocationSource(looper)
        val deferred = async {
            source.locationEvents(
                intervalMs = 2_000L,
                minUpdateDistanceM = 3f,
                priority = LocationPriority.HIGH_ACCURACY
            ).first()
        }
        advanceUntilIdle()

        verify(locationManager).requestLocationUpdates(
            eq(LocationManager.GPS_PROVIDER),
            eq(2_000L),
            eq(3f),
            listenerCaptor.capture(),
            eq(looper)
        )

        listenerCaptor.value.onLocationChanged(location)
        val emitted = deferred.await()

        assertThat(emitted.latLng).isEqualTo(LatLng(37.4220, -122.0841))
        verify(locationManager).removeUpdates(eq(listenerCaptor.value))
    }
}
