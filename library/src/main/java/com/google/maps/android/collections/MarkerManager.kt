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
package com.google.maps.android.collections

import android.view.View
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.AdvancedMarkerOptions
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlin.collections.Collection as KotlinCollection

/**
 * Keeps track of collections of markers on the map. Delegates all Marker-related events to each
 * collection's individually managed listeners.
 *
 * All marker operations (adds and removes) should occur via its collection class. That is, don't
 * add a marker via a collection, then remove it via Marker.remove()
 */
open class MarkerManager(map: GoogleMap) :
    MapObjectManager<Marker, MarkerManager.Collection>(map),
    GoogleMap.OnInfoWindowClickListener,
    GoogleMap.OnMarkerClickListener,
    GoogleMap.OnMarkerDragListener,
    GoogleMap.InfoWindowAdapter,
    GoogleMap.OnInfoWindowLongClickListener {

    override fun setListenersOnUiThread() {
        mMap.setOnInfoWindowClickListener(this)
        mMap.setOnInfoWindowLongClickListener(this)
        mMap.setOnMarkerClickListener(this)
        mMap.setOnMarkerDragListener(this)
        mMap.setInfoWindowAdapter(this)
    }

    override fun newCollection(): Collection = Collection()

    override fun getInfoWindow(marker: Marker): View? =
        mAllObjects[marker]?.mInfoWindowAdapter?.getInfoWindow(marker)

    override fun getInfoContents(marker: Marker): View? =
        mAllObjects[marker]?.mInfoWindowAdapter?.getInfoContents(marker)

    override fun onInfoWindowClick(marker: Marker) {
        mAllObjects[marker]?.mInfoWindowClickListener?.onInfoWindowClick(marker)
    }

    override fun onInfoWindowLongClick(marker: Marker) {
        mAllObjects[marker]?.mInfoWindowLongClickListener?.onInfoWindowLongClick(marker)
    }

    override fun onMarkerClick(marker: Marker): Boolean =
        mAllObjects[marker]?.mMarkerClickListener?.onMarkerClick(marker) ?: false

    override fun onMarkerDragStart(marker: Marker) {
        mAllObjects[marker]?.mMarkerDragListener?.onMarkerDragStart(marker)
    }

    override fun onMarkerDrag(marker: Marker) {
        mAllObjects[marker]?.mMarkerDragListener?.onMarkerDrag(marker)
    }

    override fun onMarkerDragEnd(marker: Marker) {
        mAllObjects[marker]?.mMarkerDragListener?.onMarkerDragEnd(marker)
    }

    override fun removeObjectFromMap(marker: Marker) {
        marker.remove()
    }

    override fun setVisible(mapObject: Marker, visible: Boolean) {
        mapObject.isVisible = visible
    }

    /** A collection of [Marker]s on the map with its own set of listeners. */
    open inner class Collection : MapObjectManager<Marker, Collection>.Collection() {
        internal var mInfoWindowClickListener: GoogleMap.OnInfoWindowClickListener? = null
        internal var mInfoWindowLongClickListener: GoogleMap.OnInfoWindowLongClickListener? = null
        internal var mMarkerClickListener: GoogleMap.OnMarkerClickListener? = null
        internal var mMarkerDragListener: GoogleMap.OnMarkerDragListener? = null
        internal var mInfoWindowAdapter: GoogleMap.InfoWindowAdapter? = null

        open fun addMarker(opts: MarkerOptions): Marker =
            checkAndAdd(mMap.addMarker(opts), "Marker")

        open fun addMarker(opts: AdvancedMarkerOptions): Marker =
            checkAndAdd(mMap.addMarker(opts), "AdvancedMarker")

        open fun addAll(opts: KotlinCollection<MarkerOptions>) =
            addAll(opts, ::addMarker)

        open fun addAll(opts: KotlinCollection<MarkerOptions>, defaultVisible: Boolean) =
            addAll(opts, defaultVisible, ::addMarker)

        open fun getMarkers(): KotlinCollection<Marker> = getObjects()

        open fun setOnInfoWindowClickListener(infoWindowClickListener: GoogleMap.OnInfoWindowClickListener?) {
            mInfoWindowClickListener = infoWindowClickListener
        }

        open fun setOnInfoWindowLongClickListener(infoWindowLongClickListener: GoogleMap.OnInfoWindowLongClickListener?) {
            mInfoWindowLongClickListener = infoWindowLongClickListener
        }

        open fun setOnMarkerClickListener(markerClickListener: GoogleMap.OnMarkerClickListener?) {
            mMarkerClickListener = markerClickListener
        }

        open fun setOnMarkerDragListener(markerDragListener: GoogleMap.OnMarkerDragListener?) {
            mMarkerDragListener = markerDragListener
        }

        open fun setInfoWindowAdapter(infoWindowAdapter: GoogleMap.InfoWindowAdapter?) {
            mInfoWindowAdapter = infoWindowAdapter
        }
    }
}

/**
 * Adds a new [Marker] to the underlying map and to this [MarkerManager.Collection] with the
 * provided [optionsActions].
 */
public inline fun MarkerManager.Collection.addMarker(optionsActions: MarkerOptions.() -> Unit): Marker =
    this.addMarker(
        MarkerOptions().apply(optionsActions)
    )

/**
 * Returns a flow that emits when a marker in this collection is clicked. Using this to observe marker clicks
 * will override an existing listener (if any) to [MarkerManager.Collection.setOnMarkerClickListener].
 *
 * **Warning**: This is a cold flow wrapping a single-listener SDK callback. Concurrently subscribing
 * multiple collectors will result in listener hijacking, and cancelling any observer will unregister
 * the active listener completely. Always share this flow (e.g. using [kotlinx.coroutines.flow.shareIn])
 * for multi-observer configurations.
 */
public fun MarkerManager.Collection.clickEvents(): Flow<Marker> =
    callbackFlow {
        setOnMarkerClickListener {
            trySend(it).isSuccess
        }
        awaitClose {
            setOnMarkerClickListener(null)
        }
    }

/**
 * Returns a flow that emits when a marker's info window in this collection is clicked. Using this to observe info window clicks
 * will override an existing listener (if any) to [MarkerManager.Collection.setOnInfoWindowClickListener].
 *
 * **Warning**: This is a cold flow wrapping a single-listener SDK callback. Concurrently subscribing
 * multiple collectors will result in listener hijacking, and cancelling any observer will unregister
 * the active listener completely. Always share this flow (e.g. using [kotlinx.coroutines.flow.shareIn])
 * for multi-observer configurations.
 */
public fun MarkerManager.Collection.infoWindowClickEvents(): Flow<Marker> =
    callbackFlow {
        setOnInfoWindowClickListener {
            trySend(it).isSuccess
        }
        awaitClose {
            setOnInfoWindowClickListener(null)
        }
    }

/**
 * Returns a flow that emits when a marker's info window in this collection is long clicked. Using this to observe info window long clicks
 * will override an existing listener (if any) to [MarkerManager.Collection.setOnInfoWindowLongClickListener].
 *
 * **Warning**: This is a cold flow wrapping a single-listener SDK callback. Concurrently subscribing
 * multiple collectors will result in listener hijacking, and cancelling any observer will unregister
 * the active listener completely. Always share this flow (e.g. using [kotlinx.coroutines.flow.shareIn])
 * for multi-observer configurations.
 */
public fun MarkerManager.Collection.infoWindowLongClickEvents(): Flow<Marker> =
    callbackFlow {
        setOnInfoWindowLongClickListener {
            trySend(it).isSuccess
        }
        awaitClose {
            setOnInfoWindowLongClickListener(null)
        }
    }
