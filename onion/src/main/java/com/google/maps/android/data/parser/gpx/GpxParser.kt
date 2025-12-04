/*
 * Copyright 2025 Google LLC
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
package com.google.maps.android.data.parser.gpx

import kotlinx.serialization.decodeFromString
import nl.adaptivity.xmlutil.serialization.XML
import java.io.InputStream
import java.nio.charset.StandardCharsets

/**
 * A parser for GPX (GPS Exchange Format) files.
 *
 * Uses the `pdvrieze/xmlutil` library for XML parsing.
 * Supports parsing GPX 1.0 and 1.1 formats (normalizing 1.0 to 1.1).
 */
class GpxParser {
    private val xml = XML {
        defaultPolicy {
            ignoreUnknownChildren()
            isCollectingNSAttributes = true
        }
    }

    fun parse(inputStream: InputStream): Gpx {
        val xmlContent = inputStream.bufferedReader(StandardCharsets.UTF_8).use { it.readText() }
        // Normalize GPX 1.0 namespace to 1.1 to allow parsing with the same model
        // (The structure for waypoints and tracks is compatible enough for our needs)
        val normalizedXml = xmlContent.replace("http://www.topografix.com/GPX/1/0", "http://www.topografix.com/GPX/1/1")
        return xml.decodeFromString<Gpx>(normalizedXml)
    }

    companion object {
        val SUPPORTED_EXTENSIONS = setOf("gpx")
        fun canParse(header: String): Boolean {
            return header.contains("<gpx")
        }
    }
}
