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

package com.google.maps.android

import android.graphics.Bitmap
import android.location.Location
import androidx.annotation.IntDef
import com.google.android.gms.maps.CameraUpdate
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.GoogleMapOptions
import com.google.android.gms.maps.model.Circle
import com.google.android.gms.maps.model.CircleOptions
import com.google.android.gms.maps.model.GroundOverlay
import com.google.android.gms.maps.model.GroundOverlayOptions
import com.google.android.gms.maps.model.IndoorBuilding
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PointOfInterest
import com.google.android.gms.maps.model.Polygon
import com.google.android.gms.maps.model.PolygonOptions
import com.google.android.gms.maps.model.Polyline
import com.google.android.gms.maps.model.PolylineOptions
import com.google.android.gms.maps.model.TileOverlay
import com.google.android.gms.maps.model.TileOverlayOptions
import com.google.maps.android.model.circleOptions
import com.google.maps.android.model.groundOverlayOptions
import com.google.maps.android.model.markerOptions
import com.google.maps.android.model.polygonOptions
import com.google.maps.android.model.polylineOptions
import com.google.maps.android.model.tileOverlayOptions
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

@IntDef(
    GoogleMap.OnCameraMoveStartedListener.REASON_GESTURE,
    GoogleMap.OnCameraMoveStartedListener.REASON_API_ANIMATION,
    GoogleMap.OnCameraMoveStartedListener.REASON_DEVELOPER_ANIMATION
)
@Retention(AnnotationRetention.SOURCE)
public annotation class MoveStartedReason

public sealed class CameraEvent
public object CameraIdleEvent : CameraEvent()
public object CameraMoveCanceledEvent : CameraEvent()
public object CameraMoveEvent : CameraEvent()
public data class CameraMoveStartedEvent(@param:MoveStartedReason val reason: Int) : CameraEvent()

/**
 * Change event when a marker is dragged. See [GoogleMap.setOnMarkerDragListener]
 */
public sealed class OnMarkerDragEvent {
    public abstract val marker: Marker
}

/**
 * Event emitted repeatedly while a marker is being dragged.
 */
public data class MarkerDragEvent(public override val marker: Marker) : OnMarkerDragEvent()

/**
 * Event emitted when a marker has finished being dragged.
 */
public data class MarkerDragEndEvent(public override val marker: Marker) : OnMarkerDragEvent()

/**
 * Event emitted when a marker starts being dragged.
 */
public data class MarkerDragStartEvent(public override val marker: Marker) : OnMarkerDragEvent()

/**
 * Change event when the indoor state changes. See [GoogleMap.OnIndoorStateChangeListener]
 */
public sealed class IndoorChangeEvent

/**
 * Change event when an indoor building is focused.
 * See [GoogleMap.OnIndoorStateChangeListener.onIndoorBuildingFocused]
 */
public object IndoorBuildingFocusedEvent : IndoorChangeEvent()

/**
 * Change event when an indoor level is activated.
 * See [GoogleMap.OnIndoorStateChangeListener.onIndoorLevelActivated]
 */
public data class IndoorLevelActivatedEvent(val building: IndoorBuilding) : IndoorChangeEvent()

/**
 * Returns a [Flow] of [CameraEvent] items so that camera movements can be observed. Using this to
 * observe camera events will set listeners and thus override existing listeners to
 * [GoogleMap.setOnCameraIdleListener], [GoogleMap.setOnCameraMoveCanceledListener],
 * [GoogleMap.setOnCameraMoveListener] and [GoogleMap.setOnCameraMoveStartedListener].
 */
@Deprecated(
    message = "Use cameraIdleEvents(), cameraMoveCanceledEvents(), cameraMoveEvents() or cameraMoveStartedEvents",
)
public fun GoogleMap.cameraEvents(): Flow<CameraEvent> =
    callbackFlow {
        setOnCameraIdleListener {
            trySend(CameraIdleEvent)
        }
        setOnCameraMoveCanceledListener {
            trySend(CameraMoveCanceledEvent)
        }
        setOnCameraMoveListener {
            trySend(CameraMoveEvent)
        }
        setOnCameraMoveStartedListener {
            trySend(CameraMoveStartedEvent(it))
        }
        awaitClose {
            setOnCameraIdleListener(null)
            setOnCameraMoveCanceledListener(null)
            setOnCameraMoveListener(null)
            setOnCameraMoveStartedListener(null)
        }
    }

