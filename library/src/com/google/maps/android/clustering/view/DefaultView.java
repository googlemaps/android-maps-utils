package com.google.maps.android.clustering.view;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.TimeInterpolator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.MessageQueue;
import android.util.SparseArray;
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
import com.google.maps.android.SphericalUtil;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterItem;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.ui.TextIconGenerator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * The default view for a ClusterManager. Markers are animated in and out of clusters.
 */
public class DefaultView<T extends ClusterItem> implements ClusterView<T> {
    public static final boolean SHOULD_ANIMATE = Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB;
    private final GoogleMap mMap;
    private final TextIconGenerator mBubbleIconFactory;
    private final ClusterManager<T> mClusterManager;

    /**
     * Markers that are currently on the map.
     */
    private Set<MarkerWithPosition> mMarkers = new HashSet<MarkerWithPosition>();

    /**
     * Icons for each bucket.
     */
    private SparseArray<BitmapDescriptor> mIcons = new SparseArray<BitmapDescriptor>();

    /**
     * Markers for single ClusterItems.
     */
    private MarkerCache<T> mMarkerCache = new MarkerCache<T>();

    /**
     * If cluster size is less than this size, display individual markers.
     */
    public static final int MIN_CLUSTER_SIZE = 4;

    /**
     * The currently displayed set of clusters.
     */
    private Set<? extends Cluster<T>> mClusters;

    /**
     * Lookup between markers and the associated cluster.
     */
    private Map<Marker, Cluster<T>> mMarkerToCluster = new HashMap<Marker, Cluster<T>>();

    /**
     * The target zoom level for the current set of clusters.
     */
    private float mZoom;

    private final ViewModifier mViewModifier = new ViewModifier();

    private ClusterManager.OnClusterClickListener<T> mClickListener;
    private ClusterManager.OnClusterItemClickListener<T> mItemClickListener;

