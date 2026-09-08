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
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
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

    override fun setVisible(mapObject: Polygon, visible: Boolean) {
        mapObject.isVisible = visible
    }

    override fun onPolygonClick(polygon: Polygon) {
        mAllObjects[polygon]?.mPolygonClickListener?.onPolygonClick(polygon)
    }

    /** A collection of [Polygon]s on the map with its own set of listeners. */
    open inner class Collection : MapObjectManager<Polygon, Collection>.Collection() {
        internal var mPolygonClickListener: GoogleMap.OnPolygonClickListener? = null

        open fun addPolygon(opts: PolygonOptions): Polygon =
            checkAndAdd(mMap.addPolygon(opts), "Polygon")

        open fun addAll(opts: KotlinCollection<PolygonOptions>) =
            addAll(opts, ::addPolygon)

        open fun addAll(opts: KotlinCollection<PolygonOptions>, defaultVisible: Boolean) =
            addAll(opts, defaultVisible, ::addPolygon)

        open fun getPolygons(): KotlinCollection<Polygon> = getObjects()

        open fun setOnPolygonClickListener(polygonClickListener: GoogleMap.OnPolygonClickListener?) {
            mPolygonClickListener = polygonClickListener
        }
    }
}

/**
 * Adds a new [Polygon] to the underlying map and to this [PolygonManager.Collection] with the
 * provided [optionsActions].
 */
public inline fun PolygonManager.Collection.addPolygon(
    optionsActions: PolygonOptions.() -> Unit
): Polygon =
    this.addPolygon(
        PolygonOptions().apply(optionsActions)
    )

/**
 * Returns a flow that emits when a polygon in this collection is clicked. Using this to observe polygon clicks
 * will override an existing listener (if any) to [PolygonManager.Collection.setOnPolygonClickListener].
 *
 * **Warning**: This is a cold flow wrapping a single-listener SDK callback. Concurrently subscribing
 * multiple collectors will result in listener hijacking, and cancelling any observer will unregister
 * the active listener completely. Always share this flow (e.g. using [kotlinx.coroutines.flow.shareIn])
 * for multi-observer configurations.
 */
public fun PolygonManager.Collection.clickEvents(): Flow<Polygon> =
    callbackFlow {
        setOnPolygonClickListener {
            trySend(it).isSuccess
        }
        awaitClose {
            setOnPolygonClickListener(null)
        }
    }
