/*
 * Copyright 2025 Google LLC
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
package com.google.maps.android.data.renderer.mapper

import com.google.maps.android.data.parser.kml.Folder
import com.google.maps.android.data.parser.kml.Kml
import com.google.maps.android.data.parser.kml.LineString as KmlLineString
import com.google.maps.android.data.parser.kml.MultiGeometry as KmlMultiGeometry
import com.google.maps.android.data.parser.kml.Point as KmlPoint
import com.google.maps.android.data.parser.kml.Polygon as KmlPolygon
import com.google.maps.android.data.parser.kml.Placemark
import com.google.maps.android.data.renderer.model.DataLayer
import com.google.maps.android.data.renderer.model.DataScene
import com.google.maps.android.data.renderer.model.Feature
import com.google.maps.android.data.renderer.model.Geometry
import com.google.maps.android.data.renderer.model.LineString as RendererLineString
import com.google.maps.android.data.renderer.model.MultiGeometry as RendererMultiGeometry
import com.google.maps.android.data.renderer.model.Point as RendererPoint
import com.google.maps.android.data.renderer.model.PointGeometry
import com.google.maps.android.data.renderer.model.Polygon as RendererPolygon
import com.google.maps.android.data.renderer.model.LineStyle
import com.google.maps.android.data.renderer.model.PointStyle
import com.google.maps.android.data.renderer.model.PolygonStyle
import com.google.maps.android.data.renderer.model.Style
import com.google.maps.android.data.renderer.model.GroundOverlay
import com.google.maps.android.data.renderer.model.GroundOverlayStyle
import com.google.maps.android.data.parser.kml.GroundOverlay as KmlGroundOverlay
import com.google.maps.android.data.parser.kml.Style as KmlStyle
import com.google.maps.android.data.parser.kml.StyleMap as KmlStyleMap

/**
 * A mapper class responsible for transforming KML objects into the platform-agnostic
 * [DataScene] model.
 *
 * This mapper handles:
 * - Placemarks (Points, LineStrings, Polygons, MultiGeometries)
 * - Folders (recursive structure flattened for rendering)
 * - GroundOverlays
 * - Styles (resolving StyleUrls and StyleMaps)
 */
object KmlMapper {
    fun toScene(kml: Kml): DataScene {
        return DataScene(listOf(toLayer(kml)))
    }

    internal fun toLayer(kml: Kml): DataLayer {
        val styles = mutableMapOf<String, KmlStyle>()
        val styleMaps = mutableMapOf<String, KmlStyleMap>()

        kml.document?.styles?.forEach { style ->
            style.id?.let { styles[it] = style }
        }
        kml.document?.styleMaps?.forEach { styleMap ->
            styleMap.id?.let { styleMaps[it] = styleMap }
        }
        kml.style?.let { style -> style.id?.let { styles[it] = style } }
        kml.styleMap?.let { styleMap -> styleMap.id?.let { styleMaps[it] = styleMap } }

        val features = buildList {

            kml.document?.let {
                it.placemarks.forEach { placemark ->
                    add(placemark.toRendererFeature(styles, styleMaps))
                }
                it.folders.forEach { folder ->
                    addFolder(folder, this, styles, styleMaps)
                }
                it.groundOverlays.forEach { groundOverlay ->
                    add(groundOverlay.toRendererFeature())
                }
            }
            kml.placemark?.let {
                add(it.toRendererFeature(styles, styleMaps))
            }
            kml.folder?.let { folder ->
                addFolder(folder, this, styles, styleMaps)
            }
            kml.groundOverlay?.let { groundOverlay ->
                add(groundOverlay.toRendererFeature())
            }
        }

        return DataLayer(features, properties = mapOf("images" to kml.images))
    }

