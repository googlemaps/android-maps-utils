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
package com.google.maps.android.data.kml

import android.content.Context
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.maps.android.collections.GroundOverlayManager
import com.google.maps.android.collections.MarkerManager
import com.google.maps.android.collections.PolygonManager
import com.google.maps.android.collections.PolylineManager
import com.google.maps.android.data.Geometry
import com.google.maps.android.data.Layer
import com.google.maps.android.data.Renderer
import com.google.maps.android.data.parser.kml.KmlParser
import com.google.maps.android.data.parser.kml.KmzParser
import com.google.maps.android.data.renderer.UrlIconProvider
import com.google.maps.android.data.renderer.mapview.MapViewRenderer
import org.xmlpull.v1.XmlPullParserException
import java.io.BufferedInputStream
import java.io.IOException
import java.io.InputStream
import java.nio.charset.StandardCharsets

@Deprecated("Use the new platform-agnostic data layer and renderer instead.")
public class KmlLayer : Layer {
    private val mPlacemarks = mutableListOf<KmlPlacemark>()
    private val mContainers = mutableListOf<KmlContainer>()
    private val mGroundOverlays = mutableListOf<KmlGroundOverlay>()
    private var mRenderer: MapViewRenderer? = null
    private var mIsLayerOnMap = false
    private val mPlacemarkMap = HashMap<KmlPlacemark, com.google.maps.android.data.renderer.model.Feature>()
    private val mModelToLegacyPlacemarks = HashMap<com.google.maps.android.data.renderer.model.Feature, KmlPlacemark>()
    private var mFeatureClickListener: OnFeatureClickListener? = null

    @JvmOverloads
    @Throws(XmlPullParserException::class, IOException::class)
    public constructor(
        map: GoogleMap?,
        resourceId: Int,
        context: Context,
        markerManager: MarkerManager? = null,
        polygonManager: PolygonManager? = null,
        polylineManager: PolylineManager? = null,
        groundOverlayManager: GroundOverlayManager? = null,
        cache: Renderer.ImagesCache? = null,
        maxKmzEntryCount: Int = 200,
        maxKmzUncompressedTotalSize: Long = 50 * 1024 * 1024,
    ) : this(
        map,
        context.resources.openRawResource(resourceId),
        context,
        markerManager,
        polygonManager,
        polylineManager,
        groundOverlayManager,
        cache,
        maxKmzEntryCount,
        maxKmzUncompressedTotalSize,
    )

