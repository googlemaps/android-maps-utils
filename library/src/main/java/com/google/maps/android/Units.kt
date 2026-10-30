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
@file:JvmName("Units")

package com.google.maps.android

import android.content.res.Resources
import androidx.core.os.ConfigurationCompat
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.text.NumberFormat
import java.util.Locale
import kotlin.math.abs
import kotlin.math.roundToLong

// =================================================================================================
// § 1. Physical Conversion Constants
// =================================================================================================
//
// By international agreement (1959 International Yard and Pound Agreement), one international foot
// is defined as exactly 0.3048 meters (12 inches * 2.54 cm/inch / 100 cm/meter).
// All derived imperial-to-metric and metric-to-imperial conversion constants below are grounded in
// this exact definition so that round-trip conversions (e.g., `1.miles.inMiles`, `5280.feet.inMiles`)
// remain mathematically consistent.

// [START maps_android_utils_units_constants]
/** Exact number of meters in one international foot (`0.3048`). */
public const val METERS_PER_FOOT: Double = 0.3048

/** Number of meters in one kilometer (`1,000`). */
public const val METERS_PER_KILOMETER: Int = 1_000

/** Number of international feet in one meter (`~3.280839895013123`). */
public const val FEET_PER_METER: Double = 1.0 / METERS_PER_FOOT

/** Number of feet in one statute mile (`5,280`). */
public const val FEET_PER_MILE: Int = 5_280

/** Exact number of meters in one statute mile (`1,609.344`). */
public const val METERS_PER_MILE: Double = FEET_PER_MILE * METERS_PER_FOOT

/** Number of statute miles in one meter (`~0.000621371192237334`). */
public const val MILES_PER_METER: Double = 1.0 / METERS_PER_MILE
// [END maps_android_utils_units_constants]

// =================================================================================================
// § 2. Type-Safe Length Representation: The `Meters` Value Class
// =================================================================================================
//
// Geographical calculations in the Maps SDK and `SphericalUtil` produce raw `Double` values in
// meters, whereas UI components and domain models frequently mix feet, miles, meters, and
// kilometers. Wrapping scalar measurements in an inline `Meters` value class eliminates unit
// confusion at compile time without incurring heap allocation overhead in Kotlin code.

// [START maps_android_utils_units_meters]

/**
 * An inline value class representing a length, distance, or elevation measurement in meters.
 *
 * Construct instances using the [Number] extension properties ([meters], [kilometers], [feet],
 * [miles]) and convert back to raw scalar units via [inMeters], [inKilometers], [inFeet], and
 * [inMiles].
 *
 * @property value The underlying measurement in meters.
 */
@JvmInline
public value class Meters(public val value: Double) : Comparable<Meters> {
    override fun compareTo(other: Meters): Int = value.compareTo(other.value)

    /** Adds another [Meters] measurement to this measurement. */
    public operator fun plus(other: Meters): Meters = Meters(value = this.value + other.value)

    /** Subtracts another [Meters] measurement from this measurement. */
    public operator fun minus(other: Meters): Meters = Meters(value = this.value - other.value)

    /** Multiplies this measurement by a scalar [Number]. */
    public operator fun times(scalar: Number): Meters = Meters(value = this.value * scalar.toDouble())

    /** Divides this measurement by a scalar [Number]. */
    public operator fun div(scalar: Number): Meters = Meters(value = this.value / scalar.toDouble())

    /** Computes the dimensionless ratio between this measurement and [other]. */
    public operator fun div(other: Meters): Double = this.value / other.value

    /** Negates this measurement. */
    public operator fun unaryMinus(): Meters = Meters(value = -this.value)
}

/** Creates a [Meters] measurement from a [Number] of meters. */
@get:JvmName("meters")
public inline val Number.meters: Meters
    get() = Meters(value = this.toDouble())

/** Creates a [Meters] measurement from a [Number] of kilometers. */
@get:JvmName("kilometers")
public inline val Number.kilometers: Meters
    get() = Meters(value = this.toDouble() * METERS_PER_KILOMETER)

/** Creates a [Meters] measurement from a [Number] of international feet. */
@get:JvmName("feet")
public inline val Number.feet: Meters
    get() = Meters(value = this.toDouble() * METERS_PER_FOOT)

/** Creates a [Meters] measurement from a [Number] of statute miles. */
@get:JvmName("miles")
public inline val Number.miles: Meters
    get() = Meters(value = this.toDouble() * METERS_PER_MILE)

/** Returns the value of this [Meters] measurement in meters as a [Double]. */
@get:JvmName("inMeters")
public inline val Meters.inMeters: Double
    get() = value

/** Returns the equivalent number of kilometers for this [Meters] measurement. */
@get:JvmName("inKilometers")
public inline val Meters.inKilometers: Double
    get() = value / METERS_PER_KILOMETER

/** Returns the equivalent number of international feet for this [Meters] measurement. */
@get:JvmName("inFeet")
public inline val Meters.inFeet: Double
    get() = value * FEET_PER_METER

/** Returns the equivalent number of statute miles for this [Meters] measurement. */
@get:JvmName("inMiles")
public inline val Meters.inMiles: Double
    get() = value * MILES_PER_METER
// [END maps_android_utils_units_meters]

// =================================================================================================
// § 3. Framework-Agnostic Unit System Selection
// =================================================================================================
//
// Developers need a framework-agnostic way to determine whether to display measurements in Metric
// or Imperial units without coupling business logic to Android `Context` or Jetpack Compose
// `LocalConfiguration`. `UnitSystem` resolves the appropriate system from any `java.util.Locale`
// (defaulting to `Locale.getDefault()`) or ISO 3166-1 alpha-2 country code, while allowing
// applications to explicitly override the user's preference.

// [START maps_android_utils_units_system]

/**
 * Represents a measurement system used to format distances and elevations.
 */
public enum class UnitSystem {
    /**
     * The Metric system (meters and kilometers for distances; meters for elevations).
     */
    METRIC,

    /**
     * The Imperial / US customary system (feet and miles for distances; feet for elevations).
     */
    IMPERIAL,
    ;

    /**
     * Returns the [UnitsConverter] strategy corresponding to this [UnitSystem].
     */
    public val converter: UnitsConverter
        get() = when (this) {
            METRIC -> MetricUnitsConverter
            IMPERIAL -> ImperialUnitsConverter
        }

