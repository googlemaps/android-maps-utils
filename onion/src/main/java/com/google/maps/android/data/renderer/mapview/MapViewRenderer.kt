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
package com.google.maps.android.data.renderer.mapview

import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.AdvancedMarkerOptions
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.GroundOverlayOptions
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PinConfig
import com.google.android.gms.maps.model.PolygonOptions
import com.google.android.gms.maps.model.PolylineOptions
import com.google.maps.android.data.renderer.DataRenderer
import com.google.maps.android.data.renderer.IconProvider
import com.google.maps.android.data.renderer.model.DataLayer
import com.google.maps.android.data.renderer.model.DataScene
import com.google.maps.android.data.renderer.model.Feature
import com.google.maps.android.data.renderer.model.GroundOverlay
import com.google.maps.android.data.renderer.model.GroundOverlayStyle
import com.google.maps.android.data.renderer.model.LineString
import com.google.maps.android.data.renderer.model.LineStyle
import com.google.maps.android.data.renderer.model.MultiGeometry
import com.google.maps.android.data.renderer.model.Point
import com.google.maps.android.data.renderer.model.PointGeometry
import com.google.maps.android.data.renderer.model.PointStyle
import com.google.maps.android.data.renderer.model.Polygon
import com.google.maps.android.data.renderer.model.PolygonStyle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import java.util.IdentityHashMap

/**
 * A concrete implementation of the [DataRenderer] interface that renders a [DataScene] onto a [GoogleMap].
 *
 * This class is responsible for translating the platform-agnostic [DataScene] and [Feature] models
 * into specific Google Maps SDK objects (e.g., Markers, Polylines, Polygons).
 *
 * @property map The [GoogleMap] instance to render features on.
 */
