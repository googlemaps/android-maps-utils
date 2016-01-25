/*
 * Copyright 2013 Google Inc.
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
import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.MessageQueue;
import android.os.Process;
import android.support.annotation.Nullable;
import android.util.Log;
import android.util.SparseArray;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.Projection;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.MarkerManager;
import com.google.maps.android.R;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterItem;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.geometry.Point;
import com.google.maps.android.projection.SphericalMercatorProjection;
import com.google.maps.android.ui.IconGenerator;
import com.google.maps.android.ui.SquareTextView;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static com.google.maps.android.clustering.algo.NonHierarchicalDistanceBasedAlgorithm.MAX_DISTANCE_AT_ZOOM;

/**
 * The default view for a ClusterManager. Markers are animated in and out of clusters.
 */
public class DefaultClusterRenderer<T extends ClusterItem> implements ClusterRenderer<T> {

    private static final String TAG = "DefaultClusterRenderer";
    private static final boolean SHOULD_ANIMATE = Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB;
    private static final int[] BUCKETS = {10, 20, 50, 100, 200, 500, 1000};

    private final GoogleMap mMap;
    private final IconGenerator mIconGenerator;
    private final ClusterManager<T> mClusterManager;
    private final ViewModifier mViewModifier;
    private final float mDensity;
    private final AtomicBoolean mRenderInProgress = new AtomicBoolean(false);

    private ShapeDrawable mColoredCircleBackground;

    /**
     * Markers that are currently on the map.
     */
    private Set<MarkerWithPosition> mMarkers = Collections.newSetFromMap(new ConcurrentHashMap<MarkerWithPosition, Boolean>());

    /**
     * Icons for each bucket.
     */
    private SparseArray<BitmapDescriptor> mIcons = new SparseArray<>();

    /**
     * If cluster size is less than this size, display individual markers.
     */
    private static final int MIN_CLUSTER_SIZE = 4;

    /**
     * The currently displayed set of clusters.
     */
    private Set<? extends Cluster<T>> mDisplayedClusters;

    /**
     * The current set of clusters delivered through {@link #onClustersChanged(Set)}. This is not necessarily the set being displayed yet.
     */
    private Set<? extends Cluster<T>> mCurrentClusters;

    /**
     * Lookup between markers and the associated cluster.
     */
    private Map<Marker, Cluster<T>> mMarkerToCluster = new HashMap<>();
    private Map<Cluster<T>, Marker> mClusterToMarker = new HashMap<>();

    /**
     * The target zoom level for the current set of clusters.
     */
    private float mZoom;

    private final MarkerManager.MarkerItemCollectionObserver<T> mItemCollectionObserver = new MarkerManager.MarkerItemCollectionObserver<T>() {

        @Override
        public boolean onCanAddMarker(MarkerManager.MarkerItemCollection<T> collection, T item) {
            if (Log.isLoggable(TAG, Log.DEBUG)) {
                Log.d(TAG, "collection add marker: " + item);
            }

            return !isItemClusteredVisually(item);
        }
    };

    public DefaultClusterRenderer(Context context, GoogleMap map, ClusterManager<T> clusterManager) {
        mMap = map;
        mDensity = context.getResources().getDisplayMetrics().density;
        mViewModifier = new ViewModifier(this);

        mIconGenerator = new IconGenerator(context);
        mIconGenerator.setContentView(makeSquareTextView(context));
        mIconGenerator.setTextAppearance(R.style.ClusterIcon_TextAppearance);
        mIconGenerator.setBackground(makeClusterBackground());
        mClusterManager = clusterManager;
    }

    private boolean isItemClusteredVisually(T item) {
        if (mCurrentClusters != null) {
            final Set<? extends Cluster<T>> currentClusters = mCurrentClusters;
            for (Cluster<T> cluster : currentClusters) {
                if (shouldRenderAsCluster(cluster) && cluster.getItems().contains(item)) {
                    if (Log.isLoggable(TAG, Log.DEBUG)) {
                        Log.d(TAG, "item in cluster: " + item);
                    }

                    return true;
                }
            }
        }

        if (Log.isLoggable(TAG, Log.DEBUG)) {
            Log.d(TAG, "item not in cluster: " + item);
        }

        return false;
    }