    public companion object {
        /**
         * ISO 3166-1 alpha-2 country codes that default to [IMPERIAL] units for road distances:
         * United States (`"US"`), Liberia (`"LR"`), Myanmar (`"MM"`), and United Kingdom (`"GB"`).
         *
         * Note: While the United Kingdom uses miles and feet for road distances (per the UK
         * *Traffic Signs Regulations and General Directions*), UK topographic maps (Ordnance Survey)
         * display elevations in meters. Therefore, `"GB"` resolves to [IMPERIAL] for distances
         * ([distanceFromCountryCode]) and [METRIC] for elevations ([elevationFromCountryCode]), and
         * [getUnitsConverter] for `"GB"` / [Locale.UK] automatically returns a converter that
         * formats distances in feet/miles and elevations in meters.
         */
        @JvmField
        public val IMPERIAL_COUNTRY_CODES: Set<String> = setOf("US", "LR", "MM", "GB")

        /**
         * ISO 3166-1 alpha-2 country codes that default to [IMPERIAL] units for elevations:
         * United States (`"US"`), Liberia (`"LR"`), and Myanmar (`"MM"`).
         */
        @JvmField
        public val IMPERIAL_ELEVATION_COUNTRY_CODES: Set<String> = setOf("US", "LR", "MM")

        /**
         * Returns the preferred distance [UnitSystem] for the given [locale].
         *
         * Note: For `"GB"` ([Locale.UK]), this returns [IMPERIAL] (the UK road-distance system),
         * whereas [elevationFromLocale] returns [METRIC]. To obtain a [UnitsConverter] that
         * automatically handles mixed distance/elevation locales like `"GB"`, use [getUnitsConverter].
         *
         * @param locale The [Locale] whose country code determines the unit system.
         * @return [IMPERIAL] if the locale's country is in [IMPERIAL_COUNTRY_CODES], otherwise [METRIC].
         */
        @JvmStatic
        @JvmOverloads
        public fun fromLocale(locale: Locale = Locale.getDefault()): UnitSystem = distanceFromLocale(locale)

        /**
         * Returns the preferred distance [UnitSystem] for the given [locale] (defaulting to [Locale.getDefault]).
         *
         * @param locale The [Locale] whose country code determines the distance unit system.
         * @return [IMPERIAL] for `"US"`, `"LR"`, `"MM"`, and `"GB"`, or [METRIC] otherwise.
         */
        @JvmStatic
        @JvmOverloads
        public fun distanceFromLocale(locale: Locale = Locale.getDefault()): UnitSystem =
            distanceFromCountryCode(locale.country)

        /**
         * Returns the preferred elevation [UnitSystem] for the given [locale] (defaulting to [Locale.getDefault]).
         *
         * @param locale The [Locale] whose country code determines the elevation unit system.
         * @return [IMPERIAL] for `"US"`, `"LR"`, and `"MM"`, or [METRIC] otherwise (including `"GB"`).
         */
        @JvmStatic
        @JvmOverloads
        public fun elevationFromLocale(locale: Locale = Locale.getDefault()): UnitSystem =
            elevationFromCountryCode(locale.country)

        /**
         * Returns the preferred distance [UnitSystem] for the primary locale of the given Android [resources].
         *
         * @param resources The Android [Resources] whose configuration locale determines the unit system.
         * @return [IMPERIAL] if the primary locale's country is in [IMPERIAL_COUNTRY_CODES], otherwise [METRIC].
         */
        @JvmStatic
        public fun fromResources(resources: Resources): UnitSystem = distanceFromResources(resources)

        /**
         * Returns the preferred distance [UnitSystem] for the primary locale of the given Android [resources].
         *
         * @param resources The Android [Resources] whose configuration locale determines the distance unit system.
         * @return [IMPERIAL] for `"US"`, `"LR"`, `"MM"`, and `"GB"`, or [METRIC] otherwise.
         */
        @JvmStatic
        public fun distanceFromResources(resources: Resources): UnitSystem =
            distanceFromLocale(primaryLocale(resources))

        /**
         * Returns the preferred elevation [UnitSystem] for the primary locale of the given Android [resources].
         *
         * @param resources The Android [Resources] whose configuration locale determines the elevation unit system.
         * @return [IMPERIAL] for `"US"`, `"LR"`, and `"MM"`, or [METRIC] otherwise (including `"GB"`).
         */
        @JvmStatic
        public fun elevationFromResources(resources: Resources): UnitSystem =
            elevationFromLocale(primaryLocale(resources))

        /**
         * Returns the preferred distance [UnitSystem] for the given ISO 3166-1 alpha-2 [countryCode].
         *
         * Comparison is case-insensitive. If [countryCode] is `null`, blank, or not in
         * [IMPERIAL_COUNTRY_CODES], [METRIC] is returned.
         *
         * @param countryCode The two-letter country code (e.g., `"US"`, `"GB"`, `"FR"`).
         * @return The corresponding distance [UnitSystem].
         */
        @JvmStatic
        public fun fromCountryCode(countryCode: String?): UnitSystem = distanceFromCountryCode(countryCode)

        /**
         * Returns the preferred distance [UnitSystem] for the given ISO 3166-1 alpha-2 [countryCode].
         *
         * @param countryCode The two-letter country code (e.g., `"US"`, `"GB"`, `"FR"`).
         * @return [IMPERIAL] if [countryCode] is in [IMPERIAL_COUNTRY_CODES], otherwise [METRIC].
         */
        @JvmStatic
        public fun distanceFromCountryCode(countryCode: String?): UnitSystem {
            if (countryCode.isNullOrBlank()) return METRIC
            return if (countryCode.uppercase(Locale.ROOT) in IMPERIAL_COUNTRY_CODES) {
                IMPERIAL
            } else {
                METRIC
            }
        }

        /**
         * Returns the preferred elevation [UnitSystem] for the given ISO 3166-1 alpha-2 [countryCode].
         *
         * @param countryCode The two-letter country code (e.g., `"US"`, `"GB"`, `"FR"`).
         * @return [IMPERIAL] if [countryCode] is in [IMPERIAL_ELEVATION_COUNTRY_CODES], otherwise [METRIC].
         */
        @JvmStatic
        public fun elevationFromCountryCode(countryCode: String?): UnitSystem {
            if (countryCode.isNullOrBlank()) return METRIC
            return if (countryCode.uppercase(Locale.ROOT) in IMPERIAL_ELEVATION_COUNTRY_CODES) {
                IMPERIAL
            } else {
                METRIC
            }
        }
    }
}
// [END maps_android_utils_units_system]

/**
 * Platform-agnostic symbols and grouping rules for formatting numeric measurements.
 *
 * Separating locale symbol resolution from the core number formatting algorithm allows distance
 * and elevation formatting to run in pure Kotlin (`kotlin.math` and `StringBuilder`) without
 * relying on `java.util.Formatter` (`String.format`), keeping the formatting logic compatible
 * with Kotlin Multiplatform (`commonMain`).
 *
 * @property groupingSeparator Character used to separate thousands groups (e.g., `','` in `en-US`, `'.'` in `de-DE`).
 * @property decimalSeparator Character used to separate integer and fractional parts (e.g., `'.'` in `en-US`, `','` in `de-DE`).
 * @property groupingSize Number of digits in each group to the left of the decimal point (typically `3`, or `<= 0` to disable grouping).
 * @property trimTrailingZeros Whether to omit trailing fractional zeros (and the decimal separator when the fractional part rounds to zero, e.g., formatting `1.0` as `"1"` instead of `"1.0"`).
 */
public data class NumberFormatSymbols @JvmOverloads constructor(
    public val groupingSeparator: Char = ',',
    public val decimalSeparator: Char = '.',
    public val groupingSize: Int = 3,
    public val trimTrailingZeros: Boolean = false,
) {
    /**
     * Formats [value] rounded to [decimals] fractional digits with thousands grouping according to
     * this [NumberFormatSymbols] instance using pure Kotlin integer arithmetic (`roundToLong`, `/`,
     * `%`) and `buildString`, avoiding any dependency on `java.util.Formatter` (`String.format`).
     *
     * @param value The numeric value to format.
     * @param decimals Number of digits after the decimal separator (must be `>= 0`; defaults to `0`).
     * @return The formatted numeric string without a unit suffix (e.g., `"1,235"`, `"1,234.50"`).
     * @throws IllegalArgumentException if [decimals] is negative.
     */
    @JvmOverloads
    public fun formatFixedDecimal(value: Double, decimals: Int = 0): String =
        formatFixedDecimal(value = value, decimals = decimals, symbols = this)

    /**
     * Formats a scalar distance [value] (in large units such as kilometers or miles) using logical
     * precision based on order of magnitude and this [NumberFormatSymbols] instance:
     * - Values `< 10` (when rounded to 1 decimal place) are formatted with `1` decimal digit
     *   (keeping the trailing `.0` unless [trimTrailingZeros] is `true`, e.g., `"0.3"`, `"1.0"`,
     *   `"2.5"`, `"9.9"`).
     * - Values `>= 10` are formatted as whole integers with `0` decimal digits and thousands
     *   grouping (e.g., `"10"`, `"1,250"`).
     *
     * @param value The numeric value in target units (e.g., kilometers or miles).
     * @return The formatted numeric string without a unit suffix.
     */
    public fun formatDistanceValue(value: Double): String = formatDistanceValue(value = value, symbols = this)

    public companion object {
        /**
         * Standard English number formatting symbols (`','` thousands separator, `'.'` decimal
         * separator, group size `3`, keeping trailing `.0`).
         */
        @JvmField
        public val DEFAULT: NumberFormatSymbols = NumberFormatSymbols()

        /**
         * Compact number formatting symbols for space-constrained map widgets (such as a `ScaleBar`):
         * no thousands grouping (`groupingSize = 0`) and trailing `.0` trimmed (`"1 km"`, `"2 mi"`,
         * `"1977 km"`).
         */
        @JvmField
        public val COMPACT: NumberFormatSymbols = NumberFormatSymbols(
            groupingSize = 0,
            trimTrailingZeros = true,
        )

        /**
         * Resolves the [NumberFormatSymbols] for the given [locale] (defaulting to [Locale.getDefault]).
         *
         * @param locale The [Locale] whose grouping and decimal rules should be used.
         * @return The corresponding [NumberFormatSymbols].
         */
        @JvmStatic
        @JvmOverloads
        public fun fromLocale(locale: Locale = Locale.getDefault()): NumberFormatSymbols {
            val dfs = DecimalFormatSymbols.getInstance(locale)
            val decimalFormat = NumberFormat.getNumberInstance(locale) as? DecimalFormat
            val groupingSize = if (decimalFormat != null && decimalFormat.isGroupingUsed) {
                decimalFormat.groupingSize
            } else {
                0
            }
            return NumberFormatSymbols(
                groupingSeparator = dfs.groupingSeparator,
                decimalSeparator = dfs.decimalSeparator,
                groupingSize = groupingSize,
            )
        }
    }
}

