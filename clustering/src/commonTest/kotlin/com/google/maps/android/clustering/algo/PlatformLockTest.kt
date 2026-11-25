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
package com.google.maps.android.clustering.algo

import com.google.maps.android.clustering.ClusterItem
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class PlatformLockTest {
    @Test
    fun testWithLockReturnsBlockResult() {
        val lock = PlatformLock()
        assertEquals(42, lock.withLock { 42 })
    }

    @Test
    fun testLockIsReentrant() {
        val lock = PlatformLock()
        // A non-reentrant lock would deadlock here.
        val result = lock.withLock { lock.withLock { "nested" } }
        assertEquals("nested", result)
    }

    @Test
    fun testWithLockReleasesOnException() {
        val lock = PlatformLock()
        assertFailsWith<IllegalStateException> {
            lock.withLock { error("boom") }
        }
        // The lock must still be usable after the block threw.
        assertEquals(1, lock.withLock { 1 })
    }

    @Test
    fun testMonitorLockIsReentrant() {
        val lock = MonitorLock(Any())
        val result = lock.withLock { lock.withLock { "nested" } }
        assertEquals("nested", result)
    }

    @Test
    fun testAlgorithmLockAndUnlockCanNest() {
        val algo = NonHierarchicalDistanceBasedAlgorithm<ClusterItem>()
        algo.lock()
        algo.lock()
        algo.unlock()
        algo.unlock()
        // Locking again after balanced unlocks must not block.
        algo.lock()
        algo.unlock()
    }
}
