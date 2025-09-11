/*
 * Copyright 2025 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.google.maps.android.data

import java.util.Observable

/**
 * An abstraction that shares the common properties of
 * [com.google.maps.android.data.kml.KmlPlacemark] and
 * [com.google.maps.android.data.geojson.GeoJsonFeature]
 */
open class Feature(
    geometry: Geometry<*>?,
    id: String?,
    properties: Map<String, String>?
) : Observable() {
    open var id: String? = id
        protected set

    private val _properties: MutableMap<String, String> = properties?.toMutableMap() ?: mutableMapOf()

    open var geometry: Geometry<*>? = geometry
        protected set(value) {
            field = value
            setChanged()
            notifyObservers()
        }

    /**
     * Returns all the stored property keys
     */
    val propertyKeys: Iterable<String>
        get() = _properties.keys

    /**
     * Gets the property entry set
     */
    val properties: Iterable<Map.Entry<String, String>>
        get() = _properties.entries

    /**
     * Gets the value for a stored property
     *
     * @param property key of the property
     * @return value of the property if its key exists, otherwise null
     */
    fun getProperty(property: String): String? = _properties[property]

    /**
     * Checks whether the given property key exists
     *
     * @param property key of the property to check
     * @return true if property key exists, false otherwise
     */
    fun hasProperty(property: String): Boolean = _properties.containsKey(property)

    /**
     * Gets whether the placemark has properties
     *
     * @return true if there are properties in the properties map, false otherwise
     */
    fun hasProperties(): Boolean = _properties.isNotEmpty()

    /**
     * Checks if the geometry is assigned
     *
     * @return true if feature contains geometry object, otherwise null
     */
    fun hasGeometry(): Boolean = geometry != null

    /**
     * Store a new property key and value
     *
     * @param property      key of the property to store
     * @param propertyValue value of the property to store
     * @return previous value with the same key, otherwise null if the key didn't exist
     */
    protected open fun setProperty(property: String, propertyValue: String): String? {
        val prev = _properties.put(property, propertyValue)
        setChanged()
        notifyObservers()
        return prev
    }

    /**
     * Removes a given property
     *
     * @param property key of the property to remove
     * @return value of the removed property or null if there was no corresponding key
     */
    protected open fun removeProperty(property: String): String? {
        val prev = _properties.remove(property)
        setChanged()
        notifyObservers()
        return prev
    }
}
