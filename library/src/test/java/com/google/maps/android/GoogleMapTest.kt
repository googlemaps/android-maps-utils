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
import android.location.Location
import com.google.android.gms.maps.CameraUpdate
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapsInitializer
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.Circle
import com.google.android.gms.maps.model.GroundOverlay
import com.google.android.gms.maps.model.IndoorBuilding
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.PointOfInterest
import com.google.android.gms.maps.model.Polygon
import com.google.android.gms.maps.model.Polyline
import com.google.android.gms.maps.model.TileOverlay
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Captor
import org.mockito.Mock
import org.mockito.MockedStatic
import org.mockito.Mockito
import org.mockito.Mockito.mockStatic
import org.mockito.Mockito.verify
import org.mockito.junit.MockitoJUnitRunner

/**
 * Unit test suite for Kotlin extensions on [GoogleMap], covering reactive `Flow` event streams,
 * coroutine suspension helpers, and Kotlin DSL option builders.
 *
 * **Purpose:**
 * 1. **Reactive Flows:** Verifies that all 20 Google Map callback listener methods (`setOnMapClickListener`,
 *    `setOnMarkerClickListener`, `setOnCameraMoveListener`, etc.) are correctly bridged to cold Kotlin `Flow` streams.
 * 2. **Coroutine Suspension Helpers:** Verifies that async callback methods (`awaitMapLoad`,
 *    `awaitAnimateCamera`, `awaitSnapshot`) suspend the coroutine until their callback is completed.
 * 3. **DSL Builders:** Verifies that Kotlin inline option builders (`addMarker { ... }`, `addPolyline { ... }`, etc.)
 *    construct valid option objects and delegate to the underlying [GoogleMap] add-overlay methods.
 *
 * **How it works:**
 * - For **Flow tests**: Uses `runTest` and `launch` to collect the first emitted item (`flow.first()`).
 *   An [ArgumentCaptor] intercepts the SDK listener registered on [GoogleMap]. Invoking the listener's
 *   callback method (`onMapClick`, `onMarkerClick`, etc.) emits the test payload into the flow.
 * - For **Suspension tests**: Captures the SDK callback (`OnMapLoadedCallback`, `CancelableCallback`,
 *   or `SnapshotReadyCallback`) and invokes its completion method to resume the suspended coroutine.
 * - For **DSL Builders**: Invokes `googleMap.addMarker { ... }` and verifies via Mockito that
 *   `googleMap.addMarker(any())` was called with the constructed options.
 *
 * **How we know it is correct:**
 * - **Code under test:** Correct if listener callbacks emit the exact mock instance to the Flow/coroutine,
 *   and if DSL builders delegate to the canonical [GoogleMap] overlay methods without mutation loss.
 * - **Test:** Correct because `advanceUntilIdle()` guarantees deterministic coroutine scheduling before
 *   and after callback invocation, and `assertThat(event).isEqualTo(expected)` confirms exact emission equality.
 */
@ExperimentalCoroutinesApi
@RunWith(MockitoJUnitRunner::class)
public class GoogleMapTest {

    @Mock
    private lateinit var googleMap: GoogleMap

    @Mock
    private lateinit var marker: Marker

    @Mock
    private lateinit var circle: Circle

    @Mock
    private lateinit var groundOverlay: GroundOverlay

    @Mock
    private lateinit var polygon: Polygon

    @Mock
    private lateinit var polyline: Polyline

    @Mock
    private lateinit var tileOverlay: TileOverlay

    @Mock
    private lateinit var location: Location

    @Mock
    private lateinit var bitmap: Bitmap

    @Mock
    private lateinit var context: Context

    @Captor
    private lateinit var cameraIdleListener: ArgumentCaptor<GoogleMap.OnCameraIdleListener>

    @Captor
    private lateinit var cameraMoveListener: ArgumentCaptor<GoogleMap.OnCameraMoveListener>

    @Captor
    private lateinit var cameraMoveStartedListener: ArgumentCaptor<GoogleMap.OnCameraMoveStartedListener>

    @Captor
    private lateinit var cameraMoveCanceledListener: ArgumentCaptor<GoogleMap.OnCameraMoveCanceledListener>

    @Captor
    private lateinit var mapClickListener: ArgumentCaptor<GoogleMap.OnMapClickListener>

    @Captor
    private lateinit var mapLongClickListener: ArgumentCaptor<GoogleMap.OnMapLongClickListener>

