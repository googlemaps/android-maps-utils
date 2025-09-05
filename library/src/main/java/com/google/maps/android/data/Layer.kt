/*
 * Copyright 2025 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.google.maps.android.data

import com.google.android.gms.maps.GoogleMap
import com.google.maps.android.data.geojson.GeoJsonLineStringStyle
import com.google.maps.android.data.geojson.GeoJsonPointStyle
import com.google.maps.android.data.geojson.GeoJsonPolygonStyle
import com.google.maps.android.data.geojson.GeoJsonRenderer
import com.google.maps.android.data.kml.KmlContainer
import com.google.maps.android.data.kml.KmlGroundOverlay
import com.google.maps.android.data.kml.KmlRenderer

/**
 * An abstraction that shares the common properties of
 * [com.google.maps.android.data.kml.KmlLayer] and
 * [com.google.maps.android.data.geojson.GeoJsonLayer]
 */
abstract class Layer<T : Feature> {
    @JvmField
    protected var renderer: Renderer<T>? = null

    /**
     * Adds the KML data to the map
     */
    protected fun addKMLToMap() {
        val kmlRenderer = renderer as? KmlRenderer
            ?: throw UnsupportedOperationException("Stored renderer is not a KmlRenderer")
        kmlRenderer.addLayerToMap()
    }

    /**
     * Adds GeoJson data to the map
     */
    protected fun addGeoJsonToMap() {
        val geoJsonRenderer = renderer as? GeoJsonRenderer
            ?: throw UnsupportedOperationException("Stored renderer is not a GeoJsonRenderer")
        geoJsonRenderer.addLayerToMap()
    }

    abstract fun addLayerToMap()

    /**
     * Removes all the data from the map and clears all the stored placemarks
     */
    fun removeLayerFromMap() {
        when (val r = renderer) {
            is GeoJsonRenderer -> r.removeLayerFromMap()
            is KmlRenderer -> r.removeLayerFromMap()
        }
    }

    /**
     * Sets a single click listener for the entire GoogleMap object, that will be called
     * with the corresponding Feature object when an object on the map (Polygon,
     * Marker, Polyline) is clicked.
     *
     * If getFeature() returns null this means that either the object is inside a KMLContainer,
     * or the object is a MultiPolygon, MultiLineString or MultiPoint and must
     * be handled differently.
     *
     * @param listener Listener providing the onFeatureClick method to call.
     */
    fun setOnFeatureClickListener(listener: OnFeatureClickListener) {
        renderer?.setOnFeatureClickListener(listener)
    }

    /**
     * Callback interface for when a map object is clicked.
     */
    fun interface OnFeatureClickListener {
        fun onFeatureClick(feature: Feature)
    }

    /**
     * Stores a new Renderer object into mRenderer
     *
     * @param renderer the new Renderer object that belongs to this Layer
     */
    protected fun storeRenderer(renderer: Renderer<T>) {
        this.renderer = renderer
    }

    /**
     * Gets an iterable of all Feature elements that have been added to the layer
     *
     * @return iterable of Feature elements
     */
    open fun getFeatures(): Iterable<T> {
        return renderer?.features ?: emptyList()
    }

    /**
     * Retrieves a corresponding Feature instance for the given Object
     * Allows maps with multiple layers to determine which layer the Object
     * belongs to.
     *
     * @param mapObject Object
     * @return Feature for the given object
     */
    fun getFeature(mapObject: Any): T? {
        return renderer?.getFeature(mapObject) as T?
    }

    fun getContainerFeature(mapObject: Any): T? {
        return renderer?.getContainerFeature(mapObject) as T?
    }

    /**
     * Checks if there are any features currently on the layer
     *
     * @return true if there are features on the layer, false otherwise
     */
    protected open fun hasFeatures(): Boolean = renderer?.hasFeatures() ?: false

    /**
     * Checks if the layer contains any KmlContainers
     *
     * @return true if there is at least 1 container within the KmlLayer, false otherwise
     */
    protected open fun hasContainers(): Boolean {
        return (renderer as? KmlRenderer)?.hasNestedContainers() ?: false
    }

    /**
     * Gets an iterable of KmlContainerInterface objects
     *
     * @return iterable of KmlContainerInterface objects
     */
    protected open fun getContainers(): Iterable<KmlContainer>? {
        return (renderer as? KmlRenderer)?.nestedContainers
    }

    /**
     * Gets an iterable of KmlGroundOverlay objects
     *
     * @return iterable of KmlGroundOverlay objects
     */
    protected open fun getGroundOverlays(): Iterable<KmlGroundOverlay>? {
        return (renderer as? KmlRenderer)?.groundOverlays
    }

    /**
     * Gets the map on which the layer is rendered
     *
     * @return map on which the layer is rendered
     */
    val map: GoogleMap?
        get() = renderer?.map

    /**
     * Renders the layer on the given map. The layer on the current map is removed and
     * added to the given map.
     *
     * @param map to render the layer on, if null the layer is cleared from the current map
     */
    fun setMap(map: GoogleMap?) {
        renderer?.map = map
    }

    /**
     * Checks if the current layer has been added to the map
     *
     * @return true if the layer is on the map, false otherwise
     */
    val isLayerOnMap: Boolean
        get() = renderer?.isLayerOnMap ?: false

    /**
     * Adds a provided feature to the map
     *
     * @param feature feature to add to map
     */
    protected open fun addFeature(feature: T) {
        renderer?.addFeature(feature)
    }

    /**
     * Remove a specified feature from the map
     *
     * @param feature feature to be removed
     */
    protected open fun removeFeature(feature: T) {
        renderer?.removeFeature(feature)
    }

    /**
     * Gets the default style used to render GeoJsonPoints. Any changes to this style will be
     * reflected in the features that use it.
     *
     * @return default style used to render GeoJsonPoints
     */
    val defaultPointStyle: GeoJsonPointStyle?
        get() = renderer?.defaultPointStyle

    /**
     * Gets the default style used to render GeoJsonLineStrings. Any changes to this style will be
     * reflected in the features that use it.
     *
     * @return default style used to render GeoJsonLineStrings
     */
    val defaultLineStringStyle: GeoJsonLineStringStyle?
        get() = renderer?.defaultLineStringStyle

    /**
     * Gets the default style used to render GeoJsonPolygons. Any changes to this style will be
     * reflected in the features that use it.
     *
     * @return default style used to render GeoJsonPolygons
     */
    val defaultPolygonStyle: GeoJsonPolygonStyle?
        get() = renderer?.defaultPolygonStyle
}
