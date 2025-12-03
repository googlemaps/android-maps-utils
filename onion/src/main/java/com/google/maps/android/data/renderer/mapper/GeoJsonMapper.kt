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
import com.google.maps.android.data.renderer.model.DataLayer
import com.google.maps.android.data.renderer.model.DataScene
import com.google.maps.android.data.renderer.model.Feature
import com.google.maps.android.data.renderer.model.Geometry
import com.google.maps.android.data.renderer.model.LineString
import com.google.maps.android.data.renderer.model.MultiGeometry
import com.google.maps.android.data.renderer.model.Point
import com.google.maps.android.data.renderer.model.PointGeometry
import com.google.maps.android.data.renderer.model.Polygon as ModelPolygon
import com.google.maps.android.data.renderer.model.LineStyle
import com.google.maps.android.data.renderer.model.PolygonStyle

/**
 * A mapper class responsible for transforming GeoJSON objects into the platform-agnostic
 * [DataScene] model.
 */
object GeoJsonMapper {

    /**
     * Converts a [GeoJsonObject] into a [DataScene].
     *
     * @param geoJsonObject The GeoJSON object to convert.
     * @return A [DataScene] representation of the GeoJSON object.
     */
    /**
     * Converts a [GeoJsonObject] into a [DataScene].
     *
     * @param geoJsonObject The GeoJSON object to convert.
     * @return A [DataScene] representation of the GeoJSON object.
     */
    fun toScene(geoJsonObject: GeoJsonObject): DataScene {
        return DataScene(listOf(toLayer(geoJsonObject)))
    }

    fun toLayer(geoJsonObject: GeoJsonObject): DataLayer {
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

        return DataLayer(features)
    }

    private fun toFeature(id: String?, geoJsonGeometry: GeoJsonGeometry, properties: Map<String, String?>?): Feature {
        val geometry = toGeometry(geoJsonGeometry)
        val featureProperties = properties?.filterValues { it != null }?.mapValues { it.value as Any } ?: emptyMap()
        val finalProperties = if (id != null) featureProperties + ("id" to id) else featureProperties

        val style = properties?.let { props ->
            when (geometry) {
                is LineString, is MultiGeometry -> { // MultiGeometry could contain lines
                    val strokeColor = props["stroke"]?.let { parseColor(it) }
                    val strokeWidth = props["stroke-width"]?.toFloatOrNull()
                    if (strokeColor != null || strokeWidth != null) {
                        LineStyle(
                            color = strokeColor ?: 0xFF000000.toInt(),
                            width = strokeWidth ?: 1.0f
                        )
                    } else null
                }
                is ModelPolygon -> {
                    val strokeColor = props["stroke"]?.let { parseColor(it) }
                    val strokeWidth = props["stroke-width"]?.toFloatOrNull()
                    val fillColor = props["fill"]?.let { parseColor(it) }
                    val fillOpacity = props["fill-opacity"]?.toFloatOrNull()
                    val strokeOpacity = props["stroke-opacity"]?.toFloatOrNull()

                    val finalFillColor = if (fillColor != null && fillOpacity != null) {
                        applyOpacity(fillColor, fillOpacity)
                    } else fillColor

                    val finalStrokeColor = if (strokeColor != null && strokeOpacity != null) {
                        applyOpacity(strokeColor, strokeOpacity)
                    } else strokeColor

                    if (finalFillColor != null || finalStrokeColor != null || strokeWidth != null) {
                        PolygonStyle(
                            fillColor = finalFillColor ?: 0x00000000,
                            strokeColor = finalStrokeColor ?: 0xFF000000.toInt(),
                            strokeWidth = strokeWidth ?: 1.0f
                        )
                    } else null
                }
                is PointGeometry -> {
                     // TODO: Marker styling (marker-color, marker-size, marker-symbol)
                     null
                }
                else -> null
            }
        }

        return Feature(geometry, style = style, properties = finalProperties)
    }

    private fun parseColor(colorString: String): Int? {
        if (colorString.startsWith("#")) {
            // Handle hex color
            val hex = colorString.substring(1)
            return try {
                when (hex.length) {
                    6 -> {
                        val r = hex.substring(0, 2).toInt(16)
                        val g = hex.substring(2, 4).toInt(16)
                        val b = hex.substring(4, 6).toInt(16)
                        (0xFF shl 24) or (r shl 16) or (g shl 8) or b
                    }
                    8 -> {
                        val a = hex.substring(0, 2).toInt(16)
                        val r = hex.substring(2, 4).toInt(16)
                        val g = hex.substring(4, 6).toInt(16)
                        val b = hex.substring(6, 8).toInt(16)
                        (a shl 24) or (r shl 16) or (g shl 8) or b
                    }
                    3 -> {
                        val r = hex.substring(0, 1).toInt(16)
                        val g = hex.substring(1, 2).toInt(16)
                        val b = hex.substring(2, 3).toInt(16)
                        (0xFF shl 24) or ((r * 17) shl 16) or ((g * 17) shl 8) or (b * 17)
                    }
                    else -> null
                }
            } catch (e: NumberFormatException) {
                null
            }
        }
        // Fallback to Android Color for named colors if needed, or just support basic names
        return when (colorString.lowercase()) {
            "red" -> -0x10000
            "green" -> -0xff0100
            "blue" -> -0xffff01
            "black" -> -0x1000000
            "white" -> -0x1
            "gray", "grey" -> -0x777778
            "cyan" -> -0xff0001
            "magenta" -> -0xff01
            "yellow" -> -0x100
            "lightgray", "lightgrey" -> -0x2c2c2d // 0xFFD3D3D3
            "darkgray", "darkgrey" -> -0x565657 // 0xFFA9A9A9
            else -> try {
                android.graphics.Color.parseColor(colorString)
            } catch (e: Exception) {
                null
            }
        }
    }

    private fun applyOpacity(color: Int, opacity: Float): Int {
        val alpha = (opacity * 255).toInt().coerceIn(0, 255)
        return (color and 0x00FFFFFF) or (alpha shl 24)
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