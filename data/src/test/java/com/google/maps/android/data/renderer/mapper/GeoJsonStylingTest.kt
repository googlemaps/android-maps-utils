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
package com.google.maps.android.data.renderer.mapper

import com.google.maps.android.data.parser.geojson.GeoJsonParser
import com.google.maps.android.data.renderer.model.LineStyle
import com.google.maps.android.data.renderer.model.PolygonStyle
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.ByteArrayInputStream

class GeoJsonStylingTest {
    @Test
    fun `parse styled polygon`() {
        val geoJson =
            """
            {
              "type": "FeatureCollection",
              "features": [
                {
                  "type": "Feature",
                  "properties": {
                    "stroke": "#ff0000",
                    "stroke-width": 5.0,
                    "stroke-opacity": 0.5,
                    "fill": "#00ff00",
                    "fill-opacity": 0.25
                  },
                  "geometry": {
                    "type": "Polygon",
                    "coordinates": [
                      [
                        [100.0, 0.0],
                        [101.0, 0.0],
                        [101.0, 1.0],
                        [100.0, 1.0],
                        [100.0, 0.0]
                      ]
                    ]
                  }
                }
              ]
            }
            """.trimIndent()

        val parser = GeoJsonParser()
        val geoJsonObject = parser.parse(ByteArrayInputStream(geoJson.toByteArray()))!!
        val layer = GeoJsonMapper.toLayer(geoJsonObject)
        val feature = layer.features.first()

        assertTrue(feature.style is PolygonStyle)
        val style = feature.style as PolygonStyle

        // Verify stroke color (red with 0.5 opacity)
        // Alpha 0.5 * 255 = 127 (0x7F)
        val expectedStrokeColor = (0x7F shl 24) or 0xFF0000
        assertEquals(expectedStrokeColor, style.strokeColor)

        // Verify stroke width
        assertEquals(5.0f, style.strokeWidth, 0.001f)

        // Verify fill color (green with 0.25 opacity)
        // Alpha 0.25 * 255 = 63 (0x3F)
        val expectedFillColor = (0x3F shl 24) or 0x00FF00
        assertEquals(expectedFillColor, style.fillColor)
    }

    @Test
    fun `parse styled linestring`() {
        val geoJson =
            """
            {
              "type": "Feature",
              "properties": {
                "stroke": "#0000ff",
                "stroke-width": 3.0
              },
              "geometry": {
                "type": "LineString",
                "coordinates": [
                  [100.0, 0.0],
                  [101.0, 1.0]
                ]
              }
            }
            """.trimIndent()

        val parser = GeoJsonParser()
        val geoJsonObject = parser.parse(ByteArrayInputStream(geoJson.toByteArray()))!!
        val layer = GeoJsonMapper.toLayer(geoJsonObject)
        val feature = layer.features.first()

        assertTrue(feature.style is LineStyle)
        val style = feature.style as LineStyle

        // Verify stroke color (blue, full opacity)
        assertEquals(0xFF0000FF.toInt(), style.color)

        // Verify stroke width
        assertEquals(3.0f, style.width!!, 0.001f)
    }

    @Test
    fun `parse styled multipolygon applies polygon style`() {
        val geoJson =
            """
            {
              "type": "Feature",
              "properties": {
                "stroke": "#ff0000",
                "stroke-width": 2.0,
                "fill": "#00ff00",
                "fill-opacity": 0.25
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
                  ],
                  [
                    [
                      [102.0, 2.0],
                      [103.0, 2.0],
                      [103.0, 3.0],
                      [102.0, 2.0]
                    ]
                  ]
                ]
              }
            }
            """.trimIndent()

        val parser = GeoJsonParser()
        val geoJsonObject = parser.parse(ByteArrayInputStream(geoJson.toByteArray()))!!
        val layer = GeoJsonMapper.toLayer(geoJsonObject)
        val feature = layer.features.first()

        // A MultiPolygon must carry polygon styling (fill), not line styling.
        assertTrue(feature.style is PolygonStyle)
        val style = feature.style as PolygonStyle

        assertEquals(0xFFFF0000.toInt(), style.strokeColor)
        assertEquals(2.0f, style.strokeWidth, 0.001f)

        // Fill: green with 0.25 opacity (0.25 * 255 = 63 = 0x3F)
        val expectedFillColor = (0x3F shl 24) or 0x00FF00
        assertEquals(expectedFillColor, style.fillColor)
    }

