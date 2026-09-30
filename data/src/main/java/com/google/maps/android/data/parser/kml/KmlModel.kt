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
package com.google.maps.android.data.parser.kml

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import nl.adaptivity.xmlutil.serialization.XmlElement
import nl.adaptivity.xmlutil.serialization.XmlSerialName

/**
 * This file contains the data classes for serializing KML files.
 * The structure is based on the KML 2.2 standard.
 *
 * This model is designed to be resilient to variations in KML files
 * by defining common but currently unused elements like Style and StyleMap,
 * preventing the parser from failing on them.
 */

private const val KML_NAMESPACE = "http://www.opengis.net/kml/2.2"
private const val GOOGLE_KML_NAMESPACE = "http://www.google.com/kml/ext/2.2"

@Serializable
@XmlSerialName("altitudeMode", namespace = GOOGLE_KML_NAMESPACE, prefix = "gx")
public enum class AltitudeMode {
    @SerialName("relativeToGround")
    RELATIVE_TO_GROUND,

    @SerialName("absolute")
    ABSOLUTE,

    @SerialName("relativeToSeaFloor")
    RELATIVE_TO_SEA_FLOOR,

    @SerialName("clampToGround")
    CLAMP_TO_GROUND,

    @SerialName("clampToSeaFloor")
    CLAMP_TO_SEA_FLOOR,
}

public sealed interface KmlFeature

@Serializable
@XmlSerialName("kml", namespace = KML_NAMESPACE, prefix = "")
public data class Kml(
    @XmlElement(true)
    @XmlSerialName("Document", namespace = KML_NAMESPACE, prefix = "")
    public val document: Document? = null,
    @XmlElement(true)
    @XmlSerialName("Folder", namespace = KML_NAMESPACE, prefix = "")
    public val folder: Folder? = null,
    @XmlElement(true)
    @XmlSerialName("Placemark", namespace = KML_NAMESPACE, prefix = "")
    public val placemark: Placemark? = null,
    // Include Style and StyleMap to prevent parsing errors on files that contain them.
    @XmlElement(true)
    @XmlSerialName("Style", namespace = KML_NAMESPACE, prefix = "")
    public val style: Style? = null,
    @XmlElement(true)
    @XmlSerialName("StyleMap", namespace = KML_NAMESPACE, prefix = "")
    public val styleMap: StyleMap? = null,
    @XmlElement(true)
    @XmlSerialName("GroundOverlay", namespace = KML_NAMESPACE, prefix = "")
    public val groundOverlay: GroundOverlay? = null,
    @kotlinx.serialization.Transient
    public val images: Map<String, android.graphics.Bitmap> = emptyMap(),
)

@Serializable
@XmlSerialName("GroundOverlay", namespace = KML_NAMESPACE, prefix = "")
public data class GroundOverlay(
    @XmlElement(true)
    @XmlSerialName("name", namespace = KML_NAMESPACE, prefix = "")
    public val name: String? = null,
    @XmlElement(true)
    @XmlSerialName("drawOrder", namespace = KML_NAMESPACE, prefix = "")
    public val drawOrder: Int? = null,
    @XmlElement(true)
    @XmlSerialName("visibility", namespace = KML_NAMESPACE, prefix = "")
    @Serializable(with = BooleanSerializer::class)
    public val visibility: Boolean = true,
    @XmlElement(true)
    @XmlSerialName("color", namespace = KML_NAMESPACE, prefix = "")
    @Serializable(with = ColorSerializer::class)
    public val color: Int? = null,
    @XmlElement(true)
    @XmlSerialName("Icon", namespace = KML_NAMESPACE, prefix = "")
    public val icon: Icon? = null,
    @XmlElement(true)
    @XmlSerialName("LatLonBox", namespace = KML_NAMESPACE, prefix = "")
    public val latLonBox: LatLonBox? = null,
    @XmlElement(true)
    @XmlSerialName("LatLonQuad", namespace = KML_NAMESPACE, prefix = "")
    public val latLonQuad: LatLonQuad? = null,
)

