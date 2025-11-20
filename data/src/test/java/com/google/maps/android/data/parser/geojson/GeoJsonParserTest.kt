package com.google.maps.android.data.parser.geojson

import com.google.common.truth.Truth.assertThat
import org.json.JSONObject
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.io.InputStreamReader

@RunWith(RobolectricTestRunner::class)
class GeoJsonParserTest {

    private fun parseFile(fileName: String): GeoJson {
        val stream = javaClass.classLoader!!.getResourceAsStream(fileName)
        val reader = InputStreamReader(stream)
        val content = reader.readText()
        reader.close()
        return GeoJsonParser(JSONObject(content)).parse()
    }

    @Test
    fun `test earthquakes_json`() {
        val geoJson = parseFile("earthquakes.json")
        assertThat(geoJson).isInstanceOf(GeoJson.FeatureCollection::class.java)
        val featureCollection = geoJson as GeoJson.FeatureCollection
        assertThat(featureCollection.features).hasSize(213)

        val feature = featureCollection.features[0]
        assertThat(feature.properties).containsEntry("mag", "2.2")
        assertThat(feature.id).isEqualTo("ak13731988")
        assertThat(feature.geometry).isInstanceOf(GeoJson.Point::class.java)

        val point = feature.geometry as GeoJson.Point
        assertThat(point.coordinates.latitude).isEqualTo(57.7582)
        assertThat(point.coordinates.longitude).isEqualTo(-156.1488)
    }


    @Test
    fun `test earthquakes_with_usa_json`() {
        val geoJson = parseFile("earthquakes_with_usa.json")
        assertThat(geoJson).isInstanceOf(GeoJson.FeatureCollection::class.java)
        val featureCollection = geoJson as GeoJson.FeatureCollection
        assertThat(featureCollection.features).hasSize(214)

        // Test the first feature (a Point)
        val pointFeature = featureCollection.features[0]
        assertThat(pointFeature.properties).containsEntry("mag", "2.2")
        assertThat(pointFeature.id).isEqualTo("ak13731988")
        assertThat(pointFeature.geometry).isInstanceOf(GeoJson.Point::class.java)
        val point = pointFeature.geometry as GeoJson.Point
        assertThat(point.coordinates.latitude).isEqualTo(57.7582)
        assertThat(point.coordinates.longitude).isEqualTo(-156.1488)

        // Test the third feature (a MultiPolygon)
        val polygonFeature = featureCollection.features[2]
        assertThat(polygonFeature.properties).containsEntry("title", "MultiPolygon United States of America")
        assertThat(polygonFeature.geometry).isInstanceOf(GeoJson.MultiPolygon::class.java)
        val multiPolygon = polygonFeature.geometry as GeoJson.MultiPolygon
        assertThat(multiPolygon.polygons).hasSize(3)
        val firstPolygon = multiPolygon.polygons[0]
        assertThat(firstPolygon.coordinates).hasSize(1)
        assertThat(firstPolygon.coordinates[0]).hasSize(31)
    }

    @Test
    fun `test south_london_line_geojson_json`() {
        val geoJson = parseFile("south_london_line_geojson.json")
        assertThat(geoJson).isInstanceOf(GeoJson.FeatureCollection::class.java)
        val featureCollection = geoJson as GeoJson.FeatureCollection
        assertThat(featureCollection.features).hasSize(1)

        val feature = featureCollection.features[0]
        assertThat(feature.properties).containsEntry("title", "South London Line GeoJSON")
        assertThat(feature.geometry).isInstanceOf(GeoJson.LineString::class.java)

        val lineString = feature.geometry as GeoJson.LineString
        assertThat(lineString.coordinates).hasSize(6)
        assertThat(lineString.coordinates[0].latitude).isEqualTo(51.402204296190476)
        assertThat(lineString.coordinates[0].longitude).isEqualTo(-0.2245330810546875)
        assertThat(lineString.coordinates[5].latitude).isEqualTo(51.45229536554372)
        assertThat(lineString.coordinates[5].longitude).isEqualTo(-0.03570556640625)
    }


    @Test
    fun `test south_london_square_geojson_json`() {
        val geoJson = parseFile("south_london_square_geojson.json")
        assertThat(geoJson).isInstanceOf(GeoJson.FeatureCollection::class.java)
        val featureCollection = geoJson as GeoJson.FeatureCollection
        assertThat(featureCollection.features).hasSize(1)

        val feature = featureCollection.features[0]
        assertThat(feature.properties).containsEntry("title", "South London Polygon GeoJSON")
        assertThat(feature.geometry).isInstanceOf(GeoJson.Polygon::class.java)

        val polygon = feature.geometry as GeoJson.Polygon
        assertThat(polygon.coordinates).hasSize(1) // Single exterior boundary
        val boundary = polygon.coordinates[0]
        assertThat(boundary).hasSize(5)

        // Check a few coordinates to ensure correctness
        assertThat(boundary[0].latitude).isEqualTo(51.41034247807634)
        assertThat(boundary[0].longitude).isEqualTo(-0.196380615234375)
        assertThat(boundary[2].latitude).isEqualTo(51.44159675846268)
        assertThat(boundary[2].longitude).isEqualTo(-0.09716033935546874)
        assertThat(boundary[4].latitude).isEqualTo(51.41034247807634)
        assertThat(boundary[4].longitude).isEqualTo(-0.196380615234375)
    }

    @Test
    fun `test usa_json`() {
        val geoJson = parseFile("usa.json")
        assertThat(geoJson).isInstanceOf(GeoJson.FeatureCollection::class.java)
        val featureCollection = geoJson as GeoJson.FeatureCollection
        assertThat(featureCollection.features).hasSize(1)

        val feature = featureCollection.features[0]
        assertThat(feature.properties).containsEntry("title", "MultiPolygon United States of America")
        assertThat(feature.geometry).isInstanceOf(GeoJson.MultiPolygon::class.java)

        val multiPolygon = feature.geometry as GeoJson.MultiPolygon
        assertThat(multiPolygon.polygons).hasSize(3)

        val firstPolygon = multiPolygon.polygons[0]
        assertThat(firstPolygon.coordinates).hasSize(1)
        assertThat(firstPolygon.coordinates[0]).hasSize(31)
    }
}