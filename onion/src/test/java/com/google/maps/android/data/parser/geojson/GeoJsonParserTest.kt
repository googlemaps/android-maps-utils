package com.google.maps.android.data.parser.geojson

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.io.ByteArrayInputStream
import java.io.File
import kotlin.test.assertFailsWith

@RunWith(RobolectricTestRunner::class)
class GeoJsonParserTest {

    private val parser = GeoJsonParser()

    @Test
    fun testParsePoint() {
        val geoJson = """
            {
              "type": "FeatureCollection",
              "features": [
                {
                  "type": "Feature",
                  "geometry": {
                    "type": "Point",
                    "coordinates": [102.0, 0.5]
                  },
                  "properties": {
                    "prop0": "value0"
                  }
                }
              ]
            }
        """.trimIndent()
        val stream = ByteArrayInputStream(geoJson.toByteArray())
        val geoJsonObj = parser.parse(stream) as GeoJsonFeatureCollection

        assertThat(geoJsonObj.features.size).isEqualTo(1)
        val feature = geoJsonObj.features[0]
        assertThat(feature.properties?.size).isEqualTo(1)
        assertThat(feature.properties?.get("prop0")).isEqualTo("value0")
        val point = feature.geometry as GeoJsonPoint
        assertThat(point.coordinates).isEqualTo(listOf(102.0, 0.5))
    }

    @Test
    fun testParseLineString() {
        val geoJson = """
            {
              "type": "FeatureCollection",
              "features": [
                {
                  "type": "Feature",
                  "geometry": {
                    "type": "LineString",
                    "coordinates": [ [102.0, 0.0], [103.0, 1.0], [104.0, 0.0], [105.0, 1.0] ]
                  },
                  "properties": {
                    "prop0": "value0",
                    "prop1": "0.0"
                  }
                }
              ]
            }
        """.trimIndent()
        val stream = ByteArrayInputStream(geoJson.toByteArray())
        val geoJsonObj = parser.parse(stream) as GeoJsonFeatureCollection

        assertThat(geoJsonObj.features.size).isEqualTo(1)
        val feature = geoJsonObj.features[0]
        val lineString = feature.geometry as GeoJsonLineString
        assertThat(lineString.coordinates.size).isEqualTo(4)
        assertThat(lineString.coordinates[0]).isEqualTo(listOf(102.0, 0.0))
        assertThat(lineString.coordinates[1]).isEqualTo(listOf(103.0, 1.0))
        assertThat(lineString.coordinates[2]).isEqualTo(listOf(104.0, 0.0))
        assertThat(lineString.coordinates[3]).isEqualTo(listOf(105.0, 1.0))
    }

    @Test
    fun testParsePolygon() {
        val geoJson = """
            {
              "type": "FeatureCollection",
              "features": [
                {
                  "type": "Feature",
                  "geometry": {
                    "type": "Polygon",
                    "coordinates": [
                      [ [100.0, 0.0], [101.0, 0.0], [101.0, 1.0], [100.0, 1.0], [100.0, 0.0] ]
                    ]
                  }
                }
              ]
            }
        """.trimIndent()
        val stream = ByteArrayInputStream(geoJson.toByteArray())
        val geoJsonObj = parser.parse(stream) as GeoJsonFeatureCollection

        assertThat(geoJsonObj.features.size).isEqualTo(1)
        val feature = geoJsonObj.features[0]
        val polygon = feature.geometry as GeoJsonPolygon
        assertThat(polygon.coordinates.size).isEqualTo(1)
        assertThat(polygon.coordinates[0].size).isEqualTo(5)
    }

    @Test
    fun testParseMultiPoint() {
        val geoJson = """
        {
          "type": "Feature",
          "geometry": {
            "type": "MultiPoint",
            "coordinates": [ [100.0, 0.0], [101.0, 1.0] ]
          }
        }
        """.trimIndent()
        val stream = ByteArrayInputStream(geoJson.toByteArray())
        val geoJsonObj = parser.parse(stream) as GeoJsonFeature

        val multiPoint = geoJsonObj.geometry as GeoJsonMultiPoint
        assertThat(multiPoint.coordinates.size).isEqualTo(2)
        assertThat(multiPoint.coordinates[0]).isEqualTo(listOf(100.0, 0.0))
        assertThat(multiPoint.coordinates[1]).isEqualTo(listOf(101.0, 1.0))
    }

    @Test
    fun testParseMultiLineString() {
        val geoJson = """
        {
          "type": "Feature",
          "geometry": {
            "type": "MultiLineString",
            "coordinates": [
              [ [100.0, 0.0], [101.0, 1.0] ],
              [ [102.0, 2.0], [103.0, 3.0] ]
            ]
          }
        }
        """.trimIndent()
        val stream = ByteArrayInputStream(geoJson.toByteArray())
        val geoJsonObj = parser.parse(stream) as GeoJsonFeature

        val multiLineString = geoJsonObj.geometry as GeoJsonMultiLineString
        assertThat(multiLineString.coordinates.size).isEqualTo(2)
        assertThat(multiLineString.coordinates[0][0]).isEqualTo(listOf(100.0, 0.0))
        assertThat(multiLineString.coordinates[0][1]).isEqualTo(listOf(101.0, 1.0))
        assertThat(multiLineString.coordinates[1][0]).isEqualTo(listOf(102.0, 2.0))
        assertThat(multiLineString.coordinates[1][1]).isEqualTo(listOf(103.0, 3.0))
    }