    private fun addFolder(folder: Folder, features: MutableList<Feature>, styles: Map<String, KmlStyle>, styleMaps: Map<String, KmlStyleMap>) {
        folder.placemarks.forEach { placemark ->
            features.add(placemark.toRendererFeature(styles, styleMaps))
        }
        folder.folders.forEach { subFolder ->
            addFolder(subFolder, features, styles, styleMaps)
        }
        folder.groundOverlays.forEach { groundOverlay ->
            features.add(groundOverlay.toRendererFeature())
        }
    }
}

fun Kml.toLayer(): DataLayer {
    return KmlMapper.toLayer(this)
}

private fun Placemark.toRendererFeature(styles: Map<String, KmlStyle>, styleMaps: Map<String, KmlStyleMap>): Feature {
    val geometry = toGeometry(this)
    var style: Style? = null

    // Resolve style
    val kmlStyle = this.style ?: styleUrl?.let { url ->
        val styleId = if (url.startsWith("#")) url.substring(1) else url
        var resolvedStyle = styles[styleId]
        if (resolvedStyle == null) {
            // Try to resolve as StyleMap
            val styleMap = styleMaps[styleId]
            styleMap?.let { map ->
                val normalPair = map.pairs.find { it.key == "normal" } ?: map.pairs.firstOrNull()
                normalPair?.styleUrl?.let { normalUrl ->
                    val normalStyleId = if (normalUrl.startsWith("#")) normalUrl.substring(1) else normalUrl
                    resolvedStyle = styles[normalStyleId]
                }
            }
        }
        resolvedStyle
    }

    kmlStyle?.let {
        style = it.toRendererStyle(geometry)
    }

    val properties = mutableMapOf<String, Any>()
    name?.let { properties["name"] = it }
    description?.let { properties["description"] = it }
    extendedData?.data?.forEach { data ->
        data.name?.let { name ->
            data.value?.let { value ->
                properties[name] = value
            }
        }
    }

    // Add timestamps from Track if present
    track?.whens?.let { whens ->
        properties["timestamps"] = whens
    }
    // Add timestamps from MultiTrack if present (flattened or list of lists?)
    // For simplicity, let's just add them if it's a single track inside MultiTrack or just skip for now to avoid complexity
    // actually, MultiTrack might have multiple tracks with their own times.
    // Let's just handle single Track for now as it's the most common case for "gx:Track" usage in simple files.
    multiTrack?.tracks?.flatMap { it.whens }?.let { allWhens ->
        if (allWhens.isNotEmpty()) {
            properties["timestamps"] = allWhens
        }
    }

    return Feature(geometry, style = style, properties = properties)
}

private fun KmlGroundOverlay.toRendererFeature(): Feature {
    val geometry = GroundOverlay(
        north = latLonBox?.north ?: 0.0,
        south = latLonBox?.south ?: 0.0,
        east = latLonBox?.east ?: 0.0,
        west = latLonBox?.west ?: 0.0,
        rotation = latLonBox?.rotation?.toFloat() ?: 0f
    )
    val style = GroundOverlayStyle(
        iconUrl = icon?.href,
        zIndex = drawOrder?.toFloat() ?: 0f,
        transparency = 0f, // KML color alpha not directly mapped to transparency in this simple mapper yet
        visibility = visibility
    )
    val properties = mutableMapOf<String, Any>()
    name?.let { properties["name"] = it }
    
    return Feature(geometry, style = style, properties = properties)
}

private fun KmlStyle.toRendererStyle(geometry: Geometry): Style? {
    return when (geometry) {
        is PointGeometry -> {
            iconStyle?.let {
                PointStyle(
                    scale = it.scale,
                    iconUrl = it.icon?.href,
                    // TODO: Map other properties like heading, hotSpot if needed
                )
            }
        }
        is RendererLineString -> {
            lineStyle?.let {
                LineStyle(
                    color = convertKmlColor(it.color ?: 0xFF000000.toInt()),
                    width = it.width ?: 1.0f
                )
            }
        }
        is RendererPolygon -> {
            polyStyle?.let {
                PolygonStyle(
                    fillColor = if (it.fill) convertKmlColor(it.color ?: 0x00000000) else 0x00000000,
                    strokeColor = convertKmlColor(lineStyle?.color ?: 0xFF000000.toInt()),
                    strokeWidth = lineStyle?.width ?: 1.0f,
                    // TODO: Handle outline property
                )
            }
        }
        else -> null
    }
}

