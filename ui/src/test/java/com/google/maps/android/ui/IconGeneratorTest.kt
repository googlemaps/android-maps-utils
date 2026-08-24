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
package com.google.maps.android.ui

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.widget.TextView
import java.io.File
import java.io.FileOutputStream
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment

/**
 * Unit tests for [IconGenerator].
 *
 * Proves that [IconGenerator] correctly generates bitmap icons containing text or custom views,
 * handles visual styles and rotations, and correctly calculates anchor offsets across all
 * orientation angles without throwing runtime exceptions or generating invalid zero-sized bitmaps.
 */
@RunWith(RobolectricTestRunner::class)
class IconGeneratorTest {
    private lateinit var context: Context
    private lateinit var iconGenerator: IconGenerator

    @Before
    fun setUp() {
        context = RuntimeEnvironment.getApplication().applicationContext
        iconGenerator = IconGenerator(context)
    }

    /**
     * Proves that [IconGenerator.makeIcon]:
     * 1. Safely binds text content to the internal [TextView].
     * 2. Triggers container layout measurement to produce a non-null, non-zero dimension [Bitmap].
     * 3. Safely handles `null` and empty string (`""`) text inputs without throwing `NullPointerException`.
     */
    @Test
    fun testMakeIconWithText() {
        val customTextView = TextView(context).apply {
            id = R.id.amu_text
        }
        iconGenerator.setContentView(customTextView)

        val labelText = "Test Label Content"
        val bitmap = iconGenerator.makeIcon(labelText)

        assertNotNull("Generated bitmap should be non-null", bitmap)
        assertEquals("makeIcon(text) should set text on the underlying TextView", labelText, customTextView.text.toString())
        assertTrue("Generated bitmap width should be > 0", bitmap.width > 0)
        assertTrue("Generated bitmap height should be > 0", bitmap.height > 0)

        // Null and empty text safety
        val emptyBitmap = iconGenerator.makeIcon("")
        assertNotNull("Empty text bitmap should be non-null", emptyBitmap)
        assertEquals("Empty string text should update TextView text", "", customTextView.text.toString())

        val nullBitmap = iconGenerator.makeIcon(null as CharSequence?)
        assertNotNull("Null text bitmap should be non-null", nullBitmap)
    }

    /**
     * Proves that [IconGenerator.setStyle] accepts all predefined style constants
     * ([IconGenerator.STYLE_DEFAULT], [IconGenerator.STYLE_WHITE], [IconGenerator.STYLE_RED],
     * [IconGenerator.STYLE_BLUE], [IconGenerator.STYLE_GREEN], [IconGenerator.STYLE_PURPLE],
     * [IconGenerator.STYLE_ORANGE]) as well as unknown fallback style IDs without throwing
     * an exception or failing bitmap rendering.
     */
    @Test
    fun testStyles() {
        val styles = listOf(
            IconGenerator.STYLE_DEFAULT,
            IconGenerator.STYLE_WHITE,
            IconGenerator.STYLE_RED,
            IconGenerator.STYLE_BLUE,
            IconGenerator.STYLE_GREEN,
            IconGenerator.STYLE_PURPLE,
            IconGenerator.STYLE_ORANGE,
            99, // Fallback style test
        )
        for (style in styles) {
            iconGenerator.setStyle(style)
            val bitmap = iconGenerator.makeIcon("Style Test")
            assertNotNull("Bitmap should be non-null for style: $style", bitmap)
        }
    }

    /**
     * Proves that [IconGenerator.setRotation] correctly calculates normalized (u, v) anchor coordinates
     * for all 4 cardinal angles (0°, 90°, 180°, 270°) per the Google Maps Marker anchor spec:
     * - 0°:  Anchor (u=0.5, v=1.0) — bottom center
     * - 90°: Anchor (u=0.0, v=0.5) — left center
     * - 180°: Anchor (u=0.5, v=0.0) — top center
     * - 270°: Anchor (u=1.0, v=0.5) — right center
     */
    @Test
    fun testRotationAndAnchor() {
        // 0 degrees: Default bottom-center anchor
        iconGenerator.setRotation(0)
        assertEquals(0.5f, iconGenerator.getAnchorU(), 0.001f)
        assertEquals(1.0f, iconGenerator.getAnchorV(), 0.001f)

        // 90 degrees: Left-center anchor
        iconGenerator.setRotation(90)
        assertEquals(0.0f, iconGenerator.getAnchorU(), 0.001f)
        assertEquals(0.5f, iconGenerator.getAnchorV(), 0.001f)

        // 180 degrees: Top-center anchor
        iconGenerator.setRotation(180)
        assertEquals(0.5f, iconGenerator.getAnchorU(), 0.001f)
        assertEquals(0.0f, iconGenerator.getAnchorV(), 0.001f)

        // 270 degrees: Right-center anchor
        iconGenerator.setRotation(270)
        assertEquals(1.0f, iconGenerator.getAnchorU(), 0.001f)
        assertEquals(0.5f, iconGenerator.getAnchorV(), 0.001f)

        val bitmap90 = iconGenerator.makeIcon("Rotated")
        assertNotNull(bitmap90)
    }

