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

import com.google.maps.android.data.parser.gpx.Gpx
import com.google.maps.android.data.parser.gpx.Rte
import com.google.maps.android.data.parser.gpx.Trk
import com.google.maps.android.data.parser.gpx.Wpt
import com.google.maps.android.data.renderer.model.Feature
import com.google.maps.android.data.renderer.model.Geometry
import com.google.maps.android.data.renderer.model.DataLayer
import com.google.maps.android.data.renderer.model.LineString
import com.google.maps.android.data.renderer.model.MultiGeometry
import com.google.maps.android.data.renderer.model.Point
import com.google.maps.android.data.renderer.model.PointGeometry
import com.google.maps.android.data.renderer.model.DataScene

/**
 * A mapper class responsible for transforming GPX objects into the platform-agnostic
 * [DataScene] model.
 *
 * This mapper handles:
 * - Waypoints (mapped to Points)
 * - Routes (mapped to LineStrings)
 * - Tracks (mapped to MultiGeometries or LineStrings)
 */
object GpxMapper {
    fun toScene(gpx: Gpx): DataScene {
        return DataScene(listOf(toLayer(gpx)))
    }

    fun toLayer(gpx: Gpx): DataLayer {
        val features = mutableListOf<Feature>()

        gpx.waypoints.forEach { wpt ->
            features.add(wpt.toFeature())
        }

        gpx.routes.forEach { rte ->
            features.add(rte.toFeature())
        }

        gpx.tracks.forEach { trk ->
            features.add(trk.toFeature())
        }

        return DataLayer(features)
    }
}

private fun Wpt.toFeature(): Feature {
    val point = Point(lat, lon, ele)
    val geometry = PointGeometry(point)
    val properties = mutableMapOf<String, Any>()
    name?.let { properties["name"] = it }
    desc?.let { properties["desc"] = it }
    time?.let { properties["time"] = it }
    sym?.let { properties["sym"] = it }
    return Feature(geometry, properties = properties)
}

private fun Rte.toFeature(): Feature {
    val points = routePoints.map { Point(it.lat, it.lon, it.ele) }
    val geometry = LineString(points)
    val properties = mutableMapOf<String, Any>()
    name?.let { properties["name"] = it }
    desc?.let { properties["desc"] = it }
    return Feature(geometry, properties = properties)
}

private fun Trk.toFeature(): Feature {
    val lineStrings = trackSegments.map { segment ->
        LineString(segment.trackPoints.map { Point(it.lat, it.lon, it.ele) })
    }
    val geometry = if (lineStrings.size == 1) {
        lineStrings.first()
    } else {
        MultiGeometry(lineStrings)
    }
    val properties = mutableMapOf<String, Any>()
    name?.let { properties["name"] = it }
    desc?.let { properties["desc"] = it }
    return Feature(geometry, properties = properties)
}