    @JvmOverloads
    @Throws(XmlPullParserException::class, IOException::class)
    public constructor(
        map: GoogleMap?,
        stream: InputStream,
        context: Context,
        markerManager: MarkerManager? = null,
        polygonManager: PolygonManager? = null,
        polylineManager: PolylineManager? = null,
        groundOverlayManager: GroundOverlayManager? = null,
        cache: Renderer.ImagesCache? = null,
        maxKmzEntryCount: Int = 200,
        maxKmzUncompressedTotalSize: Long = 50 * 1024 * 1024,
    ) {
        mGoogleMap = map
        mRenderer = map?.let { MapViewRenderer(it, UrlIconProvider()) }

        val bis = BufferedInputStream(stream)
        bis.mark(1024)
        val headerBytes = ByteArray(1024)
        val read = bis.read(headerBytes)
        bis.reset()

        val isKmz = read > 0 && String(headerBytes, 0, read, StandardCharsets.UTF_8).startsWith("PK")
        val kmlObj =
            if (isKmz) {
                KmzParser(
                    maxKmzEntryCount = maxKmzEntryCount,
                    maxKmzUncompressedTotalSize = maxKmzUncompressedTotalSize
                ).parse(bis)
            } else {
                KmlParser().parse(bis)
            }

        // Register KMZ cached images to renderer if any
        kmlObj.images.forEach { (name, bitmap) ->
            mRenderer?.cacheImageData(name, bitmap)
        }

        // Parse Document
        kmlObj.document?.let { doc ->
            val styles = doc.styles.associate { it.id!! to toLegacyStyle(it) }
            val styleMaps = doc.styleMaps.associate { it.id!! to (it.pairs.firstOrNull { p -> p.key == "normal" }?.styleUrl ?: "") }

            val placemarksMap = HashMap<KmlPlacemark, Any?>()
            doc.placemarks.forEach { p ->
                placemarksMap[toLegacyPlacemark(p, styles, styleMaps)] = null
            }

            val childContainers = ArrayList<KmlContainer>()
            doc.folders.forEach { f ->
                childContainers.add(toLegacyContainer(f, styles, styleMaps))
            }

            val groundOverlaysMap = HashMap<KmlGroundOverlay, com.google.android.gms.maps.model.GroundOverlay?>()
            doc.groundOverlays.forEach { g ->
                groundOverlaysMap[toLegacyGroundOverlay(g)] = null
            }

            val properties = HashMap<String, String>()
            doc.name?.let { properties["name"] = it }
            doc.description?.let { properties["description"] = it }

            val rootContainer =
                KmlContainer(
                    properties,
                    HashMap(styles),
                    placemarksMap,
                    HashMap(styleMaps),
                    childContainers,
                    groundOverlaysMap,
                    doc.name ?: "Document",
                )

            mContainers.add(rootContainer)
        }

        // Parse top-level placemark or folder if Document is null
        if (kmlObj.document == null) {
            kmlObj.placemark?.let { mPlacemarks.add(toLegacyPlacemark(it, emptyMap(), emptyMap())) }
            kmlObj.folder?.let { mContainers.add(toLegacyContainer(it, emptyMap(), emptyMap())) }
            kmlObj.groundOverlay?.let { mGroundOverlays.add(toLegacyGroundOverlay(it)) }
        }
    }

    private fun toLegacyStyle(style: com.google.maps.android.data.parser.kml.Style): KmlStyle {
        val legacy = KmlStyle()
        legacy.setStyleId(style.id)
        style.iconStyle?.let { iconStyle ->
            legacy.setIconScale(iconStyle.scale.toDouble())
            iconStyle.icon?.href?.let { legacy.setIconUrl(it) }
            iconStyle.hotSpot?.let { hotSpot ->
                legacy.setHotSpot(hotSpot.x.toFloat(), hotSpot.y.toFloat(), hotSpot.xunits, hotSpot.yunits)
            }
        }
        style.lineStyle?.let { lineStyle ->
            lineStyle.color?.let { legacy.mPolylineOptions.color(abgrToArgb(it)) }
            lineStyle.width?.let { legacy.setWidth(it) }
        }
        style.polyStyle?.let { polyStyle ->
            legacy.setFill(polyStyle.fill)
            legacy.setOutline(polyStyle.outline)
            polyStyle.color?.let { legacy.mPolygonOptions.fillColor(abgrToArgb(it)) }
        }
        return legacy
    }

    private fun toLegacyPlacemark(
        placemark: com.google.maps.android.data.parser.kml.Placemark,
        styles: Map<String, KmlStyle>,
        styleMaps: Map<String, String>,
    ): KmlPlacemark {
        val geometry = toLegacyGeometry(placemark)
        val properties = mutableMapOf<String, String>()
        placemark.name?.let { properties["name"] = it }
        placemark.description?.let { properties["description"] = it }
        placemark.extendedData?.data?.forEach { d ->
            if (d.name != null && d.value != null) {
                properties[d.name] = d.value
            }
        }

        val styleUrl = placemark.styleUrl?.substringAfter("#") ?: ""
        val resolvedStyleUrl = styleMaps[styleUrl] ?: styleUrl
        val inlineStyle = placemark.style?.let { toLegacyStyle(it) } ?: styles[resolvedStyleUrl]

        return KmlPlacemark(geometry, resolvedStyleUrl, inlineStyle, properties)
    }

