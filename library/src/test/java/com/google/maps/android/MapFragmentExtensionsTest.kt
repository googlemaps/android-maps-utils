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

import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapFragment
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.OnStreetViewPanoramaReadyCallback
import com.google.android.gms.maps.StreetViewPanorama
import com.google.android.gms.maps.StreetViewPanoramaFragment
import com.google.android.gms.maps.StreetViewPanoramaView
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.SupportStreetViewPanoramaFragment
import com.google.android.gms.maps.model.StreetViewPanoramaCamera
import com.google.android.gms.maps.model.StreetViewPanoramaLocation
import com.google.android.gms.maps.model.StreetViewPanoramaOrientation
import com.google.common.truth.Truth.assertThat
import com.google.maps.android.ktx.awaitMap as ktxAwaitMap
import com.google.maps.android.ktx.awaitStreetViewPanorama as ktxAwaitStreetViewPanorama
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentCaptor
import org.mockito.Captor
import org.mockito.Mock
import org.mockito.Mockito.verify
import org.mockito.junit.MockitoJUnitRunner

/**
 * Unit test suite for Kotlin coroutine extensions on Map and StreetViewPanorama Fragments/Views.
 *
 * **Purpose:**
 * Tests that asynchronous callback-based Google Maps SDK methods ([SupportMapFragment.getMapAsync],
 * [MapView.getMapAsync], [SupportStreetViewPanoramaFragment.getStreetViewPanoramaAsync], etc.) are
 * correctly bridged to Kotlin coroutine suspending functions (`awaitMap()`, `awaitStreetViewPanorama()`),
 * and that StreetViewPanorama callback listeners are bridged to reactive Kotlin `Flow` streams.
 *
 * **How it works:**
 * 1. For suspending functions (`awaitMap`, `awaitStreetViewPanorama`): Uses `runTest` and `launch`
 *    to invoke the suspending extension. Intercepts the SDK callback ([OnMapReadyCallback] or
 *    [OnStreetViewPanoramaReadyCallback]) using a Mockito [ArgumentCaptor]. Invoking `onMapReady(googleMap)`
 *    resumes the suspended coroutine.
 * 2. For Flow extensions (`cameraChangeEvents`, `clickEvents`, etc.): Uses `flow.first()` inside `launch`,
 *    captures the registered SDK listener via [ArgumentCaptor], and invokes the listener callback to
 *    emit an item into the flow.
 *
 * **How we know it is correct:**
 * - **Code under test:** Correct if invoking the SDK callback (`onMapReady`, `onStreetViewPanoramaReady`,
 *   or listener callback) resumes the coroutine or emits to the flow with the exact mock instance.
 * - **Test:** Correct because `advanceUntilIdle()` ensures deterministic coroutine scheduling before
 *   and after callback execution, and `assertThat(result).isEqualTo(expected)` verifies instance equality.
 */
@ExperimentalCoroutinesApi
@RunWith(MockitoJUnitRunner::class)
class MapFragmentExtensionsTest {

    @Mock
    private lateinit var googleMap: GoogleMap

    @Mock
    private lateinit var streetViewPanorama: StreetViewPanorama

    @Mock
    private lateinit var supportMapFragment: SupportMapFragment

    @Mock
    private lateinit var mapFragment: MapFragment

    @Mock
    private lateinit var mapView: MapView

    @Mock
    private lateinit var supportStreetViewPanoramaFragment: SupportStreetViewPanoramaFragment

    @Mock
    private lateinit var streetViewPanoramaFragment: StreetViewPanoramaFragment

    @Mock
    private lateinit var streetViewPanoramaView: StreetViewPanoramaView

    @Mock
    private lateinit var panoramaCamera: StreetViewPanoramaCamera

    @Mock
    private lateinit var panoramaLocation: StreetViewPanoramaLocation

    @Mock
    private lateinit var panoramaOrientation: StreetViewPanoramaOrientation

    @Captor
    private lateinit var onMapReadyCallback: ArgumentCaptor<OnMapReadyCallback>

