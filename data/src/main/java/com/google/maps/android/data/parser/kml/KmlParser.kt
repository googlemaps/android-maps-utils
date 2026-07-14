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

import nl.adaptivity.xmlutil.EventType
import nl.adaptivity.xmlutil.XmlDelegatingReader
import nl.adaptivity.xmlutil.XmlException
import nl.adaptivity.xmlutil.XmlReader
import nl.adaptivity.xmlutil.serialization.XML
import nl.adaptivity.xmlutil.xmlStreaming
import java.io.InputStream
import java.nio.charset.StandardCharsets

class KmlParser {
    private val xml =
        XML {
            defaultPolicy {
                ignoreUnknownChildren()
                isCollectingNSAttributes = true
            }
        }

    fun parseAsKml(inputStream: InputStream): Kml {
        val xmlContent = inputStream.bufferedReader(StandardCharsets.UTF_8).use { it.readText() }
        return DepthLimitingReader(xmlStreaming.newReader(xmlContent)).use { reader ->
            xml.decodeFromReader(Kml.serializer(), reader)
        }
    }

    fun parse(inputStream: InputStream): Kml = parseAsKml(inputStream)

    companion object {
        val SUPPORTED_EXTENSIONS = setOf("kml")

        /**
         * Maximum element nesting depth accepted when parsing. The generated serializers
         * recurse once per nested element (e.g. `<Folder>` in `<Folder>`, `<MultiGeometry>`
         * in `<MultiGeometry>`), so without a bound a maliciously deep document causes a
         * StackOverflowError. Legitimate KML stays far below this limit.
         */
        const val MAX_ELEMENT_DEPTH = 50

        fun canParse(header: String): Boolean = header.contains("<kml")
    }
}

/** Fails parsing with an [XmlException] once elements nest deeper than [KmlParser.MAX_ELEMENT_DEPTH]. */
private class DepthLimitingReader(delegate: XmlReader) : XmlDelegatingReader(delegate) {
    override fun next(): EventType = checkDepth(super.next())

    override fun nextTag(): EventType = checkDepth(super.nextTag())

    private fun checkDepth(event: EventType): EventType {
        if (event == EventType.START_ELEMENT && depth > KmlParser.MAX_ELEMENT_DEPTH) {
            throw XmlException(
                "KML element nesting exceeds the maximum depth of ${KmlParser.MAX_ELEMENT_DEPTH}"
            )
        }
        return event
    }
}
