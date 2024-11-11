/*
 * Copyright 2013 Google Inc.
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
package com.google.maps.android.projection

import com.google.android.gms.maps.model.LatLng
import kotlin.math.atan
import kotlin.math.exp
import kotlin.math.ln
import kotlin.math.sin
import com.google.maps.android.MathUtil.toRadians
import com.google.maps.android.MathUtil.toDegrees

class SphericalMercatorProjection(private val mWorldWidth: Double) {
    @Suppress("deprecation")
    fun toPoint(latLng: LatLng): Point {
        val x = latLng.longitude / 360 + .5
        val siny = sin(latLng.latitude.toRadians())
        val y = 0.5 * ln((1 + siny) / (1 - siny)) / -(2 * Math.PI) + .5

        return Point(x * mWorldWidth, y * mWorldWidth)
    }

    fun toLatLng(point: com.google.maps.android.geometry.Point): LatLng {
        val x = point.x / mWorldWidth - 0.5
        val lng = x * 360

        val y = .5 - (point.y / mWorldWidth)
        val lat = 90 - (atan(exp(-y * 2 * Math.PI)) * 2).toDegrees()

        return LatLng(lat, lng)
    }
}