    @Override
    public void onAdd() {
        final MarkerManager.MarkerItemCollection<T> markerCollection = mClusterManager.getMarkerCollection();

        markerCollection.registerObserver(mItemCollectionObserver);
    }

    @Override
    public void onRemove() {
        MarkerManager.MarkerItemCollection<T> markerCollection = mClusterManager.getMarkerCollection();
        markerCollection.unregisterObserver(mItemCollectionObserver);
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
        squareTextView.setId(R.id.text);
        int twelveDpi = (int) (12 * mDensity);
        squareTextView.setPadding(twelveDpi, twelveDpi, twelveDpi, twelveDpi);
        return squareTextView;
    }

    protected int getColor(int clusterSize) {
        final float hueRange = 220;
        final float sizeRange = 300;
        final float size = Math.min(clusterSize, sizeRange);
        final float hue = (sizeRange - size) * (sizeRange - size) / (sizeRange * sizeRange) * hueRange;
        return Color.HSVToColor(new float[]{
                hue, 1f, .6f
        });
    }

    protected String getClusterText(int bucket) {
        if (bucket < BUCKETS[0]) {
            return String.valueOf(bucket);
        }
        return String.valueOf(bucket) + "+";
    }

    /**
     * Gets the "bucket" for a particular cluster. By default, uses the number of points within the cluster, bucketed to some set points.
     */
    protected int getBucket(Cluster<T> cluster) {
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
     * ViewModifier ensures only one re-rendering of the view occurs at a time, and schedules re-rendering, which is performed by the
     * RenderTask.
     */
    private static class ViewModifier<T extends ClusterItem> extends Handler {

        private static final int RUN_TASK = 0;
        private static final int TASK_FINISHED = 1;

        private final WeakReference<DefaultClusterRenderer> mParent;

        private boolean mViewModificationInProgress = false;
        private DefaultClusterRenderer.RenderTask mNextClusters = null;

        public ViewModifier(DefaultClusterRenderer parent) {
            mParent = new WeakReference<>(parent);
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case TASK_FINISHED:
                    mViewModificationInProgress = false;
                    if (mNextClusters != null) {
                        // Run the task that was queued up.
                        sendEmptyMessage(RUN_TASK);
                    } else {
                        if (Log.isLoggable(TAG, Log.DEBUG)) {
                            Log.d(TAG, "rendering finished");
                        }

                        final DefaultClusterRenderer parent = mParent.get();
                        if (parent != null) {
                            parent.mRenderInProgress.set(false);
                        }
                    }
                    break;

                case RUN_TASK:
                    removeMessages(RUN_TASK);

                    final DefaultClusterRenderer parent = mParent.get();
                    if (parent != null) {
                        if (!mViewModificationInProgress && mNextClusters != null) {
                            if (Log.isLoggable(TAG, Log.DEBUG)) {
                                Log.d(TAG, "rendering in progress");
                            }

                            parent.mRenderInProgress.set(true);

                            DefaultClusterRenderer.RenderTask renderTask;
                            synchronized (this) {
                                renderTask = mNextClusters;
                                mNextClusters = null;
                                mViewModificationInProgress = true;
                            }

                            renderTask.setCallback(new Runnable() {
                                @Override
                                public void run() {
                                    sendEmptyMessage(TASK_FINISHED);
                                }
                            });

                            renderTask.setProjection(parent.mMap.getProjection());
                            renderTask.setMapZoom(parent.mMap.getCameraPosition().zoom);
                            new Thread(renderTask).start();
                        }
                    }
                    break;
            }
        }

        public void queue(Set<? extends Cluster<T>> clusters) {
            synchronized (this) {
                final DefaultClusterRenderer parent = mParent.get();
                if (parent != null) {
                    // Overwrite any pending cluster tasks - we don't care about intermediate states.
                    mNextClusters = parent.new RenderTask(clusters);
                }
            }
            sendEmptyMessage(RUN_TASK);
        }
    }

    /**
     * Determine whether the cluster should be rendered as individual markers or a cluster.
     */
    protected boolean shouldRenderAsCluster(Cluster<T> cluster) {
        return cluster.getSize() > MIN_CLUSTER_SIZE;
    }