/**
 * A suspending function that awaits the completion of the [cameraUpdate] animation.
 *
 * @param cameraUpdate the [CameraUpdate] to apply on the map
 * @param durationMs the duration in milliseconds of the animation. Defaults to 3 seconds.
 */
public suspend inline fun GoogleMap.awaitAnimateCamera(
    cameraUpdate: CameraUpdate,
    durationMs: Int = 3000
): Unit =
    suspendCancellableCoroutine { continuation ->
        animateCamera(cameraUpdate, durationMs, object : GoogleMap.CancelableCallback {
            override fun onFinish() {
                continuation.resume(Unit)
            }

            override fun onCancel() {
                continuation.cancel()
            }
        })
    }

/**
 * A suspending function that awaits for the map to be loaded. Uses
 * [GoogleMap.setOnMapLoadedCallback].
 */
public suspend inline fun GoogleMap.awaitMapLoad(): Unit =
    suspendCoroutine { continuation ->
        setOnMapLoadedCallback {
            continuation.resume(Unit)
        }
    }

/**
 * Returns a flow that emits when the camera is idle. Using this to observe camera idle events will
 * override an existing listener (if any) to [GoogleMap.setOnCameraIdleListener].
 */
public fun GoogleMap.cameraIdleEvents(): Flow<Unit> =
    callbackFlow {
        setOnCameraIdleListener {
            trySend(Unit)
        }
        awaitClose {
            setOnCameraIdleListener(null)
        }
    }

/**
 * Returns a flow that emits when a camera move is canceled. Using this to observe camera move
 * cancel events will override an existing listener (if any) to
 * [GoogleMap.setOnCameraMoveCanceledListener].
 */
public fun GoogleMap.cameraMoveCanceledEvents(): Flow<Unit> =
    callbackFlow {
        setOnCameraMoveCanceledListener {
            trySend(Unit)
        }
        awaitClose {
            setOnCameraMoveCanceledListener(null)
        }
    }

/**
 * Returns a flow that emits when the camera moves. Using this to observe camera move events will
 * override an existing listener (if any) to [GoogleMap.setOnCameraMoveListener].
 */
public fun GoogleMap.cameraMoveEvents(): Flow<Unit> =
    callbackFlow {
        setOnCameraMoveListener {
            trySend(Unit)
        }
        awaitClose {
            setOnCameraMoveListener(null)
        }
    }

/**
 * A suspending function that returns a bitmap snapshot of the current view of the map. Uses
 * [GoogleMap.snapshot].
 *
 * @param bitmap an optional preallocated bitmap
 * @return the snapshot
 */
public suspend inline fun GoogleMap.awaitSnapshot(bitmap: Bitmap? = null): Bitmap? =
    suspendCoroutine { continuation ->
        snapshot({ continuation.resume(it) }, bitmap)
    }

/**
 * Returns a flow that emits when a camera move started. Using this to observe camera move start
 * events will override an existing listener (if any) to [GoogleMap.setOnCameraMoveStartedListener].
 */
public fun GoogleMap.cameraMoveStartedEvents(): Flow<Int> =
    callbackFlow {
        setOnCameraMoveStartedListener {
            trySend(it)
        }
        awaitClose {
            setOnCameraMoveStartedListener(null)
        }
    }

/**
 * Returns a flow that emits when a circle is clicked. Using this to observe circle clicks events
 * will override an existing listener (if any) to [GoogleMap.setOnCircleClickListener].
 */
public fun GoogleMap.circleClickEvents(): Flow<Circle> =
    callbackFlow {
        setOnCircleClickListener {
            trySend(it)
        }
        awaitClose {
            setOnCircleClickListener(null)
        }
    }

/**
 * Returns a flow that emits when a ground overlay is clicked. Using this to observe ground overlay
 * clicks events will override an existing listener (if any) to
 * [GoogleMap.setOnGroundOverlayClickListener].
 */
public fun GoogleMap.groundOverlayClicks(): Flow<GroundOverlay> =
    callbackFlow {
        setOnGroundOverlayClickListener {
            trySend(it)
        }
        awaitClose {
            setOnGroundOverlayClickListener(null)
        }
    }

/**
 * Returns a flow that emits when the indoor state changes. Using this to observe indoor state
 * change events will override an existing listener (if any) to
 * [GoogleMap.setOnIndoorStateChangeListener]
 */
