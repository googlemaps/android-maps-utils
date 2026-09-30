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
import android.graphics.Color
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
    private val mGroundOverlayMap = HashMap<KmlGroundOverlay, com.google.maps.android.data.renderer.model.Feature>()
    private val mModelToLegacyPlacemarks = HashMap<com.google.maps.android.data.renderer.model.Feature, KmlPlacemark>()
    private var mFeatureClickListener: OnFeatureClickListener? = null
    private var mMarkerManager: MarkerManager? = null
    private var mPolygonManager: PolygonManager? = null
    private var mPolylineManager: PolylineManager? = null
    private var mGroundOverlayManager: GroundOverlayManager? = null
    private var mManagersMap: GoogleMap? = null
    private var mMarkerCollection: MarkerManager.Collection? = null
    private var mPolygonCollection: PolygonManager.Collection? = null
    private var mPolylineCollection: PolylineManager.Collection? = null
    private var mGroundOverlayCollection: GroundOverlayManager.Collection? = null
    private val mCachedImages = HashMap<String, android.graphics.Bitmap>()

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
        mMarkerManager = markerManager
        mPolygonManager = polygonManager
        mPolylineManager = polylineManager
        mGroundOverlayManager = groundOverlayManager
        initializeRenderer(map)

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
            mCachedImages[name] = bitmap
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

    private fun initializeRenderer(map: GoogleMap?) {
        if (map == null) {
            mRenderer = null
            return
        }
        if (mManagersMap == null &&
            (mMarkerManager != null || mPolygonManager != null || mPolylineManager != null || mGroundOverlayManager != null)
        ) {
            mManagersMap = map
            mMarkerCollection = mMarkerManager?.newCollection()
            mPolygonCollection = mPolygonManager?.newCollection()
            mPolylineCollection = mPolylineManager?.newCollection()
            mGroundOverlayCollection = mGroundOverlayManager?.newCollection()
        }
        val renderer =
            MapViewRenderer(
                map = map,
                iconProvider = UrlIconProvider(),
                markerCollection = mMarkerCollection,
                polygonCollection = mPolygonCollection,
                polylineCollection = mPolylineCollection,
                groundOverlayCollection = mGroundOverlayCollection,
            )
        mCachedImages.forEach { (name, bitmap) ->
            renderer.cacheImageData(name, bitmap)
        }
        mRenderer = renderer
    }

    override fun getMap(): GoogleMap? = mGoogleMap

    override fun setMap(map: GoogleMap?) {
        val wasLayerOnMap = mIsLayerOnMap
        if (wasLayerOnMap) {
            removeLayerFromMap()
        }
        if (map != null && mManagersMap != null && map !== mManagersMap) {
            mMarkerManager = null
            mPolygonManager = null
            mPolylineManager = null
            mGroundOverlayManager = null
            mMarkerCollection = null
            mPolygonCollection = null
            mPolylineCollection = null
            mGroundOverlayCollection = null
            mManagersMap = null
        }
        mGoogleMap = map
        if (map == null) {
            mRenderer = null
        } else {
            initializeRenderer(map)
            mFeatureClickListener?.let { setOnFeatureClickListener(it) }
            if (wasLayerOnMap) {
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

    private fun buildPointStyle(inline: KmlStyle?): com.google.maps.android.data.renderer.model.PointStyle =
        com.google.maps.android.data.renderer.model.PointStyle(
            // mMarkerColor is a hue (0..360), not an ARGB color — convert it before handing it to the
            // renderer, which derives the marker's hue and alpha from an ARGB value. Passing the raw
            // hue (or 0) made the alpha channel 0 and rendered every KML point marker invisible.
            color =
                inline?.mMarkerColor?.let { hue -> Color.HSVToColor(floatArrayOf(hue, 1f, 1f)) }
                    ?: Color.BLACK,
            iconUrl = inline?.getIconUrl(),
            zIndex = inline?.mMarkerOptions?.zIndex ?: 0f,
        )

    private fun buildLineStyle(inline: KmlStyle?): com.google.maps.android.data.renderer.model.LineStyle =
        com.google.maps.android.data.renderer.model.LineStyle(
            color = inline?.mPolylineOptions?.color ?: 0xFF000000.toInt(),
            width = inline?.mPolylineOptions?.width ?: 1.0f,
            geodesic = inline?.mPolylineOptions?.isGeodesic ?: false,
            zIndex = inline?.mPolylineOptions?.zIndex ?: 0f,
            clickable = inline?.mPolylineOptions?.isClickable ?: true,
            visible = inline?.mPolylineOptions?.isVisible ?: true,
        )

    private fun buildPolygonStyle(inline: KmlStyle?): com.google.maps.android.data.renderer.model.PolygonStyle =
        com.google.maps.android.data.renderer.model.PolygonStyle(
            fillColor = inline?.mPolygonOptions?.fillColor ?: 0x00000000,
            strokeColor = inline?.mPolygonOptions?.strokeColor ?: 0xFF000000.toInt(),
            strokeWidth = inline?.mPolygonOptions?.strokeWidth ?: 1.0f,
            geodesic = inline?.mPolygonOptions?.isGeodesic ?: false,
            zIndex = inline?.mPolygonOptions?.zIndex ?: 0f,
            clickable = inline?.mPolygonOptions?.isClickable ?: true,
            visible = inline?.mPolygonOptions?.isVisible ?: true,
        )

    private fun toModelFeature(placemark: KmlPlacemark): com.google.maps.android.data.renderer.model.Feature {
        val existing = mPlacemarkMap[placemark]
        if (existing != null) return existing

        val geometry = placemark.getGeometry()!!
        val modelGeometry = toModelGeometry(geometry)
        val properties = placemark.getPropertyKeys().associateWith { placemark.getProperty(it) as Any }

        val inline = placemark.getInlineStyle()
        val style =
            when (modelGeometry) {
                is com.google.maps.android.data.renderer.model.PointGeometry -> buildPointStyle(inline)
                is com.google.maps.android.data.renderer.model.LineString -> buildLineStyle(inline)
                is com.google.maps.android.data.renderer.model.Polygon -> buildPolygonStyle(inline)
                is com.google.maps.android.data.renderer.model.MultiGeometry ->
                    com.google.maps.android.data.renderer.model.CompositeStyle(
                        pointStyle = buildPointStyle(inline),
                        lineStyle = buildLineStyle(inline),
                        polygonStyle = buildPolygonStyle(inline),
                    )
                else -> null
            }

        val modelFeature =
            com.google.maps.android.data.renderer.model
                .Feature(modelGeometry, style, properties)
        mPlacemarkMap[placemark] = modelFeature
        mModelToLegacyPlacemarks[modelFeature] = placemark
        return modelFeature
    }

    private fun toModelFeature(groundOverlay: KmlGroundOverlay): com.google.maps.android.data.renderer.model.Feature {
        val existing = mGroundOverlayMap[groundOverlay]
        if (existing != null) return existing

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
        val modelFeature =
            com.google.maps.android.data.renderer.model
                .Feature(modelGeometry, style, properties)
        mGroundOverlayMap[groundOverlay] = modelFeature
        return modelFeature
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

    private fun collectPlacemarks(
        container: KmlContainer,
        out: MutableList<KmlPlacemark>,
    ) {
        out.addAll(container.getPlacemarks())
        container.getContainers().forEach { collectPlacemarks(it, out) }
    }

    /**
     * Returns all [KmlPlacemark] objects in this layer, including both top-level placemarks
     * and placemarks nested inside `<Document>` and `<Folder>` [KmlContainer]s.
     */
    public fun getAllPlacemarks(): List<KmlPlacemark> {
        val all = ArrayList<KmlPlacemark>(mPlacemarks)
        mContainers.forEach { collectPlacemarks(it, all) }
        return all
    }

    private fun collectGroundOverlays(
        container: KmlContainer,
        out: MutableList<KmlGroundOverlay>,
    ) {
        out.addAll(container.getGroundOverlays())
        container.getContainers().forEach { collectGroundOverlays(it, out) }
    }

    /**
     * Returns all [KmlGroundOverlay] objects in this layer, including both top-level ground overlays
     * and ground overlays nested inside `<Document>` and `<Folder>` [KmlContainer]s.
     */
    public fun getAllGroundOverlays(): List<KmlGroundOverlay> {
        val all = ArrayList<KmlGroundOverlay>(mGroundOverlays)
        mContainers.forEach { collectGroundOverlays(it, all) }
        return all
    }

    public fun hasPlacemarks(): Boolean = mPlacemarks.isNotEmpty()

    public fun getPlacemarks(): Iterable<KmlPlacemark> = mPlacemarks

    public fun hasContainers(): Boolean = mContainers.isNotEmpty()

    public fun getContainers(): Iterable<KmlContainer> = mContainers

    public fun getGroundOverlays(): Iterable<KmlGroundOverlay> = mGroundOverlays

    override val features: Iterable<KmlPlacemark>
        get() = mPlacemarks

    override fun isLayerOnMap(): Boolean = mIsLayerOnMap

    override fun setOnFeatureClickListener(listener: OnFeatureClickListener) {
        mFeatureClickListener = listener
        val renderer = mRenderer
        val map = mGoogleMap ?: return

        val markerClickListener =
            GoogleMap.OnMarkerClickListener { marker ->
                val feature = findLegacyFeatureForMapObject(marker)
                if (feature != null) {
                    mFeatureClickListener?.onFeatureClick(feature)
                }
                false
            }
        if (renderer?.markerCollection != null) {
            renderer.markerCollection.setOnMarkerClickListener(markerClickListener)
        } else {
            map.setOnMarkerClickListener(markerClickListener)
        }

        val polygonClickListener =
            GoogleMap.OnPolygonClickListener { polygon ->
                val feature = findLegacyFeatureForMapObject(polygon)
                if (feature != null) {
                    mFeatureClickListener?.onFeatureClick(feature)
                }
            }
        if (renderer?.polygonCollection != null) {
            renderer.polygonCollection.setOnPolygonClickListener(polygonClickListener)
        } else {
            map.setOnPolygonClickListener(polygonClickListener)
        }

        val polylineClickListener =
            GoogleMap.OnPolylineClickListener { polyline ->
                val feature = findLegacyFeatureForMapObject(polyline)
                if (feature != null) {
                    mFeatureClickListener?.onFeatureClick(feature)
                }
            }
        if (renderer?.polylineCollection != null) {
            renderer.polylineCollection.setOnPolylineClickListener(polylineClickListener)
        } else {
            map.setOnPolylineClickListener(polylineClickListener)
        }
    }

    private fun findLegacyFeatureForMapObject(mapObject: Any): KmlPlacemark? {
        val renderer = mRenderer ?: return null
        val modelFeature = renderer.getFeatureForMapObject(mapObject) ?: return null
        return mModelToLegacyPlacemarks[modelFeature]
    }
}
