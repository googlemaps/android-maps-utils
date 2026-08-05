/*
 * Copyright 2026 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.google.maps.android.collections

import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.maps.android.collections.MarkerManager
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

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