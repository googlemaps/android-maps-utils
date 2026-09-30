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
package com.google.maps.android

import com.google.android.gms.maps.model.LatLng
import com.google.common.truth.Truth.assertThat
import org.junit.Assert.assertThrows
import org.junit.Test

/**
 * Tests for [FlexiblePolylineUtil].
 *
 * The expected encoded strings are interoperability vectors: the two dimensional case is the
 * worked example published in the format specification, and the remaining cases were produced
 * by a separate implementation written from the same specification, so a regression in the
 * encoder cannot hide behind a matching regression in the decoder.
 */
class FlexiblePolylineUtilTest {
    private companion object {
        /** The worked example from the specification: Frankfurt, precision 5, no third value. */
        const val SPEC_EXAMPLE = "BFoz5xJ67i1B1B7PzIhaxL7Y"

        val SPEC_EXAMPLE_POINTS =
            listOf(
                FlexiblePoint(50.10228, 8.69821),
                FlexiblePoint(50.10201, 8.69567),
                FlexiblePoint(50.10063, 8.69150),
                FlexiblePoint(50.09878, 8.68752),
            )

        /** Tolerance well below the quantisation of the coarsest precision under test. */
        const val EPSILON = 1e-9
    }

    @Test
    fun `encode matches the specification worked example`() {
        val encoded = FlexiblePolylineUtil.encode(SPEC_EXAMPLE_POINTS, precision = 5)

        assertThat(encoded).isEqualTo(SPEC_EXAMPLE)
    }

    @Test
    fun `decode reads the specification worked example`() {
        val decoded = FlexiblePolylineUtil.decode(SPEC_EXAMPLE)

        assertThat(decoded.precision).isEqualTo(5)
        assertThat(decoded.thirdDimension).isEqualTo(ThirdDimension.ABSENT)
        assertThat(decoded.thirdDimensionPrecision).isEqualTo(0)
        assertThat(decoded.points).hasSize(4)
        decoded.points.forEachIndexed { index, actual ->
            val expected = SPEC_EXAMPLE_POINTS[index]
            assertThat(actual.latitude).isWithin(EPSILON).of(expected.latitude)
            assertThat(actual.longitude).isWithin(EPSILON).of(expected.longitude)
            assertThat(actual.thirdDimensionValue).isNull()
        }
    }

    @Test
    fun `encode writes the header declared by the caller`() {
        // 'B' is version 1; the second character carries precision, third dimension and its
        // precision, so an empty path encodes to exactly the two header characters.
        assertThat(FlexiblePolylineUtil.encode(emptyList(), precision = 5)).isEqualTo("BF")
        assertThat(FlexiblePolylineUtil.encode(emptyList(), precision = 7)).isEqualTo("BH")
    }

    @Test
    fun `encode and decode round trip an altitude polyline`() {
        val points =
            listOf(
                FlexiblePoint(50.10228, 8.69821, 10.0),
                FlexiblePoint(50.10201, 8.69567, 20.5),
                FlexiblePoint(50.10063, 8.69150, 30.25),
            )

        val encoded =
            FlexiblePolylineUtil.encode(
                points = points,
                precision = 7,
                thirdDimension = ThirdDimension.ALTITUDE,
                thirdDimensionPrecision = 2,
            )

        assertThat(encoded).isEqualTo("BnJglg07do9-8lFw-B3oFvzxB0hCv-anuxC-8B")

        val decoded = FlexiblePolylineUtil.decode(encoded)
        assertThat(decoded.precision).isEqualTo(7)
        assertThat(decoded.thirdDimension).isEqualTo(ThirdDimension.ALTITUDE)
        assertThat(decoded.thirdDimensionPrecision).isEqualTo(2)
        decoded.points.forEachIndexed { index, actual ->
            val expected = points[index]
            assertThat(actual.latitude).isWithin(EPSILON).of(expected.latitude)
            assertThat(actual.longitude).isWithin(EPSILON).of(expected.longitude)
            assertThat(actual.thirdDimensionValue!!)
                .isWithin(EPSILON)
                .of(expected.thirdDimensionValue!!)
        }
    }

