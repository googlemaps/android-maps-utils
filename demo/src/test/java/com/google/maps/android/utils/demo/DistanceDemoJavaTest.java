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
package com.google.maps.android.utils.demo;

import static com.google.common.truth.Truth.assertThat;

import com.google.maps.android.ImperialUnitsConverter;
import com.google.maps.android.MetricUnitsConverter;
import com.google.maps.android.UnitSystem;
import com.google.maps.android.Units;
import com.google.maps.android.UnitsConverter;
import java.util.Locale;
import org.junit.Test;

public class DistanceDemoJavaTest {

  @Test
  public void formatDistanceBetween_formatsDefaultAndExplicitUnitSystemsFromJava() {
    UnitsConverter usDefault = Units.getUnitsConverter(Locale.US);
    assertThat(usDefault).isSameInstanceAs(ImperialUnitsConverter.INSTANCE);
    assertThat(
            DistanceDemoActivity.formatDistanceBetween(
                DistanceDemoActivity.SYDNEY_SOUTH,
                DistanceDemoActivity.SYDNEY_NORTH,
                Locale.US,
                usDefault))
        .isEqualTo("7.5 mi");

    Locale auLocale = new Locale.Builder().setLanguage("en").setRegion("AU").build();
    UnitsConverter auDefault = Units.getUnitsConverter(auLocale);
    assertThat(auDefault).isSameInstanceAs(MetricUnitsConverter.INSTANCE);
    assertThat(
            DistanceDemoActivity.formatDistanceBetween(
                DistanceDemoActivity.SYDNEY_SOUTH,
                DistanceDemoActivity.SYDNEY_NORTH,
                auLocale,
                auDefault))
        .isEqualTo("12 km");

    // Explicit UnitSystem override in Java
    UnitsConverter metricOverride = Units.getUnitsConverter(Locale.US, UnitSystem.METRIC);
    assertThat(
            DistanceDemoActivity.formatDistanceBetween(
                DistanceDemoActivity.SYDNEY_SOUTH,
                DistanceDemoActivity.SYDNEY_NORTH,
                Locale.US,
                metricOverride))
        .isEqualTo("12 km");
  }
}
