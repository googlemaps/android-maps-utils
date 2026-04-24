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
package com.google.maps.android.ui

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorFilter
import android.graphics.PixelFormat
import android.graphics.PorterDuff
import android.graphics.Rect
import android.graphics.drawable.Drawable
import androidx.core.content.ContextCompat

/**
 * Draws a bubble with a shadow, filled with any color.
 */
internal class BubbleDrawable(context: Context) : Drawable() {
    private val shadow: Drawable? = ContextCompat.getDrawable(context, R.drawable.amu_bubble_shadow)
    private val mask: Drawable? = ContextCompat.getDrawable(context, R.drawable.amu_bubble_mask)
    private var color: Int = Color.WHITE

    fun setColor(color: Int) {
        this.color = color
    }

    override fun draw(canvas: Canvas) {
        mask?.draw(canvas)
        canvas.drawColor(color, PorterDuff.Mode.SRC_IN)
        shadow?.draw(canvas)
    }

    override fun setAlpha(alpha: Int) {
        throw UnsupportedOperationException()
    }

    override fun setColorFilter(cf: ColorFilter?) {
        throw UnsupportedOperationException()
    }

    @Deprecated("Deprecated in Java",
        ReplaceWith("PixelFormat.TRANSLUCENT", "android.graphics.PixelFormat")
    )
    override fun getOpacity(): Int {
        return PixelFormat.TRANSLUCENT
    }

    override fun setBounds(left: Int, top: Int, right: Int, bottom: Int) {
        mask?.setBounds(left, top, right, bottom)
        shadow?.setBounds(left, top, right, bottom)
    }

    override fun setBounds(bounds: Rect) {
        mask?.bounds = bounds
        shadow?.bounds = bounds
    }

    override fun getPadding(padding: Rect): Boolean {
        return mask?.getPadding(padding) ?: false
    }
}
