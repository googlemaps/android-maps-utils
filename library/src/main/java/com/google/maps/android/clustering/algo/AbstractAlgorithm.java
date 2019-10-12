package com.google.maps.android.clustering.algo;

import com.google.maps.android.clustering.ClusterItem;

import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Base Algorithm class that implements lock/unlock functionality.
 */
public abstract class AbstractAlgorithm<T extends ClusterItem> implements Algorithm<T> {

    private final ReadWriteLock mLock = new ReentrantReadWriteLock();

    @Override
    public void lock() {
        mLock.writeLock().lock();
    }

    @Override
    public void unlock() {
        mLock.writeLock().unlock();
    }
}
