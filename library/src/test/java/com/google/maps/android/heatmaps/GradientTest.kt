/*
 * Copyright 2025 Google LLC
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

package com.google.maps.android.heatmaps

import android.graphics.Color
import android.os.Build
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class GradientTest {
    @Test
    fun testInterpolateColor() {
        // Expect itself
        assertThat(Gradient.interpolateColor(Color.RED, Color.RED, 0.5f)).isEqualTo(Color.RED)
        assertThat(Gradient.interpolateColor(Color.BLUE, Color.BLUE, 0.5f)).isEqualTo(Color.BLUE)
        assertThat(Gradient.interpolateColor(Color.GREEN, Color.GREEN, 0.5f)).isEqualTo(Color.GREEN)

        // Expect first to be returned
        val result = Gradient.interpolateColor(Color.RED, Color.BLUE, 0f)
        assertThat(result).isEqualTo(Color.RED)

        // Expect second to be returned
        val result2 = Gradient.interpolateColor(Color.RED, Color.BLUE, 1f)
        assertThat(result2).isEqualTo(Color.BLUE)

        // Expect same value (should wraparound correctly, shortest path both times)
        assertThat(Gradient.interpolateColor(Color.BLUE, Color.RED, 0.5f))
            .isEqualTo(Gradient.interpolateColor(Color.RED, Color.BLUE, 0.5f))
        assertThat(Gradient.interpolateColor(Color.BLUE, Color.RED, 0.8f))
            .isEqualTo(Gradient.interpolateColor(Color.RED, Color.BLUE, 0.2f))
        assertThat(Gradient.interpolateColor(Color.BLUE, Color.RED, 0.2f))
            .isEqualTo(Gradient.interpolateColor(Color.RED, Color.BLUE, 0.8f))

        // Due to issue with Color.RGBToHSV() below only works on Android O and greater (#573)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            assertThat(Gradient.interpolateColor(Color.RED, Color.BLUE, 0.2f)).isEqualTo(-65434)
            assertThat(Gradient.interpolateColor(Color.RED, Color.BLUE, 0.5f)).isEqualTo(Color.MAGENTA)
            assertThat(Gradient.interpolateColor(Color.RED, Color.BLUE, 0.8f)).isEqualTo(-10092289)
            assertThat(Gradient.interpolateColor(Color.RED, Color.GREEN, 0.5f)).isEqualTo(Color.YELLOW)
            assertThat(Gradient.interpolateColor(Color.BLUE, Color.GREEN, 0.5f)).isEqualTo(Color.CYAN)
        }
    }

    @Test
    fun testSimpleColorMap() {
        val colors = intArrayOf(Color.RED, Color.BLUE)
        val startPoints = floatArrayOf(0f, 1.0f)
        val g = Gradient(colors, startPoints, 2)
        val colorMap = g.generateColorMap(1.0)
        assertThat(colorMap[0]).isEqualTo(Color.RED)
        assertThat(colorMap[1]).isEqualTo(Gradient.interpolateColor(Color.RED, Color.BLUE, 0.5f))
    }

    @Test
    fun testLargerColorMap() {
        val colors = intArrayOf(Color.RED, Color.GREEN)
        val startPoints = floatArrayOf(0f, 1.0f)
        val g = Gradient(colors, startPoints, 10)
        val colorMap = g.generateColorMap(1.0)
        assertThat(colorMap[0]).isEqualTo(Color.RED)
        for (i in 1..9) {
            assertThat(colorMap[i]).isEqualTo(Gradient.interpolateColor(Color.RED, Color.GREEN, i * 0.1f))
        }
    }

    @Test
    fun testOpacityInterpolation() {
        val colors = intArrayOf(Color.argb(0, 0, 255, 0), Color.GREEN, Color.RED)
        val startPoints = floatArrayOf(0f, 0.2f, 1f)
        val g = Gradient(colors, startPoints, 10)
        var colorMap = g.generateColorMap(1.0)
        assertThat(colorMap[0]).isEqualTo(Color.argb(0, 0, 255, 0))
        assertThat(colorMap[1]).isEqualTo(Color.argb(127, 0, 255, 0))
        assertThat(colorMap[2]).isEqualTo(Color.GREEN)
        assertThat(colorMap[3]).isEqualTo(Gradient.interpolateColor(Color.GREEN, Color.RED, 0.125f))
        assertThat(colorMap[4]).isEqualTo(Gradient.interpolateColor(Color.GREEN, Color.RED, 0.25f))
        assertThat(colorMap[5]).isEqualTo(Gradient.interpolateColor(Color.GREEN, Color.RED, 0.375f))
        assertThat(colorMap[6]).isEqualTo(Gradient.interpolateColor(Color.GREEN, Color.RED, 0.5f))
        assertThat(colorMap[7]).isEqualTo(Gradient.interpolateColor(Color.GREEN, Color.RED, 0.625f))
        assertThat(colorMap[8]).isEqualTo(Gradient.interpolateColor(Color.GREEN, Color.RED, 0.75f))
        assertThat(colorMap[9]).isEqualTo(Gradient.interpolateColor(Color.GREEN, Color.RED, 0.875f))
        colorMap = g.generateColorMap(0.5)
        assertThat(colorMap[0]).isEqualTo(Color.argb(0, 0, 255, 0))
        assertThat(colorMap[1]).isEqualTo(Color.argb(63, 0, 255, 0))
        assertThat(colorMap[2]).isEqualTo(Color.argb(127, 0, 255, 0))
    }

    @Test
    fun testMoreColorsThanColorMap() {
        val colors = intArrayOf(Color.argb(0, 0, 255, 0), Color.GREEN, Color.RED, Color.BLUE)
        val startPoints = floatArrayOf(0f, 0.2f, 0.5f, 1f)
        val g = Gradient(colors, startPoints, 2)
        val colorMap = g.generateColorMap(1.0)
        assertThat(colorMap[0]).isEqualTo(Color.GREEN)
        assertThat(colorMap[1]).isEqualTo(Color.RED)
    }
}