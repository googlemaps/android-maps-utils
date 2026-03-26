/*
 * Copyright 2026 Google LLC
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
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.AccelerateInterpolator
import android.view.animation.BounceInterpolator
import android.view.animation.DecelerateInterpolator
import android.view.animation.LinearInterpolator
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
import java.util.Collections
import java.util.LinkedList
import java.util.Queue
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executors
import java.util.concurrent.locks.Condition
import java.util.concurrent.locks.Lock
import java.util.concurrent.locks.ReentrantLock
import kotlin.math.abs
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.sign

/**
 * The default view for a ClusterManager. Markers are animated in and out of clusters.
 */
open class ClusterRendererMultipleItems<T : ClusterItem>(
    context: Context,
    private val mMap: GoogleMap,
    private val mClusterManager: ClusterManager<T>
) : ClusterRenderer<T> {

    private val mIconGenerator: IconGenerator = IconGenerator(context)
    private val mDensity: Float = context.resources.displayMetrics.density
    private var mAnimate: Boolean = true
    private var mAnimationDurationMs: Long = 300
    private val mExecutor = Executors.newSingleThreadExecutor()
    private val ongoingAnimations: Queue<AnimationTask> = LinkedList()
    
    private var mColoredCircleBackground: ShapeDrawable? = null
    
    // Default interpolator
    private var animationInterp: TimeInterpolator = DecelerateInterpolator()

    enum class AnimationType {
        LINEAR,
        EASE_IN,
        EASE_OUT,
        EASE_IN_OUT,
        FAST_OUT_SLOW_IN,
        BOUNCE,
        ACCELERATE,
        DECELERATE
    }

    fun setAnimationType(type: AnimationType) {
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
    fun setAnimationInterpolator(interpolator: TimeInterpolator) {
        animationInterp = interpolator
    }

    /**
     * Markers that are currently on the map.
     */
    private var mMarkers: MutableSet<MarkerWithPosition<T>> = Collections.newSetFromMap(ConcurrentHashMap())

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
    var minClusterSize: Int = 2

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

    private val mViewModifier = ViewModifier(Looper.getMainLooper())

    private var mClickListener: ClusterManager.OnClusterClickListener<T>? = null
    private var mInfoWindowClickListener: ClusterManager.OnClusterInfoWindowClickListener<T>? = null
    private var mInfoWindowLongClickListener: ClusterManager.OnClusterInfoWindowLongClickListener<T>? = null
    private var mItemClickListener: ClusterManager.OnClusterItemClickListener<T>? = null
    private var mItemInfoWindowClickListener: ClusterManager.OnClusterItemInfoWindowClickListener<T>? = null
    private var mItemInfoWindowLongClickListener: ClusterManager.OnClusterItemInfoWindowLongClickListener<T>? = null

    init {
        mIconGenerator.setContentView(makeSquareTextView(context))
        mIconGenerator.setTextAppearance(R.style.amu_ClusterIcon_TextAppearance)
        mIconGenerator.setBackground(makeClusterBackground())
    }

    override fun onAdd() {
        RendererLogger.d("ClusterRenderer", "Setting up MarkerCollection listeners")

        mClusterManager.markerCollection.setOnMarkerClickListener { marker ->
            RendererLogger.d("ClusterRenderer", "Marker clicked: $marker")
            mItemClickListener != null && mItemClickListener!!.onClusterItemClick(mMarkerCache[marker])
        }

        mClusterManager.markerCollection.setOnInfoWindowClickListener { marker ->
            RendererLogger.d("ClusterRenderer", "Info window clicked for marker: $marker")
            mItemInfoWindowClickListener?.onClusterItemInfoWindowClick(mMarkerCache[marker])
        }

        mClusterManager.markerCollection.setOnInfoWindowLongClickListener { marker ->
            RendererLogger.d("ClusterRenderer", "Info window long-clicked for marker: $marker")
            mItemInfoWindowLongClickListener?.onClusterItemInfoWindowLongClick(mMarkerCache[marker])
        }

        RendererLogger.d("ClusterRenderer", "Setting up ClusterMarkerCollection listeners")

        mClusterManager.clusterMarkerCollection.setOnMarkerClickListener { marker ->
            mClickListener != null && mClickListener!!.onClusterClick(mClusterMarkerCache[marker])
        }

        mClusterManager.clusterMarkerCollection.setOnInfoWindowClickListener { marker ->
            RendererLogger.d("ClusterRenderer", "Info window clicked for cluster marker: $marker")
            mInfoWindowClickListener?.onClusterInfoWindowClick(mClusterMarkerCache[marker])
        }

        mClusterManager.clusterMarkerCollection.setOnInfoWindowLongClickListener { marker ->
            RendererLogger.d("ClusterRenderer", "Info window long-clicked for cluster marker: $marker")
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
        return Color.HSVToColor(floatArrayOf(hue, 1f, .6f))
    }

    @StyleRes
    override fun getClusterTextAppearance(clusterSize: Int): Int {
        return R.style.amu_ClusterIcon_TextAppearance // Default value
    }

    /**
     * Enables or disables logging for the cluster renderer.
     *
     *
     * When enabled, the renderer will log internal operations such as cluster rendering,
     * marker updates, and other debug information. This is useful for development and debugging,
     * but should typically be disabled in production builds.
     *
     * @param enabled `true` to enable logging; `false` to disable it.
     */
    fun setLoggingEnabled(enabled: Boolean) {
        RendererLogger.setEnabled(enabled)
    }

    protected fun getClusterText(bucket: Int): String {
        return if (bucket < BUCKETS[0]) {
            bucket.toString()
        } else "$bucket+"
    }

    /**
     * Gets the "bucket" for a particular cluster. By default, uses the number of points within the
     * cluster, bucketed to some set points.
     */
    protected fun getBucket(cluster: Cluster<T>): Int {
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
    private inner class ViewModifier(looper: Looper) : Handler(looper) {
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
     * Transforms the current view (represented by DefaultClusterRenderer.mClusters and DefaultClusterRenderer.mZoom) to a
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

        fun setCallback(callback: Runnable?) {
            mCallback = callback
        }

        fun setProjection(projection: Projection?) {
            mProjection = projection
        }

        fun setMapZoom(zoom: Float) {
            mMapZoom = zoom
            mSphericalMercatorProjection = SphericalMercatorProjection(
                256 * 2.0.pow(min(zoom.toDouble(), mZoom.toDouble()))
            )
        }

        @SuppressLint("NewApi")
        override fun run() {
            val markerModifier = MarkerModifier()
            val zoom = mMapZoom
            val markersToRemove = mMarkers
            var visibleBounds: LatLngBounds

            try {
                visibleBounds = mProjection!!.visibleRegion.latLngBounds
                RendererLogger.d("ClusterRenderer", "Visible bounds calculated: $visibleBounds")
            } catch (e: Exception) {
                RendererLogger.e("ClusterRenderer", "Error getting visible bounds, defaulting to (0,0)")
                visibleBounds = LatLngBounds.builder().include(LatLng(0.0, 0.0)).build()
            }

            // Find all of the existing clusters that are on-screen. These are candidates for markers to animate from.
            var existingClustersOnScreen: MutableList<Point>? = null
            if (this@ClusterRendererMultipleItems.mClusters != null && mAnimate) {
                existingClustersOnScreen = ArrayList()
                for (c in this@ClusterRendererMultipleItems.mClusters!!) {
                    if (shouldRenderAsCluster(c) && visibleBounds.contains(c.position)) {
                        val point = mSphericalMercatorProjection!!.toPoint(c.position)
                        existingClustersOnScreen.add(point)
                    }
                }
                RendererLogger.d("ClusterRenderer", "Existing clusters on screen found: " + existingClustersOnScreen.size)
            }

            // Create the new markers and animate them to their new positions.
            val newMarkers: MutableSet<MarkerWithPosition<T>> = Collections.newSetFromMap(ConcurrentHashMap())
            for (c in clusters) {
                val onScreen = visibleBounds.contains(c.position)
                if (mAnimate) {
                    val point = mSphericalMercatorProjection!!.toPoint(c.position)
                    val closest = findClosestCluster(existingClustersOnScreen, point)
                    if (closest != null) {
                        val animateFrom = mSphericalMercatorProjection!!.toLatLng(closest)
                        markerModifier.add(true, CreateMarkerTask(c, newMarkers, animateFrom))
                        RendererLogger.d("ClusterRenderer", "Animating cluster from closest cluster: " + c.position)
                    } else {
                        markerModifier.add(true, CreateMarkerTask(c, newMarkers, null))
                        RendererLogger.d("ClusterRenderer", "Animating cluster without closest point: " + c.position)
                    }
                } else {
                    markerModifier.add(onScreen, CreateMarkerTask(c, newMarkers, null))
                    RendererLogger.d("ClusterRenderer", "Adding cluster without animation: " + c.position)
                }
            }

            // Wait for all markers to be added.
            markerModifier.waitUntilFree()
            RendererLogger.d("ClusterRenderer", "All new markers added, count: " + newMarkers.size)

            // Don't remove any markers that were just added. This is basically anything that had a hit in the MarkerCache.
            markersToRemove.removeAll(newMarkers)
            RendererLogger.d("ClusterRenderer", "Markers to remove after filtering new markers: " + markersToRemove.size)

            // Find all of the new clusters that were added on-screen. These are candidates for markers to animate from.
            var newClustersOnScreen: MutableList<Point>? = null
            if (mAnimate) {
                newClustersOnScreen = ArrayList()
                for (c in clusters) {
                    if (shouldRenderAsCluster(c) && visibleBounds.contains(c.position)) {
                        val p = mSphericalMercatorProjection!!.toPoint(c.position)
                        newClustersOnScreen.add(p)
                    }
                }
                RendererLogger.d("ClusterRenderer", "New clusters on screen found: " + newClustersOnScreen.size)
            }


            for (marker in markersToRemove) {
                val onScreen = marker.position?.let { visibleBounds.contains(it) } ?: false

                if (onScreen && mAnimate) {
                    val point = mSphericalMercatorProjection!!.toPoint(marker.position!!)
                    val closest = findClosestCluster(newClustersOnScreen, point)
                    if (closest != null) {
                        val animateTo = mSphericalMercatorProjection!!.toLatLng(closest)
                        markerModifier.animateThenRemove(marker, marker.position!!, animateTo!!)
                        RendererLogger.d("ClusterRenderer", "Animating then removing marker at position: " + marker.position)
                    } else if (mClusterMarkerCache.mCache.keys.iterator().hasNext() && mClusterMarkerCache.mCache.keys.iterator().next().items.contains(marker.clusterItem)) {
                        var foundItem: T? = null
                        for (cluster in mClusterMarkerCache.mCache.keys) {
                            for (clusterItem in cluster.items) {
                                if (clusterItem == marker.clusterItem) {
                                    foundItem = clusterItem
                                    break
                                }
                            }
                        }
                        // Remove it because it will join a cluster
                        markerModifier.animateThenRemove(marker, marker.position!!, foundItem!!.position)
                        RendererLogger.d("ClusterRenderer", "Animating then removing marker joining cluster at position: " + marker.position)
                    } else {
                        markerModifier.remove(true, marker.marker)
                        RendererLogger.d("ClusterRenderer", "Removing marker without animation at position: " + marker.position)
                    }
                } else {
                    markerModifier.remove(onScreen, marker.marker)
                    RendererLogger.d("ClusterRenderer", "Removing marker (onScreen=" + onScreen + ") at position: " + marker.position)
                }
            }

            // Wait until all marker removal operations are completed.
            markerModifier.waitUntilFree()
            RendererLogger.d("ClusterRenderer", "All marker removal operations completed.")

            mMarkers = newMarkers
            this@ClusterRendererMultipleItems.mClusters = clusters
            mZoom = zoom

            // Run the callback once everything is done.
            mCallback?.run()
            RendererLogger.d("ClusterRenderer", "Cluster update callback executed.")
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

    fun stopAnimation() {
        for (animation in ongoingAnimations) {
            animation.cancel()
        }
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

        private fun withLock(runnable: Runnable) {
            lock.lock()
            try {
                runnable.run()
            } finally {
                lock.unlock()
            }
        }

        /**
         * Creates markers for a cluster some time in the future.
         *
         * @param priority whether this operation should have priority.
         */
        fun add(priority: Boolean, c: CreateMarkerTask) {
            withLock {
                sendEmptyMessage(BLANK)
                if (priority) {
                    mOnScreenCreateMarkerTasks.add(c)
                } else {
                    mCreateMarkerTasks.add(c)
                }
            }
        }

        /**
         * Removes a markerWithPosition some time in the future.
         *
         * @param priority whether this operation should have priority.
         * @param m        the markerWithPosition to remove.
         */
        fun remove(priority: Boolean, m: Marker) {
            withLock {
                sendEmptyMessage(BLANK)
                if (priority) {
                    mOnScreenRemoveMarkerTasks.add(m)
                } else {
                    mRemoveMarkerTasks.add(m)
                }
            }
        }

        /**
         * Animates a markerWithPosition some time in the future.
         *
         * @param marker the markerWithPosition to animate.
         * @param from   the position to animate from.
         * @param to     the position to animate to.
         */
        fun animate(marker: MarkerWithPosition<T>, from: LatLng, to: LatLng) {
            withLock {
                val task = AnimationTask(marker, from, to, lock)

                for (existingTask in ongoingAnimations) {
                    if (existingTask.marker.id == task.marker.id) {
                        existingTask.cancel()
                        break
                    }
                }

                mAnimationTasks.add(task)
                ongoingAnimations.add(task)
            }
        }

        /**
         * Animates a markerWithPosition some time in the future, and removes it when the animation
         * is complete.
         *
         * @param marker the markerWithPosition to animate.
         * @param from   the position to animate from.
         * @param to     the position to animate to.
         */
        fun animateThenRemove(marker: MarkerWithPosition<T>, from: LatLng, to: LatLng) {
            withLock {
                val animationTask = AnimationTask(marker, from, to, lock)
                for (existingTask in ongoingAnimations) {
                    if (existingTask.marker.id == animationTask.marker.id) {
                        existingTask.cancel()
                        break
                    }
                }

                ongoingAnimations.add(animationTask)
                animationTask.removeOnAnimationComplete(mClusterManager.markerManager)
                mAnimationTasks.add(animationTask)
            }
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
                mAnimationTasks.poll()?.perform()
            } else if (!mOnScreenCreateMarkerTasks.isEmpty()) {
                mOnScreenCreateMarkerTasks.poll()?.perform(this)
            } else if (!mCreateMarkerTasks.isEmpty()) {
                mCreateMarkerTasks.poll()?.perform(this)
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
                lock.lock()
                try {
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
        val mCache: MutableMap<T, Marker> = HashMap()
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
     * @param item          item to be rendered
     * @param markerOptions the markerOptions representing the provided item
     */
    protected open fun onBeforeClusterItemRendered(item: T, markerOptions: MarkerOptions) {
        if (item.title != null && item.snippet != null) {
            markerOptions.title(item.title)
            markerOptions.snippet(item.snippet)
        } else if (item.title != null) {
            markerOptions.title(item.title)
        } else if (item.snippet != null) {
            markerOptions.title(item.snippet)
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
     * @param markerOptions markerOptions representing the provided cluster
     */
    protected open fun onBeforeClusterRendered(cluster: Cluster<T>, markerOptions: MarkerOptions) {
        // TODO: consider adding anchor(.5, .5) (Individual markers will overlap more often)
        markerOptions.icon(getDescriptorForCluster(cluster))
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
    protected fun getDescriptorForCluster(cluster: Cluster<T>): BitmapDescriptor {
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
    protected fun onClusterRendered(cluster: Cluster<T>, marker: Marker) {}

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
    protected open fun onClusterUpdated(cluster: Cluster<T>, marker: Marker) {
        // TODO: consider adding anchor(.5, .5) (Individual markers will overlap more often)
        marker.setIcon(getDescriptorForCluster(cluster))
    }

    /**
     * Called after the marker for a ClusterItem has been added to the map.
     *
     * @param clusterItem the item that was just added to the map
     * @param marker      the marker representing the item that was just added to the map
     */
    protected fun onClusterItemRendered(clusterItem: T, marker: Marker) {}

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
        private val newMarkers: MutableSet<MarkerWithPosition<T>>,
        private val animateFrom: LatLng?
    ) {

        fun perform(markerModifier: MarkerModifier) {
            // Don't show small clusters. Render the markers inside, instead.
            if (!shouldRenderAsCluster(cluster)) {
                RendererLogger.d("ClusterRenderer", "Rendering individual cluster items, count: " + cluster.items.size)
                for (item in cluster.items) {
                    var marker = mMarkerCache[item]
                    val markerWithPosition: MarkerWithPosition<T>
                    var currentLocation: LatLng? = item.position
                    if (marker == null) {
                        RendererLogger.d("ClusterRenderer", "Creating new marker for cluster item at position: $currentLocation")
                        val markerOptions = MarkerOptions()
                        if (animateFrom != null) {
                            RendererLogger.d("ClusterRenderer", "Animating from position: $animateFrom")
                            markerOptions.position(animateFrom)
                        } else if (mClusterMarkerCache.mCache.keys.iterator().hasNext() && mClusterMarkerCache.mCache.keys.iterator().next().items.contains(item)) {
                            var foundItem: T? = null
                            for (cluster in mClusterMarkerCache.mCache.keys) {
                                for (clusterItem in cluster.items) {
                                    if (clusterItem == item) {
                                        foundItem = clusterItem
                                        break
                                    }
                                }
                            }
                            currentLocation = foundItem?.position
                            RendererLogger.d("ClusterRenderer", "Found item in cache for animation at position: $currentLocation")
                            markerOptions.position(currentLocation!!)
                        } else {
                            markerOptions.position(item.position)
                            if (item.zIndex != null) {
                                markerOptions.zIndex(item.zIndex!!)
                            }
                        }
                        onBeforeClusterItemRendered(item, markerOptions)
                        marker = mClusterManager.markerCollection.addMarker(markerOptions)
                        markerWithPosition = MarkerWithPosition(marker, item)
                        mMarkerCache.put(item, marker)
                        if (animateFrom != null) {
                            markerModifier.animate(markerWithPosition, animateFrom, item.position)
                            RendererLogger.d("ClusterRenderer", "Animating marker from " + animateFrom + " to " + item.position)
                        } else if (currentLocation != null) {
                            markerModifier.animate(markerWithPosition, currentLocation, item.position)
                            RendererLogger.d("ClusterRenderer", "Animating marker from " + currentLocation + " to " + item.position)
                        }
                    } else {
                        markerWithPosition = MarkerWithPosition(marker, item)
                        markerModifier.animate(markerWithPosition, marker.position, item.position)
                        RendererLogger.d("ClusterRenderer", "Animating existing marker from " + marker.position + " to " + item.position)
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
            RendererLogger.d("ClusterRenderer", "Rendering cluster marker at position: " + cluster.position)
            var marker = mClusterMarkerCache[cluster]
            val markerWithPosition: MarkerWithPosition<T>
            if (marker == null) {
                RendererLogger.d("ClusterRenderer", "Creating new cluster marker")
                val markerOptions = MarkerOptions().position(if (animateFrom == null) cluster.position else animateFrom)
                onBeforeClusterRendered(cluster, markerOptions)
                marker = mClusterManager.clusterMarkerCollection.addMarker(markerOptions)
                mClusterMarkerCache.put(cluster, marker)
                markerWithPosition = MarkerWithPosition(marker, null)
                if (animateFrom != null) {
                    markerModifier.animate(markerWithPosition, animateFrom, cluster.position)
                    RendererLogger.d("ClusterRenderer", "Animating cluster marker from " + animateFrom + " to " + cluster.position)
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
    private class MarkerWithPosition<T>(val marker: Marker, val clusterItem: T?) {
        var position: LatLng? = marker.position

        override fun equals(other: Any?): Boolean {
            return if (other is MarkerWithPosition<*>) {
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
        val markerWithPosition: MarkerWithPosition<T>,
        val from: LatLng,
        val to: LatLng,
        val lock: Lock
    ) : AnimatorListenerAdapter(), ValueAnimator.AnimatorUpdateListener {
        val marker: Marker = markerWithPosition.marker
        private var mRemoveOnComplete: Boolean = false
        private var mMarkerManager: MarkerManager? = null
        private var valueAnimator: ValueAnimator? = null

        fun perform() {
            valueAnimator = ValueAnimator.ofFloat(0.0f, 1.0f)
            valueAnimator!!.interpolator = animationInterp
            valueAnimator!!.duration = mAnimationDurationMs
            valueAnimator!!.addUpdateListener(this)
            valueAnimator!!.addListener(this)
            valueAnimator!!.start()
        }

        fun cancel() {
            if (Looper.myLooper() != Looper.getMainLooper()) {
                Handler(Looper.getMainLooper()).post { cancel() }
                return
            }
            lock.lock()
            try {
                markerWithPosition.position = to
                mRemoveOnComplete = false
                valueAnimator!!.cancel()
                ongoingAnimations.remove(this)
            } finally {
                lock.unlock()
            }
        }

        override fun onAnimationEnd(animation: Animator) {
            if (mRemoveOnComplete) {
                mMarkerCache.remove(marker)
                mClusterMarkerCache.remove(marker)
                mMarkerManager!!.remove(marker)
            }
            markerWithPosition.position = to

            // Remove the task from the queue
            lock.lock()
            try {
                ongoingAnimations.remove(this)
            } finally {
                lock.unlock()
            }
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
            markerWithPosition.position = position
        }
    }

    companion object {
        private val BUCKETS = intArrayOf(10, 20, 50, 100, 200, 500, 1000)
        private const val RUN_TASK = 0
        private const val TASK_FINISHED = 1
        private const val BLANK = 0

        private fun distanceSquared(a: Point, b: Point): Double {
            return (a.x - b.x) * (a.x - b.x) + (a.y - b.y) * (a.y - b.y)
        }
    }
}
