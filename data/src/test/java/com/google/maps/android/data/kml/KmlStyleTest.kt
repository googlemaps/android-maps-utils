/*
 * Copyright 2026 Google LLC
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
package com.google.maps.android.data.kml

import android.graphics.Color
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.common.truth.Truth.assertThat
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.slot
import io.mockk.unmockkStatic
import io.mockk.verify
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@Suppress("DEPRECATION")
@RunWith(RobolectricTestRunner::class)
class KmlStyleTest {
    private val hues = mutableListOf<Float>()

    @Before
    fun setUp() {
        mockkStatic(BitmapDescriptorFactory::class)
        val hue = slot<Float>()
        every { BitmapDescriptorFactory.defaultMarker(capture(hue)) } answers {
            hues += hue.captured
            mockk()
        }
    }

    @After
    fun tearDown() {
        unmockkStatic(BitmapDescriptorFactory::class)
    }

    @Test
    fun defaults() {
        val style = KmlStyle()

        assertThat(style.hasFill()).isTrue()
        assertThat(style.hasOutline()).isTrue()
        assertThat(style.getIconScale()).isEqualTo(1.0)
        assertThat(style.getIconUrl()).isNull()
        assertThat(style.getStyleId()).isNull()
        assertThat(style.hasBalloonStyle()).isFalse()
        assertThat(style.isIconRandomColorMode()).isFalse()
        assertThat(style.isLineRandomColorMode()).isFalse()
        assertThat(style.isPolyRandomColorMode()).isFalse()
        assertThat(style.isStyleSet("fillColor")).isFalse()
        assertThat(style.getPolylineOptions().isClickable).isTrue()
        assertThat(style.getPolygonOptions().isClickable).isTrue()
    }

    @Test
    fun `fill color converts KML aabbggrr to ARGB`() {
        val style = KmlStyle()

        // KML colors are alpha, blue, green, red: this is half-transparent green.
        style.setFillColor("7f00ff00")

        assertThat(style.getPolygonOptions().fillColor).isEqualTo(0x7F00FF00)
        assertThat(style.isStyleSet("fillColor")).isTrue()
    }

    @Test
    fun `fill color reads red from the last two digits`() {
        val style = KmlStyle()

        style.setFillColor("ff0000ff")

        assertThat(style.getPolygonOptions().fillColor).isEqualTo(Color.RED)
    }

    @Test
    fun `six digit colors are bbggrr and opaque`() {
        val style = KmlStyle()

        style.setFillColor("0000ff")

        assertThat(style.getPolygonOptions().fillColor).isEqualTo(Color.RED)
    }

    @Test
    fun `surrounding whitespace in colors is ignored`() {
        val style = KmlStyle()

        style.setFillColor("  ff00ff00\n")

        assertThat(style.getPolygonOptions().fillColor).isEqualTo(Color.GREEN)
    }

    @Test
    fun `outline color and width apply to lines and polygon strokes`() {
        val style = KmlStyle()

        style.setOutlineColor("ffff0000")
        style.setWidth(4f)

        assertThat(style.getPolylineOptions().color).isEqualTo(Color.BLUE)
        assertThat(style.getPolylineOptions().width).isEqualTo(4f)
        assertThat(style.getPolygonOptions().strokeColor).isEqualTo(Color.BLUE)
        assertThat(style.getPolygonOptions().strokeWidth).isEqualTo(4f)
        assertThat(style.isStyleSet("outlineColor")).isTrue()
        assertThat(style.isStyleSet("width")).isTrue()
    }

    @Test
    fun `polygon without fill keeps the default transparent fill`() {
        val style = KmlStyle()
        style.setFillColor("ff00ff00")

        style.setFill(false)

        assertThat(style.hasFill()).isFalse()
        assertThat(style.getPolygonOptions().fillColor).isEqualTo(0)
    }

    @Test
    fun `polygon without outline has no stroke`() {
        val style = KmlStyle()
        style.setOutlineColor("ffff0000")
        style.setWidth(4f)

        style.setOutline(false)

        assertThat(style.hasOutline()).isFalse()
        assertThat(style.getPolygonOptions().strokeWidth).isEqualTo(0f)
        assertThat(style.isStyleSet("outline")).isTrue()
    }

    @Test
    fun `marker color sets the default marker hue`() {
        val style = KmlStyle()

        style.setMarkerColor("ffff0000")

        verify { BitmapDescriptorFactory.defaultMarker(240f) }
        assertThat(style.mMarkerColor).isEqualTo(240f)
        assertThat(style.isStyleSet("markerColor")).isTrue()
    }

    @Test
    fun `heading and fraction hotspot end up in the marker options`() {
        val style = KmlStyle()

        style.setHeading(90f)
        style.setHotSpot(0.25f, 0.75f, "fraction", "fraction")

        val options = style.getMarkerOptions()
        assertThat(options.rotation).isEqualTo(90f)
        assertThat(options.anchorU).isEqualTo(0.25f)
        assertThat(options.anchorV).isEqualTo(0.75f)
        assertThat(style.isStyleSet("heading")).isTrue()
        assertThat(style.isStyleSet("hotSpot")).isTrue()
    }

    @Test
    fun `hotspot units other than fraction fall back to the bottom center`() {
        val style = KmlStyle()

        style.setHotSpot(10f, 20f, "pixels", "insetPixels")

        val options = style.getMarkerOptions()
        assertThat(options.anchorU).isEqualTo(0.5f)
        assertThat(options.anchorV).isEqualTo(1.0f)
    }

    @Test
    fun `marker options are a copy`() {
        val style = KmlStyle()
        style.setHeading(45f)

        val first = style.getMarkerOptions()
        first.rotation(180f)

        assertThat(style.getMarkerOptions().rotation).isEqualTo(45f)
    }

    @Test
    fun `color modes are random only for the value random`() {
        val style = KmlStyle()

        style.setIconColorMode("random")
        style.setLineColorMode("normal")
        style.setPolyColorMode("random")

        assertThat(style.isIconRandomColorMode()).isTrue()
        assertThat(style.isLineRandomColorMode()).isFalse()
        assertThat(style.isPolyRandomColorMode()).isTrue()
        assertThat(style.isStyleSet("iconColorMode")).isTrue()
        assertThat(style.isStyleSet("lineColorMode")).isTrue()
        assertThat(style.isStyleSet("polyColorMode")).isTrue()
    }

    @Test
    fun `balloon text, icon and id setters`() {
        val style = KmlStyle()

        style.setInfoWindowText("Hello")
        style.setIconUrl("https://example.com/icon.png")
        style.setIconScale(2.5)
        style.setStyleId("#highlight")

        assertThat(style.hasBalloonStyle()).isTrue()
        assertThat(style.getBalloonOptions()).containsExactly("text", "Hello")
        assertThat(style.getIconUrl()).isEqualTo("https://example.com/icon.png")
        assertThat(style.getIconScale()).isEqualTo(2.5)
        assertThat(style.getStyleId()).isEqualTo("#highlight")
        assertThat(style.isStyleSet("iconUrl")).isTrue()
        assertThat(style.isStyleSet("iconScale")).isTrue()
        assertThat(style.toString()).contains("style id=#highlight")
    }

    @Test
    fun `random color keeps zero components at zero and never brightens`() {
        val color = Color.rgb(0, 200, 0)

        repeat(50) {
            val random = KmlStyle.computeRandomColor(color)
            assertThat(Color.red(random)).isEqualTo(0)
            assertThat(Color.blue(random)).isEqualTo(0)
            assertThat(Color.green(random)).isLessThan(200)
        }
    }

    @Test
    fun `random color of black is black`() {
        assertThat(KmlStyle.computeRandomColor(Color.BLACK)).isEqualTo(Color.BLACK)
    }

    /**
     * Random icon color mode applies a random scale to the marker's ARGB color, so a green
     * marker stays a shade of green (or black, once scaled to zero).
     */
    @Test
    fun `random icon color mode keeps the marker hue`() {
        val style = KmlStyle()
        style.setMarkerColor("ff00ff00")
        style.setIconColorMode("random")
        hues.clear()

        repeat(20) { style.getMarkerOptions() }

        // A random linear scale of pure green is still green, or black once scaled to zero.
        assertThat(hues).isNotEmpty()
        hues.forEach { assertThat(it).isAnyOf(120f, 0f) }
    }
}