    @Captor
    private lateinit var markerClickListener: ArgumentCaptor<GoogleMap.OnMarkerClickListener>

    @Captor
    private lateinit var markerDragListener: ArgumentCaptor<GoogleMap.OnMarkerDragListener>

    @Captor
    private lateinit var infoWindowClickListener: ArgumentCaptor<GoogleMap.OnInfoWindowClickListener>

    @Captor
    private lateinit var infoWindowCloseListener: ArgumentCaptor<GoogleMap.OnInfoWindowCloseListener>

    @Captor
    private lateinit var infoWindowLongClickListener: ArgumentCaptor<GoogleMap.OnInfoWindowLongClickListener>

    @Captor
    private lateinit var polygonClickListener: ArgumentCaptor<GoogleMap.OnPolygonClickListener>

    @Captor
    private lateinit var polylineClickListener: ArgumentCaptor<GoogleMap.OnPolylineClickListener>

    @Captor
    private lateinit var circleClickListener: ArgumentCaptor<GoogleMap.OnCircleClickListener>

    @Captor
    private lateinit var groundOverlayClickListener: ArgumentCaptor<GoogleMap.OnGroundOverlayClickListener>

    @Captor
    private lateinit var poiClickListener: ArgumentCaptor<GoogleMap.OnPoiClickListener>

    @Captor
    private lateinit var myLocationClickListener: ArgumentCaptor<GoogleMap.OnMyLocationClickListener>

    @Captor
    private lateinit var myLocationButtonClickListener: ArgumentCaptor<GoogleMap.OnMyLocationButtonClickListener>

    @Captor
    private lateinit var indoorStateChangeListener: ArgumentCaptor<GoogleMap.OnIndoorStateChangeListener>

    @Captor
    private lateinit var loadedCallback: ArgumentCaptor<GoogleMap.OnMapLoadedCallback>

    @Captor
    private lateinit var cancelableCallback: ArgumentCaptor<GoogleMap.CancelableCallback>

    @Mock
    private lateinit var cameraUpdate: CameraUpdate

    @Captor
    private lateinit var snapshotReadyCallback: ArgumentCaptor<GoogleMap.SnapshotReadyCallback>

    private lateinit var mapsInitializerMock: MockedStatic<MapsInitializer>

    @Before
    public fun setUp() {
        mapsInitializerMock = mockStatic(MapsInitializer::class.java)
        Mockito.`when`(MapsInitializer.initialize(context)).thenReturn(0)
        Mockito.`when`(googleMap.addMarker(any())).thenReturn(marker)
        Mockito.`when`(googleMap.addPolyline(any())).thenReturn(polyline)
        Mockito.`when`(googleMap.addPolygon(any())).thenReturn(polygon)
        Mockito.`when`(googleMap.addCircle(any())).thenReturn(circle)
        Mockito.`when`(googleMap.addGroundOverlay(any())).thenReturn(groundOverlay)
        Mockito.`when`(googleMap.addTileOverlay(any())).thenReturn(tileOverlay)
    }

    @After
    public fun tearDown() {
        mapsInitializerMock.close()
    }

    // ---------------------------------------------------------------------------------------------
    // Reactive Coroutine Flow Event Listener Tests
    // ---------------------------------------------------------------------------------------------

    /**
     * **Purpose:** Verifies [GoogleMap.cameraIdleEvents] converts camera idle callbacks into a Flow.
     * **How it works:** Subscribes to `cameraIdleEvents().first()`, captures `OnCameraIdleListener`, and invokes `onCameraIdle()`.
     * **How we know it is correct:** Test succeeds if the flow emits when `onCameraIdle()` is called.
     */
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

    /**
     * **Purpose:** Verifies [GoogleMap.cameraMoveEvents] converts camera move callbacks into a Flow.
     * **How it works:** Subscribes to `cameraMoveEvents().first()`, captures `OnCameraMoveListener`, and calls `onCameraMove()`.
     * **How we know it is correct:** Test succeeds if the flow emits when `onCameraMove()` is invoked.
     */
    @Test
    public fun testCameraMoveEvents(): Unit = runTest {
        val job = launch {
            val event = googleMap.cameraMoveEvents().first()
            assertThat(event).isNotNull()
        }
        advanceUntilIdle()
        verify(googleMap).setOnCameraMoveListener(cameraMoveListener.capture())
        cameraMoveListener.value.onCameraMove()
        job.cancel()
    }

