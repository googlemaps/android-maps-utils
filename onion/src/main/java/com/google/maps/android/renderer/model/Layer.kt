/*
 * Copyright 2025 Google LLC
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
 */

package com.google.maps.android.renderer.model

import java.util.Collections

/**
 * A container for a collection of [MapObject]s.
 *
 * A Layer represents a logical grouping of map objects, such as the contents of
 * a single KML file
 * or a specific dataset. Layers can be added to and removed from a
 * [com.google.maps.android.renderer.Renderer].
 */
class Layer {
    private val _mapObjects = mutableListOf<MapObject>()

    /**
     * Gets all map objects in the layer.
     *
     * @return An unmodifiable view of the map objects.
     */
    val mapObjects: Collection<MapObject>
        get() = Collections.unmodifiableCollection(_mapObjects)

    /**
     * Adds a map object to the layer.
     *
     * @param mapObject The map object to add.
     */
    fun addMapObject(mapObject: MapObject) {
        _mapObjects.add(mapObject)
    }

    /**
     * Adds a collection of map objects to the layer.
     *
     * @param mapObjects The collection of map objects to add.
     */
    fun addMapObjects(mapObjects: Collection<MapObject>) {
        _mapObjects.addAll(mapObjects)
    }

    /**
     * Removes a map object from the layer.
     *
     * @param mapObject The map object to remove.
     * @return True if the object was removed, false otherwise.
     */
    fun removeMapObject(mapObject: MapObject): Boolean {
        return _mapObjects.remove(mapObject)
    }

    /**
     * Clears all map objects from the layer.
     */
    fun clear() {
        _mapObjects.clear()
    }
}
