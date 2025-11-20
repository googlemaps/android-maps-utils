package com.google.maps.android.data.parser

import java.io.InputStream

/**
 * Interface for a file parser that transforms an input stream
 * from a specific format into the unified [GeoData] model.
 */
interface GeoFileParser {
    /**
     * Parses the given input stream.
     * @param inputStream The stream of the file to parse.
     * @return A [GeoData] object representing the parsed data.
     * @throws IllegalArgumentException if the file is malformed.
     */
    fun parse(inputStream: InputStream): GeoData
}
