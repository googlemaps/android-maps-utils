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
package com.google.maps.android

import android.content.Context
import android.content.res.Resources
import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import io.mockk.every
import io.mockk.mockk
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.util.Locale

/**
 * Comprehensive unit tests for [Meters], [UnitSystem], [UnitsConverter], [MetricUnitsConverter],
 * [ImperialUnitsConverter], and logical distance/elevation formatting.
 */
@RunWith(RobolectricTestRunner::class)
class UnitsTest {

    // =============================================================================================
    // § 1. Physical Conversion Constants & `Meters` Value Class
    // =============================================================================================

    @Test
    fun conversionConstants_areMathematicallyConsistent() {
        assertThat(METERS_PER_FOOT).isEqualTo(0.3048)
        assertThat(METERS_PER_KILOMETER).isEqualTo(1_000)
        assertThat(FEET_PER_MILE).isEqualTo(5_280)
        assertThat(METERS_PER_MILE).isWithin(1e-9).of(1609.344)
        assertThat(METERS_PER_FOOT * FEET_PER_METER).isWithin(1e-12).of(1.0)
        assertThat(METERS_PER_MILE * MILES_PER_METER).isWithin(1e-12).of(1.0)
    }

    @Test
    fun numberExtensions_constructMetersAccurately() {
        assertThat(10.meters).isEqualTo(10.m)
        assertThat(10.meters.value).isEqualTo(10.0)

        assertThat(1.km).isEqualTo(1_000.m)
        assertThat(2.5.kilometers).isEqualTo(2_500.meters)

        assertThat(10.feet.value).isWithin(1e-9).of(3.048)
        assertThat(10.ft).isEqualTo(10.feet)

        assertThat(1.miles.value).isWithin(1e-9).of(1609.344)
        assertThat(1.mi).isEqualTo(1.miles)
        assertThat(5280.feet.value).isWithin(1e-9).of(1.miles.value)
    }

    @Test
    fun metersExtensions_convertToScalarUnitsAccurately() {
        assertThat(1500.meters.toMeters).isEqualTo(1500.0)
        assertThat(1234.m.toKilometers).isWithin(1e-6).of(1.234)
        assertThat(300.meters.toFeet).isWithin(1e-4).of(984.252)
        assertThat(10.km.toMiles).isWithin(1e-4).of(6.21371)

        // Exact round-trip conversions
        assertThat(1.miles.toMiles).isWithin(1e-12).of(1.0)
        assertThat(1.feet.toFeet).isWithin(1e-12).of(1.0)
        assertThat(5280.feet.toMiles).isWithin(1e-12).of(1.0)
    }

    @Test
    fun meters_arithmeticAndComparisons_behaveCorrectly() {
        val shortDistance = 100.meters
        val longDistance = 500.meters

        assertThat(shortDistance).isLessThan(longDistance)
        assertThat(longDistance).isGreaterThan(shortDistance)
        assertThat(100.meters).isEquivalentAccordingToCompareTo(100.m)

        assertThat(longDistance + shortDistance).isEqualTo(600.meters)
        assertThat(longDistance - shortDistance).isEqualTo(400.meters)
        assertThat(shortDistance * 3).isEqualTo(300.meters)
        assertThat(longDistance / 2).isEqualTo(250.meters)
        assertThat(longDistance / shortDistance).isEqualTo(5.0)
        assertThat(-shortDistance).isEqualTo((-100).meters)
    }

    // =============================================================================================
    // § 2. Framework-Agnostic `UnitSystem` & Locale Resolution
    // =============================================================================================

    @Test
    fun unitSystem_fromCountryCode_returnsImperialForUsLrMmAndGb() {
        for (code in listOf("US", "us", "LR", "lr", "MM", "mm", "GB", "gb")) {
            assertThat(UnitSystem.fromCountryCode(code)).isEqualTo(UnitSystem.IMPERIAL)
            assertThat(getUnitsConverter(code)).isSameInstanceAs(ImperialUnitsConverter)
        }
    }

    @Test
    fun unitSystem_fromCountryCode_returnsMetricForOtherCountriesAndNullOrBlank() {
        for (code in listOf("CA", "FR", "DE", "IN", "JP", "AU", "BR", "", "   ", null)) {
            assertThat(UnitSystem.fromCountryCode(code)).isEqualTo(UnitSystem.METRIC)
            assertThat(getUnitsConverter(code)).isSameInstanceAs(MetricUnitsConverter)
        }
    }

