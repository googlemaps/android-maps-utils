/*
 * Copyright 2025 Google LLC
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
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.AccelerateInterpolator
import android.view.animation.BounceInterpolator
import android.view.animation.DecelerateInterpolator
import android.view.animation.LinearInterpolator
import androidx.annotation.NonNull
import androidx.annotation.StyleRes
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.Projection
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.maps.android.clustering.Cluster
import com.google.maps.android.clustering.ClusterItem
import com.google.maps.android.clustering.ClusterManager
import com.google.maps.android.collections.MarkerManager
import com.google.maps.android.data.RendererLogger
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
import java.util.concurrent.locks.Lock
import java.util.concurrent.locks.ReentrantLock
import kotlin.math.pow

/**
 * The default view for a ClusterManager. Markers are animated in and out of clusters.
 */
public open class ClusterRendererMultipleItems<T : ClusterItem>(
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
    private val ongoingAnimations = LinkedList<AnimationTask>()
    private var animationInterp: TimeInterpolator = DecelerateInterpolator()

    private val buckets = intArrayOf(10, 20, 50, 100, 200, 500, 1000)
    private lateinit var coloredCircleBackground: ShapeDrawable

    public enum class AnimationType {
        LINEAR,
        EASE_IN,
        EASE_OUT,
        EASE_IN_OUT,
        FAST_OUT_SLOW_IN,
        BOUNCE,
        ACCELERATE,
        DECELERATE
    }

    public fun setAnimationType(type: AnimationType) {
        animationInterp = when (type) {
            AnimationType.LINEAR -> LinearInterpolator()
            AnimationType.EASE_IN, AnimationType.ACCELERATE -> AccelerateInterpolator()
            AnimationType.EASE_OUT, AnimationType.DECELERATE -> DecelerateInterpolator()
            AnimationType.EASE_IN_OUT -> AccelerateDecelerateInterpolator()
            AnimationType.FAST_OUT_SLOW_IN -> FastOutSlowInInterpolator()
            AnimationType.BOUNCE -> BounceInterpolator()
        }
    }

    /**
     * Sets the interpolator for the animation.
     *
     * @param interpolator the interpolator to use for the animation.
     */
    public fun setAnimationInterpolator(interpolator: TimeInterpolator) {
        animationInterp = interpolator
    }

    /**
     * Markers that are currently on the map.
     */
    private var markers: MutableSet<MarkerWithPosition<T>> = Collections.newSetFromMap(
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
    private var minClusterSize = 2

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
        density = context.resources.displayMetrics.density
        iconGenerator = IconGenerator(context)
        iconGenerator.setContentView(makeSquareTextView(context))
        iconGenerator.setTextAppearance(R.style.amu_ClusterIcon_TextAppearance)
        iconGenerator.setBackground(makeClusterBackground())
    }

    override fun onAdd() {
        RendererLogger.d("ClusterRenderer", "Setting up MarkerCollection listeners")

        clusterManager.markerCollection.setOnMarkerClickListener { marker ->
            RendererLogger.d("ClusterRenderer", "Marker clicked: $marker")
            onClusterItemClickListener?.onClusterItemClick(markerCache[marker]!!) ?: false
        }

        clusterManager.markerCollection.setOnInfoWindowClickListener { marker ->
            RendererLogger.d("ClusterRenderer", "Info window clicked for marker: $marker")
            onClusterItemInfoWindowClickListener?.onClusterItemInfoWindowClick(markerCache[marker]!!)
        }

        clusterManager.markerCollection.setOnInfoWindowLongClickListener { marker ->
            RendererLogger.d("ClusterRenderer", "Info window long-clicked for marker: $marker")
            onClusterItemInfoWindowLongClickListener?.onClusterItemInfoWindowLongClick(markerCache[marker]!!)
        }

        RendererLogger.d("ClusterRenderer", "Setting up ClusterMarkerCollection listeners")

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

    /**
     * Enables or disables logging for the cluster renderer.
     *
     * <p>When enabled, the renderer will log internal operations such as cluster rendering,
     * marker updates, and other debug information. This is useful for development and debugging,
     * but should typically be disabled in production builds.</p>
     *
     * @param enabled [true] to enable logging; [false] to disable it.
     */
    public fun setLoggingEnabled(enabled: Boolean) {
        RendererLogger.setEnabled(enabled)
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

        fun setCallback(callback: Runnable) {
            this.callback = callback
        }

        fun setProjection(projection: Projection) {
            this.projection = projection
        }

        fun setMapZoom(zoom: Float) {
            mapZoom = zoom
            sphericalMercatorProjection =
                SphericalMercatorProjection(256.0 * 2.0.pow(mapZoom.coerceAtMost(this@ClusterRendererMultipleItems.zoom).toDouble()))
        }

        override fun run() {
            val markerModifier = MarkerModifier()
            val zoom = mapZoom
            val markersToRemove = markers
            var visibleBounds: LatLngBounds

            try {
                visibleBounds = projection!!.visibleRegion.latLngBounds
                RendererLogger.d("ClusterRenderer", "Visible bounds calculated: $visibleBounds")
            } catch (e: Exception) {
                RendererLogger.e("ClusterRenderer", "Error getting visible bounds, defaulting to (0,0)")
                visibleBounds = LatLngBounds.builder().include(LatLng(0.0, 0.0)).build()
            }

            // Find all of the existing clusters that are on-screen. These are candidates for markers to animate from.
            var existingClustersOnScreen: MutableList<Point>? = null
            if (this@ClusterRendererMultipleItems.clusters != null && animate) {
                existingClustersOnScreen = mutableListOf()
                for (c in this@ClusterRendererMultipleItems.clusters!!) {
                    if (shouldRenderAsCluster(c) && visibleBounds.contains(c.position)) {
                        val point = sphericalMercatorProjection!!.toPoint(c.position)
                        existingClustersOnScreen.add(point)
                    }
                }
                RendererLogger.d("ClusterRenderer", "Existing clusters on screen found: ${existingClustersOnScreen.size}")
            }

            // Create the new markers and animate them to their new positions.
            val newMarkers = Collections.newSetFromMap(ConcurrentHashMap<MarkerWithPosition<T>, Boolean>())
            val createMarkerScope = CoroutineScope(executor)
            for (c in clusters) {
                val onScreen = visibleBounds.contains(c.position)
                createMarkerScope.launch {
                    if (animate) {
                        val point = sphericalMercatorProjection!!.toPoint(c.position)
                        val closest = findClosestCluster(existingClustersOnScreen, point)
                        if (closest != null) {
                            val animateFrom = sphericalMercatorProjection!!.toLatLng(closest)
                            markerModifier.add(true, CreateMarkerTask(c, newMarkers, animateFrom))
                            RendererLogger.d("ClusterRenderer", "Animating cluster from closest cluster: ${c.position}")
                        } else {
                            markerModifier.add(true, CreateMarkerTask(c, newMarkers, null))
                            RendererLogger.d("ClusterRenderer", "Animating cluster without closest point: ${c.position}")
                        }
                    } else {
                        markerModifier.add(onScreen, CreateMarkerTask(c, newMarkers, null))
                        RendererLogger.d("ClusterRenderer", "Adding cluster without animation: ${c.position}")
                    }
                }
            }

            // Wait for all markers to be added.
            runBlocking(executor) { markerModifier.waitUntilFree() }
            RendererLogger.d("ClusterRenderer", "All new markers added, count: ${newMarkers.size}")

            // Don't remove any markers that were just added. This is basically anything that had a hit in the MarkerCache.
            markersToRemove.removeAll(newMarkers)
            RendererLogger.d("ClusterRenderer", "Markers to remove after filtering new markers: ${markersToRemove.size}")

            // Find all of the new clusters that were added on-screen. These are candidates for markers to animate from.
            var newClustersOnScreen: MutableList<Point>? = null
            if (animate) {
                newClustersOnScreen = mutableListOf()
                for (c in clusters) {
                    if (shouldRenderAsCluster(c) && visibleBounds.contains(c.position)) {
                        val p = sphericalMercatorProjection!!.toPoint(c.position)
                        newClustersOnScreen.add(p)
                    }
                }
                RendererLogger.d("ClusterRenderer", "New clusters on screen found: ${newClustersOnScreen.size}")
            }

            val removeMarkerScope = CoroutineScope(executor)
            for (marker in markersToRemove) {
                removeMarkerScope.launch {
                    val onScreen = visibleBounds.contains(marker.position)
                    if (onScreen && animate) {
                        val point = sphericalMercatorProjection!!.toPoint(marker.position)
                        val closest = findClosestCluster(newClustersOnScreen, point)
                        if (closest != null) {
                            val animateTo = sphericalMercatorProjection!!.toLatLng(closest)
                            markerModifier.animateThenRemove(marker, marker.position, animateTo)
                            RendererLogger.d("ClusterRenderer", "Animating then removing marker at position: ${marker.position}")
                        } else if (clusterMarkerCache.containsItem(marker.clusterItem as Cluster<T>)) {
                            val foundItem = clusterMarkerCache.get(marker.clusterItem as Cluster<T>)
                            // Remove it because it will join a cluster
                            markerModifier.animateThenRemove(marker, marker.position, foundItem!!.position)
                            RendererLogger.d("ClusterRenderer", "Animating then removing marker joining cluster at position: ${marker.position}")
                        } else {
                            markerModifier.remove(true, marker.marker)
                            RendererLogger.d("ClusterRenderer", "Removing marker without animation at position: ${marker.position}")
                        }
                    } else {
                        markerModifier.remove(onScreen, marker.marker)
                        RendererLogger.d("ClusterRenderer", "Removing marker (onScreen=$onScreen) at position: ${marker.position}")
                    }
                }
            }

            runBlocking(executor) { markerModifier.waitUntilFree() }
            RendererLogger.d("ClusterRenderer", "All marker removal operations completed.")

            this@ClusterRendererMultipleItems.markers = newMarkers
            this@ClusterRendererMultipleItems.clusters = clusters
            this@ClusterRendererMultipleItems.zoom = zoom

            // Run the callback once everything is done.
            callback?.run()
            RendererLogger.d("ClusterRenderer", "Cluster update callback executed.")
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

    public fun stopAnimation() {
        for (animation in ongoingAnimations) {
            animation.cancel()
        }
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

        suspend fun animate(marker: MarkerWithPosition<T>, from: LatLng, to: LatLng) {
            enqueueTask {
                val animationTask = AnimationTask(marker, from, to)
                ongoingAnimations.add(animationTask)
                animationTask.perform()
            }
        }

        suspend fun animateThenRemove(marker: MarkerWithPosition<T>, from: LatLng, to: LatLng) {
            enqueueTask {
                val animationTask = AnimationTask(marker, from, to)
                animationTask.removeOnAnimationComplete(clusterManager.markerManager)
                ongoingAnimations.add(animationTask)
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
     * @param markerOptions the markerOptions representing the provided item
     */
    protected open fun onBeforeClusterItemRendered(
        @NonNull item: T,
        @NonNull markerOptions: MarkerOptions
    ) {
        if (item.title != null && item.snippet != null) {
            markerOptions.title(item.title)
            markerOptions.snippet(item.snippet)
        } else if (item.title != null) {
            markerOptions.title(item.title)
        } else if (item.snippet != null) {
            markerOptions.title(item.snippet)
        }
        item.zIndex?.let { markerOptions.zIndex(it) }
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
     * @param markerOptions markerOptions representing the provided cluster
     */
    protected open fun onBeforeClusterRendered(
        @NonNull cluster: Cluster<T>,
        @NonNull markerOptions: MarkerOptions
    ) {
        // TODO: consider adding anchor(.5, .5) (Individual markers will overlap more often)
        markerOptions.icon(getDescriptorForCluster(cluster))
        val items = ArrayList(cluster.items)
        if (items.isNotEmpty()) {
            items[0].zIndex?.let { markerOptions.zIndex(it) }
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
    protected open fun onClusterUpdated(@NonNull cluster: Cluster<T>, @NonNull marker: Marker) {
        // TODO: consider adding anchor(.5, .5) (Individual markers will overlap more often)
        marker.setIcon(getDescriptorForCluster(cluster))
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
        private val newMarkers: MutableSet<MarkerWithPosition<T>>,
        private val animateFrom: LatLng?
    ) {
        suspend fun perform(markerModifier: MarkerModifier) {
            // Don't show small clusters. Render the markers inside, instead.
            if (!shouldRenderAsCluster(cluster)) {
                RendererLogger.d("ClusterRenderer", "Rendering individual cluster items, count: ${cluster.items.size}")
                for (item in cluster.items) {
                    var marker = markerCache[item]
                    val markerWithPosition: MarkerWithPosition<T>
                    var currentLocation: LatLng? = item.position
                    if (marker == null) {
                        RendererLogger.d("ClusterRenderer", "Creating new marker for cluster item at position: $currentLocation")
                        val markerOptions = MarkerOptions()
                        if (animateFrom != null) {
                            RendererLogger.d("ClusterRenderer", "Animating from position: $animateFrom")
                            markerOptions.position(animateFrom)
                        } else if (markerCache.containsItem(item)) {
                            val foundItem = markerCache.get(item)
                            currentLocation = foundItem!!.position
                            RendererLogger.d("ClusterRenderer", "Found item in cache for animation at position: $currentLocation")
                            markerOptions.position(currentLocation)
                        } else {
                            markerOptions.position(item.position)
                            item.zIndex?.let { markerOptions.zIndex(it) }
                        }
                        onBeforeClusterItemRendered(item, markerOptions)
                        marker = clusterManager.markerCollection.addMarker(markerOptions)
                        markerWithPosition = MarkerWithPosition(marker, item)
                        markerCache.put(item, marker)
                        if (animateFrom != null) {
                            markerModifier.animate(markerWithPosition, animateFrom, item.position)
                            RendererLogger.d("ClusterRenderer", "Animating marker from $animateFrom to ${item.position}")
                        } else if (currentLocation != null) {
                            markerModifier.animate(markerWithPosition, currentLocation, item.position)
                            RendererLogger.d("ClusterRenderer", "Animating marker from $currentLocation to ${item.position}")
                        }
                    } else {
                        markerWithPosition = MarkerWithPosition(marker, item)
                        markerModifier.animate(markerWithPosition, marker.position, item.position)
                        RendererLogger.d("ClusterRenderer", "Animating existing marker from ${marker.position} to ${item.position}")
                        if (markerWithPosition.position != item.position) {
                            RendererLogger.d("ClusterRenderer", "Updating cluster item marker position")
                            onClusterItemUpdated(item, marker)
                        }
                    }
                    onClusterItemRendered(item, marker)
                    newMarkers.add(markerWithPosition)
                }
                return
            }

            // Handle cluster markers
            RendererLogger.d("ClusterRenderer", "Rendering cluster marker at position: ${cluster.position}")
            var marker = clusterMarkerCache[cluster]
            val markerWithPosition: MarkerWithPosition<T>
            if (marker == null) {
                RendererLogger.d("ClusterRenderer", "Creating new cluster marker")
                val markerOptions = MarkerOptions().position(animateFrom ?: cluster.position)
                onBeforeClusterRendered(cluster, markerOptions)
                marker = clusterManager.clusterMarkerCollection.addMarker(markerOptions)
                clusterMarkerCache.put(cluster, marker)
                markerWithPosition = MarkerWithPosition(marker, null)
                if (animateFrom != null) {
                    markerModifier.animate(markerWithPosition, animateFrom, cluster.position)
                    RendererLogger.d("ClusterRenderer", "Animating cluster marker from $animateFrom to ${cluster.position}")
                }
            } else {
                markerWithPosition = MarkerWithPosition(marker, null)
                RendererLogger.d("ClusterRenderer", "Updating existing cluster marker")
                onClusterUpdated(cluster, marker)
            }
            onClusterRendered(cluster, marker)
            newMarkers.add(markerWithPosition)
        }
    }

    /**
     * A Marker and its position. [Marker.getPosition] must be called from the UI thread, so this
     * object allows lookup from other threads.
     */
    private data class MarkerWithPosition<T>(
        val marker: Marker,
        val clusterItem: T?
    ) {
        var position: LatLng = marker.position
    }

    /**
     * Animates a markerWithPosition from one position to another. TODO: improve performance for
     * slow devices (e.g. Nexus S).
     */
    private inner class AnimationTask(
        private val markerWithPosition: MarkerWithPosition<T>,
        private val from: LatLng,
        private val to: LatLng
    ) : AnimatorListenerAdapter(), ValueAnimator.AnimatorUpdateListener {
        private val marker: Marker = markerWithPosition.marker
        private var removeOnComplete: Boolean = false
        private var markerManager: MarkerManager? = null
        private var valueAnimator: ValueAnimator? = null

        fun perform() {
            valueAnimator = ValueAnimator.ofFloat(0.0f, 1.0f)
            valueAnimator?.interpolator = animationInterp
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
            ongoingAnimations.remove(this)
        }

        override fun onAnimationEnd(animation: Animator) {
            if (removeOnComplete) {
                markerCache.remove(marker)
                clusterMarkerCache.remove(marker)
                markerManager?.remove(marker)
            }
            markerWithPosition.position = to

            // Remove the task from the queue
            ongoingAnimations.remove(this)
        }

        fun removeOnAnimationComplete(markerManager: MarkerManager) {
            this.markerManager = markerManager
            removeOnComplete = true
        }

        override fun onAnimationUpdate(@NonNull valueAnimator: ValueAnimator) {
            if (to == null || from == null || marker == null) {
                return
            }

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
            markerWithPosition.position = position
        }
    }
}