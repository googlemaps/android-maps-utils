/*
 * Copyright 2014 Google Inc.
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

package com.google.maps.android.heatmaps;

import android.graphics.Color;

import junit.framework.TestCase;

public class GradientTest extends TestCase {

    public void testInterpolateColor() {
        int red = Color.RED;
        int blue = Color.BLUE;
        int green = Color.GREEN;

        // Expect itself
        assertEquals(red, Gradient.interpolateColor(red, red, 0.5f));
        assertEquals(blue, Gradient.interpolateColor(blue, blue, 0.5f));
        assertEquals(green, Gradient.interpolateColor(green, green, 0.5f));

        // Expect first to be returned
        int result = Gradient.interpolateColor(red, blue, 0);
        assertEquals(red, result);

        // Expect second to be returned
        result = Gradient.interpolateColor(red, blue, 1);
        assertEquals(blue, result);

        // Expect same value (should wraparound correctly, shortest path both times)
        assertEquals(Gradient.interpolateColor(red, blue, 0.5f),
                Gradient.interpolateColor(blue, red, 0.5f));
        assertEquals(Gradient.interpolateColor(red, blue, 0.2f),
                Gradient.interpolateColor(blue, red, 0.8f));
        assertEquals(Gradient.interpolateColor(red, blue, 0.8f),
                Gradient.interpolateColor(blue, red, 0.2f));

        // Testing actual values now
        assertEquals(Gradient.interpolateColor(red, blue, 0.2f), -65433);
        assertEquals(Gradient.interpolateColor(red, blue, 0.5f), Color.MAGENTA);
        assertEquals(Gradient.interpolateColor(red, blue, 0.8f), -10092289);
        assertEquals(Gradient.interpolateColor(red, green, 0.5f), Color.YELLOW);
        assertEquals(Gradient.interpolateColor(blue, green, 0.5f), Color.CYAN);
    }

    public void testSimpleColorMap() {
        int[] colors = {Color.RED, Color.BLUE};
        float[] startPoints = {0f, 1.0f};

        Gradient g = new Gradient(colors, startPoints, 2);
        int[] colorMap = g.generateColorMap(1.0);
        assertEquals(colorMap[0], Color.RED);
        assertEquals(colorMap[1], Gradient.interpolateColor(Color.RED, Color.BLUE, 0.5f));
    }

    public void testLargerColorMap() {
        int[] colors = {Color.RED, Color.GREEN};
        float[] startPoints = {0f, 1.0f};

        Gradient g = new Gradient(colors, startPoints, 10);
        int[] colorMap = g.generateColorMap(1.0);
        assertEquals(colorMap[0], Color.RED);
        for (int i = 1; i < 10; i++) {
            assertEquals(colorMap[i], Gradient.interpolateColor(Color.RED, Color.GREEN, (i * 0.1f)));
        }
    }

    public void testOpacityInterpolation() {
        int[] colors = {
                Color.argb(0, 0, 255, 0),
                Color.GREEN,
                Color.RED
        };
        float[] startPoints = {
                0f, 0.2f, 1f
        };
        Gradient g = new Gradient(colors, startPoints, 10);
        int[] colorMap = g.generateColorMap(1.0);
        assertEquals(colorMap[0], Color.argb(0, 0, 255, 0));
        assertEquals(colorMap[1], Color.argb(127, 0, 255, 0));
        assertEquals(colorMap[2], Color.GREEN);
        assertEquals(colorMap[3], Gradient.interpolateColor(Color.GREEN, Color.RED, 0.125f));
        assertEquals(colorMap[4], Gradient.interpolateColor(Color.GREEN, Color.RED, 0.25f));
        assertEquals(colorMap[5], Gradient.interpolateColor(Color.GREEN, Color.RED, 0.375f));
        assertEquals(colorMap[6], Gradient.interpolateColor(Color.GREEN, Color.RED, 0.5f));
        assertEquals(colorMap[7], Gradient.interpolateColor(Color.GREEN, Color.RED, 0.625f));
        assertEquals(colorMap[8], Gradient.interpolateColor(Color.GREEN, Color.RED, 0.75f));
        assertEquals(colorMap[9], Gradient.interpolateColor(Color.GREEN, Color.RED, 0.875f));

        colorMap = g.generateColorMap(0.5);
        assertEquals(colorMap[0], Color.argb(0, 0, 255, 0));
        assertEquals(colorMap[1], Color.argb(63, 0, 255, 0));
        assertEquals(colorMap[2], Color.argb(127, 0, 255, 0));
    }

    public void testMoreColorsThanColorMap() {
        int[] colors = {
                Color.argb(0, 0, 255, 0),
                Color.GREEN,
                Color.RED,
                Color.BLUE
        };
        float[] startPoints = {
                0f, 0.2f, 0.5f, 1f
        };
        Gradient g = new Gradient(colors, startPoints, 2);
        int[] colorMap = g.generateColorMap(1.0);
        assertEquals(colorMap[0], Color.GREEN);
        assertEquals(colorMap[1], Color.RED);
    }
}
