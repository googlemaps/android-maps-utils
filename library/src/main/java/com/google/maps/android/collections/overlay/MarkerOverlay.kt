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
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker

/**
 * High-level, decoupled wrapper around Google Maps [Marker].
 *
 * Provides property accessors, lifecycle controls, and direct lambda listener bindings.
 */
class MarkerOverlay(
    override val native: Marker,
    private val onRemoveCallback: (MarkerOverlay) -> Boolean,
) : PointOverlay<Marker> {

    override var position: LatLng
        get() = native.position
        set(value) { native.position = value }

    override var isVisible: Boolean
        get() = native.isVisible
        set(value) { native.isVisible = value }

    override var zIndex: Float
        get() = native.zIndex
        set(value) { native.zIndex = value }

    override var tag: Any?
        get() = native.tag
        set(value) { native.tag = value }

    var title: String?
        get() = native.title
        set(value) { native.title = value }

    var snippet: String?
        get() = native.snippet
        set(value) { native.snippet = value }

    var isDraggable: Boolean
        get() = native.isDraggable
        set(value) { native.isDraggable = value }

    var alpha: Float
        get() = native.alpha
        set(value) { native.alpha = value }

    var rotation: Float
        get() = native.rotation
        set(value) { native.rotation = value }

    var isFlat: Boolean
        get() = native.isFlat
        set(value) { native.isFlat = value }

    val isInfoWindowShown: Boolean
        get() = native.isInfoWindowShown

    fun showInfoWindow() = native.showInfoWindow()

    fun hideInfoWindow() = native.hideInfoWindow()

    override fun remove(): Boolean = onRemoveCallback(this)

    internal var clickListener: ((MarkerOverlay) -> Boolean)? = null
    internal var infoWindowClickListener: ((MarkerOverlay) -> Unit)? = null
    internal var infoWindowLongClickListener: ((MarkerOverlay) -> Unit)? = null
    internal var dragStartListener: ((MarkerOverlay) -> Unit)? = null
    internal var dragListener: ((MarkerOverlay) -> Unit)? = null
    internal var dragEndListener: ((MarkerOverlay) -> Unit)? = null
    internal var infoWindowProvider: ((MarkerOverlay) -> View?)? = null
    internal var infoContentsProvider: ((MarkerOverlay) -> View?)? = null

    /**
     * Registers a click listener for this specific marker overlay.
     *
     * @param listener Callback returning `true` if the event is consumed, `false` otherwise.
     */
    fun onClick(listener: (MarkerOverlay) -> Boolean) {
        clickListener = listener
    }

    /**
     * Registers an info window click listener for this specific marker.
     */
    fun onInfoWindowClick(listener: (MarkerOverlay) -> Unit) {
        infoWindowClickListener = listener
    }

    /**
     * Registers an info window long-click listener for this specific marker.
     */
    fun onInfoWindowLongClick(listener: (MarkerOverlay) -> Unit) {
        infoWindowLongClickListener = listener
    }

    /**
     * Registers drag event callbacks for this marker.
     */
    fun onDrag(
        onStart: ((MarkerOverlay) -> Unit)? = null,
        onDrag: ((MarkerOverlay) -> Unit)? = null,
        onEnd: ((MarkerOverlay) -> Unit)? = null,
    ) {
        dragStartListener = onStart
        dragListener = onDrag
        dragEndListener = onEnd
    }

    /**
     * Sets custom info window view providers for this marker.
     */
    fun setCustomInfoWindow(
        infoWindow: ((MarkerOverlay) -> View?)? = null,
        infoContents: ((MarkerOverlay) -> View?)? = null,
    ) {
        infoWindowProvider = infoWindow
        infoContentsProvider = infoContents
    }
}