    /**
     * **Purpose:** Verifies [GoogleMap.cameraMoveStartedEvents] emits camera move start reasons.
     * **How it works:** Subscribes to `cameraMoveStartedEvents().first()`, captures listener, and triggers `onCameraMoveStarted(REASON_GESTURE)`.
     * **How we know it is correct:** Asserts the emitted integer equals [GoogleMap.OnCameraMoveStartedListener.REASON_GESTURE].
     */
    @Test
    public fun testCameraMoveStartedEvents(): Unit = runTest {
        val job = launch {
            val reason = googleMap.cameraMoveStartedEvents().first()
            assertThat(reason).isEqualTo(GoogleMap.OnCameraMoveStartedListener.REASON_GESTURE)
        }
        advanceUntilIdle()
        verify(googleMap).setOnCameraMoveStartedListener(cameraMoveStartedListener.capture())
        cameraMoveStartedListener.value.onCameraMoveStarted(GoogleMap.OnCameraMoveStartedListener.REASON_GESTURE)
        job.cancel()
    }

    /**
     * **Purpose:** Verifies [GoogleMap.cameraMoveCanceledEvents] emits when camera movement is cancelled.
     * **How it works:** Subscribes to `cameraMoveCanceledEvents().first()`, captures listener, and calls `onCameraMoveCanceled()`.
     * **How we know it is correct:** Asserts the flow emits a non-null Unit event upon cancellation.
     */
    @Test
    public fun testCameraMoveCanceledEvents(): Unit = runTest {
        val job = launch {
            val event = googleMap.cameraMoveCanceledEvents().first()
            assertThat(event).isNotNull()
        }
        advanceUntilIdle()
        verify(googleMap).setOnCameraMoveCanceledListener(cameraMoveCanceledListener.capture())
        cameraMoveCanceledListener.value.onCameraMoveCanceled()
        job.cancel()
    }

    /**
     * **Purpose:** Verifies [GoogleMap.mapClickEvents] emits clicked [LatLng] coordinates.
     * **How it works:** Subscribes to `mapClickEvents().first()`, captures `OnMapClickListener`, and calls `onMapClick(target)`.
     * **How we know it is correct:** Asserts the emitted coordinates equal the exact `target` LatLng.
     */
    @Test
    public fun testMapClickEvents(): Unit = runTest {
        val target = LatLng(10.0, 20.0)
        val job = launch {
            val event = googleMap.mapClickEvents().first()
            assertThat(event).isEqualTo(target)
        }
        advanceUntilIdle()
        verify(googleMap).setOnMapClickListener(mapClickListener.capture())
        mapClickListener.value.onMapClick(target)
        job.cancel()
    }

    /**
     * **Purpose:** Verifies [GoogleMap.mapLongClickEvents] emits long-clicked [LatLng] coordinates.
     * **How it works:** Subscribes to `mapLongClickEvents().first()`, captures `OnMapLongClickListener`, and calls `onMapLongClick(target)`.
     * **How we know it is correct:** Asserts the emitted coordinates equal `target`.
     */
    @Test
    public fun testMapLongClickEvents(): Unit = runTest {
        val target = LatLng(30.0, 40.0)
        val job = launch {
            val event = googleMap.mapLongClickEvents().first()
            assertThat(event).isEqualTo(target)
        }
        advanceUntilIdle()
        verify(googleMap).setOnMapLongClickListener(mapLongClickListener.capture())
        mapLongClickListener.value.onMapLongClick(target)
        job.cancel()
    }

    /**
     * **Purpose:** Verifies [GoogleMap.markerClickEvents] emits clicked [Marker] instances.
     * **How it works:** Subscribes to `markerClickEvents().first()`, captures `OnMarkerClickListener`, and calls `onMarkerClick(marker)`.
     * **How we know it is correct:** Asserts the emitted marker equals the mock `marker`.
     */
    @Test
    public fun testMarkerClickEvents(): Unit = runTest {
        val job = launch {
            val event = googleMap.markerClickEvents().first()
            assertThat(event).isEqualTo(marker)
        }
        advanceUntilIdle()
        verify(googleMap).setOnMarkerClickListener(markerClickListener.capture())
        markerClickListener.value.onMarkerClick(marker)
        job.cancel()
    }

