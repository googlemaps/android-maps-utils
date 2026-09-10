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
import com.google.maps.android.clustering.ClusterItem
import org.junit.Assert.assertEquals
import org.junit.Test

class CentroidNonHierarchicalDistanceBasedAlgorithmTest {
    class TestClusterItem(lat: Double, lng: Double) : ClusterItem {
        override val position: LatLng = LatLng(lat, lng)

        override val title: String? = null

        override val snippet: String? = null

        override val zIndex: Float = 0f
    }

    // computeCentroid is protected; the previous Java test relied on JVM package-level
    // access, which Kotlin does not have.
    private class ExposedAlgorithm : CentroidNonHierarchicalDistanceBasedAlgorithm<TestClusterItem>() {
        fun centroidOf(items: Collection<TestClusterItem>): LatLng = computeCentroid(items)
    }

    @Test
    fun testComputeCentroid() {
        val algo = ExposedAlgorithm()

        val items =
            listOf(
                TestClusterItem(10.0, 20.0),
                TestClusterItem(20.0, 30.0),
                TestClusterItem(30.0, 40.0),
            )

        val centroid = algo.centroidOf(items)

        assertEquals(20.0, centroid.latitude, 0.0001)
        assertEquals(30.0, centroid.longitude, 0.0001)
    }
}
