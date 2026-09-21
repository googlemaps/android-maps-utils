/*
 * Copyright 2026 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.maps.android.utils.demo

import android.app.Activity
import android.graphics.Bitmap
import android.graphics.Color
import android.os.Bundle
import android.view.View
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolygonOptions
import com.google.android.maps.robolectric.annotation.EnableMapsShadows
import com.google.android.maps.testing.golden.GoldenImageDiff
import com.google.android.maps.testing.golden.GoldenTileStrategy
import com.google.android.maps.testing.golden.captureSnapshot
import com.google.android.maps.testing.golden.enableGoldenTesting
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.async
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import org.robolectric.shadows.ShadowLooper
import java.util.concurrent.atomic.AtomicReference

/**
 * Deterministic unit test suite utilizing the unreleased `golden-testing` and `robolectric-testing`
 * libraries from `android-maps-robolectric` to verify z-index layering behavior in headless JVM tests.
 *
 * Tests:
 * 1. Initial z-index dictates the visual stacking order of overlapping shapes.
 * 2. Directly setting `zIndex` on the returned native [Polygon] object causes it to dynamically emerge on top.
 * 3. Directly lowering `zIndex` on the returned native [Polygon] object causes it to dynamically sink below.
 * 4. Markers always render on top of polygons regardless of the polygon's explicit zIndex.
 */
// [START maps_android_utils_zindex_robolectric_golden_test]
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [34])
@EnableMapsShadows
class ZIndexGoldenRobolectricTest {

    private lateinit var activity: Activity
    private lateinit var mapView: MapView
    private lateinit var googleMap: GoogleMap

    companion object {
        private const val COLOR_CORAL = -0xaa0000 // Red / Coral (Opaque)
        private const val COLOR_TEAL = -0xff0001 // Cyan / Teal (Opaque)
    }

    @Before
    fun setUp() {
        activity = Robolectric.buildActivity(Activity::class.java).create().get()
        mapView = MapView(activity)
        mapView.measure(
            View.MeasureSpec.makeMeasureSpec(200, View.MeasureSpec.EXACTLY),
            View.MeasureSpec.makeMeasureSpec(200, View.MeasureSpec.EXACTLY)
        )
        mapView.layout(0, 0, 200, 200)
        mapView.onCreate(Bundle())

        val mapRef = AtomicReference<GoogleMap>()
        mapView.getMapAsync { mapRef.set(it) }
        ShadowLooper.idleMainLooper()
        googleMap = checkNotNull(mapRef.get())

        // Enable deterministic synthetic tiles so screenshots are fully predictable
        googleMap.enableGoldenTesting(strategy = GoldenTileStrategy.CoordinateGrid())
    }

    @Test
    fun testInitialZIndexDictatesStackingOrder() = runTest {
        val center = LatLng(0.0, 0.0)
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(center, 3f))

        // Polygon 1 (Coral) at zIndex = 1.0f
        googleMap.addPolygon(
            PolygonOptions()
                .add(LatLng(-10.0, -10.0), LatLng(-10.0, 5.0), LatLng(5.0, 5.0), LatLng(5.0, -10.0))
                .fillColor(COLOR_CORAL)
                .strokeWidth(0f)
                .zIndex(1.0f)
        )

        // Polygon 2 (Teal) at zIndex = 2.0f, overlapping Polygon 1 at (0, 0)
        googleMap.addPolygon(
            PolygonOptions()
                .add(LatLng(-5.0, -5.0), LatLng(-5.0, 10.0), LatLng(10.0, 10.0), LatLng(10.0, -5.0))
                .fillColor(COLOR_TEAL)
                .strokeWidth(0f)
                .zIndex(2.0f)
        )

        val snapshot = captureSnapshotWithShadowLooper()
        val overlapScreenPoint = googleMap.projection.toScreenLocation(center)

