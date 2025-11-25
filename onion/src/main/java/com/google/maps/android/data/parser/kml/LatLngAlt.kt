package com.google.maps.android.data.parser.kml

import kotlinx.serialization.Serializable

@Serializable(with = LatLngAltSerializer::class)
data class LatLngAlt(
    val latitude: Double,
    val longitude: Double,
    val altitude: Double? = null,
)