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

internal class CircleOptionsTest {

    @Test
    fun testBuilder() {
        val circleOptions = circleOptions {
            center(LatLng(0.0, 0.0))
            clickable(true)
            fillColor(0)
            radius(1.23)
            strokeColor(1)
            strokeWidth(2f)
            visible(true)
            zIndex(1f)
        }
        assertThat(circleOptions.center).isEqualTo(LatLng(0.0, 0.0))
        assertThat(circleOptions.isClickable).isTrue()
        assertThat(circleOptions.fillColor).isEqualTo(0)
        assertThat(circleOptions.radius).isWithin(1e-6).of(1.23)
        assertThat(circleOptions.strokeColor).isEqualTo(1)
        assertThat(circleOptions.strokeWidth).isEqualTo(2f)
        assertThat(circleOptions.isVisible).isTrue()
        assertThat(circleOptions.zIndex).isWithin(1e-6f).of(1f)
    }
}