// =================================================================================================
// § 4. Logical Distance & Elevation Formatting
// =================================================================================================
//
// When displaying distances in larger units (kilometers or miles), fixed-precision formatting
// either loses detail at short range (e.g., truncating 1.5 km to "1 km") or introduces noisy
// fractional digits at long range (e.g., "142.3 km").
//
// To maintain at least two significant digits across orders of magnitude:
// 1. Small units (`m` below 1 km, `ft` below 0.25 mi, and all elevations) are formatted as whole
//    integers (`0` decimal places, e.g., `"450 m"`, `"528 ft"`, `"14,440 ft"`).
// 2. Large units (`km` or `mi`) under 10 units are formatted to `1` decimal place, keeping the
//    trailing `.0` (e.g., `"0.3 mi"`, `"1.0 km"`, `"2.5 mi"`).
// 3. Large units (`km` or `mi`) of 10 units or greater are rounded to whole integers (`0` decimal
//    places, e.g., `"10 km"`, `"12 mi"`).

/**
 * Functional (SAM) interface for formatting a measurement in [meters] into a localized string
 * using the provided [symbols].
 *
 * Used by [UnitsConverter.create] to allow Java and Kotlin callers to supply custom distance or
 * elevation formatters (or method references such as `MetricUnitsConverter.INSTANCE::toElevationString`).
 */
public fun interface MeasurementFormatter {
    /**
     * Formats a measurement of [meters] (in raw meters) using [symbols].
     *
     * @param meters The measurement value in meters.
     * @param symbols The [NumberFormatSymbols] used for grouping and decimal separators.
     * @return The formatted measurement string with units.
     */
    public fun format(meters: Double, symbols: NumberFormatSymbols): String
}

/**
 * Strategy for converting and formatting [Meters] measurements into human-readable distance and
 * elevation strings.
 *
 * Obtain a standard instance via [getUnitsConverter], [UnitSystem.converter], [MetricUnitsConverter],
 * or [ImperialUnitsConverter], or construct a composite/custom converter via [UnitsConverter] (Kotlin)
 * or [UnitsConverter.create] (Java/Kotlin).
 */
public abstract class UnitsConverter {
    /**
     * Formats a distance in [meters] into a human-readable string with units using pure Kotlin
     * number formatting and the given [symbols].
     *
     * @param meters The distance to format.
     * @param symbols The [NumberFormatSymbols] used for grouping and decimal separators.
     * @return The formatted distance string (e.g., `"450 m"`, `"2.0 km"`, `"10 km"`, `"528 ft"`, `"2.5 mi"`).
     */
    public abstract fun toDistanceString(meters: Meters, symbols: NumberFormatSymbols): String

    /**
     * Formats a distance in [meters] (as a raw [Double]) using the given [symbols].
     *
     * @param meters The distance in meters to format.
     * @param symbols The [NumberFormatSymbols] used for grouping and decimal separators.
     * @return The formatted distance string.
     */
    public fun toDistanceString(meters: Double, symbols: NumberFormatSymbols): String =
        toDistanceString(Meters(meters), symbols)

    /**
     * Formats a distance in [meters] into a localized string with units.
     *
     * @param meters The distance to format.
     * @param locale The [Locale] used for number formatting rules (defaults to [Locale.getDefault]).
     * @return The formatted distance string (e.g., `"450 m"`, `"2.0 km"`, `"10 km"`, `"528 ft"`, `"2.5 mi"`).
     */
    public fun toDistanceString(meters: Meters, locale: Locale = Locale.getDefault()): String =
        toDistanceString(meters, NumberFormatSymbols.fromLocale(locale))

    /**
     * Formats a distance in [meters] (as a raw [Double]) into a localized string with units.
     *
     * @param meters The distance in meters to format.
     * @param locale The [Locale] used for number formatting rules (defaults to [Locale.getDefault]).
     * @return The formatted distance string.
     */
    @JvmOverloads
    public fun toDistanceString(meters: Double, locale: Locale = Locale.getDefault()): String =
        toDistanceString(Meters(meters), NumberFormatSymbols.fromLocale(locale))

    /**
     * Formats a distance in [meters] into a localized string using the translated unit templates
     * (`R.string.amu_in_*`) and primary locale of the given Android [resources].
     *
     * @param meters The distance to format.
     * @param resources The Android [Resources] used to resolve localized unit templates and number formatting.
     * @return The formatted distance string (e.g., `"450 m"`, `"2.0 km"`, `"528 フィート"`, `"2,5 ми"`).
     */
    public open fun toDistanceString(meters: Meters, resources: Resources): String =
        toDistanceString(meters, NumberFormatSymbols.fromLocale(primaryLocale(resources)))

    /**
     * Formats a distance in [meters] (as a raw [Double]) into a localized string using the
     * translated unit templates (`R.string.amu_in_*`) and primary locale of [resources].
     *
     * @param meters The distance in meters to format.
     * @param resources The Android [Resources] used to resolve localized unit templates and number formatting.
     * @return The formatted distance string.
     */
    public fun toDistanceString(meters: Double, resources: Resources): String =
        toDistanceString(Meters(meters), resources)

    /**
     * Formats an elevation in [meters] into a human-readable string with units using pure Kotlin
     * number formatting and the given [symbols].
     *
     * @param meters The elevation to format.
     * @param symbols The [NumberFormatSymbols] used for grouping and decimal separators.
     * @return The formatted elevation string (e.g., `"4,401 m"` or `"14,440 ft"`).
     */
    public abstract fun toElevationString(meters: Meters, symbols: NumberFormatSymbols): String

    /**
     * Formats an elevation in [meters] (as a raw [Double]) using the given [symbols].
     *
     * @param meters The elevation in meters to format.
     * @param symbols The [NumberFormatSymbols] used for grouping and decimal separators.
     * @return The formatted elevation string.
     */
    public fun toElevationString(meters: Double, symbols: NumberFormatSymbols): String =
        toElevationString(Meters(meters), symbols)

