package com.google.maps.android.data.parser.kml

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

internal object BooleanSerializer : KSerializer<Boolean> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("Boolean", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: Boolean) {
        encoder.encodeString(if (value) "1" else "0")
    }

    override fun deserialize(decoder: Decoder): Boolean {
        return decoder.decodeString().let {
            it.equals("true", ignoreCase = true) || it == "1"
        }
    }
}