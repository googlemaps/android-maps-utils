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
import com.google.maps.android.data.parser.geojson.GeoJsonFeatureCollection
import com.google.maps.android.data.parser.geojson.GeoJsonGeometry
import com.google.maps.android.data.parser.geojson.GeoJsonGeometryCollection
import com.google.maps.android.data.parser.geojson.GeoJsonLineString
import com.google.maps.android.data.parser.geojson.GeoJsonMultiLineString
import com.google.maps.android.data.parser.geojson.GeoJsonMultiPoint
import com.google.maps.android.data.parser.geojson.GeoJsonMultiPolygon
import com.google.maps.android.data.parser.geojson.GeoJsonObject
import com.google.maps.android.data.parser.geojson.GeoJsonPoint
import com.google.maps.android.data.parser.geojson.GeoJsonPolygon
import com.google.maps.android.data.renderer.model.Feature
import com.google.maps.android.data.renderer.model.Geometry
import com.google.maps.android.data.renderer.model.Layer
import com.google.maps.android.data.renderer.model.LineString
import com.google.maps.android.data.renderer.model.MultiGeometry
import com.google.maps.android.data.renderer.model.Point
import com.google.maps.android.data.renderer.model.PointGeometry
import com.google.maps.android.data.renderer.model.Polygon as ModelPolygon
import com.google.maps.android.data.renderer.model.Scene

/**
 * A mapper class responsible for transforming GeoJSON objects into the platform-agnostic
 * [Scene] model.
 */
object GeoJsonMapper {

    /**
     * Converts a [GeoJsonObject] into a [Scene].
     *
     * @param geoJsonObject The GeoJSON object to convert.
     * @return A [Scene] representation of the GeoJSON object.
     */
    fun toScene(geoJsonObject: GeoJsonObject): Scene {
        return Scene(listOf(toLayer(geoJsonObject)))
    }

    fun toLayer(geoJsonObject: GeoJsonObject): Layer {
        val features = mutableListOf<Feature>()

        when (geoJsonObject) {
            is GeoJsonFeatureCollection -> {
                geoJsonObject.features.forEach { feature ->
                    feature.geometry?.let { geoJsonGeometry ->
                        features.add(toFeature(feature.id, geoJsonGeometry, feature.properties))
                    }
                }
            }
            is GeoJsonFeature -> {
                geoJsonObject.geometry?.let { geoJsonGeometry ->
                    features.add(toFeature(geoJsonObject.id, geoJsonGeometry, geoJsonObject.properties))
                }
            }
            is GeoJsonGeometry -> {
                features.add(toFeature(null, geoJsonObject, null))
            }
        }

        return Layer(features)
    }

    private fun toFeature(id: String?, geoJsonGeometry: GeoJsonGeometry, properties: Map<String, String?>?): Feature {
        val geometry = toGeometry(geoJsonGeometry)
        // TODO: Map GeoJSON styles to platform-agnostic Style objects
        val featureProperties = properties?.filterValues { it != null }?.mapValues { it.value as Any } ?: emptyMap()
        val finalProperties = if (id != null) featureProperties + ("id" to id) else featureProperties

        return Feature(geometry, properties = finalProperties)
    }

    private fun toGeometry(geoJsonGeometry: GeoJsonGeometry): Geometry {
        return when (geoJsonGeometry) {
            is GeoJsonPoint -> PointGeometry(geoJsonGeometry.coordinates.toPoint())
            is GeoJsonMultiPoint -> MultiGeometry(geoJsonGeometry.coordinates.map { PointGeometry(it.toPoint()) })
            is GeoJsonLineString -> LineString(geoJsonGeometry.coordinates.map { it.toPoint() })
            is GeoJsonMultiLineString -> MultiGeometry(geoJsonGeometry.coordinates.map { LineString(it.map { coordinates -> coordinates.toPoint() }) })
            is GeoJsonPolygon -> ModelPolygon(geoJsonGeometry.coordinates.first().map { it.toPoint() },
                geoJsonGeometry.coordinates.drop(1).map { it.map { coordinates -> coordinates.toPoint() } })
            is GeoJsonMultiPolygon -> MultiGeometry(geoJsonGeometry.coordinates.map { polygonCoordinates ->
                ModelPolygon(polygonCoordinates.first().map { it.toPoint() },
                    polygonCoordinates.drop(1).map { it.map { coordinates -> coordinates.toPoint() } })
            })
            is GeoJsonGeometryCollection -> MultiGeometry(geoJsonGeometry.geometries.map { toGeometry(it) })
        }
    }

    private fun Coordinates.toPoint(): Point {
        return Point(lat, lng, alt)
    }
}