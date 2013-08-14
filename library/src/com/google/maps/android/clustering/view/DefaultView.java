package com.google.maps.android.clustering.view;

import android.content.Context;
import android.util.SparseArray;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterItem;
import com.google.maps.android.ui.BubbleIconFactory;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class DefaultView<T extends ClusterItem> implements ClusterView<T> {
    private final GoogleMap mMap;
    private final BubbleIconFactory mBubbleIconFactory;

    // TODO: use weak references or bidi map.
    private Map<T, Marker> mMarkerCache = new HashMap<T, Marker>();
    private Map<Marker, T> mMarkerCacheReverse = new HashMap<Marker, T>();

    /**
     * Markers that are currently on the map.
     */
    private Set<Marker> mMarkers = new HashSet<Marker>();

    private SparseArray<BitmapDescriptor> mIcons = new SparseArray<BitmapDescriptor>();
    private Set<? extends Cluster<T>> mCurrentClusters;

    public DefaultView(Context context, GoogleMap map) {
        mMap = map;
        mBubbleIconFactory = new BubbleIconFactory(context);
        mBubbleIconFactory.setStyle(BubbleIconFactory.Style.BLUE);
    }

    private void onCreateCluster(Cluster<T> cluster) {
        if (cluster.getSize() < 4) {
            for (T item : cluster.getItems()) {
                Marker marker = mMarkerCache.get(item);
                if (marker == null) {
                    marker = mMap.addMarker(item.getMarkerOptions());
                    mMarkerCache.put(item, marker);
                    mMarkerCacheReverse.put(marker, item);
                }
                mMarkers.add(marker);
            }
            return;
        }

        BitmapDescriptor icon = getBitmapDescriptor(cluster);
        Marker marker = mMap.addMarker(new MarkerOptions().
                icon(icon).
                title(getTitle(cluster)).
                position(cluster.getPosition()));
        mMarkers.add(marker);
    }

    private String getTitle(Cluster cluster) {
        return "Items: " + cluster.getSize();
    }

    private BitmapDescriptor getBitmapDescriptor(Cluster cluster) {
        return getIcon(getBucket(cluster.getSize()));
    }

    private BitmapDescriptor getIcon(int bucket) {
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

    @Override
    public void onClustersChanged(Set<? extends Cluster<T>> clusters) {
        if (clusters.equals(mCurrentClusters)) {
            return;
        }
        mCurrentClusters = clusters;
        Set<Marker> oldMarkers = mMarkers;
        mMarkers = new HashSet<Marker>();
        for (Cluster c : clusters) {
            onCreateCluster(c);
        }
        oldMarkers.removeAll(mMarkers);
        for (Marker marker : oldMarkers) {
            T item = mMarkerCacheReverse.get(marker);
            if (item != null) {
                mMarkerCache.remove(item);
                mMarkerCacheReverse.remove(marker);
            }
            marker.remove();
        }
    }
}