@Serializable
@XmlSerialName("Icon", namespace = KML_NAMESPACE, prefix = "")
public data class Icon(
    @XmlElement(true)
    @XmlSerialName("href", namespace = KML_NAMESPACE, prefix = "")
    public val href: String? = null,
)

@Serializable
@XmlSerialName("LatLonBox", namespace = KML_NAMESPACE, prefix = "")
public data class LatLonBox(
    @XmlElement(true)
    @XmlSerialName("north", namespace = KML_NAMESPACE, prefix = "")
    public val north: Double,
    @XmlElement(true)
    @XmlSerialName("south", namespace = KML_NAMESPACE, prefix = "")
    public val south: Double,
    @XmlElement(true)
    @XmlSerialName("east", namespace = KML_NAMESPACE, prefix = "")
    public val east: Double,
    @XmlElement(true)
    @XmlSerialName("west", namespace = KML_NAMESPACE, prefix = "")
    public val west: Double,
    @XmlElement(true)
    @XmlSerialName("rotation", namespace = KML_NAMESPACE, prefix = "")
    public val rotation: Double? = null,
)

@Serializable
@XmlSerialName("LatLonQuad", namespace = KML_NAMESPACE, prefix = "")
public data class LatLonQuad(
    @XmlElement(true)
    @XmlSerialName("coordinates", namespace = KML_NAMESPACE, prefix = "")
    @Serializable(with = LatLngAltListSerializer::class)
    public val coordinates: List<LatLngAlt> = emptyList(),
)



public fun Kml.findByPlacemarksById(id: String): List<Placemark> =
    buildList {
        document?.placemarks?.filter { it.id == id }?.let { addAll(it) }
    }

@Serializable
@XmlSerialName("Document", namespace = KML_NAMESPACE, prefix = "")
public data class Document(
    @XmlElement(true)
    @XmlSerialName("name", namespace = KML_NAMESPACE, prefix = "")
    public val name: String? = null,
    @XmlElement(true)
    @XmlSerialName("description", namespace = KML_NAMESPACE, prefix = "")
    public val description: String? = null,
    @XmlElement(true)
    @XmlSerialName("Folder", namespace = KML_NAMESPACE, prefix = "")
    public val folders: List<Folder> = emptyList(),
    @XmlElement(true)
    @XmlSerialName("Placemark", namespace = KML_NAMESPACE, prefix = "")
    public val placemarks: List<Placemark> = emptyList(),
    @XmlElement(true)
    @XmlSerialName("Style", namespace = KML_NAMESPACE, prefix = "")
    public val styles: List<Style> = emptyList(),
    @XmlElement(true)
    @XmlSerialName("StyleMap", namespace = KML_NAMESPACE, prefix = "")
    public val styleMaps: List<StyleMap> = emptyList(),
    @XmlElement(true)
    @XmlSerialName("GroundOverlay", namespace = KML_NAMESPACE, prefix = "")
    public val groundOverlays: List<GroundOverlay> = emptyList(),
) : KmlFeature

@Serializable
@XmlSerialName("Folder", namespace = KML_NAMESPACE, prefix = "")
public data class Folder(
    @XmlElement(true)
    @XmlSerialName("name", namespace = KML_NAMESPACE, prefix = "")
    public val name: String? = null,
    @XmlElement(true)
    @XmlSerialName("description", namespace = KML_NAMESPACE, prefix = "")
    public val description: String? = null,
    @XmlElement(true)
    @XmlSerialName("visibility", namespace = KML_NAMESPACE, prefix = "")
    @Serializable(with = BooleanSerializer::class)
    public val visibility: Boolean = true,
    @XmlElement(true)
    @XmlSerialName("Folder", namespace = KML_NAMESPACE, prefix = "")
    public val folders: List<Folder> = emptyList(),
    @XmlElement(true)
    @XmlSerialName("Placemark", namespace = KML_NAMESPACE, prefix = "")
    public val placemarks: List<Placemark> = emptyList(),
    @XmlElement(true)
    @XmlSerialName("GroundOverlay", KML_NAMESPACE)
    public val groundOverlays: List<GroundOverlay> = emptyList(),
) : KmlFeature

