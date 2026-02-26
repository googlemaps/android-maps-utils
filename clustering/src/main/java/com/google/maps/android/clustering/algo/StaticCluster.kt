/*
 * Copyright 2023 Google LLC
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
class StaticCluster<T : ClusterItem>(private val mCenter: LatLng) : Cluster<T> {
    private val mItems: MutableCollection<T> = LinkedHashSet()

    fun add(t: T): Boolean {
        return mItems.add(t)
    }

    override val position: LatLng
        get() = mCenter

    fun remove(t: T): Boolean {
        return mItems.remove(t)
    }

    override val items: Collection<T>
        get() = mItems

    override val size: Int
        get() = mItems.size

    override fun toString(): String {
        return "StaticCluster{" +
                "mCenter=" + mCenter +
                ", mItems.size=" + mItems.size +
                '}'
    }

    override fun hashCode(): Int {
        return mCenter.hashCode() + mItems.hashCode()
    }

    override fun equals(other: Any?): Boolean {
        if (other !is StaticCluster<*>) {
            return false
        }

        return other.mCenter == mCenter && other.mItems == mItems
    }
}
