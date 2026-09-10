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

import android.os.Handler
import android.os.Looper
import com.google.android.gms.maps.GoogleMap
import kotlin.collections.Collection as KotlinCollection

/**
 * Abstract base implementation for map object collection manager classes.
 *
 * Keeps track of collections of objects on the map. Delegates all object-related events to each
 * collection's individually managed listeners.
 *
 * All object operations (adds and removes) should occur via its collection class. That is, don't
 * add an object via a collection, then remove it via Object.remove()
 */
abstract class MapObjectManager<O : Any, C : MapObjectManager<O, C>.Collection>(
    @JvmField
    protected val mMap: GoogleMap,
) {
    private val mNamedCollections: MutableMap<String, C> = mutableMapOf()

    @JvmField
    protected val mAllObjects: MutableMap<O, C> = mutableMapOf()

    init {
        Handler(Looper.getMainLooper()).post {
            setListenersOnUiThread()
        }
    }

    internal abstract fun setListenersOnUiThread()

    abstract fun newCollection(): C

    /**
     * Create a new named collection, which can later be looked up by [getCollection]
     *
     * @param id a unique id for this collection.
     */
    open fun newCollection(id: String): C {
        require(mNamedCollections[id] == null) { "collection id is not unique: $id" }
        val collection = newCollection()
        mNamedCollections[id] = collection
        return collection
    }

    /**
     * Gets a named collection that was created by [newCollection]
     *
     * @param id the unique id for this collection.
     */
    open fun getCollection(id: String): C? = mNamedCollections[id]

    /**
     * Removes an object from its collection.
     *
     * @param mapObject the object to remove.
     * @return true if the object was removed.
     */
    open fun remove(mapObject: O?): Boolean =
        mapObject != null && mAllObjects[mapObject]?.remove(mapObject) == true

    protected abstract fun removeObjectFromMap(mapObject: O)

    protected open fun setVisible(mapObject: O, visible: Boolean) {}

    open inner class Collection {
        private val mObjects: MutableSet<O> = mutableSetOf()

        // Safe unchecked cast: this inner collection is an instance of subclass C.
        @Suppress("UNCHECKED_CAST")
        protected open fun add(mapObject: O) {
            mObjects.add(mapObject)
            mAllObjects[mapObject] = this@Collection as C
        }

        protected open fun checkAndAdd(mapObject: O?, typeName: String): O =
            checkNotNull(mapObject) { "Failed to add $typeName to GoogleMap" }.also { add(it) }

        open fun showAll() {
            for (mapObject in mObjects) {
                setVisible(mapObject, true)
            }
        }

        open fun hideAll() {
            for (mapObject in mObjects) {
                setVisible(mapObject, false)
            }
        }

        protected open fun <T> addAll(opts: KotlinCollection<T>, adder: (T) -> O) {
            for (opt in opts) {
                adder(opt)
            }
        }

        protected open fun <T> addAll(opts: KotlinCollection<T>, defaultVisible: Boolean, adder: (T) -> O) {
            for (opt in opts) {
                val obj = adder(opt)
                setVisible(obj, defaultVisible)
            }
        }

        open fun remove(mapObject: O?): Boolean {
            if (mapObject == null) return false
            if (mObjects.remove(mapObject)) {
                mAllObjects.remove(mapObject)
                removeObjectFromMap(mapObject)
                return true
            }
            return false
        }

        open fun clear() {
            for (mapObject in mObjects) {
                removeObjectFromMap(mapObject)
                mAllObjects.remove(mapObject)
            }
            mObjects.clear()
        }

        protected open fun getObjects(): KotlinCollection<O> = mObjects
    }
}
