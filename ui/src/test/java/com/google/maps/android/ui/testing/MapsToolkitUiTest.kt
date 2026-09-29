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
package com.google.maps.android.ui.testing

import android.app.Activity
import android.os.Bundle
import android.os.SystemClock
import android.view.View
import android.widget.TextView
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.maps.robolectric.annotation.EnableMapsShadows
import com.google.android.maps.robolectric.shadows.ShadowGoogleMap
import com.google.android.maps.robolectric.shadows.ShadowMarker
import com.google.android.maps.robolectric.truth.LatLngSubject
import com.google.android.maps.robolectric.truth.isWithin
import com.google.android.maps.robolectric.truth.kilometers
import com.google.android.maps.robolectric.truth.meters
import com.google.android.maps.testing.golden.GoldenImageDiff
import com.google.android.maps.testing.golden.GoldenTileStrategy
import com.google.android.maps.testing.golden.captureSnapshot
import com.google.android.maps.testing.golden.enableGoldenTesting
import com.google.android.maps.testing.visual.GeminiVisualTestHelper
import com.google.common.truth.Truth.assertThat
import com.google.maps.android.ui.AnimationUtil
import com.google.maps.android.ui.IconGenerator
import com.google.maps.android.ui.RotationLayout
import com.google.maps.android.ui.SquareTextView
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicReference
import kotlin.test.assertFailsWith
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
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
 * End-to-end JVM integration and adversarial verification suite for the `:ui` module
 * ([IconGenerator], [RotationLayout], [SquareTextView], [AnimationUtil]) powered by the
 * Android Maps Testing Toolkit (`v1.1.0-rc01`).
 *
 * ## Architectural Scope
 * 1. **IconGenerator & Marker Snapshot Verification**: Generates bubble icons across styles
 *    (`STYLE_DEFAULT`, `STYLE_RED`, `STYLE_BLUE`, `STYLE_GREEN`, `STYLE_PURPLE`, `STYLE_ORANGE`),
 *    rotations (`0°`, `90°`, `180°`, `270°`), and content rotations, attaches them via
 *    [BitmapDescriptorFactory.fromBitmap] to markers on [ShadowGoogleMap], and verifies visual
 *    rendering on [MapView] via [GoldenImageDiff].
 * 2. **Visual Testing Toolkit (`com.google.android.maps.testing.visual`) Integration**:
 *    Exercises [GeminiVisualTestHelper] model resolution hierarchy (`preferredModel`,
 *    `gemini.model` system property, and `resetSessionModel()`).
 * 3. **Marker Animation Stepping Across the Antimeridian**: Steps [AnimationUtil.animateMarkerTo]
 *    frame-by-frame via [ShadowLooper] across the 180th meridian (`+179.0°` to `-179.0°`),
 *    verifying intermediate and final coordinates with [LatLngSubject].
 * 4. **Adversarial Defect Reproductions (Bug #10)**:
 *    - **Bug #10A**: `AnimationUtil.animateMarkerTo(marker, finalPosition, durationInMs = 0)`
 *      computes `t = 0 / 0.0f = Float.NaN` on the initial frame and corrupts `marker.position`
 *      to `LatLng(Double.NaN, Double.NaN)`.
 *    - **Bug #10B**: `IconGenerator.setRotation(degrees)` uses `(degrees + 360) % 360 / 90`,
 *      which produces a negative index for negative angles `<= -450°` (e.g. `-450°`), causing
 *      `getAnchorU()` / `getAnchorV()` to crash with `IllegalStateException`.
 */
@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [34])
@EnableMapsShadows
class MapsToolkitUiTest {

    private lateinit var activity: Activity
    private lateinit var mapView: MapView
    private lateinit var googleMap: GoogleMap
    private lateinit var shadowMap: ShadowGoogleMap

    @Before
    fun setUp() {
        activity = Robolectric.buildActivity(Activity::class.java).create().get()
        mapView = MapView(activity).apply {
            onCreate(Bundle())
            onStart()
            onResume()
            measure(
                View.MeasureSpec.makeMeasureSpec(300, View.MeasureSpec.EXACTLY),
                View.MeasureSpec.makeMeasureSpec(300, View.MeasureSpec.EXACTLY)
            )
            layout(0, 0, 300, 300)
        }

        val mapRef = AtomicReference<GoogleMap>()
        mapView.getMapAsync { mapRef.set(it) }
        ShadowLooper.idleMainLooper()

        googleMap = checkNotNull(mapRef.get())
        shadowMap = Shadow.extract(googleMap) as ShadowGoogleMap
    }

