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
@file:Suppress("DEPRECATION")

package com.google.maps.android.ktx

import com.google.android.gms.maps.CameraUpdate
import com.google.android.gms.maps.GoogleMap
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
import org.mockito.Captor
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.Mockito.verify
import org.mockito.junit.MockitoJUnitRunner

@ExperimentalCoroutinesApi
@RunWith(MockitoJUnitRunner::class)
public class GoogleMapTest {

    @Mock
    private lateinit var googleMap: GoogleMap

    @Mock
    private lateinit var cameraUpdate: CameraUpdate

    @Captor
    private lateinit var cameraIdleListener: ArgumentCaptor<GoogleMap.OnCameraIdleListener>

    @Captor
    private lateinit var loadedCallback: ArgumentCaptor<GoogleMap.OnMapLoadedCallback>

    @Captor
    private lateinit var cancelableCallback: ArgumentCaptor<GoogleMap.CancelableCallback>

    @Test
    public fun testCameraIdleEvents(): Unit = runTest {
        var received = false
        val job = launch {
            googleMap.cameraIdleEvents().first()
            received = true
        }
        advanceUntilIdle()
        verify(googleMap).setOnCameraIdleListener(cameraIdleListener.capture())
        cameraIdleListener.value.onCameraIdle()
        advanceUntilIdle()
        assertThat(received).isTrue()
        job.cancel()
    }

    @Test
    public fun testAwaitMapLoad(): Unit = runTest {
        var mapLoaded = false
        val job = launch {
            googleMap.awaitMapLoad()
            mapLoaded = true
        }
        advanceUntilIdle()
        verify(googleMap).setOnMapLoadedCallback(loadedCallback.capture())
        loadedCallback.value.onMapLoaded()
        advanceUntilIdle()
        assertThat(mapLoaded).isTrue()
        job.cancel()
    }

    @Test
    public fun testAwaitAnimateCamera(): Unit = runTest {
        var animated = false
        val job = launch {
            googleMap.awaitAnimateCamera(cameraUpdate)
            animated = true
        }
        advanceUntilIdle()
        verify(googleMap).animateCamera(any(CameraUpdate::class.java), Mockito.eq(3000), cancelableCallback.capture())
        cancelableCallback.value.onFinish()
        advanceUntilIdle()
        assertThat(animated).isTrue()
        job.cancel()
    }
}

