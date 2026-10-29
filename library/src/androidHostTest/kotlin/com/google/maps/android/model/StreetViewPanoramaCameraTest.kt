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

import com.google.common.truth.Truth.assertThat
import org.junit.Test

internal class StreetViewPanoramaCameraTest {

    @Test
    fun `test that StreetViewPanoramaCamera is constructed`() {
        val camera = streetViewPanoramaCamera {
            bearing(1f)
            tilt(20f)
            zoom(2f)
        }
        assertThat(camera.bearing).isWithin(1e-6f).of(1f)
        assertThat(camera.tilt).isWithin(1e-6f).of(20f)
        assertThat(camera.zoom).isWithin(1e-6f).of(2f)
    }
}
