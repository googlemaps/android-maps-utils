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

import com.google.maps.android.clustering.Cluster
import com.google.maps.android.clustering.ClusterItem

/**
 * Logic for computing clusters
 */
public interface Algorithm<T : ClusterItem> {

    /**
     * Adds an item to the algorithm
     * @param item the item to be added
     * @return true if the algorithm contents changed as a result of the call
     */
    public fun addItem(item: T): Boolean

    /**
     * Adds a collection of items to the algorithm
     * @param items the items to be added
     * @return true if the algorithm contents changed as a result of the call
     */
    public fun addItems(items: Collection<T>): Boolean

    public fun clearItems()

    /**
     * Removes an item from the algorithm
     * @param item the item to be removed
     * @return true if this algorithm contained the specified element (or equivalently, if this
     * algorithm changed as a result of the call).
     */
    public fun removeItem(item: T): Boolean

    /**
     * Updates the provided item in the algorithm
     * @param item the item to be updated
     * @return true if the item existed in the algorithm and was updated, or false if the item did
     * not exist in the algorithm and the algorithm contents remain unchanged.
     */
    public fun updateItem(item: T): Boolean

    /**
     * Removes a collection of items from the algorithm
     * @param items the items to be removed
     * @return true if this algorithm contents changed as a result of the call
     */
    public fun removeItems(items: Collection<T>): Boolean

    public fun getClusters(zoom: Float): Set<Cluster<T>>

    public val items: Collection<T>

    public var maxDistanceBetweenClusteredItems: Int

    public fun lock()

    public fun unlock()
}
