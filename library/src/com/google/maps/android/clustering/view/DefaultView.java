package com.google.maps.android.clustering.view;

import android.content.Context;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterItem;
import com.google.maps.android.ui.BubbleIconFactory;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class DefaultView<T extends ClusterItem> implements ClusterView<T> {
    private final GoogleMap mMap;
    private final BubbleIconFactory mBubbleIconFactory;
    private final Collection<Marker> mMarkers = new HashSet<Marker>();

    public DefaultView(Context context, GoogleMap map) {
        mMap = map;
        mBubbleIconFactory = new BubbleIconFactory(context);
        mBubbleIconFactory.setStyle(BubbleIconFactory.Style.BLUE);
    }

    private Marker onCreateCluster(Cluster<T> cluster) {
        if (cluster.getSize() == 1) {
            for (ClusterItem item : cluster.getItems()) {
                return mMap.addMarker(item.getMarkerOptions());
            }
        }

        BitmapDescriptor icon = getBitmapDescriptor(cluster);
        return mMap.addMarker(new MarkerOptions().
                icon(icon).
                title(getTitle(cluster)).
                position(cluster.getPosition()));
    }

    private String getTitle(Cluster cluster) {
        return "Items: " + cluster.getSize();
    }

    private BitmapDescriptor getBitmapDescriptor(Cluster cluster) {
        return BitmapDescriptorFactory.fromBitmap(mBubbleIconFactory.makeIcon(cluster.getSize() + ""));
    }

    @Override
    public void onClustersChanged(Set<? extends Cluster<T>> clusters) {
        for (Marker m : mMarkers) {
            m.remove();
        }
        mMarkers.clear();
        for (Cluster c : clusters) {
            mMarkers.add(onCreateCluster(c));
        }
    }
}