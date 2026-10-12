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
package com.google.maps.android.utils.demo.smoke

import android.app.Activity
import android.os.SystemClock
import android.util.Log
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.test.espresso.Espresso
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.runner.lifecycle.ActivityLifecycleMonitorRegistry
import androidx.test.runner.lifecycle.Stage
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.common.truth.Truth.assertWithMessage
import com.google.maps.android.utils.demo.MainActivity
import com.google.maps.android.utils.demo.StreetViewDemoActivity
import com.google.maps.android.utils.demo.StreetViewDemoJavaActivity
import com.google.maps.android.utils.demo.demoGroups
import com.google.maps.android.utils.demo.demoTestTag
import com.google.maps.android.utils.demo.groupTestTag
import com.google.maps.android.utils.demo.hasMapsApiKey
import com.google.maps.android.utils.demo.model.Demo
import com.google.maps.android.utils.demo.model.DemoGroup
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

/**
 * End-to-end happy path for the demo app: starts [MainActivity], opens every demo through the
 * menu like a user would, interacts with its map, and navigates back.
 *
 * For each demo it checks that:
 * - the demo activity is reached from the menu and stays resumed (no crash, no early `finish()`),
 * - its map becomes ready, and survives a zoom out and back in, which also triggers the
 *   camera-idle path that clustering and other utilities hook into,
 * - back navigation returns to the menu.
 *
 * The demo list comes from [demoGroups], so new demos are covered automatically.
 *
 * Without a Maps API key in a valid format the demos close themselves, so no cases are generated
 * and a warning is logged. An assumption would be reported as a failure by the test platform.
 * Tiles only render with a real key, so waiting for the map to finish loading is opt-in:
 * pass `-Pandroid.testInstrumentationRunnerArguments.requireMapLoaded=true` to enable it.
 */
