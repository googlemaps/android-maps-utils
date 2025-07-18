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

package com.google.maps.android.clustering.view;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.TimeInterpolator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.MessageQueue;
import android.util.SparseArray;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.BounceInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.LinearInterpolator;

import androidx.annotation.NonNull;
import androidx.annotation.StyleRes;
import androidx.interpolator.view.animation.FastOutSlowInInterpolator;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.Projection;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.R;
import com.google.maps.android.RendererLogger;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterItem;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.collections.MarkerManager;
import com.google.maps.android.geometry.Point;
import com.google.maps.android.projection.SphericalMercatorProjection;
import com.google.maps.android.ui.IconGenerator;
import com.google.maps.android.ui.SquareTextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * The default view for a ClusterManager. Markers are animated in and out of clusters.
 */
public class ClusterRendererMultipleItems<T extends ClusterItem> implements ClusterRenderer<T> {
    private final GoogleMap mMap;
    private final IconGenerator mIconGenerator;
    private final ClusterManager<T> mClusterManager;
    private final float mDensity;
    private boolean mAnimate;
    private long mAnimationDurationMs;
    private final Executor mExecutor = Executors.newSingleThreadExecutor();
    private final Queue<AnimationTask> ongoingAnimations = new LinkedList<>();
    private static TimeInterpolator animationInterp = new DecelerateInterpolator();

    private static final int[] BUCKETS = {10, 20, 50, 100, 200, 500, 1000};
    private ShapeDrawable mColoredCircleBackground;

    public enum AnimationType {
        LINEAR,
        EASE_IN,
        EASE_OUT,
        EASE_IN_OUT,
        FAST_OUT_SLOW_IN,
        BOUNCE,
        ACCELERATE,
        DECELERATE
    }

    public void setAnimationType(AnimationType type) {
        switch (type) {
            case EASE_IN, ACCELERATE:
                animationInterp = new AccelerateInterpolator();
                break;
            case EASE_OUT:
                animationInterp = new DecelerateInterpolator();
                break;
            case EASE_IN_OUT:
                animationInterp = new AccelerateDecelerateInterpolator();
                break;
            case FAST_OUT_SLOW_IN:
                animationInterp = new FastOutSlowInInterpolator();
                break;
            case BOUNCE:
                animationInterp = new BounceInterpolator();
                break;
            case DECELERATE:
                animationInterp = new DecelerateInterpolator();
                break;
            default:
                animationInterp = new LinearInterpolator();
                break;
        }
    }


    /**
     * Markers that are currently on the map.
     */
    private Set<MarkerWithPosition> mMarkers = Collections.newSetFromMap(new ConcurrentHashMap<>());

    /**
     * Icons for each bucket.
     */
    private final SparseArray<BitmapDescriptor> mIcons = new SparseArray<>();

    /**
     * Markers for single ClusterItems.
     */
    private final MarkerCache<T> mMarkerCache = new MarkerCache<>();

    /**
     * If cluster size is less than this size, display individual markers.
     */
    private int mMinClusterSize = 2;

    /**
     * The currently displayed set of clusters.
     */
    private Set<? extends Cluster<T>> mClusters;

    /**
     * Markers for Clusters.
     */
    private final MarkerCache<Cluster<T>> mClusterMarkerCache = new MarkerCache<>();

    /**
     * The target zoom level for the current set of clusters.
     */
    private float mZoom;

    private final ViewModifier mViewModifier = new ViewModifier(Looper.getMainLooper());

    private ClusterManager.OnClusterClickListener<T> mClickListener;
    private ClusterManager.OnClusterInfoWindowClickListener<T> mInfoWindowClickListener;
    private ClusterManager.OnClusterInfoWindowLongClickListener<T> mInfoWindowLongClickListener;
    private ClusterManager.OnClusterItemClickListener<T> mItemClickListener;
    private ClusterManager.OnClusterItemInfoWindowClickListener<T> mItemInfoWindowClickListener;
    private ClusterManager.OnClusterItemInfoWindowLongClickListener<T> mItemInfoWindowLongClickListener;

    public ClusterRendererMultipleItems(Context context, GoogleMap map, ClusterManager<T> clusterManager) {
        mMap = map;
        mAnimate = true;
        mAnimationDurationMs = 300;
        mDensity = context.getResources().getDisplayMetrics().density;
        mIconGenerator = new IconGenerator(context);
        mIconGenerator.setContentView(makeSquareTextView(context));
        mIconGenerator.setTextAppearance(R.style.amu_ClusterIcon_TextAppearance);
        mIconGenerator.setBackground(makeClusterBackground());
        mClusterManager = clusterManager;
    }

    @Override
    public void onAdd() {
        RendererLogger.d("ClusterRenderer", "Setting up MarkerCollection listeners");

        mClusterManager.getMarkerCollection().setOnMarkerClickListener(marker -> {
            RendererLogger.d("ClusterRenderer", "Marker clicked: " + marker);
            return mItemClickListener != null && mItemClickListener.onClusterItemClick(mMarkerCache.get(marker));
        });

        mClusterManager.getMarkerCollection().setOnInfoWindowClickListener(marker -> {
            RendererLogger.d("ClusterRenderer", "Info window clicked for marker: " + marker);
            if (mItemInfoWindowClickListener != null) {
                mItemInfoWindowClickListener.onClusterItemInfoWindowClick(mMarkerCache.get(marker));
            }
        });

        mClusterManager.getMarkerCollection().setOnInfoWindowLongClickListener(marker -> {
            RendererLogger.d("ClusterRenderer", "Info window long-clicked for marker: " + marker);
            if (mItemInfoWindowLongClickListener != null) {
                mItemInfoWindowLongClickListener.onClusterItemInfoWindowLongClick(mMarkerCache.get(marker));
            }
        });

        RendererLogger.d("ClusterRenderer", "Setting up ClusterMarkerCollection listeners");

        mClusterManager.getClusterMarkerCollection().setOnMarkerClickListener(marker -> mClickListener != null && mClickListener.onClusterClick(mClusterMarkerCache.get(marker)));

        mClusterManager.getClusterMarkerCollection().setOnInfoWindowClickListener(marker -> {
            RendererLogger.d("ClusterRenderer", "Info window clicked for cluster marker: " + marker);
            if (mInfoWindowClickListener != null) {
                mInfoWindowClickListener.onClusterInfoWindowClick(mClusterMarkerCache.get(marker));
            }
        });

        mClusterManager.getClusterMarkerCollection().setOnInfoWindowLongClickListener(marker -> {
            RendererLogger.d("ClusterRenderer", "Info window long-clicked for cluster marker: " + marker);
            if (mInfoWindowLongClickListener != null) {
                mInfoWindowLongClickListener.onClusterInfoWindowLongClick(mClusterMarkerCache.get(marker));
            }
        });
    }


