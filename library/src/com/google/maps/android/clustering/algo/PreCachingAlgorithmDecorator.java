package com.google.maps.android.clustering.algo;

import android.support.v4.util.LruCache;

import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterItem;

import java.util.Collection;
import java.util.Set;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Optimistically fetch clusters for adjacent zoom levels, caching them as necessary.
 */
public class PreCachingAlgorithmDecorator<T extends ClusterItem> implements Algorithm<T> {
    private final Algorithm<T> mAlgorithm;

    // TODO: evaluate maxSize parameter for LruCache.
    private final LruCache<Integer, Set<? extends Cluster<T>>> mCache = new LruCache<Integer, Set<? extends Cluster<T>>>(5);
    private final ReadWriteLock mCacheLock = new ReentrantReadWriteLock();

    public PreCachingAlgorithmDecorator(Algorithm<T> algorithm) {
        mAlgorithm = algorithm;
    }

    public void addItem(T item) {
        mAlgorithm.addItem(item);
        clearCache();
    }

    @Override
    public void addItems(Collection<T> items) {
        mAlgorithm.addItems(items);
        clearCache();
    }

    @Override
    public void clearItems() {
        mAlgorithm.clearItems();
        clearCache();
    }

    public void removeItem(T item) {
        mAlgorithm.removeItem(item);
        clearCache();
    }

    private void clearCache() {
        mCache.evictAll();
    }

    @Override
    public Set<? extends Cluster<T>> getClusters(double zoom) {
        int discreteZoom = (int) zoom;
        Set<? extends Cluster<T>> results = getClustersInternal(discreteZoom);
        // TODO: Check if requests are already in-flight.
        if (mCache.get(discreteZoom + 1) == null) {
            new Thread(new PrecacheRunnable(discreteZoom + 1)).start();
        }
        if (mCache.get(discreteZoom - 1) == null) {
            new Thread(new PrecacheRunnable(discreteZoom - 1)).start();
        }
        return results;
    }

    @Override
    public Collection<T> getItems() {
        return mAlgorithm.getItems();
    }

    private Set<? extends Cluster<T>> getClustersInternal(int discreteZoom) {
        Set<? extends Cluster<T>> results;
        mCacheLock.readLock().lock();
        results = mCache.get(discreteZoom);
        mCacheLock.readLock().unlock();

        if (results == null) {
            mCacheLock.writeLock().lock();
            results = mCache.get(discreteZoom);
            if (results == null) {
                results = mAlgorithm.getClusters(discreteZoom);
                mCache.put(discreteZoom, results);
            }
            mCacheLock.writeLock().unlock();
        }
        return results;
    }

    private class PrecacheRunnable implements Runnable {
        private final int mZoom;

        public PrecacheRunnable(int zoom) {
            mZoom = zoom;
        }

        @Override
        public void run() {
            try {
                // Wait between 500 - 1000 ms.
                Thread.sleep((long) (Math.random() * 500 + 500));
            } catch (InterruptedException e) {
                // ignore. keep going.
            }
            getClustersInternal(mZoom);
        }
    }
}
