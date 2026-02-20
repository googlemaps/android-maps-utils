/*
 * Copyright 2013 Google Inc.
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

package com.google.maps.android.clustering.algo

import androidx.collection.LruCache
import com.google.maps.android.clustering.Cluster
import com.google.maps.android.clustering.ClusterItem
import java.util.concurrent.Executor
import java.util.concurrent.Executors
import java.util.concurrent.locks.ReadWriteLock
import java.util.concurrent.locks.ReentrantReadWriteLock

/**
 * Optimistically fetch clusters for adjacent zoom levels, caching them as necessary.
 */
class PreCachingAlgorithmDecorator<T : ClusterItem>(private val algorithm: Algorithm<T>) : AbstractAlgorithm<T>() {

    // TODO: evaluate maxSize parameter for LruCache.
    private val mCache = LruCache<Int, Set<Cluster<T>>>(5)
    private val mCacheLock: ReadWriteLock = ReentrantReadWriteLock()
    private val mExecutor: Executor = Executors.newCachedThreadPool()

    override fun addItem(item: T): Boolean {
        val result = algorithm.addItem(item)
        if (result) {
            clearCache()
        }
        return result
    }

    override fun addItems(items: Collection<T>): Boolean {
        val result = algorithm.addItems(items)
        if (result) {
            clearCache()
        }
        return result
    }

    override fun clearItems() {
        algorithm.clearItems()
        clearCache()
    }

    override fun removeItem(item: T): Boolean {
        val result = algorithm.removeItem(item)
        if (result) {
            clearCache()
        }
        return result
    }

    override fun removeItems(items: Collection<T>): Boolean {
        val result = algorithm.removeItems(items)
        if (result) {
            clearCache()
        }
        return result
    }

    override fun updateItem(item: T): Boolean {
        val result = algorithm.updateItem(item)
        if (result) {
            clearCache()
        }
        return result
    }

    private fun clearCache() {
        mCache.evictAll()
    }

    override fun getClusters(zoom: Float): Set<Cluster<T>> {
        val discreteZoom = zoom.toInt()
        val results = getClustersInternal(discreteZoom)
        // TODO: Check if requests are already in-flight.
        if (mCache.get(discreteZoom + 1) == null) {
            mExecutor.execute(PrecacheRunnable(discreteZoom + 1))
        }
        if (mCache.get(discreteZoom - 1) == null) {
            mExecutor.execute(PrecacheRunnable(discreteZoom - 1))
        }
        return results
    }

    override val items: Collection<T>
        get() = algorithm.items

    override var maxDistanceBetweenClusteredItems: Int
        get() = algorithm.maxDistanceBetweenClusteredItems
        set(maxDistance) {
            algorithm.maxDistanceBetweenClusteredItems = maxDistance
            clearCache()
        }

    private fun getClustersInternal(discreteZoom: Int): Set<Cluster<T>> {
        var results: Set<Cluster<T>>?
        mCacheLock.readLock().lock()
        results = mCache.get(discreteZoom)
        mCacheLock.readLock().unlock()

        if (results == null) {
            mCacheLock.writeLock().lock()
            try {
                results = mCache.get(discreteZoom)
                if (results == null) {
                    results = algorithm.getClusters(discreteZoom.toFloat())
                    mCache.put(discreteZoom, results)
                }
            } finally {
                mCacheLock.writeLock().unlock()
            }
        }
        return results!!
    }

    private inner class PrecacheRunnable(private val zoom: Int) : Runnable {
        override fun run() {
            try {
                // Wait between 500 - 1000 ms.
                Thread.sleep((Math.random() * 500 + 500).toLong())
            } catch (e: InterruptedException) {
                // ignore. keep going.
            }
            getClustersInternal(zoom)
        }
    }
}
