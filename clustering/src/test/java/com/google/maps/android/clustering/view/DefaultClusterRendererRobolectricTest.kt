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
package com.google.maps.android.clustering.view

import android.app.Activity
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.clustering.Cluster
import com.google.maps.android.clustering.ClusterItem
import com.google.maps.android.clustering.ClusterManager
import com.google.maps.android.clustering.clusterClickEvents
import com.google.maps.android.clustering.clusterInfoWindowClickEvents
import com.google.maps.android.clustering.clusterInfoWindowLongClickEvents
import com.google.maps.android.clustering.clusterItemClickEvents
import com.google.maps.android.clustering.clusterItemInfoWindowClickEvents
import com.google.maps.android.clustering.clusterItemInfoWindowLongClickEvents
import com.google.maps.android.robolectric.MapsRobolectricTestRunner
import com.google.maps.android.robolectric.ktx.click
import com.google.maps.android.robolectric.ktx.clickInfoWindow
import com.google.maps.android.robolectric.ktx.createTestMapView
import com.google.maps.android.robolectric.ktx.getMapBlocking
import com.google.maps.android.robolectric.ktx.longClickInfoWindow
import com.google.maps.android.robolectric.ktx.markers
import com.google.maps.android.robolectric.ktx.shadow
import com.google.maps.android.robolectric.powerassert.assertGeo
import com.google.maps.android.robolectric.powerassert.assertMap
import com.google.maps.android.robolectric.powerassert.assertMarker
import com.google.maps.android.robolectric.powerassert.distanceTo
import com.google.maps.android.robolectric.powerassert.meters
import com.google.maps.android.testing.golden.GoldenImageDiff
import com.google.maps.android.testing.golden.GoldenTileStrategy
import com.google.maps.android.testing.golden.captureSnapshot
import com.google.maps.android.testing.golden.enableGoldenTesting
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import org.robolectric.shadows.ShadowLooper
import java.util.concurrent.CountDownLatch
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicReference

/**
 * Stateful Robolectric verification suite for [DefaultClusterRenderer] zoom-level marker
 * transitions, KTX [ClusterManager] click/info-window Flows, and cached marker state updates.
 */
@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(MapsRobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [34])
class DefaultClusterRendererRobolectricTest {

    class MutableClusterItem(
        val id: String,
        override var position: LatLng,
        override var title: String?,
        override var snippet: String?,
        override var zIndex: Float? = 0f,
    ) : ClusterItem {
        fun update(
            newPos: LatLng = position,
            newTitle: String? = title,
            newSnippet: String? = snippet,
            newZIndex: Float? = zIndex,
        ) {
            position = newPos
            title = newTitle
            snippet = newSnippet
            zIndex = newZIndex
        }

        override fun equals(other: Any?): Boolean = other is MutableClusterItem && other.id == id
        override fun hashCode(): Int = id.hashCode()
    }

    private lateinit var activity: Activity
    private lateinit var mapView: MapView
    private lateinit var googleMap: GoogleMap
    private lateinit var clusterManager: ClusterManager<MutableClusterItem>
    private lateinit var renderer: DefaultClusterRenderer<MutableClusterItem>
    private lateinit var renderExecutor: ExecutorService
    private val activeRenderLatch = AtomicReference<CountDownLatch?>()

    @Before
    fun setUp() {
        activity = Robolectric.buildActivity(Activity::class.java).create().get()
        mapView = createTestMapView(width = 400, height = 400, activity = activity).apply {
            onStart()
            onResume()
        }
        googleMap = mapView.getMapBlocking()

        clusterManager = ClusterManager(
            activity,
            googleMap,
            UnconfinedTestDispatcher(),
            UnconfinedTestDispatcher(),
        )
        renderExecutor = Executors.newSingleThreadExecutor()
        renderer = DefaultClusterRenderer(activity, googleMap, clusterManager) { command ->
            val latch = CountDownLatch(1)
            activeRenderLatch.set(latch)
            renderExecutor.execute {
                try {
                    command.run()
                } finally {
                    latch.countDown()
                }
            }
        }.apply {
            setAnimation(false)
        }
        clusterManager.renderer = renderer

        ShadowLooper.idleMainLooper()
        googleMap.setOnCameraIdleListener(clusterManager)
        googleMap.setOnMarkerClickListener(clusterManager)
        googleMap.setOnInfoWindowClickListener(clusterManager)
        googleMap.setOnInfoWindowLongClickListener(clusterManager.markerManager)
    }

