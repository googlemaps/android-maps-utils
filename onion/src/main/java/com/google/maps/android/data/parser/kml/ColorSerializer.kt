package com.google.maps.android.data.parser.kml

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

internal object ColorSerializer : KSerializer<Int> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("Color", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: Int) {
        val argb =
            String.format("#%08x", value) // returns aabbggrr, e.g. #ff0000ff for blue
        val a = argb.substring(1..2)
        val r = argb.substring(3..4)
        val g = argb.substring(5..6)
        val b = argb.substring(7..8)
        encoder.encodeString("$a$b$g$r")
    }

    override fun deserialize(decoder: Decoder): Int {
        val abgr = decoder.decodeString()
        return abgr.toLong(16).toInt()
    }
}