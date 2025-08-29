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
import com.google.maps.android.data.geojson.GeoJsonPolygon
import org.junit.Assert.assertThrows
import org.junit.Test
import java.lang.reflect.InvocationTargetException

class MultiGeometryTest {

    @Test
    fun `geometryType is correct`() {
        val multiGeometry = MultiGeometry(emptyList<Geometry<*>>())
        assertThat(multiGeometry.geometryType).isEqualTo("MultiGeometry")
    }

    @Test
    fun `geometryObject returns correct geometries`() {
        val points = listOf(
            Point(LatLng(0.0, 0.0)),
            Point(LatLng(5.0, 5.0)),
            Point(LatLng(10.0, 10.0))
        )
        val multiGeometry = MultiGeometry(points)
        assertThat(multiGeometry.geometryObject).containsExactlyElementsIn(points).inOrder()

        val emptyPoints = emptyList<Point>()
        val emptyMultiGeometry = MultiGeometry(emptyPoints)
        assertThat(emptyMultiGeometry.geometryObject).isEmpty()
    }

    @Test
    fun `constructor throws for null geometries`() {
        val exception = assertThrows(InvocationTargetException::class.java) {
            // Using reflection to simulate a Java call with a null value
            val constructor = MultiGeometry::class.java.getConstructor(List::class.java)
            constructor.newInstance(null)
        }
        assertThat(exception.cause).isInstanceOf(NullPointerException::class.java)
    }

    @Test
    fun `setGeometries is a no-op`() {
        val geometry1 = Point(LatLng(1.0, 1.0))
        val multiGeometry = MultiGeometry(listOf(geometry1))

        val geometry2 = Point(LatLng(2.0, 2.0))
        multiGeometry.setGeometries(listOf(geometry2))

        // Assert that the geometry object has not changed
        assertThat(multiGeometry.geometryObject).containsExactly(geometry1)
    }

    @Test
    fun `test with LineString geometries`() {
        val lineStrings = listOf(
            LineString(listOf(LatLng(0.0, 0.0), LatLng(50.0, 50.0))),
            LineString(listOf(LatLng(56.0, 65.0), LatLng(23.0, 23.0)))
        )
        val multiGeometry = MultiGeometry(lineStrings)
        assertThat(multiGeometry.geometryType).isEqualTo("MultiGeometry")
        assertThat(multiGeometry.geometryObject).containsExactlyElementsIn(lineStrings).inOrder()
    }

    @Test
    fun `test with Polygon geometries`() {
        val polygons = listOf(
            GeoJsonPolygon(listOf(listOf(LatLng(0.0, 0.0), LatLng(20.0, 20.0), LatLng(60.0, 60.0), LatLng(0.0, 0.0)))),
            GeoJsonPolygon(listOf(
                listOf(LatLng(0.0, 0.0), LatLng(50.0, 80.0), LatLng(10.0, 15.0), LatLng(0.0, 0.0)),
                listOf(LatLng(0.0, 0.0), LatLng(20.0, 20.0), LatLng(60.0, 60.0), LatLng(0.0, 0.0))
            ))
        )
        val multiGeometry = MultiGeometry(polygons)
        assertThat(multiGeometry.geometryType).isEqualTo("MultiGeometry")
        assertThat(multiGeometry.geometryObject).containsExactlyElementsIn(polygons).inOrder()
    }
}