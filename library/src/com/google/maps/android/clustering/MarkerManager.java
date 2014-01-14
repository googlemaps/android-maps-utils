package com.google.maps.android.clustering;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.*;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Keeps track of collections of markers on the map. Delegates all Marker-related events to each
 * collection's individually managed listeners.
 * <p/>
 * All marker operations (adds and removes) should occur via its collection class. That is, don't
 * add a marker via a collection, then remove it via Marker.remove()
 */
public class MarkerManager<T extends ClusterItem> implements GoogleMap.OnMarkerClickListener, GoogleMap.OnMarkerDragListener {
    private final GoogleMap mMap;

    private final MarkerCache<T> itemMarkerCache = new MarkerCache<T>();
    private final MarkerCache<Cluster<T>> clusterMarkerCache = new MarkerCache<Cluster<T>>();

    private final Map<String, Collection> mNamedCollections = new HashMap<String, Collection>();
    private final Map<Marker, Collection> mAllMarkers = new HashMap<Marker, Collection>();



    public MarkerManager(GoogleMap map) {
        this.mMap = map;
    }

    /**
     * Gets a named collection that was created by {@link #newCollection(String)}
     * @param id the unique id for this collection.
     */
    public Collection getCollection(String id) {
        return mNamedCollections.get(id);
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        Collection collection = mAllMarkers.get(marker);
        if (collection != null && collection.mMarkerClickListener != null) {
            return collection.mMarkerClickListener.onMarkerClick(marker);
        }
        return false;
    }

    @Override
    public void onMarkerDragStart(Marker marker) {
        Collection collection = mAllMarkers.get(marker);
        if (collection != null && collection.mMarkerDragListener != null) {
            collection.mMarkerDragListener.onMarkerDragStart(marker);
        }
    }

    @Override
    public void onMarkerDrag(Marker marker) {
        Collection collection = mAllMarkers.get(marker);
        if (collection != null && collection.mMarkerDragListener != null) {
            collection.mMarkerDragListener.onMarkerDrag(marker);
        }
    }

    @Override
    public void onMarkerDragEnd(Marker marker) {
        Collection collection = mAllMarkers.get(marker);
        if (collection != null && collection.mMarkerDragListener != null) {
            collection.mMarkerDragListener.onMarkerDragEnd(marker);
        }
    }

    public class Collection {
        private final Set<Marker> mMarkers = new HashSet<Marker>();
        private GoogleMap.OnMarkerClickListener mMarkerClickListener;
        private GoogleMap.OnMarkerDragListener mMarkerDragListener;

        public Collection() {
        }


        public void clear() {
            for (Marker marker : mMarkers) {
                marker.remove();
                mAllMarkers.remove(marker);
            }
            mMarkers.clear();
        }

        public java.util.Collection<Marker> getMarkers() {
            return Collections.unmodifiableCollection(mMarkers);
        }

        public void setOnMarkerClickListener(GoogleMap.OnMarkerClickListener markerClickListener) {
            mMarkerClickListener = markerClickListener;
        }

        public void setOnMarkerDragListener(GoogleMap.OnMarkerDragListener markerDragListener) {
            mMarkerDragListener = markerDragListener;
        }

    }

    public void clear() {
        for(Marker marker : itemMarkerCache.clear()){
            marker.remove();
        }

        for(Marker marker : clusterMarkerCache.clear()){
            marker.remove();
        }
    }

    public Marker putItem(T item, MarkerOptions opts) {
        final Marker marker = mMap.addMarker(opts);
        itemMarkerCache.put(item, marker);
        return marker;
    }

    public Marker putCluster(Cluster<T> cluster, MarkerOptions opts) {
        final Marker marker = mMap.addMarker(opts);
        clusterMarkerCache.put(cluster, marker);
        return marker;
    }

    public Marker getMarkerFor(T item) {
        return itemMarkerCache.get(item);
    }

    public Marker getMarkerFor(Cluster<T> cluster) {
        return clusterMarkerCache.get(cluster);
    }

    public T getItemFor(Marker marker) {
        return itemMarkerCache.get(marker);
    }

    public Cluster<T> getClusterFor(Marker marker) {
        return clusterMarkerCache.get(marker);
    }

    public boolean remove(Marker marker){

        boolean wasRemoved = itemMarkerCache.remove(marker);
        wasRemoved |= clusterMarkerCache.remove(marker);
        marker.remove();
        return wasRemoved;
    }

    /**
     * A cache of markers representing individual ClusterItems.
     */
    public static class MarkerCache<I> {
        private final Map<I, Marker> mCache = new HashMap<I, Marker>();
        private final Map<Marker, I> mCacheReverse = new HashMap<Marker, I>();
        private final ReadWriteLock readWriteLock = new ReentrantReadWriteLock();

        private MarkerCache(){}

        public Marker get(I item) {
            readWriteLock.readLock().lock();
            try{
                return mCache.get(item);
            } finally {
                readWriteLock.readLock().unlock();
            }
        }

        public I get(Marker m) {
            readWriteLock.readLock().lock();
            try{
                return mCacheReverse.get(m);
            } finally {
                readWriteLock.readLock().unlock();
            }
        }

        public void put(I item, Marker m) {
            readWriteLock.writeLock().lock();
            try{
                mCache.put(item, m);
                mCacheReverse.put(m, item);
            } finally {
                readWriteLock.writeLock().unlock();
            }
        }

        public boolean remove(Marker m) {
            readWriteLock.writeLock().lock();
            try{
                I item = mCacheReverse.get(m);
                mCacheReverse.remove(m);
                return mCache.remove(item) == null;
            } finally {
                readWriteLock.writeLock().unlock();
            }
        }

        public  java.util.Collection<Marker> clear() {
            readWriteLock.writeLock().lock();
            try{
                mCacheReverse.clear();
                java.util.Collection<Marker> markers = mCache.values();
                mCache.clear();
                return markers;
            } finally {
                readWriteLock.writeLock().unlock();
            }
        }
    }
}
