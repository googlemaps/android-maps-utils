package com.google.maps.android.data.parser.kml

import com.google.maps.android.data.parser.Feature
import com.google.maps.android.data.parser.GeoData
import com.google.maps.android.data.parser.GeoFileParser
import com.google.maps.android.data.parser.Geometry
import kotlinx.serialization.decodeFromString
import nl.adaptivity.xmlutil.serialization.XML
import java.io.InputStream
import java.nio.charset.StandardCharsets

class KmlParser : GeoFileParser {
    private val xml = XML {
        defaultPolicy {
            ignoreUnknownChildren()
            isCollectingNSAttributes = true
        }
    }

    override fun parse(inputStream: InputStream): GeoData {
        return transformToGeoData(parseAsKml(inputStream))
    }

    fun parseAsKml(inputStream: InputStream): Kml {
        val xmlContent = inputStream.bufferedReader(StandardCharsets.UTF_8).use { it.readText() }
        val kml = xml.decodeFromString<Kml>(xmlContent)
        return kml
    }

    fun transformToGeoData(kml: Kml): GeoData {
        val features = mutableListOf<Feature>()
        kml.document?.let { features.addAll(transformContainer(it)) }
        kml.folder?.let { features.addAll(transformContainer(it)) }
        kml.placemark?.let { transformPlacemark(it)?.let { f -> features.add(f) } }
        return GeoData(features)
    }

    private fun transformContainer(container: Any): List<Feature> {
        val features = mutableListOf<Feature>()
        when (container) {
            is Document -> {
                container.placemarks.forEach { transformPlacemark(it)?.let { f -> features.add(f) } }
                container.folders.forEach { features.addAll(transformContainer(it)) }
            }
            is Folder -> {
                container.placemarks.forEach { transformPlacemark(it)?.let { f -> features.add(f) } }
                container.folders.forEach { features.addAll(transformContainer(it)) }
            }
        }
        return features
    }

    private fun transformPlacemark(placemark: Placemark): Feature? {
        val geometry = parseGeometry(placemark)
        val properties = parseProperties(placemark)
        return if (geometry != null) Feature(geometry, properties) else null
    }

    private fun parseProperties(placemark: Placemark): Map<String, Any> {
        val properties = mutableMapOf<String, Any>()
        placemark.name?.let { properties["name"] = it.trim() }
        placemark.description?.let { properties["description"] = it.trim() }
        placemark.balloonVisibility.let { properties["gx:balloonVisibility"] = it }
        placemark.extendedData?.data?.forEach {
            val value = it.value?.trim() ?: ""
            it.name?.let { name -> properties[name] = value }
        }
        return properties
    }

    private fun parseGeometry(placemark: Placemark): Geometry? {
        return when {
            placemark.point != null -> parsePoint(placemark.point.coordinates)
            placemark.lineString != null -> parseLineString(placemark.lineString.coordinates)
            placemark.polygon != null -> parsePolygon(placemark.polygon)
            placemark.multiGeometry != null -> parseMultiGeometry(placemark.multiGeometry)
            else -> null
        }
    }

    private fun parsePoint(coordinates: String): Geometry.Point {
        val coords = parseCoordinates(coordinates).first()
        return Geometry.Point(coords.get(1), coords.get(0), coords.getOrNull(2))
    }

    private fun parseLineString(coordinates: String): Geometry.LineString {
        val points = parseCoordinates(coordinates).map {
            Geometry.Point(it.get(1), it.get(0), it.getOrNull(2))
        }
        return Geometry.LineString(points)
    }

    private fun parsePolygon(polygon: Polygon): Geometry.Polygon {
        val shell = parseCoordinates(polygon.outerBoundaryIs.linearRing.coordinates).map { coordinate ->
            Geometry.Point(coordinate[1], coordinate[0], coordinate.getOrNull(2))
        }
        val holes = polygon.innerBoundaryIs.map {
            parseCoordinates(it.linearRing.coordinates).map { coordinate ->
                Geometry.Point(coordinate[1], coordinate[0], coordinate.getOrNull(2))
            }
        }
        return Geometry.Polygon(shell, holes)
    }

    private fun parseMultiGeometry(multiGeometry: MultiGeometry): Geometry.GeometryCollection {
        val geometries = mutableListOf<Geometry>()
        multiGeometry.points.forEach { geometries.add(parsePoint(it.coordinates)) }
        multiGeometry.lineStrings.forEach { geometries.add(parseLineString(it.coordinates)) }
        multiGeometry.polygons.forEach { geometries.add(parsePolygon(it)) }
        multiGeometry.multiGeometries.forEach { geometries.add(parseMultiGeometry(it)) }
        return Geometry.GeometryCollection(geometries)
    }

    private fun parseCoordinates(coordinatesStr: String): List<List<Double>> {
        return coordinatesStr.trim().split(Regex("\\s+")).map {
            it.split(",").map { s -> s.toDouble() }
        }
    }
}
