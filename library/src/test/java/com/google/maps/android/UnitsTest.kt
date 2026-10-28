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

import android.content.res.Configuration
import android.content.res.Resources
import com.google.common.truth.Truth.assertThat
import io.mockk.every
import io.mockk.mockkStatic
import org.junit.Assert.assertThrows
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import java.text.DecimalFormat
import java.text.NumberFormat
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
        assertThat(10.meters.value).isEqualTo(10.0)
        assertThat(1.kilometers).isEqualTo(1_000.meters)
        assertThat(2.5.kilometers).isEqualTo(2_500.meters)
        assertThat(10.feet.value).isWithin(1e-9).of(3.048)
        assertThat(1.miles.value).isWithin(1e-9).of(1609.344)
        assertThat(5280.feet.value).isWithin(1e-9).of(1.miles.value)
    }

    @Test
    fun metersExtensions_convertToScalarUnitsAccurately() {
        assertThat(1500.meters.inMeters).isEqualTo(1500.0)
        assertThat(1234.meters.inKilometers).isWithin(1e-6).of(1.234)
        assertThat(300.meters.inFeet).isWithin(1e-4).of(984.252)
        assertThat(10.kilometers.inMiles).isWithin(1e-4).of(6.21371)

        // Exact round-trip conversions
        assertThat(1.miles.inMiles).isWithin(1e-12).of(1.0)
        assertThat(1.feet.inFeet).isWithin(1e-12).of(1.0)
        assertThat(5280.feet.inMiles).isWithin(1e-12).of(1.0)
    }

    @Test
    fun meters_arithmeticAndComparisons_behaveCorrectly() {
        val shortDistance = 100.meters
        val longDistance = 500.meters

        assertThat(shortDistance).isLessThan(longDistance)
        assertThat(longDistance).isGreaterThan(shortDistance)
        assertThat(100.meters).isEquivalentAccordingToCompareTo(Meters(100.0))

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
    fun unitSystem_fromCountryCode_returnsImperialForUsLrMm_andMixedForGb() {
        for (code in listOf("US", "us", "LR", "lr", "MM", "mm")) {
            assertThat(UnitSystem.fromCountryCode(code)).isEqualTo(UnitSystem.IMPERIAL)
            assertThat(UnitSystem.distanceFromCountryCode(code)).isEqualTo(UnitSystem.IMPERIAL)
            assertThat(UnitSystem.elevationFromCountryCode(code)).isEqualTo(UnitSystem.IMPERIAL)
            assertThat(getUnitsConverter(code)).isSameInstanceAs(ImperialUnitsConverter)
        }
        for (code in listOf("GB", "gb")) {
            assertThat(UnitSystem.fromCountryCode(code)).isEqualTo(UnitSystem.IMPERIAL)
            assertThat(UnitSystem.distanceFromCountryCode(code)).isEqualTo(UnitSystem.IMPERIAL)
            assertThat(UnitSystem.elevationFromCountryCode(code)).isEqualTo(UnitSystem.METRIC)
            val gbConverter = getUnitsConverter(code)
            assertThat(gbConverter.toDistanceString(2_000.meters, Locale.UK)).isEqualTo("1.2 mi")
            assertThat(gbConverter.toElevationString(1_345.meters, Locale.UK)).isEqualTo("1,345 m")
        }
    }

    @Test
    fun unitSystem_fromCountryCode_returnsMetricForOtherCountriesAndNullOrBlank() {
        for (code in listOf("CA", "FR", "DE", "IN", "JP", "AU", "BR", "", "   ", null)) {
            assertThat(UnitSystem.fromCountryCode(code)).isEqualTo(UnitSystem.METRIC)
            assertThat(UnitSystem.distanceFromCountryCode(code)).isEqualTo(UnitSystem.METRIC)
            assertThat(UnitSystem.elevationFromCountryCode(code)).isEqualTo(UnitSystem.METRIC)
            assertThat(getUnitsConverter(code)).isSameInstanceAs(MetricUnitsConverter)
        }
    }

    @Test
    fun unitSystem_fromLocale_resolvesAppropriateDistanceAndElevationUnitSystems() {
        assertThat(UnitSystem.fromLocale(Locale.US)).isEqualTo(UnitSystem.IMPERIAL)
        assertThat(UnitSystem.distanceFromLocale(Locale.US)).isEqualTo(UnitSystem.IMPERIAL)
        assertThat(UnitSystem.elevationFromLocale(Locale.US)).isEqualTo(UnitSystem.IMPERIAL)

        // GB uses Imperial for road distances (ft/mi) and Metric for elevations (m)
        assertThat(UnitSystem.fromLocale(Locale.UK)).isEqualTo(UnitSystem.IMPERIAL)
        assertThat(UnitSystem.distanceFromLocale(Locale.UK)).isEqualTo(UnitSystem.IMPERIAL)
        assertThat(UnitSystem.elevationFromLocale(Locale.UK)).isEqualTo(UnitSystem.METRIC)
        assertThat(2_000.meters.toDistanceString(Locale.UK)).isEqualTo("1.2 mi")
        assertThat(4401.2.meters.toElevationString(Locale.UK)).isEqualTo("4,401 m")

        val lrLocale = Locale.Builder().setLanguage("en").setRegion("LR").build()
        assertThat(UnitSystem.distanceFromLocale(lrLocale)).isEqualTo(UnitSystem.IMPERIAL)
        assertThat(UnitSystem.elevationFromLocale(lrLocale)).isEqualTo(UnitSystem.IMPERIAL)

        val mmLocale = Locale.Builder().setLanguage("my").setRegion("MM").build()
        assertThat(UnitSystem.distanceFromLocale(mmLocale)).isEqualTo(UnitSystem.IMPERIAL)
        assertThat(UnitSystem.elevationFromLocale(mmLocale)).isEqualTo(UnitSystem.IMPERIAL)

        assertThat(UnitSystem.fromLocale(Locale.CANADA)).isEqualTo(UnitSystem.METRIC)
        assertThat(UnitSystem.distanceFromLocale(Locale.FRANCE)).isEqualTo(UnitSystem.METRIC)
        assertThat(UnitSystem.elevationFromLocale(Locale.FRANCE)).isEqualTo(UnitSystem.METRIC)
        assertThat(UnitSystem.fromLocale(Locale.JAPAN)).isEqualTo(UnitSystem.METRIC)
    }

    @Test
    fun preferredUnitsOverrides_changeDistanceOrElevationUnitsSimply() {
        // 1. Overriding elevation or distance on a locale-resolved converter via withElevationUnits / withDistanceUnits
        val ukDefault = getUnitsConverter(Locale.UK)
        assertThat(ukDefault.toDistanceString(2_000.meters, Locale.UK)).isEqualTo("1.2 mi")
        assertThat(ukDefault.toElevationString(1_345.meters, Locale.UK)).isEqualTo("1,345 m")

        val ukWithFeetElevation = ukDefault.withElevationUnits(UnitSystem.IMPERIAL)
        assertThat(ukWithFeetElevation).isSameInstanceAs(ImperialUnitsConverter)
        assertThat(ukWithFeetElevation.toDistanceString(2_000.meters, Locale.UK)).isEqualTo("1.2 mi")
        assertThat(ukWithFeetElevation.toElevationString(1_345.meters, Locale.UK)).isEqualTo("4,413 ft")

        val ukWithMetricDistance = ukDefault.withDistanceUnits(UnitSystem.METRIC)
        assertThat(ukWithMetricDistance).isSameInstanceAs(MetricUnitsConverter)
        assertThat(ukWithMetricDistance.toDistanceString(2_000.meters, Locale.UK)).isEqualTo("2.0 km")
        assertThat(ukWithMetricDistance.toElevationString(1_345.meters, Locale.UK)).isEqualTo("1,345 m")

        // 2. Overriding distance or elevation directly in getUnitsConverter(...)
        val usWithMetricElevation = getUnitsConverter(Locale.US, elevationUnits = UnitSystem.METRIC)
        assertThat(usWithMetricElevation.toDistanceString(2_000.meters, Locale.US)).isEqualTo("1.2 mi")
        assertThat(usWithMetricElevation.toElevationString(4401.2.meters, Locale.US)).isEqualTo("4,401 m")

        val deWithImperialDistance = getUnitsConverter(Locale.GERMANY, distanceUnits = UnitSystem.IMPERIAL)
        assertThat(deWithImperialDistance.toDistanceString(2_000.meters, Locale.GERMANY)).isEqualTo("1,2 mi")
        assertThat(deWithImperialDistance.toElevationString(4401.2.meters, Locale.GERMANY)).isEqualTo("4.401 m")

        // 3. Constructing directly from (distanceUnits, elevationUnits)
        val hybrid = UnitsConverter(distanceUnits = UnitSystem.IMPERIAL, elevationUnits = UnitSystem.METRIC)
        assertThat(hybrid).isSameInstanceAs(ukDefault)
        assertThat(UnitsConverter(UnitSystem.METRIC, UnitSystem.METRIC)).isSameInstanceAs(MetricUnitsConverter)
        assertThat(UnitsConverter(UnitSystem.IMPERIAL, UnitSystem.IMPERIAL)).isSameInstanceAs(ImperialUnitsConverter)
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
        assertThat(MetricUnitsConverter.toDistanceString(999.4.meters, Locale.US)).isEqualTo("999 m")
    }

    @Test
    fun metricDistance_fromOneToUnderTenKilometers_keepsOneDecimalWithTrailingZero() {
        // 999.6 m rounds to 1,000 m, so it must switch to the kilometers branch ("1.0 km") rather than "1,000 m"
        assertThat(MetricUnitsConverter.toDistanceString(999.6.meters, Locale.US)).isEqualTo("1.0 km")
        assertThat(MetricUnitsConverter.toDistanceString(1_000.meters, Locale.US)).isEqualTo("1.0 km")
        assertThat(MetricUnitsConverter.toDistanceString(1_010.meters, Locale.US)).isEqualTo("1.0 km")
        assertThat(MetricUnitsConverter.toDistanceString(1_500.meters, Locale.US)).isEqualTo("1.5 km")
        assertThat(MetricUnitsConverter.toDistanceString(1_900.meters, Locale.US)).isEqualTo("1.9 km")
        assertThat(MetricUnitsConverter.toDistanceString(2_000.meters, Locale.US)).isEqualTo("2.0 km")
        assertThat(MetricUnitsConverter.toDistanceString(9_900.meters, Locale.US)).isEqualTo("9.9 km")
    }

    @Test
    fun metricDistance_tenKilometersOrGreater_roundsToWholeKilometers() {
        // 9.96 km rounds to 10 at 1 decimal place, so it should use the whole-number format ("10 km")
        assertThat(MetricUnitsConverter.toDistanceString(9_960.meters, Locale.US)).isEqualTo("10 km")
        assertThat(MetricUnitsConverter.toDistanceString(10_000.meters, Locale.US)).isEqualTo("10 km")
        assertThat(MetricUnitsConverter.toDistanceString(10_400.meters, Locale.US)).isEqualTo("10 km")
        assertThat(MetricUnitsConverter.toDistanceString(11_600.meters, Locale.US)).isEqualTo("12 km")
        assertThat(MetricUnitsConverter.toDistanceString(1_250.kilometers, Locale.US)).isEqualTo("1,250 km")
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
        assertThat(ImperialUnitsConverter.toDistanceString(1_319.4.feet, Locale.US)).isEqualTo("1,319 ft")
    }

    @Test
    fun imperialDistance_fromQuarterMileToUnderTenMiles_keepsOneDecimalWithTrailingZero() {
        // 1,319.6 ft rounds to 1,320 ft (0.25 mi), so it must switch to miles ("0.3 mi") rather than "1,320 ft"
        assertThat(ImperialUnitsConverter.toDistanceString(1_319.6.feet, Locale.US)).isEqualTo("0.3 mi")
        // Exactly 0.25 miles (1,320 feet) switches to miles ("0.3 mi" at 1 decimal place)
        assertThat(ImperialUnitsConverter.toDistanceString(0.25.miles, Locale.US)).isEqualTo("0.3 mi")
        assertThat(ImperialUnitsConverter.toDistanceString(0.5.miles, Locale.US)).isEqualTo("0.5 mi")
        assertThat(ImperialUnitsConverter.toDistanceString(1.0.miles, Locale.US)).isEqualTo("1.0 mi")
        assertThat(ImperialUnitsConverter.toDistanceString(2_000.meters, Locale.US)).isEqualTo("1.2 mi")
        assertThat(ImperialUnitsConverter.toDistanceString(3_200.meters, Locale.US)).isEqualTo("2.0 mi")
        assertThat(ImperialUnitsConverter.toDistanceString(2.5.miles, Locale.US)).isEqualTo("2.5 mi")
        assertThat(ImperialUnitsConverter.toDistanceString(9.9.miles, Locale.US)).isEqualTo("9.9 mi")
    }

    @Test
    fun imperialDistance_tenMilesOrGreater_roundsToWholeMiles() {
        assertThat(ImperialUnitsConverter.toDistanceString(9.96.miles, Locale.US)).isEqualTo("10 mi")
        assertThat(ImperialUnitsConverter.toDistanceString(10.0.miles, Locale.US)).isEqualTo("10 mi")
        assertThat(ImperialUnitsConverter.toDistanceString(16_100.meters, Locale.US)).isEqualTo("10 mi")
        assertThat(ImperialUnitsConverter.toDistanceString(25.6.miles, Locale.US)).isEqualTo("26 mi")
        assertThat(ImperialUnitsConverter.toDistanceString(1_250.miles, Locale.US)).isEqualTo("1,250 mi")
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
    // § 6. Extension Functions, Raw `Double` Overloads, & `NumberFormatSymbols`
    // =============================================================================================

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
        assertThat(MetricUnitsConverter.toDistanceString(2000.0, Locale.US)).isEqualTo("2.0 km")
        assertThat(MetricUnitsConverter.toElevationString(450.0, Locale.US)).isEqualTo("450 m")

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

        // When a locale's NumberFormat has isGroupingUsed == false, fromLocale must return groupingSize = 0
        val ungroupedFormat = (NumberFormat.getNumberInstance(Locale.US) as DecimalFormat).apply {
            isGroupingUsed = false
        }
        mockkStatic(NumberFormat::class) {
            every { NumberFormat.getNumberInstance(Locale.US) } returns ungroupedFormat
            val symbolsFromUngroupedLocale = NumberFormatSymbols.fromLocale(Locale.US)
            assertThat(symbolsFromUngroupedLocale.groupingSize).isEqualTo(0)
            assertThat(ImperialUnitsConverter.toElevationString(4401.2.meters, symbolsFromUngroupedLocale))
                .isEqualTo("14440 ft")
        }
    }

    // =============================================================================================
    // § 7. Public `NumberFormatSymbols` Formatting & Custom / Composite `UnitsConverter`s
    // =============================================================================================

    @Test
    fun numberFormatSymbols_formatFixedDecimal_and_formatDistanceValue_supportArbitraryPrecision() {
        val usSymbols = NumberFormatSymbols.DEFAULT
        assertThat(usSymbols.formatFixedDecimal(1234.567)).isEqualTo("1,235")
        assertThat(usSymbols.formatFixedDecimal(1234.567, decimals = 0)).isEqualTo("1,235")
        assertThat(usSymbols.formatFixedDecimal(1234.567, decimals = 1)).isEqualTo("1,234.6")
        assertThat(usSymbols.formatFixedDecimal(1234.5, decimals = 2)).isEqualTo("1,234.50")
        assertThat(usSymbols.formatFixedDecimal(1.006, decimals = 2)).isEqualTo("1.01")
        assertThat(usSymbols.formatFixedDecimal(1.05, decimals = 2)).isEqualTo("1.05")
        assertThat(usSymbols.formatFixedDecimal(-0.001, decimals = 2)).isEqualTo("0.00")
        assertThat(usSymbols.formatFixedDecimal(-42.5, decimals = 1)).isEqualTo("-42.5")

        val deSymbols = NumberFormatSymbols.fromLocale(Locale.GERMANY)
        assertThat(deSymbols.formatFixedDecimal(1234.5, decimals = 2)).isEqualTo("1.234,50")
        assertThat(deSymbols.formatDistanceValue(2.0)).isEqualTo("2,0")
        assertThat(deSymbols.formatDistanceValue(1250.4)).isEqualTo("1.250")

        assertThrows(IllegalArgumentException::class.java) {
            usSymbols.formatFixedDecimal(10.0, decimals = -1)
        }
    }

    @Test
    fun customUnitsConverter_mixesAndMatchesConvertersAndCustomFormatters() {
        // 1. Mix & match existing converters (e.g., UK: Imperial road distances + Metric elevations)
        val ukConverter = UnitsConverter(
            distanceConverter = ImperialUnitsConverter,
            elevationConverter = MetricUnitsConverter,
        )
        assertThat(2_000.meters.toDistanceString(Locale.UK, ukConverter)).isEqualTo("1.2 mi")
        assertThat(1_345.meters.toElevationString(Locale.UK, ukConverter)).isEqualTo("1,345 m")

        // 2. Mix converter with a custom formatter lambda
        val imperialWithAslElevation = UnitsConverter(
            distanceConverter = ImperialUnitsConverter,
            elevationFormatter = { meters, symbols ->
                "${symbols.formatFixedDecimal(meters.inMeters, decimals = 1)} m ASL"
            },
        )
        assertThat(imperialWithAslElevation.toDistanceString(0.5.miles, Locale.US)).isEqualTo("0.5 mi")
        assertThat(imperialWithAslElevation.toElevationString(1344.56.meters, Locale.US))
            .isEqualTo("1,344.6 m ASL")

        // 3. Custom distance formatter + existing elevation converter
        val nauticalDistanceWithMetricElevation = UnitsConverter(
            distanceFormatter = { meters, symbols ->
                val nauticalMiles = meters.inMeters / 1852.0
                "${symbols.formatFixedDecimal(nauticalMiles, decimals = 2)} NM"
            },
            elevationConverter = MetricUnitsConverter,
        )
        assertThat(nauticalDistanceWithMetricElevation.toDistanceString(1852.meters, Locale.US))
            .isEqualTo("1.00 NM")
        assertThat(nauticalDistanceWithMetricElevation.toElevationString(450.meters, Locale.US))
            .isEqualTo("450 m")

        // 4. Both custom formatters (or method references)
        val customBoth = UnitsConverter(
            distanceFormatter = { meters, symbols ->
                "${symbols.formatFixedDecimal(meters.inKilometers, decimals = 2)} kilometers"
            },
            elevationFormatter = MetricUnitsConverter::toElevationString,
        )
        assertThat(customBoth.toDistanceString(1500.meters, Locale.FRANCE))
            .isEqualTo("1,50 kilometers")
        assertThat(customBoth.toElevationString(4401.2.meters, Locale.FRANCE))
            .isEqualTo(
                "4\u202f401 m".replace('\u202f', NumberFormatSymbols.fromLocale(Locale.FRANCE).groupingSeparator),
            )
    }

    // =============================================================================================
    // § 8. Android `Resources` Overloads & Localized Unit Label Translations
    // =============================================================================================

    @Test
    fun resourcesOverloads_resolveUnitSystemAndFormatWithLocalizedStringResources() {
        val usResources = resourcesForLocale(Locale.US)
        assertThat(UnitSystem.fromResources(usResources)).isEqualTo(UnitSystem.IMPERIAL)
        assertThat(getUnitsConverter(usResources)).isSameInstanceAs(ImperialUnitsConverter)
        assertThat(0.1.miles.toDistanceString(usResources)).isEqualTo("528 ft")
        assertThat(2.5.miles.toDistanceString(usResources)).isEqualTo("2.5 mi")
        assertThat(4401.2.meters.toElevationString(usResources)).isEqualTo("14,440 ft")

        val deResources = resourcesForLocale(Locale.GERMANY)
        assertThat(UnitSystem.fromResources(deResources)).isEqualTo(UnitSystem.METRIC)
        assertThat(getUnitsConverter(deResources)).isSameInstanceAs(MetricUnitsConverter)
        assertThat(2_000.meters.toDistanceString(deResources)).isEqualTo("2,0 km")
        assertThat(4401.2.meters.toElevationString(deResources)).isEqualTo("4.401 m")

        // UK Resources default out-of-the-box to Imperial road distances + Metric elevations
        val ukResources = resourcesForLocale(Locale.UK)
        assertThat(UnitSystem.distanceFromResources(ukResources)).isEqualTo(UnitSystem.IMPERIAL)
        assertThat(UnitSystem.elevationFromResources(ukResources)).isEqualTo(UnitSystem.METRIC)
        assertThat(2_000.meters.toDistanceString(ukResources)).isEqualTo("1.2 mi")
        assertThat(1_345.meters.toElevationString(ukResources)).isEqualTo("1,345 m")
        assertThat(
            1_345.meters.toElevationString(
                ukResources,
                getUnitsConverter(ukResources, elevationUnits = UnitSystem.IMPERIAL),
            ),
        ).isEqualTo("4,413 ft")
    }

    @Test
    fun localizedStringResources_translateUnitLabelsAcrossLanguages() {
        // Japanese (ja-JP): metric uses "m" / "km", imperial uses "フィート" / "マイル"
        val jaResources = resourcesForLocale(Locale.JAPAN)
        assertThat(450.meters.toDistanceString(jaResources)).isEqualTo("450 m")
        assertThat(2.0.kilometers.toDistanceString(jaResources)).isEqualTo("2.0 km")
        assertThat(0.1.miles.toDistanceString(jaResources, ImperialUnitsConverter)).isEqualTo("528 フィート")
        assertThat(2.5.miles.toDistanceString(jaResources, ImperialUnitsConverter)).isEqualTo("2.5 マイル")
        assertThat(4401.2.meters.toElevationString(jaResources, ImperialUnitsConverter)).isEqualTo("14,440 フィート")

        // Korean (ko-KR): imperial uses "피트" / "마일" (no space per Korean CLDR convention)
        val koResources = resourcesForLocale(Locale.KOREA)
        assertThat(450.meters.toDistanceString(koResources)).isEqualTo("450 m")
        assertThat(2.0.kilometers.toDistanceString(koResources)).isEqualTo("2.0 km")
        assertThat(0.1.miles.toDistanceString(koResources, ImperialUnitsConverter)).isEqualTo("528피트")
        assertThat(2.5.miles.toDistanceString(koResources, ImperialUnitsConverter)).isEqualTo("2.5마일")

        // Simplified Chinese (zh-CN): "米", "公里", "英尺", "英里"
        val zhCnResources = resourcesForLocale(Locale.SIMPLIFIED_CHINESE)
        assertThat(450.meters.toDistanceString(zhCnResources)).isEqualTo("450 米")
        assertThat(2.0.kilometers.toDistanceString(zhCnResources)).isEqualTo("2.0 公里")
        assertThat(0.1.miles.toDistanceString(zhCnResources, ImperialUnitsConverter)).isEqualTo("528 英尺")
        assertThat(2.5.miles.toDistanceString(zhCnResources, ImperialUnitsConverter)).isEqualTo("2.5 英里")

        // Traditional Chinese (zh-TW): "公尺", "公里", "英尺", "英里"
        val zhTwResources = resourcesForLocale(Locale.TRADITIONAL_CHINESE)
        assertThat(450.meters.toDistanceString(zhTwResources)).isEqualTo("450 公尺")
        assertThat(2.0.kilometers.toDistanceString(zhTwResources)).isEqualTo("2.0 公里")

        // Russian (ru-RU): Cyrillic "м", "км", "фт", "ми" + comma decimal separator
        val ruLocale = Locale.Builder().setLanguage("ru").setRegion("RU").build()
        val ruResources = resourcesForLocale(ruLocale)
        assertThat(450.meters.toDistanceString(ruResources)).isEqualTo("450 м")
        assertThat(2.0.kilometers.toDistanceString(ruResources)).isEqualTo("2,0 км")
        assertThat(0.1.miles.toDistanceString(ruResources, ImperialUnitsConverter)).isEqualTo("528 фт")
        assertThat(2.5.miles.toDistanceString(ruResources, ImperialUnitsConverter)).isEqualTo("2,5 ми")

        // Arabic (ar-EG): "م", "كم", "قدم", "ميل"
        val arLocale = Locale.Builder().setLanguage("ar").setRegion("EG").build()
        val arResources = resourcesForLocale(arLocale)
        assertThat(450.meters.toDistanceString(arResources)).endsWith(" م")
        assertThat(2.0.kilometers.toDistanceString(arResources)).endsWith(" كم")
        assertThat(0.1.miles.toDistanceString(arResources, ImperialUnitsConverter)).endsWith(" قدم")
        assertThat(2.5.miles.toDistanceString(arResources, ImperialUnitsConverter)).endsWith(" ميل")

        // French (fr-FR): feet translated as "pi"
        val frResources = resourcesForLocale(Locale.FRANCE)
        assertThat(0.1.miles.toDistanceString(frResources, ImperialUnitsConverter)).isEqualTo("528 pi")
        assertThat(2.0.kilometers.toDistanceString(frResources)).isEqualTo("2,0 km")
    }

    private fun resourcesForLocale(locale: Locale): Resources {
        val baseContext = RuntimeEnvironment.getApplication()
        val config = Configuration(baseContext.resources.configuration).apply {
            setLocale(locale)
        }
        return baseContext.createConfigurationContext(config).resources
    }
}
