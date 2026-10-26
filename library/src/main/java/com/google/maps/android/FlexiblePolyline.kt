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

import android.os.Build
import androidx.annotation.RequiresApi
import com.google.android.gms.maps.model.LatLng
import java.time.Instant
import kotlin.math.abs
import kotlin.math.pow

/**
 * The kind of optional third value carried by every point of a flexible polyline.
 *
 * @property value The numeric type identifier used on the wire.
 */
public enum class ThirdDimension(public val value: Int) {
    /** No third value is present; the polyline has no vertical or custom third dimension. */
    ABSENT(0),

    /**
     * A discrete level, typically used to disambiguate stacked geometry such as a bridge
     * crossing over the road beneath it.
     */
    LEVEL(1),

    /** Height above the reference ellipsoid, in metres. */
    ALTITUDE(2),

    /** Height of the terrain above the reference ellipsoid, in metres. */
    ELEVATION(3),

    /** Reserved by the specification for future use. Decoded, but rejected when encoding. */
    RESERVED1(4),

    /** Reserved by the specification for future use. Decoded, but rejected when encoding. */
    RESERVED2(5),

    /** Application defined value, with semantics agreed out of band. */
    CUSTOM1(6),

    /** Application defined value, with semantics agreed out of band. */
    CUSTOM2(7),
    ;

    internal companion object {
        /**
         * Returns the constant matching the given wire [value].
         *
         * @throws IllegalArgumentException if [value] is outside the range `0..7`.
         */
        fun fromValue(value: Int): ThirdDimension = entries.firstOrNull { it.value == value }
            ?: throw IllegalArgumentException("Unsupported third dimension type: $value")
    }
}

/**
 * A single point of a flexible polyline, supporting 2D `(lat, lng)`, 3D `(lat, lng, z)` or
 * `(lat, lng, time)`, and 4D `(lat, lng, z, time)` track coordinates.
 *
 * @property latitude Latitude in degrees.
 * @property longitude Longitude in degrees.
 * @property thirdDimensionValue The third value for this point (such as elevation or altitude in
 * metres), or `null` when the polyline carries no third dimension. The unit is defined by the
 * polyline's [ThirdDimension].
 * @property timestampMillis The timestamp for this point in epoch milliseconds, or `null` when
 * the polyline carries no time dimension.
 */
public data class FlexiblePoint
@JvmOverloads
public constructor(
    public val latitude: Double,
    public val longitude: Double,
    public val thirdDimensionValue: Double? = null,
    public val timestampMillis: Long? = null,
) {
    /**
     * Constructs a point with an optional third dimension and a [Instant] timestamp, converted
     * to epoch milliseconds.
     *
     * @param latitude Latitude in degrees.
     * @param longitude Longitude in degrees.
     * @param thirdDimensionValue Optional third dimension value (such as elevation in metres).
     * @param timestamp Timestamp as an [Instant].
     */
    @RequiresApi(Build.VERSION_CODES.O)
    @JvmOverloads
    public constructor(
        latitude: Double,
        longitude: Double,
        thirdDimensionValue: Double? = null,
        timestamp: Instant,
    ) : this(
        latitude = latitude,
        longitude = longitude,
        thirdDimensionValue = thirdDimensionValue,
        timestampMillis = timestamp.toEpochMilli(),
    )

    /**
     * The timestamp for this point as an [Instant], or `null` when the polyline carries no time
     * dimension.
     */
    @get:RequiresApi(Build.VERSION_CODES.O)
    public val timestamp: Instant?
        get() = timestampMillis?.let(Instant::ofEpochMilli)

    /**
     * Returns this point as a [LatLng], discarding [thirdDimensionValue] and [timestampMillis].
     *
     * @return The two dimensional position of this point.
     */
    public fun toLatLng(): LatLng = LatLng(latitude, longitude)
}

/**
 * A decoded flexible polyline, together with the parameters read from its header.
 *
 * @property points The decoded points, in order.
 * @property precision The number of decimal places preserved for latitude and longitude.
 * @property thirdDimension The kind of third value carried by [points].
 * @property thirdDimensionPrecision The number of decimal places preserved for the third
 * value, or `0` when [thirdDimension] is [ThirdDimension.ABSENT].
 * @property hasTime Whether each point in [points] carries a [FlexiblePoint.timestampMillis].
 * @property timePrecision The number of decimal places of a second preserved for timestamps
 * (for example, `0` for whole seconds, `1` for `100 ms`, `2` for `10 ms`, `3` for `1 ms`), or `0`
 * when [hasTime] is `false`.
 */
