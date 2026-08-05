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

package com.google.maps.android.model

import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.common.truth.Truth.assertThat
import com.nhaarman.mockitokotlin2.mock
import org.junit.Test

internal class GroundOverlayOptionsTest {

    @Test
    fun testBuilder() {
        val descriptor: BitmapDescriptor = mock()
        val groundOverlayOptions = groundOverlayOptions {
            image(descriptor)
            bearing(1f)
            clickable(true)
            transparency(0.5f)
            visible(true)
        }
        assertThat(groundOverlayOptions.image).isEqualTo(descriptor)
        assertThat(groundOverlayOptions.bearing).isWithin(1e-6f).of(1f)
        assertThat(groundOverlayOptions.isClickable).isTrue()
        assertThat(groundOverlayOptions.transparency).isWithin(1e-6f).of(0.5f)
        assertThat(groundOverlayOptions.isVisible).isTrue()
    }
}
