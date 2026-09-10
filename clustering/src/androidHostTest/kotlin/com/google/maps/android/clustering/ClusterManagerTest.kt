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

package com.google.maps.android.clustering

import android.content.Context
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.common.truth.Truth.assertThat
import com.google.maps.android.clustering.algo.GridBasedAlgorithm
import com.google.maps.android.clustering.algo.ScreenBasedAlgorithmAdapter
import com.google.maps.android.clustering.view.ClusterRenderer
import com.google.maps.android.collections.MarkerManager
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment

/**
 * Unit tests for [ClusterManager].
 */
@RunWith(RobolectricTestRunner::class)
class ClusterManagerTest {

    private class TestItem(lat: Double, lng: Double) : ClusterItem {
        override val position: LatLng = LatLng(lat, lng)
        override val title: String? = null
        override val snippet: String? = null
        override val zIndex: Float? = null
    }

    private lateinit var context: Context
    private lateinit var map: GoogleMap
    private lateinit var markerManager: MarkerManager
    private lateinit var clusterManager: ClusterManager<TestItem>

    @Before
    fun setUp() {
        context = RuntimeEnvironment.getApplication()
        map = mockk(relaxed = true)
        markerManager = MarkerManager(map)
        clusterManager = ClusterManager(context, map, markerManager)
    }

    @Test
    fun testItemLifecycle() {
        val item1 = TestItem(10.0, 10.0)
        val item2 = TestItem(20.0, 20.0)

        assertThat(clusterManager.addItem(item1)).isTrue()
        assertThat(clusterManager.addItems(listOf(item2))).isTrue()

        assertThat(clusterManager.updateItem(item1)).isTrue()

        assertThat(clusterManager.removeItem(item1)).isTrue()
        assertThat(clusterManager.removeItems(listOf(item2))).isTrue()

        clusterManager.addItem(item1)
        clusterManager.clearItems()
    }

    @Test
    fun testAlgorithmAndRendererCustomization() {
        val customScreenAlgo = ScreenBasedAlgorithmAdapter(GridBasedAlgorithm<TestItem>())
        clusterManager.setAlgorithm(customScreenAlgo)
        assertThat(clusterManager.algorithm).isEqualTo(customScreenAlgo)

        val customBaseAlgo = GridBasedAlgorithm<TestItem>()
        clusterManager.algorithm = customBaseAlgo
        assertThat(clusterManager.algorithm).isInstanceOf(ScreenBasedAlgorithmAdapter::class.java)

        val customRenderer = mockk<ClusterRenderer<TestItem>>(relaxed = true)
        clusterManager.renderer = customRenderer
        assertThat(clusterManager.renderer).isEqualTo(customRenderer)
        verify { customRenderer.onAdd() }

        clusterManager.setAnimation(true)
    }

    @Test
    fun testDelegatedMapEvents() {
        val mockMarker = mockk<Marker>(relaxed = true)

        clusterManager.onCameraIdle()
        clusterManager.onMarkerClick(mockMarker)
        clusterManager.onInfoWindowClick(mockMarker)
    }

    @Test
    fun testListenerSetters() {
        val clusterClickListener = ClusterManager.OnClusterClickListener<TestItem> { true }
        val itemClickListener = ClusterManager.OnClusterItemClickListener<TestItem> { true }
        val clusterInfoClickListener = ClusterManager.OnClusterInfoWindowClickListener<TestItem> {}
        val itemInfoClickListener = ClusterManager.OnClusterItemInfoWindowClickListener<TestItem> {}

        clusterManager.setOnClusterClickListener(clusterClickListener)
        clusterManager.setOnClusterItemClickListener(itemClickListener)
        clusterManager.setOnClusterInfoWindowClickListener(clusterInfoClickListener)
        clusterManager.setOnClusterItemInfoWindowClickListener(itemInfoClickListener)
    }
}
