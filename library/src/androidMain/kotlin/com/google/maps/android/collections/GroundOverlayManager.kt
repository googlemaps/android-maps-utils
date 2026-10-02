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
import kotlin.collections.Collection as KotlinCollection

/**
 * Keeps track of collections of ground overlays on the map. Delegates all GroundOverlay-related
 * events to each collection's individually managed listeners.
 *
 * All ground overlay operations (adds and removes) should occur via its collection class. That
 * is, don't add a ground overlay via a collection, then remove it via GroundOverlay.remove()
 */
public open class GroundOverlayManager(map: GoogleMap) :
    MapObjectManager<GroundOverlay, GroundOverlayManager.Collection>(map),
    GoogleMap.OnGroundOverlayClickListener {

    override fun setListenersOnUiThread() {
        mMap.setOnGroundOverlayClickListener(this)
    }

    public override fun newCollection(): Collection = Collection()

    public override fun removeObjectFromMap(groundOverlay: GroundOverlay) {
        groundOverlay.remove()
    }

    public override fun setVisible(mapObject: GroundOverlay, visible: Boolean) {
        mapObject.isVisible = visible
    }

    public override fun onGroundOverlayClick(groundOverlay: GroundOverlay) {
        mAllObjects[groundOverlay]?.mGroundOverlayClickListener?.onGroundOverlayClick(groundOverlay)
    }

    /** A collection of [GroundOverlay]s on the map with its own set of listeners. */
    public open inner class Collection :
        MapObjectManager<GroundOverlay, Collection>.Collection() {
        internal var mGroundOverlayClickListener: GoogleMap.OnGroundOverlayClickListener? = null

        public open fun addGroundOverlay(opts: GroundOverlayOptions): GroundOverlay =
            checkAndAdd(mMap.addGroundOverlay(opts), "GroundOverlay")

        public open fun addAll(opts: KotlinCollection<GroundOverlayOptions>): Unit =
            addAll(opts, ::addGroundOverlay)

        public open fun addAll(opts: KotlinCollection<GroundOverlayOptions>, defaultVisible: Boolean): Unit =
            addAll(opts, defaultVisible, ::addGroundOverlay)

        public open fun getGroundOverlays(): KotlinCollection<GroundOverlay> = getObjects()

        public open fun setOnGroundOverlayClickListener(
            groundOverlayClickListener: GoogleMap.OnGroundOverlayClickListener?,
        ) {
            mGroundOverlayClickListener = groundOverlayClickListener
        }
    }
}