    @Test
    fun unitSystem_fromLocale_resolvesAppropriateUnitSystem() {
        assertThat(UnitSystem.fromLocale(Locale.US)).isEqualTo(UnitSystem.IMPERIAL)
        assertThat(UnitSystem.fromLocale(Locale.UK)).isEqualTo(UnitSystem.IMPERIAL)
        assertThat(UnitSystem.fromLocale(Locale.Builder().setLanguage("en").setRegion("LR").build()))
            .isEqualTo(UnitSystem.IMPERIAL)
        assertThat(UnitSystem.fromLocale(Locale.Builder().setLanguage("my").setRegion("MM").build()))
            .isEqualTo(UnitSystem.IMPERIAL)

        assertThat(UnitSystem.fromLocale(Locale.CANADA)).isEqualTo(UnitSystem.METRIC)
        assertThat(UnitSystem.fromLocale(Locale.FRANCE)).isEqualTo(UnitSystem.METRIC)
        assertThat(UnitSystem.fromLocale(Locale.JAPAN)).isEqualTo(UnitSystem.METRIC)
    }

    @Test
    fun getUnitsConverter_defaultsToDeviceLocale() {
        val originalLocale = Locale.getDefault()
        try {
            Locale.setDefault(Locale.US)
            assertThat(UnitSystem.fromLocale()).isEqualTo(UnitSystem.IMPERIAL)
            assertThat(getUnitsConverter()).isSameInstanceAs(ImperialUnitsConverter)

            Locale.setDefault(Locale.GERMANY)
            assertThat(UnitSystem.fromLocale()).isEqualTo(UnitSystem.METRIC)
            assertThat(getUnitsConverter()).isSameInstanceAs(MetricUnitsConverter)
        } finally {
            Locale.setDefault(originalLocale)
        }
    }

    @Test
    fun getUnitsConverter_fromExplicitUnitSystem_returnsExpectedConverter() {
        assertThat(getUnitsConverter(UnitSystem.METRIC)).isSameInstanceAs(MetricUnitsConverter)
        assertThat(getUnitsConverter(UnitSystem.IMPERIAL)).isSameInstanceAs(ImperialUnitsConverter)
        assertThat(MetricUnitsConverter.unitSystem).isEqualTo(UnitSystem.METRIC)
        assertThat(ImperialUnitsConverter.unitSystem).isEqualTo(UnitSystem.IMPERIAL)
    }

    // =============================================================================================
    // § 3. Logical Distance Formatting: Metric (`m` and `km`)
    // =============================================================================================

    @Test
    fun metricDistance_belowOneKilometer_formatsAsWholeMeters() {
        assertThat(MetricUnitsConverter.toDistanceString(0.meters, Locale.US)).isEqualTo("0 m")
        assertThat(MetricUnitsConverter.toDistanceString(100.meters, Locale.US)).isEqualTo("100 m")
        assertThat(MetricUnitsConverter.toDistanceString(450.meters, Locale.US)).isEqualTo("450 m")
        assertThat(MetricUnitsConverter.toDistanceString(999.meters, Locale.US)).isEqualTo("999 m")

        val template = MetricUnitsConverter.toDistanceUnits(450.meters)
        assertThat(template.unitsTemplate).isEqualTo(R.string.amu_in_meters)
        assertThat(template.value).isEqualTo(450.0)
    }

    @Test
    fun metricDistance_fromOneToUnderTenKilometers_keepsOneDecimalWithTrailingZero() {
        assertThat(MetricUnitsConverter.toDistanceString(1_000.meters, Locale.US)).isEqualTo("1.0 km")
        assertThat(MetricUnitsConverter.toDistanceString(1_010.meters, Locale.US)).isEqualTo("1.0 km")
        assertThat(MetricUnitsConverter.toDistanceString(1_500.meters, Locale.US)).isEqualTo("1.5 km")
        assertThat(MetricUnitsConverter.toDistanceString(1_900.meters, Locale.US)).isEqualTo("1.9 km")
        assertThat(MetricUnitsConverter.toDistanceString(2_000.meters, Locale.US)).isEqualTo("2.0 km")
        assertThat(MetricUnitsConverter.toDistanceString(9_900.meters, Locale.US)).isEqualTo("9.9 km")

        val template = MetricUnitsConverter.toDistanceUnits(2_000.meters)
        assertThat(template.unitsTemplate).isEqualTo(R.string.amu_in_kilometers)
        assertThat(template.value).isEqualTo(2.0)
    }