class MapViewRenderer(
    private val map: GoogleMap,
    private val iconProvider: IconProvider
) : DataRenderer {

    // Scope for all coroutines launched by this renderer.
    // Using SupervisorJob so that failure of one icon load doesn't cancel others.
    private val rendererScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    /**
     * Controls whether to use the new Advanced Markers API (if available) or legacy Markers.
     * Default is false (legacy Markers).
     */
    var useAdvancedMarkers: Boolean = false

    // Cache for local images (e.g. from KMZ files)
    private val localImages = mutableMapOf<String, android.graphics.Bitmap>()

    /**
     * Caches an image for a specific URL.
     * This is useful for KMZ files or other scenarios where images are loaded locally
     * and should be used instead of fetching from the network.
     *
     * @param url The URL associated with the image.
     * @param bitmap The bitmap to cache.
     */
    fun cacheImageData(url: String, bitmap: android.graphics.Bitmap) {
        localImages[url] = bitmap
    }

    // Track rendered map objects for each feature so we can remove them later
    private val renderedFeatures = IdentityHashMap<Feature, MutableList<Any>>()

    override fun render(scene: DataScene) {
        scene.layers.forEach { renderLayer(it) }
    }

    override fun addLayer(layer: DataLayer) {
        renderLayer(layer)
    }

    override fun removeLayer(layer: DataLayer) {
        layer.features.forEach { removeFeature(it) }
    }

    private fun renderLayer(layer: DataLayer) {
        layer.features.forEach { feature ->
            addFeature(feature)
        }
    }

    override fun addFeature(feature: Feature) {
        val mapObjects = mutableListOf<Any>()
        when (feature.geometry) {
            is PointGeometry -> {
                val point = feature.geometry.point
                val style = feature.style as? PointStyle
                if (useAdvancedMarkers) {
                    val markerOptions = createAdvancedMarkerOptions(point, style)
                    val marker = map.addMarker(markerOptions)!!
                    mapObjects.add(marker)
                    style?.iconUrl?.let { url ->
                        val localBitmap = localImages[url]
                        if (localBitmap != null) {
                            marker.setIcon(BitmapDescriptorFactory.fromBitmap(localBitmap))
                        } else {
                            rendererScope.launch {
                                val bitmap = iconProvider.loadIcon(url)
                                if (bitmap != null) {
                                    try {
                                        marker.setIcon(BitmapDescriptorFactory.fromBitmap(bitmap))
                                    } catch (e: Exception) {
                                        // Marker might have been removed or other issue
                                    }
                                }
                            }
                        }
                    }
                } else {
                    val markerOptions = createMarkerOptions(point, style)
                    val marker = map.addMarker(markerOptions)!!
                    mapObjects.add(marker)
                    style?.iconUrl?.let { url ->
                        val localBitmap = localImages[url]
                        if (localBitmap != null) {
                            marker.setIcon(BitmapDescriptorFactory.fromBitmap(localBitmap))
                        } else {
                            rendererScope.launch {
                                val bitmap = iconProvider.loadIcon(url)
                                if (bitmap != null) {
                                    try {
                                        marker.setIcon(BitmapDescriptorFactory.fromBitmap(bitmap))
                                    } catch (e: Exception) {
                                        // Marker might have been removed
                                    }
                                }
                            }
                        }
                    }
                }
            }
            is LineString -> {
                val lineString = feature.geometry
                val style = feature.style as? LineStyle
                val polylineOptions = createPolylineOptions(lineString, style)
                mapObjects.add(map.addPolyline(polylineOptions))
            }
            is Polygon -> {
                val polygon = feature.geometry
                val style = feature.style as? PolygonStyle
                val polygonOptions = createPolygonOptions(polygon, style)
                mapObjects.add(map.addPolygon(polygonOptions))
            }
            is MultiGeometry -> {
                feature.geometry.geometries.forEach { geometry ->
                    // Recursively add each geometry in the MultiGeometry
                    addFeature(feature.copy(geometry = geometry))
                }
            }
            is GroundOverlay -> {
                val groundOverlay = feature.geometry
                val style = feature.style as? GroundOverlayStyle
                val options = createGroundOverlayOptions(groundOverlay, style)
                
                style?.iconUrl?.let { url ->
                    val localBitmap = localImages[url]
                    if (localBitmap != null) {
                        options.image(BitmapDescriptorFactory.fromBitmap(localBitmap))
                        val mapOverlay = map.addGroundOverlay(options)
                        if (mapOverlay != null) {
                            mapObjects.add(mapOverlay)
                        }
                    } else {
                        // GroundOverlay requires an image to be set immediately.
                        // Use a transparent placeholder.
                        options.image(BitmapDescriptorFactory.fromBitmap(TRANSPARENT_BITMAP))
                        options.visible(false) // Hide until loaded
                        
                        val mapOverlay = map.addGroundOverlay(options)
                        if (mapOverlay != null) {
                            mapObjects.add(mapOverlay)
                            rendererScope.launch {
                                val bitmap = iconProvider.loadIcon(url)
                                if (bitmap != null) {
                                    try {
                                        mapOverlay.setImage(BitmapDescriptorFactory.fromBitmap(bitmap))
                                        mapOverlay.isVisible = style?.visibility ?: true // Restore visibility
                                    } catch (e: Exception) {
                                        // Overlay might be removed
                                    }
                                }
                            }
                        }
                    }
                } ?: run { // No iconUrl, add directly
                    val mapOverlay = map.addGroundOverlay(options)
                    if (mapOverlay != null) {
                        mapObjects.add(mapOverlay)
                    }
                }
            }
        }
        if (mapObjects.isNotEmpty()) {
            renderedFeatures[feature] = mapObjects
        }
    }

    override fun removeFeature(feature: Feature) {
        renderedFeatures[feature]?.forEach { mapObject ->
            when (mapObject) {
                is com.google.android.gms.maps.model.Marker -> mapObject.remove()
                is com.google.android.gms.maps.model.Polyline -> mapObject.remove()
                is com.google.android.gms.maps.model.Polygon -> mapObject.remove()
                is com.google.android.gms.maps.model.GroundOverlay -> mapObject.remove()
            }
        }
        renderedFeatures.remove(feature)
    }

    override fun clear() {
        rendererScope.cancel()
        renderedFeatures.values.flatten().forEach { mapObject ->
            when (mapObject) {
                is com.google.android.gms.maps.model.Marker -> mapObject.remove()
                is com.google.android.gms.maps.model.Polyline -> mapObject.remove()
                is com.google.android.gms.maps.model.Polygon -> mapObject.remove()
                is com.google.android.gms.maps.model.GroundOverlay -> mapObject.remove()
            }
        }
        renderedFeatures.clear()
    }

    private fun createMarkerOptions(point: Point, style: PointStyle?): MarkerOptions {
        val markerOptions = MarkerOptions().position(LatLng(point.lat, point.lng))
        style?.let {
            // Default icon if not loaded yet
            markerOptions.icon(BitmapDescriptorFactory.defaultMarker(hueFromColor(it.color)))
            it.heading?.let { heading -> markerOptions.rotation(heading) }
            markerOptions.anchor(it.anchorU, it.anchorV)
            markerOptions.alpha(android.graphics.Color.alpha(it.color) / 255.0f)
        }
        return markerOptions
    }

    private fun createAdvancedMarkerOptions(point: Point, style: PointStyle?): AdvancedMarkerOptions {
        val markerOptions = AdvancedMarkerOptions().position(LatLng(point.lat, point.lng))
        style?.let {
            // Default pin if not loaded yet
            val pinConfig = PinConfig.builder()
                .setBackgroundColor(it.color)
                .setBorderColor(android.graphics.Color.WHITE) // Default border
                .build()
            markerOptions.icon(BitmapDescriptorFactory.fromPinConfig(pinConfig))
            
            // AdvancedMarkerOptions does not support rotation directly in the same way as MarkerOptions for flat icons,
            // but it supports collision behavior etc.
            // Rotation is not directly exposed on AdvancedMarkerOptions builder in the same way, or requires View.
            // For now, we skip rotation for Advanced Markers in this basic implementation.
            
            // Alpha is also not directly on AdvancedMarkerOptions builder, it's on the Marker object.
            // We can't set it here easily without creating the marker first.
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

    private fun createGroundOverlayOptions(groundOverlay: GroundOverlay, style: GroundOverlayStyle?): GroundOverlayOptions {
        val options = GroundOverlayOptions()
        options.positionFromBounds(groundOverlay.latLngBounds)
        options.bearing(groundOverlay.rotation)
        style?.let {
            options.zIndex(it.zIndex)
            options.transparency(it.transparency)
            options.visible(it.visibility)
        }
        return options
    }

    private fun hueFromColor(color: Int): Float {
        val hsv = FloatArray(3)
        android.graphics.Color.colorToHSV(color, hsv)
        return hsv[0]
    }

    companion object {
        private val TRANSPARENT_BITMAP by lazy {
            android.graphics.Bitmap.createBitmap(1, 1, android.graphics.Bitmap.Config.ARGB_8888)
        }
    }
}