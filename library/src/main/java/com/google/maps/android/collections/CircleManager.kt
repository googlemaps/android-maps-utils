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
import com.google.android.gms.maps.model.Circle
import com.google.android.gms.maps.model.CircleOptions
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlin.collections.Collection as KotlinCollection

/**
 * Keeps track of collections of circles on the map. Delegates all Circle-related events to each
 * collection's individually managed listeners.
 *
 * All circle operations (adds and removes) should occur via its collection class. That is, don't
 * add a circle via a collection, then remove it via Circle.remove()
 */
open class CircleManager(map: GoogleMap) :
    MapObjectManager<Circle, CircleManager.Collection>(map),
    GoogleMap.OnCircleClickListener {

    override fun setListenersOnUiThread() {
        mMap.setOnCircleClickListener(this)
    }

    override fun newCollection(): Collection = Collection()

    override fun removeObjectFromMap(circle: Circle) {
        circle.remove()
    }

    override fun setVisible(mapObject: Circle, visible: Boolean) {
        mapObject.isVisible = visible
    }

    override fun onCircleClick(circle: Circle) {
        mAllObjects[circle]?.mCircleClickListener?.onCircleClick(circle)
    }

    /** A collection of [Circle]s on the map with its own set of listeners. */
    open inner class Collection : MapObjectManager<Circle, Collection>.Collection() {
        internal var mCircleClickListener: GoogleMap.OnCircleClickListener? = null

        open fun addCircle(opts: CircleOptions): Circle =
            checkAndAdd(mMap.addCircle(opts), "Circle")

        open fun addAll(opts: KotlinCollection<CircleOptions>) =
            addAll(opts, ::addCircle)

        open fun addAll(opts: KotlinCollection<CircleOptions>, defaultVisible: Boolean) =
            addAll(opts, defaultVisible, ::addCircle)

        open fun getCircles(): KotlinCollection<Circle> = getObjects()

        open fun setOnCircleClickListener(circleClickListener: GoogleMap.OnCircleClickListener?) {
            mCircleClickListener = circleClickListener
        }
    }
}

/**
 * Adds a new [Circle] to the underlying map and to this [CircleManager.Collection] with the
 * provided [optionsActions].
 */
public inline fun CircleManager.Collection.addCircle(optionsActions: CircleOptions.() -> Unit): Circle =
    this.addCircle(
        CircleOptions().apply(optionsActions)
    )

/**
 * Returns a flow that emits when a circle in this collection is clicked. Using this to observe circle clicks
 * will override an existing listener (if any) to [CircleManager.Collection.setOnCircleClickListener].
 *
 * **Warning**: This is a cold flow wrapping a single-listener SDK callback. Concurrently subscribing
 * multiple collectors will result in listener hijacking, and cancelling any observer will unregister
 * the active listener completely. Always share this flow (e.g. using [kotlinx.coroutines.flow.shareIn])
 * for multi-observer configurations.
 */
public fun CircleManager.Collection.clickEvents(): Flow<Circle> =
    callbackFlow {
        setOnCircleClickListener {
            trySend(it).isSuccess
        }
        awaitClose {
            setOnCircleClickListener(null)
        }
    }