public data class FlexiblePolyline
@JvmOverloads
public constructor(
    public val points: List<FlexiblePoint>,
    public val precision: Int,
    public val thirdDimension: ThirdDimension,
    public val thirdDimensionPrecision: Int,
    public val hasTime: Boolean = false,
    public val timePrecision: Int = 0,
) {
    /**
     * Total elapsed time of this polyline in milliseconds from the first point to the last point,
     * or `null` when [hasTime] is `false` or [points] is empty.
     */
    public val durationMillis: Long?
        get() {
            if (!hasTime || points.isEmpty()) return null
            val start = points.first().timestampMillis ?: return null
            val end = points.last().timestampMillis ?: return null
            return end - start
        }

    /**
     * Returns the decoded points as [LatLng] values ready to pass to
     * [com.google.android.gms.maps.model.PolylineOptions], discarding any third dimension and
     * timestamps.
     *
     * @return The two dimensional path described by this polyline.
     */
    public fun toLatLngs(): List<LatLng> = points.map(FlexiblePoint::toLatLng)

    /**
     * Interpolates a [FlexiblePoint] along this polyline at [timestampMillis].
     *
     * Requires [hasTime] to be `true` and [points] to be non-empty with non-decreasing
     * timestamps. Timestamps before the first point or after the last point clamp to the
     * respective endpoint. Position is interpolated geodesically via
     * [SphericalUtil.interpolate], and [FlexiblePoint.thirdDimensionValue] (when present)
     * is interpolated linearly in time.
     *
     * @param timestampMillis Target timestamp in epoch milliseconds.
     * @return The interpolated [FlexiblePoint], or `null` if this polyline has no time
     * dimension or no points.
     */
    public fun interpolateAtTime(timestampMillis: Long): FlexiblePoint? {
        if (!hasTime || points.isEmpty()) return null
        val first = points.first()
        val firstTime = first.timestampMillis ?: return null
        if (timestampMillis <= firstTime) return first
        val last = points.last()
        val lastTime = last.timestampMillis ?: return null
        if (timestampMillis >= lastTime) return last

        val index = findLastIndexAtOrBefore(timestampMillis) ?: return null
        val p0 = points[index]
        val p1 = points[index + 1]
        val t0 = p0.timestampMillis ?: return null
        val t1 = p1.timestampMillis ?: return null
        if (timestampMillis == t0 || t1 <= t0) return p0

        val fraction = (timestampMillis - t0).toDouble() / (t1 - t0).toDouble()
        val position = SphericalUtil.interpolate(p0.toLatLng(), p1.toLatLng(), fraction)
        val third =
            if (p0.thirdDimensionValue != null && p1.thirdDimensionValue != null) {
                p0.thirdDimensionValue + (p1.thirdDimensionValue - p0.thirdDimensionValue) * fraction
            } else {
                p0.thirdDimensionValue
            }
        return FlexiblePoint(
            latitude = position.latitude,
            longitude = position.longitude,
            thirdDimensionValue = third,
            timestampMillis = timestampMillis,
        )
    }

    /**
     * Interpolates a [FlexiblePoint] along this polyline at [timestamp].
     *
     * @param timestamp Target timestamp.
     * @return The interpolated [FlexiblePoint], or `null` if this polyline has no time
     * dimension or no points.
     */
    @RequiresApi(Build.VERSION_CODES.O)
    public fun interpolateAtTime(timestamp: Instant): FlexiblePoint? = interpolateAtTime(timestamp.toEpochMilli())

    /**
     * Returns the prefix of this polyline's points from the start up to [timestampMillis],
     * ending at the interpolated position for [timestampMillis].
     *
     * Useful for rendering the completed portion of a timed track (such as a hike, bike ride,
     * or flight path) up to a playback cursor.
     *
     * @param timestampMillis Target timestamp in epoch milliseconds.
     * @return The points from the start of the track up to [timestampMillis], or an empty list
     * if this polyline has no time dimension, has no points, or starts after [timestampMillis].
     */
    public fun getPrefixAtTime(timestampMillis: Long): List<FlexiblePoint> {
        if (!hasTime || points.isEmpty()) return emptyList()
        val firstTime = points.first().timestampMillis ?: return emptyList()
        if (timestampMillis < firstTime) return emptyList()
        val lastTime = points.last().timestampMillis ?: return emptyList()
        if (timestampMillis >= lastTime) return points

        val index = findLastIndexAtOrBefore(timestampMillis) ?: return emptyList()
        val prefix = points.subList(0, index + 1).toMutableList()
        val lastIncludedTime = points[index].timestampMillis ?: return emptyList()
        if (lastIncludedTime < timestampMillis) {
            interpolateAtTime(timestampMillis)?.let(prefix::add)
        }
        return prefix
    }

    /**
     * Returns the prefix of this polyline's points from the start up to [timestamp], ending at
     * the interpolated position for [timestamp].
     *
     * @param timestamp Target timestamp.
     * @return The points from the start of the track up to [timestamp], or an empty list if
     * this polyline has no time dimension, has no points, or starts after [timestamp].
     */
    @RequiresApi(Build.VERSION_CODES.O)
    public fun getPrefixAtTime(timestamp: Instant): List<FlexiblePoint> = getPrefixAtTime(timestamp.toEpochMilli())

    /**
     * Binary searches [points] for the last index `i` in `0 until points.lastIndex` where
     * `points[i].timestampMillis <= timestampMillis`.
     */
    private fun findLastIndexAtOrBefore(timestampMillis: Long): Int? {
        var low = 0
        var high = points.lastIndex
        while (low + 1 < high) {
            val mid = (low + high) ushr 1
            val midTime = points[mid].timestampMillis ?: return null
            if (midTime <= timestampMillis) {
                low = mid
            } else {
                high = mid
            }
        }
        return low
    }
}

