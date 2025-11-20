package com.google.maps.android.data.parser.geojson

import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import org.json.JSONArray
import org.json.JSONObject

class GeoJsonParser(private val jsonObject: JSONObject) {

    fun parse(): GeoJson {
        return when (val type = jsonObject.getString("type")) {
            "Feature" -> parseFeature(jsonObject)
            "FeatureCollection" -> parseFeatureCollection(jsonObject)
            "Point", "MultiPoint", "LineString", "MultiLineString", "Polygon", "MultiPolygon", "GeometryCollection" -> parseGeometry(jsonObject)
            else -> throw IllegalArgumentException("Unknown GeoJSON type: $type")
        }
    }

    private fun parseFeature(jsonObject: JSONObject): GeoJson.Feature {
        val geometry = if (jsonObject.has("geometry") && !jsonObject.isNull("geometry")) {
            parseGeometry(jsonObject.getJSONObject("geometry"))
        } else {
            null
        }
        val properties = if (jsonObject.has("properties") && !jsonObject.isNull("properties")) {
            parseProperties(jsonObject.getJSONObject("properties"))
        } else {
            emptyMap()
        }
        val id = if (jsonObject.has("id")) jsonObject.getString("id") else null
        val boundingBox = if (jsonObject.has("bbox")) parseBoundingBox(jsonObject.getJSONArray("bbox")) else null
        return GeoJson.Feature(geometry, properties, id, boundingBox)
    }

    private fun parseFeatureCollection(jsonObject: JSONObject): GeoJson.FeatureCollection {
        val features = mutableListOf<GeoJson.Feature>()
        val featuresArray = jsonObject.getJSONArray("features")
        for (i in 0 until featuresArray.length()) {
            features.add(parseFeature(featuresArray.getJSONObject(i)))
        }
        val boundingBox = if (jsonObject.has("bbox")) parseBoundingBox(jsonObject.getJSONArray("bbox")) else null
        return GeoJson.FeatureCollection(features, boundingBox)
    }

    private fun parseGeometry(jsonObject: JSONObject): GeoJson {
        return when (val type = jsonObject.getString("type")) {
            "Point" -> parsePoint(jsonObject.getJSONArray("coordinates"))
            "MultiPoint" -> parseMultiPoint(jsonObject.getJSONArray("coordinates"))
            "LineString" -> parseLineString(jsonObject.getJSONArray("coordinates"))
            "MultiLineString" -> parseMultiLineString(jsonObject.getJSONArray("coordinates"))
            "Polygon" -> parsePolygon(jsonObject.getJSONArray("coordinates"))
            "MultiPolygon" -> parseMultiPolygon(jsonObject.getJSONArray("coordinates"))
            "GeometryCollection" -> parseGeometryCollection(jsonObject.getJSONArray("geometries"))
            else -> throw IllegalArgumentException("Unknown GeoJSON geometry type: $type")
        }
    }

    private fun parseProperties(jsonObject: JSONObject): Map<String, String?> {
        val properties = mutableMapOf<String, String?>()
        val keys = jsonObject.keys()
        while (keys.hasNext()) {
            val key = keys.next()
            properties[key] = if (jsonObject.isNull(key)) null else jsonObject.getString(key)
        }
        return properties
    }

    private fun parseBoundingBox(jsonArray: JSONArray): LatLngBounds {
        return LatLngBounds(
            LatLng(jsonArray.getDouble(1), jsonArray.getDouble(0)),
            LatLng(jsonArray.getDouble(3), jsonArray.getDouble(2))
        )
    }

    private fun parsePoint(jsonArray: JSONArray): GeoJson.Point {
        return GeoJson.Point(LatLng(jsonArray.getDouble(1), jsonArray.getDouble(0)))
    }

    private fun parseMultiPoint(jsonArray: JSONArray): GeoJson.MultiPoint {
        val points = mutableListOf<GeoJson.Point>()
        for (i in 0 until jsonArray.length()) {
            points.add(parsePoint(jsonArray.getJSONArray(i)))
        }
        return GeoJson.MultiPoint(points)
    }

    private fun parseLineString(jsonArray: JSONArray): GeoJson.LineString {
        val coordinates = mutableListOf<LatLng>()
        for (i in 0 until jsonArray.length()) {
            val coordinate = jsonArray.getJSONArray(i)
            coordinates.add(LatLng(coordinate.getDouble(1), coordinate.getDouble(0)))
        }
        return GeoJson.LineString(coordinates)
    }

    private fun parseMultiLineString(jsonArray: JSONArray): GeoJson.MultiLineString {
        val lineStrings = mutableListOf<GeoJson.LineString>()
        for (i in 0 until jsonArray.length()) {
            lineStrings.add(parseLineString(jsonArray.getJSONArray(i)))
        }
        return GeoJson.MultiLineString(lineStrings)
    }

    private fun parsePolygon(jsonArray: JSONArray): GeoJson.Polygon {
        val coordinates = mutableListOf<List<LatLng>>()
        for (i in 0 until jsonArray.length()) {
            val lineString = jsonArray.getJSONArray(i)
            val lineStringCoordinates = mutableListOf<LatLng>()
            for (j in 0 until lineString.length()) {
                val coordinate = lineString.getJSONArray(j)
                lineStringCoordinates.add(LatLng(coordinate.getDouble(1), coordinate.getDouble(0)))
            }
            coordinates.add(lineStringCoordinates)
        }
        return GeoJson.Polygon(coordinates)
    }

    private fun parseMultiPolygon(jsonArray: JSONArray): GeoJson.MultiPolygon {
        val polygons = mutableListOf<GeoJson.Polygon>()
        for (i in 0 until jsonArray.length()) {
            polygons.add(parsePolygon(jsonArray.getJSONArray(i)))
        }
        return GeoJson.MultiPolygon(polygons)
    }

    private fun parseGeometryCollection(jsonArray: JSONArray): GeoJson.GeometryCollection {
        val geometries = mutableListOf<GeoJson>()
        for (i in 0 until jsonArray.length()) {
            geometries.add(parseGeometry(jsonArray.getJSONObject(i)))
        }
        return GeoJson.GeometryCollection(geometries)
    }
}
