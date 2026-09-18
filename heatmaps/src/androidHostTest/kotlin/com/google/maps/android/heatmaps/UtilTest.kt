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
package com.google.maps.android.heatmaps

import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.geometry.Bounds
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/** Tests for heatmap utility functions */
@RunWith(RobolectricTestRunner::class)
class UtilTest {
    @Test
    fun testGenerateKernel() {
        val testKernel = HeatmapTileProvider.generateKernel(5, 1.5)
        val expectedKernel =
            doubleArrayOf(
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
                0.0038659201394728076,
            )

        assertArrayEquals(expectedKernel, testKernel, 0.0)
    }

    @Test
    fun testConvolveCorners() {
        /*
        1 0 0 0 1
        0 0 0 0 0
        0 0 0 0 0
        0 0 0 0 0
        1 0 0 0 1
        */
        val grid = Array(5) { DoubleArray(5) }
        grid[0][0] = 1.0
        grid[4][4] = 1.0
        grid[4][0] = 1.0
        grid[0][4] = 1.0
        val testKernel = doubleArrayOf(0.5, 1.0, 0.5)
        val convolved = HeatmapTileProvider.convolve(grid, testKernel)
        val expected =
            arrayOf(
                doubleArrayOf(0.25, 0.0, 0.25),
                doubleArrayOf(0.0, 0.0, 0.0),
                doubleArrayOf(0.25, 0.0, 0.25),
            )
        assertArrayEquals(expected, convolved)
    }

    @Test
    fun testConvolveEdges() {
        /*
        0 0 1 0 0
        0 0 0 0 0
        1 0 0 0 1
        0 0 0 0 0
        0 0 1 0 0
        */
        val grid = Array(5) { DoubleArray(5) }
        grid[0][2] = 1.0
        grid[2][0] = 1.0
        grid[2][4] = 1.0
        grid[4][2] = 1.0
        val testKernel = doubleArrayOf(0.5, 1.0, 0.5)
        val convolved = HeatmapTileProvider.convolve(grid, testKernel)
        val expected =
            arrayOf(
                doubleArrayOf(0.5, 0.5, 0.5),
                doubleArrayOf(0.5, 0.0, 0.5),
                doubleArrayOf(0.5, 0.5, 0.5),
            )
        assertArrayEquals(expected, convolved)
    }

    @Test
    fun testConvolveCentre() {
        /*
        0 0 0 0 0
        0 0 1 0 0
        0 1 2 1 0
        0 0 1 0 0
        0 0 0 0 0
        */
        val grid = Array(5) { DoubleArray(5) }
        grid[2][2] = 2.0
        grid[2][1] = 1.0
        grid[1][2] = 1.0
        grid[2][3] = 1.0
        grid[3][2] = 1.0
        val testKernel = doubleArrayOf(0.5, 1.0, 0.5)
        val convolved = HeatmapTileProvider.convolve(grid, testKernel)
        val expected =
            arrayOf(
                doubleArrayOf(1.5, 2.5, 1.5),
                doubleArrayOf(2.5, 4.0, 2.5),
                doubleArrayOf(1.5, 2.5, 1.5),
            )
        assertArrayEquals(expected, convolved)
    }

    @Test
    fun testGetBounds() {
        /*
        y
        ^
        |  3
        |     1
        |        2
        ------------> x
         */

        val data = ArrayList<WeightedLatLng>()
        val first = WeightedLatLng(LatLng(10.0, 20.0))
        data.add(first)
        val x1 = first.point.x
        val y1 = first.point.y

        var bounds = HeatmapTileProvider.getBounds(data)
        var expected = Bounds(x1, x1, y1, y1)

        assertTrue(bounds.contains(expected) && expected.contains(bounds))

        val second = WeightedLatLng(LatLng(20.0, 30.0))
        data.add(second)
        val x2 = second.point.x
        val y2 = second.point.y

        bounds = HeatmapTileProvider.getBounds(data)
        expected = Bounds(x1, x2, y2, y1)

        assertTrue(bounds.contains(expected) && expected.contains(bounds))

        val third = WeightedLatLng(LatLng(5.0, 10.0))
        data.add(third)
        val x3 = third.point.x
        val y3 = third.point.y

        bounds = HeatmapTileProvider.getBounds(data)
        expected = Bounds(x3, x2, y2, y3)
        assertTrue(bounds.contains(expected) && expected.contains(bounds))
    }
}
