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

class PointTest {
    @Test
    fun `geometryType returns correct type`() {
        val point = Point(LatLng(0.0, 50.0))
        assertThat(point.geometryType).isEqualTo("Point")
    }

    @Test
    fun `geometryObject returns correct coordinates`() {
        val coordinates = LatLng(0.0, 50.0)
        val point = Point(coordinates)
        assertThat(point.geometryObject).isEqualTo(coordinates)
        assertThat(point.coordinates).isEqualTo(coordinates)
    }

    @Test
    fun `constructor throws for null coordinates`() {
        // In Kotlin, Point(null) is a compile-time error.
        // This test verifies the runtime exception for Java compatibility.
        val exception = assertThrows(InvocationTargetException::class.java) {
            // Using reflection to simulate a Java call with a null value
            val constructor = Point::class.java.getConstructor(LatLng::class.java)
            constructor.newInstance(null)
        }
        assertThat(exception.cause).isInstanceOf(NullPointerException::class.java)
    }

    @Test
    fun `equals and hashCode work as expected`() {
        val point1 = Point(LatLng(10.0, 20.0))
        val point2 = Point(LatLng(10.0, 20.0))
        val point3 = Point(LatLng(30.0, 40.0))

        assertThat(point1).isEqualTo(point2)
        assertThat(point1.hashCode()).isEqualTo(point2.hashCode())
        assertThat(point1).isNotEqualTo(point3)
        assertThat(point1.hashCode()).isNotEqualTo(point3.hashCode())
        assertThat(point1).isNotEqualTo(null)
    }

    @Test
    fun `toString returns correct string representation`() {
        val point = Point(LatLng(1.0, 2.0))
        assertThat(point.toString()).isEqualTo("Point(coordinates=lat/lng: (1.0,2.0))")
    }
}