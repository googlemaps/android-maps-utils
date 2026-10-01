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
package com.google.maps.android.data.geojson

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
import com.google.maps.android.data.MultiGeometry
import com.google.maps.android.data.parser.geojson.GeoJsonFeature as ParserGeoJsonFeature
import com.google.maps.android.data.parser.geojson.GeoJsonFeatureCollection as ParserGeoJsonFeatureCollection
import com.google.maps.android.data.parser.geojson.GeoJsonGeometry as ParserGeoJsonGeometry
import com.google.maps.android.data.parser.geojson.GeoJsonGeometryCollection as ParserGeoJsonGeometryCollection
import com.google.maps.android.data.parser.geojson.GeoJsonLineString as ParserGeoJsonLineString
import com.google.maps.android.data.parser.geojson.GeoJsonMultiLineString as ParserGeoJsonMultiLineString
import com.google.maps.android.data.parser.geojson.GeoJsonMultiPoint as ParserGeoJsonMultiPoint
import com.google.maps.android.data.parser.geojson.GeoJsonMultiPolygon as ParserGeoJsonMultiPolygon
import com.google.maps.android.data.parser.geojson.GeoJsonParser
import com.google.maps.android.data.parser.geojson.GeoJsonPoint as ParserGeoJsonPoint
import com.google.maps.android.data.parser.geojson.GeoJsonPolygon as ParserGeoJsonPolygon
import com.google.maps.android.data.renderer.UrlIconProvider
import com.google.maps.android.data.renderer.mapview.MapViewRenderer
import com.google.maps.android.data.renderer.model.CompositeStyle as ModelCompositeStyle
import com.google.maps.android.data.renderer.model.Feature as ModelFeature
import com.google.maps.android.data.renderer.model.Geometry as ModelGeometry
import com.google.maps.android.data.renderer.model.LineString as ModelLineString
import com.google.maps.android.data.renderer.model.LineStyle as ModelLineStyle
import com.google.maps.android.data.renderer.model.MultiGeometry as ModelMultiGeometry
import com.google.maps.android.data.renderer.model.Point as ModelPoint
import com.google.maps.android.data.renderer.model.PointGeometry as ModelPointGeometry
import com.google.maps.android.data.renderer.model.PointStyle as ModelPointStyle
import com.google.maps.android.data.renderer.model.Polygon as ModelPolygon
import com.google.maps.android.data.renderer.model.PolygonStyle as ModelPolygonStyle
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.io.InputStream
import java.util.IdentityHashMap
import java.util.Observer


@Deprecated("Use the new platform-agnostic data layer and renderer instead.")
public class GeoJsonLayer : Layer {
    private val mFeatures = mutableListOf<GeoJsonFeature>()
    private var mBoundingBox: LatLngBounds? = null
    private var mRenderer: MapViewRenderer? = null
    private var mIsLayerOnMap = false
    private val mFeatureMap = HashMap<GeoJsonFeature, ModelFeature>()
    private val mModelToLegacyFeatures = IdentityHashMap<ModelFeature, GeoJsonFeature>()
    private var mMarkerManager: MarkerManager? = null
    private var mPolygonManager: PolygonManager? = null
    private var mPolylineManager: PolylineManager? = null
    private var mGroundOverlayManager: GroundOverlayManager? = null
    private var mManagersMap: GoogleMap? = null
    private var mMarkerCollection: MarkerManager.Collection? = null
    private var mPolygonCollection: PolygonManager.Collection? = null
    private var mPolylineCollection: PolylineManager.Collection? = null
    private var mGroundOverlayCollection: GroundOverlayManager.Collection? = null
    private val mCachedDescriptors = HashMap<String, com.google.android.gms.maps.model.BitmapDescriptor>()

    private var mFeatureClickListener: OnFeatureClickListener? = null
    private val mFeatureObserver = Observer { observable, _ ->
        if (observable is GeoJsonFeature) onFeatureChanged(observable)
    }

    public interface GeoJsonOnFeatureClickListener : OnFeatureClickListener

    @JvmOverloads
    @Throws(JSONException::class)
    public constructor(
        map: GoogleMap?,
        geoJsonFile: JSONObject,
        markerManager: MarkerManager? = null,
        polygonManager: PolygonManager? = null,
        polylineManager: PolylineManager? = null,
        groundOverlayManager: GroundOverlayManager? = null,
    ) {
        mGoogleMap = map
        mMarkerManager = markerManager
        mPolygonManager = polygonManager
        mPolylineManager = polylineManager
        mGroundOverlayManager = groundOverlayManager
        val stream = geoJsonFile.toString().byteInputStream()
        parseGeoJson(stream)
        initializeRenderer(map)
    }

