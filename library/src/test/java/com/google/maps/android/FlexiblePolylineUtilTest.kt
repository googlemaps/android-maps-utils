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
import java.time.Instant

/**
 * Tests for [FlexiblePolylineUtil], [FlexiblePolyline], and [FlexiblePoint].
 *
 * The expected encoded strings are interoperability vectors: the two dimensional case is the
 * worked example published in the format specification, and the remaining cases verify three
 * and four dimensional (elevation + time) encoding, quantization, header inspection, and
 * temporal interpolation.
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
        assertThat(decoded.hasTime).isFalse()
        assertThat(decoded.timePrecision).isEqualTo(0)
        assertThat(decoded.durationMillis).isNull()
        assertThat(decoded.points).hasSize(4)
        decoded.points.forEachIndexed { index, actual ->
            val expected = SPEC_EXAMPLE_POINTS[index]
            assertThat(actual.latitude).isWithin(EPSILON).of(expected.latitude)
            assertThat(actual.longitude).isWithin(EPSILON).of(expected.longitude)
            assertThat(actual.thirdDimensionValue).isNull()
            assertThat(actual.timestampMillis).isNull()
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
        assertThat(decoded.hasTime).isFalse()
        assertThat(decoded.timePrecision).isEqualTo(0)
        decoded.points.forEachIndexed { index, actual ->
            val expected = points[index]
            assertThat(actual.latitude).isWithin(EPSILON).of(expected.latitude)
            assertThat(actual.longitude).isWithin(EPSILON).of(expected.longitude)
            assertThat(actual.thirdDimensionValue!!)
                .isWithin(EPSILON)
                .of(expected.thirdDimensionValue!!)
            assertThat(actual.timestampMillis).isNull()
        }
    }

    @Test
    fun `encode and decode round trip a 4D track using default 11cm and 100ms precisions`() {
        val baseTimeMillis = 1_761_403_200_000L
        val points =
            listOf(
                FlexiblePoint(37.819929, -122.478255, 67.2, baseTimeMillis),
                FlexiblePoint(37.820412, -122.478491, 68.8, baseTimeMillis + 200L),
                FlexiblePoint(37.821054, -122.478711, 71.1, baseTimeMillis + 700L),
                FlexiblePoint(37.821890, -122.478998, 70.4, baseTimeMillis + 1_400L),
            )

        val encoded =
            FlexiblePolylineUtil.encode(
                points = points,
                thirdDimension = ThirdDimension.ELEVATION,
                thirdDimensionPrecision = 1,
            )

        assertThat(FlexiblePolylineUtil.getThirdDimension(encoded)).isEqualTo(ThirdDimension.ELEVATION)
        assertThat(FlexiblePolylineUtil.hasTime(encoded)).isTrue()

        val decoded = FlexiblePolylineUtil.decode(encoded)
        assertThat(decoded.precision).isEqualTo(FlexiblePolylineUtil.DEFAULT_PRECISION)
        assertThat(decoded.precision).isEqualTo(6)
        assertThat(decoded.thirdDimension).isEqualTo(ThirdDimension.ELEVATION)
        assertThat(decoded.thirdDimensionPrecision).isEqualTo(1)
        assertThat(decoded.hasTime).isTrue()
        assertThat(decoded.timePrecision).isEqualTo(FlexiblePolylineUtil.DEFAULT_TIME_PRECISION)
        assertThat(decoded.timePrecision).isEqualTo(1)
        assertThat(decoded.durationMillis).isEqualTo(1_400L)
        assertThat(decoded.points).hasSize(points.size)
        decoded.points.forEachIndexed { index, actual ->
            val expected = points[index]
            assertThat(actual.latitude).isWithin(1e-6).of(expected.latitude)
            assertThat(actual.longitude).isWithin(1e-6).of(expected.longitude)
            assertThat(actual.thirdDimensionValue!!).isWithin(1e-1).of(expected.thirdDimensionValue!!)
            assertThat(actual.timestampMillis).isEqualTo(expected.timestampMillis)
        }
    }

    @Test
    fun `encode and decode round trip a 4D track with user selected precisions`() {
        val baseTimeMillis = 1_761_403_200_000L
        val points =
            listOf(
                FlexiblePoint(37.8199286, -122.4782551, 67.25, baseTimeMillis),
                FlexiblePoint(37.8204123, -122.4784912, 68.80, baseTimeMillis + 250L),
                FlexiblePoint(37.8210540, -122.4787105, 71.15, baseTimeMillis + 750L),
                FlexiblePoint(37.8218902, -122.4789980, 70.40, baseTimeMillis + 1_375L),
            )

        val encoded =
            FlexiblePolylineUtil.encode(
                points = points,
                precision = 7,
                thirdDimension = ThirdDimension.ELEVATION,
                thirdDimensionPrecision = 2,
                hasTime = true,
                timePrecision = 3,
            )

        val decoded = FlexiblePolylineUtil.decode(encoded)
        assertThat(decoded.precision).isEqualTo(7)
        assertThat(decoded.thirdDimension).isEqualTo(ThirdDimension.ELEVATION)
        assertThat(decoded.thirdDimensionPrecision).isEqualTo(2)
        assertThat(decoded.hasTime).isTrue()
        assertThat(decoded.timePrecision).isEqualTo(3)
        assertThat(decoded.durationMillis).isEqualTo(1_375L)
        decoded.points.forEachIndexed { index, actual ->
            val expected = points[index]
            assertThat(actual.latitude).isWithin(1e-7).of(expected.latitude)
            assertThat(actual.longitude).isWithin(1e-7).of(expected.longitude)
            assertThat(actual.thirdDimensionValue!!).isWithin(1e-2).of(expected.thirdDimensionValue!!)
            assertThat(actual.timestampMillis).isEqualTo(expected.timestampMillis)
        }
    }

    @Test
    fun `encode and decode round trip a 2D plus time track with Instant overloads`() {
        val t0 = Instant.parse("2026-10-25T18:00:00.125Z")
        val t1 = Instant.parse("2026-10-25T18:00:01.625Z")
        val points =
            listOf(
                FlexiblePoint(latitude = 50.10228, longitude = 8.69821, timestamp = t0),
                FlexiblePoint(latitude = 50.10201, longitude = 8.69567, timestamp = t1),
            )

        val encoded = FlexiblePolylineUtil.encode(points, precision = 6, timePrecision = 3)

        assertThat(FlexiblePolylineUtil.getThirdDimension(encoded)).isEqualTo(ThirdDimension.ABSENT)
        assertThat(FlexiblePolylineUtil.hasTime(encoded)).isTrue()

        val decoded = FlexiblePolylineUtil.decode(encoded)
        assertThat(decoded.hasTime).isTrue()
        assertThat(decoded.timePrecision).isEqualTo(3)
        assertThat(decoded.points[0].timestamp).isEqualTo(t0)
        assertThat(decoded.points[1].timestamp).isEqualTo(t1)
    }

    @Test
    fun `encode quantises timestamps and rounds ties half away from zero`() {
        val points =
            listOf(
                FlexiblePoint(50.10228, 8.69821, timestampMillis = 1_500L),
                FlexiblePoint(50.10201, 8.69567, timestampMillis = -1_500L),
                FlexiblePoint(50.10063, 8.69150, timestampMillis = 1_499L),
            )

        val encodedSeconds =
            FlexiblePolylineUtil.encode(
                points = points,
                precision = 5,
                hasTime = true,
                timePrecision = 0,
            )
        val decodedSeconds = FlexiblePolylineUtil.decode(encodedSeconds)
        assertThat(decodedSeconds.timePrecision).isEqualTo(0)
        assertThat(decodedSeconds.points.map { it.timestampMillis })
            .containsExactly(2_000L, -2_000L, 1_000L)
            .inOrder()

        val decisecondPoints =
            listOf(
                FlexiblePoint(50.10228, 8.69821, timestampMillis = 1_050L),
                FlexiblePoint(50.10201, 8.69567, timestampMillis = 1_140L),
            )
        val decodedDeciseconds =
            FlexiblePolylineUtil.decode(
                FlexiblePolylineUtil.encode(
                    points = decisecondPoints,
                    precision = 5,
                    hasTime = true,
                    timePrecision = 1,
                ),
            )
        assertThat(decodedDeciseconds.timePrecision).isEqualTo(1)
        assertThat(decodedDeciseconds.points.map { it.timestampMillis })
            .containsExactly(1_100L, 1_100L)
            .inOrder()
    }

    @Test
    fun `encode and decode support sub-millisecond time precision`() {
        val points =
            listOf(
                FlexiblePoint(50.10228, 8.69821, timestampMillis = 1_700_000_000_123L),
                FlexiblePoint(50.10201, 8.69567, timestampMillis = 1_700_000_000_456L),
            )

        val encoded =
            FlexiblePolylineUtil.encode(
                points = points,
                precision = 5,
                hasTime = true,
                timePrecision = 6,
            )
        val decoded = FlexiblePolylineUtil.decode(encoded)

        assertThat(decoded.timePrecision).isEqualTo(6)
        assertThat(decoded.points.map { it.timestampMillis })
            .containsExactly(1_700_000_000_123L, 1_700_000_000_456L)
            .inOrder()
    }

    @Test
    fun `interpolateAtTime and getPrefixAtTime interpolate position and elevation along track`() {
        val t0 = 1_000L
        val t1 = 2_000L
        val t2 = 3_000L
        val polyline =
            FlexiblePolyline(
                points =
                listOf(
                    FlexiblePoint(0.0, 0.0, 100.0, t0),
                    FlexiblePoint(0.0, 1.0, 200.0, t1),
                    FlexiblePoint(0.0, 2.0, 400.0, t2),
                ),
                precision = 7,
                thirdDimension = ThirdDimension.ELEVATION,
                thirdDimensionPrecision = 2,
                hasTime = true,
                timePrecision = 3,
            )

        assertThat(polyline.interpolateAtTime(500L)).isEqualTo(polyline.points.first())
        assertThat(polyline.interpolateAtTime(3_500L)).isEqualTo(polyline.points.last())
        assertThat(polyline.interpolateAtTime(t1)).isEqualTo(polyline.points[1])

        val mid = polyline.interpolateAtTime(Instant.ofEpochMilli(1_500L))!!
        assertThat(mid.latitude).isWithin(1e-6).of(0.0)
        assertThat(mid.longitude).isWithin(1e-6).of(0.5)
        assertThat(mid.thirdDimensionValue!!).isWithin(1e-6).of(150.0)
        assertThat(mid.timestampMillis).isEqualTo(1_500L)

        assertThat(polyline.getPrefixAtTime(500L)).isEmpty()
        assertThat(polyline.getPrefixAtTime(t0)).containsExactly(polyline.points[0])

        val prefixAtMid = polyline.getPrefixAtTime(Instant.ofEpochMilli(1_500L))
        assertThat(prefixAtMid).hasSize(2)
        assertThat(prefixAtMid[0]).isEqualTo(polyline.points[0])
        assertThat(prefixAtMid[1].longitude).isWithin(1e-6).of(0.5)
        assertThat(prefixAtMid[1].thirdDimensionValue!!).isWithin(1e-6).of(150.0)

        assertThat(polyline.getPrefixAtTime(t2)).isEqualTo(polyline.points)
    }

    @Test
    fun `interpolateAtTime and getPrefixAtTime return null or empty when polyline has no time`() {
        val untimed = FlexiblePolylineUtil.decode(SPEC_EXAMPLE)

        assertThat(untimed.interpolateAtTime(1_000L)).isNull()
        assertThat(untimed.getPrefixAtTime(1_000L)).isEmpty()
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
    fun `encode rounds ties half away from zero like the reference implementation`() {
        // 2.5 and -2.5 at precision 0 round to 3 and -3, whose zigzag values 6 and 5 are 'G'
        // and 'F'. Rounding half to even would give 2 and -2, encoded as "BAED".
        val encoded = FlexiblePolylineUtil.encode(listOf(FlexiblePoint(2.5, -2.5)), precision = 0)

        assertThat(encoded).isEqualTo("BAGF")
    }

    @Test
    fun `encode rounds third dimension ties half away from zero`() {
        // 12.345 * 100 is exactly 1234.5 in double arithmetic, so ordinary decimal input
        // lands on a tie as well.
        val decoded =
            FlexiblePolylineUtil.decode(
                FlexiblePolylineUtil.encode(
                    points =
                    listOf(
                        FlexiblePoint(12.345, -12.345, 10.5),
                        FlexiblePoint(0.0, 0.0, -10.5),
                    ),
                    precision = 2,
                    thirdDimension = ThirdDimension.ALTITUDE,
                    thirdDimensionPrecision = 0,
                ),
            )

        assertThat(decoded.points[0].latitude).isWithin(EPSILON).of(12.35)
        assertThat(decoded.points[0].longitude).isWithin(EPSILON).of(-12.35)
        assertThat(decoded.points.map { it.thirdDimensionValue })
            .containsExactly(11.0, -11.0)
            .inOrder()
    }

    @Test
    fun `encode and decode round trip deltas whose zigzag value sets the top bit`() {
        // A delta of magnitude 2^62 or more zigzags to a value with bit 63 set, which must be
        // handled as unsigned on both sides. The second delta overflows a Long and relies on
        // the decoder wrapping the same way the encoder did.
        val points =
            listOf(
                FlexiblePoint(0.0, 0.0, 5e18),
                FlexiblePoint(0.0, 0.0, -5e18),
            )

        val decoded =
            FlexiblePolylineUtil.decode(
                FlexiblePolylineUtil.encode(
                    points = points,
                    precision = 0,
                    thirdDimension = ThirdDimension.CUSTOM1,
                    thirdDimensionPrecision = 0,
                ),
            )

        assertThat(decoded.points.map { it.thirdDimensionValue })
            .containsExactly(5e18, -5e18)
            .inOrder()
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
    fun `toLatLngs discards the third dimension and timestamps`() {
        val encoded =
            FlexiblePolylineUtil.encode(
                points = listOf(FlexiblePoint(50.10228, 8.69821, 42.0, 1_700_000_000_000L)),
                thirdDimension = ThirdDimension.ALTITUDE,
                thirdDimensionPrecision = 1,
            )

        val decoded = FlexiblePolylineUtil.decode(encoded)

        assertThat(decoded.points.single().thirdDimensionValue).isNotNull()
        assertThat(decoded.points.single().timestampMillis).isEqualTo(1_700_000_000_000L)
        assertThat(decoded.toLatLngs().single().latitude).isWithin(EPSILON).of(50.10228)
    }

    @Test
    fun `getThirdDimension and hasTime read the header without decoding points`() {
        assertThat(FlexiblePolylineUtil.getThirdDimension(SPEC_EXAMPLE))
            .isEqualTo(ThirdDimension.ABSENT)
        assertThat(FlexiblePolylineUtil.hasTime(SPEC_EXAMPLE)).isFalse()
        assertThat(FlexiblePolylineUtil.getThirdDimension("BnJglg07do9-8lFw-B3oFvzxB0hCv-anuxC-8B"))
            .isEqualTo(ThirdDimension.ALTITUDE)
        assertThat(FlexiblePolylineUtil.hasTime("BnJglg07do9-8lFw-B3oFvzxB0hCv-anuxC-8B")).isFalse()
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

    @Test
    fun `encode rejects an out of range timePrecision or timePrecision without hasTime`() {
        assertThrows(IllegalArgumentException::class.java) {
            FlexiblePolylineUtil.encode(
                points = SPEC_EXAMPLE_POINTS,
                hasTime = true,
                timePrecision = 10,
            )
        }
        assertThrows(IllegalArgumentException::class.java) {
            FlexiblePolylineUtil.encode(
                points = SPEC_EXAMPLE_POINTS,
                hasTime = true,
                timePrecision = -1,
            )
        }
        assertThrows(IllegalArgumentException::class.java) {
            FlexiblePolylineUtil.encode(
                points = SPEC_EXAMPLE_POINTS,
                hasTime = false,
                timePrecision = 3,
            )
        }
    }

    @Test
    fun `encode rejects a point missing its timestampMillis when hasTime is true`() {
        val exception =
            assertThrows(IllegalArgumentException::class.java) {
                FlexiblePolylineUtil.encode(
                    points =
                    listOf(
                        FlexiblePoint(50.10228, 8.69821, timestampMillis = 1_700_000_000_000L),
                        FlexiblePoint(50.10201, 8.69567),
                    ),
                )
            }

        assertThat(exception).hasMessageThat().contains("timestampMillis")
    }
}
