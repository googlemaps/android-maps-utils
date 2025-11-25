package com.google.maps.android.data.parser.geojson

// Using a sealed interface for all GeoJSON objects
sealed interface GeoJsonObject {
    val type: String
}

// Sealed interface for Geometry objects
sealed interface GeoJsonGeometry : GeoJsonObject

data class GeoJsonPoint(
    val coordinates: List<Double>
) : GeoJsonGeometry {
    override val type: String = "Point"
}

data class GeoJsonMultiPoint(
    val coordinates: List<List<Double>>
) : GeoJsonGeometry {
    override val type: String = "MultiPoint"
}

data class GeoJsonLineString(
    val coordinates: List<List<Double>>
) : GeoJsonGeometry {
    override val type: String = "LineString"
}

data class GeoJsonMultiLineString(
    val coordinates: List<List<List<Double>>>
) : GeoJsonGeometry {
    override val type: String = "MultiLineString"
}

data class GeoJsonPolygon(
    val coordinates: List<List<List<Double>>>
) : GeoJsonGeometry {
    override val type: String = "Polygon"
}

data class GeoJsonMultiPolygon(
    val coordinates: List<List<List<List<Double>>>>
) : GeoJsonGeometry {
    override val type: String = "MultiPolygon"
}

data class GeoJsonGeometryCollection(
    val geometries: List<GeoJsonGeometry>
) : GeoJsonGeometry {
    override val type: String = "GeometryCollection"
}

data class GeoJsonFeature(
    val geometry: GeoJsonGeometry?,
    val properties: Map<String, String?>?,
    val id: String? = null // id is optional and can be string or number
) : GeoJsonObject {
    override val type: String = "Feature"
}

data class GeoJsonFeatureCollection(
    val features: List<GeoJsonFeature>
) : GeoJsonObject {
    override val type: String = "FeatureCollection"
}