/**
 * Converts KML color (AABBGGRR) to Android color (AARRGGBB).
 */
private fun convertKmlColor(color: Int): Int {
    val a = (color shr 24) and 0xFF
    val b = (color shr 16) and 0xFF
    val g = (color shr 8) and 0xFF
    val r = color and 0xFF
    return (a shl 24) or (r shl 16) or (g shl 8) or b
}

private fun toGeometry(placemark: Placemark): Geometry {
    placemark.point?.let { point ->
        return point.toPointGeometry()
    }
    placemark.lineString?.let {
        return it.toRendererLineString()
    }
    placemark.polygon?.let {
        return it.toRendererPolygon()
    }
    placemark.multiGeometry?.let {
        return it.toRendererMultiGeometry()
    }
    placemark.track?.let {
        return it.toRendererLineString()
    }
    placemark.multiTrack?.let {
        return it.toRendererMultiGeometry()
    }
    // Should not happen if KML is valid
    throw IllegalArgumentException("Placemark must contain a geometry")
}

private fun com.google.maps.android.data.parser.kml.Track.toRendererLineString(): RendererLineString {
    val coordinates = coords.mapNotNull { coordString ->
        val parts = coordString.trim().split("\\s+".toRegex())
        if (parts.size >= 2) {
            val lon = parts[0].toDoubleOrNull()
            val lat = parts[1].toDoubleOrNull()
            val alt = if (parts.size > 2) parts[2].toDoubleOrNull() ?: 0.0 else 0.0
            if (lon != null && lat != null) {
                RendererPoint(lat, lon, alt)
            } else null
        } else null
    }
    return RendererLineString(coordinates)
}

private fun com.google.maps.android.data.parser.kml.MultiTrack.toRendererMultiGeometry(): RendererMultiGeometry {
    val geometries = tracks.map { it.toRendererLineString() }
    return RendererMultiGeometry(geometries)
}

private fun KmlPoint.toPointGeometry(): PointGeometry {
    return PointGeometry(
        RendererPoint(
            lat = coordinates.latitude,
            lng = coordinates.longitude,
            alt = coordinates.altitude
        )
    )
}

private fun KmlLineString.toRendererLineString(): RendererLineString {
    return RendererLineString(coordinates.map {
        RendererPoint(
            lat = it.latitude,
            lng = it.longitude,
            alt = it.altitude
        )
    })
}

private fun KmlPolygon.toRendererPolygon(): RendererPolygon {
    val outerBoundary = outerBoundaryIs.linearRing.coordinates.map {
        RendererPoint(
            lat = it.latitude,
            lng = it.longitude,
            alt = it.altitude
        )
    }
    val innerBoundaries = innerBoundaryIs.map { boundary ->
        boundary.linearRing.coordinates.map {
            RendererPoint(
                lat = it.latitude,
                lng = it.longitude,
                alt = it.altitude
            )
        }
    }
    return RendererPolygon(outerBoundary, innerBoundaries)
}

private fun KmlMultiGeometry.toRendererMultiGeometry(): RendererMultiGeometry {
    val geometries = mutableListOf<Geometry>()
    points.forEach { point ->
        geometries.add(point.toPointGeometry())
    }
    lineStrings.forEach { lineString ->
        geometries.add(lineString.toRendererLineString())
    }
    polygons.forEach { polygon ->
        geometries.add(polygon.toRendererPolygon())
    }
    return RendererMultiGeometry(geometries)
}
