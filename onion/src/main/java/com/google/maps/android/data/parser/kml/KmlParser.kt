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

    fun parseAsKml(inputStream: InputStream): Kml {
        val xmlContent = inputStream.bufferedReader(StandardCharsets.UTF_8).use { it.readText() }
        return xml.decodeFromString<Kml>(xmlContent)
    }

    fun parse(inputStream: InputStream): Kml {
        return parseAsKml(inputStream)
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

    private fun parsePoint(latLngAlt: LatLngAlt): Geometry.Point {
        return Geometry.Point(latLngAlt.latitude, latLngAlt.longitude, latLngAlt.altitude)
    }

    private fun parseLineString(coordinates: List<LatLngAlt>): Geometry.LineString {
        val points = coordinates.map {
            Geometry.Point(it.latitude, it.longitude, it.altitude)
        }
        return Geometry.LineString(points)
    }

    private fun parsePolygon(polygon: Polygon): Geometry.Polygon {
        val shell = polygon.outerBoundaryIs.linearRing.coordinates.map { coordinate ->
            Geometry.Point(coordinate.latitude, coordinate.longitude, coordinate.altitude)
        }
        val holes = polygon.innerBoundaryIs.map {
            it.linearRing.coordinates.map { coordinate ->
                Geometry.Point(coordinate.latitude, coordinate.longitude, coordinate.altitude)
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
}
