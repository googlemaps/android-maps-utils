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

package com.google.maps.android.clustering.view

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.TimeInterpolator
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.graphics.drawable.LayerDrawable
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.OvalShape
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.os.MessageQueue
import android.util.SparseArray
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import androidx.annotation.NonNull
import androidx.annotation.StyleRes
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.Projection
import com.google.android.gms.maps.model.AdvancedMarker
import com.google.android.gms.maps.model.AdvancedMarkerOptions
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.Marker
import com.google.maps.android.clustering.Cluster
import com.google.maps.android.clustering.ClusterItem
import com.google.maps.android.clustering.ClusterManager
import com.google.maps.android.collections.MarkerManager
import com.google.maps.android.geometry.Point
import com.google.maps.android.projection.SphericalMercatorProjection
import com.google.maps.android.ui.IconGenerator
import com.google.maps.android.ui.R
import com.google.maps.android.ui.SquareTextView
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.Collections
import java.util.LinkedList
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executors
import java.util.concurrent.locks.Condition
import java.util.concurrent.locks.ReentrantLock
import kotlin.math.pow

/**
 * The default view for a ClusterManager. Markers are animated in and out of clusters.
 */
public class DefaultAdvancedMarkersClusterRenderer<T : ClusterItem>(
    private val context: Context,
    private val map: GoogleMap,
    private val clusterManager: ClusterManager<T>
) : ClusterRenderer<T> {

    private val iconGenerator: IconGenerator
    private val density: Float
    private var animate: Boolean = true
    private var animationDurationMs: Long = 300
    private val executor: CoroutineDispatcher =
        Executors.newSingleThreadExecutor().asCoroutineDispatcher()

    private val buckets = intArrayOf(10, 20, 50, 100, 200, 500, 1000)
    private lateinit var coloredCircleBackground: ShapeDrawable

    /**
     * Markers that are currently on the map.
     */
    private var markers: MutableSet<MarkerWithPosition> = Collections.newSetFromMap(
        ConcurrentHashMap()
    )

    /**
     * Icons for each bucket.
     */
    private val icons = SparseArray<BitmapDescriptor>()

    /**
     * Markers for single ClusterItems.
     */
    private val markerCache = MarkerCache<T>()

    /**
     * If cluster size is less than this size, display individual markers.
     */
    private var minClusterSize = 4

    /**
     * The currently displayed set of clusters.
     */
    private var clusters: Set<Cluster<T>>? = null

    /**
     * Markers for Clusters.
     */
    private val clusterMarkerCache = MarkerCache<Cluster<T>>()

    /**
     * The target zoom level for the current set of clusters.
     */
    private var zoom: Float = 0f

    private val viewModifier = ViewModifier()

    private var onClusterClickListener: ClusterManager.OnClusterClickListener<T>? = null
    private var onClusterInfoWindowClickListener: ClusterManager.OnClusterInfoWindowClickListener<T>? =
        null
    private var onClusterInfoWindowLongClickListener: ClusterManager.OnClusterInfoWindowLongClickListener<T>? =
        null
    private var onClusterItemClickListener: ClusterManager.OnClusterItemClickListener<T>? = null
    private var onClusterItemInfoWindowClickListener: ClusterManager.OnClusterItemInfoWindowClickListener<T>? =
        null
    private var onClusterItemInfoWindowLongClickListener: ClusterManager.OnClusterItemInfoWindowLongClickListener<T>? =
        null

    init {
        val context = clusterManager.markerManager.context
        density = context.resources.displayMetrics.density
        iconGenerator = IconGenerator(context)
        iconGenerator.setContentView(makeSquareTextView(context))
        iconGenerator.setTextAppearance(R.style.amu_ClusterIcon_TextAppearance)
        iconGenerator.setBackground(makeClusterBackground())
    }

    override fun onAdd() {
        clusterManager.markerCollection.setOnMarkerClickListener { marker ->
            onClusterItemClickListener?.onClusterItemClick(markerCache[marker]!!) ?: false
        }

        clusterManager.markerCollection.setOnInfoWindowClickListener { marker ->
            onClusterItemInfoWindowClickListener?.onClusterItemInfoWindowClick(markerCache[marker]!!)
        }

        clusterManager.markerCollection.setOnInfoWindowLongClickListener { marker ->
            onClusterItemInfoWindowLongClickListener?.onClusterItemInfoWindowLongClick(markerCache[marker]!!)
        }

        clusterManager.clusterMarkerCollection.setOnMarkerClickListener { marker ->
            onClusterClickListener?.onClusterClick(clusterMarkerCache[marker]!!) ?: false
        }

        clusterManager.clusterMarkerCollection.setOnInfoWindowClickListener { marker ->
            onClusterInfoWindowClickListener?.onClusterInfoWindowClick(clusterMarkerCache[marker]!!)
        }

        clusterManager.clusterMarkerCollection.setOnInfoWindowLongClickListener { marker ->
            onClusterInfoWindowLongClickListener?.onClusterInfoWindowLongClick(
                clusterMarkerCache[marker]!!
            )
        }
    }

    override fun onRemove() {
        clusterManager.markerCollection.setOnMarkerClickListener(null)
        clusterManager.markerCollection.setOnInfoWindowClickListener(null)
        clusterManager.markerCollection.setOnInfoWindowLongClickListener(null)
        clusterManager.clusterMarkerCollection.setOnMarkerClickListener(null)
        clusterManager.clusterMarkerCollection.setOnInfoWindowClickListener(null)
        clusterManager.clusterMarkerCollection.setOnInfoWindowLongClickListener(null)
    }

    private fun makeClusterBackground(): LayerDrawable {
        coloredCircleBackground = ShapeDrawable(OvalShape())
        val outline = ShapeDrawable(OvalShape())
        outline.paint.color = 0x80ffffff.toInt() // Transparent white.
        val background = LayerDrawable(arrayOf<Drawable>(outline, coloredCircleBackground))
        val strokeWidth = (density * 3).toInt()
        background.setLayerInset(1, strokeWidth, strokeWidth, strokeWidth, strokeWidth)
        return background
    }

    private fun makeSquareTextView(context: Context): SquareTextView {
        val squareTextView = SquareTextView(context)
        val layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        squareTextView.layoutParams = layoutParams
        squareTextView.id = R.id.amu_text
        val twelveDpi = (12 * density).toInt()
        squareTextView.setPadding(twelveDpi, twelveDpi, twelveDpi, twelveDpi)
        return squareTextView
    }

    override fun getColor(clusterSize: Int): Int {
        val hueRange = 220f
        val sizeRange = 300f
        val size = clusterSize.coerceAtMost(sizeRange.toInt()).toFloat()
        val hue = (sizeRange - size) * (sizeRange - size) / (sizeRange * sizeRange) * hueRange
        return Color.HSVToColor(
            floatArrayOf(
                hue, 1f, .6f
            )
        )
    }

    @StyleRes
    override fun getClusterTextAppearance(clusterSize: Int): Int {
        return R.style.amu_ClusterIcon_TextAppearance // Default value
    }

    @NonNull
    protected open fun getClusterText(bucket: Int): String {
        return if (bucket < buckets[0]) {
            bucket.toString()
        } else {
            "$bucket+"
        }
    }

    /**
     * Gets the "bucket" for a particular cluster. By default, uses the number of points within the
     * cluster, bucketed to some set points.
     */
    protected open fun getBucket(@NonNull cluster: Cluster<T>): Int {
        val size = cluster.size
        if (size <= buckets[0]) {
            return size
        }
        for (i in 0 until buckets.size - 1) {
            if (size < buckets[i + 1]) {
                return buckets[i]
            }
        }
        return buckets[buckets.size - 1]
    }

    /**
     * Gets the minimum cluster size used to render clusters. For example, if "4" is returned,
     * then for any clusters of size 3 or less the items will be rendered as individual markers
     * instead of as a single cluster marker.
     *
     * @return the minimum cluster size used to render clusters. For example, if "4" is returned,
     * then for any clusters of size 3 or less the items will be rendered as individual markers
     * instead of as a single cluster marker.
     */
    public fun getMinClusterSize(): Int {
        return minClusterSize
    }

    /**
     * Sets the minimum cluster size used to render clusters. For example, if "4" is provided,
     * then for any clusters of size 3 or less the items will be rendered as individual markers
     * instead of as a single cluster marker.
     *
     * @param minClusterSize the minimum cluster size used to render clusters. For example, if "4"
     *                       is provided, then for any clusters of size 3 or less the items will be
     *                       rendered as individual markers instead of as a single cluster marker.
     */
    public fun setMinClusterSize(minClusterSize: Int) {
        this.minClusterSize = minClusterSize
    }

    /**
     * ViewModifier ensures only one re-rendering of the view occurs at a time, and schedules
     * re-rendering, which is performed by the RenderTask.
     */
    private inner class ViewModifier {
        private val mutex = Mutex()
        private var nextClusters: RenderTask? = null

        fun queue(clusters: Set<Cluster<T>>) {
            CoroutineScope(Dispatchers.Main).launch {
                mutex.withLock {
                    nextClusters = RenderTask(clusters)
                    processQueue()
                }
            }
        }

        private fun processQueue() {
            if (nextClusters == null) {
                return
            }

            val projection = map.projection
            val renderTask = nextClusters!!
            nextClusters = null

            renderTask.setCallback {
                CoroutineScope(Dispatchers.Main).launch {
                    mutex.withLock {
                        processQueue()
                    }
                }
            }
            renderTask.setProjection(projection)
            renderTask.setMapZoom(map.cameraPosition.zoom)
            CoroutineScope(executor).launch {
                renderTask.run()
            }
        }
    }

    /**
     * Determine whether the cluster should be rendered as individual markers or a cluster.
     *
     * @param cluster cluster to examine for rendering
     * @return true if the provided cluster should be rendered as a single marker on the map, false
     * if the items within this cluster should be rendered as individual markers instead.
     */
    protected open fun shouldRenderAsCluster(@NonNull cluster: Cluster<T>): Boolean {
        return cluster.size >= minClusterSize
    }

    /**
     * Determines if the new clusters should be rendered on the map, given the old clusters. This
     * method is primarily for optimization of performance, and the default implementation simply
     * checks if the new clusters are equal to the old clusters, and if so, it returns false.
     *
     * However, there are cases where you may want to re-render the clusters even if they didn't
     * change. For example, if you want a cluster with one item to render as a cluster above
     * a certain zoom level and as a marker below a certain zoom level (even if the contents of the
     * clusters themselves did not change). In this case, you could check the zoom level in an
     * implementation of this method and if that zoom level threshold is crossed return true, else
     * `return super.shouldRender(oldClusters, newClusters)`.
     *
     * Note that always returning true from this method could potentially have negative performance
     * implications as clusters will be re-rendered on each pass even if they don't change.
     *
     * @param oldClusters The clusters from the previous iteration of the clustering algorithm
     * @param newClusters The clusters from the current iteration of the clustering algorithm
     * @return true if the new clusters should be rendered on the map, and false if they should not. This
     * method is primarily for optimization of performance, and the default implementation simply
     * checks if the new clusters are equal to the old clusters, and if so, it returns false.
     */
    protected open fun shouldRender(
        @NonNull oldClusters: Set<Cluster<T>>,
        @NonNull newClusters: Set<Cluster<T>>
    ): Boolean {
        return newClusters != oldClusters
    }

    /**
     * Transforms the current view (represented by DefaultClusterRenderer.mClusters and DefaultClusterRenderer.mZoom) to a
     * new zoom level and set of clusters.
     *
     * This must be run off the UI thread. Work is coordinated in the RenderTask, then queued up to
     * be executed by a MarkerModifier.
     *
     * There are three stages for the render:
     *
     * 1. Markers are added to the map
     *
     * 2. Markers are animated to their final position
     *
     * 3. Any old markers are removed from the map
     *
     * When zooming in, markers are animated out from the nearest existing cluster. When zooming
     * out, existing clusters are animated to the nearest new cluster.
     */
    private inner class RenderTask(
        val clusters: Set<Cluster<T>>
    ) : Runnable {
        private var callback: Runnable? = null
        private var projection: Projection? = null
        private var sphericalMercatorProjection: SphericalMercatorProjection? = null
        private var mapZoom: Float = 0f

        /**
         * A callback to be run when all work has been completed.
         */
        fun setCallback(callback: Runnable?) {
            this.callback = callback
        }

        fun setProjection(projection: Projection?) {
            this.projection = projection
        }

        fun setMapZoom(zoom: Float) {
            mapZoom = zoom
            sphericalMercatorProjection =
                SphericalMercatorProjection(256.0 * 2.0.pow(mapZoom.coerceAtMost(this@DefaultAdvancedMarkersClusterRenderer.zoom).toDouble()))
        }

        override fun run() {
            if (!shouldRender(immutableOf(this@DefaultAdvancedMarkersClusterRenderer.clusters), immutableOf(clusters))) {
                callback?.run()
                return
            }

            val markerModifier = MarkerModifier()

            val zoom = mapZoom
            val zoomingIn = zoom > this@DefaultAdvancedMarkersClusterRenderer.zoom
            val zoomDelta = zoom - this@DefaultAdvancedMarkersClusterRenderer.zoom

            val markersToRemove = this@DefaultAdvancedMarkersClusterRenderer.markers
            // Prevent crashes: https://issuetracker.google.com/issues/35827242
            val visibleBounds: LatLngBounds = try {
                projection!!.visibleRegion.latLngBounds
            } catch (e: Exception) {
                e.printStackTrace()
                LatLngBounds.builder()
                    .include(LatLng(0.0, 0.0))
                    .build()
            }
            // TODO: Add some padding, so that markers can animate in from off-screen.

            // Find all of the existing clusters that are on-screen. These are candidates for
            // markers to animate from.
            var existingClustersOnScreen: MutableList<Point>? = null
            if (this@DefaultAdvancedMarkersClusterRenderer.clusters != null && animate) {
                existingClustersOnScreen = mutableListOf()
                for (c in this@DefaultAdvancedMarkersClusterRenderer.clusters!!) {
                    if (shouldRenderAsCluster(c) && visibleBounds.contains(c.position)) {
                        val point = sphericalMercatorProjection!!.toPoint(c.position)
                        existingClustersOnScreen.add(point)
                    }
                }
            }

            // Create the new markers and animate them to their new positions.
            val newMarkers = Collections.newSetFromMap(
                ConcurrentHashMap<MarkerWithPosition, Boolean>()
            )
            val createMarkerScope = CoroutineScope(executor)
            for (c in clusters) {
                val onScreen = visibleBounds.contains(c.position)
                createMarkerScope.launch {
                    if (zoomingIn && onScreen && animate) {
                        val point = sphericalMercatorProjection!!.toPoint(c.position)
                        val closest = findClosestCluster(existingClustersOnScreen, point)
                        if (closest != null) {
                            val animateTo = sphericalMercatorProjection!!.toLatLng(closest)
                            markerModifier.add(true, CreateMarkerTask(c, newMarkers, animateTo))
                        } else {
                            markerModifier.add(true, CreateMarkerTask(c, newMarkers, null))
                        }
                    } else {
                        markerModifier.add(onScreen, CreateMarkerTask(c, newMarkers, null))
                    }
                }
            }

            // Wait for all markers to be added.
            runBlocking(executor) { markerModifier.waitUntilFree() }

            // Don't remove any markers that were just added. This is basically anything that had
            // a hit in the MarkerCache.
            markersToRemove.removeAll(newMarkers)

            // Find all of the new clusters that were added on-screen. These are candidates for
            // markers to animate from.
            var newClustersOnScreen: MutableList<Point>? = null
            if (animate) {
                newClustersOnScreen = mutableListOf()
                for (c in clusters) {
                    if (shouldRenderAsCluster(c) && visibleBounds.contains(c.position)) {
                        val p = sphericalMercatorProjection!!.toPoint(c.position)
                        newClustersOnScreen.add(p)
                    }
                }
            }

            val removeMarkerScope = CoroutineScope(executor)
            // Remove the old markers, animating them into clusters if zooming out.
            for (marker in markersToRemove) {
                removeMarkerScope.launch {
                    val onScreen = visibleBounds.contains(marker.position)
                    // Don't animate when zooming out more than 3 zoom levels.
                    // TODO: drop animation based on speed of device & number of markers to animate.
                    if (!zoomingIn && zoomDelta > -3 && onScreen && animate) {
                        val point = sphericalMercatorProjection!!.toPoint(marker.position)
                        val closest = findClosestCluster(newClustersOnScreen, point)
                        if (closest != null) {
                            val animateTo = sphericalMercatorProjection!!.toLatLng(closest)
                            markerModifier.animateThenRemove(marker, marker.position, animateTo)
                        } else {
                            markerModifier.remove(true, marker.marker)
                        }
                    } else {
                        markerModifier.remove(onScreen, marker.marker)
                    }
                }
            }

            runBlocking(executor) { markerModifier.waitUntilFree() }

            this@DefaultAdvancedMarkersClusterRenderer.markers = newMarkers
            this@DefaultAdvancedMarkersClusterRenderer.clusters = clusters
            this@DefaultAdvancedMarkersClusterRenderer.zoom = zoom

            callback?.run()
        }
    }

    override fun onClustersChanged(clusters: Set<Cluster<T>>) {
        viewModifier.queue(clusters)
    }

    override fun setOnClusterClickListener(listener: ClusterManager.OnClusterClickListener<T>?) {
        onClusterClickListener = listener
    }

    override fun setOnClusterInfoWindowClickListener(listener: ClusterManager.OnClusterInfoWindowClickListener<T>?) {
        onClusterInfoWindowClickListener = listener
    }

    override fun setOnClusterInfoWindowLongClickListener(listener: ClusterManager.OnClusterInfoWindowLongClickListener<T>?) {
        onClusterInfoWindowLongClickListener = listener
    }

    override fun setOnClusterItemClickListener(listener: ClusterManager.OnClusterItemClickListener<T>?) {
        onClusterItemClickListener = listener
    }

    override fun setOnClusterItemInfoWindowClickListener(listener: ClusterManager.OnClusterItemInfoWindowClickListener<T>?) {
        onClusterItemInfoWindowClickListener = listener
    }

    override fun setOnClusterItemInfoWindowLongClickListener(listener: ClusterManager.OnClusterItemInfoWindowLongClickListener<T>?) {
        onClusterItemInfoWindowLongClickListener = listener
    }

    override fun setAnimation(animate: Boolean) {
        this.animate = animate
    }

    /**
     * The default duration is 300 milliseconds.
     *
     * @param animationDurationMs long: The length of the animation, in milliseconds. This value cannot be negative.
     */
    override fun setAnimationDuration(animationDurationMs: Long) {
        this.animationDurationMs = animationDurationMs
    }

    private fun immutableOf(clusters: Set<Cluster<T>>?): Set<Cluster<T>> {
        return clusters?.let { Collections.unmodifiableSet(it) } ?: Collections.emptySet()
    }

    private fun distanceSquared(a: Point, b: Point): Double {
        return (a.x - b.x).pow(2) + (a.y - b.y).pow(2)
    }

    private fun findClosestCluster(markers: List<Point>?, point: Point): Point? {
        if (markers == null || markers.isEmpty()) return null

        val maxDistance = clusterManager.algorithm.maxDistanceBetweenClusteredItems
        var minDistSquared = (maxDistance * maxDistance).toDouble()
        var closest: Point? = null
        for (candidate in markers) {
            val dist = distanceSquared(candidate, point)
            if (dist < minDistSquared) {
                closest = candidate
                minDistSquared = dist
            }
        }
        return closest
    }

    /**
     * Handles all markerWithPosition manipulations on the map. Work (such as adding, removing, or
     * animating a markerWithPosition) is performed while trying not to block the rest of the app's
     * UI.
     */
    private inner class MarkerModifier {
        private val mutex = Mutex()
        private val tasksChannel = Channel<suspend () -> Unit>(Channel.UNLIMITED)
        private val mainCoroutineScope = CoroutineScope(Dispatchers.Main)
        private var activeJobs = 0

        init {
            mainCoroutineScope.launch {
                for (task in tasksChannel) {
                    task.invoke()
                    mutex.withLock {
                        activeJobs--
                    }
                }
            }
        }

        private suspend fun enqueueTask(task: suspend () -> Unit) {
            mutex.withLock {
                activeJobs++
                tasksChannel.send(task)
            }
        }

        suspend fun add(priority: Boolean, c: CreateMarkerTask) {
            enqueueTask {
                // Don't use priority in coroutine channel, just add
                c.perform(this)
            }
        }

        suspend fun remove(priority: Boolean, m: Marker) {
            enqueueTask {
                removeMarker(m)
            }
        }

        suspend fun animate(marker: MarkerWithPosition, from: LatLng, to: LatLng) {
            enqueueTask {
                AnimationTask(marker, from, to).perform()
            }
        }

        suspend fun animateThenRemove(marker: MarkerWithPosition, from: LatLng, to: LatLng) {
            enqueueTask {
                val animationTask = AnimationTask(marker, from, to)
                animationTask.removeOnAnimationComplete(clusterManager.markerManager)
                animationTask.perform()
            }
        }

        private fun removeMarker(m: Marker) {
            markerCache.remove(m)
            clusterMarkerCache.remove(m)
            clusterManager.markerManager.remove(m)
        }

        suspend fun waitUntilFree() {
            mutex.withLock {
                while (activeJobs > 0) {
                    delay(10) // Small delay to yield control and allow tasks to process
                }
            }
        }
    }

    /**
     * A cache of markers representing individual ClusterItems.
     */
    private class MarkerCache<T> {
        private val cache = ConcurrentHashMap<T, Marker>()
        private val cacheReverse = ConcurrentHashMap<Marker, T>()

        operator fun get(item: T): Marker? {
            return cache[item]
        }

        operator fun get(m: Marker): T? {
            return cacheReverse[m]
        }

        fun put(item: T, m: Marker) {
            cache[item] = m
            cacheReverse[m] = item
        }

        fun remove(m: Marker) {
            val item = cacheReverse[m]
            cacheReverse.remove(m)
            cache.remove(item)
        }

        fun containsItem(item: T): Boolean {
            return cache.containsKey(item)
        }

        fun containsMarker(marker: Marker): Boolean {
            return cacheReverse.containsKey(marker)
        }
    }

    /**
     * Called before the marker for a ClusterItem is added to the map. The default implementation
     * sets the marker and snippet text based on the respective item text if they are both
     * available, otherwise it will set the title if available, and if not it will set the marker
     * title to the item snippet text if that is available.
     *
     * The first time [ClusterManager.cluster] is invoked on a set of items
     * [onBeforeClusterItemRendered] will be called and
     * [onClusterItemUpdated] will not be called.
     * If an item is removed and re-added (or updated) and [ClusterManager.cluster] is
     * invoked again, then [onClusterItemUpdated] will be called and
     * [onBeforeClusterItemRendered] will not be called.
     *
     * @param item          item to be rendered
     * @param advancedMarkerOptions the AdvancedMarkerOptions representing the provided item
     */
    protected open fun onBeforeClusterItemRendered(
        @NonNull item: T,
        @NonNull advancedMarkerOptions: AdvancedMarkerOptions
    ) {
        if (item.title != null && item.snippet != null) {
            advancedMarkerOptions.title(item.title)
            advancedMarkerOptions.snippet(item.snippet)
        } else if (item.title != null) {
            advancedMarkerOptions.title(item.title)
        } else if (item.snippet != null) {
            advancedMarkerOptions.title(item.snippet)
        }
        item.zIndex?.let { advancedMarkerOptions.zIndex(it) }
    }

    /**
     * Called when a cached marker for a ClusterItem already exists on the map so the marker may
     * be updated to the latest item values. Default implementation updates the title and snippet
     * of the marker if they have changed and refreshes the info window of the marker if it is open.
     * Note that the contents of the item may not have changed since the cached marker was created -
     * implementations of this method are responsible for checking if something changed (if that
     * matters to the implementation).
     *
     * The first time [ClusterManager.cluster] is invoked on a set of items
     * [onBeforeClusterItemRendered] will be called and
     * [onClusterItemUpdated] will not be called.
     * If an item is removed and re-added (or updated) and [ClusterManager.cluster] is
     * invoked again, then [onClusterItemUpdated] will be called and
     * [onBeforeClusterItemRendered] will not be called.
     *
     * @param item   item being updated
     * @param marker cached marker that contains a potentially previous state of the item.
     */
    protected open fun onClusterItemUpdated(@NonNull item: T, @NonNull marker: Marker) {
        var changed = false
        // Update marker text if the item text changed - same logic as adding marker in CreateMarkerTask.perform()
        if (item.title != null && item.snippet != null) {
            if (item.title != marker.title) {
                marker.title = item.title
                changed = true
            }
            if (item.snippet != marker.snippet) {
                marker.snippet = item.snippet
                changed = true
            }
        } else if (item.snippet != null && item.snippet != marker.title) {
            marker.title = item.snippet
            changed = true
        } else if (item.title != null && item.title != marker.title) {
            marker.title = item.title
            changed = true
        }
        // Update marker position if the item changed position
        if (marker.position != item.position) {
            marker.position = item.position
            item.zIndex?.let { marker.zIndex = it }
            changed = true
        }
        if (changed && marker.isInfoWindowShown) {
            // Force a refresh of marker info window contents
            marker.showInfoWindow()
        }
    }

    /**
     * Called before the marker for a Cluster is added to the map.
     * The default implementation draws a circle with a rough count of the number of items.
     *
     * The first time [ClusterManager.cluster] is invoked on a set of items
     * [onBeforeClusterRendered] will be called and
     * [onClusterUpdated] will not be called. If an item is removed and
     * re-added (or updated) and [ClusterManager.cluster] is invoked
     * again, then [onClusterUpdated] will be called and
     * [onBeforeClusterRendered] will not be called.
     *
     * @param cluster       cluster to be rendered
     * @param advancedMarkerOptions markerOptions representing the provided cluster
     */
    protected open fun onBeforeClusterRendered(
        @NonNull cluster: Cluster<T>,
        @NonNull advancedMarkerOptions: AdvancedMarkerOptions
    ) {
        // TODO: consider adding anchor(.5, .5) (Individual markers will overlap more often)
        advancedMarkerOptions.icon(getDescriptorForCluster(cluster))
        val items = ArrayList(cluster.items)
        if (items.isNotEmpty()) {
            items[0].zIndex?.let { advancedMarkerOptions.zIndex(it) }
        }
    }

    /**
     * Gets a BitmapDescriptor for the given cluster that contains a rough count of the number of
     * items. Used to set the cluster marker icon in the default implementations of
     * [onBeforeClusterRendered] and
     * [onClusterUpdated].
     *
     * @param cluster cluster to get BitmapDescriptor for
     * @return a BitmapDescriptor for the marker icon for the given cluster that contains a rough
     * count of the number of items.
     */
    @NonNull
    protected open fun getDescriptorForCluster(@NonNull cluster: Cluster<T>): BitmapDescriptor {
        val bucket = getBucket(cluster)
        var descriptor = icons.get(bucket)
        if (descriptor == null) {
            coloredCircleBackground.paint.color = getColor(bucket)
            iconGenerator.setTextAppearance(getClusterTextAppearance(bucket))
            descriptor = BitmapDescriptorFactory.fromBitmap(iconGenerator.makeIcon(getClusterText(bucket)))
            icons.put(bucket, descriptor)
        }
        return descriptor
    }

    /**
     * Called after the marker for a Cluster has been added to the map.
     *
     * @param cluster the cluster that was just added to the map
     * @param marker  the marker representing the cluster that was just added to the map
     */
    protected open fun onClusterRendered(@NonNull cluster: Cluster<T>, @NonNull marker: Marker) {}

    /**
     * Called when a cached marker for a Cluster already exists on the map so the marker may
     * be updated to the latest cluster values. Default implementation updated the icon with a
     * circle with a rough count of the number of items. Note that the contents of the cluster may
     * not have changed since the cached marker was created - implementations of this method are
     * responsible for checking if something changed (if that matters to the implementation).
     *
     * The first time [ClusterManager.cluster] is invoked on a set of items
     * [onBeforeClusterRendered] will be called and
     * [onClusterUpdated] will not be called. If an item is removed and
     * re-added (or updated) and [ClusterManager.cluster] is invoked
     * again, then [onClusterUpdated] will be called and
     * [onBeforeClusterRendered] will not be called.
     *
     * @param cluster cluster being updated
     * @param marker  cached marker that contains a potentially previous state of the cluster
     */
    protected open fun onClusterUpdated(@NonNull cluster: Cluster<T>, @NonNull marker: AdvancedMarker) {
        // TODO: consider adding anchor(.5, .5) (Individual markers will overlap more often)
        marker.setIcon(getDescriptorForCluster(cluster));
    }

    /**
     * Called after the marker for a ClusterItem has been added to the map.
     *
     * @param clusterItem the item that was just added to the map
     * @param marker      the marker representing the item that was just added to the map
     */
    protected open fun onClusterItemRendered(@NonNull clusterItem: T, @NonNull marker: Marker) {}

    /**
     * Get the marker from a ClusterItem
     *
     * @param clusterItem ClusterItem which you will obtain its marker
     * @return a marker from a ClusterItem or null if it does not exists
     */
    public fun getMarker(clusterItem: T): Marker? {
        return markerCache[clusterItem]
    }

    /**
     * Get the ClusterItem from a marker
     *
     * @param marker which you will obtain its ClusterItem
     * @return a ClusterItem from a marker or null if it does not exists
     */
    public fun getClusterItem(marker: Marker): T? {
        return markerCache[marker]
    }

    /**
     * Get the marker from a Cluster
     *
     * @param cluster which you will obtain its marker
     * @return a marker from a cluster or null if it does not exists
     */
    public fun getMarker(cluster: Cluster<T>): Marker? {
        return clusterMarkerCache[cluster]
    }

    /**
     * Get the Cluster from a marker
     *
     * @param marker which you will obtain its Cluster
     * @return a Cluster from a marker or null if it does not exists
     */
    public fun getCluster(marker: Marker): Cluster<T>? {
        return clusterMarkerCache[marker]
    }

    /**
     * Creates markerWithPosition(s) for a particular cluster, animating it if necessary.
     */
    private inner class CreateMarkerTask(
        private val cluster: Cluster<T>,
        private val newMarkers: MutableSet<MarkerWithPosition>,
        private val animateFrom: LatLng?
    ) {
        suspend fun perform(markerModifier: MarkerModifier) {
            // Don't show small clusters. Render the markers inside, instead.
            if (!shouldRenderAsCluster(cluster)) {
                for (item in cluster.items) {
                    var marker = markerCache[item]
                    val markerWithPosition: MarkerWithPosition
                    if (marker == null) {
                        val advancedMarkerOptions = AdvancedMarkerOptions()
                        if (animateFrom != null) {
                            advancedMarkerOptions.position(animateFrom)
                        } else {
                            advancedMarkerOptions.position(item.position)
                            item.zIndex?.let { advancedMarkerOptions.zIndex(it) }
                        }
                        onBeforeClusterItemRendered(item, advancedMarkerOptions)
                        marker = clusterManager.markerCollection.addMarker(advancedMarkerOptions) as AdvancedMarker
                        markerWithPosition = MarkerWithPosition(marker)
                        markerCache.put(item, marker)
                        if (animateFrom != null) {
                            markerModifier.animate(markerWithPosition, animateFrom, item.position)
                        }
                    } else {
                        markerWithPosition = MarkerWithPosition(marker)
                        onClusterItemUpdated(item, marker)
                    }
                    onClusterItemRendered(item, marker)
                    newMarkers.add(markerWithPosition)
                }
                return
            }

            var marker = clusterMarkerCache[cluster]
            val markerWithPosition: MarkerWithPosition
            if (marker == null) {
                val advancedMarkerOptions = AdvancedMarkerOptions().position(animateFrom ?: cluster.position)
                onBeforeClusterRendered(cluster, advancedMarkerOptions)
                marker = clusterManager.clusterMarkerCollection.addMarker(advancedMarkerOptions) as AdvancedMarker
                clusterMarkerCache.put(cluster, marker)
                markerWithPosition = MarkerWithPosition(marker)
                if (animateFrom != null) {
                    markerModifier.animate(markerWithPosition, animateFrom, cluster.position)
                }
            } else {
                markerWithPosition = MarkerWithPosition(marker)
                onClusterUpdated(cluster, marker as AdvancedMarker)
            }
            onClusterRendered(cluster, marker)
            newMarkers.add(markerWithPosition)
        }
    }

    /**
     * A Marker and its position. [Marker.getPosition] must be called from the UI thread, so this
     * object allows lookup from other threads.
     */
    private class MarkerWithPosition(
        val marker: Marker
    ) {
        var position: LatLng = marker.position

        override fun equals(other: Any?): Boolean {
            if (other !is MarkerWithPosition) {
                return false
            }
            return marker == other.marker
        }

        override fun hashCode(): Int {
            return marker.hashCode()
        }
    }

    private val ANIMATION_INTERP: TimeInterpolator = DecelerateInterpolator()

    /**
     * Animates a markerWithPosition from one position to another. TODO: improve performance for
     * slow devices (e.g. Nexus S).
     */
    private inner class AnimationTask(
        private val markerWithPosition: MarkerWithPosition,
        private val from: LatLng,
        private val to: LatLng
    ) : AnimatorListenerAdapter(), ValueAnimator.AnimatorUpdateListener {
        private val marker: Marker = markerWithPosition.marker
        private var removeOnComplete: Boolean = false
        private var markerManager: MarkerManager? = null
        private var valueAnimator: ValueAnimator? = null

        fun perform() {
            valueAnimator = ValueAnimator.ofFloat(0.0f, 1.0f)
            valueAnimator?.interpolator = ANIMATION_INTERP
            valueAnimator?.duration = animationDurationMs
            valueAnimator?.addUpdateListener(this)
            valueAnimator?.addListener(this)
            valueAnimator?.start()
        }

        fun cancel() {
            if (Looper.myLooper() != Looper.getMainLooper()) {
                Handler(Looper.getMainLooper()).post(this::cancel)
                return
            }
            markerWithPosition.position = to
            removeOnComplete = false
            valueAnimator?.cancel()
        }

        override fun onAnimationEnd(animation: Animator) {
            if (removeOnComplete) {
                markerCache.remove(marker)
                clusterMarkerCache.remove(marker)
                markerManager?.remove(marker)
            }
            markerWithPosition.position = to
        }

        fun removeOnAnimationComplete(markerManager: MarkerManager) {
            this.markerManager = markerManager
            removeOnComplete = true
        }

        override fun onAnimationUpdate(@NonNull valueAnimator: ValueAnimator) {
            val fraction = valueAnimator.animatedFraction
            val lat = (to.latitude - from.latitude) * fraction + from.latitude
            var lngDelta = to.longitude - from.longitude

            // Take the shortest path across the 180th meridian.
            if (Math.abs(lngDelta) > 180) {
                lngDelta -= Math.signum(lngDelta) * 360
            }
            val lng = lngDelta * fraction + from.longitude
            val position = LatLng(lat, lng)
            marker.position = position
        }
    }
}