    // ============================================================================================
    // Section 1: IconGenerator Styles, Rotations, GoldenImageDiff & Visual Helper
    // ============================================================================================

    /**
     * Verifies that [IconGenerator] produces distinct, deterministic bitmaps across styles and
     * 90-degree rotations, swaps width/height at 90° and 270°, computes rotated anchor coordinates,
     * and renders visibly onto a [MapView] snapshot verified by [GoldenImageDiff].
     */
    @Test
    fun iconGenerator_rendersDistinctStylesAndRotationsVerifiedByGoldenImageDiff() = runTest {
        val dispatcher = UnconfinedTestDispatcher(testScheduler)
        val center = LatLng(37.7749, -122.4194)
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(center, 15f))
        googleMap.enableGoldenTesting(strategy = GoldenTileStrategy.SolidColorHash())

        val iconGen = IconGenerator(activity)
        val defaultBmp1 = iconGen.makeIcon("Coffee")
        val defaultBmp2 = iconGen.makeIcon("Coffee")

        // Deterministic rendering: two calls with same state produce 0.0% pixel diff
        GoldenImageDiff.assertMatches(
            expected = defaultBmp1,
            actual = defaultBmp2,
            maxDiffPercentage = 0.0
        )

        // Changing style from STYLE_DEFAULT (white) to STYLE_RED alters bubble pixels
        iconGen.setStyle(IconGenerator.STYLE_RED)
        val redBmp = iconGen.makeIcon("Coffee")
        val styleDiff = GoldenImageDiff.compare(expected = defaultBmp1, actual = redBmp)
        assertThat(styleDiff.matches).isFalse()
        assertThat(styleDiff.diffPixelCount).isGreaterThan(100)

        // Verify all other built-in styles render non-empty bitmaps
        for (style in listOf(
            IconGenerator.STYLE_WHITE,
            IconGenerator.STYLE_BLUE,
            IconGenerator.STYLE_GREEN,
            IconGenerator.STYLE_PURPLE,
            IconGenerator.STYLE_ORANGE
        )) {
            iconGen.setStyle(style)
            val bmp = iconGen.makeIcon("Tag")
            assertThat(bmp.width).isGreaterThan(0)
            assertThat(bmp.height).isGreaterThan(0)
        }

        // Verify 0°, 90°, 180°, 270° rotations swap dimensions and rotate anchors accurately
        iconGen.setRotation(0)
        assertThat(iconGen.getAnchorU()).isEqualTo(0.5f)
        assertThat(iconGen.getAnchorV()).isEqualTo(1.0f)
        val unrotated = iconGen.makeIcon("Wide Banner Text")

        iconGen.setRotation(90)
        assertThat(iconGen.getAnchorU()).isEqualTo(0.0f)
        assertThat(iconGen.getAnchorV()).isEqualTo(0.5f)
        val rotated90 = iconGen.makeIcon("Wide Banner Text")
        assertThat(rotated90.width).isEqualTo(unrotated.height)
        assertThat(rotated90.height).isEqualTo(unrotated.width)

        iconGen.setRotation(180)
        assertThat(iconGen.getAnchorU()).isEqualTo(0.5f)
        assertThat(iconGen.getAnchorV()).isEqualTo(0.0f)

        iconGen.setRotation(270)
        assertThat(iconGen.getAnchorU()).isEqualTo(1.0f)
        assertThat(iconGen.getAnchorV()).isEqualTo(0.5f)

        // Attach the generated IconGenerator bitmap to a Marker on GoogleMap and verify via snapshot
        iconGen.setRotation(0)
        val emptyMapJob = async(dispatcher) { googleMap.captureSnapshot() }
        ShadowLooper.idleMainLooper()
        val emptyMapSnapshot = emptyMapJob.await()

        val marker = checkNotNull(
            googleMap.addMarker(
                MarkerOptions()
                    .position(center)
                    .icon(BitmapDescriptorFactory.fromBitmap(redBmp))
                    .anchor(iconGen.getAnchorU(), iconGen.getAnchorV())
            )
        )
        val shadowMarker = Shadow.extract(marker) as ShadowMarker
        assertThat(shadowMarker.iconBitmap).isSameInstanceAs(redBmp)

