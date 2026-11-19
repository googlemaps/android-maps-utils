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
import com.google.maps.android.robolectric.ktx.createTestMapView
import com.google.maps.android.robolectric.ktx.getMapBlocking
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
import com.google.maps.android.testing.golden.GoldenTileProvider
import com.google.maps.android.testing.golden.GoldenTileStrategy
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import org.robolectric.shadows.ShadowLooper
import com.google.common.truth.Truth.assertThat as truthAssertThat

/**
 * Headless JVM Robolectric verification suite for `:library`'s KTX coroutine lifecycle bridges
 * (`awaitMap`, `awaitAnimateCamera`) and overlay builder DSLs on a stateful [GoogleMap].
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
        truthAssertThat(mapView.shadow.isOnCreateCalled).isTrue()
        truthAssertThat(mapView.shadow.isOnStartCalled).isTrue()
        truthAssertThat(mapView.shadow.isOnResumeCalled).isTrue()

        val deferredFromView = async(UnconfinedTestDispatcher(testScheduler)) { mapView.awaitMap() }
        ShadowLooper.idleMainLooper()
        truthAssertThat(deferredFromView.await()).isSameInstanceAs(googleMap)

        val options = buildGoogleMapOptions {
            mapType(GoogleMap.MAP_TYPE_HYBRID)
            compassEnabled(true)
        }
        val fragment = SupportMapFragment.newInstance(options)
        val deferredFromFragment = async(UnconfinedTestDispatcher(testScheduler)) { fragment.awaitMap() }
        ShadowLooper.idleMainLooper()
        truthAssertThat(deferredFromFragment.await()).isNotNull()
    }

    @Test
    fun markerPolygonAndPolylineDsls_populateMapAndSatisfyDomainSubjects() = runTest {
        val boulder = LatLng(40.0150, -105.2705)
        googleMap.awaitAnimateCamera(CameraUpdateFactory.newLatLngZoom(boulder, 14f), durationMs = 250)

        assertThat(googleMap)
            .isCenteredOn(boulder, tolerance = 1.meters)
            .hasZoom(14f)

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
        assertThat(googleMap).hasMarkerCount(1)
        assertThat(marker)
            .isAt(boulder, tolerance = 1.meters)
            .hasTitle("Boulder")
            .hasSnippet("Colorado")
            .hasAlpha(0.85f)
            .hasRotation(45f)
            .hasZIndex(2f)
            .isDraggable()
            .isFlat()

        val triangle = listOf(
            boulder,
            boulder.withSphericalOffset(500.0, 90.0),
            boulder.withSphericalOffset(500.0, 180.0),
        )
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

        assertThat(googleMap).hasPolygonCount(1).hasPolylineCount(1)
        assertThat(polygon).hasPointCount(3).hasFillColor(Color.GREEN).hasStrokeWidth(6f)
        assertThat(polyline).hasPointCount(3).startsAt(boulder, tolerance = 1.meters)
        truthAssertThat(polygon.area).isGreaterThan(100_000.0)
        truthAssertThat(polyline.sphericalPathLength).isWithin(15.0).of(1207.11)
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
        assertThat(googleMap).hasCircleCount(1)
        assertThat(circle).isCenteredOn(boulder, tolerance = 1.meters).hasRadius(250.0).hasFillColor(Color.YELLOW)

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
                tileProvider(GoldenTileProvider(strategy = GoldenTileStrategy.Checkerboard()))
                fadeIn(false)
                zIndex(10f)
            },
        )
        tileOverlay.clearTileCache()

        assertThat(googleMap).hasGroundOverlayCount(1).hasTileOverlayCount(1)
        truthAssertThat(groundOverlay.transparency).isWithin(0.001f).of(0.2f)
        truthAssertThat(tileOverlay.shadow.isTileCacheCleared).isTrue()
    }
}
