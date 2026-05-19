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
package com.google.maps.android.data.kml

import com.google.android.gms.maps.model.GroundOverlay

/**
 * Represents a KML Document or Folder.
 */
@Deprecated("Use the new platform-agnostic data layer and renderer instead.")
public class KmlContainer(
    private val properties: HashMap<String, String>,
    private val styles: HashMap<String, KmlStyle>,
    private val placemarks: HashMap<KmlPlacemark, Any?>,
    private val styleMap: HashMap<String, String>,
    private val containers: ArrayList<KmlContainer>,
    private val groundOverlays: HashMap<KmlGroundOverlay, GroundOverlay?>,
    private val containerId: String?,
) {
    internal fun getStyles(): HashMap<String, KmlStyle> = styles

    internal fun setPlacemark(
        placemark: KmlPlacemark,
        obj: Any?,
    ) {
        placemarks[placemark] = obj
    }

    internal fun getStyleMap(): HashMap<String, String> = styleMap

    internal fun getGroundOverlayHashMap(): HashMap<KmlGroundOverlay, GroundOverlay?> = groundOverlays

    public fun getContainerId(): String? = containerId

    public fun getStyle(styleID: String): KmlStyle? = styles[styleID]

    public fun getStyleIdFromMap(styleID: String): String? = styleMap[styleID]

    internal fun getPlacemarksHashMap(): HashMap<KmlPlacemark, Any?> = placemarks

    public fun getProperty(propertyName: String): String? = properties[propertyName]

    public fun hasProperties(): Boolean = properties.isNotEmpty()

    public fun hasProperty(keyValue: String): Boolean = properties.containsKey(keyValue)

    public fun hasContainers(): Boolean = containers.isNotEmpty()

    public fun getContainers(): Iterable<KmlContainer> = containers

    public fun getProperties(): Iterable<String> = properties.keys

    public fun getPlacemarks(): Iterable<KmlPlacemark> = placemarks.keys

    public fun hasPlacemarks(): Boolean = placemarks.isNotEmpty()

    public fun getGroundOverlays(): Iterable<KmlGroundOverlay> = groundOverlays.keys

    override fun toString(): String =
        StringBuilder("Container")
            .apply {
                append("{")
                append("\n properties=").append(properties)
                append(",\n placemarks=").append(placemarks)
                append(",\n containers=").append(containers)
                append(",\n ground overlays=").append(groundOverlays)
                append(",\n style maps=").append(styleMap)
                append(",\n styles=").append(styles)
                append("\n}\n")
            }.toString()
}
