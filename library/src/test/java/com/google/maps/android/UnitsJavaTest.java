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
package com.google.maps.android;

import static com.google.common.truth.Truth.assertThat;
import static com.google.maps.android.Units.feet;
import static com.google.maps.android.Units.inFeet;
import static com.google.maps.android.Units.inKilometers;
import static com.google.maps.android.Units.inMeters;
import static com.google.maps.android.Units.inMiles;
import static com.google.maps.android.Units.kilometers;
import static com.google.maps.android.Units.meters;
import static com.google.maps.android.Units.miles;
import static com.google.maps.android.Units.toDistanceString;
import static com.google.maps.android.Units.toElevationString;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import java.util.Locale;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;

/**
 * Verifies the Java interoperability surface for {@link Units}, {@link UnitSystem}, {@link
 * NumberFormatSymbols}, {@link UnitsConverter}, {@link MetricUnitsConverter}, and {@link
 * ImperialUnitsConverter}.
 */
@RunWith(RobolectricTestRunner.class)
public class UnitsJavaTest {

  @Test
  public void unitConversions_workIdiomaticallyFromJava() {
    // [START maps_android_utils_units_java_conversions]
    // Expressing numbers in meters from Java via static factory methods
    double twelveFeetInMeters = feet(12);
    assertThat(twelveFeetInMeters).isWithin(1e-9).of(12 * Units.METERS_PER_FOOT);
    assertThat(inFeet(twelveFeetInMeters)).isWithin(1e-9).of(12.0);

    double twoAndHalfKmInMeters = kilometers(2.5);
    assertThat(twoAndHalfKmInMeters).isEqualTo(2_500.0);
    assertThat(inKilometers(twoAndHalfKmInMeters)).isWithin(1e-9).of(2.5);

    double quarterMileInMeters = miles(0.25);
    assertThat(quarterMileInMeters).isWithin(1e-9).of(1_320 * Units.METERS_PER_FOOT);
    assertThat(inMiles(quarterMileInMeters)).isWithin(1e-9).of(0.25);
    assertThat(inFeet(quarterMileInMeters)).isWithin(1e-9).of(1_320.0);

    double hundredMeters = meters(100);
    assertThat(hundredMeters).isEqualTo(100.0);
    assertThat(inMeters(hundredMeters)).isEqualTo(100.0);
    // [END maps_android_utils_units_java_conversions]
  }

  @Test
  public void unitSystemAndConverters_resolveFromJava() {
    assertThat(UnitSystem.fromLocale(Locale.US)).isEqualTo(UnitSystem.IMPERIAL);
    assertThat(UnitSystem.fromLocale(Locale.FRANCE)).isEqualTo(UnitSystem.METRIC);
    assertThat(UnitSystem.fromCountryCode("GB")).isEqualTo(UnitSystem.IMPERIAL);
    assertThat(UnitSystem.fromCountryCode("DE")).isEqualTo(UnitSystem.METRIC);

    UnitsConverter imperial = Units.getUnitsConverter(UnitSystem.IMPERIAL);
    UnitsConverter metric = Units.getUnitsConverter(UnitSystem.METRIC);
    assertThat(imperial).isSameInstanceAs(ImperialUnitsConverter.INSTANCE);
    assertThat(metric).isSameInstanceAs(MetricUnitsConverter.INSTANCE);

    // Threshold getters are exposed without inline value class mangling
    assertThat(ImperialUnitsConverter.INSTANCE.getMilesThreshold()).isWithin(1e-9).of(miles(0.25));
    assertThat(MetricUnitsConverter.INSTANCE.getKilometersThreshold()).isEqualTo(kilometers(1.0));
  }

  @Test
  public void distanceAndElevationFormatting_worksFromJava() {
    UnitsConverter imperial = ImperialUnitsConverter.INSTANCE;
    UnitsConverter metric = MetricUnitsConverter.INSTANCE;

    // Formatting via converter instance methods
    assertThat(imperial.toDistanceString(feet(528), Locale.US)).isEqualTo("528 ft");
    assertThat(imperial.toDistanceString(feet(1319.6), Locale.US)).isEqualTo("0.3 mi");
    assertThat(imperial.toDistanceString(miles(0.25), Locale.US)).isEqualTo("0.3 mi");
    assertThat(imperial.toDistanceString(miles(2.0), Locale.US)).isEqualTo("2.0 mi");
    assertThat(imperial.toDistanceString(miles(12.4), Locale.US)).isEqualTo("12 mi");
    assertThat(imperial.toElevationString(meters(4401.2), Locale.US)).isEqualTo("14,440 ft");

    assertThat(metric.toDistanceString(meters(450), Locale.US)).isEqualTo("450 m");
    assertThat(metric.toDistanceString(meters(999.6), Locale.US)).isEqualTo("1.0 km");
    assertThat(metric.toDistanceString(kilometers(2.0), Locale.US)).isEqualTo("2.0 km");
    assertThat(metric.toDistanceString(kilometers(12.4), Locale.US)).isEqualTo("12 km");
    assertThat(metric.toElevationString(meters(4401.2), Locale.GERMANY)).isEqualTo("4.401 m");

    // Formatting via top-level Units static methods
    assertThat(toDistanceString(kilometers(2.0), Locale.US)).isEqualTo("1.2 mi");
    assertThat(toDistanceString(kilometers(2.0), Locale.FRANCE)).isEqualTo("2,0 km");
    assertThat(toDistanceString(kilometers(2.0), metric, NumberFormatSymbols.DEFAULT))
        .isEqualTo("2.0 km");
    assertThat(toElevationString(meters(4401.2), Locale.US)).isEqualTo("14,440 ft");
    assertThat(toElevationString(meters(4401.2), metric, NumberFormatSymbols.DEFAULT))
        .isEqualTo("4,401 m");

    // Standalone logical number formatting
    assertThat(Units.formatDistanceValue(2.0, Locale.US)).isEqualTo("2.0");
    assertThat(Units.formatDistanceValue(1250.4, NumberFormatSymbols.fromLocale(Locale.GERMANY)))
        .isEqualTo("1.250");
    assertThat(NumberFormatSymbols.DEFAULT.formatFixedDecimal(1234.5, 2)).isEqualTo("1,234.50");
    assertThat(NumberFormatSymbols.DEFAULT.formatFixedDecimal(1234.5)).isEqualTo("1,235");
    assertThat(NumberFormatSymbols.DEFAULT.formatDistanceValue(2.0)).isEqualTo("2.0");
  }