    @Test
    fun metricDistance_tenKilometersOrGreater_roundsToWholeKilometers() {
        // 9.96 km rounds to 10 at 1 decimal place, so it should use the whole-number format ("10 km")
        assertThat(MetricUnitsConverter.toDistanceString(9_960.meters, Locale.US)).isEqualTo("10 km")
        assertThat(MetricUnitsConverter.toDistanceString(10_000.meters, Locale.US)).isEqualTo("10 km")
        assertThat(MetricUnitsConverter.toDistanceString(10_400.meters, Locale.US)).isEqualTo("10 km")
        assertThat(MetricUnitsConverter.toDistanceString(11_600.meters, Locale.US)).isEqualTo("12 km")
        assertThat(MetricUnitsConverter.toDistanceString(1_250.km, Locale.US)).isEqualTo("1,250 km")

        val template = MetricUnitsConverter.toDistanceUnits(10_400.meters)
        assertThat(template.unitsTemplate).isEqualTo(R.string.amu_in_kilometers_whole)
        assertThat(template.value).isEqualTo(10.4)
    }

    // =============================================================================================
    // § 4. Logical Distance Formatting: Imperial (`ft` and `mi`, switching at 0.25 miles)
    // =============================================================================================

    @Test
    fun imperialDistance_belowQuarterMile_formatsAsWholeFeet() {
        assertThat(ImperialUnitsConverter.toDistanceString(0.meters, Locale.US)).isEqualTo("0 ft")
        assertThat(ImperialUnitsConverter.toDistanceString(100.meters, Locale.US)).isEqualTo("328 ft")
        assertThat(ImperialUnitsConverter.toDistanceString(0.1.miles, Locale.US)).isEqualTo("528 ft")
        assertThat(ImperialUnitsConverter.toDistanceString(1_319.feet, Locale.US)).isEqualTo("1,319 ft")

        val template = ImperialUnitsConverter.toDistanceUnits(0.1.miles)
        assertThat(template.unitsTemplate).isEqualTo(R.string.amu_in_feet)
        assertThat(template.value).isWithin(0.01).of(528.0)
    }

    @Test
    fun imperialDistance_fromQuarterMileToUnderTenMiles_keepsOneDecimalWithTrailingZero() {
        // Exactly 0.25 miles (1,320 feet) switches to miles ("0.3 mi" at 1 decimal place)
        assertThat(ImperialUnitsConverter.toDistanceString(0.25.miles, Locale.US)).isEqualTo("0.3 mi")
        assertThat(ImperialUnitsConverter.toDistanceString(0.5.miles, Locale.US)).isEqualTo("0.5 mi")
        assertThat(ImperialUnitsConverter.toDistanceString(1.0.miles, Locale.US)).isEqualTo("1.0 mi")
        assertThat(ImperialUnitsConverter.toDistanceString(2_000.meters, Locale.US)).isEqualTo("1.2 mi")
        assertThat(ImperialUnitsConverter.toDistanceString(3_200.meters, Locale.US)).isEqualTo("2.0 mi")
        assertThat(ImperialUnitsConverter.toDistanceString(2.5.miles, Locale.US)).isEqualTo("2.5 mi")
        assertThat(ImperialUnitsConverter.toDistanceString(9.9.miles, Locale.US)).isEqualTo("9.9 mi")

        val template = ImperialUnitsConverter.toDistanceUnits(2.5.miles)
        assertThat(template.unitsTemplate).isEqualTo(R.string.amu_in_miles)
        assertThat(template.value).isWithin(1e-9).of(2.5)
    }

    @Test
    fun imperialDistance_tenMilesOrGreater_roundsToWholeMiles() {
        assertThat(ImperialUnitsConverter.toDistanceString(9.96.miles, Locale.US)).isEqualTo("10 mi")
        assertThat(ImperialUnitsConverter.toDistanceString(10.0.miles, Locale.US)).isEqualTo("10 mi")
        assertThat(ImperialUnitsConverter.toDistanceString(16_100.meters, Locale.US)).isEqualTo("10 mi")
        assertThat(ImperialUnitsConverter.toDistanceString(25.6.miles, Locale.US)).isEqualTo("26 mi")
        assertThat(ImperialUnitsConverter.toDistanceString(1_250.miles, Locale.US)).isEqualTo("1,250 mi")

        val template = ImperialUnitsConverter.toDistanceUnits(10.0.miles)
        assertThat(template.unitsTemplate).isEqualTo(R.string.amu_in_miles_whole)
        assertThat(template.value).isWithin(1e-9).of(10.0)
    }

