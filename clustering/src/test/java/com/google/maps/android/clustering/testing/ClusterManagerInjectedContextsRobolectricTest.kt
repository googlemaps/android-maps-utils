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
package com.google.maps.android.clustering.testing

import android.app.Activity
import android.os.Bundle
import android.view.View
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.model.LatLng
import com.google.android.maps.robolectric.annotation.EnableMapsShadows
import com.google.android.maps.robolectric.shadows.ShadowGoogleMap
import com.google.common.truth.Truth.assertThat
import com.google.maps.android.clustering.ClusterItem
import com.google.maps.android.clustering.ClusterManager
import com.google.maps.android.clustering.view.DefaultClusterRenderer
import com.google.maps.android.collections.MarkerManager
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestCoroutineScheduler
import kotlinx.coroutines.test.TestDispatcher
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import org.robolectric.shadow.api.Shadow
import org.robolectric.shadows.ShadowLooper
import java.util.concurrent.atomic.AtomicReference

/**
 * Validates [ClusterManager]'s coroutine context injection on a stateful [GoogleMap]
 * using the Android Maps Testing Toolkit (:robolectric-testing).
 */
@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [34])
@EnableMapsShadows
class ClusterManagerInjectedContextsRobolectricTest {

    data class SimpleTestItem(
        val id: String,
        override val position: LatLng,
        override val title: String? = null,
        override val snippet: String? = null,
        override val zIndex: Float? = 0f,
    ) : ClusterItem

    private lateinit var activity: Activity
    private lateinit var mapView: MapView
    private lateinit var googleMap: GoogleMap
    private lateinit var shadowMap: ShadowGoogleMap
    private lateinit var scheduler: TestCoroutineScheduler
    private lateinit var mainDispatcher: TestDispatcher
    private lateinit var backgroundDispatcher: TestDispatcher

    @Before
    fun setUp() {
        activity = Robolectric.buildActivity(Activity::class.java).create().get()
        mapView = MapView(activity).apply {
            onCreate(Bundle())
            onStart()
            onResume()
            measure(
                View.MeasureSpec.makeMeasureSpec(400, View.MeasureSpec.EXACTLY),
                View.MeasureSpec.makeMeasureSpec(400, View.MeasureSpec.EXACTLY),
            )
            layout(0, 0, 400, 400)
        }

        val mapRef = AtomicReference<GoogleMap>()
        mapView.getMapAsync { mapRef.set(it) }
        ShadowLooper.idleMainLooper()

        googleMap = checkNotNull(mapRef.get())
        shadowMap = Shadow.extract(googleMap) as ShadowGoogleMap
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(0.0, 0.0), 10f))
        ShadowLooper.idleMainLooper()

        scheduler = TestCoroutineScheduler()
        mainDispatcher = StandardTestDispatcher(scheduler)
        backgroundDispatcher = StandardTestDispatcher(scheduler)
    }

    private fun pumpUntilSettled() {
        scheduler.advanceUntilIdle()
        repeat(30) {
            ShadowLooper.idleMainLooper()
            Thread.sleep(5)
        }
        ShadowLooper.idleMainLooper()
    }

    @Test
    fun clusterWithInjectedContexts_rendersMarkersOnlyAfterSchedulerAdvances() {
        val clusterManager = ClusterManager<SimpleTestItem>(
            activity,
            googleMap,
            MarkerManager(googleMap),
            mainDispatcher,
            backgroundDispatcher,
        )
        (clusterManager.renderer as DefaultClusterRenderer).setAnimation(false)
        googleMap.setOnCameraIdleListener(clusterManager)

        // Drain any initial tasks from ClusterManager setup
        pumpUntilSettled()

        // Initially zero markers
        assertThat(shadowMap.markers).isEmpty()

        // Add 5 items that are close together (should cluster)
        val items = listOf(
            SimpleTestItem("1", LatLng(0.0, 0.0)),
            SimpleTestItem("2", LatLng(0.0001, 0.0001)),
            SimpleTestItem("3", LatLng(0.0002, 0.0002)),
            SimpleTestItem("4", LatLng(0.0003, 0.0003)),
            SimpleTestItem("5", LatLng(0.0004, 0.0004)),
        )
        clusterManager.addItems(items)
        clusterManager.cluster()

        // Before advancing test scheduler, background clustering has not completed
        // so no markers are drawn
        assertThat(shadowMap.markers).isEmpty()

        // Advance the scheduler and looper to let background computation run on backgroundDispatcher
        // and UI delivery run on mainDispatcher
        pumpUntilSettled()

        // Now clusters are rendered on the map!
        assertThat(shadowMap.markers).isNotEmpty()
    }

    @Test
    fun clusterWithCustomMarkerManagerAndInjectedContexts() {
        // Use 5-arg constructor
        val customMarkerManager = MarkerManager(googleMap)
        val clusterManager = ClusterManager<SimpleTestItem>(
            activity,
            googleMap,
            customMarkerManager,
            mainDispatcher,
            backgroundDispatcher,
        )
        (clusterManager.renderer as DefaultClusterRenderer).setAnimation(false)

        pumpUntilSettled()

        clusterManager.addItem(SimpleTestItem("item1", LatLng(0.0, 0.0)))
        clusterManager.cluster()

        pumpUntilSettled()

        assertThat(shadowMap.markers).hasSize(1)
        assertThat(shadowMap.markers.first().position).isEqualTo(LatLng(0.0, 0.0))
    }

    @Test
    fun rapidClusterCalls_cancelInFlightCoroutinesWithoutCrashing() {
        val clusterManager = ClusterManager<SimpleTestItem>(
            activity,
            googleMap,
            MarkerManager(googleMap),
            mainDispatcher,
            backgroundDispatcher,
        )
        (clusterManager.renderer as DefaultClusterRenderer).setAnimation(false)

        pumpUntilSettled()

        for (i in 1..20) {
            clusterManager.addItem(SimpleTestItem("item$i", LatLng(i * 0.001, i * 0.001)))
            clusterManager.cluster()
        }

        // Settle completely
        pumpUntilSettled()

        // Settles cleanly without throwing CancellationException or ConcurrentModificationException
        assertThat(shadowMap.markers).isNotEmpty()
    }
}
