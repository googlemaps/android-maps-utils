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

import android.location.Location
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.Circle
import com.google.android.gms.maps.model.GroundOverlay
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.PointOfInterest
import com.google.android.gms.maps.model.Polygon
import com.google.android.gms.maps.model.Polyline
import com.google.maps.android.robolectric.MapsRobolectricTestRunner
import com.google.maps.android.robolectric.ktx.click
import com.google.maps.android.robolectric.ktx.clickInfoWindow
import com.google.maps.android.robolectric.ktx.clickMap
import com.google.maps.android.robolectric.ktx.createTestMap
import com.google.maps.android.robolectric.ktx.dragTo
import com.google.maps.android.robolectric.ktx.longClickInfoWindow
import com.google.maps.android.robolectric.ktx.longClickMap
import com.google.maps.android.robolectric.ktx.shadow
import com.google.maps.android.robolectric.powerassert.assertGeo
import com.google.maps.android.robolectric.powerassert.assertMarker
import com.google.maps.android.robolectric.powerassert.distanceTo
import com.google.maps.android.robolectric.powerassert.meters
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * Headless JVM Robolectric verification suite for `:library`'s [GoogleMap] cold `callbackFlow`
 * event streams and listener unregistration on cancellation.
 */
@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(MapsRobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [34])
class GoogleMapFlowsRobolectricTest {

    private lateinit var googleMap: GoogleMap

    @Before
    fun setUp() {
        googleMap = createTestMap(width = 400, height = 400)
    }

    @Test
    fun cameraEventFlows_emitMoveStartedMoveIdleAndCanceledEvents() = runTest {
        val dispatcher = UnconfinedTestDispatcher(testScheduler)
        val origin = LatLng(37.4220, -122.0841)
        val moveStartedReasons = mutableListOf<Int>()
        val moveEvents = mutableListOf<Unit>()
        val idleEvents = mutableListOf<Unit>()
        val canceledEvents = mutableListOf<Unit>()

        val jobs = listOf(
            launch(dispatcher) { googleMap.cameraMoveStartedEvents().toList(moveStartedReasons) },
            launch(dispatcher) { googleMap.cameraMoveEvents().toList(moveEvents) },
            launch(dispatcher) { googleMap.cameraIdleEvents().toList(idleEvents) },
            launch(dispatcher) { googleMap.cameraMoveCanceledEvents().toList(canceledEvents) },
        )

        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(origin, 15f))
        googleMap.shadow.cancelCameraMove()