public fun GoogleMap.indoorStateChangeEvents(): Flow<IndoorChangeEvent> =
    callbackFlow {
        setOnIndoorStateChangeListener(object : GoogleMap.OnIndoorStateChangeListener {
            override fun onIndoorBuildingFocused() {
                trySend(IndoorBuildingFocusedEvent)
            }

            override fun onIndoorLevelActivated(indoorBuilding: IndoorBuilding) {
                trySend(IndoorLevelActivatedEvent(building = indoorBuilding))
            }
        })
        awaitClose {
            setOnIndoorStateChangeListener(null)
        }
    }

/**
 * Returns a flow that emits when a marker's info window is clicked. Using this to observe info
 * info window clicks will override an existing listener (if any) to
 * [GoogleMap.setOnInfoWindowClickListener]
 */
public fun GoogleMap.infoWindowClickEvents(): Flow<Marker> =
    callbackFlow {
        setOnInfoWindowClickListener {
            trySend(it)
        }
        awaitClose {
            setOnInfoWindowClickListener(null)
        }
    }

/**
 * Returns a flow that emits when a marker's info window is closed. Using this to observe info
 * window closes will override an existing listener (if any) to
 * [GoogleMap.setOnInfoWindowCloseListener]
 */
public fun GoogleMap.infoWindowCloseEvents(): Flow<Marker> =
    callbackFlow {
        setOnInfoWindowCloseListener {
            trySend(it)
        }
        awaitClose {
            setOnInfoWindowCloseListener(null)
        }
    }

/**
 * Returns a flow that emits when a marker's info window is long pressed. Using this to observe info
 * window long presses will override an existing listener (if any) to
 * [GoogleMap.setOnInfoWindowLongClickListener]
 */
public fun GoogleMap.infoWindowLongClickEvents(): Flow<Marker> =
    callbackFlow {
        setOnInfoWindowLongClickListener {
            trySend(it)
        }
        awaitClose {
            setOnInfoWindowLongClickListener(null)
        }
    }

/**
 * Returns a flow that emits when the map is clicked. Using this to observe map click events will
 * override an existing listener (if any) to [GoogleMap.setOnMapClickListener]
 */
public fun GoogleMap.mapClickEvents(): Flow<LatLng> =
    callbackFlow {
        setOnMapClickListener {
            trySend(it)
        }
        awaitClose {
            setOnMapClickListener(null)
        }
    }

/**
 * Returns a flow that emits when the map is long clicked. Using this to observe map click events
 * will override an existing listener (if any) to [GoogleMap.setOnMapLongClickListener]
 */
public fun GoogleMap.mapLongClickEvents(): Flow<LatLng> =
    callbackFlow {
        setOnMapLongClickListener {
            trySend(it)
        }
        awaitClose {
            setOnMapLongClickListener(null)
        }
    }

/**
 * Returns a flow that emits when a marker on the map is clicked. Using this to observe marker click
 * events will override an existing listener (if any) to [GoogleMap.setOnMarkerClickListener]
 */
public fun GoogleMap.markerClickEvents(): Flow<Marker> =
    callbackFlow {
        setOnMarkerClickListener {
            trySend(it).isSuccess
        }
        awaitClose {
            setOnMarkerClickListener(null)
        }
    }

/**
 * Returns a flow that emits when a marker is dragged. Using this to observer marker drag events
 * will override existing listeners (if any) to [GoogleMap.setOnMarkerDragListener]
 */
public fun GoogleMap.markerDragEvents(): Flow<OnMarkerDragEvent> =
    callbackFlow {
        setOnMarkerDragListener(object : GoogleMap.OnMarkerDragListener {
            override fun onMarkerDragStart(marker: Marker) {
                trySend(MarkerDragStartEvent(marker = marker))
            }

            override fun onMarkerDrag(marker: Marker) {
                trySend(MarkerDragEvent(marker = marker))
            }

            override fun onMarkerDragEnd(marker: Marker) {
                trySend(MarkerDragEndEvent(marker = marker))
            }

        })
        awaitClose {
            setOnMarkerDragListener(null)
        }
    }

/**
 * Returns a flow that emits when the my location button is clicked. Using this to observe my
 * location button click events will override an existing listener (if any) to
 * [GoogleMap.setOnMyLocationButtonClickListener]
 */
