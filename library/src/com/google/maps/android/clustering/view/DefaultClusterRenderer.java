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
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.util.Pair;
import android.util.SparseArray;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.Projection;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import static com.google.maps.android.SphericalUtil.computeDistanceBetween;
import static com.google.maps.android.SphericalUtil.computeOffset;
import static com.google.maps.android.clustering.algo.NonHierarchicalDistanceBasedAlgorithm.MAX_DISTANCE_AT_ZOOM;

/**
 * The default view for a ClusterManager. Markers are animated in and out of clusters.
 */
public class DefaultClusterRenderer<T extends ClusterItem> implements ClusterRenderer<T>, GoogleMap.OnCameraChangeListener {

    private static final String TAG = "DefaultClusterRenderer";
    private static final boolean SHOULD_ANIMATE = Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB;
    private static final int[] BUCKETS = {10, 20, 50, 100, 200, 500, 1000};
    private static final float DEFAULT_VISIBLE_REGION_PADDING = 0.3f;
    private static final int DEFAULT_MIN_CLUSTER_SIZE = 4;
    private static final int DEFAULT_BATCH_MAP_TASK = 10;

    private final GoogleMap mMap;
    private final IconGenerator mIconGenerator;
    private final ClusterManager<T> mClusterManager;
    private final ViewModifier<T> mViewModifier;
    private final float mDensity;

    private ShapeDrawable mColoredCircleBackground;

    /**
     * Markers that are currently on the map. Thread-safe.
     */
    private final Set<MarkerWithPosition> mMarkers = Collections.newSetFromMap(new ConcurrentHashMap<MarkerWithPosition, Boolean>());

    /**
     * Icons for each bucket.
     */
    private SparseArray<BitmapDescriptor> mIcons = new SparseArray<>();

    /**
     * If cluster size is less than this size, display individual markers.
     */
    private int mMinClusterSize = DEFAULT_MIN_CLUSTER_SIZE;

    /**
     * Number of map tasks that will be performed batched.
     */
    private int mBatchMapTask = DEFAULT_BATCH_MAP_TASK;

    /**
     * The currently displayed set of clusters.
     */
    private final AtomicReference<Set<? extends Cluster<T>>> mDisplayedClusters = new AtomicReference<>();

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
    private CameraPosition mPreviousCameraPosition;

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
        mViewModifier = new ViewModifier<>(this);