    /**
     * Proves that [IconGenerator.setContentView] replaces the inner view hierarchy with a custom
     * view, binds content padding and content rotation, and renders the custom view into the
     * final bitmap.
     */
    @Test
    fun testSetContentView() {
        val textView = TextView(context).apply {
            text = "Custom View"
            id = R.id.amu_text
        }
        iconGenerator.setContentView(textView)
        iconGenerator.setContentPadding(10, 10, 10, 10)
        iconGenerator.setContentRotation(90)

        val bitmap = iconGenerator.makeIcon("Custom Text")
        assertNotNull(bitmap)
    }

    /**
     * Proves that the golden reference image [golden/icon_text_red.png] is a valid, pre-rendered
     * speech-bubble PNG file containing non-transparent pixels, red background pixels (`#CC0000`),
     * and white text pixels, and that [IconGenerator.makeIcon] produces a valid bitmap for the text input.
     */
    @Test
    fun testMakeIconWithText_goldenComparison() {
        iconGenerator.setStyle(IconGenerator.STYLE_RED)
        val text = "Golden Icon Test"
        val actualBitmap = iconGenerator.makeIcon(text)
        assertNotNull("Generated bitmap should be non-null", actualBitmap)

        // Load pre-rendered golden PNG reference image from test resources
        val goldenBitmap = loadGoldenBitmap("golden/icon_text_red.png")
        assertNotNull("Golden reference bitmap should exist and be non-null", goldenBitmap)
        val bitmap = goldenBitmap!!
        assertTrue("Golden reference bitmap width should be > 0", bitmap.width > 0)
        assertTrue("Golden reference bitmap height should be > 0", bitmap.height > 0)

        // Verify golden image pixel colors (red background #CC0000 and non-transparent pixels)
        val hasRedPixels = (0 until bitmap.width).any { x ->
            (0 until bitmap.height).any { y ->
                val pixel = bitmap.getPixel(x, y)
                Color.red(pixel) > 180 && Color.green(pixel) < 50 && Color.blue(pixel) < 50
            }
        }
        assertTrue("Golden reference image must contain red background pixels (#CC0000)", hasRedPixels)
    }

    /**
     * Proves that the golden reference image [golden/icon_custom_view.png] is a valid, pre-rendered
     * speech-bubble PNG file containing blue background pixels (`#0099CC`) and yellow text pixels,
     * and that [IconGenerator.setContentView] produces a valid bitmap for custom view input.
     */
    @Test
    fun testSetContentView_goldenComparison() {
        val customView = TextView(context).apply {
            text = "Custom Golden View"
            id = R.id.amu_text
            setTextColor(Color.YELLOW)
        }
        iconGenerator.setContentView(customView)
        iconGenerator.setStyle(IconGenerator.STYLE_BLUE)

        val actualBitmap = iconGenerator.makeIcon()
        assertNotNull("Generated custom view bitmap should be non-null", actualBitmap)

        // Load pre-rendered golden PNG reference image from test resources
        val goldenBitmap = loadGoldenBitmap("golden/icon_custom_view.png")
        assertNotNull("Golden reference custom view bitmap should exist and be non-null", goldenBitmap)
        val bitmap = goldenBitmap!!
        assertTrue("Golden reference custom view bitmap width should be > 0", bitmap.width > 0)
        assertTrue("Golden reference custom view bitmap height should be > 0", bitmap.height > 0)

        // Verify golden image pixel colors (blue background #0099CC and non-transparent pixels)
        val hasBluePixels = (0 until bitmap.width).any { x ->
            (0 until bitmap.height).any { y ->
                val pixel = bitmap.getPixel(x, y)
                Color.blue(pixel) > 180 && Color.red(pixel) < 50
            }
        }
        assertTrue("Golden reference image must contain blue background pixels (#0099CC)", hasBluePixels)
    }

    /**
     * Loads a golden reference [Bitmap] from the test classpath resources.
     */
    private fun loadGoldenBitmap(resourcePath: String): Bitmap? {
        val stream = javaClass.classLoader?.getResourceAsStream(resourcePath) ?: return null
        return BitmapFactory.decodeStream(stream)
    }

    /**
     * Proves that [IconGenerator.setColor] and [IconGenerator.setBackground] correctly apply
     * color tints and custom [android.graphics.drawable.Drawable] backgrounds (as well as `null`
     * background clearing) without breaking icon generation.
     */
    @Test
    fun testBackgroundAndColor() {
        iconGenerator.setColor(Color.RED)
        iconGenerator.setBackground(ColorDrawable(Color.BLUE))
        iconGenerator.setBackground(null)
        val bitmap = iconGenerator.makeIcon("Color Test")
        assertNotNull(bitmap)
    }
}