    @Captor
    private lateinit var onStreetViewPanoramaReadyCallback: ArgumentCaptor<OnStreetViewPanoramaReadyCallback>

    @Captor
    private lateinit var cameraChangeListener: ArgumentCaptor<StreetViewPanorama.OnStreetViewPanoramaCameraChangeListener>

    @Captor
    private lateinit var changeListener: ArgumentCaptor<StreetViewPanorama.OnStreetViewPanoramaChangeListener>

    @Captor
    private lateinit var clickListener: ArgumentCaptor<StreetViewPanorama.OnStreetViewPanoramaClickListener>

    @Captor
    private lateinit var longClickListener: ArgumentCaptor<StreetViewPanorama.OnStreetViewPanoramaLongClickListener>

    // ---------------------------------------------------------------------------------------------
    // Canonical awaitMap() tests for SupportMapFragment, MapFragment, and MapView
    // ---------------------------------------------------------------------------------------------

    /**
     * **Purpose:** Verifies [SupportMapFragment.awaitMap] suspends until [OnMapReadyCallback.onMapReady] is invoked.
     * **How it works:** Calls `awaitMap()` in a coroutine, captures `OnMapReadyCallback`, and invokes `onMapReady(googleMap)`.
     * **How we know it is correct:** Test asserts `awaitMap()` returns the exact `googleMap` mock passed to `onMapReady`.
     */
    @Test
    fun testSupportMapFragmentAwaitMap() = runTest {
        var map: GoogleMap? = null
        val job = launch {
            map = supportMapFragment.awaitMap()
        }
        advanceUntilIdle()
        verify(supportMapFragment).getMapAsync(onMapReadyCallback.capture())
        onMapReadyCallback.value.onMapReady(googleMap)
        advanceUntilIdle()
        assertThat(map).isEqualTo(googleMap)
        job.cancel()
    }

    /**
     * **Purpose:** Verifies [MapFragment.awaitMap] suspends until [OnMapReadyCallback.onMapReady] is invoked.
     * **How it works:** Captures `OnMapReadyCallback` on `mapFragment.getMapAsync` and invokes `onMapReady(googleMap)`.
     * **How we know it is correct:** Asserts the resumed value equals `googleMap`.
     */
    @Test
    fun testMapFragmentAwaitMap() = runTest {
        var map: GoogleMap? = null
        val job = launch {
            map = mapFragment.awaitMap()
        }
        advanceUntilIdle()
        verify(mapFragment).getMapAsync(onMapReadyCallback.capture())
        onMapReadyCallback.value.onMapReady(googleMap)
        advanceUntilIdle()
        assertThat(map).isEqualTo(googleMap)
        job.cancel()
    }

    /**
     * **Purpose:** Verifies [MapView.awaitMap] suspends until [OnMapReadyCallback.onMapReady] is invoked.
     * **How it works:** Captures `OnMapReadyCallback` on `mapView.getMapAsync` and invokes `onMapReady(googleMap)`.
     * **How we know it is correct:** Asserts the resumed value equals `googleMap`.
     */
    @Test
    fun testMapViewAwaitMap() = runTest {
        var map: GoogleMap? = null
        val job = launch {
            map = mapView.awaitMap()
        }
        advanceUntilIdle()
        verify(mapView).getMapAsync(onMapReadyCallback.capture())
        onMapReadyCallback.value.onMapReady(googleMap)
        advanceUntilIdle()
        assertThat(map).isEqualTo(googleMap)
        job.cancel()
    }

    // ---------------------------------------------------------------------------------------------
    // Canonical awaitStreetViewPanorama() tests for SupportStreetViewPanoramaFragment, etc.
    // ---------------------------------------------------------------------------------------------

