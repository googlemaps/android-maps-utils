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
package com.google.maps.android.data

import java.util.Observable

/**
 * An abstraction that shares the common properties of KmlPlacemark and GeoJsonFeature.
 */
@Deprecated("Use the new platform-agnostic data layer and renderer instead.")
public open class Feature(
    private var geometry: Geometry?,
    protected var mId: String?,
    properties: Map<String, String>?,
) : Observable() {
    private val mProperties: MutableMap<String, String> = properties?.toMutableMap() ?: HashMap()

    public fun getPropertyKeys(): Iterable<String> = mProperties.keys

    public fun getProperties(): Iterable<Map.Entry<String, String>> = mProperties.entries

    public fun getProperty(property: String): String? = mProperties[property]

    public fun getId(): String? = mId

    public fun hasProperty(property: String): Boolean = mProperties.containsKey(property)

    public fun getGeometry(): Geometry? = geometry

    public fun hasProperties(): Boolean = mProperties.isNotEmpty()

    public fun hasGeometry(): Boolean = geometry != null

    protected open fun setProperty(
        property: String,
        propertyValue: String,
    ): String? = mProperties.put(property, propertyValue)

    protected open fun removeProperty(property: String): String? = mProperties.remove(property)

    protected open fun setGeometry(geometry: Geometry?) {
        this.geometry = geometry
    }
}