        // Since Teal has higher zIndex (2.0f > 1.0f), the pixel at the overlap must be Teal
        assertThat(snapshot.getPixel(overlapScreenPoint.x, overlapScreenPoint.y)).isEqualTo(COLOR_TEAL)
    }

    @Test
    fun testDynamicZIndexIncreaseCausesObjectToEmergeOnTop() = runTest {
        val center = LatLng(0.0, 0.0)
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(center, 3f))

        // 1. Add Polygon 1 (Coral) with initial zIndex = 1.0f
        val coralPolygon = checkNotNull(
            googleMap.addPolygon(
                PolygonOptions()
                    .add(LatLng(-10.0, -10.0), LatLng(-10.0, 5.0), LatLng(5.0, 5.0), LatLng(5.0, -10.0))
                    .fillColor(COLOR_CORAL)
                    .strokeWidth(0f)
                    .zIndex(1.0f)
            )
        )

        // 2. Add Polygon 2 (Teal) with initial zIndex = 2.0f
        googleMap.addPolygon(
            PolygonOptions()
                .add(LatLng(-5.0, -5.0), LatLng(-5.0, 10.0), LatLng(10.0, 10.0), LatLng(10.0, -5.0))
                .fillColor(COLOR_TEAL)
                .strokeWidth(0f)
                .zIndex(2.0f)
        )

        val baselineSnapshot = captureSnapshotWithShadowLooper()
        val overlapPoint = googleMap.projection.toScreenLocation(center)
        assertThat(baselineSnapshot.getPixel(overlapPoint.x, overlapPoint.y)).isEqualTo(COLOR_TEAL)

        // 3. Directly update zIndex on the returned native map object to 3.0f (emerges on top!)
        coralPolygon.zIndex = 3.0f

        val updatedSnapshot = captureSnapshotWithShadowLooper()

        // 4. Overlap pixel must now be Coral
        assertThat(updatedSnapshot.getPixel(overlapPoint.x, overlapPoint.y)).isEqualTo(COLOR_CORAL)

        // 5. Verify visual difference using GoldenImageDiff
        val diff = GoldenImageDiff.compare(baselineSnapshot, updatedSnapshot)
        assertThat(diff.matches).isFalse()
        assertThat(diff.diffPixelCount).isGreaterThan(0)
    }

    @Test
    fun testDynamicZIndexDecreaseCausesObjectToSinkBelow() = runTest {
        val center = LatLng(0.0, 0.0)
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(center, 3f))

        // Add Polygon 1 (Coral) with zIndex = 3.0f (initially on top)
        val coralPolygon = checkNotNull(
            googleMap.addPolygon(
                PolygonOptions()
                    .add(LatLng(-10.0, -10.0), LatLng(-10.0, 5.0), LatLng(5.0, 5.0), LatLng(5.0, -10.0))
                    .fillColor(COLOR_CORAL)
                    .strokeWidth(0f)
                    .zIndex(3.0f)
            )
        )

        // Add Polygon 2 (Teal) with zIndex = 2.0f
        googleMap.addPolygon(
            PolygonOptions()
                .add(LatLng(-5.0, -5.0), LatLng(-5.0, 10.0), LatLng(10.0, 10.0), LatLng(10.0, -5.0))
                .fillColor(COLOR_TEAL)
                .strokeWidth(0f)
                .zIndex(2.0f)
        )

        val topSnapshot = captureSnapshotWithShadowLooper()
        val overlapPoint = googleMap.projection.toScreenLocation(center)
        assertThat(topSnapshot.getPixel(overlapPoint.x, overlapPoint.y)).isEqualTo(COLOR_CORAL)

        // Directly lower zIndex on the returned map object to 0.0f (sinks below Teal)
        coralPolygon.zIndex = 0.0f

        val loweredSnapshot = captureSnapshotWithShadowLooper()
        assertThat(loweredSnapshot.getPixel(overlapPoint.x, overlapPoint.y)).isEqualTo(COLOR_TEAL)
    }

    @Test
    fun testMarkerAlwaysRendersAbovePolygonsRegardlessOfZIndex() = runTest {
        val center = LatLng(0.0, 0.0)
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(center, 3f))

        // Polygon with an enormous zIndex
        googleMap.addPolygon(
            PolygonOptions()
                .add(LatLng(-10.0, -10.0), LatLng(-10.0, 10.0), LatLng(10.0, 10.0), LatLng(10.0, -10.0))
                .fillColor(COLOR_CORAL)
                .strokeWidth(0f)
                .zIndex(9999.0f)
        )

        // Marker centered at the origin with low zIndex = 0.0f
        googleMap.addMarker(
            MarkerOptions()
                .position(center)
                .icon(BitmapDescriptorFactory.defaultMarker())
                .zIndex(0.0f)
        )

        val snapshot = captureSnapshotWithShadowLooper()
        val markerCenterPoint = googleMap.projection.toScreenLocation(center).apply { y -= 12 }

        // The pixel at the marker pin head is NOT Coral (it's the marker pin)
        assertThat(snapshot.getPixel(markerCenterPoint.x, markerCenterPoint.y)).isNotEqualTo(COLOR_CORAL)
    }

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    private fun TestScope.captureSnapshotWithShadowLooper(): Bitmap {
        val deferred = async { googleMap.captureSnapshot() }
        testScheduler.advanceUntilIdle()
        ShadowLooper.idleMainLooper()
        testScheduler.advanceUntilIdle()
        return deferred.getCompleted()
    }
}
// [END maps_android_utils_zindex_robolectric_golden_test]
