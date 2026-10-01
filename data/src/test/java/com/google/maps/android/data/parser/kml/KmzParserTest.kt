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
import io.mockk.every
import io.mockk.mockkStatic
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.Assert.assertThrows
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

class KmzParserTest {
    @Test
    fun `parse extracts KML and images from KMZ`() {
        val kmlContent =
            """
            <kml xmlns="http://www.opengis.net/kml/2.2">
                <Document>
                    <Placemark>
                        <name>Test Placemark</name>
                    </Placemark>
                </Document>
            </kml>
            """.trimIndent()

        val kmzStream =
            createKmzStream(
                "doc.kml" to kmlContent.toByteArray(),
                "image.png" to ByteArray(10), // Dummy image data
            )

        // Mock ImageDecoder
        val mockBitmap = io.mockk.mockk<Bitmap>(relaxed = true)
        val mockImageDecoder =
            object : ImageDecoder {
                override fun decode(bytes: ByteArray): Bitmap? = if (bytes.size == 10) mockBitmap else null
            }

        val parser = KmzParser(mockImageDecoder)
        val kml = parser.parse(kmzStream)

        assertNotNull(kml.document)
        assertEquals(
            "Test Placemark",
            kml.document
                ?.placemarks
                ?.first()
                ?.name,
        )
        assertTrue(kml.images.containsKey("image.png"))
        assertEquals(mockBitmap, kml.images["image.png"])
    }

    /**
     * Demonstrates issue #1790: KMZ directory entries bypass the cumulative decompression limit.
     *
     * A malicious KMZ can package a large compressed payload inside an entry named with a trailing slash
     * (e.g., "padding/"). Without proper accounting or validation, ZipInputStream.closeEntry() silently
     * inflates and drains the entry without incrementing the uncompressed byte counter, bypassing
     * the configured decompression budget.
     */
    @Test
    fun `parse rejects directory entry with payload exceeding decompression limit`() {
        val kmlContent =
            """
            <kml xmlns="http://www.opengis.net/kml/2.2">
                <Document>
                    <Placemark>
                        <name>Test Placemark</name>
                    </Placemark>
                </Document>
            </kml>
            """.trimIndent()

        // 2,000 bytes payload in a directory entry, configured with a 1,000-byte limit
        val directoryPayload = ByteArray(2000) { 0x41 }
        val kmzStream =
            createKmzStream(
                "padding/" to directoryPayload,
                "doc.kml" to kmlContent.toByteArray(),
            )

        val parser = KmzParser(maxKmzUncompressedTotalSize = 1000L)

        assertThrows(IOException::class.java) {
            parser.parse(kmzStream)
        }
    }

    /**
     * Verifies that standard well-formed KMZ files containing empty directory entries
     * (e.g. "images/") parse successfully without false-positive zip bomb rejections.
     */
    @Test
    fun `parse allows legitimate empty directory entries`() {
        val kmlContent =
            """
            <kml xmlns="http://www.opengis.net/kml/2.2">
                <Document>
                    <Placemark>
                        <name>Doc in Dir</name>
                    </Placemark>
                </Document>
            </kml>
            """.trimIndent()

        val kmzStream =
            createKmzStream(
                "images/" to ByteArray(0),
                "doc.kml" to kmlContent.toByteArray(),
            )

        val parser = KmzParser()
        val kml = parser.parse(kmzStream)

        assertNotNull(kml.document)
        assertEquals(
            "Doc in Dir",
            kml.document
                ?.placemarks
                ?.first()
                ?.name,
        )
    }

    private fun createKmzStream(vararg entries: kotlin.Pair<String, ByteArray>): ByteArrayInputStream {
        val baos = ByteArrayOutputStream()
        val zos = ZipOutputStream(baos)

        entries.forEach { (name, data) ->
            zos.putNextEntry(ZipEntry(name))
            zos.write(data)
            zos.closeEntry()
        }

        zos.close()
        return ByteArrayInputStream(baos.toByteArray())
    }
}