/**
 * Encodes and decodes the Flexible Polyline format, an open compact encoding for coordinate
 * sequences published at https://github.com/heremaps/flexible-polyline, extended with an optional
 * self-describing time dimension for 3D `(lat, lng, time)` and 4D `(lat, lng, z, time)` tracks.
 *
 * The format is a superset of the Encoded Polyline Algorithm Format handled by
 * [PolyUtil.decode] and [PolyUtil.encode]. It differs in four ways:
 *
 * - precision is configurable rather than fixed at five decimal places (defaulting to `6`, or
 *   roughly `11 cm` spatial resolution);
 * - a header travels with the data, so a decoder reads the precision and dimensions out of the
 *   string instead of having to be told out of band;
 * - each point may carry an optional third value such as [ThirdDimension.ELEVATION] or
 *   [ThirdDimension.ALTITUDE];
 * - each point may also carry a sub-second timestamp ([FlexiblePoint.timestampMillis]),
 *   stored as integer varint deltas in the time precision declared in bits `11..15` of the header
 *   (defaulting to `1`, or `100 ms` resolution).
 *
 * When `hasTime` is `false`, bits `11..15` of the header are zero and the encoded output is
 * bit-for-bit identical to the HERE Flexible Polyline v1 specification.
 *
 * It also uses the URL safe base64 alphabet, so encoded strings need no escaping in query
 * parameters or JSON. [PolyUtil] handles the Google format.
 */
public object FlexiblePolylineUtil {
    /** The format version written to, and expected in, the header. */
    private const val FORMAT_VERSION = 1L

    /** The URL safe base64 alphabet, indexed by six bit group value. */
    private const val ENCODING_TABLE =
        "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789-_"

    /** The largest number of decimal places the header can represent for spatial dimensions. */
    private const val MAX_PRECISION = 15

    /**
     * The largest number of decimal places of a second supported for timestamps (`9` = nanoseconds,
     * the finest power-of-ten resolution that fits epoch timestamps in a signed 64-bit [Long]).
     */
    private const val MAX_TIME_PRECISION = 9

    /** Default coordinate precision (`6` decimal places, or roughly `11 cm` resolution). */
    public const val DEFAULT_PRECISION: Int = 6

    /** Default time precision when encoding timestamps (`1` = deciseconds, `100 ms` / `0.1 s`). */
    public const val DEFAULT_TIME_PRECISION: Int = 1