    @Test
    fun `parse styled multilinestring keeps line style`() {
        val geoJson =
            """
            {
              "type": "Feature",
              "properties": {
                "stroke": "#0000ff",
                "stroke-width": 3.0
              },
              "geometry": {
                "type": "MultiLineString",
                "coordinates": [
                  [
                    [100.0, 0.0],
                    [101.0, 1.0]
                  ],
                  [
                    [102.0, 2.0],
                    [103.0, 3.0]
                  ]
                ]
              }
            }
            """.trimIndent()

        val parser = GeoJsonParser()
        val geoJsonObject = parser.parse(ByteArrayInputStream(geoJson.toByteArray()))!!
        val layer = GeoJsonMapper.toLayer(geoJsonObject)
        val feature = layer.features.first()

        assertTrue(feature.style is LineStyle)
        val style = feature.style as LineStyle
        assertEquals(0xFF0000FF.toInt(), style.color)
        assertEquals(3.0f, style.width, 0.001f)
    }

    @Test
    fun `parse styled multipolygon with stroke opacity applies opacity to both stroke and fill`() {
        val geoJson =
            """
            {
              "type": "Feature",
              "properties": {
                "stroke": "#ff0000",
                "stroke-width": 4.0,
                "stroke-opacity": 0.5,
                "fill": "#00ff00",
                "fill-opacity": 0.5
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
            }
            """.trimIndent()

        val parser = GeoJsonParser()
        val geoJsonObject = parser.parse(ByteArrayInputStream(geoJson.toByteArray()))!!
        val layer = GeoJsonMapper.toLayer(geoJsonObject)
        val feature = layer.features.first()

        assertTrue(feature.style is PolygonStyle)
        val style = feature.style as PolygonStyle

        // Alpha 0.5 * 255 = 127 (0x7F)
        val expectedStrokeColor = (0x7F shl 24) or 0xFF0000
        val expectedFillColor = (0x7F shl 24) or 0x00FF00

        assertEquals(expectedStrokeColor, style.strokeColor)
        assertEquals(expectedFillColor, style.fillColor)
        assertEquals(4.0f, style.strokeWidth, 0.001f)
    }

    @Test
    fun `parse styled geometry collection containing mixed elements defaults to line style`() {
        val geoJson =
            """
            {
              "type": "Feature",
              "properties": {
                "stroke": "#0000ff",
                "stroke-width": 2.0
              },
              "geometry": {
                "type": "GeometryCollection",
                "geometries": [
                  {
                    "type": "Polygon",
                    "coordinates": [
                      [
                        [100.0, 0.0],
                        [101.0, 0.0],
                        [101.0, 1.0],
                        [100.0, 0.0]
                      ]
                    ]
                  },
                  {
                    "type": "LineString",
                    "coordinates": [
                      [100.0, 0.0],
                      [101.0, 1.0]
                    ]
                  }
                ]
              }
            }
            """.trimIndent()

        val parser = GeoJsonParser()
        val geoJsonObject = parser.parse(ByteArrayInputStream(geoJson.toByteArray()))!!
        val layer = GeoJsonMapper.toLayer(geoJsonObject)
        val feature = layer.features.first()

        assertTrue(feature.style is LineStyle)
        val style = feature.style as LineStyle
        assertEquals(0xFF0000FF.toInt(), style.color)
        assertEquals(2.0f, style.width, 0.001f)
    }
}