    /**
     * Formats an elevation in [meters] into a localized string with units.
     *
     * @param meters The elevation to format.
     * @param locale The [Locale] used for number formatting rules (defaults to [Locale.getDefault]).
     * @return The formatted elevation string (e.g., `"4,401 m"` or `"14,440 ft"`).
     */
    public fun toElevationString(meters: Meters, locale: Locale = Locale.getDefault()): String =
        toElevationString(meters, NumberFormatSymbols.fromLocale(locale))

    /**
     * Formats an elevation in [meters] (as a raw [Double]) into a localized string with units.
     *
     * @param meters The elevation in meters to format.
     * @param locale The [Locale] used for number formatting rules (defaults to [Locale.getDefault]).
     * @return The formatted elevation string.
     */
    @JvmOverloads
    public fun toElevationString(meters: Double, locale: Locale = Locale.getDefault()): String =
        toElevationString(Meters(meters), NumberFormatSymbols.fromLocale(locale))

    /**
     * Formats an elevation in [meters] into a localized string using the translated unit templates
     * (`R.string.amu_in_*`) and primary locale of the given Android [resources].
     *
     * @param meters The elevation to format.
     * @param resources The Android [Resources] used to resolve localized unit templates and number formatting.
     * @return The formatted elevation string (e.g., `"4,401 m"`, `"14,440 ft"`, `"14,440 フィート"`).
     */
    public open fun toElevationString(meters: Meters, resources: Resources): String =
        toElevationString(meters, NumberFormatSymbols.fromLocale(primaryLocale(resources)))

    /**
     * Formats an elevation in [meters] (as a raw [Double]) into a localized string using the
     * translated unit templates (`R.string.amu_in_*`) and primary locale of [resources].
     *
     * @param meters The elevation in meters to format.
     * @param resources The Android [Resources] used to resolve localized unit templates and number formatting.
     * @return The formatted elevation string.
     */
    public fun toElevationString(meters: Double, resources: Resources): String =
        toElevationString(Meters(meters), resources)

    /**
     * Returns a [UnitsConverter] that formats distances using [distanceUnits] while preserving
     * this converter's elevation formatting.
     *
     * @param distanceUnits The preferred [UnitSystem] for distances ([UnitSystem.METRIC] or [UnitSystem.IMPERIAL]).
     * @return A [UnitsConverter] with the updated distance unit system.
     */
    public fun withDistanceUnits(distanceUnits: UnitSystem): UnitsConverter =
        withDistanceConverter(distanceUnits.converter)

    /**
     * Returns a [UnitsConverter] that formats elevations using [elevationUnits] while preserving
     * this converter's distance formatting.
     *
     * @param elevationUnits The preferred [UnitSystem] for elevations ([UnitSystem.METRIC] or [UnitSystem.IMPERIAL]).
     * @return A [UnitsConverter] with the updated elevation unit system.
     */
    public fun withElevationUnits(elevationUnits: UnitSystem): UnitsConverter =
        withElevationConverter(elevationUnits.converter)

    /**
     * Returns a [UnitsConverter] that delegates distance formatting to [distanceConverter] while
     * preserving this converter's elevation formatting.
     *
     * @param distanceConverter The converter to use for [toDistanceString].
     * @return A composite [UnitsConverter].
     */
    public fun withDistanceConverter(distanceConverter: UnitsConverter): UnitsConverter =
        create(distanceConverter = distanceConverter, elevationConverter = this)

    /**
     * Returns a [UnitsConverter] that delegates elevation formatting to [elevationConverter] while
     * preserving this converter's distance formatting.
     *
     * @param elevationConverter The converter to use for [toElevationString].
     * @return A composite [UnitsConverter].
     */
    public fun withElevationConverter(elevationConverter: UnitsConverter): UnitsConverter =
        create(distanceConverter = this, elevationConverter = elevationConverter)

    /**
     * Returns a [UnitsConverter] that uses [distanceFormatter] for distances while preserving
     * this converter's elevation formatting.
     *
     * @param distanceFormatter Custom formatter for distances.
     * @return A composite [UnitsConverter].
     */
    public fun withDistanceFormatter(distanceFormatter: MeasurementFormatter): UnitsConverter =
        create(distanceFormatter = distanceFormatter, elevationConverter = this)

    /**
     * Returns a [UnitsConverter] that uses [elevationFormatter] for elevations while preserving
     * this converter's distance formatting.
     *
     * @param elevationFormatter Custom formatter for elevations.
     * @return A composite [UnitsConverter].
     */
    public fun withElevationFormatter(elevationFormatter: MeasurementFormatter): UnitsConverter =
        create(distanceConverter = this, elevationFormatter = elevationFormatter)

    public companion object {
        private val IMPERIAL_DISTANCE_METRIC_ELEVATION_CONVERTER: UnitsConverter by lazy(
            LazyThreadSafetyMode.PUBLICATION,
        ) {
            CompositeUnitsConverter(
                distanceFormatter = ImperialUnitsConverter::toDistanceString,
                elevationFormatter = MetricUnitsConverter::toElevationString,
                distanceResourcesFormatter = ImperialUnitsConverter::toDistanceString,
                elevationResourcesFormatter = MetricUnitsConverter::toElevationString,
                distanceDelegate = ImperialUnitsConverter,
                elevationDelegate = MetricUnitsConverter,
            )
        }

        private val METRIC_DISTANCE_IMPERIAL_ELEVATION_CONVERTER: UnitsConverter by lazy(
            LazyThreadSafetyMode.PUBLICATION,
        ) {
            CompositeUnitsConverter(
                distanceFormatter = MetricUnitsConverter::toDistanceString,
                elevationFormatter = ImperialUnitsConverter::toElevationString,
                distanceResourcesFormatter = MetricUnitsConverter::toDistanceString,
                elevationResourcesFormatter = ImperialUnitsConverter::toElevationString,
                distanceDelegate = MetricUnitsConverter,
                elevationDelegate = ImperialUnitsConverter,
            )
        }

        /**
         * Creates a [UnitsConverter] using the specified [distanceUnits] and [elevationUnits].
         *
         * @param distanceUnits The [UnitSystem] to use for distances ([UnitSystem.METRIC] or [UnitSystem.IMPERIAL]).
         * @param elevationUnits The [UnitSystem] to use for elevations ([UnitSystem.METRIC] or [UnitSystem.IMPERIAL]).
         * @return The corresponding [UnitsConverter].
         */
        @JvmStatic
        public fun create(distanceUnits: UnitSystem, elevationUnits: UnitSystem): UnitsConverter =
            create(distanceUnits.converter, elevationUnits.converter)

        /**
         * Creates a composite [UnitsConverter] that delegates distance formatting to
         * [distanceConverter] and elevation formatting to [elevationConverter].
         *
         * For example, to format road distances in Imperial units (`ft` / `mi`) and elevations in
         * Metric units (`m`) for the UK:
         * ```java
         * UnitsConverter ukConverter = UnitsConverter.create(
         *     ImperialUnitsConverter.INSTANCE,
         *     MetricUnitsConverter.INSTANCE
         * );
         * ```
         */
        @JvmStatic
        public fun create(distanceConverter: UnitsConverter, elevationConverter: UnitsConverter): UnitsConverter {
            val resolvedDistance =
                (distanceConverter as? CompositeUnitsConverter)?.distanceDelegate ?: distanceConverter
            val resolvedElevation =
                (elevationConverter as? CompositeUnitsConverter)?.elevationDelegate ?: elevationConverter
            return when {
                resolvedDistance === MetricUnitsConverter && resolvedElevation === MetricUnitsConverter ->
                    MetricUnitsConverter

                resolvedDistance === ImperialUnitsConverter && resolvedElevation === ImperialUnitsConverter ->
                    ImperialUnitsConverter

                resolvedDistance === ImperialUnitsConverter && resolvedElevation === MetricUnitsConverter ->
                    IMPERIAL_DISTANCE_METRIC_ELEVATION_CONVERTER

                resolvedDistance === MetricUnitsConverter && resolvedElevation === ImperialUnitsConverter ->
                    METRIC_DISTANCE_IMPERIAL_ELEVATION_CONVERTER

                else ->
                    CompositeUnitsConverter(
                        distanceFormatter = resolvedDistance::toDistanceString,
                        elevationFormatter = resolvedElevation::toElevationString,
                        distanceResourcesFormatter = resolvedDistance::toDistanceString,
                        elevationResourcesFormatter = resolvedElevation::toElevationString,
                        distanceDelegate = resolvedDistance,
                        elevationDelegate = resolvedElevation,
                    )
            }
        }

        /**
         * Creates a custom [UnitsConverter] using the supplied [distanceFormatter] and
         * [elevationFormatter].
         */
        @JvmStatic
        public fun create(
            distanceFormatter: MeasurementFormatter,
            elevationFormatter: MeasurementFormatter,
        ): UnitsConverter = CompositeUnitsConverter(
            distanceFormatter = { meters, symbols -> distanceFormatter.format(meters.value, symbols) },
            elevationFormatter = { meters, symbols -> elevationFormatter.format(meters.value, symbols) },
        )

        /**
         * Creates a composite [UnitsConverter] that delegates distance formatting to
         * [distanceConverter] and uses [elevationFormatter] for elevations.
         */
        @JvmStatic
        public fun create(
            distanceConverter: UnitsConverter,
            elevationFormatter: MeasurementFormatter,
        ): UnitsConverter {
            val resolvedDistance =
                (distanceConverter as? CompositeUnitsConverter)?.distanceDelegate ?: distanceConverter
            return CompositeUnitsConverter(
                distanceFormatter = resolvedDistance::toDistanceString,
                elevationFormatter = { meters, symbols -> elevationFormatter.format(meters.value, symbols) },
                distanceResourcesFormatter = resolvedDistance::toDistanceString,
                distanceDelegate = resolvedDistance,
            )
        }

        /**
         * Creates a composite [UnitsConverter] that uses [distanceFormatter] for distances and
         * delegates elevation formatting to [elevationConverter].
         */
        @JvmStatic
        public fun create(
            distanceFormatter: MeasurementFormatter,
            elevationConverter: UnitsConverter,
        ): UnitsConverter {
            val resolvedElevation =
                (elevationConverter as? CompositeUnitsConverter)?.elevationDelegate ?: elevationConverter
            return CompositeUnitsConverter(
                distanceFormatter = { meters, symbols -> distanceFormatter.format(meters.value, symbols) },
                elevationFormatter = resolvedElevation::toElevationString,
                elevationResourcesFormatter = resolvedElevation::toElevationString,
                elevationDelegate = resolvedElevation,
            )
        }
    }
}