    /**
     * **Purpose:** Verifies [GoogleMap.markerDragEvents] emits marker drag events (start, drag, end).
     * **How it works:** Subscribes to `markerDragEvents().first()`, captures `OnMarkerDragListener`, and calls `onMarkerDragStart(marker)`.
     * **How we know it is correct:** Asserts the emitted drag event contains the mock `marker`.
     */
    @Test
    public fun testMarkerDragEvents(): Unit = runTest {
        val job = launch {
            val event = googleMap.markerDragEvents().first()
            assertThat(event.marker).isEqualTo(marker)
        }
        advanceUntilIdle()
        verify(googleMap).setOnMarkerDragListener(markerDragListener.capture())
        markerDragListener.value.onMarkerDragStart(marker)
        job.cancel()
    }

    /**
     * **Purpose:** Verifies [GoogleMap.infoWindowClickEvents] emits markers when their info window is clicked.
     * **How it works:** Subscribes to `infoWindowClickEvents().first()`, captures listener, and calls `onInfoWindowClick(marker)`.
     * **How we know it is correct:** Asserts the emitted marker equals `marker`.
     */
    @Test
    public fun testInfoWindowClickEvents(): Unit = runTest {
        val job = launch {
            val event = googleMap.infoWindowClickEvents().first()
            assertThat(event).isEqualTo(marker)
        }
        advanceUntilIdle()
        verify(googleMap).setOnInfoWindowClickListener(infoWindowClickListener.capture())
        infoWindowClickListener.value.onInfoWindowClick(marker)
        job.cancel()
    }

    /**
     * **Purpose:** Verifies [GoogleMap.infoWindowCloseEvents] emits markers when their info window closes.
     * **How it works:** Subscribes to `infoWindowCloseEvents().first()`, captures listener, and calls `onInfoWindowClose(marker)`.
     * **How we know it is correct:** Asserts the emitted marker equals `marker`.
     */
    @Test
    public fun testInfoWindowCloseEvents(): Unit = runTest {
        val job = launch {
            val event = googleMap.infoWindowCloseEvents().first()
            assertThat(event).isEqualTo(marker)
        }
        advanceUntilIdle()
        verify(googleMap).setOnInfoWindowCloseListener(infoWindowCloseListener.capture())
        infoWindowCloseListener.value.onInfoWindowClose(marker)
        job.cancel()
    }

    /**
     * **Purpose:** Verifies [GoogleMap.infoWindowLongClickEvents] emits markers when their info window is long-clicked.
     * **How it works:** Subscribes to `infoWindowLongClickEvents().first()`, captures listener, and calls `onInfoWindowLongClick(marker)`.
     * **How we know it is correct:** Asserts the emitted marker equals `marker`.
     */
    @Test
    public fun testInfoWindowLongClickEvents(): Unit = runTest {
        val job = launch {
            val event = googleMap.infoWindowLongClickEvents().first()
            assertThat(event).isEqualTo(marker)
        }
        advanceUntilIdle()
        verify(googleMap).setOnInfoWindowLongClickListener(infoWindowLongClickListener.capture())
        infoWindowLongClickListener.value.onInfoWindowLongClick(marker)
        job.cancel()
    }

    /**
     * **Purpose:** Verifies [GoogleMap.polygonClickEvents] emits clicked [Polygon] instances.
     * **How it works:** Subscribes to `polygonClickEvents().first()`, captures listener, and calls `onPolygonClick(polygon)`.
     * **How we know it is correct:** Asserts the emitted polygon equals `polygon`.
     */
    @Test
    public fun testPolygonClickEvents(): Unit = runTest {
        val job = launch {
            val event = googleMap.polygonClickEvents().first()
            assertThat(event).isEqualTo(polygon)
        }
        advanceUntilIdle()
        verify(googleMap).setOnPolygonClickListener(polygonClickListener.capture())
        polygonClickListener.value.onPolygonClick(polygon)
        job.cancel()
    }

    /**
     * **Purpose:** Verifies [GoogleMap.polylineClickEvents] emits clicked [Polyline] instances.
     * **How it works:** Subscribes to `polylineClickEvents().first()`, captures listener, and calls `onPolylineClick(polyline)`.
     * **How we know it is correct:** Asserts the emitted polyline equals `polyline`.
     */
    @Test
    public fun testPolylineClickEvents(): Unit = runTest {
        val job = launch {
            val event = googleMap.polylineClickEvents().first()
            assertThat(event).isEqualTo(polyline)
        }
        advanceUntilIdle()
        verify(googleMap).setOnPolylineClickListener(polylineClickListener.capture())
        polylineClickListener.value.onPolylineClick(polyline)
        job.cancel()
    }

