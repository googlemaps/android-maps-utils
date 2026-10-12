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
 */
package com.google.maps.android.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class ModelTest {
    @Test
    fun latLngValueSemantics() {
        val a = LatLng(10.0, 20.0)
        val b = LatLng(10.0, 20.0)

        assertEquals(a, b)
        assertEquals(a.hashCode(), b.hashCode())
        assertEquals(a.toString(), b.toString())
        assertNotEquals(a, LatLng(10.0, 21.0))
    }

    @Test
    fun cameraPositionValueSemantics() {
        val a = CameraPosition(LatLng(10.0, 20.0), 12f, 30f, 45f)
        val b = CameraPosition(LatLng(10.0, 20.0), 12f, 30f, 45f)

        assertEquals(a, b)
        assertEquals(a.hashCode(), b.hashCode())
        assertEquals(a.toString(), b.toString())
        assertNotEquals(a, CameraPosition(LatLng(10.0, 21.0), 12f, 30f, 45f))
        assertNotEquals(a, CameraPosition(LatLng(10.0, 20.0), 13f, 30f, 45f))
        assertNotEquals(a, CameraPosition(LatLng(10.0, 20.0), 12f, 31f, 45f))
        assertNotEquals(a, CameraPosition(LatLng(10.0, 20.0), 12f, 30f, 46f))
    }
}