private class CompositeUnitsConverter(
    private val distanceFormatter: (Meters, NumberFormatSymbols) -> String,
    private val elevationFormatter: (Meters, NumberFormatSymbols) -> String,
    private val distanceResourcesFormatter: ((Meters, Resources) -> String)? = null,
    private val elevationResourcesFormatter: ((Meters, Resources) -> String)? = null,
    internal val distanceDelegate: UnitsConverter? = null,
    internal val elevationDelegate: UnitsConverter? = null,
) : UnitsConverter() {
    override fun toDistanceString(meters: Meters, symbols: NumberFormatSymbols): String =
        distanceFormatter(meters, symbols)

    override fun toDistanceString(meters: Meters, resources: Resources): String =
        distanceResourcesFormatter?.invoke(meters, resources) ?: super.toDistanceString(meters, resources)

    override fun toElevationString(meters: Meters, symbols: NumberFormatSymbols): String =
        elevationFormatter(meters, symbols)

    override fun toElevationString(meters: Meters, resources: Resources): String =
        elevationResourcesFormatter?.invoke(meters, resources) ?: super.toElevationString(meters, resources)
}

// [START maps_android_utils_units_converters]

/**
 * Renders distance and elevation measurements in Imperial units (`ft` and `mi`).
 *
 * - **Distances** rounding to below `0.25` miles (`1,320` feet) are displayed in whole feet (`ft`).
 * - **Distances** from `0.25` miles up to `< 10` miles are displayed in miles (`mi`) with `1`
 *   decimal digit (keeping trailing `.0`, e.g., `"0.3 mi"`, `"1.0 mi"`, `"2.5 mi"`).
 * - **Distances** of `10` miles or greater are displayed in whole miles (`mi`, e.g., `"10 mi"`).
 * - **Elevations** are always displayed in whole feet (`ft`).
 */
public object ImperialUnitsConverter : UnitsConverter() {
    /** Threshold (`0.25` miles = `1,320` feet) at which imperial distances switch from feet to miles. */
    @get:JvmName("getMilesThreshold")
    public val MILES_THRESHOLD: Meters = 0.25.miles

    /** The [UnitSystem] implemented by this converter ([UnitSystem.IMPERIAL]). */
    public val unitSystem: UnitSystem = UnitSystem.IMPERIAL

    /**
     * Returns an Imperial [UnitsConverter] that switches from feet (`ft`) to miles (`mi`) at
     * [milesThreshold] (for example, `ImperialUnitsConverter(milesThreshold = 1.miles)` for a
     * `ScaleBar`).
     *
     * @param milesThreshold Threshold at which imperial distances switch from feet to miles.
     * @return An Imperial [UnitsConverter] configured with [milesThreshold].
     */
    @JvmSynthetic
    public operator fun invoke(milesThreshold: Meters = MILES_THRESHOLD): UnitsConverter =
        withMilesThreshold(milesThreshold)

    /**
     * Returns an Imperial [UnitsConverter] that switches from feet (`ft`) to miles (`mi`) at
     * [milesThreshold] (for example, `ImperialUnitsConverter.withMilesThreshold(miles(1))` in Java).
     *
     * @param milesThreshold Threshold at which imperial distances switch from feet to miles.
     * @return An Imperial [UnitsConverter] configured with [milesThreshold].
     */
    @JvmStatic
    @JvmName("withMilesThreshold")
    public fun withMilesThreshold(milesThreshold: Meters): UnitsConverter = if (milesThreshold == MILES_THRESHOLD) {
        this
    } else {
        ThresholdImperialUnitsConverter(milesThreshold)
    }

    override fun toDistanceString(meters: Meters, symbols: NumberFormatSymbols): String = formatDistance(
        meters = meters,
        thresholdMeters = MILES_THRESHOLD,
        symbols = symbols,
        formatFeet = { "$it ft" },
        formatMiles = { "$it mi" },
    )

    override fun toDistanceString(meters: Meters, resources: Resources): String = formatDistance(
        meters = meters,
        thresholdMeters = MILES_THRESHOLD,
        symbols = NumberFormatSymbols.fromLocale(primaryLocale(resources)),
        formatFeet = { resources.getString(R.string.amu_in_feet, it) },
        formatMiles = { resources.getString(R.string.amu_in_miles, it) },
    )

    override fun toElevationString(meters: Meters, symbols: NumberFormatSymbols): String =
        "${symbols.formatFixedDecimal(meters.inFeet, decimals = 0)} ft"

    override fun toElevationString(meters: Meters, resources: Resources): String {
        val symbols = NumberFormatSymbols.fromLocale(primaryLocale(resources))
        return resources.getString(R.string.amu_in_feet, symbols.formatFixedDecimal(meters.inFeet, decimals = 0))
    }

    internal inline fun formatDistance(
        meters: Meters,
        thresholdMeters: Meters,
        symbols: NumberFormatSymbols,
        formatFeet: (String) -> String,
        formatMiles: (String) -> String,
    ): String {
        val feet = meters.inFeet
        val thresholdFeet = thresholdMeters.inFeet.roundToLong()
        return if (abs(feet).roundToLong() < thresholdFeet) {
            formatFeet(symbols.formatFixedDecimal(feet, decimals = 0))
        } else {
            val rawMiles = meters.inMiles
            val minMiles = thresholdMeters.inMiles
            val miles =
                if (abs(rawMiles) < minMiles) {
                    if (rawMiles < 0.0) -minMiles else minMiles
                } else {
                    rawMiles
                }
            formatMiles(symbols.formatDistanceValue(miles))
        }
    }
}

