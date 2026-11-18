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
package com.google.maps.android.ui

import android.app.Activity
import android.view.View
import android.widget.TextView
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
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
import com.google.maps.android.robolectric.truth.kilometers
import com.google.maps.android.robolectric.truth.meters
import com.google.maps.android.testing.golden.GoldenImageDiff
import com.google.maps.android.testing.golden.GoldenTileStrategy
import com.google.maps.android.testing.golden.captureSnapshot
import com.google.maps.android.testing.golden.enableGoldenTesting
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import org.robolectric.shadows.ShadowLooper
import java.util.concurrent.TimeUnit
import com.google.common.truth.Truth.assertThat as truthAssertThat

/**
 * Stateful Robolectric verification suite for `:ui` components ([IconGenerator], [RotationLayout],
 * [SquareTextView], and [AnimationUtil]) integrated against a measured [GoogleMap] and [ShadowLooper].
 *
 * Note: Maps shadows are declared directly on `@Config(shadows = [...])` because Robolectric's
 * `ConfigMerger` does not traverse meta-annotations, avoiding global `robolectric.properties` pollution.
 */
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
class UiMapIntegrationRobolectricTest {

    private lateinit var activity: Activity
    private lateinit var googleMap: GoogleMap

    @Before
    fun setUp() {
        activity = Robolectric.buildActivity(Activity::class.java).create().get()
        val mapView = createTestMapView(width = 300, height = 300, activity = activity).apply {
            onStart()
            onResume()
        }
        googleMap = mapView.getMapBlocking()
    }

    @Test
    fun iconGenerator_rendersDeterministicBitmapsAcrossStylesRotationsAndMapSnapshots() = runTest {
        val center = LatLng(37.7749, -122.4194)
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(center, 15f))
        googleMap.enableGoldenTesting(strategy = GoldenTileStrategy.SolidColorHash())

        val iconGen = IconGenerator(activity)
        val defaultBmp1 = iconGen.makeIcon("Coffee")
        val defaultBmp2 = iconGen.makeIcon("Coffee")
        GoldenImageDiff.assertMatches(expected = defaultBmp1, actual = defaultBmp2, maxDiffPercentage = 0.0)

        iconGen.setStyle(IconGenerator.STYLE_RED)
        val redBmp = iconGen.makeIcon("Coffee")
        val styleDiff = GoldenImageDiff.compare(expected = defaultBmp1, actual = redBmp)
        truthAssertThat(styleDiff.matches).isFalse()
        truthAssertThat(styleDiff.diffPixelCount).isGreaterThan(100)

        iconGen.setRotation(0)
        val unrotated = iconGen.makeIcon("Wide Banner Text")
        iconGen.setRotation(90)
        val rotated90 = iconGen.makeIcon("Wide Banner Text")
        truthAssertThat(rotated90.width).isEqualTo(unrotated.height)
        truthAssertThat(rotated90.height).isEqualTo(unrotated.width)

        iconGen.setRotation(0)
        val emptyMapSnapshot = googleMap.captureSnapshot()

        val marker = checkNotNull(
            googleMap.addMarker(
                MarkerOptions()
                    .position(center)
                    .icon(BitmapDescriptorFactory.fromBitmap(redBmp))
                    .anchor(iconGen.getAnchorU(), iconGen.getAnchorV()),
            ),
        )
        assertThat(googleMap).hasMarkerCount(1)
        truthAssertThat(marker.shadow.iconBitmap).isSameInstanceAs(redBmp)

        val mapDiff = GoldenImageDiff.compare(expected = emptyMapSnapshot, actual = googleMap.captureSnapshot())
        truthAssertThat(mapDiff.matches).isFalse()
        truthAssertThat(mapDiff.diffPixelCount).isGreaterThan(100)
    }

    @Test
    fun squareTextViewAndRotationLayout_enforceSquareBoundsAndRotatedDimensions() {
        val squareTextView = SquareTextView(activity).apply { text = "12345" }
        squareTextView.measure(
            View.MeasureSpec.makeMeasureSpec(120, View.MeasureSpec.EXACTLY),
            View.MeasureSpec.makeMeasureSpec(40, View.MeasureSpec.EXACTLY),
        )
        truthAssertThat(squareTextView.measuredWidth).isEqualTo(120)
        truthAssertThat(squareTextView.measuredHeight).isEqualTo(120)

        val rotationLayout = RotationLayout(activity).apply {
            addView(TextView(activity))
            setViewRotation(90)
        }
        rotationLayout.measure(
            View.MeasureSpec.makeMeasureSpec(200, View.MeasureSpec.EXACTLY),
            View.MeasureSpec.makeMeasureSpec(80, View.MeasureSpec.EXACTLY),
        )
        truthAssertThat(rotationLayout.measuredWidth).isEqualTo(80)
        truthAssertThat(rotationLayout.measuredHeight).isEqualTo(200)
    }

    @Test
    fun animateMarkerTo_interpolatesShortestPathAcrossAntimeridianOnStatefulMarker() {
        val start = LatLng(0.0, 179.0)
        val end = LatLng(2.0, -179.0)
        val marker = checkNotNull(googleMap.addMarker(MarkerOptions().position(start)))

        AnimationUtil.animateMarkerTo(marker, end, durationInMs = 2000L)

        // At midpoint (~1000ms), shortest-path wrapping crosses ±180° near (1.0, 180.0) rather than (1.0, 0.0)
        ShadowLooper.idleMainLooper(1000, TimeUnit.MILLISECONDS)
        assertThat(marker).isAt(LatLng(1.0, 180.0), tolerance = 2.kilometers)

        ShadowLooper.idleMainLooper(1100, TimeUnit.MILLISECONDS)
        assertThat(marker).isAt(end, tolerance = 1.meters)
    }
}
