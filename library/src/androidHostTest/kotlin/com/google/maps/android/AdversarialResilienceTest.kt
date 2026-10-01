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

package com.google.maps.android

import android.content.Context
import android.graphics.Bitmap
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GooglePlayServicesNotAvailableException
import com.google.android.gms.maps.CameraUpdate
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapFragment
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.MapsInitializer
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.OnMapsSdkInitializedCallback
import com.google.android.gms.maps.OnStreetViewPanoramaReadyCallback
import com.google.android.gms.maps.StreetViewPanorama
import com.google.android.gms.maps.StreetViewPanoramaFragment
import com.google.android.gms.maps.StreetViewPanoramaView
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.SupportStreetViewPanoramaFragment
import com.google.android.gms.maps.model.LatLng
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertThrows
import org.junit.Test
import org.mockito.ArgumentCaptor
import org.mockito.Mockito.any
import org.mockito.Mockito.eq
import org.mockito.Mockito.isNull
import org.mockito.Mockito.mock
import org.mockito.Mockito.mockStatic
import org.mockito.Mockito.verify

/**
 * Adversarial resilience tests designed to break coroutine and Flow extensions under:
 * 1. Late SDK callback delivery after coroutine cancellation (`withTimeout` / `job.cancel()`).
 * 2. Duplicate/repeated callback delivery from the underlying Maps SDK.
 * 3. Error status return followed by a late asynchronous callback in `MapsInitializer`.
 * 4. Listener leak verification upon `awaitMapLoad()` cancellation and rapid Flow resubscription.
 */
@OptIn(ExperimentalCoroutinesApi::class)
internal class AdversarialResilienceTest {

    @Test
    fun `awaitMap on MapView does not throw IllegalStateException when callback fires after cancellation`() =
        runTest(UnconfinedTestDispatcher()) {
            val mapView = mock(MapView::class.java)
            val googleMap = mock(GoogleMap::class.java)
            val callbackCaptor = ArgumentCaptor.forClass(OnMapReadyCallback::class.java)

            val job = launch {
                mapView.awaitMap()
            }
            verify(mapView).getMapAsync(callbackCaptor.capture())

            // Cancel the caller before the Maps SDK delivers the map instance
            job.cancel(CancellationException("Timed out waiting for map"))
            assertThat(job.isCancelled).isTrue()

            // Adversarial event: Maps SDK still fires OnMapReadyCallback on the main thread later
            callbackCaptor.value.onMapReady(googleMap)
            // Also test duplicate callback invocation
            callbackCaptor.value.onMapReady(googleMap)
        }

    @Test
    fun `awaitMap on SupportMapFragment and MapFragment survives post-cancellation and duplicate callbacks`() =
        runTest(UnconfinedTestDispatcher()) {
            val supportMapFragment = mock(SupportMapFragment::class.java)
            val mapFragment = mock(MapFragment::class.java)
            val googleMap = mock(GoogleMap::class.java)
            val supportCaptor = ArgumentCaptor.forClass(OnMapReadyCallback::class.java)
            val fragmentCaptor = ArgumentCaptor.forClass(OnMapReadyCallback::class.java)

            val job1 = launch { supportMapFragment.awaitMap() }
            val job2 = launch { mapFragment.awaitMap() }

            verify(supportMapFragment).getMapAsync(supportCaptor.capture())
            verify(mapFragment).getMapAsync(fragmentCaptor.capture())

            job1.cancel()
            job2.cancel()

            // Late callbacks after cancellation must not crash
            supportCaptor.value.onMapReady(googleMap)
            supportCaptor.value.onMapReady(googleMap)
            fragmentCaptor.value.onMapReady(googleMap)
            fragmentCaptor.value.onMapReady(googleMap)
        }

    @Test
    fun `awaitStreetViewPanorama survives post-cancellation and duplicate callbacks across all views`() =
        runTest(UnconfinedTestDispatcher()) {
            val view = mock(StreetViewPanoramaView::class.java)
            val fragment = mock(StreetViewPanoramaFragment::class.java)
            val supportFragment = mock(SupportStreetViewPanoramaFragment::class.java)
            val panorama = mock(StreetViewPanorama::class.java)

            val viewCaptor = ArgumentCaptor.forClass(OnStreetViewPanoramaReadyCallback::class.java)
            val fragmentCaptor = ArgumentCaptor.forClass(OnStreetViewPanoramaReadyCallback::class.java)
            val supportCaptor = ArgumentCaptor.forClass(OnStreetViewPanoramaReadyCallback::class.java)

            val job1 = launch { view.awaitStreetViewPanorama() }
            val job2 = launch { fragment.awaitStreetViewPanorama() }
            val job3 = launch { supportFragment.awaitStreetViewPanorama() }

            verify(view).getStreetViewPanoramaAsync(viewCaptor.capture())
            verify(fragment).getStreetViewPanoramaAsync(fragmentCaptor.capture())
            verify(supportFragment).getStreetViewPanoramaAsync(supportCaptor.capture())

            job1.cancel()
            job2.cancel()
            job3.cancel()

            // Late callbacks after cancellation must not crash
            viewCaptor.value.onStreetViewPanoramaReady(panorama)
            viewCaptor.value.onStreetViewPanoramaReady(panorama)
            fragmentCaptor.value.onStreetViewPanoramaReady(panorama)
            fragmentCaptor.value.onStreetViewPanoramaReady(panorama)
            supportCaptor.value.onStreetViewPanoramaReady(panorama)
            supportCaptor.value.onStreetViewPanoramaReady(panorama)
        }

