package com.google.maps.android.data.parser.kml

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import nl.adaptivity.xmlutil.serialization.XmlElement
import nl.adaptivity.xmlutil.serialization.XmlSerialName
import nl.adaptivity.xmlutil.serialization.XmlValue

/**
 * This file contains the data classes for serializing KML files.
 * The structure is based on the KML 2.2 standard.
 *
 * This model is designed to be resilient to variations in KML files
 * by defining common but currently unused elements like Style and StyleMap,
 * preventing the parser from failing on them.
 */

private const val KML_NAMESPACE = "http://www.opengis.net/kml/2.2"

@Serializable
@XmlSerialName("altitudeMode", namespace = "http://www.google.com/kml/ext/2.2", prefix = "gx")
enum class AltitudeMode {
    @SerialName("relativeToGround")
    RELATIVE_TO_GROUND,

    @SerialName("absolute")
    ABSOLUTE,

    @SerialName("relativeToSeaFloor")
    RELATIVE_TO_SEA_FLOOR,

    @SerialName("clampToGround")
    CLAMP_TO_GROUND,

    @SerialName("clampToSeaFloor")
    CLAMP_TO_SEA_FLOOR;
}

sealed interface KmlFeature

@Serializable
@XmlSerialName("kml", namespace = KML_NAMESPACE, prefix = "")
data class Kml(
    @XmlElement(true)
    @XmlSerialName("Document", namespace = KML_NAMESPACE, prefix = "")
    val document: Document? = null,

    @XmlElement(true)
    @XmlSerialName("Folder", namespace = KML_NAMESPACE, prefix = "")
    val folder: Folder? = null,

    @XmlElement(true)
    @XmlSerialName("Placemark", namespace = KML_NAMESPACE, prefix = "")
    val placemark: Placemark? = null,

    // Include Style and StyleMap to prevent parsing errors on files that contain them.
    @XmlElement(true)
    @XmlSerialName("Style", namespace = KML_NAMESPACE, prefix = "")
    val style: Style? = null,

    @XmlElement(true)
    @XmlSerialName("StyleMap", namespace = KML_NAMESPACE, prefix = "")
    val styleMap: StyleMap? = null
)

fun Kml.findByPlacemarksById(id: String): List<Placemark> {
    return buildList {
        document?.placemarks?.filter { it.id == id }?.let { addAll(it) }
    }
}

@Serializable
@XmlSerialName("Document", namespace = KML_NAMESPACE, prefix = "")
data class Document(
    @XmlElement(true)
    @XmlSerialName("name", namespace = KML_NAMESPACE, prefix = "")
    val name: String? = null,

    @XmlElement(true)
    @XmlSerialName("description", namespace = KML_NAMESPACE, prefix = "")
    val description: String? = null,

    @XmlElement(true)
    @XmlSerialName("Folder", namespace = KML_NAMESPACE, prefix = "")
    val folders: List<Folder> = emptyList(),

    @XmlElement(true)
    @XmlSerialName("Placemark", namespace = KML_NAMESPACE, prefix = "")
    val placemarks: List<Placemark> = emptyList(),

    @XmlElement(true)
    @XmlSerialName("Style", namespace = KML_NAMESPACE, prefix = "")
    val styles: List<Style> = emptyList(),

    @XmlElement(true)
    @XmlSerialName("StyleMap", namespace = KML_NAMESPACE, prefix = "")
    val styleMaps: List<StyleMap> = emptyList()
) : KmlFeature

@Serializable
@XmlSerialName("Folder", namespace = KML_NAMESPACE, prefix = "")
data class Folder(
    @XmlElement(true)
    @XmlSerialName("name", namespace = KML_NAMESPACE, prefix = "")
    val name: String? = null,

    @XmlElement(true)
    @XmlSerialName("description", namespace = KML_NAMESPACE, prefix = "")
    val description: String? = null,

    @XmlElement(true)
    @XmlSerialName("Folder", namespace = KML_NAMESPACE, prefix = "")
    val folders: List<Folder> = emptyList(),

    @XmlElement(true)
    @XmlSerialName("Placemark", namespace = KML_NAMESPACE, prefix = "")
    val placemarks: List<Placemark> = emptyList()
) : KmlFeature

@Serializable
@XmlSerialName("Placemark", namespace = KML_NAMESPACE, prefix = "")
data class Placemark(
    val id: String? = null,

    @XmlElement(true)
    @XmlSerialName("name", namespace = KML_NAMESPACE, prefix = "")
    val name: String? = null,

    @XmlElement(true)
    @XmlSerialName("description", namespace = KML_NAMESPACE, prefix = "")
    val description: String? = null,

    @XmlElement(true)
    @XmlSerialName("styleUrl", namespace = KML_NAMESPACE, prefix = "")
    val styleUrl: String? = null,

    @XmlElement(true)
    @XmlSerialName("Point", namespace = KML_NAMESPACE, prefix = "")
    val point: Point? = null,

    @XmlElement(true)
    @XmlSerialName("LineString", namespace = KML_NAMESPACE, prefix = "")
    val lineString: LineString? = null,

    @XmlElement(true)
    @XmlSerialName("Polygon", namespace = KML_NAMESPACE, prefix = "")
    val polygon: Polygon? = null,

    @XmlElement(true)
    @XmlSerialName("MultiGeometry", namespace = KML_NAMESPACE, prefix = "")
    val multiGeometry: MultiGeometry? = null,

    @XmlSerialName("balloonVisibility", namespace = "http://www.google.com/kml/ext/2.2", prefix = "gx")
    val balloonVisibility: Int = 1,

    @XmlElement(true)
    @XmlSerialName("ExtendedData", namespace = KML_NAMESPACE, prefix = "")
    val extendedData: ExtendedData? = null
) : KmlFeature

