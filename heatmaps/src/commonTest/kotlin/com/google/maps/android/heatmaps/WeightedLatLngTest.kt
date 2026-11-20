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
package com.google.maps.android.heatmaps

import com.google.maps.android.model.LatLng
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull

class WeightedLatLngTest {
    @Test
    fun testConstructorWithIntensity() {
        val latLng = LatLng(10.0, 20.0)
        val weighted = WeightedLatLng(latLng, 5.0)
        assertEquals(latLng, weighted.latLng)
        assertEquals(5.0, weighted.intensity)
        assertNotNull(weighted.point)
    }

    @Test
    fun testConstructorWithDefaultIntensity() {
        val latLng = LatLng(10.0, 20.0)
        val weighted = WeightedLatLng(latLng)
        assertEquals(latLng, weighted.latLng)
        assertEquals(WeightedLatLng.DEFAULT_INTENSITY, weighted.intensity)
    }

    @Test
    fun testConstructorWithNegativeIntensity() {
        val latLng = LatLng(10.0, 20.0)
        val weighted = WeightedLatLng(latLng, -1.0)
        assertEquals(WeightedLatLng.DEFAULT_INTENSITY, weighted.intensity)
    }

    @Test
    fun testDataClassMethods() {
        val latLng1 = LatLng(1.0, 2.0)
        val weighted1 = WeightedLatLng(latLng1, 3.0)
        val weighted2 = WeightedLatLng(latLng1, 3.0)
        val weighted3 = WeightedLatLng(latLng1, 4.0)

        // Test equals()
        assertEquals(weighted2, weighted1)
        assertNotEquals(weighted3, weighted1)

        // Test hashCode()
        assertEquals(weighted2.hashCode(), weighted1.hashCode())
        assertNotEquals(weighted3.hashCode(), weighted1.hashCode())

        // Test copy()
        val weightedCopy = weighted1.copy(intensity = 5.0)
        assertEquals(latLng1, weightedCopy.latLng)
        assertEquals(5.0, weightedCopy.intensity)
    }
}
