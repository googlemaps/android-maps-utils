/*
 * Copyright 2026 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.maps.android.renderer

import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.CircleOptions
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolygonOptions
import com.google.android.gms.maps.model.PolylineOptions
import com.google.maps.android.renderer.model.Circle
import com.google.maps.android.renderer.model.Layer
import com.google.maps.android.renderer.model.MapObject
import com.google.maps.android.renderer.model.Marker
import com.google.maps.android.renderer.model.Polygon
import com.google.maps.android.renderer.model.Polyline

/**
 * A [Renderer] implementation that draws to a [GoogleMap].
 */
class GoogleMapRenderer(private val map: GoogleMap) : Renderer {
    private val layers = mutableSetOf<Layer>()
    // Map to keep track of the GoogleMap SDK objects created for each MapObject
    private val renderedObjects = mutableMapOf<MapObject, Any>()

    override fun addLayer(layer: Layer) {
        if (layers.add(layer)) {
            layer.mapObjects.forEach { renderObject(it) }
        }
    }

    override fun removeLayer(layer: Layer): Boolean {
        if (layers.remove(layer)) {
            layer.mapObjects.forEach { removeRenderedObject(it) }
            return true
        }
        return false
    }

    override fun getLayers(): Collection<Layer> {
        return layers
    }

    override fun clear() {
        layers.forEach { layer ->
            layer.mapObjects.forEach { removeRenderedObject(it) }
        }
        layers.clear()
    }

    private fun renderObject(mapObject: MapObject) {
        if (!mapObject.isVisible) {
            return
        }

        when (mapObject.type) {
            MapObject.Type.MARKER -> renderMarker(mapObject as Marker)
            MapObject.Type.POLYLINE -> renderPolyline(mapObject as Polyline)
            MapObject.Type.POLYGON -> renderPolygon(mapObject as Polygon)
            MapObject.Type.CIRCLE -> renderCircle(mapObject as Circle)
        }
    }

    private fun removeRenderedObject(mapObject: MapObject) {
        val sdkObject = renderedObjects.remove(mapObject)
        when (sdkObject) {
            is com.google.android.gms.maps.model.Marker -> sdkObject.remove()
            is com.google.android.gms.maps.model.Polyline -> sdkObject.remove()
            is com.google.android.gms.maps.model.Polygon -> sdkObject.remove()
            is com.google.android.gms.maps.model.Circle -> sdkObject.remove()
        }
    }

    private fun renderMarker(marker: Marker) {
        val options = MarkerOptions().apply {
            position(marker.position)
            title(marker.title)
            snippet(marker.snippet)
            icon(marker.icon)
            alpha(marker.alpha)
            anchor(marker.anchorU, marker.anchorV)
            rotation(marker.rotation)
            flat(marker.flat)
            draggable(marker.draggable)
            visible(marker.isVisible)
            zIndex(marker.zIndex)
        }
        map.addMarker(options)?.let {
            renderedObjects[marker] = it
        }
    }

    private fun renderPolyline(polyline: Polyline) {
        val options = PolylineOptions().apply {
            addAll(polyline.points)
            width(polyline.width)
            color(polyline.color)
            clickable(polyline.isClickable)
            geodesic(polyline.isGeodesic)
            visible(polyline.isVisible)
            zIndex(polyline.zIndex)
            startCap(polyline.startCap)
            endCap(polyline.endCap)
            jointType(polyline.jointType)
            polyline.pattern?.let { pattern(it) }
        }
        val sdkPolyline = map.addPolyline(options)
        renderedObjects[polyline] = sdkPolyline
    }

    private fun renderPolygon(polygon: Polygon) {
        val options = PolygonOptions().apply {
            addAll(polygon.points)
            strokeWidth(polygon.strokeWidth)
            strokeColor(polygon.strokeColor)
            fillColor(polygon.fillColor)
            clickable(polygon.isClickable)
            geodesic(polygon.isGeodesic)
            visible(polygon.isVisible)
            zIndex(polygon.zIndex)
            strokeJointType(polygon.strokeJointType)
            polygon.holes.forEach { addHole(it) }
            polygon.strokePattern?.let { strokePattern(it) }
        }
        val sdkPolygon = map.addPolygon(options)
        renderedObjects[polygon] = sdkPolygon
    }

    private fun renderCircle(circle: Circle) {
        val options = CircleOptions().apply {
            center(circle.center)
            radius(circle.radius)
            strokeWidth(circle.strokeWidth)
            strokeColor(circle.strokeColor)
            fillColor(circle.fillColor)
            clickable(circle.isClickable)
            visible(circle.isVisible)
            zIndex(circle.zIndex)
            circle.strokePattern?.let { strokePattern(it) }
        }
        val sdkCircle = map.addCircle(options)
        renderedObjects[circle] = sdkCircle
    }
}
