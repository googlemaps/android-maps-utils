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
package com.google.maps.android.data.parser

import com.google.common.truth.Truth.assertThat
import com.google.maps.android.data.parser.geojson.GeoJsonParser
import com.google.maps.android.data.parser.gpx.GpxParser
import com.google.maps.android.data.parser.kml.KmlParser
import com.google.maps.android.data.renderer.mapper.GeoJsonMapper
import com.google.maps.android.data.renderer.mapper.toLayer
import com.google.maps.android.data.renderer.model.LineStyle
import com.google.maps.android.data.renderer.model.PolygonStyle
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import kotlin.test.assertFailsWith

@RunWith(RobolectricTestRunner::class)
class SecurityHardeningTest {
    private val kmlParser = KmlParser()
    private val gpxParser = GpxParser()
    private val geoJsonParser = GeoJsonParser()

    @Test
    fun testKmlCoordinateNanPoisoning_throwsException() {
        val kml = """
            <kml xmlns="http://www.opengis.net/kml/2.2">
              <Document>
                <Placemark>
                  <Point>
                    <coordinates>NaN,NaN,0</coordinates>
                  </Point>
                </Placemark>
              </Document>
            </kml>
        """.trimIndent()

        assertFailsWith<Exception> {
            val parsed = kmlParser.parseAsKml(kml.byteInputStream())
            parsed.toLayer()
        }
    }

    @Test
    fun testKmlCoordinateInfinityPoisoning_throwsException() {
        val kml = """
            <kml xmlns="http://www.opengis.net/kml/2.2">
              <Document>
                <Placemark>
                  <Point>
                    <coordinates>10.0,Infinity,0</coordinates>
                  </Point>
                </Placemark>
              </Document>
            </kml>
        """.trimIndent()

        assertFailsWith<Exception> {
            val parsed = kmlParser.parseAsKml(kml.byteInputStream())
            parsed.toLayer()
        }
    }

    @Test
    fun testGpxCoordinateNanPoisoning_throwsException() {
        val gpx = """
            <gpx xmlns="http://www.topografix.com/GPX/1/1" version="1.1">
              <wpt lat="NaN" lon="10.0">
                <name>Poisoned Waypoint</name>
              </wpt>
            </gpx>
        """.trimIndent()

        assertFailsWith<Exception> {
            val parsed = gpxParser.parse(gpx.byteInputStream())
            parsed.toLayer()
        }
    }

    @Test
    fun testGpxCoordinateInfinityPoisoning_throwsException() {
        val gpx = """
            <gpx xmlns="http://www.topografix.com/GPX/1/1" version="1.1">
              <wpt lat="10.0" lon="-Infinity">
                <name>Poisoned Waypoint</name>
              </wpt>
            </gpx>
        """.trimIndent()

        assertFailsWith<Exception> {
            val parsed = gpxParser.parse(gpx.byteInputStream())
            parsed.toLayer()
        }
    }

    @Test
    fun testGeoJsonNonFiniteStyleProperties_sanitizedToSafeDefaults() {
        val json = """
            {
              "type": "FeatureCollection",
              "features": [
                {
                  "type": "Feature",
                  "geometry": {
                    "type": "Polygon",
                    "coordinates": [[[0.0, 0.0], [0.0, 10.0], [10.0, 10.0], [10.0, 0.0], [0.0, 0.0]]]
                  },
                  "properties": {
                    "stroke-width": "NaN",
                    "fill-opacity": "Infinity",
                    "stroke-opacity": "-5.0",
                    "stroke": "#FF0000",
                    "fill": "#00FF00"
                  }
                }
              ]
            }
        """.trimIndent()

        val layer = geoJsonParser.parse(json.byteInputStream())!!.toLayer()
        val feature = layer.features.first()
        val style = feature.style as PolygonStyle

        // Non-finite width must fall back to safe default 1.0f rather than Float.NaN
        assertThat(style.strokeWidth).isEqualTo(1.0f)
        assertThat(style.strokeWidth.isFinite()).isTrue()
    }

    @Test
    fun testGeoJsonLineStringNonFiniteStrokeWidth_sanitizedToSafeDefault() {
        val json = """
            {
              "type": "FeatureCollection",
              "features": [
                {
                  "type": "Feature",
                  "geometry": {
                    "type": "LineString",
                    "coordinates": [[0.0, 0.0], [10.0, 10.0]]
                  },
                  "properties": {
                    "stroke-width": "Infinity",
                    "stroke": "#0000FF"
                  }
                }
              ]
            }
        """.trimIndent()

        val layer = geoJsonParser.parse(json.byteInputStream())!!.toLayer()
        val feature = layer.features.first()
        val style = feature.style as LineStyle

        // Infinity width must fall back to safe default 1.0f rather than Float.POSITIVE_INFINITY
        assertThat(style.width).isEqualTo(1.0f)
        assertThat(style.width.isFinite()).isTrue()
    }

    @Test
    fun testKmlNonFiniteStyleProperties_sanitizedToSafeDefaults() {
        val kml = """
            <kml xmlns="http://www.opengis.net/kml/2.2">
              <Document>
                <Style id="poisonedStyle">
                  <LineStyle>
                    <width>NaN</width>
                  </LineStyle>
                  <IconStyle>
                    <scale>Infinity</scale>
                  </IconStyle>
                </Style>
                <Placemark>
                  <styleUrl>#poisonedStyle</styleUrl>
                  <LineString>
                    <coordinates>0,0,0 10,10,0</coordinates>
                  </LineString>
                </Placemark>
              </Document>
            </kml>
        """.trimIndent()

        // Should parse safely without crashing and sanitize non-finite width/scale to safe defaults
        val layer = kmlParser.parseAsKml(kml.byteInputStream()).toLayer()
        val feature = layer.features.first()
        val style = feature.style as LineStyle
        assertThat(style.width.isFinite()).isTrue()
        assertThat(style.width).isAtLeast(0.0f)
    }
}
