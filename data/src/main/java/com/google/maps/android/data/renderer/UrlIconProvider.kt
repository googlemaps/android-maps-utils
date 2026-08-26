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
import android.util.LruCache
import com.google.maps.android.data.kml.DefaultKmlUrlSanitizer
import com.google.maps.android.data.kml.KmlUrlSanitizer
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.ConcurrentHashMap

/**
 * Implementation of [IconProvider] that loads icons from URLs.
 * Uses an [LruCache] to cache loaded icons and handles "thundering herd" by deduplicating in-flight requests.
 */
open class UrlIconProvider @JvmOverloads constructor(
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO,
    // Icon/overlay hrefs originate from the (frequently untrusted) KML/GeoJSON document. The
    // sanitizer is applied before every fetch to prevent SSRF; the secure default blocks
    // non-http(s) schemes and hosts resolving to loopback/link-local/private ranges. Pass a
    // custom implementation to widen/narrow the policy, or `null` to disable (not recommended).
    private val urlSanitizer: KmlUrlSanitizer? = DefaultKmlUrlSanitizer(),
) : IconProvider {
    private val memoryCache: LruCache<String, Bitmap>
    private val inFlight = ConcurrentHashMap<String, Deferred<Bitmap?>>()
    private val downloadScope = CoroutineScope(SupervisorJob() + dispatcher)

    init {
        // Get max available VM memory, exceeding this amount will throw an
        // OutOfMemory exception. Stored in kilobytes as LruCache takes an
        // int in its constructor.
        val maxMemory = (Runtime.getRuntime().maxMemory() / 1024).toInt()

        // Use 1/8th of the available memory for this memory cache.
        val cacheSize = maxMemory / 8

        memoryCache =
            object : LruCache<String, Bitmap>(cacheSize) {
                override fun sizeOf(
                    key: String,
                    bitmap: Bitmap,
                ): Int {
                    // The cache size will be measured in kilobytes rather than
                    // number of items.
                    return bitmap.byteCount / 1024
                }
            }
    }

    override suspend fun loadIcon(url: String): Bitmap? {
        // 1. Fast path: Memory cache
        memoryCache.get(url)?.let { return it }

        // 2. In-flight deduplication
        // computeIfAbsent is not available on API 21, so we use synchronized block
        val deferred =
            synchronized(inFlight) {
                inFlight.getOrPut(url) {
                    downloadScope.async {
                        try {
                            val bitmap = loadBitmapFromUrl(url)
                            if (bitmap != null) {
                                memoryCache.put(url, bitmap)
                            }
                            bitmap
                        } finally {
                            // Always remove from in-flight map when done, success or failure
                            inFlight.remove(url)
                        }
                    }
                }
            }

        // 3. Wait for result
        return deferred.await()
    }

    protected open suspend fun loadBitmapFromUrl(urlString: String): Bitmap? =
        withContext(dispatcher) {
            try {
                // Follow redirects manually, re-validating every hop through the sanitizer, so a
                // 30x to an internal host cannot bypass the host check (redirect-based SSRF) while
                // still supporting legitimate redirects (CDNs, signed S3/GCS URLs).
                var current: String = urlString
                var redirectsLeft = MAX_REDIRECTS
                while (true) {
                    // Validate the (untrusted) href before issuing any request. `null` => blocked.
                    val safeUrl =
                        if (urlSanitizer != null) {
                            urlSanitizer.sanitizeUrl(current) ?: return@withContext null
                        } else {
                            current
                        }
                    val url = URL(safeUrl)
                    val connection = url.openConnection() as HttpURLConnection
                    try {
                        // Do not auto-follow: we re-run each redirect target through the sanitizer.
                        connection.instanceFollowRedirects = false
                        connection.doInput = true
                        connection.connect()
                        val code = connection.responseCode
                        when {
                            code in 200..299 -> {
                                return@withContext connection.inputStream.use {
                                    BitmapFactory.decodeStream(it)
                                }
                            }
                            code in 300..399 -> {
                                if (redirectsLeft-- <= 0) return@withContext null
                                val location =
                                    connection.getHeaderField("Location")
                                        ?: return@withContext null
                                // Resolve relative redirects against the current URL; the next loop
                                // iteration re-sanitizes the resolved absolute URL before connecting.
                                current = URL(url, location).toString()
                            }
                            else -> return@withContext null
                        }
                    } finally {
                        connection.disconnect()
                    }
                }
                @Suppress("UNREACHABLE_CODE")
                null
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }

    private companion object {
        /** Maximum number of redirects to follow before giving up. */
        private const val MAX_REDIRECTS = 5
    }
}