    /** Number of decimal places of a second represented by [FlexiblePoint.timestampMillis]. */
    private const val MILLIS_TIME_PRECISION = 3

    /** Header bit 11 indicating that each point carries a timestamp delta after its spatial deltas. */
    private const val HAS_TIME_BIT = 1 shl 11

    /** Mask of all defined bits (`0..15`) in the header varint. */
    private const val KNOWN_HEADER_MASK = 0xFFFFL

    /** Mask of the five value bits of a varint group. */
    private const val VALUE_MASK = 0x1FL

    /** The continuation bit of a varint group. */
    private const val CONTINUATION_BIT = 0x20L

    /** Precomputed powers of ten (`10^0` through `10^9`) for exact integer time scaling. */
    private val POWERS_OF_TEN =
        LongArray(MAX_TIME_PRECISION + 1) { index ->
            var result = 1L
            repeat(index) { result *= 10L }
            result
        }

    /** Reverse of [ENCODING_TABLE]; `-1` marks a character outside the alphabet. */
    private val DECODING_TABLE =
        IntArray(128) { -1 }.apply {
            ENCODING_TABLE.forEachIndexed { index, character -> this[character.code] = index }
        }

    /**
     * Encodes a sequence of [LatLng] values as a two dimensional flexible polyline.
     *
     * @param path The points to encode, in order.
     * @param precision The number of decimal places to preserve, in the range `0..15`.
     * Defaults to [DEFAULT_PRECISION] (`6`, or roughly `11 cm` resolution).
     * @return The encoded polyline, including its header.
     * @throws IllegalArgumentException if [precision] is outside `0..15`.
     */
    @JvmStatic
    @JvmOverloads
    public fun encodeLatLngs(path: List<LatLng>, precision: Int = DEFAULT_PRECISION): String = encode(
        points = path.map { FlexiblePoint(it.latitude, it.longitude) },
        precision = precision,
    )

