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

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.geometry.Bounds;

import org.junit.Test;

import java.util.ArrayList;

import static org.junit.Assert.*;

/**
 * Tests for heatmap utility functions
 */
public class UtilTest {
    @Test
    public void testGenerateKernel() {
        double[] testKernel = HeatmapTileProvider.generateKernel(5, 1.5);
        double[] expectedKernel = {
                0.0038659201394728076,
                0.028565500784550377,
                0.1353352832366127,
                0.41111229050718745,
                0.8007374029168081,
                1.0,
                0.8007374029168081,
                0.41111229050718745,
                0.1353352832366127,
                0.028565500784550377,
                0.0038659201394728076
        };

        assertArrayEquals(expectedKernel, testKernel, 0.0);
    }

    @Test
    public void testConvolveCorners() {
        /*
        1 0 0 0 1
        0 0 0 0 0
        0 0 0 0 0
        0 0 0 0 0
        1 0 0 0 1
        */
        double[][] grid = new double[5][5];
        grid[0][0] = 1;
        grid[4][4] = 1;
        grid[4][0] = 1;
        grid[0][4] = 1;
        double[] testKernel = {0.5, 1, 0.5};
        double[][] convolved = HeatmapTileProvider.convolve(grid, testKernel);
        double[][] expected = {{0.25, 0, 0.25}, {0, 0, 0}, {0.25, 0, 0.25}};
        assertArrayEquals(expected, convolved);
    }

    @Test
    public void testConvolveEdges() {
        /*
        0 0 1 0 0
        0 0 0 0 0
        1 0 0 0 1
        0 0 0 0 0
        0 0 1 0 0
        */
        double[][] grid = new double[5][5];
        grid[0][2] = 1;
        grid[2][0] = 1;
        grid[2][4] = 1;
        grid[4][2] = 1;
        double[] testKernel = {0.5, 1, 0.5};
        double[][] convolved = HeatmapTileProvider.convolve(grid, testKernel);
        double[][] expected = {{0.5, 0.5, 0.5}, {0.5, 0, 0.5}, {0.5, 0.5, 0.5}};
        assertArrayEquals(expected, convolved);
    }

    @Test
    public void testConvolveCentre() {
        /*
        0 0 0 0 0
        0 0 1 0 0
        0 1 2 1 0
        0 0 1 0 0
        0 0 0 0 0
        */
        double[][] grid = new double[5][5];
        grid[2][2] = 2;
        grid[2][1] = 1;
        grid[1][2] = 1;
        grid[2][3] = 1;
        grid[3][2] = 1;
        double[] testKernel = {0.5, 1, 0.5};
        double[][] convolved = HeatmapTileProvider.convolve(grid, testKernel);
        double[][] expected = {{1.5, 2.5, 1.5}, {2.5, 4.0, 2.5}, {1.5, 2.5, 1.5}};
        assertArrayEquals(expected, convolved);
    }

    @Test
    public void testGetBounds() {

        /*
        y
        ^
        |  3
        |     1
        |        2
        ------------> x
         */

        ArrayList<WeightedLatLng> data = new ArrayList<>();
        WeightedLatLng first = new WeightedLatLng(new LatLng(10, 20));
        data.add(first);
        double x1 = first.getPoint().x;
        double y1 = first.getPoint().y;

        Bounds bounds = HeatmapTileProvider.getBounds(data);
        Bounds expected = new Bounds(x1, x1, y1, y1);

        assertTrue(bounds.contains(expected) && expected.contains(bounds));

        WeightedLatLng second = new WeightedLatLng(new LatLng(20, 30));
        data.add(second);
        double x2 = second.getPoint().x;
        double y2 = second.getPoint().y;

        bounds = HeatmapTileProvider.getBounds(data);
        expected = new Bounds(x1, x2, y2, y1);

        assertTrue(bounds.contains(expected) && expected.contains(bounds));

        WeightedLatLng third = new WeightedLatLng(new LatLng(5, 10));
        data.add(third);
        double x3 = third.getPoint().x;
        double y3 = third.getPoint().y;

        bounds = HeatmapTileProvider.getBounds(data);
        expected = new Bounds(x3, x2, y2, y3);
        assertTrue(bounds.contains(expected) && expected.contains(bounds));
    }
}