@Serializable
@XmlSerialName("Placemark", namespace = KML_NAMESPACE, prefix = "")
public data class Placemark(
    public val id: String? = null,
    @XmlElement(true)
    @XmlSerialName("name", namespace = KML_NAMESPACE, prefix = "")
    public val name: String? = null,
    @XmlElement(true)
    @XmlSerialName("description", namespace = KML_NAMESPACE, prefix = "")
    public val description: String? = null,
    @XmlElement(true)
    @XmlSerialName("styleUrl", namespace = KML_NAMESPACE, prefix = "")
    public val styleUrl: String? = null,
    @XmlElement(true)
    @XmlSerialName("Point", namespace = KML_NAMESPACE, prefix = "")
    public val point: Point? = null,
    @XmlElement(true)
    @XmlSerialName("LineString", namespace = KML_NAMESPACE, prefix = "")
    public val lineString: LineString? = null,
    @XmlElement(true)
    @XmlSerialName("Polygon", namespace = KML_NAMESPACE, prefix = "")
    public val polygon: Polygon? = null,
    @XmlElement(true)
    @XmlSerialName("MultiGeometry", namespace = KML_NAMESPACE, prefix = "")
    public val multiGeometry: MultiGeometry? = null,
    @XmlElement(true)
    @XmlSerialName("Track", namespace = GOOGLE_KML_NAMESPACE, prefix = "gx")
    public val track: Track? = null,
    @XmlElement(true)
    @XmlSerialName("MultiTrack", namespace = GOOGLE_KML_NAMESPACE, prefix = "gx")
    public val multiTrack: MultiTrack? = null,
    @XmlSerialName("balloonVisibility", namespace = GOOGLE_KML_NAMESPACE, prefix = "gx")
    public val balloonVisibility: Int = 1,
    @XmlElement(true)
    @XmlSerialName("Style", namespace = KML_NAMESPACE, prefix = "")
    public val style: Style? = null,
    @XmlElement(true)
    @XmlSerialName("ExtendedData", namespace = KML_NAMESPACE, prefix = "")
    public val extendedData: ExtendedData? = null,
) : KmlFeature

@Serializable
@XmlSerialName("Track", namespace = GOOGLE_KML_NAMESPACE, prefix = "gx")
public data class Track(
    @XmlElement(true)
    @XmlSerialName("when", namespace = KML_NAMESPACE, prefix = "")
    public val whens: List<String> = emptyList(),
    @XmlElement(true)
    @XmlSerialName("coord", namespace = GOOGLE_KML_NAMESPACE, prefix = "gx")
    public val coords: List<String> = emptyList(),
    @XmlElement(true)
    @XmlSerialName("altitudeMode", namespace = GOOGLE_KML_NAMESPACE, prefix = "gx")
    public val altitudeMode: AltitudeMode? = null,
)

@Serializable
@XmlSerialName("MultiTrack", namespace = GOOGLE_KML_NAMESPACE, prefix = "gx")
public data class MultiTrack(
    @XmlElement(true)
    @XmlSerialName("altitudeMode", namespace = GOOGLE_KML_NAMESPACE, prefix = "gx")
    public val altitudeMode: AltitudeMode? = null,
    @XmlElement(true)
    @XmlSerialName("Track", namespace = GOOGLE_KML_NAMESPACE, prefix = "gx")
    public val tracks: List<Track> = emptyList(),
)

@Serializable
@XmlSerialName("Point", namespace = KML_NAMESPACE, prefix = "")
public data class Point(
    @XmlElement(true)
    @XmlSerialName("coordinates", namespace = KML_NAMESPACE, prefix = "")
    public val coordinates: LatLngAlt,
    @XmlSerialName("altitudeMode", namespace = GOOGLE_KML_NAMESPACE, prefix = "gx")
    public val altitudeMode: AltitudeMode? = null,
)