    @JvmOverloads
    @Throws(IOException::class, JSONException::class)
    public constructor(
        map: GoogleMap?,
        resourceId: Int,
        context: Context,
        markerManager: MarkerManager? = null,
        polygonManager: PolygonManager? = null,
        polylineManager: PolylineManager? = null,
        groundOverlayManager: GroundOverlayManager? = null,
    ) {
        mGoogleMap = map
        mMarkerManager = markerManager
        mPolygonManager = polygonManager
        mPolylineManager = polylineManager
        mGroundOverlayManager = groundOverlayManager
        val stream = context.resources.openRawResource(resourceId)
        parseGeoJson(stream)
        initializeRenderer(map)
    }

    private fun parseGeoJson(stream: InputStream) {
        val parsed = GeoJsonParser().parse(stream) ?: throw JSONException("Failed to parse GeoJSON")

        // Map parsed object to our legacy GeoJsonFeature instances
        when (parsed) {
            is ParserGeoJsonFeatureCollection -> {
                parsed.features.forEach { feature ->
                    val legacyGeometry = feature.geometry?.let { toLegacyGeometry(it) }
                    val legacyProps = feature.properties?.filterValues { it != null }?.mapValues { it.value as String }
                    val legacyFeature = GeoJsonFeature(legacyGeometry, feature.id, legacyProps, null)

                    // Wire default styles
                    legacyFeature.pointStyle = mDefaultPointStyle
                    legacyFeature.lineStringStyle = mDefaultLineStringStyle
                    legacyFeature.polygonStyle = mDefaultPolygonStyle

                    mFeatures.add(legacyFeature)
                }
            }

            is ParserGeoJsonFeature -> {
                val legacyGeometry = parsed.geometry?.let { toLegacyGeometry(it) }
                val legacyProps = parsed.properties?.filterValues { it != null }?.mapValues { it.value as String }
                val legacyFeature = GeoJsonFeature(legacyGeometry, parsed.id, legacyProps, null)

                legacyFeature.pointStyle = mDefaultPointStyle
                legacyFeature.lineStringStyle = mDefaultLineStringStyle
                legacyFeature.polygonStyle = mDefaultPolygonStyle

                mFeatures.add(legacyFeature)
            }

            is ParserGeoJsonGeometry -> {
                val legacyGeometry = toLegacyGeometry(parsed)
                val legacyFeature = GeoJsonFeature(legacyGeometry, null, null, null)

                legacyFeature.pointStyle = mDefaultPointStyle
                legacyFeature.lineStringStyle = mDefaultLineStringStyle
                legacyFeature.polygonStyle = mDefaultPolygonStyle

                mFeatures.add(legacyFeature)
            }
        }

        mFeatures.forEach { it.addObserver(mFeatureObserver) }
        // Calculate bounding box
        calculateBoundingBox()
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
        mCachedDescriptors.forEach { (key, descriptor) ->
            renderer.cacheIconDescriptor(key, descriptor)
        }
        mRenderer = renderer
    }

    private fun toLegacyGeometry(geometry: ParserGeoJsonGeometry): Geometry =
        when (geometry) {
            is ParserGeoJsonPoint -> {
                GeoJsonPoint(LatLng(geometry.coordinates.lat, geometry.coordinates.lng), geometry.coordinates.alt)
            }

            is ParserGeoJsonLineString -> {
                GeoJsonLineString(geometry.coordinates.map { LatLng(it.lat, it.lng) }, geometry.coordinates.mapNotNull { it.alt })
            }

            is ParserGeoJsonPolygon -> {
                GeoJsonPolygon(geometry.coordinates.map { ring -> ring.map { LatLng(it.lat, it.lng) } })
            }

            is ParserGeoJsonMultiPoint -> {
                GeoJsonMultiPoint(geometry.coordinates.map { GeoJsonPoint(LatLng(it.lat, it.lng), it.alt) })
            }

            is ParserGeoJsonMultiLineString -> {
                GeoJsonMultiLineString(geometry.coordinates.map { GeoJsonLineString(it.map { c -> LatLng(c.lat, c.lng) }) })
            }

            is ParserGeoJsonMultiPolygon -> {
                GeoJsonMultiPolygon(geometry.coordinates.map { GeoJsonPolygon(it.map { ring -> ring.map { c -> LatLng(c.lat, c.lng) } }) })
            }

            is ParserGeoJsonGeometryCollection -> {
                GeoJsonGeometryCollection(geometry.geometries.map { toLegacyGeometry(it) })
            }
        }