    /**
     * Transforms the current view (represented by DefaultClusterRenderer.mDisplayedClusters and DefaultClusterRenderer.mZoom) to a new zoom
     * level and set of clusters.
     *
     * <p>This must be run off the UI thread. Work is coordinated in the RenderTask, then queued up to be executed by a MarkerModifier.<p/>
     *
     * <p>There are three stages for the render: <ol> <li>Markers are added to the map</li> <li>Markers are animated to their final
     * position</li> <li>Any old markers are removed from the map</li> </ol> </p>
     *
     * <p>When zooming in, markers are animated out from the nearest existing cluster. When zooming out, existing clusters are animated to
     * the nearest new cluster.</p>
     */
    private class RenderTask implements Runnable {

        private final Set<? extends Cluster<T>> clusters;

        private Runnable mCallback;
        private Projection mProjection;
        private SphericalMercatorProjection mSphericalMercatorProjection;
        private float mMapZoom;

        private RenderTask(Set<? extends Cluster<T>> clusters) {
            this.clusters = clusters;
        }

        /**
         * A callback to be run when all work has been completed.
         *
         * @param callback
         */
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

        public void run() {
            Thread.currentThread().setPriority(Process.THREAD_PRIORITY_BACKGROUND);

            if (clusters.equals(DefaultClusterRenderer.this.mDisplayedClusters)) {
                mCallback.run();
                return;
            }

            final MarkerModifier markerModifier = new MarkerModifier(DefaultClusterRenderer.this);

            final float zoom = mMapZoom;
            final boolean zoomingIn = zoom > mZoom;
            final float zoomDelta = zoom - mZoom;

            final Set<MarkerWithPosition> markersToRemove = mMarkers;
            final LatLngBounds visibleBounds = mProjection.getVisibleRegion().latLngBounds;
            // TODO: Add some padding, so that markers can animate in from off-screen.

            // Find all of the existing clusters that are on-screen. These are candidates for
            // markers to animate from.
            List<Point> existingClustersOnScreen = null;
            if (DefaultClusterRenderer.this.mDisplayedClusters != null && SHOULD_ANIMATE) {
                existingClustersOnScreen = new ArrayList<Point>();
                for (Cluster<T> c : DefaultClusterRenderer.this.mDisplayedClusters) {
                    if (shouldRenderAsCluster(c) && visibleBounds.contains(c.getPosition())) {
                        Point point = mSphericalMercatorProjection.toPoint(c.getPosition());
                        existingClustersOnScreen.add(point);
                    }
                }
            }

            // Create the new markers and animate them to their new positions.
            final Set<MarkerWithPosition> newMarkers = Collections.newSetFromMap(new ConcurrentHashMap<MarkerWithPosition, Boolean>());

            for (Cluster<T> c : clusters) {
                boolean onScreen = visibleBounds.contains(c.getPosition());
                if (zoomingIn && onScreen && SHOULD_ANIMATE) {
                    Point point = mSphericalMercatorProjection.toPoint(c.getPosition());
                    Point closest = findClosestCluster(existingClustersOnScreen, point);
                    if (closest != null) {
                        LatLng animateTo = mSphericalMercatorProjection.toLatLng(closest);
                        markerModifier.add(true, new CreateMarkerTask<>(DefaultClusterRenderer.this, c, newMarkers, animateTo));
                    } else {
                        markerModifier.add(true, new CreateMarkerTask<>(DefaultClusterRenderer.this, c, newMarkers, null));
                    }
                } else {
                    markerModifier.add(onScreen, new CreateMarkerTask<>(DefaultClusterRenderer.this, c, newMarkers, null));
                }
            }

            // Wait for all markers to be added.
            markerModifier.waitUntilFree();

            // Don't remove any markers that were just added. This is basically anything that had
            // a hit in the MarkerCache.
            markersToRemove.removeAll(newMarkers);

            // Find all of the new clusters that were added on-screen. These are candidates for
            // markers to animate from.
            List<Point> newClustersOnScreen = null;
            if (SHOULD_ANIMATE) {
                newClustersOnScreen = new ArrayList<Point>();
                for (Cluster<T> c : clusters) {
                    if (shouldRenderAsCluster(c) && visibleBounds.contains(c.getPosition())) {
                        Point p = mSphericalMercatorProjection.toPoint(c.getPosition());
                        newClustersOnScreen.add(p);
                    }
                }
            }

            // Remove the old markers, animating them into clusters if zooming out.
            for (final MarkerWithPosition marker : markersToRemove) {
                boolean onScreen = visibleBounds.contains(marker.position);
                // Don't animate when zooming out more than 3 zoom levels.
                // TODO: drop animation based on speed of device & number of markers to animate.
                if (!zoomingIn && zoomDelta > -3 && onScreen && SHOULD_ANIMATE) {
                    final Point point = mSphericalMercatorProjection.toPoint(marker.position);
                    final Point closest = findClosestCluster(newClustersOnScreen, point);
                    if (closest != null) {
                        LatLng animateTo = mSphericalMercatorProjection.toLatLng(closest);
                        markerModifier.animateThenRemove(marker, marker.position, animateTo);
                    } else {
                        markerModifier.remove(true, marker.marker);
                    }
                } else {
                    markerModifier.remove(onScreen, marker.marker);
                }
            }

            markerModifier.waitUntilFree();

            mMarkers = newMarkers;
            DefaultClusterRenderer.this.mDisplayedClusters = clusters;
            mZoom = zoom;

            mCallback.run();
        }
    }

