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

import kotlin.test.Test
import kotlin.test.assertEquals

class GradientTest {
    @Test
    fun testInterpolateColor() {
        // Expect itself
        assertEquals(Color.RED, Gradient.interpolateColor(Color.RED, Color.RED, 0.5f))
        assertEquals(Color.BLUE, Gradient.interpolateColor(Color.BLUE, Color.BLUE, 0.5f))
        assertEquals(Color.GREEN, Gradient.interpolateColor(Color.GREEN, Color.GREEN, 0.5f))

        // Expect first to be returned
        val result = Gradient.interpolateColor(Color.RED, Color.BLUE, 0f)
        assertEquals(Color.RED, result)

        // Expect second to be returned
        val result2 = Gradient.interpolateColor(Color.RED, Color.BLUE, 1f)
        assertEquals(Color.BLUE, result2)

        // Expect same value (should wraparound correctly, shortest path both times)
        assertEquals(
            Gradient.interpolateColor(Color.RED, Color.BLUE, 0.5f),
            Gradient.interpolateColor(Color.BLUE, Color.RED, 0.5f),
        )
        assertEquals(
            Gradient.interpolateColor(Color.RED, Color.BLUE, 0.2f),
            Gradient.interpolateColor(Color.BLUE, Color.RED, 0.8f),
        )
        assertEquals(
            Gradient.interpolateColor(Color.RED, Color.BLUE, 0.8f),
            Gradient.interpolateColor(Color.BLUE, Color.RED, 0.2f),
        )

        // The values Color.RGBToHSV() gives on Android O and greater (#573). ColorUtils
        // reproduces them on every platform, so these hold everywhere.
        assertEquals(-65434, Gradient.interpolateColor(Color.RED, Color.BLUE, 0.2f))
        assertEquals(Color.MAGENTA, Gradient.interpolateColor(Color.RED, Color.BLUE, 0.5f))
        assertEquals(-10092289, Gradient.interpolateColor(Color.RED, Color.BLUE, 0.8f))
        assertEquals(Color.YELLOW, Gradient.interpolateColor(Color.RED, Color.GREEN, 0.5f))
        assertEquals(Color.CYAN, Gradient.interpolateColor(Color.BLUE, Color.GREEN, 0.5f))
    }

    @Test
    fun testSimpleColorMap() {
        val colors = intArrayOf(Color.RED, Color.BLUE)
        val startPoints = floatArrayOf(0f, 1.0f)
        val g = Gradient(colors, startPoints, 2)
        val colorMap = g.generateColorMap(1.0)
        assertEquals(Color.RED, colorMap[0])
        assertEquals(Gradient.interpolateColor(Color.RED, Color.BLUE, 0.5f), colorMap[1])
    }

    @Test
    fun testLargerColorMap() {
        val colors = intArrayOf(Color.RED, Color.GREEN)
        val startPoints = floatArrayOf(0f, 1.0f)
        val g = Gradient(colors, startPoints, 10)
        val colorMap = g.generateColorMap(1.0)
        assertEquals(Color.RED, colorMap[0])
        for (i in 1..9) {
            assertEquals(Gradient.interpolateColor(Color.RED, Color.GREEN, i * 0.1f), colorMap[i])
        }
    }

    @Test
    fun testOpacityInterpolation() {
        val colors = intArrayOf(ColorUtils.argb(0, 0, 255, 0), Color.GREEN, Color.RED)
        val startPoints = floatArrayOf(0f, 0.2f, 1f)
        val g = Gradient(colors, startPoints, 10)
        var colorMap = g.generateColorMap(1.0)
        assertEquals(ColorUtils.argb(0, 0, 255, 0), colorMap[0])
        assertEquals(ColorUtils.argb(127, 0, 255, 0), colorMap[1])
        assertEquals(Color.GREEN, colorMap[2])
        assertEquals(Gradient.interpolateColor(Color.GREEN, Color.RED, 0.125f), colorMap[3])
        assertEquals(Gradient.interpolateColor(Color.GREEN, Color.RED, 0.25f), colorMap[4])
        assertEquals(Gradient.interpolateColor(Color.GREEN, Color.RED, 0.375f), colorMap[5])
        assertEquals(Gradient.interpolateColor(Color.GREEN, Color.RED, 0.5f), colorMap[6])
        assertEquals(Gradient.interpolateColor(Color.GREEN, Color.RED, 0.625f), colorMap[7])
        assertEquals(Gradient.interpolateColor(Color.GREEN, Color.RED, 0.75f), colorMap[8])
        assertEquals(Gradient.interpolateColor(Color.GREEN, Color.RED, 0.875f), colorMap[9])
        colorMap = g.generateColorMap(0.5)
        assertEquals(ColorUtils.argb(0, 0, 255, 0), colorMap[0])
        assertEquals(ColorUtils.argb(63, 0, 255, 0), colorMap[1])
        assertEquals(ColorUtils.argb(127, 0, 255, 0), colorMap[2])
    }

    @Test
    fun testMoreColorsThanColorMap() {
        val colors = intArrayOf(ColorUtils.argb(0, 0, 255, 0), Color.GREEN, Color.RED, Color.BLUE)
        val startPoints = floatArrayOf(0f, 0.2f, 0.5f, 1f)
        val g = Gradient(colors, startPoints, 2)
        val colorMap = g.generateColorMap(1.0)
        assertEquals(Color.GREEN, colorMap[0])
        assertEquals(Color.RED, colorMap[1])
    }

    /** The `android.graphics.Color` constants these tests were written against. */
    private object Color {
        const val RED: Int = 0xFFFF0000.toInt()
        const val GREEN: Int = 0xFF00FF00.toInt()
        const val BLUE: Int = 0xFF0000FF.toInt()
        const val YELLOW: Int = 0xFFFFFF00.toInt()
        const val CYAN: Int = 0xFF00FFFF.toInt()
        const val MAGENTA: Int = 0xFFFF00FF.toInt()
    }
}