    /**
     * **Purpose:** Verifies [SupportStreetViewPanoramaFragment.awaitStreetViewPanorama] suspends until panorama is ready.
     * **How it works:** Captures `OnStreetViewPanoramaReadyCallback` on `getStreetViewPanoramaAsync` and calls `onStreetViewPanoramaReady`.
     * **How we know it is correct:** Asserts `awaitStreetViewPanorama()` returns the expected `streetViewPanorama` instance.
     */
    @Test
    fun testSupportStreetViewPanoramaFragmentAwaitPanorama() = runTest {
        var panorama: StreetViewPanorama? = null
        val job = launch {
            panorama = supportStreetViewPanoramaFragment.awaitStreetViewPanorama()
        }
        advanceUntilIdle()
        verify(supportStreetViewPanoramaFragment).getStreetViewPanoramaAsync(onStreetViewPanoramaReadyCallback.capture())
        onStreetViewPanoramaReadyCallback.value.onStreetViewPanoramaReady(streetViewPanorama)
        advanceUntilIdle()
        assertThat(panorama).isEqualTo(streetViewPanorama)
        job.cancel()
    }

    /**
     * **Purpose:** Verifies [StreetViewPanoramaFragment.awaitStreetViewPanorama] suspends until panorama is ready.
     * **How it works:** Captures callback from `streetViewPanoramaFragment.getStreetViewPanoramaAsync` and invokes ready callback.
     * **How we know it is correct:** Asserts resumed panorama equals `streetViewPanorama`.
     */
    @Test
    fun testStreetViewPanoramaFragmentAwaitPanorama() = runTest {
        var panorama: StreetViewPanorama? = null
        val job = launch {
            panorama = streetViewPanoramaFragment.awaitStreetViewPanorama()
        }
        advanceUntilIdle()
        verify(streetViewPanoramaFragment).getStreetViewPanoramaAsync(onStreetViewPanoramaReadyCallback.capture())
        onStreetViewPanoramaReadyCallback.value.onStreetViewPanoramaReady(streetViewPanorama)
        advanceUntilIdle()
        assertThat(panorama).isEqualTo(streetViewPanorama)
        job.cancel()
    }

    /**
     * **Purpose:** Verifies [StreetViewPanoramaView.awaitStreetViewPanorama] suspends until panorama is ready.
     * **How it works:** Captures callback from `streetViewPanoramaView.getStreetViewPanoramaAsync` and invokes ready callback.
     * **How we know it is correct:** Asserts resumed panorama equals `streetViewPanorama`.
     */
    @Test
    fun testStreetViewPanoramaViewAwaitPanorama() = runTest {
        var panorama: StreetViewPanorama? = null
        val job = launch {
            panorama = streetViewPanoramaView.awaitStreetViewPanorama()
        }
        advanceUntilIdle()
        verify(streetViewPanoramaView).getStreetViewPanoramaAsync(onStreetViewPanoramaReadyCallback.capture())
        onStreetViewPanoramaReadyCallback.value.onStreetViewPanoramaReady(streetViewPanorama)
        advanceUntilIdle()
        assertThat(panorama).isEqualTo(streetViewPanorama)
        job.cancel()
    }

    // ---------------------------------------------------------------------------------------------
    // Backwards-Compatible KTX Shims for awaitMap() and awaitStreetViewPanorama()
    // ---------------------------------------------------------------------------------------------

    /**
     * **Purpose:** Verifies deprecated KTX shim `awaitMap(SupportMapFragment)` forwards to canonical awaitMap().
     * **How it works:** Calls KTX shim in coroutine, invokes captured `OnMapReadyCallback`, and verifies resumption.
     * **How we know it is correct:** Asserts KTX shim returns the exact `googleMap` instance without regression.
     */
    @Test
    fun testKtxSupportMapFragmentAwaitMap() = runTest {
        var map: GoogleMap? = null
        val job = launch {
            map = supportMapFragment.ktxAwaitMap()
        }
        advanceUntilIdle()
        verify(supportMapFragment).getMapAsync(onMapReadyCallback.capture())
        onMapReadyCallback.value.onMapReady(googleMap)
        advanceUntilIdle()
        assertThat(map).isEqualTo(googleMap)
        job.cancel()
    }

