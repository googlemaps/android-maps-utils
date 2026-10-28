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
package com.google.maps.android.utils.demo

import com.google.common.truth.Truth.assertThat
import com.google.maps.android.ImperialUnitsConverter
import com.google.maps.android.MetricUnitsConverter
import com.google.maps.android.UnitSystem
import com.google.maps.android.meters
import com.google.maps.android.utils.demo.UnitsDemoActivity.UnitPreference
import org.junit.Test
import java.util.Locale

class UnitsDemoTest {
    @Test
    fun unitPreference_resolvesExpectedConverter() {
        assertThat(UnitPreference.DEVICE_DEFAULT.resolveConverter(Locale.US))
            .isSameInstanceAs(ImperialUnitsConverter)

        // UK (GB) defaults to Imperial road distances (ft/mi) + Metric elevations (m)
        assertThat(UnitPreference.DEVICE_DEFAULT.resolveDistanceUnits(Locale.UK))
            .isEqualTo(UnitSystem.IMPERIAL)
        assertThat(UnitPreference.DEVICE_DEFAULT.resolveElevationUnits(Locale.UK))
            .isEqualTo(UnitSystem.METRIC)
        val ukConverter = UnitPreference.DEVICE_DEFAULT.resolveConverter(Locale.UK)
        assertThat(ukConverter.toDistanceString(2_000.meters, Locale.UK)).isEqualTo("1.2 mi")
        assertThat(ukConverter.toElevationString(1_345.meters, Locale.UK)).isEqualTo("1,345 m")

        assertThat(UnitPreference.DEVICE_DEFAULT.resolveConverter(Locale.FRANCE))
            .isSameInstanceAs(MetricUnitsConverter)

        assertThat(UnitPreference.METRIC.resolveConverter(Locale.US))
            .isSameInstanceAs(MetricUnitsConverter)
        assertThat(UnitPreference.IMPERIAL.resolveConverter(Locale.FRANCE))
            .isSameInstanceAs(ImperialUnitsConverter)
    }
}
