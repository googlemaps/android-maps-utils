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
import com.google.android.maps.robolectric.shadows.ShadowMarker
import com.google.android.maps.robolectric.truth.LatLngSubject
import com.google.android.maps.robolectric.truth.isWithin
import com.google.android.maps.robolectric.truth.meters
import com.google.android.maps.testing.golden.GoldenImageDiff
import com.google.android.maps.testing.golden.GoldenTileStrategy
import com.google.android.maps.testing.golden.captureSnapshot
import com.google.android.maps.testing.golden.enableGoldenTesting
import com.google.common.truth.Truth.assertThat
import com.google.maps.android.clustering.Cluster
import com.google.maps.android.clustering.ClusterItem
import com.google.maps.android.clustering.ClusterManager
import com.google.maps.android.clustering.algo.CentroidNonHierarchicalDistanceBasedAlgorithm
import com.google.maps.android.clustering.algo.GridBasedAlgorithm
import com.google.maps.android.clustering.algo.NonHierarchicalDistanceBasedAlgorithm
import com.google.maps.android.clustering.algo.NonHierarchicalViewBasedAlgorithm
import com.google.maps.android.clustering.algo.PreCachingAlgorithmDecorator
import com.google.maps.android.clustering.clusterClickEvents
import com.google.maps.android.clustering.clusterInfoWindowClickEvents
import com.google.maps.android.clustering.clusterInfoWindowLongClickEvents
import com.google.maps.android.clustering.clusterItemClickEvents
import com.google.maps.android.clustering.clusterItemInfoWindowClickEvents
import com.google.maps.android.clustering.clusterItemInfoWindowLongClickEvents
import com.google.maps.android.clustering.view.DefaultClusterRenderer
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
 * End-to-end JVM integration and adversarial verification suite for the `:clustering` module
 * powered by the Android Maps Testing Toolkit (`v1.1.0-rc01`).
 *
 * ## Architectural Scope
 * Prior to this suite, `:clustering` had **zero** unit tests exercising [DefaultClusterRenderer]
 * against a stateful [GoogleMap] or [MapView]. Here we combine:
 * 1. **Stateful Map & Projection Shadows**: [ShadowGoogleMap], [ShadowMarker], and a measured
 *    `400x400` [MapView] with accurate Web Mercator [com.google.android.maps.robolectric.shadows.ShadowProjection].
 * 2. **Algorithm Matrix Verification**: Exercises [NonHierarchicalDistanceBasedAlgorithm],
 *    [CentroidNonHierarchicalDistanceBasedAlgorithm], [NonHierarchicalViewBasedAlgorithm],
 *    [GridBasedAlgorithm], and [PreCachingAlgorithmDecorator] across zoom transitions and
 *    [ClusterManager.diff] mutations.
 * 3. **Cluster & Item Reactive Flows + Golden Snapshot Verification**: Verifies that
 *    [DefaultClusterRenderer] generates real cluster bucket bitmaps via [com.google.maps.android.ui.IconGenerator],
 *    attaches them to `clusterMarkerCollection` or `markerCollection` depending on `minClusterSize`
 *    and zoom level, routes all 6 KTX `ClusterManagerFlows` (`clusterClickEvents`,
 *    `clusterItemClickEvents`, `clusterInfoWindowClickEvents`, `clusterInfoWindowLongClickEvents`,
 *    `clusterItemInfoWindowClickEvents`, `clusterItemInfoWindowLongClickEvents`), and produces
 *    verifiable pixel changes via [GoldenImageDiff].
 * 4. **Adversarial Defect Proof (Bug #8)**: Exposes two state-synchronization defects in
 *    `DefaultClusterRenderer.onClusterItemUpdated`:
 *    - Stale `snippet` (and `title`) retention when a [ClusterItem]'s `snippet` or `title` is
 *      updated to `null`.
 *    - Ignored `zIndex` updates when a [ClusterItem]'s `zIndex` changes while its `position`
 *      remains unchanged.
 */
@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [34])
@EnableMapsShadows
class MapsToolkitClusteringTest {

