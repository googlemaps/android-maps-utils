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
import androidx.annotation.StringRes
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
// this exact definition so that round-trip conversions (e.g., `1.miles.toMiles`, `5280.feet.toMiles`)
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
 * Construct instances using the [Number] extension properties ([meters], [m], [kilometers], [km],
 * [feet], [ft], [miles], [mi]) and convert back to raw scalar units via [toMeters], [toKilometers],
 * [toFeet], and [toMiles].
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

/** Shorthand to create a [Meters] measurement from a [Number] of meters. */
@get:JvmName("m")
public inline val Number.m: Meters
    get() = Meters(value = this.toDouble())

/** Creates a [Meters] measurement from a [Number] of kilometers. */
@get:JvmName("kilometers")
public inline val Number.kilometers: Meters
    get() = Meters(value = this.toDouble() * METERS_PER_KILOMETER)

/** Shorthand to create a [Meters] measurement from a [Number] of kilometers. */
@get:JvmName("km")
public inline val Number.km: Meters
    get() = Meters(value = this.toDouble() * METERS_PER_KILOMETER)

/** Creates a [Meters] measurement from a [Number] of international feet. */
@get:JvmName("feet")
public inline val Number.feet: Meters
    get() = Meters(value = this.toDouble() * METERS_PER_FOOT)

/** Shorthand to create a [Meters] measurement from a [Number] of international feet. */
@get:JvmName("ft")
public inline val Number.ft: Meters
    get() = Meters(value = this.toDouble() * METERS_PER_FOOT)

/** Creates a [Meters] measurement from a [Number] of statute miles. */
@get:JvmName("miles")
public inline val Number.miles: Meters
    get() = Meters(value = this.toDouble() * METERS_PER_MILE)

/** Shorthand to create a [Meters] measurement from a [Number] of statute miles. */
@get:JvmName("mi")
public inline val Number.mi: Meters
    get() = Meters(value = this.toDouble() * METERS_PER_MILE)

/** Returns the value of this [Meters] measurement in meters as a [Double]. */
@get:JvmName("toMeters")
public inline val Meters.toMeters: Double
    get() = value

/** Returns the equivalent number of kilometers for this [Meters] measurement. */
@get:JvmName("toKilometers")
public inline val Meters.toKilometers: Double
    get() = value / METERS_PER_KILOMETER

/** Returns the equivalent number of international feet for this [Meters] measurement. */
@get:JvmName("toFeet")
public inline val Meters.toFeet: Double
    get() = value * FEET_PER_METER

