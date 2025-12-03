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

import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.clustering.Cluster
import com.google.maps.android.clustering.ClusterItem

/**
 * A cluster whose center is determined upon creation.
 */
public data class StaticCluster<T : ClusterItem>(
    override val position: LatLng,
    private val itemsSet: MutableSet<T> = LinkedHashSet()
) : Cluster<T> {

    override val items: Collection<T>
        get() = itemsSet

    public fun add(t: T): Boolean {
        return itemsSet.add(t)
    }

    public fun remove(t: T): Boolean {
        return itemsSet.remove(t)
    }

    override val size: Int
        get() = itemsSet.size

    override fun toString(): String =
        "StaticCluster{position=$position, items.size=${itemsSet.size}}"
}