    private fun toLegacyContainer(
        folder: com.google.maps.android.data.parser.kml.Folder,
        styles: Map<String, KmlStyle>,
        styleMaps: Map<String, String>,
    ): KmlContainer {
        val properties = HashMap<String, String>()
        folder.name?.let { properties["name"] = it }
        folder.description?.let { properties["description"] = it }

        val placemarksMap = HashMap<KmlPlacemark, Any?>()
        folder.placemarks.forEach { p ->
            placemarksMap[toLegacyPlacemark(p, styles, styleMaps)] = null
        }

        val nestedContainers = ArrayList<KmlContainer>()
        folder.folders.forEach { f ->
            nestedContainers.add(toLegacyContainer(f, styles, styleMaps))
        }

        val groundOverlaysMap = HashMap<KmlGroundOverlay, com.google.android.gms.maps.model.GroundOverlay?>()
        folder.groundOverlays.forEach { g ->
            groundOverlaysMap[toLegacyGroundOverlay(g)] = null
        }

        return KmlContainer(
            properties,
            HashMap(styles),
            placemarksMap,
            HashMap(styleMaps),
            nestedContainers,
            groundOverlaysMap,
            folder.name,
        )
    }

    private fun toLegacyGroundOverlay(groundOverlay: com.google.maps.android.data.parser.kml.GroundOverlay): KmlGroundOverlay {
        val properties = mutableMapOf<String, String>()
        groundOverlay.name?.let { properties["name"] = it }

        val bounds =
            LatLngBounds(
                LatLng(groundOverlay.latLonBox!!.south, groundOverlay.latLonBox.west),
                LatLng(groundOverlay.latLonBox.north, groundOverlay.latLonBox.east),
            )

        return KmlGroundOverlay(
            imageUrl = groundOverlay.icon?.href ?: "",
            latLngBox = bounds,
            drawOrder = groundOverlay.drawOrder?.toFloat() ?: 0f,
            visibility = if (groundOverlay.visibility) 1 else 0,
            properties = properties,
            rotation = groundOverlay.latLonBox.rotation?.toFloat() ?: 0f,
        )
    }

    private fun toLegacyGeometry(placemark: com.google.maps.android.data.parser.kml.Placemark): Geometry? =
        when {
            placemark.point != null -> {
                KmlPoint(
                    LatLng(placemark.point.coordinates.latitude, placemark.point.coordinates.longitude),
                    placemark.point.coordinates.altitude,
                )
            }

            placemark.lineString != null -> {
                KmlLineString(
                    ArrayList(placemark.lineString.coordinates.map { LatLng(it.latitude, it.longitude) }),
                    ArrayList(placemark.lineString.coordinates.mapNotNull { it.altitude }),
                )
            }

            placemark.polygon != null -> {
                KmlPolygon(
                    placemark.polygon.outerBoundaryIs.linearRing.coordinates
                        .map { LatLng(it.latitude, it.longitude) },
                    placemark.polygon.innerBoundaryIs.map { ring -> ring.linearRing.coordinates.map { LatLng(it.latitude, it.longitude) } },
                )
            }

            placemark.multiGeometry != null -> {
                val list = ArrayList<Geometry>()
                placemark.multiGeometry.points.forEach {
                    list.add(KmlPoint(LatLng(it.coordinates.latitude, it.coordinates.longitude), it.coordinates.altitude))
                }
                placemark.multiGeometry.lineStrings.forEach {
                    list.add(
                        KmlLineString(
                            ArrayList(
                                it.coordinates.map { c ->
                                    LatLng(c.latitude, c.longitude)
                                },
                            ),
                        ),
                    )
                }
                placemark.multiGeometry.polygons.forEach {
                    list.add(
                        KmlPolygon(
                            it.outerBoundaryIs.linearRing.coordinates.map { c ->
                                LatLng(c.latitude, c.longitude)
                            },
                            it.innerBoundaryIs.map { ring ->
                                ring.linearRing.coordinates.map { c -> LatLng(c.latitude, c.longitude) }
                            },
                        ),
                    )
                }
                KmlMultiGeometry(list)
            }

            else -> {
                null
            }
        }

    private fun abgrToArgb(color: Int): Int {
        val a = (color shr 24) and 0xFF
        val b = (color shr 16) and 0xFF
        val g = (color shr 8) and 0xFF
        val r = color and 0xFF
        return (a shl 24) or (r shl 16) or (g shl 8) or b
    }

