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

package com.google.maps.android.projection

import com.google.android.gms.maps.model.LatLng
import com.google.common.truth.Truth.assertThat
import org.junit.Test

/**
 * Unit tests for [SphericalMercatorProjection].
 */
class SphericalMercatorProjectionTest {

    @Test
    fun testRoundTripProjection() {
        val projection = SphericalMercatorProjection(256.0)
        val original = LatLng(37.7749, -122.4194)

        val point = projection.toPoint(original)
        val reconstructed = projection.toLatLng(point)

        assertThat(reconstructed.latitude).isWithin(1e-6).of(original.latitude)
        assertThat(reconstructed.longitude).isWithin(1e-6).of(original.longitude)
    }

    @Test
    fun testOriginProjection() {
        val projection = SphericalMercatorProjection(1.0)
        val center = LatLng(0.0, 0.0)

        val point = projection.toPoint(center)
        assertThat(point.x).isWithin(1e-6).of(0.5)
        assertThat(point.y).isWithin(1e-6).of(0.5)

        val reconstructed = projection.toLatLng(point)
        assertThat(reconstructed.latitude).isWithin(1e-6).of(0.0)
        assertThat(reconstructed.longitude).isWithin(1e-6).of(0.0)
    }
}