    private fun calculateBoundingBox() {
        val boundsBuilder = LatLngBounds.builder()
        var hasPoints = false

        mFeatures.forEach { feature ->
            val geometry = feature.getGeometry() ?: return@forEach
            when (geometry) {
                is GeoJsonPoint -> {
                    boundsBuilder.include(geometry.getCoordinates())
                    hasPoints = true
                }

                is GeoJsonLineString -> {
                    geometry.getCoordinates().forEach {
                        boundsBuilder.include(it)
                        hasPoints = true
                    }
                }

                is GeoJsonPolygon -> {
                    geometry.getOuterBoundaryCoordinates().forEach {
                        boundsBuilder.include(it)
                        hasPoints = true
                    }
                }

                is MultiGeometry -> {
                    includeMultiGeometryBounds(geometry, boundsBuilder)
                    hasPoints = true
                }
            }
        }
        if (hasPoints) {
            mBoundingBox = boundsBuilder.build()
        }
    }

    private fun includeMultiGeometryBounds(
        multiGeometry: MultiGeometry,
        builder: LatLngBounds.Builder,
    ) {
        multiGeometry.getGeometryObject().forEach { geom ->
            when (geom) {
                is GeoJsonPoint -> builder.include(geom.getCoordinates())
                is GeoJsonLineString -> geom.getCoordinates().forEach { builder.include(it) }
                is GeoJsonPolygon -> geom.getOuterBoundaryCoordinates().forEach { builder.include(it) }
                is MultiGeometry -> includeMultiGeometryBounds(geom, builder)
            }
        }
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
        mFeatures.forEach { feature ->
            if (feature.getGeometry() != null) renderer.addFeature(toModelFeature(feature))
        }
        mIsLayerOnMap = true
    }

    override fun removeLayerFromMap() {
        val renderer = mRenderer ?: return
        mFeatures.forEach { feature ->
            mFeatureMap[feature]?.let { renderer.removeFeature(it) }
        }
        mIsLayerOnMap = false
    }

    private fun buildPointStyle(feature: GeoJsonFeature): ModelPointStyle {
        val pointStyle = feature.pointStyle ?: mDefaultPointStyle
        val iconUrl =
            pointStyle.getIcon()?.let { descriptor ->
                val key = "geojson-descriptor://${System.identityHashCode(descriptor)}"
                mCachedDescriptors[key] = descriptor
                mRenderer?.cacheIconDescriptor(key, descriptor)
                key
            }
        return ModelPointStyle(
            // The renderer derives the marker's alpha from the color's alpha channel, so encode the
            // legacy style's alpha into an otherwise-black color (hue 0 keeps the default marker look).
            // A transparent color here (e.g. 0) would render the marker invisible.
            color = Color.argb((pointStyle.getAlpha() * 255).toInt().coerceIn(0, 255), 0, 0, 0),
            iconUrl = iconUrl,
            anchorU = pointStyle.getAnchorU(),
            anchorV = pointStyle.getAnchorV(),
            heading = pointStyle.getRotation(),
            zIndex = pointStyle.getZIndex(),
            title = pointStyle.getTitle(),
            snippet = pointStyle.getSnippet(),
            draggable = pointStyle.isDraggable(),
            flat = pointStyle.isFlat(),
            visible = pointStyle.isVisible(),
            infoWindowAnchorU = pointStyle.getInfoWindowAnchorU(),
            infoWindowAnchorV = pointStyle.getInfoWindowAnchorV(),
        )
    }

    private fun buildLineStyle(feature: GeoJsonFeature): ModelLineStyle {
        val lineStyle = feature.lineStringStyle ?: mDefaultLineStringStyle
        return ModelLineStyle(
            color = lineStyle.color,
            width = lineStyle.getWidth(),
            geodesic = lineStyle.isGeodesic(),
            zIndex = lineStyle.getZIndex(),
            clickable = lineStyle.isClickable(),
            visible = lineStyle.isVisible(),
        )
    }

    private fun buildPolygonStyle(feature: GeoJsonFeature): ModelPolygonStyle {
        val polygonStyle = feature.polygonStyle ?: mDefaultPolygonStyle
        return ModelPolygonStyle(
            fillColor = polygonStyle.fillColor,
            strokeColor = polygonStyle.getStrokeColor(),
            strokeWidth = polygonStyle.getStrokeWidth(),
            geodesic = polygonStyle.isGeodesic(),
            zIndex = polygonStyle.getZIndex(),
            clickable = polygonStyle.isClickable(),
            visible = polygonStyle.isVisible(),
        )
    }

