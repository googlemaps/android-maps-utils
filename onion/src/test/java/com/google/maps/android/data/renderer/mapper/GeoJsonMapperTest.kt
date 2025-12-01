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

import com.google.maps.android.data.parser.geojson.Coordinates
import com.google.maps.android.data.parser.geojson.GeoJsonFeature
import com.google.maps.android.data.parser.geojson.GeoJsonLineString
import com.google.maps.android.data.parser.geojson.GeoJsonPoint
import com.google.maps.android.data.parser.geojson.GeoJsonPolygon
import com.google.maps.android.data.renderer.model.LineString
import com.google.maps.android.data.renderer.model.Point
import com.google.maps.android.data.renderer.model.PointGeometry
import com.google.maps.android.data.renderer.model.Polygon
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Unit tests for [GeoJsonMapper].
 *
 * These tests verify the correct transformation of GeoJSON objects (which use longitude, latitude)
 * into the internal renderer model objects (which use latitude, longitude).
 */
class GeoJsonMapperTest {

    @Test
    fun `test GeoJsonPoint to Scene`() {
        // GeoJSON Point: (longitude, latitude, [altitude]) -> internal Point: (latitude, longitude, [altitude])
        val geoJsonPoint = GeoJsonPoint(Coordinates(2.0, 1.0, 3.0))
        val scene = GeoJsonMapper.toScene(geoJsonPoint)
        assertEquals(1, scene.features.size)
        val feature = scene.features[0]
        val geometry = feature.geometry as PointGeometry
        assertEquals(Point(2.0, 1.0, 3.0), geometry.point)
    }

    @Test
    fun `test GeoJsonLineString to Scene`() {
        // GeoJSON LineString coordinates are (longitude, latitude)
        val geoJsonLineString = GeoJsonLineString(listOf(Coordinates(2.0, 1.0), Coordinates(4.0, 3.0)))
        val scene = GeoJsonMapper.toScene(geoJsonLineString)
        assertEquals(1, scene.features.size)
        val feature = scene.features[0]
        val geometry = feature.geometry as LineString
        assertEquals(listOf(Point(2.0, 1.0), Point(4.0, 3.0)), geometry.points)
    }

    @Test
    fun `test GeoJsonPolygon to Scene`() {
        // GeoJSON Polygon coordinates are (longitude, latitude)
        val geoJsonPolygon = GeoJsonPolygon(
            listOf(
                listOf(Coordinates(2.0, 1.0), Coordinates(4.0, 3.0), Coordinates(6.0, 5.0), Coordinates(2.0, 1.0))
            )
        )
        val scene = GeoJsonMapper.toScene(geoJsonPolygon)
        assertEquals(1, scene.features.size)
        val feature = scene.features[0]
        val geometry = feature.geometry as Polygon
        assertEquals(
            listOf(Point(2.0, 1.0), Point(4.0, 3.0), Point(6.0, 5.0), Point(2.0, 1.0)),
            geometry.outerBoundary
        )
        assertEquals(0, geometry.innerBoundaries.size)
    }

    @Test
    fun `test GeoJsonPolygon with hole to Scene`() {
        // GeoJSON Polygon coordinates are (longitude, latitude)
        val geoJsonPolygon = GeoJsonPolygon(
            listOf(
                listOf(Coordinates(0.0, 0.0), Coordinates(10.0, 0.0), Coordinates(10.0, 10.0), Coordinates(0.0, 10.0), Coordinates(0.0, 0.0)),
                listOf(Coordinates(2.0, 2.0), Coordinates(8.0, 2.0), Coordinates(8.0, 8.0), Coordinates(2.0, 8.0), Coordinates(2.0, 2.0))
            )
        )
        val scene = GeoJsonMapper.toScene(geoJsonPolygon)
        assertEquals(1, scene.features.size)
        val feature = scene.features[0]
        val geometry = feature.geometry as Polygon
        assertEquals(
            listOf(Point(0.0, 0.0), Point(10.0, 0.0), Point(10.0, 10.0), Point(0.0, 10.0), Point(0.0, 0.0)),
            geometry.outerBoundary
        )
        assertEquals(1, geometry.innerBoundaries.size)
        assertEquals(
            listOf(Point(2.0, 2.0), Point(8.0, 2.0), Point(8.0, 8.0), Point(2.0, 8.0), Point(2.0, 2.0)),
            geometry.innerBoundaries[0]
        )
    }

    @Test
    fun `test GeoJsonFeature to Scene`() {
        val geoJsonFeature = GeoJsonFeature(
            GeoJsonPoint(Coordinates(2.0, 1.0)),
            mapOf("name" to "Test Point", "value" to "123"),
            "feature1"
        )
        val scene = GeoJsonMapper.toScene(geoJsonFeature)
        assertEquals(1, scene.features.size)
        val feature = scene.features[0]
        assertEquals("feature1", feature.properties["id"])
        assertEquals("Test Point", feature.properties["name"])
        assertEquals("123", feature.properties["value"])
    }
}
