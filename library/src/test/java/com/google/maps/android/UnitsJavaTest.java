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
import static com.google.maps.android.Units.ft;
import static com.google.maps.android.Units.kilometers;
import static com.google.maps.android.Units.km;
import static com.google.maps.android.Units.m;
import static com.google.maps.android.Units.meters;
import static com.google.maps.android.Units.mi;
import static com.google.maps.android.Units.miles;
import static com.google.maps.android.Units.toDistanceString;
import static com.google.maps.android.Units.toElevationString;
import static com.google.maps.android.Units.toFeet;
import static com.google.maps.android.Units.toKilometers;
import static com.google.maps.android.Units.toMeters;
import static com.google.maps.android.Units.toMiles;

import android.content.Context;
import android.content.res.Resources;
import androidx.test.core.app.ApplicationProvider;
import java.util.Locale;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

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
    assertThat(ft(12)).isEqualTo(twelveFeetInMeters);
    assertThat(toFeet(twelveFeetInMeters)).isWithin(1e-9).of(12.0);

    double twoAndHalfKmInMeters = kilometers(2.5);
    assertThat(twoAndHalfKmInMeters).isEqualTo(2_500.0);
    assertThat(km(2.5)).isEqualTo(twoAndHalfKmInMeters);
    assertThat(toKilometers(twoAndHalfKmInMeters)).isWithin(1e-9).of(2.5);

    double quarterMileInMeters = miles(0.25);
    assertThat(quarterMileInMeters).isWithin(1e-9).of(1_320 * Units.METERS_PER_FOOT);
    assertThat(mi(0.25)).isEqualTo(quarterMileInMeters);
    assertThat(toMiles(quarterMileInMeters)).isWithin(1e-9).of(0.25);
    assertThat(toFeet(quarterMileInMeters)).isWithin(1e-9).of(1_320.0);

    double hundredMeters = meters(100);
    assertThat(hundredMeters).isEqualTo(100.0);
    assertThat(m(100)).isEqualTo(100.0);
    assertThat(toMeters(hundredMeters)).isEqualTo(100.0);
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
  }

  @Test
  public void distanceAndElevationFormatting_worksFromJava() {
    UnitsConverter imperial = ImperialUnitsConverter.INSTANCE;
    UnitsConverter metric = MetricUnitsConverter.INSTANCE;

    // Formatting via converter instance methods
    assertThat(imperial.toDistanceString(feet(528), Locale.US)).isEqualTo("528 ft");
    assertThat(imperial.toDistanceString(miles(0.25), Locale.US)).isEqualTo("0.3 mi");
    assertThat(imperial.toDistanceString(miles(2.0), Locale.US)).isEqualTo("2.0 mi");
    assertThat(imperial.toDistanceString(miles(12.4), Locale.US)).isEqualTo("12 mi");
    assertThat(imperial.toElevationString(meters(4401.2), Locale.US)).isEqualTo("14,440 ft");

    assertThat(metric.toDistanceString(meters(450), Locale.US)).isEqualTo("450 m");
    assertThat(metric.toDistanceString(km(2.0), Locale.US)).isEqualTo("2.0 km");
    assertThat(metric.toDistanceString(km(12.4), Locale.US)).isEqualTo("12 km");
    assertThat(metric.toElevationString(meters(4401.2), Locale.GERMANY)).isEqualTo("4.401 m");

    // Formatting via top-level Units static methods
    assertThat(toDistanceString(km(2.0), Locale.US)).isEqualTo("1.2 mi");
    assertThat(toDistanceString(km(2.0), Locale.FRANCE)).isEqualTo("2,0 km");
    assertThat(toDistanceString(km(2.0), metric, NumberFormatSymbols.DEFAULT)).isEqualTo("2.0 km");
    assertThat(toElevationString(meters(4401.2), Locale.US)).isEqualTo("14,440 ft");
    assertThat(toElevationString(meters(4401.2), metric, NumberFormatSymbols.DEFAULT))
        .isEqualTo("4,401 m");

    // Formatting with Android Resources from Java
    Context context = ApplicationProvider.getApplicationContext();
    Resources resources = context.getResources();
    assertThat(toDistanceString(km(2.0), resources, metric)).isEqualTo("2.0 km");
    assertThat(toElevationString(meters(4401.2), resources, imperial)).isEqualTo("14,440 ft");

    // Standalone logical number formatting
    assertThat(Units.formatDistanceValue(2.0, Locale.US)).isEqualTo("2.0");
    assertThat(Units.formatDistanceValue(1250.4, NumberFormatSymbols.fromLocale(Locale.GERMANY)))
        .isEqualTo("1.250");
  }
}
