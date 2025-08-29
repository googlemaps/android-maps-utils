package com.google.maps.android.ui

import android.content.Context
import android.graphics.Bitmap
import android.widget.TextView
import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class IconGeneratorTest {

    private lateinit var iconGenerator: IconGenerator
    private lateinit var context: Context

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        iconGenerator = IconGenerator(context)
    }

    @Test
    fun testMakeIcon() {
        val icon = iconGenerator.makeIcon("Test")
        assertThat(icon).isNotNull()
        assertThat(icon.width).isGreaterThan(0)
        assertThat(icon.height).isGreaterThan(0)
    }

    @Test
    fun testSetContentView() {
        val textView = TextView(context)
        textView.text = "Custom View"
        iconGenerator.setContentView(textView)
        val icon: Bitmap = iconGenerator.makeIcon()
        assertThat(icon).isNotNull()
        assertThat(icon.width).isGreaterThan(0)
        assertThat(icon.height).isGreaterThan(0)
    }

    @Test
    fun testSetRotation() {
        iconGenerator.setRotation(90)
        assertThat(iconGenerator.getAnchorU()).isWithin(1e-6f).of(0.0f)
        assertThat(iconGenerator.getAnchorV()).isWithin(1e-6f).of(0.5f)
        val icon = iconGenerator.makeIcon("Rotated")
        assertThat(icon).isNotNull()
    }

    @Test
    fun testSetContentRotation() {
        iconGenerator.setContentRotation(90)
        val icon = iconGenerator.makeIcon("Content Rotated")
        assertThat(icon).isNotNull()
    }

    @Test
    fun testSetStyle() {
        iconGenerator.setStyle(IconGenerator.STYLE_RED)
        // Hard to test the color, but we can check that it doesn't crash
        val icon = iconGenerator.makeIcon("Styled")
        assertThat(icon).isNotNull()
    }
}