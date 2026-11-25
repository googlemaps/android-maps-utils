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

import android.graphics.Bitmap
import android.graphics.Color
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.TileProvider
import com.google.maps.android.robolectric.MapsRobolectricTestRunner
import com.google.maps.android.robolectric.ktx.circles
import com.google.maps.android.robolectric.ktx.createTestMapView
import com.google.maps.android.robolectric.ktx.getMapBlocking
import com.google.maps.android.robolectric.ktx.groundOverlays
import com.google.maps.android.robolectric.ktx.markers
import com.google.maps.android.robolectric.ktx.polygons
import com.google.maps.android.robolectric.ktx.polylines
import com.google.maps.android.robolectric.ktx.shadow
import com.google.maps.android.robolectric.ktx.tileOverlays
import com.google.maps.android.robolectric.powerassert.assertGeo
import com.google.maps.android.robolectric.powerassert.assertMap
import com.google.maps.android.robolectric.powerassert.assertMarker
import com.google.maps.android.robolectric.powerassert.connects
import com.google.maps.android.robolectric.powerassert.distanceTo
import com.google.maps.android.robolectric.powerassert.isCenteredOn
import com.google.maps.android.robolectric.powerassert.meters
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import org.robolectric.shadows.ShadowLooper
import kotlin.math.abs

/**
 * Headless JVM Robolectric verification suite for `:library`'s KTX coroutine lifecycle bridges
 * (`awaitMap`, `awaitAnimateCamera`) and overlay builder DSLs on a stateful [GoogleMap].
 */
@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(MapsRobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [34])
class GoogleMapDslRobolectricTest {

    private lateinit var mapView: MapView
    private lateinit var googleMap: GoogleMap

    @Before
    fun setUp() {
        mapView = createTestMapView(width = 400, height = 400).apply {
            onStart()
            onResume()
        }
        googleMap = mapView.getMapBlocking()
    }

    @Test
    fun awaitMap_onMapViewAndSupportMapFragment_resolvesStatefulGoogleMap() = runTest {
        assertGeo(
            mapView.shadow.isOnCreateCalled &&
                mapView.shadow.isOnStartCalled &&
                mapView.shadow.isOnResumeCalled,
        )

        val deferredFromView = async(UnconfinedTestDispatcher(testScheduler)) { mapView.awaitMap() }
        ShadowLooper.idleMainLooper()
        assertGeo(deferredFromView.await() === googleMap)

        val options = buildGoogleMapOptions {
            mapType(GoogleMap.MAP_TYPE_HYBRID)
            compassEnabled(true)
        }
        val fragment = SupportMapFragment.newInstance(options)
        val deferredFromFragment = async(UnconfinedTestDispatcher(testScheduler)) { fragment.awaitMap() }
        ShadowLooper.idleMainLooper()
        val fragmentMap = deferredFromFragment.await()
        fragmentMap.assertMap(fragmentMap.markers.isEmpty())
    }

    @Test
    fun markerPolygonAndPolylineDsls_populateMapWithExpectedGeometry() = runTest {
        val boulder = LatLng(40.0150, -105.2705)
        googleMap.awaitAnimateCamera(CameraUpdateFactory.newLatLngZoom(boulder, 14f), durationMs = 250)

        googleMap.assertMap(googleMap.isCenteredOn(boulder, expectedZoom = 14f, tolerance = 1.meters))

        val marker = checkNotNull(
            googleMap.addMarker {
                position(boulder)
                title("Boulder")
                snippet("Colorado")
                alpha(0.85f)
                rotation(45f)
                zIndex(2f)
                draggable(true)
                flat(true)
            },
        )
        googleMap.assertMap(googleMap.markers.size == 1)
        marker.assertMarker(
            (marker.position distanceTo boulder) <= 1.meters &&
                marker.title == "Boulder" &&
                marker.snippet == "Colorado" &&
                marker.alpha == 0.85f &&
                marker.rotation == 45f &&
                marker.zIndex == 2f &&
                marker.isDraggable &&
                marker.isFlat,
        )

        val east = boulder.withSphericalOffset(500.0, 90.0)
        val south = boulder.withSphericalOffset(500.0, 180.0)
        val triangle = listOf(boulder, east, south)
        val polygon = googleMap.addPolygon {
            addAll(triangle)
            fillColor(Color.GREEN)
            strokeColor(Color.DKGRAY)
            strokeWidth(6f)
            clickable(true)
            geodesic(true)
        }
        val polyline = googleMap.addPolyline {
            addAll(triangle)
            color(Color.BLUE)
            width(8f)
            clickable(true)
        }

        googleMap.assertMap(googleMap.polygons.size == 1 && googleMap.polylines.size == 1)
        assertGeo(
            polygon.points.size == 3 &&
                polygon.fillColor == Color.GREEN &&
                polygon.strokeWidth == 6f &&
                polygon.area > 100_000.0,
        )
        assertGeo(
            polyline.points.size == 3 &&
                polyline.connects(boulder, south, tolerance = 1.meters) &&
                abs(polyline.sphericalPathLength - 1207.11) <= 15.0,
        )
    }

    @Test
    fun circleGroundOverlayAndTileOverlayDsls_registerStatefulOverlays() {
        val boulder = LatLng(40.0150, -105.2705)
        val circle = googleMap.addCircle {
            center(boulder)
            radius(250.0)
            fillColor(Color.YELLOW)
            strokeColor(Color.BLACK)
            clickable(true)
        }
        googleMap.assertMap(googleMap.circles.size == 1)
        assertGeo(
            (circle.center distanceTo boulder) <= 1.meters &&
                circle.radius == 250.0 &&
                circle.fillColor == Color.YELLOW,
        )

        val overlayBitmap = Bitmap.createBitmap(32, 32, Bitmap.Config.ARGB_8888).apply { eraseColor(Color.MAGENTA) }
        val groundOverlay = checkNotNull(
            googleMap.addGroundOverlay {
                image(BitmapDescriptorFactory.fromBitmap(overlayBitmap))
                position(boulder, 100f, 100f)
                clickable(true)
                transparency(0.2f)
            },
        )
        val tileOverlay = checkNotNull(
            googleMap.addTileOverlay {
                tileProvider(TileProvider { _, _, _ -> TileProvider.NO_TILE })
                fadeIn(false)
                zIndex(10f)
            },
        )
        tileOverlay.clearTileCache()

        googleMap.assertMap(googleMap.groundOverlays.size == 1 && googleMap.tileOverlays.size == 1)
        assertGeo(
            abs(groundOverlay.transparency - 0.2f) <= 0.001f &&
                tileOverlay.shadow.isTileCacheCleared,
        )
    }
}
