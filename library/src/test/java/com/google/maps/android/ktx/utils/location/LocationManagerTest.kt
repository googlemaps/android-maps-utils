@file:Suppress("DEPRECATION")
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
 *
 */

package com.google.maps.android.ktx.utils.location

import android.annotation.SuppressLint
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.anyFloat
import org.mockito.ArgumentMatchers.anyLong
import org.mockito.ArgumentMatchers.eq
import org.mockito.Captor
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.Mockito.verify
import org.mockito.junit.MockitoJUnitRunner

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(MockitoJUnitRunner::class)
public class LocationManagerTest {

    @Mock
    private lateinit var locationManager: LocationManager

    @Mock
    private lateinit var location: Location

    @Captor
    private lateinit var locationListenerCaptor: ArgumentCaptor<LocationListener>

    @SuppressLint("MissingPermission")
    @Test
    public fun testCoarseLocationEvents(): Unit = runTest {
        // Setup providers mock
        `when`(locationManager.allProviders).thenReturn(listOf(LocationManager.NETWORK_PROVIDER))

        val job = launch {
            val event = locationManager.coarseLocationEvents(1000L, 1f).first()
            assertThat(event).isEqualTo(location)
        }
        advanceUntilIdle()

        // Verify registered with correct provider and capture listener
        verify(locationManager).requestLocationUpdates(
            eq(LocationManager.NETWORK_PROVIDER),
            eq(1000L),
            eq(1f),
            locationListenerCaptor.capture()
        )

        // Trigger event
        locationListenerCaptor.value.onLocationChanged(location)
        advanceUntilIdle()

        job.cancel()
        advanceUntilIdle()

        // Verify cleanup
        verify(locationManager).removeUpdates(eq(locationListenerCaptor.value))
    }

    @SuppressLint("MissingPermission")
    @Test
    public fun testCoarseLocationProviderDisabled(): Unit = runTest {
        `when`(locationManager.allProviders).thenReturn(listOf(LocationManager.NETWORK_PROVIDER))

        val exceptions = mutableListOf<Throwable>()
        val job = launch {
            try {
                locationManager.coarseLocationEvents(1000L, 1f).collect {}
            } catch (e: Throwable) {
                exceptions.add(e)
            }
        }
        advanceUntilIdle()

        verify(locationManager).requestLocationUpdates(
            eq(LocationManager.NETWORK_PROVIDER),
            anyLong(),
            anyFloat(),
            locationListenerCaptor.capture()
        )

        // Simulate provider disablement!
        locationListenerCaptor.value.onProviderDisabled(LocationManager.NETWORK_PROVIDER)
        advanceUntilIdle()

        // Verify that the flow threw a CancellationException and terminated cleanly
        assertThat(exceptions).hasSize(1)
        assertThat(exceptions.first()).isInstanceOf(kotlinx.coroutines.CancellationException::class.java)
        assertThat(exceptions.first().message).contains("Location provider ${LocationManager.NETWORK_PROVIDER} was disabled")

        // Verify cleanup runs automatically on closure!
        verify(locationManager).removeUpdates(eq(locationListenerCaptor.value))
        job.cancel()
    }

    @SuppressLint("MissingPermission")
    @Test
    public fun testFineLocationEvents(): Unit = runTest {
        val job = launch {
            val event = locationManager.fineLocationEvents(2000L, 2f).first()
            assertThat(event).isEqualTo(location)
        }
        advanceUntilIdle()

        // Verify registered with GPS provider
        verify(locationManager).requestLocationUpdates(
            eq(LocationManager.GPS_PROVIDER),
            eq(2000L),
            eq(2f),
            locationListenerCaptor.capture()
        )

        // Trigger event
        locationListenerCaptor.value.onLocationChanged(location)
        advanceUntilIdle()

        job.cancel()
        advanceUntilIdle()

        // Verify cleanup
        verify(locationManager).removeUpdates(eq(locationListenerCaptor.value))
    }
}
