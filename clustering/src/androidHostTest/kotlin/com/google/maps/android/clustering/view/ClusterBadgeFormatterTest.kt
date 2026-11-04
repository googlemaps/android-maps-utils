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

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import java.util.Locale

class ClusterBadgeFormatterTest {

    @Test
    fun formatExact_standardLocale() {
        val us = Locale.US
        val german = Locale.GERMANY

        assertThat(
            ClusterBadgeFormatter.formatExact(
                1234,
                useCompact = false,
                compactUnitUppercase = false,
                locale = us,
            ),
        ).isEqualTo("1,234")
        assertThat(
            ClusterBadgeFormatter.formatExact(
                1234,
                useCompact = false,
                compactUnitUppercase = false,
                locale = german,
            ),
        ).isEqualTo("1.234")
        assertThat(
            ClusterBadgeFormatter.formatExact(
                5,
                useCompact = false,
                compactUnitUppercase = false,
                locale = us,
            ),
        ).isEqualTo("5")
    }

    @Test
    fun formatExact_compact() {
        val us = Locale.US

        assertThat(
            ClusterBadgeFormatter.formatExact(
                1450,
                useCompact = true,
                compactUnitUppercase = false,
                locale = us,
            ),
        ).isEqualTo("1.4k")
        assertThat(
            ClusterBadgeFormatter.formatExact(
                1000,
                useCompact = true,
                compactUnitUppercase = false,
                locale = us,
            ),
        ).isEqualTo("1k")
        assertThat(
            ClusterBadgeFormatter.formatExact(
                1000,
                useCompact = true,
                compactUnitUppercase = true,
                locale = us,
            ),
        ).isEqualTo("1K")
        assertThat(
            ClusterBadgeFormatter.formatExact(
                2500000,
                useCompact = true,
                compactUnitUppercase = false,
                locale = us,
            ),
        ).isEqualTo("2.5m")
    }

    @Test
    fun roundToSignificantDigits() {
        // 1 digit
        assertThat(ClusterBadgeFormatter.roundToSignificantDigits(5, 1)).isEqualTo(5)
        assertThat(ClusterBadgeFormatter.roundToSignificantDigits(1450, 1)).isEqualTo(1000)
        assertThat(ClusterBadgeFormatter.roundToSignificantDigits(350, 1)).isEqualTo(300)
        assertThat(ClusterBadgeFormatter.roundToSignificantDigits(5000, 1)).isEqualTo(5000)

        // 3 digits
        assertThat(ClusterBadgeFormatter.roundToSignificantDigits(1450, 3)).isEqualTo(1450)
        assertThat(ClusterBadgeFormatter.roundToSignificantDigits(1456, 3)).isEqualTo(1450)
        assertThat(ClusterBadgeFormatter.roundToSignificantDigits(1999, 2)).isEqualTo(1900)
    }

    @Test
    fun formatSignificantDigits_neverOverstatesCount() {
        val us = Locale.US

        // 1,450 with 3 digits rounded is 1,450.
        // Floored compact is 1.4k+ (must NEVER round up to 1.5k+)
        val rounded1450 = ClusterBadgeFormatter.roundToSignificantDigits(1450, 3)
        assertThat(
            ClusterBadgeFormatter.formatSignificantDigits(
                rounded1450,
                useCompact = true,
                compactUnitUppercase = false,
                locale = us,
            ),
        ).isEqualTo("1.4k+")

        // 1,999 with 2 digits rounded is 1,900.
        // Floored compact is 1.9k+ (never 2k+)
        val rounded1999 = ClusterBadgeFormatter.roundToSignificantDigits(1999, 2)
        assertThat(
            ClusterBadgeFormatter.formatSignificantDigits(
                rounded1999,
                useCompact = true,
                compactUnitUppercase = false,
                locale = us,
            ),
        ).isEqualTo("1.9k+")

        // Exact thousand: 1000 -> 1k+
        val rounded1000 = ClusterBadgeFormatter.roundToSignificantDigits(1000, 1)
        assertThat(
            ClusterBadgeFormatter.formatSignificantDigits(
                rounded1000,
                useCompact = true,
                compactUnitUppercase = false,
                locale = us,
            ),
        ).isEqualTo("1k+")
        assertThat(
            ClusterBadgeFormatter.formatSignificantDigits(
                rounded1000,
                useCompact = true,
                compactUnitUppercase = true,
                locale = us,
            ),
        ).isEqualTo("1K+")

        // Non-compact
        assertThat(
            ClusterBadgeFormatter.formatSignificantDigits(
                rounded1450,
                useCompact = false,
                compactUnitUppercase = false,
                locale = us,
            ),
        ).isEqualTo("1450+")
    }

    @Test
    fun formatSignificantDigits_localizedDecimalSeparator() {
        val german = Locale.GERMANY
        val rounded1450 = ClusterBadgeFormatter.roundToSignificantDigits(1450, 3)
        assertThat(
            ClusterBadgeFormatter.formatSignificantDigits(
                rounded1450,
                useCompact = true,
                compactUnitUppercase = false,
                locale = german,
            ),
        ).isEqualTo("1,4k+")
    }

    @Test
    fun formatCompactBucket() {
        val us = Locale.US
        assertThat(
            ClusterBadgeFormatter.formatCompactBucket(
                5,
                compactUnitUppercase = false,
                firstBucket = 10,
                locale = us,
            ),
        ).isEqualTo("5")
        assertThat(
            ClusterBadgeFormatter.formatCompactBucket(
                50,
                compactUnitUppercase = false,
                firstBucket = 10,
                locale = us,
            ),
        ).isEqualTo("50+")
        assertThat(
            ClusterBadgeFormatter.formatCompactBucket(
                1000,
                compactUnitUppercase = false,
                firstBucket = 10,
                locale = us,
            ),
        ).isEqualTo("1k+")
        assertThat(
            ClusterBadgeFormatter.formatCompactBucket(
                10000,
                compactUnitUppercase = true,
                firstBucket = 10,
                locale = us,
            ),
        ).isEqualTo("10K+")
        assertThat(
            ClusterBadgeFormatter.formatCompactBucket(
                1000000,
                compactUnitUppercase = false,
                firstBucket = 10,
                locale = us,
            ),
        ).isEqualTo("1m+")
    }
}