    override fun getMap(): GoogleMap? = mGoogleMap

    override fun setMap(map: GoogleMap?) {
        mGoogleMap = map
        if (map == null) {
            removeLayerFromMap()
            mRenderer = null
        } else {
            mRenderer = MapViewRenderer(map, UrlIconProvider())
            if (mIsLayerOnMap) {
                addLayerToMap()
            }
        }
    }

    override fun addLayerToMap() {
        val renderer = mRenderer ?: return
        mPlacemarks.forEach { p -> renderer.addFeature(toModelFeature(p)) }
        mContainers.forEach { c -> addContainerToMap(c, renderer) }
        mGroundOverlays.forEach { g -> renderer.addFeature(toModelFeature(g)) }
        mIsLayerOnMap = true
    }

    override fun removeLayerFromMap() {
        val renderer = mRenderer ?: return
        mPlacemarks.forEach { p -> renderer.removeFeature(toModelFeature(p)) }
        mContainers.forEach { c -> removeContainerFromMap(c, renderer) }
        mGroundOverlays.forEach { g -> renderer.removeFeature(toModelFeature(g)) }
        mIsLayerOnMap = false
    }

    private fun addContainerToMap(
        container: KmlContainer,
        renderer: MapViewRenderer,
    ) {
        container.getPlacemarks().forEach { p -> renderer.addFeature(toModelFeature(p)) }
        container.getGroundOverlays().forEach { g -> renderer.addFeature(toModelFeature(g)) }
        container.getContainers().forEach { c -> addContainerToMap(c, renderer) }
    }

    private fun removeContainerFromMap(
        container: KmlContainer,
        renderer: MapViewRenderer,
    ) {
        container.getPlacemarks().forEach { p -> renderer.removeFeature(toModelFeature(p)) }
        container.getGroundOverlays().forEach { g -> renderer.removeFeature(toModelFeature(g)) }
        container.getContainers().forEach { c -> removeContainerFromMap(c, renderer) }
    }

    private fun toModelFeature(placemark: KmlPlacemark): com.google.maps.android.data.renderer.model.Feature {
        val existing = mPlacemarkMap[placemark]
        if (existing != null) return existing

        val geometry = placemark.getGeometry()!!
        val modelGeometry = toModelGeometry(geometry)
        val properties = placemark.getPropertyKeys().associateWith { placemark.getProperty(it) as Any }

        val inline = placemark.getInlineStyle()
        val style =
            when (modelGeometry) {
                is com.google.maps.android.data.renderer.model.PointGeometry -> {
                    com.google.maps.android.data.renderer.model.PointStyle(
                        color = inline?.mMarkerColor?.toInt() ?: 0,
                        iconUrl = inline?.getIconUrl(),
                    )
                }

                is com.google.maps.android.data.renderer.model.LineString -> {
                    com.google.maps.android.data.renderer.model.LineStyle(
                        color = inline?.mPolylineOptions?.color ?: 0xFF000000.toInt(),
                        width = inline?.mPolylineOptions?.width ?: 1.0f,
                    )
                }

                is com.google.maps.android.data.renderer.model.Polygon -> {
                    com.google.maps.android.data.renderer.model.PolygonStyle(
                        fillColor = inline?.mPolygonOptions?.fillColor ?: 0x00000000,
                        strokeColor = inline?.mPolygonOptions?.strokeColor ?: 0xFF000000.toInt(),
                        strokeWidth = inline?.mPolygonOptions?.strokeWidth ?: 1.0f,
                    )
                }

                else -> {
                    null
                }
            }

        val modelFeature =
            com.google.maps.android.data.renderer.model
                .Feature(modelGeometry, style, properties)
        mPlacemarkMap[placemark] = modelFeature
        mModelToLegacyPlacemarks[modelFeature] = placemark
        return modelFeature
    }

