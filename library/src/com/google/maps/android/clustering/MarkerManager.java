package com.google.maps.android.clustering;

import com.google.android.gms.maps.model.Marker;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Keeps track of collections of markers on the map. Delegates all Marker-related events to each
 * collection's individually managed listeners.
 * <p/>
 * All marker operations (adds and removes) should occur via its collection class. That is, don't
 * add a marker via a collection, then remove it via Marker.remove()
 */
class MarkerManager<T extends ClusterItem>  {

    private final MarkerCache<T> itemMarkerCache = new MarkerCache<T>();
    private final MarkerCache<Cluster<T>> clusterMarkerCache = new MarkerCache<Cluster<T>>();

    MarkerManager() {
    }

    public void clear() {
        for(Marker marker : itemMarkerCache.clear()){
            marker.remove();
        }

        for(Marker marker : clusterMarkerCache.clear()){
            marker.remove();
        }
    }

    public void putItem(T item, Marker marker) {
        itemMarkerCache.put(item, marker);
    }

    public void putCluster(Cluster<T> cluster, Marker marker) {
        clusterMarkerCache.put(cluster, marker);
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