    @Override
    public void onRemove() {
        mClusterManager.getMarkerCollection().setOnMarkerClickListener(null);
        mClusterManager.getMarkerCollection().setOnInfoWindowClickListener(null);
        mClusterManager.getMarkerCollection().setOnInfoWindowLongClickListener(null);
        mClusterManager.getClusterMarkerCollection().setOnMarkerClickListener(null);
        mClusterManager.getClusterMarkerCollection().setOnInfoWindowClickListener(null);
        mClusterManager.getClusterMarkerCollection().setOnInfoWindowLongClickListener(null);
    }

    private LayerDrawable makeClusterBackground() {
        mColoredCircleBackground = new ShapeDrawable(new OvalShape());
        ShapeDrawable outline = new ShapeDrawable(new OvalShape());
        outline.getPaint().setColor(0x80ffffff); // Transparent white.
        LayerDrawable background = new LayerDrawable(new Drawable[]{outline, mColoredCircleBackground});
        int strokeWidth = (int) (mDensity * 3);
        background.setLayerInset(1, strokeWidth, strokeWidth, strokeWidth, strokeWidth);
        return background;
    }

    private SquareTextView makeSquareTextView(Context context) {
        SquareTextView squareTextView = new SquareTextView(context);
        ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        squareTextView.setLayoutParams(layoutParams);
        squareTextView.setId(R.id.amu_text);
        int twelveDpi = (int) (12 * mDensity);
        squareTextView.setPadding(twelveDpi, twelveDpi, twelveDpi, twelveDpi);
        return squareTextView;
    }

    @Override
    public int getColor(int clusterSize) {
        final float hueRange = 220;
        final float sizeRange = 300;
        final float size = Math.min(clusterSize, sizeRange);
        final float hue = (sizeRange - size) * (sizeRange - size) / (sizeRange * sizeRange) * hueRange;
        return Color.HSVToColor(new float[]{hue, 1f, .6f});
    }

    @StyleRes
    @Override
    public int getClusterTextAppearance(int clusterSize) {
        return R.style.amu_ClusterIcon_TextAppearance; // Default value
    }

    /**
     * Enables or disables logging for the cluster renderer.
     *
     * <p>When enabled, the renderer will log internal operations such as cluster rendering,
     * marker updates, and other debug information. This is useful for development and debugging,
     * but should typically be disabled in production builds.</p>
     *
     * @param enabled {@code true} to enable logging; {@code false} to disable it.
     */
    public void setLoggingEnabled(boolean enabled) {
        RendererLogger.setEnabled(enabled);
    }

    @NonNull
    protected String getClusterText(int bucket) {
        if (bucket < BUCKETS[0]) {
            return String.valueOf(bucket);
        }
        return bucket + "+";
    }

