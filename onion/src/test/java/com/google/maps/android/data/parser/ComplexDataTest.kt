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

import com.google.maps.android.data.parser.geojson.GeoJsonParser
import com.google.maps.android.data.parser.kml.KmlParser
import com.google.maps.android.data.renderer.mapper.GeoJsonMapper
import com.google.maps.android.data.renderer.mapper.KmlMapper
import com.google.maps.android.data.renderer.model.LineString
import com.google.maps.android.data.renderer.model.MultiGeometry
import com.google.maps.android.data.renderer.model.PointGeometry
import com.google.maps.android.data.renderer.model.Polygon
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ComplexDataTest {

    @Test
    fun `test complex KML parsing`() {
        val stream = javaClass.classLoader?.getResourceAsStream("kml-types.kml")
        assertNotNull("kml-types.kml not found", stream)
        val kml = KmlParser().parse(stream!!)
        val layer = KmlMapper.toLayer(kml)

        assertEquals(3, layer.features.size)

        // 1. Styled Point
        val pointFeature = layer.features.find { it.properties["name"] == "Styled Point" }
        assertNotNull(pointFeature)
        assertTrue(pointFeature!!.geometry is PointGeometry)
        assertEquals(-122.0839597, (pointFeature.geometry as PointGeometry).point.lng, 0.0001)

        // 2. Polygon with Hole
        val polygonFeature = layer.features.find { it.properties["name"] == "Polygon with a Hole" }
        assertNotNull(polygonFeature)
        assertTrue(polygonFeature!!.geometry is Polygon)
        val polygon = polygonFeature.geometry as Polygon
        assertEquals(1, polygon.innerBoundaries.size) // Verify hole exists

        // 3. MultiGeometry
        val multiFeature = layer.features.find { it.properties["name"] == "MultiGeometry Example" }
        assertNotNull(multiFeature)
        assertTrue(multiFeature!!.geometry is MultiGeometry)
        val multiGeometry = multiFeature.geometry as MultiGeometry
        assertEquals(2, multiGeometry.geometries.size)
        assertTrue(multiGeometry.geometries.any { it is PointGeometry })
        assertTrue(multiGeometry.geometries.any { it is LineString })
    }

    @Test
    fun `test complex GeoJSON parsing`() {
        val stream = javaClass.classLoader?.getResourceAsStream("geojson-types.json")
        assertNotNull("geojson-types.json not found", stream)
        val geoJson = GeoJsonParser().parse(stream!!)
        assertNotNull(geoJson)
        val layer = GeoJsonMapper.toLayer(geoJson!!)

        assertEquals(4, layer.features.size)

        // 1. Point
        val pointFeature = layer.features.find { it.properties["name"] == "City Hall" }
        assertNotNull(pointFeature)
        assertTrue(pointFeature!!.geometry is PointGeometry)

        // 2. LineString
        val lineFeature = layer.features.find { it.properties["name"] == "Main Street" }
        assertNotNull(lineFeature)
        assertTrue(lineFeature!!.geometry is LineString)

        // 3. Polygon
        val polyFeature = layer.features.find { it.properties["name"] == "Central Park" }
        assertNotNull(polyFeature)
        assertTrue(polyFeature!!.geometry is Polygon)

        // 4. GeometryCollection
        val collectionFeature = layer.features.find { it.properties["name"] == "District Boundaries" }
        assertNotNull(collectionFeature)
        assertTrue(collectionFeature!!.geometry is MultiGeometry)
        val collection = collectionFeature.geometry as MultiGeometry
        assertEquals(2, collection.geometries.size)
        assertTrue(collection.geometries.any { it is LineString })
        assertTrue(collection.geometries.any { it is PointGeometry })
    }
}
