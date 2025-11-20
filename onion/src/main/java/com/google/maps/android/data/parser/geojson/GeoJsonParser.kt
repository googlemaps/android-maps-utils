package com.google.maps.android.data.parser.geojson

import com.google.maps.android.data.parser.Feature
import com.google.maps.android.data.parser.GeoData
import com.google.maps.android.data.parser.GeoFileParser
import com.google.maps.android.data.parser.Geometry
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.io.InputStream

class GeoJsonParser : GeoFileParser {
    override fun parse(inputStream: InputStream): GeoData {
        val json = inputStream.bufferedReader().use { it.readText() }
        val jsonElement = Json.parseToJsonElement(json)
        val features = mutableListOf<Feature>()

        if (jsonElement.toString().startsWith("[")) {
            // This is a flat array of coordinates, not a valid GeoJSON object
            jsonElement.jsonArray.forEach {
                val lat = it.jsonObject["lat"]?.jsonPrimitive?.content?.toDouble()
                val lon = it.jsonObject["lng"]?.jsonPrimitive?.content?.toDouble()
                if (lat != null && lon != null) {
                    features.add(Feature(Geometry.Point(lat, lon, null)))
                }
            }
        } else {
            when (jsonElement.jsonObject["type"]?.jsonPrimitive?.content) {
                "FeatureCollection" -> {
                    jsonElement.jsonObject["features"]?.jsonArray?.forEach { featureJson ->
                        parseFeature(featureJson)?.let { features.add(it) }
                    }
                }
                "Feature" -> {
                    parseFeature(jsonElement)?.let { features.add(it) }
                }
            }
        }

        return GeoData(features)
    }
}

private fun parseFeature(json: JsonElement): Feature? {
    val geometryJson = json.jsonObject["geometry"]
    if (geometryJson == null || geometryJson.toString() == "null") {
        return null
    }
    val propertiesJson = json.jsonObject["properties"]?.jsonObject
    val geometry = parseGeometry(geometryJson) ?: return null
    val properties = propertiesJson?.let {
        it.entries.associate { (key, value) -> key to value.jsonPrimitive.content }
    } ?: emptyMap()
    return Feature(geometry, properties)
}

private fun parseGeometry(json: JsonElement): Geometry? {
    val type = json.jsonObject["type"]?.jsonPrimitive?.content ?: return null
    val coordinates = json.jsonObject["coordinates"]?.jsonArray
    val geometries = json.jsonObject["geometries"]?.jsonArray
    return when (type) {
        "Point" -> parsePoint(coordinates!!)
        "LineString" -> parseLineString(coordinates!!)
        "Polygon" -> parsePolygon(coordinates!!)
        "MultiPoint" -> parseMultiPoint(coordinates!!)
        "MultiLineString" -> parseMultiLineString(coordinates!!)
        "MultiPolygon" -> parseMultiPolygon(coordinates!!)
        "GeometryCollection" -> parseGeometryCollection(geometries!!)
        else -> null
    }
}

private fun parsePoint(coordinates: List<JsonElement>): Geometry.Point {
    val lon = coordinates[0].jsonPrimitive.content.toDouble()
    val lat = coordinates[1].jsonPrimitive.content.toDouble()
    val alt = if (coordinates.size > 2) coordinates[2].jsonPrimitive.content.toDouble() else null
    return Geometry.Point(lat, lon, alt)
}

private fun parseLineString(coordinates: List<JsonElement>): Geometry.LineString {
    val points = coordinates.map {
        parsePoint(it.jsonArray.toList())
    }
    return Geometry.LineString(points)
}

private fun parsePolygon(coordinates: List<JsonElement>): Geometry.Polygon {
    val shell = coordinates[0].jsonArray.map {
        parsePoint(it.jsonArray.toList())
    }
    val holes = if (coordinates.size > 1) {
        coordinates.subList(1, coordinates.size).map {
            it.jsonArray.map { point ->
                parsePoint(point.jsonArray.toList())
            }
        }
    } else {
        emptyList()
    }
    return Geometry.Polygon(shell, holes)
}

private fun parseMultiPoint(coordinates: List<JsonElement>): Geometry.LineString {
    val points = coordinates.map {
        parsePoint(it.jsonArray.toList())
    }
    return Geometry.LineString(points)
}

private fun parseMultiLineString(coordinates: List<JsonElement>): Geometry.LineString {
    val points = coordinates.flatMap {
        it.jsonArray.map { point ->
            parsePoint(point.jsonArray.toList())
        }
    }
    return Geometry.LineString(points)
}

private fun parseMultiPolygon(coordinates: List<JsonElement>): Geometry.Polygon {
    val shell = coordinates[0].jsonArray[0].jsonArray.map {
        parsePoint(it.jsonArray.toList())
    }
    val holes = if (coordinates[0].jsonArray.size > 1) {
        coordinates[0].jsonArray.subList(1, coordinates[0].jsonArray.size).map {
            it.jsonArray.map { point ->
                parsePoint(point.jsonArray.toList())
            }
        }
    } else {
        emptyList()
    }
    return Geometry.Polygon(shell, holes)
}

private fun parseGeometryCollection(geometries: List<JsonElement>): Geometry.GeometryCollection {
    val parsedGeometries = mutableListOf<Geometry>()
    geometries.forEach {
        parseGeometry(it)?.let { geometry ->
            parsedGeometries.add(geometry)
        }
    }
    return Geometry.GeometryCollection(parsedGeometries)
}