    private fun toModelFeature(groundOverlay: KmlGroundOverlay): com.google.maps.android.data.renderer.model.Feature {
        val bounds = groundOverlay.getLatLngBox()
        val modelGeometry =
            com.google.maps.android.data.renderer.model.GroundOverlay(
                north = bounds.northeast.latitude,
                south = bounds.southwest.latitude,
                east = bounds.northeast.longitude,
                west = bounds.southwest.longitude,
                rotation = groundOverlay.getGroundOverlayOptions().bearing,
            )
        val style =
            com.google.maps.android.data.renderer.model.GroundOverlayStyle(
                iconUrl = groundOverlay.getImageUrl(),
                zIndex = groundOverlay.getGroundOverlayOptions().zIndex,
                visibility = groundOverlay.getGroundOverlayOptions().isVisible,
            )
        val properties = groundOverlay.getProperties().associateWith { groundOverlay.getProperty(it) as Any }
        return com.google.maps.android.data.renderer.model
            .Feature(modelGeometry, style, properties)
    }

    private fun toModelGeometry(geometry: Geometry): com.google.maps.android.data.renderer.model.Geometry =
        when (geometry) {
            is KmlPoint -> {
                com.google.maps.android.data.renderer.model.PointGeometry(
                    com.google.maps.android.data.renderer.model.Point(
                        geometry.getGeometryObject().latitude,
                        geometry.getGeometryObject().longitude,
                        geometry.getAltitude(),
                    ),
                )
            }

            is KmlLineString -> {
                com.google.maps.android.data.renderer.model.LineString(
                    geometry.getGeometryObject().map {
                        com.google.maps.android.data.renderer.model
                            .Point(it.latitude, it.longitude)
                    },
                )
            }

            is KmlPolygon -> {
                com.google.maps.android.data.renderer.model.Polygon(
                    geometry.getOuterBoundaryCoordinates().map {
                        com.google.maps.android.data.renderer.model.Point(
                            it.latitude,
                            it.longitude,
                        )
                    },
                    geometry.getInnerBoundaryCoordinates().map { inner ->
                        inner.map {
                            com.google.maps.android.data.renderer.model
                                .Point(it.latitude, it.longitude)
                        }
                    },
                )
            }

            is KmlMultiGeometry -> {
                com.google.maps.android.data.renderer.model.MultiGeometry(
                    geometry.getGeometryObject().map { toModelGeometry(it) },
                )
            }

            else -> {
                throw IllegalArgumentException("Unknown geometry type")
            }
        }

    public fun hasPlacemarks(): Boolean = mPlacemarks.isNotEmpty()

    public fun getPlacemarks(): Iterable<KmlPlacemark> = mPlacemarks

    public fun hasContainers(): Boolean = mContainers.isNotEmpty()

    public fun getContainers(): Iterable<KmlContainer> = mContainers

    public fun getGroundOverlays(): Iterable<KmlGroundOverlay> = mGroundOverlays

    override val features: Iterable<KmlPlacemark>
        get() = mPlacemarks

    public fun isLayerOnMap(): Boolean = mIsLayerOnMap

    override fun setOnFeatureClickListener(listener: OnFeatureClickListener) {
        mFeatureClickListener = listener
        mGoogleMap?.let { map ->
            map.setOnMarkerClickListener { marker ->
                val feature = findLegacyFeatureForMapObject(marker)
                if (feature != null) {
                    mFeatureClickListener?.onFeatureClick(feature)
                }
                false
            }
            map.setOnPolygonClickListener { polygon ->
                val feature = findLegacyFeatureForMapObject(polygon)
                if (feature != null) {
                    mFeatureClickListener?.onFeatureClick(feature)
                }
            }
            map.setOnPolylineClickListener { polyline ->
                val feature = findLegacyFeatureForMapObject(polyline)
                if (feature != null) {
                    mFeatureClickListener?.onFeatureClick(feature)
                }
            }
        }
    }

    private fun findLegacyFeatureForMapObject(mapObject: Any): KmlPlacemark? {
        val renderer = mRenderer ?: return null
        val modelFeature = renderer.getFeatureForMapObject(mapObject) ?: return null
        return mModelToLegacyPlacemarks[modelFeature]
    }
}
