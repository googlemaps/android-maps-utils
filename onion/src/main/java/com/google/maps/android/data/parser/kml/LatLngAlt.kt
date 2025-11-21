package com.google.maps.android.data.parser.kml

data class LatLngAlt(
    val latitude: Double,
    val longitude: Double,
    val altitude: Double? = null,
) {
    companion object Companion {
        fun fromString(input: String): LatLngAlt? {
            val parts = input.split(",").map { it.trim().toDouble() }

            if (parts.size < 2) {
                return null
            }

            return LatLngAlt(
                latitude = parts[1],
                longitude = parts[0],
                altitude = parts.getOrNull(2)
            )
        }
    }
}