    /**
     * Mutable [ClusterItem] with stable `id`-based identity so [DefaultClusterRenderer]'s
     * internal `MarkerCache` recognizes updated items across re-clustering passes.
     */
    class MutableTestItem(
        val id: String,
        override var position: LatLng,
        override var title: String?,
        override var snippet: String?,
        override var zIndex: Float? = 0f
    ) : ClusterItem {
        fun update(
            newPos: LatLng = this.position,
            newTitle: String? = this.title,
            newSnippet: String? = this.snippet,
            newZIndex: Float? = this.zIndex
        ) {
            this.position = newPos
            this.title = newTitle
            this.snippet = newSnippet
            this.zIndex = newZIndex
        }

        override fun equals(other: Any?): Boolean =
            other is MutableTestItem && other.id == this.id

        override fun hashCode(): Int = id.hashCode()
    }

    private lateinit var activity: Activity
    private lateinit var mapView: MapView
    private lateinit var googleMap: GoogleMap
    private lateinit var shadowMap: ShadowGoogleMap
    private lateinit var clusterManager: ClusterManager<MutableTestItem>
    private lateinit var renderer: DefaultClusterRenderer<MutableTestItem>

    @Before
    fun setUp() {
        activity = Robolectric.buildActivity(Activity::class.java).create().get()
        mapView = MapView(activity).apply {
            onCreate(Bundle())
            onStart()
            onResume()
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

        clusterManager = ClusterManager(activity, googleMap)
        renderer = DefaultClusterRenderer(activity, googleMap, clusterManager).apply {
            setAnimation(false)
        }
        clusterManager.renderer = renderer

        // Wire ClusterManager as the GoogleMap camera-idle, marker-click, and info-window listener
        // and drain the main looper so MarkerManager registers its underlying map listeners.
        ShadowLooper.idleMainLooper()
        googleMap.setOnCameraIdleListener(clusterManager)
        googleMap.setOnMarkerClickListener(clusterManager)
        googleMap.setOnInfoWindowClickListener(clusterManager)
        googleMap.setOnInfoWindowLongClickListener(clusterManager.markerManager)
    }

    /**
     * Drives [DefaultClusterRenderer.onClustersChanged] and pumps the main looper so the background
     * `RenderTask` thread and the main-thread `MarkerModifier` handler complete their lock/condition
     * handshake deterministically.
     */
    private fun renderClustersAndIdle(clusters: Set<Cluster<MutableTestItem>>) {
        renderer.onClustersChanged(clusters)
        repeat(30) {
            ShadowLooper.idleMainLooper()
            Thread.sleep(5)
        }
        ShadowLooper.idleMainLooper()
    }

    // ============================================================================================
    // Section 1: Zoom-Dependent Clustering, Rendering, Flows & Golden Visual Diffs
    // ============================================================================================

    /**
     * Verifies that 6 closely spaced items render as a single aggregated cluster marker with a
     * generated [com.google.maps.android.ui.IconGenerator] bitmap at low zoom (`zoom = 10f`), and
     * split into 6 individual markers when zoomed in (`zoom = 19f`), while firing all 6 KTX
     * `ClusterManagerFlows` and producing distinct golden snapshots.
     */
    @Test
    fun clusterRendererAndFlows_transitionFromSingleClusterToIndividualMarkersAcrossZooms() = runTest {
        val dispatcher = UnconfinedTestDispatcher(testScheduler)
        val center = LatLng(37.7749, -122.4194)

        // Enable synthetic golden tile background
        googleMap.enableGoldenTesting(
            strategy = GoldenTileStrategy.SolidColorHash(),
            fadeIn = false
        )
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(center, 10f))

        val baselineJob = async(dispatcher) { googleMap.captureSnapshot() }
        ShadowLooper.idleMainLooper()
        val emptyMapSnapshot = baselineJob.await()

        // Add 6 closely spaced items around San Francisco (~20-50m apart)
        val items = (1..6).map { index ->
            MutableTestItem(
                id = "item_$index",
                position = LatLng(center.latitude + index * 0.0001, center.longitude + index * 0.0001),
                title = "Place #$index",
                snippet = "Snippet #$index",
                zIndex = index.toFloat()
            )
        }
        clusterManager.addItems(items)

        // At zoom 10f, all 6 items fall into 1 cluster (>= minClusterSize = 4)
        val lowZoomClusters = clusterManager.algorithm.getClusters(10.0f)
        assertThat(lowZoomClusters).hasSize(1)
        val singleCluster = lowZoomClusters.first()
        assertThat(singleCluster.size).isEqualTo(6)

        renderClustersAndIdle(lowZoomClusters)

        assertThat(clusterManager.clusterMarkerCollection.getMarkers()).hasSize(1)
        assertThat(clusterManager.markerCollection.getMarkers()).isEmpty()

        val clusterMarker = checkNotNull(renderer.getMarker(singleCluster))
        assertThat(renderer.getCluster(clusterMarker)).isEqualTo(singleCluster)
        val shadowClusterMarker = Shadow.extract(clusterMarker) as ShadowMarker
        assertThat(shadowClusterMarker.iconBitmap).isNotNull()

        // Capture snapshot with rendered cluster icon and verify GoldenImageDiff detects the icon
        val clusterSnapJob = async(dispatcher) { googleMap.captureSnapshot() }
        ShadowLooper.idleMainLooper()
        val clusterSnapshot = clusterSnapJob.await()
        val clusterDiff = GoldenImageDiff.compare(
            expected = emptyMapSnapshot,
            actual = clusterSnapshot,
            maxDiffPercentage = 0.0
        )
        assertThat(clusterDiff.diffPixelCount).isGreaterThan(50)

        // Subscribe to all 6 ClusterManagerFlows
        val clickedClusters = mutableListOf<Cluster<MutableTestItem>>()
        val clickedClusterInfoWindows = mutableListOf<Cluster<MutableTestItem>>()
        val longClickedClusterInfoWindows = mutableListOf<Cluster<MutableTestItem>>()
        val clickedItems = mutableListOf<MutableTestItem>()
        val clickedItemInfoWindows = mutableListOf<MutableTestItem>()
        val longClickedItemInfoWindows = mutableListOf<MutableTestItem>()

        val flowJobs = listOf(
            launch(dispatcher) { clusterManager.clusterClickEvents().toList(clickedClusters) },
            launch(dispatcher) { clusterManager.clusterInfoWindowClickEvents().toList(clickedClusterInfoWindows) },
            launch(dispatcher) { clusterManager.clusterInfoWindowLongClickEvents().toList(longClickedClusterInfoWindows) },
            launch(dispatcher) { clusterManager.clusterItemClickEvents().toList(clickedItems) },
            launch(dispatcher) { clusterManager.clusterItemInfoWindowClickEvents().toList(clickedItemInfoWindows) },
            launch(dispatcher) { clusterManager.clusterItemInfoWindowLongClickEvents().toList(longClickedItemInfoWindows) }
        )

        // Dispatch click, infoWindowClick, and infoWindowLongClick on the cluster marker
        shadowMap.clickMarker(clusterMarker)
        shadowMap.clickInfoWindow(clusterMarker)
        shadowMap.longClickInfoWindow(clusterMarker)

        assertThat(clickedClusters).containsExactly(singleCluster)
        assertThat(clickedClusterInfoWindows).containsExactly(singleCluster)
        assertThat(longClickedClusterInfoWindows).containsExactly(singleCluster)

        // Now zoom in to 20f where the 6 items separate into individual 1-item clusters (< minClusterSize)
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(center, 20f))
        val highZoomClusters = clusterManager.algorithm.getClusters(20.0f)
        assertThat(highZoomClusters).hasSize(6)