    @Test
    fun `encode and decode round trip negative levels`() {
        val points =
            listOf(
                FlexiblePoint(50.10228, 8.69821, 0.0),
                FlexiblePoint(50.10201, 8.69567, 1.0),
                FlexiblePoint(50.10063, 8.69150, -1.0),
            )

        val encoded =
            FlexiblePolylineUtil.encode(
                points = points,
                precision = 5,
                thirdDimension = ThirdDimension.LEVEL,
            )

        assertThat(encoded).isEqualTo("BVoz5xJ67i1BA1B7PCzIhaD")
        assertThat(FlexiblePolylineUtil.decode(encoded).points.map { it.thirdDimensionValue })
            .containsExactly(0.0, 1.0, -1.0)
            .inOrder()
    }

    @Test
    fun `encode and decode handle southern and western hemispheres`() {
        val points =
            listOf(
                FlexiblePoint(-33.8523, -70.6693),
                FlexiblePoint(-33.8570, -70.6712),
            )

        val encoded = FlexiblePolylineUtil.encode(points, precision = 7)

        assertThat(encoded).isEqualTo("BHv351lUv4j9jqBv57CvjlB")

        val decoded = FlexiblePolylineUtil.decode(encoded)
        assertThat(decoded.points[0].latitude).isWithin(EPSILON).of(-33.8523)
        assertThat(decoded.points[0].longitude).isWithin(EPSILON).of(-70.6693)
        assertThat(decoded.points[1].latitude).isWithin(EPSILON).of(-33.8570)
        assertThat(decoded.points[1].longitude).isWithin(EPSILON).of(-70.6712)
    }

    @Test
    fun `encode and decode round trip a single point`() {
        val encoded =
            FlexiblePolylineUtil.encode(listOf(FlexiblePoint(50.10228, 8.69821)), precision = 7)

        assertThat(encoded).isEqualTo("BHglg07do9-8lF")
        assertThat(FlexiblePolylineUtil.decode(encoded).points).hasSize(1)
    }

    @Test
    fun `decode returns no points for a header only polyline`() {
        val decoded = FlexiblePolylineUtil.decode("BH")

        assertThat(decoded.points).isEmpty()
        assertThat(decoded.precision).isEqualTo(7)
    }

    @Test
    fun `encode quantises to the requested precision`() {
        val encoded =
            FlexiblePolylineUtil.encode(
                listOf(FlexiblePoint(50.4, 8.6), FlexiblePoint(51.6, 9.4)),
                precision = 0,
            )

        val decoded = FlexiblePolylineUtil.decode(encoded)
        assertThat(decoded.precision).isEqualTo(0)
        assertThat(decoded.points.map { it.latitude }).containsExactly(50.0, 52.0).inOrder()
        assertThat(decoded.points.map { it.longitude }).containsExactly(9.0, 9.0).inOrder()
    }

    @Test
    fun `encoded strings use only url safe characters`() {
        val encoded =
            FlexiblePolylineUtil.encode(
                listOf(
                    FlexiblePoint(-33.84960, 151.21163),
                    FlexiblePoint(-33.85725, 151.21002),
                ),
                precision = 7,
            )

        assertThat(encoded).matches("[A-Za-z0-9_-]+")
    }

    @Test
    fun `encodeLatLngs round trips through decodeToLatLngs`() {
        val path =
            listOf(
                LatLng(-33.84960, 151.21163),
                LatLng(-33.85300, 151.21092),
                LatLng(-33.85725, 151.21002),
            )

        val decoded = FlexiblePolylineUtil.decodeToLatLngs(FlexiblePolylineUtil.encodeLatLngs(path))

        assertThat(decoded).hasSize(path.size)
        decoded.forEachIndexed { index, actual ->
            assertThat(actual.latitude).isWithin(EPSILON).of(path[index].latitude)
            assertThat(actual.longitude).isWithin(EPSILON).of(path[index].longitude)
        }
    }

