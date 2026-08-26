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
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.AdvancedMarkerOptions
import com.google.android.gms.maps.model.Circle
import com.google.android.gms.maps.model.CircleOptions
import com.google.android.gms.maps.model.GroundOverlay
import com.google.android.gms.maps.model.GroundOverlayOptions
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.Polygon
import com.google.android.gms.maps.model.PolygonOptions
import com.google.android.gms.maps.model.Polyline
import com.google.android.gms.maps.model.PolylineOptions
import com.google.maps.android.collections.CircleManager
import com.google.maps.android.collections.GroundOverlayManager
import com.google.maps.android.collections.MarkerManager
import com.google.maps.android.collections.PolygonManager
import com.google.maps.android.collections.PolylineManager
import java.util.Collections
import java.util.concurrent.ConcurrentHashMap

/**
 * Unified coordinator and factory for all map overlays on a [GoogleMap].
 *
 * This manager provides a single entry point for creating heterogeneous [OverlayCollection]s,
 * dispatching map interaction events, and supporting full coexistence with legacy collection managers
 * ([MarkerManager], [CircleManager], [PolygonManager], [PolylineManager], [GroundOverlayManager]).
 *
 * @param map The target [GoogleMap].
 * @param markerManager Optional legacy [MarkerManager] to share listeners with.
 * @param circleManager Optional legacy [CircleManager] to share listeners with.
 * @param polygonManager Optional legacy [PolygonManager] to share listeners with.
 * @param polylineManager Optional legacy [PolylineManager] to share listeners with.
 * @param groundOverlayManager Optional legacy [GroundOverlayManager] to share listeners with.
 */
