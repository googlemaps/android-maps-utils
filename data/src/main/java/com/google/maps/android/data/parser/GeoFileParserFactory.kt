package com.google.maps.android.data.parser

import com.google.maps.android.data.parser.geojson.GeoJsonParser
import com.google.maps.android.data.parser.kml.KmlParser

enum class FileType {
    KML, GEOJSON, UNKNOWN
}

class GeoFileParserFactory {
    /**
     * Determines the file type from a file name extension.
     * A more robust implementation might inspect the file's magic numbers or content.
     */
    fun getFileType(fileName: String): FileType {
        return when (fileName.substringAfterLast('.').lowercase()) {
            "kml" -> FileType.KML
            "geojson", "json" -> FileType.GEOJSON
            else -> FileType.UNKNOWN
        }
    }

    /**
     * Returns the appropriate parser for the given file type.
     */
    fun createParser(fileType: FileType): GeoFileParser {
        return when (fileType) {
            FileType.KML -> KmlParser()
            FileType.GEOJSON -> GeoJsonParser()
            else -> throw IllegalArgumentException("Unknown or unsupported file type")
        }
    }
}
