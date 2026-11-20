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
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class ColorUtilsTest {
    @Test
    fun testChannelsRoundTripThroughArgb() {
        val color = ColorUtils.argb(0x12, 0x34, 0x56, 0x78)

        assertEquals(0x12345678, color)
        assertEquals(0x12, ColorUtils.alpha(color))
        assertEquals(0x34, ColorUtils.red(color))
        assertEquals(0x56, ColorUtils.green(color))
        assertEquals(0x78, ColorUtils.blue(color))
    }

    @Test
    fun testAlphaOfOpaqueColorIsUnsigned() {
        // An opaque color is a negative Int; alpha must not sign-extend.
        val opaqueWhite = ColorUtils.argb(255, 255, 255, 255)

        assertEquals(-1, opaqueWhite)
        assertEquals(255, ColorUtils.alpha(opaqueWhite))
    }

    @Test
    fun testRgbToHsvPrimaries() {
        assertContentEquals(floatArrayOf(0f, 1f, 1f), hsv(255, 0, 0))
        assertContentEquals(floatArrayOf(120f, 1f, 1f), hsv(0, 255, 0))
        assertContentEquals(floatArrayOf(240f, 1f, 1f), hsv(0, 0, 255))
        assertContentEquals(floatArrayOf(60f, 1f, 1f), hsv(255, 255, 0))
        assertContentEquals(floatArrayOf(300f, 1f, 1f), hsv(255, 0, 255))
    }

    @Test
    fun testRgbToHsvGrays() {
        assertContentEquals(floatArrayOf(0f, 0f, 0f), hsv(0, 0, 0))
        assertContentEquals(floatArrayOf(0f, 0f, 1f), hsv(255, 255, 255))
        assertContentEquals(floatArrayOf(0f, 0f, 128 / 255f), hsv(128, 128, 128))
    }

    @Test
    fun testHsvToColorPrimaries() {
        assertEquals(ColorUtils.argb(255, 255, 0, 0), ColorUtils.hsvToColor(255, floatArrayOf(0f, 1f, 1f)))
        assertEquals(ColorUtils.argb(255, 0, 255, 0), ColorUtils.hsvToColor(255, floatArrayOf(120f, 1f, 1f)))
        assertEquals(ColorUtils.argb(255, 0, 0, 255), ColorUtils.hsvToColor(255, floatArrayOf(240f, 1f, 1f)))
        assertEquals(ColorUtils.argb(10, 0, 255, 255), ColorUtils.hsvToColor(10, floatArrayOf(180f, 1f, 1f)))
    }

    @Test
    fun testHsvToColorClampsAndWrapsOutOfRangeValues() {
        // A hue of 360 or more is treated as 0, as on Android.
        assertEquals(ColorUtils.argb(255, 255, 0, 0), ColorUtils.hsvToColor(255, floatArrayOf(360f, 1f, 1f)))
        // Saturation and value are clamped to [0, 1].
        assertEquals(ColorUtils.argb(255, 255, 0, 0), ColorUtils.hsvToColor(255, floatArrayOf(0f, 2f, 2f)))
        // Zero saturation is a gray whatever the hue.
        assertEquals(
            ColorUtils.argb(255, 128, 128, 128),
            ColorUtils.hsvToColor(255, floatArrayOf(200f, 0f, 128 / 255f)),
        )
    }

    @Test
    fun testHsvRoundTrip() {
        val hsv = FloatArray(3)
        for (r in 0..255 step 15) {
            for (g in 0..255 step 15) {
                for (b in 0..255 step 15) {
                    ColorUtils.rgbToHsv(r, g, b, hsv)
                    assertEquals(ColorUtils.argb(255, r, g, b), ColorUtils.hsvToColor(255, hsv), "rgb($r, $g, $b)")
                }
            }
        }
    }

    @Test
    fun testRejectsShortHsvArray() {
        assertFailsWith<IllegalArgumentException> { ColorUtils.rgbToHsv(0, 0, 0, FloatArray(2)) }
        assertFailsWith<IllegalArgumentException> { ColorUtils.hsvToColor(255, FloatArray(2)) }
    }

    private fun hsv(red: Int, green: Int, blue: Int): FloatArray = FloatArray(3).also {
        ColorUtils.rgbToHsv(red, green, blue, it)
    }
}
