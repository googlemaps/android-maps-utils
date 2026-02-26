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
package com.google.maps.android.data.parser.kml

import com.google.maps.android.data.renderer.mapper.KmlMapper
import com.google.maps.android.data.renderer.model.LineString
import com.google.maps.android.data.renderer.model.MultiGeometry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.ByteArrayInputStream

class KmlTrackTest {

    @Test
    fun `parse gx Track`() {
        val kmlContent = """
            <kml xmlns="http://www.opengis.net/kml/2.2" xmlns:gx="http://www.google.com/kml/ext/2.2">
                <Placemark>
                    <name>Test Track</name>
                    <gx:Track>
                        <when>2010-05-28T02:02:09Z</when>
                        <when>2010-05-28T02:02:35Z</when>
                        <gx:coord>-122.207881 37.371915 156.000000</gx:coord>
                        <gx:coord>-122.205712 37.373288 152.000000</gx:coord>
                    </gx:Track>
                </Placemark>
            </kml>
        """.trimIndent()

        val kml = KmlParser().parse(ByteArrayInputStream(kmlContent.toByteArray()))
        val layer = KmlMapper.toLayer(kml)
        val feature = layer.features.first()

        assertEquals("Test Track", feature.properties["name"])
        assertTrue(feature.geometry is LineString)
        val lineString = feature.geometry as LineString
        assertEquals(2, lineString.points.size)
        assertEquals(37.371915, lineString.points[0].lat, 0.0001)
        assertEquals(-122.207881, lineString.points[0].lng, 0.0001)
        assertEquals(156.0, lineString.points[0].alt!!, 0.0001)

        assertTrue(feature.properties.containsKey("timestamps"))
        val timestamps = feature.properties["timestamps"] as List<*>
        assertEquals(2, timestamps.size)
        assertEquals("2010-05-28T02:02:09Z", timestamps[0])
    }

    @Test
    fun `parse gx MultiTrack`() {
        val kmlContent = """
            <kml xmlns="http://www.opengis.net/kml/2.2" xmlns:gx="http://www.google.com/kml/ext/2.2">
                <Placemark>
                    <name>Test MultiTrack</name>
                    <gx:MultiTrack>
                        <gx:Track>
                            <gx:coord>-122.0 37.0 0</gx:coord>
                            <gx:coord>-122.1 37.1 0</gx:coord>
                        </gx:Track>
                        <gx:Track>
                            <gx:coord>-122.2 37.2 0</gx:coord>
                            <gx:coord>-122.3 37.3 0</gx:coord>
                        </gx:Track>
                    </gx:MultiTrack>
                </Placemark>
            </kml>
        """.trimIndent()

        val kml = KmlParser().parse(ByteArrayInputStream(kmlContent.toByteArray()))
        val layer = KmlMapper.toLayer(kml)
        val feature = layer.features.first()

        assertEquals("Test MultiTrack", feature.properties["name"])
        assertTrue(feature.geometry is MultiGeometry)
        val multiGeometry = feature.geometry as MultiGeometry
        assertEquals(2, multiGeometry.geometries.size)
        assertTrue(multiGeometry.geometries[0] is LineString)
        assertTrue(multiGeometry.geometries[1] is LineString)
    }
}
