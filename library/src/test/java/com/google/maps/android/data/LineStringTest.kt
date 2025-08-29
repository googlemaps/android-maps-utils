/*
 * Copyright 2025 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.google.maps.android.data

import com.google.android.gms.maps.model.LatLng
import com.google.common.truth.Truth.assertThat
import org.junit.Assert.assertThrows
import org.junit.Test
import java.lang.reflect.InvocationTargetException

class LineStringTest {
    private fun createSimpleLineString(): LineString {
        val coordinates = listOf(
            LatLng(95.0, 60.0), // Note: latitude is clamped to 90.0
            LatLng(93.0, 57.0), // Note: latitude is clamped to 90.0
            LatLng(95.0, 55.0), // Note: latitude is clamped to 90.0
            LatLng(95.0, 53.0), // Note: latitude is clamped to 90.0
            LatLng(91.0, 54.0), // Note: latitude is clamped to 90.0
            LatLng(86.0, 56.0)
        )
        return LineString(coordinates)
    }

    @Test
    fun `getType returns correct type`() {
        val lineString = createSimpleLineString()
        assertThat(lineString.getGeometryType()).isEqualTo("LineString")
    }

    @Test
    fun `getGeometryObject returns correct coordinates`() {
        val lineString = createSimpleLineString()
        val expectedCoordinates = listOf(
            LatLng(90.0, 60.0),
            LatLng(90.0, 57.0),
            LatLng(90.0, 55.0),
            LatLng(90.0, 53.0),
            LatLng(90.0, 54.0),
            LatLng(86.0, 56.0)
        )
        assertThat(lineString.getGeometryObject()).containsExactlyElementsIn(expectedCoordinates).inOrder()
    }

    @Test
    fun `constructor throws for null coordinates`() {
        val exception = assertThrows(InvocationTargetException::class.java) {
            val constructor = LineString::class.java.getConstructor(List::class.java)
            constructor.newInstance(null)
        }
        assertThat(exception.cause).isInstanceOf(NullPointerException::class.java)
    }

    @Test
    fun `equals and hashCode work as expected`() {
        val lineString1 = createSimpleLineString()
        val lineString2 = createSimpleLineString()
        val lineString3 = LineString(listOf(LatLng(1.0, 2.0)))

        assertThat(lineString1).isEqualTo(lineString2)
        assertThat(lineString1.hashCode()).isEqualTo(lineString2.hashCode())
        assertThat(lineString1).isNotEqualTo(lineString3)
        assertThat(lineString1.hashCode()).isNotEqualTo(lineString3.hashCode())
    }

    @Test
    fun `toString returns correct string representation`() {
        val lineString = LineString(listOf(LatLng(1.0, 2.0)))
        assertThat(lineString.toString()).isEqualTo("LineString(coordinates=[lat/lng: (1.0,2.0)])")
    }
}