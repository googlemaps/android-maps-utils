package com.google.maps.android.clustering.view;

import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.MessageQueue;
import android.util.Log;
import android.util.SparseArray;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.Projection;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.SphericalUtil;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterItem;
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

public class DefaultView<T extends ClusterItem> implements ClusterView<T> {
    private final GoogleMap mMap;
    private final TextIconGenerator mBubbleIconFactory;

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
     * The target zoom level for the current set of clusters.
     */
    private float mZoom;

    private final ViewModifier mViewModifier = new ViewModifier();

    public DefaultView(Context context, GoogleMap map) {
        mMap = map;
        mBubbleIconFactory = new TextIconGenerator(context);
        mBubbleIconFactory.setStyle(TextIconGenerator.STYLE_BLUE);
    }

    private BitmapDescriptor getIcon(Cluster<T> cluster) {
        int bucket = getBucket(cluster.getSize());
        BitmapDescriptor descriptor = mIcons.get(bucket);
        if (descriptor == null) {
            descriptor = BitmapDescriptorFactory.fromBitmap(mBubbleIconFactory.makeIcon(bucket + (bucket >= 10 ? "+" : "")));
            mIcons.put(bucket, descriptor);
        }
        return descriptor;
    }

    private int getBucket(int size) {
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

    private class ViewModifier extends Handler {
        public static final int RUN_TASK = 0;
        public static final int TASK_FINISHED = 1;
        private boolean mViewModificationInProgress = false;
        private ClusterTask mNextClusters = null;

        @Override
        public void handleMessage(Message msg) {
            if (msg.what == TASK_FINISHED) {
                mViewModificationInProgress = false;
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

            ClusterTask clusterTask;
            synchronized (this) {
                clusterTask = mNextClusters;
                mNextClusters = null;
                mViewModificationInProgress = true;
            }

            clusterTask.setCallback(new Runnable() {
                @Override
                public void run() {
                    sendEmptyMessage(TASK_FINISHED);
                }
            });
            clusterTask.setProjection(mMap.getProjection());
            clusterTask.setMapZoom(mMap.getCameraPosition().zoom);
            new Thread(clusterTask).start();
        }

        public void queue(Set<? extends Cluster<T>> clusters) {
            synchronized (this) {
                // Overwrite any pending cluster tasks - we don't care about intermediate states.
                mNextClusters = new ClusterTask(mMap.getCameraPosition().zoom, clusters);
            }
            sendEmptyMessage(RUN_TASK);
        }
    }

    private class ClusterTask implements Runnable {
        final float zoom;
        final Set<? extends Cluster<T>> clusters;
        private Runnable mCallback;
        private Projection mProjection;
        private float mMapZoom;

        private ClusterTask(float zoom, Set<? extends Cluster<T>> clusters) {
            this.zoom = zoom;
            this.clusters = clusters;
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

            final Set<MarkerWithPosition> oldMarkers = mMarkers;
            final LatLngBounds visibleBounds = mProjection.getVisibleRegion().latLngBounds;
            // TODO: Add some padding.
            List<LatLng> existingClustersOnScreen = new ArrayList<LatLng>();
            if (mClusters != null) {
                for (Cluster<T> c : mClusters) {
                    if (visibleBounds.contains(c.getPosition()) && c.getSize() > MIN_CLUSTER_SIZE) {
                        existingClustersOnScreen.add(c.getPosition());
                    }
                }
            }

            // Create the new markers and animate them to their new positions.
            final Set<MarkerWithPosition> newMarkers = new HashSet<MarkerWithPosition>();
            for (Cluster<T> c : clusters) {
                boolean onScreen = visibleBounds.contains(c.getPosition());
                if (zoomingIn && onScreen) {
                    LatLng closest = findClosestCluster(existingClustersOnScreen, c.getPosition());
                    if (closest != null) {
                        // TODO: only animate a limited distance. Otherwise Markers fly out of screen.
                        markerModifier.addOnScreen(c, newMarkers, closest, true);
                    } else {
                        markerModifier.addOnScreen(c, newMarkers, null, true);
                    }
                } else if (onScreen) {
                    markerModifier.addOnScreen(c, newMarkers, null, false);
                } else {
                    markerModifier.add(c, newMarkers, null, zoomingIn);
                }
            }

            // Wait for all markers to be added.
            markerModifier.waitUntilFree();

            oldMarkers.removeAll(newMarkers);

            // Remove the old markers, animating them into clusters if zooming out.
            final List<Marker> animatedOldClusters = new ArrayList<Marker>();
            List<LatLng> newClustersOnScreen = new ArrayList<LatLng>();
            for (Cluster<T> c : clusters) {
                if (visibleBounds.contains(c.getPosition()) && c.getSize() > MIN_CLUSTER_SIZE) {
                    newClustersOnScreen.add(c.getPosition());
                }
            }

            for (final MarkerWithPosition marker : oldMarkers) {
                boolean onScreen = visibleBounds.contains(marker.position);
                if (!zoomingIn && onScreen) {
                    final LatLng closest = findClosestCluster(newClustersOnScreen, marker.position);
                    if (closest != null) {
                        // Animate
                        markerModifier.animate(marker.marker, marker.position, closest);
                        animatedOldClusters.add(marker.marker);
                    } else {
                        markerModifier.removeOnScreen(marker.marker);
                    }
                } else if (onScreen) {
                    markerModifier.removeOnScreen(marker.marker);
                } else {
                    markerModifier.remove(marker.marker);
                }
            }

            markerModifier.waitUntilFree();

            mMarkers = newMarkers;
            mClusters = clusters;
            mZoom = zoom;

            // Remove any of the markers that were combining into a cluster.
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
                onAnimationComplete(animatedOldClusters, newMarkers, markerModifier);
                markerModifier.waitUntilFree();
                mCallback.run();
            } else if (!zoomingIn && !animatedOldClusters.isEmpty()) {
                // After the animation is complete, remove the animated markers.
                // TODO: wait properly, instead of sleeping.
                try {
                    Thread.sleep(1000, 0);
                } catch (InterruptedException e) {
                }
                onAnimationComplete(animatedOldClusters, newMarkers, markerModifier);
                markerModifier.waitUntilFree();
                mCallback.run();
            } else {
                mCallback.run();
            }
        }

        public void setCallback(Runnable callback) {
            mCallback = callback;
        }

        public void setProjection(Projection projection) {
            this.mProjection = projection;
        }

        public void setMapZoom(float zoom) {
            this.mMapZoom = zoom;
        }
    }

    @Override
    public void onClustersChanged(Set<? extends Cluster<T>> clusters) {
        mViewModifier.queue(clusters);
    }

    private void onAnimationComplete(List<Marker> animatedOldClusters, Set<MarkerWithPosition> newMarkers, MarkerModifier markerModifier) {
        for (Marker m : animatedOldClusters) {
            markerModifier.removeOnScreen(m);
        }
        for (MarkerWithPosition m : newMarkers) {
            markerModifier.setVisible(m.marker);
        }
    }

    private static LatLng findClosestCluster(List<LatLng> markers, LatLng point) {
        if (markers.isEmpty()) return null;

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

    private class MarkerModifier extends Handler implements MessageQueue.IdleHandler {
        private static final int BLANK = 0;
        private final Lock lock = new ReentrantLock();
        private final Condition busyCondition = lock.newCondition();
        private Queue<CreateMarkerTask> mCreateMarkerTasks = new LinkedList<CreateMarkerTask>();
        private Queue<CreateMarkerTask> mOnScreenCreateMarkerTasks = new LinkedList<CreateMarkerTask>();
        private Queue<Marker> mRemoveMarkerTasks = new LinkedList<Marker>();
        private Queue<Marker> mOnScreenRemoveMarkerTasks = new LinkedList<Marker>();
        private Queue<Marker> mSetVisibleTasks = new LinkedList<Marker>();
        private boolean mListenerAdded;
        private Queue<AnimationTask> mAnimationTasks = new LinkedList<AnimationTask>();


        private MarkerModifier() {
            super(Looper.getMainLooper());
        }

        public void add(Cluster<T> c, Set<MarkerWithPosition> newMarkers, LatLng closest, boolean visible) {
            lock.lock();
            sendEmptyMessage(BLANK);
            mCreateMarkerTasks.add(new CreateMarkerTask(c, newMarkers, closest, visible));
            lock.unlock();
        }

        public void addOnScreen(Cluster<T> c, Set<MarkerWithPosition> newMarkers, LatLng closest, boolean visible) {
            lock.lock();
            sendEmptyMessage(BLANK);
            mCreateMarkerTasks.add(new CreateMarkerTask(c, newMarkers, closest, visible));
            lock.unlock();
        }

        public void remove(Marker m) {
            lock.lock();
            sendEmptyMessage(BLANK);
            mRemoveMarkerTasks.add(m);
            lock.unlock();
        }

        public void removeOnScreen(Marker m) {
            lock.lock();
            sendEmptyMessage(BLANK);
            mOnScreenRemoveMarkerTasks.add(m);
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

                if (!mOnScreenCreateMarkerTasks.isEmpty()) {
                    CreateMarkerTask task = mOnScreenCreateMarkerTasks.poll();
                    task.perform(this);
                } else if (!mCreateMarkerTasks.isEmpty()) {
                    CreateMarkerTask task = mCreateMarkerTasks.poll();
                    task.perform(this);
                } else if (!mOnScreenRemoveMarkerTasks.isEmpty()) {
                    Marker m = mOnScreenRemoveMarkerTasks.poll();
                    mMarkerCache.remove(m);
                    m.remove();
                } else if (!mRemoveMarkerTasks.isEmpty()) {
                    Marker m = mRemoveMarkerTasks.poll();
                    mMarkerCache.remove(m);
                    m.remove();
                } else if (!mSetVisibleTasks.isEmpty()) {
                    Marker m = mSetVisibleTasks.poll();
                    m.setVisible(true);
                } else if (!mAnimationTasks.isEmpty()) {
                    AnimationTask animationTask = mAnimationTasks.poll();
                    animationTask.perform();
                }

                if (!isBusy()) {
                    mListenerAdded = false;
                    Looper.myQueue().removeIdleHandler(this);
                } else {
                    sendEmptyMessageDelayed(BLANK, 10);
                }
                busyCondition.signalAll();
            } finally {
                lock.unlock();
            }
        }

        public boolean isBusy() {
            return !(mCreateMarkerTasks.isEmpty() && mOnScreenCreateMarkerTasks.isEmpty() &&
                    mOnScreenRemoveMarkerTasks.isEmpty() && mRemoveMarkerTasks.isEmpty() &&
                    mSetVisibleTasks.isEmpty() && mAnimationTasks.isEmpty()
            );
        }

        public void waitUntilFree() {
            while (isBusy()) {
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
            sendEmptyMessage(BLANK);
            return true;
        }

        public void setVisible(final Marker m) {
            lock.lock();
            sendEmptyMessage(BLANK);
            mSetVisibleTasks.add(m);
            lock.unlock();
        }

        public void animate(Marker marker, LatLng from, LatLng to) {
            mAnimationTasks.add(new AnimationTask(marker, from, to));
        }
    }

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

    private class CreateMarkerTask {
        private final boolean visible;
        private final Cluster<T> cluster;
        private final Set<MarkerWithPosition> newMarkers;
        private final LatLng animateFrom;

        public CreateMarkerTask(Cluster<T> c, Set<MarkerWithPosition> markersAdded, LatLng animateFrom, boolean visible) {
            this.cluster = c;
            this.newMarkers = markersAdded;
            this.animateFrom = animateFrom;
            this.visible = visible;
        }

        private void perform(MarkerModifier markerModifier) {
            if (cluster.getSize() <= MIN_CLUSTER_SIZE) {
                for (T item : cluster.getItems()) {
                    Marker marker = mMarkerCache.get(item);
                    if (marker == null) {
                        marker = mMap.addMarker(item.getMarkerOptions());
                        mMarkerCache.put(item, marker);
                        if (animateFrom != null) {
                            marker.setVisible(visible);
                            markerModifier.animate(marker, animateFrom, marker.getPosition());
                        }
                    }
                    newMarkers.add(new MarkerWithPosition(marker));
                }
                return;
            }

            Marker marker = mMap.addMarker(new MarkerOptions().
                    icon(getIcon(cluster)).
                    title("Items: " + cluster.getSize()).
                    position(cluster.getPosition()));

            if (animateFrom != null) {
                markerModifier.animate(marker, animateFrom, marker.getPosition());
            }
            newMarkers.add(new MarkerWithPosition(marker));
            marker.setVisible(visible);
        }
    }

    private static class MarkerWithPosition {
        private final Marker marker;
        private final LatLng position;

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

    private static class AnimationTask {
        private final Marker marker;
        private final LatLng from;
        private final LatLng to;

        private AnimationTask(Marker marker, LatLng from, LatLng to) {
            this.marker = marker;
            this.from = from;
            this.to = to;
        }

        @SuppressLint("NewApi")
        public void perform() {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
                marker.setPosition(to);
                return;
            }
            ValueAnimator valueAnimator = ValueAnimator.ofFloat(0, 1);
            valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator valueAnimator) {
                    marker.setPosition(SphericalUtil.interpolate(from, to, valueAnimator.getAnimatedFraction()));
                }
            });
            valueAnimator.start();
        }
    }
}