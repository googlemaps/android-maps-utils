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

package com.google.maps.android.clustering.algo

import android.content.Context
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.common.truth.Truth.assertThat
import com.google.maps.android.clustering.ClusterItem
import com.google.maps.android.clustering.ClusterManager
import com.google.maps.android.clustering.view.DefaultClusterRenderer
import com.google.maps.android.collections.MarkerManager
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment

/**
 * End-to-end integration test verifying [SuperClusterAlgorithm] operating inside a stateful
 * [ClusterManager] with [DefaultClusterRenderer].
 */
@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
class SuperClusterRobolectricTest {

    data class TestItem(
        val id: String,
        override val position: LatLng,
        override val title: String? = null,
        override val snippet: String? = null,
        override val zIndex: Float? = 0f,
    ) : ClusterItem

    private lateinit var context: Context
    private lateinit var googleMap: GoogleMap
    private lateinit var markerManager: MarkerManager
    private lateinit var clusterManager: ClusterManager<TestItem>
    private lateinit var superClusterAlgorithm: SuperClusterAlgorithm<TestItem>
    private lateinit var renderer: DefaultClusterRenderer<TestItem>

    @Before
    fun setUp() {
        context = RuntimeEnvironment.getApplication()
        googleMap = mockk(relaxed = true)
        markerManager = MarkerManager(googleMap)

        clusterManager = ClusterManager(context, googleMap, markerManager)
        superClusterAlgorithm = SuperClusterAlgorithm(
            viewWidth = 800,
            viewHeight = 800,
        )
        clusterManager.setAlgorithm(superClusterAlgorithm)

        renderer = DefaultClusterRenderer(context, googleMap, clusterManager).apply {
            setAnimation(false)
        }
        clusterManager.renderer = renderer
    }

    // [START maps_android_utils_supercluster_robolectric_test]
    @Test
    fun superClusterAlgorithm_integratesWithClusterManagerAndRendersClusters() = runTest {
        // Add 10 items clustered in SF and 10 items clustered in NY
        val sfBase = LatLng(37.7749, -122.4194)
        val nyBase = LatLng(40.7128, -74.0060)
        val items = ArrayList<TestItem>()

        for (i in 0 until 10) {
            items.add(TestItem("sf_$i", LatLng(sfBase.latitude + i * 0.0001, sfBase.longitude + i * 0.0001), "SF $i"))
            items.add(TestItem("ny_$i", LatLng(nyBase.latitude + i * 0.0001, nyBase.longitude + i * 0.0001), "NY $i"))
        }

        clusterManager.addItems(items)

        // Set camera at world view (zoom 3)
        every { googleMap.cameraPosition } returns CameraPosition.fromLatLngZoom(LatLng(39.0, -98.0), 3f)
        clusterManager.onCameraIdle()

        // At zoom 3, SF items should cluster together, and NY items should cluster together
        val clustersAtZoom3 = superClusterAlgorithm.getClusters(3f)
        assertThat(clustersAtZoom3.size).isAtMost(2)
        var totalPointsAtZoom3 = 0
        for (c in clustersAtZoom3) {
            totalPointsAtZoom3 += c.size
        }
        assertThat(totalPointsAtZoom3).isEqualTo(20)

        // Now zoom in to San Francisco street level (zoom 18)
        every { googleMap.cameraPosition } returns CameraPosition.fromLatLngZoom(sfBase, 18f)
        clusterManager.onCameraIdle()

        val clustersAtZoom18 = superClusterAlgorithm.getClusters(18f)
        // In SF at zoom 18, individual markers should be visible
        assertThat(clustersAtZoom18).isNotEmpty()
        for (c in clustersAtZoom18) {
            assertThat(c.size).isEqualTo(1)
        }
    }
    // [END maps_android_utils_supercluster_robolectric_test]

    @Test
    fun superClusterAlgorithm_clearItemsRemovesAllMarkers() = runTest {
        val item1 = TestItem("1", LatLng(37.7749, -122.4194))
        val item2 = TestItem("2", LatLng(37.7750, -122.4195))
        clusterManager.addItems(listOf(item1, item2))
        clusterManager.cluster()

        assertThat(superClusterAlgorithm.items).hasSize(2)

        clusterManager.clearItems()
        clusterManager.cluster()

        assertThat(superClusterAlgorithm.items).isEmpty()
        assertThat(superClusterAlgorithm.getClusters(10f)).isEmpty()
    }
}
