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

import com.google.android.gms.maps.model.LatLng
import com.google.common.truth.Truth.assertThat
import org.junit.Test

internal class MarkerOptionsTest {

    @Test
    fun testBuilder() {
        val markerOptions = markerOptions {
            position(LatLng(1.0, 2.0))
            alpha(0.5f)
            draggable(false)
            flat(true)
            title("Test")
            snippet("Snippet")
            visible(true)
        }
        assertThat(markerOptions.position).isEqualTo(LatLng(1.0, 2.0))
        assertThat(markerOptions.alpha).isWithin(1e-6f).of(0.5f)
        assertThat(markerOptions.isDraggable).isFalse()
        assertThat(markerOptions.isFlat).isTrue()
        assertThat(markerOptions.title).isEqualTo("Test")
        assertThat(markerOptions.snippet).isEqualTo("Snippet")
        assertThat(markerOptions.isVisible).isTrue()
    }
}