@Serializable
@XmlSerialName("Point", namespace = KML_NAMESPACE, prefix = "")
data class Point(
    @XmlElement(true)
    @XmlSerialName("coordinates", namespace = KML_NAMESPACE, prefix = "")
    val coordinates: String,

    @XmlSerialName("altitudeMode", namespace = "http://www.google.com/kml/ext/2.2", prefix = "gx")
    val altitudeMode: AltitudeMode? = null
) {
    @Transient
    val latLngAlt = LatLngAlt.fromString(coordinates)
}

@Serializable
@XmlSerialName("LineString", namespace = KML_NAMESPACE, prefix = "")
data class LineString(
    @XmlElement(true)
    @XmlSerialName("coordinates", namespace = KML_NAMESPACE, prefix = "")
    val coordinates: String
)

@Serializable
@XmlSerialName("Polygon", namespace = KML_NAMESPACE, prefix = "")
data class Polygon(
    @XmlElement(true)
    @XmlSerialName("outerBoundaryIs", namespace = KML_NAMESPACE, prefix = "")
    val outerBoundaryIs: Boundary,

    @XmlElement(true)
    @XmlSerialName("innerBoundaryIs", namespace = KML_NAMESPACE, prefix = "")
    val innerBoundaryIs: List<Boundary> = emptyList()
)

@Serializable
data class Boundary(
    @XmlElement(true)
    @XmlSerialName("LinearRing", namespace = KML_NAMESPACE, prefix = "")
    val linearRing: LinearRing
)

@Serializable
data class LinearRing(
    @XmlElement(true)
    @XmlSerialName("coordinates", namespace = KML_NAMESPACE, prefix = "")
    val coordinates: String
)

@Serializable
@XmlSerialName("MultiGeometry", namespace = KML_NAMESPACE, prefix = "")
data class MultiGeometry(
    @XmlElement(true)
    @XmlSerialName("Point", namespace = KML_NAMESPACE, prefix = "")
    val points: List<Point> = emptyList(),

    @XmlElement(true)
    @XmlSerialName("LineString", namespace = KML_NAMESPACE, prefix = "")
    val lineStrings: List<LineString> = emptyList(),

    @XmlElement(true)
    @XmlSerialName("Polygon", namespace = KML_NAMESPACE, prefix = "")
    val polygons: List<Polygon> = emptyList(),

    @XmlElement(true)
    @XmlSerialName("MultiGeometry", namespace = KML_NAMESPACE, prefix = "")
    val multiGeometries: List<MultiGeometry> = emptyList()
)

@Serializable
@XmlSerialName("ExtendedData", namespace = KML_NAMESPACE, prefix = "")
data class ExtendedData(
    @XmlElement(true)
    @XmlSerialName("Data", namespace = KML_NAMESPACE, prefix = "")
    val data: List<Data> = emptyList()
)

@Serializable
@XmlSerialName("Data", namespace = KML_NAMESPACE, prefix = "")
data class Data(
    @XmlSerialName("name")
    val name: String? = null, // name is an attribute

    @XmlElement(true)
    @XmlSerialName("value", namespace = KML_NAMESPACE, prefix = "")
    val value: String? = null,
)

@Serializable
@XmlSerialName("Style", namespace = KML_NAMESPACE, prefix = "")
data class Style(
    val id: String? = null,
    @XmlElement(true) @XmlSerialName("LineStyle", namespace = KML_NAMESPACE, prefix = "") val lineStyle: LineStyle? = null,
    @XmlElement(true) @XmlSerialName("PolyStyle", namespace = KML_NAMESPACE, prefix = "") val polyStyle: PolyStyle? = null
) : KmlFeature

@Serializable
@XmlSerialName("StyleMap", namespace = KML_NAMESPACE, prefix = "")
data class StyleMap(
    val id: String? = null,
    @XmlElement(true) @XmlSerialName("Pair", namespace = KML_NAMESPACE, prefix = "") val pairs: List<Pair> = emptyList()
) : KmlFeature

@Serializable
@XmlSerialName("LineStyle")
data class LineStyle(
    @XmlSerialName("color")
    val color: String? = null,

    @XmlSerialName("width")
    val width: Float? = null
)

@Serializable
@XmlSerialName("PolyStyle", namespace = KML_NAMESPACE, prefix = "")
data class PolyStyle(
    @XmlElement(true) @XmlSerialName("color", namespace = KML_NAMESPACE, prefix = "") val color: String? = null,
    @XmlElement(true) @XmlSerialName("fill", namespace = KML_NAMESPACE, prefix = "") val fill: String? = null,
    @XmlElement(true) @XmlSerialName("outline", namespace = KML_NAMESPACE, prefix = "") val outline: String? = null
)

@Serializable
@XmlSerialName("Pair", namespace = KML_NAMESPACE, prefix = "")
data class Pair(
    @XmlElement(true) @XmlSerialName("key", namespace = KML_NAMESPACE, prefix = "") val key: String? = null,
    @XmlElement(true) @XmlSerialName("styleUrl", namespace = KML_NAMESPACE, prefix = "") val styleUrl: String? = null
)

@Serializable
@XmlSerialName("gx:BalloonVisibility", namespace = "http://www.google.com/kml/ext/2.2", prefix = "gx")
data class BalloonVisibility(
    val value: Int = 1
)

internal fun String.stripWhitespace(): String = filter { !it.isWhitespace() }

internal fun String.simplify(): String = this.stripWhitespace().lowercase()