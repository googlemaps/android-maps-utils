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

package com.google.maps.android.clustering.view

import java.text.DecimalFormat
import java.text.NumberFormat
import java.util.Locale
import kotlin.math.floor
import kotlin.math.log10
import kotlin.math.max
import kotlin.math.pow

/**
 * Shared formatter for cluster badge text across cluster renderers.
 *
 * Centralizes exact count, bucketed, and significant-digit badge formatting.
 * For compact numbers with '+' suffixes, values are strictly truncated/floored using
 * integer tenths so badges never overstate cluster counts.
 */
internal object ClusterBadgeFormatter {

    /**
     * Formats an exact count, using the default locale for thousand separators or
     * compact SI formatting ('k', 'm', 'K', 'M') if [useCompact] is true.
     */
    fun formatExact(
        count: Int,
        useCompact: Boolean,
        compactUnitUppercase: Boolean,
        locale: Locale = Locale.getDefault(),
    ): String {
        if (useCompact) {
            return formatCompactNumber(count, compactUnitUppercase, locale)
        }
        return NumberFormat.getIntegerInstance(locale).format(count)
    }

    /**
     * Rounds a cluster size down to the specified number of significant digits.
     */
    fun roundToSignificantDigits(size: Int, digits: Int): Int {
        if (size < 10) return size
        val d = digits.coerceAtLeast(1)
        val m = floor(log10(size.toDouble())).toInt()
        val p = max(0, m - d + 1)
        val divisor = 10.0.pow(p.toDouble()).toInt()
        return (size / divisor) * divisor
    }

    /**
     * Formats a cluster count using significant-digit precision.
     */
    fun formatSignificantDigits(
        roundedCount: Int,
        useCompact: Boolean,
        compactUnitUppercase: Boolean,
        locale: Locale = Locale.getDefault(),
    ): String {
        if (roundedCount < 10) {
            return roundedCount.toString()
        }
        if (useCompact) {
            val kSuffix = if (compactUnitUppercase) "K" else "k"
            val mSuffix = if (compactUnitUppercase) "M" else "m"
            if (roundedCount >= 1_000_000) {
                return "${formatTenths(roundedCount / 100_000, locale)}$mSuffix+"
            } else if (roundedCount >= 1_000) {
                return "${formatTenths(roundedCount / 100, locale)}$kSuffix+"
            }
        }
        return "$roundedCount+"
    }

    /**
     * Formats a bucket count with compact formatting (e.g. 1000 -> "1k+").
     * If [bucket] is smaller than [firstBucket], returns the exact count without '+'.
     */
    fun formatCompactBucket(
        bucket: Int,
        compactUnitUppercase: Boolean,
        firstBucket: Int = 10,
        locale: Locale = Locale.getDefault(),
    ): String {
        if (bucket < firstBucket) {
            return formatCompactNumber(bucket, compactUnitUppercase, locale)
        }
        val kSuffix = if (compactUnitUppercase) "K" else "k"
        val mSuffix = if (compactUnitUppercase) "M" else "m"
        return when {
            bucket >= 1_000_000 -> "${formatTenths(bucket / 100_000, locale)}$mSuffix+"
            bucket >= 1_000 -> "${formatTenths(bucket / 100, locale)}$kSuffix+"
            else -> "$bucket+"
        }
    }

    /**
     * Formats an exact number into compact form (without '+').
     */
    fun formatCompactNumber(number: Int, compactUnitUppercase: Boolean, locale: Locale = Locale.getDefault()): String {
        val kSuffix = if (compactUnitUppercase) "K" else "k"
        val mSuffix = if (compactUnitUppercase) "M" else "m"
        return when {
            number >= 1_000_000 -> "${formatTenths(number / 100_000, locale)}$mSuffix"
            number >= 1_000 -> "${formatTenths(number / 100, locale)}$kSuffix"
            else -> number.toString()
        }
    }

    /**
     * Formats an integer count of tenths (e.g. 14 -> "1.4", 10 -> "1") using the given [locale].
     */
    private fun formatTenths(tenths: Int, locale: Locale): String {
        if (tenths % 10 == 0) {
            return (tenths / 10).toString()
        }
        val value = tenths / 10.0
        val format = NumberFormat.getNumberInstance(locale) as? DecimalFormat
            ?: DecimalFormat("#.#")
        format.maximumFractionDigits = 1
        format.minimumFractionDigits = 1
        format.isGroupingUsed = false
        return format.format(value)
    }
}