        renderClustersAndIdle(highZoomClusters)

        assertThat(clusterManager.clusterMarkerCollection.getMarkers()).isEmpty()
        assertThat(clusterManager.markerCollection.getMarkers()).hasSize(6)

        val firstItem = items.first()
        val firstItemMarker = checkNotNull(renderer.getMarker(firstItem))
        assertThat(renderer.getClusterItem(firstItemMarker)).isEqualTo(firstItem)
        LatLngSubject.assertThat(firstItemMarker.position).isWithin(1.meters).of(firstItem.position)

        // Dispatch click, infoWindowClick, and infoWindowLongClick on the individual item marker
        shadowMap.clickMarker(firstItemMarker)
        shadowMap.clickInfoWindow(firstItemMarker)
        shadowMap.longClickInfoWindow(firstItemMarker)

        assertThat(clickedItems).containsExactly(firstItem)
        assertThat(clickedItemInfoWindows).containsExactly(firstItem)
        assertThat(longClickedItemInfoWindows).containsExactly(firstItem)

        flowJobs.forEach { it.cancel() }
    }

    // ============================================================================================
    // Section 2: Algorithm Family & ClusterManager.diff() Verification
    // ============================================================================================

    /**
     * Exercises [CentroidNonHierarchicalDistanceBasedAlgorithm], [GridBasedAlgorithm],
     * [NonHierarchicalViewBasedAlgorithm], and [PreCachingAlgorithmDecorator], plus
     * [ClusterManager.diff] for atomic batch additions, removals, and modifications.
     */
    @Test
    fun clusteringAlgorithmsAndDiff_computeExpectedCentroidsAndViewportPartitions() {
        val base = LatLng(40.7128, -74.0060)
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(base, 12f))

        val item1 = MutableTestItem("1", base, "NYC 1", "S1")
        val item2 = MutableTestItem("2", LatLng(40.7130, -74.0062), "NYC 2", "S2")
        val item3 = MutableTestItem("3", LatLng(40.7132, -74.0064), "NYC 3", "S3")
        val distantItem = MutableTestItem("4", LatLng(40.8500, -73.9000), "Bronx", "S4")

        // 1. CentroidNonHierarchicalDistanceBasedAlgorithm computes true geographic mean center
        val centroidAlgo = CentroidNonHierarchicalDistanceBasedAlgorithm<MutableTestItem>()
        clusterManager.algorithm = centroidAlgo
        clusterManager.addItems(listOf(item1, item2, item3, distantItem))

        val centroidClusters = clusterManager.algorithm.getClusters(12f)
        assertThat(centroidClusters).hasSize(2)
        val nycCluster = centroidClusters.first { it.size == 3 }
        val expectedLat = (item1.position.latitude + item2.position.latitude + item3.position.latitude) / 3.0
        val expectedLng = (item1.position.longitude + item2.position.longitude + item3.position.longitude) / 3.0
        LatLngSubject.assertThat(nycCluster.position)
            .isWithin(1.meters)
            .of(LatLng(expectedLat, expectedLng))

        // 2. ClusterManager.diff() atomically adds, removes, and modifies items
        val addedItem = MutableTestItem("5", LatLng(40.7129, -74.0061), "NYC 5", "S5")
        item1.update(newTitle = "NYC 1 Updated")
        clusterManager.diff(
            add = listOf(addedItem),
            remove = listOf(distantItem),
            modify = listOf(item1)
        )
        val afterDiffClusters = clusterManager.algorithm.getClusters(12f)
        assertThat(afterDiffClusters).hasSize(1)
        assertThat(afterDiffClusters.first().size).isEqualTo(4)

        // 3. GridBasedAlgorithm
        val gridAlgo = GridBasedAlgorithm<MutableTestItem>()
        clusterManager.algorithm = gridAlgo
        assertThat(clusterManager.algorithm.getClusters(12f)).hasSize(1)

        // 4. NonHierarchicalViewBasedAlgorithm uses MapView dimensions & CameraPosition
        val viewBasedAlgo = NonHierarchicalViewBasedAlgorithm<MutableTestItem>(
            mapView.width,
            mapView.height
        )
        clusterManager.setAlgorithm(viewBasedAlgo)
        viewBasedAlgo.onCameraChange(googleMap.cameraPosition)
        assertThat(viewBasedAlgo.shouldReclusterOnMapMovement()).isTrue()
        assertThat(clusterManager.algorithm.getClusters(12f)).isNotEmpty()

        // 5. PreCachingAlgorithmDecorator
        val preCaching = PreCachingAlgorithmDecorator(NonHierarchicalDistanceBasedAlgorithm<MutableTestItem>())
        clusterManager.algorithm = preCaching
        assertThat(clusterManager.algorithm.getClusters(12f)).hasSize(1)
    }

    // ============================================================================================
    // Section 3: Adversarial Defect Reproduction (Bug #8 in DefaultClusterRenderer)
    // ============================================================================================

    /**
     * **BUG #8 REPRODUCTION (`DefaultClusterRenderer.onClusterItemUpdated` Stale Title/Snippet &
     * Ignored `zIndex` Updates)**:
     *
     * Examination of `DefaultClusterRenderer.kt:833-866` reveals two state-synchronization bugs
     * when a cached [ClusterItem] marker is updated on the map:
     *
     * 1. **Stale `snippet` (and `title`) when updated to `null`**:
     *    ```kotlin
     *    if (item.title != null && item.snippet != null) {
     *        ...
     *    } else if (item.snippet != null && item.snippet != marker.title) {
     *        marker.title = item.snippet
     *        changed = true
     *    } else if (item.title != null && item.title != marker.title) {
     *        marker.title = item.title
     *        changed = true
     *    }
     *    ```
     *    When an item initially has both `title = "Initial Title"` and `snippet = "Initial Snippet"`,
     *    and is later updated to `title = "Updated Title", snippet = null`:
     *    - It enters `else if (item.title != null && item.title != marker.title)` which updates
     *      `marker.title`, **but never clears `marker.snippet`**, leaving `"Initial Snippet"`
     *      permanently stuck on the [com.google.android.gms.maps.model.Marker]!
     *    - Furthermore, if both `title` and `snippet` are updated to `null`, **neither** `marker.title`
     *      nor `marker.snippet` is cleared!
     *
     * 2. **Ignored `zIndex` update when `position` does not change**:
     *    ```kotlin
     *    if (marker.position != item.position) {
     *        marker.position = item.position
     *        if (item.zIndex != null) {
     *            marker.zIndex = item.zIndex!!
     *        }
     *        changed = true
     *    }
     *    ```
     *    Because `marker.zIndex = item.zIndex!!` is nested inside `if (marker.position != item.position)`,
     *    changing an item's `zIndex` in-place (e.g., bringing a selected marker to the front by
     *    updating `zIndex` from `1f` to `100f` without moving its coordinates) is **completely ignored**!
     */
    @Test
    fun bug8_defaultClusterRenderer_onClusterItemUpdatedRetainsStaleSnippetAndIgnoresInPlaceZIndexChange() {
        val pos = LatLng(37.7749, -122.4194)
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(pos, 18f))

        val item = MutableTestItem(
            id = "target_item",
            position = pos,
            title = "Initial Title",
            snippet = "Initial Snippet",
            zIndex = 1.0f
        )
        clusterManager.addItem(item)

        // First render pass creates and caches the marker via onBeforeClusterItemRendered
        renderClustersAndIdle(clusterManager.algorithm.getClusters(18f))

        val cachedMarker = checkNotNull(renderer.getMarker(item))
        assertThat(cachedMarker.title).isEqualTo("Initial Title")
        assertThat(cachedMarker.snippet).isEqualTo("Initial Snippet")
        assertThat(cachedMarker.zIndex).isWithin(0.001f).of(1.0f)

        // Mutate item in-place:
        // - Change title to "Updated Title"
        // - Clear snippet to null
        // - Elevate zIndex from 1.0f to 100.0f (while keeping position unchanged)
        item.update(
            newPos = pos,
            newTitle = "Updated Title",
            newSnippet = null,
            newZIndex = 100.0f
        )
        clusterManager.updateItem(item)

        // Add a second item so `newClusters != oldClusters` and `shouldRender` runs `CreateMarkerTask`
        // on the cached marker, invoking `onClusterItemUpdated(item, cachedMarker)`.
        val triggerItem = MutableTestItem(
            id = "trigger_item",
            position = LatLng(37.7760, -122.4194),
            title = "Trigger",
            snippet = null,
            zIndex = 2.0f
        )
        clusterManager.addItem(triggerItem)
        renderClustersAndIdle(clusterManager.algorithm.getClusters(18f))

        // Verify that the same cached Marker instance was updated via onClusterItemUpdated:
        assertThat(renderer.getMarker(item)).isSameInstanceAs(cachedMarker)
        assertThat(cachedMarker.title).isEqualTo("Updated Title")

        // BUG #8A VERIFIED FIXED: item.snippet is null, and cachedMarker.snippet is properly cleared to null!
        assertThat(item.snippet).isNull()
        assertThat(cachedMarker.snippet).isNull()

        // BUG #8B VERIFIED FIXED: item.zIndex is updated to 100.0f in-place even when position is unchanged!
        assertThat(item.zIndex).isEqualTo(100.0f)
        assertThat(cachedMarker.zIndex).isWithin(0.001f).of(100.0f)
    }
}
