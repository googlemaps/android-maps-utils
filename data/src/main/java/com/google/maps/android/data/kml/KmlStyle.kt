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

import android.graphics.Color
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolygonOptions
import com.google.android.gms.maps.model.PolylineOptions
import com.google.maps.android.data.Style
import java.util.Random

/**
 * Represents the defined styles in the KML document.
 */
@Deprecated("Use the new platform-agnostic data layer and renderer instead.")
public class KmlStyle : Style() {
    private val mBalloonOptions = HashMap<String, String>()
    private val mStylesSet = HashSet<String>()
    private var mFill = true
    private var mOutline = true
    private var mIconUrl: String? = null
    private var mScale = 1.0
    private var mStyleId: String? = null
    private var mIconRandomColorMode = false
    private var mLineRandomColorMode = false
    private var mPolyRandomColorMode = false
    internal var mMarkerColor = 0f

    public fun setInfoWindowText(text: String) {
        mBalloonOptions["text"] = text
    }

    public fun getStyleId(): String? = mStyleId

    public fun setStyleId(styleId: String?) {
        mStyleId = styleId
    }

    public fun isStyleSet(style: String): Boolean = mStylesSet.contains(style)

    public fun hasFill(): Boolean = mFill

    public fun setFill(fill: Boolean) {
        mFill = fill
    }

    public fun getIconScale(): Double = mScale

    public fun setIconScale(scale: Double) {
        mScale = scale
        mStylesSet.add("iconScale")
    }

    public fun hasOutline(): Boolean = mOutline

    public fun hasBalloonStyle(): Boolean = mBalloonOptions.isNotEmpty()

    public fun setOutline(outline: Boolean) {
        mOutline = outline
        mStylesSet.add("outline")
    }

    public fun getIconUrl(): String? = mIconUrl

    public fun setIconUrl(iconUrl: String?) {
        mIconUrl = iconUrl
        mStylesSet.add("iconUrl")
    }

    public fun setFillColor(color: String) {
        val polygonColorNum = Color.parseColor("#" + convertColor(color))
        setPolygonFillColor(polygonColorNum)
        mStylesSet.add("fillColor")
    }

    public fun setMarkerColor(color: String) {
        val integerColor = Color.parseColor("#" + convertColor(color))
        mMarkerColor = getHueValue(integerColor)
        mMarkerOptions.icon(BitmapDescriptorFactory.defaultMarker(mMarkerColor))
        mStylesSet.add("markerColor")
    }

    public fun setHeading(heading: Float) {
        setMarkerRotation(heading)
        mStylesSet.add("heading")
    }

    public fun setHotSpot(
        x: Float,
        y: Float,
        xUnits: String,
        yUnits: String,
    ) {
        setMarkerHotSpot(x, y, xUnits, yUnits)
        mStylesSet.add("hotSpot")
    }

    public fun setIconColorMode(colorMode: String) {
        mIconRandomColorMode = colorMode == "random"
        mStylesSet.add("iconColorMode")
    }

    public fun isIconRandomColorMode(): Boolean = mIconRandomColorMode

    public fun setLineColorMode(colorMode: String) {
        mLineRandomColorMode = colorMode == "random"
        mStylesSet.add("lineColorMode")
    }

    public fun isLineRandomColorMode(): Boolean = mLineRandomColorMode

    public fun setPolyColorMode(colorMode: String) {
        mPolyRandomColorMode = colorMode == "random"
        mStylesSet.add("polyColorMode")
    }

    public fun isPolyRandomColorMode(): Boolean = mPolyRandomColorMode

    public fun setOutlineColor(color: String) {
        mPolylineOptions.color(Color.parseColor("#" + convertColor(color)))
        mPolygonOptions.strokeColor(Color.parseColor("#" + convertColor(color)))
        mStylesSet.add("outlineColor")
    }

    public fun setWidth(width: Float) {
        setLineStringWidth(width)
        setPolygonStrokeWidth(width)
        mStylesSet.add("width")
    }

    public fun getBalloonOptions(): HashMap<String, String> = mBalloonOptions

    public fun getMarkerOptions(): MarkerOptions {
        val newMarkerOption = MarkerOptions()
        newMarkerOption.rotation(mMarkerOptions.rotation)
        newMarkerOption.anchor(mMarkerOptions.anchorU, mMarkerOptions.anchorV)
        if (mIconRandomColorMode) {
            val hue = getHueValue(computeRandomColor(mMarkerColor.toInt()))
            mMarkerOptions.icon(BitmapDescriptorFactory.defaultMarker(hue))
        }
        newMarkerOption.icon(mMarkerOptions.icon)
        return newMarkerOption
    }

    public fun getPolylineOptions(): PolylineOptions =
        PolylineOptions().apply {
            color(mPolylineOptions.color)
            width(mPolylineOptions.width)
            clickable(mPolylineOptions.isClickable)
        }

    public fun getPolygonOptions(): PolygonOptions =
        PolygonOptions().apply {
            if (mFill) {
                fillColor(mPolygonOptions.fillColor)
            }
            var strokeW = 0.0f
            if (mOutline) {
                strokeColor(mPolygonOptions.strokeColor)
                strokeW = mPolygonOptions.strokeWidth
            }
            strokeWidth(strokeW)
            clickable(mPolygonOptions.isClickable)
        }

    override fun toString(): String =
        StringBuilder("Style")
            .apply {
                append("{")
                append("\n balloon options=").append(mBalloonOptions)
                append(",\n fill=").append(mFill)
                append(",\n outline=").append(mOutline)
                append(",\n icon url=").append(mIconUrl)
                append(",\n scale=").append(mScale)
                append(",\n style id=").append(mStyleId)
                append("\n}\n")
            }.toString()

    companion object {
        private const val HSV_VALUES = 3
        private const val HUE_VALUE = 0

        private fun getHueValue(integerColor: Int): Float {
            val hsvValues = FloatArray(HSV_VALUES)
            Color.colorToHSV(integerColor, hsvValues)
            return hsvValues[HUE_VALUE]
        }

        private fun convertColor(color: String): String {
            val trimmed = color.trim()
            return if (trimmed.length > 6) {
                trimmed.substring(0, 2) + trimmed.substring(6, 8) +
                    trimmed.substring(4, 6) + trimmed.substring(2, 4)
            } else {
                trimmed.substring(4, 6) + trimmed.substring(2, 4) +
                    trimmed.substring(0, 2)
            }
        }

        public fun computeRandomColor(color: Int): Int {
            val random = Random()
            var red = Color.red(color)
            var green = Color.green(color)
            var blue = Color.blue(color)
            if (red != 0) {
                red = random.nextInt(red)
            }
            if (blue != 0) {
                blue = random.nextInt(blue)
            }
            if (green != 0) {
                green = random.nextInt(green)
            }
            return Color.rgb(red, green, blue)
        }
    }
}