@RunWith(Parameterized::class)
class DemoSmokeTest(
    private val group: DemoGroup,
    private val demo: Demo,
    @Suppress("unused") private val name: String,
) {
    private val instrumentation = InstrumentationRegistry.getInstrumentation()

    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    private val requireMapLoaded =
        InstrumentationRegistry.getArguments().getString(ARG_REQUIRE_MAP_LOADED).toBoolean()

    @Test
    fun opensFromMenuAndRuns() {
        composeRule.onNodeWithTag(groupTestTag(group)).performClick()
        composeRule.onNodeWithTag(demoTestTag(demo)).performScrollTo().performClick()

        val activity = waitForResumedActivity(demo.activityClass)

        if (demo.activityClass !in DEMOS_WITHOUT_MAP) {
            val map = awaitMap(activity)
            zoomAndSettle(map, -1f)
            zoomAndSettle(map, 1f)
        } else {
            SystemClock.sleep(SETTLE_MS)
        }

        assertWithMessage("${demo.activityClass.simpleName} finished by itself")
            .that(activity.isFinishing)
            .isFalse()
        assertWithMessage("${demo.activityClass.simpleName} is no longer in the foreground")
            .that(resumedActivity())
            .isSameInstanceAs(activity)

        Espresso.pressBackUnconditionally()
        waitForResumedActivity(MainActivity::class.java)
    }

    private fun resumedActivity(): Activity? {
        var resumed: Activity? = null
        instrumentation.runOnMainSync {
            resumed =
                ActivityLifecycleMonitorRegistry
                    .getInstance()
                    .getActivitiesInStage(Stage.RESUMED)
                    .firstOrNull()
        }
        return resumed
    }

    private fun waitForResumedActivity(activityClass: Class<out Activity>): Activity {
        val deadline = SystemClock.uptimeMillis() + TIMEOUT_MS
        while (SystemClock.uptimeMillis() < deadline) {
            val resumed = resumedActivity()
            if (resumed != null && resumed.javaClass == activityClass) {
                return resumed
            }
            SystemClock.sleep(POLL_MS)
        }
        throw AssertionError(
            "${activityClass.simpleName} was not resumed within ${TIMEOUT_MS}ms, " +
                "the foreground activity is ${resumedActivity()?.javaClass?.simpleName}",
        )
    }

    /**
     * Finds the map hosted by [activity], either as a [SupportMapFragment] (possibly nested) or as a
     * [MapView] anywhere in the view hierarchy, and waits for it to be ready.
     */
    private fun awaitMap(activity: Activity): GoogleMap {
        val deadline = SystemClock.uptimeMillis() + TIMEOUT_MS
        var map: GoogleMap? = null
        val ready = CountDownLatch(1)
        val callback = OnMapReadyCallback {
            map = it
            ready.countDown()
        }
        var requested = false
        while (!requested && SystemClock.uptimeMillis() < deadline) {
            instrumentation.runOnMainSync {
                val fragment = (activity as? FragmentActivity)?.supportFragmentManager?.findMapFragment()
                val mapView = findMapView(activity.window.decorView)
                when {
                    fragment != null -> fragment.getMapAsync(callback)
                    mapView != null -> mapView.getMapAsync(callback)
                    else -> return@runOnMainSync
                }
                requested = true
            }
            if (!requested) SystemClock.sleep(POLL_MS)
        }
        assertWithMessage("No SupportMapFragment or MapView found in ${activity.javaClass.simpleName}")
            .that(requested)
            .isTrue()
        assertWithMessage("Map in ${activity.javaClass.simpleName} was not ready within ${TIMEOUT_MS}ms")
            .that(ready.await(TIMEOUT_MS, TimeUnit.MILLISECONDS))
            .isTrue()
        return map!!
    }

    /**
     * Zooms by [delta] and gives the demo time to react to the camera change. When
     * [requireMapLoaded] is set, also waits for the map to finish rendering the new viewport.
     *
     * Only the map loaded callback is replaced here, demos rely on the camera listeners.
     */
    private fun zoomAndSettle(map: GoogleMap, delta: Float) {
        val loaded = CountDownLatch(1)
        instrumentation.runOnMainSync {
            map.setOnMapLoadedCallback { loaded.countDown() }
            map.moveCamera(CameraUpdateFactory.zoomBy(delta))
        }
        if (requireMapLoaded) {
            assertWithMessage("Map in ${demo.activityClass.simpleName} did not finish loading")
                .that(loaded.await(MAP_LOADED_TIMEOUT_MS, TimeUnit.MILLISECONDS))
                .isTrue()
        }
        SystemClock.sleep(SETTLE_MS)
        instrumentation.waitForIdleSync()
    }

    private fun FragmentManager.findMapFragment(): SupportMapFragment? {
        for (fragment in fragments) {
            if (fragment is SupportMapFragment) return fragment
            fragment.childFragmentManager.findMapFragment()?.let { return it }
        }
        return null
    }

    private fun findMapView(view: View): MapView? {
        if (view is MapView) return view
        if (view is ViewGroup) {
            for (i in 0 until view.childCount) {
                findMapView(view.getChildAt(i))?.let { return it }
            }
        }
        return null
    }

    companion object {
        private const val TAG = "DemoSmokeTest"
        private const val ARG_REQUIRE_MAP_LOADED = "requireMapLoaded"
        private const val TIMEOUT_MS = 15_000L
        private const val MAP_LOADED_TIMEOUT_MS = 30_000L
        private const val SETTLE_MS = 1_500L
        private const val POLL_MS = 100L

        /** Demos that show results without a map. */
        private val DEMOS_WITHOUT_MAP: Set<Class<out Activity>> =
            setOf(
                StreetViewDemoActivity::class.java,
                StreetViewDemoJavaActivity::class.java,
            )

        @JvmStatic
        @Parameterized.Parameters(name = "{2}")
        fun demos(): List<Array<Any>> {
            if (!hasMapsApiKey(InstrumentationRegistry.getInstrumentation().targetContext)) {
                Log.w(TAG, "No Maps API key in a valid format in secrets.properties, skipping all demos.")
                return emptyList()
            }
            return demoGroups().flatMap { group ->
                group.demos.map { demo -> arrayOf(group, demo, demo.activityClass.simpleName) }
            }
        }
    }
}
