package com.google.maps.android.data.parser.geojson

import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds

sealed class GeoJson {
    data class Point(val coordinates: LatLng) : GeoJson()
    data class MultiPoint(val points: List<Point>) : GeoJson()
    data class LineString(val coordinates: List<LatLng>) : GeoJson()
    data class MultiLineString(val lineStrings: List<LineString>) : GeoJson()
    data class Polygon(val coordinates: List<List<LatLng>>) : GeoJson()
    data class MultiPolygon(val polygons: List<Polygon>) : GeoJson()
    data class GeometryCollection(val geometries: List<GeoJson>) : GeoJson()
    data class Feature(
        val geometry: GeoJson?,
        val properties: Map<String, String?>,
        val id: String?,
        val boundingBox: LatLngBounds?
    ) : GeoJson()

    data class FeatureCollection(
        val features: List<Feature>,
        val boundingBox: LatLngBounds?
    ) : GeoJson()
}
