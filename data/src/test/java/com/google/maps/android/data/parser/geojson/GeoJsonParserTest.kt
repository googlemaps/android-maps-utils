package com.google.maps.android.data.parser.geojson

import com.google.maps.android.data.parser.Geometry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.io.ByteArrayInputStream
import java.io.File

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
        val geoData = parser.parse(stream)

        assertEquals(1, geoData.features.size)
        val feature = geoData.features[0]
        assertEquals(1, feature.properties.size)
        assertEquals("value0", feature.properties["prop0"])
        val point = feature.geometry as Geometry.Point
        assertEquals(0.5, point.lat, 0.0)
        assertEquals(102.0, point.lon, 0.0)
        assertNull(point.alt)
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
        val geoData = parser.parse(stream)

        assertEquals(1, geoData.features.size)
        val feature = geoData.features[0]
        val lineString = feature.geometry as Geometry.LineString
        assertEquals(4, lineString.points.size)
        assertEquals(0.0, lineString.points[0].lat, 0.0)
        assertEquals(102.0, lineString.points[0].lon, 0.0)
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
        val geoData = parser.parse(stream)

        assertEquals(1, geoData.features.size)
        val feature = geoData.features[0]
        val polygon = feature.geometry as Geometry.Polygon
        assertEquals(5, polygon.shell.size)
        assertTrue(polygon.holes.isEmpty())
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
        val geoData = parser.parse(stream)

        assertEquals(1, geoData.features.size)
        val feature = geoData.features[0]
        val lineString = feature.geometry as Geometry.LineString
        assertEquals(2, lineString.points.size)
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
        val geoData = parser.parse(stream)

        assertEquals(1, geoData.features.size)
        val feature = geoData.features[0]
        val lineString = feature.geometry as Geometry.LineString
        assertEquals(4, lineString.points.size)
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
        val geoData = parser.parse(stream)

        assertEquals(1, geoData.features.size)
        val feature = geoData.features[0]
        val polygon = feature.geometry as Geometry.Polygon
        assertEquals(5, polygon.shell.size)
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
        val geoData = parser.parse(stream)

        assertEquals(1, geoData.features.size)
        val feature = geoData.features[0]
        assertTrue(feature.geometry is Geometry.GeometryCollection)
        val geomCollection = feature.geometry as Geometry.GeometryCollection
        assertEquals(2, geomCollection.geometries.size)
        assertTrue(geomCollection.geometries[0] is Geometry.Point)
        assertTrue(geomCollection.geometries[1] is Geometry.LineString)
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
        val geoData = parser.parse(stream)

        assertEquals(0, geoData.features.size)
    }

    @Test
    fun testUsaJson() {
        val stream = File("src/test/resources/usa.json").inputStream()
        val geoData = parser.parse(stream)
        assertEquals(1, geoData.features.size)
    }

    @Test
    fun testSouthLondonSquareJson() {
        val stream = File("src/test/resources/south_london_square_geojson.json").inputStream()
        val geoData = parser.parse(stream)
        assertEquals(1, geoData.features.size)
    }

    @Test
    fun testPoliceJson() {
        val stream = File("src/test/resources/police.json").inputStream()
        val geoData = parser.parse(stream)
        assertTrue(geoData.features.isNotEmpty())
    }

    @Test
    fun testSouthLondonLineJson() {
        val stream = File("src/test/resources/south_london_line_geojson.json").inputStream()
        val geoData = parser.parse(stream)
        assertEquals(1, geoData.features.size)
    }

    @Test
    fun testEarthquakesWithUsaJson() {
        val stream = File("src/test/resources/earthquakes_with_usa.json").inputStream()
        val geoData = parser.parse(stream)
        assertTrue(geoData.features.isNotEmpty())
    }

    @Test
    fun testRadarSearchJson() {
        val stream = File("src/test/resources/radar_search.json").inputStream()
        val geoData = parser.parse(stream)
        assertTrue(geoData.features.isNotEmpty())
    }

    @Test
    fun testMedicareJson() {
        val stream = File("src/test/resources/medicare.json").inputStream()
        val geoData = parser.parse(stream)
        assertTrue(geoData.features.isNotEmpty())
    }

    @Test
    fun testEarthquakesJson() {
        val stream = File("src/test/resources/earthquakes.json").inputStream()
        val geoData = parser.parse(stream)
        assertTrue(geoData.features.isNotEmpty())
    }

    @Test(expected = Exception::class)
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
        parser.parse(stream)
    }
}