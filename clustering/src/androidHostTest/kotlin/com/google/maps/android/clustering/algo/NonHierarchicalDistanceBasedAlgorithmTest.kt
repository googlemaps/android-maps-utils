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

import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.clustering.ClusterItem
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import kotlin.concurrent.thread

class NonHierarchicalDistanceBasedAlgorithmTest {
    class TestClusterItem(lat: Double, lng: Double) : ClusterItem {
        override val position: LatLng = LatLng(lat, lng)

        override val title: String? = null

        override val snippet: String? = null

        override val zIndex: Float = 0f
    }

    /** A subclass guarding its own work the way the KDoc told subclasses to before the multiplatform migration. */
    class SynchronizingAlgorithm : NonHierarchicalDistanceBasedAlgorithm<TestClusterItem>() {
        fun <R> holdingQuadTree(block: () -> R): R = synchronized(mQuadTree) { block() }
    }

    @Test
    fun testSubclassSynchronizingOnQuadTreeExcludesAddItem() {
        val algo = SynchronizingAlgorithm()
        val added = CountDownLatch(1)

        algo.holdingQuadTree {
            thread {
                algo.addItem(TestClusterItem(10.0, 10.0))
                added.countDown()
            }
            // addItem must wait for the subclass to release mQuadTree.
            assertFalse(added.await(200, TimeUnit.MILLISECONDS))
        }

        assertTrue(added.await(5, TimeUnit.SECONDS))
        assertEquals(1, algo.items.size)
    }

    @Test
    fun testLockIsReentrantFromSubclass() {
        val algo = SynchronizingAlgorithm()

        // Calling base methods while already holding the monitor must not deadlock.
        algo.holdingQuadTree {
            algo.addItem(TestClusterItem(10.0, 10.0))
            algo.removeItem(algo.items.first())
        }

        assertTrue(algo.items.isEmpty())
    }
}
