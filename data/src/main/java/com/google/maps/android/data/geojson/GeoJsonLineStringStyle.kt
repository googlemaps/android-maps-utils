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

import com.google.android.gms.maps.model.Cap
import com.google.android.gms.maps.model.PatternItem
import com.google.android.gms.maps.model.PolylineOptions
import com.google.maps.android.data.Style

/**
 * A class that allows for GeoJsonLineString objects to be styled and for these styles to be
 * translated into a PolylineOptions object.
 */
@Deprecated("Use the new platform-agnostic data layer and renderer instead.")
public class GeoJsonLineStringStyle :
    Style(),
    GeoJsonStyle {
    override fun getGeometryType(): Array<String> = GEOMETRY_TYPE

    public var color: Int
        get() = mPolylineOptions.color
        set(color) {
            mPolylineOptions.color(color)
            styleChanged()
        }

    public fun isClickable(): Boolean = mPolylineOptions.isClickable

    public fun setClickable(clickable: Boolean) {
        mPolylineOptions.clickable(clickable)
        styleChanged()
    }

    public fun isGeodesic(): Boolean = mPolylineOptions.isGeodesic

    public fun setGeodesic(geodesic: Boolean) {
        mPolylineOptions.geodesic(geodesic)
        styleChanged()
    }

    public fun getWidth(): Float = mPolylineOptions.width

    public fun setWidth(width: Float) {
        setLineStringWidth(width)
        styleChanged()
    }

    public fun getZIndex(): Float = mPolylineOptions.zIndex

    public fun setZIndex(zIndex: Float) {
        mPolylineOptions.zIndex(zIndex)
        styleChanged()
    }

    override fun isVisible(): Boolean = mPolylineOptions.isVisible

    override fun setVisible(visible: Boolean) {
        mPolylineOptions.visible(visible)
        styleChanged()
    }

    private fun styleChanged() {
        setChanged()
        notifyObservers()
    }

    public fun toPolylineOptions(): PolylineOptions =
        PolylineOptions().apply {
            color(mPolylineOptions.color)
            clickable(mPolylineOptions.isClickable)
            geodesic(mPolylineOptions.isGeodesic)
            visible(mPolylineOptions.isVisible)
            width(mPolylineOptions.width)
            zIndex(mPolylineOptions.zIndex)
            pattern(getPattern())
            startCap(getStartCap())
            endCap(getEndCap())
        }

    override fun toString(): String =
        StringBuilder("LineStringStyle{")
            .apply {
                append("\n geometry type=").append(GEOMETRY_TYPE.contentToString())
                append(",\n color=").append(color)
                append(",\n clickable=").append(isClickable())
                append(",\n geodesic=").append(isGeodesic())
                append(",\n visible=").append(isVisible())
                append(",\n width=").append(getWidth())
                append(",\n z index=").append(getZIndex())
                append(",\n pattern=").append(getPattern())
                append(",\n startCap=").append(getStartCap())
                append(",\n endCap=").append(getEndCap())
                append("\n}\n")
            }.toString()

    public fun getPattern(): List<PatternItem>? = mPolylineOptions.pattern

    public fun setPattern(pattern: List<PatternItem>?) {
        mPolylineOptions.pattern(pattern)
        styleChanged()
    }

    public fun setStartCap(cap: Cap) {
        mPolylineOptions.startCap(cap)
        styleChanged()
    }

    public fun setEndCap(cap: Cap) {
        mPolylineOptions.endCap(cap)
        styleChanged()
    }

    public fun getStartCap(): Cap = mPolylineOptions.startCap

    public fun getEndCap(): Cap = mPolylineOptions.endCap

    companion object {
        private val GEOMETRY_TYPE = arrayOf("LineString", "MultiLineString", "GeometryCollection")
    }
}