    /**
     * Encodes a sequence of points as a flexible polyline (2D, 3D, or 4D with elevation and time).
     *
     * When [thirdDimension] is anything other than [ThirdDimension.ABSENT], every point must
     * supply a [FlexiblePoint.thirdDimensionValue]. Likewise, when [hasTime] is `true`, every
     * point must supply a [FlexiblePoint.timestampMillis], because the format stores one delta
     * per enabled dimension per point and cannot represent a gap.
     *
     * @param points The points to encode, in order.
     * @param precision The number of decimal places to preserve for latitude and longitude,
     * in the range `0..15`. Defaults to [DEFAULT_PRECISION] (`6`, or roughly `11 cm` resolution).
     * @param thirdDimension The kind of third value carried by [points].
     * @param thirdDimensionPrecision The number of decimal places to preserve for the third
     * value, in the range `0..15`. Must be `0` when [thirdDimension] is
     * [ThirdDimension.ABSENT].
     * @param hasTime Whether to encode [FlexiblePoint.timestampMillis] for each point. Defaults
     * to `true` when any point in [points] has a non-null [FlexiblePoint.timestampMillis].
     * @param timePrecision The number of decimal places of a second to preserve for timestamps,
     * in the range `0..9` (`0` = `1 s`, `1` = `100 ms`, `2` = `10 ms`, `3` = `1 ms`). Defaults
     * to [DEFAULT_TIME_PRECISION] (`1`, `100 ms` resolution) when [hasTime] is `true`, and
     * must be `0` when [hasTime] is `false`.
     * @return The encoded polyline, including its header.
     * @throws IllegalArgumentException if a precision is out of range, if [thirdDimension] is a
     * value the specification reserves, if a dimension precision is non-zero for an absent
     * dimension, or if a point is missing a required third value or timestamp.
     */
    @JvmStatic
    @JvmOverloads
    public fun encode(
        points: List<FlexiblePoint>,
        precision: Int = DEFAULT_PRECISION,
        thirdDimension: ThirdDimension = ThirdDimension.ABSENT,
        thirdDimensionPrecision: Int = 0,
        hasTime: Boolean = points.any { it.timestampMillis != null },
        timePrecision: Int = if (hasTime) DEFAULT_TIME_PRECISION else 0,
    ): String {
        require(precision in 0..MAX_PRECISION) {
            "precision must be in 0..$MAX_PRECISION but was $precision"
        }
        require(thirdDimensionPrecision in 0..MAX_PRECISION) {
            "thirdDimensionPrecision must be in 0..$MAX_PRECISION but was " +
                "$thirdDimensionPrecision"
        }
        require(
            thirdDimension != ThirdDimension.RESERVED1 &&
                thirdDimension != ThirdDimension.RESERVED2,
        ) {
            "$thirdDimension is reserved by the specification and cannot be encoded"
        }
        require(thirdDimension != ThirdDimension.ABSENT || thirdDimensionPrecision == 0) {
            "thirdDimensionPrecision must be 0 when no third dimension is present"
        }
        require(timePrecision in 0..MAX_TIME_PRECISION) {
            "timePrecision must be in 0..$MAX_TIME_PRECISION but was $timePrecision"
        }
        require(hasTime || timePrecision == 0) {
            "timePrecision must be 0 when no time dimension is present"
        }

        val result = StringBuilder()
        encodeUnsignedVarint(FORMAT_VERSION, result)
        encodeUnsignedVarint(
            headerContent(
                precision = precision,
                thirdDimension = thirdDimension,
                thirdDimensionPrecision = thirdDimensionPrecision,
                hasTime = hasTime,
                timePrecision = timePrecision,
            ),
            result,
        )

        val multiplier = 10.0.pow(precision)
        val thirdMultiplier = 10.0.pow(thirdDimensionPrecision)
        var lastLat = 0L
        var lastLng = 0L
        var lastThird = 0L
        var lastTime = 0L

        for (point in points) {
            val lat = scale(point.latitude, multiplier)
            val lng = scale(point.longitude, multiplier)
            encodeSignedVarint(lat - lastLat, result)
            encodeSignedVarint(lng - lastLng, result)
            lastLat = lat
            lastLng = lng

            if (thirdDimension != ThirdDimension.ABSENT) {
                val value =
                    requireNotNull(point.thirdDimensionValue) {
                        "thirdDimensionValue is required for every point when encoding " +
                            "$thirdDimension"
                    }
                val third = scale(value, thirdMultiplier)
                encodeSignedVarint(third - lastThird, result)
                lastThird = third
            }

            if (hasTime) {
                val timestampMillis =
                    requireNotNull(point.timestampMillis) {
                        "timestampMillis is required for every point when encoding time"
                    }
                val scaledTime = scaleTimeMillis(timestampMillis, timePrecision)
                encodeSignedVarint(scaledTime - lastTime, result)
                lastTime = scaledTime
            }
        }
        return result.toString()
    }

    /**
     * Decodes a flexible polyline, reading its precision, third dimension, and optional time
     * dimension from the header.
     *
     * @param encoded The encoded polyline.
     * @return The decoded points together with the header parameters.
     * @throws IllegalArgumentException if [encoded] is empty, declares an unsupported format
     * version or header flags, contains a character outside the alphabet, or ends part way
     * through a point.
     */
    @JvmStatic
    public fun decode(encoded: String): FlexiblePolyline {
        require(encoded.isNotEmpty()) { "encoded polyline must not be empty" }

        val cursor = Cursor(encoded)
        val version = decodeUnsignedVarint(cursor)
        require(version == FORMAT_VERSION) {
            "Unsupported flexible polyline version: $version"
        }

        val headerLong = decodeUnsignedVarint(cursor)
        require((headerLong and KNOWN_HEADER_MASK.inv()) == 0L) {
            "Unsupported flexible polyline header flags: $headerLong"
        }
        val header = headerLong.toInt()
        val precision = header and 0x0F
        val thirdDimension = ThirdDimension.fromValue((header shr 4) and 0x07)
        val thirdDimensionPrecision = (header shr 7) and 0x0F
        val hasTime = (header and HAS_TIME_BIT) != 0
        val timePrecision = (header shr 12) and 0x0F
        require(hasTime || timePrecision == 0) {
            "timePrecision must be 0 when no time dimension is present"
        }
        require(timePrecision in 0..MAX_TIME_PRECISION) {
            "timePrecision must be in 0..$MAX_TIME_PRECISION but was $timePrecision"
        }

        val multiplier = 10.0.pow(precision)
        val thirdMultiplier = 10.0.pow(thirdDimensionPrecision)
        val hasThirdDimension = thirdDimension != ThirdDimension.ABSENT
        val points = mutableListOf<FlexiblePoint>()
        var lat = 0L
        var lng = 0L
        var third = 0L
        var time = 0L

        while (!cursor.exhausted()) {
            lat += decodeSignedVarint(cursor)
            lng += decodeSignedVarint(cursor)
            val thirdValue =
                if (hasThirdDimension) {
                    third += decodeSignedVarint(cursor)
                    third / thirdMultiplier
                } else {
                    null
                }
            val timestampMillis =
                if (hasTime) {
                    time += decodeSignedVarint(cursor)
                    unscaleTimeMillis(time, timePrecision)
                } else {
                    null
                }
            points.add(
                FlexiblePoint(
                    latitude = lat / multiplier,
                    longitude = lng / multiplier,
                    thirdDimensionValue = thirdValue,
                    timestampMillis = timestampMillis,
                ),
            )
        }

        return FlexiblePolyline(
            points = points,
            precision = precision,
            thirdDimension = thirdDimension,
            thirdDimensionPrecision = thirdDimensionPrecision,
            hasTime = hasTime,
            timePrecision = timePrecision,
        )
    }

