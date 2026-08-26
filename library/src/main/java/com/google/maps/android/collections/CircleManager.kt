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

    override fun onCircleClick(circle: Circle) {
        mAllObjects[circle]?.mCircleClickListener?.onCircleClick(circle)
    }

    /** A collection of [Circle]s on the map with its own set of listeners. */
    open inner class Collection : MapObjectManager<Circle, Collection>.Collection() {
        internal var mCircleClickListener: GoogleMap.OnCircleClickListener? = null

        open fun addCircle(opts: CircleOptions): Circle =
            mMap.addCircle(opts).also { super.add(it) }

        open fun addAll(opts: KotlinCollection<CircleOptions>) {
            for (opt in opts) {
                addCircle(opt)
            }
        }

        open fun addAll(opts: KotlinCollection<CircleOptions>, defaultVisible: Boolean) {
            for (opt in opts) {
                addCircle(opt).isVisible = defaultVisible
            }
        }

        open fun showAll() {
            for (circle in getCircles()) {
                circle.isVisible = true
            }
        }

        open fun hideAll() {
            for (circle in getCircles()) {
                circle.isVisible = false
            }
        }

        override fun remove(circle: Circle?): Boolean = super.remove(circle)

        open fun getCircles(): KotlinCollection<Circle> = getObjects()

        open fun setOnCircleClickListener(circleClickListener: GoogleMap.OnCircleClickListener?) {
            mCircleClickListener = circleClickListener
        }
    }
}