  @Test
  public void customUnitsConverter_worksFromJavaWithConvertersAndFormatters() {
    // 1. UK (GB) defaults out-of-the-box to Imperial distances (ft/mi) + Metric elevations (m)
    UnitsConverter ukDefault = Units.getUnitsConverter(Locale.UK);
    assertThat(ukDefault.toDistanceString(kilometers(2.0), Locale.UK)).isEqualTo("1.2 mi");
    assertThat(ukDefault.toElevationString(meters(1345.0), Locale.UK)).isEqualTo("1,345 m");

    // 2. Simple overrides for either distance or elevation from Java
    UnitsConverter ukWithFeetElevation = ukDefault.withElevationUnits(UnitSystem.IMPERIAL);
    assertThat(ukWithFeetElevation).isSameInstanceAs(ImperialUnitsConverter.INSTANCE);
    assertThat(ukWithFeetElevation.toElevationString(meters(1345.0), Locale.UK))
        .isEqualTo("4,413 ft");

    UnitsConverter ukWithMetricDistance = ukDefault.withDistanceUnits(UnitSystem.METRIC);
    assertThat(ukWithMetricDistance).isSameInstanceAs(MetricUnitsConverter.INSTANCE);
    assertThat(ukWithMetricDistance.toDistanceString(kilometers(2.0), Locale.UK))
        .isEqualTo("2.0 km");

    UnitsConverter fromUnitSystems = UnitsConverter.create(UnitSystem.IMPERIAL, UnitSystem.METRIC);
    assertThat(fromUnitSystems).isSameInstanceAs(ukDefault);

    // 3. Mix & match existing converters explicitly
    UnitsConverter ukConverter =
        UnitsConverter.create(ImperialUnitsConverter.INSTANCE, MetricUnitsConverter.INSTANCE);
    assertThat(ukConverter.toDistanceString(kilometers(2.0), Locale.UK)).isEqualTo("1.2 mi");
    assertThat(ukConverter.toElevationString(meters(1345.0), Locale.UK)).isEqualTo("1,345 m");

    // 2. Custom MeasurementFormatter lambdas and method references from Java
    UnitsConverter customConverter =
        UnitsConverter.create(
            (meters, symbols) -> symbols.formatFixedDecimal(inKilometers(meters), 2) + " km",
            MetricUnitsConverter.INSTANCE::toElevationString);
    assertThat(customConverter.toDistanceString(meters(1500), Locale.US)).isEqualTo("1.50 km");
    assertThat(customConverter.toElevationString(meters(4401.2), Locale.US)).isEqualTo("4,401 m");
  }

  @Test
  public void resourcesOverloads_andLocalizedTranslations_workFromJava() {
    Context app = RuntimeEnvironment.getApplication();
    Configuration jaConfig = new Configuration(app.getResources().getConfiguration());
    jaConfig.setLocale(Locale.JAPAN);
    Resources jaResources = app.createConfigurationContext(jaConfig).getResources();

    assertThat(UnitSystem.fromResources(jaResources)).isEqualTo(UnitSystem.METRIC);
    assertThat(toDistanceString(meters(450), jaResources)).isEqualTo("450 m");
    assertThat(toDistanceString(miles(2.5), jaResources, ImperialUnitsConverter.INSTANCE))
        .isEqualTo("2.5 マイル");
    assertThat(toElevationString(meters(4401.2), jaResources, ImperialUnitsConverter.INSTANCE))
        .isEqualTo("14,440 フィート");
  }

  @Test
  public void compactSymbols_andCustomThresholds_workFromJava() {
    UnitsConverter scaleBarImperial = ImperialUnitsConverter.withMilesThreshold(miles(1));
    assertThat(
            toDistanceString(
                meters(1900), MetricUnitsConverter.INSTANCE, NumberFormatSymbols.COMPACT))
        .isEqualTo("1.9 km");
    assertThat(
            toDistanceString(
                meters(1000), MetricUnitsConverter.INSTANCE, NumberFormatSymbols.COMPACT))
        .isEqualTo("1 km");
    assertThat(
            toDistanceString(
                meters(3456780), MetricUnitsConverter.INSTANCE, NumberFormatSymbols.COMPACT))
        .isEqualTo("3457 km");
    assertThat(toDistanceString(meters(500), scaleBarImperial, NumberFormatSymbols.COMPACT))
        .isEqualTo("1640 ft");
    assertThat(toDistanceString(meters(2000), scaleBarImperial, NumberFormatSymbols.COMPACT))
        .isEqualTo("1.2 mi");
    assertThat(toDistanceString(meters(3200), scaleBarImperial, NumberFormatSymbols.COMPACT))
        .isEqualTo("2 mi");

    UnitsConverter halfKmMetric = MetricUnitsConverter.withKilometersThreshold(meters(500));
    assertThat(toDistanceString(meters(500), halfKmMetric, NumberFormatSymbols.DEFAULT))
        .isEqualTo("0.5 km");
  }
}
