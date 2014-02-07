package com.google.maps.android.clustering.algo;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterItem;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * A cluster whose center is determined upon creation.
 */
public class StaticCluster<T extends ClusterItem> implements Cluster<T> {
    private final LatLng mCenter;
    private final List<T> mItems = new ArrayList<T>();

    public StaticCluster(LatLng center) {
        mCenter = center;
    }

    public boolean add(T t) {
        return mItems.add(t);
    }

    @Override
    public LatLng getPosition() {
        return mCenter;
    }

    public boolean remove(T t) {
        return mItems.remove(t);
    }

    @Override
    public Collection<T> getItems() {
        return mItems;
    }

    @Override
    public int getSize() {
        return mItems.size();
    }
    
    private static boolean isEqual(double d0, double d1) {
        final double epsilon = 0.0000001;
        return Math.abs(d0 - d1) < epsilon;
    }
    
    private static boolean isEqualPosition(LatLng position0, LatLng position1) {
    	return isEqual(position0.latitude, position1.latitude) && 
    		   isEqual(position0.longitude, position1.longitude);
    }
    
    @Override
	public boolean isOneLocation() {
    	LatLng position = null;
    	for (T item : mItems) {
    		if (position == null || isEqualPosition(item.getPosition(), position)) {
    			position = item.getPosition();
    		} else {
    			position = null;
    			break;
    		}
		}
    	
    	return position != null;
    }

    @Override
    public String toString() {
        return "StaticCluster{" +
                "mCenter=" + mCenter +
                ", mItems.size=" + mItems.size() +
                '}';
    }
}