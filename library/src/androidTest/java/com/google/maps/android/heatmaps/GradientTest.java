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

import org.junit.Test;

import android.graphics.Color;
import android.os.Build;

import static android.graphics.Color.BLUE;
import static android.graphics.Color.GREEN;
import static android.graphics.Color.RED;
import static org.junit.Assert.assertEquals;

public class GradientTest {
    @Test
    public void testInterpolateColor() {
        // Expect itself
        assertEquals(RED, Gradient.interpolateColor(RED, RED, 0.5f));
        assertEquals(BLUE, Gradient.interpolateColor(BLUE, BLUE, 0.5f));
        assertEquals(GREEN, Gradient.interpolateColor(GREEN, GREEN, 0.5f));

        // Expect first to be returned
        int result = Gradient.interpolateColor(RED, BLUE, 0);
        assertEquals(RED, result);

        // Expect second to be returned
        result = Gradient.interpolateColor(RED, BLUE, 1);
        assertEquals(BLUE, result);

        // Expect same value (should wraparound correctly, shortest path both times)
        assertEquals(
                Gradient.interpolateColor(BLUE, RED, 0.5f),
                Gradient.interpolateColor(RED, BLUE, 0.5f));
        assertEquals(
                Gradient.interpolateColor(BLUE, RED, 0.8f),
                Gradient.interpolateColor(RED, BLUE, 0.2f));
        assertEquals(
                Gradient.interpolateColor(BLUE, RED, 0.2f),
                Gradient.interpolateColor(RED, BLUE, 0.8f));

        // Due to issue with Color.RGBToHSV() below only works on Android O and greater (#573)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            assertEquals(-65434, Gradient.interpolateColor(RED, BLUE, 0.2f));
            assertEquals(Color.MAGENTA, Gradient.interpolateColor(RED, BLUE, 0.5f));
            assertEquals(-10092289, Gradient.interpolateColor(RED, BLUE, 0.8f));
            assertEquals(Color.YELLOW, Gradient.interpolateColor(RED, GREEN, 0.5f));
            assertEquals(Color.CYAN, Gradient.interpolateColor(BLUE, GREEN, 0.5f));
        }
    }

    @Test
    public void testSimpleColorMap() {
        int[] colors = {RED, BLUE};
        float[] startPoints = {0f, 1.0f};

        Gradient g = new Gradient(colors, startPoints, 2);
        int[] colorMap = g.generateColorMap(1.0);
        assertEquals(RED, colorMap[0]);
        assertEquals(Gradient.interpolateColor(RED, BLUE, 0.5f), colorMap[1]);
    }

    @Test
    public void testLargerColorMap() {
        int[] colors = {RED, GREEN};
        float[] startPoints = {0f, 1.0f};

        Gradient g = new Gradient(colors, startPoints, 10);
        int[] colorMap = g.generateColorMap(1.0);
        assertEquals(RED, colorMap[0]);
        for (int i = 1; i < 10; i++) {
            assertEquals(Gradient.interpolateColor(RED, GREEN, (i * 0.1f)), colorMap[i]);
        }
    }

    @Test
    public void testOpacityInterpolation() {
        int[] colors = {Color.argb(0, 0, 255, 0), GREEN, RED};
        float[] startPoints = {0f, 0.2f, 1f};
        Gradient g = new Gradient(colors, startPoints, 10);
        int[] colorMap = g.generateColorMap(1.0);
        assertEquals(Color.argb(0, 0, 255, 0), colorMap[0]);
        assertEquals(Color.argb(127, 0, 255, 0), colorMap[1]);
        assertEquals(GREEN, colorMap[2]);
        assertEquals(Gradient.interpolateColor(GREEN, RED, 0.125f), colorMap[3]);
        assertEquals(Gradient.interpolateColor(GREEN, RED, 0.25f), colorMap[4]);
        assertEquals(Gradient.interpolateColor(GREEN, RED, 0.375f), colorMap[5]);
        assertEquals(Gradient.interpolateColor(GREEN, RED, 0.5f), colorMap[6]);
        assertEquals(Gradient.interpolateColor(GREEN, RED, 0.625f), colorMap[7]);
        assertEquals(Gradient.interpolateColor(GREEN, RED, 0.75f), colorMap[8]);
        assertEquals(Gradient.interpolateColor(GREEN, RED, 0.875f), colorMap[9]);

        colorMap = g.generateColorMap(0.5);
        assertEquals(Color.argb(0, 0, 255, 0), colorMap[0]);
        assertEquals(Color.argb(63, 0, 255, 0), colorMap[1]);
        assertEquals(Color.argb(127, 0, 255, 0), colorMap[2]);
    }

    @Test
    public void testMoreColorsThanColorMap() {
        int[] colors = {Color.argb(0, 0, 255, 0), GREEN, RED, BLUE};
        float[] startPoints = {0f, 0.2f, 0.5f, 1f};
        Gradient g = new Gradient(colors, startPoints, 2);
        int[] colorMap = g.generateColorMap(1.0);
        assertEquals(GREEN, colorMap[0]);
        assertEquals(RED, colorMap[1]);
    }
}
