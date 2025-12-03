/*
 * Copyright 2023 Google Inc.
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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.concurrent.withLock

/**
 * Optimistically fetch clusters for adjacent zoom levels, caching them as necessary.
 */
public class PreCachingAlgorithmDecorator<T : ClusterItem>(
    private val algorithm: Algorithm<T>
) : AbstractAlgorithm<T>() {

    // TODO: evaluate maxSize parameter for LruCache.
    private val cache = LruCache<Int, Set<Cluster<T>>>(5)
    private val cacheLock = ReentrantReadWriteLock()
    private val coroutineScope = CoroutineScope(Dispatchers.IO)

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
        cache.evictAll()
    }

    override fun getClusters(zoom: Float): Set<Cluster<T>> {
        val discreteZoom = zoom.toInt()
        val results = getClustersInternal(discreteZoom)
        // TODO: Check if requests are already in-flight.
        if (cache[discreteZoom + 1] == null) {
            coroutineScope.launch {
                delay((Math.random() * 500 + 500).toLong())
                getClustersInternal(discreteZoom + 1)
            }
        }
        if (cache[discreteZoom - 1] == null) {
            coroutineScope.launch {
                delay((Math.random() * 500 + 500).toLong())
                getClustersInternal(discreteZoom - 1)
            }
        }
        return results
    }

    override val items: Collection<T>
        get() = algorithm.items

    override var maxDistanceBetweenClusteredItems: Int
        get() = algorithm.maxDistanceBetweenClusteredItems
        set(value) {
            algorithm.maxDistanceBetweenClusteredItems = value
            clearCache()
        }

    private fun getClustersInternal(discreteZoom: Int): Set<Cluster<T>> {
        return cacheLock.readLock().withLock {
            cache[discreteZoom]
        } ?: cacheLock.writeLock().withLock {
            cache[discreteZoom] ?: run {
                val clusters = algorithm.getClusters(discreteZoom.toFloat())
                cache.put(discreteZoom, clusters)
                clusters
            }
        }
    }
}
