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

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import android.widget.TextView
import kotlin.math.max

/**
 * This class is extending from TextView to avoid introducing App Compat dependencies. Android Studio might show an error here or
 * not, depending on the Inspection Settings. It's not really an error, just a warning.
 */
@SuppressLint("AppCompatCustomView")
class SquareTextView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : TextView(context, attrs, defStyle) {
    private var offsetTop = 0
    private var offsetLeft = 0

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val width = measuredWidth
        val height = measuredHeight
        val dimension = max(width, height)
        if (width > height) {
            offsetTop = width - height
            offsetLeft = 0
        } else {
            offsetTop = 0
            offsetLeft = height - width
        }
        setMeasuredDimension(dimension, dimension)
    }

    override fun draw(canvas: Canvas) {
        canvas.translate(offsetLeft / 2f, offsetTop / 2f)
        super.draw(canvas)
    }
}
