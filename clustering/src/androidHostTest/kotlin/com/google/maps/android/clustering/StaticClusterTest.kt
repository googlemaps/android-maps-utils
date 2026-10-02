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

import com.google.android.gms.maps.model.LatLng
import com.google.common.truth.Truth.assertThat
import com.google.maps.android.clustering.algo.StaticCluster
import org.junit.Test

/**
 * Unit tests for [StaticCluster].
 */
class StaticClusterTest {

    private class TestItem(lat: Double, lng: Double) : ClusterItem {
        override val position: LatLng = LatLng(lat, lng)
        override val title: String? = null
        override val snippet: String? = null
        override val zIndex: Float? = null
    }

    @Test
    fun testEquality() {
        val cluster1 = StaticCluster<ClusterItem>(LatLng(0.1, 0.5))
        val cluster2 = StaticCluster<ClusterItem>(LatLng(0.1, 0.5))

        assertThat(cluster1).isEqualTo(cluster2)
        assertThat(cluster1).isNotSameInstanceAs(cluster2)
        assertThat(cluster1.hashCode()).isEqualTo(cluster2.hashCode())
    }

    @Test
    fun testUnequality() {
        val cluster1 = StaticCluster<ClusterItem>(LatLng(0.1, 0.5))
        val cluster2 = StaticCluster<ClusterItem>(LatLng(0.2, 0.3))

        assertThat(cluster1).isNotEqualTo(cluster2)
        assertThat(cluster1.hashCode()).isNotEqualTo(cluster2.hashCode())
        assertThat(cluster1).isNotEqualTo(null)
        assertThat(cluster1).isNotEqualTo("not a cluster")
    }

    @Test
    fun testItemOperationsAndProperties() {
        val center = LatLng(10.0, 20.0)
        val cluster = StaticCluster<ClusterItem>(center)

        assertThat(cluster.position).isEqualTo(center)
        assertThat(cluster.size).isEqualTo(0)
        assertThat(cluster.items).isEmpty()

        val item = TestItem(10.0, 20.0)
        cluster.add(item)
        assertThat(cluster.size).isEqualTo(1)
        assertThat(cluster.items).containsExactly(item)
        assertThat(cluster.toString()).contains("StaticCluster")

        cluster.remove(item)
        assertThat(cluster.size).isEqualTo(0)
        assertThat(cluster.items).isEmpty()
    }
}
