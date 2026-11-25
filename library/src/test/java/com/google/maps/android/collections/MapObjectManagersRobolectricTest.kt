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
package com.google.maps.android.collections

import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.Circle
import com.google.android.gms.maps.model.GroundOverlay
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.Polygon
import com.google.android.gms.maps.model.Polyline
import com.google.maps.android.markerClickEvents
import com.google.maps.android.robolectric.MapsRobolectricTestRunner
import com.google.maps.android.robolectric.ktx.click
import com.google.maps.android.robolectric.ktx.clickInfoWindow
import com.google.maps.android.robolectric.ktx.createTestMap
import com.google.maps.android.robolectric.ktx.longClickInfoWindow
import com.google.maps.android.robolectric.ktx.markers
import com.google.maps.android.robolectric.powerassert.assertGeo
import com.google.maps.android.robolectric.powerassert.assertMap
import com.google.maps.android.robolectric.powerassert.assertMarker
import com.google.maps.android.withSphericalOffset
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import org.robolectric.shadows.ShadowLooper

/**
 * Stateful Robolectric verification suite for [MarkerManager], [PolygonManager],
 * [PolylineManager], [CircleManager], and [GroundOverlayManager] collection isolation,
 * reactive Flow routing, and synchronous UI-thread listener registration.
 */
@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(MapsRobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [34])
class MapObjectManagersRobolectricTest {

    private lateinit var googleMap: GoogleMap

    @Before
    fun setUp() {
        googleMap = createTestMap(width = 400, height = 400)
    }

    @Test
    fun markerManager_isolatesCollectionsVisibilityClearingAndClickFlows() = runTest {
        val dispatcher = UnconfinedTestDispatcher(testScheduler)
        val center = LatLng(47.6062, -122.3321)
        val markerManager = MarkerManager(googleMap)
        ShadowLooper.idleMainLooper()

        val hotels = markerManager.newCollection("hotels")
        val restaurants = markerManager.newCollection("restaurants")

        val hotelMarker = hotels.addMarker {
            position(center)
            title("Hotel")
        }
        val restaurantMarker = restaurants.addMarker {
            position(center.withSphericalOffset(100.0, 90.0))
            title("Restaurant")
        }

        val hotelClicks = mutableListOf<Marker>()
        val restaurantClicks = mutableListOf<Marker>()
        val hotelInfoClicks = mutableListOf<Marker>()
        val hotelInfoLongClicks = mutableListOf<Marker>()

        val jobs = listOf(
            launch(dispatcher) { hotels.clickEvents().toList(hotelClicks) },
            launch(dispatcher) { restaurants.clickEvents().toList(restaurantClicks) },
            launch(dispatcher) { hotels.infoWindowClickEvents().toList(hotelInfoClicks) },
            launch(dispatcher) { hotels.infoWindowLongClickEvents().toList(hotelInfoLongClicks) },
        )

        hotelMarker.click()
        hotelMarker.clickInfoWindow()
        hotelMarker.longClickInfoWindow()

        assertGeo(
            hotelClicks == listOf(hotelMarker) &&
                restaurantClicks.isEmpty() &&
                hotelInfoClicks == listOf(hotelMarker) &&
                hotelInfoLongClicks == listOf(hotelMarker),
        )

        restaurantMarker.click()
        assertGeo(restaurantClicks == listOf(restaurantMarker))

        hotels.hideAll()
        hotelMarker.assertMarker(!hotelMarker.isVisible)
        restaurantMarker.assertMarker(restaurantMarker.isVisible)

        hotels.showAll()
        hotelMarker.assertMarker(hotelMarker.isVisible)

        hotels.clear()
        googleMap.assertMap(googleMap.markers == listOf(restaurantMarker))
        jobs.forEach { it.cancel() }
    }

    @Test
    fun shapeAndGroundOverlayManagers_routeCollectionClickFlowsIndependently() = runTest {
        val dispatcher = UnconfinedTestDispatcher(testScheduler)
        val center = LatLng(47.6062, -122.3321)

        val polygonManager = PolygonManager(googleMap)
        val polylineManager = PolylineManager(googleMap)
        val circleManager = CircleManager(googleMap)
        val groundOverlayManager = GroundOverlayManager(googleMap)
        ShadowLooper.idleMainLooper()

        val polyCol = polygonManager.newCollection()
        val lineCol = polylineManager.newCollection()
        val circleCol = circleManager.newCollection()
        val groundCol = groundOverlayManager.newCollection()

        val polygon = polyCol.addPolygon {
            add(center, center.withSphericalOffset(50.0, 0.0), center.withSphericalOffset(50.0, 90.0))
            clickable(true)
        }
        val polyline = lineCol.addPolyline {
            add(center, center.withSphericalOffset(100.0, 180.0))
            clickable(true)
        }
        val circle = circleCol.addCircle {
            center(center)
            radius(75.0)
            clickable(true)
        }
        val groundOverlay = groundCol.addGroundOverlay {
            position(center, 60f)
            clickable(true)
        }

        val polyClicks = mutableListOf<Polygon>()
        val lineClicks = mutableListOf<Polyline>()
        val circleClicks = mutableListOf<Circle>()
        val groundClicks = mutableListOf<GroundOverlay>()

        val jobs = listOf(
            launch(dispatcher) { polyCol.clickEvents().toList(polyClicks) },
            launch(dispatcher) { lineCol.clickEvents().toList(lineClicks) },
            launch(dispatcher) { circleCol.clickEvents().toList(circleClicks) },
            launch(dispatcher) { groundCol.clickEvents().toList(groundClicks) },
        )

        polygon.click()
        polyline.click()
        circle.click()
        groundOverlay.click()

        assertGeo(
            polyClicks == listOf(polygon) &&
                lineClicks == listOf(polyline) &&
                circleClicks == listOf(circle) &&
                groundClicks == listOf(groundOverlay),
        )
        jobs.forEach { it.cancel() }
    }

    /**
     * Verifies that constructing [MarkerManager] on the main thread registers its listeners
     * synchronously rather than posting a delayed init runnable that would drop immediate clicks
     * or clobber a subsequently attached `googleMap.markerClickEvents()` collector.
     */
    @Test
    fun markerManager_registersListenersSynchronouslyOnMainThreadWithoutClobberingSubsequentFlows() = runTest {
        val dispatcher = UnconfinedTestDispatcher(testScheduler)
        val point = LatLng(37.7749, -122.4194)

        val markerManager = MarkerManager(googleMap)
        val collection = markerManager.newCollection()
        val marker = collection.addMarker(MarkerOptions().position(point).title("San Francisco"))

        val managerClicks = mutableListOf<Marker>()
        collection.setOnMarkerClickListener {
            managerClicks.add(it)
            true
        }

        // Immediate click before idling the main looper routes synchronously to MarkerManager
        marker.click()
        assertGeo(managerClicks == listOf(marker))

        // Subscribing to direct googleMap.markerClickEvents() afterward is not overwritten when looper drains
        val directFlowClicks = mutableListOf<Marker>()
        val flowJob = launch(dispatcher) {
            googleMap.markerClickEvents().toList(directFlowClicks)
        }

        marker.click()
        assertGeo(directFlowClicks == listOf(marker))

        ShadowLooper.idleMainLooper()
        marker.click()
        assertGeo(directFlowClicks.size == 2)
        flowJob.cancel()
    }
}
