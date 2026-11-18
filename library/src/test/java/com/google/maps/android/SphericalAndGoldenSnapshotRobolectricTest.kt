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

import android.graphics.Color
import android.graphics.Point
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.robolectric.ktx.createTestMap
import com.google.maps.android.robolectric.ktx.tileOverlays
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
import com.google.maps.android.robolectric.truth.isWithin
import com.google.maps.android.robolectric.truth.meters
import com.google.maps.android.testing.golden.GoldenImageDiff
import com.google.maps.android.testing.golden.GoldenTileStrategy
import com.google.maps.android.testing.golden.captureSnapshot
import com.google.maps.android.testing.golden.enableGoldenTesting
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
import kotlin.math.abs
import com.google.common.truth.Truth.assertThat as truthAssertThat

/**
 * Verifies [SphericalUtil] and [PolyUtil] KTX geometry alignment against [ShadowProjection]
 * Web Mercator screen-space transforms and pixel-accurate canvas rendering via [GoldenImageDiff].
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
class SphericalAndGoldenSnapshotRobolectricTest {

    private lateinit var googleMap: GoogleMap

    @Before
    fun setUp() {
        googleMap = createTestMap(width = 400, height = 400)
    }

    @Test
    fun sphericalAndPolyUtils_alignWithShadowProjectionAndVisibleBounds() {
        val origin = LatLng(37.7749, -122.4194)
        val (lat, lng) = origin
        truthAssertThat(lat).isEqualTo(37.7749)
        truthAssertThat(lng).isEqualTo(-122.4194)

        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(origin, 15f))
        val projection = googleMap.projection

        val centerPoint = projection.toScreenLocation(origin)
        truthAssertThat(centerPoint).isEqualTo(Point(200, 200))

        val east500m = origin.withSphericalOffset(500.0, 90.0)
        assertThat(east500m).isWithin(501.meters).of(origin)
        truthAssertThat(origin.sphericalDistance(east500m)).isWithin(0.5).of(500.0)
        truthAssertThat(origin.sphericalHeading(east500m)).isWithin(0.01).of(90.0)

        val recoveredOrigin = checkNotNull(east500m.computeSphericalOffsetOrigin(500.0, 90.0))
        assertThat(recoveredOrigin).isWithin(1.meters).of(origin)

        val midpoint = origin.withSphericalLinearInterpolation(east500m, 0.5)
        assertThat(midpoint).isWithin(251.meters).of(origin)
        truthAssertThat(listOf(origin, east500m).isLocationOnPath(midpoint, geodesic = true, tolerance = 1.0)).isTrue()

        val eastScreen = projection.toScreenLocation(east500m)
        truthAssertThat(eastScreen.x).isGreaterThan(centerPoint.x)
        truthAssertThat(eastScreen.y).isEqualTo(centerPoint.y)
        assertThat(projection.fromScreenLocation(eastScreen)).isWithin(10.meters).of(east500m)

        assertThat(googleMap)
            .showsOnScreen(origin)
            .showsOnScreen(east500m)

        val noisyPath = listOf(
            origin,
            origin.withSphericalLinearInterpolation(east500m, 0.25).withSphericalOffset(0.2, 0.0),
            midpoint,
            origin.withSphericalLinearInterpolation(east500m, 0.75).withSphericalOffset(0.2, 180.0),
            east500m,
        )
        val simplified = noisyPath.simplify(tolerance = 2.0)
        truthAssertThat(simplified).containsExactly(origin, east500m).inOrder()

        val decoded = simplified.latLngListEncode().toLatLngList()
        truthAssertThat(decoded).hasSize(2)
        assertThat(decoded[0]).isWithin(2.meters).of(origin)
        assertThat(decoded[1]).isWithin(2.meters).of(east500m)
    }

    @Test
    fun goldenSnapshots_renderExactOverlayColorsAtProjectedCoordinates() = runTest {
        val dispatcher = UnconfinedTestDispatcher(testScheduler)
        val center = LatLng(37.7749, -122.4194)
        val distantCorner = center.withSphericalOffset(1500.0, 315.0)
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(center, 14f))

        val goldenTileOverlay = googleMap.enableGoldenTesting(
            strategy = GoldenTileStrategy.CoordinateGrid(),
            tileSize = 256,
            fadeIn = false,
        )
        assertThat(googleMap).hasMapType(GoogleMap.MAP_TYPE_NONE)
        truthAssertThat(googleMap.tileOverlays).contains(goldenTileOverlay)

        val baselineJob = async(dispatcher) { googleMap.awaitSnapshot() }
        ShadowLooper.idleMainLooper()
        val baselineBitmap = checkNotNull(baselineJob.await())
        val identicalBitmap = googleMap.captureSnapshot()
        GoldenImageDiff.assertMatches(expected = baselineBitmap, actual = identicalBitmap, maxDiffPercentage = 0.0)

        val square = listOf(
            center.withSphericalOffset(600.0, 45.0),
            center.withSphericalOffset(600.0, 135.0),
            center.withSphericalOffset(600.0, 225.0),
            center.withSphericalOffset(600.0, 315.0),
            center.withSphericalOffset(600.0, 45.0),
        )
        truthAssertThat(square.isClosedPolygon()).isTrue()
        truthAssertThat(square.containsLocation(center, geodesic = true)).isTrue()

        val polygon = googleMap.addPolygon {
            addAll(square)
            fillColor(Color.RED)
            strokeColor(Color.BLUE)
            strokeWidth(4f)
        }
        truthAssertThat(polygon.contains(center)).isTrue()
        truthAssertThat(polygon.isOnEdge(square[0], tolerance = 5.0)).isTrue()
        truthAssertThat(abs(polygon.signedArea)).isWithin(1.0).of(polygon.area)

        // Verify semantic pixel color inside the opaque red polygon vs outside
        val exteriorColorBeforeCircle = googleMap.captureSnapshot().let {
            val pt = googleMap.projection.toScreenLocation(distantCorner)
            it.getPixel(pt.x, pt.y)
        }
        assertThat(googleMap).hasPixelColorAt(center, Color.RED)
        truthAssertThat(exteriorColorBeforeCircle).isNotEqualTo(Color.RED)

        // Adding a higher-layer green circle over center replaces the center pixel with Color.GREEN
        googleMap.addCircle {
            center(center)
            radius(150.0)
            fillColor(Color.GREEN)
            strokeWidth(0f)
        }
        assertThat(googleMap).hasPixelColorAt(center, Color.GREEN)

        val diffResult = GoldenImageDiff.compare(
            expected = baselineBitmap,
            actual = googleMap.captureSnapshot(),
            maxDiffPercentage = 0.001,
            generateDiffBitmap = true,
        )
        truthAssertThat(diffResult.matches).isFalse()
        truthAssertThat(diffResult.diffBitmap).isNotNull()
    }
}
