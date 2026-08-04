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
package com.google.maps.android.data.parser.geojson

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.io.InputStream

class GeoJsonParser(
    private val maxInputSize: Long = DEFAULT_MAX_INPUT_SIZE,
    private val maxStructuralDepth: Int = DEFAULT_MAX_STRUCTURAL_DEPTH,
) {
    fun parse(inputStream: InputStream): GeoJsonObject? {
        // Bound the untrusted input *before* materialising it. [Json.parseToJsonElement] reads the
        // whole document into an in-memory tree, so without these guards a hostile document can
        // exhaust memory (a wide document) or overflow the parser's stack (a deeply nested one)
        // before the [MAX_GEOMETRY_DEPTH] check — which runs on the already-built tree — can act.
        val json = inputStream.readTextBounded(maxInputSize)
        checkStructuralDepth(json, maxStructuralDepth)
        val jsonElement = Json.parseToJsonElement(json)

        return when (jsonElement.jsonObject["type"]?.jsonPrimitive?.content) {
            "FeatureCollection" -> {
                parseFeatureCollection(jsonElement)
            }

            "Feature" -> {
                parseFeature(jsonElement)
            }

            "Point", "LineString", "Polygon", "MultiPoint", "MultiLineString", "MultiPolygon", "GeometryCollection" -> {
                parseGeometry(
                    jsonElement,
                )
            }

            else -> {
                null
            }
        }
    }

    companion object {
        const val MAX_GEOMETRY_DEPTH = 20

        /**
         * Maximum number of characters read from an untrusted document. [Json.parseToJsonElement]
         * materialises the whole document in memory (with a large text-to-object amplification), so an
         * unbounded input is an [OutOfMemoryError] vector even with no nesting. The default is far
         * above any realistic layer (~60x the largest bundled sample); raise it via the constructor
         * for trusted large sources.
         */
        const val DEFAULT_MAX_INPUT_SIZE = 10L * 1024 * 1024

        /**
         * Maximum raw structural nesting (`{`/`[`) accepted. [Json.parseToJsonElement] parses nested
         * JSON with stack recursion, so a deeply nested document overflows the stack *before* the
         * [MAX_GEOMETRY_DEPTH] guard (which runs on the already-built tree) can act. Chosen to stay
         * safe on small Android thread stacks while remaining far above any legitimate GeoJSON, whose
         * structural depth is well under 100 even with maximally nested geometries.
         */
        const val DEFAULT_MAX_STRUCTURAL_DEPTH = 512

        val SUPPORTED_EXTENSIONS = setOf("json", "geojson")

        fun canParse(header: String): Boolean = header.trimStart().startsWith("{")
    }
}

/**
 * Reads the stream as UTF-8 text, failing fast with an [IllegalArgumentException] once more than
 * [maxChars] characters have been read, so an oversized untrusted document is never fully materialised.
 */
private fun InputStream.readTextBounded(maxChars: Long): String {
    val reader = bufferedReader()
    val builder = StringBuilder()
    val buffer = CharArray(8 * 1024)
    var total = 0L
    while (true) {
        val read = reader.read(buffer)
        if (read < 0) break
        total += read
        require(total <= maxChars) {
            "GeoJSON input exceeds the maximum allowed size of $maxChars characters"
        }
        builder.append(buffer, 0, read)
    }
    return builder.toString()
}

/**
 * Rejects, with an [IllegalArgumentException], any document whose raw structural nesting (`{`/`[`)
 * exceeds [maxDepth]. Runs in a single pass that ignores brackets inside string literals, *before*
 * [Json.parseToJsonElement], so a maliciously deep document is rejected instead of overflowing the
 * parser's stack.
 */
private fun checkStructuralDepth(json: String, maxDepth: Int) {
    var depth = 0
    var inString = false
    var escaped = false
    for (c in json) {
        if (inString) {
            when {
                escaped -> escaped = false
                c == '\\' -> escaped = true
                c == '"' -> inString = false
            }
            continue
        }
        when (c) {
            '"' -> inString = true
            '[', '{' -> {
                depth++
                require(depth <= maxDepth) {
                    "GeoJSON structural nesting exceeds the maximum depth of $maxDepth"
                }
            }
            ']', '}' -> if (depth > 0) depth--
        }
    }
}