    /**
     * Decodes a flexible polyline into a path ready to draw on a map, discarding any third
     * dimension and timestamps.
     *
     * @param encoded The encoded polyline.
     * @return The decoded two dimensional path.
     * @throws IllegalArgumentException if [encoded] is not a well formed flexible polyline.
     */
    @JvmStatic
    public fun decodeToLatLngs(encoded: String): List<LatLng> = decode(encoded).toLatLngs()

    /**
     * Returns the third dimension declared by an encoded polyline without decoding its points.
     *
     * @param encoded The encoded polyline.
     * @return The kind of third value the polyline carries.
     * @throws IllegalArgumentException if [encoded] has no readable header.
     */
    @JvmStatic
    public fun getThirdDimension(encoded: String): ThirdDimension {
        val header = readHeader(encoded)
        return ThirdDimension.fromValue((header shr 4) and 0x07)
    }

    /**
     * Returns whether an encoded polyline carries a time dimension without decoding its points.
     *
     * @param encoded The encoded polyline.
     * @return `true` if each point in [encoded] includes a timestamp delta.
     * @throws IllegalArgumentException if [encoded] has no readable header.
     */
    @JvmStatic
    public fun hasTime(encoded: String): Boolean {
        val header = readHeader(encoded)
        return (header and HAS_TIME_BIT) != 0
    }

    /** Validates and reads the header bitfield without decoding coordinate points. */
    private fun readHeader(encoded: String): Int {
        require(encoded.isNotEmpty()) { "encoded polyline must not be empty" }
        val cursor = Cursor(encoded)
        val version = decodeUnsignedVarint(cursor)
        require(version == FORMAT_VERSION) {
            "Unsupported flexible polyline version: $version"
        }
        val headerLong = decodeUnsignedVarint(cursor)
        require((headerLong and KNOWN_HEADER_MASK.inv()) == 0L) {
            "Unsupported flexible polyline header flags: $headerLong"
        }
        return headerLong.toInt()
    }

    /** Packs the header parameters into the single value that follows the version. */
    private fun headerContent(
        precision: Int,
        thirdDimension: ThirdDimension,
        thirdDimensionPrecision: Int,
        hasTime: Boolean,
        timePrecision: Int,
    ): Long {
        val timeBits = if (hasTime) (timePrecision shl 12) or HAS_TIME_BIT else 0
        return (
            timeBits or
                (thirdDimensionPrecision shl 7) or
                (thirdDimension.value shl 4) or
                precision
            ).toLong()
    }

    /**
     * Scales a coordinate to the fixed point integer the format stores.
     *
     * Ties round half away from zero, as the reference implementation does, so that values
     * such as `2.5` at precision `0` encode to the same string as other encoders.
     */
    private fun scale(value: Double, multiplier: Double): Long {
        val scaled = value * multiplier
        val magnitude = Math.round(abs(scaled))
        return if (scaled < 0) -magnitude else magnitude
    }

