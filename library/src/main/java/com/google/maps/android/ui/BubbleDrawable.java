/*
 * Copyright 2023 Google Inc.
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

package com.google.maps.android.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;

import com.google.maps.android.R;

import androidx.core.content.ContextCompat;

/**
 * Draws a bubble with a shadow, filled with any color.
 */
class BubbleDrawable extends Drawable {

    private final Drawable mShadow;
    private final Drawable mMask;
    private int mColor = Color.WHITE;

    public BubbleDrawable(Context context) {
        mMask = ContextCompat.getDrawable(context, R.drawable.amu_bubble_mask);
        mShadow = ContextCompat.getDrawable(context, R.drawable.amu_bubble_shadow);
    }

    public void setColor(int color) {
        mColor = color;
    }

    @Override
    public void draw(Canvas canvas) {
        mMask.draw(canvas);
        canvas.drawColor(mColor, PorterDuff.Mode.SRC_IN);
        mShadow.draw(canvas);
    }

    @Override
    public void setAlpha(int alpha) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setColorFilter(ColorFilter cf) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getOpacity() {
        return PixelFormat.TRANSLUCENT;
    }

    @Override
    public void setBounds(int left, int top, int right, int bottom) {
        mMask.setBounds(left, top, right, bottom);
        mShadow.setBounds(left, top, right, bottom);
    }

    @Override
    public void setBounds(Rect bounds) {
        mMask.setBounds(bounds);
        mShadow.setBounds(bounds);
    }

    @Override
    public boolean getPadding(Rect padding) {
        return mMask.getPadding(padding);
    }
}
