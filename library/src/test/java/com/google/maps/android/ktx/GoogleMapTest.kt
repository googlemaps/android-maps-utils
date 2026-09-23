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
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.first
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
        val deferred = async {
            googleMap.cameraIdleEvents().first()
        }
        advanceUntilIdle()
        verify(googleMap).setOnCameraIdleListener(cameraIdleListener.capture())
        cameraIdleListener.value.onCameraIdle()
        assertThat(deferred.await()).isEqualTo(Unit)
    }

    @Test
    public fun testAwaitMapLoad(): Unit = runTest {
        val deferred = async {
            googleMap.awaitMapLoad()
        }
        advanceUntilIdle()
        verify(googleMap).setOnMapLoadedCallback(loadedCallback.capture())
        loadedCallback.value.onMapLoaded()
        assertThat(deferred.await()).isEqualTo(Unit)
    }

    @Test
    public fun testAwaitAnimateCamera(): Unit = runTest {
        val deferredDefault = async {
            googleMap.awaitAnimateCamera(cameraUpdate)
        }
        advanceUntilIdle()
        verify(googleMap).animateCamera(any(CameraUpdate::class.java), cancelableCallback.capture())
        cancelableCallback.value.onFinish()
        assertThat(deferredDefault.await()).isEqualTo(Unit)

        val deferredWithDuration = async {
            googleMap.awaitAnimateCamera(cameraUpdate, 500)
        }
        advanceUntilIdle()
        verify(googleMap).animateCamera(any(CameraUpdate::class.java), Mockito.eq(500), cancelableCallback.capture())
        cancelableCallback.value.onFinish()
        assertThat(deferredWithDuration.await()).isEqualTo(Unit)
    }
}