@Serializable
@XmlSerialName("LineString", namespace = KML_NAMESPACE, prefix = "")
public data class LineString(
    @XmlElement(true)
    @XmlSerialName("coordinates", namespace = KML_NAMESPACE, prefix = "")
    @Serializable(with = LatLngAltListSerializer::class)
    public val coordinates: List<LatLngAlt>,
)

@Serializable
@XmlSerialName("Polygon", namespace = KML_NAMESPACE, prefix = "")
public data class Polygon(
    @XmlElement(true)
    @XmlSerialName("outerBoundaryIs", namespace = KML_NAMESPACE, prefix = "")
    public val outerBoundaryIs: Boundary,
    @XmlElement(true)
    @XmlSerialName("innerBoundaryIs", namespace = KML_NAMESPACE, prefix = "")
    public val innerBoundaryIs: List<Boundary> = emptyList(),
    @XmlElement(true)
    @XmlSerialName("extrude", namespace = KML_NAMESPACE, prefix = "")
    @Serializable(with = BooleanSerializer::class)
    public val extrude: Boolean = false,
    @XmlElement(true)
    @XmlSerialName("altitudeMode", namespace = GOOGLE_KML_NAMESPACE, prefix = "gx")
    public val altitudeMode: AltitudeMode? = null,
)

@Serializable
public data class Boundary(
    @XmlElement(true)
    @XmlSerialName("LinearRing", namespace = KML_NAMESPACE, prefix = "")
    public val linearRing: LinearRing,
)

@Serializable
public data class LinearRing(
    @XmlElement(true)
    @XmlSerialName("coordinates", namespace = KML_NAMESPACE, prefix = "")
    @Serializable(with = LatLngAltListSerializer::class)
    public val coordinates: List<LatLngAlt>,
)

@Serializable
@XmlSerialName("MultiGeometry", namespace = KML_NAMESPACE, prefix = "")
public data class MultiGeometry(
    @XmlElement(true)
    @XmlSerialName("Point", namespace = KML_NAMESPACE, prefix = "")
    public val points: List<Point> = emptyList(),
    @XmlElement(true)
    @XmlSerialName("LineString", namespace = KML_NAMESPACE, prefix = "")
    public val lineStrings: List<LineString> = emptyList(),
    @XmlElement(true)
    @XmlSerialName("Polygon", namespace = KML_NAMESPACE, prefix = "")
    public val polygons: List<Polygon> = emptyList(),
    @XmlElement(true)
    @XmlSerialName("MultiGeometry", namespace = KML_NAMESPACE, prefix = "")
    public val multiGeometries: List<MultiGeometry> = emptyList(),
)

@Serializable
@XmlSerialName("ExtendedData", namespace = KML_NAMESPACE, prefix = "")
public data class ExtendedData(
    @XmlElement(true)
    @XmlSerialName("Data", namespace = KML_NAMESPACE, prefix = "")
    public val data: List<Data> = emptyList(),
)

@Serializable
@XmlSerialName("Data", namespace = KML_NAMESPACE, prefix = "")
public data class Data(
    @XmlSerialName("name")
    public val name: String? = null, // name is an attribute
    @XmlElement(true)
    @XmlSerialName("value", namespace = KML_NAMESPACE, prefix = "")
    public val value: String? = null,
)

@Serializable
@XmlSerialName("IconStyle", namespace = KML_NAMESPACE, prefix = "")
public data class IconStyle(
    @XmlElement(true)
    @XmlSerialName("scale", namespace = KML_NAMESPACE, prefix = "")
    public val scale: Float = 1.0f,
    @XmlElement(true)
    @XmlSerialName("Icon", namespace = KML_NAMESPACE, prefix = "")
    public val icon: Icon? = null,
    @XmlElement(true)
    @XmlSerialName("hotSpot", namespace = KML_NAMESPACE, prefix = "")
    public val hotSpot: HotSpot? = null,
)

