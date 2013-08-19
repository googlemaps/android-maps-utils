package com.google.maps.android.clustering.algo;

import android.annotation.TargetApi;
import android.os.Build;
import android.util.Log;
import android.util.LruCache;

import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterItem;

import java.util.Set;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

@TargetApi(Build.VERSION_CODES.HONEYCOMB_MR1)
public class PreCachingDecorator<T extends ClusterItem> implements Algorithm<T> {
    private final static String TAG = PreCachingDecorator.class.getName();
    private final Algorithm<T> mAlgorithm;
    private final LruCache<Integer, Set<? extends Cluster<T>>> mCache = new LruCache<Integer, Set<? extends Cluster<T>>>(5);
    private final ReadWriteLock mCacheLock = new ReentrantReadWriteLock();

    public PreCachingDecorator(Algorithm<T> algorithm) {
        mAlgorithm = algorithm;
    }

    public void addItem(T item) {
        mAlgorithm.addItem(item);
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
        new Thread(new PrecacheRunnable(discreteZoom + 1)).start();
        new Thread(new PrecacheRunnable(discreteZoom - 1)).start();
        return results;
    }

    public Set<? extends Cluster<T>> getClustersInternal(int discreteZoom) {
        Log.d(TAG, "getClusters: zoom " + discreteZoom);
        Set<? extends Cluster<T>> results;
        mCacheLock.readLock().lock();
        results = mCache.get(discreteZoom);
        mCacheLock.readLock().unlock();

        if (results == null) {
            mCacheLock.writeLock().lock();
            results = mCache.get(discreteZoom);
            if (results == null) {
                Log.d(TAG, "cache miss for zoom " + discreteZoom);
                results = mAlgorithm.getClusters(discreteZoom);
                mCache.put(discreteZoom, results);
            } else {
                Log.d(TAG, "cache hit for zoom " + discreteZoom);
            }
            mCacheLock.writeLock().unlock();
        } else {
            Log.d(TAG, "cache hit for zoom " + discreteZoom);
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
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                // ignore. keep going.
            }
            long start = System.currentTimeMillis();
            getClustersInternal(mZoom);
            Log.d(TAG, "pre-filling cache for zoom " + mZoom + " took " + (System.currentTimeMillis() - start));
        }
    }
}