    @Test
    fun testParseMultiPolygon() {
        val geoJson = """
        {
          "type": "Feature",
          "geometry": {
            "type": "MultiPolygon",
            "coordinates": [
              [[[102.0, 2.0], [103.0, 2.0], [103.0, 3.0], [102.0, 3.0], [102.0, 2.0]]],
              [[[100.0, 0.0], [101.0, 0.0], [101.0, 1.0], [100.0, 1.0], [100.0, 0.0]]]
            ]
          }
        }
        """.trimIndent()
        val stream = ByteArrayInputStream(geoJson.toByteArray())
        val geoJsonObj = parser.parse(stream) as GeoJsonFeature

        val multiPolygon = geoJsonObj.geometry as GeoJsonMultiPolygon
        assertThat(multiPolygon.coordinates.size).isEqualTo(2)
    }

    @Test
    fun testParseGeometryCollection() {
        val geoJson = """
        {
          "type": "Feature",
          "geometry": {
            "type": "GeometryCollection",
            "geometries": [
              {
                "type": "Point",
                "coordinates": [100.0, 0.0]
              },
              {
                "type": "LineString",
                "coordinates": [ [101.0, 0.0], [102.0, 1.0] ]
              }
            ]
          }
        }
        """.trimIndent()
        val stream = ByteArrayInputStream(geoJson.toByteArray())
        val geoJsonObj = parser.parse(stream) as GeoJsonFeature

        assertThat(geoJsonObj.geometry).isInstanceOf(GeoJsonGeometryCollection::class.java)
        val geomCollection = geoJsonObj.geometry as GeoJsonGeometryCollection
        assertThat(geomCollection.geometries.size).isEqualTo(2)
        assertThat(geomCollection.geometries[0]).isInstanceOf(GeoJsonPoint::class.java)
        assertThat(geomCollection.geometries[1]).isInstanceOf(GeoJsonLineString::class.java)
    }

    @Test
    fun testParseNullGeometry() {
        val geoJson = """
        {
          "type": "Feature",
          "geometry": null,
          "properties": {
            "name": "null island"
          }
        }
        """.trimIndent()
        val stream = ByteArrayInputStream(geoJson.toByteArray())
        val geoJsonObj = parser.parse(stream) as GeoJsonFeature

        assertThat(geoJsonObj.geometry).isNull()
        assertThat(geoJsonObj.properties?.get("name")).isEqualTo("null island")
    }

    @Test
    fun testUsaJson() {
        val stream = File("src/test/resources/usa.json").inputStream()
        val geoJsonObj = parser.parse(stream) as GeoJsonFeatureCollection
        assertThat(geoJsonObj.features.size).isEqualTo(1)
    }

    @Test
    fun testSouthLondonSquareJson() {
        val stream = File("src/test/resources/south_london_square_geojson.json").inputStream()
        val geoJsonObj = parser.parse(stream) as GeoJsonFeatureCollection
        assertThat(geoJsonObj.features.size).isEqualTo(1)
    }

    @Test
    fun testSouthLondonLineJson() {
        val stream = File("src/test/resources/south_london_line_geojson.json").inputStream()
        val geoJsonObj = parser.parse(stream) as GeoJsonFeatureCollection
        assertThat(geoJsonObj.features.size).isEqualTo(1)
    }

    @Test
    fun testEarthquakesWithUsaJson() {
        val stream = File("src/test/resources/earthquakes_with_usa.json").inputStream()
        val geoJsonObj = parser.parse(stream) as GeoJsonFeatureCollection
        assertThat(geoJsonObj.features.isNotEmpty()).isTrue()
    }

    @Test
    fun testEarthquakesJson() {
        val stream = File("src/test/resources/earthquakes.json").inputStream()
        val geoJsonObj = parser.parse(stream) as GeoJsonFeatureCollection
        assertThat(geoJsonObj.features.isNotEmpty()).isTrue()
    }

    @Test
    fun testParseInvalidJson() {
        val geoJson = """
            {
              "type": "FeatureCollection",
              "features": [
                {
                  "type": "Feature",
                  "geometry": {
                    "type": "Point",
                    "coordinates": [102.0, 0.5]
                  },
                  "properties": {
                    "prop0": "value0"
                  }
                }
              ]
            }
        """.trimIndent().substring(10) // malformed json
        val stream = ByteArrayInputStream(geoJson.toByteArray())
        val parser = GeoJsonParser()
        assertFailsWith<Exception> {
            parser.parse(stream)
        }
    }
}