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
 *
 */

package com.google.maps.android.geometry

import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

internal class PointExtensionsTest {

    private lateinit var point: Point

    @BeforeTest
    fun setUp() {
        point = Point(1.0, 2.0)
    }

    @Test
    fun destructureX() {
        val (x, _) = point
        assertEquals(1.0, x, 1e-6)
    }

    @Test
    fun destructureY() {
        val (_, y) = point
        assertEquals(2.0, y, 1e-6)
    }
}
