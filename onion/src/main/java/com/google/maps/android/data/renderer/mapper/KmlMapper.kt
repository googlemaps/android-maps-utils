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

import com.google.maps.android.data.parser.kml.Kml
import com.google.maps.android.data.parser.kml.Placemark
import com.google.maps.android.data.renderer.model.Feature
import com.google.maps.android.data.renderer.model.Geometry
import com.google.maps.android.data.renderer.model.LineString
import com.google.maps.android.data.renderer.model.MultiGeometry
import com.google.maps.android.data.renderer.model.Point
import com.google.maps.android.data.renderer.model.PointGeometry
import com.google.maps.android.data.renderer.model.Polygon as ModelPolygon
import com.google.maps.android.data.renderer.model.Scene

object KmlMapper {
    fun toScene(kml: Kml): Scene {
        val features = mutableListOf<Feature>()
        kml.document?.let {
            it.placemarks.forEach { placemark ->
                features.add(toFeature(placemark))
            }
        }
        kml.placemark?.let {
            features.add(toFeature(it))
        }
        return Scene(features)
    }

    private fun toFeature(placemark: Placemark): Feature {
        val geometry = toGeometry(placemark)
        // TODO: Map KML styles to platform-agnostic Style objects
        val properties = mutableMapOf<String, Any>()
        placemark.name?.let { properties["name"] = it }
        placemark.description?.let { properties["description"] = it }
        placemark.extendedData?.data?.forEach { data ->
            data.name?.let { name ->
                data.value?.let { value ->
                    properties[name] = value
                }
            }
        }

        return Feature(geometry, properties = properties)
    }

    private fun toGeometry(placemark: Placemark): Geometry {
        placemark.point?.let {
            return PointGeometry(Point(it.coordinates.latitude, it.coordinates.longitude, it.coordinates.altitude))
        }
        placemark.lineString?.let {
            return LineString(it.points.map { Point(it.latitude, it.longitude, it.altitude) })
        }
        placemark.polygon?.let {
            val outerBoundary = it.outerBoundaryIs.linearRing.points.map { Point(it.latitude, it.longitude, it.altitude) }
            val innerBoundaries = it.innerBoundaryIs.map { boundary ->
                boundary.linearRing.points.map { Point(it.latitude, it.longitude, it.altitude) }
            }
            return ModelPolygon(outerBoundary, innerBoundaries)
        }
        placemark.multiGeometry?.let {
            val geometries = mutableListOf<Geometry>()
            it.points.forEach { point ->
                geometries.add(PointGeometry(Point(point.coordinates.latitude, point.coordinates.longitude, point.coordinates.altitude)))
            }
            it.lineStrings.forEach { lineString ->
                geometries.add(LineString(lineString.points.map { Point(it.latitude, it.longitude, it.altitude) }))
            }
            it.polygons.forEach { polygon ->
                val outerBoundary = polygon.outerBoundaryIs.linearRing.points.map { Point(it.latitude, it.longitude, it.altitude) }
                val innerBoundaries = polygon.innerBoundaryIs.map { boundary ->
                    boundary.linearRing.points.map { Point(it.latitude, it.longitude, it.altitude) }
                }
                geometries.add(ModelPolygon(outerBoundary, innerBoundaries))
            }
            return MultiGeometry(geometries)
        }
        // Should not happen if KML is valid
        throw IllegalArgumentException("Placemark must contain a geometry")
    }
}