    /**
     * Gets the "bucket" for a particular cluster. By default, uses the number of points within the
     * cluster, bucketed to some set points.
     */
    protected int getBucket(@NonNull Cluster<T> cluster) {
        int size = cluster.getSize();
        if (size <= BUCKETS[0]) {
            return size;
        }
        for (int i = 0; i < BUCKETS.length - 1; i++) {
            if (size < BUCKETS[i + 1]) {
                return BUCKETS[i];
            }
        }
        return BUCKETS[BUCKETS.length - 1];
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
    public int getMinClusterSize() {
        return mMinClusterSize;
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
    public void setMinClusterSize(int minClusterSize) {
        mMinClusterSize = minClusterSize;
    }

    /**
     * ViewModifier ensures only one re-rendering of the view occurs at a time, and schedules
     * re-rendering, which is performed by the RenderTask.
     */
    @SuppressLint("HandlerLeak")
    private class ViewModifier extends Handler {
        public ViewModifier(Looper looper) {
            super(looper);
        }

        private static final int RUN_TASK = 0;
        private static final int TASK_FINISHED = 1;
        private boolean mViewModificationInProgress = false;
        private RenderTask mNextClusters = null;

        @Override
        public void handleMessage(Message msg) {
            if (msg.what == TASK_FINISHED) {
                mViewModificationInProgress = false;
                if (mNextClusters != null) {
                    // Run the task that was queued up.
                    sendEmptyMessage(RUN_TASK);
                }
                return;
            }
            removeMessages(RUN_TASK);

            if (mViewModificationInProgress) {
                // Busy - wait for the callback.
                return;
            }

            if (mNextClusters == null) {
                // Nothing to do.
                return;
            }
            Projection projection = mMap.getProjection();

            RenderTask renderTask;
            synchronized (this) {
                renderTask = mNextClusters;
                mNextClusters = null;
                mViewModificationInProgress = true;
            }

            renderTask.setCallback(() -> sendEmptyMessage(TASK_FINISHED));
            renderTask.setProjection(projection);
            renderTask.setMapZoom(mMap.getCameraPosition().zoom);
            mExecutor.execute(renderTask);
        }

        public void queue(Set<? extends Cluster<T>> clusters) {
            synchronized (this) {
                // Overwrite any pending cluster tasks - we don't care about intermediate states.
                mNextClusters = new RenderTask(clusters);
            }
            sendEmptyMessage(RUN_TASK);
        }
    }

    /**
     * Determine whether the cluster should be rendered as individual markers or a cluster.
     *
     * @param cluster cluster to examine for rendering
     * @return true if the provided cluster should be rendered as a single marker on the map, false
     * if the items within this cluster should be rendered as individual markers instead.
     */
    protected boolean shouldRenderAsCluster(@NonNull Cluster<T> cluster) {
        return cluster.getSize() >= mMinClusterSize;
    }

    /**
     * Transforms the current view (represented by DefaultClusterRenderer.mClusters and DefaultClusterRenderer.mZoom) to a
     * new zoom level and set of clusters.
     * <p/>
     * This must be run off the UI thread. Work is coordinated in the RenderTask, then queued up to
     * be executed by a MarkerModifier.
     * <p/>
     * There are three stages for the render:
     * <p/>
     * 1. Markers are added to the map
     * <p/>
     * 2. Markers are animated to their final position
     * <p/>
     * 3. Any old markers are removed from the map
     * <p/>
     * When zooming in, markers are animated out from the nearest existing cluster. When zooming
     * out, existing clusters are animated to the nearest new cluster.
     */
    private class RenderTask implements Runnable {

        final Set<? extends Cluster<T>> clusters;

        private Runnable mCallback;

        private Projection mProjection;

        private SphericalMercatorProjection mSphericalMercatorProjection;
        private float mMapZoom;

        private RenderTask(Set<? extends Cluster<T>> clusters) {
            this.clusters = clusters;
        }

        public void setCallback(Runnable callback) {
            mCallback = callback;
        }

        public void setProjection(Projection projection) {
            this.mProjection = projection;
        }

        public void setMapZoom(float zoom) {
            this.mMapZoom = zoom;
            this.mSphericalMercatorProjection = new SphericalMercatorProjection(256 * Math.pow(2, Math.min(zoom, mZoom)));
        }

        @SuppressLint("NewApi")
        public void run() {
            final MarkerModifier markerModifier = new MarkerModifier();
            final float zoom = mMapZoom;
            final Set<MarkerWithPosition> markersToRemove = mMarkers;
            LatLngBounds visibleBounds;

            try {
                visibleBounds = mProjection.getVisibleRegion().latLngBounds;
                RendererLogger.d("ClusterRenderer", "Visible bounds calculated: " + visibleBounds);
            } catch (Exception e) {
                RendererLogger.e("ClusterRenderer", "Error getting visible bounds, defaulting to (0,0)");
                visibleBounds = LatLngBounds.builder().include(new LatLng(0, 0)).build();
            }

            // Find all of the existing clusters that are on-screen. These are candidates for markers to animate from.
            List<Point> existingClustersOnScreen = null;
            if (ClusterRendererMultipleItems.this.mClusters != null && mAnimate) {
                existingClustersOnScreen = new ArrayList<>();
                for (Cluster<T> c : ClusterRendererMultipleItems.this.mClusters) {
                    if (shouldRenderAsCluster(c) && visibleBounds.contains(c.getPosition())) {
                        Point point = mSphericalMercatorProjection.toPoint(c.getPosition());
                        existingClustersOnScreen.add(point);
                    }
                }
                RendererLogger.d("ClusterRenderer", "Existing clusters on screen found: " + existingClustersOnScreen.size());
            }

            // Create the new markers and animate them to their new positions.
            final Set<MarkerWithPosition> newMarkers = Collections.newSetFromMap(new ConcurrentHashMap<>());
            for (Cluster<T> c : clusters) {
                boolean onScreen = visibleBounds.contains(c.getPosition());
                if (mAnimate) {
                    Point point = mSphericalMercatorProjection.toPoint(c.getPosition());
                    Point closest = findClosestCluster(existingClustersOnScreen, point);
                    if (closest != null) {
                        LatLng animateFrom = mSphericalMercatorProjection.toLatLng(closest);
                        markerModifier.add(true, new CreateMarkerTask(c, newMarkers, animateFrom));
                        RendererLogger.d("ClusterRenderer", "Animating cluster from closest cluster: " + c.getPosition());
                    } else {
                        markerModifier.add(true, new CreateMarkerTask(c, newMarkers, null));
                        RendererLogger.d("ClusterRenderer", "Animating cluster without closest point: " + c.getPosition());
                    }

                } else {
                    markerModifier.add(onScreen, new CreateMarkerTask(c, newMarkers, null));
                    RendererLogger.d("ClusterRenderer", "Adding cluster without animation: " + c.getPosition());
                }
            }

            // Wait for all markers to be added.
            markerModifier.waitUntilFree();
            RendererLogger.d("ClusterRenderer", "All new markers added, count: " + newMarkers.size());

            // Don't remove any markers that were just added. This is basically anything that had a hit in the MarkerCache.
            markersToRemove.removeAll(newMarkers);
            RendererLogger.d("ClusterRenderer", "Markers to remove after filtering new markers: " + markersToRemove.size());

            // Find all of the new clusters that were added on-screen. These are candidates for markers to animate from.
            List<Point> newClustersOnScreen = null;
            if (mAnimate) {
                newClustersOnScreen = new ArrayList<>();
                for (Cluster<T> c : clusters) {
                    if (shouldRenderAsCluster(c) && visibleBounds.contains(c.getPosition())) {
                        Point p = mSphericalMercatorProjection.toPoint(c.getPosition());
                        newClustersOnScreen.add(p);
                    }
                }
                RendererLogger.d("ClusterRenderer", "New clusters on screen found: " + newClustersOnScreen.size());
            }

            for (final MarkerWithPosition marker : markersToRemove) {
                boolean onScreen = visibleBounds.contains(marker.position);
                if (onScreen && mAnimate) {
                    final Point point = mSphericalMercatorProjection.toPoint(marker.position);
                    final Point closest = findClosestCluster(newClustersOnScreen, point);
                    if (closest != null) {
                        LatLng animateTo = mSphericalMercatorProjection.toLatLng(closest);
                        markerModifier.animateThenRemove(marker, marker.position, animateTo);
                        RendererLogger.d("ClusterRenderer", "Animating then removing marker at position: " + marker.position);
                    } else if (mClusterMarkerCache.mCache.keySet().iterator().hasNext() && mClusterMarkerCache.mCache.keySet().iterator().next().getItems().contains(marker.clusterItem)) {
                        T foundItem = null;
                        for (Cluster<T> cluster : mClusterMarkerCache.mCache.keySet()) {
                            for (T clusterItem : cluster.getItems()) {
                                if (clusterItem.equals(marker.clusterItem)) {
                                    foundItem = clusterItem;
                                    break;
                                }
                            }
                        }
                        // Remove it because it will join a cluster
                        markerModifier.animateThenRemove(marker, marker.position, foundItem.getPosition());
                        RendererLogger.d("ClusterRenderer", "Animating then removing marker joining cluster at position: " + marker.position);
                    } else {
                        markerModifier.remove(true, marker.marker);
                        RendererLogger.d("ClusterRenderer", "Removing marker without animation at position: " + marker.position);
                    }
                } else {
                    markerModifier.remove(onScreen, marker.marker);
                    RendererLogger.d("ClusterRenderer", "Removing marker (onScreen=" + onScreen + ") at position: " + marker.position);
                }
            }

            // Wait until all marker removal operations are completed.
            markerModifier.waitUntilFree();
            RendererLogger.d("ClusterRenderer", "All marker removal operations completed.");

            mMarkers = newMarkers;
            ClusterRendererMultipleItems.this.mClusters = clusters;
            mZoom = zoom;

            // Run the callback once everything is done.
            mCallback.run();
            RendererLogger.d("ClusterRenderer", "Cluster update callback executed.");
        }
    }


    @Override
    public void onClustersChanged(Set<? extends Cluster<T>> clusters) {
        mViewModifier.queue(clusters);
    }

    @Override
    public void setOnClusterClickListener(ClusterManager.OnClusterClickListener<T> listener) {
        mClickListener = listener;
    }

    @Override
    public void setOnClusterInfoWindowClickListener(ClusterManager.OnClusterInfoWindowClickListener<T> listener) {
        mInfoWindowClickListener = listener;
    }

    @Override
    public void setOnClusterInfoWindowLongClickListener(ClusterManager.OnClusterInfoWindowLongClickListener<T> listener) {
        mInfoWindowLongClickListener = listener;
    }

    @Override
    public void setOnClusterItemClickListener(ClusterManager.OnClusterItemClickListener<T> listener) {
        mItemClickListener = listener;
    }

    @Override
    public void setOnClusterItemInfoWindowClickListener(ClusterManager.OnClusterItemInfoWindowClickListener<T> listener) {
        mItemInfoWindowClickListener = listener;
    }

    @Override
    public void setOnClusterItemInfoWindowLongClickListener(ClusterManager.OnClusterItemInfoWindowLongClickListener<T> listener) {
        mItemInfoWindowLongClickListener = listener;
    }

    @Override
    public void setAnimation(boolean animate) {
        mAnimate = animate;
    }

    /**
     * {@inheritDoc} The default duration is 300 milliseconds.
     *
     * @param animationDurationMs long: The length of the animation, in milliseconds. This value cannot be negative.
     */
    @Override
    public void setAnimationDuration(long animationDurationMs) {
        mAnimationDurationMs = animationDurationMs;
    }

    public void stopAnimation() {
        for (AnimationTask animation : ongoingAnimations) {
            animation.cancel();
        }
    }

    private static double distanceSquared(Point a, Point b) {
        return (a.x - b.x) * (a.x - b.x) + (a.y - b.y) * (a.y - b.y);
    }

    private Point findClosestCluster(List<Point> markers, Point point) {
        if (markers == null || markers.isEmpty()) return null;

        int maxDistance = mClusterManager.getAlgorithm().getMaxDistanceBetweenClusteredItems();
        double minDistSquared = maxDistance * maxDistance;
        Point closest = null;
        for (Point candidate : markers) {
            double dist = distanceSquared(candidate, point);
            if (dist < minDistSquared) {
                closest = candidate;
                minDistSquared = dist;
            }
        }
        return closest;
    }

    /**
     * Handles all markerWithPosition manipulations on the map. Work (such as adding, removing, or
     * animating a markerWithPosition) is performed while trying not to block the rest of the app's
     * UI.
     */
    @SuppressLint("HandlerLeak")
    private class MarkerModifier extends Handler implements MessageQueue.IdleHandler {
        private static final int BLANK = 0;

        private final Lock lock = new ReentrantLock();
        private final Condition busyCondition = lock.newCondition();

        private final Queue<CreateMarkerTask> mCreateMarkerTasks = new LinkedList<>();
        private final Queue<CreateMarkerTask> mOnScreenCreateMarkerTasks = new LinkedList<>();
        private final Queue<Marker> mRemoveMarkerTasks = new LinkedList<>();
        private final Queue<Marker> mOnScreenRemoveMarkerTasks = new LinkedList<>();
        private final Queue<AnimationTask> mAnimationTasks = new LinkedList<>();


        /**
         * Whether the idle listener has been added to the UI thread's MessageQueue.
         */
        private boolean mListenerAdded;

        private MarkerModifier() {
            super(Looper.getMainLooper());
        }

        /**
         * Creates markers for a cluster some time in the future.
         *
         * @param priority whether this operation should have priority.
         */
        public void add(boolean priority, CreateMarkerTask c) {
            lock.lock();
            sendEmptyMessage(BLANK);
            if (priority) {
                mOnScreenCreateMarkerTasks.add(c);
            } else {
                mCreateMarkerTasks.add(c);
            }
            lock.unlock();
        }

        /**
         * Removes a markerWithPosition some time in the future.
         *
         * @param priority whether this operation should have priority.
         * @param m        the markerWithPosition to remove.
         */
        public void remove(boolean priority, Marker m) {
            lock.lock();
            sendEmptyMessage(BLANK);
            if (priority) {
                mOnScreenRemoveMarkerTasks.add(m);
            } else {
                mRemoveMarkerTasks.add(m);
            }
            lock.unlock();
        }

        /**
         * Animates a markerWithPosition some time in the future.
         *
         * @param marker the markerWithPosition to animate.
         * @param from   the position to animate from.
         * @param to     the position to animate to.
         */
        public void animate(MarkerWithPosition marker, LatLng from, LatLng to) {
            lock.lock();
            AnimationTask task = new AnimationTask(marker, from, to, lock);

            for (AnimationTask existingTask : ongoingAnimations) {
                if (existingTask.marker.getId().equals(task.marker.getId())) {
                    existingTask.cancel();
                    break;
                }
            }

            mAnimationTasks.add(task);
            ongoingAnimations.add(task);
            lock.unlock();
        }

        /**
         * Animates a markerWithPosition some time in the future, and removes it when the animation
         * is complete.
         *
         * @param marker the markerWithPosition to animate.
         * @param from   the position to animate from.
         * @param to     the position to animate to.
         */
        public void animateThenRemove(MarkerWithPosition marker, LatLng from, LatLng to) {
            lock.lock();
            AnimationTask animationTask = new AnimationTask(marker, from, to, lock);
            for (AnimationTask existingTask : ongoingAnimations) {
                if (existingTask.marker.getId().equals(animationTask.marker.getId())) {
                    existingTask.cancel();
                    break;
                }
            }

            ongoingAnimations.add(animationTask);
            animationTask.removeOnAnimationComplete(mClusterManager.getMarkerManager());
            mAnimationTasks.add(animationTask);
            lock.unlock();
        }

        @Override
        public void handleMessage(@NonNull Message msg) {
            if (!mListenerAdded) {
                Looper.myQueue().addIdleHandler(this);
                mListenerAdded = true;
            }
            removeMessages(BLANK);

            lock.lock();
            try {

                // Perform up to 10 tasks at once.
                // Consider only performing 10 remove tasks, not adds and animations.
                // Removes are relatively slow and are much better when batched.
                for (int i = 0; i < 10; i++) {
                    performNextTask();
                }

                if (!isBusy()) {
                    mListenerAdded = false;
                    Looper.myQueue().removeIdleHandler(this);
                    // Signal any other threads that are waiting.
                    busyCondition.signalAll();
                } else {
                    // Sometimes the idle queue may not be called - schedule up some work regardless
                    // of whether the UI thread is busy or not.
                    // TODO: try to remove this.
                    sendEmptyMessageDelayed(BLANK, 10);
                }
            } finally {
                lock.unlock();
            }
        }

        /**
         * Perform the next task. Prioritise any on-screen work.
         */
        private void performNextTask() {
            if (!mOnScreenRemoveMarkerTasks.isEmpty()) {
                removeMarker(mOnScreenRemoveMarkerTasks.poll());
            } else if (!mAnimationTasks.isEmpty()) {
                Objects.requireNonNull(mAnimationTasks.poll()).perform();
            } else if (!mOnScreenCreateMarkerTasks.isEmpty()) {
                Objects.requireNonNull(mOnScreenCreateMarkerTasks.poll()).perform(this);
            } else if (!mCreateMarkerTasks.isEmpty()) {
                Objects.requireNonNull(mCreateMarkerTasks.poll()).perform(this);
            } else if (!mRemoveMarkerTasks.isEmpty()) {
                removeMarker(mRemoveMarkerTasks.poll());
            }
        }

        private void removeMarker(Marker m) {
            mMarkerCache.remove(m);
            mClusterMarkerCache.remove(m);
            mClusterManager.getMarkerManager().remove(m);
        }

        /**
         * @return true if there is still work to be processed.
         */
        public boolean isBusy() {
            try {
                lock.lock();
                return !(mCreateMarkerTasks.isEmpty() && mOnScreenCreateMarkerTasks.isEmpty() && mOnScreenRemoveMarkerTasks.isEmpty() && mRemoveMarkerTasks.isEmpty() && mAnimationTasks.isEmpty());
            } finally {
                lock.unlock();
            }
        }

        /**
         * Blocks the calling thread until all work has been processed.
         */
        public void waitUntilFree() {
            while (isBusy()) {
                // Sometimes the idle queue may not be called - schedule up some work regardless
                // of whether the UI thread is busy or not.
                // TODO: try to remove this.
                sendEmptyMessage(BLANK);
                lock.lock();
                try {
                    if (isBusy()) {
                        busyCondition.await();
                    }
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                } finally {
                    lock.unlock();
                }
            }
        }

        @Override
        public boolean queueIdle() {
            // When the UI is not busy, schedule some work.
            sendEmptyMessage(BLANK);
            return true;
        }
    }

    /**
     * A cache of markers representing individual ClusterItems.
     */
    private static class MarkerCache<T> {
        private final Map<T, Marker> mCache = new HashMap<>();
        private final Map<Marker, T> mCacheReverse = new HashMap<>();

        public Marker get(T item) {
            return mCache.get(item);
        }

        public T get(Marker m) {
            return mCacheReverse.get(m);
        }

        public void put(T item, Marker m) {
            mCache.put(item, m);
            mCacheReverse.put(m, item);
        }

        public void remove(Marker m) {
            T item = mCacheReverse.get(m);
            mCacheReverse.remove(m);
            mCache.remove(item);
        }
    }

    /**
     * Called before the marker for a ClusterItem is added to the map. The default implementation
     * sets the marker and snippet text based on the respective item text if they are both
     * available, otherwise it will set the title if available, and if not it will set the marker
     * title to the item snippet text if that is available.
     * <p>
     * The first time {@link ClusterManager#cluster()} is invoked on a set of items
     * {@link #onBeforeClusterItemRendered(ClusterItem, MarkerOptions)} will be called and
     * {@link #onClusterItemUpdated(ClusterItem, Marker)} will not be called.
     * If an item is removed and re-added (or updated) and {@link ClusterManager#cluster()} is
     * invoked again, then {@link #onClusterItemUpdated(ClusterItem, Marker)} will be called and
     * {@link #onBeforeClusterItemRendered(ClusterItem, MarkerOptions)} will not be called.
     *
     * @param item          item to be rendered
     * @param markerOptions the markerOptions representing the provided item
     */
    protected void onBeforeClusterItemRendered(@NonNull T item, @NonNull MarkerOptions markerOptions) {
        if (item.getTitle() != null && item.getSnippet() != null) {
            markerOptions.title(item.getTitle());
            markerOptions.snippet(item.getSnippet());
        } else if (item.getTitle() != null) {
            markerOptions.title(item.getTitle());
        } else if (item.getSnippet() != null) {
            markerOptions.title(item.getSnippet());
        }
    }

    /**
     * Called when a cached marker for a ClusterItem already exists on the map so the marker may
     * be updated to the latest item values. Default implementation updates the title and snippet
     * of the marker if they have changed and refreshes the info window of the marker if it is open.
     * Note that the contents of the item may not have changed since the cached marker was created -
     * implementations of this method are responsible for checking if something changed (if that
     * matters to the implementation).
     * <p>
     * The first time {@link ClusterManager#cluster()} is invoked on a set of items
     * {@link #onBeforeClusterItemRendered(ClusterItem, MarkerOptions)} will be called and
     * {@link #onClusterItemUpdated(ClusterItem, Marker)} will not be called.
     * If an item is removed and re-added (or updated) and {@link ClusterManager#cluster()} is
     * invoked again, then {@link #onClusterItemUpdated(ClusterItem, Marker)} will be called and
     * {@link #onBeforeClusterItemRendered(ClusterItem, MarkerOptions)} will not be called.
     *
     * @param item   item being updated
     * @param marker cached marker that contains a potentially previous state of the item.
     */
    protected void onClusterItemUpdated(@NonNull T item, @NonNull Marker marker) {
        boolean changed = false;
        // Update marker text if the item text changed - same logic as adding marker in CreateMarkerTask.perform()
        if (item.getTitle() != null && item.getSnippet() != null) {
            if (!item.getTitle().equals(marker.getTitle())) {
                marker.setTitle(item.getTitle());
                changed = true;
            }
            if (!item.getSnippet().equals(marker.getSnippet())) {
                marker.setSnippet(item.getSnippet());
                changed = true;
            }
        } else if (item.getSnippet() != null && !item.getSnippet().equals(marker.getTitle())) {
            marker.setTitle(item.getSnippet());
            changed = true;
        } else if (item.getTitle() != null && !item.getTitle().equals(marker.getTitle())) {
            marker.setTitle(item.getTitle());
            changed = true;
        }
        // Update marker position if the item changed position
        if (!marker.getPosition().equals(item.getPosition())) {
            marker.setPosition(item.getPosition());
            if (item.getZIndex() != null) {
                marker.setZIndex(item.getZIndex());
            }
            changed = true;
        }
        if (changed && marker.isInfoWindowShown()) {
            // Force a refresh of marker info window contents
            marker.showInfoWindow();
        }
    }

    /**
     * Called before the marker for a Cluster is added to the map.
     * The default implementation draws a circle with a rough count of the number of items.
     * <p>
     * The first time {@link ClusterManager#cluster()} is invoked on a set of items
     * {@link #onBeforeClusterRendered(Cluster, MarkerOptions)} will be called and
     * {@link #onClusterUpdated(Cluster, Marker)} will not be called. If an item is removed and
     * re-added (or updated) and {@link ClusterManager#cluster()} is invoked
     * again, then {@link #onClusterUpdated(Cluster, Marker)} will be called and
     * {@link #onBeforeClusterRendered(Cluster, MarkerOptions)} will not be called.
     *
     * @param cluster       cluster to be rendered
     * @param markerOptions markerOptions representing the provided cluster
     */
    protected void onBeforeClusterRendered(@NonNull Cluster<T> cluster, @NonNull MarkerOptions markerOptions) {
        // TODO: consider adding anchor(.5, .5) (Individual markers will overlap more often)
        markerOptions.icon(getDescriptorForCluster(cluster));
    }

    /**
     * Gets a BitmapDescriptor for the given cluster that contains a rough count of the number of
     * items. Used to set the cluster marker icon in the default implementations of
     * {@link #onBeforeClusterRendered(Cluster, MarkerOptions)} and
     * {@link #onClusterUpdated(Cluster, Marker)}.
     *
     * @param cluster cluster to get BitmapDescriptor for
     * @return a BitmapDescriptor for the marker icon for the given cluster that contains a rough
     * count of the number of items.
     */
    @NonNull
    protected BitmapDescriptor getDescriptorForCluster(@NonNull Cluster<T> cluster) {
        int bucket = getBucket(cluster);
        BitmapDescriptor descriptor = mIcons.get(bucket);
        if (descriptor == null) {
            mColoredCircleBackground.getPaint().setColor(getColor(bucket));
            mIconGenerator.setTextAppearance(getClusterTextAppearance(bucket));
            descriptor = BitmapDescriptorFactory.fromBitmap(mIconGenerator.makeIcon(getClusterText(bucket)));
            mIcons.put(bucket, descriptor);
        }
        return descriptor;
    }

    /**
     * Called after the marker for a Cluster has been added to the map.
     *
     * @param cluster the cluster that was just added to the map
     * @param marker  the marker representing the cluster that was just added to the map
     */
    protected void onClusterRendered(@NonNull Cluster<T> cluster, @NonNull Marker marker) {
    }

    /**
     * Called when a cached marker for a Cluster already exists on the map so the marker may
     * be updated to the latest cluster values. Default implementation updated the icon with a
     * circle with a rough count of the number of items. Note that the contents of the cluster may
     * not have changed since the cached marker was created - implementations of this method are
     * responsible for checking if something changed (if that matters to the implementation).
     * <p>
     * The first time {@link ClusterManager#cluster()} is invoked on a set of items
     * {@link #onBeforeClusterRendered(Cluster, MarkerOptions)} will be called and
     * {@link #onClusterUpdated(Cluster, Marker)} will not be called. If an item is removed and
     * re-added (or updated) and {@link ClusterManager#cluster()} is invoked
     * again, then {@link #onClusterUpdated(Cluster, Marker)} will be called and
     * {@link #onBeforeClusterRendered(Cluster, MarkerOptions)} will not be called.
     *
     * @param cluster cluster being updated
     * @param marker  cached marker that contains a potentially previous state of the cluster
     */
    protected void onClusterUpdated(@NonNull Cluster<T> cluster, @NonNull Marker marker) {
        // TODO: consider adding anchor(.5, .5) (Individual markers will overlap more often)
        marker.setIcon(getDescriptorForCluster(cluster));
    }

    /**
     * Called after the marker for a ClusterItem has been added to the map.
     *
     * @param clusterItem the item that was just added to the map
     * @param marker      the marker representing the item that was just added to the map
     */
    protected void onClusterItemRendered(@NonNull T clusterItem, @NonNull Marker marker) {
    }

    /**
     * Get the marker from a ClusterItem
     *
     * @param clusterItem ClusterItem which you will obtain its marker
     * @return a marker from a ClusterItem or null if it does not exists
     */
    public Marker getMarker(T clusterItem) {
        return mMarkerCache.get(clusterItem);
    }

    /**
     * Get the ClusterItem from a marker
     *
     * @param marker which you will obtain its ClusterItem
     * @return a ClusterItem from a marker or null if it does not exists
     */
    public T getClusterItem(Marker marker) {
        return mMarkerCache.get(marker);
    }

    /**
     * Get the marker from a Cluster
     *
     * @param cluster which you will obtain its marker
     * @return a marker from a cluster or null if it does not exists
     */
    public Marker getMarker(Cluster<T> cluster) {
        return mClusterMarkerCache.get(cluster);
    }

    /**
     * Get the Cluster from a marker
     *
     * @param marker which you will obtain its Cluster
     * @return a Cluster from a marker or null if it does not exists
     */
    public Cluster<T> getCluster(Marker marker) {
        return mClusterMarkerCache.get(marker);
    }

    /**
     * Creates markerWithPosition(s) for a particular cluster, animating it if necessary.
     */
    private class CreateMarkerTask {
        private final Cluster<T> cluster;
        private final Set<MarkerWithPosition> newMarkers;
        private final LatLng animateFrom;

        /**
         * @param c            the cluster to render.
         * @param markersAdded a collection of markers to append any created markers.
         * @param animateFrom  the location to animate the markerWithPosition from, or null if no
         *                     animation is required.
         */
        public CreateMarkerTask(Cluster<T> c, Set<MarkerWithPosition> markersAdded, LatLng animateFrom) {
            this.cluster = c;
            this.newMarkers = markersAdded;
            this.animateFrom = animateFrom;
        }

        private void perform(MarkerModifier markerModifier) {
            // Don't show small clusters. Render the markers inside, instead.
            if (!shouldRenderAsCluster(cluster)) {
                RendererLogger.d("ClusterRenderer", "Rendering individual cluster items, count: " + cluster.getItems().size());
                for (T item : cluster.getItems()) {
                    Marker marker = mMarkerCache.get(item);
                    MarkerWithPosition<T> markerWithPosition;
                    LatLng currentLocation = item.getPosition();
                    if (marker == null) {
                        RendererLogger.d("ClusterRenderer", "Creating new marker for cluster item at position: " + currentLocation);
                        MarkerOptions markerOptions = new MarkerOptions();
                        if (animateFrom != null) {
                            RendererLogger.d("ClusterRenderer", "Animating from position: " + animateFrom);
                            markerOptions.position(animateFrom);
                        } else if (mClusterMarkerCache.mCache.keySet().iterator().hasNext() && mClusterMarkerCache.mCache.keySet().iterator().next().getItems().contains(item)) {
                            T foundItem = null;
                            for (Cluster<T> cluster : mClusterMarkerCache.mCache.keySet()) {
                                for (T clusterItem : cluster.getItems()) {
                                    if (clusterItem.equals(item)) {
                                        foundItem = clusterItem;
                                        break;
                                    }
                                }
                            }
                            currentLocation = foundItem.getPosition();
                            RendererLogger.d("ClusterRenderer", "Found item in cache for animation at position: " + currentLocation);
                            markerOptions.position(currentLocation);
                        } else {
                            markerOptions.position(item.getPosition());
                            if (item.getZIndex() != null) {
                                markerOptions.zIndex(item.getZIndex());
                            }
                        }
                        onBeforeClusterItemRendered(item, markerOptions);
                        marker = mClusterManager.getMarkerCollection().addMarker(markerOptions);
                        markerWithPosition = new MarkerWithPosition<>(marker, item);
                        mMarkerCache.put(item, marker);
                        if (animateFrom != null) {
                            markerModifier.animate(markerWithPosition, animateFrom, item.getPosition());
                            RendererLogger.d("ClusterRenderer", "Animating marker from " + animateFrom + " to " + item.getPosition());
                        } else if (currentLocation != null) {
                            markerModifier.animate(markerWithPosition, currentLocation, item.getPosition());
                            RendererLogger.d("ClusterRenderer", "Animating marker from " + currentLocation + " to " + item.getPosition());
                        }
                    } else {
                        markerWithPosition = new MarkerWithPosition<>(marker, item);
                        markerModifier.animate(markerWithPosition, marker.getPosition(), item.getPosition());
                        RendererLogger.d("ClusterRenderer", "Animating existing marker from " + marker.getPosition() + " to " + item.getPosition());
                        if (!markerWithPosition.position.equals(item.getPosition())) {
                            RendererLogger.d("ClusterRenderer", "Updating cluster item marker position");
                            onClusterItemUpdated(item, marker);
                        }
                    }
                    onClusterItemRendered(item, marker);
                    newMarkers.add(markerWithPosition);
                }
                return;
            }

            // Handle cluster markers
            RendererLogger.d("ClusterRenderer", "Rendering cluster marker at position: " + cluster.getPosition());
            Marker marker = mClusterMarkerCache.get(cluster);
            MarkerWithPosition markerWithPosition;
            if (marker == null) {
                RendererLogger.d("ClusterRenderer", "Creating new cluster marker");
                MarkerOptions markerOptions = new MarkerOptions().position(animateFrom == null ? cluster.getPosition() : animateFrom);
                onBeforeClusterRendered(cluster, markerOptions);
                marker = mClusterManager.getClusterMarkerCollection().addMarker(markerOptions);
                mClusterMarkerCache.put(cluster, marker);
                markerWithPosition = new MarkerWithPosition(marker, null);
                if (animateFrom != null) {
                    markerModifier.animate(markerWithPosition, animateFrom, cluster.getPosition());
                    RendererLogger.d("ClusterRenderer", "Animating cluster marker from " + animateFrom + " to " + cluster.getPosition());
                }
            } else {
                markerWithPosition = new MarkerWithPosition(marker, null);
                RendererLogger.d("ClusterRenderer", "Updating existing cluster marker");
                onClusterUpdated(cluster, marker);
            }
            onClusterRendered(cluster, marker);
            newMarkers.add(markerWithPosition);
        }
    }

    /**
     * A Marker and its position. {@link Marker#getPosition()} must be called from the UI thread, so this
     * object allows lookup from other threads.
     */
    private static class MarkerWithPosition<T> {
        private final Marker marker;
        private final T clusterItem;
        private LatLng position;

        private MarkerWithPosition(Marker marker, T clusterItem) {
            this.marker = marker;
            this.clusterItem = clusterItem;
            position = marker.getPosition();
        }

        @Override
        public boolean equals(Object other) {
            if (other instanceof MarkerWithPosition) {
                return marker.equals(((MarkerWithPosition) other).marker);
            }
            return false;
        }

        @Override
        public int hashCode() {
            return marker.hashCode();
        }
    }


    /**
     * Animates a markerWithPosition from one position to another. TODO: improve performance for
     * slow devices (e.g. Nexus S).
     */
    private class AnimationTask extends AnimatorListenerAdapter implements ValueAnimator.AnimatorUpdateListener {
        private final MarkerWithPosition markerWithPosition;
        private final Marker marker;
        private final LatLng from;
        private final LatLng to;
        private boolean mRemoveOnComplete;
        private MarkerManager mMarkerManager;
        private ValueAnimator valueAnimator;

        private final Lock lock;

        private AnimationTask(MarkerWithPosition markerWithPosition, LatLng from, LatLng to, Lock lock) {
            this.markerWithPosition = markerWithPosition;
            this.marker = markerWithPosition.marker;
            this.from = from;
            this.to = to;
            this.lock = lock;
        }

        public void perform() {
            valueAnimator = ValueAnimator.ofFloat(0.0f, 1.0f);
            valueAnimator.setInterpolator(animationInterp);
            valueAnimator.setDuration(mAnimationDurationMs);
            valueAnimator.addUpdateListener(this);
            valueAnimator.addListener(this);
            valueAnimator.start();
        }

        public void cancel() {
            if (Looper.myLooper() != Looper.getMainLooper()) {
                new Handler(Looper.getMainLooper()).post(this::cancel);
                return;
            }
            try {
                markerWithPosition.position = to;
                mRemoveOnComplete = false;
                valueAnimator.cancel();
                lock.lock();
                ongoingAnimations.remove(this);
            } finally {
                lock.unlock();
            }
        }

        @Override
        public void onAnimationEnd(Animator animation) {
            if (mRemoveOnComplete) {
                mMarkerCache.remove(marker);
                mClusterMarkerCache.remove(marker);
                mMarkerManager.remove(marker);
            }
            markerWithPosition.position = to;

            // Remove the task from the queue
            lock.lock();
            ongoingAnimations.remove(this);
            lock.unlock();
        }

        public void removeOnAnimationComplete(MarkerManager markerManager) {
            mMarkerManager = markerManager;
            mRemoveOnComplete = true;
        }

        @Override
        public void onAnimationUpdate(@NonNull ValueAnimator valueAnimator) {
            if (to == null || from == null || marker == null) {
                return;
            }
            float fraction = valueAnimator.getAnimatedFraction();
            double lat = (to.latitude - from.latitude) * fraction + from.latitude;
            double lngDelta = to.longitude - from.longitude;

            // Take the shortest path across the 180th meridian.
            if (Math.abs(lngDelta) > 180) {
                lngDelta -= Math.signum(lngDelta) * 360;
            }
            double lng = lngDelta * fraction + from.longitude;
            LatLng position = new LatLng(lat, lng);
            marker.setPosition(position);
            markerWithPosition.position = position;
        }
    }
}
