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

import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.common.truth.Truth.assertThat
import com.google.maps.android.clustering.ClusterItem
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for [NonHierarchicalViewBasedAlgorithm].
 *
 * Verifies that the view-based clustering algorithm properly constrains clustering to visible screen bounds,
 * updates when the camera changes, handles world-wrapping across the antimeridian, and dynamically adjusts
 * to screen dimension updates.
 */
class NonHierarchicalViewBasedAlgorithmTest {

    private class TestItem(lat: Double, lng: Double) : ClusterItem {
        override val position: LatLng = LatLng(lat, lng)
        override val title: String? = null
        override val snippet: String? = null
        override val zIndex: Float? = null
    }

    private lateinit var algorithm: NonHierarchicalViewBasedAlgorithm<TestItem>

    @Before
    fun setUp() {
        // Initial viewport: 1000dp width x 1000dp height
        algorithm = NonHierarchicalViewBasedAlgorithm(1000, 1000)
    }

    @Test
    fun testShouldReclusterOnMapMovement() {
        assertThat(algorithm.shouldReclusterOnMapMovement()).isTrue()
    }

    @Test
    fun testEmptyBoundsWithoutCameraChange() {
        val item = TestItem(0.0, 0.0)
        algorithm.addItem(item)

        // With no camera position established, visible bounds default to (0,0,0,0)
        val clusters = algorithm.getClusters(10f)
        assertThat(clusters).isEmpty()
    }

    @Test
    fun testClusteringWithinVisibleBounds() {
        val visibleItem = TestItem(0.0, 0.0)
        val farItem = TestItem(80.0, 170.0)

        algorithm.addItem(visibleItem)
        algorithm.addItem(farItem)

        // Set camera directly centered on visibleItem at high zoom
        algorithm.onCameraChange(CameraPosition.builder().target(LatLng(0.0, 0.0)).zoom(10f).build())

        val clusters = algorithm.getClusters(10f)
        assertThat(clusters).hasSize(1)
        val cluster = clusters.first()
        assertThat(cluster.items).contains(visibleItem)
        assertThat(cluster.items).doesNotContain(farItem)
    }

    @Test
    fun testAntimeridianWrappingBoundsWest() {
        val itemNearAntimeridian = TestItem(0.0, -179.0)
        algorithm.addItem(itemNearAntimeridian)

        // Center on -179.9 with huge viewport to force visibleBounds.minX < 0
        algorithm.updateViewSize(2000, 1000)
        algorithm.onCameraChange(CameraPosition.builder().target(LatLng(0.0, -179.9)).zoom(1f).build())

        val clusters = algorithm.getClusters(1f)
        assertThat(clusters).isNotEmpty()
    }

    @Test
    fun testAntimeridianWrappingBoundsEast() {
        val itemNearAntimeridian = TestItem(0.0, 179.0)
        algorithm.addItem(itemNearAntimeridian)

        // Center on +179.9 with huge viewport to force visibleBounds.maxX > 1
        algorithm.updateViewSize(2000, 1000)
        algorithm.onCameraChange(CameraPosition.builder().target(LatLng(0.0, 179.9)).zoom(1f).build())

        val clusters = algorithm.getClusters(1f)
        assertThat(clusters).isNotEmpty()
    }

    @Test
    fun testUpdateViewSize() {
        algorithm.updateViewSize(500, 500)
        algorithm.onCameraChange(CameraPosition.builder().target(LatLng(0.0, 0.0)).zoom(10f).build())

        val item = TestItem(0.0, 0.0)
        algorithm.addItem(item)

        val clusters = algorithm.getClusters(10f)
        assertThat(clusters).hasSize(1)
    }
}