private fun parseFeatureCollection(json: JsonElement): GeoJsonFeatureCollection {
    val featuresJson = json.jsonObject["features"]?.jsonArray
    val features = featuresJson?.map { parseFeature(it) } ?: emptyList()
    return GeoJsonFeatureCollection(features)
}

private fun parseFeature(json: JsonElement): GeoJsonFeature {
    val geometryJson = json.jsonObject["geometry"]
    val propertiesJson = json.jsonObject["properties"]?.jsonObject
    val id = json.jsonObject["id"]?.jsonPrimitive?.content

    val geometry =
        if (geometryJson != null && geometryJson !is JsonNull) {
            parseGeometry(geometryJson)
        } else {
            null
        }

    val properties =
        propertiesJson?.let {
            it.entries.associate { (key, value) ->
                val propertyValue =
                    when (value) {
                        is JsonPrimitive -> value.contentOrNull
                        is JsonNull -> null
                        else -> value.toString()
                    }
                key to propertyValue
            }
        }

    return GeoJsonFeature(geometry, properties, id)
}

internal fun parseGeometry(
    json: JsonElement,
    maxDepth: Int = GeoJsonParser.MAX_GEOMETRY_DEPTH,
): GeoJsonGeometry? {
    if (maxDepth < 0) return null
    val type = json.jsonObject["type"]?.jsonPrimitive?.content ?: return null
    val coordinates = json.jsonObject["coordinates"]?.jsonArray
    val geometries = json.jsonObject["geometries"]?.jsonArray

    return when (type) {
        "Point" -> coordinates?.let { GeoJsonPoint(parsePoint(it)) }
        "LineString" -> coordinates?.let { GeoJsonLineString(parseLineString(it)) }
        "Polygon" -> coordinates?.let { GeoJsonPolygon(parsePolygon(it)) }
        "MultiPoint" -> coordinates?.let { GeoJsonMultiPoint(parseMultiPoint(it)) }
        "MultiLineString" -> coordinates?.let { GeoJsonMultiLineString(parseMultiLineString(it)) }
        "MultiPolygon" -> coordinates?.let { GeoJsonMultiPolygon(parseMultiPolygon(it)) }
        "GeometryCollection" -> geometries?.let { GeoJsonGeometryCollection(parseGeometryCollection(it, maxDepth - 1)) }
        else -> null
    }
}

private fun parseCoordinates(coordinates: List<JsonElement>): Coordinates {
    val lng = coordinates[0].jsonPrimitive.content.toDouble()
    val lat = coordinates[1].jsonPrimitive.content.toDouble()
    val alt = if (coordinates.size > 2) coordinates[2].jsonPrimitive.content.toDouble() else null
    // Reject non-finite values (e.g. "Infinity"/"NaN", which String.toDouble() accepts) so poisoned
    // coordinates cannot flow unchecked into LatLng/LatLngBounds and corrupt rendering downstream.
    require(lng.isFinite() && lat.isFinite() && (alt == null || alt.isFinite())) {
        "GeoJSON coordinate contains a non-finite value"
    }
    return Coordinates(lat, lng, alt)
}

private fun parsePoint(coordinates: List<JsonElement>): Coordinates = parseCoordinates(coordinates)

private fun parseLineString(coordinates: List<JsonElement>): List<Coordinates> =
    coordinates.map {
        parseCoordinates(it.jsonArray.toList())
    }

private fun parsePolygon(coordinates: List<JsonElement>): List<List<Coordinates>> =
    coordinates.map {
        parseLineString(it.jsonArray.toList())
    }

private fun parseMultiPoint(coordinates: List<JsonElement>): List<Coordinates> =
    coordinates.map {
        parseCoordinates(it.jsonArray.toList())
    }

private fun parseMultiLineString(coordinates: List<JsonElement>): List<List<Coordinates>> =
    coordinates.map {
        parseLineString(it.jsonArray.toList())
    }

private fun parseMultiPolygon(coordinates: List<JsonElement>): List<List<List<Coordinates>>> =
    coordinates.map {
        parsePolygon(it.jsonArray.toList())
    }

private fun parseGeometryCollection(
    geometries: List<JsonElement>,
    maxDepth: Int,
): List<GeoJsonGeometry> =
    geometries.mapNotNull {
        parseGeometry(it, maxDepth)
    }
