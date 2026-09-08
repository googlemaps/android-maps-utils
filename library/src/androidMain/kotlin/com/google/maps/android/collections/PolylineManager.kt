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

import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.Polyline
import com.google.android.gms.maps.model.PolylineOptions
import kotlin.collections.Collection as KotlinCollection

/**
 * Keeps track of collections of polylines on the map. Delegates all Polyline-related events to each
 * collection's individually managed listeners.
 *
 * All polyline operations (adds and removes) should occur via its collection class. That is,
 * don't add a polyline via a collection, then remove it via Polyline.remove()
 */
open class PolylineManager(map: GoogleMap) :
    MapObjectManager<Polyline, PolylineManager.Collection>(map),
    GoogleMap.OnPolylineClickListener {

    override fun setListenersOnUiThread() {
        mMap.setOnPolylineClickListener(this)
    }

    override fun newCollection(): Collection = Collection()

    override fun removeObjectFromMap(polyline: Polyline) {
        polyline.remove()
    }

    override fun setVisible(mapObject: Polyline, visible: Boolean) {
        mapObject.isVisible = visible
    }

    override fun onPolylineClick(polyline: Polyline) {
        mAllObjects[polyline]?.mPolylineClickListener?.onPolylineClick(polyline)
    }

    /** A collection of [Polyline]s on the map with its own set of listeners. */
    open inner class Collection : MapObjectManager<Polyline, Collection>.Collection() {
        internal var mPolylineClickListener: GoogleMap.OnPolylineClickListener? = null

        open fun addPolyline(opts: PolylineOptions): Polyline =
            checkAndAdd(mMap.addPolyline(opts), "Polyline")

        open fun addAll(opts: KotlinCollection<PolylineOptions>) =
            addAll(opts, ::addPolyline)

        open fun addAll(opts: KotlinCollection<PolylineOptions>, defaultVisible: Boolean) =
            addAll(opts, defaultVisible, ::addPolyline)

        open fun getPolylines(): KotlinCollection<Polyline> = getObjects()

        open fun setOnPolylineClickListener(
            polylineClickListener: GoogleMap.OnPolylineClickListener?,
        ) {
            mPolylineClickListener = polylineClickListener
        }
    }
}
