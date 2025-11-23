package com.google.maps.android.data.parser.kml

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

internal object LatLngAltSerializer : KSerializer<LatLngAlt> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("LatLngAlt", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: LatLngAlt) {
        val altitude = value.altitude?.let { ",$it" } ?: ""
        encoder.encodeString("${value.longitude},${value.latitude}$altitude")
    }

    override fun deserialize(decoder: Decoder): LatLngAlt {
        return parse(decoder.decodeString())
    }

    internal fun parse(string: String): LatLngAlt {
        val parts = string.split(",").map { it.trim().toDouble() }
        return LatLngAlt(
            longitude = parts[0],
            latitude = parts[1],
            altitude = parts.getOrNull(2)
        )
    }
}