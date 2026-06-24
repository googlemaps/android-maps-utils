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

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.io.BufferedInputStream
import java.io.ByteArrayInputStream
import java.io.IOException
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
    override fun decode(bytes: ByteArray): Bitmap? = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
}

/**
 * Parser for KMZ files (zipped KML).
 *
 * Extracts the main KML file and any associated images from the Zip stream.
 */
class KmzParser(
    private val imageDecoder: ImageDecoder = AndroidImageDecoder(),
    private val maxKmzEntryCount: Int = 200,
    private val maxKmzUncompressedTotalSize: Long = 50 * 1024 * 1024,
) {
    /**
     * Wrapper for an InputStream that counts the number of bytes read and throws an IOException
     * if the limit is exceeded.
     */
    private class CountingInputStream(
        private val mIn: InputStream,
        private val mMaxBytes: Long,
    ) : InputStream() {
        private var mTotalBytes: Long = 0

        @Throws(IOException::class)
        override fun read(): Int {
            val b = mIn.read()
            if (b != -1) {
                mTotalBytes++
                checkLimit()
            }
            return b
        }

        @Throws(IOException::class)
        override fun read(b: ByteArray, off: Int, len: Int): Int {
            val n = mIn.read(b, off, len)
            if (n != -1) {
                mTotalBytes += n
                checkLimit()
            }
            return n
        }

        @Throws(IOException::class)
        override fun skip(n: Long): Long {
            val skipped = mIn.skip(n)
            if (skipped > 0) {
                mTotalBytes += skipped
                checkLimit()
            }
            return skipped
        }

        @Throws(IOException::class)
        private fun checkLimit() {
            if (mTotalBytes > mMaxBytes) {
                throw java.io.IOException("Zip bomb detected! Uncompressed size exceeds limit of $mMaxBytes bytes.")
            }
        }
    }

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
        val countingStream = CountingInputStream(zipInputStream, maxKmzUncompressedTotalSize)

        try {
            var entry: ZipEntry? = zipInputStream.nextEntry
            var entryCount = 0
            while (entry != null) {
                entryCount++
                if (entryCount > maxKmzEntryCount) {
                    throw java.io.IOException("Zip bomb detected! Max number of entries exceeded: $maxKmzEntryCount")
                }
                val name = entry.name
                if (!entry.isDirectory) {
                    if (name.endsWith(".kml", ignoreCase = true) && kml == null) {
                        // Found the KML file (first one found is usually the main one in KMZ)
                        // We need to read it into a byte array because we can't close the ZipInputStream yet
                        val bytes = countingStream.readBytes()
                        kml = KmlParser().parse(ByteArrayInputStream(bytes))
                    } else {
                        // Try to decode as image
                        val bytes = countingStream.readBytes()
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
        val SUPPORTED_EXTENSIONS = setOf("kmz")

        fun canParse(header: String): Boolean {
            return header.startsWith("PK") // Zip file signature
        }
    }
}