        mIconGenerator = createIconGenerator(context);
        mClusterManager = clusterManager;
    }

    /**
     * Create a {@link IconGenerator} for this ClusterRenderer. Subclasses may want to provide a custom
     * implementation here.
     *
     * @param context current context
     * @return an implementation of {@link IconGenerator}
     */
    @NonNull
    protected IconGenerator createIconGenerator(Context context) {
        IconGenerator iconGenerator = new IconGenerator(context);
        iconGenerator.setContentView(makeSquareTextView(context));
        iconGenerator.setTextAppearance(R.style.ClusterIcon_TextAppearance);
        iconGenerator.setBackground(makeClusterBackground());
        return iconGenerator;
    }


    /**
     * @param item
     * @return {@code true} if the supplied item belongs to a cluster, otherwise {@code false}
     */
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
        mClusterManager.getMarkerCollection().registerObserver(mItemCollectionObserver);
    }

    @Override
    public void onRemove() {
        mClusterManager.getMarkerCollection().unregisterObserver(mItemCollectionObserver);
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

        private static final int MSG_RUN = 0;
        private static final int MSG_FINISHED = 1;

        private final DefaultClusterRenderer<T> mParent;

        private boolean mViewModificationInProgress = false;
        private RenderTask<T> mNextClusters = null;

        private final Runnable mFinishedRunnable = new Runnable() {
            @Override
            public void run() {
                sendEmptyMessage(MSG_FINISHED);
            }
        };
        private final ExecutorService mExecutorService;

        public ViewModifier(DefaultClusterRenderer<T> parent) {
            mParent = parent;
            mExecutorService = Executors.newCachedThreadPool();
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_FINISHED:
                    mViewModificationInProgress = false;
                    if (mNextClusters != null) {
                        // Run the task that was queued up.
                        sendEmptyMessage(MSG_RUN);
                    } else {
                        if (Log.isLoggable(TAG, Log.DEBUG)) {
                            Log.d(TAG, "rendering finished");
                        }
                    }
                    break;

                case MSG_RUN:
                    removeMessages(MSG_RUN);

                    if (!mViewModificationInProgress && mNextClusters != null) {
                        if (Log.isLoggable(TAG, Log.DEBUG)) {
                            Log.d(TAG, "rendering in progress");
                        }

                        RenderTask renderTask;
                        synchronized (this) {
                            renderTask = mNextClusters;
                            mNextClusters = null;
                            mViewModificationInProgress = true;
                        }

                        renderTask.setCallback(mFinishedRunnable);
                        renderTask.setProjection(mParent.mMap.getProjection());
                        renderTask.setMapZoom(mParent.mMap.getCameraPosition().zoom);
                        mExecutorService.execute(renderTask);
                    }
                    break;
            }
        }

        public void queueZoomed(Set<? extends Cluster<T>> clusters) {
            queue(clusters, true);
        }

        public void queuePanned(Set<? extends Cluster<T>> clusters) {
            queue(clusters, false);
        }

        private void queue(Set<? extends Cluster<T>> clusters, boolean mapZoomed) {
            synchronized (this) {
                // Overwrite any pending cluster tasks - we don't care about intermediate states.
                mNextClusters = new RenderTask<>(mParent, clusters, mapZoomed);
            }
            sendEmptyMessage(MSG_RUN);
        }
    }

    /**
     * If cluster size is less than this size, display individual markers.
     *
     * @param minClusterSize must be greater than <code>0</code>
     */
    public void setMinClusterSize(int minClusterSize) {
        mMinClusterSize = Math.max(1, minClusterSize);
    }

    /**
     * Determine whether the cluster should be rendered as individual markers or a cluster.
     */
    protected boolean shouldRenderAsCluster(Cluster<T> cluster) {
        return cluster.getSize() > mMinClusterSize;
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
    private static class RenderTask<T extends ClusterItem> implements Runnable {

        private final Set<? extends Cluster<T>> mClusters;
        private final boolean mMapZoomed;
        private final DefaultClusterRenderer<T> mParent;

        private Runnable mCallback;
        private Projection mProjection;
        private SphericalMercatorProjection mSphericalMercatorProjection;
        private float mMapZoom;
        private float mTargetZoom;

        private RenderTask(DefaultClusterRenderer<T> parent, Set<? extends Cluster<T>> clusters, boolean mapZoomed) {
            mParent = parent;
            mClusters = clusters;
            mMapZoomed = mapZoomed;
            mTargetZoom = mParent.mZoom;
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
            this.mSphericalMercatorProjection = new SphericalMercatorProjection(256 * Math.pow(2, Math.min(zoom, mTargetZoom)));
        }

        public void run() {
            Thread.currentThread().setPriority(Process.THREAD_PRIORITY_BACKGROUND);

            Set<? extends Cluster<T>> currentClusters = mParent.mDisplayedClusters.get();
            if (currentClusters == null) {
                currentClusters = Collections.emptySet();
            }

            // if the map zoomed but clusters didn't change, there is nothing to do
            if (mMapZoomed && mClusters.equals(currentClusters)) {
                mCallback.run();
                return;
            }

            final MarkerModifier<T> markerModifier = new MarkerModifier<>(mParent);

            final float zoom = mMapZoom;
            final boolean zoomingIn = zoom > mTargetZoom;
            final float zoomDelta = zoom - mTargetZoom;

            final Set<MarkerWithPosition> markersToRemove = mParent.mMarkers;
            LatLngBounds visibleBounds = mProjection.getVisibleRegion().latLngBounds;
            // Add some padding, so that markers can animate in from off-screen.
            double padding = computeDistanceBetween(visibleBounds.northeast, visibleBounds.southwest) * DEFAULT_VISIBLE_REGION_PADDING;
            visibleBounds = new LatLngBounds(computeOffset(visibleBounds.southwest, padding, 225),
                    computeOffset(visibleBounds.northeast, padding, 45));

            // Find all of the existing clusters that are on-screen. These are candidates for markers to animate from.
            List<Point> existingClustersOnScreen = null;
            if (SHOULD_ANIMATE) {
                existingClustersOnScreen = new ArrayList<>();
                for (Cluster<T> c : currentClusters) {
                    if (mParent.shouldRenderAsCluster(c) && visibleBounds.contains(c.getPosition())) {
                        Point point = mSphericalMercatorProjection.toPoint(c.getPosition());
                        existingClustersOnScreen.add(point);
                    }
                }
            }

            // Add new clusters ...
            for (Cluster<T> c : mClusters) {
                boolean onScreen = visibleBounds.contains(c.getPosition());
                if (onScreen && !mParent.hasMarker(c)) { // ... but only the visible ones and clusters without a marker
                    if (SHOULD_ANIMATE) {
                        Point point = mSphericalMercatorProjection.toPoint(c.getPosition());
                        Point closest = findClosestCluster(existingClustersOnScreen, point);
                        if (closest != null) {
                            LatLng animateTo = mSphericalMercatorProjection.toLatLng(closest);
                            markerModifier.add(true, new CreateMarkerRunnable<>(markerModifier, mParent, c, animateTo));
                        } else {
                            markerModifier.add(true, new CreateMarkerRunnable<>(markerModifier, mParent, c, null));
                        }
                    } else {
                        markerModifier.add(true, new CreateMarkerRunnable<>(markerModifier, mParent, c, null));
                    }
                }
            }

            // Wait for all markers to be added.
            markerModifier.waitUntilFree();

            // The new set of markers which will be the base for the next time
            Set<MarkerWithPosition> newMarkers = null;

            // Only clean up when zoomed and thus clusters changed
            if (mMapZoomed) {
                // Don't remove any markers that were just added. This is basically anything that had a hit in the MarkerCache.
                newMarkers = markerModifier.getNewMarkers();
                markersToRemove.removeAll(newMarkers);
            } else {
                // The map panned and new markers where added: extend the set of markers
                mParent.mMarkers.addAll(markerModifier.getNewMarkers());
            }

            // Find all of the new clusters that were added on-screen. These are candidates for markers to animate from.
            List<Point> newClustersOnScreen = null;
            if (SHOULD_ANIMATE) {
                newClustersOnScreen = new ArrayList<>();
                for (Cluster<T> c : mClusters) {
                    if (mParent.shouldRenderAsCluster(c) && visibleBounds.contains(c.getPosition())) {
                        Point p = mSphericalMercatorProjection.toPoint(c.getPosition());
                        newClustersOnScreen.add(p);
                    }
                }
            }

            // Remove the old markers, animating them into clusters if zooming out.
            if (mMapZoomed) {
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
            }

            if (newMarkers != null) {
                mParent.mMarkers.clear();
                mParent.mMarkers.addAll(newMarkers);
            }

            mParent.mDisplayedClusters.set(mClusters);
            mParent.mZoom = zoom;

            markerModifier.close();
            mCallback.run();
        }
    }

    @Override
    public void onClustersChanged(Set<? extends Cluster<T>> clusters) {
        if (Log.isLoggable(TAG, Log.DEBUG)) {
            Log.d(TAG, "clusters changed");
        }

        mCurrentClusters = clusters;
        mViewModifier.queueZoomed(clusters);
    }

    @Override
    public void onCameraChange(CameraPosition cameraPosition) {
        if (mCurrentClusters != null && mPreviousCameraPosition != null && mPreviousCameraPosition.zoom == cameraPosition.zoom) {
            mViewModifier.queuePanned(mCurrentClusters);
        }

        mPreviousCameraPosition = cameraPosition;
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
    private static class MarkerModifier<T extends ClusterItem> extends Handler {

        private static final int MSG_WORK = 0;
        private static final int MSG_ADD_ITEM_MARKER = 1;
        private static final int MSG_ADD_MARKERPOSITION = 2;
        private static final int MSG_ADD_CLUSTER_MARKER = 3;
        private static final int MSG_DECREMENT_BUSY = 4;

        private static final String DATA_ANIMATE_FROM = "animateFrom";

        private final DefaultClusterRenderer<T> mParent;
        private final ReentrantLock lock = new ReentrantLock();
        private final Condition busyCondition = lock.newCondition();
        private final AtomicInteger mBusyRunners = new AtomicInteger(0);

        private Queue<CreateMarkerRunnable> mCreateMarkerRunnables = new LinkedList<>();
        private Queue<CreateMarkerRunnable> mOnScreenCreateMarkerRunnables = new LinkedList<>();
        private Queue<Marker> mRemoveMarkerTasks = new LinkedList<>();
        private Queue<Marker> mOnScreenRemoveMarkerTasks = new LinkedList<>();
        private Queue<DefaultClusterRenderer.AnimationTask> mAnimationTasks = new LinkedList<>();

        private final HandlerThread mBackgroundThread;
        private final Handler mBackgroundHandler;
        private final Set<MarkerWithPosition> mNewMarkers;

        private MarkerModifier(DefaultClusterRenderer<T> parent) {
            super(Looper.getMainLooper());

            mParent = parent;

            mBackgroundThread = new HandlerThread("MarkerModifier-Background", Process.THREAD_PRIORITY_BACKGROUND);
            mBackgroundThread.start();
            mBackgroundHandler = new Handler(mBackgroundThread.getLooper());

            // Create the new markers and animate them to their new positions.
            mNewMarkers = Collections.newSetFromMap(new ConcurrentHashMap<MarkerWithPosition, Boolean>());
        }

        public void close() {
            mBackgroundThread.quit();
        }

        /**
         * Creates markers for a cluster some time in the future.
         *
         * @param priority whether this operation should have priority.
         */
        public void add(boolean priority, CreateMarkerRunnable c) {
            lock.lock();
            if (priority) {
                mOnScreenCreateMarkerRunnables.add(c);
            } else {
                mCreateMarkerRunnables.add(c);
            }
            sendEmptyMessage(MSG_WORK);
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
            if (priority) {
                mOnScreenRemoveMarkerTasks.add(m);
            } else {
                mRemoveMarkerTasks.add(m);
            }
            sendEmptyMessage(MSG_WORK);
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
            mAnimationTasks.add(mParent.new AnimationTask(marker, from, to));
            sendEmptyMessage(MSG_WORK);
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
            DefaultClusterRenderer.AnimationTask animationTask = mParent.new AnimationTask(marker, from, to);
            animationTask.removeOnAnimationComplete(mParent.mClusterManager.getMarkerManager());
            mAnimationTasks.add(animationTask);
            sendEmptyMessage(MSG_WORK);
            lock.unlock();
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_ADD_ITEM_MARKER: {
                    final Pair<T, MarkerOptions> pair = (Pair<T, MarkerOptions>) msg.obj;
                    final T item = pair.first;
                    final MarkerOptions opt = pair.second;
                    final LatLng animateFrom = msg.getData().getParcelable(DATA_ANIMATE_FROM);

                    Marker marker = mParent.mClusterManager.getMarkerCollection().addMarker(opt, item);
                    if (marker != null) {
                        MarkerWithPosition markerWithPosition = new MarkerWithPosition(marker);

                        if (animateFrom != null) {
                            animate(markerWithPosition, animateFrom, item.getPosition());
                        }

                        handleAfterMarkerAdded(item, markerWithPosition);
                    }
                }
                break;

                case MSG_ADD_CLUSTER_MARKER: {
                    final Pair<Cluster<T>, MarkerOptions> pair = (Pair<Cluster<T>, MarkerOptions>) msg.obj;
                    final Cluster<T> cluster = pair.first;
                    final MarkerOptions opt = pair.second;
                    final LatLng animateFrom = msg.getData().getParcelable(DATA_ANIMATE_FROM);

                    Marker marker = mParent.mClusterManager.getClusterMarkerCollection().addMarker(opt);
                    mParent.mMarkerToCluster.put(marker, cluster);
                    mParent.mClusterToMarker.put(cluster, marker);
                    MarkerWithPosition markerWithPosition = new MarkerWithPosition(marker);

                    if (animateFrom != null) {
                        animate(markerWithPosition, animateFrom, cluster.getPosition());
                    }

                    mParent.onClusterRendered(cluster, marker);
                    mNewMarkers.add(markerWithPosition);

                }
                break;

                case MSG_ADD_MARKERPOSITION:
                    final Pair<T, Marker> item = (Pair<T, Marker>) msg.obj;
                    handleAfterMarkerAdded(item.first, new MarkerWithPosition(item.second));
                    break;

                case MSG_DECREMENT_BUSY:
                    lock.lock();
                    mBusyRunners.decrementAndGet();
                    lock.unlock();
                    break;

                case MSG_WORK:
                    lock.lock();
                    try {
                        performNextTask();

                        if (!isBusy()) {
                            // Signal any other threads that are waiting.
                            busyCondition.signalAll();

                            // clean up left over tasks
                            removeMessages(MSG_WORK);
                        }
                    } finally {
                        lock.unlock();
                    }
                    break;
            }
        }

        private void handleAfterMarkerAdded(T item, MarkerWithPosition markerWithPosition) {
            mParent.onClusterItemRendered(item, markerWithPosition.marker);
            mNewMarkers.add(markerWithPosition);
        }

        /**
         * Perform the next task. Prioritise any on-screen work.
         */
        private void performNextTask() {

            // Consider only performing 10 remove tasks, not adds and animations.
            // Removes are relatively slow and are much better when batched.
            int batch = mParent.mBatchMapTask;

            if (!mOnScreenRemoveMarkerTasks.isEmpty()) {
                while (!mOnScreenRemoveMarkerTasks.isEmpty() && batch-- > 0) {
                    removeMarker(mOnScreenRemoveMarkerTasks.poll());
                }
            } else if (!mAnimationTasks.isEmpty()) {
                mAnimationTasks.poll().perform();
            } else if (!mOnScreenCreateMarkerRunnables.isEmpty()) {
                mBackgroundHandler.post(mOnScreenCreateMarkerRunnables.poll());
            } else if (!mCreateMarkerRunnables.isEmpty()) {
                mBackgroundHandler.post(mCreateMarkerRunnables.poll());
            } else if (!mRemoveMarkerTasks.isEmpty()) {
                while (!mRemoveMarkerTasks.isEmpty() && batch-- > 0) {
                    removeMarker(mRemoveMarkerTasks.poll());
                }
            }
        }

        private void removeMarker(Marker m) {
            final Cluster cluster = (Cluster) mParent.mMarkerToCluster.get(m);
            mParent.mClusterToMarker.remove(cluster);
            mParent.mMarkerToCluster.remove(m);
            mParent.mClusterManager.getMarkerManager().remove(m);
        }

        public Marker getMarker(T item) {
            return mParent.mClusterManager.getMarkerCollection().getMarker(item);
        }

        public Set<MarkerWithPosition> getNewMarkers() {
            return mNewMarkers;
        }

        public void addMarker(T item, MarkerOptions markerOptions, LatLng animateFrom) {
            final Message message = obtainMessage(MSG_ADD_ITEM_MARKER, new Pair<>(item, markerOptions));
            message.getData().putParcelable(DATA_ANIMATE_FROM, animateFrom);
            message.sendToTarget();
        }

        public void addMarkerPosition(ClusterItem item, Marker marker) {
            obtainMessage(MSG_ADD_MARKERPOSITION, new Pair<>(item, marker)).sendToTarget();
        }

        public void addMarker(Cluster cluster, MarkerOptions markerOptions, LatLng animateFrom) {
            final Message message = obtainMessage(MSG_ADD_CLUSTER_MARKER, new Pair<>(cluster, markerOptions));
            message.getData().putParcelable(DATA_ANIMATE_FROM, animateFrom);
            message.sendToTarget();
        }

        public void signalRunnerStarted() {
            lock.lock();
            mBusyRunners.incrementAndGet();
            lock.unlock();
        }

        public void signalRunnerFinished() {
            sendEmptyMessage(MSG_DECREMENT_BUSY);
        }

        /**
         * @return true if there is still work to be processed.
         */
        public boolean isBusy() {
            try {
                lock.lock();
                return !(mCreateMarkerRunnables.isEmpty() && mOnScreenCreateMarkerRunnables.isEmpty() &&
                        mOnScreenRemoveMarkerTasks.isEmpty() && mRemoveMarkerTasks.isEmpty() &&
                        mAnimationTasks.isEmpty() && mBusyRunners.get() == 0
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
                lock.lock();
                try {
                    if (isBusy()) {
                        busyCondition.await(1, TimeUnit.SECONDS);
                    }
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                } finally {
                    lock.unlock();
                }
            }
        }
    }

    /**
     * Called before the marker for a ClusterItem is added to the map. The marker appearance can be customized here.
     *
     * <p>The default implementation doesn't change the default marker.</p>
     *
     * <p>This method will be called on a background thread</p>
     */
    protected void onBeforeClusterItemRendered(T item, MarkerOptions markerOptions) {
    }

    /**
     * Called before the marker for a Cluster is added to the map. The marker appearance can be customized here.
     *
     * <p>The default implementation draws a circle with a rough count of the number of items.</p>
     *
     * <p>This method will be called on a background thread</p>
     */
    protected void onBeforeClusterRendered(Cluster<T> cluster, MarkerOptions markerOptions) {
        int bucket = getBucket(cluster);
        BitmapDescriptor descriptor = mIcons.get(bucket);
        if (descriptor == null || BuildConfig.DEBUG) {
            mColoredCircleBackground.getPaint().setColor(getColor(bucket));
            descriptor = BitmapDescriptorFactory.fromBitmap(mIconGenerator.makeIcon(getClusterText(bucket)));
            mIcons.put(bucket, descriptor);
        }

        markerOptions.anchor(0.5f, 0.5f);
        markerOptions.icon(descriptor);
    }

    /**
     * Called after the marker for a Cluster has been added to the map.
     *
     * <p>This method will be called on the main thread</p>
     */
    protected void onClusterRendered(Cluster<T> cluster, Marker marker) {
    }

    /**
     * Called after the marker for a ClusterItem has been added to the map.
     *
     * <p>This method will be called on the main thread</p>
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

    /**
     * Check whether there is a marker for a cluster.
     *
     * @param cluster the cluster to check for
     * @return <code>true</code> if there is a marker on the map for the supplied cluster, <code>false</code> otherwise
     */
    public boolean hasMarker(Cluster<T> cluster) {
        return mClusterToMarker.containsKey(cluster);
    }

    @Override
    @Nullable
    public Cluster<T> getCluster(Marker marker) {
        return mMarkerToCluster.get(marker);
    }

    /**
     * Creates markerWithPosition(s) for a particular cluster, animating it if necessary.
     */
    private static class CreateMarkerRunnable<T extends ClusterItem> implements Runnable {

        private final MarkerModifier<T> mMarkerModifier;
        private final DefaultClusterRenderer<T> mParent;
        private final Cluster<T> cluster;
        private final LatLng animateFrom;

        /**
         * @param markerModifier
         * @param parent
         * @param c              the cluster to render.
         * @param animateFrom    the location to animate the markerWithPosition from, or null if no animation is required.
         */
        public CreateMarkerRunnable(MarkerModifier<T> markerModifier, DefaultClusterRenderer<T> parent, Cluster<T> c, LatLng animateFrom) {
            mMarkerModifier = markerModifier;
            mParent = parent;
            this.cluster = c;
            this.animateFrom = animateFrom;

            // signal work is to be done as early as possible
            mMarkerModifier.signalRunnerStarted();
        }

        public void run() {
            try {
                // Don't show small clusters. Render the markers inside, instead.
                if (!mParent.shouldRenderAsCluster(cluster)) {

                    for (T item : cluster.getItems()) {
                        // Clusters might have changed already. Don't render markers that are clustered.
                        if (!mParent.isItemClusteredVisually(item)) {

                            Marker marker = mMarkerModifier.getMarker(item);
                            if (marker == null) {
                                MarkerOptions markerOptions = new MarkerOptions();
                                if (animateFrom != null) {
                                    markerOptions.position(animateFrom);
                                } else {
                                    markerOptions.position(item.getPosition());
                                }

                                mParent.onBeforeClusterItemRendered(item, markerOptions);

                                mMarkerModifier.addMarker(item, markerOptions, animateFrom);
                            } else {
                                mMarkerModifier.addMarkerPosition(item, marker);
                            }
                        }
                    }
                } else {
                    MarkerOptions markerOptions = new MarkerOptions().
                            position(animateFrom == null ? cluster.getPosition() : animateFrom);

                    mParent.onBeforeClusterRendered(cluster, markerOptions);

                    mMarkerModifier.addMarker(cluster, markerOptions, animateFrom);
                }
            } finally {
                mMarkerModifier.signalRunnerFinished();
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

            if (other instanceof LatLng) {
                return position.equals(other);
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
