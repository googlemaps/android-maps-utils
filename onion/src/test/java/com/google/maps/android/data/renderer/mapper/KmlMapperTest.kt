/*
 * Copyright 2025 Google LLC
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
package com.google.maps.android.data.renderer.mapper

import com.google.maps.android.data.parser.kml.Boundary
import com.google.maps.android.data.parser.kml.Document
import com.google.maps.android.data.parser.kml.Kml
import com.google.maps.android.data.parser.kml.LatLngAlt
import com.google.maps.android.data.parser.kml.LineString as KmlLineString
import com.google.maps.android.data.parser.kml.LinearRing
import com.google.maps.android.data.parser.kml.Placemark
import com.google.maps.android.data.parser.kml.Point as KmlPoint
import com.google.maps.android.data.parser.kml.Polygon as KmlPolygon
import com.google.maps.android.data.renderer.model.LineString
import com.google.maps.android.data.renderer.model.Point
import com.google.maps.android.data.renderer.model.PointGeometry
import com.google.maps.android.data.renderer.model.Polygon
import org.junit.Assert.assertEquals
import org.junit.Test

class KmlMapperTest {

    @Test
    fun `test KmlPoint to Scene`() {
        val kml = Kml(
            placemark = Placemark(
                point = KmlPoint(LatLngAlt(2.0, 1.0, 3.0))
            )
        )
        val scene = KmlMapper.toScene(kml)
        assertEquals(1, scene.features.size)
        val feature = scene.features[0]
        val geometry = feature.geometry as PointGeometry
        assertEquals(Point(2.0, 1.0, 3.0), geometry.point)
    }

    @Test
    fun `test KmlLineString to Scene`() {
        val kml = Kml(
            placemark = Placemark(
                lineString = KmlLineString("1.0,2.0 3.0,4.0")
            )
        )
        val scene = KmlMapper.toScene(kml)
        assertEquals(1, scene.features.size)
        val feature = scene.features[0]
        val geometry = feature.geometry as LineString
        assertEquals(listOf(Point(2.0, 1.0, null), Point(4.0, 3.0, null)), geometry.points)
    }

    @Test
    fun `test KmlPolygon to Scene`() {
        val kml = Kml(
            placemark = Placemark(
                polygon = KmlPolygon(
                    outerBoundaryIs = Boundary(
                        linearRing = LinearRing("1.0,2.0 3.0,4.0 5.0,6.0 1.0,2.0")
                    )
                )
            )
        )
        val scene = KmlMapper.toScene(kml)
        assertEquals(1, scene.features.size)
        val feature = scene.features[0]
        val geometry = feature.geometry as Polygon
        assertEquals(
            listOf(Point(2.0, 1.0, null), Point(4.0, 3.0, null), Point(6.0, 5.0, null), Point(2.0, 1.0, null)),
            geometry.outerBoundary
        )
        assertEquals(0, geometry.innerBoundaries.size)
    }

    @Test
    fun `test KmlPolygon with hole to Scene`() {
        val kml = Kml(
            placemark = Placemark(
                polygon = KmlPolygon(
                    outerBoundaryIs = Boundary(
                        linearRing = LinearRing("0.0,0.0 10.0,0.0 10.0,10.0 0.0,10.0 0.0,0.0")
                    ),
                    innerBoundaryIs = listOf(
                        Boundary(
                            linearRing = LinearRing("2.0,2.0 8.0,2.0 8.0,8.0 2.0,8.0 2.0,2.0")
                        )
                    )
                )
            )
        )
        val scene = KmlMapper.toScene(kml)
        assertEquals(1, scene.features.size)
        val feature = scene.features[0]
        val geometry = feature.geometry as Polygon
        assertEquals(
            listOf(Point(0.0, 0.0, null), Point(0.0, 10.0, null), Point(10.0, 10.0, null), Point(10.0, 0.0, null), Point(0.0, 0.0, null)),
            geometry.outerBoundary
        )
        assertEquals(1, geometry.innerBoundaries.size)
        assertEquals(
            listOf(Point(2.0, 2.0, null), Point(2.0, 8.0, null), Point(8.0, 8.0, null), Point(8.0, 2.0, null), Point(2.0, 2.0, null)),
            geometry.innerBoundaries[0]
        )
    }

    @Test
    fun `test Document with Placemark to Scene`() {
        val kml = Kml(
            document = Document(
                placemarks = listOf(
                    Placemark(
                        point = KmlPoint(LatLngAlt(2.0, 1.0, 3.0))
                    )
                )
            )
        )
        val scene = KmlMapper.toScene(kml)
        assertEquals(1, scene.features.size)
        val feature = scene.features[0]
        val geometry = feature.geometry as PointGeometry
        assertEquals(Point(2.0, 1.0, 3.0), geometry.point)
    }
}
