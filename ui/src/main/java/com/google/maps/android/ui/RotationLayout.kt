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
import android.graphics.Canvas
import android.util.AttributeSet
import android.widget.FrameLayout

/**
 * RotationLayout rotates the contents of the layout by multiples of 90 degrees.
 *
 * May not work with padding.
 */
class RotationLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : FrameLayout(context, attrs, defStyle) {
    private var rotation = 0

    public override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        if (rotation == 1 || rotation == 3) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec)
            setMeasuredDimension(measuredHeight, measuredWidth)
        } else {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        }
    }

    /**
     * @param degrees the rotation, in degrees.
     */
    fun setViewRotation(degrees: Int) {
        rotation = (degrees + 360) % 360 / 90
    }

    public override fun dispatchDraw(canvas: Canvas) {
        if (rotation == 0) {
            super.dispatchDraw(canvas)
            return
        }
        if (rotation == 1) {
            canvas.translate(width.toFloat(), 0f)
            canvas.rotate(90f, width / 2f, 0f)
            canvas.translate(height / 2f, width / 2f)
        } else if (rotation == 2) {
            canvas.rotate(180f, width / 2f, height / 2f)
        } else {
            canvas.translate(0f, height.toFloat())
            canvas.rotate(270f, width / 2f, 0f)
            canvas.translate(height / 2f, -width / 2f)
        }
        super.dispatchDraw(canvas)
    }
}
