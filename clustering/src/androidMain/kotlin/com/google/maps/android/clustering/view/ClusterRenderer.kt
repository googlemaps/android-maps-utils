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
package com.google.maps.android.clustering.view

import androidx.annotation.StyleRes
import com.google.maps.android.clustering.Cluster
import com.google.maps.android.clustering.ClusterItem
import com.google.maps.android.clustering.ClusterManager.OnClusterClickListener
import com.google.maps.android.clustering.ClusterManager.OnClusterInfoWindowClickListener
import com.google.maps.android.clustering.ClusterManager.OnClusterInfoWindowLongClickListener
import com.google.maps.android.clustering.ClusterManager.OnClusterItemClickListener
import com.google.maps.android.clustering.ClusterManager.OnClusterItemInfoWindowClickListener
import com.google.maps.android.clustering.ClusterManager.OnClusterItemInfoWindowLongClickListener

/**
 * Renders clusters.
 */
public interface ClusterRenderer<T : ClusterItem> {
    /**
     * Called when the view needs to be updated because new clusters need to be displayed.
     *
     * @param clusters the clusters to be displayed.
     */
    public fun onClustersChanged(clusters: Set<Cluster<T>>)

    public fun setOnClusterClickListener(listener: OnClusterClickListener<T>?)

    public fun setOnClusterInfoWindowClickListener(listener: OnClusterInfoWindowClickListener<T>?)

    public fun setOnClusterInfoWindowLongClickListener(listener: OnClusterInfoWindowLongClickListener<T>?)

    public fun setOnClusterItemClickListener(listener: OnClusterItemClickListener<T>?)

    public fun setOnClusterItemInfoWindowClickListener(listener: OnClusterItemInfoWindowClickListener<T>?)

    public fun setOnClusterItemInfoWindowLongClickListener(listener: OnClusterItemInfoWindowLongClickListener<T>?)

    /**
     * Called to set animation on or off
     */
    public fun setAnimation(animate: Boolean)

    /**
     * Sets the length of the animation in milliseconds.
     */
    public fun setAnimationDuration(animationDurationMs: Long)

    /**
     * Called when the view is added.
     */
    public fun onAdd()

    /**
     * Called when the view is removed.
     */
    public fun onRemove()

    /**
     * Called to determine the color of a Cluster.
     */
    public fun getColor(clusterSize: Int): Int

    /**
     * Called to determine the text appearance of a cluster.
     */
    @StyleRes
    public fun getClusterTextAppearance(clusterSize: Int): Int
}