    /**
     * Scales [timestampMillis] (in milliseconds, `10^-3` seconds) to the integer unit stored
     * on the wire for [timePrecision] decimal places of a second, rounding ties half away from
     * zero in exact integer arithmetic.
     */
    private fun scaleTimeMillis(timestampMillis: Long, timePrecision: Int): Long {
        if (timePrecision == MILLIS_TIME_PRECISION) {
            return timestampMillis
        }
        if (timePrecision > MILLIS_TIME_PRECISION) {
            val factor = POWERS_OF_TEN[timePrecision - MILLIS_TIME_PRECISION]
            return Math.multiplyExact(timestampMillis, factor)
        }
        val divisor = POWERS_OF_TEN[MILLIS_TIME_PRECISION - timePrecision]
        val quotient = timestampMillis / divisor
        val remainder = abs(timestampMillis % divisor)
        return if (remainder * 2L >= divisor) {
            if (timestampMillis < 0L) quotient - 1L else quotient + 1L
        } else {
            quotient
        }
    }

    /**
     * Converts a decoded wire time value at [timePrecision] decimal places of a second back to
     * epoch milliseconds, rounding ties half away from zero when [timePrecision] exceeds
     * millisecond resolution.
     */
    private fun unscaleTimeMillis(wireTime: Long, timePrecision: Int): Long {
        if (timePrecision == MILLIS_TIME_PRECISION) {
            return wireTime
        }
        if (timePrecision < MILLIS_TIME_PRECISION) {
            val factor = POWERS_OF_TEN[MILLIS_TIME_PRECISION - timePrecision]
            return wireTime * factor
        }
        val divisor = POWERS_OF_TEN[timePrecision - MILLIS_TIME_PRECISION]
        val quotient = wireTime / divisor
        val remainder = abs(wireTime % divisor)
        return if (remainder * 2L >= divisor) {
            if (wireTime < 0L) quotient - 1L else quotient + 1L
        } else {
            quotient
        }
    }

    /**
     * Appends [value] as an unsigned varint over five bit groups, least significant first.
     *
     * [value] is treated as unsigned, so a zigzag value with the top bit set still encodes.
     */
    private fun encodeUnsignedVarint(value: Long, result: StringBuilder) {
        var remaining = value
        while ((remaining and VALUE_MASK.inv()) != 0L) {
            result.append(ENCODING_TABLE[((remaining and VALUE_MASK) or CONTINUATION_BIT).toInt()])
            remaining = remaining ushr 5
        }
        result.append(ENCODING_TABLE[remaining.toInt()])
    }

    /** Appends [value] as a zigzag mapped varint, so small negatives stay short. */
    private fun encodeSignedVarint(value: Long, result: StringBuilder) {
        val zigzag = if (value < 0) (value shl 1).inv() else (value shl 1)
        encodeUnsignedVarint(zigzag, result)
    }

    /** Reads one unsigned varint, advancing [cursor] past it. */
    private fun decodeUnsignedVarint(cursor: Cursor): Long {
        var result = 0L
        var shift = 0
        while (!cursor.exhausted()) {
            val group = cursor.nextGroup()
            result = result or ((group and VALUE_MASK) shl shift)
            if ((group and CONTINUATION_BIT) == 0L) {
                return result
            }
            shift += 5
            require(shift < Long.SIZE_BITS) { "Varint at index ${cursor.index} is too long" }
        }
        throw IllegalArgumentException("Encoded polyline ends in an incomplete value")
    }

    /** Reads one zigzag mapped varint, advancing [cursor] past it. */
    private fun decodeSignedVarint(cursor: Cursor): Long {
        val zigzag = decodeUnsignedVarint(cursor)
        return if ((zigzag and 1L) == 1L) (zigzag ushr 1).inv() else (zigzag ushr 1)
    }

    /** Tracks the read position while decoding, and validates the alphabet. */
    private class Cursor(private val encoded: String) {
        var index: Int = 0
            private set

        fun exhausted(): Boolean = index >= encoded.length

        /**
         * Returns the six bit group at the current position and advances past it.
         *
         * @throws IllegalArgumentException if the character is outside the alphabet.
         */
        fun nextGroup(): Long {
            val character = encoded[index]
            val group =
                if (character.code < DECODING_TABLE.size) {
                    DECODING_TABLE[character.code]
                } else {
                    -1
                }
            require(group >= 0) {
                "Invalid character '$character' at index $index of encoded polyline"
            }
            index++
            return group.toLong()
        }
    }
}
