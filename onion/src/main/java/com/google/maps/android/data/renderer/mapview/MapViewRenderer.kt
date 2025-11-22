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
package com.google.maps.android.data.renderer.mapview

import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions
import com.google.android.gms.maps.model.PolygonOptions
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.maps.android.data.renderer.Renderer
import com.google.maps.android.data.renderer.model.Feature
import com.google.maps.android.data.renderer.model.Geometry
import com.google.maps.android.data.renderer.model.LineString
import com.google.maps.android.data.renderer.model.LineStyle
import com.google.maps.android.data.renderer.model.MultiGeometry
import com.google.maps.android.data.renderer.model.Point
import com.google.maps.android.data.renderer.model.PointGeometry
import com.google.maps.android.data.renderer.model.PointStyle
import com.google.maps.android.data.renderer.model.Polygon
import com.google.maps.android.data.renderer.model.PolygonStyle
import com.google.maps.android.data.renderer.model.Scene
import com.google.maps.android.data.renderer.model.Style

/**
 * A concrete implementation of the [Renderer] interface that renders a [Scene] onto a [GoogleMap].
 *
 * This class is responsible for translating the platform-agnostic [Scene] and [Feature] models
 * into specific Google Maps SDK objects (e.g., Markers, Polylines, Polygons).
 *
 * @property googleMap The [GoogleMap] instance to render features on.
 */
class MapViewRenderer(private val googleMap: GoogleMap) : Renderer {

    private val renderedFeatures = mutableMapOf<Feature, List<Any>>() // Stores rendered map objects

    override fun render(scene: Scene) {
        clear()
        scene.features.forEach { addFeature(it) }
    }

    override fun addFeature(feature: Feature) {
        val mapObjects = mutableListOf<Any>()
        when (feature.geometry) {
            is PointGeometry -> {
                val point = feature.geometry.point
                val style = feature.style as? PointStyle
                val markerOptions = createMarkerOptions(point, style)
                mapObjects.add(googleMap.addMarker(markerOptions)!!)
            }
            is LineString -> {
                val lineString = feature.geometry
                val style = feature.style as? LineStyle
                val polylineOptions = createPolylineOptions(lineString, style)
                mapObjects.add(googleMap.addPolyline(polylineOptions))
            }
            is Polygon -> {
                val polygon = feature.geometry
                val style = feature.style as? PolygonStyle
                val polygonOptions = createPolygonOptions(polygon, style)
                mapObjects.add(googleMap.addPolygon(polygonOptions))
            }
            is MultiGeometry -> {
                feature.geometry.geometries.forEach { geometry ->
                    // Recursively add each geometry in the MultiGeometry
                    addFeature(feature.copy(geometry = geometry))
                }
            }
        }
        renderedFeatures[feature] = mapObjects
    }

    override fun removeFeature(feature: Feature) {
        renderedFeatures[feature]?.forEach { mapObject ->
            when (mapObject) {
                is com.google.android.gms.maps.model.Marker -> mapObject.remove()
                is com.google.android.gms.maps.model.Polyline -> mapObject.remove()
                is com.google.android.gms.maps.model.Polygon -> mapObject.remove()
            }
        }
        renderedFeatures.remove(feature)
    }

    override fun clear() {
        renderedFeatures.values.flatten().forEach { mapObject ->
            when (mapObject) {
                is com.google.android.gms.maps.model.Marker -> mapObject.remove()
                is com.google.android.gms.maps.model.Polyline -> mapObject.remove()
                is com.google.android.gms.maps.model.Polygon -> mapObject.remove()
            }
        }
        renderedFeatures.clear()
    }

    private fun createMarkerOptions(point: Point, style: PointStyle?): MarkerOptions {
        val markerOptions = MarkerOptions().position(LatLng(point.lat, point.lng))
        style?.let {
            if (it.iconUrl != null) {
                // TODO: Load custom icon from URL/resource
                // For now, using default marker
            } else {
                markerOptions.icon(BitmapDescriptorFactory.defaultMarker(hueFromColor(it.color)))
            }
            it.heading?.let { heading -> markerOptions.rotation(heading) }
            markerOptions.anchor(it.anchorU, it.anchorV)
            markerOptions.alpha(android.graphics.Color.alpha(it.color) / 255.0f)
        }
        return markerOptions
    }

    private fun createPolylineOptions(lineString: LineString, style: LineStyle?): PolylineOptions {
        val polylineOptions = PolylineOptions()
        lineString.points.map { LatLng(it.lat, it.lng) }.forEach { polylineOptions.add(it) }
        style?.let {
            polylineOptions.color(it.color)
            polylineOptions.width(it.width)
            polylineOptions.geodesic(it.geodesic)
        }
        return polylineOptions
    }

    private fun createPolygonOptions(polygon: Polygon, style: PolygonStyle?): PolygonOptions {
        val polygonOptions = PolygonOptions()

        polygon.outerBoundary.map { LatLng(it.lat, it.lng) }.forEach { polygonOptions.add(it) }
        polygon.innerBoundaries.forEach { innerBoundary ->
            polygonOptions.addHole(innerBoundary.map { LatLng(it.lat, it.lng) })
        }
        style?.let {
            polygonOptions.fillColor(it.fillColor)
            polygonOptions.strokeColor(it.strokeColor)
            polygonOptions.strokeWidth(it.strokeWidth)
            polygonOptions.geodesic(it.geodesic)
        }
        return polygonOptions
    }

    private fun hueFromColor(color: Int): Float {
        val hsv = FloatArray(3)
        android.graphics.Color.colorToHSV(color, hsv)
        return hsv[0]
    }
}
