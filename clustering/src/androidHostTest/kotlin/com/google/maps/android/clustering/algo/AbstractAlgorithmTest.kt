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

import com.google.android.gms.maps.model.LatLng
import com.google.common.truth.Truth.assertThat
import com.google.maps.android.clustering.Cluster
import com.google.maps.android.clustering.ClusterItem
import org.junit.Test

/**
 * Unit tests for [AbstractAlgorithm].
 */
class AbstractAlgorithmTest {

    private class TestItem : ClusterItem {
        override val position: LatLng = LatLng(0.0, 0.0)
        override val title: String? = null
        override val snippet: String? = null
        override val zIndex: Float? = null
    }

    private class ConcreteAlgorithm : AbstractAlgorithm<TestItem>() {
        override fun addItem(item: TestItem): Boolean = true
        override fun addItems(items: Collection<TestItem>): Boolean = true
        override fun clearItems() {}
        override fun removeItem(item: TestItem): Boolean = true
        override fun removeItems(items: Collection<TestItem>): Boolean = true
        override fun updateItem(item: TestItem): Boolean = true
        override fun getClusters(zoom: Float): Set<Cluster<TestItem>> = emptySet()
        override val items: Collection<TestItem> get() = emptyList()
        override var maxDistanceBetweenClusteredItems: Int = 100
    }

    @Test
    fun testLockOperations() {
        val algorithm = ConcreteAlgorithm()
        var lockExecuted = false
        algorithm.lock()
        try {
            lockExecuted = true
        } finally {
            algorithm.unlock()
        }
        assertThat(lockExecuted).isTrue()
    }
}
