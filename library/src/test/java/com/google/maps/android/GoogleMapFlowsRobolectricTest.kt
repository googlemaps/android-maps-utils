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
import com.google.maps.android.robolectric.ktx.click
import com.google.maps.android.robolectric.ktx.clickInfoWindow
import com.google.maps.android.robolectric.ktx.clickMap
import com.google.maps.android.robolectric.ktx.createTestMap
import com.google.maps.android.robolectric.ktx.dragTo
import com.google.maps.android.robolectric.ktx.longClickInfoWindow
import com.google.maps.android.robolectric.ktx.longClickMap
import com.google.maps.android.robolectric.ktx.shadow
import com.google.maps.android.robolectric.shadows.ShadowBitmapDescriptor
import com.google.maps.android.robolectric.shadows.ShadowBitmapDescriptorFactory
import com.google.maps.android.robolectric.shadows.ShadowCameraUpdate
import com.google.maps.android.robolectric.shadows.ShadowCameraUpdateFactory
import com.google.maps.android.robolectric.shadows.ShadowCircle
import com.google.maps.android.robolectric.shadows.ShadowGoogleMap
import com.google.maps.android.robolectric.shadows.ShadowGroundOverlay
import com.google.maps.android.robolectric.shadows.ShadowMapView
import com.google.maps.android.robolectric.shadows.ShadowMapsInitializer
import com.google.maps.android.robolectric.shadows.ShadowMarker
import com.google.maps.android.robolectric.shadows.ShadowPolygon
import com.google.maps.android.robolectric.shadows.ShadowPolyline
import com.google.maps.android.robolectric.shadows.ShadowProjection
import com.google.maps.android.robolectric.shadows.ShadowSupportMapFragment
import com.google.maps.android.robolectric.shadows.ShadowTileOverlay
import com.google.maps.android.robolectric.shadows.ShadowUiSettings
import com.google.maps.android.robolectric.truth.assertThat
import com.google.maps.android.robolectric.truth.meters
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import com.google.common.truth.Truth.assertThat as truthAssertThat

/**
 * Headless JVM Robolectric verification suite for `:library`'s [GoogleMap] cold `callbackFlow`
 * event streams and listener unregistration on cancellation.
 *
 * Note: Maps shadows are declared directly on `@Config(shadows = [...])` because Robolectric's
 * `ConfigMerger` does not traverse meta-annotations, avoiding global `robolectric.properties` pollution.
 */
@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(
    sdk = [34],
    shadows = [
        ShadowMapView::class,
        ShadowGoogleMap::class,
        ShadowCameraUpdate::class,
        ShadowCameraUpdateFactory::class,
        ShadowMarker::class,
        ShadowPolygon::class,
        ShadowPolyline::class,
        ShadowProjection::class,
        ShadowCircle::class,
        ShadowUiSettings::class,
        ShadowBitmapDescriptor::class,
        ShadowBitmapDescriptorFactory::class,
        ShadowMapsInitializer::class,
        ShadowSupportMapFragment::class,
        ShadowGroundOverlay::class,
        ShadowTileOverlay::class,
    ],
)
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

        truthAssertThat(moveStartedReasons).containsExactly(
            GoogleMap.OnCameraMoveStartedListener.REASON_DEVELOPER_ANIMATION,
        )
        truthAssertThat(moveEvents).hasSize(1)
        truthAssertThat(idleEvents).hasSize(1)
        truthAssertThat(canceledEvents).hasSize(1)
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
        truthAssertThat(clicks).containsExactly(origin)
        truthAssertThat(longClicks).containsExactly(origin)

        clickJob.cancel()
        longClickJob.cancel()
        googleMap.clickMap(origin)
        googleMap.longClickMap(origin)
        truthAssertThat(clicks).hasSize(1)
        truthAssertThat(longClicks).hasSize(1)
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
        assertThat(marker).isInfoWindowShown()
        marker.clickInfoWindow()
        marker.longClickInfoWindow()
        marker.hideInfoWindow()
        assertThat(marker).isInfoWindowHidden().isAt(destination, tolerance = 1.meters)

        truthAssertThat(markerClicks).containsExactly(marker)
        truthAssertThat(dragEvents).containsExactly(
            MarkerDragStartEvent(marker),
            MarkerDragEvent(marker),
            MarkerDragEndEvent(marker),
        ).inOrder()
        truthAssertThat(infoClicks).containsExactly(marker)
        truthAssertThat(infoLongClicks).containsExactly(marker)
        truthAssertThat(infoCloses).containsExactly(marker)
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
        truthAssertThat(googleMap.shadow.clickMyLocationButton()).isTrue()
        val location = Location("gps").apply {
            latitude = origin.latitude
            longitude = origin.longitude
        }
        googleMap.shadow.clickMyLocation(location)

        truthAssertThat(clickedPolygons).containsExactly(polygon)
        truthAssertThat(clickedPolylines).containsExactly(polyline)
        truthAssertThat(clickedCircles).containsExactly(circle)
        truthAssertThat(clickedOverlays).containsExactly(groundOverlay)
        truthAssertThat(clickedPois).containsExactly(poi)
        truthAssertThat(myLocationBtnClicks).hasSize(1)
        truthAssertThat(myLocationClicks).containsExactly(location)
        jobs.forEach { it.cancel() }

        val mapLoadJob = async(dispatcher) { googleMap.awaitMapLoad() }
        googleMap.shadow.notifyMapLoaded()
        mapLoadJob.await()
    }
}
