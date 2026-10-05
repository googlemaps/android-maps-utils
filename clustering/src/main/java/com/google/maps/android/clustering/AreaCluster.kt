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

package com.google.maps.android.clustering

import com.google.android.gms.maps.model.LatLng
import java.util.Collections

/**
 * A lightweight [Cluster] implementation representing either an aggregated cluster of items
 * or a single unclustered leaf item.
 *
 * @param T The type of [ClusterItem] represented by this cluster.
 * @property position The geographic position (centroid) of the cluster, or position of the individual item.
 * @property size The total number of items represented by this cluster.
 * @property leafItem The underlying [ClusterItem] if this is a single item ([size] == 1), or `null` if composite.
 */
public class AreaCluster<T : ClusterItem>(
    public override val position: LatLng,
    public override val size: Int,
    public val leafItem: T? = null,
) : Cluster<T> {
    public override val items: Collection<T>
        get() = if (leafItem != null) Collections.singleton(leafItem) else emptyList()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is AreaCluster<*>) return false
        return size == other.size && position == other.position && leafItem == other.leafItem
    }

    override fun hashCode(): Int {
        var result = position.hashCode()
        result = 31 * result + size
        result = 31 * result + (leafItem?.hashCode() ?: 0)
        return result
    }

    override fun toString(): String =
        if (leafItem != null) "AreaCluster(position=$position, leafItem=$leafItem)"
        else "AreaCluster(position=$position, size=$size)"
}