    @Override
    public void onClustersChanged(Set<? extends Cluster<T>> clusters) {
        if (Log.isLoggable(TAG, Log.DEBUG)) {
            Log.d(TAG, "clusters changed");
        }

        mCurrentClusters = clusters;
        mViewModifier.queue(clusters);
    }

    private static double distanceSquared(Point a, Point b) {
        return (a.x - b.x) * (a.x - b.x) + (a.y - b.y) * (a.y - b.y);
    }

    private static Point findClosestCluster(List<Point> markers, Point point) {
        if (markers == null || markers.isEmpty()) return null;

        // TODO: make this configurable.
        double minDistSquared = MAX_DISTANCE_AT_ZOOM * MAX_DISTANCE_AT_ZOOM;
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
     * Handles all markerWithPosition manipulations on the map. Work (such as adding, removing, or animating a markerWithPosition) is
     * performed while trying not to block the rest of the app's UI.
     */
    private static class MarkerModifier extends Handler implements MessageQueue.IdleHandler {
        private static final int BLANK = 0;

        private final WeakReference<DefaultClusterRenderer> mParent;
        private final Lock lock = new ReentrantLock();
        private final Condition busyCondition = lock.newCondition();

        private Queue<DefaultClusterRenderer.CreateMarkerTask> mCreateMarkerTasks = new LinkedList<>();
        private Queue<DefaultClusterRenderer.CreateMarkerTask> mOnScreenCreateMarkerTasks = new LinkedList<>();
        private Queue<Marker> mRemoveMarkerTasks = new LinkedList<>();
        private Queue<Marker> mOnScreenRemoveMarkerTasks = new LinkedList<>();
        private Queue<DefaultClusterRenderer.AnimationTask> mAnimationTasks = new LinkedList<>();

        /**
         * Whether the idle listener has been added to the UI thread's MessageQueue.
         */
        private boolean mListenerAdded;

        private MarkerModifier(DefaultClusterRenderer parent) {
            super(Looper.getMainLooper());

            mParent = new WeakReference<>(parent);
        }

        /**
         * Creates markers for a cluster some time in the future.
         *
         * @param priority whether this operation should have priority.
         */
        public void add(boolean priority, DefaultClusterRenderer.CreateMarkerTask c) {
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
            final DefaultClusterRenderer parent = mParent.get();
            if (parent != null) {
                mAnimationTasks.add(parent.new AnimationTask(marker, from, to));
            }
            lock.unlock();
        }

        /**
         * Animates a markerWithPosition some time in the future, and removes it when the animation is complete.
         *
         * @param marker the markerWithPosition to animate.
         * @param from   the position to animate from.
         * @param to     the position to animate to.
         */
        public void animateThenRemove(MarkerWithPosition marker, LatLng from, LatLng to) {
            lock.lock();
            final DefaultClusterRenderer parent = mParent.get();
            if (parent != null) {
                DefaultClusterRenderer.AnimationTask animationTask = parent.new AnimationTask(marker, from, to);
                animationTask.removeOnAnimationComplete(parent.mClusterManager.getMarkerManager());
                mAnimationTasks.add(animationTask);
            }
            lock.unlock();
        }

        @Override
        public void handleMessage(Message msg) {
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
                mAnimationTasks.poll().perform();
            } else if (!mOnScreenCreateMarkerTasks.isEmpty()) {
                mOnScreenCreateMarkerTasks.poll().perform(this);
            } else if (!mCreateMarkerTasks.isEmpty()) {
                mCreateMarkerTasks.poll().perform(this);
            } else if (!mRemoveMarkerTasks.isEmpty()) {
                removeMarker(mRemoveMarkerTasks.poll());
            }
        }

        private void removeMarker(Marker m) {
            final DefaultClusterRenderer parent = mParent.get();
            if (parent != null) {
                Cluster cluster = (Cluster) parent.mMarkerToCluster.get(m);
                parent.mClusterToMarker.remove(cluster);
                parent.mMarkerToCluster.remove(m);
                parent.mClusterManager.getMarkerManager().remove(m);
            }
        }

        /**
         * @return true if there is still work to be processed.
         */
        public boolean isBusy() {
            try {
                lock.lock();
                return !(mCreateMarkerTasks.isEmpty() && mOnScreenCreateMarkerTasks.isEmpty() &&
                        mOnScreenRemoveMarkerTasks.isEmpty() && mRemoveMarkerTasks.isEmpty() &&
                        mAnimationTasks.isEmpty()
                );
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
     * Called before the marker for a ClusterItem is added to the map.
     */
    protected void onBeforeClusterItemRendered(T item, MarkerOptions markerOptions) {
    }

    /**
     * Called before the marker for a Cluster is added to the map. The default implementation draws a circle with a rough count of the
     * number of items.
     */
    protected void onBeforeClusterRendered(Cluster<T> cluster, MarkerOptions markerOptions) {
        int bucket = getBucket(cluster);
        BitmapDescriptor descriptor = mIcons.get(bucket);
        if (descriptor == null) {
            mColoredCircleBackground.getPaint().setColor(getColor(bucket));
            descriptor = BitmapDescriptorFactory.fromBitmap(mIconGenerator.makeIcon(getClusterText(bucket)));
            mIcons.put(bucket, descriptor);
        }
        markerOptions.anchor(0.5f, 0.5f);
        markerOptions.icon(descriptor);
    }

    /**
     * Called after the marker for a Cluster has been added to the map.
     */
    protected void onClusterRendered(Cluster<T> cluster, Marker marker) {
    }

    /**
     * Called after the marker for a ClusterItem has been added to the map.
     */
    protected void onClusterItemRendered(T clusterItem, Marker marker) {
    }

    /**
     * Get the marker from a ClusterItem
     *
     * @param clusterItem ClusterItem which you will obtain its marker
     * @return a marker from a ClusterItem or null if it does not exists
     */
    public Marker getMarker(T clusterItem) {
        return mClusterManager.getMarkerCollection().getMarker(clusterItem);
    }

    /**
     * Get the ClusterItem from a marker
     *
     * @param marker which you will obtain its ClusterItem
     * @return a ClusterItem from a marker or null if it does not exists
     */
    public T getClusterItem(Marker marker) {
        return mClusterManager.getMarkerCollection().getItem(marker);
    }

    /**
     * Get the marker from a Cluster
     *
     * @param cluster which you will obtain its marker
     * @return a marker from a cluster or null if it does not exists
     */
    public Marker getMarker(Cluster<T> cluster) {
        return mClusterToMarker.get(cluster);
    }

    @Override
    @Nullable
    public Cluster<T> getCluster(Marker marker) {
        return mMarkerToCluster.get(marker);
    }

    /**
     * Creates markerWithPosition(s) for a particular cluster, animating it if necessary.
     */
    private static class CreateMarkerTask<T extends ClusterItem> {

        private final DefaultClusterRenderer<T> mParent;
        private final Cluster<T> cluster;
        private final Set<MarkerWithPosition> newMarkers;
        private final LatLng animateFrom;

        /**
         * @param parent
         * @param c            the cluster to render.
         * @param markersAdded a collection of markers to append any created markers.
         * @param animateFrom  the location to animate the markerWithPosition from, or null if no animation is required.
         */
        public CreateMarkerTask(DefaultClusterRenderer<T> parent, Cluster<T> c, Set<MarkerWithPosition> markersAdded, LatLng animateFrom) {
            mParent = parent;
            this.cluster = c;
            this.newMarkers = markersAdded;
            this.animateFrom = animateFrom;
        }

        private void perform(MarkerModifier markerModifier) {
            // Don't show small clusters. Render the markers inside, instead.
            if (!mParent.shouldRenderAsCluster(cluster)) {

                MarkerManager.MarkerItemCollection<T> markerCollection = mParent.mClusterManager.getMarkerCollection();

                for (T item : cluster.getItems()) {
                    Marker marker = markerCollection.getMarker(item);
                    MarkerWithPosition markerWithPosition = null;

                    if (marker == null) {
                        MarkerOptions markerOptions = new MarkerOptions();
                        if (animateFrom != null) {
                            markerOptions.position(animateFrom);
                        } else {
                            markerOptions.position(item.getPosition());
                        }

                        // TODO should run in the background
                        mParent.onBeforeClusterItemRendered(item, markerOptions);

                        marker = markerCollection.addMarker(markerOptions, item);
                        if (marker != null) {
                            markerWithPosition = new MarkerWithPosition(marker);
                            if (animateFrom != null) {
                                markerModifier.animate(markerWithPosition, animateFrom, item.getPosition());
                            }
                        }
                    } else {
                        markerWithPosition = new MarkerWithPosition(marker);
                    }

                    if (marker != null) {
                        mParent.onClusterItemRendered(item, marker);
                    }

                    if (markerWithPosition != null) {
                        newMarkers.add(markerWithPosition);
                    }
                }

            } else {
                MarkerOptions markerOptions = new MarkerOptions().
                        position(animateFrom == null ? cluster.getPosition() : animateFrom);

                mParent.onBeforeClusterRendered(cluster, markerOptions);

                Marker marker = mParent.mClusterManager.getClusterMarkerCollection().addMarker(markerOptions);
                mParent.mMarkerToCluster.put(marker, cluster);
                mParent.mClusterToMarker.put(cluster, marker);
                MarkerWithPosition markerWithPosition = new MarkerWithPosition(marker);
                if (animateFrom != null) {
                    markerModifier.animate(markerWithPosition, animateFrom, cluster.getPosition());
                }
                mParent.onClusterRendered(cluster, marker);
                newMarkers.add(markerWithPosition);
            }
        }
    }

    /**
     * A Marker and its position. Marker.getPosition() must be called from the UI thread, so this object allows lookup from other threads.
     */
    private static class MarkerWithPosition {
        private final Marker marker;
        private LatLng position;

        private MarkerWithPosition(Marker marker) {
            this.marker = marker;
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

    private static final TimeInterpolator ANIMATION_INTERP = new DecelerateInterpolator();

    /**
     * Animates a markerWithPosition from one position to another. TODO: improve performance for slow devices (e.g. Nexus S).
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR1)
    private class AnimationTask extends AnimatorListenerAdapter implements ValueAnimator.AnimatorUpdateListener {
        private final MarkerWithPosition markerWithPosition;
        private final Marker marker;
        private final LatLng from;
        private final LatLng to;
        private boolean mRemoveOnComplete;
        private MarkerManager mMarkerManager;

        private AnimationTask(MarkerWithPosition markerWithPosition, LatLng from, LatLng to) {
            this.markerWithPosition = markerWithPosition;
            this.marker = markerWithPosition.marker;
            this.from = from;
            this.to = to;
        }

        public void perform() {
            ValueAnimator valueAnimator = ValueAnimator.ofFloat(0, 1);
            valueAnimator.setInterpolator(ANIMATION_INTERP);
            valueAnimator.addUpdateListener(this);
            valueAnimator.addListener(this);
            valueAnimator.start();
        }

        @Override
        public void onAnimationEnd(Animator animation) {
            if (mRemoveOnComplete) {
                Cluster<T> cluster = mMarkerToCluster.get(marker);
                mClusterToMarker.remove(cluster);
                mMarkerToCluster.remove(marker);
                mMarkerManager.remove(marker);
            }
            markerWithPosition.position = to;
        }

        public void removeOnAnimationComplete(MarkerManager markerManager) {
            mMarkerManager = markerManager;
            mRemoveOnComplete = true;
        }

        @Override
        public void onAnimationUpdate(ValueAnimator valueAnimator) {
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
        }
    }
}
