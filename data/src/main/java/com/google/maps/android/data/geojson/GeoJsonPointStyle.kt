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

import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.MarkerOptions
import com.google.maps.android.data.Style

/**
 * A class that allows for GeoJsonPoint objects to be styled and for these styles to be translated
 * into a MarkerOptions object.
 */
@Deprecated("Use the new platform-agnostic data layer and renderer instead.")
public class GeoJsonPointStyle :
    Style(),
    GeoJsonStyle {
    override fun getGeometryType(): Array<String> = GEOMETRY_TYPE

    public fun getAlpha(): Float = mMarkerOptions.alpha

    public fun setAlpha(alpha: Float) {
        mMarkerOptions.alpha(alpha)
        styleChanged()
    }

    public fun getAnchorU(): Float = mMarkerOptions.anchorU

    public fun getAnchorV(): Float = mMarkerOptions.anchorV

    public fun setAnchor(
        anchorU: Float,
        anchorV: Float,
    ) {
        setMarkerHotSpot(anchorU, anchorV, "fraction", "fraction")
        styleChanged()
    }

    public fun isDraggable(): Boolean = mMarkerOptions.isDraggable

    public fun setDraggable(draggable: Boolean) {
        mMarkerOptions.draggable(draggable)
        styleChanged()
    }

    public fun isFlat(): Boolean = mMarkerOptions.isFlat

    public fun setFlat(flat: Boolean) {
        mMarkerOptions.flat(flat)
        styleChanged()
    }

    public fun getIcon(): BitmapDescriptor? = mMarkerOptions.icon

    public fun setIcon(bitmap: BitmapDescriptor?) {
        mMarkerOptions.icon(bitmap)
        styleChanged()
    }

    public fun getInfoWindowAnchorU(): Float = mMarkerOptions.infoWindowAnchorU

    public fun getInfoWindowAnchorV(): Float = mMarkerOptions.infoWindowAnchorV

    public fun setInfoWindowAnchor(
        infoWindowAnchorU: Float,
        infoWindowAnchorV: Float,
    ) {
        mMarkerOptions.infoWindowAnchor(infoWindowAnchorU, infoWindowAnchorV)
        styleChanged()
    }

    override fun getRotation(): Float = mMarkerOptions.rotation

    public fun setRotation(rotation: Float) {
        setMarkerRotation(rotation)
        styleChanged()
    }

    public fun getSnippet(): String? = mMarkerOptions.snippet

    public fun setSnippet(snippet: String?) {
        mMarkerOptions.snippet(snippet)
        styleChanged()
    }

    public fun getTitle(): String? = mMarkerOptions.title

    public fun setTitle(title: String?) {
        mMarkerOptions.title(title)
        styleChanged()
    }

    override fun isVisible(): Boolean = mMarkerOptions.isVisible

    override fun setVisible(visible: Boolean) {
        mMarkerOptions.visible(visible)
        styleChanged()
    }

    public fun getZIndex(): Float = mMarkerOptions.zIndex

    public fun setZIndex(zIndex: Float) {
        mMarkerOptions.zIndex(zIndex)
        styleChanged()
    }

    private fun styleChanged() {
        setChanged()
        notifyObservers()
    }

    public fun toMarkerOptions(): MarkerOptions =
        MarkerOptions().apply {
            alpha(mMarkerOptions.alpha)
            anchor(mMarkerOptions.anchorU, mMarkerOptions.anchorV)
            draggable(mMarkerOptions.isDraggable)
            flat(mMarkerOptions.isFlat)
            icon(mMarkerOptions.icon)
            infoWindowAnchor(mMarkerOptions.infoWindowAnchorU, mMarkerOptions.infoWindowAnchorV)
            rotation(mMarkerOptions.rotation)
            snippet(mMarkerOptions.snippet)
            title(mMarkerOptions.title)
            visible(mMarkerOptions.isVisible)
            zIndex(mMarkerOptions.zIndex)
        }

    override fun toString(): String =
        StringBuilder("PointStyle{")
            .apply {
                append("\n geometry type=").append(GEOMETRY_TYPE.contentToString())
                append(",\n alpha=").append(getAlpha())
                append(",\n anchor U=").append(getAnchorU())
                append(",\n anchor V=").append(getAnchorV())
                append(",\n draggable=").append(isDraggable())
                append(",\n flat=").append(isFlat())
                append(",\n info window anchor U=").append(getInfoWindowAnchorU())
                append(",\n info window anchor V=").append(getInfoWindowAnchorV())
                append(",\n rotation=").append(getRotation())
                append(",\n snippet=").append(getSnippet())
                append(",\n title=").append(getTitle())
                append(",\n visible=").append(isVisible())
                append(",\n z index=").append(getZIndex())
                append("\n}\n")
            }.toString()

    companion object {
        private val GEOMETRY_TYPE = arrayOf("Point", "MultiPoint", "GeometryCollection")
    }
}
