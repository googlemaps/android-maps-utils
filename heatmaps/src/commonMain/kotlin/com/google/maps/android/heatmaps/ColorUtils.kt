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
package com.google.maps.android.heatmaps

import kotlin.math.floor
import kotlin.math.roundToInt

/**
 * Multiplatform replacements for the `android.graphics.Color` operations the heatmap gradient
 * uses. The HSV conversions reproduce the Android/Skia implementations exactly (as of Android O),
 * which GradientTest verifies against hardcoded Android color values.
 */
internal object ColorUtils {
    fun alpha(color: Int): Int = color ushr 24

    fun red(color: Int): Int = (color shr 16) and 0xFF

    fun green(color: Int): Int = (color shr 8) and 0xFF

    fun blue(color: Int): Int = color and 0xFF

    fun argb(alpha: Int, red: Int, green: Int, blue: Int): Int =
        (alpha shl 24) or (red shl 16) or (green shl 8) or blue

    fun rgbToHsv(red: Int, green: Int, blue: Int, hsv: FloatArray) {
        require(hsv.size >= 3) { "3 components required for hsv" }
        val max = maxOf(red, green, blue)
        val min = minOf(red, green, blue)
        val delta = max - min

        val v = max / 255f
        if (delta == 0) {
            hsv[0] = 0f
            hsv[1] = 0f
            hsv[2] = v
            return
        }

        val s = delta / max.toFloat()
        var h =
            when (max) {
                red -> (green - blue) / delta.toFloat()
                green -> 2f + (blue - red) / delta.toFloat()
                else -> 4f + (red - green) / delta.toFloat()
            }
        h *= 60f
        if (h < 0f) {
            h += 360f
        }

        hsv[0] = h
        hsv[1] = s
        hsv[2] = v
    }

    fun hsvToColor(alpha: Int, hsv: FloatArray): Int {
        require(hsv.size >= 3) { "3 components required for hsv" }
        val h = hsv[0]
        val s = hsv[1].coerceIn(0f, 1f)
        val v = hsv[2].coerceIn(0f, 1f)

        if (s <= 0f) {
            val unit = (v * 255f).roundToInt()
            return argb(alpha, unit, unit, unit)
        }

        val hx = if (h < 0f || h >= 360f) 0f else h / 60f
        val w = floor(hx).toInt()
        val f = hx - w

        val p = v * (1f - s)
        val q = v * (1f - s * f)
        val t = v * (1f - s * (1f - f))

        val (r, g, b) =
            when (w) {
                0 -> Triple(v, t, p)
                1 -> Triple(q, v, p)
                2 -> Triple(p, v, t)
                3 -> Triple(p, q, v)
                4 -> Triple(t, p, v)
                else -> Triple(v, p, q)
            }

        return argb(alpha, (r * 255f).roundToInt(), (g * 255f).roundToInt(), (b * 255f).roundToInt())
    }
}