    // =============================================================================================
    // § 5. Elevation Formatting (`m` and `ft` only)
    // =============================================================================================

    @Test
    fun elevationFormatting_alwaysUsesMetersOrFeetRegardlessOfMagnitude() {
        // Mount Elbert: 4,401.2 m / 14,440 ft
        val mountElbert = 4401.2.meters
        assertThat(MetricUnitsConverter.toElevationString(mountElbert, Locale.US)).isEqualTo("4,401 m")
        assertThat(ImperialUnitsConverter.toElevationString(mountElbert, Locale.US)).isEqualTo("14,440 ft")

        // Mount Everest: 8,848.86 m / 29,032 ft
        val mountEverest = 8848.86.meters
        assertThat(MetricUnitsConverter.toElevationString(mountEverest, Locale.US)).isEqualTo("8,849 m")
        assertThat(ImperialUnitsConverter.toElevationString(mountEverest, Locale.US)).isEqualTo("29,032 ft")

        // Below sea level (Death Valley: -86 m / -282 ft)
        val deathValley = (-86).meters
        assertThat(MetricUnitsConverter.toElevationString(deathValley, Locale.US)).isEqualTo("-86 m")
        assertThat(ImperialUnitsConverter.toElevationString(deathValley, Locale.US)).isEqualTo("-282 ft")
    }

    @Test
    fun negativeNearZeroValues_doNotFormatWithNegativeSign() {
        assertThat(MetricUnitsConverter.toDistanceString((-0.01).meters, Locale.US)).isEqualTo("0 m")
        assertThat(ImperialUnitsConverter.toDistanceString((-0.01).meters, Locale.US)).isEqualTo("0 ft")
        assertThat(MetricUnitsConverter.toElevationString((-0.01).meters, Locale.US)).isEqualTo("0 m")
        assertThat(ImperialUnitsConverter.toElevationString((-0.01).meters, Locale.US)).isEqualTo("0 ft")
        assertThat(formatDistanceValue(-0.01, Locale.US)).isEqualTo("0.0")
    }

    // =============================================================================================
    // § 6. Android `Resources`, Extension Functions, & Raw `Double` Overloads
    // =============================================================================================

    @Test
    fun androidResourcesFormatting_matchesPureJvmFormatting() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val resources = context.resources

        assertThat(MetricUnitsConverter.toDistanceString(resources, 450.meters)).isEqualTo("450 m")
        assertThat(MetricUnitsConverter.toDistanceString(resources, 2_000.meters)).isEqualTo("2.0 km")
        assertThat(MetricUnitsConverter.toDistanceString(resources, 12_400.meters)).isEqualTo("12 km")
        assertThat(MetricUnitsConverter.toElevationString(resources, 4401.2.meters)).isEqualTo("4,401 m")

        assertThat(ImperialUnitsConverter.toDistanceString(resources, 0.1.miles)).isEqualTo("528 ft")
        assertThat(ImperialUnitsConverter.toDistanceString(resources, 2.0.miles)).isEqualTo("2.0 mi")
        assertThat(ImperialUnitsConverter.toDistanceString(resources, 12.4.miles)).isEqualTo("12 mi")
        assertThat(ImperialUnitsConverter.toElevationString(resources, 4401.2.meters)).isEqualTo("14,440 ft")

