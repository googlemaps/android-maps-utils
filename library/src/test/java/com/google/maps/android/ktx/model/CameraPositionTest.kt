@file:Suppress("DEPRECATION")
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

package com.google.maps.android.ktx.model

import com.google.android.gms.maps.model.LatLng
import com.google.common.truth.Truth.assertThat
import org.junit.Test

internal class CameraPositionTest {

    @Test
    fun testBuilder() {
        val cameraPosition = cameraPosition {
            bearing(1f)
            target(LatLng(1.0, 2.0))
            tilt(1f)
            zoom(12f)
        }
        assertThat(cameraPosition.bearing).isWithin(1e-6f).of(1f)
        assertThat(cameraPosition.target).isEqualTo(LatLng(1.0, 2.0))
        assertThat(cameraPosition.tilt).isWithin(1e-6f).of(1f)
        assertThat(cameraPosition.zoom).isWithin(1e-6f).of(12f)
    }
}
