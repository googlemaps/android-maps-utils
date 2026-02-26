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

import kotlinx.serialization.decodeFromString
import nl.adaptivity.xmlutil.serialization.XML
import java.io.InputStream
import java.nio.charset.StandardCharsets

class KmlParser {
    private val xml = XML {
        defaultPolicy {
            ignoreUnknownChildren()
            isCollectingNSAttributes = true
        }
    }

    fun parseAsKml(inputStream: InputStream): Kml {
        val xmlContent = inputStream.bufferedReader(StandardCharsets.UTF_8).use { it.readText() }
        return xml.decodeFromString<Kml>(xmlContent)
    }

    fun parse(inputStream: InputStream): Kml {
        return parseAsKml(inputStream)
    }

    companion object {
        val SUPPORTED_EXTENSIONS = setOf("kml")
        fun canParse(header: String): Boolean {
            return header.contains("<kml")
        }
    }
}
