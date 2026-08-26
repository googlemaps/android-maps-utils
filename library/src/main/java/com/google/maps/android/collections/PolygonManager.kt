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
import com.google.android.gms.maps.model.Polygon
import com.google.android.gms.maps.model.PolygonOptions
import kotlin.collections.Collection as KotlinCollection

/**
 * Keeps track of collections of polygons on the map. Delegates all Polygon-related events to each
 * collection's individually managed listeners.
 *
 * All polygon operations (adds and removes) should occur via its collection class. That is,
 * don't add a polygon via a collection, then remove it via Polygon.remove()
 */
open class PolygonManager(map: GoogleMap) :
    MapObjectManager<Polygon, PolygonManager.Collection>(map),
    GoogleMap.OnPolygonClickListener {

    override fun setListenersOnUiThread() {
        mMap.setOnPolygonClickListener(this)
    }

    override fun newCollection(): Collection = Collection()

    override fun removeObjectFromMap(polygon: Polygon) {
        polygon.remove()
    }

    override fun onPolygonClick(polygon: Polygon) {
        mAllObjects[polygon]?.mPolygonClickListener?.onPolygonClick(polygon)
    }

    /** A collection of [Polygon]s on the map with its own set of listeners. */
    open inner class Collection : MapObjectManager<Polygon, Collection>.Collection() {
        internal var mPolygonClickListener: GoogleMap.OnPolygonClickListener? = null

        open fun addPolygon(opts: PolygonOptions): Polygon =
            mMap.addPolygon(opts).also { super.add(it) }

        open fun addAll(opts: KotlinCollection<PolygonOptions>) {
            for (opt in opts) {
                addPolygon(opt)
            }
        }

        open fun addAll(opts: KotlinCollection<PolygonOptions>, defaultVisible: Boolean) {
            for (opt in opts) {
                addPolygon(opt).isVisible = defaultVisible
            }
        }

        open fun showAll() {
            for (polygon in getPolygons()) {
                polygon.isVisible = true
            }
        }

        open fun hideAll() {
            for (polygon in getPolygons()) {
                polygon.isVisible = false
            }
        }

        override fun remove(polygon: Polygon?): Boolean = super.remove(polygon)

        open fun getPolygons(): KotlinCollection<Polygon> = getObjects()

        open fun setOnPolygonClickListener(polygonClickListener: GoogleMap.OnPolygonClickListener?) {
            mPolygonClickListener = polygonClickListener
        }
    }
}
