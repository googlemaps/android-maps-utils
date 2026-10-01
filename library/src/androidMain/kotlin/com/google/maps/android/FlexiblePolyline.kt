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
import kotlin.math.abs
import kotlin.math.pow

/**
 * The kind of optional third value carried by every point of a flexible polyline.
 *
 * @property value The numeric type identifier used on the wire.
 */
public enum class ThirdDimension(
    public val value: Int,
) {
    /** No third value is present; the polyline is purely two dimensional. */
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
        fun fromValue(value: Int): ThirdDimension =
            entries.firstOrNull { it.value == value }
                ?: throw IllegalArgumentException("Unsupported third dimension type: $value")
    }
}

/**
 * A single point of a flexible polyline.
 *
 * @property latitude Latitude in degrees.
 * @property longitude Longitude in degrees.
 * @property thirdDimensionValue The third value for this point, or `null` when the polyline
 * carries no third dimension. The unit is defined by the polyline's [ThirdDimension].
 */
public data class FlexiblePoint
    @JvmOverloads
    public constructor(
        public val latitude: Double,
        public val longitude: Double,
        public val thirdDimensionValue: Double? = null,
    ) {
        /**
         * Returns this point as a [LatLng], discarding [thirdDimensionValue].
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
 */
public data class FlexiblePolyline(
    public val points: List<FlexiblePoint>,
    public val precision: Int,
    public val thirdDimension: ThirdDimension,
    public val thirdDimensionPrecision: Int,
) {
    /**
     * Returns the decoded points as [LatLng] values ready to pass to
     * [com.google.android.gms.maps.model.PolylineOptions], discarding any third dimension.
     *
     * @return The two dimensional path described by this polyline.
     */
    public fun toLatLngs(): List<LatLng> = points.map(FlexiblePoint::toLatLng)
}

/**
 * Encodes and decodes the HERE Flexible Polyline format, an open compact encoding for
 * coordinate sequences published at https://github.com/heremaps/flexible-polyline.
 *
 * The format is a superset of the Encoded Polyline Algorithm Format handled by
 * [PolyUtil.decode] and [PolyUtil.encode]. It differs in three ways:
 *
 * - precision is configurable rather than fixed at five decimal places;
 * - a header travels with the data, so a decoder reads the precision out of the string
 *   instead of having to be told out of band;
 * - each point may carry an optional third value such as altitude or level.
 *
 * It also uses the URL safe base64 alphabet, so encoded strings need no escaping in query
 * parameters or JSON. [PolyUtil] handles the Google format.
 *
 * Written from the published specification.
 */
public object FlexiblePolylineUtil {
    /** The format version written to, and expected in, the header. */
    private const val FORMAT_VERSION = 1L

    /** The URL safe base64 alphabet, indexed by six bit group value. */
    private const val ENCODING_TABLE =
        "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789-_"

    /** The largest number of decimal places the header can represent. */
    private const val MAX_PRECISION = 15

    /** Mask of the five value bits of a varint group. */
    private const val VALUE_MASK = 0x1FL

    /** The continuation bit of a varint group. */
    private const val CONTINUATION_BIT = 0x20L

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
     * Seven, the default, keeps roughly centimetre resolution.
     * @return The encoded polyline, including its header.
     * @throws IllegalArgumentException if [precision] is outside `0..15`.
     */
    @JvmStatic
    @JvmOverloads
    public fun encodeLatLngs(
        path: List<LatLng>,
        precision: Int = 7,
    ): String =
        encode(
            points = path.map { FlexiblePoint(it.latitude, it.longitude) },
            precision = precision,
        )

    /**
     * Encodes a sequence of points as a flexible polyline.
     *
     * When [thirdDimension] is anything other than [ThirdDimension.ABSENT], every point must
     * supply a [FlexiblePoint.thirdDimensionValue], because the format stores one third value
     * per point and cannot represent a gap.
     *
     * @param points The points to encode, in order.
     * @param precision The number of decimal places to preserve for latitude and longitude,
     * in the range `0..15`. Seven, the default, keeps roughly centimetre resolution.
     * @param thirdDimension The kind of third value carried by [points].
     * @param thirdDimensionPrecision The number of decimal places to preserve for the third
     * value, in the range `0..15`. Must be `0` when [thirdDimension] is
     * [ThirdDimension.ABSENT].
     * @return The encoded polyline, including its header.
     * @throws IllegalArgumentException if a precision is outside `0..15`, if
     * [thirdDimension] is a value the specification reserves, if
     * [thirdDimensionPrecision] is non zero for an absent third dimension, or if a point is
     * missing a required third value.
     */
    @JvmStatic
    @JvmOverloads
    public fun encode(
        points: List<FlexiblePoint>,
        precision: Int = 7,
        thirdDimension: ThirdDimension = ThirdDimension.ABSENT,
        thirdDimensionPrecision: Int = 0,
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

        val result = StringBuilder()
        encodeUnsignedVarint(FORMAT_VERSION, result)
        encodeUnsignedVarint(
            headerContent(precision, thirdDimension, thirdDimensionPrecision),
            result,
        )

        val multiplier = 10.0.pow(precision)
        val thirdMultiplier = 10.0.pow(thirdDimensionPrecision)
        var lastLat = 0L
        var lastLng = 0L
        var lastThird = 0L

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
        }
        return result.toString()
    }

    /**
     * Decodes a flexible polyline, reading its precision and third dimension from the header.
     *
     * @param encoded The encoded polyline.
     * @return The decoded points together with the header parameters.
     * @throws IllegalArgumentException if [encoded] is empty, declares an unsupported format
     * version, contains a character outside the alphabet, or ends part way through a point.
     */
    @JvmStatic
    public fun decode(encoded: String): FlexiblePolyline {
        require(encoded.isNotEmpty()) { "encoded polyline must not be empty" }

        val cursor = Cursor(encoded)
        val version = decodeUnsignedVarint(cursor)
        require(version == FORMAT_VERSION) {
            "Unsupported flexible polyline version: $version"
        }

        val header = decodeUnsignedVarint(cursor).toInt()
        val precision = header and 0x0F
        val thirdDimension = ThirdDimension.fromValue((header shr 4) and 0x07)
        val thirdDimensionPrecision = (header shr 7) and 0x0F

        val multiplier = 10.0.pow(precision)
        val thirdMultiplier = 10.0.pow(thirdDimensionPrecision)
        val hasThirdDimension = thirdDimension != ThirdDimension.ABSENT
        val points = mutableListOf<FlexiblePoint>()
        var lat = 0L
        var lng = 0L
        var third = 0L

        while (!cursor.exhausted()) {
            lat += decodeSignedVarint(cursor)
            lng += decodeSignedVarint(cursor)
            if (hasThirdDimension) {
                third += decodeSignedVarint(cursor)
                points.add(
                    FlexiblePoint(
                        lat / multiplier,
                        lng / multiplier,
                        third / thirdMultiplier,
                    ),
                )
            } else {
                points.add(FlexiblePoint(lat / multiplier, lng / multiplier))
            }
        }

        return FlexiblePolyline(
            points = points,
            precision = precision,
            thirdDimension = thirdDimension,
            thirdDimensionPrecision = thirdDimensionPrecision,
        )
    }

    /**
     * Decodes a flexible polyline into a path ready to draw on a map, discarding any third
     * dimension.
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
        require(encoded.isNotEmpty()) { "encoded polyline must not be empty" }
        val cursor = Cursor(encoded)
        val version = decodeUnsignedVarint(cursor)
        require(version == FORMAT_VERSION) {
            "Unsupported flexible polyline version: $version"
        }
        return ThirdDimension.fromValue((decodeUnsignedVarint(cursor).toInt() shr 4) and 0x07)
    }

    /** Packs the header parameters into the single value that follows the version. */
    private fun headerContent(
        precision: Int,
        thirdDimension: ThirdDimension,
        thirdDimensionPrecision: Int,
    ): Long =
        ((thirdDimensionPrecision shl 7) or (thirdDimension.value shl 4) or precision).toLong()

    /**
     * Scales a coordinate to the fixed point integer the format stores.
     *
     * Ties round half away from zero, as the reference implementation does, so that values
     * such as `2.5` at precision `0` encode to the same string as other encoders.
     */
    private fun scale(
        value: Double,
        multiplier: Double,
    ): Long {
        val scaled = value * multiplier
        val magnitude = Math.round(abs(scaled))
        return if (scaled < 0) -magnitude else magnitude
    }

    /**
     * Appends [value] as an unsigned varint over five bit groups, least significant first.
     *
     * [value] is treated as unsigned, so a zigzag value with the top bit set still encodes.
     */
    private fun encodeUnsignedVarint(
        value: Long,
        result: StringBuilder,
    ) {
        var remaining = value
        while ((remaining and VALUE_MASK.inv()) != 0L) {
            result.append(ENCODING_TABLE[((remaining and VALUE_MASK) or CONTINUATION_BIT).toInt()])
            remaining = remaining ushr 5
        }
        result.append(ENCODING_TABLE[remaining.toInt()])
    }

    /** Appends [value] as a zigzag mapped varint, so small negatives stay short. */
    private fun encodeSignedVarint(
        value: Long,
        result: StringBuilder,
    ) {
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
    private class Cursor(
        private val encoded: String,
    ) {
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
