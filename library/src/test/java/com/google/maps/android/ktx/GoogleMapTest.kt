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
 */
package com.google.maps.android.ktx

import android.content.Context
import com.google.android.gms.maps.CameraUpdate
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapsInitializer
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.Marker
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
import org.mockito.Mockito.verify
import org.mockito.Mockito
import org.mockito.junit.MockitoJUnitRunner
import com.google.common.truth.Truth.assertThat
import org.junit.After
import org.junit.Before
import org.mockito.MockedStatic
import org.mockito.Mockito.mockStatic

@ExperimentalCoroutinesApi
@RunWith(MockitoJUnitRunner::class)
public class GoogleMapTest {

    @Mock
    private lateinit var googleMap: GoogleMap

    @Mock
    private lateinit var marker: Marker

    @Mock
    private lateinit var context: Context

    @Captor
    private lateinit var cameraIdleListener: ArgumentCaptor<GoogleMap.OnCameraIdleListener>

    @Captor
    private lateinit var loadedCallback: ArgumentCaptor<GoogleMap.OnMapLoadedCallback>

    @Captor
    private lateinit var cancelableCallback: ArgumentCaptor<GoogleMap.CancelableCallback>

    private lateinit var mapsInitializerMock: MockedStatic<MapsInitializer>

    @Before
    public fun setUp() {
        mapsInitializerMock = mockStatic(MapsInitializer::class.java)
        Mockito.`when`(MapsInitializer.initialize(context)).thenReturn(0)
    }

    @After
    public fun tearDown() {
        mapsInitializerMock.close()
    }

    @Test
    public fun testCameraIdleEvents(): Unit = runTest {
        val job = launch {
            val event = googleMap.cameraIdleEvents().first()
            assertThat(event).isNotNull()
        }
        advanceUntilIdle()
        verify(googleMap).setOnCameraIdleListener(cameraIdleListener.capture())
        cameraIdleListener.value.onCameraIdle()
        job.cancel()
    }

    @Test
    public fun testAwaitMapLoad(): Unit = runTest {
        val job = launch {
            googleMap.awaitMapLoad()
        }
        advanceUntilIdle()
        verify(googleMap).setOnMapLoadedCallback(loadedCallback.capture())
        loadedCallback.value.onMapLoaded()
        job.cancel()
    }

    public fun testAwaitAnimateCamera(): Unit = runTest {
        val job = launch {
            googleMap.awaitAnimateCamera(com.google.android.gms.maps.CameraUpdateFactory.newCameraPosition(
                CameraPosition.builder().target(com.google.android.gms.maps.model.LatLng(10.0, 20.0)).zoom(5f).build()
            ))
        }
        advanceUntilIdle()
        verify(googleMap).animateCamera(any(CameraUpdate::class.java), cancelableCallback.capture())
        cancelableCallback.value.onFinish()
        job.cancel()
    }
}