private class ThresholdImperialUnitsConverter(private val milesThreshold: Meters) : UnitsConverter() {
    override fun toDistanceString(meters: Meters, symbols: NumberFormatSymbols): String =
        ImperialUnitsConverter.formatDistance(
            meters = meters,
            thresholdMeters = milesThreshold,
            symbols = symbols,
            formatFeet = { "$it ft" },
            formatMiles = { "$it mi" },
        )

    override fun toDistanceString(meters: Meters, resources: Resources): String = ImperialUnitsConverter.formatDistance(
        meters = meters,
        thresholdMeters = milesThreshold,
        symbols = NumberFormatSymbols.fromLocale(primaryLocale(resources)),
        formatFeet = { resources.getString(R.string.amu_in_feet, it) },
        formatMiles = { resources.getString(R.string.amu_in_miles, it) },
    )

    override fun toElevationString(meters: Meters, symbols: NumberFormatSymbols): String =
        ImperialUnitsConverter.toElevationString(meters, symbols)

    override fun toElevationString(meters: Meters, resources: Resources): String =
        ImperialUnitsConverter.toElevationString(meters, resources)
}

/**
 * Renders distance and elevation measurements in Metric units (`m` and `km`).
 *
 * - **Distances** rounding to below `1,000` meters are displayed in whole meters (`m`).
 * - **Distances** from `1,000` meters (`1.0` km) up to `< 10` kilometers are displayed in
 *   kilometers (`km`) with `1` decimal digit (keeping trailing `.0`, e.g., `"1.0 km"`, `"2.5 km"`).
 * - **Distances** of `10` kilometers or greater are displayed in whole kilometers (`km`, e.g., `"10 km"`).
 * - **Elevations** are always displayed in whole meters (`m`).
 */
public object MetricUnitsConverter : UnitsConverter() {
    /** Threshold (`1,000` meters = `1` kilometer) at which metric distances switch from meters to kilometers. */
    @get:JvmName("getKilometersThreshold")
    public val KILOMETERS_THRESHOLD: Meters = METERS_PER_KILOMETER.meters

    /** The [UnitSystem] implemented by this converter ([UnitSystem.METRIC]). */
    public val unitSystem: UnitSystem = UnitSystem.METRIC

    /**
     * Returns a Metric [UnitsConverter] that switches from meters (`m`) to kilometers (`km`) at
     * [kilometersThreshold].
     *
     * @param kilometersThreshold Threshold at which metric distances switch from meters to kilometers.
     * @return A Metric [UnitsConverter] configured with [kilometersThreshold].
     */
    @JvmSynthetic
    public operator fun invoke(kilometersThreshold: Meters = KILOMETERS_THRESHOLD): UnitsConverter =
        withKilometersThreshold(kilometersThreshold)

    /**
     * Returns a Metric [UnitsConverter] that switches from meters (`m`) to kilometers (`km`) at
     * [kilometersThreshold].
     *
     * @param kilometersThreshold Threshold at which metric distances switch from meters to kilometers.
     * @return A Metric [UnitsConverter] configured with [kilometersThreshold].
     */
    @JvmStatic
    @JvmName("withKilometersThreshold")
    public fun withKilometersThreshold(kilometersThreshold: Meters): UnitsConverter =
        if (kilometersThreshold == KILOMETERS_THRESHOLD) {
            this
        } else {
            ThresholdMetricUnitsConverter(kilometersThreshold)
        }

    override fun toDistanceString(meters: Meters, symbols: NumberFormatSymbols): String = formatDistance(
        meters = meters,
        thresholdMeters = KILOMETERS_THRESHOLD,
        symbols = symbols,
        formatMeters = { "$it m" },
        formatKilometers = { "$it km" },
    )

    override fun toDistanceString(meters: Meters, resources: Resources): String = formatDistance(
        meters = meters,
        thresholdMeters = KILOMETERS_THRESHOLD,
        symbols = NumberFormatSymbols.fromLocale(primaryLocale(resources)),
        formatMeters = { resources.getString(R.string.amu_in_meters, it) },
        formatKilometers = { resources.getString(R.string.amu_in_kilometers, it) },
    )

    override fun toElevationString(meters: Meters, symbols: NumberFormatSymbols): String =
        "${symbols.formatFixedDecimal(meters.inMeters, decimals = 0)} m"

    override fun toElevationString(meters: Meters, resources: Resources): String {
        val symbols = NumberFormatSymbols.fromLocale(primaryLocale(resources))
        return resources.getString(R.string.amu_in_meters, symbols.formatFixedDecimal(meters.inMeters, decimals = 0))
    }

    internal inline fun formatDistance(
        meters: Meters,
        thresholdMeters: Meters,
        symbols: NumberFormatSymbols,
        formatMeters: (String) -> String,
        formatKilometers: (String) -> String,
    ): String {
        val metersValue = meters.inMeters
        val thresholdWholeMeters = thresholdMeters.inMeters.roundToLong()
        return if (abs(metersValue).roundToLong() < thresholdWholeMeters) {
            formatMeters(symbols.formatFixedDecimal(metersValue, decimals = 0))
        } else {
            val rawKilometers = meters.inKilometers
            val minKilometers = thresholdMeters.inKilometers
            val kilometers =
                if (abs(rawKilometers) < minKilometers) {
                    if (rawKilometers < 0.0) -minKilometers else minKilometers
                } else {
                    rawKilometers
                }
            formatKilometers(symbols.formatDistanceValue(kilometers))
        }
    }
}

private class ThresholdMetricUnitsConverter(private val kilometersThreshold: Meters) : UnitsConverter() {
    override fun toDistanceString(meters: Meters, symbols: NumberFormatSymbols): String =
        MetricUnitsConverter.formatDistance(
            meters = meters,
            thresholdMeters = kilometersThreshold,
            symbols = symbols,
            formatMeters = { "$it m" },
            formatKilometers = { "$it km" },
        )

    override fun toDistanceString(meters: Meters, resources: Resources): String = MetricUnitsConverter.formatDistance(
        meters = meters,
        thresholdMeters = kilometersThreshold,
        symbols = NumberFormatSymbols.fromLocale(primaryLocale(resources)),
        formatMeters = { resources.getString(R.string.amu_in_meters, it) },
        formatKilometers = { resources.getString(R.string.amu_in_kilometers, it) },
    )

    override fun toElevationString(meters: Meters, symbols: NumberFormatSymbols): String =
        MetricUnitsConverter.toElevationString(meters, symbols)

    override fun toElevationString(meters: Meters, resources: Resources): String =
        MetricUnitsConverter.toElevationString(meters, resources)
}
// [END maps_android_utils_units_converters]

// =================================================================================================
// § 5. Top-Level Factory & Extension Functions
// =================================================================================================

/**
 * Constructs a [UnitsConverter] using the specified [distanceUnits] and [elevationUnits].
 *
 * @param distanceUnits The [UnitSystem] to use for distances ([UnitSystem.METRIC] or [UnitSystem.IMPERIAL]).
 * @param elevationUnits The [UnitSystem] to use for elevations ([UnitSystem.METRIC] or [UnitSystem.IMPERIAL]).
 * @return The corresponding [UnitsConverter].
 */
public fun UnitsConverter(distanceUnits: UnitSystem, elevationUnits: UnitSystem): UnitsConverter =
    UnitsConverter.create(distanceUnits, elevationUnits)