        val mapWithIconJob = async(dispatcher) { googleMap.captureSnapshot() }
        ShadowLooper.idleMainLooper()
        val mapWithIconSnapshot = mapWithIconJob.await()

        val mapDiff = GoldenImageDiff.compare(
            expected = emptyMapSnapshot,
            actual = mapWithIconSnapshot
        )
        assertThat(mapDiff.matches).isFalse()
        assertThat(mapDiff.diffPixelCount).isGreaterThan(100)
    }

    /**
     * Verifies [SquareTextView] square dimension enforcement, [RotationLayout] dimension swapping,
     * and [GeminiVisualTestHelper] model resolution hierarchy from `maps-testing-visual`.
     */
    @Test
    fun squareTextViewRotationLayoutAndGeminiVisualTestHelper_behaveDeterministically() = runTest {
        // 1. SquareTextView always measures to max(width, height) x max(width, height)
        val squareTextView = SquareTextView(activity).apply {
            text = "12345"
        }
        squareTextView.measure(
            View.MeasureSpec.makeMeasureSpec(120, View.MeasureSpec.EXACTLY),
            View.MeasureSpec.makeMeasureSpec(40, View.MeasureSpec.EXACTLY)
        )
        assertThat(squareTextView.measuredWidth).isEqualTo(120)
        assertThat(squareTextView.measuredHeight).isEqualTo(120)

        // 2. RotationLayout swaps width and height when rotated by 90 or 270 degrees
        val rotationLayout = RotationLayout(activity)
        val child = TextView(activity)
        rotationLayout.addView(child)
        rotationLayout.setViewRotation(90)
        rotationLayout.measure(
            View.MeasureSpec.makeMeasureSpec(200, View.MeasureSpec.EXACTLY),
            View.MeasureSpec.makeMeasureSpec(80, View.MeasureSpec.EXACTLY)
        )
        assertThat(rotationLayout.measuredWidth).isEqualTo(80)
        assertThat(rotationLayout.measuredHeight).isEqualTo(200)

        // 3. GeminiVisualTestHelper model resolution priority
        GeminiVisualTestHelper.resetSessionModel()
        val explicitHelper = GeminiVisualTestHelper(preferredModel = "gemini-2.5-flash")
        assertThat(explicitHelper.getOrResolveModel("dummy-key")).isEqualTo("gemini-2.5-flash")

        System.setProperty("gemini.model", "gemini-2.5-pro-preview")
        try {
            val propHelper = GeminiVisualTestHelper()
            assertThat(propHelper.getOrResolveModel("dummy-key")).isEqualTo("gemini-2.5-pro-preview")
        } finally {
            System.clearProperty("gemini.model")
            GeminiVisualTestHelper.resetSessionModel()
        }
    }

    // ============================================================================================
    // Section 2: Marker Animation Across Antimeridian & Bug #10 Reproductions
    // ============================================================================================

    /**
     * Verifies that [AnimationUtil.animateMarkerTo] smoothly interpolates a [com.google.android.gms.maps.model.Marker]
     * across the 180th meridian (International Date Line) using shortest-path longitude wrapping
     * (`+179.0°` to `-179.0°` via `±180.0°` rather than traversing 358° the wrong way around the globe).
     */
    @Test
    fun animationUtil_animateMarkerTo_interpolatesShortestPathAcrossAntimeridian() {
        val start = LatLng(0.0, 179.0)
        val end = LatLng(2.0, -179.0)
        val marker = checkNotNull(googleMap.addMarker(MarkerOptions().position(start)))

        // Default durationInMs = 2000L (125 * 16ms = 2000ms)
        AnimationUtil.animateMarkerTo(marker, end, durationInMs = 2000L)

        // Advance clock to midpoint (1000ms, 62 frames * 16ms = 992ms): marker crosses the antimeridian
        // near (lat = 1.0, lng = ±180.0), within 2km of (1.0, 180.0) (vs ~20,000km if it went the wrong way!)
        ShadowLooper.idleMainLooper(1000, TimeUnit.MILLISECONDS)
        LatLngSubject.assertThat(marker.position)
            .isWithin(2.kilometers)
            .of(LatLng(1.0, 180.0))

        // Advance clock to completion (2000ms): marker reaches final destination (2.0, -179.0)
        ShadowLooper.idleMainLooper(1100, TimeUnit.MILLISECONDS)
        LatLngSubject.assertThat(marker.position)
            .isWithin(1.meters)
            .of(end)
    }

    /**
     * **BUG #10A REPRODUCTION (`AnimationUtil.animateMarkerTo` With `durationInMs = 0` Corrupts
     * `Marker.position` to `LatLng(NaN, NaN)`)**:
     *
     * - When a caller invokes `AnimationUtil.animateMarkerTo(marker, finalPosition, durationInMs = 0)`
     *   (for example, when user preferences or test settings disable animation duration by passing `0L`),
     *   `AnimationUtil.kt:54-57` executes on the first posted frame (`elapsed = 0L`):
     *   ```kotlin
     *   val elapsed = SystemClock.uptimeMillis() - start
     *   val t = elapsed / durationInMs.toFloat() // 0 / 0.0f == Float.NaN!
     *   val v = interpolator.getInterpolation(t) // Float.NaN!
     *   marker.position = latLngInterpolator.interpolate(v, startPosition, finalPosition)
     *   ```
     * - Because `0 / 0.0f` is `Float.NaN` (and `t < 1` is `false` for `NaN`), the animation loop
     *   terminates immediately after setting `marker.position = LatLng(Double.NaN, Double.NaN)`!
     *
     * **BUG #10B REPRODUCTION (`AnimationUtil.animateMarkerTo` Does Not Clamp `t` to `1.0f`, Causing
     * `AccelerateDecelerateInterpolator` to Rebound Backward on Non-Multiple-of-16ms Durations or Frame Drops)**:
     *
     * - `AnimationUtil.kt:55-63` computes `val t = elapsed / durationInMs.toFloat()` without clamping
     *   `t.coerceAtMost(1.0f)`.
     * - Because `AccelerateDecelerateInterpolator.getInterpolation(t)` evaluates
     *   `cos((t + 1) * PI) / 2 + 0.5`, whenever `elapsed > durationInMs` on the final tick (for example,
     *   `durationInMs = 1000L` where `63 * 16ms = 1008ms`, or when the main thread drops frames),
     *   `t > 1.0f` causes the cosine curve to **rebound backward** (`v < 1.0f`) and terminate without
     *   ever snapping `marker.position` to `finalPosition`!
     *
     * **BUG #10C REPRODUCTION (`IconGenerator.setRotation` Crashes `getAnchorU()` / `getAnchorV()`
     * on Negative Angles `<= -450°`)**:
     *
     * - `IconGenerator.kt:135-137` computes `rotation = (degrees + 360) % 360 / 90`.
     * - Because Kotlin's `%` operator is remainder (preserving the dividend's sign) rather than
     *   `Math.floorMod`, passing `degrees = -450` yields `(-450 + 360) % 360 / 90 == -1`, which
     *   subsequently crashes `rotateAnchor` with `IllegalStateException`!
     */
    @Test
    fun bug10_animationUtilZeroDurationAndClampedOvershootAndIconGeneratorNegativeRotation() {
        val start = LatLng(0.0, 179.0)
        val destination = LatLng(2.0, -179.0)
        val marker = checkNotNull(googleMap.addMarker(MarkerOptions().position(start)))

        // BUG #10A VERIFIED FIXED: durationInMs = 0L snaps directly to destination without NaN!
        AnimationUtil.animateMarkerTo(marker, destination, durationInMs = 0L)
        ShadowLooper.idleMainLooper()
        LatLngSubject.assertThat(marker.position)
            .isWithin(1.meters)
            .of(destination)

        // BUG #10B VERIFIED FIXED: With durationInMs = 1000L, clamping t to 1.0f lands on exact destination!
        marker.position = start
        AnimationUtil.animateMarkerTo(marker, destination, durationInMs = 1000L)
        ShadowLooper.idleMainLooper(1100, TimeUnit.MILLISECONDS)
        LatLngSubject.assertThat(marker.position)
            .isWithin(1.meters)
            .of(destination)

        // BUG #10C VERIFIED FIXED: IconGenerator.setRotation(-450) normalizes via floorMod to 270° (rotation = 3)!
        val iconGenerator = IconGenerator(activity)
        iconGenerator.setRotation(-450)
        assertThat(iconGenerator.getAnchorU()).isWithin(0.001f).of(1.0f)
        assertThat(iconGenerator.getAnchorV()).isWithin(0.001f).of(0.5f)
    }
}