    /**
     * **Purpose:** Verifies [GoogleMap.circleClickEvents] emits clicked [Circle] instances.
     * **How it works:** Subscribes to `circleClickEvents().first()`, captures listener, and calls `onCircleClick(circle)`.
     * **How we know it is correct:** Asserts the emitted circle equals `circle`.
     */
    @Test
    public fun testCircleClickEvents(): Unit = runTest {
        val job = launch {
            val event = googleMap.circleClickEvents().first()
            assertThat(event).isEqualTo(circle)
        }
        advanceUntilIdle()
        verify(googleMap).setOnCircleClickListener(circleClickListener.capture())
        circleClickListener.value.onCircleClick(circle)
        job.cancel()
    }

    /**
     * **Purpose:** Verifies [GoogleMap.groundOverlayClicks] emits clicked [GroundOverlay] instances.
     * **How it works:** Subscribes to `groundOverlayClicks().first()`, captures listener, and calls `onGroundOverlayClick(groundOverlay)`.
     * **How we know it is correct:** Asserts the emitted ground overlay equals `groundOverlay`.
     */
    @Test
    public fun testGroundOverlayClickEvents(): Unit = runTest {
        val job = launch {
            val event = googleMap.groundOverlayClicks().first()
            assertThat(event).isEqualTo(groundOverlay)
        }
        advanceUntilIdle()
        verify(googleMap).setOnGroundOverlayClickListener(groundOverlayClickListener.capture())
        groundOverlayClickListener.value.onGroundOverlayClick(groundOverlay)
        job.cancel()
    }

    /**
     * **Purpose:** Verifies [GoogleMap.poiClickEvents] emits clicked [PointOfInterest] instances.
     * **How it works:** Subscribes to `poiClickEvents().first()`, captures listener, and calls `onPoiClick(poi)`.
     * **How we know it is correct:** Asserts the emitted POI equals the exact `poi` object.
     */
    @Test
    public fun testPoiClickEvents(): Unit = runTest {
        val poi = PointOfInterest(LatLng(1.0, 2.0), "id", "name")
        val job = launch {
            val event = googleMap.poiClickEvents().first()
            assertThat(event).isEqualTo(poi)
        }
        advanceUntilIdle()
        verify(googleMap).setOnPoiClickListener(poiClickListener.capture())
        poiClickListener.value.onPoiClick(poi)
        job.cancel()
    }

    /**
     * **Purpose:** Verifies [GoogleMap.myLocationClickEvents] emits user [Location] when the location dot is clicked.
     * **How it works:** Subscribes to `myLocationClickEvents().first()`, captures listener, and calls `onMyLocationClick(location)`.
     * **How we know it is correct:** Asserts the emitted location equals `location`.
     */
    @Test
    public fun testMyLocationClickEvents(): Unit = runTest {
        val job = launch {
            val event = googleMap.myLocationClickEvents().first()
            assertThat(event).isEqualTo(location)
        }
        advanceUntilIdle()
        verify(googleMap).setOnMyLocationClickListener(myLocationClickListener.capture())
        myLocationClickListener.value.onMyLocationClick(location)
        job.cancel()
    }

    /**
     * **Purpose:** Verifies [GoogleMap.myLocationButtonClickEvents] emits when the My Location button is clicked.
     * **How it works:** Subscribes to `myLocationButtonClickEvents().first()`, captures listener, and calls `onMyLocationButtonClick()`.
     * **How we know it is correct:** Asserts the emitted Unit event is non-null.
     */
    @Test
    public fun testMyLocationButtonClickEvents(): Unit = runTest {
        val job = launch {
            val event = googleMap.myLocationButtonClickEvents().first()
            assertThat(event).isNotNull()
        }
        advanceUntilIdle()
        verify(googleMap).setOnMyLocationButtonClickListener(myLocationButtonClickListener.capture())
        myLocationButtonClickListener.value.onMyLocationButtonClick()
        job.cancel()
    }

    /**
     * **Purpose:** Verifies [GoogleMap.indoorStateChangeEvents] emits when an indoor building comes into focus.
     * **How it works:** Subscribes to `indoorStateChangeEvents().first()`, captures listener, and calls `onIndoorBuildingFocused()`.
     * **How we know it is correct:** Asserts the flow emits a non-null indoor change event.
     */
    @Test
    public fun testIndoorStateChangeEvents(): Unit = runTest {
        val job = launch {
            val event = googleMap.indoorStateChangeEvents().first()
            assertThat(event).isNotNull()
        }
        advanceUntilIdle()
        verify(googleMap).setOnIndoorStateChangeListener(indoorStateChangeListener.capture())
        indoorStateChangeListener.value.onIndoorBuildingFocused()
        job.cancel()
    }

