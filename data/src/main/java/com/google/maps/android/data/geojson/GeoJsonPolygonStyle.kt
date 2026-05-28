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

import com.google.android.gms.maps.model.PatternItem
import com.google.android.gms.maps.model.PolygonOptions
import com.google.maps.android.data.Style

/**
 * A class that allows for GeoJsonPolygon objects to be styled and for these styles to be
 * translated into a PolygonOptions object.
 */
@Deprecated("Use the new platform-agnostic data layer and renderer instead.")
public class GeoJsonPolygonStyle :
    Style(),
    GeoJsonStyle {
    override fun getGeometryType(): Array<String> = GEOMETRY_TYPE

    public var fillColor: Int
        get() = mPolygonOptions.fillColor
        set(fillColor) {
            setPolygonFillColor(fillColor)
            styleChanged()
        }

    public fun isGeodesic(): Boolean = mPolygonOptions.isGeodesic

    public fun setGeodesic(geodesic: Boolean) {
        mPolygonOptions.geodesic(geodesic)
        styleChanged()
    }

    public fun getStrokeColor(): Int = mPolygonOptions.strokeColor

    public fun setStrokeColor(strokeColor: Int) {
        mPolygonOptions.strokeColor(strokeColor)
        styleChanged()
    }

    public fun getStrokeJointType(): Int = mPolygonOptions.strokeJointType

    public fun setStrokeJointType(strokeJointType: Int) {
        mPolygonOptions.strokeJointType(strokeJointType)
        styleChanged()
    }

    public fun getStrokePattern(): List<PatternItem>? = mPolygonOptions.strokePattern

    public fun setStrokePattern(strokePattern: List<PatternItem>?) {
        mPolygonOptions.strokePattern(strokePattern)
        styleChanged()
    }

    public fun getStrokeWidth(): Float = mPolygonOptions.strokeWidth

    public fun setStrokeWidth(strokeWidth: Float) {
        setPolygonStrokeWidth(strokeWidth)
        styleChanged()
    }

    public fun getZIndex(): Float = mPolygonOptions.zIndex

    public fun setZIndex(zIndex: Float) {
        mPolygonOptions.zIndex(zIndex)
        styleChanged()
    }

    override fun isVisible(): Boolean = mPolygonOptions.isVisible

    override fun setVisible(visible: Boolean) {
        mPolygonOptions.visible(visible)
        styleChanged()
    }

    private fun styleChanged() {
        setChanged()
        notifyObservers()
    }

    public fun toPolygonOptions(): PolygonOptions =
        PolygonOptions().apply {
            fillColor(mPolygonOptions.fillColor)
            geodesic(mPolygonOptions.isGeodesic)
            strokeColor(mPolygonOptions.strokeColor)
            strokeJointType(mPolygonOptions.strokeJointType)
            strokePattern(mPolygonOptions.strokePattern)
            strokeWidth(mPolygonOptions.strokeWidth)
            visible(mPolygonOptions.isVisible)
            zIndex(mPolygonOptions.zIndex)
            clickable(mPolygonOptions.isClickable)
        }

    override fun toString(): String =
        StringBuilder("PolygonStyle{")
            .apply {
                append("\n geometry type=").append(GEOMETRY_TYPE.contentToString())
                append(",\n fill color=").append(fillColor)
                append(",\n geodesic=").append(isGeodesic())
                append(",\n stroke color=").append(getStrokeColor())
                append(",\n stroke joint type=").append(getStrokeJointType())
                append(",\n stroke pattern=").append(getStrokePattern())
                append(",\n stroke width=").append(getStrokeWidth())
                append(",\n visible=").append(isVisible())
                append(",\n z index=").append(getZIndex())
                append(",\n clickable=").append(isClickable())
                append("\n}\n")
            }.toString()

    public fun setClickable(clickable: Boolean) {
        mPolygonOptions.clickable(clickable)
        styleChanged()
    }

    public fun isClickable(): Boolean = mPolygonOptions.isClickable

    companion object {
        private val GEOMETRY_TYPE = arrayOf("Polygon", "MultiPolygon", "GeometryCollection")
    }
}
