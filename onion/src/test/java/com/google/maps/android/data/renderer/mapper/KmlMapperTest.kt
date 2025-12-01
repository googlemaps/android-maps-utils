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
import com.google.maps.android.data.renderer.model.PointStyle
import com.google.maps.android.data.renderer.model.Polygon
import com.google.maps.android.data.renderer.model.PolygonStyle
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
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
        assertEquals(Point(lat = 2.0, lng = 1.0, alt = 3.0), geometry.point)
    }

    @Test
    fun `test KmlLineString to Scene`() {
        val kml = Kml(
            placemark = Placemark(
                lineString = KmlLineString(
                    listOf(
                        LatLngAlt(2.0, 1.0),
                        LatLngAlt(4.0, 3.0)
                    )
                )
            )
        )
        val scene = KmlMapper.toScene(kml)
        assertEquals(1, scene.features.size)
        val feature = scene.features[0]
        val geometry = feature.geometry as LineString
        assertEquals(listOf(Point(lat = 2.0, lng = 1.0, alt = null), Point(lat = 4.0, lng = 3.0, alt = null)), geometry.points)
    }

    @Test
    fun `test KmlPolygon to Scene`() {
        val kml = Kml(
            placemark = Placemark(
                polygon = KmlPolygon(
                    outerBoundaryIs = Boundary(
                        linearRing = LinearRing(
                            listOf(
                                LatLngAlt(1.0, 2.0),
                                LatLngAlt(3.0, 4.0),
                                LatLngAlt(5.0, 6.0),
                                LatLngAlt(1.0, 2.0)
                            )
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
            listOf(Point(lat = 1.0, lng = 2.0, alt = null), Point(lat = 3.0, lng = 4.0, alt = null), Point(lat = 5.0, lng = 6.0, alt = null), Point(lat = 1.0, lng = 2.0, alt = null)),
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
                        linearRing = LinearRing(
                            listOf(
                                LatLngAlt(0.0, 0.0, 0.0),
                                LatLngAlt(0.0, 10.0, 0.0),
                                LatLngAlt(10.0, 10.0, 0.0),
                                LatLngAlt(10.0, 0.0, 0.0),
                                LatLngAlt(0.0, 0.0, 0.0)
                            )
                        )
                    ),
                    innerBoundaryIs = listOf(
                        Boundary(
                            linearRing = LinearRing(
                                listOf(
                                    LatLngAlt(2.0, 2.0, 0.0),
                                    LatLngAlt(2.0, 8.0, 0.0),
                                    LatLngAlt(8.0, 8.0, 0.0),
                                    LatLngAlt(8.0, 2.0, 0.0),
                                    LatLngAlt(2.0, 2.0, 0.0)
                                )
                            )
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
            listOf(Point(lat = 0.0, lng = 0.0, alt = 0.0), Point(lat = 0.0, lng = 10.0, alt = 0.0), Point(lat = 10.0, lng = 10.0, alt = 0.0), Point(lat = 10.0, lng = 0.0, alt = 0.0), Point(lat = 0.0, lng = 0.0, alt = 0.0)),
            geometry.outerBoundary
        )
        assertEquals(1, geometry.innerBoundaries.size)
        assertEquals(
            listOf(Point(lat = 2.0, lng = 2.0, alt = 0.0), Point(lat = 2.0, lng = 8.0, alt = 0.0), Point(lat = 8.0, lng = 8.0, alt = 0.0), Point(lat = 8.0, lng = 2.0, alt = 0.0), Point(lat = 2.0, lng = 2.0, alt = 0.0)),
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
        assertEquals(Point(lat = 2.0, lng = 1.0, alt = 3.0), geometry.point)
    }

    @Test
    fun `test top_peaks kml styling`() {
        val stream = javaClass.classLoader!!.getResourceAsStream("top_peaks.kml")
        val kml = com.google.maps.android.data.parser.kml.KmlParser().parse(stream)
        val scene = KmlMapper.toScene(kml)

        // Find Mount Elbert
        val mountElbert = scene.features.find { it.properties["name"] == "Mount Elbert ☝️" }
        assertNotNull("Mount Elbert should be found", mountElbert)
        val elbertStyle = mountElbert!!.style as PointStyle
        assertEquals(1.2f, elbertStyle.scale, 0.01f)
        assertEquals("http://maps.google.com/mapfiles/kml/paddle/red-stars.png", elbertStyle.iconUrl)

        // Find Ute Peak
        val utePeak = scene.features.find { it.properties["name"] == "Ute Peak 🏞️" }
        assertNotNull("Ute Peak should be found", utePeak)
        val uteStyle = utePeak!!.style as PointStyle
        assertEquals(1.0f, uteStyle.scale, 0.01f)
        assertEquals("http://maps.google.com/mapfiles/kml/pushpin/ylw-pushpin.png", uteStyle.iconUrl)
    }

    @Test
    fun `test mountain_ranges styling`() {
        val stream = javaClass.classLoader!!.getResourceAsStream("mountain_ranges.kml")
        val kml = com.google.maps.android.data.parser.kml.KmlParser().parse(stream)
        val scene = KmlMapper.toScene(kml)

        // Find Sangre de Cristo Mountains
        val sangreDeCristo = scene.features.find { it.properties["name"] == "Sangre de Cristo Mountains" }
        assertNotNull("Sangre de Cristo Mountains should be found", sangreDeCristo)
        
        val style = sangreDeCristo!!.style as PolygonStyle
        
        // Expected colors:
        // KML LineStyle color: ff1427a5 (AABBGGRR) -> Android: ffa52714 (AARRGGBB)
        // KML PolyStyle color: 4d1427a5 (AABBGGRR) -> Android: 4da52714 (AARRGGBB)
        
        assertEquals("Fill color should match", 0x4da52714.toInt(), style.fillColor)
        assertEquals("Stroke color should match", 0xffa52714.toInt(), style.strokeColor)
        assertEquals("Stroke width should match", 1.2f, style.strokeWidth, 0.01f)
    }
}
