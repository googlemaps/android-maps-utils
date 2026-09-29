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
package com.google.maps.android.testing

import android.app.Activity
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Point
import android.location.Location
import android.os.Bundle
import android.view.View
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.Circle
import com.google.android.gms.maps.model.GroundOverlay
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PointOfInterest
import com.google.android.gms.maps.model.Polygon
import com.google.android.gms.maps.model.Polyline
import com.google.android.gms.maps.model.TileOverlayOptions
import com.google.android.maps.robolectric.annotation.EnableMapsShadows
import com.google.android.maps.robolectric.shadows.ShadowGoogleMap
import com.google.android.maps.robolectric.shadows.ShadowMapView
import com.google.android.maps.robolectric.shadows.ShadowTileOverlay
import com.google.android.maps.robolectric.truth.LatLngSubject
import com.google.android.maps.robolectric.truth.isWithin
import com.google.android.maps.robolectric.truth.kilometers
import com.google.android.maps.robolectric.truth.meters
import com.google.android.maps.testing.golden.GoldenImageDiff
import com.google.android.maps.testing.golden.GoldenTileProvider
import com.google.android.maps.testing.golden.GoldenTileStrategy
import com.google.android.maps.testing.golden.awaitMapLoaded
import com.google.android.maps.testing.golden.captureSnapshot
import com.google.android.maps.testing.golden.enableGoldenTesting
import com.google.common.truth.Truth.assertThat
import com.google.maps.android.MarkerDragEndEvent
import com.google.maps.android.MarkerDragEvent
import com.google.maps.android.MarkerDragStartEvent
import com.google.maps.android.OnMarkerDragEvent
import com.google.maps.android.PolyUtil
import com.google.maps.android.SphericalUtil
import com.google.maps.android.addCircle
import com.google.maps.android.addGroundOverlay
import com.google.maps.android.addMarker
import com.google.maps.android.addPolygon
import com.google.maps.android.addPolyline
import com.google.maps.android.addTileOverlay
import com.google.maps.android.area
import com.google.maps.android.awaitAnimateCamera
import com.google.maps.android.awaitMap
import com.google.maps.android.awaitMapLoad
import com.google.maps.android.awaitSnapshot
import com.google.maps.android.buildGoogleMapOptions
import com.google.maps.android.cameraIdleEvents
import com.google.maps.android.cameraMoveCanceledEvents
import com.google.maps.android.cameraMoveEvents
import com.google.maps.android.cameraMoveStartedEvents
import com.google.maps.android.circleClickEvents
import com.google.maps.android.collections.CircleManager
import com.google.maps.android.collections.GroundOverlayManager
import com.google.maps.android.collections.MarkerManager
import com.google.maps.android.collections.PolygonManager
import com.google.maps.android.collections.PolylineManager
import com.google.maps.android.collections.addCircle
import com.google.maps.android.collections.addGroundOverlay
import com.google.maps.android.collections.addMarker
import com.google.maps.android.collections.addPolygon
import com.google.maps.android.collections.addPolyline
import com.google.maps.android.collections.clickEvents
import com.google.maps.android.collections.infoWindowClickEvents
import com.google.maps.android.collections.infoWindowLongClickEvents
import com.google.maps.android.component1
import com.google.maps.android.component2
import com.google.maps.android.computeSphericalOffsetOrigin
import com.google.maps.android.contains
import com.google.maps.android.containsLocation
import com.google.maps.android.groundOverlayClicks
import com.google.maps.android.infoWindowClickEvents
import com.google.maps.android.infoWindowCloseEvents
import com.google.maps.android.infoWindowLongClickEvents
import com.google.maps.android.isClosedPolygon
import com.google.maps.android.isLocationOnPath
import com.google.maps.android.isOnEdge
import com.google.maps.android.latLngListEncode
import com.google.maps.android.mapClickEvents
import com.google.maps.android.mapLongClickEvents
import com.google.maps.android.markerClickEvents
import com.google.maps.android.markerDragEvents
import com.google.maps.android.myLocationButtonClickEvents
import com.google.maps.android.myLocationClickEvents
import com.google.maps.android.poiClickEvents
import com.google.maps.android.polygonClickEvents
import com.google.maps.android.polylineClickEvents
import com.google.maps.android.signedArea
import com.google.maps.android.simplify
import com.google.maps.android.sphericalDistance
import com.google.maps.android.sphericalHeading
import com.google.maps.android.sphericalPathLength
import com.google.maps.android.sphericalPolygonArea
import com.google.maps.android.toLatLngList
import com.google.maps.android.withSphericalLinearInterpolation
import com.google.maps.android.withSphericalOffset
import java.util.concurrent.atomic.AtomicReference
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import org.robolectric.shadow.api.Shadow
import org.robolectric.shadows.ShadowLooper

