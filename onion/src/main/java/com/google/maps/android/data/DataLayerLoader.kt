/*
 * Copyright 2026 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.maps.android.data

import android.content.Context
import com.google.maps.android.data.parser.geojson.GeoJsonParser
import com.google.maps.android.data.parser.gpx.GpxParser
import com.google.maps.android.data.parser.kml.KmlParser
import com.google.maps.android.data.parser.kml.KmzParser
import com.google.maps.android.data.renderer.mapper.GeoJsonMapper
import com.google.maps.android.data.renderer.mapper.GpxMapper
import com.google.maps.android.data.renderer.mapper.KmlMapper
import com.google.maps.android.data.renderer.mapper.toLayer
import com.google.maps.android.data.renderer.model.DataLayer
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.InputStream
import java.nio.charset.StandardCharsets

/**
 * Unified loader for DataLayers.
 *
 * Handles loading from assets or streams, determining the correct parser based on
 * file extension or content sniffing.
 */
class DataLayerLoader(
    private val context: Context,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) {

    /**
     * Loads a DataLayer from an asset file.
     *
     * @param context The context to access assets.
     * @param assetName The name of the asset file.
     * @return The loaded DataLayer, or null if loading failed.
     */
    suspend fun loadAsset(assetName: String): DataLayer? {
        return withContext(dispatcher) {
            try {
                val inputStream = context.assets.open(assetName)
                loadInputStream(inputStream, assetName)
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }

    /**
     * Loads a DataLayer from an InputStream.
     *
     * @param inputStream The input stream to read from.
     * @param fileName The name of the file (optional), used for extension matching.
     * @return The loaded DataLayer, or null if loading failed.
     */
    suspend fun loadInputStream(inputStream: InputStream, fileName: String?): DataLayer? {
        return withContext(dispatcher) {
            try {
                // 1. Try to match by extension
                if (fileName != null) {
                    val extension = fileName.substringAfterLast('.', "").lowercase()
                    if (extension.isNotEmpty()) {
                        val layer = tryParseByExtension(inputStream, extension)
                        if (layer != null) return@withContext layer
                    }
                }

                // 2. If extension match failed or no extension, try sniffing
                // We need a fresh stream or a reset stream for sniffing if the previous attempt consumed it.
                // However, the previous tryParseByExtension likely consumed the stream if it matched but failed.
                // Ideally, we should peek or buffer. For now, let's assume if extension matched, we committed to that parser.
                // But if we want robust fallback, we might need to handle stream resetting.
                // Given the current architecture, let's assume if extension matches, that's the intended format.
                // If extension didn't match any known parser, we fall back to sniffing.
                
                // Note: InputStreams from assets/content resolvers might not support mark/reset.
                // If we really need to sniff after a failed extension parse, we'd need to buffer the whole stream.
                // For this implementation, we'll rely on extension first. If no extension match, we sniff.
                
                 if (fileName == null || !isKnownExtension(fileName.substringAfterLast('.', "").lowercase())) {
                     return@withContext sniffAndParse(inputStream)
                 }

                null
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }

    private fun isKnownExtension(extension: String): Boolean {
        return GpxParser.SUPPORTED_EXTENSIONS.contains(extension) ||
                KmlParser.SUPPORTED_EXTENSIONS.contains(extension) ||
                KmzParser.SUPPORTED_EXTENSIONS.contains(extension) ||
                GeoJsonParser.SUPPORTED_EXTENSIONS.contains(extension)
    }

    private fun tryParseByExtension(inputStream: InputStream, extension: String): DataLayer? {
        return when {
            GpxParser.SUPPORTED_EXTENSIONS.contains(extension) -> {
                GpxParser().parse(inputStream).toLayer()
            }
            KmlParser.SUPPORTED_EXTENSIONS.contains(extension) -> {
                KmlParser().parse(inputStream).toLayer()
            }
            KmzParser.SUPPORTED_EXTENSIONS.contains(extension) -> {
                KmzParser().parse(inputStream).toLayer()
            }
            GeoJsonParser.SUPPORTED_EXTENSIONS.contains(extension) -> {
                GeoJsonParser().parse(inputStream)?.toLayer()
            }
            else -> null
        }
    }

    private fun sniffAndParse(inputStream: InputStream): DataLayer? {
        // This is tricky without buffering. We need to read the header.
        // If the stream supports mark/reset, we can use that.
        // Otherwise, we might consume the stream.
        // For now, let's read a small header buffer.
        
        if (!inputStream.markSupported()) {
             // If we can't mark, we can't reliably sniff and then parse with the same stream
             // unless we wrap it in a BufferedInputStream.
             // Let's assume the caller might have passed a BufferedInputStream or we wrap it.
             // But we can't easily wrap it here and pass it to parsers if they expect a specific subclass?
             // Actually parsers just take InputStream.
             
             // Let's wrap in BufferedInputStream if not already
             val bufferedStream = java.io.BufferedInputStream(inputStream)
             bufferedStream.mark(1024)
             val headerBytes = ByteArray(1024)
             val read = bufferedStream.read(headerBytes)
             bufferedStream.reset()
             
             if (read <= 0) return null
             val header = String(headerBytes, 0, read, StandardCharsets.UTF_8)

             return when {
                 GpxParser.canParse(header) -> {
                     GpxParser().parse(bufferedStream).toLayer()
                 }
                 KmlParser.canParse(header) -> {
                     KmlParser().parse(bufferedStream).toLayer()
                 }
                 KmzParser.canParse(header) -> {
                     KmzParser().parse(bufferedStream).toLayer()
                 }
                 GeoJsonParser.canParse(header) -> {
                     GeoJsonParser().parse(bufferedStream)?.toLayer()
                 }
                 else -> null
             }
        } else {
            inputStream.mark(1024)
            val headerBytes = ByteArray(1024)
            val read = inputStream.read(headerBytes)
            inputStream.reset()

            if (read <= 0) return null
            val header = String(headerBytes, 0, read, StandardCharsets.UTF_8)

            return when {
                GpxParser.canParse(header) -> {
                    GpxParser().parse(inputStream).toLayer()
                }
                KmlParser.canParse(header) -> {
                    KmlParser().parse(inputStream).toLayer()
                }
                KmzParser.canParse(header) -> {
                    KmzParser().parse(inputStream).toLayer()
                }
                GeoJsonParser.canParse(header) -> {
                    GeoJsonParser().parse(inputStream)?.toLayer()
                }
                else -> null
            }
        }
    }
}
