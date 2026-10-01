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
import kotlin.collections.Collection as KotlinCollection

/**
 * Keeps track of collections of circles on the map. Delegates all Circle-related events to each
 * collection's individually managed listeners.
 *
 * All circle operations (adds and removes) should occur via its collection class. That is, don't
 * add a circle via a collection, then remove it via Circle.remove()
 */
public open class CircleManager(map: GoogleMap) :
    MapObjectManager<Circle, CircleManager.Collection>(map),
    GoogleMap.OnCircleClickListener {

    override fun setListenersOnUiThread() {
        mMap.setOnCircleClickListener(this)
    }

    public override fun newCollection(): Collection = Collection()

    public override fun removeObjectFromMap(circle: Circle) {
        circle.remove()
    }

    public override fun setVisible(mapObject: Circle, visible: Boolean) {
        mapObject.isVisible = visible
    }

    public override fun onCircleClick(circle: Circle) {
        mAllObjects[circle]?.mCircleClickListener?.onCircleClick(circle)
    }

    /** A collection of [Circle]s on the map with its own set of listeners. */
    public open inner class Collection : MapObjectManager<Circle, Collection>.Collection() {
        internal var mCircleClickListener: GoogleMap.OnCircleClickListener? = null

        public open fun addCircle(opts: CircleOptions): Circle =
            checkAndAdd(mMap.addCircle(opts), "Circle")

        public open fun addAll(opts: KotlinCollection<CircleOptions>): Unit =
            addAll(opts, ::addCircle)

        public open fun addAll(opts: KotlinCollection<CircleOptions>, defaultVisible: Boolean): Unit =
            addAll(opts, defaultVisible, ::addCircle)

        public open fun getCircles(): KotlinCollection<Circle> = getObjects()

        public open fun setOnCircleClickListener(circleClickListener: GoogleMap.OnCircleClickListener?) {
            mCircleClickListener = circleClickListener
        }
    }
}
