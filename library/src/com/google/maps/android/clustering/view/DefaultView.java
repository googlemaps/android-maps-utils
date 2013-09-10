package com.google.maps.android.clustering.view;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.util.SparseArray;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.SphericalUtil;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterItem;
import com.google.maps.android.ui.BubbleIconFactory;
import com.google.maps.android.ui.TextIconGenerator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class DefaultView<T extends ClusterItem> implements ClusterView<T> {
    private final GoogleMap mMap;
    private float mZoom;
    private final TextIconGenerator mBubbleIconFactory;

    /**
     * Markers that are currently on the map.
     */
    private Set<Marker> mMarkers = new HashSet<Marker>();

    private SparseArray<BitmapDescriptor> mIcons = new SparseArray<BitmapDescriptor>();
    private Set<? extends Cluster<T>> mClusters;
    private MarkerCache mMarkerCache = new MarkerCache();

    /**
     * If cluster size is less than this size, display individual markers.
     */
    public static final int MIN_CLUSTER_SIZE = 4;

    public DefaultView(Context context, GoogleMap map) {
        mMap = map;
        mBubbleIconFactory = new TextIconGenerator(context);
        mBubbleIconFactory.setStyle(TextIconGenerator.STYLE_BLUE);
    }

    private boolean onCreateCluster(Cluster<T> cluster, Collection<Marker> markersAdded, LatLng animateFrom, boolean visible) {
        if (cluster.getSize() <= MIN_CLUSTER_SIZE) {
            for (T item : cluster.getItems()) {
                Marker marker = mMarkerCache.get(item);
                if (marker == null) {
                    marker = mMap.addMarker(item.getMarkerOptions());
                    mMarkerCache.put(item, marker);
                    if (animateFrom != null) {
                        marker.setVisible(visible);
                        animate(marker, animateFrom, marker.getPosition());
                    }
                }
                markersAdded.add(marker);
            }
            return false;
        }

        Marker marker = mMap.addMarker(new MarkerOptions().
                icon(getIcon(cluster)).
                title("Items: " + cluster.getSize()).
                position(cluster.getPosition()));

        if (animateFrom != null) {
            animate(marker, animateFrom, marker.getPosition());
        }
        markersAdded.add(marker);
        marker.setVisible(visible);
        return true;
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

    @SuppressLint("NewApi")
    @Override
    public void onClustersChanged(Set<? extends Cluster<T>> clusters) {
        if (clusters.equals(mClusters)) {
            return;
        }
        float zoom = mMap.getCameraPosition().zoom;
        boolean zoomingIn = zoom > mZoom;

        Set<Marker> oldMarkers = mMarkers;
        LatLngBounds visibleBounds = mMap.getProjection().getVisibleRegion().latLngBounds;
        // TODO: Add some padding.
        List<LatLng> existingClustersOnScreen = new ArrayList<LatLng>();
        if (mClusters != null) {
            for (Cluster<T> c : mClusters) {
                if (visibleBounds.contains(c.getPosition()) && c.getSize() > MIN_CLUSTER_SIZE) {
                    existingClustersOnScreen.add(c.getPosition());
                }
            }
        }

        final Set<Marker> newMarkers = new HashSet<Marker>();
        for (Cluster<T> c : clusters) {
            if (zoomingIn && visibleBounds.contains(c.getPosition())) {
                LatLng closest = findClosestCluster(existingClustersOnScreen, c.getPosition());
                if (closest != null) {
                    // TODO: only animate a limited distance. Otherwise Markers fly out of screen.
                    onCreateCluster(c, newMarkers, closest, true);
                } else {
                    onCreateCluster(c, newMarkers, null, true);
                }
            } else {
                onCreateCluster(c, newMarkers, null, zoomingIn);
            }
        }

        oldMarkers.removeAll(newMarkers);

        final List<Marker> animatedOldClusters = new ArrayList<Marker>();
        List<LatLng> newClustersOnScreen = new ArrayList<LatLng>();
        for (Cluster<T> c : clusters) {
            if (visibleBounds.contains(c.getPosition()) && c.getSize() > MIN_CLUSTER_SIZE) {
                newClustersOnScreen.add(c.getPosition());
            }
        }
        for (Marker marker : oldMarkers) {
            if (!zoomingIn && visibleBounds.contains(marker.getPosition())) {
                LatLng closest = findClosestCluster(newClustersOnScreen, marker.getPosition());
                if (closest != null) {
                    animate(marker, marker.getPosition(), closest);
                    animatedOldClusters.add(marker);
                } else {
                    mMarkerCache.remove(marker);
                    marker.remove();
                }
            } else {
                mMarkerCache.remove(marker);
                marker.remove();
            }
        }

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
            onAnimationComplete(animatedOldClusters, newMarkers);
        } else if (!zoomingIn && !animatedOldClusters.isEmpty()) {
            // After the animation is complete, remove the animated markers.

            ValueAnimator valueAnimator = ValueAnimator.ofFloat(0, 1);
            valueAnimator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    onAnimationComplete(animatedOldClusters, newMarkers);
                }
            });
            valueAnimator.start();
        }

        mMarkers = newMarkers;
        mClusters = clusters;
        mZoom = zoom;
    }

    private void onAnimationComplete(List<Marker> animatedOldClusters, Set<Marker> newMarkers) {
        for (Marker m : animatedOldClusters) {
            mMarkerCache.remove(m);
            m.remove();
        }
        for (Marker m : newMarkers) {
            m.setVisible(true);
        }
    }

    private LatLng findClosestCluster(List<LatLng> markers, LatLng point) {
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

    private class MarkerCache {
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

    @SuppressLint("NewApi")
    public static void animate(final Marker marker, final LatLng from, final LatLng to) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
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