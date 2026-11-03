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
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;

/**
 * Verifies 100% of the Java interoperability surface for {@link Units}, {@link UnitSystem}, {@link
 * NumberFormatSymbols}, {@link MeasurementFormatter}, {@link UnitsConverter}, {@link
 * MetricUnitsConverter}, and {@link ImperialUnitsConverter}.
 */
@RunWith(RobolectricTestRunner.class)
public class UnitsJavaTest {

  private Locale originalDefaultLocale;

  @Before
  public void setUp() {
    originalDefaultLocale = Locale.getDefault();
    Locale.setDefault(Locale.US);
  }

  @After
  public void tearDown() {
    Locale.setDefault(originalDefaultLocale);
  }

  @Test
  public void physicalConstants_andUnitConversions_workIdiomaticallyFromJava() {
    // [START maps_android_utils_units_java_conversions]
    // Verify physical conversion constants are directly accessible from Java
    assertThat(Units.METERS_PER_FOOT).isEqualTo(0.3048);
    assertThat(Units.METERS_PER_KILOMETER).isEqualTo(1_000);
    assertThat(Units.FEET_PER_METER).isWithin(1e-12).of(1.0 / 0.3048);
    assertThat(Units.FEET_PER_MILE).isEqualTo(5_280);
    assertThat(Units.METERS_PER_MILE).isWithin(1e-9).of(1_609.344);
    assertThat(Units.MILES_PER_METER).isWithin(1e-12).of(1.0 / 1_609.344);

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
  public void unitSystemAndConverters_allOverloadsResolveFromJava() {
    // Country code sets exposed as static fields via @JvmField
    assertThat(UnitSystem.IMPERIAL_COUNTRY_CODES).containsExactly("US", "LR", "MM", "GB");
    assertThat(UnitSystem.IMPERIAL_ELEVATION_COUNTRY_CODES).containsExactly("US", "LR", "MM");

    // 0-arg and 1-arg Locale / CountryCode resolution for general, distance, and elevation
    assertThat(UnitSystem.fromLocale()).isEqualTo(UnitSystem.IMPERIAL);
    assertThat(UnitSystem.distanceFromLocale()).isEqualTo(UnitSystem.IMPERIAL);
    assertThat(UnitSystem.elevationFromLocale()).isEqualTo(UnitSystem.IMPERIAL);

    assertThat(UnitSystem.fromLocale(Locale.US)).isEqualTo(UnitSystem.IMPERIAL);
    assertThat(UnitSystem.fromLocale(Locale.FRANCE)).isEqualTo(UnitSystem.METRIC);
    assertThat(UnitSystem.distanceFromLocale(Locale.UK)).isEqualTo(UnitSystem.IMPERIAL);
    assertThat(UnitSystem.elevationFromLocale(Locale.UK)).isEqualTo(UnitSystem.METRIC);

    assertThat(UnitSystem.fromCountryCode("GB")).isEqualTo(UnitSystem.IMPERIAL);
    assertThat(UnitSystem.fromCountryCode("DE")).isEqualTo(UnitSystem.METRIC);
    assertThat(UnitSystem.distanceFromCountryCode("GB")).isEqualTo(UnitSystem.IMPERIAL);
    assertThat(UnitSystem.elevationFromCountryCode("GB")).isEqualTo(UnitSystem.METRIC);
    assertThat(UnitSystem.distanceFromCountryCode("US")).isEqualTo(UnitSystem.IMPERIAL);
    assertThat(UnitSystem.elevationFromCountryCode("US")).isEqualTo(UnitSystem.IMPERIAL);

    // Enum getConverter() and Singleton getUnitSystem()
    assertThat(UnitSystem.IMPERIAL.getConverter())
        .isSameInstanceAs(ImperialUnitsConverter.INSTANCE);
    assertThat(UnitSystem.METRIC.getConverter()).isSameInstanceAs(MetricUnitsConverter.INSTANCE);
    assertThat(ImperialUnitsConverter.INSTANCE.getUnitSystem()).isEqualTo(UnitSystem.IMPERIAL);
    assertThat(MetricUnitsConverter.INSTANCE.getUnitSystem()).isEqualTo(UnitSystem.METRIC);

    // Threshold getters are exposed without inline value class mangling
    assertThat(ImperialUnitsConverter.INSTANCE.getMilesThreshold()).isWithin(1e-9).of(miles(0.25));
    assertThat(MetricUnitsConverter.INSTANCE.getKilometersThreshold()).isEqualTo(kilometers(1.0));

    // All Units.getUnitsConverter(...) overloads (0-arg, UnitSystem, Locale 1/2/3-arg, String
    // 1/2/3-arg)
    assertThat(Units.getUnitsConverter()).isSameInstanceAs(ImperialUnitsConverter.INSTANCE);
    assertThat(Units.getUnitsConverter(UnitSystem.IMPERIAL))
        .isSameInstanceAs(ImperialUnitsConverter.INSTANCE);
    assertThat(Units.getUnitsConverter(UnitSystem.METRIC))
        .isSameInstanceAs(MetricUnitsConverter.INSTANCE);

    assertThat(Units.getUnitsConverter(Locale.US))
        .isSameInstanceAs(ImperialUnitsConverter.INSTANCE);
    assertThat(Units.getUnitsConverter(Locale.US, UnitSystem.METRIC))
        .isEqualTo(UnitsConverter.create(UnitSystem.METRIC, UnitSystem.IMPERIAL));
    assertThat(Units.getUnitsConverter(Locale.US, UnitSystem.METRIC, UnitSystem.METRIC))
        .isSameInstanceAs(MetricUnitsConverter.INSTANCE);

    assertThat(Units.getUnitsConverter("FR")).isSameInstanceAs(MetricUnitsConverter.INSTANCE);
    assertThat(Units.getUnitsConverter("FR", UnitSystem.IMPERIAL))
        .isEqualTo(UnitsConverter.create(UnitSystem.IMPERIAL, UnitSystem.METRIC));
    assertThat(Units.getUnitsConverter("FR", UnitSystem.IMPERIAL, UnitSystem.IMPERIAL))
        .isSameInstanceAs(ImperialUnitsConverter.INSTANCE);
  }

  @Test
  public void numberFormatSymbols_allConstructorsGettersAndFormattersWorkFromJava() {
    // 0-arg, 1-arg, 2-arg, 3-arg, and 4-arg constructors
    NumberFormatSymbols s0 = new NumberFormatSymbols();
    assertThat(s0.getGroupingSeparator()).isEqualTo(',');
    assertThat(s0.getDecimalSeparator()).isEqualTo('.');
    assertThat(s0.getGroupingSize()).isEqualTo(3);
    assertThat(s0.getTrimTrailingZeros()).isFalse();

    NumberFormatSymbols s1 = new NumberFormatSymbols(' ');
    assertThat(s1.getGroupingSeparator()).isEqualTo(' ');
    assertThat(s1.getDecimalSeparator()).isEqualTo('.');

    NumberFormatSymbols s2 = new NumberFormatSymbols('.', ',');
    assertThat(s2.getGroupingSeparator()).isEqualTo('.');
    assertThat(s2.getDecimalSeparator()).isEqualTo(',');

    NumberFormatSymbols s3 = new NumberFormatSymbols('.', ',', 4);
    assertThat(s3.getGroupingSize()).isEqualTo(4);
    assertThat(s3.getTrimTrailingZeros()).isFalse();

    NumberFormatSymbols s4 = new NumberFormatSymbols('.', ',', 0, true);
    assertThat(s4.getGroupingSize()).isEqualTo(0);
    assertThat(s4.getTrimTrailingZeros()).isTrue();
    assertThat(s4.formatFixedDecimal(1234.0, 1)).isEqualTo("1234");
    assertThat(s4.formatFixedDecimal(1234.5, 2)).isEqualTo("1234,5");

    // Static presets and fromLocale() overloads
    assertThat(NumberFormatSymbols.DEFAULT).isEqualTo(s0);
    assertThat(NumberFormatSymbols.COMPACT.getGroupingSize()).isEqualTo(0);
    assertThat(NumberFormatSymbols.COMPACT.getTrimTrailingZeros()).isTrue();
    assertThat(NumberFormatSymbols.fromLocale())
        .isEqualTo(NumberFormatSymbols.fromLocale(Locale.US));
    assertThat(NumberFormatSymbols.fromLocale(Locale.GERMANY).getDecimalSeparator()).isEqualTo(',');
  }

  @Test
  public void distanceAndElevationFormatting_allInstanceAndStaticOverloadsWorkFromJava() {
    UnitsConverter imperial = ImperialUnitsConverter.INSTANCE;
    UnitsConverter metric = MetricUnitsConverter.INSTANCE;

    // 1. UnitsConverter instance overloads: (double), (double, Locale), (double,
    // NumberFormatSymbols)
    assertThat(imperial.toDistanceString(feet(528))).isEqualTo("528 ft");
    assertThat(imperial.toDistanceString(feet(528), Locale.US)).isEqualTo("528 ft");
    assertThat(imperial.toDistanceString(feet(1319.6), Locale.US)).isEqualTo("0.3 mi");
    assertThat(imperial.toDistanceString(miles(0.25), Locale.US)).isEqualTo("0.3 mi");
    assertThat(imperial.toDistanceString(miles(2.0), Locale.US)).isEqualTo("2.0 mi");
    assertThat(imperial.toDistanceString(miles(2.0), NumberFormatSymbols.COMPACT))
        .isEqualTo("2 mi");
    assertThat(imperial.toDistanceString(miles(12.4), Locale.US)).isEqualTo("12 mi");

    assertThat(imperial.toElevationString(meters(4401.2))).isEqualTo("14,440 ft");
    assertThat(imperial.toElevationString(meters(4401.2), Locale.US)).isEqualTo("14,440 ft");
    assertThat(imperial.toElevationString(meters(4401.2), NumberFormatSymbols.COMPACT))
        .isEqualTo("14440 ft");

    assertThat(metric.toDistanceString(meters(450))).isEqualTo("450 m");
    assertThat(metric.toDistanceString(meters(450), Locale.US)).isEqualTo("450 m");
    assertThat(metric.toDistanceString(meters(999.6), Locale.US)).isEqualTo("1.0 km");
    assertThat(metric.toDistanceString(kilometers(2.0), Locale.US)).isEqualTo("2.0 km");
    assertThat(metric.toDistanceString(kilometers(2.0), NumberFormatSymbols.COMPACT))
        .isEqualTo("2 km");
    assertThat(metric.toDistanceString(kilometers(12.4), Locale.US)).isEqualTo("12 km");

    assertThat(metric.toElevationString(meters(4401.2))).isEqualTo("4,401 m");
    assertThat(metric.toElevationString(meters(4401.2), Locale.GERMANY)).isEqualTo("4.401 m");
    assertThat(metric.toElevationString(meters(4401.2), NumberFormatSymbols.COMPACT))
        .isEqualTo("4401 m");

    // 2. Top-level Units static overloads: (double), (double, Locale), (double, Locale,
    // UnitsConverter),
    //    (double, UnitsConverter), (double, UnitsConverter, NumberFormatSymbols)
    assertThat(toDistanceString(miles(2.0))).isEqualTo("2.0 mi");
    assertThat(toDistanceString(kilometers(2.0), Locale.US)).isEqualTo("1.2 mi");
    assertThat(toDistanceString(kilometers(2.0), Locale.FRANCE)).isEqualTo("2,0 km");
    assertThat(toDistanceString(kilometers(2.0), Locale.US, metric)).isEqualTo("2.0 km");
    assertThat(toDistanceString(kilometers(2.0), metric)).isEqualTo("2.0 km");
    assertThat(toDistanceString(kilometers(2.0), metric, NumberFormatSymbols.DEFAULT))
        .isEqualTo("2.0 km");

    assertThat(toElevationString(meters(4401.2))).isEqualTo("14,440 ft");
    assertThat(toElevationString(meters(4401.2), Locale.US)).isEqualTo("14,440 ft");
    assertThat(toElevationString(meters(4401.2), Locale.US, metric)).isEqualTo("4,401 m");
    assertThat(toElevationString(meters(4401.2), metric)).isEqualTo("4,401 m");
    assertThat(toElevationString(meters(4401.2), metric, NumberFormatSymbols.DEFAULT))
        .isEqualTo("4,401 m");

    // 3. Standalone logical number formatting: (double), (double, Locale), (double,
    // NumberFormatSymbols)
    assertThat(Units.formatDistanceValue(2.0)).isEqualTo("2.0");
    assertThat(Units.formatDistanceValue(2.0, Locale.US)).isEqualTo("2.0");
    assertThat(Units.formatDistanceValue(1250.4, NumberFormatSymbols.fromLocale(Locale.GERMANY)))
        .isEqualTo("1.250");
    assertThat(NumberFormatSymbols.DEFAULT.formatFixedDecimal(1234.5, 2)).isEqualTo("1,234.50");
    assertThat(NumberFormatSymbols.DEFAULT.formatFixedDecimal(1234.5)).isEqualTo("1,235");
    assertThat(NumberFormatSymbols.DEFAULT.formatDistanceValue(2.0)).isEqualTo("2.0");
  }

  @Test
  public void customUnitsConverter_allCreateAndFluentOverrideMethodsWorkFromJava() {
    // 1. UK (GB) defaults out-of-the-box to Imperial distances (ft/mi) + Metric elevations (m)
    UnitsConverter ukDefault = Units.getUnitsConverter(Locale.UK);
    assertThat(ukDefault.toDistanceString(kilometers(2.0), Locale.UK)).isEqualTo("1.2 mi");
    assertThat(ukDefault.toElevationString(meters(1345.0), Locale.UK)).isEqualTo("1,345 m");

    // 2. Fluent overrides: withDistanceUnits, withElevationUnits, withDistanceConverter,
    //    withElevationConverter, withDistanceFormatter, withElevationFormatter
    UnitsConverter ukWithFeetElevation = ukDefault.withElevationUnits(UnitSystem.IMPERIAL);
    assertThat(ukWithFeetElevation).isSameInstanceAs(ImperialUnitsConverter.INSTANCE);
    assertThat(ukWithFeetElevation.toElevationString(meters(1345.0), Locale.UK))
        .isEqualTo("4,413 ft");

    UnitsConverter ukWithMetricDistance = ukDefault.withDistanceUnits(UnitSystem.METRIC);
    assertThat(ukWithMetricDistance).isSameInstanceAs(MetricUnitsConverter.INSTANCE);
    assertThat(ukWithMetricDistance.toDistanceString(kilometers(2.0), Locale.UK))
        .isEqualTo("2.0 km");

    UnitsConverter withConverterOverrides =
        MetricUnitsConverter.INSTANCE
            .withDistanceConverter(ImperialUnitsConverter.INSTANCE)
            .withElevationConverter(MetricUnitsConverter.INSTANCE);
    assertThat(withConverterOverrides).isSameInstanceAs(ukDefault);

    UnitsConverter withFormatterOverrides =
        MetricUnitsConverter.INSTANCE
            .withDistanceFormatter(
                (meters, symbols) -> symbols.formatFixedDecimal(inKilometers(meters), 2) + " km")
            .withElevationFormatter(
                (meters, symbols) -> symbols.formatFixedDecimal(meters, 0) + " MASL");
    assertThat(withFormatterOverrides.toDistanceString(meters(1500), Locale.US))
        .isEqualTo("1.50 km");
    assertThat(withFormatterOverrides.toElevationString(meters(1345), Locale.US))
        .isEqualTo("1,345 MASL");

    // 3. All 5 UnitsConverter.create(...) overloads from Java:
    //    - create(UnitSystem, UnitSystem)
    //    - create(UnitsConverter, UnitsConverter)
    //    - create(MeasurementFormatter, MeasurementFormatter)
    //    - create(UnitsConverter, MeasurementFormatter)
    //    - create(MeasurementFormatter, UnitsConverter)
    UnitsConverter fromUnitSystems = UnitsConverter.create(UnitSystem.IMPERIAL, UnitSystem.METRIC);
    assertThat(fromUnitSystems).isSameInstanceAs(ukDefault);

    UnitsConverter fromConverters =
        UnitsConverter.create(ImperialUnitsConverter.INSTANCE, MetricUnitsConverter.INSTANCE);
    assertThat(fromConverters).isSameInstanceAs(ukDefault);

    UnitsConverter fromTwoFormatters =
        UnitsConverter.create(
            (meters, symbols) -> symbols.formatFixedDecimal(inKilometers(meters), 2) + " km",
            (meters, symbols) -> symbols.formatFixedDecimal(meters, 0) + " m ASL");
    assertThat(fromTwoFormatters.toDistanceString(meters(1500), Locale.US)).isEqualTo("1.50 km");
    assertThat(fromTwoFormatters.toElevationString(meters(1345), Locale.US))
        .isEqualTo("1,345 m ASL");

    UnitsConverter fromConverterAndFormatter =
        UnitsConverter.create(
            ImperialUnitsConverter.INSTANCE,
            (meters, symbols) -> symbols.formatFixedDecimal(meters, 0) + " m ASL");
    assertThat(fromConverterAndFormatter.toDistanceString(miles(2.0), Locale.US))
        .isEqualTo("2.0 mi");
    assertThat(fromConverterAndFormatter.toElevationString(meters(1345), Locale.US))
        .isEqualTo("1,345 m ASL");

    UnitsConverter fromFormatterAndConverter =
        UnitsConverter.create(
            (meters, symbols) -> symbols.formatFixedDecimal(inKilometers(meters), 2) + " km",
            MetricUnitsConverter.INSTANCE);
    assertThat(fromFormatterAndConverter.toDistanceString(meters(1500), Locale.US))
        .isEqualTo("1.50 km");
    assertThat(fromFormatterAndConverter.toElevationString(meters(4401.2), Locale.US))
        .isEqualTo("4,401 m");
  }

  @Test
  public void resourcesOverloads_andLocalizedTranslations_allOverloadsWorkFromJava() {
    Context app = RuntimeEnvironment.getApplication();
    Configuration jaConfig = new Configuration(app.getResources().getConfiguration());
    jaConfig.setLocale(Locale.JAPAN);
    Resources jaResources = app.createConfigurationContext(jaConfig).getResources();

    Configuration ukConfig = new Configuration(app.getResources().getConfiguration());
    ukConfig.setLocale(Locale.UK);
    Resources ukResources = app.createConfigurationContext(ukConfig).getResources();

    // UnitSystem.*FromResources
    assertThat(UnitSystem.fromResources(jaResources)).isEqualTo(UnitSystem.METRIC);
    assertThat(UnitSystem.distanceFromResources(ukResources)).isEqualTo(UnitSystem.IMPERIAL);
    assertThat(UnitSystem.elevationFromResources(ukResources)).isEqualTo(UnitSystem.METRIC);

    // Units.getUnitsConverter(Resources, ...) overloads (1-arg, 2-arg, 3-arg)
    assertThat(Units.getUnitsConverter(jaResources))
        .isSameInstanceAs(MetricUnitsConverter.INSTANCE);
    assertThat(Units.getUnitsConverter(ukResources))
        .isEqualTo(UnitsConverter.create(UnitSystem.IMPERIAL, UnitSystem.METRIC));
    assertThat(Units.getUnitsConverter(ukResources, UnitSystem.METRIC))
        .isSameInstanceAs(MetricUnitsConverter.INSTANCE);
    assertThat(Units.getUnitsConverter(ukResources, UnitSystem.IMPERIAL, UnitSystem.IMPERIAL))
        .isSameInstanceAs(ImperialUnitsConverter.INSTANCE);

    // UnitsConverter instance (double, Resources) overloads
    assertThat(MetricUnitsConverter.INSTANCE.toDistanceString(meters(450), jaResources))
        .isEqualTo("450 m");
    assertThat(ImperialUnitsConverter.INSTANCE.toDistanceString(miles(2.5), jaResources))
        .isEqualTo("2.5 マイル");
    assertThat(ImperialUnitsConverter.INSTANCE.toElevationString(meters(4401.2), jaResources))
        .isEqualTo("14,440 フィート");

    // Top-level Units static (double, Resources) and (double, Resources, UnitsConverter) overloads
    assertThat(toDistanceString(meters(450), jaResources)).isEqualTo("450 m");
    assertThat(toElevationString(meters(4401.2), jaResources)).isEqualTo("4,401 m");
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