public fun GoogleMap.myLocationButtonClickEvents(): Flow<Unit> =
    callbackFlow {
        setOnMyLocationButtonClickListener {
            trySend(Unit).isSuccess
        }
        awaitClose {
            setOnMyLocationButtonClickListener(null)
        }
    }

/**
 * Returns a flow that emits when the my location blue dot is clicked. Using this to observe my
 * location blue dot click events will override an existing listener (if any) to
 * [GoogleMap.setOnMyLocationClickListener]
 */
public fun GoogleMap.myLocationClickEvents(): Flow<Location> =
    callbackFlow {
        setOnMyLocationClickListener {
            trySend(it)
        }
        awaitClose {
            setOnMyLocationClickListener(null)
        }
    }

/**
 * Returns a flow that emits when a PointOfInterest is clicked. Using this to observe
 * PointOfInterest click events will override an existing listener (if any) to
 * [GoogleMap.setOnPoiClickListener]
 */
public fun GoogleMap.poiClickEvents(): Flow<PointOfInterest> =
    callbackFlow {
         setOnPoiClickListener {
            trySend(it)
        }
        awaitClose {
            setOnPoiClickListener(null)
        }
    }

/**
 * Returns a flow that emits when a Polygon is clicked. Using this to observe Polygon click events
 * will override an existing listener (if any) to [GoogleMap.setOnPolygonClickListener]
 */
public fun GoogleMap.polygonClickEvents(): Flow<Polygon> =
    callbackFlow {
        setOnPolygonClickListener {
            trySend(it)
        }
        awaitClose {
            setOnPolygonClickListener(null)
        }
    }

/**
 * Returns a flow that emits when a Polyline is clicked. Using this to observe Polyline click events
 * will override an existing listener (if any) to [GoogleMap.setOnPolylineClickListener]
 */
public fun GoogleMap.polylineClickEvents(): Flow<Polyline> =
    callbackFlow {
        setOnPolylineClickListener {
            trySend(it)
        }
        awaitClose {
            setOnPolylineClickListener(null)
        }
    }

/**
 * Builds a new [GoogleMapOptions] using the provided [optionsActions].
 *
 * @return the constructed [GoogleMapOptions]
 */
public inline fun buildGoogleMapOptions(optionsActions: GoogleMapOptions.() -> Unit): GoogleMapOptions =
    GoogleMapOptions().apply(
        optionsActions
    )

/**
 * Adds a [Circle] to this [GoogleMap] using the function literal with receiver [optionsActions].
 *
 * @return the added [Circle]
 */
public inline fun GoogleMap.addCircle(optionsActions: CircleOptions.() -> Unit): Circle =
    this.addCircle(
        circleOptions(optionsActions)
    )

/**
 * Adds a [GroundOverlay] to this [GoogleMap] using the function literal with receiver
 * [optionsActions].
 *
 * @return the added [Circle]
 */
public inline fun GoogleMap.addGroundOverlay(optionsActions: GroundOverlayOptions.() -> Unit): GroundOverlay? =
    this.addGroundOverlay(
        groundOverlayOptions(optionsActions)
    )

/**
 * Adds a [Marker] to this [GoogleMap] using the function literal with receiver [optionsActions].
 *
 * @return the added [Marker]
 */
public inline fun GoogleMap.addMarker(optionsActions: MarkerOptions.() -> Unit): Marker? =
    this.addMarker(
        markerOptions(optionsActions)
    )

/**
 * Adds a [Polygon] to this [GoogleMap] using the function literal with receiver [optionsActions].
 *
 * @return the added [Polygon]
 */
public inline fun GoogleMap.addPolygon(optionsActions: PolygonOptions.() -> Unit): Polygon =
    this.addPolygon(
        polygonOptions(optionsActions)
    )

/**
 * Adds a [Polyline] to this [GoogleMap] using the function literal with receiver [optionsActions].
 *
 * @return the added [Polyline]
 */
public inline fun GoogleMap.addPolyline(optionsActions: PolylineOptions.() -> Unit): Polyline =
    this.addPolyline(
        polylineOptions(optionsActions)
    )

/**
 * Adds a [TileOverlay] to this [GoogleMap] using the function literal with receiver
 * [optionsActions].
 *
 * @return the added [Polyline]
 */
public inline fun GoogleMap.addTileOverlay(optionsActions: TileOverlayOptions.() -> Unit): TileOverlay? =
    this.addTileOverlay(
        tileOverlayOptions(optionsActions)
    )
