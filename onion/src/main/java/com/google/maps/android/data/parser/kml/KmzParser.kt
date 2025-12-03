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
package com.google.maps.android.data.parser.kml

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.io.BufferedInputStream
import java.io.ByteArrayInputStream
import java.io.InputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream

/**
 * Interface for decoding images, allowing for easier testing.
 */
interface ImageDecoder {
    fun decode(bytes: ByteArray): Bitmap?
}

/**
 * Default implementation using Android's BitmapFactory.
 */
class AndroidImageDecoder : ImageDecoder {
    override fun decode(bytes: ByteArray): Bitmap? {
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
    }
}

/**
 * Parser for KMZ files (zipped KML).
 *
 * Extracts the main KML file and any associated images from the Zip stream.
 */
class KmzParser(private val imageDecoder: ImageDecoder = AndroidImageDecoder()) {

    /**
     * Parses a KMZ file from an InputStream.
     *
     * @param inputStream The InputStream of the KMZ file.
     * @return The parsed KML object, including extracted images.
     */
    fun parse(inputStream: InputStream): Kml {
        val images = mutableMapOf<String, Bitmap>()
        var kml: Kml? = null
        val zipInputStream = ZipInputStream(BufferedInputStream(inputStream))

        try {
            var entry: ZipEntry? = zipInputStream.nextEntry
            while (entry != null) {
                val name = entry.name
                if (!entry.isDirectory) {
                    if (name.endsWith(".kml", ignoreCase = true) && kml == null) {
                        // Found the KML file (first one found is usually the main one in KMZ)
                        // We need to read it into a byte array because we can't close the ZipInputStream yet
                        val bytes = zipInputStream.readBytes()
                        kml = KmlParser().parse(ByteArrayInputStream(bytes))
                    } else {
                        // Try to decode as image
                        val bytes = zipInputStream.readBytes()
                        val bitmap = imageDecoder.decode(bytes)
                        if (bitmap != null) {
                            images[name] = bitmap
                        }
                    }
                }
                zipInputStream.closeEntry()
                entry = zipInputStream.nextEntry
            }
        } finally {
            zipInputStream.close()
        }

        if (kml == null) {
            throw IllegalArgumentException("No KML file found in KMZ stream")
        }

        return kml.copy(images = images)
    }

    companion object {
        fun canParse(header: String): Boolean {
            return header.startsWith("PK") // Zip file signature
        }
    }
}
