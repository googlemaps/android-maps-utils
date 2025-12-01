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

class GeoJsonParser {
    fun parse(inputStream: InputStream): GeoJsonObject? {
        val json = inputStream.bufferedReader().use { it.readText() }
        val jsonElement = Json.parseToJsonElement(json)

        return when (jsonElement.jsonObject["type"]?.jsonPrimitive?.content) {
            "FeatureCollection" -> parseFeatureCollection(jsonElement)
            "Feature" -> parseFeature(jsonElement)
            "Point", "LineString", "Polygon", "MultiPoint", "MultiLineString", "MultiPolygon", "GeometryCollection" -> parseGeometry(
                jsonElement
            )

            else -> null
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

    val geometry = if (geometryJson != null && geometryJson !is JsonNull) {
        parseGeometry(geometryJson)
    } else {
        null
    }

    val properties = propertiesJson?.let {
        it.entries.associate { (key, value) ->
            val propertyValue = when (value) {
                is JsonPrimitive -> value.contentOrNull
                is JsonNull -> null
                else -> value.toString()
            }
            key to propertyValue
        }
    }

    return GeoJsonFeature(geometry, properties, id)
}

private fun parseGeometry(json: JsonElement): GeoJsonGeometry? {
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
        "GeometryCollection" -> geometries?.let { GeoJsonGeometryCollection(parseGeometryCollection(it)) }
        else -> null
    }
}

private fun parseCoordinates(coordinates: List<JsonElement>): Coordinates {
    val lng = coordinates[0].jsonPrimitive.content.toDouble()
    val lat = coordinates[1].jsonPrimitive.content.toDouble()
    val alt = if (coordinates.size > 2) coordinates[2].jsonPrimitive.content.toDouble() else null
    return Coordinates(lat, lng, alt)
}

private fun parsePoint(coordinates: List<JsonElement>): Coordinates {
    return parseCoordinates(coordinates)
}

private fun parseLineString(coordinates: List<JsonElement>): List<Coordinates> {
    return coordinates.map {
        parseCoordinates(it.jsonArray.toList())
    }
}

private fun parsePolygon(coordinates: List<JsonElement>): List<List<Coordinates>> {
    return coordinates.map {
        parseLineString(it.jsonArray.toList())
    }
}

private fun parseMultiPoint(coordinates: List<JsonElement>): List<Coordinates> {
    return coordinates.map {
        parseCoordinates(it.jsonArray.toList())
    }
}

private fun parseMultiLineString(coordinates: List<JsonElement>): List<List<Coordinates>> {
    return coordinates.map {
        parseLineString(it.jsonArray.toList())
    }
}

private fun parseMultiPolygon(coordinates: List<JsonElement>): List<List<List<Coordinates>>> {
    return coordinates.map {
        parsePolygon(it.jsonArray.toList())
    }
}

private fun parseGeometryCollection(geometries: List<JsonElement>): List<GeoJsonGeometry> {
    return geometries.mapNotNull {
        parseGeometry(it)
    }
}