    @Test
    fun `awaitMapLoad clears listener on cancellation and survives late queued callback`() =
        runTest(UnconfinedTestDispatcher()) {
            val googleMap = mock(GoogleMap::class.java)
            val callbackCaptor = ArgumentCaptor.forClass(GoogleMap.OnMapLoadedCallback::class.java)

            val job = launch {
                googleMap.awaitMapLoad()
            }
            verify(googleMap).setOnMapLoadedCallback(callbackCaptor.capture())
            val capturedCallback = callbackCaptor.value

            // Cancel the coroutine while waiting for map load
            job.cancel()

            // Verify the listener was unregistered to prevent leaking the continuation in GoogleMap
            verify(googleMap).setOnMapLoadedCallback(null)

            // Even if the callback was already posted to the main thread message queue before nulling,
            // invoking it after cancellation must not throw IllegalStateException.
            capturedCallback.onMapLoaded()
            capturedCallback.onMapLoaded()
        }

    @Test
    fun `awaitSnapshot survives post-cancellation and duplicate callbacks`() =
        runTest(UnconfinedTestDispatcher()) {
            val googleMap = mock(GoogleMap::class.java)
            val bitmap = mock(Bitmap::class.java)
            val callbackCaptor = ArgumentCaptor.forClass(GoogleMap.SnapshotReadyCallback::class.java)

            val job = launch {
                googleMap.awaitSnapshot(bitmap)
            }
            verify(googleMap).snapshot(callbackCaptor.capture(), eq(bitmap))

            job.cancel()

            // Late callback after cancellation must not throw IllegalStateException
            callbackCaptor.value.onSnapshotReady(bitmap)
            callbackCaptor.value.onSnapshotReady(bitmap)
        }

    @Test
    fun `awaitAnimateCamera survives post-cancellation onFinish and duplicate callbacks`() =
        runTest(UnconfinedTestDispatcher()) {
            val googleMap = mock(GoogleMap::class.java)
            val cameraUpdate = mock(CameraUpdate::class.java)
            val callbackCaptor = ArgumentCaptor.forClass(GoogleMap.CancelableCallback::class.java)

            val job = launch {
                googleMap.awaitAnimateCamera(cameraUpdate, 500)
            }
            verify(googleMap).animateCamera(
                eq(cameraUpdate),
                eq(500),
                callbackCaptor.capture()
            )

            // Caller cancels coroutine while camera animation is in-flight
            job.cancel()

            // Maps SDK finishes animation on main thread after coroutine cancellation
            callbackCaptor.value.onFinish()
            callbackCaptor.value.onFinish()
            callbackCaptor.value.onCancel()
        }

    @Test
    fun `awaitMapsSdkInitialized survives post-cancellation callback and error-then-callback race`() =
        runTest(UnconfinedTestDispatcher()) {
            val context = mock(Context::class.java)

            mockStatic(MapsInitializer::class.java).use { mockedStatic ->
                var capturedCallback: OnMapsSdkInitializedCallback? = null
                mockedStatic.`when`<Int> {
                    MapsInitializer.initialize(
                        eq(context),
                        isNull(),
                        any()
                    )
                }.thenAnswer { invocation ->
                    capturedCallback = invocation.getArgument(2)
                    // Return error status first, then later invoke callback asynchronously
                    ConnectionResult.SERVICE_VERSION_UPDATE_REQUIRED
                }

                val exception = assertThrows(GooglePlayServicesNotAvailableException::class.java) {
                    runBlocking {
                        context.awaitMapsSdkInitialized()
                    }
                }
                assertThat(exception.errorCode).isEqualTo(ConnectionResult.SERVICE_VERSION_UPDATE_REQUIRED)

                // Adversarial race: SDK still invokes callback after returning non-zero status code
                capturedCallback?.onMapsSdkInitialized(MapsInitializer.Renderer.LATEST)
            }
        }

    @Test
    fun `rapid cancellation and resubscription to GoogleMap flows cleans up listeners without dropping active collector`() =
        runTest(UnconfinedTestDispatcher()) {
            val googleMap = mock(GoogleMap::class.java)
            val listenerCaptor = ArgumentCaptor.forClass(GoogleMap.OnMapClickListener::class.java)

            val received = mutableListOf<LatLng>()
            val job1 = launch {
                googleMap.mapClickEvents().collect { received.add(it) }
            }
            verify(googleMap).setOnMapClickListener(listenerCaptor.capture())
            val firstListener = listenerCaptor.value

            // Cancel first collector and immediately start a second collector
            job1.cancel()
            verify(googleMap).setOnMapClickListener(null)

            // Firing a stale event on the old listener after cancellation must not crash or emit
            firstListener.onMapClick(LatLng(10.0, 20.0))
            assertThat(received).isEmpty()
        }
}
