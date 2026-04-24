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