    @After
    fun tearDown() {
        renderExecutor.shutdownNow()
    }

    private fun renderClustersAndIdle(clusters: Set<Cluster<MutableClusterItem>>) {
        activeRenderLatch.set(null)
        renderer.onClustersChanged(clusters)
        ShadowLooper.idleMainLooper()

        val latch = activeRenderLatch.get() ?: return
        val deadlineNanos = System.nanoTime() + TimeUnit.SECONDS.toNanos(5)
        while (!latch.await(1, TimeUnit.MILLISECONDS)) {
            ShadowLooper.idleMainLooper()
            check(System.nanoTime() < deadlineNanos) {
                "Timed out waiting for DefaultClusterRenderer RenderTask to finish"
            }
        }
        ShadowLooper.idleMainLooper()
    }

    @Test
    fun zoomTransition_aggregatesClusterIconAtLowZoomAndSplitsMarkersAtHighZoom() = runTest {
        val center = LatLng(37.7749, -122.4194)
        googleMap.enableGoldenTesting(strategy = GoldenTileStrategy.SolidColorHash(), fadeIn = false)
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(center, 10f))
        val emptySnapshot = googleMap.captureSnapshot()

        val items = (1..6).map { i ->
            MutableClusterItem(
                id = "item_$i",
                position = LatLng(center.latitude + i * 0.0001, center.longitude + i * 0.0001),
                title = "Place #$i",
                snippet = "Snippet #$i",
                zIndex = i.toFloat(),
            )
        }
        clusterManager.addItems(items)

        val lowZoomClusters = clusterManager.algorithm.getClusters(10f)
        assertGeo(lowZoomClusters.size == 1)
        val singleCluster = lowZoomClusters.single()
        renderClustersAndIdle(lowZoomClusters)

        googleMap.assertMap(
            clusterManager.clusterMarkerCollection.getMarkers().size == 1 &&
                clusterManager.markerCollection.getMarkers().isEmpty() &&
                googleMap.markers.size == 1,
        )

        val clusterMarker = checkNotNull(renderer.getMarker(singleCluster))
        val iconBitmap = checkNotNull(clusterMarker.shadow.iconBitmap)
        assertGeo(
            renderer.getCluster(clusterMarker) == singleCluster &&
                iconBitmap.width > 0 &&
                iconBitmap.height > 0,
        )

