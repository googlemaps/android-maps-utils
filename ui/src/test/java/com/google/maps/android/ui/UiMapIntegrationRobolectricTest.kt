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
import com.google.maps.android.robolectric.MapsRobolectricTestRunner
import com.google.maps.android.robolectric.ktx.createTestMapView
import com.google.maps.android.robolectric.ktx.getMapBlocking
import com.google.maps.android.robolectric.ktx.markers
import com.google.maps.android.robolectric.ktx.shadow
import com.google.maps.android.robolectric.powerassert.assertGeo
import com.google.maps.android.robolectric.powerassert.assertMap
import com.google.maps.android.robolectric.powerassert.assertMarker
import com.google.maps.android.robolectric.powerassert.distanceTo
import com.google.maps.android.robolectric.powerassert.kilometers
import com.google.maps.android.robolectric.powerassert.meters
import com.google.maps.android.testing.golden.GoldenImageDiff
import com.google.maps.android.testing.golden.GoldenTileStrategy
import com.google.maps.android.testing.golden.captureSnapshot
import com.google.maps.android.testing.golden.enableGoldenTesting
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import org.robolectric.shadows.ShadowLooper
import java.util.concurrent.TimeUnit

/**
 * Stateful Robolectric verification suite for `:ui` components ([IconGenerator], [RotationLayout],
 * [SquareTextView], and [AnimationUtil]) integrated against a measured [GoogleMap] and [ShadowLooper].
 */
@RunWith(MapsRobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [34])
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
        assertGeo(!styleDiff.matches && styleDiff.diffPixelCount > 100)

        iconGen.setRotation(0)
        val unrotated = iconGen.makeIcon("Wide Banner Text")
        iconGen.setRotation(90)
        val rotated90 = iconGen.makeIcon("Wide Banner Text")
        assertGeo(rotated90.width == unrotated.height && rotated90.height == unrotated.width)

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
        googleMap.assertMap(googleMap.markers.size == 1)
        marker.assertMarker(marker.shadow.iconBitmap === redBmp)

        val mapDiff = GoldenImageDiff.compare(expected = emptyMapSnapshot, actual = googleMap.captureSnapshot())
        assertGeo(!mapDiff.matches && mapDiff.diffPixelCount > 100)
    }

    @Test
    fun squareTextViewAndRotationLayout_enforceSquareBoundsAndRotatedDimensions() {
        val squareTextView = SquareTextView(activity).apply { text = "12345" }
        squareTextView.measure(
            View.MeasureSpec.makeMeasureSpec(120, View.MeasureSpec.EXACTLY),
            View.MeasureSpec.makeMeasureSpec(40, View.MeasureSpec.EXACTLY),
        )
        assertGeo(squareTextView.measuredWidth == 120 && squareTextView.measuredHeight == 120)

        val rotationLayout = RotationLayout(activity).apply {
            addView(TextView(activity))
            setViewRotation(90)
        }
        rotationLayout.measure(
            View.MeasureSpec.makeMeasureSpec(200, View.MeasureSpec.EXACTLY),
            View.MeasureSpec.makeMeasureSpec(80, View.MeasureSpec.EXACTLY),
        )
        assertGeo(rotationLayout.measuredWidth == 80 && rotationLayout.measuredHeight == 200)
    }

    @Test
    fun animateMarkerTo_interpolatesShortestPathAcrossAntimeridianOnStatefulMarker() {
        val start = LatLng(0.0, 179.0)
        val end = LatLng(2.0, -179.0)
        val marker = checkNotNull(googleMap.addMarker(MarkerOptions().position(start)))

        AnimationUtil.animateMarkerTo(marker, end, durationInMs = 2000L)

        // At midpoint (~1000ms), shortest-path wrapping crosses ±180° near (1.0, 180.0) rather than (1.0, 0.0)
        ShadowLooper.idleMainLooper(1000, TimeUnit.MILLISECONDS)
        marker.assertMarker((marker.position distanceTo LatLng(1.0, 180.0)) <= 2.kilometers)

        ShadowLooper.idleMainLooper(1100, TimeUnit.MILLISECONDS)
        marker.assertMarker((marker.position distanceTo end) <= 1.meters)
    }
}
