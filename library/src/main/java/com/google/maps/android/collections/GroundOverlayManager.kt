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
import com.google.android.gms.maps.model.GroundOverlay
import com.google.android.gms.maps.model.GroundOverlayOptions
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlin.collections.Collection as KotlinCollection

/**
 * Keeps track of collections of ground overlays on the map. Delegates all GroundOverlay-related
 * events to each collection's individually managed listeners.
 *
 * All ground overlay operations (adds and removes) should occur via its collection class. That
 * is, don't add a ground overlay via a collection, then remove it via GroundOverlay.remove()
 */
open class GroundOverlayManager(map: GoogleMap) :
    MapObjectManager<GroundOverlay, GroundOverlayManager.Collection>(map),
    GoogleMap.OnGroundOverlayClickListener {

    override fun setListenersOnUiThread() {
        mMap.setOnGroundOverlayClickListener(this)
    }

    override fun newCollection(): Collection = Collection()

    override fun removeObjectFromMap(groundOverlay: GroundOverlay) {
        groundOverlay.remove()
    }

    override fun setVisible(mapObject: GroundOverlay, visible: Boolean) {
        mapObject.isVisible = visible
    }

    override fun onGroundOverlayClick(groundOverlay: GroundOverlay) {
        mAllObjects[groundOverlay]?.mGroundOverlayClickListener?.onGroundOverlayClick(groundOverlay)
    }

    /** A collection of [GroundOverlay]s on the map with its own set of listeners. */
    open inner class Collection :
        MapObjectManager<GroundOverlay, Collection>.Collection() {
        internal var mGroundOverlayClickListener: GoogleMap.OnGroundOverlayClickListener? = null

        open fun addGroundOverlay(opts: GroundOverlayOptions): GroundOverlay =
            checkAndAdd(mMap.addGroundOverlay(opts), "GroundOverlay")

        open fun addAll(opts: KotlinCollection<GroundOverlayOptions>) =
            addAll(opts, ::addGroundOverlay)

        open fun addAll(opts: KotlinCollection<GroundOverlayOptions>, defaultVisible: Boolean) =
            addAll(opts, defaultVisible, ::addGroundOverlay)

        open fun getGroundOverlays(): KotlinCollection<GroundOverlay> = getObjects()

        open fun setOnGroundOverlayClickListener(
            groundOverlayClickListener: GoogleMap.OnGroundOverlayClickListener?,
        ) {
            mGroundOverlayClickListener = groundOverlayClickListener
        }
    }
}

/**
 * Adds a new [GroundOverlay] to the underlying map and to this [GroundOverlayManager.Collection]
 * with the provided [optionsActions].
 */
public inline fun GroundOverlayManager.Collection.addGroundOverlay(
    optionsActions: GroundOverlayOptions.() -> Unit
): GroundOverlay =
    this.addGroundOverlay(
        GroundOverlayOptions().apply(optionsActions)
    )

/**
 * Returns a flow that emits when a ground overlay in this collection is clicked. Using this to observe ground overlay clicks
 * will override an existing listener (if any) to [GroundOverlayManager.Collection.setOnGroundOverlayClickListener].
 *
 * **Warning**: This is a cold flow wrapping a single-listener SDK callback. Concurrently subscribing
 * multiple collectors will result in listener hijacking, and cancelling any observer will unregister
 * the active listener completely. Always share this flow (e.g. using [kotlinx.coroutines.flow.shareIn])
 * for multi-observer configurations.
 */
public fun GroundOverlayManager.Collection.clickEvents(): Flow<GroundOverlay> =
    callbackFlow {
        setOnGroundOverlayClickListener {
            trySend(it).isSuccess
        }
        awaitClose {
            setOnGroundOverlayClickListener(null)
        }
    }