@Serializable
@XmlSerialName("LabelStyle", namespace = KML_NAMESPACE, prefix = "")
public data class LabelStyle(
    @XmlElement(true)
    @XmlSerialName("scale", namespace = KML_NAMESPACE, prefix = "")
    public val scale: Float = 1.0f,
    @XmlElement(true)
    @XmlSerialName("color", namespace = KML_NAMESPACE, prefix = "")
    @Serializable(with = ColorSerializer::class)
    public val color: Int? = null,
)

@Serializable
@XmlSerialName("hotSpot", namespace = KML_NAMESPACE, prefix = "")
public data class HotSpot(
    @XmlSerialName("x")
    public val x: Double = 0.5,
    @XmlSerialName("y")
    public val y: Double = 1.0,
    @XmlSerialName("xunits")
    public val xunits: String = "fraction",
    @XmlSerialName("yunits")
    public val yunits: String = "fraction",
)

@Serializable
@XmlSerialName("Style", namespace = KML_NAMESPACE, prefix = "")
public data class Style(
    public val id: String? = null,
    @XmlElement(true)
    @XmlSerialName("IconStyle", namespace = KML_NAMESPACE, prefix = "")
    public val iconStyle: IconStyle? = null,
    @XmlElement(true)
    @XmlSerialName("LabelStyle", namespace = KML_NAMESPACE, prefix = "")
    public val labelStyle: LabelStyle? = null,
    @XmlElement(true)
    @XmlSerialName("LineStyle", namespace = KML_NAMESPACE, prefix = "")
    public val lineStyle: LineStyle? = null,
    @XmlElement(true)
    @XmlSerialName("PolyStyle", namespace = KML_NAMESPACE, prefix = "")
    public val polyStyle: PolyStyle? = null,
) : KmlFeature

@Serializable
@XmlSerialName("StyleMap", namespace = KML_NAMESPACE, prefix = "")
public data class StyleMap(
    public val id: String? = null,
    @XmlElement(true)
    @XmlSerialName("Pair", namespace = KML_NAMESPACE, prefix = "")
    public val pairs: List<Pair> = emptyList(),
) : KmlFeature

@Serializable
@XmlSerialName("LineStyle")
public data class LineStyle(
    @XmlElement(true)
    @XmlSerialName("color")
    @Serializable(with = ColorSerializer::class)
    public val color: Int? = null,
    @XmlElement(true)
    @XmlSerialName("width")
    public val width: Float? = null,
)

@Serializable
@XmlSerialName("PolyStyle", namespace = KML_NAMESPACE, prefix = "")
public data class PolyStyle(
    @XmlElement(true)
    @XmlSerialName("color", namespace = KML_NAMESPACE, prefix = "")
    @Serializable(with = ColorSerializer::class)
    public val color: Int? = null,
    @XmlElement(true)
    @XmlSerialName("fill", namespace = KML_NAMESPACE, prefix = "")
    @Serializable(with = BooleanSerializer::class)
    public val fill: Boolean = true,
    @XmlElement(true)
    @XmlSerialName("outline", namespace = KML_NAMESPACE, prefix = "")
    @Serializable(with = BooleanSerializer::class)
    public val outline: Boolean = true,
)

@Serializable
@XmlSerialName("Pair", namespace = KML_NAMESPACE, prefix = "")
public data class Pair(
    @XmlElement(true)
    @XmlSerialName("key", namespace = KML_NAMESPACE, prefix = "")
    public val key: String? = null,
    @XmlElement(true)
    @XmlSerialName("styleUrl", namespace = KML_NAMESPACE, prefix = "")
    public val styleUrl: String? = null,
)

@Serializable
@XmlSerialName("gx:BalloonVisibility", namespace = GOOGLE_KML_NAMESPACE, prefix = "gx")
public data class BalloonVisibility(
    public val value: Int = 1,
)

internal fun String.stripWhitespace(): String = filter { !it.isWhitespace() }

internal fun String.simplify(): String = this.stripWhitespace().lowercase()
