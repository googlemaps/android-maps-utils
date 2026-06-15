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

package com.google.maps.android.clustering

import android.content.Context
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.Marker
import com.google.maps.android.clustering.algo.Algorithm
import com.google.maps.android.clustering.algo.NonHierarchicalDistanceBasedAlgorithm
import com.google.maps.android.clustering.algo.PreCachingAlgorithmDecorator
import com.google.maps.android.clustering.algo.ScreenBasedAlgorithm
import com.google.maps.android.clustering.algo.ScreenBasedAlgorithmAdapter
import com.google.maps.android.clustering.view.ClusterRenderer
import com.google.maps.android.clustering.view.DefaultClusterRenderer
import com.google.maps.android.collections.MarkerManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.locks.ReentrantReadWriteLock

/**
 * Groups many items on a map based on zoom level.
 *
 * ClusterManager should be added to the map as an: <ul> <li>{@link com.google.android.gms.maps.GoogleMap.OnCameraIdleListener}</li>
 * <li>{@link com.google.android.gms.maps.GoogleMap.OnMarkerClickListener}</li> </ul>
 */
public class ClusterManager<T : ClusterItem>
@JvmOverloads constructor(
    context: Context,
    public val map: GoogleMap,
    public val markerManager: MarkerManager = MarkerManager(map)
) :
    GoogleMap.OnCameraIdleListener,
    GoogleMap.OnMarkerClickListener,
    GoogleMap.OnInfoWindowClickListener {

    public val clusterMarkerCollection: MarkerManager.Collection = markerManager.newCollection()
    public val markerCollection: MarkerManager.Collection = markerManager.newCollection()

    @Volatile
    public var algorithm: Algorithm<T> =
        ScreenBasedAlgorithmAdapter(
            PreCachingAlgorithmDecorator(
                NonHierarchicalDistanceBasedAlgorithm()
            )
        )
        set(value) {
            value.lock()
            try {
                field.lock()
                try {
                    value.addItems(field.items)
                } finally {
                    field.unlock()
                }
                field = value
            } finally {
                value.unlock()
            }

            if ((field as? ScreenBasedAlgorithm)?.shouldReclusterOnMapMovement() == true) {
                (field as? ScreenBasedAlgorithm)?.onCameraChange(map.cameraPosition)
            }
            cluster()
        }

    public var renderer: ClusterRenderer<T> = DefaultClusterRenderer(context, map, this)
        set(value) {
            field.setOnClusterClickListener(null)
            field.setOnClusterItemClickListener(null)
            clusterMarkerCollection.clear()
            markerCollection.clear()
            field.onRemove()
            field = value
            field.onAdd()
            field.setOnClusterClickListener(onClusterClickListener)
            field.setOnClusterInfoWindowClickListener(onClusterInfoWindowClickListener)
            field.setOnClusterInfoWindowLongClickListener(onClusterInfoWindowLongClickListener)
            field.setOnClusterItemClickListener(onClusterItemClickListener)
            field.setOnClusterItemInfoWindowClickListener(onClusterItemInfoWindowClickListener)
            field.setOnClusterItemInfoWindowLongClickListener(onClusterItemInfoWindowLongClickListener)
            cluster()
        }

    private var previousCameraPosition: CameraPosition? = null
    private val clusterTaskLock = ReentrantReadWriteLock()
    private val coroutineScope = CoroutineScope(Dispatchers.Default)

    public var onClusterItemClickListener: OnClusterItemClickListener<T>? = null
    public var onClusterInfoWindowClickListener: OnClusterInfoWindowClickListener<T>? = null
    public var onClusterInfoWindowLongClickListener: OnClusterInfoWindowLongClickListener<T>? = null
    public var onClusterItemInfoWindowClickListener: OnClusterItemInfoWindowClickListener<T>? = null
    public var onClusterItemInfoWindowLongClickListener: OnClusterItemInfoWindowLongClickListener<T>? =
        null
    public var onClusterClickListener: OnClusterClickListener<T>? = null

    init {
        renderer.onAdd()
    }

    public fun setAnimation(animate: Boolean) {
        renderer.setAnimation(animate)
    }

    /**
     * Removes all items from the cluster manager. After calling this method you must invoke
     * {@link #cluster()} for the map to be cleared.
     */
    public fun clearItems() {
        algorithm.lock()
        try {
            algorithm.clearItems()
        } finally {
            algorithm.unlock()
        }
    }

    /**
     * Adds items to clusters. After calling this method you must invoke {@link #cluster()} for the
     * state of the clusters to be updated on the map.
     * @param items items to add to clusters
     * @return true if the cluster manager contents changed as a result of the call
     */
    public fun addItems(items: Collection<T>): Boolean {
        algorithm.lock()
        try {
            return algorithm.addItems(items)
        } finally {
            algorithm.unlock()
        }
    }

    /**
     * Adds an item to a cluster. After calling this method you must invoke {@link #cluster()} for
     * the state of the clusters to be updated on the map.
     * @param myItem item to add to clusters
     * @return true if the cluster manager contents changed as a result of the call
     */
    public fun addItem(myItem: T): Boolean {
        algorithm.lock()
        try {
            return algorithm.addItem(myItem)
        } finally {
            algorithm.unlock()
        }
    }

    public fun diff(add: Collection<T>?, remove: Collection<T>?, modify: Collection<T>?) {
        algorithm.lock()
        try {
            // Add items
            if (add != null) {
                for (item in add) {
                    algorithm.addItem(item)
                }
            }

            // Remove items
            if (remove != null) {
                algorithm.removeItems(remove)
            }

            // Modify items
            if (modify != null) {
                for (item in modify) {
                    updateItem(item)
                }
            }
        } finally {
            algorithm.unlock()
        }
    }

    /**
     * Removes items from clusters. After calling this method you must invoke {@link #cluster()} for
     * the state of the clusters to be updated on the map.
     * @param items items to remove from clusters
     * @return true if the cluster manager contents changed as a result of the call
     */
    public fun removeItems(items: Collection<T>): Boolean {
        algorithm.lock()
        try {
            return algorithm.removeItems(items)
        } finally {
            algorithm.unlock()
        }
    }

    /**
     * Removes an item from clusters. After calling this method you must invoke {@link #cluster()}
     * for the state of the clusters to be updated on the map.
     * @param item item to remove from clusters
     * @return true if the item was removed from the cluster manager as a result of this call
     */
    public fun removeItem(item: T): Boolean {
        algorithm.lock()
        try {
            return algorithm.removeItem(item)
        } finally {
            algorithm.unlock()
        }
    }

    /**
     * Updates an item in clusters. After calling this method you must invoke {@link #cluster()} for
     * the state of the clusters to be updated on the map.
     * @param item item to update in clusters
     * @return true if the item was updated in the cluster manager, false if the item is not
     * contained within the cluster manager and the cluster manager contents are unchanged
     */
    public fun updateItem(item: T): Boolean {
        algorithm.lock()
        try {
            return algorithm.updateItem(item)
        } finally {
            algorithm.unlock()
        }
    }

    /**
     * Force a re-cluster on the map. You should call this after adding, removing, updating,
     * or clearing item(s).
     */
    public fun cluster() {
        clusterTaskLock.writeLock().lock()
        try {
            coroutineScope.launch {
                val clusters = withContext(Dispatchers.Default) {
                    algorithm.lock()
                    try {
                        algorithm.getClusters(map.cameraPosition.zoom)
                    } finally {
                        algorithm.unlock()
                    }
                }
                withContext(Dispatchers.Main) {
                    renderer.onClustersChanged(clusters)
                }
            }
        } finally {
            clusterTaskLock.writeLock().unlock()
        }
    }

    /**
     * Might re-cluster.
     */
    override fun onCameraIdle() {
        if (renderer is GoogleMap.OnCameraIdleListener) {
            (renderer as GoogleMap.OnCameraIdleListener).onCameraIdle()
        }

        (algorithm as? ScreenBasedAlgorithm)?.onCameraChange(map.cameraPosition)

        // delegate clustering to the algorithm
        if ((algorithm as? ScreenBasedAlgorithm)?.shouldReclusterOnMapMovement() == true) {
            cluster()

            // Don't re-compute clusters if the map has just been panned/tilted/rotated.
        } else if (previousCameraPosition == null || previousCameraPosition!!.zoom != map.cameraPosition.zoom) {
            previousCameraPosition = map.cameraPosition
            cluster()
        }
    }

    override fun onMarkerClick(marker: Marker): Boolean {
        return markerManager.onMarkerClick(marker)
    }

    override fun onInfoWindowClick(marker: Marker) {
        markerManager.onInfoWindowClick(marker)
    }

    /**
     * Called when a Cluster is clicked.
     */
    public fun interface OnClusterClickListener<T : ClusterItem> {
        /**
         * Called when cluster is clicked.
         * Return true if click has been handled
         * Return false and the click will dispatched to the next listener
         */
        public fun onClusterClick(cluster: Cluster<T>): Boolean
    }

    /**
     * Called when a Cluster's Info Window is clicked.
     */
    public fun interface OnClusterInfoWindowClickListener<T : ClusterItem> {
        public fun onClusterInfoWindowClick(cluster: Cluster<T>)
    }

    /**
     * Called when a Cluster's Info Window is long clicked.
     */
    public fun interface OnClusterInfoWindowLongClickListener<T : ClusterItem> {
        public fun onClusterInfoWindowLongClick(cluster: Cluster<T>)
    }

    /**
     * Called when an individual ClusterItem is clicked.
     */
    public fun interface OnClusterItemClickListener<T : ClusterItem> {

        /**
         * Called when {@code item} is clicked.
         *
         * @param item the item clicked
         *
         * @return true if the listener consumed the event (i.e. the default behavior should not
         * occur), false otherwise (i.e. the default behavior should occur).  The default behavior
         * is for the camera to move to the marker and an info window to appear.
         */
        public fun onClusterItemClick(item: T): Boolean
    }

    /**
     * Called when an individual ClusterItem's Info Window is clicked.
     */
    public fun interface OnClusterItemInfoWindowClickListener<T : ClusterItem> {
        public fun onClusterItemInfoWindowClick(item: T)
    }

    /**
     * Called when an individual ClusterItem's Info Window is long clicked.
     */
    public fun interface OnClusterItemInfoWindowLongClickListener<T : ClusterItem> {
        public fun onClusterItemInfoWindowLongClick(item: T)
    }
}
