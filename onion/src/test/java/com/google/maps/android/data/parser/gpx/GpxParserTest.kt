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
package com.google.maps.android.data.parser.gpx

import com.google.maps.android.data.renderer.mapper.GpxMapper
import com.google.maps.android.data.renderer.model.LineString
import com.google.maps.android.data.renderer.model.PointGeometry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.ByteArrayInputStream

class GpxParserTest {

    @Test
    fun `test parse simple GPX`() {
        val gpxXml = """
            <?xml version="1.0" encoding="UTF-8"?>
            <gpx version="1.1" creator="Test" xmlns="http://www.topografix.com/GPX/1/1">
                <metadata>
                    <name>Test GPX</name>
                </metadata>
                <wpt lat="37.7749" lon="-122.4194">
                    <ele>10.0</ele>
                    <name>San Francisco</name>
                </wpt>
                <trk>
                    <name>Test Track</name>
                    <trkseg>
                        <trkpt lat="37.7749" lon="-122.4194">
                            <ele>10.0</ele>
                        </trkpt>
                        <trkpt lat="37.7750" lon="-122.4195">
                            <ele>11.0</ele>
                        </trkpt>
                    </trkseg>
                </trk>
            </gpx>
        """.trimIndent()

        val parser = GpxParser()
        val gpx = parser.parse(ByteArrayInputStream(gpxXml.toByteArray()))

        assertEquals("1.1", gpx.version)
        assertEquals("Test", gpx.creator)
        assertEquals("Test GPX", gpx.metadata?.name)
        assertEquals(1, gpx.waypoints.size)
        assertEquals("San Francisco", gpx.waypoints[0].name)
        assertEquals(37.7749, gpx.waypoints[0].lat, 0.0001)
        assertEquals(1, gpx.tracks.size)
        assertEquals("Test Track", gpx.tracks[0].name)
        assertEquals(1, gpx.tracks[0].trackSegments.size)
        assertEquals(2, gpx.tracks[0].trackSegments[0].trackPoints.size)
    }

    @Test
    fun `test map GPX to Layer`() {
        val gpx = Gpx(
            waypoints = listOf(
                Wpt(lat = 1.0, lon = 2.0, ele = 3.0, name = "Wpt1")
            ),
            tracks = listOf(
                Trk(
                    name = "Trk1",
                    trackSegments = listOf(
                        TrkSeg(
                            trackPoints = listOf(
                                Wpt(lat = 1.0, lon = 2.0),
                                Wpt(lat = 3.0, lon = 4.0)
                            )
                        )
                    )
                )
            )
        )

        val layer = GpxMapper.toLayer(gpx)
        assertEquals(2, layer.features.size)

        val wptFeature = layer.features[0]
        assertTrue(wptFeature.geometry is PointGeometry)
        assertEquals("Wpt1", wptFeature.properties["name"])

        val trkFeature = layer.features[1]
        assertTrue(trkFeature.geometry is LineString)
        assertEquals("Trk1", trkFeature.properties["name"])
    }
}