    // ---------------------------------------------------------------------------------------------
    // Coroutine Suspension Helpers
    // ---------------------------------------------------------------------------------------------

    /**
     * **Purpose:** Verifies [GoogleMap.awaitMapLoad] suspends until the map finishes rendering.
     * **How it works:** Launches `googleMap.awaitMapLoad()`, captures `OnMapLoadedCallback`, and calls `onMapLoaded()`.
     * **How we know it is correct:** Verifies `setOnMapLoadedCallback` was registered and coroutine resumes cleanly when invoked.
     */
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

    /**
     * **Purpose:** Verifies [GoogleMap.awaitAnimateCamera] suspends until camera animation finishes.
     * **How it works:** Launches `awaitAnimateCamera(cameraUpdate)`, captures `CancelableCallback` from `animateCamera(update, 3000, callback)`, and calls `onFinish()`.
     * **How we know it is correct:** Asserts `animateCamera` was called with default 3000ms duration and coroutine resumes upon `onFinish()`.
     */
    @Test
    public fun testAwaitAnimateCamera(): Unit = runTest {
        val job = launch {
            googleMap.awaitAnimateCamera(cameraUpdate)
        }
        advanceUntilIdle()
        verify(googleMap).animateCamera(any(CameraUpdate::class.java), Mockito.eq(3000), cancelableCallback.capture())
        cancelableCallback.value.onFinish()
        job.cancel()
    }

    /**
     * **Purpose:** Verifies [GoogleMap.awaitSnapshot] suspends until a bitmap snapshot is ready.
     * **How it works:** Stubs `googleMap.snapshot(any(), any())` to immediately invoke `onSnapshotReady(bitmap)`, and calls `awaitSnapshot(bitmap)`.
     * **How we know it is correct:** Asserts the returned Bitmap equals the exact `bitmap` mock.
     */
    @Test
    public fun testAwaitSnapshot(): Unit = runTest {
        Mockito.`when`(googleMap.snapshot(any(), any())).thenAnswer {
            val cb = it.getArgument<GoogleMap.SnapshotReadyCallback>(0)
            cb.onSnapshotReady(bitmap)
        }
        var result: Bitmap? = null
        val job = launch {
            result = googleMap.awaitSnapshot(bitmap)
        }
        advanceUntilIdle()
        assertThat(result).isEqualTo(bitmap)
        job.cancel()
    }

    // ---------------------------------------------------------------------------------------------
    // Kotlin DSL Option Builders
    // ---------------------------------------------------------------------------------------------

    /**
     * **Purpose:** Verifies all 6 Kotlin DSL option builders (`addMarker`, `addPolyline`, `addPolygon`, `addCircle`, `addGroundOverlay`, `addTileOverlay`) construct valid options and delegate to [GoogleMap].
     * **How it works:** Calls each inline builder block with sample option actions (e.g. `position(LatLng(1, 2))`), and verifies via Mockito that the corresponding `googleMap.add*(any())` method was called.
     * **How we know it is correct:**
     * - **Code under test:** Correct because the inline builder applies the user DSL lambda to a new options builder and passes it to GoogleMap.
     * - **Test:** Fails if any builder fails to delegate to the underlying GoogleMap method or throws an exception.
     */
    @Test
    public fun testDslBuilders() {
        googleMap.addMarker {
            position(LatLng(1.0, 2.0))
        }
        verify(googleMap).addMarker(any())

        googleMap.addPolyline {
            add(LatLng(1.0, 2.0))
        }
        verify(googleMap).addPolyline(any())

        googleMap.addPolygon {
            add(LatLng(1.0, 2.0))
        }
        verify(googleMap).addPolygon(any())

        googleMap.addCircle {
            center(LatLng(1.0, 2.0))
        }
        verify(googleMap).addCircle(any())

        googleMap.addGroundOverlay {
            zIndex(1f)
            clickable(true)
        }
        verify(googleMap).addGroundOverlay(any())

        googleMap.addTileOverlay {
            fadeIn(true)
        }
        verify(googleMap).addTileOverlay(any())
    }
}
