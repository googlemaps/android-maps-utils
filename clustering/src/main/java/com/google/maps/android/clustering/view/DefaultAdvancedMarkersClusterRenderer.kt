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
import android.annotation.SuppressLint
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
import com.google.maps.android.collections.MarkerManager
import com.google.maps.android.ui.R
import com.google.maps.android.clustering.Cluster
import com.google.maps.android.clustering.ClusterItem
import com.google.maps.android.clustering.ClusterManager
import com.google.maps.android.geometry.Point
import com.google.maps.android.projection.SphericalMercatorProjection
import com.google.maps.android.ui.IconGenerator
import com.google.maps.android.ui.SquareTextView

import java.util.ArrayList
import java.util.Collections
import java.util.HashMap
import java.util.LinkedList
import java.util.Queue
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executors
import java.util.concurrent.locks.Condition
import java.util.concurrent.locks.ReentrantLock
import kotlin.math.abs
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.sign

/**
 * The default view for a ClusterManager. Markers are animated in and out of clusters.
 */
open class DefaultAdvancedMarkersClusterRenderer<T : ClusterItem>(
    context: Context,
    private val mMap: GoogleMap,
    private val mClusterManager: ClusterManager<T>
) : ClusterRenderer<T> {

    private val mIconGenerator: IconGenerator = IconGenerator(context)
    private val mDensity: Float = context.resources.displayMetrics.density
    private var mAnimate: Boolean = true
    private var mAnimationDurationMs: Long = 300
    private val mExecutor = Executors.newSingleThreadExecutor()
    private var mColoredCircleBackground: ShapeDrawable? = null

    /**
     * Markers that are currently on the map.
     */
    private var mMarkers: MutableSet<MarkerWithPosition> = Collections.newSetFromMap(
        ConcurrentHashMap()
    )

    /**
     * Icons for each bucket.
     */
    private val mIcons = SparseArray<BitmapDescriptor>()

    /**
     * Markers for single ClusterItems.
     */
    private val mMarkerCache = MarkerCache<T>()

    /**
     * If cluster size is less than this size, display individual markers.
     */
    var minClusterSize: Int = 4

    /**
     * The currently displayed set of clusters.
     */
    private var mClusters: Set<Cluster<T>>? = null

    /**
     * Markers for Clusters.
     */
    private val mClusterMarkerCache = MarkerCache<Cluster<T>>()

    /**
     * The target zoom level for the current set of clusters.
     */
    private var mZoom: Float = 0f

    private val mViewModifier = ViewModifier()

    private var mClickListener: ClusterManager.OnClusterClickListener<T>? = null
    private var mInfoWindowClickListener: ClusterManager.OnClusterInfoWindowClickListener<T>? = null
    private var mInfoWindowLongClickListener: ClusterManager.OnClusterInfoWindowLongClickListener<T>? = null
    private var mItemClickListener: ClusterManager.OnClusterItemClickListener<T>? = null
    private var mItemInfoWindowClickListener: ClusterManager.OnClusterItemInfoWindowClickListener<T>? = null
    private var mItemInfoWindowLongClickListener: ClusterManager.OnClusterItemInfoWindowLongClickListener<T>? = null

    init {
        val squareTextView = makeSquareTextView(context)
        mIconGenerator.setContentView(squareTextView)
        mIconGenerator.setTextAppearance(R.style.amu_ClusterIcon_TextAppearance)
        mIconGenerator.setBackground(makeClusterBackground())
    }

    override fun onAdd() {
        mClusterManager.markerCollection.setOnMarkerClickListener { marker ->
            mItemClickListener != null && mItemClickListener!!.onClusterItemClick(mMarkerCache[marker])
        }

        mClusterManager.markerCollection.setOnInfoWindowClickListener { marker ->
            mItemInfoWindowClickListener?.onClusterItemInfoWindowClick(mMarkerCache[marker])
        }

        mClusterManager.markerCollection.setOnInfoWindowLongClickListener { marker ->
            mItemInfoWindowLongClickListener?.onClusterItemInfoWindowLongClick(mMarkerCache[marker])
        }

        mClusterManager.clusterMarkerCollection.setOnMarkerClickListener { marker ->
            mClickListener != null && mClickListener!!.onClusterClick(mClusterMarkerCache[marker])
        }

        mClusterManager.clusterMarkerCollection.setOnInfoWindowClickListener { marker ->
            mInfoWindowClickListener?.onClusterInfoWindowClick(mClusterMarkerCache[marker])
        }

        mClusterManager.clusterMarkerCollection.setOnInfoWindowLongClickListener { marker ->
            mInfoWindowLongClickListener?.onClusterInfoWindowLongClick(mClusterMarkerCache[marker])
        }
    }

    override fun onRemove() {
        mClusterManager.markerCollection.setOnMarkerClickListener(null)
        mClusterManager.markerCollection.setOnInfoWindowClickListener(null)
        mClusterManager.markerCollection.setOnInfoWindowLongClickListener(null)
        mClusterManager.clusterMarkerCollection.setOnMarkerClickListener(null)
        mClusterManager.clusterMarkerCollection.setOnInfoWindowClickListener(null)
        mClusterManager.clusterMarkerCollection.setOnInfoWindowLongClickListener(null)
    }

    private fun makeClusterBackground(): LayerDrawable {
        mColoredCircleBackground = ShapeDrawable(OvalShape())
        val outline = ShapeDrawable(OvalShape())
        outline.paint.color = -0x7f000001 // Transparent white.
        val background = LayerDrawable(arrayOf<Drawable>(outline, mColoredCircleBackground!!))
        val strokeWidth = (mDensity * 3).toInt()
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
        val twelveDpi = (12 * mDensity).toInt()
        squareTextView.setPadding(twelveDpi, twelveDpi, twelveDpi, twelveDpi)
        return squareTextView
    }

    override fun getColor(clusterSize: Int): Int {
        val hueRange = 220f
        val sizeRange = 300f
        val size = min(clusterSize.toFloat(), sizeRange)
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

    protected open fun getClusterText(bucket: Int): String {
        return if (bucket < BUCKETS[0]) {
            bucket.toString()
        } else "$bucket+"
    }

    /**
     * Gets the "bucket" for a particular cluster. By default, uses the number of points within the
     * cluster, bucketed to some set points.
     */
    protected open fun getBucket(cluster: Cluster<T>): Int {
        val size = cluster.size
        if (size <= BUCKETS[0]) {
            return size
        }
        for (i in 0 until BUCKETS.size - 1) {
            if (size < BUCKETS[i + 1]) {
                return BUCKETS[i]
            }
        }
        return BUCKETS[BUCKETS.size - 1]
    }

    /**
     * ViewModifier ensures only one re-rendering of the view occurs at a time, and schedules
     * re-rendering, which is performed by the RenderTask.
     */
    @SuppressLint("HandlerLeak")
    private inner class ViewModifier : Handler(Looper.getMainLooper()) {
        private var mViewModificationInProgress = false
        private var mNextClusters: RenderTask? = null

        override fun handleMessage(msg: Message) {
            if (msg.what == TASK_FINISHED) {
                mViewModificationInProgress = false
                if (mNextClusters != null) {
                    // Run the task that was queued up.
                    sendEmptyMessage(RUN_TASK)
                }
                return
            }
            removeMessages(RUN_TASK)

            if (mViewModificationInProgress) {
                // Busy - wait for the callback.
                return
            }

            if (mNextClusters == null) {
                // Nothing to do.
                return
            }
            val projection = mMap.projection

            var renderTask: RenderTask?
            synchronized(this) {
                renderTask = mNextClusters
                mNextClusters = null
                mViewModificationInProgress = true
            }

            renderTask!!.setCallback { sendEmptyMessage(TASK_FINISHED) }
            renderTask!!.setProjection(projection)
            renderTask!!.setMapZoom(mMap.cameraPosition.zoom)
            mExecutor.execute(renderTask)
        }

        fun queue(clusters: Set<Cluster<T>>) {
            synchronized(this) {
                // Overwrite any pending cluster tasks - we don't care about intermediate states.
                mNextClusters = RenderTask(clusters)
            }
            sendEmptyMessage(RUN_TASK)
        }
    }

    /**
     * Determine whether the cluster should be rendered as individual markers or a cluster.
     *
     * @param cluster cluster to examine for rendering
     * @return true if the provided cluster should be rendered as a single marker on the map, false
     * if the items within this cluster should be rendered as individual markers instead.
     */
    protected open fun shouldRenderAsCluster(cluster: Cluster<T>): Boolean {
        return cluster.size >= minClusterSize
    }

    /**
     * Determines if the new clusters should be rendered on the map, given the old clusters. This
     * method is primarily for optimization of performance, and the default implementation simply
     * checks if the new clusters are equal to the old clusters, and if so, it returns false.
     *
     *
     * However, there are cases where you may want to re-render the clusters even if they didn't
     * change. For example, if you want a cluster with one item to render as a cluster above
     * a certain zoom level and as a marker below a certain zoom level (even if the contents of the
     * clusters themselves did not change). In this case, you could check the zoom level in an
     * implementation of this method and if that zoom level threshold is crossed return true, else
     * `return super.shouldRender(oldClusters, newClusters)`.
     *
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
    protected open fun shouldRender(oldClusters: Set<Cluster<T>>, newClusters: Set<Cluster<T>>): Boolean {
        return newClusters != oldClusters
    }

    /**
     * Transforms the current view (represented by DefaultAdvancedMarkersClusterRenderer.mClusters and DefaultAdvancedMarkersClusterRenderer.mZoom) to a
     * new zoom level and set of clusters.
     *
     *
     * This must be run off the UI thread. Work is coordinated in the RenderTask, then queued up to
     * be executed by a MarkerModifier.
     *
     *
     * There are three stages for the render:
     *
     *
     * 1. Markers are added to the map
     *
     *
     * 2. Markers are animated to their final position
     *
     *
     * 3. Any old markers are removed from the map
     *
     *
     * When zooming in, markers are animated out from the nearest existing cluster. When zooming
     * out, existing clusters are animated to the nearest new cluster.
     */
    private inner class RenderTask(val clusters: Set<Cluster<T>>) : Runnable {
        private var mCallback: Runnable? = null
        private var mProjection: Projection? = null
        private var mSphericalMercatorProjection: SphericalMercatorProjection? = null
        private var mMapZoom: Float = 0f

        /**
         * A callback to be run when all work has been completed.
         *
         * @param callback
         */
        fun setCallback(callback: Runnable) {
            mCallback = callback
        }

        fun setProjection(projection: Projection) {
            this.mProjection = projection
        }

        fun setMapZoom(zoom: Float) {
            this.mMapZoom = zoom
            this.mSphericalMercatorProjection = SphericalMercatorProjection(
                256 * 2.0.pow(min(zoom.toDouble(), mZoom.toDouble()))
            )
        }

        @SuppressLint("NewApi")
        override fun run() {
            if (!shouldRender(immutableOf(this@DefaultAdvancedMarkersClusterRenderer.mClusters), immutableOf(clusters))) {
                mCallback!!.run()
                return
            }

            val markerModifier = MarkerModifier()

            val zoom = mMapZoom
            val zoomingIn = zoom > mZoom
            val zoomDelta = zoom - mZoom

            val markersToRemove = mMarkers
            // Prevent crashes: https://issuetracker.google.com/issues/35827242
            var visibleBounds: LatLngBounds
            try {
                visibleBounds = mProjection!!.visibleRegion.latLngBounds
            } catch (e: Exception) {
                e.printStackTrace()
                visibleBounds = LatLngBounds.builder()
                    .include(LatLng(0.0, 0.0))
                    .build()
            }
            // TODO: Add some padding, so that markers can animate in from off-screen.

            // Find all of the existing clusters that are on-screen. These are candidates for
            // markers to animate from.
            var existingClustersOnScreen: MutableList<Point>? = null
            if (this@DefaultAdvancedMarkersClusterRenderer.mClusters != null && mAnimate) {
                existingClustersOnScreen = ArrayList()
                for (c in this@DefaultAdvancedMarkersClusterRenderer.mClusters!!) {
                    if (shouldRenderAsCluster(c) && visibleBounds.contains(c.position)) {
                        val point = mSphericalMercatorProjection!!.toPoint(c.position)
                        existingClustersOnScreen.add(point)
                    }
                }
            }

            // Create the new markers and animate them to their new positions.
            val newMarkers = Collections.newSetFromMap(
                ConcurrentHashMap<MarkerWithPosition, Boolean>()
            )
            for (c in clusters) {
                val onScreen = visibleBounds.contains(c.position)
                if (zoomingIn && onScreen && mAnimate) {
                    val point = mSphericalMercatorProjection!!.toPoint(c.position)
                    val closest = findClosestCluster(existingClustersOnScreen, point)
                    if (closest != null) {
                        val animateTo = mSphericalMercatorProjection!!.toLatLng(closest)
                        markerModifier.add(true, CreateMarkerTask(c, newMarkers, animateTo))
                    } else {
                        markerModifier.add(true, CreateMarkerTask(c, newMarkers, null))
                    }
                } else {
                    markerModifier.add(onScreen, CreateMarkerTask(c, newMarkers, null))
                }
            }

            // Wait for all markers to be added.
            markerModifier.waitUntilFree()

            // Don't remove any markers that were just added. This is basically anything that had
            // a hit in the MarkerCache.
            markersToRemove.removeAll(newMarkers)

            // Find all of the new clusters that were added on-screen. These are candidates for
            // markers to animate from.
            var newClustersOnScreen: MutableList<Point>? = null
            if (mAnimate) {
                newClustersOnScreen = ArrayList()
                for (c in clusters) {
                    if (shouldRenderAsCluster(c) && visibleBounds.contains(c.position)) {
                        val p = mSphericalMercatorProjection!!.toPoint(c.position)
                        newClustersOnScreen.add(p)
                    }
                }
            }

            // Remove the old markers, animating them into clusters if zooming out.
            for (marker in markersToRemove) {
                val onScreen = visibleBounds.contains(marker.position)
                // Don't animate when zooming out more than 3 zoom levels.
                // TODO: drop animation based on speed of device & number of markers to animate.
                if (!zoomingIn && zoomDelta > -3 && onScreen && mAnimate) {
                    val point = mSphericalMercatorProjection!!.toPoint(marker.position)
                    val closest = findClosestCluster(newClustersOnScreen, point)
                    if (closest != null) {
                        val animateTo = mSphericalMercatorProjection!!.toLatLng(closest)
                        markerModifier.animateThenRemove(marker, marker.position, animateTo!!)
                    } else {
                        markerModifier.remove(true, marker.marker)
                    }
                } else {
                    markerModifier.remove(onScreen, marker.marker)
                }
            }

            markerModifier.waitUntilFree()

            mMarkers = newMarkers
            this@DefaultAdvancedMarkersClusterRenderer.mClusters = clusters
            mZoom = zoom

            mCallback!!.run()
        }
    }

    override fun onClustersChanged(clusters: Set<Cluster<T>>) {
        mViewModifier.queue(clusters)
    }

    override fun setOnClusterClickListener(listener: ClusterManager.OnClusterClickListener<T>?) {
        mClickListener = listener
    }

    override fun setOnClusterInfoWindowClickListener(listener: ClusterManager.OnClusterInfoWindowClickListener<T>?) {
        mInfoWindowClickListener = listener
    }

    override fun setOnClusterInfoWindowLongClickListener(listener: ClusterManager.OnClusterInfoWindowLongClickListener<T>?) {
        mInfoWindowLongClickListener = listener
    }

    override fun setOnClusterItemClickListener(listener: ClusterManager.OnClusterItemClickListener<T>?) {
        mItemClickListener = listener
    }

    override fun setOnClusterItemInfoWindowClickListener(listener: ClusterManager.OnClusterItemInfoWindowClickListener<T>?) {
        mItemInfoWindowClickListener = listener
    }

    override fun setOnClusterItemInfoWindowLongClickListener(listener: ClusterManager.OnClusterItemInfoWindowLongClickListener<T>?) {
        mItemInfoWindowLongClickListener = listener
    }

    override fun setAnimation(animate: Boolean) {
        mAnimate = animate
    }

    /**
     * [.setAnimationDuration] The default duration is 300 milliseconds.
     *
     * @param animationDurationMs long: The length of the animation, in milliseconds. This value cannot be negative.
     */
    override fun setAnimationDuration(animationDurationMs: Long) {
        mAnimationDurationMs = animationDurationMs
    }

    private fun immutableOf(clusters: Set<Cluster<T>>?): Set<Cluster<T>> {
        return if (clusters != null) Collections.unmodifiableSet(clusters) else Collections.emptySet()
    }

    private fun findClosestCluster(markers: List<Point>?, point: Point): Point? {
        if (markers == null || markers.isEmpty()) return null

        val maxDistance = mClusterManager.algorithm.maxDistanceBetweenClusteredItems
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
    @SuppressLint("HandlerLeak")
    private inner class MarkerModifier : Handler(Looper.getMainLooper()), MessageQueue.IdleHandler {

        private val lock = ReentrantLock()
        private val busyCondition = lock.newCondition()

        private val mCreateMarkerTasks: Queue<CreateMarkerTask> = LinkedList()
        private val mOnScreenCreateMarkerTasks: Queue<CreateMarkerTask> = LinkedList()
        private val mRemoveMarkerTasks: Queue<Marker> = LinkedList()
        private val mOnScreenRemoveMarkerTasks: Queue<Marker> = LinkedList()
        private val mAnimationTasks: Queue<AnimationTask> = LinkedList()

        /**
         * Whether the idle listener has been added to the UI thread's MessageQueue.
         */
        private var mListenerAdded: Boolean = false

        /**
         * Creates markers for a cluster some time in the future.
         *
         * @param priority whether this operation should have priority.
         */
        fun add(priority: Boolean, c: CreateMarkerTask) {
            lock.lock()
            sendEmptyMessage(BLANK)
            if (priority) {
                mOnScreenCreateMarkerTasks.add(c)
            } else {
                mCreateMarkerTasks.add(c)
            }
            lock.unlock()
        }

        /**
         * Removes a markerWithPosition some time in the future.
         *
         * @param priority whether this operation should have priority.
         * @param m        the markerWithPosition to remove.
         */
        fun remove(priority: Boolean, m: Marker) {
            lock.lock()
            sendEmptyMessage(BLANK)
            if (priority) {
                mOnScreenRemoveMarkerTasks.add(m)
            } else {
                mRemoveMarkerTasks.add(m)
            }
            lock.unlock()
        }

        /**
         * Animates a markerWithPosition some time in the future.
         *
         * @param marker the markerWithPosition to animate.
         * @param from   the position to animate from.
         * @param to     the position to animate to.
         */
        fun animate(marker: MarkerWithPosition, from: LatLng, to: LatLng) {
            lock.lock()
            mAnimationTasks.add(AnimationTask(marker, from, to))
            lock.unlock()
        }

        /**
         * Animates a markerWithPosition some time in the future, and removes it when the animation
         * is complete.
         *
         * @param marker the markerWithPosition to animate.
         * @param from   the position to animate from.
         * @param to     the position to animate to.
         */
        fun animateThenRemove(marker: MarkerWithPosition, from: LatLng, to: LatLng) {
            lock.lock()
            val animationTask = AnimationTask(marker, from, to)
            animationTask.removeOnAnimationComplete(mClusterManager.markerManager)
            mAnimationTasks.add(animationTask)
            lock.unlock()
        }

        override fun handleMessage(msg: Message) {
            if (!mListenerAdded) {
                Looper.myQueue().addIdleHandler(this)
                mListenerAdded = true
            }
            removeMessages(BLANK)

            lock.lock()
            try {

                // Perform up to 10 tasks at once.
                // Consider only performing 10 remove tasks, not adds and animations.
                // Removes are relatively slow and are much better when batched.
                for (i in 0..9) {
                    performNextTask()
                }

                if (!isBusy) {
                    mListenerAdded = false
                    Looper.myQueue().removeIdleHandler(this)
                    // Signal any other threads that are waiting.
                    busyCondition.signalAll()
                } else {
                    // Sometimes the idle queue may not be called - schedule up some work regardless
                    // of whether the UI thread is busy or not.
                    // TODO: try to remove this.
                    sendEmptyMessageDelayed(BLANK, 10)
                }
            } finally {
                lock.unlock()
            }
        }

        /**
         * Perform the next task. Prioritise any on-screen work.
         */
        private fun performNextTask() {
            if (!mOnScreenRemoveMarkerTasks.isEmpty()) {
                removeMarker(mOnScreenRemoveMarkerTasks.poll())
            } else if (!mAnimationTasks.isEmpty()) {
                mAnimationTasks.poll().perform()
            } else if (!mOnScreenCreateMarkerTasks.isEmpty()) {
                mOnScreenCreateMarkerTasks.poll().perform(this)
            } else if (!mCreateMarkerTasks.isEmpty()) {
                mCreateMarkerTasks.poll().perform(this)
            } else if (!mRemoveMarkerTasks.isEmpty()) {
                removeMarker(mRemoveMarkerTasks.poll())
            }
        }

        private fun removeMarker(m: Marker?) {
            mMarkerCache.remove(m)
            mClusterMarkerCache.remove(m)
            mClusterManager.markerManager.remove(m)
        }

        /**
         * @return true if there is still work to be processed.
         */
        val isBusy: Boolean
            get() {
                try {
                    lock.lock()
                    return !(mCreateMarkerTasks.isEmpty() && mOnScreenCreateMarkerTasks.isEmpty() &&
                            mOnScreenRemoveMarkerTasks.isEmpty() && mRemoveMarkerTasks.isEmpty() &&
                            mAnimationTasks.isEmpty())
                } finally {
                    lock.unlock()
                }
            }

        /**
         * Blocks the calling thread until all work has been processed.
         */
        fun waitUntilFree() {
            while (isBusy) {
                // Sometimes the idle queue may not be called - schedule up some work regardless
                // of whether the UI thread is busy or not.
                // TODO: try to remove this.
                sendEmptyMessage(BLANK)
                lock.lock()
                try {
                    if (isBusy) {
                        busyCondition.await()
                    }
                } catch (e: InterruptedException) {
                    throw RuntimeException(e)
                } finally {
                    lock.unlock()
                }
            }
        }

        override fun queueIdle(): Boolean {
            // When the UI is not busy, schedule some work.
            sendEmptyMessage(BLANK)
            return true
        }
    }

    /**
     * A cache of markers representing individual ClusterItems.
     */
    private class MarkerCache<T> {
        private val mCache: MutableMap<T, Marker> = HashMap()
        private val mCacheReverse: MutableMap<Marker, T> = HashMap()

        operator fun get(item: T): Marker? {
            return mCache[item]
        }

        operator fun get(m: Marker): T? {
            return mCacheReverse[m]
        }

        fun put(item: T, m: Marker) {
            mCache[item] = m
            mCacheReverse[m] = item
        }

        fun remove(m: Marker?) {
            val item = mCacheReverse[m]
            mCacheReverse.remove(m)
            mCache.remove(item)
        }
    }

    /**
     * Called before the marker for a ClusterItem is added to the map. The default implementation
     * sets the marker and snippet text based on the respective item text if they are both
     * available, otherwise it will set the title if available, and if not it will set the marker
     * title to the item snippet text if that is available.
     *
     *
     * The first time [ClusterManager.cluster] is invoked on a set of items
     * [.onBeforeClusterItemRendered] will be called and
     * [.onClusterItemUpdated] will not be called.
     * If an item is removed and re-added (or updated) and [ClusterManager.cluster] is
     * invoked again, then [.onClusterItemUpdated] will be called and
     * [.onBeforeClusterItemRendered] will not be called.
     *
     * @param item   item to be rendered
     * @param advancedMarkerOptions the AdvancedMarkerOptions representing the provided item
     */
    protected open fun onBeforeClusterItemRendered(item: T, advancedMarkerOptions: AdvancedMarkerOptions) {
        if (item.title != null && item.snippet != null) {
            advancedMarkerOptions.title(item.title)
            advancedMarkerOptions.snippet(item.snippet)
        } else if (item.title != null) {
            advancedMarkerOptions.title(item.title)
        } else if (item.snippet != null) {
            advancedMarkerOptions.title(item.snippet)
        }
    }

    /**
     * Called when a cached marker for a ClusterItem already exists on the map so the marker may
     * be updated to the latest item values. Default implementation updates the title and snippet
     * of the marker if they have changed and refreshes the info window of the marker if it is open.
     * Note that the contents of the item may not have changed since the cached marker was created -
     * implementations of this method are responsible for checking if something changed (if that
     * matters to the implementation).
     *
     *
     * The first time [ClusterManager.cluster] is invoked on a set of items
     * [.onBeforeClusterItemRendered] will be called and
     * [.onClusterItemUpdated] will not be called.
     * If an item is removed and re-added (or updated) and [ClusterManager.cluster] is
     * invoked again, then [.onClusterItemUpdated] will be called and
     * [.onBeforeClusterItemRendered] will not be called.
     *
     * @param item   item being updated
     * @param marker cached marker that contains a potentially previous state of the item.
     */
    protected open fun onClusterItemUpdated(item: T, marker: Marker) {
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
            if (item.zIndex != null) {
                marker.zIndex = item.zIndex!!
            }
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
     *
     * The first time [ClusterManager.cluster] is invoked on a set of items
     * [.onBeforeClusterRendered] will be called and
     * [.onClusterUpdated] will not be called. If an item is removed and
     * re-added (or updated) and [ClusterManager.cluster] is invoked
     * again, then [.onClusterUpdated] will be called and
     * [.onBeforeClusterRendered] will not be called.
     *
     * @param cluster       cluster to be rendered
     * @param advancedMarkerOptions markerOptions representing the provided cluster
     */
    protected open fun onBeforeClusterRendered(
        cluster: Cluster<T>,
        advancedMarkerOptions: AdvancedMarkerOptions
    ) {
        // TODO: consider adding anchor(.5, .5) (Individual markers will overlap more often)
        advancedMarkerOptions.icon(getDescriptorForCluster(cluster))
    }

    /**
     * Gets a BitmapDescriptor for the given cluster that contains a rough count of the number of
     * items. Used to set the cluster marker icon in the default implementations of
     * [.onBeforeClusterRendered] and
     * [.onClusterUpdated].
     *
     * @param cluster cluster to get BitmapDescriptor for
     * @return a BitmapDescriptor for the marker icon for the given cluster that contains a rough
     * count of the number of items.
     */
    protected open fun getDescriptorForCluster(cluster: Cluster<T>): BitmapDescriptor {
        val bucket = getBucket(cluster)
        var descriptor = mIcons[bucket]
        if (descriptor == null) {
            mColoredCircleBackground!!.paint.color = getColor(bucket)
            mIconGenerator.setTextAppearance(getClusterTextAppearance(bucket))
            descriptor = BitmapDescriptorFactory.fromBitmap(mIconGenerator.makeIcon(getClusterText(bucket)))
            mIcons.put(bucket, descriptor)
        }
        return descriptor
    }

    /**
     * Called after the marker for a Cluster has been added to the map.
     *
     * @param cluster the cluster that was just added to the map
     * @param marker  the marker representing the cluster that was just added to the map
     */
    protected open fun onClusterRendered(cluster: Cluster<T>, marker: Marker) {}

    /**
     * Called when a cached marker for a Cluster already exists on the map so the marker may
     * be updated to the latest cluster values. Default implementation updated the icon with a
     * circle with a rough count of the number of items. Note that the contents of the cluster may
     * not have changed since the cached marker was created - implementations of this method are
     * responsible for checking if something changed (if that matters to the implementation).
     *
     *
     * The first time [ClusterManager.cluster] is invoked on a set of items
     * [.onBeforeClusterRendered] will be called and
     * [.onClusterUpdated] will not be called. If an item is removed and
     * re-added (or updated) and [ClusterManager.cluster] is invoked
     * again, then [.onClusterUpdated] will be called and
     * [.onBeforeClusterRendered] will not be called.
     *
     * @param cluster cluster being updated
     * @param marker  cached marker that contains a potentially previous state of the cluster
     */
    protected open fun onClusterUpdated(cluster: Cluster<T>, marker: AdvancedMarker) {
        // TODO: consider adding anchor(.5, .5) (Individual markers will overlap more often)
        marker.setIcon(getDescriptorForCluster(cluster))
    }

    /**
     * Called after the marker for a ClusterItem has been added to the map.
     *
     * @param clusterItem the item that was just added to the map
     * @param marker      the marker representing the item that was just added to the map
     */
    protected open fun onClusterItemRendered(clusterItem: T, marker: Marker) {}

    /**
     * Get the marker from a ClusterItem
     *
     * @param clusterItem ClusterItem which you will obtain its marker
     * @return a marker from a ClusterItem or null if it does not exists
     */
    fun getMarker(clusterItem: T): Marker? {
        return mMarkerCache[clusterItem]
    }

    /**
     * Get the ClusterItem from a marker
     *
     * @param marker which you will obtain its ClusterItem
     * @return a ClusterItem from a marker or null if it does not exists
     */
    fun getClusterItem(marker: Marker): T? {
        return mMarkerCache[marker]
    }

    /**
     * Get the marker from a Cluster
     *
     * @param cluster which you will obtain its marker
     * @return a marker from a cluster or null if it does not exists
     */
    fun getMarker(cluster: Cluster<T>): Marker? {
        return mClusterMarkerCache[cluster]
    }

    /**
     * Get the Cluster from a marker
     *
     * @param marker which you will obtain its Cluster
     * @return a Cluster from a marker or null if it does not exists
     */
    fun getCluster(marker: Marker): Cluster<T>? {
        return mClusterMarkerCache[marker]
    }

    /**
     * Creates markerWithPosition(s) for a particular cluster, animating it if necessary.
     */
    private inner class CreateMarkerTask(
        private val cluster: Cluster<T>,
        private val newMarkers: MutableSet<MarkerWithPosition>,
        private val animateFrom: LatLng?
    ) {

        fun perform(markerModifier: MarkerModifier) {
            // Don't show small clusters. Render the markers inside, instead.
            if (!shouldRenderAsCluster(cluster)) {
                for (item in cluster.items) {
                    var marker = mMarkerCache[item] as AdvancedMarker?
                    var markerWithPosition: MarkerWithPosition
                    if (marker == null) {
                        val advancedMarkerOptions = AdvancedMarkerOptions()
                        if (animateFrom != null) {
                            advancedMarkerOptions.position(animateFrom)
                        } else {
                            advancedMarkerOptions.position(item.position)
                            if (item.zIndex != null) {
                                advancedMarkerOptions.zIndex(item.zIndex!!)
                            }
                        }
                        onBeforeClusterItemRendered(item, advancedMarkerOptions)
                        marker = mClusterManager.markerCollection.addMarker(advancedMarkerOptions) as AdvancedMarker?
                        markerWithPosition = MarkerWithPosition(marker!!)
                        mMarkerCache.put(item, marker!!)
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

            var marker = mClusterMarkerCache[cluster] as AdvancedMarker?
            var markerWithPosition: MarkerWithPosition
            if (marker == null) {
                val advancedMarkerOptions = AdvancedMarkerOptions().position(if (animateFrom == null) cluster.position else animateFrom)
                onBeforeClusterRendered(cluster, advancedMarkerOptions)
                val `object` = mClusterManager.clusterMarkerCollection.addMarker(advancedMarkerOptions)
                marker = `object` as AdvancedMarker?
                mClusterMarkerCache.put(cluster, marker!!)
                markerWithPosition = MarkerWithPosition(marker)
                if (animateFrom != null) {
                    markerModifier.animate(markerWithPosition, animateFrom, cluster.position)
                }
            } else {
                markerWithPosition = MarkerWithPosition(marker)
                onClusterUpdated(cluster, marker)
            }
            onClusterRendered(cluster, marker!!)
            newMarkers.add(markerWithPosition)
        }
    }

    /**
     * A Marker and its position. [Marker.getPosition] must be called from the UI thread, so this
     * object allows lookup from other threads.
     */
    private class MarkerWithPosition(val marker: Marker) {
        var position: LatLng = marker.position

        override fun equals(other: Any?): Boolean {
            return if (other is MarkerWithPosition) {
                marker == other.marker
            } else false
        }

        override fun hashCode(): Int {
            return marker.hashCode()
        }
    }

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
        private var mRemoveOnComplete: Boolean = false
        private var mMarkerManager: MarkerManager? = null

        fun perform() {
            val valueAnimator = ValueAnimator.ofFloat(0.0f, 1.0f)
            valueAnimator.interpolator = ANIMATION_INTERP
            valueAnimator.duration = mAnimationDurationMs
            valueAnimator.addUpdateListener(this)
            valueAnimator.addListener(this)
            valueAnimator.start()
        }

        override fun onAnimationEnd(animation: Animator) {
            if (mRemoveOnComplete) {
                mMarkerCache.remove(marker)
                mClusterMarkerCache.remove(marker)
                mMarkerManager!!.remove(marker)
            }
            markerWithPosition.position = to
        }

        fun removeOnAnimationComplete(markerManager: MarkerManager) {
            mMarkerManager = markerManager
            mRemoveOnComplete = true
        }

        override fun onAnimationUpdate(valueAnimator: ValueAnimator) {
            val fraction = valueAnimator.animatedFraction
            val lat = (to.latitude - from.latitude) * fraction + from.latitude
            var lngDelta = to.longitude - from.longitude

            // Take the shortest path across the 180th meridian.
            if (abs(lngDelta) > 180) {
                lngDelta -= sign(lngDelta) * 360
            }
            val lng = lngDelta * fraction + from.longitude
            val position = LatLng(lat, lng)
            marker.position = position
        }
    }

    companion object {
        private val BUCKETS = intArrayOf(10, 20, 50, 100, 200, 500, 1000)
        private val ANIMATION_INTERP: TimeInterpolator = DecelerateInterpolator()
        private const val RUN_TASK = 0
        private const val TASK_FINISHED = 1
        private const val BLANK = 0

        private fun distanceSquared(a: Point, b: Point): Double {
            return (a.x - b.x) * (a.x - b.x) + (a.y - b.y) * (a.y - b.y)
        }
    }
}
