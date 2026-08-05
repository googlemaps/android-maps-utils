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
import android.os.Looper
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.Priority
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchers.eq
import org.mockito.Captor
import org.mockito.Mock
import org.mockito.Mockito.verify
import org.mockito.junit.MockitoJUnitRunner

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(MockitoJUnitRunner::class)
public class FusedLocationProviderTest {

    @Mock
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    @Mock
    private lateinit var location: Location

    @Mock
    private lateinit var looper: Looper

    @Captor
    private lateinit var locationCallbackCaptor: ArgumentCaptor<LocationCallback>

    @SuppressLint("MissingPermission")
    @Test
    public fun testLocationEvents(): Unit = runTest {
        val request = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 1000L).build()

        val job = launch {
            val event = fusedLocationClient.locationEvents(request, looper).first()
            assertThat(event).isEqualTo(location)
        }
        advanceUntilIdle()

        verify(fusedLocationClient).requestLocationUpdates(
            eq(request),
            locationCallbackCaptor.capture(),
            eq(looper)
        )

        val result = LocationResult.create(listOf(location))
        locationCallbackCaptor.value.onLocationResult(result)
        advanceUntilIdle()

        job.cancel()
        advanceUntilIdle()

        verify(fusedLocationClient).removeLocationUpdates(eq(locationCallbackCaptor.value))
    }

    @SuppressLint("MissingPermission")
    @Test
    public fun testFusedLocationEvents(): Unit = runTest {
        val job = launch {
            val event = fusedLocationClient.fusedLocationEvents(
                intervalMs = 2000L,
                minUpdateDistanceM = 5f,
                priority = Priority.PRIORITY_BALANCED_POWER_ACCURACY,
                looper = looper
            ).first()
            assertThat(event).isEqualTo(location)
        }
        advanceUntilIdle()

        verify(fusedLocationClient).requestLocationUpdates(
            any(LocationRequest::class.java),
            locationCallbackCaptor.capture(),
            eq(looper)
        )

        val result = LocationResult.create(listOf(location))
        locationCallbackCaptor.value.onLocationResult(result)
        advanceUntilIdle()

        job.cancel()
        advanceUntilIdle()

        verify(fusedLocationClient).removeLocationUpdates(eq(locationCallbackCaptor.value))
    }
}