        // Extension functions with Resources
        assertThat(2_000.meters.toDistanceString(resources, MetricUnitsConverter)).isEqualTo("2.0 km")
        assertThat(4401.2.meters.toElevationString(resources, ImperialUnitsConverter)).isEqualTo("14,440 ft")
    }

    @Test
    fun mockedResourcesFormatting_invokesExpectedResourceTemplates() {
        val mockResources = mockk<Resources>()
        every { mockResources.getString(R.string.amu_in_feet, *anyVararg()) } returns "528 ft"
        every { mockResources.getString(R.string.amu_in_kilometers, *anyVararg()) } returns "2.5 km"

        assertThat(ImperialUnitsConverter.toDistanceString(mockResources, 0.1.miles)).isEqualTo("528 ft")
        assertThat(MetricUnitsConverter.toDistanceString(mockResources, 2_500.meters)).isEqualTo("2.5 km")
    }

    @Test
    fun metersExtensionsAndDoubleOverloads_formatCorrectly() {
        // Extension functions with Locale
        assertThat(2_000.meters.toDistanceString(Locale.US)).isEqualTo("1.2 mi")
        assertThat(2_000.meters.toDistanceString(Locale.FRANCE)).isEqualTo("2,0 km")
        assertThat(2_000.meters.toDistanceString(MetricUnitsConverter)).isEqualTo(
            MetricUnitsConverter.toDistanceString(2_000.meters),
        )

        assertThat(4401.2.meters.toElevationString(Locale.US)).isEqualTo("14,440 ft")
        assertThat(4401.2.meters.toElevationString(MetricUnitsConverter)).isEqualTo(
            MetricUnitsConverter.toElevationString(4401.2.meters),
        )

        // Raw Double overloads (for Java callers)
        val context = ApplicationProvider.getApplicationContext<Context>()
        val resources = context.resources
        assertThat(MetricUnitsConverter.toDistanceUnits(2000.0).value).isEqualTo(2.0)
        assertThat(MetricUnitsConverter.toElevationUnits(450.0).value).isEqualTo(450.0)
        assertThat(MetricUnitsConverter.toDistanceString(2000.0, Locale.US)).isEqualTo("2.0 km")
        assertThat(MetricUnitsConverter.toElevationString(450.0, Locale.US)).isEqualTo("450 m")
        assertThat(MetricUnitsConverter.toDistanceString(resources, 2000.0)).isEqualTo("2.0 km")
        assertThat(MetricUnitsConverter.toElevationString(resources, 450.0)).isEqualTo("450 m")

        // Standalone logical number formatter
        assertThat(formatDistanceValue(0.25, Locale.US)).isEqualTo("0.3")
        assertThat(formatDistanceValue(2.0, Locale.US)).isEqualTo("2.0")
        assertThat(formatDistanceValue(9.94, Locale.US)).isEqualTo("9.9")
        assertThat(formatDistanceValue(9.96, Locale.US)).isEqualTo("10")
        assertThat(formatDistanceValue(12.4, Locale.US)).isEqualTo("12")
    }

    @Test
    fun numberFormatSymbols_respectsLocaleGroupingAndDecimalRulesInPureKotlin() {
        val usSymbols = NumberFormatSymbols.fromLocale(Locale.US)
        assertThat(usSymbols.groupingSeparator).isEqualTo(',')
        assertThat(usSymbols.decimalSeparator).isEqualTo('.')
        assertThat(usSymbols.groupingSize).isEqualTo(3)
        assertThat(MetricUnitsConverter.toElevationString(1_234_567.meters, usSymbols)).isEqualTo("1,234,567 m")
        assertThat(MetricUnitsConverter.toDistanceString(2_500.0, usSymbols)).isEqualTo("2.5 km")

        val deSymbols = NumberFormatSymbols.fromLocale(Locale.GERMANY)
        assertThat(deSymbols.groupingSeparator).isEqualTo('.')
        assertThat(deSymbols.decimalSeparator).isEqualTo(',')
        assertThat(deSymbols.groupingSize).isEqualTo(3)
        assertThat(MetricUnitsConverter.toElevationString(4401.2.meters, deSymbols)).isEqualTo("4.401 m")
        assertThat(MetricUnitsConverter.toElevationString(4401.2, Locale.GERMANY)).isEqualTo("4.401 m")
        assertThat(MetricUnitsConverter.toDistanceString(2_000.meters, deSymbols)).isEqualTo("2,0 km")
        assertThat(formatDistanceValue(1_250.4, deSymbols)).isEqualTo("1.250")

        // Custom KMP symbols (e.g. grouping disabled)
        val ungroupedSymbols = NumberFormatSymbols(groupingSeparator = '_', decimalSeparator = '.', groupingSize = 0)
        assertThat(ImperialUnitsConverter.toElevationString(4401.2.meters, ungroupedSymbols)).isEqualTo("14440 ft")
        assertThat(4401.2.meters.toElevationString(ImperialUnitsConverter, ungroupedSymbols)).isEqualTo("14440 ft")
        assertThat(2_500.meters.toDistanceString(MetricUnitsConverter, ungroupedSymbols)).isEqualTo("2.5 km")
    }
}
