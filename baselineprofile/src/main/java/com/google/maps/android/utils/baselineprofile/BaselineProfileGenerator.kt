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

package com.google.maps.android.utils.baselineprofile

import androidx.benchmark.macro.MacrobenchmarkScope
import androidx.benchmark.macro.junit4.BaselineProfileRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Generates the Baseline Profiles of the library modules by running the demos that exercise them:
 * polyline encoding, simplification and distances (library), IconGenerator (ui), clustering,
 * heatmaps and GeoJSON and KML layers (data).
 *
 * Run it with an emulator or device attached and a real MAPS_API_KEY in secrets.properties:
 * ```
 * ./gradlew :library:generateBaselineProfile :clustering:generateBaselineProfile \
 *   :heatmaps:generateBaselineProfile :data:generateBaselineProfile :ui:generateBaselineProfile
 * ```
 * Each module keeps only the rules for its own classes (see BaselineProfileConventionPlugin in
 * build-logic) and the result is written to its `src/main/generated/baselineProfiles/baseline-prof.txt`,
 * which is packaged in the AAR.
 */
@RunWith(AndroidJUnit4::class)
class BaselineProfileGenerator {
    @get:Rule
    val rule = BaselineProfileRule()

    @Test
    fun generate() {
        rule.collect(packageName = TARGET_PACKAGE) {
            pressHome()
            startActivityAndWait()
            DEMOS.forEach { demo -> exploreDemo(demo) }
        }
    }
}

/** Demos that cover the public API of library, ui, clustering, heatmaps and data. */
private val DEMOS = listOf(
    "PolyDecodeDemoActivity",
    "PolySimplifyDemoActivity",
    "FlexiblePolylineDemoActivity",
    "DistanceDemoActivity",
    "KtxExtensionsDemoActivity",
    "IconGeneratorDemoActivity",
    "ClusteringDemoActivity",
    "BigClusteringDemoActivity",
    "CustomMarkerClusteringDemoActivity",
    "ZoomClusteringDemoActivity",
    "HeatmapsDemoActivity",
    "GeoJsonDemoActivity",
    "KmlDemoActivity",
    "MultiLayerDemoActivity",
)

/**
 * Opens [demo], waits for the map to load and pans and zooms it, so camera changes, re-clustering
 * and layer rendering are part of the profile.
 */
internal fun MacrobenchmarkScope.exploreDemo(demo: String) {
    // startActivityAndWait relies on frame stats that the map's SurfaceView does not always
    // report, so the demos are started with `am start -W`, which waits for the launch instead.
    device.executeShellCommand("am start -W -n $packageName/$DEMO_PACKAGE.$demo")
    // The map loads asynchronously after the first frame, and there is no view to wait for.
    Thread.sleep(MAP_LOAD_DELAY_MS)

    val centerX = device.displayWidth / 2
    val centerY = device.displayHeight / 2
    val distance = device.displayWidth / 4
    device.swipe(centerX + distance, centerY, centerX - distance, centerY, SWIPE_STEPS)
    device.waitForIdle()
    device.swipe(centerX, centerY + distance, centerX, centerY - distance, SWIPE_STEPS)
    device.waitForIdle()
    // Double tap zooms in.
    device.click(centerX, centerY)
    device.click(centerX, centerY)
    Thread.sleep(CAMERA_ANIMATION_DELAY_MS)

    device.pressBack()
    device.waitForIdle()
}

internal const val TARGET_PACKAGE = "com.google.maps.android.utils.demo"
private const val DEMO_PACKAGE = "com.google.maps.android.utils.demo"

private const val MAP_LOAD_DELAY_MS = 3_000L
private const val CAMERA_ANIMATION_DELAY_MS = 1_000L
private const val SWIPE_STEPS = 20
