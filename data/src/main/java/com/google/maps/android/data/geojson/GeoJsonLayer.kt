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
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.maps.android.collections.GroundOverlayManager
import com.google.maps.android.collections.MarkerManager
import com.google.maps.android.collections.PolygonManager
import com.google.maps.android.collections.PolylineManager
import com.google.maps.android.data.Geometry
import com.google.maps.android.data.Layer
import com.google.maps.android.data.parser.geojson.GeoJsonParser
import com.google.maps.android.data.renderer.UrlIconProvider
import com.google.maps.android.data.renderer.mapview.MapViewRenderer
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.io.InputStream

@Deprecated("Use the new platform-agnostic data layer and renderer instead.")
public class GeoJsonLayer : Layer {
    private val mFeatures = mutableListOf<GeoJsonFeature>()
    private var mBoundingBox: LatLngBounds? = null
    private var mRenderer: MapViewRenderer? = null
    private var mIsLayerOnMap = false
    private val mFeatureMap = HashMap<GeoJsonFeature, com.google.maps.android.data.renderer.model.Feature>()
    private val mModelToLegacyFeatures = HashMap<com.google.maps.android.data.renderer.model.Feature, GeoJsonFeature>()
    private var mFeatureClickListener: OnFeatureClickListener? = null

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
        val stream = context.resources.openRawResource(resourceId)
        parseGeoJson(stream)
        initializeRenderer(map)
    }

    private fun parseGeoJson(stream: InputStream) {
        val parsed = GeoJsonParser().parse(stream) ?: throw JSONException("Failed to parse GeoJSON")

        // Map parsed object to our legacy GeoJsonFeature instances
        when (parsed) {
            is com.google.maps.android.data.parser.geojson.GeoJsonFeatureCollection -> {
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

            is com.google.maps.android.data.parser.geojson.GeoJsonFeature -> {
                val legacyGeometry = parsed.geometry?.let { toLegacyGeometry(it) }
                val legacyProps = parsed.properties?.filterValues { it != null }?.mapValues { it.value as String }
                val legacyFeature = GeoJsonFeature(legacyGeometry, parsed.id, legacyProps, null)

                legacyFeature.pointStyle = mDefaultPointStyle
                legacyFeature.lineStringStyle = mDefaultLineStringStyle
                legacyFeature.polygonStyle = mDefaultPolygonStyle

                mFeatures.add(legacyFeature)
            }

            is com.google.maps.android.data.parser.geojson.GeoJsonGeometry -> {
                val legacyGeometry = toLegacyGeometry(parsed)
                val legacyFeature = GeoJsonFeature(legacyGeometry, null, null, null)

                legacyFeature.pointStyle = mDefaultPointStyle
                legacyFeature.lineStringStyle = mDefaultLineStringStyle
                legacyFeature.polygonStyle = mDefaultPolygonStyle

                mFeatures.add(legacyFeature)
            }
        }

        // Calculate bounding box
        calculateBoundingBox()
    }

    private fun initializeRenderer(map: GoogleMap?) {
        mRenderer = map?.let { MapViewRenderer(it, UrlIconProvider()) }
    }

    private fun toLegacyGeometry(geometry: com.google.maps.android.data.parser.geojson.GeoJsonGeometry): Geometry =
        when (geometry) {
            is com.google.maps.android.data.parser.geojson.GeoJsonPoint -> {
                GeoJsonPoint(LatLng(geometry.coordinates.lat, geometry.coordinates.lng), geometry.coordinates.alt)
            }

            is com.google.maps.android.data.parser.geojson.GeoJsonLineString -> {
                GeoJsonLineString(geometry.coordinates.map { LatLng(it.lat, it.lng) }, geometry.coordinates.mapNotNull { it.alt })
            }

            is com.google.maps.android.data.parser.geojson.GeoJsonPolygon -> {
                GeoJsonPolygon(geometry.coordinates.map { ring -> ring.map { LatLng(it.lat, it.lng) } })
            }

            is com.google.maps.android.data.parser.geojson.GeoJsonMultiPoint -> {
                GeoJsonMultiPoint(geometry.coordinates.map { GeoJsonPoint(LatLng(it.lat, it.lng), it.alt) })
            }

            is com.google.maps.android.data.parser.geojson.GeoJsonMultiLineString -> {
                GeoJsonMultiLineString(geometry.coordinates.map { GeoJsonLineString(it.map { c -> LatLng(c.lat, c.lng) }) })
            }

            is com.google.maps.android.data.parser.geojson.GeoJsonMultiPolygon -> {
                GeoJsonMultiPolygon(geometry.coordinates.map { GeoJsonPolygon(it.map { ring -> ring.map { c -> LatLng(c.lat, c.lng) } }) })
            }

            is com.google.maps.android.data.parser.geojson.GeoJsonGeometryCollection -> {
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

                is com.google.maps.android.data.MultiGeometry -> {
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
        multiGeometry: com.google.maps.android.data.MultiGeometry,
        builder: LatLngBounds.Builder,
    ) {
        multiGeometry.getGeometryObject().forEach { geom ->
            when (geom) {
                is GeoJsonPoint -> builder.include(geom.getCoordinates())
                is GeoJsonLineString -> geom.getCoordinates().forEach { builder.include(it) }
                is GeoJsonPolygon -> geom.getOuterBoundaryCoordinates().forEach { builder.include(it) }
                is com.google.maps.android.data.MultiGeometry -> includeMultiGeometryBounds(geom, builder)
            }
        }
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
        mFeatures.forEach { feature ->
            val modelFeature = toModelFeature(feature)
            renderer.addFeature(modelFeature)
        }
        mIsLayerOnMap = true
    }

    override fun removeLayerFromMap() {
        val renderer = mRenderer ?: return
        mFeatures.forEach { feature ->
            val modelFeature = toModelFeature(feature)
            renderer.removeFeature(modelFeature)
        }
        mIsLayerOnMap = false
    }

    private fun toModelFeature(feature: GeoJsonFeature): com.google.maps.android.data.renderer.model.Feature {
        val existing = mFeatureMap[feature]
        if (existing != null) return existing

        val geometry = feature.getGeometry()!!
        val modelGeometry = toModelGeometry(geometry)
        val properties = feature.getPropertyKeys().associateWith { feature.getProperty(it) as Any }

        val style =
            when (modelGeometry) {
                is com.google.maps.android.data.renderer.model.PointGeometry -> {
                    val pointStyle = feature.pointStyle ?: mDefaultPointStyle
                    com.google.maps.android.data.renderer.model.PointStyle(
                        color = 0,
                        anchorU = pointStyle.getAnchorU(),
                        anchorV = pointStyle.getAnchorV(),
                        heading = pointStyle.getRotation(),
                    )
                }

                is com.google.maps.android.data.renderer.model.LineString -> {
                    val lineStyle = feature.lineStringStyle ?: mDefaultLineStringStyle
                    com.google.maps.android.data.renderer.model.LineStyle(
                        color = lineStyle.color,
                        width = lineStyle.getWidth(),
                        geodesic = lineStyle.isGeodesic(),
                    )
                }

                is com.google.maps.android.data.renderer.model.Polygon -> {
                    val polygonStyle = feature.polygonStyle ?: mDefaultPolygonStyle
                    com.google.maps.android.data.renderer.model.PolygonStyle(
                        fillColor = polygonStyle.fillColor,
                        strokeColor = polygonStyle.getStrokeColor(),
                        strokeWidth = polygonStyle.getStrokeWidth(),
                        geodesic = polygonStyle.isGeodesic(),
                    )
                }

                else -> {
                    null
                }
            }

        val modelFeature =
            com.google.maps.android.data.renderer.model
                .Feature(modelGeometry, style, properties)
        mFeatureMap[feature] = modelFeature
        mModelToLegacyFeatures[modelFeature] = feature
        return modelFeature
    }

    private fun toModelGeometry(geometry: Geometry): com.google.maps.android.data.renderer.model.Geometry =
        when (geometry) {
            is GeoJsonPoint -> {
                com.google.maps.android.data.renderer.model.PointGeometry(
                    com.google.maps.android.data.renderer.model.Point(
                        geometry.getCoordinates().latitude,
                        geometry.getCoordinates().longitude,
                        geometry.getAltitude(),
                    ),
                )
            }

            is GeoJsonLineString -> {
                com.google.maps.android.data.renderer.model.LineString(
                    geometry.getCoordinates().map {
                        com.google.maps.android.data.renderer.model
                            .Point(it.latitude, it.longitude)
                    },
                )
            }

            is GeoJsonPolygon -> {
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

            is com.google.maps.android.data.MultiGeometry -> {
                com.google.maps.android.data.renderer.model.MultiGeometry(
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
        mFeatures.add(feature)
        if (mIsLayerOnMap) {
            mRenderer?.addFeature(toModelFeature(feature))
        }
    }

    public fun removeFeature(feature: GeoJsonFeature) {
        mFeatures.remove(feature)
        val modelFeature = mFeatureMap.remove(feature)
        if (modelFeature != null) {
            mModelToLegacyFeatures.remove(modelFeature)
            if (mIsLayerOnMap) {
                mRenderer?.removeFeature(modelFeature)
            }
        }
    }

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

    private fun findLegacyFeatureForMapObject(mapObject: Any): GeoJsonFeature? {
        val renderer = mRenderer ?: return null
        val modelFeature = renderer.getFeatureForMapObject(mapObject) ?: return null
        return mModelToLegacyFeatures[modelFeature]
    }

    public fun getBoundingBox(): LatLngBounds? = mBoundingBox

    public fun isLayerOnMap(): Boolean = mIsLayerOnMap

    override fun toString(): String =
        StringBuilder("Collection{")
            .apply {
                append("\n Bounding box=").append(mBoundingBox)
                append("\n}\n")
            }.toString()
}
