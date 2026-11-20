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
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.net.HttpURLConnection
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
class UrlIconProviderTest {
    @Test
    fun loadIcon_returnsBitmap() = runTest {
        val testDispatcher = StandardTestDispatcher(testScheduler)
        val provider = TestableUrlIconProvider(testDispatcher)
        val url = "http://example.com/icon.png"

        val bitmap = provider.loadIcon(url)

        assertNotNull(bitmap)
        assertEquals(1, provider.loadCount.get())
    }

    @Test
    fun loadIcon_cachesBitmap() = runTest {
        val testDispatcher = StandardTestDispatcher(testScheduler)
        val provider = TestableUrlIconProvider(testDispatcher)
        val url = "http://example.com/icon.png"

        val bitmap1 = provider.loadIcon(url)
        val bitmap2 = provider.loadIcon(url)

        assertNotNull(bitmap1)
        assertSame(bitmap1, bitmap2)
        assertEquals(1, provider.loadCount.get())
    }

    @Test
    fun loadIcon_deduplicatesRequests() = runTest {
        val testDispatcher = StandardTestDispatcher(testScheduler)
        val provider = TestableUrlIconProvider(testDispatcher, delayMillis = 1000)
        val url = "http://example.com/icon.png"

        // Launch multiple requests concurrently
        val deferred1 = async { provider.loadIcon(url) }
        val deferred2 = async { provider.loadIcon(url) }
        val deferred3 = async { provider.loadIcon(url) }

        // Advance time to finish the single underlying request
        advanceTimeBy(1001)

        val results = awaitAll(deferred1, deferred2, deferred3)

        // Verify all got the same bitmap
        val firstBitmap = results[0]
        results.forEach { assertSame(firstBitmap, it) }

        // Verify only one actual load happened
        assertEquals(1, provider.loadCount.get())
    }

    @Test
    fun loadIcon_handlesFailures() = runTest {
        val testDispatcher = StandardTestDispatcher(testScheduler)
        val provider = TestableUrlIconProvider(testDispatcher, shouldFail = true)
        val url = "http://example.com/error.png"

        val bitmap = provider.loadIcon(url)

        assertEquals(null, bitmap)
        assertEquals(1, provider.loadCount.get())

        // Retry should trigger another load attempt (since failure wasn't cached)
        provider.shouldFail = false
        val retryBitmap = provider.loadIcon(url)

        assertNotNull(retryBitmap)
        assertEquals(2, provider.loadCount.get())
    }

    @Test
    fun loadIcon_rejectsOversizedBitmapReturnedBySubclass() = runTest {
        val testDispatcher = StandardTestDispatcher(testScheduler)
        val oversized =
            mockk<Bitmap>(relaxed = true) {
                every { allocationByteCount } returns (SafeBitmapDecoder.MAX_DECODED_BYTES + 1).toInt()
            }
        val provider =
            object : UrlIconProvider(testDispatcher) {
                override suspend fun loadBitmapFromUrl(urlString: String): Bitmap = oversized
            }

        assertNull(provider.loadIcon("http://example.com/oversized.png"))
        verify(exactly = 1) { oversized.recycle() }
    }

    @Test
    fun loadIcon_limitsConcurrentDistinctDownloads() = runTest {
        val testDispatcher = StandardTestDispatcher(testScheduler)
        val provider = TestableUrlIconProvider(testDispatcher, delayMillis = 1000)

        val results =
            listOf(
                async { provider.loadIcon("http://example.com/one.png") },
                async { provider.loadIcon("http://example.com/two.png") },
                async { provider.loadIcon("http://example.com/three.png") },
                async { provider.loadIcon("http://example.com/four.png") },
                async { provider.loadIcon("http://example.com/five.png") },
            )
        advanceUntilIdle()
        results.awaitAll()

        assertEquals(4, provider.maxActiveLoads.get())
        assertEquals(5, provider.loadCount.get())
    }

    @Test
    fun loadIcon_stalledDownloadTimesOutAndReleasesPermit() = runTest {
        val testDispatcher = StandardTestDispatcher(testScheduler)
        val provider =
            object : UrlIconProvider(testDispatcher) {
                override suspend fun loadBitmapFromUrl(urlString: String): Bitmap? {
                    if (urlString.contains("stall")) delay(Long.MAX_VALUE)
                    return Bitmap.createBitmap(10, 10, Bitmap.Config.ARGB_8888)
                }
            }

        val stalled = List(4) { async { provider.loadIcon("http://example.com/stall$it.png") } }
        val healthy = async { provider.loadIcon("http://example.com/ok.png") }
        advanceUntilIdle()

        stalled.forEach { assertNull(it.await()) }
        assertNotNull(healthy.await())
    }

    @Test
    fun cancellingDownloadDisconnectsWhileReadIsBlocked() = runTest {
        val enteredRead = CompletableDeferred<Unit>()
        val readUnblocked = CompletableDeferred<Boolean>()
        val disconnected = CountDownLatch(1)
        val connection = mockk<HttpURLConnection>()
        every { connection.disconnect() } answers { disconnected.countDown() }
        every { connection.responseCode } answers {
            enteredRead.complete(Unit)
            readUnblocked.complete(disconnected.await(5, TimeUnit.SECONDS))
            HttpURLConnection.HTTP_OK
        }

        val download = async(Dispatchers.IO) {
            disconnectOnCancellation(connection).use { connection.responseCode }
        }
        enteredRead.await()
        download.cancelAndJoin()

        assertTrue(readUnblocked.await())
        verify(exactly = 1) { connection.disconnect() }
    }

    @Test
    fun loadIcon_failedLoadIsNotLeftInFlight() = runTest {
        val testDispatcher = StandardTestDispatcher(testScheduler)
        val provider = TestableUrlIconProvider(testDispatcher, shouldFail = true)
        val url = "http://example.com/fail.png"

        assertNull(provider.loadIcon(url))
        provider.shouldFail = false

        assertNotNull(provider.loadIcon(url))
        assertEquals(2, provider.loadCount.get())
    }

    // Subclass to mock network calls
    private class TestableUrlIconProvider(
        dispatcher: kotlinx.coroutines.CoroutineDispatcher,
        val delayMillis: Long = 0,
        var shouldFail: Boolean = false,
    ) : UrlIconProvider(dispatcher) {
        val loadCount = AtomicInteger(0)
        val activeLoads = AtomicInteger(0)
        val maxActiveLoads = AtomicInteger(0)

        override suspend fun loadBitmapFromUrl(urlString: String): Bitmap? {
            loadCount.incrementAndGet()
            val active = activeLoads.incrementAndGet()
            maxActiveLoads.updateAndGet { previous -> maxOf(previous, active) }
            try {
                if (delayMillis > 0) {
                    delay(delayMillis)
                }
                if (shouldFail) {
                    return null
                }
                return Bitmap.createBitmap(10, 10, Bitmap.Config.ARGB_8888)
            } finally {
                activeLoads.decrementAndGet()
            }
        }
    }
}