/** Returns the equivalent number of statute miles for this [Meters] measurement. */
@get:JvmName("toMiles")
public inline val Meters.toMiles: Double
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
         * ISO 3166-1 alpha-2 country codes that default to [IMPERIAL] units for road distances
         * and elevations: United States (`"US"`), Liberia (`"LR"`), Myanmar (`"MM"`), and
         * United Kingdom (`"GB"`).
         */
        public val IMPERIAL_COUNTRY_CODES: Set<String> = setOf("US", "LR", "MM", "GB")

        /**
         * Returns the preferred [UnitSystem] for the given [locale].
         *
         * Defaults to [Locale.getDefault], providing a framework-agnostic way to select the
         * device's appropriate unit system without requiring an Android `Context`.
         *
         * @param locale The [Locale] whose country code determines the unit system.
         * @return [IMPERIAL] if the locale's country is in [IMPERIAL_COUNTRY_CODES], otherwise [METRIC].
         */
        @JvmStatic
        @JvmOverloads
        public fun fromLocale(locale: Locale = Locale.getDefault()): UnitSystem = fromCountryCode(locale.country)

        /**
         * Returns the preferred [UnitSystem] for the given ISO 3166-1 alpha-2 [countryCode].
         *
         * Comparison is case-insensitive. If [countryCode] is `null`, blank, or not in
         * [IMPERIAL_COUNTRY_CODES], [METRIC] is returned.
         *
         * @param countryCode The two-letter country code (e.g., `"US"`, `"GB"`, `"FR"`).
         * @return The corresponding [UnitSystem].
         */
        @JvmStatic
        public fun fromCountryCode(countryCode: String?): UnitSystem {
            if (countryCode.isNullOrBlank()) return METRIC
            return if (countryCode.uppercase() in IMPERIAL_COUNTRY_CODES) {
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
 */
public data class NumberFormatSymbols(
    public val groupingSeparator: Char = ',',
    public val decimalSeparator: Char = '.',
    public val groupingSize: Int = 3,
) {
    public companion object {
        /**
         * Standard English number formatting symbols (`','` thousands separator, `'.'` decimal
         * separator, group size `3`).
         */
        @JvmField
        public val DEFAULT: NumberFormatSymbols = NumberFormatSymbols()

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
                3
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
 * Pairs a converted numeric measurement [value] with the Android string resource ID
 * ([unitsTemplate]) used to format it.
 *
 * @property value The converted numerical value in the target unit (e.g., feet, miles, meters, or kilometers).
 * @property unitsTemplate The `@StringRes` resource ID for formatting [value] with its unit label.
 */
public data class ValueWithUnitsTemplate(
    public val value: Double,
    @param:StringRes @field:StringRes @get:StringRes public val unitsTemplate: Int,
)

/**
 * Strategy for converting and formatting [Meters] measurements into human-readable distance and
 * elevation strings.
 *
 * Obtain an instance via [getUnitsConverter], [UnitSystem.converter], or directly reference
 * [MetricUnitsConverter] or [ImperialUnitsConverter].
 */
public abstract class UnitsConverter {
    /** The [UnitSystem] implemented by this converter. */
    public abstract val unitSystem: UnitSystem

    /**
     * Converts a distance in [meters] to a [ValueWithUnitsTemplate] containing the scaled numeric
     * value and the appropriate string resource template ID.
     */
    public abstract fun toDistanceUnits(meters: Meters): ValueWithUnitsTemplate

    /**
     * Converts a distance in [meters] (as a raw [Double]) to a [ValueWithUnitsTemplate].
     */
    public fun toDistanceUnits(meters: Double): ValueWithUnitsTemplate = toDistanceUnits(Meters(meters))

    /**
     * Converts an elevation in [meters] to a [ValueWithUnitsTemplate] containing the scaled numeric
     * value and the appropriate string resource template ID.
     */
    public abstract fun toElevationUnits(meters: Meters): ValueWithUnitsTemplate

    /**
     * Converts an elevation in [meters] (as a raw [Double]) to a [ValueWithUnitsTemplate].
     */
    public fun toElevationUnits(meters: Double): ValueWithUnitsTemplate = toElevationUnits(Meters(meters))

    /**
     * Formats a distance in [meters] into a human-readable string with units using pure Kotlin
     * number formatting and the given [symbols].
     *
     * @param meters The distance to format.
     * @param symbols The [NumberFormatSymbols] used for grouping and decimal separators.
     * @return The formatted distance string (e.g., `"450 m"`, `"2.0 km"`, `"10 km"`, `"528 ft"`, `"2.5 mi"`).
     */
    public fun toDistanceString(meters: Meters, symbols: NumberFormatSymbols): String {
        val (value, resourceId) = toDistanceUnits(meters)
        return formatWithTemplate(resourceId, value, symbols)
    }

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
     * Formats a distance in [meters] into a localized string with units without requiring Android
     * [Resources].
     *
     * @param meters The distance to format.
     * @param locale The [Locale] used for number formatting rules (defaults to [Locale.getDefault]).
     * @return The formatted distance string (e.g., `"450 m"`, `"2.0 km"`, `"10 km"`, `"528 ft"`, `"2.5 mi"`).
     */
    public fun toDistanceString(meters: Meters, locale: Locale = Locale.getDefault()): String =
        toDistanceString(meters, NumberFormatSymbols.fromLocale(locale))

    /**
     * Formats a distance in [meters] (as a raw [Double]) into a localized string with units without
     * requiring Android [Resources].
     *
     * @param meters The distance in meters to format.
     * @param locale The [Locale] used for number formatting rules (defaults to [Locale.getDefault]).
     * @return The formatted distance string.
     */
    @JvmOverloads
    public fun toDistanceString(meters: Double, locale: Locale = Locale.getDefault()): String =
        toDistanceString(Meters(meters), NumberFormatSymbols.fromLocale(locale))

    /**
     * Formats a distance in [meters] using the provided Android [resources].
     *
     * @param resources Android [Resources] used to resolve the unit string template.
     * @param meters The distance to format.
     * @return The formatted distance string.
     */
    public fun toDistanceString(resources: Resources, meters: Meters): String {
        val (value, resourceId) = toDistanceUnits(meters = meters)
        return resources.getString(resourceId, value)
    }

    /**
     * Formats a distance in [meters] (as a raw [Double]) using the provided Android [resources].
     *
     * @param resources Android [Resources] used to resolve the unit string template.
     * @param meters The distance in meters to format.
     * @return The formatted distance string.
     */
    public fun toDistanceString(resources: Resources, meters: Double): String =
        toDistanceString(resources, Meters(meters))

    /**
     * Formats an elevation in [meters] into a human-readable string with units using pure Kotlin
     * number formatting and the given [symbols].
     *
     * @param meters The elevation to format.
     * @param symbols The [NumberFormatSymbols] used for grouping and decimal separators.
     * @return The formatted elevation string (e.g., `"4,401 m"` or `"14,440 ft"`).
     */
    public fun toElevationString(meters: Meters, symbols: NumberFormatSymbols): String {
        val (value, resourceId) = toElevationUnits(meters)
        return formatWithTemplate(resourceId, value, symbols)
    }

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
     * Formats an elevation in [meters] into a localized string with units without requiring Android
     * [Resources].
     *
     * @param meters The elevation to format.
     * @param locale The [Locale] used for number formatting rules (defaults to [Locale.getDefault]).
     * @return The formatted elevation string (e.g., `"4,401 m"` or `"14,440 ft"`).
     */
    public fun toElevationString(meters: Meters, locale: Locale = Locale.getDefault()): String =
        toElevationString(meters, NumberFormatSymbols.fromLocale(locale))

    /**
     * Formats an elevation in [meters] (as a raw [Double]) into a localized string with units
     * without requiring Android [Resources].
     *
     * @param meters The elevation in meters to format.
     * @param locale The [Locale] used for number formatting rules (defaults to [Locale.getDefault]).
     * @return The formatted elevation string.
     */
    @JvmOverloads
    public fun toElevationString(meters: Double, locale: Locale = Locale.getDefault()): String =
        toElevationString(Meters(meters), NumberFormatSymbols.fromLocale(locale))

    /**
     * Formats an elevation in [meters] using the provided Android [resources].
     *
     * @param resources Android [Resources] used to resolve the unit string template.
     * @param meters The elevation to format.
     * @return The formatted elevation string.
     */
    public fun toElevationString(resources: Resources, meters: Meters): String {
        val (value, resourceId) = toElevationUnits(meters = meters)
        return resources.getString(resourceId, value)
    }

    /**
     * Formats an elevation in [meters] (as a raw [Double]) using the provided Android [resources].
     *
     * @param resources Android [Resources] used to resolve the unit string template.
     * @param meters The elevation in meters to format.
     * @return The formatted elevation string.
     */
    public fun toElevationString(resources: Resources, meters: Double): String =
        toElevationString(resources, Meters(meters))
}

// [START maps_android_utils_units_converters]

/**
 * Renders distance and elevation measurements in Imperial units (`ft` and `mi`).
 *
 * - **Distances** below `0.25` miles (`1,320` feet) are displayed in whole feet (`ft`).
 * - **Distances** from `0.25` miles up to `< 10` miles are displayed in miles (`mi`) with `1`
 *   decimal digit (keeping trailing `.0`, e.g., `"0.3 mi"`, `"1.0 mi"`, `"2.5 mi"`).
 * - **Distances** of `10` miles or greater are displayed in whole miles (`mi`, e.g., `"10 mi"`).
 * - **Elevations** are always displayed in whole feet (`ft`).
 */
public object ImperialUnitsConverter : UnitsConverter() {
    /** Threshold (`0.25` miles = `1,320` feet) at which imperial distances switch from feet to miles. */
    public val MILES_THRESHOLD: Meters = 0.25.miles

    override val unitSystem: UnitSystem = UnitSystem.IMPERIAL

    override fun toDistanceUnits(meters: Meters): ValueWithUnitsTemplate =
        if (abs(meters.value) < MILES_THRESHOLD.value) {
            ValueWithUnitsTemplate(normalizeZero(meters.toFeet, decimals = 0), R.string.amu_in_feet)
        } else {
            val miles = meters.toMiles
            if (isUnderTenAtOneDecimal(miles)) {
                ValueWithUnitsTemplate(normalizeZero(miles, decimals = 1), R.string.amu_in_miles)
            } else {
                ValueWithUnitsTemplate(normalizeZero(miles, decimals = 0), R.string.amu_in_miles_whole)
            }
        }

    override fun toElevationUnits(meters: Meters): ValueWithUnitsTemplate =
        ValueWithUnitsTemplate(normalizeZero(meters.toFeet, decimals = 0), R.string.amu_in_feet)
}

/**
 * Renders distance and elevation measurements in Metric units (`m` and `km`).
 *
 * - **Distances** below `1,000` meters are displayed in whole meters (`m`).
 * - **Distances** from `1,000` meters (`1.0` km) up to `< 10` kilometers are displayed in
 *   kilometers (`km`) with `1` decimal digit (keeping trailing `.0`, e.g., `"1.0 km"`, `"2.5 km"`).
 * - **Distances** of `10` kilometers or greater are displayed in whole kilometers (`km`, e.g., `"10 km"`).
 * - **Elevations** are always displayed in whole meters (`m`).
 */
public object MetricUnitsConverter : UnitsConverter() {
    /** Threshold (`1,000` meters = `1` kilometer) at which metric distances switch from meters to kilometers. */
    public val KILOMETERS_THRESHOLD: Meters = METERS_PER_KILOMETER.meters

    override val unitSystem: UnitSystem = UnitSystem.METRIC

    override fun toDistanceUnits(meters: Meters): ValueWithUnitsTemplate =
        if (abs(meters.value) < KILOMETERS_THRESHOLD.value) {
            ValueWithUnitsTemplate(normalizeZero(meters.toMeters, decimals = 0), R.string.amu_in_meters)
        } else {
            val kilometers = meters.toKilometers
            if (isUnderTenAtOneDecimal(kilometers)) {
                ValueWithUnitsTemplate(normalizeZero(kilometers, decimals = 1), R.string.amu_in_kilometers)
            } else {
                ValueWithUnitsTemplate(normalizeZero(kilometers, decimals = 0), R.string.amu_in_kilometers_whole)
            }
        }

    override fun toElevationUnits(meters: Meters): ValueWithUnitsTemplate =
        ValueWithUnitsTemplate(normalizeZero(meters.toMeters, decimals = 0), R.string.amu_in_meters)
}
// [END maps_android_utils_units_converters]

// =================================================================================================
// § 5. Top-Level Factory & Extension Functions
// =================================================================================================

/**
 * Returns the appropriate [UnitsConverter] for the given [locale] (defaulting to [Locale.getDefault]).
 *
 * @param locale The [Locale] used to determine the preferred unit system.
 * @return [ImperialUnitsConverter] for imperial locales (`US`, `LR`, `MM`, `GB`), or
 *   [MetricUnitsConverter] otherwise.
 */
@JvmOverloads
public fun getUnitsConverter(locale: Locale = Locale.getDefault()): UnitsConverter =
    UnitSystem.fromLocale(locale).converter

/**
 * Returns the appropriate [UnitsConverter] for the given ISO 3166-1 alpha-2 [countryCode].
 *
 * @param countryCode The two-letter country code (case-insensitive), or `null`.
 * @return [ImperialUnitsConverter] if [countryCode] is in [UnitSystem.IMPERIAL_COUNTRY_CODES],
 *   otherwise [MetricUnitsConverter].
 */
public fun getUnitsConverter(countryCode: String?): UnitsConverter = UnitSystem.fromCountryCode(countryCode).converter

/**
 * Returns the [UnitsConverter] corresponding to the specified [unitSystem].
 *
 * @param unitSystem The desired [UnitSystem].
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
 * Formats this [Meters] measurement as a human-readable distance string using Android [resources].
 *
 * @param resources Android [Resources] used to resolve the unit string template.
 * @param converter The [UnitsConverter] to use (defaults to [getUnitsConverter]).
 * @return The formatted distance string.
 */
@JvmName("toDistanceString")
@JvmOverloads
public fun Meters.toDistanceString(resources: Resources, converter: UnitsConverter = getUnitsConverter()): String =
    converter.toDistanceString(resources, this)

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
 * Formats this [Meters] measurement as a human-readable elevation string using Android [resources].
 *
 * @param resources Android [Resources] used to resolve the unit string template.
 * @param converter The [UnitsConverter] to use (defaults to [getUnitsConverter]).
 * @return The formatted elevation string.
 */
@JvmName("toElevationString")
@JvmOverloads
public fun Meters.toElevationString(resources: Resources, converter: UnitsConverter = getUnitsConverter()): String =
    converter.toElevationString(resources, this)

// =================================================================================================
// § 6. Internal Pure-Kotlin Formatting Helpers
// =================================================================================================

// [START maps_android_utils_units_pure_kotlin_formatter]

/**
 * Returns `true` if `abs(value)` rounds to less than `10.0` at one decimal place (`< 9.95`).
 * Values in `[9.95, 10.0)` round to `10` at one decimal place and therefore use the whole-number
 * format (`"10"`) rather than `"10.0"`.
 */
private fun isUnderTenAtOneDecimal(value: Double): Boolean = (abs(value) * 10.0).roundToLong() < 100L

/**
 * Prevents negative values that round to zero at the given [decimals] precision from formatting
 * as `"-0"` or `"-0.0"`.
 */
private fun normalizeZero(value: Double, decimals: Int): Double {
    val factor = if (decimals == 0) 1.0 else 10.0
    if (abs(value) * factor < 0.5) return 0.0
    return value
}

/**
 * Formats [value] with `0` or `1` decimal places and locale-specific thousands grouping using pure
 * `kotlin.math` integer arithmetic (`roundToLong`, `/`, `%`) and `buildString`, avoiding any
 * dependency on `java.util.Formatter` (`String.format`).
 */
internal fun formatFixedDecimal(
    value: Double,
    decimals: Int,
    symbols: NumberFormatSymbols = NumberFormatSymbols.DEFAULT,
): String {
    val factor = if (decimals <= 0) 1L else 10L
    val scaled = (abs(value) * factor).roundToLong()
    val isNegative = value < 0.0 && scaled > 0L
    val integerPart = if (decimals <= 0) scaled else scaled / 10L
    val groupedInteger = formatGroupedInteger(
        value = integerPart,
        groupingSeparator = symbols.groupingSeparator,
        groupingSize = symbols.groupingSize,
    )
    return buildString {
        if (isNegative) append('-')
        append(groupedInteger)
        if (decimals > 0) {
            append(symbols.decimalSeparator)
            append(scaled % 10L)
        }
    }
}

/**
 * Formats a non-negative integer [value] with [groupingSeparator] inserted every [groupingSize]
 * digits from the right.
 */
internal fun formatGroupedInteger(value: Long, groupingSeparator: Char, groupingSize: Int = 3): String {
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

/**
 * Formats [value] with its unit suffix according to [resourceId] and [symbols] without requiring
 * Android [Resources] or `java.util.Formatter`.
 */
private fun formatWithTemplate(@StringRes resourceId: Int, value: Double, symbols: NumberFormatSymbols): String =
    when (resourceId) {
        R.string.amu_in_feet -> "${formatFixedDecimal(value, decimals = 0, symbols)} ft"
        R.string.amu_in_miles -> "${formatFixedDecimal(value, decimals = 1, symbols)} mi"
        R.string.amu_in_miles_whole -> "${formatFixedDecimal(value, decimals = 0, symbols)} mi"
        R.string.amu_in_meters -> "${formatFixedDecimal(value, decimals = 0, symbols)} m"
        R.string.amu_in_kilometers -> "${formatFixedDecimal(value, decimals = 1, symbols)} km"
        R.string.amu_in_kilometers_whole -> "${formatFixedDecimal(value, decimals = 0, symbols)} km"
        else -> formatFixedDecimal(value, decimals = 1, symbols)
    }