    @Test
    fun `toLatLngs discards the third dimension`() {
        val encoded =
            FlexiblePolylineUtil.encode(
                points = listOf(FlexiblePoint(50.10228, 8.69821, 42.0)),
                thirdDimension = ThirdDimension.ALTITUDE,
                thirdDimensionPrecision = 1,
            )

        val decoded = FlexiblePolylineUtil.decode(encoded)

        assertThat(decoded.points.single().thirdDimensionValue).isNotNull()
        assertThat(decoded.toLatLngs().single().latitude).isWithin(EPSILON).of(50.10228)
    }

    @Test
    fun `getThirdDimension reads the header without decoding points`() {
        assertThat(FlexiblePolylineUtil.getThirdDimension(SPEC_EXAMPLE))
            .isEqualTo(ThirdDimension.ABSENT)
        assertThat(FlexiblePolylineUtil.getThirdDimension("BnJglg07do9-8lFw-B3oFvzxB0hCv-anuxC-8B"))
            .isEqualTo(ThirdDimension.ALTITUDE)
    }

    @Test
    fun `decode rejects an empty string`() {
        assertThrows(IllegalArgumentException::class.java) {
            FlexiblePolylineUtil.decode("")
        }
    }

    @Test
    fun `decode rejects an unsupported version`() {
        // 'C' is version 2, which this implementation does not claim to understand.
        val exception =
            assertThrows(IllegalArgumentException::class.java) {
                FlexiblePolylineUtil.decode("CFoz5xJ67i1B")
            }

        assertThat(exception).hasMessageThat().contains("version")
    }

    @Test
    fun `decode rejects a character outside the alphabet`() {
        val exception =
            assertThrows(IllegalArgumentException::class.java) {
                FlexiblePolylineUtil.decode("BFoz5xJ67i1B*")
            }

        assertThat(exception).hasMessageThat().contains("Invalid character")
    }

    @Test
    fun `decode rejects a polyline truncated mid point`() {
        // A two dimensional polyline needs latitude and longitude for every point; drop the
        // trailing longitude and the string no longer describes whole points.
        val truncated = SPEC_EXAMPLE.dropLast(2)

        assertThrows(IllegalArgumentException::class.java) {
            FlexiblePolylineUtil.decode(truncated)
        }
    }

    @Test
    fun `encode rejects an out of range precision`() {
        assertThrows(IllegalArgumentException::class.java) {
            FlexiblePolylineUtil.encode(SPEC_EXAMPLE_POINTS, precision = 16)
        }
        assertThrows(IllegalArgumentException::class.java) {
            FlexiblePolylineUtil.encode(SPEC_EXAMPLE_POINTS, precision = -1)
        }
    }

    @Test
    fun `encode rejects a reserved third dimension`() {
        val exception =
            assertThrows(IllegalArgumentException::class.java) {
                FlexiblePolylineUtil.encode(
                    points = listOf(FlexiblePoint(50.10228, 8.69821, 1.0)),
                    thirdDimension = ThirdDimension.RESERVED1,
                )
            }

        assertThat(exception).hasMessageThat().contains("reserved")
    }

    @Test
    fun `encode rejects a third dimension precision without a third dimension`() {
        assertThrows(IllegalArgumentException::class.java) {
            FlexiblePolylineUtil.encode(
                points = SPEC_EXAMPLE_POINTS,
                thirdDimension = ThirdDimension.ABSENT,
                thirdDimensionPrecision = 2,
            )
        }
    }

    @Test
    fun `encode rejects a point missing its third value`() {
        val exception =
            assertThrows(IllegalArgumentException::class.java) {
                FlexiblePolylineUtil.encode(
                    points =
                        listOf(
                            FlexiblePoint(50.10228, 8.69821, 10.0),
                            FlexiblePoint(50.10201, 8.69567),
                        ),
                    thirdDimension = ThirdDimension.ALTITUDE,
                    thirdDimensionPrecision = 1,
                )
            }

        assertThat(exception).hasMessageThat().contains("thirdDimensionValue")
    }
}
