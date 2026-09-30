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

package com.google.maps.android.clustering.view

import android.content.Context
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng
import com.google.common.truth.Truth.assertThat
import com.google.maps.android.clustering.Cluster
import com.google.maps.android.clustering.ClusterItem
import com.google.maps.android.clustering.ClusterManager
import com.google.maps.android.clustering.algo.StaticCluster
import io.mockk.mockk
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment

@RunWith(RobolectricTestRunner::class)
class DefaultClusterRendererTest {

    private data class Item(
        val id: Int = 0,
        override val position: LatLng = LatLng(0.0, 0.0),
        override val title: String? = null,
        override val snippet: String? = null,
        override val zIndex: Float? = null,
    ) : ClusterItem

    private class TestRenderer(
        context: Context,
        map: GoogleMap,
        clusterManager: ClusterManager<Item>,
    ) : DefaultClusterRenderer<Item>(context, map, clusterManager) {
        public override fun getClusterText(bucketOrSize: Int): String = super.getClusterText(bucketOrSize)
        public override fun getBucket(cluster: Cluster<Item>): Int = super.getBucket(cluster)
    }

    private lateinit var renderer: TestRenderer

    @Before
    fun setUp() {
        val context = RuntimeEnvironment.getApplication()
        val googleMap: GoogleMap = mockk(relaxed = true)
        val clusterManager = ClusterManager<Item>(context, googleMap)
        renderer = TestRenderer(context, googleMap, clusterManager)
    }

    @Test
    fun defaultFormatting_usesStandardBucketsAndSuffix() {
        assertThat(renderer.useCompactNumberFormatting).isFalse()

        // Standard bucket text
        assertThat(renderer.getClusterText(5)).isEqualTo("5")
        assertThat(renderer.getClusterText(10)).isEqualTo("10+")
        assertThat(renderer.getClusterText(100)).isEqualTo("100+")
        assertThat(renderer.getClusterText(1000)).isEqualTo("1000+")
    }

    // [START maps_android_utils_compact_notation_test]
    @Test
    fun compactNumberFormatting_formatsThousandsAsKAndMillionsAsM() {
        renderer.useCompactNumberFormatting = true
        renderer.compactUnitUppercase = true
        renderer.maxNonZeroDigits = 2

        assertThat(renderer.getClusterText(5)).isEqualTo("5")
        assertThat(renderer.getClusterText(10)).isEqualTo("10+")
        assertThat(renderer.getClusterText(500)).isEqualTo("500+")

        // Thousands (K)
        assertThat(renderer.getClusterText(1_000)).isEqualTo("1K+")
        assertThat(renderer.getClusterText(2_500)).isEqualTo("2.5K+")
        assertThat(renderer.getClusterText(10_000)).isEqualTo("10K+")
        assertThat(renderer.getClusterText(50_000)).isEqualTo("50K+")
        assertThat(renderer.getClusterText(100_000)).isEqualTo("100K+")

        // Millions (M)
        assertThat(renderer.getClusterText(1_000_000)).isEqualTo("1M+")
        assertThat(renderer.getClusterText(2_500_000)).isEqualTo("2.5M+")
        assertThat(renderer.getClusterText(10_000_000)).isEqualTo("10M+")
    }
    // [END maps_android_utils_compact_notation_test]

    // [START maps_android_utils_nonzero_digits_test]
    @Test
    fun nonZeroDigitsOption_formatsLabelsWithSpecifiedPrecision() {
        renderer.showExactCount = false
        renderer.useCompactNumberFormatting = true
        renderer.compactUnitUppercase = false
        renderer.maxNonZeroDigits = 1

        // 1 non-zero digit examples: 5, 7, 10+, 50+, 100+, 1k+
        assertThat(renderer.getClusterText(5)).isEqualTo("5")
        assertThat(renderer.getClusterText(7)).isEqualTo("7")
        assertThat(renderer.getClusterText(10)).isEqualTo("10+")
        assertThat(renderer.getClusterText(14)).isEqualTo("10+")
        assertThat(renderer.getClusterText(50)).isEqualTo("50+")
        assertThat(renderer.getClusterText(58)).isEqualTo("50+")
        assertThat(renderer.getClusterText(100)).isEqualTo("100+")
        assertThat(renderer.getClusterText(140)).isEqualTo("100+")
        assertThat(renderer.getClusterText(1_000)).isEqualTo("1k+")
        assertThat(renderer.getClusterText(1_450)).isEqualTo("1k+")
        assertThat(renderer.getClusterText(50_000)).isEqualTo("50k+")
        assertThat(renderer.getClusterText(1_000_000)).isEqualTo("1m+")

        // Uppercase 'K' and 'M'
        renderer.compactUnitUppercase = true
        assertThat(renderer.getClusterText(1_000)).isEqualTo("1K+")
        assertThat(renderer.getClusterText(1_000_000)).isEqualTo("1M+")

        // 2 non-zero digits
        renderer.maxNonZeroDigits = 2
        renderer.compactUnitUppercase = false
        assertThat(renderer.getClusterText(1_450)).isEqualTo("1.4k+")
        assertThat(renderer.getClusterText(48_210)).isEqualTo("48k+")
        assertThat(renderer.getClusterText(1_250_000)).isEqualTo("1.2m+")
    }
    // [END maps_android_utils_nonzero_digits_test]

    @Test
    fun exactCountFormatting_displaysExactSizeAndExactCompactKM() {
        renderer.showExactCount = true

        // Without compact SI formatting
        assertThat(renderer.getClusterText(4)).isEqualTo("4")
        assertThat(renderer.getClusterText(45)).isEqualTo("45")
        assertThat(renderer.getClusterText(1_234)).isEqualTo("1,234")
        assertThat(renderer.getClusterText(100_000)).isEqualTo("100,000")

        // With compact SI formatting
        renderer.useCompactNumberFormatting = true
        renderer.compactUnitUppercase = true
        assertThat(renderer.getClusterText(4)).isEqualTo("4")
        assertThat(renderer.getClusterText(45)).isEqualTo("45")
        assertThat(renderer.getClusterText(1_000)).isEqualTo("1K")
        assertThat(renderer.getClusterText(2_500)).isEqualTo("2.5K")
        assertThat(renderer.getClusterText(100_000)).isEqualTo("100K")
        assertThat(renderer.getClusterText(1_000_000)).isEqualTo("1M")
    }

    @Test
    fun customBuckets_partitionsClusterSizesAccurately() {
        renderer.buckets = intArrayOf(10, 50, 250, 1_000, 10_000, 100_000, 1_000_000)

        fun createClusterWithSize(clusterSize: Int): Cluster<Item> = object : Cluster<Item> {
            override val position: LatLng = LatLng(0.0, 0.0)
            override val items: Collection<Item> = emptyList()
            override val size: Int = clusterSize
        }

        assertThat(renderer.getBucket(createClusterWithSize(5))).isEqualTo(5)
        assertThat(renderer.getBucket(createClusterWithSize(25))).isEqualTo(10)
        assertThat(renderer.getBucket(createClusterWithSize(100))).isEqualTo(50)
        assertThat(renderer.getBucket(createClusterWithSize(500))).isEqualTo(250)
        assertThat(renderer.getBucket(createClusterWithSize(5_000))).isEqualTo(1_000)
        assertThat(renderer.getBucket(createClusterWithSize(50_000))).isEqualTo(10_000)
        assertThat(renderer.getBucket(createClusterWithSize(500_000))).isEqualTo(100_000)
        assertThat(renderer.getBucket(createClusterWithSize(5_000_000))).isEqualTo(1_000_000)
    }
}
