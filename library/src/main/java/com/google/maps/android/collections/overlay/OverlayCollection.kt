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

package com.google.maps.android.collections.overlay

import android.view.View
import com.google.android.gms.maps.model.AdvancedMarkerOptions
import com.google.android.gms.maps.model.CircleOptions
import com.google.android.gms.maps.model.GroundOverlayOptions
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolygonOptions
import com.google.android.gms.maps.model.PolylineOptions
import java.util.Collections
import kotlin.jvm.JvmName

/**
 * A logical grouping / feature layer of heterogeneous [MapOverlay] instances on a map.
 *
 * Allows batch lifecycle operations (show, hide, clear, remove) and unified `add(...)` methods across
 * markers, circles, polygons, polylines, and ground overlays.
 */
public class OverlayCollection internal constructor(public val id: String?, private val manager: OverlayManager) {
    private val mOverlays: MutableSet<MapOverlay<*>> = Collections.synchronizedSet(LinkedHashSet())

    // Direct typed lookups
    public val overlays: Set<MapOverlay<*>>
        get() = synchronized(mOverlays) { HashSet(mOverlays) }

    public val markers: List<MarkerOverlay>
        get() = synchronized(mOverlays) { mOverlays.filterIsInstance<MarkerOverlay>() }

    public val circles: List<CircleOverlay>
        get() = synchronized(mOverlays) { mOverlays.filterIsInstance<CircleOverlay>() }

    public val polygons: List<PolygonOverlay>
        get() = synchronized(mOverlays) { mOverlays.filterIsInstance<PolygonOverlay>() }

    public val polylines: List<PolylineOverlay>
        get() = synchronized(mOverlays) { mOverlays.filterIsInstance<PolylineOverlay>() }

    public val groundOverlays: List<GroundOverlayOverlay>
        get() = synchronized(mOverlays) { mOverlays.filterIsInstance<GroundOverlayOverlay>() }

    public val size: Int
        get() = mOverlays.size

    public val isEmpty: Boolean
        get() = mOverlays.isEmpty()

    /**
     * Controls the visibility of all overlays in this collection.
     */
    public var isVisible: Boolean = true
        set(value) {
            field = value
            synchronized(mOverlays) {
                for (overlay in mOverlays) {
                    overlay.isVisible = value
                }
            }
        }

    public fun showAll() {
        isVisible = true
    }

    public fun hideAll() {
        isVisible = false
    }

    // Collection-wide event listeners
    internal var markerClickListener: ((MarkerOverlay) -> Boolean)? = null
    internal var circleClickListener: ((CircleOverlay) -> Unit)? = null
    internal var polygonClickListener: ((PolygonOverlay) -> Unit)? = null
    internal var polylineClickListener: ((PolylineOverlay) -> Unit)? = null
    internal var groundOverlayClickListener: ((GroundOverlayOverlay) -> Unit)? = null
    internal var infoWindowClickListener: ((MarkerOverlay) -> Unit)? = null
    internal var infoWindowLongClickListener: ((MarkerOverlay) -> Unit)? = null
    internal var infoWindowProvider: ((MarkerOverlay) -> View?)? = null
    internal var infoContentsProvider: ((MarkerOverlay) -> View?)? = null

    // --- Overloaded add methods ---

    public fun add(options: MarkerOptions): MarkerOverlay =
        manager.createMarker(options, this).also { mOverlays.add(it) }

    public fun add(options: AdvancedMarkerOptions): MarkerOverlay =
        manager.createAdvancedMarker(options, this).also { mOverlays.add(it) }

    public fun add(options: CircleOptions): CircleOverlay =
        manager.createCircle(options, this).also { mOverlays.add(it) }

    public fun add(options: PolygonOptions): PolygonOverlay =
        manager.createPolygon(options, this).also { mOverlays.add(it) }

    public fun add(options: PolylineOptions): PolylineOverlay =
        manager.createPolyline(options, this).also { mOverlays.add(it) }

    public fun add(options: GroundOverlayOptions): GroundOverlayOverlay =
        manager.createGroundOverlay(options, this).also { mOverlays.add(it) }

    // --- Operator overloads ---

    public operator fun plusAssign(options: MarkerOptions) {
        add(options)
    }

    public operator fun plusAssign(options: AdvancedMarkerOptions) {
        add(options)
    }

    public operator fun plusAssign(options: CircleOptions) {
        add(options)
    }

    public operator fun plusAssign(options: PolygonOptions) {
        add(options)
    }

    public operator fun plusAssign(options: PolylineOptions) {
        add(options)
    }

    public operator fun plusAssign(options: GroundOverlayOptions) {
        add(options)
    }

    // --- Batch addAll methods ---

    @JvmName("addAllMarkers")
    public fun addAll(optionsList: Collection<MarkerOptions>): List<MarkerOverlay> = optionsList.map { add(it) }

    @JvmName("addAllAdvancedMarkers")
    public fun addAllAdvancedMarkers(optionsList: Collection<AdvancedMarkerOptions>): List<MarkerOverlay> =
        optionsList.map { add(it) }

    @JvmName("addAllCircles")
    public fun addAll(optionsList: Collection<CircleOptions>): List<CircleOverlay> = optionsList.map { add(it) }

    @JvmName("addAllPolygons")
    public fun addAll(optionsList: Collection<PolygonOptions>): List<PolygonOverlay> = optionsList.map { add(it) }

    @JvmName("addAllPolylines")
    public fun addAll(optionsList: Collection<PolylineOptions>): List<PolylineOverlay> = optionsList.map { add(it) }

    @JvmName("addAllGroundOverlays")
    public fun addAll(optionsList: Collection<GroundOverlayOptions>): List<GroundOverlayOverlay> =
        optionsList.map { add(it) }

    // --- Removal & Clearing ---

    public fun remove(overlay: MapOverlay<*>): Boolean {
        if (mOverlays.remove(overlay)) {
            manager.onOverlayRemoved(overlay)
            return true
        }
        return false
    }

    public fun clear() {
        val snapshot = synchronized(mOverlays) {
            val list = ArrayList(mOverlays)
            mOverlays.clear()
            list
        }
        for (overlay in snapshot) {
            manager.onOverlayRemoved(overlay)
        }
    }

    // --- Collection-level listener registration ---

    public fun onMarkerClick(listener: (MarkerOverlay) -> Boolean) {
        markerClickListener = listener
    }

    public fun onCircleClick(listener: (CircleOverlay) -> Unit) {
        circleClickListener = listener
    }

    public fun onPolygonClick(listener: (PolygonOverlay) -> Unit) {
        polygonClickListener = listener
    }

    public fun onPolylineClick(listener: (PolylineOverlay) -> Unit) {
        polylineClickListener = listener
    }

    public fun onGroundOverlayClick(listener: (GroundOverlayOverlay) -> Unit) {
        groundOverlayClickListener = listener
    }

    public fun onInfoWindowClick(listener: (MarkerOverlay) -> Unit) {
        infoWindowClickListener = listener
    }

    public fun onInfoWindowLongClick(listener: (MarkerOverlay) -> Unit) {
        infoWindowLongClickListener = listener
    }

    public fun setCustomInfoWindow(
        infoWindow: ((MarkerOverlay) -> View?)? = null,
        infoContents: ((MarkerOverlay) -> View?)? = null,
    ) {
        infoWindowProvider = infoWindow
        infoContentsProvider = infoContents
    }
}
