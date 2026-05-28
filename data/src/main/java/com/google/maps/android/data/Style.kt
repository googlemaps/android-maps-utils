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

import android.util.Log
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolygonOptions
import com.google.android.gms.maps.model.PolylineOptions
import java.util.Observable

/**
 * An abstraction that shares the common properties of KmlStyle, GeoJsonPointStyle,
 * GeoJsonLineStringStyle and GeoJsonPolygonStyle.
 */
public abstract class Style : Observable() {
    @JvmField
    internal var mMarkerOptions: MarkerOptions = MarkerOptions()

    @JvmField
    internal var mPolylineOptions: PolylineOptions = PolylineOptions().clickable(true)

    @JvmField
    internal var mPolygonOptions: PolygonOptions = PolygonOptions().clickable(true)

    public open fun getRotation(): Float = mMarkerOptions.rotation

    public open fun setMarkerRotation(rotation: Float) {
        mMarkerOptions.rotation(rotation)
    }

    public open fun setMarkerHotSpot(
        x: Float,
        y: Float,
        xUnits: String,
        yUnits: String,
    ) {
        var xAnchor = 0.5f
        var yAnchor = 1.0f

        if (xUnits == "fraction") {
            xAnchor = x
        } else {
            Log.w(LOG_TAG, "Hotspot xUnits other than \"fraction\" are not supported.")
        }
        if (yUnits == "fraction") {
            yAnchor = y
        } else {
            Log.w(LOG_TAG, "Hotspot yUnits other than \"fraction\" are not supported.")
        }

        mMarkerOptions.anchor(xAnchor, yAnchor)
    }

    public open fun setLineStringWidth(width: Float) {
        mPolylineOptions.width(width)
    }

    public open fun setPolygonStrokeWidth(strokeWidth: Float) {
        mPolygonOptions.strokeWidth(strokeWidth)
    }

    public open fun setPolygonFillColor(fillColor: Int) {
        mPolygonOptions.fillColor(fillColor)
    }

    companion object {
        private const val LOG_TAG = "Style"
    }
}
