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
package com.google.maps.android.data.renderer

import android.graphics.Bitmap
import com.google.maps.android.data.parser.kml.KmzParser
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.GraphicsMode
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer
import java.util.zip.CRC32
import java.util.zip.DeflaterOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class SafeBitmapDecoderTest {
    @Test
    fun `decode accepts a normal image`() {
        val bitmap = SafeBitmapDecoder.decode(makeUniformRgbaPng(width = 128, height = 128))

        assertNotNull(bitmap)
        assertEquals(128, bitmap!!.width)
        assertEquals(128, bitmap.height)
    }

    @Test
    fun `decode rejects image whose pixels exceed decoded memory budget`() {
        val encoded = makeUniformRgbaPng(width = 4096, height = 4096)

        assertTrue(encoded.size < 256 * 1024)
        assertNull(SafeBitmapDecoder.decode(encoded))
    }

    @Test
    fun `KMZ parser does not retain image whose decoded size exceeds limit`() {
        val encoded = makeUniformRgbaPng(width = 4096, height = 4096)
        val kmz =
            ByteArrayOutputStream().use { output ->
                ZipOutputStream(output).use { zip ->
                    zip.putNextEntry(ZipEntry("doc.kml"))
                    zip.write("<kml xmlns=\"http://www.opengis.net/kml/2.2\"><Document/></kml>".toByteArray())
                    zip.closeEntry()
                    zip.putNextEntry(ZipEntry("images/oversized.png"))
                    zip.write(encoded)
                    zip.closeEntry()
                }
                output.toByteArray()
            }

        val parsed = KmzParser().parse(ByteArrayInputStream(kmz))

        assertFalse(parsed.images.containsKey("images/oversized.png"))
    }

    @Test
    fun `decode stream stops after encoded byte limit`() {
        val input = CountingInputStream(SafeBitmapDecoder.MAX_ENCODED_BYTES + 1)

        assertNull(SafeBitmapDecoder.decode(input))
        assertEquals(SafeBitmapDecoder.MAX_ENCODED_BYTES + 1, input.bytesRead)
    }

    @Test
    fun `dimension arithmetic rejects invalid and excessive sizes without overflow`() {
        assertFalse(SafeBitmapDecoder.dimensionsFitBudget(0, 1))
        assertFalse(SafeBitmapDecoder.dimensionsFitBudget(Int.MAX_VALUE, Int.MAX_VALUE))
        assertTrue(SafeBitmapDecoder.dimensionsFitBudget(2048, 2048))
    }

    @Test
    fun `cache accounting rounds small allocations up instead of treating them as free`() {
        val bitmap = Bitmap.createBitmap(10, 10, Bitmap.Config.ARGB_8888)

        assertEquals(1, SafeBitmapDecoder.run { bitmap.allocationKilobytes() })
    }

    private class CountingInputStream(private val size: Int) : ByteArrayInputStream(ByteArray(size)) {
        var bytesRead: Int = 0
            private set

        override fun read(buffer: ByteArray, offset: Int, length: Int): Int {
            val read = super.read(buffer, offset, length)
            if (read > 0) bytesRead += read
            return read
        }
    }

    private fun makeUniformRgbaPng(width: Int, height: Int): ByteArray {
        val compressed = ByteArrayOutputStream()
        DeflaterOutputStream(compressed).use { deflater ->
            val row = ByteArray(1 + width * 4)
            repeat(height) { deflater.write(row) }
        }

        return ByteArrayOutputStream().use { png ->
            png.write(byteArrayOf(0x89.toByte(), 0x50, 0x4e, 0x47, 0x0d, 0x0a, 0x1a, 0x0a))
            val header = ByteBuffer.allocate(13)
                .putInt(width)
                .putInt(height)
                .put(8)
                .put(6)
                .put(0)
                .put(0)
                .put(0)
                .array()
            writeChunk(png, "IHDR", header)
            writeChunk(png, "IDAT", compressed.toByteArray())
            writeChunk(png, "IEND", ByteArray(0))
            png.toByteArray()
        }
    }

    private fun writeChunk(output: ByteArrayOutputStream, type: String, data: ByteArray) {
        output.write(ByteBuffer.allocate(4).putInt(data.size).array())
        val typeBytes = type.toByteArray(Charsets.US_ASCII)
        output.write(typeBytes)
        output.write(data)
        val crc = CRC32()
        crc.update(typeBytes)
        crc.update(data)
        output.write(ByteBuffer.allocate(4).putInt(crc.value.toInt()).array())
    }
}