        assertGeo(
            moveStartedReasons == listOf(GoogleMap.OnCameraMoveStartedListener.REASON_DEVELOPER_ANIMATION) &&
                moveEvents.size == 1 &&
                idleEvents.size == 1 &&
                canceledEvents.size == 1,
        )
        jobs.forEach { it.cancel() }
    }

    @Test
    fun mapClickAndLongClickFlows_emitEventsAndUnregisterOnCancellation() = runTest {
        val dispatcher = UnconfinedTestDispatcher(testScheduler)
        val origin = LatLng(37.4220, -122.0841)
        val clicks = mutableListOf<LatLng>()
        val longClicks = mutableListOf<LatLng>()

        val clickJob = launch(dispatcher) { googleMap.mapClickEvents().toList(clicks) }
        val longClickJob = launch(dispatcher) { googleMap.mapLongClickEvents().toList(longClicks) }

        googleMap.clickMap(origin)
        googleMap.longClickMap(origin)
        assertGeo(clicks == listOf(origin) && longClicks == listOf(origin))

        clickJob.cancel()
        longClickJob.cancel()
        googleMap.clickMap(origin)
        googleMap.longClickMap(origin)
        assertGeo(clicks.size == 1 && longClicks.size == 1)
    }

    @Test
    fun markerEventFlows_emitClickDragAndInfoWindowLifecycleEvents() = runTest {
        val dispatcher = UnconfinedTestDispatcher(testScheduler)
        val origin = LatLng(37.4220, -122.0841)
        val destination = origin.withSphericalOffset(100.0, 45.0)
        val marker = checkNotNull(googleMap.addMarker { position(origin).title("Googleplex") })

        val markerClicks = mutableListOf<Marker>()
        val dragEvents = mutableListOf<OnMarkerDragEvent>()
        val infoClicks = mutableListOf<Marker>()
        val infoLongClicks = mutableListOf<Marker>()
        val infoCloses = mutableListOf<Marker>()

        val jobs = listOf(
            launch(dispatcher) { googleMap.markerClickEvents().toList(markerClicks) },
            launch(dispatcher) { googleMap.markerDragEvents().toList(dragEvents) },
            launch(dispatcher) { googleMap.infoWindowClickEvents().toList(infoClicks) },
            launch(dispatcher) { googleMap.infoWindowLongClickEvents().toList(infoLongClicks) },
            launch(dispatcher) { googleMap.infoWindowCloseEvents().toList(infoCloses) },
        )

        marker.click()
        marker.dragTo(destination)
        marker.showInfoWindow()
        marker.assertMarker(marker.isInfoWindowShown)
        marker.clickInfoWindow()
        marker.longClickInfoWindow()
        marker.hideInfoWindow()
        marker.assertMarker(!marker.isInfoWindowShown && (marker.position distanceTo destination) <= 1.meters)

        assertGeo(
            markerClicks == listOf(marker) &&
                dragEvents == listOf(
                    MarkerDragStartEvent(marker),
                    MarkerDragEvent(marker),
                    MarkerDragEndEvent(marker),
                ) &&
                infoClicks == listOf(marker) &&
                infoLongClicks == listOf(marker) &&
                infoCloses == listOf(marker),
        )
        jobs.forEach { it.cancel() }
    }

    @Test
    fun overlayPoiMyLocationAndMapLoadFlows_emitExpectedCallbacks() = runTest {
        val dispatcher = UnconfinedTestDispatcher(testScheduler)
        val origin = LatLng(37.4220, -122.0841)
        val east = origin.withSphericalOffset(80.0, 90.0)
        val north = origin.withSphericalOffset(80.0, 0.0)

        val polygon = googleMap.addPolygon { add(origin, east, north).clickable(true) }
        val polyline = googleMap.addPolyline { add(origin, east).clickable(true) }
        val circle = googleMap.addCircle { center(origin).radius(100.0).clickable(true) }
        val groundOverlay = checkNotNull(googleMap.addGroundOverlay { position(origin, 50f).clickable(true) })

        val clickedPolygons = mutableListOf<Polygon>()
        val clickedPolylines = mutableListOf<Polyline>()
        val clickedCircles = mutableListOf<Circle>()
        val clickedOverlays = mutableListOf<GroundOverlay>()
        val clickedPois = mutableListOf<PointOfInterest>()
        val myLocationBtnClicks = mutableListOf<Unit>()
        val myLocationClicks = mutableListOf<Location>()

        val jobs = listOf(
            launch(dispatcher) { googleMap.polygonClickEvents().toList(clickedPolygons) },
            launch(dispatcher) { googleMap.polylineClickEvents().toList(clickedPolylines) },
            launch(dispatcher) { googleMap.circleClickEvents().toList(clickedCircles) },
            launch(dispatcher) { googleMap.groundOverlayClicks().toList(clickedOverlays) },
            launch(dispatcher) { googleMap.poiClickEvents().toList(clickedPois) },
            launch(dispatcher) { googleMap.myLocationButtonClickEvents().toList(myLocationBtnClicks) },
            launch(dispatcher) { googleMap.myLocationClickEvents().toList(myLocationClicks) },
        )

        polygon.click()
        polyline.click()
        circle.click()
        groundOverlay.click()
        val poi = PointOfInterest(origin, "place_id_123", "Amphitheatre")
        googleMap.shadow.clickPoi(poi)
        val myLocationBtnHandled = googleMap.shadow.clickMyLocationButton()
        val location = Location("gps").apply {
            latitude = origin.latitude
            longitude = origin.longitude
        }
        googleMap.shadow.clickMyLocation(location)

        assertGeo(
            myLocationBtnHandled &&
                clickedPolygons == listOf(polygon) &&
                clickedPolylines == listOf(polyline) &&
                clickedCircles == listOf(circle) &&
                clickedOverlays == listOf(groundOverlay) &&
                clickedPois == listOf(poi) &&
                myLocationBtnClicks.size == 1 &&
                myLocationClicks == listOf(location),
        )
        jobs.forEach { it.cancel() }

        val mapLoadJob = async(dispatcher) { googleMap.awaitMapLoad() }
        googleMap.shadow.notifyMapLoaded()
        mapLoadJob.await()
    }
}