    /**
     * **Purpose:** Verifies deprecated KTX shim `awaitMap(MapFragment)` forwards to canonical awaitMap().
     * **How it works:** Calls KTX shim in coroutine, invokes captured `OnMapReadyCallback`, and verifies resumption.
     * **How we know it is correct:** Asserts KTX shim returns `googleMap`.
     */
    @Test
    fun testKtxMapFragmentAwaitMap() = runTest {
        var map: GoogleMap? = null
        val job = launch {
            map = mapFragment.ktxAwaitMap()
        }
        advanceUntilIdle()
        verify(mapFragment).getMapAsync(onMapReadyCallback.capture())
        onMapReadyCallback.value.onMapReady(googleMap)
        advanceUntilIdle()
        assertThat(map).isEqualTo(googleMap)
        job.cancel()
    }

    /**
     * **Purpose:** Verifies deprecated KTX shim `awaitMap(MapView)` forwards to canonical awaitMap().
     * **How it works:** Calls KTX shim in coroutine, invokes captured `OnMapReadyCallback`, and verifies resumption.
     * **How we know it is correct:** Asserts KTX shim returns `googleMap`.
     */
    @Test
    fun testKtxMapViewAwaitMap() = runTest {
        var map: GoogleMap? = null
        val job = launch {
            map = mapView.ktxAwaitMap()
        }
        advanceUntilIdle()
        verify(mapView).getMapAsync(onMapReadyCallback.capture())
        onMapReadyCallback.value.onMapReady(googleMap)
        advanceUntilIdle()
        assertThat(map).isEqualTo(googleMap)
        job.cancel()
    }

    /**
     * **Purpose:** Verifies deprecated KTX shim `awaitStreetViewPanorama(SupportStreetViewPanoramaFragment)` forwards correctly.
     * **How it works:** Calls KTX shim in coroutine and triggers captured SDK ready callback.
     * **How we know it is correct:** Asserts KTX shim returns `streetViewPanorama`.
     */
    @Test
    fun testKtxSupportStreetViewPanoramaFragmentAwaitPanorama() = runTest {
        var panorama: StreetViewPanorama? = null
        val job = launch {
            panorama = supportStreetViewPanoramaFragment.ktxAwaitStreetViewPanorama()
        }
        advanceUntilIdle()
        verify(supportStreetViewPanoramaFragment).getStreetViewPanoramaAsync(onStreetViewPanoramaReadyCallback.capture())
        onStreetViewPanoramaReadyCallback.value.onStreetViewPanoramaReady(streetViewPanorama)
        advanceUntilIdle()
        assertThat(panorama).isEqualTo(streetViewPanorama)
        job.cancel()
    }

    /**
     * **Purpose:** Verifies deprecated KTX shim `awaitStreetViewPanorama(StreetViewPanoramaFragment)` forwards correctly.
     * **How it works:** Calls KTX shim in coroutine and triggers captured SDK ready callback.
     * **How we know it is correct:** Asserts KTX shim returns `streetViewPanorama`.
     */
    @Test
    fun testKtxStreetViewPanoramaFragmentAwaitPanorama() = runTest {
        var panorama: StreetViewPanorama? = null
        val job = launch {
            panorama = streetViewPanoramaFragment.ktxAwaitStreetViewPanorama()
        }
        advanceUntilIdle()
        verify(streetViewPanoramaFragment).getStreetViewPanoramaAsync(onStreetViewPanoramaReadyCallback.capture())
        onStreetViewPanoramaReadyCallback.value.onStreetViewPanoramaReady(streetViewPanorama)
        advanceUntilIdle()
        assertThat(panorama).isEqualTo(streetViewPanorama)
        job.cancel()
    }

    /**
     * **Purpose:** Verifies deprecated KTX shim `awaitStreetViewPanorama(StreetViewPanoramaView)` forwards correctly.
     * **How it works:** Calls KTX shim in coroutine and triggers captured SDK ready callback.
     * **How we know it is correct:** Asserts KTX shim returns `streetViewPanorama`.
     */
    @Test
    fun testKtxStreetViewPanoramaViewAwaitPanorama() = runTest {
        var panorama: StreetViewPanorama? = null
        val job = launch {
            panorama = streetViewPanoramaView.ktxAwaitStreetViewPanorama()
        }
        advanceUntilIdle()
        verify(streetViewPanoramaView).getStreetViewPanoramaAsync(onStreetViewPanoramaReadyCallback.capture())
        onStreetViewPanoramaReadyCallback.value.onStreetViewPanoramaReady(streetViewPanorama)
        advanceUntilIdle()
        assertThat(panorama).isEqualTo(streetViewPanorama)
        job.cancel()
    }

