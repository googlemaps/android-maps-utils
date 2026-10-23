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
 * Unit tests for [ScreenBasedAlgorithmAdapter].
 *
 * Proves that [ScreenBasedAlgorithmAdapter] adapts standard [Algorithm] instances to the
 * [ScreenBasedAlgorithm] contract by delegating item lifecycle, clustering, and distance settings.
 */
class ScreenBasedAlgorithmAdapterTest {

    private class TestItem(lat: Double, lng: Double) : ClusterItem {
        override val position: LatLng = LatLng(lat, lng)
        override val title: String? = null
        override val snippet: String? = null
        override val zIndex: Float? = null
    }

    private lateinit var baseAlgorithm: NonHierarchicalDistanceBasedAlgorithm<TestItem>
    private lateinit var adapter: ScreenBasedAlgorithmAdapter<TestItem>

    @Before
    fun setUp() {
        baseAlgorithm = NonHierarchicalDistanceBasedAlgorithm()
        adapter = ScreenBasedAlgorithmAdapter(baseAlgorithm)
    }

    @Test
    fun testDelegationLifecycle() {
        val item1 = TestItem(10.0, 10.0)
        val item2 = TestItem(20.0, 20.0)

        // Add
        assertThat(adapter.addItem(item1)).isTrue()
        assertThat(adapter.addItems(listOf(item2))).isTrue()
        assertThat(adapter.items).hasSize(2)

        // Update
        assertThat(adapter.updateItem(item1)).isTrue()

        // Get clusters
        val clusters = adapter.getClusters(10f)
        assertThat(clusters).hasSize(2)

        // Max distance
        adapter.maxDistanceBetweenClusteredItems = 50
        assertThat(adapter.maxDistanceBetweenClusteredItems).isEqualTo(50)

        // Remove
        assertThat(adapter.removeItem(item1)).isTrue()
        assertThat(adapter.items).hasSize(1)
        assertThat(adapter.removeItems(listOf(item2))).isTrue()
        assertThat(adapter.items).isEmpty()

        // Clear
        adapter.addItem(item1)
        adapter.clearItems()
        assertThat(adapter.items).isEmpty()

        // Camera change stub
        adapter.onCameraChange(CameraPosition.builder().target(LatLng(0.0, 0.0)).zoom(5f).build())
        assertThat(adapter.shouldReclusterOnMapMovement()).isFalse()
    }
}
