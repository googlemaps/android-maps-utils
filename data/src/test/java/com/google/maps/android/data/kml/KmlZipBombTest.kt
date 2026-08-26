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

package com.google.maps.android.data.kml

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import org.junit.Assert.assertThrows
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

/**
 * Unit tests verifying zip-bomb and Denial of Service (DoS) protections when parsing KMZ (zipped KML) files.
 *
 * KMZ files are compressed zip archives that can contain malicious payloads such as recursive zip bombs,
 * excessive entry counts, or decompression size bombs designed to exhaust device memory.
 * These tests ensure [KmlLayer] enforces strict boundaries on entry counts and uncompressed byte size.
 */
@RunWith(RobolectricTestRunner::class)
class KmlZipBombTest {

    /**
     * Verifies that a well-formed KMZ archive containing a standard `doc.kml` entry
     * decompresses and initializes [KmlLayer] successfully without errors.
     */
    @Test
    fun testValidKmz() {
        // Construct an in-memory KMZ containing a valid, minimal KML document
        val baos = ByteArrayOutputStream()
        ZipOutputStream(baos).use { zos ->
            zos.putNextEntry(ZipEntry("doc.kml"))
            zos.write("<kml xmlns=\"http://www.opengis.net/kml/2.2\"><Document></Document></kml>".toByteArray())
            zos.closeEntry()
        }

        val context: Context = ApplicationProvider.getApplicationContext()
        val layer = KmlLayer(null, ByteArrayInputStream(baos.toByteArray()), context)
        assertThat(layer).isNotNull()
    }

    /**
     * Verifies that an archive exceeding the maximum allowed entry count (200 entries by default)
     * is rejected with an [IOException] to protect against zip bomb expansion exhaustion.
     */
    @Test
    fun testMaxEntriesLimit() {
        // Construct an in-memory KMZ containing 202 entries (exceeding the default 200 limit)
        val baos = ByteArrayOutputStream()
        ZipOutputStream(baos).use { zos ->
            for (i in 0 until 202) {
                zos.putNextEntry(ZipEntry("entry$i.txt"))
                zos.write("data".toByteArray())
                zos.closeEntry()
            }
        }

        val context: Context = ApplicationProvider.getApplicationContext()
        assertThrows(IOException::class.java) {
            KmlLayer(null, ByteArrayInputStream(baos.toByteArray()), context)
        }
    }

    /**
     * Verifies that an archive whose uncompressed contents exceed the maximum total allowed size
     * (50MB by default) is rejected with an [IOException] to prevent out-of-memory crashes.
     */
    @Test
    fun testMaxSizeLimit() {
        // Construct an in-memory KMZ containing 51 MB of uncompressed payload (exceeding 50 MB limit)
        val baos = ByteArrayOutputStream()
        ZipOutputStream(baos).use { zos ->
            zos.putNextEntry(ZipEntry("large_entry.kml"))
            val largeData = ByteArray(1024 * 1024) // 1 MB chunk
            repeat(51) {
                zos.write(largeData)
            }
            zos.closeEntry()
        }

        val context: Context = ApplicationProvider.getApplicationContext()
        assertThrows(IOException::class.java) {
            KmlLayer(null, ByteArrayInputStream(baos.toByteArray()), context)
        }
    }
}