class OverlayManager @JvmOverloads constructor(
    val map: GoogleMap,
    val markerManager: MarkerManager = MarkerManager(map),
    val circleManager: CircleManager = CircleManager(map),
    val polygonManager: PolygonManager = PolygonManager(map),
    val polylineManager: PolylineManager = PolylineManager(map),
    val groundOverlayManager: GroundOverlayManager = GroundOverlayManager(map),
) {
    private val mNamedCollections: MutableMap<String, OverlayCollection> = ConcurrentHashMap()
    private val mAnonymousCollections: MutableSet<OverlayCollection> = Collections.synchronizedSet(LinkedHashSet())

    // Internal maps from native GMS objects to their respective Overlay wrapper and collection
    private val mMarkerWrappers: MutableMap<Marker, Pair<MarkerOverlay, OverlayCollection>> = ConcurrentHashMap()
    private val mCircleWrappers: MutableMap<Circle, Pair<CircleOverlay, OverlayCollection>> = ConcurrentHashMap()
    private val mPolygonWrappers: MutableMap<Polygon, Pair<PolygonOverlay, OverlayCollection>> = ConcurrentHashMap()
    private val mPolylineWrappers: MutableMap<Polyline, Pair<PolylineOverlay, OverlayCollection>> = ConcurrentHashMap()
    private val mGroundOverlayWrappers: MutableMap<GroundOverlay, Pair<GroundOverlayOverlay, OverlayCollection>> = ConcurrentHashMap()

    // Legacy collections used for bridging event listeners
    private val mLegacyMarkerCollection = markerManager.newCollection()
    private val mLegacyCircleCollection = circleManager.newCollection()
    private val mLegacyPolygonCollection = polygonManager.newCollection()
    private val mLegacyPolylineCollection = polylineManager.newCollection()
    private val mLegacyGroundOverlayCollection = groundOverlayManager.newCollection()

    init {
        setupLegacyListeners()
    }

    private fun setupLegacyListeners() {
        mLegacyMarkerCollection.setOnMarkerClickListener { marker ->
            val pair = mMarkerWrappers[marker] ?: return@setOnMarkerClickListener false
            val overlay = pair.first
            val collection = pair.second

            // Check individual overlay listener first, then collection-level listener
            overlay.clickListener?.invoke(overlay) ?: collection.markerClickListener?.invoke(overlay) ?: false
        }

        mLegacyMarkerCollection.setOnInfoWindowClickListener { marker ->
            val pair = mMarkerWrappers[marker] ?: return@setOnInfoWindowClickListener
            val overlay = pair.first
            val collection = pair.second

            overlay.infoWindowClickListener?.invoke(overlay)
                ?: collection.infoWindowClickListener?.invoke(overlay)
        }

        mLegacyMarkerCollection.setOnInfoWindowLongClickListener { marker ->
            val pair = mMarkerWrappers[marker] ?: return@setOnInfoWindowLongClickListener
            val overlay = pair.first
            val collection = pair.second

            overlay.infoWindowLongClickListener?.invoke(overlay)
                ?: collection.infoWindowLongClickListener?.invoke(overlay)
        }

        mLegacyMarkerCollection.setOnMarkerDragListener(object : GoogleMap.OnMarkerDragListener {
            override fun onMarkerDragStart(marker: Marker) {
                mMarkerWrappers[marker]?.first?.let { it.dragStartListener?.invoke(it) }
            }

            override fun onMarkerDrag(marker: Marker) {
                mMarkerWrappers[marker]?.first?.let { it.dragListener?.invoke(it) }
            }

            override fun onMarkerDragEnd(marker: Marker) {
                mMarkerWrappers[marker]?.first?.let { it.dragEndListener?.invoke(it) }
            }
        })

        mLegacyMarkerCollection.setInfoWindowAdapter(object : GoogleMap.InfoWindowAdapter {
            override fun getInfoWindow(marker: Marker): View? {
                val pair = mMarkerWrappers[marker] ?: return null
                return pair.first.infoWindowProvider?.invoke(pair.first)
                    ?: pair.second.infoWindowProvider?.invoke(pair.first)
            }

            override fun getInfoContents(marker: Marker): View? {
                val pair = mMarkerWrappers[marker] ?: return null
                return pair.first.infoContentsProvider?.invoke(pair.first)
                    ?: pair.second.infoContentsProvider?.invoke(pair.first)
            }
        })

        mLegacyCircleCollection.setOnCircleClickListener { circle ->
            val pair = mCircleWrappers[circle] ?: return@setOnCircleClickListener
            pair.first.clickListener?.invoke(pair.first)
                ?: pair.second.circleClickListener?.invoke(pair.first)
        }

        mLegacyPolygonCollection.setOnPolygonClickListener { polygon ->
            val pair = mPolygonWrappers[polygon] ?: return@setOnPolygonClickListener
            pair.first.clickListener?.invoke(pair.first)
                ?: pair.second.polygonClickListener?.invoke(pair.first)
        }

        mLegacyPolylineCollection.setOnPolylineClickListener { polyline ->
            val pair = mPolylineWrappers[polyline] ?: return@setOnPolylineClickListener
            pair.first.clickListener?.invoke(pair.first)
                ?: pair.second.polylineClickListener?.invoke(pair.first)
        }

        mLegacyGroundOverlayCollection.setOnGroundOverlayClickListener { groundOverlay ->
            val pair = mGroundOverlayWrappers[groundOverlay] ?: return@setOnGroundOverlayClickListener
            pair.first.clickListener?.invoke(pair.first)
                ?: pair.second.groundOverlayClickListener?.invoke(pair.first)
        }
    }

    /**
     * Creates and registers a new [OverlayCollection].
     *
     * @param id Optional unique identifier for looking up the collection via [getCollection].
     * @return The newly created [OverlayCollection].
     */
    fun newCollection(id: String? = null): OverlayCollection {
        val collection = OverlayCollection(id, this)
        if (id != null) {
            require(mNamedCollections.putIfAbsent(id, collection) == null) {
                "OverlayCollection id is not unique: $id"
            }
        } else {
            mAnonymousCollections.add(collection)
        }
        return collection
    }

    /**
     * Retrieves a named collection by its [id].
     */
    fun getCollection(id: String): OverlayCollection? = mNamedCollections[id]

    /**
     * Removes and clears a named collection by its [id].
     */
    fun removeCollection(id: String): Boolean {
        val col = mNamedCollections.remove(id) ?: return false
        col.clear()
        return true
    }

    /**
     * Clears all overlays and collections managed by this instance.
     */
    fun clearAll() {
        for (col in mNamedCollections.values) {
            col.clear()
        }
        mNamedCollections.clear()

        for (col in mAnonymousCollections) {
            col.clear()
        }
        mAnonymousCollections.clear()
    }

    // --- Overlay creation helpers used by OverlayCollection ---

    internal fun createMarker(options: MarkerOptions, collection: OverlayCollection): MarkerOverlay {
        val marker = mLegacyMarkerCollection.addMarker(options)
        val wrapper = MarkerOverlay(marker) { overlay ->
            collection.remove(overlay)
        }
        mMarkerWrappers[marker] = Pair(wrapper, collection)
        return wrapper
    }

    internal fun createAdvancedMarker(options: AdvancedMarkerOptions, collection: OverlayCollection): MarkerOverlay {
        val marker = mLegacyMarkerCollection.addMarker(options)
        val wrapper = MarkerOverlay(marker) { overlay ->
            collection.remove(overlay)
        }
        mMarkerWrappers[marker] = Pair(wrapper, collection)
        return wrapper
    }

    internal fun createCircle(options: CircleOptions, collection: OverlayCollection): CircleOverlay {
        val circle = mLegacyCircleCollection.addCircle(options)
        val wrapper = CircleOverlay(circle) { overlay ->
            collection.remove(overlay)
        }
        mCircleWrappers[circle] = Pair(wrapper, collection)
        return wrapper
    }

    internal fun createPolygon(options: PolygonOptions, collection: OverlayCollection): PolygonOverlay {
        val polygon = mLegacyPolygonCollection.addPolygon(options)
        val wrapper = PolygonOverlay(polygon) { overlay ->
            collection.remove(overlay)
        }
        mPolygonWrappers[polygon] = Pair(wrapper, collection)
        return wrapper
    }

    internal fun createPolyline(options: PolylineOptions, collection: OverlayCollection): PolylineOverlay {
        val polyline = mLegacyPolylineCollection.addPolyline(options)
        val wrapper = PolylineOverlay(polyline) { overlay ->
            collection.remove(overlay)
        }
        mPolylineWrappers[polyline] = Pair(wrapper, collection)
        return wrapper
    }

    internal fun createGroundOverlay(options: GroundOverlayOptions, collection: OverlayCollection): GroundOverlayOverlay {
        val groundOverlay = mLegacyGroundOverlayCollection.addGroundOverlay(options)
        val wrapper = GroundOverlayOverlay(groundOverlay) { overlay ->
            collection.remove(overlay)
        }
        mGroundOverlayWrappers[groundOverlay] = Pair(wrapper, collection)
        return wrapper
    }

    internal fun onOverlayRemoved(overlay: MapOverlay<*>) {
        when (overlay) {
            is MarkerOverlay -> {
                mMarkerWrappers.remove(overlay.native)
                mLegacyMarkerCollection.remove(overlay.native)
            }
            is CircleOverlay -> {
                mCircleWrappers.remove(overlay.native)
                mLegacyCircleCollection.remove(overlay.native)
            }
            is PolygonOverlay -> {
                mPolygonWrappers.remove(overlay.native)
                mLegacyPolygonCollection.remove(overlay.native)
            }
            is PolylineOverlay -> {
                mPolylineWrappers.remove(overlay.native)
                mLegacyPolylineCollection.remove(overlay.native)
            }
            is GroundOverlayOverlay -> {
                mGroundOverlayWrappers.remove(overlay.native)
                mLegacyGroundOverlayCollection.remove(overlay.native)
            }
        }
    }
}