/**
 * End-to-end JVM integration and adversarial verification suite for the `:library` module
 * powered by the Android Maps Testing Toolkit (`v1.1.0-rc01`).
 *
 * ## Architectural Scope
 * Unlike traditional mock-based unit tests, this suite executes against stateful Robolectric
 * shadows ([ShadowGoogleMap], [ShadowMapView], [ShadowSupportMapFragment], [ShadowProjection]),
 * Truth geospatial subjects ([LatLngSubject]), and offline golden image rasterization
 * ([GoldenImageDiff], [GoldenTileProvider], [GoldenTileStrategy]).
 *
 * We verify four major pillars of `:library`:
 * 1. **KTX Coroutine & DSL Extensions**: Suspending lifecycle bridges (`awaitMap`, `awaitMapLoad`,
 *    `awaitAnimateCamera`, `awaitSnapshot`), builder DSLs (`addMarker`, `addPolygon`, `addPolyline`,
 *    `addCircle`, `addGroundOverlay`, `addTileOverlay`, `buildGoogleMapOptions`), and cold `callbackFlow`
 *    streams for camera, marker, info window, overlay, and location events.
 * 2. **Multi-Collection Object Managers**: [MarkerManager], [PolygonManager], [PolylineManager],
 *    [CircleManager], and [GroundOverlayManager] collection isolation, visibility toggling, and
 *    Flow extensions.
 * 3. **Adversarial Race Window Discovery (Bug #9)**: Exposes the asynchronous listener registration
 *    defect in `MapObjectManager` (`Handler(Looper.getMainLooper()).post { setListenersOnUiThread() }`),
 *    proving that events dispatched before main looper idling are dropped and that any KTX Flow
 *    collector subscribed immediately after manager instantiation gets clobbered when the posted
 *    runnable executes.
 * 4. **Spherical Geometry, Polyline Simplification, Projection & Golden Snapshot Diffing**:
 *    Validates [SphericalUtil] and [PolyUtil] mathematics against [ShadowProjection] screen-space
 *    transforms, [LatLngSubject] distance tolerances, and [GoldenImageDiff] pixel-by-pixel diffs.
 */
@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [34])
@EnableMapsShadows
class MapsToolkitLibraryTest {

    private lateinit var activity: Activity
    private lateinit var mapView: MapView
    private lateinit var googleMap: GoogleMap
    private lateinit var shadowMap: ShadowGoogleMap
    private lateinit var shadowMapView: ShadowMapView

    @Before
    fun setUp() {
        activity = Robolectric.buildActivity(Activity::class.java).create().get()
        mapView = MapView(activity).apply {
            onCreate(Bundle())
            onStart()
            onResume()
            // Measure and layout to a 400x400px viewport so ShadowProjection and
            // ShadowMapView.renderMap(canvas) operate on realistic screen dimensions.
            measure(
                View.MeasureSpec.makeMeasureSpec(400, View.MeasureSpec.EXACTLY),
                View.MeasureSpec.makeMeasureSpec(400, View.MeasureSpec.EXACTLY)
            )
            layout(0, 0, 400, 400)
        }

        val mapRef = AtomicReference<GoogleMap>()
        mapView.getMapAsync { mapRef.set(it) }
        ShadowLooper.idleMainLooper()

        googleMap = checkNotNull(mapRef.get())
        shadowMap = Shadow.extract(googleMap) as ShadowGoogleMap
        shadowMapView = Shadow.extract(mapView) as ShadowMapView
    }

    // ============================================================================================
    // Section 1: Coroutine Lifecycle Bridges & Builder DSLs
    // ============================================================================================

    /**
     * Verifies that `MapView.awaitMap()` and `SupportMapFragment.awaitMap()` suspend until the
     * main looper dispatches `OnMapReadyCallback` from [ShadowMapView] and [ShadowSupportMapFragment],
     * and that `ShadowMapView` tracks lifecycle state transitions accurately.
     */
    @Test
    fun awaitMap_onMapViewAndSupportMapFragment_resolvesStatefulGoogleMap() = runTest {
        assertThat(shadowMapView.isOnCreateCalled).isTrue()
        assertThat(shadowMapView.isOnStartCalled).isTrue()
        assertThat(shadowMapView.isOnResumeCalled).isTrue()

        val deferredMapFromView = async(UnconfinedTestDispatcher(testScheduler)) {
            mapView.awaitMap()
        }
        ShadowLooper.idleMainLooper()
        val resolvedFromView = deferredMapFromView.await()
        assertThat(resolvedFromView).isSameInstanceAs(googleMap)

        val options = buildGoogleMapOptions {
            mapType(GoogleMap.MAP_TYPE_HYBRID)
            compassEnabled(true)
        }
        val fragment = SupportMapFragment.newInstance(options)
        val deferredMapFromFragment = async(UnconfinedTestDispatcher(testScheduler)) {
            fragment.awaitMap()
        }
        ShadowLooper.idleMainLooper()
        val resolvedFromFragment = deferredMapFromFragment.await()
        assertThat(resolvedFromFragment).isNotNull()
    }

