/*
 * Copyright 2025 Google LLC
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

package com.google.maps.android.heatmaps

import com.google.android.gms.maps.model.LatLng
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class WeightedLatLngTest {

    @Test
    fun testConstructorWithIntensity() {
        val latLng = LatLng(10.0, 20.0)
        val weighted = WeightedLatLng(latLng, 5.0)
        assertThat(weighted.latLng).isEqualTo(latLng)
        assertThat(weighted.intensity).isEqualTo(5.0)
        assertThat(weighted.point).isNotNull()
    }

    @Test
    fun testConstructorWithDefaultIntensity() {
        val latLng = LatLng(10.0, 20.0)
        val weighted = WeightedLatLng(latLng)
        assertThat(weighted.latLng).isEqualTo(latLng)
        assertThat(weighted.intensity).isEqualTo(WeightedLatLng.DEFAULT_INTENSITY)
    }

    @Test
    fun testConstructorWithNegativeIntensity() {
        val latLng = LatLng(10.0, 20.0)
        val weighted = WeightedLatLng(latLng, -1.0)
        assertThat(weighted.intensity).isEqualTo(WeightedLatLng.DEFAULT_INTENSITY)
    }

    @Test
    fun testDataClassMethods() {
        val latLng1 = LatLng(1.0, 2.0)
        val weighted1 = WeightedLatLng(latLng1, 3.0)
        val weighted2 = WeightedLatLng(latLng1, 3.0)
        val weighted3 = WeightedLatLng(latLng1, 4.0)

        // Test equals()
        assertThat(weighted1).isEqualTo(weighted2)
        assertThat(weighted1).isNotEqualTo(weighted3)

        // Test hashCode()
        assertThat(weighted1.hashCode()).isEqualTo(weighted2.hashCode())
        assertThat(weighted1.hashCode()).isNotEqualTo(weighted3.hashCode())

        // Test copy()
        val weightedCopy = weighted1.copy(intensity = 5.0)
        assertThat(weightedCopy.latLng).isEqualTo(latLng1)
        assertThat(weightedCopy.intensity).isEqualTo(5.0)
    }
}
