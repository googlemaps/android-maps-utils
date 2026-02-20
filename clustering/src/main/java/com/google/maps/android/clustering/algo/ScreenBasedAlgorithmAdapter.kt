/*
 * Copyright 2016 Google Inc.
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

import com.google.android.gms.maps.model.CameraPosition
import com.google.maps.android.clustering.Cluster
import com.google.maps.android.clustering.ClusterItem

class ScreenBasedAlgorithmAdapter<T : ClusterItem>(private val algorithm: Algorithm<T>) : AbstractAlgorithm<T>(), ScreenBasedAlgorithm<T> {

    override fun shouldReclusterOnMapMovement(): Boolean {
        return false
    }

    override fun addItem(item: T): Boolean {
        return algorithm.addItem(item)
    }

    override fun addItems(items: Collection<T>): Boolean {
        return algorithm.addItems(items)
    }

    override fun clearItems() {
        algorithm.clearItems()
    }

    override fun removeItem(item: T): Boolean {
        return algorithm.removeItem(item)
    }

    override fun removeItems(items: Collection<T>): Boolean {
        return algorithm.removeItems(items)
    }

    override fun updateItem(item: T): Boolean {
        return algorithm.updateItem(item)
    }

    override fun getClusters(zoom: Float): Set<Cluster<T>> {
        return algorithm.getClusters(zoom)
    }

    override val items: Collection<T>
        get() = algorithm.items

    override var maxDistanceBetweenClusteredItems: Int
        get() = algorithm.maxDistanceBetweenClusteredItems
        set(maxDistance) {
            algorithm.maxDistanceBetweenClusteredItems = maxDistance
        }

    override fun onCameraChange(position: CameraPosition) {
        // stub
    }
}