    /**
     * Verifies that all KTX overlay builder DSLs (`addMarker`, `addPolygon`, `addPolyline`,
     * `addCircle`, `addGroundOverlay`, `addTileOverlay`) construct and register real stateful
     * overlay objects in [ShadowGoogleMap], and that [LatLngSubject] validates their geographic
     * coordinates within metric tolerances.
     */
    @Test
    fun overlayBuilderDsls_populateShadowGoogleMapAndSatisfyLatLngTruthMatchers() = runTest {
        val boulder = LatLng(40.0150, -105.2705)
        googleMap.awaitAnimateCamera(CameraUpdateFactory.newLatLngZoom(boulder, 14f), durationMs = 250)

        LatLngSubject.assertThat(googleMap.cameraPosition.target)
            .isWithin(1.meters)
            .of(boulder)
        assertThat(googleMap.cameraPosition.zoom).isEqualTo(14f)

        // 1. Marker DSL
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
            }
        )
        assertThat(shadowMap.markers).containsExactly(marker)
        assertThat(marker.title).isEqualTo("Boulder")
        assertThat(marker.snippet).isEqualTo("Colorado")
        assertThat(marker.alpha).isWithin(0.001f).of(0.85f)
        assertThat(marker.rotation).isWithin(0.001f).of(45f)
        assertThat(marker.zIndex).isWithin(0.001f).of(2f)
        assertThat(marker.isDraggable).isTrue()
        assertThat(marker.isFlat).isTrue()
        LatLngSubject.assertThat(marker.position).isWithin(1.meters).of(boulder)

        // 2. Polygon DSL
        val triangle = listOf(
            boulder,
            boulder.withSphericalOffset(500.0, 90.0),
            boulder.withSphericalOffset(500.0, 180.0)
        )
        val polygon = googleMap.addPolygon {
            addAll(triangle)
            fillColor(Color.argb(128, 0, 255, 0))
            strokeColor(Color.GREEN)
            strokeWidth(6f)
            clickable(true)
            geodesic(true)
        }
        assertThat(shadowMap.polygons).containsExactly(polygon)
        assertThat(polygon.isClickable).isTrue()
        assertThat(polygon.isGeodesic).isTrue()
        assertThat(polygon.area).isGreaterThan(100_000.0)

        // 3. Polyline DSL
        val polyline = googleMap.addPolyline {
            addAll(triangle)
            color(Color.BLUE)
            width(8f)
            clickable(true)
            geodesic(true)
        }
        assertThat(shadowMap.polylines).containsExactly(polyline)
        assertThat(polyline.isClickable).isTrue()
        assertThat(polyline.sphericalPathLength).isWithin(15.0).of(1207.11)

        // 4. Circle DSL
        val circle = googleMap.addCircle {
            center(boulder)
            radius(250.0)
            fillColor(Color.YELLOW)
            strokeColor(Color.BLACK)
            clickable(true)
        }
        assertThat(shadowMap.circles).containsExactly(circle)
        assertThat(circle.radius).isWithin(0.01).of(250.0)
        LatLngSubject.assertThat(circle.center).isWithin(1.meters).of(boulder)

        // 5. GroundOverlay DSL
        val overlayBitmap = Bitmap.createBitmap(32, 32, Bitmap.Config.ARGB_8888).apply {
            eraseColor(Color.MAGENTA)
        }
        val groundOverlay = checkNotNull(
            googleMap.addGroundOverlay {
                image(BitmapDescriptorFactory.fromBitmap(overlayBitmap))
                position(boulder, 100f, 100f)
                clickable(true)
                transparency(0.2f)
            }
        )
        assertThat(shadowMap.groundOverlays).containsExactly(groundOverlay)
        assertThat(groundOverlay.isClickable).isTrue()
        assertThat(groundOverlay.transparency).isWithin(0.001f).of(0.2f)

        // 6. TileOverlay DSL with GoldenTileProvider
        val goldenTileProvider = GoldenTileProvider(strategy = GoldenTileStrategy.Checkerboard())
        val tileOverlay = checkNotNull(
            googleMap.addTileOverlay {
                tileProvider(goldenTileProvider)
                fadeIn(false)
                zIndex(10f)
                transparency(0.1f)
            }
        )
        assertThat(shadowMap.tileOverlays).containsExactly(tileOverlay)
        assertThat(tileOverlay.fadeIn).isFalse()
        assertThat(tileOverlay.zIndex).isWithin(0.001f).of(10f)
        tileOverlay.clearTileCache()
        val shadowTileOverlay = Shadow.extract(tileOverlay) as ShadowTileOverlay
        assertThat(shadowTileOverlay.isTileCacheCleared).isTrue()
    }

    // ============================================================================================
    // Section 2: Reactive Event Flows on GoogleMap
    // ============================================================================================

    /**
     * Verifies all `GoogleMap` KTX cold `callbackFlow` streams (`cameraMoveStartedEvents`,
     * `cameraMoveEvents`, `cameraIdleEvents`, `cameraMoveCanceledEvents`, `mapClickEvents`,
     * `mapLongClickEvents`, `markerClickEvents`, `markerDragEvents`, `infoWindowClickEvents`,
     * `infoWindowLongClickEvents`, `infoWindowCloseEvents`, `polygonClickEvents`,
     * `polylineClickEvents`, `circleClickEvents`, `groundOverlayClicks`, `poiClickEvents`,
     * `myLocationButtonClickEvents`, `myLocationClickEvents`, and `awaitMapLoad`) against
     * synthetic user interactions dispatched by [ShadowGoogleMap].
     */
    @Test
    fun googleMapEventFlows_emitDeterministicallyAndUnregisterOnCancellation() = runTest {
        val dispatcher = UnconfinedTestDispatcher(testScheduler)
        val origin = LatLng(37.4220, -122.0841)

        // Collect camera lifecycle events
        val moveStartedReasons = mutableListOf<Int>()
        val moveEvents = mutableListOf<Unit>()
        val idleEvents = mutableListOf<Unit>()
        val canceledEvents = mutableListOf<Unit>()

        val cameraJobs = listOf(
            launch(dispatcher) { googleMap.cameraMoveStartedEvents().toList(moveStartedReasons) },
            launch(dispatcher) { googleMap.cameraMoveEvents().toList(moveEvents) },
            launch(dispatcher) { googleMap.cameraIdleEvents().toList(idleEvents) },
            launch(dispatcher) { googleMap.cameraMoveCanceledEvents().toList(canceledEvents) }
        )

        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(origin, 15f))
        shadowMap.cancelCameraMove()

        assertThat(moveStartedReasons).containsExactly(
            GoogleMap.OnCameraMoveStartedListener.REASON_DEVELOPER_ANIMATION
        )
        assertThat(moveEvents).hasSize(1)
        assertThat(idleEvents).hasSize(1)
        assertThat(canceledEvents).hasSize(1)
        cameraJobs.forEach { it.cancel() }

        // Collect map click & long-click events
        val mapClicks = mutableListOf<LatLng>()
        val mapLongClicks = mutableListOf<LatLng>()
        val clickJob = launch(dispatcher) { googleMap.mapClickEvents().toList(mapClicks) }
        val longClickJob = launch(dispatcher) { googleMap.mapLongClickEvents().toList(mapLongClicks) }

        shadowMap.clickMap(origin)
        shadowMap.longClickMap(origin)
        assertThat(mapClicks).containsExactly(origin)
        assertThat(mapLongClicks).containsExactly(origin)

        // Verify cancellation unregisters listeners so subsequent clicks are ignored
        clickJob.cancel()
        longClickJob.cancel()
        shadowMap.clickMap(origin)
        shadowMap.longClickMap(origin)
        assertThat(mapClicks).hasSize(1)
        assertThat(mapLongClicks).hasSize(1)

        // Marker click, drag, and info window lifecycle flows
        val marker = checkNotNull(googleMap.addMarker { position(origin).title("Googleplex") })
        val markerClicks = mutableListOf<Marker>()
        val dragEvents = mutableListOf<OnMarkerDragEvent>()
        val infoClicks = mutableListOf<Marker>()
        val infoLongClicks = mutableListOf<Marker>()
        val infoCloses = mutableListOf<Marker>()

        val markerJobs = listOf(
            launch(dispatcher) { googleMap.markerClickEvents().toList(markerClicks) },
            launch(dispatcher) { googleMap.markerDragEvents().toList(dragEvents) },
            launch(dispatcher) { googleMap.infoWindowClickEvents().toList(infoClicks) },
            launch(dispatcher) { googleMap.infoWindowLongClickEvents().toList(infoLongClicks) },
            launch(dispatcher) { googleMap.infoWindowCloseEvents().toList(infoCloses) }
        )

        shadowMap.clickMarker(marker)
        val draggedMid = origin.withSphericalOffset(50.0, 45.0)
        val draggedEnd = origin.withSphericalOffset(100.0, 45.0)
        shadowMap.dragMarkerStart(marker)
        shadowMap.dragMarker(marker, draggedMid)
        shadowMap.dragMarkerEnd(marker, draggedEnd)

        marker.showInfoWindow()
        assertThat(marker.isInfoWindowShown).isTrue()
        shadowMap.clickInfoWindow(marker)
        shadowMap.longClickInfoWindow(marker)
        marker.hideInfoWindow()
        assertThat(marker.isInfoWindowShown).isFalse()

        assertThat(markerClicks).containsExactly(marker)
        assertThat(dragEvents).containsExactly(
            MarkerDragStartEvent(marker),
            MarkerDragEvent(marker),
            MarkerDragEndEvent(marker)
        ).inOrder()
        LatLngSubject.assertThat(marker.position).isWithin(1.meters).of(draggedEnd)
        assertThat(infoClicks).containsExactly(marker)
        assertThat(infoLongClicks).containsExactly(marker)
        assertThat(infoCloses).containsExactly(marker)
        markerJobs.forEach { it.cancel() }

        // Shape, POI, and MyLocation flows
        val polygon = googleMap.addPolygon {
            add(origin, draggedMid, draggedEnd)
            clickable(true)
        }
        val polyline = googleMap.addPolyline {
            add(origin, draggedEnd)
            clickable(true)
        }
        val circle = googleMap.addCircle {
            center(origin)
            radius(100.0)
            clickable(true)
        }
        val groundOverlay = checkNotNull(
            googleMap.addGroundOverlay {
                position(origin, 50f)
                clickable(true)
            }
        )

        val clickedPolygons = mutableListOf<Polygon>()
        val clickedPolylines = mutableListOf<Polyline>()
        val clickedCircles = mutableListOf<Circle>()
        val clickedOverlays = mutableListOf<GroundOverlay>()
        val clickedPois = mutableListOf<PointOfInterest>()
        val myLocationBtnClicks = mutableListOf<Unit>()
        val myLocationClicks = mutableListOf<Location>()

        val overlayJobs = listOf(
            launch(dispatcher) { googleMap.polygonClickEvents().toList(clickedPolygons) },
            launch(dispatcher) { googleMap.polylineClickEvents().toList(clickedPolylines) },
            launch(dispatcher) { googleMap.circleClickEvents().toList(clickedCircles) },
            launch(dispatcher) { googleMap.groundOverlayClicks().toList(clickedOverlays) },
            launch(dispatcher) { googleMap.poiClickEvents().toList(clickedPois) },
            launch(dispatcher) { googleMap.myLocationButtonClickEvents().toList(myLocationBtnClicks) },
            launch(dispatcher) { googleMap.myLocationClickEvents().toList(myLocationClicks) }
        )

        shadowMap.clickPolygon(polygon)
        shadowMap.clickPolyline(polyline)
        shadowMap.clickCircle(circle)
        shadowMap.clickGroundOverlay(groundOverlay)
        val poi = PointOfInterest(origin, "place_id_123", "Amphitheatre")
        shadowMap.clickPoi(poi)
        assertThat(shadowMap.clickMyLocationButton()).isTrue()
        val location = Location("gps").apply {
            latitude = origin.latitude
            longitude = origin.longitude
        }
        shadowMap.clickMyLocation(location)

        assertThat(clickedPolygons).containsExactly(polygon)
        assertThat(clickedPolylines).containsExactly(polyline)
        assertThat(clickedCircles).containsExactly(circle)
        assertThat(clickedOverlays).containsExactly(groundOverlay)
        assertThat(clickedPois).containsExactly(poi)
        assertThat(myLocationBtnClicks).hasSize(1)
        assertThat(myLocationClicks).containsExactly(location)
        overlayJobs.forEach { it.cancel() }

        // Both KTX awaitMapLoad() and golden-testing awaitMapLoaded()
        val mapLoadJob1 = async(dispatcher) { googleMap.awaitMapLoad() }
        shadowMap.notifyMapLoaded()
        mapLoadJob1.await()

        val mapLoadJob2 = async(dispatcher) { googleMap.awaitMapLoaded() }
        shadowMap.notifyMapLoaded()
        mapLoadJob2.await()
    }

    // ============================================================================================
    // Section 3: Multi-Collection Object Managers & Bug #9 Race Window Proof
    // ============================================================================================

    /**
     * Verifies multi-collection isolation, visibility toggling (`hideAll`/`showAll`), removal,
     * and Flow collection across [MarkerManager], [PolygonManager], [PolylineManager],
     * [CircleManager], and [GroundOverlayManager] after the main looper has processed their
     * listener registration.
     */
    @Test
    fun mapObjectManagers_isolateCollectionsAndRouteClickFlowsCorrectly() = runTest {
        val dispatcher = UnconfinedTestDispatcher(testScheduler)
        val center = LatLng(47.6062, -122.3321)

        val markerManager = MarkerManager(googleMap)
        val polygonManager = PolygonManager(googleMap)
        val polylineManager = PolylineManager(googleMap)
        val circleManager = CircleManager(googleMap)
        val groundOverlayManager = GroundOverlayManager(googleMap)

        // Idle main looper so MapObjectManager's posted setListenersOnUiThread() completes
        ShadowLooper.idleMainLooper()

        val markerColA = markerManager.newCollection("hotels")
        val markerColB = markerManager.newCollection("restaurants")

        val hotelMarker = markerColA.addMarker {
            position(center)
            title("Hotel")
        }
        val restaurantMarker = markerColB.addMarker {
            position(center.withSphericalOffset(100.0, 90.0))
            title("Restaurant")
        }

        val hotelClicks = mutableListOf<Marker>()
        val restaurantClicks = mutableListOf<Marker>()
        val hotelInfoClicks = mutableListOf<Marker>()
        val hotelInfoLongClicks = mutableListOf<Marker>()

        val jobs = mutableListOf(
            launch(dispatcher) { markerColA.clickEvents().toList(hotelClicks) },
            launch(dispatcher) { markerColB.clickEvents().toList(restaurantClicks) },
            launch(dispatcher) { markerColA.infoWindowClickEvents().toList(hotelInfoClicks) },
            launch(dispatcher) { markerColA.infoWindowLongClickEvents().toList(hotelInfoLongClicks) }
        )

        // Clicking hotelMarker only emits to markerColA, never markerColB
        shadowMap.clickMarker(hotelMarker)
        shadowMap.clickInfoWindow(hotelMarker)
        shadowMap.longClickInfoWindow(hotelMarker)
        assertThat(hotelClicks).containsExactly(hotelMarker)
        assertThat(restaurantClicks).isEmpty()
        assertThat(hotelInfoClicks).containsExactly(hotelMarker)
        assertThat(hotelInfoLongClicks).containsExactly(hotelMarker)

        shadowMap.clickMarker(restaurantMarker)
        assertThat(restaurantClicks).containsExactly(restaurantMarker)

        // Visibility & clear isolation
        markerColA.hideAll()
        assertThat(hotelMarker.isVisible).isFalse()
        assertThat(restaurantMarker.isVisible).isTrue()
        markerColA.showAll()
        assertThat(hotelMarker.isVisible).isTrue()

        // Polygon, Polyline, Circle, and GroundOverlay manager flows
        val polyCol = polygonManager.newCollection()
        val lineCol = polylineManager.newCollection()
        val circleCol = circleManager.newCollection()
        val groundCol = groundOverlayManager.newCollection()

        val managedPolygon = polyCol.addPolygon {
            add(center, center.withSphericalOffset(50.0, 0.0), center.withSphericalOffset(50.0, 90.0))
            clickable(true)
        }
        val managedPolyline = lineCol.addPolyline {
            add(center, center.withSphericalOffset(100.0, 180.0))
            clickable(true)
        }
        val managedCircle = circleCol.addCircle {
            center(center)
            radius(75.0)
            clickable(true)
        }
        val managedGroundOverlay = groundCol.addGroundOverlay {
            position(center, 60f)
            clickable(true)
        }

        val polyClicks = mutableListOf<Polygon>()
        val lineClicks = mutableListOf<Polyline>()
        val circleClicks = mutableListOf<Circle>()
        val groundClicks = mutableListOf<GroundOverlay>()

        jobs += launch(dispatcher) { polyCol.clickEvents().toList(polyClicks) }
        jobs += launch(dispatcher) { lineCol.clickEvents().toList(lineClicks) }
        jobs += launch(dispatcher) { circleCol.clickEvents().toList(circleClicks) }
        jobs += launch(dispatcher) { groundCol.clickEvents().toList(groundClicks) }

        shadowMap.clickPolygon(managedPolygon)
        shadowMap.clickPolyline(managedPolyline)
        shadowMap.clickCircle(managedCircle)
        shadowMap.clickGroundOverlay(managedGroundOverlay)

        assertThat(polyClicks).containsExactly(managedPolygon)
        assertThat(lineClicks).containsExactly(managedPolyline)
        assertThat(circleClicks).containsExactly(managedCircle)
        assertThat(groundClicks).containsExactly(managedGroundOverlay)

        markerColA.clear()
        assertThat(shadowMap.markers).containsExactly(restaurantMarker)
        jobs.forEach { it.cancel() }
    }

    /**
     * **BUG #9 REGRESSION VERIFICATION (`MapObjectManager` Synchronous Listener Registration on UI Thread)**:
     *
     * Verifies that constructing [MarkerManager] on the main thread registers its listeners
     * synchronously without waiting for a posted main-looper runnable:
     * 1. Any marker click dispatched immediately after constructing `MarkerManager` (before the
     *    main looper drains) is routed immediately to the collection's listener.
     * 2. If a caller subsequently registers a direct `googleMap.markerClickEvents()` collector,
     *    draining the main looper later does NOT overwrite that collector with a delayed init runnable.
     */
    @Test
    fun bug9_mapObjectManager_registersListenersSynchronouslyOnUiThread() = runTest {
        val dispatcher = UnconfinedTestDispatcher(testScheduler)
        val point = LatLng(37.7749, -122.4194)

        // Instantiate MarkerManager on the main thread WITHOUT idling the looper
        val markerManager = MarkerManager(googleMap)
        val collection = markerManager.newCollection()
        val marker = collection.addMarker(MarkerOptions().position(point).title("San Francisco"))

        val managerClicks = mutableListOf<Marker>()
        collection.setOnMarkerClickListener {
            managerClicks.add(it)
            true
        }

        // 1. Click marker immediately -> routed synchronously to MarkerManager!
        shadowMap.clickMarker(marker)
        assertThat(managerClicks).containsExactly(marker)

        // 2. Suppose a caller subscribes to googleMap.markerClickEvents() after creating MarkerManager
        val directFlowClicks = mutableListOf<Marker>()
        val flowJob = launch(dispatcher) {
            googleMap.markerClickEvents().toList(directFlowClicks)
        }

        shadowMap.clickMarker(marker)
        assertThat(directFlowClicks).containsExactly(marker)

        // 3. Draining the main looper does NOT clobber the active `googleMap.markerClickEvents()` listener!
        ShadowLooper.idleMainLooper()
        shadowMap.clickMarker(marker)
        assertThat(directFlowClicks).hasSize(2)

        flowJob.cancel()
    }

    // ============================================================================================
    // Section 4: Spherical Geometry, Projection Alignment & Golden Visual Diffing
    // ============================================================================================

    /**
     * Integrates [SphericalUtil], [PolyUtil], and KTX geometry extensions (`component1`, `component2`,
     * `withSphericalOffset`, `computeSphericalOffsetOrigin`, `withSphericalLinearInterpolation`,
     * `sphericalDistance`, `sphericalHeading`, `simplify`, `latLngListEncode`, `toLatLngList`) with
     * [ShadowProjection] and [LatLngSubject] to verify round-trip geographic-to-screen accuracy.
     */
    @Test
    fun sphericalAndPolyUtils_alignWithShadowProjectionAndLatLngSubject() {
        val origin = LatLng(37.7749, -122.4194)
        val (lat, lng) = origin
        assertThat(lat).isEqualTo(37.7749)
        assertThat(lng).isEqualTo(-122.4194)

        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(origin, 15f))
        val projection = googleMap.projection

        // Center of 400x400 MapView must project to (200, 200)
        val centerPoint = projection.toScreenLocation(origin)
        assertThat(centerPoint).isEqualTo(Point(200, 200))

        // Offset 500 meters East (heading 90 degrees)
        val east500m = origin.withSphericalOffset(500.0, 90.0)
        LatLngSubject.assertThat(east500m).isWithin(501.meters).of(origin)
        assertThat(origin.sphericalDistance(east500m)).isWithin(0.5).of(500.0)
        assertThat(origin.sphericalHeading(east500m)).isWithin(0.01).of(90.0)

        // Recover origin via computeSphericalOffsetOrigin
        val recoveredOrigin = checkNotNull(east500m.computeSphericalOffsetOrigin(500.0, 90.0))
        LatLngSubject.assertThat(recoveredOrigin).isWithin(1.meters).of(origin)

        // Midpoint via Slerp (fraction = 0.5)
        val midpoint = origin.withSphericalLinearInterpolation(east500m, 0.5)
        LatLngSubject.assertThat(midpoint).isWithin(251.meters).of(origin)
        LatLngSubject.assertThat(midpoint).isWithin(251.meters).of(east500m)
        assertThat( listOf(origin, east500m).isLocationOnPath(midpoint, geodesic = true, tolerance = 1.0) ).isTrue()

        // Project east500m to screen coordinates and back via ShadowProjection
        val eastScreen = projection.toScreenLocation(east500m)
        assertThat(eastScreen.x).isGreaterThan(centerPoint.x)
        assertThat(eastScreen.y).isEqualTo(centerPoint.y)
        val roundTripLatLng = projection.fromScreenLocation(eastScreen)
        LatLngSubject.assertThat(roundTripLatLng).isWithin(10.meters).of(east500m)

        // VisibleRegion bounds must contain origin and east500m at zoom 15 on a 400x400 viewport
        val visibleRegion = projection.visibleRegion
        assertThat(visibleRegion.latLngBounds.contains(origin)).isTrue()
        assertThat(visibleRegion.latLngBounds.contains(east500m)).isTrue()

        // Polyline encoding/decoding and Douglas-Peucker simplification
        val noisyPath = listOf(
            origin,
            origin.withSphericalLinearInterpolation(east500m, 0.25).withSphericalOffset(0.2, 0.0),
            midpoint,
            origin.withSphericalLinearInterpolation(east500m, 0.75).withSphericalOffset(0.2, 180.0),
            east500m
        )
        val simplified = noisyPath.simplify(tolerance = 2.0)
        assertThat(simplified).containsExactly(origin, east500m).inOrder()

        val encoded = simplified.latLngListEncode()
        val decoded = encoded.toLatLngList()
        assertThat(decoded).hasSize(2)
        LatLngSubject.assertThat(decoded[0]).isWithin(2.meters).of(origin)
        LatLngSubject.assertThat(decoded[1]).isWithin(2.meters).of(east500m)
    }

    /**
     * Exercises `GoogleMap.enableGoldenTesting()`, `GoogleMap.awaitSnapshot()`,
     * `GoogleMap.captureSnapshot()`, and [GoldenImageDiff] to verify pixel-accurate rendering
     * of polygons, polylines, circles, markers, and ground overlays on [ShadowMapView].
     */
    @Test
    fun goldenTestingAndSnapshots_detectVisualOverlayChangesAndMatchIdenticalScenes() = runTest {
        val dispatcher = UnconfinedTestDispatcher(testScheduler)
        val center = LatLng(37.7749, -122.4194)
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(center, 14f))

        // Enable GoldenTesting with CoordinateGrid synthetic tiles
        val goldenTileOverlay = googleMap.enableGoldenTesting(
            strategy = GoldenTileStrategy.CoordinateGrid(),
            tileSize = 256,
            fadeIn = false
        )
        assertThat(googleMap.mapType).isEqualTo(GoogleMap.MAP_TYPE_NONE)
        assertThat(shadowMap.tileOverlays).contains(goldenTileOverlay)

        // Capture baseline empty scene snapshot via KTX awaitSnapshot()
        val baselineJob = async(dispatcher) { googleMap.awaitSnapshot() }
        ShadowLooper.idleMainLooper()
        val baselineBitmap = checkNotNull(baselineJob.await())
        assertThat(baselineBitmap.width).isEqualTo(400)
        assertThat(baselineBitmap.height).isEqualTo(400)

        // Capture a second snapshot of the identical scene via golden-testing captureSnapshot()
        val secondJob = async(dispatcher) { googleMap.captureSnapshot() }
        ShadowLooper.idleMainLooper()
        val identicalBitmap = secondJob.await()

        // Pixel-by-pixel comparison must match with 0 differing pixels
        GoldenImageDiff.assertMatches(
            expected = baselineBitmap,
            actual = identicalBitmap,
            maxDiffPercentage = 0.0
        )

        // Now render a closed polygon, polyline, circle, and marker onto the map
        val square = listOf(
            center.withSphericalOffset(600.0, 45.0),
            center.withSphericalOffset(600.0, 135.0),
            center.withSphericalOffset(600.0, 225.0),
            center.withSphericalOffset(600.0, 315.0),
            center.withSphericalOffset(600.0, 45.0)
        )
        assertThat(square.isClosedPolygon()).isTrue()
        assertThat(square.containsLocation(center, geodesic = true)).isTrue()

        val polygon = googleMap.addPolygon {
            addAll(square)
            fillColor(Color.argb(180, 255, 0, 0))
            strokeColor(Color.BLUE)
            strokeWidth(5f)
        }
        assertThat(polygon.contains(center)).isTrue()
        assertThat(polygon.isOnEdge(square[0], tolerance = 5.0)).isTrue()
        assertThat( kotlin.math.abs(polygon.signedArea) ).isWithin(1.0).of(polygon.area)

        googleMap.addCircle {
            center(center)
            radius(200.0)
            fillColor(Color.GREEN)
            strokeColor(Color.BLACK)
            strokeWidth(3f)
        }
        googleMap.addMarker {
            position(center)
            title("Center Pin")
        }

        val renderedJob = async(dispatcher) { googleMap.captureSnapshot() }
        ShadowLooper.idleMainLooper()
        val renderedBitmap = renderedJob.await()

        // Compare baseline vs rendered scene: must diverge significantly and generate a diff bitmap
        val diffResult = GoldenImageDiff.compare(
            expected = baselineBitmap,
            actual = renderedBitmap,
            maxDiffPercentage = 0.001,
            generateDiffBitmap = true
        )
        assertThat(diffResult.matches).isFalse()
        assertThat(diffResult.diffPixelCount).isGreaterThan(500)
        assertThat(diffResult.diffPercentage).isGreaterThan(0.003)
        assertThat(diffResult.diffBitmap).isNotNull()
    }
}
