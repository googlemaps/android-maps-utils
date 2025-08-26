/*
 * Copyright 2025 Google LLC
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

package com.google.maps.android;

import static com.google.common.truth.Truth.assertThat;

import org.junit.Test;

public class MathUtilTest {
    private static final double DELTA = 1e-15;

    @Test
    public void testClamp() {
        assertThat(MathUtil.clamp(1.0, 0.0, 2.0)).isWithin(DELTA).of(1.0);
        assertThat(MathUtil.clamp(-1.0, 0.0, 2.0)).isWithin(DELTA).of(0.0);
        assertThat(MathUtil.clamp(3.0, 0.0, 2.0)).isWithin(DELTA).of(2.0);
    }

    @Test
    public void testWrap() {
        assertThat(MathUtil.wrap(1.0, 0.0, 2.0)).isWithin(DELTA).of(1.0);
        assertThat(MathUtil.wrap(3.0, 0.0, 2.0)).isWithin(DELTA).of(1.0);
        assertThat(MathUtil.wrap(-1.0, 0.0, 2.0)).isWithin(DELTA).of(1.0);
    }

    @Test
    public void testMod() {
        assertThat(MathUtil.mod(1.0, 2.0)).isWithin(DELTA).of(1.0);
        assertThat(MathUtil.mod(3.0, 2.0)).isWithin(DELTA).of(1.0);
        assertThat(MathUtil.mod(-1.0, 2.0)).isWithin(DELTA).of(1.0);
    }

    @Test
    public void testMercator() {
        assertThat(MathUtil.mercator(0.0)).isWithin(DELTA).of(0.0);
        assertThat(MathUtil.mercator(Math.PI / 2)).isPositiveInfinity();
        assertThat(MathUtil.mercator(-Math.PI / 2)).isNegativeInfinity();
    }

    @Test
    public void testInverseMercator() {
        assertThat(MathUtil.inverseMercator(0.0)).isWithin(DELTA).of(0.0);
        assertThat(MathUtil.inverseMercator(Double.POSITIVE_INFINITY)).isWithin(DELTA).of(Math.PI / 2);
        assertThat(MathUtil.inverseMercator(Double.NEGATIVE_INFINITY)).isWithin(DELTA).of(-Math.PI / 2);
    }

    @Test
    public void testHav() {
        assertThat(MathUtil.hav(0.0)).isWithin(DELTA).of(0.0);
        assertThat(MathUtil.hav(Math.PI)).isWithin(DELTA).of(1.0);
        assertThat(MathUtil.hav(Math.PI / 2)).isWithin(DELTA).of(0.5);
    }

    @Test
    public void testArcHav() {
        assertThat(MathUtil.arcHav(0.0)).isWithin(DELTA).of(0.0);
        assertThat(MathUtil.arcHav(1.0)).isWithin(DELTA).of(Math.PI);
        assertThat(MathUtil.arcHav(0.5)).isWithin(DELTA).of(Math.PI / 2);
    }

    @Test
    public void testSinFromHav() {
        assertThat(MathUtil.sinFromHav(0.0)).isWithin(DELTA).of(0.0);
        assertThat(MathUtil.sinFromHav(1.0)).isWithin(DELTA).of(0.0);
        assertThat(MathUtil.sinFromHav(0.5)).isWithin(DELTA).of(1.0);
    }

    @Test
    public void testHavFromSin() {
        assertThat(MathUtil.havFromSin(0.0)).isWithin(DELTA).of(0.0);
        assertThat(MathUtil.havFromSin(1.0)).isWithin(DELTA).of(0.5);
    }

    @Test
    public void testSinSumFromHav() {
        assertThat(MathUtil.sinSumFromHav(0.0, 0.0)).isWithin(DELTA).of(0.0);
        assertThat(MathUtil.sinSumFromHav(0.5, 0.0)).isWithin(DELTA).of(1.0);
        assertThat(MathUtil.sinSumFromHav(0.0, 0.5)).isWithin(DELTA).of(1.0);
    }

    @Test
    public void testHavDistance() {
        assertThat(MathUtil.havDistance(0.0, 0.0, 0.0)).isWithin(DELTA).of(0.0);
        assertThat(MathUtil.havDistance(0.0, Math.PI, 0.0)).isWithin(DELTA).of(1.0);
    }
}