        val clusterDiff = GoldenImageDiff.compare(emptySnapshot, googleMap.captureSnapshot(), maxDiffPercentage = 0.0)
        assertGeo(clusterDiff.diffPixelCount > 50)

        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(center, 20f))
        val highZoomClusters = clusterManager.algorithm.getClusters(20f)
        renderClustersAndIdle(highZoomClusters)

        googleMap.assertMap(
            clusterManager.clusterMarkerCollection.getMarkers().isEmpty() &&
                clusterManager.markerCollection.getMarkers().size == 6 &&
                googleMap.markers.size == 6,
        )

        val firstItem = items.first()
        val firstMarker = checkNotNull(renderer.getMarker(firstItem))
        assertGeo(renderer.getClusterItem(firstMarker) == firstItem)
        firstMarker.assertMarker(
            (firstMarker.position distanceTo firstItem.position) <= 1.meters &&
                firstMarker.title == "Place #1",
        )
    }

    @Test
    fun clusterAndItemFlows_routeClickAndInfoWindowEventsDeterministically() = runTest {
        val dispatcher = UnconfinedTestDispatcher(testScheduler)
        val center = LatLng(37.7749, -122.4194)
        val items = (1..5).map { i ->
            MutableClusterItem("id_$i", LatLng(center.latitude + i * 0.0001, center.longitude), "Item $i", "S$i")
        }
        clusterManager.addItems(items)

        val clickedClusters = mutableListOf<Cluster<MutableClusterItem>>()
        val clickedClusterInfo = mutableListOf<Cluster<MutableClusterItem>>()
        val longClickedClusterInfo = mutableListOf<Cluster<MutableClusterItem>>()
        val clickedItems = mutableListOf<MutableClusterItem>()
        val clickedItemInfo = mutableListOf<MutableClusterItem>()
        val longClickedItemInfo = mutableListOf<MutableClusterItem>()

        val jobs = listOf(
            launch(dispatcher) { clusterManager.clusterClickEvents().toList(clickedClusters) },
            launch(dispatcher) { clusterManager.clusterInfoWindowClickEvents().toList(clickedClusterInfo) },
            launch(dispatcher) { clusterManager.clusterInfoWindowLongClickEvents().toList(longClickedClusterInfo) },
            launch(dispatcher) { clusterManager.clusterItemClickEvents().toList(clickedItems) },
            launch(dispatcher) { clusterManager.clusterItemInfoWindowClickEvents().toList(clickedItemInfo) },
            launch(dispatcher) { clusterManager.clusterItemInfoWindowLongClickEvents().toList(longClickedItemInfo) },
        )

        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(center, 10f))
        val lowZoomClusters = clusterManager.algorithm.getClusters(10f)
        val cluster = lowZoomClusters.single()
        renderClustersAndIdle(lowZoomClusters)

        val clusterMarker = checkNotNull(renderer.getMarker(cluster))
        clusterMarker.click()
        clusterMarker.clickInfoWindow()
        clusterMarker.longClickInfoWindow()

        assertGeo(
            clickedClusters == listOf(cluster) &&
                clickedClusterInfo == listOf(cluster) &&
                longClickedClusterInfo == listOf(cluster),
        )

        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(center, 20f))
        renderClustersAndIdle(clusterManager.algorithm.getClusters(20f))

        val targetItem = items.first()
        val itemMarker = checkNotNull(renderer.getMarker(targetItem))
        itemMarker.click()
        itemMarker.clickInfoWindow()
        itemMarker.longClickInfoWindow()

        assertGeo(
            clickedItems == listOf(targetItem) &&
                clickedItemInfo == listOf(targetItem) &&
                longClickedItemInfo == listOf(targetItem),
        )
        jobs.forEach { it.cancel() }
    }

    @Test
    fun onClusterItemUpdated_clearsNullSnippetAndAppliesInPlaceZIndexOnCachedMarker() {
        val pos = LatLng(37.7749, -122.4194)
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(pos, 18f))

        val item = MutableClusterItem("target", pos, "Initial Title", "Initial Snippet", 1.0f)
        clusterManager.addItem(item)
        renderClustersAndIdle(clusterManager.algorithm.getClusters(18f))

        val cachedMarker = checkNotNull(renderer.getMarker(item))
        cachedMarker.assertMarker(
            cachedMarker.title == "Initial Title" &&
                cachedMarker.snippet == "Initial Snippet" &&
                cachedMarker.zIndex == 1.0f,
        )

        item.update(newPos = pos, newTitle = "Updated Title", newSnippet = null, newZIndex = 100.0f)
        clusterManager.updateItem(item)
        clusterManager.addItem(MutableClusterItem("trigger", LatLng(37.7760, -122.4194), "Trigger", null, 2.0f))
        renderClustersAndIdle(clusterManager.algorithm.getClusters(18f))

        assertGeo(renderer.getMarker(item) === cachedMarker)
        cachedMarker.assertMarker(
            cachedMarker.title == "Updated Title" &&
                cachedMarker.snippet == null &&
                cachedMarker.zIndex == 100.0f,
        )
    }
}