    // ---------------------------------------------------------------------------------------------
    // StreetViewPanorama Coroutine Flow Event Listeners
    // ---------------------------------------------------------------------------------------------

    /**
     * **Purpose:** Verifies [StreetViewPanorama.cameraChangeEvents] converts camera change callbacks to a Kotlin Flow.
     * **How it works:** Subscribes to `cameraChangeEvents().first()`, captures SDK listener, and calls `onStreetViewPanoramaCameraChange(panoramaCamera)`.
     * **How we know it is correct:** Proves flow emits the exact `panoramaCamera` event passed to the SDK listener.
     */
    @Test
    fun testStreetViewPanoramaCameraChangeEvents() = runTest {
        val job = launch {
            val event = streetViewPanorama.cameraChangeEvents().first()
            assertThat(event).isEqualTo(panoramaCamera)
        }
        advanceUntilIdle()
        verify(streetViewPanorama).setOnStreetViewPanoramaCameraChangeListener(cameraChangeListener.capture())
        cameraChangeListener.value.onStreetViewPanoramaCameraChange(panoramaCamera)
        job.cancel()
    }

    /**
     * **Purpose:** Verifies [StreetViewPanorama.changeEvents] converts location change callbacks to a Kotlin Flow.
     * **How it works:** Subscribes to `changeEvents().first()`, captures SDK listener, and calls `onStreetViewPanoramaChange(panoramaLocation)`.
     * **How we know it is correct:** Proves flow emits the exact `panoramaLocation` event.
     */
    @Test
    fun testStreetViewPanoramaChangeEvents() = runTest {
        val job = launch {
            val event = streetViewPanorama.changeEvents().first()
            assertThat(event).isEqualTo(panoramaLocation)
        }
        advanceUntilIdle()
        verify(streetViewPanorama).setOnStreetViewPanoramaChangeListener(changeListener.capture())
        changeListener.value.onStreetViewPanoramaChange(panoramaLocation)
        job.cancel()
    }

    /**
     * **Purpose:** Verifies [StreetViewPanorama.clickEvents] converts panorama click callbacks to a Kotlin Flow.
     * **How it works:** Subscribes to `clickEvents().first()`, captures SDK listener, and calls `onStreetViewPanoramaClick(panoramaOrientation)`.
     * **How we know it is correct:** Proves flow emits the clicked `panoramaOrientation`.
     */
    @Test
    fun testStreetViewPanoramaClickEvents() = runTest {
        val job = launch {
            val event = streetViewPanorama.clickEvents().first()
            assertThat(event).isEqualTo(panoramaOrientation)
        }
        advanceUntilIdle()
        verify(streetViewPanorama).setOnStreetViewPanoramaClickListener(clickListener.capture())
        clickListener.value.onStreetViewPanoramaClick(panoramaOrientation)
        job.cancel()
    }

    /**
     * **Purpose:** Verifies [StreetViewPanorama.longClickEvents] converts panorama long-click callbacks to a Kotlin Flow.
     * **How it works:** Subscribes to `longClickEvents().first()`, captures SDK listener, and calls `onStreetViewPanoramaLongClick(panoramaOrientation)`.
     * **How we know it is correct:** Proves flow emits the long-clicked `panoramaOrientation`.
     */
    @Test
    fun testStreetViewPanoramaLongClickEvents() = runTest {
        val job = launch {
            val event = streetViewPanorama.longClickEvents().first()
            assertThat(event).isEqualTo(panoramaOrientation)
        }
        advanceUntilIdle()
        verify(streetViewPanorama).setOnStreetViewPanoramaLongClickListener(longClickListener.capture())
        longClickListener.value.onStreetViewPanoramaLongClick(panoramaOrientation)
        job.cancel()
    }
}