/**
 * Constructs a composite [UnitsConverter] that delegates distance formatting to
 * [distanceConverter] and elevation formatting to [elevationConverter].
 *
 * Example (Imperial road distances with Metric elevations for the UK):
 * ```kotlin
 * val ukConverter = UnitsConverter(
 *     distanceConverter = ImperialUnitsConverter,
 *     elevationConverter = MetricUnitsConverter,
 * )
 * ```
 *
 * @param distanceConverter Converter used for [UnitsConverter.toDistanceString].
 * @param elevationConverter Converter used for [UnitsConverter.toElevationString].
 * @return A composite [UnitsConverter].
 */
public fun UnitsConverter(distanceConverter: UnitsConverter, elevationConverter: UnitsConverter): UnitsConverter =
    UnitsConverter.create(distanceConverter, elevationConverter)

/**
 * Constructs a custom [UnitsConverter] using the provided [distanceFormatter] and
 * [elevationFormatter] lambdas (or method references such as `MetricUnitsConverter::toElevationString`).
 *
 * Example:
 * ```kotlin
 * val customConverter = UnitsConverter(
 *     distanceFormatter = { meters, symbols ->
 *         "${symbols.formatFixedDecimal(meters.inKilometers, decimals = 2)} km"
 *     },
 *     elevationFormatter = MetricUnitsConverter::toElevationString,
 * )
 * ```
 *
 * @param distanceFormatter Lambda formatting a [Meters] distance with [NumberFormatSymbols].
 * @param elevationFormatter Lambda formatting a [Meters] elevation with [NumberFormatSymbols].
 * @return A custom [UnitsConverter].
 */
@JvmSynthetic
public fun UnitsConverter(
    distanceFormatter: (Meters, NumberFormatSymbols) -> String,
    elevationFormatter: (Meters, NumberFormatSymbols) -> String,
): UnitsConverter = CompositeUnitsConverter(
    distanceFormatter = distanceFormatter,
    elevationFormatter = elevationFormatter,
)

/**
 * Constructs a composite [UnitsConverter] that delegates distance formatting to
 * [distanceConverter] and uses [elevationFormatter] for elevations.
 *
 * @param distanceConverter Converter used for [UnitsConverter.toDistanceString].
 * @param elevationFormatter Lambda formatting a [Meters] elevation with [NumberFormatSymbols].
 * @return A composite [UnitsConverter].
 */
@JvmSynthetic
public fun UnitsConverter(
    distanceConverter: UnitsConverter,
    elevationFormatter: (Meters, NumberFormatSymbols) -> String,
): UnitsConverter {
    val resolvedDistance = (distanceConverter as? CompositeUnitsConverter)?.distanceDelegate ?: distanceConverter
    return CompositeUnitsConverter(
        distanceFormatter = resolvedDistance::toDistanceString,
        elevationFormatter = elevationFormatter,
        distanceResourcesFormatter = resolvedDistance::toDistanceString,
        distanceDelegate = resolvedDistance,
    )
}

/**
 * Constructs a composite [UnitsConverter] that uses [distanceFormatter] for distances and
 * delegates elevation formatting to [elevationConverter].
 *
 * @param distanceFormatter Lambda formatting a [Meters] distance with [NumberFormatSymbols].
 * @param elevationConverter Converter used for [UnitsConverter.toElevationString].
 * @return A composite [UnitsConverter].
 */
@JvmSynthetic
public fun UnitsConverter(
    distanceFormatter: (Meters, NumberFormatSymbols) -> String,
    elevationConverter: UnitsConverter,
): UnitsConverter {
    val resolvedElevation = (elevationConverter as? CompositeUnitsConverter)?.elevationDelegate ?: elevationConverter
    return CompositeUnitsConverter(
        distanceFormatter = distanceFormatter,
        elevationFormatter = resolvedElevation::toElevationString,
        elevationResourcesFormatter = resolvedElevation::toElevationString,
        elevationDelegate = resolvedElevation,
    )
}

/**
 * Returns the appropriate [UnitsConverter] for the given [locale] (defaulting to [Locale.getDefault]),
 * with optional overrides for [distanceUnits] or [elevationUnits].
 *
 * For `"GB"` ([Locale.UK]), distances default to [UnitSystem.IMPERIAL] (`ft` / `mi`) and elevations
 * default to [UnitSystem.METRIC] (`m`).
 *
 * @param locale The [Locale] used to determine the default distance and elevation unit systems.
 * @param distanceUnits Preferred [UnitSystem] for distances (defaults to [UnitSystem.distanceFromLocale]).
 * @param elevationUnits Preferred [UnitSystem] for elevations (defaults to [UnitSystem.elevationFromLocale]).
 * @return The resolved [UnitsConverter].
 */
@JvmOverloads
public fun getUnitsConverter(
    locale: Locale = Locale.getDefault(),
    distanceUnits: UnitSystem = UnitSystem.distanceFromLocale(locale),
    elevationUnits: UnitSystem = UnitSystem.elevationFromLocale(locale),
): UnitsConverter = UnitsConverter.create(distanceUnits, elevationUnits)

/**
 * Returns the appropriate [UnitsConverter] for the primary locale of the given Android [resources],
 * with optional overrides for [distanceUnits] or [elevationUnits].
 *
 * For `"GB"` ([Locale.UK]), distances default to [UnitSystem.IMPERIAL] (`ft` / `mi`) and elevations
 * default to [UnitSystem.METRIC] (`m`).
 *
 * @param resources The Android [Resources] whose configuration locale determines the default unit systems.
 * @param distanceUnits Preferred [UnitSystem] for distances (defaults to [UnitSystem.distanceFromResources]).
 * @param elevationUnits Preferred [UnitSystem] for elevations (defaults to [UnitSystem.elevationFromResources]).
 * @return The resolved [UnitsConverter].
 */
@JvmOverloads
public fun getUnitsConverter(
    resources: Resources,
    distanceUnits: UnitSystem = UnitSystem.distanceFromResources(resources),
    elevationUnits: UnitSystem = UnitSystem.elevationFromResources(resources),
): UnitsConverter = UnitsConverter.create(distanceUnits, elevationUnits)

/**
 * Returns the appropriate [UnitsConverter] for the given ISO 3166-1 alpha-2 [countryCode],
 * with optional overrides for [distanceUnits] or [elevationUnits].
 *
 * For `"GB"`, distances default to [UnitSystem.IMPERIAL] (`ft` / `mi`) and elevations default to
 * [UnitSystem.METRIC] (`m`).
 *
 * @param countryCode The two-letter country code (case-insensitive), or `null`.
 * @param distanceUnits Preferred [UnitSystem] for distances (defaults to [UnitSystem.distanceFromCountryCode]).
 * @param elevationUnits Preferred [UnitSystem] for elevations (defaults to [UnitSystem.elevationFromCountryCode]).
 * @return The resolved [UnitsConverter].
 */
@JvmOverloads
public fun getUnitsConverter(
    countryCode: String?,
    distanceUnits: UnitSystem = UnitSystem.distanceFromCountryCode(countryCode),
    elevationUnits: UnitSystem = UnitSystem.elevationFromCountryCode(countryCode),
): UnitsConverter = UnitsConverter.create(distanceUnits, elevationUnits)

/**
 * Returns the [UnitsConverter] corresponding to the specified [unitSystem] for both distances
 * and elevations.
 *
 * @param unitSystem The desired [UnitSystem] for both distances and elevations.
 * @return [MetricUnitsConverter] or [ImperialUnitsConverter].
 */
public fun getUnitsConverter(unitSystem: UnitSystem): UnitsConverter = unitSystem.converter

/**
 * Formats a scalar distance [value] (in large units such as kilometers or miles) using logical
 * precision based on order of magnitude and pure Kotlin arithmetic:
 * - Values `< 10` (when rounded to 1 decimal place) are formatted with `1` decimal digit, keeping
 *   the trailing `.0` (e.g., `"0.3"`, `"1.0"`, `"2.5"`, `"9.9"`).
 * - Values `>= 10` are formatted as whole integers with `0` decimal digits and thousands grouping
 *   according to [symbols] (e.g., `"10"`, `"1,250"`).
 *
 * @param value The numeric value in target units (e.g., kilometers or miles).
 * @param symbols The [NumberFormatSymbols] used for grouping and decimal separators.
 * @return The formatted numeric string without a unit suffix.
 */
