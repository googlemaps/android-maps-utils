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
        fun canParse(header: String): Boolean {
            return header.contains("<kml")
        }
    }
}
