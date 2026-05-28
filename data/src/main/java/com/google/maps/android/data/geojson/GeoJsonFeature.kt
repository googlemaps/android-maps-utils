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
package com.google.maps.android.data.geojson

import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolygonOptions
import com.google.android.gms.maps.model.PolylineOptions
import com.google.maps.android.data.Feature
import com.google.maps.android.data.Geometry
import java.util.Observable
import java.util.Observer

/**
 * A GeoJsonFeature has a geometry, bounding box, id and set of properties. Styles are also stored
 * in this class.
 */
@Deprecated("Use the new platform-agnostic data layer and renderer instead.")
public class GeoJsonFeature(
    geometry: Geometry?,
    id: String?,
    properties: Map<String, String>?,
    private val boundingBox: LatLngBounds?,
) : Feature(geometry, id, properties),
    Observer {
    public var pointStyle: GeoJsonPointStyle? = null
        set(value) {
            field?.deleteObserver(this)
            field = value
            field?.addObserver(this)
            value?.let { checkRedrawFeature(it) }
        }

    public var lineStringStyle: GeoJsonLineStringStyle? = null
        set(value) {
            field?.deleteObserver(this)
            field = value
            field?.addObserver(this)
            value?.let { checkRedrawFeature(it) }
        }

    public var polygonStyle: GeoJsonPolygonStyle? = null
        set(value) {
            field?.deleteObserver(this)
            field = value
            field?.addObserver(this)
            value?.let { checkRedrawFeature(it) }
        }

    override fun setProperty(
        property: String,
        propertyValue: String,
    ): String? {
        val old = super.setProperty(property, propertyValue)
        setChanged()
        notifyObservers()
        return old
    }

    override fun removeProperty(property: String): String? {
        val old = super.removeProperty(property)
        setChanged()
        notifyObservers()
        return old
    }

    public fun getPolygonOptions(): PolygonOptions? = polygonStyle?.toPolygonOptions()

    public fun getMarkerOptions(): MarkerOptions? = pointStyle?.toMarkerOptions()

    public fun getPolylineOptions(): PolylineOptions? = lineStringStyle?.toPolylineOptions()

    private fun checkRedrawFeature(style: GeoJsonStyle) {
        val currentGeometry = getGeometry()
        if (currentGeometry != null && style.getGeometryType().contains(currentGeometry.getGeometryType())) {
            setChanged()
            notifyObservers()
        }
    }

    override fun setGeometry(geometry: Geometry?) {
        super.setGeometry(geometry)
        setChanged()
        notifyObservers()
    }

    public fun getBoundingBox(): LatLngBounds? = boundingBox

    override fun toString(): String =
        StringBuilder("Feature{")
            .apply {
                append("\n bounding box=").append(boundingBox)
                append(",\n geometry=").append(getGeometry())
                append(",\n point style=").append(pointStyle)
                append(",\n line string style=").append(lineStringStyle)
                append(",\n polygon style=").append(polygonStyle)
                append(",\n id=").append(mId)
                append(",\n properties=").append(getProperties())
                append("\n}\n")
            }.toString()

    override fun update(
        observable: Observable?,
        data: Any?,
    ) {
        if (observable is GeoJsonStyle) {
            checkRedrawFeature(observable)
        }
    }
}
