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
import kotlin.collections.Collection as KotlinCollection

/**
 * Keeps track of collections of markers on the map. Delegates all Marker-related events to each
 * collection's individually managed listeners.
 *
 * All marker operations (adds and removes) should occur via its collection class. That is, don't
 * add a marker via a collection, then remove it via Marker.remove()
 */
public open class MarkerManager(map: GoogleMap) :
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

    public override fun newCollection(): Collection = Collection()

    public override fun getInfoWindow(marker: Marker): View? =
        mAllObjects[marker]?.mInfoWindowAdapter?.getInfoWindow(marker)

    public override fun getInfoContents(marker: Marker): View? =
        mAllObjects[marker]?.mInfoWindowAdapter?.getInfoContents(marker)

    public override fun onInfoWindowClick(marker: Marker) {
        mAllObjects[marker]?.mInfoWindowClickListener?.onInfoWindowClick(marker)
    }

    public override fun onInfoWindowLongClick(marker: Marker) {
        mAllObjects[marker]?.mInfoWindowLongClickListener?.onInfoWindowLongClick(marker)
    }

    public override fun onMarkerClick(marker: Marker): Boolean =
        mAllObjects[marker]?.mMarkerClickListener?.onMarkerClick(marker) ?: false

    public override fun onMarkerDragStart(marker: Marker) {
        mAllObjects[marker]?.mMarkerDragListener?.onMarkerDragStart(marker)
    }

    public override fun onMarkerDrag(marker: Marker) {
        mAllObjects[marker]?.mMarkerDragListener?.onMarkerDrag(marker)
    }

    public override fun onMarkerDragEnd(marker: Marker) {
        mAllObjects[marker]?.mMarkerDragListener?.onMarkerDragEnd(marker)
    }

    public override fun removeObjectFromMap(marker: Marker) {
        marker.remove()
    }

    public override fun setVisible(mapObject: Marker, visible: Boolean) {
        mapObject.isVisible = visible
    }

    /** A collection of [Marker]s on the map with its own set of listeners. */
    public open inner class Collection : MapObjectManager<Marker, Collection>.Collection() {
        internal var mInfoWindowClickListener: GoogleMap.OnInfoWindowClickListener? = null
        internal var mInfoWindowLongClickListener: GoogleMap.OnInfoWindowLongClickListener? = null
        internal var mMarkerClickListener: GoogleMap.OnMarkerClickListener? = null
        internal var mMarkerDragListener: GoogleMap.OnMarkerDragListener? = null
        internal var mInfoWindowAdapter: GoogleMap.InfoWindowAdapter? = null

        public open fun addMarker(opts: MarkerOptions): Marker =
            checkAndAdd(mMap.addMarker(opts), "Marker")

        public open fun addMarker(opts: AdvancedMarkerOptions): Marker =
            checkAndAdd(mMap.addMarker(opts), "AdvancedMarker")

        public open fun addAll(opts: KotlinCollection<MarkerOptions>): Unit =
            addAll(opts, ::addMarker)

        public open fun addAll(opts: KotlinCollection<MarkerOptions>, defaultVisible: Boolean): Unit =
            addAll(opts, defaultVisible, ::addMarker)

        public open fun getMarkers(): KotlinCollection<Marker> = getObjects()

        public open fun setOnInfoWindowClickListener(infoWindowClickListener: GoogleMap.OnInfoWindowClickListener?) {
            mInfoWindowClickListener = infoWindowClickListener
        }

        public open fun setOnInfoWindowLongClickListener(infoWindowLongClickListener: GoogleMap.OnInfoWindowLongClickListener?) {
            mInfoWindowLongClickListener = infoWindowLongClickListener
        }

        public open fun setOnMarkerClickListener(markerClickListener: GoogleMap.OnMarkerClickListener?) {
            mMarkerClickListener = markerClickListener
        }

        public open fun setOnMarkerDragListener(markerDragListener: GoogleMap.OnMarkerDragListener?) {
            mMarkerDragListener = markerDragListener
        }

        public open fun setInfoWindowAdapter(infoWindowAdapter: GoogleMap.InfoWindowAdapter?) {
            mInfoWindowAdapter = infoWindowAdapter
        }
    }
}
