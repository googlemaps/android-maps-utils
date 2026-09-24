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
 *
 */

package com.google.maps.android.heatmaps

import com.google.android.gms.maps.model.LatLng
import com.google.common.truth.Truth.assertThat
import com.google.maps.android.heatmaps.WeightedLatLng
import com.google.maps.android.heatmaps.toWeightedLatLng
import org.junit.Test

internal class HeatmapExtensionsTest {
    @Test
    fun `to WeightedLatLng converts correctly`() {
        val latLng = LatLng(1.0, 2.0)

        weightedLatLngEquals(WeightedLatLng(latLng), latLng.toWeightedLatLng())
        weightedLatLngEquals(
            WeightedLatLng(latLng, 2.0),
            latLng.toWeightedLatLng(intensity = 2.0)
        )
    }

    private fun weightedLatLngEquals(lhs: WeightedLatLng, rhs: WeightedLatLng) {
        assertThat(lhs.point.x).isWithin(1e-6).of(rhs.point.x)
        assertThat(lhs.point.y).isWithin(1e-6).of(rhs.point.y)
        assertThat(lhs.intensity).isWithin(1e-6).of(rhs.intensity)
    }
}
