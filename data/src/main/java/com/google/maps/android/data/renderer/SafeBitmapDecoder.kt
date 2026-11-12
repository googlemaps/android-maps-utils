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
import android.graphics.BitmapFactory
import java.io.ByteArrayOutputStream
import java.io.InputStream

/**
 * Central security boundary for bitmap data originating in KML, GeoJSON, or KMZ input.
 *
 * Encoded byte limits do not bound bitmap memory: a small, highly compressible image can require
 * several bytes per pixel after decoding. Always inspect dimensions before allocating pixels, then
 * verify the actual allocation because row stride and decoder configuration can exceed estimates.
 */
internal object SafeBitmapDecoder {
    internal const val MAX_ENCODED_BYTES: Int = 10 * 1024 * 1024
    internal const val MAX_DECODED_BYTES: Long = 16L * 1024 * 1024

    private const val ARGB_8888_BYTES_PER_PIXEL = 4L
    private const val READ_BUFFER_SIZE = 8192

    internal fun decode(bytes: ByteArray): Bitmap? {
        if (bytes.isEmpty() || bytes.size > MAX_ENCODED_BYTES) return null

        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeByteArray(bytes, 0, bytes.size, bounds)
        if (!dimensionsFitBudget(bounds.outWidth, bounds.outHeight)) return null

        val options = BitmapFactory.Options().apply { inPreferredConfig = Bitmap.Config.ARGB_8888 }
        val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size, options) ?: return null
        return bitmap.takeIfWithinAllocationLimit()
    }

    internal fun decode(input: InputStream): Bitmap? {
        val encoded = readBounded(input) ?: return null
        return decode(encoded)
    }

    internal fun Bitmap.takeIfWithinAllocationLimit(): Bitmap? {
        if (allocationByteCount.toLong() <= MAX_DECODED_BYTES) return this
        recycle()
        return null
    }

    internal fun Bitmap.allocationKilobytes(): Int = maxOf(1, (allocationByteCount + 1023) / 1024)

    internal fun dimensionsFitBudget(width: Int, height: Int): Boolean {
        if (width <= 0 || height <= 0) return false
        val pixelCount = width.toLong() * height.toLong()
        return pixelCount <= MAX_DECODED_BYTES / ARGB_8888_BYTES_PER_PIXEL
    }

    private fun readBounded(input: InputStream): ByteArray? {
        val output = ByteArrayOutputStream(READ_BUFFER_SIZE)
        val buffer = ByteArray(READ_BUFFER_SIZE)
        var total = 0
        while (true) {
            val remainingWithSentinel = MAX_ENCODED_BYTES - total + 1
            val read = input.read(buffer, 0, minOf(buffer.size, remainingWithSentinel))
            if (read == -1) break
            if (read == 0) continue
            total += read
            if (total > MAX_ENCODED_BYTES) return null
            output.write(buffer, 0, read)
        }
        return output.toByteArray()
    }
}