    private fun toModelFeature(feature: GeoJsonFeature): ModelFeature {
        val existing = mFeatureMap[feature]
        if (existing != null) return existing

        val geometry = feature.getGeometry()!!
        val modelGeometry = toModelGeometry(geometry)
        val properties = feature.getPropertyKeys().associateWith { feature.getProperty(it) as Any }

        val style =
            when (modelGeometry) {
                is ModelPointGeometry -> buildPointStyle(feature)
                is ModelLineString -> buildLineStyle(feature)
                is ModelPolygon -> buildPolygonStyle(feature)
                is ModelMultiGeometry -> {
                    when (geometry) {
                        is GeoJsonMultiPolygon -> buildPolygonStyle(feature)
                        is GeoJsonMultiLineString -> buildLineStyle(feature)
                        is GeoJsonMultiPoint -> buildPointStyle(feature)
                        else ->
                            ModelCompositeStyle(
                                pointStyle = buildPointStyle(feature),
                                lineStyle = buildLineStyle(feature),
                                polygonStyle = buildPolygonStyle(feature),
                            )
                    }
                }
                else -> null
            }

        val modelFeature = ModelFeature(modelGeometry, style, properties)
        mFeatureMap[feature] = modelFeature
        mModelToLegacyFeatures[modelFeature] = feature
        return modelFeature
    }

    private fun toModelGeometry(geometry: Geometry): ModelGeometry =
        when (geometry) {
            is GeoJsonPoint -> {
                ModelPointGeometry(
                    ModelPoint(
                        geometry.getCoordinates().latitude,
                        geometry.getCoordinates().longitude,
                        geometry.getAltitude(),
                    ),
                )
            }

            is GeoJsonLineString -> {
                ModelLineString(
                    geometry.getCoordinates().map {
                        ModelPoint(it.latitude, it.longitude)
                    },
                )
            }

            is GeoJsonPolygon -> {
                ModelPolygon(
                    geometry.getOuterBoundaryCoordinates().map {
                        ModelPoint(
                            it.latitude,
                            it.longitude,
                        )
                    },
                    geometry.getInnerBoundaryCoordinates().map { inner ->
                        inner.map {
                            ModelPoint(it.latitude, it.longitude)
                        }
                    },
                )
            }

            is MultiGeometry -> {
                ModelMultiGeometry(
                    geometry.getGeometryObject().map { toModelGeometry(it) },
                )
            }

            else -> {
                throw IllegalArgumentException("Unknown geometry type")
            }
        }


    override val features: Iterable<GeoJsonFeature>
        get() = mFeatures

    public fun addFeature(feature: GeoJsonFeature) {
        if (!mFeatures.contains(feature)) {
            mFeatures.add(feature)
            feature.addObserver(mFeatureObserver)
        }
        if (mIsLayerOnMap && feature.getGeometry() != null) {
            mRenderer?.addFeature(toModelFeature(feature))
        }
    }

    public fun removeFeature(feature: GeoJsonFeature) {
        if (!mFeatures.remove(feature)) return
        feature.deleteObserver(mFeatureObserver)
        val modelFeature = mFeatureMap.remove(feature)
        if (modelFeature != null) {
            mModelToLegacyFeatures.remove(modelFeature)
            if (mIsLayerOnMap) {
                mRenderer?.removeFeature(modelFeature)
            }
        }
    }

    private fun onFeatureChanged(feature: GeoJsonFeature) {
        mFeatureMap.remove(feature)?.let { oldModel ->
            mModelToLegacyFeatures.remove(oldModel)
            if (mIsLayerOnMap) mRenderer?.removeFeature(oldModel)
        }
        if (mFeatures.contains(feature) && feature.getGeometry() != null) {
            val updatedModel = toModelFeature(feature)
            if (mIsLayerOnMap) mRenderer?.addFeature(updatedModel)
        }
    }

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

    private fun findLegacyFeatureForMapObject(mapObject: Any): GeoJsonFeature? {
        val renderer = mRenderer ?: return null
        val modelFeature = renderer.getFeatureForMapObject(mapObject) ?: return null
        return mModelToLegacyFeatures[modelFeature]
    }

    public fun getBoundingBox(): LatLngBounds? = mBoundingBox

    override fun isLayerOnMap(): Boolean = mIsLayerOnMap

    override fun toString(): String =
        StringBuilder("Collection{")
            .apply {
                append("\n Bounding box=").append(mBoundingBox)
                append("\n}\n")
            }.toString()
}