    public DefaultView(Context context, GoogleMap map, ClusterManager<T> clusterManager) {
        mMap = map;
        mBubbleIconFactory = new TextIconGenerator(context);
        mBubbleIconFactory.setStyle(TextIconGenerator.STYLE_BLUE);
        mClusterManager = clusterManager;

        clusterManager.getMarkerCollection().setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                return mItemClickListener != null && mItemClickListener.onClusterItemClick(mMarkerCache.get(marker));
            }
        });

        clusterManager.getClusterMarkerCollection().setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                return mClickListener != null && mClickListener.onClusterClick(mMarkerToCluster.get(marker));
            }
        });
    }

    /**
     * Produce an icon for a particular cluster. Subclasses should override this to customize the
     * displayed cluster markerWithPosition.
     */
    protected BitmapDescriptor getIcon(Cluster<T> cluster) {
        int bucket = getBucket(cluster.getSize());
        BitmapDescriptor descriptor = mIcons.get(bucket);
        if (descriptor == null) {
            descriptor = BitmapDescriptorFactory.fromBitmap(mBubbleIconFactory.makeIcon(bucket + (bucket >= 10 ? "+" : "")));
            mIcons.put(bucket, descriptor);
        }
        return descriptor;
    }

    /**
     * Gets the "bucket" for a particular cluster size.
     */
    protected int getBucket(int size) {
        if (size < 10) {
            return size;
        }
        if (size < 20) {
            return 10;
        }
        if (size < 40) {
            return 20;
        }
        if (size < 100) {
            return 40;
        }
        if (size < 200) {
            return 100;
        }
        if (size < 500) {
            return 200;
        }
        return 500;
    }

    /**
     * ViewModifier ensures only one re-rendering of the view occurs at a time, and schedules
     * re-rendering, which is performed by the RenderTask.
     */
    private class ViewModifier extends Handler {
        public static final int RUN_TASK = 0;
        public static final int TASK_FINISHED = 1;
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

            RenderTask renderTask;
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
            renderTask.setProjection(mMap.getProjection());
            renderTask.setMapZoom(mMap.getCameraPosition().zoom);
            new Thread(renderTask).start();
        }

        public void queue(Set<? extends Cluster<T>> clusters) {
            synchronized (this) {
                // Overwrite any pending cluster tasks - we don't care about intermediate states.
                mNextClusters = new RenderTask(mMap.getCameraPosition().zoom, clusters);
            }
            sendEmptyMessage(RUN_TASK);
        }
    }

    /**
     * Determine whether the cluster should be rendered as individual markers or a cluster.
     */
    private static boolean shouldRenderAsCluster(Cluster cluster) {
        return cluster.getSize() > MIN_CLUSTER_SIZE;
    }

    /**
     * Transforms the current view (represented by DefaultView.mClusters and DefaultView.mZoom) to a
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
        final float zoom;
        final Set<? extends Cluster<T>> clusters;
        private Runnable mCallback;
        private Projection mProjection;
        private float mMapZoom;

        private RenderTask(float zoom, Set<? extends Cluster<T>> clusters) {
            this.zoom = zoom;
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
        }

        @SuppressLint("NewApi")
        public void run() {
            if (clusters.equals(mClusters)) {
                mCallback.run();
                return;
            }

            final MarkerModifier markerModifier = new MarkerModifier();

            final float zoom = mMapZoom;
            final boolean zoomingIn = zoom > mZoom;
            final float zoomDelta = zoom - mZoom;

            final Set<MarkerWithPosition> markersToRemove = mMarkers;
            final LatLngBounds visibleBounds = mProjection.getVisibleRegion().latLngBounds;
            // TODO: Add some padding, so that markers can animate in from off-screen.

            // Find all of the existing clusters that are on-screen. These are candidates for
            // markers to animate from.
            List<LatLng> existingClustersOnScreen = null;
            if (mClusters != null && SHOULD_ANIMATE) {
                existingClustersOnScreen = new ArrayList<LatLng>();
                for (Cluster<T> c : mClusters) {
                    if (shouldRenderAsCluster(c) && visibleBounds.contains(c.getPosition())) {
                        existingClustersOnScreen.add(c.getPosition());
                    }
                }
            }

            // Create the new markers and animate them to their new positions.
            final Set<MarkerWithPosition> newMarkers = new HashSet<MarkerWithPosition>();
            for (Cluster<T> c : clusters) {
                boolean onScreen = visibleBounds.contains(c.getPosition());
                if (zoomingIn && onScreen && SHOULD_ANIMATE) {
                    LatLng closest = findClosestCluster(existingClustersOnScreen, c.getPosition());
                    if (closest != null) {
                        // TODO: only animate a limited distance. Otherwise Markers fly out of screen.
                        markerModifier.add(true, new CreateMarkerTask(c, newMarkers, closest));
                    } else {
                        markerModifier.add(true, new CreateMarkerTask(c, newMarkers, null));
                    }
                } else {
                    markerModifier.add(onScreen, new CreateMarkerTask(c, newMarkers, null));
                }
            }

            // Wait for all markers to be added.
            markerModifier.waitUntilFree();

            // Don't remove any markers that were just added. This is basically anything that had
            // a hit in the MarkerCache.
            markersToRemove.removeAll(newMarkers);

            // Find all of the new clusters that were added on-screen. These are candidates for
            // markers to animate from.
            List<LatLng> newClustersOnScreen = null;
            if (SHOULD_ANIMATE) {
                newClustersOnScreen = new ArrayList<LatLng>();
                for (Cluster<T> c : clusters) {
                    if (shouldRenderAsCluster(c) && visibleBounds.contains(c.getPosition())) {
                        newClustersOnScreen.add(c.getPosition());
                    }
                }
            }

            // Remove the old markers, animating them into clusters if zooming out.
            for (final MarkerWithPosition marker : markersToRemove) {
                boolean onScreen = visibleBounds.contains(marker.position);
                // Don't animate when zooming out more than 3 zoom levels.
                // TODO: drop animation based on speed of device & number of markers to animate.
                if (!zoomingIn && zoomDelta > -3 && onScreen && SHOULD_ANIMATE) {
                    final LatLng closest = findClosestCluster(newClustersOnScreen, marker.position);
                    if (closest != null) {
                        markerModifier.animateThenRemove(marker, marker.position, closest);
                    } else {
                        markerModifier.remove(true, marker.marker);
                    }
                } else {
                    markerModifier.remove(onScreen, marker.marker);
                }
            }

            markerModifier.waitUntilFree();

            mMarkers = newMarkers;
            mClusters = clusters;
            mZoom = zoom;

            mCallback.run();
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
    public void setOnClusterItemClickListener(ClusterManager.OnClusterItemClickListener<T> listener) {
        mItemClickListener = listener;
    }

    private static LatLng findClosestCluster(List<LatLng> markers, LatLng point) {
        if (markers == null || markers.isEmpty()) return null;

        double minDist = SphericalUtil.computeDistanceBetween(markers.get(0), point);
        LatLng closest = markers.get(0);
        for (LatLng latLng : markers) {
            double dist = SphericalUtil.computeDistanceBetween(latLng, point);
            if (dist < minDist) {
                closest = latLng;
                minDist = dist;
            }
        }
        return closest;
    }

    /**
     * Handles all markerWithPosition manipulations on the map. Work (such as adding, removing, or animating a
     * markerWithPosition) is performed while trying not to block the rest of the app's UI.
     */
    private class MarkerModifier extends Handler implements MessageQueue.IdleHandler {
        private static final int BLANK = 0;

        private final Lock lock = new ReentrantLock();
        private final Condition busyCondition = lock.newCondition();

        private Queue<CreateMarkerTask> mCreateMarkerTasks = new LinkedList<CreateMarkerTask>();
        private Queue<CreateMarkerTask> mOnScreenCreateMarkerTasks = new LinkedList<CreateMarkerTask>();
        private Queue<Marker> mRemoveMarkerTasks = new LinkedList<Marker>();
        private Queue<Marker> mOnScreenRemoveMarkerTasks = new LinkedList<Marker>();
        private Queue<AnimationTask> mAnimationTasks = new LinkedList<AnimationTask>();

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
            mAnimationTasks.add(new AnimationTask(marker, from, to));
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
            AnimationTask animationTask = new AnimationTask(marker, from, to);
            animationTask.removeOnAnimationComplete(mMarkerCache, mClusterManager.getMarkerManager());
            mAnimationTasks.add(animationTask);
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
                    performTask();
                }

                if (!isBusy()) {
                    mListenerAdded = false;
                    Looper.myQueue().removeIdleHandler(this);
                } else {
                    // Sometimes the idle queue may not be called - schedule up some work regardless
                    // of whether the UI thread is busy or not.
                    // TODO: try to remove this.
                    sendEmptyMessageDelayed(BLANK, 10);
                }

                // Signal any other threads that are waiting.
                // TODO: only signal when the queue has finished processing.
                busyCondition.signalAll();
            } finally {
                lock.unlock();
            }
        }

        /**
         * Perform the next task. Prioritise any on-screen work.
         */
        private void performTask() {
            if (!mOnScreenRemoveMarkerTasks.isEmpty()) {
                Marker m = mOnScreenRemoveMarkerTasks.poll();
                mMarkerCache.remove(m);
                mClusterManager.getMarkerManager().remove(m);
            } else if (!mAnimationTasks.isEmpty()) {
                AnimationTask animationTask = mAnimationTasks.poll();
                animationTask.perform();
            } else if (!mOnScreenCreateMarkerTasks.isEmpty()) {
                CreateMarkerTask task = mOnScreenCreateMarkerTasks.poll();
                task.perform(this);
            } else if (!mCreateMarkerTasks.isEmpty()) {
                CreateMarkerTask task = mCreateMarkerTasks.poll();
                task.perform(this);
            } else if (!mRemoveMarkerTasks.isEmpty()) {
                Marker m = mRemoveMarkerTasks.poll();
                mMarkerCache.remove(m);
                mClusterManager.getMarkerManager().remove(m);
            }
        }

        /**
         * @return true if there is still work to be processed.
         */
        public boolean isBusy() {
            return !(mCreateMarkerTasks.isEmpty() && mOnScreenCreateMarkerTasks.isEmpty() &&
                    mOnScreenRemoveMarkerTasks.isEmpty() && mRemoveMarkerTasks.isEmpty() &&
                    mAnimationTasks.isEmpty()
            );
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
        private Map<T, Marker> mMarkerCache = new HashMap<T, Marker>();
        private Map<Marker, T> mMarkerCacheReverse = new HashMap<Marker, T>();

        public Marker get(T item) {
            return mMarkerCache.get(item);
        }

        public T get(Marker m) {
            return mMarkerCacheReverse.get(m);
        }

        public void put(T item, Marker m) {
            mMarkerCache.put(item, m);
            mMarkerCacheReverse.put(m, item);
        }

        public void remove(Marker m) {
            T item = mMarkerCacheReverse.get(m);
            mMarkerCacheReverse.remove(m);
            mMarkerCache.remove(item);
        }
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
         * @param animateFrom  the location to animate the markerWithPosition from, or null if no animation is
         *                     required.
         */
        public CreateMarkerTask(Cluster<T> c, Set<MarkerWithPosition> markersAdded, LatLng animateFrom) {
            this.cluster = c;
            this.newMarkers = markersAdded;
            this.animateFrom = animateFrom;
        }

        private void perform(MarkerModifier markerModifier) {
            // Don't show small clusters. Render the markers inside, instead.
            if (!shouldRenderAsCluster(cluster)) {
                for (T item : cluster.getItems()) {
                    Marker marker = mMarkerCache.get(item);
                    MarkerWithPosition markerWithPosition;
                    if (marker == null) {
                        MarkerOptions markerOptions = item.getMarkerOptions();
                        LatLng animateTo = markerOptions.getPosition();
                        if (animateFrom != null) {
                            markerOptions.position(animateFrom);
                        }
                        marker = mClusterManager.getMarkerCollection().addMarker(markerOptions);
                        markerWithPosition = new MarkerWithPosition(marker);
                        mMarkerCache.put(item, marker);
                        if (animateFrom != null) {
                            markerModifier.animate(markerWithPosition, animateFrom, animateTo);
                        }
                    } else {
                        markerWithPosition = new MarkerWithPosition(marker);
                    }
                    newMarkers.add(markerWithPosition);
                }
                return;
            }

            MarkerOptions markerOptions = new MarkerOptions().
                    icon(getIcon(cluster)).
                    title("Items: " + cluster.getSize()).
                    position(animateFrom == null ? cluster.getPosition() : animateFrom);

            Marker marker = mClusterManager.getClusterMarkerCollection().addMarker(markerOptions);
            mMarkerToCluster.put(marker, cluster);
            MarkerWithPosition markerWithPosition = new MarkerWithPosition(marker);
            if (animateFrom != null) {
                markerModifier.animate(markerWithPosition, animateFrom, cluster.getPosition());
            }
            newMarkers.add(markerWithPosition);
        }
    }

    /**
     * A Marker and its position. Marker.getPosition() must be called from the UI thread, so this
     * object allows lookup from other threads.
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

    /**
     * Animates a markerWithPosition from one position to another. TODO: improve performance for slow devices
     * (e.g. Nexus S).
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR1)
    private static class AnimationTask extends AnimatorListenerAdapter implements ValueAnimator.AnimatorUpdateListener {
        private static final TimeInterpolator INTERP = new DecelerateInterpolator();
        private final MarkerWithPosition markerWithPosition;
        private final Marker marker;
        private MarkerCache mMarkerCache;
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
            valueAnimator.setInterpolator(INTERP);
            valueAnimator.addUpdateListener(this);
            valueAnimator.addListener(this);
            valueAnimator.start();
        }

        @Override
        public void onAnimationEnd(Animator animation) {
            if (mRemoveOnComplete) {
                mMarkerCache.remove(marker);
                mMarkerManager.remove(marker);
            }
            markerWithPosition.position = to;
        }

        public void removeOnAnimationComplete(MarkerCache markerCache, MarkerManager markerManager) {
            mMarkerCache = markerCache;
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