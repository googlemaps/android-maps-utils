@file:Suppress("DEPRECATION")
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

package com.google.maps.android.ktx.utils

import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Polygon
import com.google.common.truth.Truth.assertThat
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import org.junit.Test

internal class PolygonTest {
    @Test
    fun testContainsTrue() {
        val polygon = mockPolygon(listOf(LatLng(1.0, 2.2), LatLng(0.0, 1.0)))
        assertThat(polygon.contains(LatLng(1.0, 2.2))).isTrue()
    }

    @Test
    fun testContainsFalse() {
        val polygon = mockPolygon(listOf(LatLng(1.0, 2.2), LatLng(0.0, 1.0)))
        assertThat(polygon.contains(LatLng(1.01, 2.2))).isFalse()
    }

    @Test
    fun testIsOnEdgeTrue() {
        val polygon = mockPolygon(listOf(LatLng(1.0, 2.2), LatLng(0.0, 1.0)))
        assertThat(polygon.isOnEdge(LatLng(1.0, 2.2))).isTrue()

        // Tolerance
        assertThat(polygon.isOnEdge(LatLng(1.0000005, 2.2))).isTrue()
    }

    @Test
    fun testIsOnEdgeFalse() {
        val polygon = mockPolygon(listOf(LatLng(1.0, 2.2), LatLng(0.0, 1.0)))
        assertThat(polygon.isOnEdge(LatLng(3.0, 2.2))).isFalse()
    }

    private fun mockPolygon(p: List<LatLng>, geodesic: Boolean = true) = mock<Polygon> {
        on { points } doReturn p
        on { isGeodesic } doReturn geodesic
    }
}
