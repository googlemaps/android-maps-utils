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
package com.google.maps.android

import kotlin.math.PI
import kotlin.test.Test
import kotlin.test.assertEquals

class MathUtilTest {
    @Test
    fun testClamp() {
        assertEquals(1.0, MathUtil.clamp(1.0, 0.0, 2.0), DELTA)
        assertEquals(0.0, MathUtil.clamp(-1.0, 0.0, 2.0), DELTA)
        assertEquals(2.0, MathUtil.clamp(3.0, 0.0, 2.0), DELTA)
    }

    @Test
    fun testWrap() {
        assertEquals(1.0, MathUtil.wrap(1.0, 0.0, 2.0), DELTA)
        assertEquals(1.0, MathUtil.wrap(3.0, 0.0, 2.0), DELTA)
        assertEquals(1.0, MathUtil.wrap(-1.0, 0.0, 2.0), DELTA)
    }

    @Test
    fun testMod() {
        assertEquals(1.0, MathUtil.mod(1.0, 2.0), DELTA)
        assertEquals(1.0, MathUtil.mod(3.0, 2.0), DELTA)
        assertEquals(1.0, MathUtil.mod(-1.0, 2.0), DELTA)
    }

    @Test
    fun testMercator() {
        assertEquals(0.0, MathUtil.mercator(0.0), DELTA)
        assertEquals(Double.POSITIVE_INFINITY, MathUtil.mercator(PI / 2))
        assertEquals(Double.NEGATIVE_INFINITY, MathUtil.mercator(-PI / 2))
    }

    @Test
    fun testInverseMercator() {
        assertEquals(0.0, MathUtil.inverseMercator(0.0), DELTA)
        assertEquals(PI / 2, MathUtil.inverseMercator(Double.POSITIVE_INFINITY), DELTA)
        assertEquals(-PI / 2, MathUtil.inverseMercator(Double.NEGATIVE_INFINITY), DELTA)
    }

    @Test
    fun testHav() {
        assertEquals(0.0, MathUtil.hav(0.0), DELTA)
        assertEquals(1.0, MathUtil.hav(PI), DELTA)
        assertEquals(0.5, MathUtil.hav(PI / 2), DELTA)
    }

    @Test
    fun testArcHav() {
        assertEquals(0.0, MathUtil.arcHav(0.0), DELTA)
        assertEquals(PI, MathUtil.arcHav(1.0), DELTA)
        assertEquals(PI / 2, MathUtil.arcHav(0.5), DELTA)
    }

    @Test
    fun testSinFromHav() {
        assertEquals(0.0, MathUtil.sinFromHav(0.0), DELTA)
        assertEquals(0.0, MathUtil.sinFromHav(1.0), DELTA)
        assertEquals(1.0, MathUtil.sinFromHav(0.5), DELTA)
    }

    @Test
    fun testHavFromSin() {
        assertEquals(0.0, MathUtil.havFromSin(0.0), DELTA)
        assertEquals(0.5, MathUtil.havFromSin(1.0), DELTA)
    }

    @Test
    fun testSinSumFromHav() {
        assertEquals(0.0, MathUtil.sinSumFromHav(0.0, 0.0), DELTA)
        assertEquals(1.0, MathUtil.sinSumFromHav(0.5, 0.0), DELTA)
        assertEquals(1.0, MathUtil.sinSumFromHav(0.0, 0.5), DELTA)
    }

    @Test
    fun testHavDistance() {
        assertEquals(0.0, MathUtil.havDistance(0.0, 0.0, 0.0), DELTA)
        assertEquals(1.0, MathUtil.havDistance(0.0, PI, 0.0), DELTA)
    }

    companion object {
        private const val DELTA = 1e-15
    }
}