public fun formatDistanceValue(value: Double, symbols: NumberFormatSymbols): String =
    if (isUnderTenAtOneDecimal(value)) {
        formatFixedDecimal(value, decimals = 1, symbols = symbols)
    } else {
        formatFixedDecimal(value, decimals = 0, symbols = symbols)
    }

/**
 * Formats a scalar distance [value] (in large units such as kilometers or miles) using logical
 * precision based on order of magnitude and the formatting rules of [locale]:
 * - Values `< 10` (when rounded to 1 decimal place) are formatted with `1` decimal digit, keeping
 *   the trailing `.0` (e.g., `"0.3"`, `"1.0"`, `"2.5"`, `"9.9"`).
 * - Values `>= 10` are formatted as whole integers with `0` decimal digits (e.g., `"10"`, `"42"`).
 *
 * @param value The numeric value in target units (e.g., kilometers or miles).
 * @param locale The [Locale] whose grouping and decimal rules are applied (defaults to [Locale.getDefault]).
 * @return The formatted numeric string without a unit suffix.
 */
@JvmOverloads
public fun formatDistanceValue(value: Double, locale: Locale = Locale.getDefault()): String =
    formatDistanceValue(value, NumberFormatSymbols.fromLocale(locale))

/**
 * Formats this [Meters] measurement as a human-readable distance string using [locale] and [converter].
 *
 * @param locale The [Locale] used for unit selection and number formatting (defaults to [Locale.getDefault]).
 * @param converter The [UnitsConverter] to use (defaults to [getUnitsConverter] for [locale]).
 * @return The formatted distance string.
 */
@JvmName("toDistanceString")
@JvmOverloads
public fun Meters.toDistanceString(
    locale: Locale = Locale.getDefault(),
    converter: UnitsConverter = getUnitsConverter(locale),
): String = converter.toDistanceString(this, locale)

/**
 * Formats this [Meters] measurement as a human-readable distance string using the specified
 * [converter] and [symbols].
 *
 * @param converter The [UnitsConverter] to use.
 * @param symbols The [NumberFormatSymbols] used for grouping and decimal separators.
 * @return The formatted distance string.
 */
@JvmName("toDistanceString")
@JvmOverloads
public fun Meters.toDistanceString(
    converter: UnitsConverter,
    symbols: NumberFormatSymbols = NumberFormatSymbols.fromLocale(),
): String = converter.toDistanceString(this, symbols)

/**
 * Formats this [Meters] measurement as a localized distance string using the translated unit
 * templates (`R.string.amu_in_*`) and primary locale of [resources].
 *
 * @param resources The Android [Resources] used for unit selection, number formatting, and localized unit labels.
 * @param converter The [UnitsConverter] to use (defaults to [getUnitsConverter] for [resources]).
 * @return The formatted distance string.
 */
@JvmName("toDistanceString")
@JvmOverloads
public fun Meters.toDistanceString(
    resources: Resources,
    converter: UnitsConverter = getUnitsConverter(resources),
): String = converter.toDistanceString(this, resources)

/**
 * Formats this [Meters] measurement as a human-readable elevation string using [locale] and [converter].
 *
 * @param locale The [Locale] used for unit selection and number formatting (defaults to [Locale.getDefault]).
 * @param converter The [UnitsConverter] to use (defaults to [getUnitsConverter] for [locale]).
 * @return The formatted elevation string.
 */
@JvmName("toElevationString")
@JvmOverloads
public fun Meters.toElevationString(
    locale: Locale = Locale.getDefault(),
    converter: UnitsConverter = getUnitsConverter(locale),
): String = converter.toElevationString(this, locale)

/**
 * Formats this [Meters] measurement as a human-readable elevation string using the specified
 * [converter] and [symbols].
 *
 * @param converter The [UnitsConverter] to use.
 * @param symbols The [NumberFormatSymbols] used for grouping and decimal separators.
 * @return The formatted elevation string.
 */
@JvmName("toElevationString")
@JvmOverloads
public fun Meters.toElevationString(
    converter: UnitsConverter,
    symbols: NumberFormatSymbols = NumberFormatSymbols.fromLocale(),
): String = converter.toElevationString(this, symbols)

/**
 * Formats this [Meters] measurement as a localized elevation string using the translated unit
 * templates (`R.string.amu_in_*`) and primary locale of [resources].
 *
 * @param resources The Android [Resources] used for unit selection, number formatting, and localized unit labels.
 * @param converter The [UnitsConverter] to use (defaults to [getUnitsConverter] for [resources]).
 * @return The formatted elevation string.
 */
@JvmName("toElevationString")
@JvmOverloads
public fun Meters.toElevationString(
    resources: Resources,
    converter: UnitsConverter = getUnitsConverter(resources),
): String = converter.toElevationString(this, resources)

// =================================================================================================
// § 6. Internal Pure-Kotlin Formatting Helpers
// =================================================================================================

private fun primaryLocale(resources: Resources): Locale =
    ConfigurationCompat.getLocales(resources.configuration)[0] ?: Locale.getDefault()

// [START maps_android_utils_units_pure_kotlin_formatter]

/**
 * Returns `true` if `abs(value)` rounds to less than `10.0` at one decimal place (`< 9.95`).
 * Values in `[9.95, 10.0)` round to `10` at one decimal place and therefore use the whole-number
 * format (`"10"`) rather than `"10.0"`.
 */
private fun isUnderTenAtOneDecimal(value: Double): Boolean = (abs(value) * 10.0).roundToLong() < 100L

/**
 * Formats [value] with [decimals] fractional digits and locale-specific thousands grouping using pure
 * `kotlin.math` integer arithmetic (`roundToLong`, `/`, `%`) and `buildString`, avoiding any
 * dependency on `java.util.Formatter` (`String.format`).
 */
internal fun formatFixedDecimal(value: Double, decimals: Int, symbols: NumberFormatSymbols): String {
    require(decimals >= 0) { "decimals must be non-negative, was $decimals" }
    var factor = 1L
    repeat(decimals) { factor *= 10L }
    val scaled = (abs(value) * factor).roundToLong()
    val isNegative = value < 0.0 && scaled > 0L
    val integerPart = scaled / factor
    val groupedInteger = formatGroupedInteger(
        value = integerPart,
        groupingSeparator = symbols.groupingSeparator,
        groupingSize = symbols.groupingSize,
    )
    return buildString {
        if (isNegative) append('-')
        append(groupedInteger)
        if (decimals > 0) {
            val fractionPart = scaled % factor
            if (!symbols.trimTrailingZeros || fractionPart != 0L) {
                append(symbols.decimalSeparator)
                val fractionDigits = fractionPart.toString().padStart(decimals, '0')
                append(if (symbols.trimTrailingZeros) fractionDigits.trimEnd('0') else fractionDigits)
            }
        }
    }
}

/**
 * Formats a non-negative integer [value] with [groupingSeparator] inserted every [groupingSize]
 * digits from the right.
 */
internal fun formatGroupedInteger(value: Long, groupingSeparator: Char, groupingSize: Int): String {
    val digits = value.toString()
    if (groupingSize <= 0 || digits.length <= groupingSize) {
        return digits
    }
    return buildString(digits.length + (digits.length - 1) / groupingSize) {
        for (i in digits.indices) {
            if (i > 0 && (digits.length - i) % groupingSize == 0) {
                append(groupingSeparator)
            }
            append(digits[i])
        }
    }
}
// [END maps_android_utils_units_pure_kotlin_formatter]
