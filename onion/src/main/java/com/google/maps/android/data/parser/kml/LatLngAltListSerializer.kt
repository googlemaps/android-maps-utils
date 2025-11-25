package com.google.maps.android.data.parser.kml

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

internal object LatLngAltListSerializer : KSerializer<List<LatLngAlt>> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("LatLngAltList", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: List<LatLngAlt>) {
        val string = value.joinToString(" ") { latLngAlt ->
            val altitude = latLngAlt.altitude?.let { ",$it" } ?: ""
            "${latLngAlt.longitude},${latLngAlt.latitude}$altitude"
        }
        encoder.encodeString(string)
    }

    override fun deserialize(decoder: Decoder): List<LatLngAlt> {
        return decoder.decodeString()
            .split(Regex("""\s+"""))
            .filter(String::isNotBlank)
            .map { LatLngAltSerializer.parse(it) }
    }
}
