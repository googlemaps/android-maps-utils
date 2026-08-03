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
package com.google.maps.android.data.renderer

import com.google.maps.android.data.parser.geojson.GeoJsonParser
import com.google.maps.android.data.renderer.mapper.GeoJsonMapper
import com.google.maps.android.data.renderer.model.LineStyle
import com.google.maps.android.data.renderer.model.MultiGeometry
import com.google.maps.android.data.renderer.model.PolygonStyle
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.ByteArrayInputStream

/**
 * End-to-end integration tests verifying GeoJSON parsing and mapping into [DataLayer] features.
 */
class GeoJsonIntegrationTest {

    @Test
    fun testParseAndMapGeoJsonFeatureCollectionWithMultipleGeometryTypes() {
        val geoJsonContent =
            """
            {
              "type": "FeatureCollection",
              "features": [
                {
                  "type": "Feature",
                  "properties": {
                    "name": "Zoned Region",
                    "stroke": "#ff0000",
                    "stroke-width": 3.0,
                    "fill": "#00ff00",
                    "fill-opacity": 0.4
                  },
                  "geometry": {
                    "type": "MultiPolygon",
                    "coordinates": [
                      [
                        [
                          [100.0, 0.0],
                          [101.0, 0.0],
                          [101.0, 1.0],
                          [100.0, 0.0]
                        ]
                      ]
                    ]
                  }
                },
                {
                  "type": "Feature",
                  "properties": {
                    "name": "Transit Line",
                    "stroke": "#0000ff",
                    "stroke-width": 4.0
                  },
                  "geometry": {
                    "type": "MultiLineString",
                    "coordinates": [
                      [
                        [100.0, 0.0],
                        [101.0, 1.0]
                      ]
                    ]
                  }
                }
              ]
            }
            """.trimIndent()

        val parser = GeoJsonParser()
        val parsedObject = parser.parse(ByteArrayInputStream(geoJsonContent.toByteArray()))
        assertNotNull(parsedObject)

        val layer = GeoJsonMapper.toLayer(parsedObject!!)
        assertEquals(2, layer.features.size)

        // Verify MultiPolygon feature
        val multiPolygonFeature = layer.features[0]
        assertTrue(multiPolygonFeature.geometry is MultiGeometry)
        assertTrue(multiPolygonFeature.style is PolygonStyle)
        val polygonStyle = multiPolygonFeature.style as PolygonStyle
        assertEquals(0xFFFF0000.toInt(), polygonStyle.strokeColor)
        assertEquals(3.0f, polygonStyle.strokeWidth, 0.001f)
        val expectedFillColor = (0x66 shl 24) or 0x00FF00 // 0.4 * 255 = 102 = 0x66
        assertEquals(expectedFillColor, polygonStyle.fillColor)

        // Verify MultiLineString feature
        val multiLineFeature = layer.features[1]
        assertTrue(multiLineFeature.geometry is MultiGeometry)
        assertTrue(multiLineFeature.style is LineStyle)
        val lineStyle = multiLineFeature.style as LineStyle
        assertEquals(0xFF0000FF.toInt(), lineStyle.color)
        assertEquals(4.0f, lineStyle.width, 0.001f)
    }

    @Test
    fun testDataLayerBoundingBox_includesMultiGeometryCoordinates() {
        val geoJsonContent =
            """
            {
              "type": "FeatureCollection",
              "features": [
                {
                  "type": "Feature",
                  "properties": {},
                  "geometry": {
                    "type": "MultiPolygon",
                    "coordinates": [
                      [
                        [
                          [-74.0250, 40.7000],
                          [-74.0100, 40.7000],
                          [-74.0100, 40.7150],
                          [-74.0250, 40.7000]
                        ]
                      ]
                    ]
                  }
                },
                {
                  "type": "Feature",
                  "properties": {},
                  "geometry": {
                    "type": "MultiLineString",
                    "coordinates": [
                      [
                        [-73.9700, 40.7450],
                        [-73.9600, 40.7715]
                      ]
                    ]
                  }
                }
              ]
            }
            """.trimIndent()

        val parser = GeoJsonParser()
        val parsedObject = parser.parse(ByteArrayInputStream(geoJsonContent.toByteArray()))
        assertNotNull(parsedObject)

        val layer = GeoJsonMapper.toLayer(parsedObject!!)
        val bounds = layer.boundingBox
        assertNotNull(bounds)

        assertEquals(40.7000, bounds!!.southwest.latitude, 0.0001)
        assertEquals(-74.0250, bounds.southwest.longitude, 0.0001)
        assertEquals(40.7715, bounds.northeast.latitude, 0.0001)
        assertEquals(-73.9600, bounds.northeast.longitude, 0.0001)
    }
}

